package ekt.hack21.gr;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class GR_2_EN_Labels
{
    public LinkedHashSet<String> GR_Labels = new LinkedHashSet<String>();
    public HashMap<String, EKT_UNESCO_Label> EKT_UNESCO_OfOurInterest = new HashMap<String, EKT_UNESCO_Label>();//edw vazoume ta filtered fulla pou einai gia mapping. key: to lektiko tous sta ellhnika opws einai kai sto JSON
    private HashMap<String, Node> EKT_UNESCO_Nodes = new HashMap<String, Node>();//all Nodes in the XML. key: URI
    private String thisDirectory = new File("").getAbsolutePath();
    private Node ekt_unesco_root;

    public void getFilteredLabelsFromEKT_UNESCO() throws IOException, ParseException, SAXException, ParserConfigurationException
    {
        loadGreekJson();
        loadEKT_UNESCO_Nodes();
    }
    private void loadGreekJson() throws IOException, ParseException
    {
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_1.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_2.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_3.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_4.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_5.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH_TESTTT.json");
        //Reader readerSubset = new FileReader(thisDirectory+"/problematicTerms.json");
        Reader readerSubset = new FileReader(thisDirectory+"/3.greek_multidiscipline_SSH.json");
        JSONParser jsonParser = new JSONParser();
        JSONArray gr_labels = (JSONArray) jsonParser.parse(readerSubset);
        Iterator<String> iterator = gr_labels.iterator();
        while(iterator.hasNext())
            GR_Labels.add(iterator.next());
    }

    /**
     * Load first EKT_UNESCO_Nodes and then in the next iteration EKT_UNESCO_OfOurInterest. In the EKT_UNESCO_OfOurInterest we filter on-the-fly. Meaning we do not add if not included in the (already loaded) GR_Labels
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private void loadEKT_UNESCO_Nodes() throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document EKT_UNESCO_doc = db.parse(new File(thisDirectory+"/ekt-unesco.xml"));
        EKT_UNESCO_doc.getDocumentElement().normalize();
        Node root = EKT_UNESCO_doc.getDocumentElement();
        NodeList nodesList = root.getChildNodes();
        for (int i=0; i<nodesList.getLength(); i++)//first iteration
        {
            Node nodeXML = nodesList.item(i);
            if (nodeXML.getNodeName() != "skos:Concept")//we do not want the first XML element which is ConceptScheme
                continue;
            String uri = nodeXML.getAttributes().item(0).getNodeValue();
            EKT_UNESCO_Nodes.put(uri, nodeXML);
        }
        int countNotInterested=0;
        for (Map.Entry<String, Node> entry: EKT_UNESCO_Nodes.entrySet())//second iteration, selecting only the leaves and finding their full path from the root
        {
            Node node = entry.getValue();
            NodeList attr_nodes = node.getChildNodes();
            //boolean foundBroader = false;//skos:broader
            boolean foundNarrower = false;//skos:narrower
            String labelGr = null; String labelEn = null;
            Node attrNode = null;
            for (int i=0; i<attr_nodes.getLength(); i++)
            {
                String lang=null; String label=null;
                attrNode = attr_nodes.item(i);
                if (attrNode.getNodeName().equals("skos:narrower"))
                {
                    foundNarrower = true;
                    break;
                }
                else if (attrNode.getNodeName().equals("skos:prefLabel"))
                {
                    lang = attrNode.getAttributes().item(0).getNodeValue();
                    label = attrNode.getTextContent();
                    if (lang.equals("el"))
                        labelGr = label;
                    else
                        labelEn = label;
                }
            }

            if (!GR_Labels.contains(labelGr))//if not contained in the given list we are interested in, continue to the next one
            {
                countNotInterested++;
                //System.out.println("!! "+labelGr+" NOT of our interest.... movin on...");
                continue;
            }
            EKT_UNESCO_Label ekt_unesco_interesting = new EKT_UNESCO_Label(node, entry.getKey(), labelGr, labelEn);
            HashMap<String, String> gr_en_path = getLeafsPath(EKT_UNESCO_Nodes.get(entry.getKey()));
            ekt_unesco_interesting.path_gr = gr_en_path.get("el");
            ekt_unesco_interesting.path_en = gr_en_path.get("en");

            EKT_UNESCO_OfOurInterest.put(labelGr, ekt_unesco_interesting);
        }
        System.out.println("$$ We ignored "+countNotInterested+" EKT-UNESCO labels (leaves or not) $$");
    }

    private HashMap<String, String> getLeafsPath(Node leafNode)
    {
        HashMap<String, String> initialPath = getGR_ENPrefLabels(leafNode);
        //GR_EN_Path gr_en_path = new GR_EN_Path(initialGRPath, initialENPath);
        return getLeafsPathRec(leafNode, initialPath);
    }
    private HashMap<String, String> getLeafsPathRec(Node node, HashMap<String, String> gr_en_path)
    {
        NodeList attrNodes = node.getChildNodes();
        boolean foundBroader = false;
        Node parent = null;
        for (int i=0; i<attrNodes.getLength(); i++)
        {
            Node attrNode = attrNodes.item(i);
            if (attrNode.getNodeName().equals("skos:broader"))
            {
                parent = EKT_UNESCO_Nodes.get(attrNode.getAttributes().item(0).getNodeValue());
                HashMap<String, String> parentsLabels = getGR_ENPrefLabels(parent);
                gr_en_path.put("el", parentsLabels.get("el")+"/"+gr_en_path.get("el"));
                gr_en_path.put("en", parentsLabels.get("en")+"/"+gr_en_path.get("en"));
                foundBroader = true;
            }
        }
        if (foundBroader)
            return getLeafsPathRec(parent, gr_en_path);
        return gr_en_path;
    }
    private HashMap<String, String> getGR_ENPrefLabels(Node node)
    {
        HashMap<String, String> res = new HashMap<>();
        NodeList attrNodes = node.getChildNodes();
        String initialGRPath=""; String initialENPath="";
        for (int i=0; i<attrNodes.getLength(); i++)
        {
            if (attrNodes.item(i).getNodeName().equals("skos:prefLabel"))
            {
                String lang = attrNodes.item(i).getAttributes().item(0).getNodeValue();
                String label = attrNodes.item(i).getTextContent();
                if (lang.equals("el"))
                    res.put("el", label);
                else
                    res.put("en", label);;
            }
        }
        return res;
    }

    class GR_EN_Path
    {
        public String gr_path;
        public String en_path;
        public GR_EN_Path(String gr, String en)
        {
            this.gr_path=gr;
            this.en_path=en;
        }
    }

}

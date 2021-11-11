package ekt.hack21.gr;

import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XML_Generator
{
    private static String thisDirectory;
    private static final String LCSHSEARCH_1 = "https://id.loc.gov/search/?q=";
    private static final String LCSHSEARCH_2 = "&q=scheme:http://id.loc.gov/authorities/subjects&format=atom";
    private static GR_2_EN_Labels gr_2_en_labels;// = new GR_2_EN_Labels();

    public static void main(String[] args)
    {
        gr_2_en_labels = new GR_2_EN_Labels();
        thisDirectory = new File("").getAbsolutePath();
        try {
            gr_2_en_labels.getFilteredLabelsFromEKT_UNESCO();}
        catch (IOException | ParseException e) {e.printStackTrace();}
        catch (ParserConfigurationException e) {e.printStackTrace();}
        catch (SAXException e) {e.printStackTrace();}
        String XLSXOutput = thisDirectory+"/Mappings.xlsx";
        //String XLSXOutput = thisDirectory+"/MappingsSmall.xlsx";
        try {
            generateXML(XLSXOutput);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private static void generateXML(String xlsx) throws IOException, InvalidFormatException
    {
        FileInputStream inputStream = new FileInputStream(new File(xlsx));
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet spreadsheet = workbook.getSheetAt(0);
        HttpClient client = HttpClients.custom().build();
        Iterator<String> greekLabels = gr_2_en_labels.GR_Labels.iterator();
        int row_i=2;//start from the 3rd row
        while (greekLabels.hasNext())
        {
            EKT_UNESCO_Label nextUNESCOLabel = gr_2_en_labels.EKT_UNESCO_OfOurInterest.get(greekLabels.next());
            String nextEn = nextUNESCOLabel.label_en;

            client = HttpClients.custom().build();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("--Sending request for "+nextEn+".--");
            if (nextEn.contains("(software)"))
                nextEn="software";
            else if (nextEn.contains("(hardware)"))
                nextEn="hardware";
            if (nextEn.contains("("))
            {
                nextEn = nextEn.substring(0, nextEn.indexOf("(")).trim();
                System.out.println("--Cutting of the paranthesis: "+nextEn+".--");
            }
            nextUNESCOLabel.label_en=nextEn;
            try {
                getProposedTargets(client, nextUNESCOLabel, spreadsheet, row_i);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.err.println("*** Smthing went wrong when sending request to LCSH ***");
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            row_i++;
        }
        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream(xlsx);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private static void getProposedTargets(HttpClient client, EKT_UNESCO_Label UNESCO_label, Sheet spreadsheet, int rowNo) throws IOException, ParserConfigurationException, SAXException
    {
        //String uri = URLEncoder.encode(LCSHSEARCH_1+grLabel+LCSHSEARCH_2, StandardCharsets.UTF_8.toString());
        String uri = (LCSHSEARCH_1+UNESCO_label.label_en+LCSHSEARCH_2).replace(" ","%20");
        System.out.println(uri);
        Document doc = HttpStuff.sendHttpRequest(client, uri, "application/atom");
        NodeList entries = doc.getElementsByTagName("entry");
        System.out.println("Label '"+UNESCO_label.label_en+"' has "+entries.getLength()+" proposed mappings.");
        //System.out.println();
        int countProposedMaps = 0;
        Row nextRow = spreadsheet.createRow(rowNo);
        writeThreeCells(0, new String[]{uri,UNESCO_label.path_gr, UNESCO_label.path_en}, nextRow);
        while (countProposedMaps < entries.getLength() && countProposedMaps < 5)
        {
            Node entry = entries.item(countProposedMaps);
            ProposedTarget propTarg = getProposedTargetDetails(entry);
            //write it down to the Excel
            //System.out.println("WRITE DOWN:'"+propTarg.uri+"'-enaDyoEna'-"+propTarg.path);
            writeThreeCells(4*(countProposedMaps+1), new String[]{propTarg.uri, "", propTarg.path}, nextRow);

            countProposedMaps++;
        }


    }
    private static void writeThreeCells(int startCell, String[] cellValues, Row nextRow)
    {
        for (int i=0; i<cellValues.length; i++)
        {
            Cell cell = nextRow.createCell(startCell+i);
            cell.setCellValue(cellValues[i]);
        }
    }
    private static ProposedTarget getProposedTargetDetails(Node entry)
    {
        NodeList attrNodes = entry.getChildNodes();
        ProposedTarget propTarget = new ProposedTarget();
        String saveTargetTitle = null;
        for (int i=0; i<attrNodes.getLength(); i++)
        {
            if (attrNodes.item(i).getNodeName().equals("title"))
            {
                propTarget.title = attrNodes.item(i).getTextContent();
                saveTargetTitle = propTarget.title;
                propTarget.path = attrNodes.item(i).getTextContent();//for this level the title is the same as the path
            }
            else if (attrNodes.item(i).getNodeName().equals("link"))
            {
                NamedNodeMap attrs = attrNodes.item(i).getAttributes();
                Node attrNotNeeded = attrs.getNamedItem("type");
                if (attrNotNeeded == null)
                    propTarget.uri = attrs.getNamedItem("href").getNodeValue();
            }
        }
        //lets now find its path... swimming up to its parents...
        try {
            propTarget = swimUpRec(propTarget);
        }catch (NullPointerException eemaaa)
        {   System.err.println("#####################");
            System.err.println("#####################");
            System.err.println("Something went wrong... when swimming to the surface... Aborting...");
            System.err.println("#####################");
            System.err.println("#####################");}
        propTarget.title=saveTargetTitle;
        System.out.println(propTarget);
        return propTarget;
    }

    private static ProposedTarget swimUpRec(ProposedTarget propTarget)
    {
        HttpClient client = HttpClients.custom().build();
        Document proposedTargDoc = null;
        try {
            proposedTargDoc = HttpStuff.sendHttpRequest(client, propTarget.uri+".rdf", "application/xml");
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        //NodeList madsrdfTopicNode = proposedTargDoc.getFirstChild().getChildNodes();
        NodeList parents = proposedTargDoc.getElementsByTagName("skos:broader");
        if (parents !=null && parents.getLength()>0)
        {
            Node firstParent = parents.item(0);
            NodeList descriptionChildNodes = firstParent.getChildNodes();
            int j=0;
            for (j=0; j<descriptionChildNodes.getLength(); j++)
                if (descriptionChildNodes.item(j).getNodeName().equals("rdf:Description"))
                    break;
            //System.out.println("descriptionNode:"+descriptionChildNodes.item(j).getNodeName());
            NamedNodeMap parentsAttrs = descriptionChildNodes.item(j).getAttributes();
            String parentsUri = parentsAttrs.item(0).getNodeValue();
            ProposedTarget propParentTarget = new ProposedTarget(parentsUri);
            NodeList descriptionNodeChildren = descriptionChildNodes.item(j).getChildNodes();
            for (int jj=0; jj<descriptionNodeChildren.getLength(); jj++)
                if (descriptionNodeChildren.item(jj).getNodeName().equals("skos:prefLabel"))
                {
                    propParentTarget.title=descriptionNodeChildren.item(jj).getTextContent();
                    propParentTarget.path=propParentTarget.title+"/"+propTarget.path;
                }
            return swimUpRec(propParentTarget);
        }
        return propTarget;
    }


}

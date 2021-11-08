package ekt.hack21.gr;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedHashMap;

public class Hack21
{

    public static void main(String[] args)
    {
        ENLabels2Send2LCSH.gr_en_label = new LinkedHashMap<String, String>();
        try {
            ENLabels2Send2LCSH.getENLabels();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }

}

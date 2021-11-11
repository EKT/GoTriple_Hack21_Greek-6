package ekt.hack21.gr;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class HttpStuff
{
    public static Document sendHttpRequest(HttpClient client, String uri, String header) throws IOException, ParserConfigurationException, SAXException
    {
        HttpUriRequest request = RequestBuilder.get().setUri(uri)
                .setHeader(HttpHeaders.ACCEPT, header).build();
        HttpResponse response = null;
        try {
            response = client.execute(request);
        }catch(Exception whateva)
        {
            try {
                Thread.sleep(1000);
                response = client.execute(request);
            } catch (Exception e) {
                try {
                    Thread.sleep(10000);
                    response = client.execute(request);
                } catch (Exception eee) {
                    System.err.println("This request insists not to happen");
                    return null;
                }
            }
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(EntityUtils.toString(response.getEntity()))));
        doc.getDocumentElement().normalize();
        return doc;
    }
}

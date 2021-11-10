package ekt.hack21.gr;

import org.w3c.dom.Node;

import java.util.List;

public class EKT_UNESCO_Label
{
    public Node element;
    public String URI;
    public String path_gr;
    public String path_en;
    public String label_gr;
    public String label_en;
    //public List<ProposedTarget> mappings;


    public EKT_UNESCO_Label(Node el, String uri, /*String path_gr, String path_en,*/ String label_gr, String label_en)
    {
        this.element=el;
        this.URI=uri;
        //this.path_gr=path_gr;
        //this.path_en=path_en;
        this.label_gr=label_gr;
        this.label_en=label_en;
    }
}

package ekt.hack21.gr;

public class ProposedTarget
{
    public String uri;
    public String title;
    public String path;

    public ProposedTarget()
    {
        this.uri="";
        this.title="";
        this.path="";
    }

    public ProposedTarget(String uri)
    {
        this.uri=uri;
        //this.path=path;
    }

    public String toString()
    {
        return this.uri+" ## "+this.title+" ## "+this.path;
    }
}

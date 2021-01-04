package de.generator.nodes;

public class Child {
    private String tag;

    public Child(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "<!ELEMENT " + this.tag +" (#PCDATA)>";
    }
}

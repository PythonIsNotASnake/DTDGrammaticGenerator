package de.kejukedor.generator.nodes;

import java.util.ArrayList;
import java.util.List;

public class Parent {
    private String tag;
    private List<String> children;

    public Parent(String tag) {
        this.tag = tag;
        this.children = new ArrayList<>();
    }

    public String getTag() {
        return tag;
    }

    public List<String> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        String parent = "<!ELEMENT " + this.tag + " (";
        for (int i = 0; i < this.children.size(); i++) {
            if (i == 0) {
                parent = parent + children.get(i);
            } else {
                parent = parent + ", " + children.get(i);
            }
        }
        parent = parent + ")>";
        return parent;
    }
}

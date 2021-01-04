package de.generator.nodes;

import org.w3c.dom.*;

public class Root extends Parent {

    private Element element;

    public Root(String tag, Element element) {
        super(tag);
        this.element = element;
    }
}

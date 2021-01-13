package de.generator;

import de.generator.fileAccess.XMLReader;
import de.generator.nodes.Child;
import de.generator.nodes.Parent;
import de.generator.nodes.Root;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLToDTDAlgorithm {

    private Document xml;
    private DTD dtd;
    private XMLReader reader;

    public XMLToDTDAlgorithm() {
        reader = new XMLReader();
    }

    public DTD startAlgorithm() {
        this.xml = reader.read();
        this.dtd = new DTD(new Root(xml.getDocumentElement().getNodeName(), xml.getDocumentElement()));
        this.searchInParent(xml.getDocumentElement().cloneNode(true), dtd);
        return this.dtd;
    }

    private void searchInParent(Node node, DTD dtd) {
        // check if parent is already in dtd
        for (int parent = 0; parent < dtd.getParents().size(); parent++) {
            if (node.getNodeName() == dtd.getParents().get(parent).getTag()) {
                return;
            }
        }
        this.addParentToDTD(node, dtd);
    }

    private void addParentToDTD(Node node, DTD dtd) {
        NodeList childNodes = node.getChildNodes();
        Parent parent = new Parent(node.getNodeName());

        // create parent-element for DTD
        for (int childInNode = 0; childInNode < childNodes.getLength(); childInNode++) {
            if (childNodes.item(childInNode).getNodeName() != "#text") {
                if (parent.getChildren().size() < 1) {
                    parent.getChildren().add(childNodes.item(childInNode).getNodeName());
                } else {
                    for (int childInParent = 0; childInParent < parent.getChildren().size(); childInParent++) {
                        if (parent.getChildren().get(childInParent) == childNodes.item(childInNode).getNodeName()) {
                            parent.getChildren().set(childInParent, childNodes.item(childInNode).getNodeName() + "\u002B");
                            break;
                        } else if (parent.getChildren().get(childInParent).contains(childNodes.item(childInNode).getNodeName())) {
                            parent.getChildren().set(childInParent, childNodes.item(childInNode).getNodeName() + "\u002A");
                            break;
                        } else if ((childInParent + 1) == parent.getChildren().size()) {
                            parent.getChildren().add(childNodes.item(childInNode).getNodeName());
                            break;
                        }
                    }
                }
            }
        }
        dtd.getParents().add(parent);

        for (int childInNode = 0; childInNode < childNodes.getLength(); childInNode++) {
            if (childNodes.item(childInNode).getNodeName() != "#text") {
                NodeList tmpList = childNodes.item(childInNode).getChildNodes();
                // If greater than 1 then it is a parent
                if (tmpList.getLength() > 1) {
                    this.searchInParent(childNodes.item(childInNode), dtd);
                }
                // else it is a child
                else {
                    this.addChildToDTD(childNodes.item(childInNode), dtd);
                }
            }
        }
    }

    private void addChildToDTD(Node node, DTD dtd) {
        // Check if child exists in DTD
        for (int child = 0; child < dtd.getChildren().size(); child++) {
            if (node.getNodeName() == dtd.getChildren().get(child).getTag()) {
                return;
            }
        }
        Child child = new Child(node.getNodeName());
        dtd.getChildren().add(child);
    }
}

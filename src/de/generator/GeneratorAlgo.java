package de.generator;

import de.generator.nodes.Root;
import de.generator.fileAccess.XMLReader;
import de.generator.nodes.Child;
import de.generator.nodes.Parent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class GeneratorAlgo {

    private Document xmlA;
    private Document xmlB;
    private DTD dtdA;
    private DTD dtdB;

    public GeneratorAlgo() {
        XMLReader reader = new XMLReader();
        xmlA = reader.read();
        xmlB = reader.read();
        dtdA = new DTD(new Root(xmlA.getDocumentElement().getNodeName(), xmlA.getDocumentElement()));
        dtdB = new DTD(new Root(xmlB.getDocumentElement().getNodeName(), xmlB.getDocumentElement()));
    }

    public void searchInXML(Document xml, DTD dtd) {
        this.searchInParent(xml.getDocumentElement().cloneNode(true), dtd);
    }

    public void searchInParent(Node node, DTD dtd) {
        // check if parent is already in dtd
        for (int parent = 0; parent < dtd.getParents().size(); parent++) {
            if (node.getNodeName() == dtd.getParents().get(parent).getTag()) {
                return;
            }
        }
        this.addParentToDTD(node, dtd);
    }

    public void addParentToDTD(Node node, DTD dtd) {
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

    public DTD compareDTDs(DTD firstDTD, DTD secondDTD) {
        // compare roots
        if (firstDTD.getRoot().getTag() != secondDTD.getRoot().getTag()) {
            return null;
        }
        DTD combinedDTD = new DTD(firstDTD.getRoot());

        if (firstDTD.getParents().size() < 1 && secondDTD.getParents().size() < 1) {

        } else if (firstDTD.getParents().size() < 1) {
            for (int parentSecondDTD = 0; parentSecondDTD < secondDTD.getParents().size(); parentSecondDTD++) {
                for (int child = 0; child < secondDTD.getParents().get(parentSecondDTD).getChildren().size(); child++) {

                    if (secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).contains("\u002B")
                            || secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).contains("\u002A")) {
                        String tag = secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).substring(0, secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).length() - 1);
                        tag = tag + "\u002A";
                        secondDTD.getParents().get(parentSecondDTD).getChildren().set(child, tag);
                    } else {
                        secondDTD.getParents().get(parentSecondDTD).getChildren().set(child, secondDTD.getParents().get(parentSecondDTD).getChildren().get(child) + "\u003F");
                    }
                }
                combinedDTD.getParents().add(secondDTD.getParents().get(parentSecondDTD));
            }
        } else if (secondDTD.getParents().size() < 1) {
            for (int parentFirstDTD = 0; parentFirstDTD < firstDTD.getParents().size(); parentFirstDTD++) {
                for (int child = 0; child < firstDTD.getParents().get(parentFirstDTD).getChildren().size(); child++) {

                    if (firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002B")
                            || firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002A")) {
                        String tag = firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).substring(0, firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).length() - 1);
                        tag = tag + "\u002A";
                        firstDTD.getParents().get(parentFirstDTD).getChildren().set(child, tag);
                    } else {
                        firstDTD.getParents().get(parentFirstDTD).getChildren().set(child, firstDTD.getParents().get(parentFirstDTD).getChildren().get(child) + "\u003F");
                    }
                }
                combinedDTD.getParents().add(firstDTD.getParents().get(parentFirstDTD));
            }

        } else {
            List<Parent> doubleParentsFirst = new ArrayList<>();
            List<Parent> doubleParentsSecond = new ArrayList<>();
            for (int parentFirstDTD = 0; parentFirstDTD < firstDTD.getParents().size(); parentFirstDTD++) {
                for (int parentSecondDTD = 0; parentSecondDTD < secondDTD.getParents().size(); parentSecondDTD++) {
                    if (firstDTD.getParents().get(parentFirstDTD).getTag().contains(secondDTD.getParents().get(parentSecondDTD).getTag())
                            || secondDTD.getParents().get(parentSecondDTD).getTag().contains(firstDTD.getParents().get(parentFirstDTD).getTag())) {

                        doubleParentsFirst.add(firstDTD.getParents().get(parentFirstDTD));
                        doubleParentsSecond.add(secondDTD.getParents().get(parentSecondDTD));
                        break;
                    }
                }
            }

            // Insert every unique parent-node of firstDTD to combinedDTD
            for (int parentFirstDTD = 0; parentFirstDTD < firstDTD.getParents().size(); parentFirstDTD++) {
                for (int parentDouble = 0; parentDouble < doubleParentsFirst.size(); parentDouble++) {
                    if (firstDTD.getParents().get(parentFirstDTD).getTag().contains(doubleParentsFirst.get(parentDouble).getTag())
                            || doubleParentsFirst.get(parentDouble).getTag().contains(firstDTD.getParents().get(parentFirstDTD).getTag())) {
                        break;
                    } else if (parentDouble + 1 == doubleParentsFirst.size()) {
                        for (int child = 0; child < firstDTD.getParents().get(parentFirstDTD).getChildren().size(); child++) {
                            if (firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002B")
                                    || firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002A")) {
                                String tag = firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).substring(0, firstDTD.getParents().get(parentFirstDTD).getChildren().get(child).length() - 1);
                                tag = tag + "\u002A";
                                firstDTD.getParents().get(parentFirstDTD).getChildren().set(child, tag);
                            } else {
                                firstDTD.getParents().get(parentFirstDTD).getChildren().set(child, firstDTD.getParents().get(parentFirstDTD).getChildren().get(child) + "\u003F");
                            }
                        }
                        combinedDTD.getParents().add(firstDTD.getParents().get(parentFirstDTD));
                    }
                }
            }

            // Insert every unique parent-node of secondDTD to combinedDTD
            for (int parentSecondDTD = 0; parentSecondDTD < secondDTD.getParents().size(); parentSecondDTD++) {
                for (int parentDouble = 0; parentDouble < doubleParentsSecond.size(); parentDouble++) {
                    if (secondDTD.getParents().get(parentSecondDTD).getTag().contains(doubleParentsSecond.get(parentDouble).getTag())
                            || doubleParentsSecond.get(parentDouble).getTag().contains(secondDTD.getParents().get(parentSecondDTD).getTag())) {
                        break;
                    } else if (parentDouble + 1 == doubleParentsSecond.size()) {
                        for (int child = 0; child < secondDTD.getParents().get(parentSecondDTD).getChildren().size(); child++) {
                            if (secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).contains("\u002B")
                                    || secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).contains("\u002A")) {
                                String tag = secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).substring(0, secondDTD.getParents().get(parentSecondDTD).getChildren().get(child).length() - 1);
                                tag = tag + "\u002A";
                                secondDTD.getParents().get(parentSecondDTD).getChildren().set(child, tag);
                            } else {
                                secondDTD.getParents().get(parentSecondDTD).getChildren().set(child, secondDTD.getParents().get(parentSecondDTD).getChildren().get(child) + "\u003F");
                            }
                        }
                        combinedDTD.getParents().add(secondDTD.getParents().get(parentSecondDTD));
                    }
                }
            }

            // Compare multiple parent-nodes
            if (doubleParentsFirst.size() == doubleParentsSecond.size()) {
                for (int doubleParent = 0; doubleParent < doubleParentsFirst.size(); doubleParent++) {
                    if (doubleParentsFirst.get(doubleParent).getTag() == doubleParentsSecond.get(doubleParent).getTag()) {
                        //compare children of both parents
                        List<String> children = new ArrayList<>();

                        for (int childFirst = 0; childFirst < doubleParentsFirst.get(doubleParent).getChildren().size(); childFirst++) {
                            for (int childSecond = 0; childSecond < doubleParentsSecond.get(doubleParent).getChildren().size(); childSecond++) {

                                if (doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") && doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B")) {
                                    if (children.size() < 1) {
                                        children.add(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst));
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst)) || doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst));
                                                break;
                                            }
                                        }
                                    }
                                } else if (doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") || doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002A")
                                        || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B") || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002A")) {
                                    String tag = doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).substring(0, doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).length() - 1);
                                    tag = tag + "\u002A";

                                    if (children.size() < 1) {
                                        children.add(tag);
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(tag);
                                                break;
                                            }
                                        }
                                    }
                                } else if (!doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") && !doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002A")
                                        && !doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B") && !doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002A")) {
                                    if (children.size() < 1) {
                                        children.add(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst));
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst)) || doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(doubleParentsFirst.get(doubleParent).getChildren().get(childFirst));
                                                break;
                                            }
                                        }
                                    }
                                } else if (childSecond + 1 == doubleParentsSecond.get(doubleParent).getChildren().size()) {
                                    if (doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") || doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002A")) {
                                        String tag = doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).substring(0, doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).length() - 1);
                                        tag = tag + "\u002A";

                                        if (children.size() < 1) {
                                            children.add(tag);
                                        } else {
                                            for (int child = 0; child < children.size(); child++) {
                                                if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                    break;
                                                } else if (child + 1 == children.size()) {
                                                    children.add(tag);
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        String tag = doubleParentsFirst.get(doubleParent).getChildren().get(childFirst) + "\u003F";

                                        if (children.size() < 1) {
                                            children.add(tag);
                                        } else {
                                            for (int child = 0; child < children.size(); child++) {
                                                if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                    break;
                                                } else if (child + 1 == children.size()) {
                                                    children.add(tag);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        for (int childSecond = 0; childSecond < doubleParentsSecond.get(doubleParent).getChildren().size(); childSecond++) {
                            for (int childFirst = 0; childFirst < doubleParentsFirst.get(doubleParent).getChildren().size(); childFirst++) {

                                if (doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") && doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B")) {
                                    if (children.size() < 1) {
                                        children.add(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond));
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond)) || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond));
                                                break;
                                            }
                                        }
                                    }
                                } else if (doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") || doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002A")
                                        || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B") || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002A")) {
                                    String tag = doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).substring(0, doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).length() - 1);
                                    tag = tag + "\u002A";

                                    if (children.size() < 1) {
                                        children.add(tag);
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(tag);
                                                break;
                                            }
                                        }
                                    }
                                } else if (!doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002B") && !doubleParentsFirst.get(doubleParent).getChildren().get(childFirst).contains("\u002A")
                                        && !doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B") && !doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002A")) {
                                    if (children.size() < 1) {
                                        children.add(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond));
                                    } else {
                                        for (int child = 0; child < children.size(); child++) {
                                            if (children.get(child).contains(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond)) || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains(children.get(child))) {
                                                break;
                                            } else if (child + 1 == children.size()) {
                                                children.add(doubleParentsSecond.get(doubleParent).getChildren().get(childSecond));
                                                break;
                                            }
                                        }
                                    }
                                } else if (childFirst + 1 == doubleParentsFirst.get(doubleParent).getChildren().size()) {
                                    if (doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002B") || doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).contains("\u002A")) {
                                        String tag = doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).substring(0, doubleParentsSecond.get(doubleParent).getChildren().get(childSecond).length() - 1);
                                        tag = tag + "\u002A";

                                        if (children.size() < 1) {
                                            children.add(tag);
                                        } else {
                                            for (int child = 0; child < children.size(); child++) {
                                                if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                    break;
                                                } else if (child + 1 == children.size()) {
                                                    children.add(tag);
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        String tag = doubleParentsSecond.get(doubleParent).getChildren().get(childSecond) + "\u003F";
                                        if (children.size() < 1) {
                                            children.add(tag);
                                        } else {
                                            for (int child = 0; child < children.size(); child++) {
                                                if (children.get(child).contains(tag) || tag.contains(children.get(child))) {
                                                    break;
                                                } else if (child + 1 == children.size()) {
                                                    children.add(tag);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        Parent parent = new Parent(doubleParentsFirst.get(doubleParent).getTag());
                        for (int child = 0; child < children.size(); child++) {
                            parent.getChildren().add(children.get(child));
                        }
                        combinedDTD.getParents().add(parent);
                    }
                }
            }
        }

        // compare children
        for (int childFirstDTD = 0; childFirstDTD < firstDTD.getChildren().size(); childFirstDTD++) {
            combinedDTD.getChildren().add(firstDTD.getChildren().get(childFirstDTD));
        }

        for (int childSecondDTD = 0; childSecondDTD < secondDTD.getChildren().size(); childSecondDTD++) {
            for (int childCombinedDTD = 0; childCombinedDTD < combinedDTD.getChildren().size(); childCombinedDTD++) {
                if (combinedDTD.getChildren().get(childCombinedDTD).getTag() == secondDTD.getChildren().get(childSecondDTD).getTag()) {
                    break;
                } else if ((childCombinedDTD + 1) == combinedDTD.getChildren().size()) {
                    combinedDTD.getChildren().add(secondDTD.getChildren().get(childSecondDTD));
                }
            }
        }

        return combinedDTD;
    }

    public Document getXmlA() {
        return xmlA;
    }

    public Document getXmlB() {
        return xmlB;
    }

    public DTD getDtdA() {
        return dtdA;
    }

    public DTD getDtdB() {
        return dtdB;
    }
}

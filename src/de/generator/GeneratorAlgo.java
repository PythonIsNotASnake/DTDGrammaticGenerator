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
        if (!this.compareRoots(firstDTD, secondDTD)) {
            return null;
        }
        DTD combinedDTD = new DTD(firstDTD.getRoot());

        // In keiner DTD existieren Eltern-Knoten
        if (firstDTD.getParents().size() < 1 && secondDTD.getParents().size() < 1) {

            // Nur in der zweiten DTD existieren Eltern-Knoten
        } else if (firstDTD.getParents().size() < 1) {
            // Alle Eltern-Knoten in zweiter DTD werden hinzugefügt und Kindknoten auf Häufigkeit geprüft
            combinedDTD = this.addParentsOfOneDTD(secondDTD, combinedDTD);
        } else if (secondDTD.getParents().size() < 1) {
            combinedDTD = this.addParentsOfOneDTD(firstDTD, combinedDTD);
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
            combinedDTD = this.addUniqueParentsToCombinedDTD(firstDTD, doubleParentsFirst, combinedDTD);

            // Insert every unique parent-node of secondDTD to combinedDTD
            combinedDTD = this.addUniqueParentsToCombinedDTD(secondDTD, doubleParentsSecond, combinedDTD);

            // Compare multiple parent-nodes
            if (doubleParentsFirst.size() == doubleParentsSecond.size()) {
                for (int doubleParent = 0; doubleParent < doubleParentsFirst.size(); doubleParent++) {
                    if (doubleParentsFirst.get(doubleParent).getTag() == doubleParentsSecond.get(doubleParent).getTag()) {
                        //compare children of both parents
                        List<String> children = new ArrayList<>();
                        children = this.addMultipleParent(children, doubleParentsFirst, doubleParentsSecond, doubleParent);
                        children = this.addMultipleParent(children, doubleParentsSecond, doubleParentsFirst, doubleParent);

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
        combinedDTD.setChildren(this.combineChildren(firstDTD, secondDTD));

        return combinedDTD;
    }

    private boolean compareRoots(DTD firstDTD, DTD secondDTD) {
        if (firstDTD.getRoot().getTag() == secondDTD.getRoot().getTag()) {
            return true;
        } else {
            return false;
        }
    }

    private DTD addParentsOfOneDTD(DTD dtd, DTD combinedDTD) {
        //List<Parent> parents = new ArrayList<>();
        for (int parentDTD = 0; parentDTD < dtd.getParents().size(); parentDTD++) {
            for (int child = 0; child < dtd.getParents().get(parentDTD).getChildren().size(); child++) {

                if (dtd.getParents().get(parentDTD).getChildren().get(child).contains("\u002B")
                        || dtd.getParents().get(parentDTD).getChildren().get(child).contains("\u002A")) {
                    String tag = dtd.getParents().get(parentDTD).getChildren().get(child).substring(0, dtd.getParents().get(parentDTD).getChildren().get(child).length() - 1);
                    tag = tag + "\u002A";
                    dtd.getParents().get(parentDTD).getChildren().set(child, tag);
                } else {
                    dtd.getParents().get(parentDTD).getChildren().set(child, dtd.getParents().get(parentDTD).getChildren().get(child) + "\u003F");
                }
            }
            combinedDTD.getParents().add(dtd.getParents().get(parentDTD));
        }
        return combinedDTD;
    }

    private DTD addUniqueParentsToCombinedDTD(DTD dtdWithUniques, List<Parent> doubleParents, DTD combinedDTD) {

        for (int parentFirstDTD = 0; parentFirstDTD < dtdWithUniques.getParents().size(); parentFirstDTD++) {
            for (int parentDouble = 0; parentDouble < doubleParents.size(); parentDouble++) {
                if (dtdWithUniques.getParents().get(parentFirstDTD).getTag().contains(doubleParents.get(parentDouble).getTag())
                        || doubleParents.get(parentDouble).getTag().contains(dtdWithUniques.getParents().get(parentFirstDTD).getTag())) {
                    break;
                } else if (parentDouble + 1 == doubleParents.size()) {
                    for (int child = 0; child < dtdWithUniques.getParents().get(parentFirstDTD).getChildren().size(); child++) {
                        if (dtdWithUniques.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002B")
                                || dtdWithUniques.getParents().get(parentFirstDTD).getChildren().get(child).contains("\u002A")) {
                            String tag = dtdWithUniques.getParents().get(parentFirstDTD).getChildren().get(child).substring(0, dtdWithUniques.getParents().get(parentFirstDTD).getChildren().get(child).length() - 1);
                            tag = tag + "\u002A";
                            dtdWithUniques.getParents().get(parentFirstDTD).getChildren().set(child, tag);
                        } else {
                            dtdWithUniques.getParents().get(parentFirstDTD).getChildren().set(child, dtdWithUniques.getParents().get(parentFirstDTD).getChildren().get(child) + "\u003F");
                        }
                    }
                    combinedDTD.getParents().add(dtdWithUniques.getParents().get(parentFirstDTD));
                }
            }
        }


        return combinedDTD;
    }

    private List<String> addMultipleParent(List<String> children, List<Parent> doubleParentsFirst, List<Parent> doubleParentsSecond, int indexDoubleParent) {
        for (int childFirst = 0; childFirst < doubleParentsFirst.get(indexDoubleParent).getChildren().size(); childFirst++) {
            for (int childSecond = 0; childSecond < doubleParentsSecond.get(indexDoubleParent).getChildren().size(); childSecond++) {

                if (doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002B") && doubleParentsSecond.get(indexDoubleParent).getChildren().get(childSecond).contains("\u002B")) {
                    children = this.addChildExistsTwoTimes(children, childFirst, doubleParentsFirst, indexDoubleParent);

                } else if (doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002B") || doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002A")
                        || doubleParentsSecond.get(indexDoubleParent).getChildren().get(childSecond).contains("\u002B") || doubleParentsSecond.get(indexDoubleParent).getChildren().get(childSecond).contains("\u002A")) {
                    children = this.addChildExistsMultipleTimes(children, childFirst, doubleParentsFirst, indexDoubleParent);

                } else if (!doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002B") && !doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002A")
                        && !doubleParentsSecond.get(indexDoubleParent).getChildren().get(childSecond).contains("\u002B") && !doubleParentsSecond.get(indexDoubleParent).getChildren().get(childSecond).contains("\u002A")) {
                    children = this.addChildExistsOneTime(children, childFirst, doubleParentsFirst, indexDoubleParent);

                } else if (childSecond + 1 == doubleParentsSecond.get(indexDoubleParent).getChildren().size()) {
                    if (doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002B") || doubleParentsFirst.get(indexDoubleParent).getChildren().get(childFirst).contains("\u002A")) {
                        children = this.addLastChildWithSymbols(children, childFirst, doubleParentsFirst, indexDoubleParent);
                    } else {
                        children = this.addLastChildWithoutSymbols(children, childFirst, doubleParentsFirst, indexDoubleParent);
                    }
                    break;
                }
            }
        }
        return children;
    }

    private List<String> addChildExistsTwoTimes(List<String> children, int indexChild, List<Parent> doubleParents, int indexDoubles) {
        if (children.size() < 1) {
            children.add(doubleParents.get(indexDoubles).getChildren().get(indexChild));
        } else {
            for (int child = 0; child < children.size(); child++) {
                if (children.get(child).contains(doubleParents.get(indexDoubles).getChildren().get(indexChild)) || doubleParents.get(indexDoubles).getChildren().get(indexChild).contains(children.get(child))) {
                    break;
                } else if (child + 1 == children.size()) {
                    children.add(doubleParents.get(indexDoubles).getChildren().get(indexChild));
                    break;
                }
            }
        }
        return children;
    }

    private List<String> addChildExistsMultipleTimes(List<String> children, int indexChild, List<Parent> doubleParents, int indexDoubles) {
        String tag = doubleParents.get(indexDoubles).getChildren().get(indexChild).substring(0, doubleParents.get(indexDoubles).getChildren().get(indexChild).length() - 1);
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
        return children;
    }

    private List<String> addChildExistsOneTime(List<String> children, int indexChild, List<Parent> doubleParents, int indexDoubles) {
        if (children.size() < 1) {
            children.add(doubleParents.get(indexDoubles).getChildren().get(indexChild));
        } else {
            for (int child = 0; child < children.size(); child++) {
                if (children.get(child).contains(doubleParents.get(indexDoubles).getChildren().get(indexChild)) || doubleParents.get(indexDoubles).getChildren().get(indexChild).contains(children.get(child))) {
                    break;
                } else if (child + 1 == children.size()) {
                    children.add(doubleParents.get(indexDoubles).getChildren().get(indexChild));
                    break;
                }
            }
        }
        return children;
    }

    private List<String> addLastChildWithSymbols(List<String> children, int indexChild, List<Parent> doubleParents, int indexDoubles) {
        String tag = doubleParents.get(indexDoubles).getChildren().get(indexChild).substring(0, doubleParents.get(indexDoubles).getChildren().get(indexChild).length() - 1);
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
        return children;
    }

    private List<String> addLastChildWithoutSymbols(List<String> children, int indexChild, List<Parent> doubleParents, int indexDoubles) {
        String tag = doubleParents.get(indexDoubles).getChildren().get(indexChild) + "\u003F";

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
        return children;
    }

    private List<Child> combineChildren(DTD firstDTD, DTD secondDTD) {
        List<Child> children = new ArrayList<>();

        for (int childFirstDTD = 0; childFirstDTD < firstDTD.getChildren().size(); childFirstDTD++) {
            children.add(firstDTD.getChildren().get(childFirstDTD));
        }

        for (int childSecondDTD = 0; childSecondDTD < secondDTD.getChildren().size(); childSecondDTD++) {
            for (int childCombinedDTD = 0; childCombinedDTD < children.size(); childCombinedDTD++) {
                if (children.get(childCombinedDTD).getTag() == secondDTD.getChildren().get(childSecondDTD).getTag()) {
                    break;
                } else if ((childCombinedDTD + 1) == children.size()) {
                    children.add(secondDTD.getChildren().get(childSecondDTD));
                }
            }
        }

        return children;
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

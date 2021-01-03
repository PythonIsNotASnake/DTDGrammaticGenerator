package de.kejukedor.generator;

import de.kejukedor.generator.nodes.Child;
import de.kejukedor.generator.nodes.Parent;
import de.kejukedor.generator.nodes.Root;

import java.util.ArrayList;
import java.util.List;

public class DTD {
    private Root root;
    private List<Parent> parents;
    private List<Child> children;

    public DTD(Root root) {
        this.root = root;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public Root getRoot() {
        return root;
    }

    public List<Parent> getParents() {
        return parents;
    }

    public List<Child> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        String dtd = "";
        for (int parent = 0; parent < this.parents.size(); parent++) {
            if (parent == 0) {
                dtd = dtd + parents.get(parent);
            } else {
                dtd = dtd + "\n" + parents.get(parent);
            }
        }
        for (int child = 0; child < this.children.size(); child++) {
            dtd = dtd + "\n" + children.get(child);
        }

        return dtd;
    }
}

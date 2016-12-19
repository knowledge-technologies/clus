package clus.ext.hierarchicalmtr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanja Mileski on 12/16/2016.
 */
public class ClassHMTRNode {

    private boolean isRoot;
    private String name;
    private List<ClassHMTRNode> children;

    public String getName() {
        return name;
    }

    public List<ClassHMTRNode> getChildren() {
        return children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public void addChild(ClassHMTRNode child) {
        this.children.add(child);
    }

    public boolean hasChildren(){
        return this.getChildren().size()>0;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public ClassHMTRNode(boolean isRoot, String name) {
        this.isRoot = isRoot;
        this.name = name;
        this.children = new ArrayList<ClassHMTRNode>();
    }

    public ClassHMTRNode(String name) {
        this.isRoot = false;
        this.name = name;
        this.children = new ArrayList<ClassHMTRNode>();
    }

    public String printNodeAndChildren(){

        String childrenConcat = "";
        int i = 1;
        for (ClassHMTRNode child : this.getChildren()) {
            childrenConcat+="Child "+i+": \""+child.getName()+"\" ";
            i++;
        }

        return "Node name: \"" + this.getName() +"\" "+ childrenConcat;
    }

}

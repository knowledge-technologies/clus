package clus.ext.hierarchicalmtr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanja Mileski on 12/16/2016.
 */
public class ClusHMTRNode implements Serializable{

    private boolean isRoot;
    private String name;
    private boolean isAggregate;

    public boolean isAggregate() {
        return isAggregate;
    }

    public void setAggregate(boolean aggregate) {
        isAggregate = aggregate;
    }

    private List<ClusHMTRNode> children;

    public String getName() {
        return name;
    }

    public List<ClusHMTRNode> getChildren() {
        return children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public void addChild(ClusHMTRNode child) {
        this.children.add(child);
    }

    public boolean hasChildren(){
        return this.getChildren().size()>0;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public ClusHMTRNode(boolean isRoot, String name) {
        this.isRoot = isRoot;
        this.name = name;
        this.children = new ArrayList<ClusHMTRNode>();
        this.isAggregate = false;
    }

    public ClusHMTRNode(String name) {
        this.isRoot = false;
        this.name = name;
        this.children = new ArrayList<ClusHMTRNode>();
        this.isAggregate = false;
    }

    public String printNodeAndChildren(){

        String childrenConcat = "";
        int i = 1;
        for (ClusHMTRNode child : this.getChildren()) {
            childrenConcat+="Child "+i+": \""+child.getName()+"\" ";
            i++;
        }

        return "Node name: \"" + this.getName() +"\" "+ childrenConcat;
    }

}

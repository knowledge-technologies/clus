package clus.ext.hierarchicalmtr;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Vanja Mileski on 12/15/2016.
 */
public class ClassHMTRHierarchy {

    private String hierarchyName;
    private List<ClassHMTRNode> nodes;

    public List<ClassHMTRNode> getNodes() {
        return nodes;
    }

    private void addNode(ClassHMTRNode node){
        nodes.add(node);
    }

    private ClassHMTRNode getNode(String nodeName){

        for (ClassHMTRNode node : this.getNodes()) {
            if (node.getName().equals(nodeName)) return node;
        }
        return null;

    }

    private boolean nodeExists(String name){

        for (ClassHMTRNode node : this.getNodes()) {
            if (node.getName().equals(name)) return true;
        }
        return false;
    }

    public void createHMTRHierarchy(String hier){

        hier = hier.replace("(","");
        hier = hier.replace(")","");
        String[] relationships = hier.split(",");

        boolean root = true;

        for (String relationship : relationships) {
            String[] pcr = relationship.split("->");

            if(root == true) this.nodes.add(new ClassHMTRNode(true, pcr[0]));
            root = false;


            if (!nodeExists(pcr[0])) this.nodes.add(new ClassHMTRNode(pcr[0]));
            if (!nodeExists(pcr[1])) this.nodes.add(new ClassHMTRNode(pcr[1]));

            getNode(pcr[0]).addChild(getNode(pcr[1]));

        }
    }

    public List<ClassHMTRNode> getParents(ClassHMTRNode node){

        List<ClassHMTRNode>  parents = new ArrayList<>();

        for (ClassHMTRNode aNode: this.nodes ) {

            if (aNode.getChildren().contains(node))
                parents.add(aNode);
        }
        return parents;
    }

    public void printHierarchy(){

        System.out.println("Hiearchy: ");

        for (ClassHMTRNode node : nodes) {
            System.out.println(node.printNodeAndChildren());

            List<ClassHMTRNode> parents = getParents(node);

            System.out.print("Node parents: ");
            for (ClassHMTRNode parent : parents) {
                System.out.print("\""+parent.getName() +"\" ");
            }
            System.out.println();
    ;
        }

        System.out.println();
    }

    public ClassHMTRHierarchy(String hierarchyName) {
        this.hierarchyName = hierarchyName;
        this.nodes = new ArrayList<ClassHMTRNode>();
    }
}
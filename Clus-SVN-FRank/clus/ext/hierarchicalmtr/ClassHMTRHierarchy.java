package clus.ext.hierarchicalmtr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Vanja Mileski on 12/15/2016.
 */
public class ClassHMTRHierarchy {

    private String hierarchyName;
    private List<ClassHMTRNode> nodes;

    private Map<String, Integer> nodeDepth;

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

    public void calculateDepth(){

        this.nodeDepth = new HashMap<>();

        for (ClassHMTRNode node : this.nodes){
            this.nodeDepth.put(node.getName(),getNodeDepth(node.getName()));
        }
    }

    public void printDepth(){

        for (Map.Entry<String, Integer> entry : this.nodeDepth.entrySet()){

            System.out.println("Attribute: " + entry.getKey() + ", depth: " + entry.getValue());

        }

    }

    public boolean nodeExists(String name){

        for (ClassHMTRNode node : this.getNodes()) {
            if (node.getName().equals(name)) return true;
        }
        return false;
    }

    public void createHMTRHierarchy(String hier){

        hier = hier.replace("(","");
        hier = hier.replace(")","");
        hier = hier.replace(" ","");
        hier = hier.replace(">","");
        String[] relationships = hier.split(",");

        boolean root = true;

        for (String relationship : relationships) {
            String[] pcr = relationship.split("-");

            if(root == true) this.nodes.add(new ClassHMTRNode(true, pcr[0]));
            root = false;


            if (!nodeExists(pcr[0])) this.nodes.add(new ClassHMTRNode(pcr[0]));
            if (!nodeExists(pcr[1])) this.nodes.add(new ClassHMTRNode(pcr[1]));

            getNode(pcr[0]).addChild(getNode(pcr[1]));

        }
        calculateDepth();
    }

    public List<ClassHMTRNode> getParents(ClassHMTRNode node){

        List<ClassHMTRNode>  parents = new ArrayList<>();

        for (ClassHMTRNode aNode: this.nodes ) {

            if (aNode.getChildren().contains(node))
                parents.add(aNode);
        }
        return parents;
    }

    public boolean hasParents(ClassHMTRNode node){

        return getParents(node).size()>0;

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

    public String printHierarchyTree(ClassHMTRNode node) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printHierarchyTree(node, indent, sb);
        return sb.toString();
    }

    public void printHierarchyTree(){

        for (ClassHMTRNode n : this.getNodes()){

            if (n.isRoot()) {
                System.out.println(printHierarchyTree(n));
                break;
            }

        }

    }

    private void printHierarchyTree(ClassHMTRNode node, int indent, StringBuilder sb) {

        sb.append(getIndentString(indent));
        if (hasParents(node)) sb.append("└──");
        sb.append(node.getName());
        sb.append("\n");
        for (ClassHMTRNode n : node.getChildren()) {

            printHierarchyTree(n, indent + 1, sb);

        }

    }

    private String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < indent; i++) {
            sb.append("·     ");
        }
        return sb.toString();
    }


    private int getNodeDepth(String nodeName){
        return getNodeDepth(nodeName,0);
    }

    private int getNodeDepth(String nodeName, int currentDepth){

        ClassHMTRNode node = getNode(nodeName);

        if (node.isRoot()) return currentDepth;

        List<ClassHMTRNode>  parents = getParents(node);

        int depth = 2147483647;
        for (ClassHMTRNode parent : parents){

            int parDepth = getNodeDepth(parent.getName(), currentDepth+1);
            if (parDepth<depth) depth = parDepth;
        }
        if (depth == 2147483647) try {
            throw new IOException("Depth for "+node.getName()+" was not calculated!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return depth;
    }


    public ClassHMTRHierarchy(String hierarchyName) {
        this.hierarchyName = hierarchyName;
        this.nodes = new ArrayList<ClassHMTRNode>();
    }
}
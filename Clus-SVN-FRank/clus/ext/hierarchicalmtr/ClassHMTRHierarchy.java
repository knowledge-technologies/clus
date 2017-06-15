package clus.ext.hierarchicalmtr;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Vanja Mileski on 12/15/2016.
 */
public class ClassHMTRHierarchy implements Serializable{


    private static boolean IS_HMTR_HIER_CREATED = false;
    private static boolean IS_USING_DUMP = false;
    private String m_HierarchyName;
    private List<ClassHMTRNode> m_Nodes;
    private Map<String, Integer> m_NodeDepth;
    private Map<String, Double> m_NodeWeights;

	private String getVerticalLineText() {
		//return "·" + getSpaces();
		return "\u00B7" + getSpaces();
	}
	
	private String getHorizontalLineText() {
	    String corner="\u2514";
	    String dash="\u2500";
	    return corner+dash+dash + " ";
	}

    public static boolean isUsingDump() {
        return IS_USING_DUMP;
    }
    
    private String getSpaces(int howmany) {
		StringBuilder sb = new StringBuilder(howmany);
		for(int i=0;i<howmany;i++){
			sb.append(" ");
		}
		return sb.toString();
	}
	private String getSpaces() {
		return getSpaces(5);
	}

    public static boolean isHmtrHierCreated() {
        return IS_HMTR_HIER_CREATED;
    }

    public static void setIsHmtrHierCreated(boolean isHmtrHierCreated) {
        IS_HMTR_HIER_CREATED = isHmtrHierCreated;
    }


    public static void setIsUsingDump(boolean isUsingDump) {
        IS_USING_DUMP = isUsingDump;
    }

    public List<ClassHMTRNode> getNodes() {
        return m_Nodes;
    }

    private void addNode(ClassHMTRNode node){
        m_Nodes.add(node);
    }

    private ClassHMTRNode getNode(String nodeName){

        for (ClassHMTRNode node : this.getNodes()) {
            if (node.getName().equals(nodeName)) return node;
        }
        return null;

    }

    public void calculateDepth(){

        this.m_NodeDepth = new HashMap<String, Integer>();

        for (ClassHMTRNode node : this.m_Nodes){
            this.m_NodeDepth.put(node.getName(),getNodeDepth(node.getName()));
        }
    }

    public void printDepth(){

        for (Map.Entry<String, Integer> entry : this.m_NodeDepth.entrySet()){

            System.out.println("Attribute: " + entry.getKey() + ", depth: " + entry.getValue());

        }

    }

    public void printWeights(){

        for (Map.Entry<String, Double> entry : this.m_NodeWeights.entrySet()){

            System.out.println("Attribute: " + entry.getKey() + ", weight: " + entry.getValue());

        }

    }

    public boolean nodeExists(String name){

        for (ClassHMTRNode node : this.getNodes()) {
            if (node.getName().equals(name)) return true;
        }
        return false;
    }

    public void createHMTRHierarchy(String hier, double weight){

        hier = hier.replace("(","");
        hier = hier.replace(")","");
        hier = hier.replace(" ","");
        hier = hier.replace(">","");
        String[] relationships = hier.split(",");

        boolean root = true;

        for (String relationship : relationships) {
            String[] pcr = relationship.split("-");

            if(root == true) this.m_Nodes.add(new ClassHMTRNode(true, pcr[0]));
            root = false;


            if (!nodeExists(pcr[0])) this.m_Nodes.add(new ClassHMTRNode(pcr[0]));
            if (!nodeExists(pcr[1])) this.m_Nodes.add(new ClassHMTRNode(pcr[1]));

            getNode(pcr[0]).addChild(getNode(pcr[1]));

        }
        calculateDepth();
        calculateWeights(weight);
        setIsHmtrHierCreated(true);
    }

    private void calculateWeights(double weight) {

        if (weight > 1 || weight  < 0.1) System.err.println("Weird initialisation of HMTR weight! Weight = "+weight+"\nTypical weights: 0.75, 0.8333 etc.\nWeight 1 = pure MTR (not taking the hierarchy into account)\nSmaller values = more influence on the upper levels of the hierarchy\nProgram will continue anyways...");

        this.m_NodeWeights = new HashMap<String, Double>();

        for (Map.Entry<String, Integer> entry : this.m_NodeDepth.entrySet()){

            this.m_NodeWeights.put(entry.getKey(),Math.pow(weight, entry.getValue()));

        }

    }

    public double getWeight(String name) {
        return m_NodeWeights.get(name).doubleValue();
    }


    public List<ClassHMTRNode> getParents(ClassHMTRNode node){

        List<ClassHMTRNode>  parents = new ArrayList<ClassHMTRNode>();

        for (ClassHMTRNode aNode: this.m_Nodes ) {

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

        for (ClassHMTRNode node : m_Nodes) {
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

    public String printHierarchyTree(){

        for (ClassHMTRNode n : this.getNodes()){

            if (n.isRoot()) {
                return printHierarchyTree(n);
            }

        }
        return "";

    }


    private void printHierarchyTree(ClassHMTRNode node, int indent, StringBuilder sb) {

        sb.append(getIndentString(indent));
        if (hasParents(node)) sb.append(getHorizontalLineText());
        sb.append(node.getName());
        sb.append("\n");
        for (ClassHMTRNode n : node.getChildren()) {

            printHierarchyTree(n, indent + 1, sb);

        }

    }

    private String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < indent; i++) {
            //sb.append("·     ");
        	sb.append(getVerticalLineText());
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
        this.m_HierarchyName = hierarchyName;
        this.m_Nodes = new ArrayList<ClassHMTRNode>();
    }
}
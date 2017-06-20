
package clus.ext.hierarchicalmtr;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import clus.main.Settings;


/**
 * Created by Vanja Mileski on 12/15/2016.
 */
public class ClusHMTRHierarchy  { //implements Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    //private boolean m_IsHMTRHierarchyCreated = false;
    private boolean m_IsUsingDump = false;
    private String m_HierarchyName;
    private List<ClusHMTRNode> m_Nodes;
    private Map<String, Integer> m_NodeDepth;
    private Map<String, Double> m_NodeWeights;


    public ClusHMTRHierarchy(String hierarchyName) {
        this.m_HierarchyName = hierarchyName;
        this.m_Nodes = new ArrayList<ClusHMTRNode>();
    }

    public ClusHMTRHierarchy() {
        this ("default");
    }

    public boolean isUsingDump() {
        return m_IsUsingDump;
    }

    public void setIsUsingDump(boolean isUsingDump) {
        m_IsUsingDump = isUsingDump;
    }

    public List<ClusHMTRNode> getNodes() {
        return m_Nodes;
    }

    private void calculateDepth() {

        this.m_NodeDepth = new HashMap<String, Integer>();

        for (ClusHMTRNode node : this.m_Nodes) {
            this.m_NodeDepth.put(node.getName(), getNodeDepth(node.getName()));
        }
    }


    public void printDepth() {
        for (Map.Entry<String, Integer> entry : this.m_NodeDepth.entrySet()) {
            System.out.println("Attribute: " + entry.getKey() + ", depth: " + entry.getValue());
        }
    }


    public void printWeights() {
        for (Map.Entry<String, Double> entry : this.m_NodeWeights.entrySet()) {
            System.out.println("Attribute: " + entry.getKey() + ", weight: " + entry.getValue());
        }
    }


    public void initialize(Settings sett) {
        String hier = sett.getHMTRHierarchyString().getStringValue();
        double weight = sett.getHMTRHierarchyWeight().getValue();
        
        hier = hier.replace("(", "")
                .replace(")", "")
                .replace(" ", "")
                .replace(">", "");
        
        String[] relationships = hier.split(",");

        boolean root = true;

        for (String relationship : relationships) {
            String[] pcr = relationship.split("-");

            if (root == true)
                m_Nodes.add(new ClusHMTRNode(true, pcr[0]));
            root = false;

            if (!nodeExists(pcr[0]))
                m_Nodes.add(new ClusHMTRNode(pcr[0]));
            if (!nodeExists(pcr[1]))
                m_Nodes.add(new ClusHMTRNode(pcr[1]));

            getNode(pcr[0]).addChild(getNode(pcr[1]));

        }
        calculateDepth();
        calculateWeights(weight);
        
        sett.setSectionHMTREnabled(true);
    }


    public double getWeight(String name) {
        return m_NodeWeights.get(name).doubleValue();
    }


    public List<ClusHMTRNode> getParents(ClusHMTRNode node) {

        List<ClusHMTRNode> parents = new ArrayList<ClusHMTRNode>();

        for (ClusHMTRNode aNode : this.m_Nodes) {

            if (aNode.getChildren().contains(node))
                parents.add(aNode);
        }
        return parents;
    }


    public boolean hasParents(ClusHMTRNode node) {

        return getParents(node).size() > 0;

    }


    public void printHierarchy() {

        System.out.println("Hiearchy: ");

        for (ClusHMTRNode node : m_Nodes) {
            System.out.println(node.printNodeAndChildren());

            List<ClusHMTRNode> parents = getParents(node);

            System.out.print("Node parents: ");
            for (ClusHMTRNode parent : parents) {
                System.out.print("\"" + parent.getName() + "\" ");
            }
            System.out.println();
            ;
        }

        System.out.println();
    }


    public String printHierarchyTree(ClusHMTRNode node) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printHierarchyTree(node, indent, sb);
        return sb.toString();
    }


    public String printHierarchyTree() {

        for (ClusHMTRNode n : this.getNodes()) {

            if (n.isRoot()) { return printHierarchyTree(n); }

        }
        return "";
    }


    private void printHierarchyTree(ClusHMTRNode node, int indent, StringBuilder sb) {

        sb.append(getIndentString(indent));
        if (hasParents(node))
            sb.append(getHorizontalLineText());
        sb.append(node.getName());
        sb.append(System.lineSeparator());
        for (ClusHMTRNode n : node.getChildren()) {
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


    private int getNodeDepth(String nodeName) {
        return getNodeDepth(nodeName, 0);
    }


    private int getNodeDepth(String nodeName, int currentDepth) {

        ClusHMTRNode node = getNode(nodeName);

        if (node.isRoot())
            return currentDepth;

        List<ClusHMTRNode> parents = getParents(node);

        int depth = 2147483647;
        for (ClusHMTRNode parent : parents) {

            int parDepth = getNodeDepth(parent.getName(), currentDepth + 1);
            if (parDepth < depth)
                depth = parDepth;
        }
        if (depth == 2147483647)
            try {
                throw new IOException("Depth for " + node.getName() + " was not calculated!");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        return depth;
    }


    //    private void addNode(ClassHMTRNode node){
    //        m_Nodes.add(node);
    //    }

    private ClusHMTRNode getNode(String nodeName) {
        for (ClusHMTRNode node : this.getNodes()) {
            if (node.getName().equals(nodeName))
                return node;
        }
        return null;
    }


    private boolean nodeExists(String name) {

        for (ClusHMTRNode node : this.getNodes()) {
            if (node.getName().equals(name))
                return true;
        }
        return false;
    }


    private void calculateWeights(double weight) {

        if (weight > 1 || weight < 0.1)
            System.err.println("Weird initialisation of HMTR weight! Weight = " + weight + "\nTypical weights: 0.75, 0.8333 etc.\nWeight 1 = pure MTR (not taking the hierarchy into account)\nSmaller values = more influence on the upper levels of the hierarchy\nProgram will continue anyways...");

        this.m_NodeWeights = new HashMap<String, Double>();

        for (Map.Entry<String, Integer> entry : this.m_NodeDepth.entrySet()) {

            this.m_NodeWeights.put(entry.getKey(), Math.pow(weight, entry.getValue()));

        }

    }


    private String getVerticalLineText() {
        //return "·" + getSpaces();
        return "\u00B7" + getSpaces();
    }


    private String getHorizontalLineText() {
        String corner = "\u2514";
        String dash = "\u2500";
        return corner + dash + dash + " ";
    }


    private String getSpaces(int howmany) {
        StringBuilder sb = new StringBuilder(howmany);
        for (int i = 0; i < howmany; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }


    private String getSpaces() {
        return getSpaces(5);
    }

}

package si.ijs.kt.clus.ext.ensemble.container;

import si.ijs.kt.clus.algo.tdidt.ClusNode;


public class NodeDepthPair {

    private ClusNode m_Node;
    private double m_Depth;


    public NodeDepthPair(ClusNode node, double depth) {
        m_Node = node;
        m_Depth = depth;
    }


    public ClusNode getNode() {
        return m_Node;
    }


    public double getDepth() {
        return m_Depth;
    }


    @Override
    public String toString() {
        return "(" + m_Node.toString() + ", " + m_Depth + ")";

    }

}

/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package si.ijs.kt.clus.ext.optiontree;

import java.io.PrintWriter;
import java.io.Serializable;

import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;

public abstract class MyNode implements Node, Serializable, ClusModel {

    public final static long serialVersionUID = 1;

    protected MyArray m_Children = new MyArray();
    protected MyNode m_Parent;
    
    public MyNode() {
    }

    public MyNode(MyNode parent) {
        m_Parent = parent;
    }

    public final MyNode getRoot() {
        if (m_Parent == null) return this;
        else return m_Parent.getRoot();
    }

    public final int indexOf(MyNode child) {
        for (int i = 0; i < getNbChildren(); i++) {
            if (child == getChild(i)) return i;
        }
        return -1;
    }

    public ClusStatistic m_ClusteringStat;
    public ClusStatistic m_TargetStat;
    public ClusStatManager m_StatManager;
    
    public final ClusStatistic getClusteringStat() {
        return m_ClusteringStat;
    }

    public final ClusStatistic getTargetStat() {
        return m_TargetStat;
    }
    
    public final void setStatManager(ClusStatManager smgr) {
    	m_StatManager = smgr;
    }
    
    public final void initTargetStat(ClusStatManager smgr, RowData subset) throws ClusException {
        m_TargetStat = smgr.createTargetStat();
        subset.calcTotalStatBitVector(m_TargetStat);
    }

    public final void initClusteringStat(ClusStatManager smgr, RowData subset) throws ClusException {
        m_ClusteringStat = smgr.createClusteringStat();
        subset.calcTotalStatBitVector(m_ClusteringStat);
    }

    public final void initTargetStat(ClusStatManager smgr, ClusStatistic train, RowData subset) throws ClusException {
        m_TargetStat = smgr.createTargetStat();
        m_TargetStat.setTrainingStat(train);
        subset.calcTotalStatBitVector(m_TargetStat);
    }

    public final void initClusteringStat(ClusStatManager smgr, ClusStatistic train, RowData subset) throws ClusException {
        m_ClusteringStat = smgr.createClusteringStat();
        m_ClusteringStat.setTrainingStat(train);
        subset.calcTotalStatBitVector(m_ClusteringStat);
    }
    
//    public boolean equalsPath(int[] path) {
//        int[] mypath = getPath();
//        if (mypath.length != path.length) return false;
//        for (int i = 0; i < path.length; i++)
//            if (mypath[i] != path[i]) return false;
//        return true;
//    }
//
//    public static void showPath(int[] path, PrintWriter out) {
//        for (int i = 0; i < path.length; i++) {
//            if (i != 0) out.print(",");
//            out.print(path[i]);
//        }
//    }
//
//    public static void showPath(int[] path) {
//        for (int i = 0; i < path.length; i++) {
//            if (i != 0) System.out.print(",");
//            System.out.print(path[i]);
//        }
//    }

    public abstract MyNode cloneNode();

    public final void addChild(MyNode node) {
        node.setParent(this);
        m_Children.addElement(node);
    }

    public final void setChild(MyNode node, int idx) {
        node.setParent(this);
        m_Children.setElementAt(node, idx);
    }

    public final void removeChild(MyNode node) {
        node.setParent(null);
        m_Children.removeElement(node);
    }

    public final void removeChild(int idx) {
        MyNode child = getChild(idx);
        if (child != null) child.setParent(null);
        m_Children.removeElementAt(idx);
    }

    public final void removeAllChildren() {
        int nb = getNbChildren();
        for (int i = 0; i < nb; i++) {
            MyNode node = getChild(i);
            node.setParent(null);
        }
        m_Children.removeAllElements();
    }

    @Override
    public final MyNode getParent() {
        return m_Parent;
    }

    public final void setParent(MyNode parent) {
        m_Parent = parent;
    }

    @Override
    public final MyNode getChild(int idx) {
        return (MyNode) m_Children.elementAt(idx);
    }
    
    public final int getChildIndex(MyNode node) {
        for (int i = 0; i < getNbChildren(); i++) {
            if (m_Children.elementAt(i).equals(node)) return i;
        }
        return -1;
    }
    
    public final void setChild(int idx, MyNode node) {
        node.setParent(this);
        m_Children.setElementAt(node, idx);
    }

    @Override
    public final int getNbChildren() {
        return m_Children.size();
    }

    public final void setNbChildren(int nb) {
        m_Children.setSize(nb);
    }

    @Override
    public final boolean atTopLevel() {
        return m_Parent == null;
    }

    @Override
    public final boolean atBottomLevel() {
        return m_Children.size() == 0;
    }

    public final MyNode cloneTree() {
        MyNode clone = cloneNode();
        int arity = getNbChildren();
        clone.setNbChildren(arity);
        for (int i = 0; i < arity; i++) {
            MyNode node = getChild(i);
            clone.setChild(node.cloneTree(), i);
        }
        return clone;
    }

    public final MyNode cloneTree(MyNode n1, MyNode n2) {
        if (n1 == this) {
            return n2;
        } else {
            MyNode clone = cloneNode();
            int arity = getNbChildren();
            clone.setNbChildren(arity);
            for (int i = 0; i < arity; i++) {
                MyNode node = getChild(i);
                clone.setChild(node.cloneTree(n1, n2), i);
            }
            return clone;
        }
    }

    public final void setClusteringStat(ClusStatistic stat) {
        m_ClusteringStat = stat;
    }

    public final void setTargetStat(ClusStatistic stat) {
        m_TargetStat = stat;
    }

    public void printTree(PrintWriter writer, StatisticPrintInfo info,
            String string, RowData examples) {
      
    }

    public void printTreeInDatabase(PrintWriter writer, String[] tabitem,
            int[] tabexist, int cpt, String typetree) {
        // TODO Auto-generated method stub
        
    }
    
//  --- THIS WILL BE IMPLEMENTED FOR EACH NODE TYPE SEPARATELY ---
//    public final int getLevel() {
//        int depth = 0;
//        Node node = getParent();
//        while (node != null) {
//            depth++;
//            node = node.getParent();
//        }
//        return depth;
//    }

//  --- THIS WILL BE IMPLEMENTED FOR EACH NODE TYPE SEPARATELY ---
//    public final int getMaxLeafDepth() {
//        int nb = getNbChildren();
//        if (nb == 0) {
//            return 1;
//        } else {
//            int max = 0;
//            for (int i = 0; i < nb; i++) {
//                MyNode node = (MyNode)getChild(i);
//                max = Math.max(max, node.getMaxLeafDepth());
//            }
//            return max + 1;
//        }
//    }

//  --- THIS WILL BE IMPLEMENTED FOR EACH NODE TYPE SEPARATELY ---
//    public final int getNbNodes() {
//        int count = 1;
//        int nb = getNbChildren();
//        for (int i = 0; i < nb; i++) {
//            MyNode node = (MyNode)getChild(i);
//            count += node.getNbNodes();
//        }
//        return count;
//    }

//  --- THIS WILL BE IMPLEMENTED FOR EACH NODE TYPE SEPARATELY ---
//    public final int getNbLeaves() {
//        int nb = getNbChildren();
//        if (nb == 0) {
//            return 1;
//        } else {
//            int count = 0;
//            for (int i = 0; i < nb; i++) {
//                MyNode node = (MyNode)getChild(i);
//                count += node.getNbLeaves();
//            }
//            return count;
//        }
//    }

    @Override
    public int getNbNodes() {
        int count = 1;
        int nb = getNbChildren();
        for (int i = 0; i < nb; i++) {
            MyNode node = getChild(i);
            count += node.getNbNodes();
        }
        return count;
    }
    
    @Override
    public int getNbLeaves() {
        int nb = getNbChildren();
        if (nb == 0) {
            return 1;
        } else {
            int count = 0;
            for (int i = 0; i < nb; i++) {
                MyNode node = getChild(i);
                count += node.getNbLeaves();
            }
            return count;
        }
    }
    
    public int getNbOptionNodes() {
    	int nb = getNbChildren();
        if (nb == 0) {
        	// Really shoudn't happen
            return 0;
        } else {
        	int count = 0;
        	for (int i = 0; i < nb; i++) {
                MyNode node = getChild(i);
                count += node.getNbOptionNodes();
            }
            return count + (this instanceof ClusOptionNode ? 1 : 0);
        }
    }

    public int getNbOptions() {
    	int nb = getNbChildren();
        if (nb == 0) {
        	// Really shoudn't happen
            return 0;
        } else {
        	int count = 0;
        	for (int i = 0; i < nb; i++) {
                MyNode node = getChild(i);
                count += node.getNbOptions();
            }
            return count + (this instanceof ClusOptionNode ? this.getNbChildren() : 0);
        }
    }
    
    public int getNbTrees() {
    	int nb = getNbChildren();
    	if (nb == 0) {
    		return 1;
    	} else {
    		int count = 0;
    		if (this instanceof ClusSplitNode) {
    			count = 1;
    			for (int i = 0; i < nb; i++) {
    				MyNode node = getChild(i);
    				count *= node.getNbTrees();
    			}
    		} else if (this instanceof ClusOptionNode) {
    			for (int i = 0; i < nb; i++) {
    				MyNode node = getChild(i);
    				count += node.getNbTrees();
    			}

    		}
    		return count;
    	}
    }
    
    @Override
    public String getModelInfo() {
        return "Nodes = "+getNbNodes()+" (Leaves: "+getNbLeaves()+", OptionNodes: "+getNbOptionNodes()+", EmbededTrees: "+getNbTrees()+")";
    }
}

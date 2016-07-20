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

package clus.algo.kNN.methods.kdTree;

import clus.algo.kNN.distance.SearchDistance; 
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.main.ClusRun;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author Mitja Pugelj
 */
public class KDNode{

	public KDNode m_LeftSubTree;//Left subtree.
	public KDNode m_RightSubTree;//Right subtree.
	private KDTree m_Tree;//Reference to tree containing this node.
	protected int m_Level = 0;//Level in tree on which node is present. Root has level = 0.
	// private DataTuple m_SplittingNode = null;//Splitting value at this node.
	private ClusAttrType m_Attr;//Attribute on which splitting is performed.
	private static DataTuple m_Tuple = null;//Tuple for which nearest neighbors are searched.
	// private static int m_Count = 0;
	private double m_Median;
	protected LinkedList<DataTuple> m_Tuples;//All tuples belonging to this node.If node is internal, then tuples is empty.If tree is build without pruning, then leaves contain only one tuple.

	/**
	 * @param level Level in tree on which node is present. Root has level = 0.
	 * @param tree Reference to tree containing this node.
	 */
	public KDNode(KDTree tree, int level){
		m_LeftSubTree = null;
		m_RightSubTree = null;
		m_Tree = tree;
		m_Level = level;
		try{
			ClusAttrType[] tmp = m_Tree.getRun().getDataSet(ClusRun.TRAINSET).m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
			m_Attr = tmp[m_Level % tmp.length];
		}catch( Exception e){ e.printStackTrace(); }
	}

	/**
	 * Set tuples belonging to this node.
	 * @param tuples
	 */
	public void setTuples(LinkedList<DataTuple> tuples){
		m_Tuples = tuples;
	}

	/**
	 * Recursive split space on two subspaces along selected axis.
	 */
	protected void build(int limitRepetitions){
		try {
			if (m_Tuples.size() <= m_Tree.getMaxTuples() || limitRepetitions >= m_Tree.getRun().getDataSet(ClusRun.TRAINSET).m_Schema.getNbDescriptiveAttributes()) {
				// Leaf reached.
			} else {
				m_Median = this.calcMedian(m_Tuples, m_Attr);
				// Partition tuples in two subsets according to selected split (mean) value.
				LinkedList<DataTuple> leftTuples = new LinkedList<DataTuple>();
				LinkedList<DataTuple> rightTuples = new LinkedList<DataTuple>();
				for (DataTuple t : m_Tuples) {
					if (m_Tree.getDistance().getValue(t, m_Attr) < m_Median ) {
						leftTuples.add(t);
					} else {
						rightTuples.add(t);
					}
				}
				if (leftTuples.size() == 0 || rightTuples.size() == 0) {
					limitRepetitions++;
				}
				// Generate children and build subtree.
				m_LeftSubTree = new KDNode(m_Tree, m_Level + 1);
				m_LeftSubTree.setTuples(leftTuples);
				m_LeftSubTree.build(limitRepetitions);

				m_RightSubTree = new KDNode(m_Tree, m_Level + 1);
				m_RightSubTree.setTuples(rightTuples);
				m_RightSubTree.build(limitRepetitions);
			}
		} catch (Exception ex) { ex.printStackTrace();
		}
	}

	/**
	 * If node is an leaf in tree, function returns true, false otherwise.
	 * @return
	 */
	public boolean isLeaf(){
		return m_LeftSubTree == null && m_RightSubTree == null;
	}

	/**
	 * Find k (tree.getK) nearest neighbors for specified tuple. Be aware that
	 * this method should not be executed twice at the same time because it
	 * stores searched tuple statically in KDNode.
	 * @param tuple
	 * @return
	 */
	public void find(DataTuple tuple){
		KDNode.m_Tuple = tuple;
		find();
	}

	/**
	 * Private recursive function for finding kNN.
	 */
	private void find(){
		KDTree.operationsCount[KDTree.ALG_KD]++;
		// If node is leaf?
		if( this.isLeaf() ){
			for( DataTuple t : m_Tuples )
				m_Tree.getStack().addToStack(t, m_Tree.getDistance().calcDistance(t, m_Tuple));
		}else{
			// does tuple belong to rightSubspace?
			boolean rightSubspace = m_Tree.getDistance().getValue(m_Tuple, m_Attr) > m_Median;
			if( rightSubspace )	m_RightSubTree.find();
			else	m_LeftSubTree.find();

			// search neighbor subspace for better candidates
			KDNode neighbour = rightSubspace ? m_LeftSubTree : m_RightSubTree;
			if( neighbour.m_Tuples.size() > 0 ){
				if( !m_Tree.getStack().enoughNeighbours() )
					// not enough candidates so far, they should be added
					neighbour.find();
				else{
					double compDistance = m_Tree.getStack().getWorstNearestDistance();
					if( rightSubspace ){
						if( m_Tree.getDistance().getValue(m_Tuple, m_Attr) - compDistance < m_Median )
							neighbour.find();
					}else{
						if( m_Tree.getDistance().getValue(m_Tuple, m_Attr) + compDistance > m_Median )
							neighbour.find();
					}
				}
			}
		}
	}

	private double calcMedian(LinkedList<DataTuple> list, ClusAttrType attr){
		double median = 0;
		for( DataTuple t : list)
			median += m_Tree.getDistance().getValue(t, attr);
		return median / list.size();
	}

	/**
	 * Returns list of all tuples belonging to this node.
	 * @return
	 */
	public LinkedList<DataTuple> getTuples(){
		return m_Tuples;
	}

	/**
	 * Returns level on which this node is situated.
	 * @return
	 */
	public int getLevel(){
		return m_Level;
	}

	public void output(){
		for( int i = 0; i < m_Level; i++)
			System.out.print("\t");
		for( DataTuple tuple : m_Tuples )
			System.out.print(tuple.hashCode()+"; ");
		System.out.println("");
		if( !this.isLeaf() ){
			m_LeftSubTree.output();
			m_RightSubTree.output();
		}
	}
}

class SortByAxis implements Comparator{

	private ClusAttrType m_AttrSort;
	private SearchDistance m_DistanceSort;

	public SortByAxis(ClusAttrType attr, SearchDistance distance){
		m_AttrSort = attr;
		m_DistanceSort = distance;
	}

	public int compare(Object arg0, Object arg1) {
		DataTuple a = (DataTuple) arg0;
		DataTuple b = (DataTuple) arg1;
		return (int) Math.signum(m_DistanceSort.getValue(a, m_AttrSort) - m_DistanceSort.getValue(b, m_AttrSort));
	}

}



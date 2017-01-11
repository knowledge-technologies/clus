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

package clus.algo.kNN.methods.vpTree;

import clus.algo.kNN.methods.NNStack; 
import clus.algo.kNN.methods.SearchAlgorithm;
import clus.algo.kNN.distance.SearchDistance;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.main.ClusRun;
import clus.util.ClusException;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.commons.math.random.RandomData;//this is not with the CLUS spirit...
import org.apache.commons.math.random.RandomDataImpl;

/**
 * @author Mitja Pugelj
 */
public class VPTree extends SearchAlgorithm {

	private VPNode m_Root;
	private double m_Tau = 100; // todo: should be maximum distance
	private NNStack m_Stack;

	private RandomData m_Random = new RandomDataImpl();
	private int m_NbNeighbors;

	public VPTree(ClusRun run, SearchDistance dist){
		super(run,dist);

	}

	public void build() throws ClusException, IOException {
		RowData data = getRun().getDataSet(ClusRun.TRAINSET);
		LinkedList<VPItem> list = new LinkedList<VPItem>();
		for( DataTuple tuple : data.m_Data )
			list.add(new VPItem(tuple));
		m_Root = recursiveBuild(list);;
	}

	/**
	 * Recursively builds vp-tree.
	 * @param list list of all points in space.
	 * @return
	 */
	private VPNode recursiveBuild(LinkedList<VPItem> list){
		if( list.isEmpty() )
			return null;
		VPNode node;
		if( list.size() <= 2 ){
			node = new VPNode(list.poll());
		}else{
			/*
			 * Search for appropriate vantage point and create new node.
			 */
			node = new VPNode(this.selectVPItem(list));
			list.remove(node.getVPItem());
		}

		/*
		 * Calculate distances from vantage point to elements in space.
		 */
		//TestKnnModel.watches.get("vpB").pause();
		for( VPItem item : list )
			item.setItemsHistory(getDistance().calcDistance(node.getVPItem().getTuple(), item.getTuple()));
		//TestKnnModel.watches.get("vpB").start();
		// Calculate median
		node.setMedian(this.getMedian(list));
		if( list.size() > 0 ){
			/*
			 * Split values according to median.
			 */
			LinkedList<VPItem> leftList = new LinkedList<VPItem>();
			LinkedList<VPItem> rightList = new LinkedList<VPItem>();

			for( VPItem item : list ){
				if( item.getItemsHistory() < node.getMedian() ){
					leftList.add(item);
					node.getBounds()[VPNode.LEFT_LOW] = Math.min(node.getBounds()[VPNode.LEFT_LOW],item.getItemsHistory());
					node.getBounds()[VPNode.LEFT_HIGH] = Math.max(node.getBounds()[VPNode.LEFT_HIGH],item.getItemsHistory());
				}else{
					rightList.add(item);
					node.getBounds()[VPNode.RIGHT_LOW] = Math.min(node.getBounds()[VPNode.RIGHT_LOW],item.getItemsHistory());
					node.getBounds()[VPNode.RIGHT_HIGH] = Math.max(node.getBounds()[VPNode.RIGHT_HIGH],item.getItemsHistory());
				}
			}
			list.clear();
			//Recursively build sub-tree.
			node.setLeftSubtree(this.recursiveBuild(leftList));
			node.setRightSubtree(this.recursiveBuild(rightList));
		}
		// return root of sub-tree
		return node;
	}


	/**
	 * Selects appropriate vantage point among all points in list.
	 * @param list candidates for vantage points - elements in space
	 * @return
	 */
	private VPItem selectVPItem(LinkedList<VPItem> list){
		int sampleSize = (int) Math.max(Math.min(list.size(),3),0.1*list.size());
		int testSize = (int) Math.max(Math.min(list.size(),2),0.08*list.size());

		// Generate a random sample of candidate vantage points.
		Object[] sample = m_Random.nextSample(list, sampleSize);
		VPItem vantagePoint = null;
		double bestSpread = -1;

		// For all vantage point candidates, calculate their quality in means of wider spread.
		for( Object a : sample ){
			VPItem item = (VPItem) a;
			Object[] testSample = m_Random.nextSample(list, testSize);
			// TestKnnModel.watches.get("vpB").pause();
			double median = this.calcMedian(testSample, item);
			double variance = this.calcVariance(testSample, item, median);
			// TestKnnModel.watches.get("vpB").start();
			if( variance <= bestSpread ) continue;
			bestSpread = variance;
			vantagePoint = item;
		}
		return vantagePoint;
	}

	/**
	 * Calculate median for distances to vantage point. Distances should be computed
	 * and stored in history of each item before calling this method.
	 * @param list elements in space
	 * @return
	 */
	private double getMedian(LinkedList<VPItem> list){
		double median = 0;
		for( VPItem item : list ){
			median += item.getItemsHistory();
		}
		return median / list.size();
	}

	private double calcMedian(Object[] list, VPItem vp){
		double median = 0;
		for( Object item : list)
			median += ((VPItem) item).getItemsHistory();
		return median / list.length;
	}

	private double calcVariance(Object[] list, VPItem vp, double median){
		double variance = 0;
		for( Object item : list)
			variance += Math.pow(((VPItem) item).getItemsHistory()-median,2);
		return variance / list.length;
	}

	public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) {
		m_NbNeighbors = k;
		m_Tau = Double.MAX_VALUE;
		m_Stack = new NNStack(m_NbNeighbors);
		search(m_Root,tuple);
		return m_Stack.returnStack();
	}

	private void search(VPNode n, DataTuple q){
		if( n == null )
			return;
		VPTree.operationsCount[VPTree.ALG_VP]++;
		double x = getDistance().calcDistance(n.getVPItem().getTuple(), q);
		m_Stack.addToStack(n.getVPItem().getTuple(), x);
		m_Tau = m_Stack.getWorstNearestDistance();
		double middle = (n.getBounds()[VPNode.LEFT_HIGH]+n.getBounds()[VPNode.RIGHT_LOW])/2;
		if( x < middle ){
			if( x <= n.getBounds()[VPNode.LEFT_HIGH]+m_Tau && n.getLeftSubtree() != null )
				this.search(n.getLeftSubtree(), q);
			if( x >= n.getBounds()[VPNode.RIGHT_LOW]-m_Tau && n.getRightSubtree() != null )
				this.search(n.getRightSubtree(), q);
		}else{
			if( x >= n.getBounds()[VPNode.RIGHT_LOW]-m_Tau && n.getRightSubtree() != null )
				this.search(n.getRightSubtree(), q);
			if( x <= n.getBounds()[VPNode.LEFT_HIGH]+m_Tau && n.getLeftSubtree() != null )
				this.search(n.getLeftSubtree(), q);
		}
	}
}

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

/**
 *
 * @author Mitja Pugelj
 */
public class VPNode {
	protected static final int LEFT_LOW = 0;
	protected static final int LEFT_HIGH = 1;
	protected static final int RIGHT_LOW = 2;
	protected static final int RIGHT_HIGH = 3;

	protected VPItem m_VPitem;//Vantage point for this node.
	protected VPNode m_LeftSubTree;//Left subtree. Null if leaf.
	protected VPNode m_RightSubTree;//Right subtree. Null if leaf.
	protected double m_Median = 0.5;
	protected double[] m_Bounds;//Bounds for subspaces as seen by vantage point on this level.

	/**
	 * Constructs new VPNode.
	 * @param vp vantage point for this node
	 */
	public VPNode(VPItem vp){
		m_VPitem = vp;
		m_Bounds = new double[4];
		m_Bounds[LEFT_LOW] = m_Bounds[RIGHT_LOW] = Double.MAX_VALUE;
		m_Bounds[LEFT_HIGH] = m_Bounds[RIGHT_HIGH] = Double.MIN_VALUE;
	}

	public double[] getBounds(){
		return m_Bounds;
	}

	public VPItem getVPItem(){
		return m_VPitem;
	}

	public double getMedian(){
		return m_Median;
	}

	public void setMedian(double value){
		m_Median = value;
	}

	public VPNode getLeftSubtree(){
		return m_LeftSubTree;
	}

	public VPNode getRightSubtree(){
		return m_RightSubTree;
	}

	public void setLeftSubtree(VPNode node){
		m_LeftSubTree = node;
	}

	public void setRightSubtree(VPNode node){
		m_RightSubTree = node;
	}

}

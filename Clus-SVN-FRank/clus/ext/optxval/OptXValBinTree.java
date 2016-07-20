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

package clus.ext.optxval;

import jeans.tree.*;
import jeans.util.*;

import clus.algo.tdidt.ClusNode;
import clus.main.*;

public class OptXValBinTree extends MyNode {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_NbTests;
	protected int m_NbFoldTests;
	protected long m_Time;

 	public double[] getFIs() {
		double[] arr = new double[getMaxLeafDepth()];
		getFIs(arr, 0);
		return arr;
	}

	private void getFIs(double[] arr, int depth) {
		if (m_NbTests > 0) arr[depth] += (double)m_NbFoldTests/m_NbTests;
		for (int j = 0; j < getNbChildren(); j++) {
			OptXValBinTree tree = (OptXValBinTree)getChild(j);
			tree.getFIs(arr, depth+1);
		}
	}

 	public double[] getNodes() {
		double[] arr = new double[getMaxLeafDepth()];
		getNodes(arr, 0);
		return arr;
	}

	private void getNodes(double[] arr, int depth) {
		if (m_NbTests > 0) arr[depth]++;
		for (int j = 0; j < getNbChildren(); j++) {
			OptXValBinTree tree = (OptXValBinTree)getChild(j);
			tree.getNodes(arr, depth+1);
		}
	}

 	public double[] getTimes() {
		double[] arr = new double[getMaxLeafDepth()];
		getTimes(arr, 0);
		return arr;
	}

	private void getTimes(double[] arr, int depth) {
		arr[depth] += (double)m_Time;
		for (int j = 0; j < getNbChildren(); j++) {
			OptXValBinTree tree = (OptXValBinTree)getChild(j);
			tree.getTimes(arr, depth+1);
		}
	}

	public static void processs(MyArray nodes, MyArray left, MyArray right, OptXValBinTree tree) {
		for (int i = 0; i < nodes.size(); i++) {
			Object obj = nodes.elementAt(i);
			if (obj instanceof ClusNode) {
				ClusNode node = (ClusNode)obj;
				tree.m_Time += node.m_Time;
				if (node.getNbChildren() > 0) {
					left.addElement(node.getChild(0));
					right.addElement(node.getChild(1));

					tree.m_NbTests++;
					tree.m_NbFoldTests++;
				}
			} else {
				OptXValNode node = (OptXValNode)obj;
				tree.m_Time += node.m_Time;
				ClusNode[] cnodes = node.getNodes();
				for (int j = 0; j < cnodes.length; j++) {
					tree.m_Time += cnodes[j].m_Time;
					if (cnodes[j].getNbChildren() > 0) {
						Object n1 = cnodes[j].getChild(0);
						Object n2 = cnodes[j].getChild(1);
						if (n1 != null) left.addElement(n1);
						if (n2 != null) right.addElement(n2);
					}
				}
				for (int j = 0; j < node.getNbChildren(); j++) {
					OptXValSplit split = (OptXValSplit)node.getChild(j);
					if (split.getNbChildren() > 0) {
						left.addElement(split.getChild(0));
						right.addElement(split.getChild(1));

						tree.m_NbTests++;
						tree.m_NbFoldTests += split.getFolds().length;
					}
				}
			}
		}
	}

	public static OptXValBinTree convertTree(MyArray crnodes) {
		OptXValBinTree tree = new OptXValBinTree();
		if (crnodes.size() != 0) {
			tree.setNbChildren(2);
			MyArray left  = new MyArray();
			MyArray right = new MyArray();
			processs(crnodes, left, right, tree);
			tree.setChild(convertTree(left), 0);
			tree.setChild(convertTree(right), 1);
		}
		return tree;
	}

	public static OptXValBinTree convertTree(Object object) {
		MyArray arr = new MyArray();
		arr.addElement(object);
		return convertTree(arr);
	}
}

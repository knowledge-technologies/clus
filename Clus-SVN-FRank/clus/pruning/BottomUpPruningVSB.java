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

package clus.pruning;

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.error.*;
import clus.statistic.*;

public class BottomUpPruningVSB extends PruneTree {

	protected ClusError m_TreeErr;
	protected ClusError m_NodeErr;
	protected RowData m_Data;

	public BottomUpPruningVSB(ClusErrorList parent, RowData data) {
		m_TreeErr = parent.getFirstError();
		m_NodeErr = m_TreeErr.getErrorClone(parent);
		m_Data = data;
	}

	public int getNbResults() {
		return 1;
	}

	public void prune(ClusNode node) {
		prune(node, m_Data);
	}

	public void prune(ClusNode node, RowData data) {
		if (!node.atBottomLevel()) {
			int arity = node.getNbChildren();
			for (int i = 0; i < arity; i++) {
				RowData subset = data.applyWeighted(node.getTest(), i);
				prune((ClusNode)node.getChild(i), subset);
			}
			int nbrows = data.getNbRows();
			if (nbrows > 0) {
				m_TreeErr.reset();
				m_NodeErr.reset();
				m_TreeErr.getParent().setNbExamples(0);
				for (int i = 0; i < nbrows; i++) {
					DataTuple tuple = data.getTuple(i);
					ClusStatistic pred = node.predictWeighted(tuple);
					m_TreeErr.addExample(tuple, pred);
					m_NodeErr.addExample(tuple, node.predictWeightedLeaf(tuple));
//					System.out.println("Val: "+tuple.m_Doubles[0]+","+node.getTotalStat().getNumericPred()[0]);
				}
				// FIXME - error should count this :-)
				m_TreeErr.getParent().setNbExamples(nbrows);
				double tree_err = m_TreeErr.getModelError();
				double node_err = m_NodeErr.getModelError();
				//System.out.println("Tree Accuracy: "+tree_err);
				//System.out.println("Node Accuracy: "+node_err);
				if (m_TreeErr.shouldBeLow()) {
					if (tree_err > node_err) node.makeLeaf();
				} else {
					if (tree_err <= node_err) node.makeLeaf();
				}
			} else {
				node.makeLeaf();
			}
		}
	}

}

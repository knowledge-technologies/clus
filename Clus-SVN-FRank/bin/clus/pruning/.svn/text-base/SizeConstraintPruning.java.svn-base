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

import clus.model.test.*;
import clus.algo.tdidt.*;
import clus.data.attweights.*;
import clus.data.rows.*;
import clus.error.*;
import clus.util.*;

public class SizeConstraintPruning extends PruneTree {

	public RowData m_Data;
	public double[] m_MaxError;
	public ClusErrorList m_ErrorMeasure;
	public ClusErrorList m_AdditiveError;
	public int[] m_MaxSize;
	public ClusAttributeWeights m_TargetWeights;
	public int m_CrIndex, m_MaxIndex;

	public SizeConstraintPruning(int maxsize, ClusAttributeWeights prod) {
		m_MaxSize = new int[1];
		m_MaxSize[0] = maxsize;
		m_TargetWeights = prod;
	}

	public SizeConstraintPruning(int[] maxsize, ClusAttributeWeights prod) {
		m_MaxSize = maxsize;
		m_TargetWeights = prod;
	}

	public int getMaxSize() {
		return m_MaxSize[0];
	}

	public void setTrainingData(RowData data) {
		m_Data = data;
	}

	public ClusAttributeWeights getTargetWeights() {
		return m_TargetWeights;
	}

	public void pruneInitialize(ClusNode node, int size) {
		recursiveInitialize(node, size);
		// compute error for each internal node and leaf
		if (isUsingAdditiveError()) {
			recursiveInitializeError(node, m_Data);
		} else {
			recursiveInitializeErrorFromStatistic(node);
		}
	}

	public void pruneExecute(ClusNode node, int size) {
		computeCosts(node, size);
		pruneToSizeK(node, size);
	}

	public void prune(ClusNode node) throws ClusException {
		prune(0, node);
	}

	public int getNbResults() {
		return Math.max(1, m_MaxSize.length);
	}

	public String getPrunedName(int i) {
		return "S("+m_MaxSize[i]+")";
	}

	public void prune(int result, ClusNode node) throws ClusException {
		if (m_MaxError == null) {
			int size = m_MaxSize[result];
			int orig = node.getNbNodes();
			System.out.println("Pruning to size ("+orig+"): "+size);
			pruneInitialize(node, size);
			pruneExecute(node, size);
		} else {
			if (m_MaxSize.length == 0) {
				pruneMaxError(node, node.getNbNodes());
			} else {
				pruneMaxError(node, m_MaxSize[result]);
			}
		}
		node.clearVisitors();
	}

	public void sequenceInitialize(ClusNode node) {
		int max_size = node.getNbNodes();
		int abs_max = getMaxSize();
		if (abs_max != -1 && max_size > abs_max) max_size = abs_max;
		if ((max_size % 2) == 0) max_size--;
		m_MaxIndex = max_size;
		m_CrIndex = m_MaxIndex;
		recursiveInitialize(node, max_size);
		setOriginalTree(node);
	}

	public void sequenceReset() {
		m_CrIndex = m_MaxIndex;
	}

	public ClusNode sequenceNext() {
		if (m_CrIndex > 0) {
			ClusNode cloned = getOriginalTree().cloneTreeWithVisitors();
			pruneExecute(cloned, m_CrIndex);
			m_CrIndex -= 2;
			return cloned;
		} else {
			return null;
		}
	}

	public void sequenceToElemK(ClusNode node, int k) {
		pruneExecute(node, m_MaxIndex-2*k);
	}

	public void pruneMaxError(ClusNode node, int maxsize) throws ClusException {
		pruneInitialize(node, maxsize);
		int constr_ok_size = maxsize;
		for (int crsize = 1; crsize <= maxsize; crsize += 2) {
			ClusNode copy = node.cloneTreeWithVisitors();
			pruneExecute(copy, crsize);
			ClusErrorList cr_err = m_ErrorMeasure.getErrorClone();
			ClusError err = cr_err.getFirstError();
			// Can be made more efficient :-)
			TreeErrorComputer.computeErrorStandard(copy, m_Data, err);
			cr_err.setNbExamples(m_Data.getNbRows());
			if (m_MaxError.length == 1) {
				double max_err = m_MaxError[0];
				// System.out.println("dans SizeConstraintPruning, maxError = "+max_err);
				if (err.getModelError() <= max_err) {
					constr_ok_size = crsize;
					break;
				}
			} else {
				boolean isOK = true;
				for (int i = 0; i < m_MaxError.length; i++) {
					double err_i = m_MaxError[i];
					if (!Double.isNaN(err_i)) {
						if (err.getModelErrorComponent(i) > err_i) {
							isOK = false;
						}
					}
				}
				if (isOK) {
					constr_ok_size = crsize;
					break;
				}
			}
		}
		pruneExecute(node, constr_ok_size);
	}

	private static void recursiveInitialize(ClusNode node, int size) {
		/* Create array for each node */
		SizeConstraintVisitor visitor = new SizeConstraintVisitor(size);
		node.setVisitor(visitor);
		/* Recursively visit children */
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			recursiveInitialize(child, size);
		}
	}

	private void recursiveInitializeError(ClusNode node, RowData data) {
		// get visitor and error measure
		SizeConstraintVisitor visitor = (SizeConstraintVisitor)node.getVisitor();
		ClusErrorList parent = getAdditiveError();
		ClusError err = parent.getFirstError();
		// compute error for given node
		parent.reset();
		TreeErrorComputer.computeErrorNode(node, data, err);
		parent.setNbExamples(data.getNbRows());
		visitor.error = err.getModelErrorAdditive();
		// do recursive call for each child
		NodeTest tst = node.getTest();
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			RowData subset = data.applyWeighted(tst, i);
			recursiveInitializeError(child, subset);
		}
	}

	private void recursiveInitializeErrorFromStatistic(ClusNode node) {
		// compute error for given node
		SizeConstraintVisitor visitor = (SizeConstraintVisitor)node.getVisitor();
		visitor.error = node.getTargetStat().getError(m_TargetWeights);
		// do recursive call for each child
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			recursiveInitializeErrorFromStatistic(child);
		}
	}

	public double computeNodeCost(ClusNode node) {
		SizeConstraintVisitor visitor = (SizeConstraintVisitor)node.getVisitor();
		return visitor.error;
	}

	public double computeCosts(ClusNode node, int l) {
		SizeConstraintVisitor visitor = (SizeConstraintVisitor)node.getVisitor();
		if (visitor.computed[l]) return visitor.cost[l];
		if (l < 3 || node.atBottomLevel()) {
			visitor.cost[l] = computeNodeCost(node);
		} else {
			visitor.cost[l] = computeNodeCost(node);
			ClusNode ch1 = (ClusNode)node.getChild(0);
			ClusNode ch2 = (ClusNode)node.getChild(1);
			for (int k1 = 1; k1 <= l-2; k1++) {
				int k2 = l - k1 - 1;
				double cost = computeCosts(ch1, k1) +
				              computeCosts(ch2, k2);
				// System.out.println("cost "+cost+" "+arr[l].cost);
				if (cost < visitor.cost[l]) {
					visitor.cost[l] = cost;
					visitor.left[l] = k1;
				}
			}
		}
		// System.out.println("Node: "+node+" cost "+l+" = "+arr[l]);
		visitor.computed[l] = true;
		return visitor.cost[l];
	}

	public static void pruneToSizeK(ClusNode node, int l) {
		if (node.atBottomLevel()) return;
		SizeConstraintVisitor visitor = (SizeConstraintVisitor)node.getVisitor();
		if (l < 3 || visitor.left[l] == 0) {
			node.makeLeaf();
		} else {
			int k1 = visitor.left[l];
			int k2 = l - k1 - 1;
			pruneToSizeK((ClusNode)node.getChild(0), k1);
			pruneToSizeK((ClusNode)node.getChild(1), k2);
		}
	}

	public void setMaxError(double[] max_err) {
		m_MaxError = max_err;
	}

	public void setErrorMeasure(ClusErrorList parent) {
		m_ErrorMeasure = parent;
	}

	// if the default statistic is unable to compute the additive error used during pruning
	// note: this is, e.g., the case for time series clustering
	public void setAdditiveError(ClusErrorList parent) {
		m_AdditiveError = parent;
	}

	public ClusErrorList getAdditiveError() {
		return m_AdditiveError;
	}

	public boolean isUsingAdditiveError() {
		return m_AdditiveError != null;
	}
}

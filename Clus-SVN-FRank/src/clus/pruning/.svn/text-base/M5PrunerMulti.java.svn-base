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

/*
 * Created on May 12, 2005
 */
package clus.pruning;

import clus.statistic.*;
import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.data.attweights.*;

// import clus.weka.*;

public class M5PrunerMulti extends PruneTree {

	double m_F = 0.00001;
	double m_PruningMult = 2;
	double m_GlobalRMSE[];
	ClusAttributeWeights m_TargetWeights;
	RowData m_TrainingData;

	public M5PrunerMulti(ClusAttributeWeights prod, double mult) {
		m_TargetWeights = prod;
		m_PruningMult = mult;
	}

	public void prune(ClusNode node) {
		// ClusNode orig = null;
		// orig = (ClusNode)node.cloneTree();
		RegressionStat stat = (RegressionStat)node.getClusteringStat();
		m_GlobalRMSE = stat.getRootScaledVariances(m_TargetWeights);
		pruneRecursive(node);
		// System.out.println("Performing test of M5 pruning");
		// TestM5PruningRuleNode.performTest(orig, node, m_GlobalDeviation, m_TargetWeights, m_TrainingData);
	}

	public int getNbResults() {
		return 1;
	}

	private double pruningFactor(double num_instances, int num_params) {
		if (num_instances <= num_params) {
			return 10.0;    // Caution says Yong in his code
		}
		return ((double) (num_instances + m_PruningMult * num_params)
				/ (double) (num_instances - num_params));
	}

	public static double estimateRootScaledVariance(ClusNode tree, int attr, ClusAttributeWeights scale) {
		double totweight = tree.getClusteringStat().getTotalWeight();
		return Math.sqrt(estimateScaledVariance(tree, attr, scale) / totweight);
	}

	public static double estimateScaledVariance(ClusNode tree, int attr, ClusAttributeWeights scale) {
		if (tree.atBottomLevel()) {
			RegressionStat stat = (RegressionStat)tree.getClusteringStat();
			return stat.getScaledSS(attr, scale);
		} else {
			double result = 0.0;
			for (int i = 0; i < tree.getNbChildren(); i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				result += estimateScaledVariance(child, attr, scale);
			}
			return result;
		}
	}

	// All targets more accurate than GlobalRMSE * F?
	public boolean allAccurate(RegressionStat stat) {
		for (int i = 0; i < stat.getNbAttributes(); i++) {
			double E_leaf = stat.getRootScaledVariance(i, m_TargetWeights) *
			                pruningFactor(stat.getTotalWeight(), 1);
			if (E_leaf >= m_GlobalRMSE[i]*m_F) return false;
		}
		return true;
	}

	// All targets leaf more accurate than subtree
	public boolean allBetterThanTree(ClusNode node, RegressionStat stat, int modelsize) {
		for (int i = 0; i < stat.getNbAttributes(); i++) {
			double E_leaf = stat.getRootScaledVariance(i, m_TargetWeights) *
			                pruningFactor(stat.getTotalWeight(), 1);
			double E_tree = estimateRootScaledVariance(node, i, m_TargetWeights) *
			                 pruningFactor(stat.getTotalWeight(), modelsize);
			if (E_leaf > E_tree) {
				return false;
			}
		}
		return true;
	}

	public void pruneRecursive(ClusNode node) {
		if (node.atBottomLevel()) {
			return;
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			pruneRecursive(child);
		}
		RegressionStat leaf_stat = (RegressionStat)node.getClusteringStat();
		if (allAccurate(leaf_stat)) {
			node.makeLeaf();
		}
		int modelsize = node.getNbNodes();
		if (allBetterThanTree(node, leaf_stat, modelsize)) {
			node.makeLeaf();
		}
	}

	public void setTrainingData(RowData data) {
		m_TrainingData = data;
	}
}

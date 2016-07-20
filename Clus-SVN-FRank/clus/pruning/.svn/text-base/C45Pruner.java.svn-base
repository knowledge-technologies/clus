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

import org.apache.commons.math.*;
import org.apache.commons.math.distribution.*;

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.model.test.*;
import clus.util.*;
import clus.statistic.*;

// import clus.weka.*;

public class C45Pruner extends PruneTree {

	RowData m_TrainingData;
	boolean m_SubTreeRaising = true;
	double m_ConfidenceFactor = 0.25;
	double m_ZScore = 0.0;

	public void prune(ClusNode node) throws ClusException {
		m_ZScore = computeZScore();
		node.safePrune();
		// ClusNode orig = (ClusNode)node.cloneTree();
		node.pruneByTrainErr(null);
		pruneC45Recursive(node, m_TrainingData);
		// System.out.println("Performing test of C45 pruning");
		// TestC45PruningRuleNode.performTest(orig, node, m_TrainingData);
	}

	public int getNbResults() {
		return 1;
	}

	public void pruneC45Recursive(ClusNode node, RowData data) throws ClusException {
		if (!node.atBottomLevel()) {
			// first prune all child trees
			NodeTest tst = node.getTest();
			for (int i = 0; i < node.getNbChildren(); i++) {
				ClusNode child = (ClusNode)node.getChild(i);
				RowData subset = data.applyWeighted(tst, i);
				pruneC45Recursive(child, subset);
			}
			// compute largest branch index
			double errorsLargestBranch = 0.0;
			int indexOfLargestBranch = node.getLargestBranchIndex();
			if (m_SubTreeRaising) {
				ClusNode largest = (ClusNode)node.getChild(indexOfLargestBranch);
				errorsLargestBranch = getEstimatedErrorsForBranch(largest, data);
			} else {
				errorsLargestBranch = Double.MAX_VALUE;
			}
			// Compute error if this Tree would be leaf
			double errorsLeaf = getEstimatedErrorsForDistribution((ClassificationStat)node.getTargetStat());
			// Compute error for the whole subtree
			double errorsTree = getEstimatedErrors(node);
			// Decide if leaf is best choice.
			if (ClusUtil.smOrEq(errorsLeaf,errorsTree+0.1) &&
			    ClusUtil.smOrEq(errorsLeaf,errorsLargestBranch+0.1)){
				node.makeLeaf();
				return;
			}
			// Decide if largest branch is better choice than whole subtree.
			if (ClusUtil.smOrEq(errorsLargestBranch,errorsTree+0.1)) {
				ClusNode largest = (ClusNode)node.getChild(indexOfLargestBranch);
				node.makeLeaf();
				node.setTest(largest.getTest());
				node.setNbChildren(largest.getNbChildren());
				for (int i = 0; i < largest.getNbChildren(); i++) {
					node.setChild(largest.getChild(i), i);
				}
				node.adaptToData(data);
				pruneC45Recursive(node, data);
			}
		}
	}

	public double getEstimatedErrorsForDistribution(ClassificationStat stat) {
	    if (ClusUtil.eq(stat.getTotalWeight(), 0.0)) {
	        return 0.0;
	    } else {
	    	double nb_incorrect = stat.getError();
	    	return nb_incorrect + addErrs(stat.getTotalWeight(), nb_incorrect, m_ConfidenceFactor);
	    }
	}

	public double getEstimatedErrorsForBranch(ClusNode node, RowData data) {
		if (node.atBottomLevel()) {
			ClassificationStat stat = (ClassificationStat)node.getTargetStat().cloneStat();
			data.calcTotalStatBitVector(stat);
			return getEstimatedErrorsForDistribution(stat);
		} else {
			double sum = 0.0;
			NodeTest tst = node.getTest();
			for (int i = 0; i < node.getNbChildren(); i++) {
				ClusNode child = (ClusNode)node.getChild(i);
				RowData subset = data.applyWeighted(tst, i);
				sum += getEstimatedErrorsForBranch(child, subset);
			}
			return sum;
		}
	}

	public double getEstimatedErrors(ClusNode node) {
		if (node.atBottomLevel()) {
		    return getEstimatedErrorsForDistribution((ClassificationStat)node.getTargetStat());
		} else {
			double sum = 0.0;
			for (int i = 0; i < node.getNbChildren(); i++) {
				ClusNode child = (ClusNode)node.getChild(i);
				sum += getEstimatedErrors(child);
			}
			return sum;
		}
	}

	/* Computes estimated extra error for given total number of instances
	 * and error using normal approximation to binomial distribution (and continuity correction)
	 */
	public double addErrs(double N, double e, double CF) {
		  // Ignore stupid values for CF
		  if (CF > 0.5) {
			  return 0;
		  }
		  // Check for extreme cases at the low end because the
		  // normal approximation won't work
		  if (e < 1) {
			  // Base case (i.e. e == 0) from documenta Geigy Scientific
			  // Tables, 6th edition, page 185
			  double base = N * (1 - Math.pow(CF, 1 / N));
			  if (e == 0) {
				  return base;
			  }
			  // Use linear interpolation between 0 and 1 like C4.5 does
			  return base + e * (addErrs(N, 1, CF) - base);
		  }
		  // Use linear interpolation at the high end (i.e. between N - 0.5
		  // and N) because of the continuity correction
		  if (e + 0.5 >= N) {
			  // Make sure that we never return anything smaller than zero
			  return Math.max(N - e, 0);
		  }
		  // Compute upper limit of confidence interval
		  double z = m_ZScore;
		  double f = (e + 0.5) / N;
		  double r = (f + (z * z) / (2 * N) + z * Math.sqrt((f / N) - (f * f / N) + (z * z / (4 * N * N)))) / (1 + (z * z) / N);
		  return (r * N) - e;
	}

	public void setTrainingData(RowData data) {
		m_TrainingData = data;
	}

	public double computeZScore() throws ClusException {
		try {
			DistributionFactory distributionFactory = DistributionFactory.newInstance();
			return distributionFactory.createNormalDistribution().inverseCumulativeProbability(1 - m_ConfidenceFactor);
		} catch (MathException e) {
			throw new ClusException(e.getMessage());
		}
	}
}

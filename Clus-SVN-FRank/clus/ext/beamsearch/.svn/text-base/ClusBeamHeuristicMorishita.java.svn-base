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
 * Created on Jul 28, 2005
 *
 */
package clus.ext.beamsearch;

import clus.algo.tdidt.ClusNode;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;

public class ClusBeamHeuristicMorishita extends ClusBeamHeuristic {

	public ClusBeamHeuristicMorishita(ClusStatistic stat) {
		super(stat);
	}

	public double computeMorishitaStat(ClusStatistic stat, ClusStatistic tstat) {
		RegressionStat stat_set = (RegressionStat)stat;
		RegressionStat stat_all = (RegressionStat)tstat;
		// Compute half of formula from Definition 2 of Morishita paper
		double result = 0.0;
		for (int i = 0; i < stat_set.getNbAttributes(); i++) {
				double term_i = stat_set.getMean(i) - stat_all.getMean(i);
				result += term_i * term_i;
		}
		return result * stat_set.getTotalWeight();
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		double n_tot = c_tstat.m_SumWeight;
		double n_pos = c_pstat.m_SumWeight;
		double n_neg = n_tot - n_pos;
		// Acceptable?
		if (n_pos < Settings.MINIMAL_WEIGHT || n_neg < Settings.MINIMAL_WEIGHT) {
			return Double.NEGATIVE_INFINITY;
		}
		m_Neg.copy(c_tstat);
		m_Neg.subtractFromThis(c_pstat);
		// Does not take into account missing values!
		return computeMorishitaStat(c_pstat, c_tstat) + computeMorishitaStat(m_Neg, c_tstat);
	}

	public double estimateBeamMeasure(ClusNode tree, ClusNode parent) {
		if (tree.atBottomLevel()) {
			return computeMorishitaStat(tree.getClusteringStat(), parent.getClusteringStat());
		} else {
			double result = 0.0;
			for (int i = 0; i < tree.getNbChildren(); i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				result += estimateBeamMeasure(child);
			}
			return result;
		}
	}

	public double estimateBeamMeasure(ClusNode tree) {
		if (tree.atBottomLevel()) {
			return 0;
		} else {
			double result = 0.0;
			for (int i = 0; i < tree.getNbChildren(); i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				result += estimateBeamMeasure(child, tree);
			}
			return result;
		}
	}

	public double computeLeafAdd(ClusNode leaf) {
		return 0.0;
	}

	public String getName() {
		return "Beam Heuristic (Morishita)";
	}

	public void setRootStatistic(ClusStatistic stat) {
		super.setRootStatistic(stat);
	}
}

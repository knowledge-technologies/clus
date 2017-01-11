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
 * Created on Sep 22, 2005
 *
 */
package clus.ext.hierarchical;

import clus.algo.tdidt.ClusNode;
import clus.model.ClusModelInfo;
import clus.pruning.PruneTree;
import clus.util.ClusException;

public class HierClassTresholdPruner extends PruneTree {

	protected double[] m_Thresholds;

	public HierClassTresholdPruner(double[] tresholds) {
		m_Thresholds = tresholds;
	}

	public void prune(ClusNode node) throws ClusException {
		prune(0, node);
	}

	public int getNbResults() {
		return m_Thresholds.length;
	}

	public String getPrunedName(int i) {
		return "T("+m_Thresholds[i]+")";
	}

	public double getThreshold(int i) {
		return m_Thresholds[i];
	}

	public void updatePrunedModelInfo(ClusModelInfo info) {
		info.setShouldWritePredictions(false);
	}

	public void prune(int result, ClusNode node) throws ClusException {
		pruneRecursive(node, m_Thresholds[result]);
	}

	public void pruneRecursive(ClusNode node, double threshold) throws ClusException {
		WHTDStatistic stat = (WHTDStatistic)node.getTargetStat();
		WHTDStatistic new_stat = (WHTDStatistic)stat.cloneStat();
		new_stat.copyAll(stat);
		new_stat.setThreshold(threshold);
		new_stat.calcMean();
		node.setTargetStat(new_stat);
		for (int i = 0; i < node.getNbChildren(); i++) {
				pruneRecursive((ClusNode)node.getChild(i), threshold);
		}
	}
}

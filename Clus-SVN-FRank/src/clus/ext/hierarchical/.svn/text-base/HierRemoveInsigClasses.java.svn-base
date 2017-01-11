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
 * Created on May 26, 2005
 */
package clus.ext.hierarchical;

import clus.pruning.*;
import clus.util.*;
import clus.algo.tdidt.*;
import clus.data.*;
import clus.data.rows.*;

public class HierRemoveInsigClasses extends PruneTree {

	PruneTree m_Pruner;
	ClusData m_PruneSet;
	ClassHierarchy m_Hier;
	boolean m_NoRoot;
	boolean m_UseBonferroni;
	double m_SigLevel;
	int m_Bonferroni;

	public HierRemoveInsigClasses(ClusData pruneset, PruneTree other, boolean bonf, ClassHierarchy hier) {
		m_Pruner = other;
		m_PruneSet = pruneset;
		m_Hier = hier;
		m_UseBonferroni = bonf;
	}

	public int getNbResults() {
		return 1;
	}

	public void setNoRootPreds(boolean noroot) {
		m_NoRoot = noroot;
	}

	public void setSignificance(double siglevel) {
		m_SigLevel = siglevel;
	}

	public void prune(ClusNode node) throws ClusException {
		m_Pruner.prune(node);
		if (m_SigLevel != 0.0 && m_PruneSet.getNbRows() != 0) {
			// Make sure global statistic is also computed on prune set!
			WHTDStatistic global = (WHTDStatistic)node.getTargetStat().cloneStat();
			m_PruneSet.calcTotalStat(global);
			global.calcMean();
			m_Bonferroni = computeNRecursive(node);
			executeRecursive(node, global, (RowData)m_PruneSet);
		}
	}

	public int computeNRecursive(ClusNode node) {
		int result = 0;
		if (node.atBottomLevel()) {
			WHTDStatistic stat = (WHTDStatistic)node.getTargetStat();
			result += stat.getNbPredictedClasses();
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			result += computeNRecursive((ClusNode)node.getChild(i));
		}
		return result;
	}

	public void executeRecursive(ClusNode node, WHTDStatistic global, RowData data) {
		int arity = node.getNbChildren();
		for (int i = 0; i < arity; i++) {
			RowData subset = data.applyWeighted(node.getTest(), i);
			executeRecursive((ClusNode)node.getChild(i), global, subset);
		}
		WHTDStatistic orig = (WHTDStatistic)node.getTargetStat();
		WHTDStatistic valid = (WHTDStatistic)orig.cloneStat();
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			valid.updateWeighted(tuple, i);
		}
		valid.calcMean();
		WHTDStatistic pred = (WHTDStatistic)orig.cloneStat();
		pred.copy(orig);
		pred.setValidationStat(valid);
		pred.setGlobalStat(global);
		if (m_UseBonferroni) {
			pred.setSigLevel(m_SigLevel/m_Bonferroni);
		} else {
			pred.setSigLevel(m_SigLevel);
		}
		pred.calcMean();
		node.setTargetStat(pred);
	}
}

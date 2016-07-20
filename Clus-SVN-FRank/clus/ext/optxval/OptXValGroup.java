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

import jeans.util.list.*;

import clus.ext.ootind.*;

import clus.main.*;
import clus.algo.split.CurrentBestTestAndHeuristic;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.model.test.*;
import clus.statistic.*;

public class OptXValGroup extends MyList {

	public RowData m_Data;
	public int[] m_Folds;
	public boolean m_IsSoft;

	public NodeTest m_Test;
	public ClusStatistic[] m_TotStat;
	public ClusNode[] m_Nodes;

	public OptXValGroup(RowData data, int size) {
		m_Data = data;
		m_Folds = new int[size];
	}

	public OptXValGroup(OptXValGroup grp, int size) {
		m_Data = grp.m_Data;
		m_IsSoft = grp.m_IsSoft;
		m_Folds = new int[size];
	}

	public void optimize2() {
		m_Data.optimize2(m_Folds);
	}

	public final int[] getFolds() {
		return m_Folds;
	}

	public final void preprocNodes(OptXValNode node, OptXValInduce induce) {
		int nb = getNbFolds();
		for (int i = 0; i < nb; i++) {
			CurrentBestTestAndHeuristic sel = induce.getSelector(i);
			ClusNode fnode = getNode(i);
			if (sel.hasBestTest()) {
				fnode.testToNode(sel);
			} else {
				fnode.makeLeaf();
				cleanNode(i);
			}
			// Store node in tree
			node.setNode(getFold(i), fnode);
		}
	}

	public final void preprocNodes2(OptXValNode node, OOTInduce induce) {
		int nb = getNbFolds();
		for (int i = 0; i < nb; i++) {
			CurrentBestTestAndHeuristic sel = induce.getSelector(i);
			ClusNode fnode = getNode(i);
			if (sel.hasBestTest()) {
				fnode.testToNode(sel);
			} else {
				fnode.makeLeaf();
				cleanNode(i);
			}
			// Store node in tree
			node.setNode(getFold(i), fnode);
		}
	}

	public final void setSoft() {
		m_IsSoft = true;
	}

	public final boolean updateSoft() {
		return (m_IsSoft = m_Data.isSoft());
	}

	public final OptXValGroup cloneGroup() {
		OptXValGroup res = new OptXValGroup(m_Data, m_Folds.length);
		System.arraycopy(m_Folds, 0, res.m_Folds, 0, m_Folds.length);
		res.m_IsSoft = m_IsSoft;
		return res;
	}

	public final void setFold(int idx, int fold) {
		m_Folds[idx] = fold;
	}

	public final void setTest(NodeTest test) {
		m_Test = test;
	}

	public final NodeTest getTest() {
		return m_Test;
	}

	public final void println() {
		System.out.print("[");
		System.out.print(m_Folds[0]);
		for (int i = 1; i < m_Folds.length; i++) {
			System.out.print(","+m_Folds[i]);
		}
		System.out.print("] - ");
		System.out.println(m_Test.getString());
	}

	public final void initializeFolds() {
		for (int i = 0; i < m_Folds.length; i++)
			m_Folds[i] = i;
	}

	public final ClusNode getNode(int i) {
		return m_Nodes[i];
	}

	public final ClusNode[] getNodes() {
		return m_Nodes;
	}

	public final void cleanNode(int i) {
		m_Nodes[i] = null;
	}

	public final void create(ClusStatManager m_StatManager, int folds) {
		m_TotStat = new ClusStatistic[folds+1];
		for (int i = 0; i <= folds; i++)
			m_TotStat[i] = m_StatManager.createClusteringStat();
	}

	public final void create2(ClusStatManager m_StatManager, int folds) {
		m_TotStat = new ClusStatistic[folds];
		for (int i = 0; i < folds; i++)
			m_TotStat[i] = m_StatManager.createClusteringStat();
	}

	public final void makeNodes() {
		int nb = m_Folds.length;
		m_Nodes = new ClusNode[nb];
		for (int i = 0; i < nb; i++) {
			m_Nodes[i] = new ClusNode();
			int foldnr = m_Folds[i];
			m_Nodes[i].m_ClusteringStat = m_TotStat[foldnr];
		}
	}

	public final ClusStatistic getTotStat(int fold) {
		return m_TotStat[fold];
	}

	public final RowData getData() {
		return m_Data;
	}

	public final void setData(RowData data) {
		m_Data = data;
	}

	public final int getFold() {
		return m_Folds[0];
	}

	public final int getNbFolds() {
		return m_Folds.length;
	}

	public final int getFold(int idx) {
		return m_Folds[idx];
	}

	public final void killFold(int idx) {
		m_Folds[idx] = -1;
	}

	public final boolean cleanFolds() {
		// Count number of -1's
		int nb = 0;
		for (int i = 0; i < m_Folds.length; i++) {
			if (m_Folds[i] != -1) nb++;
		}
		// Create new array
		int idx = 0;
		int[] old = m_Folds;
		m_Folds = new int[nb];
		if (nb > 0) {
			for (int i = 0; i < old.length; i++) {
				if (old[i] != -1) m_Folds[idx++] = old[i];
			}
		}
		// Return true if empty
		return (nb == 0);
	}

	public final ClusNode makeLeaf(int idx) {
		ClusNode leaf = new ClusNode();
		leaf.m_ClusteringStat = m_TotStat[idx];
		leaf.makeLeaf();
		return leaf;
	}

	public final boolean stopCrit(int idx) {
		if (m_TotStat[idx].m_SumWeight < 2.0*Settings.MINIMAL_WEIGHT) return true;
		return false;
	}

	public final void stopCrit(OptXValNode node) {
		int nb = getNbFolds();
		for (int i = 0; i < nb; i++) {
			int fld = getFold(i);
			if (stopCrit(fld)) {
				// Grow leaf
				node.setNodeIndex(i, makeLeaf(fld));
				// Remove fold
				killFold(i);
			}
		}
	}

	public final void calcTotalStats2() {
		int nb = m_Data.getNbRows();
		for (int i = 0; i < nb; i++) {
			DataTuple tuple = m_Data.getTuple(i);
			int[] folds = tuple.m_Folds;
			for (int j = 0; j < folds.length; j++) {
				int times = folds[j];
				if (times != 0)	m_TotStat[j].updateWeighted(tuple, times*tuple.getWeight());
			}
		}
	}

	public final void calcTotalStats() {
		m_Data.calcXValTotalStat(m_TotStat);
		ClusStatistic sum = m_TotStat[0];
		// Calculate sum first
		for (int i = 1; i < m_TotStat.length; i++) {
			sum.add(m_TotStat[i]);
		}
		// Update
		for (int i = 1; i < m_TotStat.length; i++) {
			m_TotStat[i].subtractFromOther(sum);
		}
	}

	public final void calcTotalStats(ClusStatistic[] extra) {
		m_Data.calcXValTotalStat(m_TotStat, extra);
		ClusStatistic sum = m_TotStat[0];
		// Calculate sum first
		for (int i = 1; i < m_TotStat.length; i++) {
			sum.add(m_TotStat[i]);
		}
		// Update
		for (int i = 1; i < m_TotStat.length; i++) {
			m_TotStat[i].subtractFromOther(sum);
		}
		// Add extra
		for (int i = 0; i < m_TotStat.length; i++) {
			m_TotStat[i].add(extra[i]);
		}
	}

}

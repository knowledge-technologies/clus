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

package clus.algo.rules;

//import gnu.java.awt.java2d.ScanlineCoverage.Iterator;
//import gnu.javax.crypto.mode.CTR;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import clus.algo.split.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.ilevelc.ILevelCHeurStat;
import clus.ext.ilevelc.ILevelCStatistic;
import clus.ext.ilevelc.ILevelConstraint;
import clus.main.*;
import clus.model.test.NumericTest;
import clus.util.ClusException;

public class FindBestTestRules extends FindBestTest {

	public FindBestTestRules(ClusStatManager mgr) {
		super(mgr);
	}

	public FindBestTestRules(ClusStatManager mgr, NominalSplit split) {
		super(mgr, split);
	}

	/**
	 * Generate nominal split value and rule (and inverse '<=') for that.
	 * @param at
	 * @param data Data the split is based on. Chooses one value from these.
	 */
	public void findNominal(NominalAttrType at, RowData data) {
		// Reset positive statistic
		int nbvalues = at.getNbValues();
		m_BestTest.reset(nbvalues + 1);
		int nb_rows = data.getNbRows();
		if (!getSettings().isHeurRuleDist()) {
			// For each attribute value
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = data.getTuple(i);
				int value = at.getNominal(tuple);
				m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
			}
		} else {
			// TODO: Perhaps ListArray[nbvalues] instead of int[nbvalues][nb_rows] would be better?
			int[][] data_idx_per_val = new int[nbvalues][nb_rows];
			for (int j = 0; j < nbvalues; j++) {
				for (int i = 0; i < nb_rows; i++) {
					data_idx_per_val[j][i] = -1;
				}
			}
			// For each attribute value
			int[] counts = new int[nbvalues];
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = data.getTuple(i);
				int value = at.getNominal(tuple);
				m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
				if (value < nbvalues) {  // Skip missing values, will this be a problem somewhere?
					data_idx_per_val[value][i] = tuple.getIndex();
					counts[value]++;
				}
			}
			// Skip -1s
			int[][] data_ipv = new int[nbvalues][];
			for (int j = 0; j < nbvalues; j++) {
				data_ipv[j] = new int[counts[j]];
				int k = 0;
				for (int i = 0; i < nb_rows; i++) {
					if (data_idx_per_val[j][i] != -1) {
						data_ipv[j][k] = data_idx_per_val[j][i];
						k++;
					}
				}
			}
			((ClusRuleHeuristicDispersion)m_BestTest.m_Heuristic).setDataIndexesPerVal(data_ipv);
		}
		// Find best split
		m_Split.findSplit(m_BestTest, at);
	}

	/**
	 * Randomly generates nominal split
	 * @param at
	 * @param data Data the split is based on. Chooses one value from these.
	 * @param rn Random number generator.
	 */
	public void findNominalRandom(NominalAttrType at, RowData data, Random rn) {
		// Reset positive statistic
		int nbvalues = at.getNbValues();
		m_BestTest.reset(nbvalues + 1);
		// For each attribute value
		int nb_rows = data.getNbRows();
		for (int i = 0; i < nb_rows; i++) {
			DataTuple tuple = data.getTuple(i);
			int value = at.getNominal(tuple);
			m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
		}
		// Find the split
		m_Split.findRandomSplit(m_BestTest, at, rn);

		//TODO inverse splits?

	}

	/**
	 * Generate numeric split value and rule (and inverse '<=') for that.
	 * @param at
	 * @param data Data the split is based on. Chooses one value from these.
	 */
	public void findNumeric(NumericAttrType at, RowData data) {
		DataTuple tuple;
		if (at.isSparse()) {
			data.sortSparse(at, m_SortHelper);
		} else {
			data.sort(at);
		}
		m_BestTest.reset(2);
		// Missing values
		int first = 0;
		int nb_rows = data.getNbRows();
		// Copy total statistic into corrected total
		m_BestTest.copyTotal();
		if (at.hasMissing()) {
			// Because of sorting, all missing values are in the front :-)
			while (first < nb_rows && at.isMissing(tuple = data.getTuple(first))) {
				m_BestTest.m_MissingStat.updateWeighted(tuple, first);
				first++;
			}
			m_BestTest.subtractMissing();
		}
		double prev = Double.NaN;
		int[] data_idx = new int[nb_rows]; // TODO: Skip missing ones?!
		if (getSettings().isHeurRuleDist()) {
			for (int i = first; i < nb_rows; i++) {
				data_idx[i] = data.getTuple(i).getIndex();
			}
		}
		for (int i = first; i < nb_rows; i++) {
			tuple = data.getTuple(i);
			double value = at.getNumeric(tuple);
			if (value != prev) {
				if (value != Double.NaN) {
					if (getSettings().isHeurRuleDist()) {
						int[] subset_idx = new int[i-first];
						System.arraycopy(data_idx, first, subset_idx, 0, i-first);
						((ClusRuleHeuristicDispersion)m_BestTest.m_Heuristic).setDataIndexes(subset_idx);
					}
					// System.err.println("Value (>): " + value);
					m_BestTest.updateNumeric(value, at);
				}
				prev = value;
			}
			m_BestTest.m_PosStat.updateWeighted(tuple, i);
		}
		// For rules check inverse splits also
		if (m_StatManager.isRuleInduceOnly()) {
			m_BestTest.reset();
			DataTuple next_tuple = data.getTuple(nb_rows-1);
			double next = at.getNumeric(next_tuple);
			for (int i = nb_rows-1; i > first; i--) {
				tuple = next_tuple;
				next_tuple = data.getTuple(i-1);
				double value = next;
				next = at.getNumeric(next_tuple);
				m_BestTest.m_PosStat.updateWeighted(tuple, i);
				if ((value != next) && (value != Double.NaN)) {
					if (getSettings().isHeurRuleDist()) {
						int[] subset_idx = new int[nb_rows-i];
						System.arraycopy(data_idx, i, subset_idx, 0, nb_rows-i);
						((ClusRuleHeuristicDispersion)m_BestTest.m_Heuristic).setDataIndexes(subset_idx);
					}
					// System.err.println("Value (<=): " + value);
					m_BestTest.updateInverseNumeric(value, at);
				}
			}
		}
	}

	/**
	 * Randomly generates numeric split value. Also randomly chooses
	 * the > or inverse <= for the rule.
	 * @param at
	 * @param data Data the split is based on. Chooses one value from these.
	 * @param rn Random number generator.
	 */
	public void findNumericRandom(NumericAttrType at, RowData data, RowData orig_data, Random rn) {
		DataTuple tuple;
		int idx = at.getArrayIndex();
		// Sort values from large to small
		if (at.isSparse()) {
			data.sortSparse(at, m_SortHelper);
		} else {
			data.sort(at);
		}
		m_BestTest.reset(2);
		// Missing values
		int first = 0;
		int nb_rows = data.getNbRows();
		// Copy total statistic into corrected total
		m_BestTest.copyTotal();
		if (at.hasMissing()) {
			// Because of sorting, all missing values are in the front :-)
			while (first < nb_rows && (tuple = data.getTuple(first)).hasNumMissing(idx)) {
				m_BestTest.m_MissingStat.updateWeighted(tuple, first);
				first++;
			}
			m_BestTest.subtractMissing();
		}
		// Do the same for original data, except updating the statistics:
		// Sort values from large to small
		if (at.isSparse()) {
			orig_data.sortSparse(at, m_SortHelper);
		} else {
			orig_data.sort(at);
		}
		// Missing values
		int orig_first = 0;
		int orig_nb_rows = orig_data.getNbRows();
		if (at.hasMissing()) {
			// Because of sorting, all missing values are in the front :-)
			while (orig_first < orig_nb_rows &&
					(tuple = orig_data.getTuple(orig_first)).hasNumMissing(idx)) {
				orig_first++;
			}
		}

		// Generate the random split value based on the original data
		double min_value = orig_data.getTuple(orig_nb_rows-1).getDoubleVal(idx);
		double max_value = orig_data.getTuple(orig_first).getDoubleVal(idx);
		double split_value = (max_value - min_value) * rn.nextDouble() + min_value;
		for (int i = first; i < nb_rows; i++) {
			tuple = data.getTuple(i);
			if (tuple.getDoubleVal(idx) <= split_value) break;
			m_BestTest.m_PosStat.updateWeighted(tuple, i);
		}
		m_BestTest.updateNumeric(split_value, at);
		System.err.println("Inverse splits not yet included!");

		//if (rn.nextBoolean())
		//{
		//	// Randomly take the inverse test
		//	m_BestTest.updateInverseNumeric(split_value, at);
		//}
	}

}

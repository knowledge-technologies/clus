/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.algo.split;

import java.util.Random;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.RowDataSortHelper;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.heuristic.GISHeuristic;
import si.ijs.kt.clus.heuristic.VarianceReductionHeuristicCompatibility;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.main.settings.section.SettingsTree.SplitPositions;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.ClusRandomNonstatic;
import si.ijs.kt.clus.util.exception.ClusException;

public class FindBestTest {

	public CurrentBestTestAndHeuristic m_BestTest;
	// public long m_Timer = 0;
	protected RowDataSortHelper m_SortHelper = new RowDataSortHelper();

	protected ClusStatManager m_StatManager;
	protected NominalSplit m_Split;
	protected int m_MaxStats;

	// daniela
	// private RowData m_NodeData;
	public double hMaxB = Double.NEGATIVE_INFINITY;
	public double hMinB = Double.POSITIVE_INFINITY;
	// daniela end

	public FindBestTest(ClusStatManager mgr) {
		m_StatManager = mgr;
		m_MaxStats = getSchema().getMaxNbStats();

		m_BestTest = new CurrentBestTestAndHeuristic(mgr.getSettings());
	}

	public FindBestTest(ClusStatManager mgr, NominalSplit split) {
		this(mgr);

		m_Split = split;
	}

	public ClusSchema getSchema() {
		return getStatManager().getSchema();
	}

	public ClusStatManager getStatManager() {
		return m_StatManager;
	}

	public RowDataSortHelper getSortHelper() {
		return m_SortHelper;
	}

	public Settings getSettings() {
		return getStatManager().getSettings();
	}

	public CurrentBestTestAndHeuristic getBestTest() {
		return m_BestTest;
	}

	public void cleanSplit() {
		m_Split = null;
	}

	// daniela
	public void rememberMinMax(NominalAttrType at, RowData data) throws ClusException {
		findNominal(at, data, null);
		ClusStatistic m_CStat = m_StatManager.createClusteringStat();
		m_CStat.add(m_BestTest.m_TestStat[0]);
		double heur = m_BestTest.calcHeuristic(m_BestTest.m_TotStat, m_CStat);
		if (heur != Double.POSITIVE_INFINITY && heur != Double.NEGATIVE_INFINITY) {
			if (heur > hMaxB) {
				hMaxB = heur;
			}
			if (heur < hMinB) {
				hMinB = heur;
			}
		}
		// System.out.println(heur+" "+hMinB+" -- "+hMaxB);
	}
	// daniela end

	public void findNominal(NominalAttrType at, RowData data, ClusRandomNonstatic rnd) throws ClusException {
		// long start_time = System.currentTimeMillis();
		RowData sample = createSample(data, rnd);
		int nbvalues = at.getNbValues();
		m_BestTest.reset(nbvalues + 1); // Reset positive statistic
		int nb_rows = sample.getNbRows();

		if (!data.getSchema().getSettings().getAttribute().isNullGIS()) { // handle Daniela
			// daniela
			// generateMatrix for each attribute GISHeuristic
			Integer[] indicesSorted = new Integer[nb_rows]; // matejp: no sorting here, but needed for technical reasons
			for (int i = 0; i < nb_rows; i++) {
				indicesSorted[i] = i;
			}
			try {
				ClusHeuristic m_Heuristic = m_StatManager.getHeuristic();
				if (m_Heuristic instanceof VarianceReductionHeuristicCompatibility) {
					VarianceReductionHeuristicCompatibility gisHeuristic = (VarianceReductionHeuristicCompatibility) m_Heuristic;
					ClusAttrType[] arr = at.getSchema().getAllAttrUse(AttributeUseType.GIS); // numeric and string
																									// GIS
					if (arr.length == 1) {
						gisHeuristic.readMatrixFromFile(sample); // 1 coordinate and a distance.csv file associated
					} else
						gisHeuristic.generateMatrix(sample); // 2 coordinates
				} else if (m_Heuristic instanceof GISHeuristic) {
					GISHeuristic gisHeuristic = (GISHeuristic) m_Heuristic;
					ClusAttrType[] arr = at.getSchema().getAllAttrUse(AttributeUseType.GIS); // numeric and string
																									// GIS
					if (arr.length == 1) {
						gisHeuristic.readMatrixFromFile(sample); // 1 coordinate and a distance.csv file associated
					} else
						gisHeuristic.generateMatrix(sample, indicesSorted); // 2 coordinates
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ClusException("Error calculating GISHEuristics!"); // this try/catch block is weird
			}

			m_BestTest.m_PosStat.setData(sample);
			m_BestTest.m_TotStat.setData(sample);
			m_BestTest.m_TotStat.setSplitIndex(sample.getNbRows());
			m_BestTest.m_PosStat.setPrevIndex(0);
			m_BestTest.m_PosStat.initializeSum();

			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = sample.getTuple(i);
				int value = at.getNominal(tuple);
				m_BestTest.m_TestStat[value].setData(sample);
				m_BestTest.m_TestStat[value].initializeSum();
				m_BestTest.m_TestStat[value].setPrevIndex(0);
			}
			// daniela end
		}

		if (nbvalues == 2 && !at.hasMissing()) {
			// Only count ones for binary attributes (optimization)
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = sample.getTuple(i);
				int value = at.getNominal(tuple);
				// The value "1" has index 0 in the list of attribute values
				if (value == 0) {
					m_BestTest.m_TestStat[0].updateWeighted(tuple, i);
					m_BestTest.m_TestStat[0].setSplitIndex(i + 1); // daniela
				}
			}
			// Also compute the statistic for the zeros
			m_BestTest.m_TestStat[1].copy(m_BestTest.m_TotStat);
			m_BestTest.m_TestStat[1].subtractFromThis(m_BestTest.m_TestStat[0]);
		} else {
			// Regular code for non-binary attributes
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = sample.getTuple(i);
				int value = at.getNominal(tuple);
				m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
				m_BestTest.m_TestStat[value].setSplitIndex(i + 1); // daniela
			}
		}

		/*
		 * long stop_time = System.currentTimeMillis(); long elapsed = stop_time -
		 * start_time; m_Timer += elapsed;
		 */

		// System.out.println("done");

		// Find best split
		m_Split.findSplit(m_BestTest, at);
	}

	// @Deprecated
	// public void findNominalRandom(NominalAttrType at, RowData data,
	// ClusRandomNonstatic rnd) {
	// Random rn;
	// if(rnd == null){
	// rn = ClusRandom.getRandom(ClusRandom.RANDOM_EXTRATREE);
	// } else{
	// rn = rnd.getRandom(ClusRandomNonstatic.RANDOM_EXTRATREE);
	// }
	//
	// // Reset positive statistic
	// RowData sample = createSample(data, rnd);
	// int nbvalues = at.getNbValues();
	// m_BestTest.reset(nbvalues + 1);
	// // For each attribute value
	// int nb_rows = sample.getNbRows();
	// for (int i = 0; i < nb_rows; i++) {
	// DataTuple tuple = sample.getTuple(i);
	// int value = at.getNominal(tuple);
	// m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
	// }
	// // Find the split
	// m_Split.findRandomSplit(m_BestTest, at, rn);
	// }

	public void findNominalExtraTree(NominalAttrType at, RowData data, ClusRandomNonstatic rnd) throws ClusException {
		Random rn;
		if (rnd == null) {
			rn = ClusRandom.getRandom(ClusRandom.RANDOM_EXTRATREE);
			ClusEnsembleInduce.giveParallelisationWarning(ClusEnsembleInduce.m_PARALLEL_TRAP_staticRandom);
		} else {
			rn = rnd.getRandom(ClusRandomNonstatic.RANDOM_EXTRATREE);
		}
		// Reset positive statistic
		RowData sample = createSample(data, rnd);
		int nbvalues = at.getNbValues();
		m_BestTest.reset(nbvalues + 1);
		// For each attribute value
		int nb_rows = sample.getNbRows();
		for (int i = 0; i < nb_rows; i++) {
			DataTuple tuple = sample.getTuple(i);
			int value = at.getNominal(tuple);
			m_BestTest.m_TestStat[value].updateWeighted(tuple, i);
		}
		// Find the split
		m_Split.findExtraTreeSplit(m_BestTest, at, rn);
	}

	public void findNumeric(NumericAttrType at, RowData data, ClusRandomNonstatic rnd) throws Exception {
		RowData sample = createSample(data, rnd);
		DataTuple tuple;
		// if (at.isSparse()) {
		// sample.sortSparse(at, m_SortHelper);
		// }
		// else {
		// sample.sort(at);
		// }
		Integer[] indicesSorted = sample.smartSort(at);

		m_BestTest.reset(2);
		// Missing values
		int pos = 0;
		int nb_rows = sample.getNbRows();
		// Copy total statistic into corrected total
		m_BestTest.copyTotal();

		if (at.isMissing(sample.getTuple(indicesSorted[pos]))) { // at.hasMissing()
			// matejp: changed if-condition since sparse attributes do not detect missing
			// values in the reading phase
			// The conditions are equivalent since all missing values are in the front
			// because of sorting
			while (pos < nb_rows && at.isMissing(tuple = sample.getTuple(indicesSorted[pos]))) {
				m_BestTest.m_MissingStat.updateWeighted(tuple, indicesSorted[pos]);
				pos++;
			}
			m_BestTest.subtractMissing();
		}

		double prev = Double.NaN;
		boolean isGIS = !data.getSchema().getSettings().getAttribute().isNullGIS();
		if (isGIS) {
			gisMatrixForFindNumberic(sample, nb_rows, pos, indicesSorted, at); // matejp: moved to a separate method for
																				// readability
		}

		double minValue = (pos < nb_rows) ? at.getNumeric(sample.getTuple(indicesSorted[nb_rows - 1])) : Double.NaN;
		boolean isSparseAtr = at.isSparse();

		double tot_corr_SVarS = 0.0; // does not matter which value we choose;
		// boolean isEfficient = m_BestTest.m_Heuristic.isEfficient();
		// if(isEfficient){
		// tot_corr_SVarS =
		// m_BestTest.m_TotCorrStat.getSVarS(m_BestTest.m_Heuristic.getClusteringAttributeWeights());
		//// m_BestTest.m_Heuristic.setSplitStatSVarS(tot_corr_SVarS);
		// }
		boolean middleSplits = data.getSchema().getSettings().getTree().getSplitPosition()
				.equals(SplitPositions.Middle);
		if (isGIS) {
			for (int i = pos; i < nb_rows; i++) {
				tuple = sample.getTuple(indicesSorted[i]);
				double value = at.getNumeric(tuple);
				if (value != prev) {
					m_BestTest.updateNumericGIS(value, at, indicesSorted);
					prev = value;
					m_BestTest.m_PosStat.setPrevIndex(i); // daniela
				}
				m_BestTest.m_PosStat.updateWeighted(tuple, i);
				m_BestTest.m_PosStat.setSplitIndex(i + 1); // daniela
				if (isSparseAtr && value == minValue) {
					break;
				}
			}
		} else {
			for (int i = pos; i < nb_rows; i++) {
				tuple = sample.getTuple(indicesSorted[i]);
				double value = at.getNumeric(tuple);
				if (value != prev) {
					double middle = prev + (value - prev) / 2; // numerically more stable version for (value + prev) / 2
					if (middleSplits && isFinite(middle)) { // choose the splitting point: in the middle or the exact
															// attribute value
						m_BestTest.updateNumeric(middle, at, tot_corr_SVarS, false); // isEfficient
					} else {
						m_BestTest.updateNumeric(value, at, tot_corr_SVarS, false);
					}
					prev = value;
				}
				m_BestTest.m_PosStat.updateWeighted(tuple, indicesSorted[i]);
				if (isSparseAtr && value == minValue) {
					break;
				}
			}
		}
	}

	private void gisMatrixForFindNumberic(RowData sample, int nb_rows, int pos, Integer[] indicesSorted,
			NumericAttrType at) throws ClusException {
		DataTuple tuple;
		double prev = Double.NaN;
		// daniela
		m_BestTest.m_PosStat.setData(sample);
		m_BestTest.m_TotCorrStat.setData(sample);
		m_BestTest.m_TotCorrStat.setSplitIndex(sample.getNbRows());
		m_BestTest.m_PosStat.setPrevIndex(0);
		m_BestTest.m_PosStat.initializeSum();
		m_BestTest.m_TotCorrStat.initializeSum();
		// for scaling of h -regression (daniela)
		if ((m_BestTest.m_Heuristic instanceof GISHeuristic
				|| m_BestTest.m_Heuristic instanceof VarianceReductionHeuristicCompatibility)
				&& (SettingsTree.ALPHA != 1.0)) {
			for (int i = pos; i < nb_rows; i++) {
				tuple = sample.getTuple(indicesSorted[i]); // every such tuple is positive or, as some people say, sekoj
															// vakov tuple pripaga vo positive
				double value = at.getNumeric(tuple);
				if (value != prev) {
					if (!Double.isNaN(value)) {
						m_BestTest.calculateHMinMax(value, at);
						// System.out.println(at +" za: "+m_BestTest.hMax+"-->"+m_BestTest.hMin);
					}
					prev = value;
					m_BestTest.m_PosStat.setPrevIndex(i);
				}
				m_BestTest.m_PosStat.updateWeighted(tuple, i);
				m_BestTest.m_PosStat.setSplitIndex(i + 1); // add Daniela
			}
			prev = Double.NaN;
			m_BestTest.m_PosStat.setData(sample);
			m_BestTest.m_TotCorrStat.setData(sample);
			m_BestTest.m_TotCorrStat.setSplitIndex(sample.getNbRows());
			m_BestTest.m_PosStat.reset();
			m_BestTest.m_PosStat.setPrevIndex(0);
			m_BestTest.m_PosStat.initializeSum();
		}
		// end for scaling of h -regression

		// daniela generateMatrix for each attribute gisheuristic
		try {
			ClusHeuristic m_Heuristic = m_StatManager.getHeuristic();
			// System.out.println(m_Heuristic);
			if (SettingsTree.ALPHA != 1.0 && m_Heuristic instanceof VarianceReductionHeuristicCompatibility) {
				VarianceReductionHeuristicCompatibility gisHeuristic = (VarianceReductionHeuristicCompatibility) m_Heuristic;
				ClusAttrType[] arr = at.getSchema().getAllAttrUse(AttributeUseType.GIS); // numeric and string GIS
				if (arr.length == 1) {
					gisHeuristic.readMatrixFromFile(sample); // 1 coordinate and a distance.csv file associated
				} else {
					gisHeuristic.generateMatrix(sample); // 2 coordinates
				}
			}

			if (m_Heuristic instanceof GISHeuristic && SettingsTree.ALPHA != 1.0) {
				GISHeuristic gisHeuristic = (GISHeuristic) m_Heuristic;
				ClusAttrType[] arr = at.getSchema().getAllAttrUse(AttributeUseType.GIS); // numeric and string GIS
				if (arr.length == 1) {
					gisHeuristic.readMatrixFromFile(sample); // 1 coordinate and a distance.csv file associated
				} else {
					gisHeuristic.generateMatrix(sample, indicesSorted); // 2 coordinates
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ClusException("Error calculating GISHEuristics!"); // this try/catch block is weird
		}
	}

	public void findNumericExtraTree(NumericAttrType at, RowData orig_data, ClusRandomNonstatic rnd)
			throws ClusException {
		// TODO: if this method gets completed, sampling of the RowDatas must be
		// included as well

		Random rn;
		if (rnd == null) {
			rn = ClusRandom.getRandom(ClusRandom.RANDOM_EXTRATREE);
			ClusEnsembleInduce.giveParallelisationWarning(ClusEnsembleInduce.m_PARALLEL_TRAP_staticRandom);
		} else {
			rn = rnd.getRandom(ClusRandomNonstatic.RANDOM_EXTRATREE);
		}

		RowData data = createSample(orig_data, rnd);
		DataTuple tuple;
		int idx = at.getArrayIndex();

		// Sort values from large to small
		// if (at.isSparse()) {
		// data.sortSparse(at, m_SortHelper);
		// } else {
		// data.sort(at);
		// }

		Integer[] indicesSorted = data.smartSort(at);

		m_BestTest.reset(2);
		// Missing values
		int pos = 0;
		int nb_rows = data.getNbRows();
		// Copy total statistic into corrected total
		m_BestTest.copyTotal();
		if (at.hasMissing()) {
			// Because of sorting, all missing values are in the front :-)
			while (pos < nb_rows && (tuple = data.getTuple(indicesSorted[pos])).hasNumMissing(idx)) {
				m_BestTest.m_MissingStat.updateWeighted(tuple, indicesSorted[pos]);
				pos++;
			}
			m_BestTest.subtractMissing();
		}
		// now indicesSorted[pos] is the index of the tuple that has the highest (and
		// non-missing) value of the attribute

		// Generate the random split value based on the original data
		if (pos == nb_rows) {// this can prevent illegal tests;
			m_BestTest.m_BestHeur = Double.NEGATIVE_INFINITY;
		} else {
			double min_value = data.getTuple(indicesSorted[nb_rows - 1]).getDoubleVal(idx);
			double max_value = data.getTuple(indicesSorted[pos]).getDoubleVal(idx);
			double split_value = (max_value - min_value) * rn.nextDouble() + min_value;
			for (int i = pos; i < nb_rows; i++) {
				tuple = data.getTuple(indicesSorted[i]);
				if (tuple.getDoubleVal(idx) <= split_value)
					break;
				m_BestTest.m_PosStat.updateWeighted(tuple, indicesSorted[i]);
			}

			m_BestTest.updateNumeric(split_value, at); // we test only one split per attribute --> no need for
														// precomputing tot_corr_SVarS
			// m_BestTest.updateNumeric((value + prev)/2, at); //FIXME: splitting point is
			// in the middle of two values -> this is maybe better?
		}

		// System.err.println("Inverse splits not yet included!");
		// TODO: m_Selector.updateInverseNumeric(split_value, at);
	}

	// @Deprecated
	// public void findNumeric(NumericAttrType at, ArrayList data){
	// // for sparse attributes, (already sorted data)
	// ArrayList sample;
	// if (getSettings().getTree().getTreeSplitSampling() > 0) {
	// RowData tmp = new RowData(data, getSchema());
	// RowData smpl = createSample(tmp, null);
	// if (at.isSparse()) {
	// smpl.sortSparse(at, getSortHelper());
	// }
	// else {
	// smpl.sort(at);
	// }
	// sample = smpl.toArrayList();
	// }
	// else {
	// sample = data;
	// }
	// DataTuple tuple;
	// m_BestTest.reset(2);
	// // Missing values
	// int first = 0;
	// int nb_rows = sample.size();
	// // Copy total statistic into corrected total
	// m_BestTest.copyTotal();
	// if (at.hasMissing()) {
	// // Because of sorting, all missing values are in the front :-)
	// while (first < nb_rows && at.isMissing((DataTuple) sample.get(first))) {
	// tuple = (DataTuple) sample.get(first);
	// m_BestTest.m_MissingStat.updateWeighted(tuple, first);
	// first++;
	// }
	// m_BestTest.subtractMissing();
	// }
	// double prev = Double.NaN;
	//
	// for (int i = first; i < nb_rows; i++) {
	// tuple = (DataTuple) sample.get(i);
	// double value = at.getNumeric(tuple);
	// if (value != prev) {
	// if (!Double.isNaN(value)) {
	// // System.err.println("Value (>): " + value);
	// m_BestTest.updateNumeric(value, at);
	// //m_BestTest.updateNumeric((value + prev)/2, at); //FIXME: splitting point is
	// in the middle of two values -> this is maybe better?
	// }
	// prev = value;
	// }
	// m_BestTest.m_PosStat.updateWeighted(tuple, i);
	// }
	// m_BestTest.updateNumeric(0.0, at); // otherwise tests of the form "X>0.0" are
	// not considered
	// }
	//
	// @Deprecated
	// public void findNumericRandom(NumericAttrType at, RowData data, RowData
	// orig_data, Random rn) {
	// // TODO: if this method gets completed, sampling of the RowDatas must be
	// included as well
	// DataTuple tuple;
	// int idx = at.getArrayIndex();
	// // Sort values from large to small
	// if (at.isSparse()) {
	// data.sortSparse(at, m_SortHelper);
	// }
	// else {
	// data.sort(at);
	// }
	// m_BestTest.reset(2);
	// // Missing values
	// int first = 0;
	// int nb_rows = data.getNbRows();
	// // Copy total statistic into corrected total
	// m_BestTest.copyTotal();
	// if (at.hasMissing()) {
	// // Because of sorting, all missing values are in the front :-)
	// while (first < nb_rows && (tuple = data.getTuple(first)).hasNumMissing(idx))
	// {
	// m_BestTest.m_MissingStat.updateWeighted(tuple, first);
	// first++;
	// }
	// m_BestTest.subtractMissing();
	// }
	// // Do the same for original data, except updating the statistics:
	// // Sort values from large to small
	// if (at.isSparse()) {
	// orig_data.sortSparse(at, m_SortHelper);
	// }
	// else {
	// orig_data.sort(at);
	// }
	// // Missing values
	// int orig_first = 0;
	// int orig_nb_rows = orig_data.getNbRows();
	// if (at.hasMissing()) {
	// // Because of sorting, all missing values are in the front :-)
	// while (orig_first < orig_nb_rows && (tuple =
	// orig_data.getTuple(orig_first)).hasNumMissing(idx)) {
	// orig_first++;
	// }
	// }
	// // Generate the random split value based on the original data
	// double min_value = orig_data.getTuple(orig_nb_rows - 1).getDoubleVal(idx);
	// double max_value = orig_data.getTuple(orig_first).getDoubleVal(idx);
	// double split_value = (max_value - min_value) * rn.nextDouble() + min_value;
	// for (int i = first; i < nb_rows; i++) {
	// tuple = data.getTuple(i);
	// if (tuple.getDoubleVal(idx) <= split_value)
	// break;
	// m_BestTest.m_PosStat.updateWeighted(tuple, i);
	// }
	// m_BestTest.updateNumeric(split_value, at);
	// System.err.println("Inverse splits not yet included!");
	// // TODO: m_Selector.updateInverseNumeric(split_value, at);
	// }

	public void initSelectorAndSplit(ClusStatistic totstat) throws ClusException {
		m_BestTest.create(m_StatManager, m_MaxStats);
		m_BestTest.setRootStatistic(totstat);
		if (getSettings().getTree().isBinarySplit())
			m_Split = new SubsetSplit();
		else
			m_Split = new NArySplit();
		m_Split.initialize(m_StatManager);
	}

	public boolean initSelectorAndStopCrit(ClusStatistic total, RowData data) {
		m_BestTest.initTestSelector(total, data);
		m_Split.setSDataSize(data.getNbRows());
		return m_BestTest.stopCrit();
	}

	public void setInitialData(ClusStatistic total, RowData data) throws ClusException {
		m_BestTest.setInitialData(total, data);
	}

	// daniela
	public void resetMinMax() {
		hMaxB = Double.NEGATIVE_INFINITY;
		hMinB = Double.POSITIVE_INFINITY;
	}
	// daniela end

	private RowData createSample(RowData original, ClusRandomNonstatic rnd) {
		int N = getSettings().getTree().getTreeSplitSampling();
		if (N == 0) {
			return original.sample(N, rnd);
		} else {
			String message = String.format("The value of SplitSampling = %d will result in wrong results.\n"
					+ "Use SplitSampling = 0 or correct the code.", N);
			throw new RuntimeException(message);
		}
	}

	/**
	 * Set parent stats to children of this node
	 */
	public void setParentStatsToChildren() {
		m_BestTest.setParentStatsToTests();
	}

	/**
	 * Set parent stat to this node
	 * 
	 * @param stat
	 *            Statistic of the parent node
	 */
	public void setParentStatsToThis(ClusStatistic stat) {
		m_BestTest.setParentStatsToTotal(stat);
	}

	/**
	 * Checks whether the argument is neither NaN nor Infinity. See
	 * Double.isFinite(double) in Java8.
	 * 
	 * @param value
	 * @return
	 */
	private static boolean isFinite(double value) {
		// Java8: return Double.isFinite(v);
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}

}

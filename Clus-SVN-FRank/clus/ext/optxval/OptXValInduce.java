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

import java.io.*;

import clus.main.*;
import clus.util.*;
import clus.data.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.model.*;
import clus.algo.*;
import clus.algo.split.*;
import clus.algo.tdidt.*;
import clus.statistic.*;
import clus.heuristic.*;
import clus.error.multiscore.*;
import clus.tools.debug.Debug;

public abstract class OptXValInduce extends ClusInductionAlgorithm {

	protected ClusHeuristic m_Heuristic;
	protected DepthFirstInduce m_DFirst;
	protected NominalSplit m_Split;
	protected ClusStatistic[] m_PosStat;
	protected ClusStatistic[][] m_TestStat;
	protected ClusStatistic m_Scratch;
	protected int m_NbFolds;
	protected int[] m_PrevCl;
	protected double[] m_PrevVl;
	protected CurrentBestTestAndHeuristic[] m_Selector;
	protected int m_MaxStats;

	public OptXValInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	public final void findNominal(NominalAttrType at, OptXValGroup grp) {
		// Reset positive statistic
		int nbvalues = at.getNbValues();
		int statsize = nbvalues + at.intHasMissing();
		reset(statsize);
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

		// For each attribute value
		RowData data = grp.getData();
		int nb_rows = data.getNbRows();
		for (int i = 0; i < nb_rows; i++) {
			DataTuple tuple = data.getTuple(i);
			int value = at.getNominal(tuple);
if (Debug.debug == 1) {
			ClusStat.deltaTest();
}

			m_TestStat[tuple.m_Index][value].updateWeighted(tuple, i);
if (Debug.debug == 1) {
			ClusStat.deltaStat();
}

		}
		sumStats(statsize);
if (Debug.debug == 1) {
		ClusStat.deltaStat();
}

		// Find best split
		int nb = grp.getNbFolds();
		for (int i = 0; i < nb; i++) {
			int foldnr = grp.getFold(i);
			if (foldnr != 0) {
				ClusStatistic[] zero_stat = m_TestStat[0];
				ClusStatistic[] cr_stat = m_TestStat[foldnr];
				for (int j = 0; j < statsize; j++) {
					cr_stat[j].subtractFromOther(zero_stat[j]);
				}
			}
if (Debug.debug == 1) {
			ClusStat.deltaStat();
}

			m_Split.findSplit(m_Selector[i], at);
if (Debug.debug == 1) {
			ClusStat.deltaHeur();
}

		}
	}

	public final void findNumeric(NumericAttrType at, OptXValGroup grp) {
		// Sort data
		DataTuple tuple;
		RowData data = grp.getData();
		int idx = at.getArrayIndex();
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

		data.sort(at);
if (Debug.debug == 1) {
		ClusStat.deltaSort();
}

		reset(2);
		// Missing values
		int first = 0;
		int nb_rows = data.getNbRows();
		if (at.hasMissing()) {
			while (first < nb_rows && (tuple = data.getTuple(first)).hasNumMissing(idx)) {
				m_TestStat[tuple.m_Index][1].updateWeighted(tuple, first);
				first++;
			}
			subtractMissing(grp);
		} else {
			copyTotal(grp);
		}
if (Debug.debug == 1) {
		ClusStat.deltaStat();
}

		int[] folds = grp.getFolds();
//		ClusNode[] nodes = grp.getNodes();
		for (int i = 0; i < folds.length; i++) {
			m_PrevCl[i] = -1;
			m_PrevVl[i] = Double.NaN;
		}
		ClusStatistic sum = m_PosStat[0];
		if (Settings.ONE_NOMINAL) {
			for (int i = first; i < nb_rows; i++) {
				tuple = data.getTuple(i);
				boolean no_sum_calc = true;
				int foldnr = tuple.getIndex();
				int crcl = tuple.getClassification();
				double value = tuple.getDoubleVal(idx);
				for (int j = 0; j < folds.length; j++) {
					int cr_fold = folds[j];
					if (foldnr != cr_fold) {
						if (m_PrevCl[j] == -1 && value != m_PrevVl[j] && m_PrevVl[j] != Double.NaN) {
							if (no_sum_calc) {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								sum.reset();
								for (int k = 1; k <= m_NbFolds; k++) sum.add(m_PosStat[k]);
								no_sum_calc = false;
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

							}
							if (cr_fold != 0) {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[j].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

							} else {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Selector[j].updateNumeric(value, sum, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

							}
							m_PrevCl[j] = crcl;
						} else {
							if (m_PrevCl[j] != crcl) m_PrevCl[j] = -1;
						}
						m_PrevVl[j] = value;
					}
				}
if (Debug.debug == 1) {
				ClusStat.deltaTest();
}

				m_PosStat[foldnr].updateWeighted(tuple, i);
if (Debug.debug == 1) {
				ClusStat.deltaStat();
}

			}
		} else {
			for (int i = first; i < nb_rows; i++) {
				tuple = data.getTuple(i);
				boolean no_sum_calc = true;
				int foldnr = tuple.getIndex();
				double value = tuple.getDoubleVal(idx);
				for (int j = 0; j < folds.length; j++) {
					int cr_fold = folds[j];
					if (foldnr != cr_fold) {
						if (value != m_PrevVl[j] && m_PrevVl[j] != Double.NaN) {
							if (no_sum_calc) {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								sum.reset();
								for (int k = 1; k <= m_NbFolds; k++) sum.add(m_PosStat[k]);
								no_sum_calc = false;
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

							}
							if (cr_fold != 0) {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[j].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

							} else {
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Selector[j].updateNumeric(value, sum, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

							}
						}
						m_PrevVl[j] = value;
					}
				}
if (Debug.debug == 1) {
				ClusStat.deltaTest();
}

				m_PosStat[foldnr].updateWeighted(tuple, i);
if (Debug.debug == 1) {
				ClusStat.deltaStat();
}

			}
		}
	}

	public abstract OptXValNode xvalInduce(OptXValGroup mgrp);

	public ClusData createData() {
		return new RowData(m_Schema);
	}

	public final void reset(int nb) {
		for (int i = 0; i <= m_NbFolds; i++) {
			for (int j = 0; j < nb; j++) {
				m_TestStat[i][j].reset();
			}
		}
	}

	public final void sumStats(int nb) {
		// For each test stat
		for (int j = 0; j < nb; j++) {
			// Sum over folds (fold 0 = whole tree)
			ClusStatistic sum = m_TestStat[0][j];
			for (int i = 1; i <= m_NbFolds; i++) {
				sum.add(m_TestStat[i][j]);
			}
		}
	}

	public final void subtractMissing(OptXValGroup grp) {
		// Sum missing values for fold zero
		ClusStatistic sum = m_TestStat[0][1];
		for (int i = 1; i <= m_NbFolds; i++) {
			sum.add(m_TestStat[i][1]);
		}
		// subtract missing values
		ClusStatistic stot[] = grp.m_TotStat;
		for (int i = 0; i <= m_NbFolds; i++) {
			m_TestStat[i][1].subtractFromOther(stot[i]);
		}
	}

	public final void copyTotal(OptXValGroup grp) {
		ClusStatistic stot[] = grp.m_TotStat;
		for (int i = 0; i <= m_NbFolds; i++) {
			m_TestStat[i][1].copy(stot[i]);
		}
	}

	public final void findBestTest(OptXValGroup mgrp) {
		// First make nodes
		mgrp.makeNodes();
		// For each attribute
		RowData data = mgrp.getData();
		ClusSchema schema = data.getSchema();
		ClusAttrType[] attrs = schema.getDescriptiveAttributes();
		int nb_normal = attrs.length;
		for (int i = 0; i < nb_normal; i++) {
			ClusAttrType at = attrs[i];
			if (at instanceof NominalAttrType) findNominal((NominalAttrType)at, mgrp);
			else findNumeric((NumericAttrType)at, mgrp);
		}
	}

	public final CurrentBestTestAndHeuristic getSelector(int i) {
		return m_Selector[i];
	}

	public final void cleanSplit() {
		m_Split = null;
	}

	public final void createStats() {
		int mfolds = m_NbFolds+1;
		m_Heuristic = m_StatManager.getHeuristic();
		m_PosStat = new ClusStatistic[mfolds];
		m_TestStat = new ClusStatistic[mfolds][m_MaxStats];
		m_Selector = new CurrentBestTestAndHeuristic[mfolds];
		for (int i = 0; i < mfolds; i++) {
			for (int j = 0; j < m_MaxStats; j++) {
				m_TestStat[i][j] = m_StatManager.createClusteringStat();
			}
			m_PosStat[i] = m_TestStat[i][0];
			// Create test selectors for each fold :-)
			CurrentBestTestAndHeuristic sel = m_Selector[i] = new CurrentBestTestAndHeuristic();
			sel.m_Heuristic = m_Heuristic;
		}
		// Initialize test selector for depth first (1opt)
		CurrentBestTestAndHeuristic sel = m_DFirst.getBestTest();
		sel.m_Heuristic = m_Heuristic;
		sel.m_TestStat = m_TestStat[0];
		sel.m_PosStat = m_PosStat[0];
	}

	public final void initTestSelectors(OptXValGroup grp) {
		int nb = grp.getNbFolds();
		for (int i = 0; i < nb; i++) {
			int fold = grp.getFold(i);
			CurrentBestTestAndHeuristic sel = m_Selector[i];
			sel.m_TestStat = m_TestStat[fold];
			sel.m_PosStat = m_PosStat[fold];
			sel.initTestSelector(grp.getTotStat(fold));
		}
	}

	public final void setNbFolds(int folds) {
		m_NbFolds = folds;
		m_PrevCl = new int[folds+1];
		m_PrevVl = new double[folds+1];
	}

	public final void initialize(int folds) {
		// Create nominal split
		if (getSettings().isBinarySplit()) m_Split = new SubsetSplit();
		else m_Split = new NArySplit();
		// Create depth-first induce
		m_DFirst = new DepthFirstInduce(this, m_Split);
		// Set number of folds
		setNbFolds(folds);
		// Update max stats
		if (m_Schema.getNbNumericDescriptiveAttributes() > 0) m_MaxStats = Math.max(m_MaxStats, 3);
	}

	public final OptXValNode optXVal(RowData data) {
		// Create root node
if (Debug.debug == 1) {
		ClusStat.initTime();
}

		createStats();
		m_Split.initialize(m_StatManager);
		m_Scratch = m_StatManager.createClusteringStat();
		// Create first group
		OptXValGroup grp = new OptXValGroup(data, m_NbFolds+1);
		grp.initializeFolds();
		grp.create(m_StatManager, m_NbFolds);	// Create total stat for each fold
		grp.calcTotalStats();
		// Call induce
		return xvalInduce(grp);
	}

	public ClusNode induce(ClusRun cr, MultiScore score) {
		return null;
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) {
		return null;
	}
}

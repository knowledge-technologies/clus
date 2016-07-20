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
import clus.tools.debug.Debug;


import jeans.util.list.*;
import jeans.resource.*;

import java.io.*;

import clus.main.*;
import clus.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.type.*;
import clus.model.test.*;
import clus.statistic.*;

public class OptXValIndOV extends OptXValInduce {

	protected ClusStatistic[][] m_TestExtraStat;

	public OptXValIndOV(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	public static void updateExtra(DataTuple tuple, ClusStatistic[] stats, int idx) {
		int[] folds = tuple.m_Folds;
		for (int j = 0; j < folds.length; j++) stats[folds[j]].updateWeighted(tuple, idx);
	}

	public final void findNominalOV(NominalAttrType at, OptXValGroup grp) {
		// Reset positive statistic
		int nbvalues = at.getNbValues();
		int statsize = nbvalues + at.intHasMissing();
		reset(statsize);
		resetExtra(statsize);
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

			if (tuple.m_Index != -1) {
				m_TestStat[tuple.m_Index][value].updateWeighted(tuple, i);
			} else {
				updateExtra(tuple, m_TestExtraStat[value], i);
			}
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
		for (int i = nb-1; i >= 0; i--) {
			int foldnr = grp.getFold(i);
			ClusStatistic[] cr_stat = m_TestStat[foldnr];
			if (foldnr != 0) {
				ClusStatistic[] zero_stat = m_TestStat[0];
				for (int j = 0; j < statsize; j++) {
					cr_stat[j].subtractFromOther(zero_stat[j]);
				}
			}
			for (int j = 0; j < statsize; j++) {
				cr_stat[j].add(m_TestExtraStat[j][foldnr]);
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

	public final void resetExtra(int nb) {
		for (int i = 0; i < nb; i++) {
			for (int j = 0; j <= m_NbFolds; j++) {
				m_TestExtraStat[i][j].reset();
			}
		}
	}

	public final void findNumericOV(NumericAttrType at, OptXValGroup grp) {
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
		ClusStatistic[] extra = m_TestExtraStat[0];
		ClusStatistic.reset(extra);
		// Missing values
		int first = 0;
		int nb_rows = data.getNbRows();
// should add extra somewhere???
/*		if (at.hasMissing()) {
			while (first < nb_rows && (tuple = data.getTuple(first)).hasNumMissing(idx)) {
				m_TestStat[tuple.m_Index][1].updateWeighted(tuple);
				first++;
			}
			substractMissing(grp);
		} else {*/
			copyTotal(grp);
/*		}*/
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
				int foldnr = tuple.m_Index;
				int crcl = tuple.getClassification();
				double value = tuple.getDoubleVal(idx);
				if (foldnr != -1) {
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
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.add(extra[cr_fold]);
								if (cr_fold != 0) m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[j].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
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

				} else {
					int ei = 0;
					int fi = 0;
					int[] efolds = tuple.m_Folds;
					while (ei < efolds.length && fi < folds.length) {
						if (efolds[ei] == folds[fi]) {
							int cr_fold = efolds[ei];
							if (m_PrevCl[fi] == -1 && value != m_PrevVl[fi] && m_PrevVl[fi] != Double.NaN) {
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
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.add(extra[cr_fold]);
								if (cr_fold != 0) m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[fi].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

								m_PrevCl[fi] = crcl;
							} else {
								if (m_PrevCl[fi] != crcl) m_PrevCl[fi] = -1;
							}
							m_PrevVl[fi] = value;
if (Debug.debug == 1) {
							ClusStat.deltaTest();
}

							extra[cr_fold].updateWeighted(tuple, i);
if (Debug.debug == 1) {
							ClusStat.deltaStat();
}

							ei++; fi++;
						} else if (efolds[ei] < folds[fi]) {
							ei++;
						} else {
							fi++;
						}
					}
				}
			}
		} else {
			for (int i = first; i < nb_rows; i++) {
				tuple = data.getTuple(i);
				boolean no_sum_calc = true;
				int foldnr = tuple.m_Index;
				double value = tuple.getDoubleVal(idx);
				if (foldnr != -1) {
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
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.add(extra[cr_fold]);
								if (cr_fold != 0) m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[j].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
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

				} else {
					int ei = 0;
					int fi = 0;
					int[] efolds = tuple.m_Folds;
					while (ei < efolds.length && fi < folds.length) {
						if (efolds[ei] == folds[fi]) {
							int cr_fold = efolds[ei];
							if (value != m_PrevVl[fi] && m_PrevVl[fi] != Double.NaN) {
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
if (Debug.debug == 1) {
								ClusStat.deltaTest();
}

								m_Scratch.copy(sum);
								m_Scratch.add(extra[cr_fold]);
								if (cr_fold != 0) m_Scratch.subtractFromThis(m_PosStat[cr_fold]);
if (Debug.debug == 1) {
								ClusStat.deltaStat();
}

								m_Selector[fi].updateNumeric(value, m_Scratch, at);
if (Debug.debug == 1) {
								ClusStat.deltaHeur();
}

							}
							m_PrevVl[fi] = value;
if (Debug.debug == 1) {
							ClusStat.deltaTest();
}

							extra[cr_fold].updateWeighted(tuple, i);
if (Debug.debug == 1) {
							ClusStat.deltaStat();
}

							ei++; fi++;
						} else if (efolds[ei] < folds[fi]) {
							ei++;
						} else {
							fi++;
						}
					}
				}
			}
		}
	}

	public final int mkNewGroups(OptXValGroup mgrp, MyListIter ngrps) {
		int nb_groups = 0;
		int nb = mgrp.getNbFolds();
		for (int i = 0; i < nb; i++) {
			ClusNode fnode = mgrp.getNode(i);
			if (fnode != null) {
				NodeTest mtest = fnode.m_Test;
				// Count number of matching
				int gsize = 0;
				boolean soft = false;
				SoftNumericTest stest = null;
				for (int j = i+1; j < nb; j++) {
					ClusNode onode = mgrp.getNode(j);
					if (onode != null) {
						int tres = mtest.softEquals(onode.m_Test);
						if (tres != NodeTest.N_EQ) gsize++;
						if (tres == NodeTest.S_EQ) soft = true;
					}
				}
				// Create group
				int gidx = 1;
				int fold = mgrp.getFold(i);
				OptXValGroup ngrp = new OptXValGroup(mgrp, gsize+1);
				if (soft) {
					stest = new SoftNumericTest(mtest, gsize+1);
					stest.addTest(0, fold, mtest);
					ngrp.setTest(stest);
					ngrp.setSoft();
				} else {
					ngrp.setTest(mtest);
				}
				ngrp.setFold(0, fold);
				if (gsize > 0) {
					for (int j = i+1; j < nb; j++) {
						ClusNode onode = mgrp.getNode(j);
						if (onode != null) {
							int tres = mtest.softEquals(onode.m_Test);
							if (tres != NodeTest.N_EQ) {
								fold = mgrp.getFold(j);
								if (stest != null) stest.addTest(gidx, fold, onode.m_Test);
								ngrp.setFold(gidx++, fold);
								mgrp.cleanNode(j);
							}
						}
					}
				}
				// Show best test
				if (stest != null) stest.sortIntervals();
				if (Settings.VERBOSE > 0) ngrp.println();
				ngrps.insertBefore(ngrp);
				nb_groups++;
			}
		}
		return nb_groups;
	}

	public final void findBestTestOV(OptXValGroup mgrp) {
		// First make nodes
		mgrp.makeNodes();
		// For each attribute
		RowData data = mgrp.getData();
		ClusSchema schema = data.getSchema();
		ClusAttrType[] attrs = schema.getDescriptiveAttributes();
		int nb_normal = attrs.length;
		for (int i = 0; i < nb_normal; i++) {
			ClusAttrType at = attrs[i];
			if (at instanceof NominalAttrType) findNominalOV((NominalAttrType)at, mgrp);
			else findNumericOV((NumericAttrType)at, mgrp);
		}
	}

	public final void xvalInduce(OptXValNode node, OptXValGroup mgrp) {
		long t0;
if (Debug.debug == 1) {
		t0 = ResourceInfo.getCPUTime();
}

if (Debug.debug == 1) {
		ClusStat.updateMaxMemory();
}

		node.init(mgrp.getFolds());
		mgrp.stopCrit(node);
		if (mgrp.cleanFolds()) return;
		// Optimize for one fold
		if (mgrp.getNbFolds() == 1) {
		        int fold = mgrp.getFold();
			ClusNode onode = new ClusNode();
			onode.m_ClusteringStat = mgrp.getTotStat(fold);
			node.setNode(fold, onode);
if (Debug.debug == 1) {
			ClusStat.deltaSplit();
}

			m_DFirst.induce(onode, mgrp.getData().getOVFoldData(fold));
			return;
		}
		// Init test selectors
		initTestSelectors(mgrp);
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

		if (mgrp.m_IsSoft) findBestTestOV(mgrp);
		else findBestTest(mgrp);
if (Debug.debug == 1) {
		ClusStat.deltaTest();
}

		mgrp.preprocNodes(node, this);
		// Make new groups
		MyListIter ngrps = new MyListIter();
		int nb_groups = mkNewGroups(mgrp, ngrps);
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

if (Debug.debug == 1) {
		node.m_Time = ResourceInfo.getCPUTime() - t0;
}

		// Recursive calls
		if (nb_groups > 0) {
			int idx = 0;
			node.setNbChildren(nb_groups);
			OptXValGroup grp = (OptXValGroup)ngrps.getFirst();
			while (grp != null) {
				NodeTest test = grp.getTest();
				OptXValSplit split = new OptXValSplit();
				int arity = split.init(grp.getFolds(), test);
				node.setChild(split, idx++);
				RowData gdata = grp.getData();
				if (grp.m_IsSoft) {
				    long t01;
if (Debug.debug == 1) {
					t01 = ResourceInfo.getCPUTime();
}

					for (int i = 0; i < arity; i++) {
						OptXValNode child = new OptXValNode();
						split.setChild(child, i);
						OptXValGroup cgrp = grp.cloneGroup();
						if (test.isSoft()) cgrp.setData(gdata.applySoft((SoftTest)test, i));
						else cgrp.setData(gdata.apply(test, i));
						cgrp.create(m_StatManager, m_NbFolds);
						if (cgrp.updateSoft()) cgrp.calcTotalStats(m_TestExtraStat[0]);
						else cgrp.calcTotalStats();
if (Debug.debug == 1) {
						node.m_Time += ResourceInfo.getCPUTime() - t01;
}

if (Debug.debug == 1) {
						ClusStat.deltaSplit();
}

						xvalInduce(child, cgrp);
if (Debug.debug == 1) {
						t01 = ResourceInfo.getCPUTime();
}

					}
				} else {
					long t01;
if (Debug.debug == 1) {
					t01 = ResourceInfo.getCPUTime();
}

					for (int i = 0; i < arity; i++) {
						OptXValNode child = new OptXValNode();
						split.setChild(child, i);
						OptXValGroup cgrp = grp.cloneGroup();
						cgrp.setData(gdata.apply(test, i));
						cgrp.create(m_StatManager, m_NbFolds);
						cgrp.calcTotalStats();
if (Debug.debug == 1) {
						node.m_Time += ResourceInfo.getCPUTime() - t01;
}

if (Debug.debug == 1) {
						ClusStat.deltaSplit();
}

						xvalInduce(child, cgrp);
if (Debug.debug == 1) {
						t01 = ResourceInfo.getCPUTime();
}

					}
				}
				grp = (OptXValGroup)ngrps.getNext();
			}
		}
	}

	public final void createExtraStats() {
		m_TestExtraStat = new ClusStatistic[m_MaxStats][m_NbFolds+1];
		for (int j = 0; j < m_MaxStats; j++) {
			for (int i = 0; i <= m_NbFolds; i++) {
				m_TestExtraStat[j][i] = m_StatManager.createClusteringStat();
			}
		}
	}

	public OptXValNode xvalInduce(OptXValGroup mgrp) {
		createExtraStats();
		OptXValNode root = new OptXValNode();
		xvalInduce(root, mgrp);
		return root;
	}
}

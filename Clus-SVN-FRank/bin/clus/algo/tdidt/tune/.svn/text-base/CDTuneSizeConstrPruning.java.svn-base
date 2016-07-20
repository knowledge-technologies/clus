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
 * Created on May 11, 2005
 */
package clus.algo.tdidt.tune;

import java.io.*;
import java.util.*;

import jeans.io.MyFile;
import jeans.math.*;

import clus.main.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.selection.*;
import clus.statistic.*;
import clus.util.*;
import clus.pruning.*;
import clus.algo.ClusInductionAlgorithmType;
import clus.algo.tdidt.ClusDecisionTree;
import clus.algo.tdidt.ClusNode;
import clus.data.ClusData;
import clus.data.rows.*;
import clus.data.attweights.*;
import clus.data.type.*;
import clus.error.*;
import clus.ext.hierarchical.HierRemoveInsigClasses;

/*
import org.apache.commons.math.distribution.*;
import org.apache.commons.math.*;
*/

public class CDTuneSizeConstrPruning extends ClusDecisionTree {

	protected ClusInductionAlgorithmType m_Class;
	protected ClusSchema m_Schema;
	protected ClusStatistic m_TotalStat;
	protected boolean m_HasMissing;
	protected int m_NbExamples;
//	protected TDistribution m_Distribution;
	protected int m_OrigSize;
	protected double m_RelErrAcc = 0.01;
	protected ArrayList m_Graph;
	protected int m_Optimal, m_MaxSize;
	protected ClusAttributeWeights m_TargetWeights;
	protected boolean m_Relative;
	protected double m_RelativeScale;

	public CDTuneSizeConstrPruning(ClusInductionAlgorithmType clss) {
		super(clss.getClus());
		m_Class = clss;
	}

	public void printInfo() {
		System.out.println("TDIDT (Tuning Size Constraint)");
		System.out.println("Heuristic: "+getStatManager().getHeuristicName());
	}

	private final void showFold(int i) {
		if (i != 0) System.out.print(" ");
		System.out.print(String.valueOf(i+1));
		System.out.flush();
	}

	public void setRelativeMeasure(boolean enable, double value) {
		m_Relative = enable;
		m_RelativeScale = value;
	}

	public void computeTestStatistics(ClusRun[] runs, int model, ClusError error) throws IOException, ClusException {
		TreeErrorComputer comp = new TreeErrorComputer();
		for (int i = 0; i < runs.length; i++) {
			ClusNode tree = (ClusNode)runs[i].getModelInfo(model).getModel();
			TreeErrorComputer.initializeTestErrors(tree, error);
			MemoryTupleIterator test = (MemoryTupleIterator)runs[i].getTestIter();
			test.init();
			DataTuple tuple = test.readTuple();
			while (tuple != null) {
				tree.applyModelProcessor(tuple, comp);
				tuple = test.readTuple();
			}
		}
	}

	public void computeErrorStandard(ClusNode tree, int model, ClusRun run) throws ClusException, IOException {
		ClusModelInfo mi = run.getModelInfo(model);
		ClusError err = mi.getTestError().getFirstError();
		MemoryTupleIterator test = (MemoryTupleIterator)run.getTestIter();
		test.init();
		DataTuple tuple = test.readTuple();
		while (tuple != null) {
			ClusStatistic pred = tree.predictWeighted(tuple);
			err.addExample(tuple, pred);
			tuple = test.readTuple();
		}
	}

	public SingleStatList computeTreeError(ClusRun[] runs, SizeConstraintPruning[] pruners, int model, ClusSummary summ, int size) throws ClusException, IOException {
		ClusModelInfo summ_mi = summ.getModelInfo(model);
		ClusError summ_err = summ_mi.getTestError().getFirstError();
		summ_err.reset();
		SingleStatList res = new SingleStatList(runs.length);
		for (int i = 0; i < runs.length; i++) {
			ClusModelInfo mi = runs[i].getModelInfo(model);
			ClusNode tree = (ClusNode)mi.getModel();
			if (size == 1) {
				tree = tree.cloneNodeWithVisitor();
			} else {
				int modelsize = tree.getModelSize();
				if (size < modelsize) {
					tree = tree.cloneTreeWithVisitors();
					pruners[i].pruneExecute(tree, size);
				}
			}
			if (getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL) {
				PruneTree pruner = new PruneTree();
				boolean bonf = getSettings().isUseBonferroni();
				HierRemoveInsigClasses hierpruner = new HierRemoveInsigClasses(runs[i].getPruneSet(), pruner, bonf, getStatManager().getHier());
				hierpruner.setSignificance(getSettings().getHierPruneInSig());
				hierpruner.prune(tree);
			}
			ClusError err = mi.getTestError().getFirstError();
			err.reset();
			if (m_HasMissing) {
				computeErrorStandard(tree, model, runs[i]);
			} else {
				TreeErrorComputer.computeErrorSimple(tree, err);
			}
			summ_err.add(err);
			MemoryTupleIterator test = (MemoryTupleIterator)runs[i].getTestIter();
			mi.getTestError().setNbExamples(test.getNbExamples());
			if (m_Relative) {
				res.addFloat(err.getModelError()/m_RelativeScale);
			} else {
				res.addFloat(err.getModelError());
			}
		}
		summ_mi.getTestError().setNbExamples(m_NbExamples);
/*
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
		summ_mi.getTestError().showError(summ, ClusModelInfo.TEST_ERR, wrt);
		wrt.print(" -> "+summ_err.getModelError());
		wrt.flush();
*/
		if (m_Relative) {
			res.setY(summ_err.getModelError()/m_RelativeScale);
		} else {
			res.setY(summ_err.getModelError());
		}
		return res;
	}

	public SingleStatList addPoint(ArrayList points, int size, ClusRun[] runs, SizeConstraintPruning[] pruners, int model, ClusSummary summ) throws ClusException, IOException {
		int pos = 0;
		while (pos < points.size() && ((SingleStatList)points.get(pos)).getX() < size) {
			pos++;
		}
		if (pos < points.size() && ((SingleStatList)points.get(pos)).getX() == size) {
			return null;
		}
		SingleStatList point = computeTreeError(runs, pruners, model, summ, size);
		point.setX(size);
		points.add(pos, point);
		return point;
	}

	public double getRange(ArrayList graph) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < graph.size(); i++) {
			SingleStatList elem = (SingleStatList)graph.get(i);
			if (elem.getY() < min) min = elem.getY();
			if (elem.getY() > max) max = elem.getY();
		}
		return Math.abs(max-min);
	}

	public void refineGraph(ArrayList graph, ClusRun[] runs, SizeConstraintPruning[] pruners, int model, ClusSummary summ) throws ClusException, IOException {
		int prevsize = -1;
		while (true) {
			boolean not_found = true;
			// double max_diff = getRange(graph);
			for (int i = 0; i < graph.size()-2 && not_found; i++) {
				SingleStatList e1 = (SingleStatList)graph.get(i);
				SingleStatList e2 = (SingleStatList)graph.get(i+1);
				if (Math.abs(e1.getY()-e2.getY()) > m_RelErrAcc) {
					int s1 = (int)e1.getX();
					int s2 = (int)e2.getX();
					int nmean = ((s1+s2)/2-1)/2;
					int smean = 2*nmean+1;
					if (smean != s1 && smean != s2 && (m_OrigSize == -1 || smean < m_OrigSize)) {
						addPoint(graph, smean, runs, pruners, model, summ);
						System.out.print("#");
						System.out.flush();
						/* we found a place to insert */
						not_found = false;
					}
				}
			}
			if (graph.size() == prevsize) return;
			prevsize = graph.size();
		}
	}

/*
	public int significanceTest(SingleStatList l1, SingleStatList l2, double level) {
		double probability = 0.5 + level/2; // 1.0 - (1.0 - level)/2;
		try {
			double t_value = m_Distribution.inverseCumulativeProbability(probability);
			double t_test = l1.getTValueSigTest(l2);
			if (t_test > t_value) {
				if (l1.getY() > l2.getY()) return 1;
				else return -1;
			} else {
				return 0;
			}
		} catch (MathException e) {
			System.err.println("Math error: "+e.getMessage());
			return 0;
		}
	}
*/

	public int findOptimalSize(ArrayList graph, boolean shouldBeLow) {
		double best_value = shouldBeLow ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		int best_index = -1;
		for (int i = 0; i < graph.size(); i++) {
			SingleStatList elem = (SingleStatList)graph.get(i);
			if (shouldBeLow) {
				if (elem.getY() < best_value) {
					best_value = elem.getY();
					best_index = i;
				}
			} else {
				if (elem.getY() > best_value) {
					best_value = elem.getY();
					best_index = i;
				}
			}
		}
		if (best_index == -1) {
			return 1;
		}
		// double max_diff = getRange(graph);
		SingleStatList best_elem = (SingleStatList)graph.get(best_index);
		System.out.print("["+best_elem.getX()+","+best_elem.getY()+"]");
		SingleStatList result = best_elem;
		int pos = best_index - 1;
		while (pos >= 0) {
			SingleStatList prev_elem = (SingleStatList)graph.get(pos);
			if (prev_elem.getX() >= 3 && Math.abs(prev_elem.getY()-best_elem.getY()) < m_RelErrAcc) {
				result = prev_elem;
				System.out.print(" < "+prev_elem.getX());
			}
			pos--;
		}
		return (int)result.getX();
	}

	public final XValMainSelection getXValSelection(Settings sett, int nbrows) throws IOException, ClusException {
		String value = sett.getTuneFolds();
		if (value.length() > 0 && Character.isDigit(value.charAt(0))) {
			try {
				int nbfolds = Integer.parseInt(value);
				Random random = new Random(0);
				return new XValRandomSelection(nbrows, nbfolds, random);
			} catch (NumberFormatException e) {
				throw new ClusException("Illegal number of folds: "+value);
			}
		} else {
			return XValDataSelection.readFoldsFile(value, nbrows);
		}
	}

	public void findBestSize(ClusData trset) throws ClusException, IOException {
		int prevVerb = Settings.enableVerbose(0);
		ClusStatManager mgr = getStatManager();
		ClusSummary summ = new ClusSummary();
		ClusErrorList errorpar = mgr.createDefaultError();
		errorpar.setWeights(m_TargetWeights);
		summ.setTestError(errorpar);
		int model = ClusModel.ORIGINAL;
		XValMainSelection sel = getXValSelection(getSettings(), trset.getNbRows());
		int nbfolds = sel.getNbFolds();
		ClusRun[] runs = new ClusRun[nbfolds];
		// Create trees based on cross-validation
		for (int i = 0; i < nbfolds; i++) {
			showFold(i);
			XValSelection msel = new XValSelection(sel, i);
			ClusRun cr = m_Clus.partitionDataBasic(trset, msel, summ, i+1);
			ClusModel tree = m_Class.induceSingleUnpruned(cr);
			cr.getModelInfo(model).setModel(tree);
			runs[i] = cr;
		}
		// Construct tree pruners
		int maxsize = 0;
		SizeConstraintPruning pruners[] = new SizeConstraintPruning[nbfolds];
		for (int i = 0; i < nbfolds; i++) {
			ClusNode tree = (ClusNode)runs[i].getModelInfo(model).getModel();
			int size = tree.getModelSize();
			if (m_OrigSize != -1 && size > m_OrigSize) size = m_OrigSize;
			if (size > maxsize) maxsize = size;
			SizeConstraintPruning pruner = new SizeConstraintPruning(size, mgr.getClusteringWeights());
			pruner.pruneInitialize(tree, size);
			pruners[i] = pruner;
		}
		if (maxsize == 1) {
			System.out.println("Optimal size (maxsize = 1) = 1");
			m_Class.getSettings().setSizeConstraintPruning(1);
			return;
		}
		// Get training data in trees
		ClusError error = summ.getModelInfo(model).getTestError().getFirstError();
		if (!m_HasMissing) {
			computeTestStatistics(runs, model, error);
		}
		// Compute errors of default models
		ArrayList graph = new ArrayList();
		setRelativeMeasure(false, 0.0);
		SingleStatList point = computeTreeError(runs, pruners, model, summ, 1);
		setRelativeMeasure(true, point.getY());
		System.out.print(" ");
		System.out.print("<"+point.getY()+">");
		addPoint(graph, 1, runs, pruners, model, summ);
		addPoint(graph, maxsize, runs, pruners, model, summ);
		// Add trees with exponentially increasing size
		int n = 1;
		boolean shouldBeLow = error.shouldBeLow();
		while (true) {
			int size = (int)(Math.pow(2, n)+1.0);
			if (size > maxsize) break;
			if (m_OrigSize != -1 && size > m_OrigSize) break;
			SingleStatList new_pt = addPoint(graph, size, runs, pruners, model, summ);
			if (new_pt == null) {
				break;
			} else {
				if (shouldBeLow) {
					if (graph.size() > 5 && new_pt.getY() > 1.1) {
						break;
					}
				} else {
					if (graph.size() > 5 && new_pt.getY() < 0.9) {
						break;
					}
				}
				System.out.print("*");
				System.out.flush();
			}
			n++;
		}
		refineGraph(graph, runs, pruners, model, summ);
		int optimalSize = findOptimalSize(graph, shouldBeLow);
		System.out.println(" Best = "+optimalSize);
		// Write dat file
		setFinalResult(graph, optimalSize, maxsize);
		getSettings().setSizeConstraintPruning(optimalSize);
		Settings.enableVerbose(prevVerb);
	}

	public void saveInformation(String fname) {
		System.out.println("Saving: "+fname+".dat");
		MyFile file = new MyFile(fname+".dat");
		file.log(""+m_Optimal+"\t"+m_MaxSize);
		for (int i = 0; i < m_Graph.size(); i++) {
			SingleStatList elem = (SingleStatList)m_Graph.get(i);
			file.log(""+elem.getX()+"\t"+elem.getY());
		}
		file.close();
	}

	public void setFinalResult(ArrayList graph, int optimal, int maxsize) {
		m_Graph = graph;
		m_Optimal = optimal;
		m_MaxSize = maxsize;
	}

	public ClusModel induceSingle(ClusRun cr) {
		System.out.println(">>> Error: induceSingle/1 not implemented");
		return null;
	}

	public ClusStatistic createTotalStat(RowData data) {
		ClusStatistic stat = m_Class.getStatManager().createClusteringStat();
		data.calcTotalStatBitVector(stat);
		return stat;
	}

	public void induceAll(ClusRun cr) throws ClusException {
		try {
			long start_time = System.currentTimeMillis();
			m_OrigSize = getSettings().getSizeConstraintPruning(0);
			if (getSettings().getSizeConstraintPruningNumber() > 1) {
				throw new ClusException("Only one value is allowed for MaxSize if -tunesize is given");
			}
			RowData train = (RowData)cr.getTrainingSet();
			m_Schema = train.getSchema();
			m_HasMissing = m_Schema.hasMissing();
			m_TotalStat = createTotalStat(train);
			m_NbExamples = train.getNbRows();
//			m_Distribution = DistributionFactory.newInstance().createTDistribution(getSettings().getTuneNbFolds()-1);
			System.out.println("Has missing values: "+m_HasMissing);
			m_TargetWeights = m_Class.getStatManager().getClusteringWeights();
			// Find optimal F-test value
			findBestSize(train);
			System.out.println();
			// Induce final model
			m_Class.induceAll(cr);
			getSettings().setSizeConstraintPruning(m_OrigSize);
			long time = (System.currentTimeMillis()-start_time);
			if (Settings.VERBOSE > 0) System.out.println("Time: "+(double)time/1000+" sec");
			cr.setInductionTime(time);
		} catch (ClusException e) {
		    System.err.println("Error: "+e);
		} catch (IOException e) {
		    System.err.println("IO Error: "+e);
		}
	}
}

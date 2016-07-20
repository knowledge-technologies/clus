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

package clus.ext.ilevelc;

import java.io.*;
import java.util.*;

import clus.algo.tdidt.*;
import clus.data.attweights.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.model.ClusModel;
import clus.model.test.*;
import clus.main.*;
import clus.util.*;

public class ILevelCInduce extends DepthFirstInduce {

	protected NodeTest m_BestTest;
	protected ClusNode m_BestLeaf;
	protected RowDataSortHelper m_SortHelper = new RowDataSortHelper();
	protected double m_BestHeur = Double.POSITIVE_INFINITY;
	protected int m_NbClasses = 1;
	protected int m_MaxNbClasses = 2;
	protected double m_MinLeafWeight;
	protected int m_NbTrain;
	protected double m_GlobalSS;
	protected ArrayList m_Constraints;
	protected int[][] m_ConstraintsIndex;
	protected ClusNormalizedAttributeWeights m_Scale;

	public ILevelCInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	public int[][] createConstraintsIndex() {
		/* create index as array lists */
		ArrayList[] crIndex = new ArrayList[m_NbTrain];
		for (int i = 0; i < m_Constraints.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
			int t1 = ic.getT1().getIndex();
			int t2 = ic.getT2().getIndex();
			if (crIndex[t1] == null) crIndex[t1] = new ArrayList();
			if (crIndex[t2] == null) crIndex[t2] = new ArrayList();
			crIndex[t1].add(new Integer(i));
			crIndex[t2].add(new Integer(i));
		}
		/* copy it to final int matrix */
		int[][] index = new int[m_NbTrain][];
		for (int i = 0; i < m_NbTrain; i++) {
			if (crIndex[i] != null) {
				int nb = crIndex[i].size();
				index[i] = new int[nb];
				for (int j = 0; j < nb; j++) {
					Integer value = (Integer)crIndex[i].get(j);
					index[i][j] = value.intValue();
				}
			}
		}
		return index;
	}

	public ILevelConstraint[] getSubsetConstraints(RowData data) {
		int count = 0;
		boolean[] constr = new boolean[m_Constraints.size()];
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = (DataTuple)data.getTuple(i);
			int[] index = m_ConstraintsIndex[tuple.getIndex()];
			if (index != null) {
				for (int j = 0; j < index.length; j++) {
					int cid = index[j];
					if (!constr[cid]) {
						constr[cid] = true;
						count++;
					}
				}
			}
		}
		int pos = 0;
		ILevelConstraint[] result = new ILevelConstraint[count];
		for (int i = 0; i < m_Constraints.size(); i++) {
			if (constr[i]) result[pos++] = (ILevelConstraint)m_Constraints.get(i);
		}
		return result;
	}

	public int[] createIE(RowData data) {
		int[] ie = new int[m_NbTrain];
	    for (int i = 0; i < data.getNbRows(); i++) {
	  		ie[data.getTuple(i).getIndex()] = ILevelCHeurStat.NEG;
	  	}
	    return ie;
	}

	public double computeHeuristic(int violated, double ss) {
		double ss_norm = ss / m_GlobalSS;
		double violated_norm = 1.0 * violated / m_Constraints.size();
		double alpha = getSettings().getILevelCAlpha();
		double heur = (1.0-alpha) * ss_norm + alpha * violated_norm;
		// System.out.println("Violated: "+violated+" SS: "+ss+" -> "+heur);
		return heur;
	}

	public double computeHeuristic(ILevelCHeurStat pos, ILevelCHeurStat neg, ILevelCStatistic ps, boolean use_p_lab, double ss_offset, int nb_violated) {
		if (pos.getTotalWeight() < m_MinLeafWeight) return Double.POSITIVE_INFINITY;
		if (neg.getTotalWeight() < m_MinLeafWeight) return Double.POSITIVE_INFINITY;
		int pLabel = -1;
		int nbLabels = m_NbClasses;
		if (use_p_lab) {
			/* if does not occur elsewhere then it becomes available for reuse */
			nbLabels = m_NbClasses-1;
			pLabel = ps.getClusterID();
		}
		int v1 = tryLabel(pos, neg, pLabel, nbLabels);
		int v2 = tryLabel(neg, pos, pLabel, nbLabels);
		if (v1 == -1) {
			if (v2 != -1) nb_violated += v2;
			else return Double.POSITIVE_INFINITY;
		} else if (v2 == -1) {
			if (v1 != -1) nb_violated += v1;
			else return Double.POSITIVE_INFINITY;
		} else {
			nb_violated += Math.min(v1, v2);
		}
		double ss_pos = pos.getSVarS(m_Scale);
		double ss_neg = neg.getSVarS(m_Scale);
		double ss = ss_offset + ss_pos + ss_neg;
		return computeHeuristic(nb_violated, ss);
	}

	public void findNumericConstraints(NumericAttrType at, ClusNode leaf, boolean use_p_lab, double ss_offset, int violated_offset, int violated_leaf, int[] clusters) throws ClusException {
		RowData data = (RowData)leaf.getVisitor();
		ILevelCStatistic tot = (ILevelCStatistic)leaf.getClusteringStat();
		// System.out.println("Trying: "+at.getName());
		int idx = at.getArrayIndex();
	    if (at.isSparse()) {
	      data.sortSparse(at, m_SortHelper);
	    } else {
	      data.sort(at);
	    }
	    int nb_rows = data.getNbRows();
	    if (at.hasMissing()) {
	      throw new ClusException("Does not support attributes with missing values: "+at.getName());
	    }
	    /* test is of form A > vi */
	    /* vi is sorted from large to small */
	    /* pos statistic is values larger than current threshold */
	    /* neg statistic is values smaller than current threshold */
	    ILevelCStatistic cs = (ILevelCStatistic)leaf.getClusteringStat();
	    ILevelCHeurStat pos = new ILevelCHeurStat(cs, m_NbClasses);
	    ILevelCHeurStat neg = new ILevelCHeurStat(cs, m_NbClasses);
	    /* create internal/external index */
	    int[] ie = createIE(data);
	    /* pass required indices to statistics */
	    pos.setIndices(m_ConstraintsIndex, m_Constraints, ie, clusters);
	    neg.setIndices(m_ConstraintsIndex, m_Constraints, ie, clusters);
	    /* initially all data is in negative statistic */
	    for (int i = 0; i < nb_rows; i++) {
	  		DataTuple tuple = data.getTuple(i);
	  		neg.updateWeighted(tuple, tuple.getWeight());
	  	}
	    int nb_violated = violated_leaf;
	    /* then loop over all examples */
	    double prev = Double.NaN;
	  	for (int i = 0; i < nb_rows; i++) {
	  		DataTuple tuple = data.getTuple(i);
	  		double value = tuple.getDoubleVal(idx);
	  		if (value != prev && prev != Double.NaN) {
  				// m_Selector.updateNumeric(value, at);
	  			double heuristic = computeHeuristic(pos, neg, tot, use_p_lab, ss_offset, violated_offset+nb_violated);
	  			if (heuristic < m_BestHeur) {
	  				m_BestHeur = heuristic;
	  				m_BestLeaf = leaf;
	  				double pos_freq = pos.getTotalWeight() / tot.getTotalWeight();
	  				double splitpoint = (value + prev) / 2.0;
	  				m_BestTest = new NumericTest(at, splitpoint, pos_freq);
	  			}
	  			prev = value;
	  		}
	  		pos.updateWeighted(tuple, tuple.getWeight());
	  		neg.removeWeighted(tuple, tuple.getWeight());
			int tidx = tuple.getIndex();
			int[] cidx = m_ConstraintsIndex[tidx];
			if (cidx != null) {
				for (int j = 0; j < cidx.length; j++) {
					ILevelConstraint cons = (ILevelConstraint)m_Constraints.get(cidx[j]);
					int otidx = cons.getOtherTupleIdx(tuple);
					if (ie[otidx] != ILevelCHeurStat.EXT) {
						boolean was_violated = false;
						if (cons.getType() == ILevelConstraint.ILevelCMustLink) {
							if (ie[tidx] != ie[otidx]) was_violated = true;
						} else {
							if (ie[tidx] == ie[otidx]) was_violated = true;
						}
						if (was_violated) nb_violated--;
						else nb_violated++;
					}
				}
			}
	  		ie[tidx] = ILevelCHeurStat.POS;
	  	}
	}

	public void tryGivenLeaf(ClusNode leaf, boolean use_p_lab, int violated, double ss, int[] clusters) throws ClusException {
		ILevelCStatistic stat = (ILevelCStatistic)leaf.getClusteringStat();
		if (stat.getTotalWeight() <= m_MinLeafWeight) {
			/* don't refine clusters that are too small */
			return;
		}
		RowData leaf_data = (RowData)leaf.getVisitor();
		double ss_leaf = stat.getSVarS(m_Scale);
		double ss_offset = ss - ss_leaf;
		int[] v_info = countViolatedConstaints(leaf_data, clusters);
		int violated_offset = violated - v_info[0];
		System.out.println("Violated by leaf: "+v_info[0]+" internal ML: "+v_info[1]+" (of "+violated+" total)");
		ClusSchema schema = getSchema();
		ClusAttrType[] attrs = schema.getDescriptiveAttributes();
		for (int i = 0; i < attrs.length; i++) {
			ClusAttrType at = attrs[i];
			if (at instanceof NumericAttrType) findNumericConstraints((NumericAttrType)at, leaf, use_p_lab, ss_offset, violated_offset, v_info[1], clusters);
			else throw new ClusException("Unsupported descriptive attribute type: "+at.getName());
		}
	}

	public void tryEachLeaf(ClusNode tree, ClusNode root, int violated, double ss, int[] clusters) throws ClusException {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ILevelCStatistic ps = (ILevelCStatistic)tree.getClusteringStat();
			int nbParLabel = countLabel(root, ps.getClusterID());
			tryGivenLeaf(tree, nbParLabel <= 1, violated, ss, clusters);
		} else {
			for (int i = 0; i < nb_c; i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				tryEachLeaf(child, root, violated, ss, clusters);
			}
		}
	}

	public ILevelCHeurStat computeCHeurStat(ClusNode leaf, ClusNode par, int[] ie, int[] clusters) {
		RowData data = (RowData)leaf.getVisitor();
		ILevelCStatistic cs = (ILevelCStatistic)leaf.getClusteringStat();
	    ILevelCHeurStat lstat = new ILevelCHeurStat(cs, m_NbClasses);
	    lstat.setIndices(m_ConstraintsIndex, m_Constraints, ie, clusters);
	    for (int i = 0; i < data.getNbRows(); i++) {
	  		DataTuple tuple = data.getTuple(i);
	  		lstat.updateWeighted(tuple, tuple.getWeight());
	  	}
	    return lstat;
	}

	public int tryLabel(ILevelCHeurStat a, ILevelCHeurStat b, int parlabel, int nb) {
		int v1 = a.computeMinimumExtViolated(parlabel, -1, nb < m_MaxNbClasses);
		int label_a = a.getClusterID();
		if (label_a == -1) nb++;
		int v2 = b.computeMinimumExtViolated(parlabel, label_a, nb < m_MaxNbClasses);
		return (v1 == -1 || v2 == -1) ? -1 : v1 + v2;
	}

	public void assignLabels(ILevelCHeurStat a, ILevelCHeurStat b, ClusNode root, int parlabel, int nb) throws ClusException {
		int v1 = a.computeMinimumExtViolated(parlabel, -1, nb < m_MaxNbClasses);
		int label_a = a.getClusterID();
		if (label_a == -1) {
			nb++;
			label_a = freeLabel(root, -1);
			m_NbClasses = Math.max(label_a+1, m_NbClasses);
			a.setClusterID(label_a);
		}
		int v2 = b.computeMinimumExtViolated(parlabel, label_a, nb < m_MaxNbClasses);
		if (b.getClusterID() == -1) {
			int label_b = freeLabel(root, label_a);
			m_NbClasses = Math.max(label_b+1, m_NbClasses);
			b.setClusterID(label_b);
		}
		if (v1 == -1 || v2 == -1) {
			throw new ClusException("Error: can't assign labels: v1 = "+v1+" v2 = "+v2);
		}
	}

	public void storeLabels(ClusNode leaf, ILevelCHeurStat stat) {
		ILevelCStatistic cs = (ILevelCStatistic)leaf.getClusteringStat();
		ILevelCStatistic ts = (ILevelCStatistic)leaf.getTargetStat();
	    cs.setClusterID(stat.getClusterID());
	    ts.setClusterID(stat.getClusterID());
	}

	public void enterBestTest(ClusNode tree, ClusNode root, int[] clusters) throws ClusException {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			if (tree == m_BestLeaf) {
				RowData data = (RowData)tree.getVisitor();
				tree.setTest(m_BestTest);
				int arity = tree.updateArity();
				NodeTest test = tree.getTest();
				for (int j = 0; j < arity; j++) {
					ClusNode child = new ClusNode();
					tree.setChild(child, j);
					RowData subset = data.applyWeighted(test, j);
					child.initClusteringStat(getStatManager(), subset);
					child.initTargetStat(getStatManager(), subset);
					child.getTargetStat().calcMean();
					child.setVisitor(subset);
				}
				int[] ie = createIE(data);
				ILevelCStatistic ps = (ILevelCStatistic)tree.getClusteringStat();
				ILevelCHeurStat left = computeCHeurStat((ClusNode)tree.getChild(0), tree, ie, clusters);
				ILevelCHeurStat right = computeCHeurStat((ClusNode)tree.getChild(1), tree, ie, clusters);
				int nbParLabel = countLabel(root, ps.getClusterID());
				int pLabel = -1;
				int nbLabels = m_NbClasses;
				if (nbParLabel <= 0) {
					/* if does not occur elsewhere then it becomes available for reuse */
					nbLabels = m_NbClasses-1;
					pLabel = ps.getClusterID();
				}
				int v1 = tryLabel(left, right, pLabel, nbLabels);
				int v2 = tryLabel(right, left, pLabel, nbLabels);
				if (v1 == -1 || v2 == -1) {
					v1 = tryLabel(left, right, pLabel, nbLabels);
					v2 = tryLabel(right, left, pLabel, nbLabels);
				}
				if ((v1 <= v2 && v1 != -1) || v2 == -1) {
					assignLabels(left, right, root, pLabel, nbLabels);
				} else {
					assignLabels(right, left, root, pLabel, nbLabels);
				}
				storeLabels((ClusNode)tree.getChild(0), left);
				storeLabels((ClusNode)tree.getChild(1), right);
				tree.setVisitor(null);
			}
		} else {
			for (int i = 0; i < nb_c; i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				enterBestTest(child, root, clusters);
			}
		}
	}

	public int freeLabel(ClusNode tree, int ignore) {
		boolean[] set = new boolean[m_NbClasses];
		labelSet(tree, set);
		for (int i = 0; i < set.length; i++) {
			if (!set[i] && i != ignore) return i;
		}
		if (m_NbClasses < m_MaxNbClasses) return m_NbClasses;
		else return -1;
	}

	public void labelSet(ClusNode tree, boolean[] set) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ILevelCStatistic cs = (ILevelCStatistic)tree.getClusteringStat();
			if (cs.getClusterID() != -1) set[cs.getClusterID()] = true;
		} else {
			for (int i = 0; i < nb_c; i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				labelSet(child, set);
			}
		}
	}

	public int countLabel(ClusNode tree, int label) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ILevelCStatistic cs = (ILevelCStatistic)tree.getClusteringStat();
			return cs.getClusterID() == label ? 1 : 0;
		} else {
			int count = 0;
			for (int i = 0; i < nb_c; i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				count += countLabel(child, label);
			}
			return count;
		}
	}

	public void iLevelCInduce(ClusNode root) throws ClusException {
		double ss = root.estimateClusteringSS(m_Scale);
		int[] clusters = assignAllInstances(root);
		int violated = countViolatedConstaints(clusters);
		computeHeuristic(violated, ss);
		m_BestHeur = Double.POSITIVE_INFINITY;
		while (true) {
			/* reset current best values */
			m_BestTest = null;
			m_BestLeaf = null;
			/* try to refine each leaf */
			tryEachLeaf(root, root, violated, ss, clusters);
			if (m_BestTest == null) {
				/* no improvement possible */
				return;
			} else {
				/* refine tree and continue */
				enterBestTest(root, root, clusters);
				System.out.println("Tree:");
				root.printTree();
				ss = root.estimateClusteringSS(m_Scale);
				clusters = assignAllInstances(root);
				violated = countViolatedConstaints(clusters);
				double heur = computeHeuristic(violated, ss);
				if (Math.abs(heur - m_BestHeur) > 1e-6) {
					throw new ClusException("Error: heuristic "+heur+" <> "+m_BestHeur);
				}
				System.out.println("CHECK heuristic "+heur+" == "+m_BestHeur+" [OK]");
			}
		}
	}

	public void assignAllInstances(ClusNode tree, int[] clusters) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ILevelCStatistic stat = (ILevelCStatistic)tree.getClusteringStat();
			stat.assignInstances((RowData)tree.getVisitor(), clusters);
		}
		for (int i = 0; i < nb_c; i++) {
			ClusNode child = (ClusNode)tree.getChild(i);
			assignAllInstances(child, clusters);
		}
	}

	public int[] assignAllInstances(ClusNode root) {
		int[] clusters = new int[m_NbTrain];
		assignAllInstances(root, clusters);
		return clusters;
	}

	public int[] countViolatedConstaints(RowData data, int[] clusters) {
		int violated = 0;
		int violated_internal = 0;
		int[] ie = createIE(data);
		ILevelConstraint[] constr = getSubsetConstraints(data);
		for (int i = 0; i < constr.length; i++) {
			ILevelConstraint ic = constr[i];
			int type = ic.getType();
			int t1 = ic.getT1().getIndex();
			int t2 = ic.getT2().getIndex();
			if (type == ILevelConstraint.ILevelCMustLink) {
				if (clusters[t1] != clusters[t2]) violated++;
			} else {
				if (clusters[t1] == clusters[t2]) {
					violated++;
					if (ie[t1] != ILevelCHeurStat.EXT && ie[t2] != ILevelCHeurStat.EXT) {
						violated_internal++;
					}
				}
			}
		}
		int[] result = new int[2];
		result[0] = violated;
		result[1] = violated_internal;
		return result;
	}

	public int countViolatedConstaints(int[] clusters) {
		int violated = 0;
		for (int i = 0; i < m_Constraints.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
			int type = ic.getType();
			int t1 = ic.getT1().getIndex();
			int t2 = ic.getT2().getIndex();
			if (type == ILevelConstraint.ILevelCMustLink) {
				if (clusters[t1] != clusters[t2]) violated++;
			} else {
				if (clusters[t1] == clusters[t2]) violated++;
			}
		}
		return violated;
	}

	public ArrayList createConstraints(RowData data, int nbRows) {
		ArrayList constr = new ArrayList();
		ClusAttrType type = getSchema().getAttrType(getSchema().getNbAttributes()-1);
		if (type.getTypeIndex() == NominalAttrType.THIS_TYPE) {
			NominalAttrType cls = (NominalAttrType)type;
			m_MaxNbClasses = cls.getNbValues();
			int nbConstraints = getSettings().getILevelCNbRandomConstraints();
			for (int i = 0; i < nbConstraints; i++) {
				int t1i = ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, nbRows);
				int t2i = ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, nbRows);
				DataTuple t1 = data.getTuple(t1i);
				DataTuple t2 = data.getTuple(t2i);
				if (cls.getNominal(t1) == cls.getNominal(t2)) {
					constr.add(new ILevelConstraint(t1, t2, ILevelConstraint.ILevelCMustLink));
				} else {
					constr.add(new ILevelConstraint(t1, t2, ILevelConstraint.ILevelCCannotLink));
				}
			}
		}
		return constr;
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		m_NbClasses = 1;
		ClusRandom.reset(ClusRandom.RANDOM_ALGO_INTERNAL);
		RowData data = (RowData)cr.getTrainingSet();
		int nbTrain = data.getNbRows();
		/* add in test data! */
		RowData test = (RowData)cr.getTestSet();
		if (test != null) {
			ArrayList allData = new ArrayList();
			data.addTo(allData);
			test.addTo(allData);
			data = new RowData(allData, data.getSchema());
		}
		System.out.println("All data: "+data.getNbRows());
		/* and process it ... */
		data.addIndices();
		m_NbTrain = data.getNbRows();
		m_MinLeafWeight = getSettings().getMinimalWeight();
		ArrayList points = data.toArrayList();
		/* load constraints from file */
		if (getSettings().hasILevelCFile()) {
			String fname = getSettings().getILevelCFile();
			m_Constraints = ILevelConstraint.loadConstraints(fname, points);
		} else {
			/* constraints are only on the training instances */
			/* reason: nbTrain = number of training instances and training instances come first */
			m_Constraints = createConstraints(data, nbTrain);
		}
		if (getSettings().isILevelCCOPKMeans()) {
			COPKMeans km = new COPKMeans(m_MaxNbClasses, getStatManager());
			COPKMeansModel model = null;
			int sumIter = 0;
			long t1 = System.currentTimeMillis();
			for (int i = 0; i < 100000; i++) {
				model = (COPKMeansModel)km.induce(data, m_Constraints);
				sumIter = Math.max(sumIter, model.getIterations());
				model.setCSets(i+1);
				model.setAvgIter(sumIter);
				long t2 = System.currentTimeMillis();
				// if (!model.isIllegal() || (t2-t1) > 5*60*1000) return model;
				if (!model.isIllegal()) return model;
				m_Constraints = createConstraints(data, nbTrain);
			}
			return model;
		} else if (getSettings().isILevelCMPCKMeans()) {
			MPCKMeansWrapper wrap = new MPCKMeansWrapper(getStatManager());
			return wrap.induce(data, test, m_Constraints, m_MaxNbClasses);
		} else {
			/* add derived constraints */
			DerivedConstraintsComputer comp = new DerivedConstraintsComputer(points, m_Constraints);
			comp.compute();
			m_ConstraintsIndex = createConstraintsIndex();
			System.out.println("Number of instance level constraints: "+m_Constraints.size());
			/* create initial node */
			ClusNode root = new ClusNode();
			root.initClusteringStat(m_StatManager, data);
			root.initTargetStat(m_StatManager, data);
			root.setVisitor(data);
			/* assign root examples to initial cluster */
			ILevelCStatistic ilevels = (ILevelCStatistic)root.getClusteringStat();
			ilevels.setClusterID(0);
			/* initialize scale */
			m_Scale = (ClusNormalizedAttributeWeights)getStatManager().getClusteringWeights();
			m_GlobalSS = ilevels.getSVarS(m_Scale);
			System.out.println("Global SS: "+m_GlobalSS);
			/* induce tree now */
			initSelectorAndSplit(root.getClusteringStat());
			iLevelCInduce(root);
			root.postProc(null);
			cleanSplit();
			return root;
		}
	}
}

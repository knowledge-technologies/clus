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
 * Created on May 1, 2005
 */
package clus.algo.rules;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import jeans.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.*;
import clus.main.*;
import clus.statistic.*;
import clus.model.ClusModel;
import clus.model.test.*;
import clus.util.*;
import clus.data.type.*;
import clus.error.*;
//import clus.ext.ilevelc.ILevelCStatistic;
import clus.ext.ilevelc.ILevelConstraint;

public class ClusRule implements ClusModel, Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_ID;
	protected Object m_Visitor;
	protected ArrayList<ILevelConstraint> m_Constraint;
	/** Target statistics of examples covered by the rule.*/
	protected ClusStatistic m_TargetStat;
	/** Clustering statistics of examples covered by the rule.*/
	protected ClusStatistic m_ClusteringStat;
	protected ArrayList m_Tests = new ArrayList();
	protected ClusStatManager m_StatManager;
	protected ClusErrorList[] m_Errors;

	/** Array of tuples covered by this rule */
	protected ArrayList m_Data = new ArrayList();

	/** Combined statistics for training and testing data */
	protected CombStat[] m_CombStat = new CombStat[2];

	/** Number of examples covered by this rule
	 * 	Index is 0 for train set, 1 for test set*/
	protected double[] m_Coverage = new double[2];

	/** Average error score of the rule */
	protected double m_TrainErrorScore;

	/** Optimized weight of the rule */
	protected double m_OptWeight;
	
	public ClusRule(ClusStatManager statManager) {
		m_StatManager = statManager;
		m_TrainErrorScore = -1;
		m_OptWeight = -1;
	}

	public int getID() {
		return m_ID;
	}

	public void setID(int id) {
		m_ID = id;
	}
    /** Returns the prediction by this rule for the tuple. If the value is invalid, returns 0.
     */
	public ClusStatistic predictWeighted(DataTuple tuple) {
		return m_TargetStat;
	}

	/**
	 * Computes predictions for the rule by taking the mean of covered examples.
	 */
	public void computePrediction() {
		m_TargetStat.calcMean();
		m_ClusteringStat.calcMean();
		// setTrainErrorScore();
	}


	public ClusRule cloneRule() {
		ClusRule new_rule = new ClusRule(m_StatManager);
		for (int i = 0; i < getModelSize(); i++) {
			new_rule.addTest(getTest(i));
		}
		return new_rule;
	}


	/**
	 * Equality is based on the tests only.
	 */
	public boolean equals(Object other) {
		ClusRule o = (ClusRule)other;
		if (o.getModelSize() != getModelSize()) return false;
		for (int i = 0; i < getModelSize(); i++) {
			boolean has_test = false;
			for (int j = 0; j < getModelSize() && !has_test; j++) {
				if (getTest(i).equals(o.getTest(j))) has_test = true;
			}
			if (!has_test) return false;
		}
		return true;
	}

	static private final double EQUAL_MAX_DIFFER = 1E-6;
	/**
	 * Equality is based on the tests AND the targets.
	 */
	public boolean equalsDeeply(Object other) {
		ClusRule o = (ClusRule)other;
		if (o.getModelSize() != getModelSize()) return false;
		for (int i = 0; i < getModelSize(); i++) {
			boolean has_test = false;
			for (int j = 0; j < getModelSize() && !has_test; j++) {
				if (getTest(i).equals(o.getTest(j))) has_test = true;
			}
			if (!has_test) return false;
		}

		// Tests are similar -- let us check the target
		double ruleTarget[] = ((RegressionStat)o.m_TargetStat).m_Means;

		// Let us check if the targets are different
		boolean targetsAreSimilar = true;
		for (int iTarget = 0; iTarget < ruleTarget.length && targetsAreSimilar; iTarget++)
		{
			if (Math.abs(ruleTarget[iTarget]-((RegressionStat)m_TargetStat).m_Means[iTarget]) >= EQUAL_MAX_DIFFER)
				targetsAreSimilar = false;
		}

		return targetsAreSimilar;
	}

	public int hashCode() {
		int hashCode = 1234;
		for (int i = 0; i < getModelSize(); i++) {
			hashCode += getTest(i).hashCode();
		}
		return hashCode;
	}

	/** Does the rule tests cover the given tuple */
	public boolean covers(DataTuple tuple) {
		for (int i = 0; i < getModelSize(); i++) {
			NodeTest test = getTest(i);
			int res = test.predictWeighted(tuple);
			if (res != ClusNode.YES) return false;
		}
		return true;
	}

	public void simplify() {
		for (int i = getModelSize()-1; i >= 0; i--) {
			boolean found = false;
			NodeTest test_i = getTest(i);
			for (int j = 0; j < i && !found; j++) {
				NodeTest test_j = getTest(j);
				NodeTest simplify = test_j.simplifyConjunction(test_i);
				if (simplify != null) {
					setTest(j, simplify);
					found = true;
				}
			}
			if (found) removeTest(i);
		}
	}

	public RowData removeCovered(RowData data) {
		int covered = 0;
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			if (covers(tuple)) covered++;
		}
		int idx = 0;
		RowData res = new RowData(data.getSchema(), data.getNbRows()-covered);
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			// System.out.print(tuple.m_Index);
			if (!covers(tuple)) {
				res.setTuple(tuple, idx++);
				// System.out.println(tuple.m_Index);
			}
		}
//		Iterator<DataTuple> ires = res.toArrayList().iterator();
//		System.out.println("remaining data:");
//		while(ires.hasNext()){
//			DataTuple dt = ires.next();
//			System.out.print(dt.m_Index);
//		}
		return res;
	}

	public RowData computeCovered(RowData data) {
		int covered = 0;
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			if (covers(tuple)) covered++;
		}
		int idx = 0;
		RowData res = new RowData(data.getSchema(), covered);
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			if (covers(tuple)) res.setTuple(tuple, idx++);
		}
		return res;
	}

	/**
	 * Removes examples that have been covered by enough rules,
	 * i.e., rules that have weights below the threshold.
	 */
	public RowData removeCoveredEnough(RowData data) {
		double threshold = getSettings().getInstCoveringWeightThreshold();
		// if (!getSettings().isCompHeurRuleDist()) { // TODO: check if this is ok!
		if (true) {
			double covered = 0;
			for (int i = 0; i < data.getNbRows(); i++) {
				DataTuple tuple = data.getTuple(i);
				if (tuple.m_Weight < threshold) {
					covered++;
				}
			}
			int idx = 0;
			RowData res;
			res = new RowData(data.getSchema(), (int)(data.getNbRows() - covered));
			for (int i = 0; i < data.getNbRows(); i++) {
				DataTuple tuple = data.getTuple(i);
				if (!(tuple.m_Weight < threshold)) {
					res.setTuple(tuple, idx++);
				}
			}
			return res;
		} else {	// Don't remove, just set the weights to zero.
					// TODO: Check if this causes any problems
			for (int i = 0; i < data.getNbRows(); i++) {
				DataTuple tuple = data.getTuple(i);
				if (tuple.m_Weight < threshold) {
					tuple.changeWeight(0.0);
				}
			}
			return data;
		}
	}

	/**
	 * Re-weighs examples covered by this rule.
	 */
	public RowData reweighCovered(RowData data) throws ClusException {
		int cov_method = getSettings().getCoveringMethod();
		double cov_w_par = getSettings().getCoveringWeight();
		int nb_rows = data.getNbRows();
		RowData result = new RowData(data.getSchema(), nb_rows);
		double old_weight, new_weight;
		if ((cov_w_par >= 1) || (cov_w_par < 0)) {
			throw new ClusException("Weighted covering: covering weight should be between 0 and 1!");
		}
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			old_weight = tuple.getWeight();
			if (cov_method == Settings.COVERING_METHOD_WEIGHTED_ADDITIVE) {
				new_weight = cov_w_par * old_weight / (old_weight + 1);
			} else if (cov_method == Settings.COVERING_METHOD_WEIGHTED_MULTIPLICATIVE) {
				if (cov_w_par == 1) {
					throw new ClusException("Multiplicative weighted covering: covering weight should not be 1!");
				}
				new_weight = old_weight * cov_w_par;
			} else { // COVERING_METHOD_WEIGHTED_ERROR
				// DONE: weighted by a proportion of incorrectly classified target attributes.
				// TODO: weighted by a distance to a prototype of examples covered by this rule.
				//if (m_StatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
				if (m_TargetStat instanceof ClassificationStat) {
					int[] predictions = predictWeighted(tuple).getNominalPred();
					NominalAttrType[] targetAttrs = data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
					if (predictions.length > 1) { // Multiple target
						double prop_true = 0;
						for (int j = 0; j < predictions.length; j++) {
							int true_value = targetAttrs[j].getNominal(tuple);
							if (predictions[j] == true_value) {
								prop_true++;
							}
						}
						prop_true = prop_true != 0.0 ? prop_true / predictions.length : 0.0;
						new_weight = old_weight * (1 + prop_true * (cov_w_par - 1));
					} else { // Single target
						int prediction = predictions[0];
						int true_value = targetAttrs[0].getNominal(tuple);
						if (prediction == true_value) {
							new_weight = old_weight * cov_w_par;
						} else {
							new_weight = old_weight;
						}
					}
				} else if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
					ClusStatistic prediction = predictWeighted(tuple);
					ClusAttributeWeights weights = m_StatManager.getClusteringWeights();
					double coef = cov_w_par * prediction.getAbsoluteDistance(tuple, weights);
					if (coef > 1) { // Limit max weight to 1
						coef = 1;
					}
					new_weight = old_weight * coef;
				// } else if (m_StatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
				} else if (m_TargetStat instanceof RegressionStat) {
					double[] predictions = predictWeighted(tuple).getNumericPred();
					NumericAttrType[] targetAttrs = data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
					if (predictions.length > 1) { // Multiple target
						double[] true_values = new double[predictions.length];
						ClusStatistic stat = m_StatManager.getTrainSetStat();
						double[] variance = new double[predictions.length];
						double[] coef = new double[predictions.length];
						for (int j = 0; j < true_values.length; j++) {
							true_values[j] = targetAttrs[j].getNumeric(tuple);
							variance[j] = ((CombStat)stat).getRegressionStat().getVariance(j);
							coef[j] = cov_w_par * Math.abs(predictions[j] - true_values[j]) / Math.sqrt(variance[j]); // Add /2 or /4 here?
						}
						double mean_coef = 0;
						for (int j = 0; j < true_values.length; j++) {
							mean_coef += coef[j];
						}
						mean_coef /= coef.length;
						if (mean_coef > 1) { // Limit max weight to 1
							mean_coef = 1;
						}
						new_weight = old_weight * mean_coef;
					} else { // Single target
						double prediction = predictions[0];
						double true_value = targetAttrs[0].getNumeric(tuple);
						ClusStatistic stat = m_StatManager.getTrainSetStat();
						double variance = ((CombStat)stat).getRegressionStat().getVariance(0);
						double coef = cov_w_par * Math.abs(prediction - true_value) / Math.sqrt(variance); // Add /2 or /4 here?
						if (coef > 1) { // Limit max weight to 1
							coef = 1;
						}
						new_weight = old_weight * coef;
					}
				} else if (ClusStatManager.getMode() == ClusStatManager.MODE_TIME_SERIES) {
					ClusStatistic prediction = predictWeighted(tuple);
					ClusAttributeWeights weights = m_StatManager.getClusteringWeights();
					double coef = cov_w_par * prediction.getAbsoluteDistance(tuple, weights);
					if (coef > 1) { // Limit max weight to 1
						coef = 1;
					}
					new_weight = old_weight * coef;
				} else {
					throw new ClusException("reweighCovered(): Unsupported mode!");
				}
			}
			if (covers(tuple)) {
				result.setTuple(tuple.changeWeight(new_weight), i);
			} else {
				result.setTuple(tuple, i);
			}
		}
		return removeCoveredEnough(result);
	}

	public void setVisitor(Object visitor) {
		m_Visitor = visitor;
	}

	public Object getVisitor() {
		return m_Visitor;
	}
	
	public void setConstraints(ArrayList<ILevelConstraint> constraints) {
		m_Constraint = constraints;
	}
	
	public ArrayList<ILevelConstraint> getConstraints() {
		return m_Constraint;
	}
	
	public int getNumberOfViolatedConstraints(){
		int count = 0;
		Iterator<ILevelConstraint> i = m_Constraint.iterator();
		while(i.hasNext()){
			ILevelConstraint ilc = i.next();
			DataTuple t1 = ilc.getT1();
			DataTuple t2 = ilc.getT2();
			if(ilc.getType()==0){
				//ML
				if(!(m_Data.contains(t1) && m_Data.contains(t2)))
					count++;
			} else {
				if(m_Data.contains(t1) && m_Data.contains(t2))
					count++;
			}
		}
		return count;
	}
	
	public int getNumberOfViolatedConstraintsRCCC(){
		int count = 0;
		Iterator<ILevelConstraint> i = m_Constraint.iterator();
		ArrayList<DataTuple> data = ((RowData) getVisitor()).toArrayList();
		while(i.hasNext()){
			ILevelConstraint ilc = i.next();
			DataTuple t1 = ilc.getT1();
			DataTuple t2 = ilc.getT2();
			if(ilc.getType()==0){
				//ML
				if(!(data.contains(t1) && data.contains(t2)))
					count++;
			} else {
				//CL
				if(data.contains(t1) && data.contains(t2))
					count++;
			}
		}
		return count;
	}
	

	public ArrayList<ILevelConstraint> getViolatedConstraintsRCCC() {
		Iterator<ILevelConstraint> i = m_Constraint.iterator();
		ArrayList<DataTuple> data = ((RowData) getVisitor()).toArrayList();
		ArrayList<ILevelConstraint> c = new ArrayList<ILevelConstraint>();
		while(i.hasNext()){
			ILevelConstraint ilc = i.next();
			DataTuple t1 = ilc.getT1();
			DataTuple t2 = ilc.getT2();
			if(ilc.getType()==0){
				//ML
				if(!(data.contains(t1) && data.contains(t2)))
					c.add(ilc);
			} else {
				//CL
				if(data.contains(t1) && data.contains(t2))
					c.add(ilc);
			}
		}
		return c;
	}

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
	}

	public void attachModel(HashMap table) throws ClusException {
		for (int i = 0; i < m_Tests.size(); i++) {
			NodeTest test = (NodeTest)m_Tests.get(i);
			test.attachModel(table);
		}
	}

	public void printModel() {
		PrintWriter wrt = new PrintWriter(System.out);
		printModel(wrt);
		wrt.flush();
	}

	public void printModel(PrintWriter wrt) {
		printModel(wrt, StatisticPrintInfo.getInstance());
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		wrt.print("IF ");
		if (m_Tests.size() == 0) {
			wrt.print("true");
		} else {
			for (int i = 0; i < m_Tests.size(); i++) {
				NodeTest test = (NodeTest)m_Tests.get(i);
				if (i != 0) {
					wrt.println(" AND");
					wrt.print("   ");
				}
				wrt.print(test.getString());
			}
		}
		wrt.println();

		wrt.print("THEN "+m_TargetStat.getString(info));

		if (getID() != 0 && info.SHOW_INDEX) wrt.println(" ("+getID()+")");
		else wrt.println();
		String extra = m_TargetStat.getExtraInfo();
		if (extra != null) {
			// Used, e.g., in hierarchical multi-classification
			wrt.println();
			wrt.print(extra);
		}

		commonPrintForRuleTypes(wrt, info);
	}
	
	/** Print for also nonregular rules */
	protected void commonPrintForRuleTypes(PrintWriter wrt, StatisticPrintInfo info) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		if (getSettings().isRulePredictionOptimized()) {
			wrt.println("\n   Rule weight        : " + fr.format(getOptWeight()));
		}
		if (getSettings().computeDispersion() && (m_CombStat[ClusModel.TRAIN] != null)) {
			//if (getSettings().getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_OPTIMIZED) {
			wrt.println("   Dispersion (train): " + m_CombStat[ClusModel.TRAIN].getDispersionString());
			wrt.println("   Coverage   (train): " + fr.format(m_Coverage[ClusModel.TRAIN]));
			wrt.println("   Cover*Disp (train): " + fr.format((m_CombStat[ClusModel.TRAIN].dispersionCalc()*m_Coverage[ClusModel.TRAIN])));
			if (m_CombStat[ClusModel.TEST] != null) {
				wrt.println("   Dispersion (test):  " + m_CombStat[ClusModel.TEST].getDispersionString());
				wrt.println("   Coverage   (test):  " + fr.format(m_Coverage[ClusModel.TEST]));
				wrt.println("   Cover*Disp (test):  " + fr.format((m_CombStat[ClusModel.TEST].dispersionCalc()*m_Coverage[ClusModel.TEST])));
			}
		}
		if (hasErrors()) {
			// Enable with setting PrintRuleWiseErrors = Yes
			ClusErrorList train_err = getError(ClusModel.TRAIN);
			if (train_err != null) {
				wrt.println();
				wrt.println("Training error");
				train_err.showError(wrt);
			}
			ClusErrorList test_err = getError(ClusModel.TEST);
			if (test_err != null) {
				wrt.println();
				wrt.println("Testing error");
				test_err.showError(wrt);
			}
		}
	}

	public void printModelToPythonScript(PrintWriter wrt) {
	}

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean ex) {
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
	}

	public Settings getSettings() {
		return m_StatManager.getSettings();
	}

	public boolean isEmpty() {
		return getModelSize() == 0;
	}

	public int getModelSize() {
		return m_Tests.size();
	}

	public NodeTest getTest(int i) {
		return (NodeTest)m_Tests.get(i);
	}

	public void setTest(int i, NodeTest test) {
		m_Tests.set(i, test);
	}

	public void addTest(NodeTest test) {
		m_Tests.add(test);
	}

	public void removeTest(int i) {
		m_Tests.remove(i);
	}

	public double[] getCoverage() {
		return m_Coverage;
	}

	public void setCoverage(double[] coverage) {
		m_Coverage = coverage;
	}

	public ClusStatistic getTargetStat() {
		return m_TargetStat;
	}

	public ClusStatistic getClusteringStat() {
		return m_ClusteringStat;
	}

	public void setTargetStat(ClusStatistic stat) {
		m_TargetStat = stat;
	}

	/** Given double predictions, sets the target stat. A simpler way than setTargetStat */
	public void setNumericPrediction(double[] newPred) {
		RegressionStat stat = (RegressionStat)getTargetStat();
		
		for (int iTarget = 0; iTarget < stat.m_NbAttrs; iTarget++) {
			stat.m_Means[iTarget] = newPred[iTarget];
			stat.m_SumValues[iTarget] = stat.m_Means[iTarget];
			stat.m_SumWeights[iTarget] = 1;
		}
		setTargetStat(stat);
	}

	
	public void setClusteringStat(ClusStatistic stat) {
		m_ClusteringStat = stat;
	}
	/**
	 * Post process the rule.
	 * Post processing is only calculating the means for each of the target statistics.
	 * This mean is the target prediction.
	 * FIXME Could this be merged with computePrediction?
	 */
	public void postProc() {
		m_TargetStat.calcMean();
	}

	public String getModelInfo() {
		return "Tests = "+getModelSize();
	}

	public double getTrainErrorScore() {
		if (m_TrainErrorScore != -1) {
			return m_TrainErrorScore;
		} else {
			System.err.println("getTrainErrorScore(): Error score not initialized!");
			return Double.POSITIVE_INFINITY;
		}
	}

	/** Calculates TrainErrorScore - average of error rates across all target
	 *  attributes on the all training data covered by this rule (kind of ...).
	 *  To be used in some schemes (AccuracyWeighted) for combining predictions
	 *  of multiple (unordered) rules.
	 */
	public void setTrainErrorScore() {
		int nb_rows = m_Data.size();
		int nb_tar = m_TargetStat.getNbAttributes();
		if (m_TargetStat instanceof ClassificationStat) {
			int[] true_counts = new int[nb_tar];
			NominalAttrType[] targetAttrs = m_StatManager.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = (DataTuple)m_Data.get(i);
				int[] prediction = predictWeighted(tuple).getNominalPred();
				for (int j = 0; j < nb_tar; j++) {
					if (prediction[j] == targetAttrs[j].getNominal(tuple)) { // predicted == true
						true_counts[j]++;
					}
				}
			}
			double sum_err = 0;
			for (int j = 0; j < nb_tar; j++) {
				sum_err += (double)(nb_rows - true_counts[j]) / nb_rows;
			}
			m_TrainErrorScore = sum_err / nb_tar;
		} else if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
			// This is the same as for time-series. Not sure if it correct.
			double sum_diff = 0.0;
			ClusAttributeWeights weight = m_StatManager.getClusteringWeights();
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = (DataTuple)m_Data.get(i);
				ClusStatistic prediction = predictWeighted(tuple);
				sum_diff = prediction.getAbsoluteDistance(tuple, weight);
			}
			m_TrainErrorScore = sum_diff / nb_tar;
			if (m_TrainErrorScore > 1) { // Limit the error score to 1
				m_TrainErrorScore = 1;
			}
		} else if (m_TargetStat instanceof RegressionStat) {
			double norm = getSettings().getVarBasedDispNormWeight(); // For RRMSE this * std dev is the normalization
			ClusStatistic stat = m_StatManager.getTrainSetStat();
			NumericAttrType[] targetAttrs = m_StatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
			int[] target_idx = new int[nb_tar];
			double[] variance = new double[nb_tar];
			double[] diff = new double[nb_tar];
			for (int j = 0; j < nb_tar; j++) {
				target_idx[j] = targetAttrs[j].getArrayIndex();
				variance[j] = ((CombStat)stat).getRegressionStat().getVariance(target_idx[j]);
			}
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = (DataTuple)m_Data.get(i);
				double[] prediction = predictWeighted(tuple).getNumericPred();
				for (int j = 0; j < nb_tar; j++) {
					diff[j] += Math.abs(prediction[j] - targetAttrs[j].getNumeric(tuple)); // |prediction-true|
				}
			}
			double sum_diff = 0.0;
			for (int j = 0; j < nb_tar; j++) {
				sum_diff += diff[j] / nb_rows / Math.sqrt(variance[j]) / (norm*norm);
			}
			m_TrainErrorScore = sum_diff / nb_tar;
			if (m_TrainErrorScore > 1) { // Limit the error score to 1
				m_TrainErrorScore = 1;
			}
		} else if (ClusStatManager.getMode() == ClusStatManager.MODE_TIME_SERIES) {
			double sum_diff = 0.0;
			ClusAttributeWeights weight = m_StatManager.getClusteringWeights();
			// TODO: Figure out how should this weight be set ... any normalization etc ...
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = (DataTuple)m_Data.get(i);
				ClusStatistic prediction = predictWeighted(tuple);
				sum_diff = prediction.getAbsoluteDistance(tuple, weight);
			}
			m_TrainErrorScore = sum_diff / nb_tar;
			if (m_TrainErrorScore > 1) { // Limit the error score to 1
				m_TrainErrorScore = 1;
			}
		}
	}

	public double getOptWeight() {
		if (m_OptWeight != -1) { // TODO this is not ALWAYS the sign for uninitialized weight
			return m_OptWeight;
		} else {
			System.err.println("Warning: Optimal rule weight not initialized!");
			return -1.0;
		}
	}

	public void setOptWeight(double weight) {
		m_OptWeight = weight;
	}

	/**
	 * Computes the dispersion of data tuples covered by this rule.
	 * @param mode 0 for train set, 1 for test set
	 */
	public void computeDispersion(int mode) {
		CombStat combStat = (CombStat)m_StatManager.createStatistic(ClusAttrType.ATTR_USE_ALL);
		for (int i = 0; i < m_Data.size(); i++) {
			combStat.updateWeighted((DataTuple)m_Data.get(i), 0); // second parameter does nothing!
		}
		combStat.calcMean();
		m_CombStat[mode] = combStat;
		// save the coverage
		m_Coverage[mode] = m_Data.size();
	}

	/**
	 * Adds the tuple to the m_Data array
	 * @param tuple
	 */
	public void addDataTuple(DataTuple tuple) {
		m_Data.add(tuple);
	}

	/**
	 * Removes the data tuples from the m_Data array
	 */
	public void removeDataTuples() {
		m_Data.clear();
	}

	public ArrayList getData() {
		return m_Data;
	}

	/**
	 * For computation of rule-wise error measures
	 */
	public void setError(ClusErrorList error, int subset) {
		if (m_Errors == null){
			m_Errors = new ClusErrorList[2];
		}
		m_Errors[subset] = error;
	}

	public void addError(ClusErrorList error, int subset) {
		if (m_Errors == null){
			setError(error, subset);
		} else {
			m_Errors[subset].addErrors(error);
		}
	}

	public ClusErrorList getError(int subset) {
		if (m_Errors == null) return null;
		return m_Errors[subset];
	}

	public boolean hasErrors() {
		return m_Errors != null;
	}

	public boolean hasPrediction() {
		// Sometimes no valid prediction can be derived from a prototype (e.g., in HMC)
		return m_TargetStat.isValidPrediction();
	}


	/** Is this a regular rule or some other type of learner (e.g. linear term) */
	public boolean isRegularRule() {
		//NOTE if there is some other nonregular rule than linear term, at least 
		//ClusRuleSet.invertNormalizationForWeights might have to be changed	
		//return !m_isLinearTerm;
		return true;
	}

	
	public void computeCoverStat(RowData data, ClusStatistic stat) {
		int nb = data.getNbRows();
		stat.setSDataSize(nb);
		for (int i = 0; i < nb; i++) {
			DataTuple tuple = data.getTuple(i);
			if (covers(tuple)) {
				stat.updateWeighted(tuple, i);
			}
		}
		stat.optimizePreCalc(data);
	}

	public ClusModel prune(int prunetype) {
		return this;
	}

	public void retrieveStatistics(ArrayList list) {
	}
}

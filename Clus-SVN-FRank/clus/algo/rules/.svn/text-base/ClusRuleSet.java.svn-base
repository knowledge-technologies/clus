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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import jeans.util.*;
//import clus.algo.kNN.KNNStatistics;
//import clus.algo.kNN.NumericStatistic;
import clus.data.rows.*;
import clus.data.type.*;
import clus.error.ClusErrorList;
import clus.ext.hierarchical.WHTDStatistic;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.processor.ClusModelProcessor;
import clus.statistic.*;
import clus.tools.optimization.OptProbl;
//import clus.tools.optimization.de.DeAlg;
//import clus.tools.optimization.de.DeProbl; // Optimization information (data, predictions)
import clus.util.*;

/**
 * Class representing a set of predictive clustering rules.
 *
 */
public class ClusRuleSet implements ClusModel, Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	/** Default prediction if no other rule covers the instance. */
	protected ClusStatistic m_TargetStat;

	/** If we have created an all examples covering rule that is in the first position of rules
	 * Used for weight optimization.*/
	protected boolean m_allCoveringRuleExists = false;

	protected ArrayList<ClusRule> m_Rules = new ArrayList<ClusRule>();
	/** Array of tuples covered by the default rule. */
	protected ArrayList m_DefaultData = new ArrayList();
	protected ClusStatManager m_StatManager;
	protected boolean m_HasRuleErrors;
	protected String m_Comment;
	
	/** If weights are optimized, what was the best T value that was used for this rule set */
	protected double m_optWeightBestTValue = 0;
	/** If weights are optimized, what was the fitness of best T value that was used for this rule set */
	protected double m_optWeightBestFitness = 0;
	
	public ClusRuleSet(ClusStatManager statmanager) {
		m_StatManager = statmanager;
	}

	/**
	 * Clones the rule set so that it points to the same rules.
	 * @return Clone of this rule set.
	 */
	public ClusRuleSet cloneRuleSet() {
		ClusRuleSet new_ruleset = new ClusRuleSet(m_StatManager);
		for (int i = 0; i < getModelSize(); i++) {
			new_ruleset.add(getRule(i));
		}
		return new_ruleset;
	}

	/**
	 * Deep clones this ruleset, so that it points to different rules that
	 * have target statistics with the given threshold.
	 * @return Clone of this ruleset with rules with the given treshold.
	 */
	public ClusRuleSet cloneRuleSetWithThreshold(double threshold) {
		ClusRuleSet new_ruleset = new ClusRuleSet(m_StatManager);
		for (int i = 0; i < getModelSize(); i++) {
			ClusRule newRule = getRule(i).cloneRule();
			WHTDStatistic stat = (WHTDStatistic)getRule(i).getTargetStat();
			WHTDStatistic new_stat = (WHTDStatistic)stat.cloneStat();
			new_stat.copyAll(stat);
			new_stat.setThreshold(threshold);
			new_stat.calcMean();
			newRule.setTargetStat(new_stat);
			new_ruleset.add(newRule);
		}
		return new_ruleset;
	}

	/** Add given rule to this rule set
	 * @param rule Added rule
	 */
	public void add(ClusRule rule) {
		if (getSettings().isWeightedCovering()) {
			// Only add unique rules for weighted covering
			if (unique(rule)) {
				m_Rules.add(rule);
			}
		} else {
			m_Rules.add(rule);
		}
	}

	public void removeLastRule() {
		m_Rules.remove(getModelSize()-1);
	}

	/** Add given rule if it is not already in the rule set. Only descriptions are checked.
	 * @param rule Added rule
	 * @return True if the rule was unique.
	 */
	public boolean addIfUnique(ClusRule rule) {
		if (unique(rule)) {
			m_Rules.add(rule);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tests if the rule is already in the rule set.
	 * Rules are equal if their tests are same!
	 */
	public boolean unique(ClusRule rule) {
		boolean res = true;
		for (int i = 0; i < m_Rules.size(); i++) {
			if (((ClusRule)m_Rules.get(i)).equals(rule)) {
				res = false;
			}
		}
		return res;
	}


	/** Add given rule if it is not already in the rule set. Also targets are checked
	 * Implemented only for regression!
	 * @param rule Added rule
	 * @return True if the rule was unique.
	 */
	public boolean addIfUniqueDeeply(ClusRule rule) {
		if (uniqueDeeply(rule)) {
			m_Rules.add(rule);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tests if the rule is already in the rule set.
	 * Rules are deeply same if both description and target part are the same
	 * Only implemented for regression!
	 */
	static final double EQUAL_MAX_DIFFER = 1E-6;
	public boolean uniqueDeeply(ClusRule rule) {
		boolean isUnique = true;
		for (int i = 0; i < m_Rules.size() && isUnique; i++) {
			if (((ClusRule)m_Rules.get(i)).equalsDeeply(rule)){
				isUnique = false;
			}
		}
		return isUnique;
	}

	/**
	 * Returns the statistic (prediction) for a given tuple.
	 */
	public ClusStatistic predictWeighted(DataTuple tuple) {
		boolean covered = false;
		int pred_method = getSettings().getRulePredictionMethod();
		if (pred_method == Settings.RULE_PREDICTION_METHOD_DECISION_LIST) {
			for (int i = 0; i < getModelSize(); i++) {
				ClusRule rule = getRule(i);
				if (rule.covers(tuple)) {
					return rule.getTargetStat();
				}
			}
			return m_TargetStat;
		} else if (pred_method == Settings.RULE_PREDICTION_METHOD_UNION) {
			// In multi-label classification: predicted set of classes is
			// union of predictions by individual rules
			// TODO: Check if this is obsolete/move/reuse for hierarchical MLC ...
			ClusStatistic stat = m_TargetStat.cloneSimple();
			stat.unionInit();
			for (int i = 0; i < getModelSize(); i++) {
				ClusRule rule = getRule(i);
				if (rule.covers(tuple)) {
					stat.union(rule.getTargetStat());
					covered = true;
				}
			}
			stat.unionDone();

			// If covered, return stat, else default prediction
			return covered ? stat : m_TargetStat;

		} else if (getSettings().isRulePredictionOptimized()) {
			// Optimized weights
			// The rules are considered as base predictors of ensemble

			// Get the weighted prediction of the rule that covers all the
			// instances (former default rule)
			ClusRule firstRule = getRule(0);
			ClusStatistic prediction = (firstRule.predictWeighted(tuple))
			                            .copyNormalizedWeighted(getAppropriateWeight(firstRule));

			for (int iBaseRule = 1; iBaseRule < getModelSize(); iBaseRule++) {
				ClusRule rule = getRule(iBaseRule);

//				if (iBaseRule == getModelSize()-1) {
//					boolean blah = false;
//				}
				// If the rule covers tuple (indicator function is nonzero)
				if (rule.covers(tuple)) {
					ClusStatistic rulestat = rule.predictWeighted(tuple);

					// For regression normalization does not do anything.
					// For classification the values are normalized over the the counts.
					ClusStatistic norm_rulestat = rulestat.normalizedCopy();
					prediction.addPrediction(norm_rulestat, getAppropriateWeight(rule));
				}
			}

			// Is always covered because default rule is used
			return prediction;

		} else {  // Unordered rules (with different weighting schemes)
			ClusStatistic stat = m_TargetStat.cloneSimple();
			double weight_sum = 0.0;

			// Get the overall sum of weights for all the rules that cover the example
			for (int i = 0; i < getModelSize(); i++) {
				ClusRule rule = getRule(i);
				if (rule.covers(tuple)) {
					weight_sum += getAppropriateWeight(rule);
				}
			}

			// Take the prediction. Normalise
			for (int i = 0; i < getModelSize(); i++) {
				ClusRule rule = getRule(i);
				if (rule.covers(tuple)) {
					ClusStatistic rulestat = rule.predictWeighted(tuple);
					double weight = getAppropriateWeight(rule)/weight_sum;
					ClusStatistic norm_rulestat = rulestat.normalizedCopy();
					stat.addPrediction(norm_rulestat, weight);
					covered = true;
				}
			}
			if (covered) {
				stat.computePrediction();
				return stat;
			} else {
				// If not covered, return default prediction
				return m_TargetStat;
			}
		}
	}

	public double getAppropriateWeight(ClusRule rule) {
		switch (getSettings().getRulePredictionMethod()) {
		case Settings.RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED:
			return rule.m_TargetStat.m_SumWeight;
		case Settings.RULE_PREDICTION_METHOD_TOT_COVERAGE_WEIGHTED:
			return rule.getCoverage()[ClusModel.TRAIN];
		case Settings.RULE_PREDICTION_METHOD_ACC_COV_WEIGHTED:
			return rule.m_TargetStat.m_SumWeight*(1-rule.getTrainErrorScore());
		case Settings.RULE_PREDICTION_METHOD_ACCURACY_WEIGHTED:
			return 1-rule.getTrainErrorScore();
		case Settings.RULE_PREDICTION_METHOD_OPTIMIZED:
		case Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED:
		case Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY:
			return rule.getOptWeight();
		default:
			System.err.println("getAppropriateWeight(): Unknown weighted prediction method!");
			return Double.NEGATIVE_INFINITY;
		}
	}

	public void removeEmptyRules() {
		for (int i = getModelSize()-1; i >= 0; i--) {
			if (getRule(i).isEmpty()) {
				m_Rules.remove(i);
			}
		}
	}

	/**
	 * Remove rules that have less weight than OptRuleWeightThreshold set in .s file.
	 * @return Original index of the last handled weight.
	 */
	public int removeLowWeightRules() {
		double threshold = getSettings().getOptRuleWeightThreshold();
		int nb_rules = getModelSize();
		for (int i = nb_rules-1; i >= 0; i--) {

			// If the first rule is all covering, it is not removed (can predict just 0 if that is the weight)
			// This is used instead of default rule if the weights are optimized.
			if (i == 0 && m_allCoveringRuleExists)
				continue;

			if (Math.abs(getRule(i).getOptWeight()) < threshold || getRule(i).getOptWeight() == 0.0) {
				// If optimization is used, the last first covers all the instances. This should not be removed
				m_Rules.remove(i);
			}
		}
		if (Settings.VERBOSE > 0) System.out.println("Rules left: " + getModelSize() + " out of " + nb_rules);
		return nb_rules-1;
	}
	
	/** 
	 * Update rule sets so, that the offset and scaling factors of linear terms are included
	 * In the weights. Thus linear terms stay untouched in the end.
	 * NOTE: Missing values cause this function to reduce accuracy! See settings file for more information.
	 */
	public void convertToPlainLinearTerms() {
		RegressionStat firstRuleStat = (RegressionStat) getRule(0).m_TargetStat;
		
		// If w0 = 0, we have to make some changes
		if (getRule(0).getOptWeight() == 0) {
			// Equals the previous prediction, but no dividing with zero now
			for (int iTarget = 0; iTarget < firstRuleStat.m_NbAttrs; iTarget++) {
				firstRuleStat.m_Means[iTarget] = 0;
			}
			getRule(0).setOptWeight(1.0); //
		}

		double defaultWeight = getRule(0).getOptWeight();
		ClusStatistic tar_stat = m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET);
		// What is to be added to default pred
		double[] addToDefaultPred = new double[tar_stat.getNbAttributes()]; 
		
		for (int iRule = 0; iRule < getModelSize(); iRule++) {

			ClusRule cursor = getRule(iRule);
			if (!cursor.isRegularRule()) {
				addToDefaultPred = ((ClusRuleLinearTerm)cursor).convertToPlainTerm(addToDefaultPred, defaultWeight);
			}
		}
			
		
		for (int iTarget = 0; iTarget < firstRuleStat.m_NbAttrs; iTarget++) {
			firstRuleStat.m_Means[iTarget] += addToDefaultPred[iTarget];
			firstRuleStat.m_SumValues[iTarget] = firstRuleStat.m_Means[iTarget];
			firstRuleStat.m_SumWeights[iTarget] = 1;
		}
	}
	

	public void simplifyRules() {
		for (int i = getModelSize()-1; i >= 0; i--) {
			getRule(i).simplify();
		}
	}

	public void attachModel(HashMap table) throws ClusException {
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			rule.attachModel(table);
		}
	}

	public void printModel(PrintWriter wrt) {
		printModel(wrt, StatisticPrintInfo.getInstance());
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		// boolean headers = getSettings().computeDispersion() || hasRuleErrors();
		boolean headers = true;
		// [train/test][comb/num/nom]
		double[][] avg_dispersion = new double[2][3];
		double[] avg_coverage = new double[2];
		double[][] avg_prod = new double[2][3];
		
		if (!Settings.isPrintAllRules())
			wrt.println("Set of "+ m_Rules.size() +" rules.\n");
		
		if (getSettings().getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED
				&& getSettings().getOptGDNbOfTParameterTry() > 1) {
			// Print the T value that won the game.
			wrt.println("Gradient descent optimization: Smallest test fitness of "
					+ fr.format(m_optWeightBestFitness) + " with T value: " + fr.format(m_optWeightBestTValue) + "\n");
			
		}
				
		
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			if (headers) {

				if (Settings.isPrintAllRules()){
					String head = new String("Rule " + (i + 1) + ":");
					char[] underline = new char[head.length()];
					for (int j = 0; j < head.length(); j++) {
						underline[j] = '=';
					}

					wrt.println(head);
					wrt.println(new String(underline));
				}
				// Added this test so that PrintRuleWiseErrors also works in HMC setting (02/01/06)
				if (getSettings().computeDispersion()) {
					avg_dispersion[0][0] += rule.m_CombStat[0].dispersionCalc();
					avg_dispersion[0][1] += rule.m_CombStat[0].dispersionNum(1);
					avg_dispersion[0][2] += rule.m_CombStat[0].dispersionNom(1);
					avg_coverage[0] += rule.m_Coverage[0];
					avg_prod[0][0] += rule.m_CombStat[0].dispersionCalc()*rule.m_Coverage[0];
					avg_prod[0][1] += rule.m_CombStat[0].dispersionNum(1)*rule.m_Coverage[0];
					avg_prod[0][2] += rule.m_CombStat[0].dispersionNom(1)*rule.m_Coverage[0];
					if (rule.m_CombStat[1] != null) {
						avg_dispersion[1][0] += rule.m_CombStat[1].dispersionCalc();
						avg_dispersion[1][1] += rule.m_CombStat[1].dispersionNum(1);
						avg_dispersion[1][2] += rule.m_CombStat[1].dispersionNom(1);
						avg_coverage[1] += rule.m_Coverage[1];
						avg_prod[1][0] += rule.m_CombStat[1].dispersionCalc()*rule.m_Coverage[1];
						avg_prod[1][1] += rule.m_CombStat[1].dispersionNum(1)*rule.m_Coverage[1];
						avg_prod[1][2] += rule.m_CombStat[1].dispersionNom(1)*rule.m_Coverage[1];
					}
				}
			}
			if (Settings.isPrintAllRules())
			{
				rule.printModel(wrt, info);
				wrt.println();
			}
		}

		if (m_TargetStat != null && m_TargetStat.isValidPrediction()) {
			if (headers) {
				if (m_Comment == null) {
					wrt.println("Default rule:");
					wrt.println("=============");
				} else {
					wrt.println("Default rule"+m_Comment+":");
					wrt.println("=============");
				}
			}
			wrt.println("Default = "+(m_TargetStat == null ? "N/A" : m_TargetStat.getString()));
		}
		if (headers && getSettings().computeDispersion()) {
			wrt.println("\n\nRule set dispersion:");
			wrt.println("=====================");
			avg_dispersion[0][0] = avg_dispersion[0][0] == 0 ? 0 : avg_dispersion[0][0]/m_Rules.size();
			avg_dispersion[0][1] = avg_dispersion[0][1] == 0 ? 0 : avg_dispersion[0][1]/m_Rules.size();
			avg_dispersion[0][2] = avg_dispersion[0][2] == 0 ? 0 : avg_dispersion[0][2]/m_Rules.size();
			avg_coverage[0] = avg_coverage[0] == 0 ? 0 : avg_coverage[0]/m_Rules.size();
			avg_prod[0][0] = avg_prod[0][0] == 0 ? 0 : avg_prod[0][0]/m_Rules.size();
			avg_prod[0][1] = avg_prod[0][1] == 0 ? 0 : avg_prod[0][1]/m_Rules.size();
			avg_prod[0][2] = avg_prod[0][2] == 0 ? 0 : avg_prod[0][2]/m_Rules.size();
			avg_dispersion[1][0] = avg_dispersion[1][0] == 0 ? 0 : avg_dispersion[1][0]/m_Rules.size();
			avg_dispersion[1][1] = avg_dispersion[1][1] == 0 ? 0 : avg_dispersion[1][1]/m_Rules.size();
			avg_dispersion[1][2] = avg_dispersion[1][2] == 0 ? 0 : avg_dispersion[1][2]/m_Rules.size();
			avg_coverage[1] = avg_coverage[1] == 0 ? 0 : avg_coverage[1]/m_Rules.size();
			avg_prod[1][0] = avg_prod[1][0] == 0 ? 0 : avg_prod[1][0]/m_Rules.size();
			avg_prod[1][1] = avg_prod[1][1] == 0 ? 0 : avg_prod[1][1]/m_Rules.size();
			avg_prod[1][2] = avg_prod[1][2] == 0 ? 0 : avg_prod[1][2]/m_Rules.size();
			wrt.println("   Avg_Dispersion  (train): " + fr.format(avg_dispersion[0][0]) + " = " + fr.format(avg_dispersion[0][1]) + " + " + fr.format(avg_dispersion[0][2]));
			wrt.println("   Avg_Coverage    (train): " + fr.format(avg_coverage[0]));
			wrt.println("   Avg_Cover*Disp  (train): " + fr.format(avg_prod[0][0]) + " = " + fr.format(avg_prod[0][1]) + " + " + fr.format(avg_prod[0][2]));
			wrt.println("   Avg_Dispersion  (test):  " + fr.format(avg_dispersion[1][0]) + " = " + fr.format(avg_dispersion[1][1]) + " + " + fr.format(avg_dispersion[1][2]));
			wrt.println("   Avg_Coverage    (test):  " + fr.format(avg_coverage[1]));
			wrt.println("   Avg_Cover*Disp  (test):  " + fr.format(avg_prod[1][0]) + " = " + fr.format(avg_prod[1][1]) + " + " + fr.format(avg_prod[1][2]));
		}
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, ClusSchema schema) {
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			rule.printModel(wrt, info);
			wrt.println();
			wrt.println("Covered examples:");
			ArrayList data = rule.getData();
			ClusAttrType[] attrs = schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
			ClusAttrType[] key = schema.getAllAttrUse(ClusAttrType.ATTR_USE_KEY);
			for (int k = 0; k < data.size(); k++) {
				DataTuple tuple = (DataTuple)data.get(k);
				wrt.print(String.valueOf(k+1)+": ");
				boolean hasval = false;
				for (int j = 0; j < key.length; j++) {
					if (hasval) wrt.print(",");
					wrt.print(key[j].getString(tuple));
					hasval = true;
				}
				for (int j = 0; j < attrs.length; j++) {
					if (hasval) wrt.print(",");
					wrt.print(attrs[j].getString(tuple));
					hasval = true;
				}
				wrt.println();
			}
			wrt.println();
		}
		wrt.println("Default = "+(m_TargetStat == null ? "None" : m_TargetStat.getString()));
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
		addDataToRules(examples);
		// setTrainErrorScore(); // Is this needed?
		printModelAndExamples(wrt, info, examples.getSchema());
		removeDataFromRules();
	}

	public void printModelToPythonScript(PrintWriter wrt) {
	}

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean ex) {
	}

	/**
	 * How many rules in the set?
	 */
	public int getModelSize() {
		return m_Rules.size();
	}

	public Settings getSettings() {
		return m_StatManager.getSettings();
	}

	public ClusRule getRule(int i) {
		return (ClusRule)m_Rules.get(i);
	}

	/**
	 * How many literals in all of the rules.
	 * Counting linear terms as 0 literals.
	 */
	public int getNbLiterals() {
		int count = 0;
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			if (rule.isRegularRule())
				count += rule.getModelSize();
		}
		return count;
	}

	/**
	 * How many linear terms are in the rule set.
	 */
	private int getNbLinearTerms() {
		int count = 0;
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			if (!rule.isRegularRule())
				count++;
		}

		return count;
	}

	public void setTargetStat(ClusStatistic def) {
		m_TargetStat = def;
	}

	/**
	 * Post process the rule set rule by rule.
	 * Post processing is only calculating the means for each of the target statistics.
	 */
	public void postProc() {
		m_TargetStat.calcMean();
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			rule.postProc();
		}
	}

	public String getModelInfo() {
		return "Rules = "+getModelSize()+" (Tests: "+getNbLiterals()+" and linear terms: " +
		        getNbLinearTerms() + ")";
	}

	/**
	 * Computes the dispersion of data tuples covered by each rule.
	 * @param mode 0 for train set, 1 for test set
	 */
	public void computeDispersion(int mode) {
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			rule.computeDispersion(mode);
		}
	}

	/**
	 * Computes the error score of the rule set. To be used for
	 * deciding whether to add more rules to the rule set or not.
	 */
	// TODO: finish
	public double computeErrorScore(RowData data) {
		ClusStatistic tar_stat = m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET);
		// Average error rate over all target attributes
		if (tar_stat instanceof ClassificationStat) {
			double result = 0;
			int nb_rows = data.getNbRows();
			int nb_tar = tar_stat.getNbNominalAttributes();
			int[] nb_right = new int[nb_tar];
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = data.getTuple(i);
				int[] predictions = predictWeighted(tuple).getNominalPred();
				int true_value;
				NominalAttrType[] targetAttrs = data.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
				for (int j = 0; j < nb_tar; j++) {
					true_value = targetAttrs[j].getNominal(tuple);
					if (predictions[j] == true_value) {
						nb_right[j]++;
					}
				}
			}
			for (int j = 0; j < nb_tar; j++) {
				result += (1.0 * nb_rows - nb_right[j]) / nb_rows;
			}
			result /= nb_tar;
			return result;
		// Average RMSE over all target attributes
		} else if (tar_stat instanceof RegressionStat) {
			double result = 0;
			int nb_rows = data.getNbRows();
			int nb_tar = tar_stat.getNbNumericAttributes();
			double[] sum_sqr_err = new double[nb_tar];
			NumericAttrType[] targetAttrs = data.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
			for (int i = 0; i < nb_rows; i++) {
				DataTuple tuple = data.getTuple(i);
				double[] predictions = ((RegressionStat)predictWeighted(tuple)).getNumericPred();
				for (int j = 0; j < nb_tar; j++) {
					double diff = predictions[j] - targetAttrs[j].getNumeric(tuple);
					sum_sqr_err[j] += diff * diff;
				}
			}
			for (int j = 0; j < nb_tar; j++) {
				result += Math.sqrt(sum_sqr_err[j]/nb_rows);
			}
			result /= nb_tar;
			return result;
		} else { // Mixed classification and regression not yet implemented
			return -1;
		}
	}

	/**
	 * Sets TrainErrorScore in all rules which is to be used in some schemes
	 * (AccuracyWeighted) for combining predictions of multiple  (unordered) rules.
	 * COMPATIBILITY NOTE: This used to be in addDataToRules(DataTuple) ...
	 */
	public void setTrainErrorScore() {
		for (int i = 0; i < m_Rules.size(); i++) {
			((ClusRule)m_Rules.get(i)).setTrainErrorScore();
		}
	}

	/**
	 * Adds tuple to the rule which covers it. Returns true if the
	 * tuple is covered by any rule in this RuleSet and false otherwise.
	 */
	public boolean addDataToRules(DataTuple tuple) {
		boolean covered = false;
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = (ClusRule)m_Rules.get(i);
			if (rule.covers(tuple)) {
				rule.addDataTuple(tuple);
				covered = true;
			}
		}
		// COMPATIBILITY NOTE: Here used to be a call to set(Train)ErrorScore()
		// for each rule - no idea why here ... moved outside.
		return covered;
	}

	/**
	 * Adds the data tuples to rules which cover them. Non-covered
	 * tuples are added to the m_DefaultData array.
	 */
	public void addDataToRules(RowData data) {
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			if (!addDataToRules(tuple)) {
				m_DefaultData.add(tuple);
			}
		}
	}

	/**
	 * Removes the data tuples from rules and from m_DefaultData array.
	 */
	public void removeDataFromRules() {
		for (int i = 0; i < m_Rules.size(); i++) {
			((ClusRule)m_Rules.get(i)).removeDataTuples();
		}
		m_DefaultData.clear();
	}

	public ArrayList getDefaultData() {
		return m_DefaultData;
	}

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
		for (int i = 0; i < getModelSize(); i++) {
			ClusRule rule = getRule(i);
			if (rule.covers(tuple)) {
				for (int j = 0; j < mproc.size(); j++) {
					ClusModelProcessor proc = (ClusModelProcessor)mproc.elementAt(j);
					proc.modelUpdate(tuple, rule);
				}
			}
		}
	}

	public final void applyModelProcessor(DataTuple tuple, ClusModelProcessor proc) throws IOException {
		for (int i = 0; i < getModelSize(); i++) {
			ClusRule rule = getRule(i);
			if (rule.covers(tuple)) proc.modelUpdate(tuple, rule);
		}
	}

	public void setError(ClusErrorList error, int subset) {
		m_HasRuleErrors = true;
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = getRule(i);
			if (error != null) rule.setError(error.getErrorClone(), subset);
			else rule.setError(null, subset);
		}
	}

	public boolean hasRuleErrors() {
		return m_HasRuleErrors;
	}

	public int getID() {
		return 0;
	}

	public void numberRules() {
		for (int i = 0; i < m_Rules.size(); i++) {
			ClusRule rule = getRule(i);
			rule.setID(i+1);
		}
	}

	public ClusModel prune(int prunetype) {
		return this;
	}

	public void retrieveStatistics(ArrayList list) {
	}
	
	/**
	 * Returns the form of this rule set that can be used
	 * for weight optimization. The predictions should be always a value or NaN. NaN occurs if the rule does not cover
	 * the example OR the linear term predicts undefined attribute.
	 * @param outLogFile File stream for outputs.
	 * @param data Data the optimization is based on.
	 * @return Parameters for optimization. Include true values and predictions for each of the data instances.
	 */
	public OptProbl.OptParam giveFormForWeightOptimization(PrintWriter outLogFile, RowData data){
		//data = Clus.returnNormalizedData(data);

		ClusSchema schema = data.getSchema();

		/** Default prediction. This may be used to alter other predictions. */
		double[] defaultPred = null;

		// We need to transform the default rule to ordinary rule. We want to optimize it also.
		// Also if set, modify the other rules according to this
		defaultPred = addDefaultRuleToRuleSet();

		if (getSettings().isOptDefaultShiftPred()) {
			// Shift predictions according to the default rule
			shiftRulePredictions(defaultPred);
		}
		
		if (getSettings().isOptOmitRulePredictions()){
			// after creating default rule, before adding linear terms or weighting generality
			omitRulePredictions();
//					(ClusRuleLinearTerm.calcStdDevsForTheSet(data, m_StatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)))[1]);
		}

		if (getSettings().isOptWeightGenerality()) {
			// Weight the rules on generality. Do this after adding the default rule!
			// Also do this after omitting rule predictions!
			weightGeneralityForPredictions(data.getNbRows());
		}

		// Add linear terms to rule set
		if (getSettings().isOptAddLinearTerms()){
			// This should be done after default rule adding.
			
			ClusRuleLinearTerm.initializeClass(data, m_StatManager);
			
			if (getSettings().getOptAddLinearTerms() == Settings.OPT_GD_ADD_LIN_YES) {
				// Add linear terms only separately
//				addLinearTermsToRuleSet(minAndMaxes[0], minAndMaxes[1], m_linearTermsOffsets, m_linearTermsAttributeStdDevs);
				addLinearTermsToRuleSet();
			} // if memory saving is used, adding is done in the optimization phase
		}

		// Generate optimization input
		ClusStatistic tar_stat = m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET);
		int nb_target = tar_stat.getNbAttributes();
		int nb_baseFunctions = getModelSize();
		int nb_rows = data.getNbRows();
		boolean isClassification = false;
		if (m_TargetStat instanceof ClassificationStat) {
			isClassification = true;
		}

		// ***************** TRUE VALUES

		ClusAttrType[] trueValuesTemp = new ClusAttrType[nb_target];
		if (isClassification) {
			//NominalAttrType[]
			trueValuesTemp = (ClusAttrType[])schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		} else { // regression
			//NumericAttrType[]
			trueValuesTemp = (ClusAttrType[])schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		}

		/**
		 * True values for each target and instance
		 */
		//double[][] trueValues = new double[nb_rows][nb_target];
		OptProbl.TrueValues[] trueValues = new OptProbl.TrueValues[nb_rows];
		// Index over the instances of data
		for (int iRows = 0; iRows < nb_rows; iRows++) {
			DataTuple tuple = data.getTuple(iRows);

			OptProbl.TrueValues newTrueTargets = null;
			// Give the tuple to attribute line only if it is needed for implicit linear prediction. 
			if (getSettings().getOptAddLinearTerms() == Settings.OPT_GD_ADD_LIN_YES_SAVE_MEMORY) {
				newTrueTargets = new OptProbl.TrueValues(nb_target, tuple);
			} else {
				newTrueTargets = new OptProbl.TrueValues(nb_target, null);
			}
			
			trueValues[iRows] = newTrueTargets;
			
			// Take the true values from the data target by target
			for (int kTargets = 0; kTargets < nb_target; kTargets++)
			{
				if (isClassification){
					trueValues[iRows].m_targets[kTargets] = ((NominalAttrType)trueValuesTemp[kTargets]).getNominal(tuple);
				} else {
					trueValues[iRows].m_targets[kTargets] = ((NumericAttrType)trueValuesTemp[kTargets]).getNumeric(tuple);
				}
			}
		}


		// ************ PREDICTIONS
		// We have two different prediction arrays. First the one for rules, second the one for all other
		// types of predictors (e.g. linear terms)
		// To get the order right, the rules have to be the first ones. Let's check this.
		
		boolean allRulesScrolled = false;
		int nbOfRegularRules = 0;
		for (int jRules = 0; jRules < nb_baseFunctions; jRules++) {
			ClusRule rule = getRule(jRules);
			if (rule.isRegularRule()) {
				nbOfRegularRules++;
				if (allRulesScrolled) {
					// Error! There is a regular rule after something else
					System.err.println("Error: Order of rule set is wrong. Rules have to be before linear terms etc.");
					System.exit(1);
				}
			} else {
				allRulesScrolled = true; // Last rule has gone already
			}
				
		}
		

		// Number of nominal values for each target. For regression, no number of nominal values needed, i.e. 1
		int nb_values[] = new int[nb_target];

		for (int iTarget=0; iTarget < nb_target;iTarget++) {
			if (isClassification) {
				nb_values[iTarget] = ((ClassificationStat)m_TargetStat).getAttribute(0).getNbValues();
			} else { // regression
				nb_values[iTarget] = 1; // Nominal values not needed
			}
		}

		
		//DecimalFormat mf = new DecimalFormat("###.000");
		DecimalFormat mf = new DecimalFormat("#00.000");
		mf.setPositivePrefix("+"); //DEBUG
		
		// [nonrule predictor][instance][target][class_value]
		// For regression, class_value = 0 always.
		//double[][][][] rule_pred = new double[nb_rules][nb_rows][nb_target][nb_values];
		double[][][][] nonrule_pred = new double[nb_baseFunctions-nbOfRegularRules][nb_rows][nb_target][];
		
		// Rule predictions.
		OptProbl.RulePred[] rule_pred = new OptProbl.RulePred[nbOfRegularRules];
		for (int jRules = 0; jRules < nbOfRegularRules; jRules++) {
			rule_pred[jRules] = new OptProbl.RulePred(nb_rows,nb_target);
			
			ClusRule rule = getRule(jRules);
			
			// Works only for regression
			double[] targets = ((RegressionStat)rule.predictWeighted(null)).normalizedCopy().getNumericPred();
			
			for (int iTarget = 0; iTarget < nb_target; iTarget++) {
					rule_pred[jRules].m_prediction[iTarget][0] = targets[iTarget];
			}
 		}
		
		
		// If printing predictions to file, print the name of the rule here
		if (outLogFile != null) {
//			DecimalFormat format3 = new DecimalFormat("000");
//			DecimalFormat format2 = new DecimalFormat("00");
			DecimalFormat format6 = new DecimalFormat("000000");
			for (int jRules = 0; jRules < nb_baseFunctions; jRules++) {
				if (getRule(jRules).isRegularRule()) {
					outLogFile.print("R  " + format6.format(jRules+1));
				} else {
//					outLogFile.print("L  "+ format3.format(getRule(jRules).m_descriptiveDimForLinearTerm) + "/"
//					+format2.format(getRule(jRules).m_targetDimForLinearTerm));
					outLogFile.print("L  " + format6.format(jRules+1));
				}
				for (int iTarget = 1; iTarget < nb_target; iTarget++) {
					outLogFile.print("         ");
				}
			}
			outLogFile.println();
		}
		// Index over the instances of data. This loop is first because getting tuples may be slower.
		for (int iRows = 0; iRows < nb_rows; iRows++) {

			DataTuple tuple = data.getTuple(iRows);
			
			for (int jRules = 0; jRules < nb_baseFunctions; jRules++) {
				ClusRule rule = getRule(jRules);

				if (rule.isRegularRule()) {
					//rule_pred[jRules].m_cover[iRows] = rule.covers(tuple);
					//rule_pred[jRules].m_cover.set(iRows,rule.covers(tuple));
					if (rule.covers(tuple))
						rule_pred[jRules].m_cover.set(iRows);
				
					printRuleToFile(outLogFile, mf,
							rule_pred[jRules].m_prediction, rule_pred[jRules].m_cover.get(iRows));	
					
					
				} else { //nonregular rule
					int nonRegIndex = jRules-nbOfRegularRules; // Index of the nonregular rule array.
					
					// Initialize rule predictions for this rule and instance combination
					for (int iTarget=0; iTarget < nb_target;iTarget++) {
						nonrule_pred[nonRegIndex][iRows][iTarget] = new double[nb_values[iTarget]];
					}
	
					if (rule.covers(tuple)) {
	
						// Returns the prediction for the data
						if (isClassification) {
							nonrule_pred[nonRegIndex][iRows] =
								((ClassificationStat)rule.predictWeighted(tuple)).normalizedCopy().getClassCounts();
						} else {
							//Only one nominal value is used for regression.
							double[] targets = ((RegressionStat)rule.predictWeighted(tuple)).normalizedCopy()
												.getNumericPred();
							for (int kTargets = 0; kTargets < nb_target; kTargets++) {
								nonrule_pred[nonRegIndex][iRows][kTargets][0] = targets[kTargets];
							}
						}
					} else { // Rule does not cover the instance. Mark as NaN
						for (int kTargets = 0; kTargets < nb_target; kTargets++)
						{   for (int lValues = 0; lValues < nb_values[kTargets]; lValues++) {
								nonrule_pred[nonRegIndex][iRows][kTargets][lValues] = Double.NaN;
						    }
						}
					}

					printPredictionsToFile(outLogFile, mf, nonrule_pred[nonRegIndex][iRows]);

				}
			} // For rules


			if (outLogFile != null) {
				// Print the real outputs
				//wrt_pred.print(" :: {" + mf.format(true_val[iRows]) + "}\n");
				outLogFile.print(" :: [");
				for (int kTargets = 0; kTargets < nb_target; kTargets++) {
					outLogFile.print(mf.format(trueValues[iRows].m_targets[kTargets]));
					if (kTargets < nb_target-1)	outLogFile.print("; ");
				}
				outLogFile.print("]\n");
			}
		}// For instances of data

		if (outLogFile != null) {
			outLogFile.flush();
		}

		OptProbl.OptParam param = new OptProbl.OptParam(rule_pred, nonrule_pred, trueValues,
				ClusRuleLinearTerm.returnImplicitLinearTermsIfNeeded(data));
		return param;
	}

	private void printRuleToFile(PrintWriter outLogFile, DecimalFormat mf,
			double[][] prediction, boolean covers) {
		if (outLogFile == null) return;
		if (covers) {
			printPredictionsToFile(outLogFile, mf, prediction);
		} else {
			// Not covered, print only zeroes
			double[][] tempPreds = new double[prediction.length][prediction[0].length];
			printPredictionsToFile(outLogFile, mf, tempPreds);
		}
		
	}


	/** If file is given, print predictions to it */
	private void printPredictionsToFile(PrintWriter outLogFile, DecimalFormat mf, 
			double[][] targets) {
		if (outLogFile == null) return;
		
		boolean isClassification = (targets[0].length > 1);
		
		// Print predictions to the file.
		outLogFile.print("[");
		for (int kTargets = 0; kTargets < targets.length; kTargets++)
		{
			if (isClassification) outLogFile.print("{");
			for (int lValues = 0; lValues < targets[kTargets].length; lValues++) {
				outLogFile.print(mf.format(targets[kTargets][lValues]));
				if (lValues < targets[kTargets].length-1)	outLogFile.print("; ");
			}

			if (isClassification) outLogFile.print("}");
			if (kTargets < targets.length-1) outLogFile.print("; ");
		}
		outLogFile.print("]");

	}

	

	/** Shifts rule predictions according to the default 
	 * Makes the targets more equal from gradient point of view when average is considered.*/
	private void shiftRulePredictions(double[] defaultPred) {
		if (Settings.VERBOSE > 0) 
			System.out.println("Shifting rule predictions according to the default prediction.");
		for (int iRule = 1; iRule < getModelSize(); iRule++)
		{
			ClusRule rule = getRule(iRule);
			if (rule.isRegularRule()) {// If this is linear term, do not touch it
				if (!(rule.m_TargetStat instanceof RegressionStat))
					System.err.println("Error: GD optimization is implemented regression only.");

				RegressionStat stat = (RegressionStat) rule.m_TargetStat;

				for (int iTarget = 0; iTarget < stat.m_NbAttrs; iTarget++) {

					stat.m_Means[iTarget] -= defaultPred[iTarget];
					stat.m_SumValues[iTarget] = stat.m_Means[iTarget];
					stat.m_SumWeights[iTarget] = 1;
				}
			}
		}
	}

	/** Changes the rule predictions such that they always predict 1. This is by Friedman 2005; 
	 * @param stdDevs Used for omitting. Target std devs.*/
//	private void omitRulePredictions(double[] stdDevs) {
	private void omitRulePredictions() {

		if (Settings.VERBOSE > 0) 
			System.out.println("Omitting rule predictions for optimization.");
		
		// It depends on inner normalization if rule0 is omitted
		// If normalization is done in such a way that x-avg for all the targets, do not omit because it will be omitted later anyway.
		int iRule = 0;
		if (getSettings().isOptNormalization() && 
				getSettings().getOptNormalization() != Settings.OPT_NORMALIZATION_ONLY_SCALING)
			iRule = 1;
			
		for (; iRule < getModelSize(); iRule++)
		{
			ClusRule rule = getRule(iRule);
			if (rule.isRegularRule()) {// If this is linear term, do not touch it
				if (!(rule.m_TargetStat instanceof RegressionStat))
					System.err.println("Error: GD optimization is implemented regression only.");

				RegressionStat stat = (RegressionStat) rule.m_TargetStat;


				if (stat.m_NbAttrs > 1) {

					//double scalingValue = Double.POSITIVE_INFINITY;
					double scalingValue = 0.0;
					// We try to find the value that is greatest compared to std dev.
					// However, in actual scaling, we may not be using std dev information.
					double maxCompareValue = 0.0;
					for (int iTarget = 0; iTarget < stat.m_NbAttrs; iTarget++) {
//						if (Math.abs(stat.m_Means[iTarget]) < Math.abs(scalingValue)) {
//						if (Math.abs(stat.m_Means[iTarget]) > Math.abs(scalingValue)) {
						if (Math.abs(stat.m_Means[iTarget]/(2*getTargStd(iTarget))) 
								> Math.abs(maxCompareValue)) {
						
							maxCompareValue = stat.m_Means[iTarget]/(2*getTargStd(iTarget));

							scalingValue = stat.m_Means[iTarget]; //Scales values to abs(x)<=1
							
							if (getSettings().isOptNormalization()) 
								 // After normalization, the greatest value is 1
								scalingValue /= 2*getTargStd(iTarget);
								
						}
					}

					if (scalingValue == 0.0)
						scalingValue = 1.0; // Do not scale

					for (int iTarget = 0; iTarget < stat.m_NbAttrs; iTarget++) {
						//means[i] = m_SumWeights[i] != 0.0 ? m_SumValues[i] / m_SumWeights[i] : 0.0;
//					stat.m_Means[iTarget] += Math.copySign(1.0, scalingValue)-scalingValue;
//							// Here the one nearer to zero should have value -1 or 1

						stat.m_Means[iTarget] /= scalingValue;
						//  Bigger one (on absolute value) is 1
						stat.m_SumValues[iTarget] = stat.m_Means[iTarget];
						stat.m_SumWeights[iTarget] = 1;
						
//						System.out.println(stat.m_Means[iTarget]/(2*getTargStd(iTarget)));
					}
				} else {

					// Single target
					if (getSettings().isOptNormalization()) 
						stat.m_Means[0] = 2*getTargStd(0);
					else
						stat.m_Means[0] = 1;
					stat.m_SumValues[0] = stat.m_Means[0];
					stat.m_SumWeights[0] = 1;
				}

			}
		}
	}
	
	private double getTargStd(int iTarg) {
		return RuleNormalization.getTargStdDev(iTarg);
	}
	/* Scales the rule predictions so that most general (most covering) rules have bigger predictions.
	 * Thus they are more favored when optimized.
	 * Does not touch linear terms.
	 */
	private void weightGeneralityForPredictions(int nbOfExamples) {
		if (Settings.VERBOSE > 0) 
			System.out.println("Scaling the rule predictions for generalization weighting.");
		for (int iRule = 0; iRule < getModelSize(); iRule++)
		{
			ClusRule rule = getRule(iRule);
			if (rule.isRegularRule()) {// If this is linear term, do not touch it
				if (!(rule.m_TargetStat instanceof RegressionStat))
					System.err.println("Error: GD optimization is implemented regression only.");

				RegressionStat stat = (RegressionStat) rule.m_TargetStat;
				double scalingFactor = stat.m_SumWeight/(double) nbOfExamples;
				for (int iTarget = 0; iTarget < stat.m_NbAttrs; iTarget++) {
					stat.m_Means[iTarget] *= scalingFactor;
					stat.m_SumValues[iTarget] = stat.m_Means[iTarget];
					stat.m_SumWeights[iTarget] = 1;
				}
			}
		}
	}

	/**
	 * Add rules in the given set to this rule set.
	 * Uniqueness is not checked because it is based on the test only.
	 * Thus different predictions for rules do not make rules different.
	 * @param newRules Rules to be added.
	 * @param addOnlyUnique Add only unique rules.
	 * @return How many added rules were unique when only descriptions are considered.
	 */
	public int addRuleSet(ClusRuleSet newRules) {
		return addRuleSet(newRules, true);
	}

	/**
	 * Add rules in the given set to this rule set.
	 * Uniqueness is not checked because it is based on the test only.
	 * Thus different predictions for rules do not make rules different.
	 * @param newRules Rules to be added.
	 * @param addOnlyUnique Add only unique rules?
	 * @return How many added rules were added when only descriptions are considered.
	 */
	public int addRuleSet(ClusRuleSet newRules, boolean addOnlyUnique) {
		int numberAdded = 0;
		for (int iRule = 0; iRule < newRules.getModelSize(); iRule++)
		{

			if (addOnlyUnique) {
				// If we are optimizing the rule set and we are omitting the rule predictions,
				// only uniqueness of the descriptive part is important.
				if (getSettings().isRulePredictionOptimized() && getSettings().isOptOmitRulePredictions()) {
					if (addIfUnique(newRules.getRule(iRule)))
						numberAdded++;
				} else {// Add a rule from addRules to this rule set if also target part is different
					if (addIfUniqueDeeply(newRules.getRule(iRule)))
						numberAdded++;
				}
			} else { // add always, duplicates most likely exist
				m_Rules.add(newRules.getRule(iRule));
				numberAdded++;
			}
		}
		return numberAdded;
	}

	/** For optimization, we need to make the default rule a real rule. We need a weight for it etc.
	 * This rule is the rule that covers all the examples (no test statement).
	 * Also if set in settings, remove the default prediction from other rules.
	 * return Default prediction. This may be removed from other base predictors.*/
	public double[] addDefaultRuleToRuleSet() {
		if (Settings.VERBOSE > 0) 
			System.out.println("Adding default rule explicitly to rule set.");
		ClusRule defaultRuleForEnsembles = new ClusRule(m_StatManager);

		defaultRuleForEnsembles.m_TargetStat = m_TargetStat;

		// Change the default rule to the overall mean of data set - may be slightly different and this
		// causes problems later
		int nbOfTargetAtts = m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET).getNbAttributes();
		double[] newPred = new double[nbOfTargetAtts];
		for (int i = 0; i < nbOfTargetAtts; i++) {
			newPred[i] = RuleNormalization.getTargMean(i);
		}		
		defaultRuleForEnsembles.setNumericPrediction(newPred);
		
		m_Rules.add(0, defaultRuleForEnsembles); // Adds the default rule to the first position.
		m_TargetStat = null; // To make sure this is not used anymore
		m_allCoveringRuleExists = true;
		
//		double[] targets = null;
//		
//		if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
//			// 	Works only for regression
//			targets = ((RegressionStat)defaultRuleForEnsembles.
//					predictWeighted(null)).normalizedCopy().getNumericPred();
//
//		}
		
		return newPred;
	}

	/** For optimization we can add the numerical descriptive attributes to the ensemble. We add them
	 * as rules that always give the prediction of descriptive attribute
	 */
	//private void addLinearTermsToRuleSet(double[] mins, double[] maxs, double[] means, double[] stdDevs) {
	private void addLinearTermsToRuleSet() {
		if (Settings.VERBOSE > 0) 
			System.out.println("Adding linear terms as rules.");
		
		int nbTargets = (m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET)).getNbAttributes();
		int nbDescrAttr = m_StatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE).length; 
			//(m_StatManager.getStatistic(ClusAttrType.ATTR_USE_DESCRIPTIVE)).getNbAttributes();

		//NumericAttrType[] numTypes = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);


		for (int iDescriptDim = 0; iDescriptDim < nbDescrAttr; iDescriptDim++){
			for (int iTargetDim = 0; iTargetDim < nbTargets; iTargetDim++) {
				//ClusRule newRule = new ClusRule(m_StatManager, numTypes[iDescriptDim].getArrayIndex(), iTargetDim,
//				addSingleLinearTerm(iDescriptDim, iTargetDim, maxs[iDescriptDim],
//						mins[iDescriptDim], means, stdDevs, nbTargets);
				ClusRuleLinearTerm newTerm = new ClusRuleLinearTerm(m_StatManager, iDescriptDim, iTargetDim);
				m_Rules.add(newTerm);
			}
		}


		if (Settings.VERBOSE > 0) 
			System.out.println("\tAdded "+ nbDescrAttr +" linear terms for each target, total " + nbDescrAttr*nbTargets
				         + " terms.");
	}

	/** Add a linear term with weight */
	private void addSingleLinearTerm(int iDescriptDim, int iTargetDim, double weight) {
		ClusRuleLinearTerm newTerm = new ClusRuleLinearTerm(m_StatManager, iDescriptDim, iTargetDim);
		newTerm.setOptWeight(weight);
		m_Rules.add(newTerm);
	}

	/**	If used implicit linear term usage, add the linear terms with great enough weight. 
	 * @param weights Optimized weights.
	 * @param indFirstLinTerm Index of first linear term. */
	public void addImplicitLinearTermsExplicitly(ArrayList<Double> weights, int indFirstLinTerm) {

		double threshold = getSettings().getOptRuleWeightThreshold();
		int nbOfTargetAtts = m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET).getNbAttributes();
		
		int addedTerms = 0;
		// Add the linear terms that are useful enough
		for (int iLinTerm = indFirstLinTerm; iLinTerm < weights.size(); iLinTerm++) {	
			if (Math.abs(weights.get(iLinTerm)) >= threshold && weights.get(iLinTerm) != 0.0) {
				addSingleLinearTerm((int) Math.floor((double)(iLinTerm-indFirstLinTerm)/nbOfTargetAtts),
						(iLinTerm-indFirstLinTerm)%nbOfTargetAtts, ((Double)weights.get(iLinTerm)).doubleValue());	
				addedTerms++;
			}

			//addSingleLinearTerm(int iDescriptDim, int iTargetDim)
		}	
		if (Settings.VERBOSE > 0) 
			System.out.println("\tAdded "+ addedTerms +" linear terms explicitly to the set.");
		
		ClusRuleLinearTerm.DeleteImplicitLinearTerms(); // Not needed anymore

	}
}
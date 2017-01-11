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
 * Created on May 3, 2005
 */
package clus.algo.rules;

import java.io.*;
import java.util.ArrayList;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.RowData;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.test.*;
import clus.tools.optimization.GDAlg;
import clus.tools.optimization.OptAlg;
import clus.tools.optimization.OptProbl;
import clus.tools.optimization.de.DeAlg;
import clus.util.*;

/** Rule set created from a tree. Use by decision tree command line parameter.
 *
 */
public class ClusRulesFromTree {

	/** Has something to do with prediction validity.
	 * If true, we only want to validate the rule construction if no other reason for creating is stated?
 	 * Currently always true. This is also the only reason the class is not static...
	 */
	protected boolean m_Validated;

	/**
	 * Currently two modes are supported:
	 * Settings.CONVERT_RULES_LEAVES:
	 *   Generate one rule for each leaf of the tree
	 * Settings.CONVERT_RULES_ALLNODES:
	 *   Generate one rule for each node of the tree (including internal nodes) except for the root
	 */
	protected int m_Mode;

	/** The parameter seems to be always true? */
	public ClusRulesFromTree(boolean onlyValidated, int mode) {
		m_Validated = onlyValidated;
		m_Mode = mode;
	}

	/**
 	 * Same as constructRules(ClusNode node, ClusStatManager mgr) but
	 * with additional parameter - ClusRun to get access to the data set.
	 * This is for computing the data set dispersion.
	 *
	 * @param cr ClusRun
	 * @param node The root node of the tree
	 * @param mgr The data in statistics manager may be used.
	 * @param computeDispersion Do we want to compute dispersion (and include the data to rule set).
	 * @param optimizeRuleWeights Do we want to optimize the rule weight.
	 * @return The created rule set.
	 * @throws ClusException
	 * @throws IOException
	 */
	public ClusRuleSet constructRules(ClusRun cr, ClusNode node, ClusStatManager mgr,
									  boolean computeDispersion, int optimizeRuleWeights)
	throws ClusException, IOException {

		ClusRuleSet ruleSet = constructRules(node, mgr);

		RowData data = (RowData)cr.getTrainingSet();

		// Optimizing rule set if needed
		if (optimizeRuleWeights == Settings.RULE_PREDICTION_METHOD_OPTIMIZED ||
				optimizeRuleWeights == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED) {
			OptAlg optAlg = null;

			OptProbl.OptParam param = ruleSet.giveFormForWeightOptimization(null, data);

			// Find the rule weights with optimization algorithm.
			if (optimizeRuleWeights == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED) {
				optAlg = (OptAlg) new GDAlg(mgr, param, ruleSet);
			} else {
				optAlg = (OptAlg) new DeAlg(mgr, param, ruleSet);
			}

			ArrayList<Double> weights = optAlg.optimize();

			// Print weights of rules
			System.out.print("The weights for rules from trees:");
			for (int j = 0; j < ruleSet.getModelSize(); j++) {
				ruleSet.getRule(j).setOptWeight(((Double)weights.get(j)).doubleValue());
				System.out.print(((Double)weights.get(j)).doubleValue()+ "; ");
			}
			System.out.print("\n");
			ruleSet.removeLowWeightRules();
			// RowData data_copy = (RowData)data.cloneData();
			// updateDefaultRule(rset, data_copy);
		}

		if (computeDispersion)
		{
			// For some kind of reason all that is here was not done if dispersion was not computed
			RowData testdata;
			ruleSet.addDataToRules(data);
			// res.setTrainErrorScore();

			ruleSet.computeDispersion(ClusModel.TRAIN);
			ruleSet.removeDataFromRules();
			if (cr.getTestIter() != null) {
				testdata = (RowData)cr.getTestSet();
				ruleSet.addDataToRules(testdata);
				// res.setTrainErrorScore();
				ruleSet.computeDispersion(ClusModel.TEST);
				ruleSet.removeDataFromRules();
			}
		}

		// Give each rule a unique number (1..nbRules)
		ruleSet.numberRules();
		return ruleSet;
	}

	/**
	 * Construct rules from terminal nodes of the given tree. Does not do any processing
	 * like dispersion or weight optimization. Especially used
	 * when transforming multiple trees to rules.
	 * @param node The root node of the tree
	 * @param mgr The data in statistics manager may be used
	 * @return Rule set.
	 */
	public ClusRuleSet constructRules(ClusNode node, ClusStatManager mgr) {
		ClusRuleSet ruleSet = new ClusRuleSet(mgr);
		ClusRule init = new ClusRule(mgr);
//		System.out.println("Constructing rules from a tree.");
		constructRecursive(node, init, ruleSet);
		ruleSet.removeEmptyRules();
		ruleSet.simplifyRules();
		ruleSet.setTargetStat(node.getTargetStat());
		return ruleSet;
	}

	/** Only terminal nodes are added to rule set */
	public void constructRecursive(ClusNode node, ClusRule rule, ClusRuleSet set) {
		if (node.atBottomLevel() || m_Mode == Settings.CONVERT_RULES_ALLNODES) {
			if (!m_Validated || node.getTargetStat().isValidPrediction()) {
				rule.setTargetStat(node.getTargetStat());
				rule.setID(node.getID());
				set.add(rule);
			}
		}
		NodeTest test = node.getTest();
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			NodeTest branchTest = test.getBranchTest(i);
			ClusRule child_rule = rule.cloneRule();
			child_rule.addTest(branchTest);
			constructRecursive(child, child_rule, set);
		}
	}
}


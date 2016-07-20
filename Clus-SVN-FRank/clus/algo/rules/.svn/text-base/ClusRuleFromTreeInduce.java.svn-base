package clus.algo.rules;

import java.io.IOException;
//import java.util.ArrayList;

import clus.Clus;
//import clus.algo.tdidt.ClusDecisionTree;
import clus.algo.tdidt.ClusNode;
//import clus.algo.tdidt.DepthFirstInduce;
import clus.algo.rules.ClusRulesFromTree;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
//import clus.ext.ensembles.ClusBoostingForest;
import clus.ext.ensembles.ClusEnsembleInduce;
import clus.ext.ensembles.ClusForest;
import clus.main.ClusRun;
//import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;
//import clus.algo.tdidt.ClusNode;

/**
 * Create rules by decision tree ensemble algorithms (forests).
 * Use this by 'CoveringMethod = RulesFromTree' .
 *
 * This has to be own induce class because we need Clus instance for creating tree ensemble.
 * @author Timo Aho
 *
 *
 */
public class ClusRuleFromTreeInduce extends ClusRuleInduce {

	protected Clus m_Clus;

	public ClusRuleFromTreeInduce(ClusSchema schema, Settings sett, Clus clus)
			throws ClusException, IOException {
		super(schema, sett);
		m_Clus = clus;
		sett.setSectionEnsembleEnabled(true); // For printing out the ensemble texts
		getSettings().setEnsembleMode(true); // For ensemble things working
	}


	/**
	 * Induces rules from ensemble tree, similar to ClusRuleInduce.induce
	 */
	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {

		// The params may already have been disabled, thus we do not want to disable them again
		// (forgets original values)
//		getSettings().returnRuleInduceParams();
		getSettings().disableRuleInduceParams();
		
		// Train the decision tree ensemble with hopefully all the available settings.
		ClusEnsembleInduce ensemble = new ClusEnsembleInduce(this, m_Clus);

		ensemble.induceAll(cr);
		getSettings().returnRuleInduceParams();
		
		// Following  might cause problems
		//	clus.main.ClusStatManager.heuristicNeedsCombStat() -- ok as long as Heuristic = VarianceReduction	
		//	clus.main.ClusStatManager.initDispersionWeights -- ok (just printing)
		//clus.main.ClusStatManager.initHeuristic -- treetorule was already excluded
		//clus.main.ClusStatManager.initNormalizationWeights -- ok does not hold for tree to rule
		//clus.main.ClusStatManager.initWeighs -- ok, if left away makes the ensemble be as good as it is with forest only
		// however, not sure if this affects the FIRE at all - normalization for rules (changes weights)
		// clus.initialize -- ok, the undefined values have to be restored(?)
		
		/** The real trained ensemble model without pruning. Use unpruned tree because weight optimizing
		 * should get rid of bad rules anyway. */
		ClusForest forestModel = (ClusForest)cr.getModel(ClusModel.ORIGINAL);

		/**
		 * The class for transforming single trees to rules
		 */
		ClusRulesFromTree treeTransform = new ClusRulesFromTree(true, getSettings().rulesFromTree()); // Parameter always true
		ClusRuleSet ruleSet = new ClusRuleSet(getStatManager()); // Manager from super class

		//ClusRuleSet ruleSet = new ClusRuleSet(m_Clus.getStatManager());

		// Get the trees and transform to rules
		int numberOfUniqueRules = 0;

		for (int iTree = 0; iTree < forestModel.getNbModels(); iTree++)
		{
			// Take the root node of the tree
			ClusNode treeRootNode = (ClusNode)forestModel.getModel(iTree);

			// Transform the tree into rules and add them to current rule set
			numberOfUniqueRules +=
				ruleSet.addRuleSet(treeTransform.constructRules(treeRootNode,
						getStatManager()));
		}

		if (Settings.VERBOSE > 0) 
			System.out.println("Transformed " + forestModel.getNbModels() + " trees in ensemble into rules.\n\tCreated " +
				+ ruleSet.getModelSize() +  " rules. (" + numberOfUniqueRules + " of them are unique.)");

		RowData trainingData = (RowData)cr.getTrainingSet();

		// ************************** The following copied from ClusRuleInduce.separateAndConquor
		// Do not have any idea what it is about

		// The default rule
		ClusStatistic left_over;
		if (trainingData.getNbRows() > 0) {
			left_over = createTotalTargetStat(trainingData);
			left_over.calcMean();
		} else {
			if (Settings.VERBOSE > 0) System.out.println("All training examples covered - default rule on entire training set!");
			ruleSet.m_Comment = new String(" (on entire training set)");
			left_over = getStatManager().getTrainSetStat(ClusAttrType.ATTR_USE_TARGET).cloneStat();
			left_over.copy(getStatManager().getTrainSetStat(ClusAttrType.ATTR_USE_TARGET));
			left_over.calcMean();
			//left_over.setSumWeight(0);
			System.err.println(left_over.toString());
		}
		System.out.println("Left Over: "+left_over);
		ruleSet.setTargetStat(left_over);




		// ************************** The following are copied from ClusRuleInduce.induce
		// Do not have much idea what it is about. However, optimization is needed

		// The rule set was altered. Compute the means (predictions?) for rules again.
		ruleSet.postProc();


		// Optimizing rule set
		if (getSettings().isRulePredictionOptimized()) {
			ruleSet = optimizeRuleSet(ruleSet, (RowData)cr.getTrainingSet());
		}
//		ruleSet.setTrainErrorScore(); // Seems to be needed only for some other covering method. Not always needed?
//		ruleSet.addDataToRules(trainingData); // May take huge amount of memory

		// Computing dispersion
		if (getSettings().computeDispersion()) {
			ruleSet.computeDispersion(ClusModel.TRAIN);
			ruleSet.removeDataFromRules();
			if (cr.getTestIter() != null) {
				RowData testdata = (RowData)cr.getTestSet(); // or trainingData?
				ruleSet.addDataToRules(testdata);
				ruleSet.computeDispersion(ClusModel.TEST);
				ruleSet.removeDataFromRules();
			}
		}

		// Number rules (for output purpose in WritePredictions)
		ruleSet.numberRules();
		return ruleSet;
	}

	/**
	 * Induces the rule models. ClusModel.PRUNED = the optimized rule model
	 * ClusModel.DEFAULT = the ensemble tree model.
	 */
	public void induceAll(ClusRun cr) throws ClusException, IOException {
		RowData trainData = (RowData)cr.getTrainingSet();
		getStatManager().getHeuristic().setTrainData(trainData);
//		ClusStatistic trainStat = getStatManager().getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING);
//		double value = trainStat.getDispersion(getStatManager().getClusteringWeights(), trainData);
//		getStatManager().getHeuristic().setTrainDataHeurValue(value);

		
		// Adds a single default predictor, however forest automatically adds a 
		// forest of stumps as a default predictor.
//		ClusModelInfo default_model = cr.addModelInfo(ClusModel.DEFAULT);
//		ClusModel def = ClusDecisionTree.induceDefault(cr);
//		default_model.setModel(def);
//		default_model.setName("Default");

//		ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL);
//		model_info.setName("Original");
//		model_info.setModel(model);

		// Only pruned used for rules.
		ClusModel model = induceSingleUnpruned(cr);
		ClusModelInfo rules_model = cr.addModelInfo(ClusModel.RULES);
		rules_model.setModel(model);
		rules_model.setName("Rules");
	}

}


package si.ijs.kt.clus.algo.rules;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROSForestInfo;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROSModelInfo;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Create rules by decision tree ensemble algorithms (forests).
 * Use this by 'CoveringMethod = RulesFromTree' .
 *
 * This has to be own induce class because we need Clus instance for creating tree ensemble.
 * 
 * @author Timo Aho
 *
 *
 */
public class ClusRuleFromTreeInduce extends ClusRuleInduce {

    protected Clus m_Clus;


    public ClusRuleFromTreeInduce(ClusSchema schema, Settings sett, Clus clus) throws ClusException, IOException {
        super(schema, sett);
        m_Clus = clus;
        sett.getEnsemble().setSectionEnsembleEnabled(true); // For printing out the ensemble texts
        getSettings().getEnsemble().setEnsembleMode(true); // For ensemble things working
    }


    /**
     * Induces rules from ensemble tree, similar to ClusRuleInduce.induce
     * 
     * @throws Exception
     */
    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws Exception {

        // The params may already have been disabled, thus we do not want to disable them again
        // (forgets original values)
        // getSettings().returnRuleInduceParams();
        getSettings().getRules().disableRuleInduceParams();

        // Train the decision tree ensemble with hopefully all the available settings.
        ClusEnsembleInduce ensemble = new ClusEnsembleInduce(this, m_Clus);

        ensemble.induceAll(cr);
        getSettings().getRules().returnRuleInduceParams();

        // Following might cause problems
        // clus.main.ClusStatManager.heuristicNeedsCombStat() -- ok as long as Heuristic = VarianceReduction
        // clus.main.ClusStatManager.initDispersionWeights -- ok (just printing)
        // clus.main.ClusStatManager.initHeuristic -- treetorule was already excluded
        // clus.main.ClusStatManager.initNormalizationWeights -- ok does not hold for tree to rule
        // clus.main.ClusStatManager.initWeighs -- ok, if left away makes the ensemble be as good as it is with forest
        // only
        // however, not sure if this affects the FIRE at all - normalization for rules (changes weights)
        // clus.initialize -- ok, the undefined values have to be restored(?)

        /**
         * The real trained ensemble model without pruning. Use unpruned tree because weight optimizing
         * should get rid of bad rules anyway.
         */
        ClusForest forestModel = (ClusForest) cr.getModel(ClusModel.ORIGINAL);

        /**
         * The class for transforming single trees to rules
         * Parameter always true
         */
        ClusRulesFromTree treeTransform = new ClusRulesFromTree(true, getSettings().getTree().rulesFromTree());
        ClusRuleSet ruleSet = new ClusRuleSet(getStatManager()); // Manager from super class

        // Get the trees and transform to rules
        int numberOfUniqueRules = 0;
        ClusStatManager sm = getStatManager();

        /** if ROS enabled */
        ClusROSForestInfo rosForestInfo = forestModel.getEnsembleROSForestInfo();
        ClusROSModelInfo info = null;

        for (int tree = 0; tree < forestModel.getNbModels(); tree++) {
            // Take the root node of the tree
            ClusNode treeRootNode = (ClusNode) forestModel.getModel(tree);

            switch (getSettings().getEnsemble().getEnsembleROSAlgorithmType()) {
                case FixedSubspaces:
                case DynamicSubspaces:
                    info = rosForestInfo.getROSModelInfo(tree);
                    treeRootNode.setROSModelInfo(info);
                    break;
                default:
                    break;
            }

            // Transform the tree into rules and add them to current rule set
            ClusRuleSet rs = treeTransform.constructRules(treeRootNode, sm);

            numberOfUniqueRules += ruleSet.addRuleSet(rs);

        }
        ClusLogger.info("Transformed " + forestModel.getNbModels() + " trees in ensemble into rules. Created " + ruleSet.getModelSize() + " rules. (" + numberOfUniqueRules + " of them are unique.)");

        // The default rule, which will later be explicitly added to the rule set (i.e., will be equal to other rules)
        // in order to properly optimize the rule set
        RowData trainingData = (RowData) cr.getTrainingSet();
        ClusLogger.info("Calculating the default rule predictions");
        ClusStatistic left_over = createTotalTargetStat(trainingData);
        ruleSet.setTargetStat(left_over);

        // The rule set was altered. Compute the means (predictions?) for rules again.
        ruleSet.postProc();

        // Optimizing rule set
        if (getSettings().getRules().isRulePredictionOptimized()) {
            ruleSet = optimizeRuleSet(ruleSet, (RowData) cr.getTrainingSet());
        }

        // Computing dispersion
        if (getSettings().getRules().computeDispersion()) {
            ruleSet.computeDispersion(ClusModel.TRAIN);
            ruleSet.removeDataFromRules();
            if (cr.getTestIter() != null) {
                RowData testdata = cr.getTestSet(); // or trainingData?
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
     * 
     * @throws Exception
     */
    @Override
    public void induceAll(ClusRun cr) throws Exception {
        RowData trainData = (RowData) cr.getTrainingSet();
        getStatManager().getHeuristic().setTrainData(trainData);

        // Only pruned used for rules.
        ClusModel model = induceSingleUnpruned(cr);
        ClusModelInfo rules_model = cr.addModelInfo(ClusModel.RULES);
        rules_model.setModel(model);
        rules_model.setName("Rules");
    }

}

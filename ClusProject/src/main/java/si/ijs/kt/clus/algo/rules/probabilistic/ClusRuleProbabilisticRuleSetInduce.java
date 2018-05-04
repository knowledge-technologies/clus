
package si.ijs.kt.clus.algo.rules.probabilistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.rules.ClusRuleInduce;
import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.algo.rules.ClusRulesFromTree;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.optiontree.DepthFirstInduceWithOptions;
import si.ijs.kt.clus.ext.optiontree.MyNode;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsOutput.ConvertRules;
import si.ijs.kt.clus.main.settings.section.SettingsRules.InitialRuleGeneratingMethod;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.tools.optimization.sls.OptSmoothLocalSearch;


public class ClusRuleProbabilisticRuleSetInduce extends ClusRuleInduce {

    // parameters
    long m_seed = 1;
    Integer m_maxRuleCardinality = null; // number of max tests in a rule
    Integer m_maxRulesNb = null; // max number of rules in a rule set
    double m_validationRatio = 0.05; // percentage of train test to use for validation

    Random m_randGen; // an instance of random generator
    ClusRun m_mainClusRun = null;
    Clus m_mainClus = null;
    RowData m_trainingData = null; // training data
    RowData m_validationData = null; // validation data
    RowData m_originalFullData = null; // initial data stored here
    int m_ensembleSize; // ensemble size
    boolean estimateWeights = true;

    // debug
    String m_folderName = "rules.debug";


    /**
     * Constructor
     * 
     * @param schema
     *        ClusSchema object
     * @param sett
     *        Settings object
     * @param clus
     *        Clus object
     * @throws ClusException
     * @throws IOException
     */
    public ClusRuleProbabilisticRuleSetInduce(ClusSchema schema, Settings sett, Clus clus) throws ClusException, IOException {
        super(schema, sett);

        if (sett.getGeneral().hasRandomSeed()) {
            m_seed = sett.getGeneral().getRandomSeed();
        }

        m_randGen = new Random(m_seed);

        m_mainClus = clus;

        m_maxRuleCardinality = sett.getRules().getMaxRuleCardinality();
        m_maxRulesNb = sett.getRules().getMaxRulesNb();
        m_validationRatio = sett.getRules().getValidationSetPercentage();
        m_ensembleSize = sett.getEnsemble().getNbBaggingSets().getInt();
    }


    void splitData(double validationRatio) {
        // splitting initial dataset into two parts
        RowData all_data = (RowData) m_originalFullData.cloneData();

        all_data.addIndices(); // enumerate data !!!!!

        int validationSize = (int) (all_data.getNbRows() * validationRatio);
        int trainSize = all_data.getNbRows() - validationSize;

        if (trainSize < 1.0)
            System.err.println("Validation set is too big!");
        System.out.println(String.format("Splitting learning data: Train set: %s examples | Validation set: %s examples", trainSize, validationSize));

        RowData validationData = new RowData(getSchema());
        RowData trainData = new RowData(getSchema());

        // randomly take examples from dataset ant place them into validation set
        int newIndex = -1;
        ArrayList<Integer> trainingExamples = new ArrayList<Integer>();
        ArrayList<Integer> validationExamples = new ArrayList<Integer>();

        while (validationData.getNbRows() < validationSize) {
            newIndex = m_randGen.nextInt(all_data.getNbRows() - 1);
            // newIndex = randInt(0, all_data.getNbRows() - 1);
            if (!validationExamples.contains(newIndex)) {
                validationData.add(all_data.getTuple(newIndex).cloneTuple());
                validationExamples.add(newIndex);
            }
        }

        for (int i = 0; i < all_data.getNbRows(); i++) {
            if (!trainingExamples.contains(i) && !validationExamples.contains(i)) {
                trainData.add(all_data.getTuple(i).cloneTuple());

                trainingExamples.add(i);
            }
        }

        m_trainingData = trainData;
        m_validationData = validationData;
    }


    @Override
    public void induceAll(ClusRun mainClusRun) throws Exception {
        m_mainClusRun = mainClusRun;
        m_originalFullData = (RowData) mainClusRun.getTrainingSet();

        // print constraints
        System.out.println("Constraints: MaxRuleCardinality = " + m_maxRuleCardinality + " MaxRuleSetSize = " + m_maxRulesNb);

        // generate objective function
        ClusRuleProbabilisticRuleSetInduceWeights objWeights = new ClusRuleProbabilisticRuleSetInduceWeights();
        ClusRuleProbabilisticRuleSetInduceWeights bestWeights = new ClusRuleProbabilisticRuleSetInduceWeights();

        if (estimateWeights) // otherwise take defaults
        {
            System.out.println("ESTIMATING WEIGHTS");

            getSettings().getEnsemble().setNbBags(1); // make estimation on one PCT

            // get initial rule set
            ClusRuleSet initialRuleSet = getInitialRuleSet(1 - m_validationRatio); // take 10% (1-0.9) of data to create
                                                                                   // rules and estimate weights
            // m_validationData = m_trainingData; // when we run the weight estimation procedure, we just use the
            // training data (reduced)

            // calculate (correct)coverage bit vectors for all data but later use a validation set (this is an
            // optimization)
            initialRuleSet.calculateCoverageBitVectors(m_originalFullData);

            // calculate default rule and prototypes for the sampled set
            calculateDefaultRuleAndPrototypesForRuleSet(initialRuleSet);

            ClusRuleSet finalSet = null; // the final result
            double previousBestScore = -1;
            String bestWeightsString = "";

            // weights to try
            // ArrayList<Integer> lambdas = new ArrayList<Integer>();

            double lowerBound = 0;// 1;
            double upperBound = 2;// 5;
            int upperMax = 1000;
            int lowerMin = -1000;
            double step = 10;
            double tmpval;

            for (double lbd = lowerBound; lbd <= upperBound && lowerBound > lowerMin && upperBound < upperMax; lbd = lbd + 0.1) {
                objWeights.objectiveAccuracyEnabled = true;
                objWeights.objectiveSizeEnabled = true;

                objWeights.WEIGHT_OBJECTIVE_ACCURACY = 1.0;
                objWeights.WEIGHT_OBJECTIVE_SIZE = lbd;

                Function<ClusRuleSet, Double> objectiveFunction = getObjectiveFunction(initialRuleSet, objWeights);

                ClusRuleSet currentSet = runOnce(initialRuleSet, objectiveFunction);

                if ((tmpval = objectiveFunction.apply(currentSet)) > previousBestScore) {
                    finalSet = currentSet;
                    previousBestScore = tmpval;
                    bestWeightsString = objWeights.getWeightsString();

                    bestWeights.setWeights(objWeights.getWeights());
                }

                System.err.println(lbd + ";" + currentSet.computeErrorScore(m_validationData) + ";" + currentSet.getModelSize() + ";" + tmpval);

                if (lbd == upperBound) {
                    // if the last possible weight happens to be the best, change search space
                    if (bestWeights.WEIGHT_OBJECTIVE_SIZE == upperBound) {
                        lowerBound = upperBound;
                        upperBound = upperBound + step;

                        lbd = lowerBound - 1;
                    }
                    // if the first possible weight is the best, also change
                    else if (bestWeights.WEIGHT_OBJECTIVE_SIZE == 1) {
                        upperBound = lowerBound;
                        lowerBound = lowerBound - step;
                        lbd = lowerBound;
                    }
                }
            }

            System.err.println("BEST:\nWeights: " + bestWeightsString + "\nRule set size: " + finalSet.getModelSize() + "\nRMSE: " + finalSet.computeErrorScore(m_validationData));
            estimateWeights = false; // so that rf induce does not save models
            //
            // // calculate statistics - does this work properly?
            // TupleIterator ti = m_trainingData.getIterator();
            // DataTuple tuple;
            // while ((tuple = ti.readTuple()) != null)
            // {
            // for (int i = 0; i < initialRuleSet.getModelSize(); i++)
            // {
            // initialRuleSet.getRule(i).m_TargetStat.updateWeighted(tuple, i);
            // }
            // }
        }

        // take bestWeights (they could have been optimized beforehand)

        // get initial rule set
        getSettings().getEnsemble().setNbBags(m_ensembleSize);
        ClusRuleSet initialRuleSet = getInitialRuleSet(m_validationRatio);

        // calculate (correct)coverage bit vectors for all data but later use a validation set (this is an optimization)
        initialRuleSet.calculateCoverageBitVectors(m_originalFullData);

        // calculate default rule and prototypes for the sampled set
        calculateDefaultRuleAndPrototypesForRuleSet(initialRuleSet);

        // QUICK TEST HACK
        bestWeights.WEIGHT_OBJECTIVE_ACCURACY = 100;

        Function<ClusRuleSet, Double> objectiveFunction = getObjectiveFunction(initialRuleSet, bestWeights);

        ClusRuleSet currentSet = runOnce(initialRuleSet, objectiveFunction);

        ClusRuleHelperMethods.debug_PrintRuleSet(currentSet, m_folderName, "final_rules.txt", true, false);
        ClusRuleHelperMethods.debug_PrintRuleSet(currentSet, m_folderName, "final_tests.txt", false, true);

        // add finalSet to clus run
        m_mainClusRun.addModelInfo(ClusModel.DEFAULT);
        m_mainClusRun.addModelInfo(ClusModel.RULES);
        ClusModelInfo rules_model_info = m_mainClusRun.getModelInfo(ClusModel.RULES);
        rules_model_info.setName("Rules");
        rules_model_info.setModel(currentSet);

        System.err.println("INDUCED: " + currentSet.getModelSize() + "; RMSE: " + currentSet.computeErrorScore(m_validationData));

    }


    ClusRuleSet runOnce(ClusRuleSet initialRuleSet, Function<ClusRuleSet, Double> objectiveFunction) throws ClusException, InterruptedException {
        /**
         * In order to efficiently construct a decision set that maximizes the objective function
         * we run SLS twice with different delta parameters and return the better of the two according
         * to the objectiveFunction value
         */
        OptSmoothLocalSearch optAlg = new OptSmoothLocalSearch(m_maxRulesNb, m_randGen, getStatManager(), m_mainClusRun, this);

        double bias = (double) 1 / 3;

        ClusRuleSet optimizedSet1 = optAlg.SmoothLocalSearch(initialRuleSet, bias, bias, objectiveFunction);
        ClusRuleSet optimizedSet2 = optAlg.SmoothLocalSearch(initialRuleSet, bias, -1, objectiveFunction);

        ClusRuleSet finalSet = objectiveFunction.apply(optimizedSet1) > objectiveFunction.apply(optimizedSet2) ? optimizedSet1 : optimizedSet2; // take
                                                                                                                                                // the
                                                                                                                                                // better
                                                                                                                                                // performing
                                                                                                                                                // of
                                                                                                                                                // the
                                                                                                                                                // two

        // calculate default rule
        calculateDefaultRuleAndPrototypesForRuleSet(finalSet);

        return finalSet;
    }


    /**
     * Calculate the default rule for a given rule set
     * 
     * @param ruleSet
     * @return rule set with a default rule
     * @throws ClusException
     * @throws InterruptedException
     */
    public ClusRuleSet calculateDefaultRuleAndPrototypesForRuleSet(ClusRuleSet ruleSet) throws ClusException, InterruptedException {

        // default rule is over all training set
        RowData trainingData = (RowData) m_mainClusRun.getTrainingSet();
        RowData uncoveredData = trainingData;

        // // find examples that were left uncovered
        // BitSet uncovered = ruleSet.getUncovered(trainingData.getNbRows());
        //
        // // get uncovered data
        // RowData uncoveredData = trainingData.getDataWithMask(uncovered);
        // if (uncoveredData.getNbRows() == 0) // everything is covered, default to full training set
        // {
        // uncoveredData = (RowData) m_mainClusRun.getTrainingSet();
        // ruleSet.m_Comment = new String(" (on entire training set)");
        // }
        //

        // the default rule
        ClusStatistic defaultRule = createTotalTargetStat(uncoveredData);

        defaultRule = getStatManager().getTrainSetStat(AttributeUseType.Target).cloneStat();
        defaultRule.copy(getStatManager().getTrainSetStat(AttributeUseType.Target));
        defaultRule.calcMean();
        ruleSet.setTargetStat(defaultRule);

        ruleSet.postProc();

        return ruleSet;

    }


    /**
     * Create objective function with respect to initial rule set
     * 
     * @param initialRuleSet
     * @return
     */
    Function<ClusRuleSet, Double> getObjectiveFunction(ClusRuleSet initialRuleSet, final ClusRuleProbabilisticRuleSetInduceWeights weights) {
        int tmpMaxGlobalRuleCardinality = 0;
        for (int r = 0; r < initialRuleSet.getRules().size(); r++) {
            if (tmpMaxGlobalRuleCardinality < initialRuleSet.getRule(r).getModelSize())
                tmpMaxGlobalRuleCardinality = initialRuleSet.getRule(r).getModelSize();
        }

        final int N = m_trainingData.getNbRows(); // number of instances in the training dataset (upper bound used in
                                                  // overlap objective)
        final int maxGlobalRuleCardinality = tmpMaxGlobalRuleCardinality; // maximum cardinality of any rule in
                                                                          // initialRuleSet (upper bound used in
                                                                          // cardinality objective)
        final int initialRuleSetSize = initialRuleSet.getModelSize();

        // 1. Conciseness: we favor decision sets with smaller number of rules
        final Function<ClusRuleSet, Double> objectiveRuleSetSize = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                return (double) (initialRuleSetSize - t.getRules().size()) / initialRuleSetSize;
            }
        };

        // 2. Conciseness: we favor decision sets with rules of low cardinality (number of tests)
        final Function<ClusRuleSet, Double> objectiveCardinality = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                double f = 0;
                for (int r = 0; r < t.getRules().size(); r++) {
                    f += (maxGlobalRuleCardinality - t.getRule(r).getTests().size());
                }
                return f;
            }
        };

        // 3. Overlap: we favor decision sets with rules that do not overlap
        Function<ClusRuleSet, Double> objectiveRuleOverlap = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                double f = 0;
                for (int r1 = 0; r1 < t.getRules().size(); r1++) {
                    for (int r2 = r1 + 1; r2 < t.getRules().size(); r2++) {
                        f += (N - t.getRule(r1).overlap(t.getRule(r2)));
                    }
                }
                return f;
            }
        };

        // 4. Accuracy (of the whole rule set)
        final Function<ClusRuleSet, Double> objectiveAccuracy = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                double errorScore = -1; // this will crash the application if catch is invoked
                try {
                    errorScore = t.computeErrorScore(m_validationData);
                    return 1 / Math.abs(errorScore + 1); // if errorScore=0 ==> objectiveValue = 1 (max); else
                                                         // objectiveValue in (0,1)
                    // average RMSE over all targets
                }
                catch (ClusException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return Double.POSITIVE_INFINITY;
                }
            }
        };

        // 5. Correct-Cover (Accuracy per rule)
        final Function<ClusRuleSet, Double> objectiveCorrectCover = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                int correctCoverCount = 0;
                DataTuple tuple;

                for (int example = 0; example < m_validationData.getNbRows(); example++) {
                    tuple = m_validationData.getTuple(example);

                    if (t.correctCoverRuleCount(tuple) >= 1) {
                        correctCoverCount++;
                    }
                }
                return (double) correctCoverCount;
            }
        };

        // 6. Inorrect-Cover (Inaccuracy per rule)
        final Function<ClusRuleSet, Double> objectiveIncorrectCover = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                double f = t.incorrectCoverAcrossAllRules();

                f = (N * t.getRules().size()) - f;

                return f;
            }
        };

        // Final objective function
        final Function<ClusRuleSet, Double> objectiveFunction = new Function<ClusRuleSet, Double>() {

            @Override
            public Double apply(ClusRuleSet t) {
                double sum = (weights.isEnabledObjectiveAccuracy() ? weights.WEIGHT_OBJECTIVE_ACCURACY * objectiveAccuracy.apply(t) : 0) + (weights.isEnabledObjectiveSize() ? weights.WEIGHT_OBJECTIVE_SIZE * objectiveRuleSetSize.apply(t) : 0)
                // + (weights.isEnabledObjectiveCardinality() ? weights.WEIGHT_OBJECTIVE_CARDINALITY *
                // objectiveCardinality.apply(t) : 0)
                // + (weights.isEnabledObjectiveOverlap() ? weights.WEIGHT_OBJECTIVE_OVERLAP *
                // objectiveRuleOverlap.apply(t) : 0)
                // + (weights.isEnabledObjectiveCorrectCover() ? weights.WEIGHT_OBJECTIVE_CORRECT_COVER *
                // objectiveCorrectCover.apply(t) : 0)
                // + (weights.isEnabledObjectiveIncorrectCover() ? weights.WEIGHT_OBJECTIVE_INCORRECT_COVER *
                // objectiveIncorrectCover.apply(t) : 0)
                ;

                // System.out.println(sum);

                return sum;
            }
        };

        return objectiveFunction;

    }


    /**
     * INITIAL RULE GENERATION
     * 
     * @return A set of generated rules which is later used as input for the optimization.
     * @throws Exception
     */
    ClusRuleSet getInitialRuleSet(double validationRatio) throws Exception {
        // split the learning data into train and validation set
        splitData(validationRatio);

        int tmpVerbose = getSettings().getGeneral().enableVerbose(0);

        InitialRuleGeneratingMethod generatingMode = getSettings().getRules().getInitialRuleGeneratingMethod();

        ClusRuleSet initialRules = null;

        switch (generatingMode) {
            case RandomForest:
                initialRules = getRandomForestRules(m_trainingData);
                break;

            case OptionTree:
                initialRules = getOptionTreeRules(m_trainingData);
                break;

            default:
                throw new RuntimeException("Unknown initial rules generation method.");
        }

        // return VERBOSE to its initial value
        getSettings().getGeneral().enableVerbose(tmpVerbose);

        if (initialRules.getModelSize() == 0) { throw new RuntimeException(String.format("No rules have been induced! Decrease validation set size (current value: %s).", getSettings().getRules().getValidationSetPercentage())); }

        // remove rules with too high cardinalities
        for (int r = initialRules.getModelSize() - 1; r >= 0; r--) {
            if (m_maxRuleCardinality < initialRules.getRule(r).getModelSize()) {
                initialRules.remove(initialRules.getRule(r));
            }
        }

        // enumerate rules
        initialRules.numberRules();

        // save to disk
        ClusRuleHelperMethods.debug_PrintInitialRules(initialRules, m_folderName, "InitialRuleSet", true);
        ClusRuleHelperMethods.debug_PrintInitialRules(initialRules, m_folderName, "InitialRuleSet_Reduced", false);

        return initialRules;
    }


    /**
     * Generate initial rules from Random Forest ensemble.
     * 
     * @return A set of generated rules.
     * @throws Exception
     */
    ClusRuleSet getRandomForestRules(RowData dataToUse) throws Exception {
        System.out.println("Inducing random forest for initial rule set");
        System.out.println("==============================================================================");

        boolean ensembleMode = getSettings().getEnsemble().isEnsembleMode();
        boolean sectionEnsembleEnabled = getSettings().getEnsemble().isSectionEnsembleEnabled();

        getSettings().getEnsemble().setSectionEnsembleEnabled(true); // For printing out the ensemble texts
        getSettings().getEnsemble().setEnsembleMode(true); // For ensemble things working

        getSettings().getRules().disableRuleInduceParams();

        // set training data before induction
        m_mainClusRun.setTrainingSet(dataToUse);

        // Train the decision tree ensemble with hopefully all the available settings.
        ClusEnsembleInduce ensemble = new ClusEnsembleInduce(this, m_mainClus);

        // create new run so that it doesn't mix with final rule model
        ClusRun forestRun = new ClusRun(m_mainClusRun);

        ensemble.induceAll(forestRun);
        getSettings().getRules().returnRuleInduceParams();

        ClusForest forestModel = (ClusForest) forestRun.getModel(ClusModel.ORIGINAL);

        ClusRulesFromTree treeTransform = new ClusRulesFromTree(true, ConvertRules.AllNodes);
        ClusRuleSet ruleSet = new ClusRuleSet(getStatManager()); // Manager from super class

        // Get the trees and transform to rules
        int numberOfUniqueRules = 0;
        for (int iTree = 0; iTree < forestModel.getNbModels(); iTree++) {
            // Take the root node of the tree
            ClusNode treeRootNode = (ClusNode) forestModel.getModel(iTree);

            // Transform the tree into rules and add them to current rule set
            numberOfUniqueRules += ruleSet.addRuleSet(treeTransform.constructRules(treeRootNode, getStatManager()));
        }

        System.out.println("Transformed " + forestModel.getNbModels() + " trees in ensemble into rules.\n\tCreated " + +ruleSet.getModelSize() + " rules. (" + numberOfUniqueRules + " of them are unique.)");

        getSettings().getEnsemble().setSectionEnsembleEnabled(sectionEnsembleEnabled); // For printing out the ensemble
                                                                                       // texts
        getSettings().getEnsemble().setEnsembleMode(ensembleMode); // For ensemble things working

        System.out.println("==============================================================================");

        if (!estimateWeights) {
            // get default model and add it to the initial Clus run
            ClusModelInfo ensemble_model_info_default = forestRun.getModelInfo(ClusModel.DEFAULT);
            m_mainClusRun.addModelInfo(ensemble_model_info_default);

            // get PCT ensemble model and add it to the initial Clus run
            ClusModelInfo ensemble_model_info_original = forestRun.getModelInfo(ClusModel.ORIGINAL);
            ensemble_model_info_original.setName("RF Ensemble");
            m_mainClusRun.addModelInfo(ensemble_model_info_original);

            // get initial rule set
            ClusModelInfo rules_model_info = new ClusModelInfo("Rules");
            rules_model_info.setModel(ruleSet);
            m_mainClusRun.addModelInfo(rules_model_info);
        }

        return ruleSet;
    }


    /**
     * Generate initial rules from Option Tree.
     * 
     * @return A set of generated rules.
     * @throws ClusException
     * @throws IOException
     * @throws InterruptedException
     */
    ClusRuleSet getOptionTreeRules(RowData dataToUse) throws ClusException, IOException, InterruptedException {

        boolean sectionOptionEnabled = m_mainClus.getSettings().getOptionTree().isSectionOptionEnabled();
        m_mainClus.getSettings().getOptionTree().setSectionOptionEnabled(true);
        m_mainClus.getStatManager().setRuleInduceOnly(false);

        ClusStatManager mngr = new ClusStatManager(getSchema(), getSettings());

        ClusSummary summary = new ClusSummary();
        summary.setStatManager(mngr);

        ClusRun optionRun = new ClusRun(m_trainingData, summary);

        DepthFirstInduceWithOptions inducer = new DepthFirstInduceWithOptions(optionRun.getStatManager().getSchema(), optionRun.getStatManager().getSettings());
        inducer.initialize();

        inducer.getStatManager().createClusteringStat();
        inducer.getStatManager().createTargetStat();

        inducer.getStatManager().initClusteringWeights();
        // inducer.getStatManager().initWeights();
        inducer.getStatManager().initBeamSearchHeuristic();

        inducer.getStatManager().initHeuristic();
        inducer.getStatManager().initStopCriterion();

        MyNode optionModel = (MyNode) inducer.induceSingleUnpruned(optionRun);
        // get OptionPCT model and add it to the initial Clus run

        ClusModelInfo defInfo = m_mainClusRun.addModelInfo(ClusModel.DEFAULT);
        ClusModel defModel = ClusDecisionTree.induceDefault(m_mainClusRun);
        defInfo.setModel(defModel);

        ClusModelInfo origInfo = m_mainClusRun.addModelInfo(ClusModel.ORIGINAL);
        origInfo.setName("Option Tree");
        origInfo.setModel(optionModel);

        ClusRulesFromTree treeTransform = new ClusRulesFromTree(true, ConvertRules.AllNodes);
        ClusRuleSet ruleSet = new ClusRuleSet(getStatManager()); // Manager from super class

        // Get the option tree and transform it to rules
        int numberOfUniqueRules = 0;
        // Take the root node of the tree
        MyNode treeRootNode = optionModel.getParent();

        // Transform the tree into rules and add them to current rule set
        numberOfUniqueRules += ruleSet.addRuleSet(treeTransform.constructOptionRules(optionModel, getStatManager()));

        for (int i = 0; i < ruleSet.getModelSize(); i++) {
            // ruleSet.getRule(i).computePrediction();
            // ruleSet.getRule(i).computeCovered(m_trainingData);
            ruleSet.getRule(i).getTargetStat().calcMean();
        }

        System.out.println("Transformed an Option Tree into " + ruleSet.getModelSize() + " rules. (" + numberOfUniqueRules + " of them are unique.)");

        m_mainClus.getSettings().getOptionTree().setSectionOptionEnabled(sectionOptionEnabled);

        return ruleSet;

    }

    // /**
    // * Calculate the default rule for a given rule set
    // * @param ruleSet
    // * @return rule set with a default rule
    // */
    // ClusRuleSet calculateDefaultRuleAndPrototypesForRuleSet(ClusRuleSet ruleSet)
    // {
    // RowData trainingData = (RowData)m_mainClusRun.getTrainingSet();
    //
    // // find examples that were left uncovered
    // BitSet uncovered = ruleSet.getUncovered(trainingData.getNbRows());
    //
    // // get uncovered data
    // RowData uncoveredData = trainingData.getDataWithMask(uncovered);
    // if (uncoveredData.getNbRows() == 0) // everything is covered, default to full training set
    // {
    // uncoveredData = (RowData) m_mainClusRun.getTrainingSet();
    // ruleSet.m_Comment = new String(" (on entire training set)");
    // }
    //
    //
    // // the default rule
    // ClusStatistic defaultRule = createTotalTargetStat(uncoveredData);
    //
    // defaultRule = getStatManager().getTrainSetStat(AttributeUseType.Target).cloneStat();
    // defaultRule.copy(getStatManager().getTrainSetStat(AttributeUseType.Target));
    // defaultRule.calcMean();
    // ruleSet.setTargetStat(defaultRule);
    //
    // ruleSet.postProc();
    //
    // return ruleSet;
    //
    // }

    // /**
    // * Smooth Local Search optimization
    // * @param initialRuleSet
    // * @param delta
    // * @param deltaPrime
    // * @return
    // */
    // ClusRuleSet SmoothLocalSearch(ClusRuleSet initialRuleSet, double delta, double deltaPrime, Function<ClusRuleSet,
    // Double> objectiveFunction)
    // {
    // System.out.println("Smooth Local Search optimization: started");
    //
    // // calculate tresholds only once
    // double probabilityBiased = (1+delta)/2;
    // double probabilityPrimeBiased = (1+deltaPrime)/2;
    // ArrayList<Integer> indices = new ArrayList<Integer>();
    //
    // // prepare shuffle index
    // for(int i = 0;i < initialRuleSet.getModelSize(); i++) indices.add(i);
    //
    // // biased rules indices
    // HashSet<Integer> A = new HashSet<Integer>();
    //
    // // estimate of the scale of objective f
    // ClusRuleSet rndSet = randomSampleWithBias(initialRuleSet, A, 0.5, (ArrayList<Integer>) indices.clone()); // delta
    // = 0
    // double OPT = objectiveFunction.apply(rndSet);
    //
    // // error margin for estimates calculations
    // double errorMargin = (1/Math.pow(initialRuleSet.getModelSize(), 2)) * OPT;
    // double[] estimates = new double[initialRuleSet.getModelSize()];
    //
    // // tmp variables
    // ClusRuleSet setWithRuleToCheck;
    // ClusRuleSet setWithoutRuleToCheck;
    // ClusRule ruleToCheck;
    //
    //
    // // calculate estimates
    // startover:
    // {
    // for (int rule = 0; rule < initialRuleSet.getModelSize(); rule++)
    // {
    // // get randomly sampled rule set with bias
    // rndSet = randomSampleWithBias(initialRuleSet, A, probabilityBiased, (ArrayList<Integer>) indices.clone());
    // setWithRuleToCheck = rndSet.cloneRuleSet();
    // setWithoutRuleToCheck = rndSet.cloneRuleSet();
    //
    // // current rule
    // //ruleToCheck = initialRuleSet.getRule(rule).cloneRule();
    // ruleToCheck = initialRuleSet.getRule(rule);
    //
    // // add / remove ruleToCheck from two sets that we are estimating the quality of
    // setWithRuleToCheck.add(ruleToCheck);
    // setWithoutRuleToCheck.remove(ruleToCheck);
    //
    // // calculate estimate
    // estimates[rule] = getExpectedValue(setWithRuleToCheck, errorMargin, objectiveFunction) -
    // getExpectedValue(setWithoutRuleToCheck, errorMargin, objectiveFunction);
    // }
    //
    // //System.out.println("SLS: Finding biased rules");
    // for (int rule = 0; rule < initialRuleSet.getModelSize(); rule++)
    // {
    // if (!A.contains(rule) &&
    // estimates[rule] > 2 * errorMargin)
    // {
    // // add rule to biased set
    // A.add(rule);
    //
    // // go calculate estimates again
    // continue startover;
    // }
    // }
    //
    // System.out.println("SLS: Removing bad rules");
    // for (int rule : A)
    // {
    // if (estimates[rule] < -2*errorMargin)
    // {
    // // remove rule from biased set
    // A.remove(rule);
    //
    // // go calculate estimates again
    // continue startover;
    // }
    // }
    // }// label block
    //
    //
    //
    // // return a random subset with bias deltaPrime on A
    // rndSet = randomSampleWithBias(initialRuleSet, A, probabilityPrimeBiased, (ArrayList<Integer>) indices.clone());
    //
    // System.out.println("Smooth local search optimization: finished");
    // return rndSet;
    // }


    // /**
    // * Randomly sample rules with bias
    // * @param initialRuleSet
    // * @param indicesOfBiasedRules
    // * @param probabilityBiased (1+delta)/2 probabilityNormal = 1- (probabilityBiased)
    // * @param calculateDefaultRuleAndPrototypes Should the method calculate default rule and prototypes?
    // * @return A randomly sampled rule set with bias towards some of the elements
    // */
    // ClusRuleSet randomSampleWithBias(ClusRuleSet initialRuleSet, HashSet indicesOfBiasedRules, double
    // probabilityBiased, ArrayList<Integer> indices)
    // {
    // ClusRuleSet returnSet = new ClusRuleSet(getStatManager());
    // double prob;
    //
    // Collections.shuffle(indices, m_randGen); // important to use the same rnd generator as with everything else, in
    // order to get repeatable results
    //
    // int rule;
    // for (int index = 0; index < Math.min(indices.size(), m_maxRulesNb); index++)
    // {
    // rule = indices.get(index);
    // // ask God
    // prob = m_randGen.nextDouble();
    //
    // if ((indicesOfBiasedRules.contains(rule) && prob >= (1-probabilityBiased)) ||
    // (!indicesOfBiasedRules.contains(rule) && prob >= probabilityBiased))
    // {
    // //returnSet.add(initialRuleSet.getRule(rule).cloneRule());
    // returnSet.add(initialRuleSet.getRule(rule));
    // }
    // }
    //
    // // calculate default rule and prototypes for the sampled set
    // calculateDefaultRuleAndPrototypesForRuleSet(returnSet);
    //
    //
    // return returnSet;
    // }
    //
    // /**
    // * Randomly sample from rule set without bias
    // * @param initialRuleSet
    // * @return Randomly sampled rule set
    // */
    // ClusRuleSet randomSampleWithoutBias(ClusRuleSet rules)
    // {
    // ClusRuleSet returnSet = new ClusRuleSet(getStatManager());
    // for (int rule = 0; rule < rules.getModelSize(); rule++)
    // {
    // // ask God
    // if (m_randGen.nextDouble() >= 0.5)
    // {
    // //returnSet.add(rules.getRule(rule).cloneRule());
    // returnSet.add(rules.getRule(rule));
    // }
    // }
    //
    // // calculate default rule and prototypes for the sampled set
    // calculateDefaultRuleAndPrototypesForRuleSet(returnSet);
    //
    // return returnSet;
    // }
    //
    // /**
    // * Calculate expected value for objectiveFunction given error margin and a rule set
    // * @param rules Rule set to use for estimation calculation
    // * @param errorMargin Treshold that is used as stopping criterion
    // * @param objectiveFunction Objective function to use for value calculation
    // * @return Expected value
    // */
    // double getExpectedValue(ClusRuleSet rules, double errorMargin, Function<ClusRuleSet, Double> objectiveFunction)
    // {
    // double sum = 0, tmp = 0, mean = 0;
    // double error = Double.MAX_VALUE;
    // int counter = 0;
    // ClusRuleSet tmpSet;
    // ArrayList<Double> items = new ArrayList<Double>();
    //
    // // estimate error within errorMargin
    // while(error > errorMargin || counter < 2)
    // {
    // counter++;
    //
    // // sample random from rules
    // tmpSet = randomSampleWithoutBias(rules);
    //
    // // get objective function value
    // tmp = objectiveFunction.apply(tmpSet);
    // sum += tmp;
    // items.add(tmp);
    //
    // // calculate mean and SD
    // if (counter >= 2)
    // {
    // mean = sum/counter;
    // tmp = 0;
    // for (double d : items)
    // {
    // tmp += Math.pow((mean-d), 2);
    // }
    // tmp = (1/(counter-1)) * tmp;
    // tmp = Math.sqrt(tmp);
    //
    // error = tmp/Math.sqrt(counter);
    // }
    // }
    //
    // return mean;
    // }

    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
        throw new RuntimeException("induceSingleUnpruned()");
    }
}

/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.ext.ensemble;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.algo.tdidt.DepthFirstInduce;
import si.ijs.kt.clus.algo.tdidt.DepthFirstInduceSparse;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.ext.ensemble.callable.InduceExtraTreeCallable;
import si.ijs.kt.clus.ext.ensemble.callable.InduceOneBagCallable;
import si.ijs.kt.clus.ext.ensemble.container.OneBagResults;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROS;
import si.ijs.kt.clus.ext.featureRanking.ClusEnsembleFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRanking;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusOutput;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsEnsemble;
import si.ijs.kt.clus.main.settings.SettingsGeneral;
import si.ijs.kt.clus.main.settings.SettingsMLC;
import si.ijs.kt.clus.main.settings.SettingsOutput;
import si.ijs.kt.clus.main.settings.SettingsRules;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.io.ClusModelCollectionIO;
import si.ijs.kt.clus.selection.BaggingSelection;
import si.ijs.kt.clus.selection.BaggingSelectionSemiSupervised;
import si.ijs.kt.clus.selection.OOBSelection;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.ComponentStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.ClusRandomNonstatic;
import si.ijs.kt.clus.util.cloner.Cloner;
import si.ijs.kt.clus.util.jeans.resource.ResourceInfo;
import si.ijs.kt.clus.util.tools.optimization.GDProbl;


public class ClusEnsembleInduce extends ClusInductionAlgorithm {

    Clus m_BagClus;
    static ClusAttrType[] m_RandomSubspaces; // TODO: this field should be removed in the future
    //ClusForest m_OForest;// Forest with the original models
    ClusForest[] m_OForests;

    //ClusForest m_DForest;
    static int m_Mode;

    // Memory optimization
    private static boolean m_OptMode;
    //private ClusEnsembleInduceOptimization m_Optimization;
    private ClusEnsembleInduceOptimization[] m_Optimizations;

    // Output ensemble at different values
    int[] m_OutEnsembleAt;// sorted values (ascending)!
    static int m_NbMaxBags;

    // members for ROS ensembles (target subspacing)
    static int m_EnsembleROSScope;
    ClusEnsembleROSInfo m_EnsembleROSInfo = null;

    // Out-Of-Bag Error Estimate
    ClusOOBErrorEstimate m_OOBEstimation;

    // Feature Ranking via Random Forests OR via Genie3 etc.
    boolean m_FeatRank;
    // ClusEnsembleFeatureRanking m_FeatureRanking;
    ClusEnsembleFeatureRanking[] m_FeatureRankings;

    /** Number of the threads when growing the trees. */
    private int m_NbThreads;

    /**
     * Random tree depths for different iterations, used for tree to rules optimization procedures.
     * This is static because we want different tree depths for different folds.
     */
    // static protected Random m_randTreeDepth = new Random(0);

    public static final int m_PARALLEL_TRAP_BestFirst_getDescriptiveAttributes = 0;
    public static final int m_PARALLEL_TRAP_DepthFirst_getDescriptiveAttributes = 1;
    public static final int m_PARALLEL_TRAP_staticRandom = 2;
    public static final int m_PARALLEL_TRAP_optimization = 3;
    private static boolean[] m_WarningsGiven = new boolean[4];


    public ClusEnsembleInduce(ClusSchema schema, Settings sett, Clus clus) throws ClusException, IOException {
        super(schema, sett);
        initialize(schema, sett, clus);
        sett.getEnsemble().updateNbRandomAttrSelected(schema);// moved here because of HSC bug
    }


    // used by FIRE
    public ClusEnsembleInduce(ClusInductionAlgorithm other, Clus clus) throws ClusException, IOException {
        super(other);
        initialize(getSchema(), getSettings(), clus);
        getSettings().getEnsemble().updateNbRandomAttrSelected(getSchema());// moved here because of HSC bug
    }


    public void initialize(ClusSchema schema, Settings settMain, Clus clus) throws ClusException, IOException {
        m_BagClus = clus;
        m_Mode = getStatManager().getMode();
        // optimize if not XVAL and HMC

        SettingsEnsemble sett = settMain.getEnsemble();

        m_OptMode = (sett.shouldOptimizeEnsemble() && ((m_Mode == ClusStatManager.MODE_HIERARCHICAL) || (m_Mode == ClusStatManager.MODE_REGRESSION) || (m_Mode == ClusStatManager.MODE_CLASSIFY)));
        m_EnsembleROSScope = sett.getEnsembleROSScope();

        // m_OptMode = (Settings.shouldOptimizeEnsemble() && !Settings.IS_XVAL && ((m_Mode ==
        // ClusStatManager.MODE_HIERARCHICAL)||(m_Mode == ClusStatManager.MODE_REGRESSION) || (m_Mode ==
        // ClusStatManager.MODE_CLASSIFY)));
        m_OutEnsembleAt = sett.getNbBaggingSets().getIntVectorSorted();
        m_NbMaxBags = getNbTrees(m_OutEnsembleAt.length - 1);
        m_FeatRank = sett.shouldPerformRanking() && !settMain.getExperimental().IS_XVAL;
        if (m_FeatRank && !sett.shouldEstimateOOB() && sett.getRankingMethod() == SettingsEnsemble.RANKING_RFOREST) {
            System.err.println("For Feature Ranking RForest, OOB estimate of error should also be performed.");
            System.err.println("OOB Error Estimate is set to true.");

            sett.setOOBestimate(true);
        }
        if (sett.shouldEstimateOOB())
            m_OOBEstimation = new ClusOOBErrorEstimate(m_Mode, settMain);
        if (m_FeatRank) {
            if (m_BagClus.getSettings().getMLC().getSectionMultiLabel().isEnabled()) {
                int[] rankingMeasures = m_BagClus.getSettings().getMLC().getMultiLabelRankingMeasures();
                if (rankingMeasures.length == 1 && rankingMeasures[0] == SettingsMLC.MULTILABEL_MEASURES_ALL) {
                    m_BagClus.getSettings().getMLC().setToAllMultiLabelRankingMeasures();
                }
            }

            // m_FeatureRanking = new ClusFeatureRanking();

            // setNbFeatureRankings(schema, clus.getStatManager());
            //int nbRankings = getNbFeatureRankings();
            //if(getSettings().getVerbose() > 0){
            //	System.out.println("Number of feature rankings computed: " + nbRankings);
            //}
            // m_FeatureRanking.initializeAttributes(schema.getDescriptiveAttributes(), nbRankings);
            // if (sett.getEnsembleMethod() == Settings.ENSEMBLE_EXTRA_TREES){
            // // moj komentar //Dragi comment - take 2
            // if (sett.getRankingMethod() != Settings.RANKING_GENIE3 && sett.getRankingMethod() !=
            // Settings.RANKING_SYMBOLIC){
            // System.err.println("Feature ranking with Extra trees is enabled only with GENIE3 or SYMBOLIC ranking.
            // Setting to GENIE3.");
            // sett.setFeatureRankingMethod(Settings.RANKING_GENIE3);
            // }
            // }
        }
        setNumberOfThreads(sett.getNumberOfThreads());
    }


    /**
     * Sets the number of threads used when inducing an ensemble. If the wanted number of threads
     * ({@code Settings.m_NumberOfThreads}) equals {@code 0}, then all available processors are used.
     * 
     * @param nbThreads
     *        number of threads, greater or equal to {@code 0}.
     */
    public void setNumberOfThreads(int nbThreads) {
        if (nbThreads == 0) {
            m_NbThreads = Runtime.getRuntime().availableProcessors();
        }
        else {
            m_NbThreads = nbThreads;
        }
    }


    public void setNbFeatureRankings(ClusSchema schema, ClusStatManager mgr) {
        for (int forest = 0; forest < m_FeatureRankings.length; forest++) {
            int nbTrees = getNbTrees(forest);
            setNbFeatureRankings(m_FeatureRankings[forest], schema, mgr, nbTrees);
        }

    }


    /**
     * Sets the number of feature rankings and the ranking description.
     * 
     * @param franking
     * @param schema
     * @param mgr
     */
    public void setNbFeatureRankings(ClusEnsembleFeatureRanking franking, ClusSchema schema, ClusStatManager mgr, int nbTrees) {
        ClusStatistic clusteringStat = mgr.getStatistic(ClusAttrType.ATTR_USE_CLUSTERING);
        //Settings sett = schema.getSettings();
        SettingsEnsemble sett = schema.getSettings().getEnsemble();

        int nbRankings = 0;
        ClusAttrType[] clusteringAttrs = schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);

        if (clusteringAttrs.length < 2 && sett.shouldPerformRankingPerTarget()) {
            System.err.println("Situation:");
            System.err.println("- there is only one clustering attribute");
            System.err.println("- per target ranking is set to Yes");
            System.err.println("Consequences:");
            System.err.println("- per target ranking == overAll ranking");
            System.err.println("- per target ranking will be set to No");
            sett.setPerformRankingPerTarget(false);
        }
        ArrayList<String> rankingNames = new ArrayList<String>();
        switch (sett.getRankingMethod()) {
            case SettingsEnsemble.RANKING_RFOREST:
                ClusErrorList errLst = franking.computeErrorList(schema, mgr);
                int nbErrors = errLst.getNbErrors();
                for (int i = 0; i < nbErrors; i++) {
                    nbRankings++; // overall
                    rankingNames.add(String.format("%s:%s", errLst.getError(i).getName(), "overall"));

                    if (sett.shouldPerformRankingPerTarget() && (errLst.getError(i) instanceof ComponentError)) {
                        int nbDim = errLst.getError(i).getDimension();
                        nbRankings += nbDim;
                        for (int j = 0; j < nbDim; j++) {
                            rankingNames.add(String.format("%s:%s", errLst.getError(i).getName(), clusteringAttrs[j].getName()));
                        }
                    }
                }
                franking.setRForestFimpHeader(rankingNames);
                break;
            case SettingsEnsemble.RANKING_GENIE3:
                nbRankings++; // overall
                rankingNames.add(String.format("overAll"));
                if (sett.shouldPerformRankingPerTarget()) {
                    if (!(clusteringStat instanceof ComponentStatistic)) {
                        System.err.println("Cannot perform per-target ranking for the given type(s) of targets!");
                        System.err.println("This option is now set to false.");
                        sett.setPerformRankingPerTarget(false);
                    }
                    else {
                        int nbComponents = ((ComponentStatistic) clusteringStat).getNbStatisticComponents();
                        nbRankings += nbComponents;
                        for (int j = 0; j < nbComponents; j++) {
                            rankingNames.add(String.format("%s", clusteringAttrs[j].getName()));
                        }
                    }
                }
                franking.setGenie3FimpHeader(rankingNames);
                break;
            case SettingsEnsemble.RANKING_SYMBOLIC:
                double[] weights = sett.getSymbolicWeights();
                if (weights == null) {
                    weights = new double[] { sett.getSymbolicWeight() };
                }
                nbRankings = weights.length;
                franking.setSymbolicFimpHeader(weights);
                break;
        }
        franking.setNbFeatureRankings(nbRankings);
        int ensType = sett.getEnsembleMethod();
        int rankType = sett.getRankingMethod();
        franking.setEnsembleRankigDescription(ensType, rankType, nbTrees);
    }


    /**
     * Makes new ClusOOBErrorEstimate. This is sometimes needed because ClusOOBErrorEstimate has many static members
     */
    public void resetClusOOBErrorEstimate() {
        m_OOBEstimation = new ClusOOBErrorEstimate(m_Mode, getSettings());
    }


    /**
     * Train a decision tree ensemble with an algorithm given in settings
     * 
     * @throws ClusException,
     *         IOException, InterruptedException
     */
    public void induceAll(ClusRun cr) throws ClusException, IOException, InterruptedException {

        SettingsEnsemble set = getSettings().getEnsemble();
        SettingsGeneral setg = getSettings().getGeneral();

        System.out.println("Memory And Time Optimization = " + m_OptMode);
        System.out.println("Out-Of-Bag Estimate of the error = " + set.shouldEstimateOOB());
        System.out.println("Perform Feature Ranking = " + m_FeatRank);

        if (set.isEnsembleROSEnabled()) {
            m_EnsembleROSInfo = ClusROS.prepareROSEnsembleInfo(getSettings(), getSchema());
        }

        // initialise ranking stuff here: we need stat manager with clustering statistics != null
        //        m_FeatureRanking = new ClusEnsembleFeatureRanking();
        m_FeatureRankings = new ClusEnsembleFeatureRanking[m_OutEnsembleAt.length];
        for (int forest = 0; forest < m_FeatureRankings.length; forest++) {
            m_FeatureRankings[forest] = new ClusEnsembleFeatureRanking(getSettings());
        }

        ClusStatManager mgr = getStatManager();
        ClusSchema schema = mgr.getSchema();
        //        setNbFeatureRankings(m_FeatureRanking, schema, mgr);

        setNbFeatureRankings(schema, mgr);
        //        int nbRankings = m_FeatureRanking.getNbFeatureRankings();
        int nbRankings = m_FeatureRankings[0].getNbFeatureRankings();
        for (int forest = 1; forest < m_FeatureRankings.length; forest++) {
            if (m_FeatureRankings[forest].getNbFeatureRankings() != nbRankings) {
                System.err.println("The number of feature rankings should be the same for all forests! Exiting.");
                System.exit(-1);
            }
        }
        if (setg.getVerbose() > 0) {
            System.out.println("Number of feature rankings computed: " + nbRankings);
        }

        //        m_FeatureRanking.initializeAttributes(schema.getDescriptiveAttributes(), nbRankings);
        for (int forest = 0; forest < m_FeatureRankings.length; forest++) {
            m_FeatureRankings[forest].initializeAttributes(schema.getDescriptiveAttributes(), nbRankings);
        }

        //m_OForest = new ClusForest(getStatManager(), m_Optimization);
        //m_OForest.setEnsembleROSInfo(m_EnsembleROSInfo); // TODO: this should be removed; we use m_OForests from now on

        // TODO: do we need m_OForest and m_OForests ? this should just be m_OForests
        m_OForests = new ClusForest[m_OutEnsembleAt.length];
        m_Optimizations = new ClusEnsembleInduceOptimization[m_OForests.length];
        for (int i = 0; i < m_OForests.length; i++) {
            m_OForests[i] = new ClusForest(getStatManager(), m_Optimizations[i]);

            if (set.isEnsembleROSEnabled()) {
                m_OForests[i].setEnsembleROSInfo(m_EnsembleROSInfo.getTrimmedInfo(m_OutEnsembleAt[i]));
            }
        }

        //m_DForest = new ClusForest(getStatManager(), m_Optimization);
        TupleIterator train_iterator = null; // = train set iterator
        TupleIterator test_iterator = null; // = test set iterator      

        if (m_OptMode) {
            train_iterator = cr.getTrainIter();
            if (cr.getTestIter() != null) {
                test_iterator = cr.getTestSet().getIterator();
            }
            //m_Optimization = initializeOptimization(train_iterator, test_iterator);
            //m_Optimization.initPredictions(m_OForest.getStat(), m_EnsembleROSInfo); // TODO: this should be removed!
            //m_OForest.setOptimization(m_Optimization);

            for (int i = 0; i < m_Optimizations.length; i++) {
                m_Optimizations[i] = initializeOptimization(train_iterator, test_iterator);
                m_Optimizations[i].initPredictions(m_OForests[i].getStat(), m_EnsembleROSInfo.getTrimmedInfo(m_OutEnsembleAt[i]));
                m_OForests[i].setOptimization(m_Optimizations[i]);
            }

            //m_DForest.setOptimization(m_Optimizations);
        }

        switch (set.getEnsembleMethod()) {
            case SettingsEnsemble.ENSEMBLE_BAGGING: { // Bagging
                System.out.println("Ensemble Method: Bagging");
                induceBagging(cr);
                break;
            }
            case SettingsEnsemble.ENSEMBLE_RFOREST: { // RForest
                System.out.println("Ensemble Method: Random Forest");
                induceBagging(cr);
                break;
            }
            case SettingsEnsemble.ENSEMBLE_RSUBSPACES: { // RSubspaces
                System.out.println("Ensemble Method: Random Subspaces");
                induceSubspaces(cr);
                break;
            }
            case SettingsEnsemble.ENSEMBLE_BAGSUBSPACES: { // Bagging Subspaces
                System.out.println("Ensemble Method: Bagging of Subspaces");
                induceBaggingSubspaces(cr);
                break;
            }
            case SettingsEnsemble.ENSEMBLE_RFOREST_NO_BOOTSTRAP: { // RForest without bootstrapping (setting: RFeatSelection)
                System.out.println("Ensemble Method: Random Forest without bootstrapping");
                induceRForestNoBootstrap(cr); // TODO: martinb has a problem with the name of this method
                break;
            }
            case SettingsEnsemble.ENSEMBLE_PERT: { // PERT in combination with bagging
                System.out.println("Ensemble Method: PERT (in combination with Bagging)");
                induceBagging(cr);
                break;
            }
            case SettingsEnsemble.ENSEMBLE_EXTRA_TREES: { // Extra-Trees ensemble (published by Geurts et al.)
                System.out.println("Ensemble Method: Extra-trees");
                induceExtraTrees(cr);
                break;
            }
        }
        if (m_FeatRank) {
            //            m_FeatureRanking.createFimp(cr);
            for (int forest = 0; forest < m_FeatureRankings.length; forest++) {
                m_FeatureRankings[forest].createFimp(cr, String.format("Trees%d", m_OForests[forest].getNbModels()), getNbTrees(forest));
            }
        }
        if (m_OptMode) {
            //m_Optimization.roundPredictions();
            for (int i = 0; i < m_Optimizations.length; i++) {
                m_Optimizations[i].roundPredictions();
            }
        }

        postProcessForest(cr);

        /*
         * added July 2014, Jurica Levatiæ JSI
         * output hierarchy.txt file (for HMC)
         * disabled this at DepthFirstInduce if ensemble mode is on, otherwise
         * this, potentially huge, file is re-written for every tree
         */
        ClusStatistic stat = cr.getStatManager().createClusteringStat();
        ((RowData) cr.getTrainingSet()).calcTotalStatBitVector(stat);
        stat.showRootInfo(); // clus.ext.hierarchical.WHTDStatistic will write hierarchy.txt here
        /* end added by Jurica */

        // This section is for calculation of the similarity in the ensemble
        // ClusBeamSimilarityOutput bsimout = new ClusBeamSimilarityOutput(cr.getStatManager().getSettings());
        // bsimout.appendToFile(m_OForest.getModels(), cr);
    }


    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException {
        ClusRun myRun = new ClusRun(cr);
        induceAll(myRun);
        ClusModelInfo info = myRun.getModelInfo(ClusModel.ORIGINAL);
        return info.getModel();
    }


    // this ensemble method builds random forests (i.e. chooses the best test from a subset of attributes at each node),
    // but does not construct bootstrap replicates of the dataset
    public void induceRForestNoBootstrap(ClusRun cr) throws ClusException, IOException {
        long summ_time = 0; // = ResourceInfo.getTime();

        TupleIterator train_iterator = m_OptMode ? cr.getTrainIter() : null; // = train set iterator
        TupleIterator test_iterator = m_OptMode ? cr.getTestIter() : null; // = test set iterator

        Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
        int[] seeds = new int[m_NbMaxBags];
        for (int i = 0; i < m_NbMaxBags; i++) {
            seeds[i] = bagSeedGenerator.nextInt();
        }
        for (int i = 1; i <= m_NbMaxBags; i++) {
            long one_bag_time = ResourceInfo.getTime();
            if (getSettings().getGeneral().getVerbose() > 0)
                System.out.println("Bag: " + i);

            ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);

            ClusRun crSingle = new ClusRun(cr.getTrainingSet(), cr.getSummary());
            DepthFirstInduce ind;
            if (getSchema().isSparse()) {
                ind = new DepthFirstInduceSparse(this);
            }
            else {
                ind = new DepthFirstInduce(this);
            }
            ind.initialize();
            crSingle.getStatManager().initClusteringWeights();

            ClusModel model = ind.induceSingleUnpruned(crSingle, rnd);
            summ_time += ResourceInfo.getTime() - one_bag_time;

            //m_OForest.updateCounts((ClusNode) model);
            if (m_OptMode) {
                //m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);

                updatePredictionsForTuples(model, train_iterator, test_iterator, i);
            }
            else {
                //m_OForest.addModelToForest(model);

                addModelToForests(model, i);

                // ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
                // m_DForest.addModelToForest(defmod);
            }

            // Valid only when test set is supplied
            if (m_OptMode && (i != m_NbMaxBags) && checkToOutEnsemble(i)) {

                //                crSingle.setInductionTime(summ_time);
                //                postProcessForest(crSingle);
                //                crSingle.setTestSet(cr.getTestIter());
                //                crSingle.setTrainingSet(cr.getTrainingSet());
                //                outputBetweenForest(crSingle, m_BagClus, "_" + i + "_");
            }
            crSingle.deleteData();
            crSingle.setModels(new ArrayList<ClusModelInfo>());
        }
    }


    public void induceSubspaces(ClusRun cr) throws ClusException, IOException {
        //long summ_time = 0; // = ResourceInfo.getTime();
        TupleIterator train_iterator = m_OptMode ? cr.getTrainIter() : null; // = train set iterator
        TupleIterator test_iterator = m_OptMode ? cr.getTestIter() : null; // = test set iterator

        //m_OForest.setEnsembleROSInfo(m_EnsembleROSInfos[m_OForests.length]); // TODO: this should be removed
        for (int i = 0; i < m_OForests.length; i++) {
            m_OForests[i].setEnsembleROSInfo(m_EnsembleROSInfo.getTrimmedInfo(m_OutEnsembleAt[i]));
        }

        Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
        int[] seeds = new int[m_NbMaxBags];
        for (int i = 0; i < m_NbMaxBags; i++) {
            seeds[i] = bagSeedGenerator.nextInt();
        }
        for (int i = 1; i <= m_NbMaxBags; i++) {
            long one_bag_time = ResourceInfo.getTime();
            if (getSettings().getGeneral().getVerbose() > 0)
                System.out.println("Bag: " + i);

            ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);

            ClusRun crSingle = new ClusRun(cr.getTrainingSet(), cr.getSummary());
            // parallelisation !!!
            ClusEnsembleInduce.setRandomSubspaces(selectRandomSubspaces(cr.getStatManager().getSchema().getDescriptiveAttributes(), cr.getStatManager().getSettings().getEnsemble().getNbRandomAttrSelected(), ClusRandomNonstatic.RANDOM_SELECTION, rnd));
            DepthFirstInduce ind;
            if (getSchema().isSparse()) {
                ind = new DepthFirstInduceSparse(this);
            }
            else {
                ind = new DepthFirstInduce(this);
            }
            ind.initialize();
            crSingle.getStatManager().initClusteringWeights();

            initializeROS(crSingle.getStatManager(), i); // this needs to be changed for parallel implementation

            ClusModel model = ind.induceSingleUnpruned(crSingle, rnd);
            // summ_time += ResourceInfo.getTime() - one_bag_time;

            //m_OForest.updateCounts((ClusNode) model);
            if (m_OptMode) {
                //m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);
                updatePredictionsForTuples(model, train_iterator, test_iterator, i);
            }
            else {
                addModelToForests(model, i);

                //m_OForest.addModelToForest(model);
                // ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
                // m_DForest.addModelToForest(defmod);
            }
            // Valid only when test set is supplied
            if (m_OptMode && (i != m_NbMaxBags) && checkToOutEnsemble(i)) {
                //                crSingle.setInductionTime(summ_time);
                //                postProcessForest(crSingle);
                //                crSingle.setTestSet(cr.getTestIter());
                //                crSingle.setTrainingSet(cr.getTrainingSet());
                //                outputBetweenForest(crSingle, m_BagClus, "_" + i + "_");
            }
            crSingle.deleteData();
            crSingle.setModels(new ArrayList<ClusModelInfo>());
        }
    }


    public void induceBagging(ClusRun cr) throws ClusException, IOException, InterruptedException {
        int nbrows = cr.getTrainingSet().getNbRows();
        ((RowData) cr.getTrainingSet()).addIndices(); // necessary to be able to print paths
        if ((RowData) cr.getTestSet() != null) {
            ((RowData) cr.getTestSet()).addIndices();
        }

        TupleIterator train_iterator = m_OptMode ? cr.getTrainIter() : null; // = train set iterator
        TupleIterator test_iterator = m_OptMode ? cr.getTestIter() : null; // = test set iterator

        OOBSelection oob_total = null; // = total OOB selection
        OOBSelection oob_sel = null; // = current OOB selection

        SettingsEnsemble sett = getSettings().getEnsemble();

        //- Added by Jurica L. (IJS)
        //- check if dataset contains unlabeled examples, then use different sampling, labeled and unlabled examples are sampled separately
        int nbUnlabeled = 0;
        nbUnlabeled = ((RowData) cr.getTrainingSet()).getNbUnlabeled();
        if (nbUnlabeled > 0) {
            if (nbUnlabeled > 0) { //sort datasets, so that labeled examples come first and unlabeled second, needed for semi-supervised bagging selection
                ((RowData) cr.getTrainingSet()).sortLabeledFirst();
            }
        }
        // - end added by Jurica

        //        for (int i = 0; i < m_OForests.length; i++) {
        //            m_OForests[i].setEnsembleROSInfo(m_EnsembleROSInfo.getTrimmedInfo(m_OutEnsembleAt[i]));
        //        }

        // We store the old maxDepth to this if needed. Thus we get the right depth to .out files etc.
        int origMaxDepth = -1;
        if (sett.isEnsembleRandomDepth()) {
            // Random depth for the ensembles
            // The original Max depth is used as the average
            origMaxDepth = getSettings().getConstraints().getTreeMaxDepth();
        }
        BaggingSelection msel = null;
        int[] bagSelections = sett.getBagSelection().getIntVectorSorted();
        // bagSelections is either -1, 0, a value in [1,Iterations], or 2 values in [1,Iterations]

        Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
        int[] seeds = new int[m_NbMaxBags];
        for (int i = 0; i < m_NbMaxBags; i++) {
            seeds[i] = bagSeedGenerator.nextInt();
        }

        Cloner cloner = new Cloner();
        ClusStatManager[] statManagerClones = new ClusStatManager[m_NbMaxBags];
        for (int i = 0; i < statManagerClones.length; i++) {
            statManagerClones[i] = cloner.deepClone(cr.getStatManager()); // must be cloned here
        }

        ExecutorService executor = Executors.newFixedThreadPool(m_NbThreads);
        ArrayList<Future<OneBagResults>> bagResults = new ArrayList<Future<OneBagResults>>();

        if (bagSelections[0] == -1) {
            // normal bagging procedure
            for (int i = 1; i <= m_NbMaxBags; i++) {
                ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);

                if (nbUnlabeled > 0) {
                    msel = new BaggingSelectionSemiSupervised(nbrows, cr.getTrainingSet().getNbRows() - nbUnlabeled, nbUnlabeled, rnd);
                }
                else {
                    msel = new BaggingSelection(nbrows, sett.getEnsembleBagSize(), rnd);
                }

                if (sett.shouldEstimateOOB()) { // OOB estimate is on

                    // TODO: synchronisation
                    oob_sel = new OOBSelection(msel);
                    if (i == 1) { // initialization
                        oob_total = new OOBSelection(msel);
                    }
                    else {
                        oob_total.addToThis(oob_sel);
                    }
                }
                OOBSelection current_oob_total = cloner.deepClone(oob_total);
                // induceOneBag(cr, i, origMaxDepth, oob_sel, oob_total, train_iterator, test_iterator, msel, rnd);
                InduceOneBagCallable worker = new InduceOneBagCallable(this, cr, i, origMaxDepth, oob_sel, current_oob_total, train_iterator, test_iterator, msel, rnd, statManagerClones[i - 1]);
                Future<OneBagResults> submit = executor.submit(worker);
                bagResults.add(submit);
            }
        }
        else if (bagSelections[0] == 0) {
            // we assume that files _bagI.model exist, for I=1..m_NbMaxBags and build the forest from them
            makeForestFromBags(cr, train_iterator, test_iterator);
        }
        else {
            // only one or a range of bags needs to be computed (and the model output) e.g. because we want to run the
            // forest in parallel,
            // or because we want to add a number of trees to an existing forest.
            for (int i = 1; i < bagSelections[0]; i++) {
                // we eventually want the same bags as when computing them sequentially.
                msel = new BaggingSelection(nbrows, sett.getEnsembleBagSize(), null); // not really necessary any more
            }
            for (int i = bagSelections[0]; i <= bagSelections[1]; i++) {
                ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);

                if (nbUnlabeled > 0) {
                    msel = new BaggingSelectionSemiSupervised(nbrows, cr.getTrainingSet().getNbRows() - nbUnlabeled, nbUnlabeled, rnd);
                }
                else {
                    msel = new BaggingSelection(nbrows, sett.getEnsembleBagSize(), rnd);
                }

                if (sett.shouldEstimateOOB()) { // OOB estimate is on
                    oob_sel = new OOBSelection(msel);
                }
                // induceOneBag(cr, i, origMaxDepth, oob_sel, oob_total, train_iterator, test_iterator, msel, rnd);
                InduceOneBagCallable worker = new InduceOneBagCallable(this, cr, i, origMaxDepth, oob_sel, oob_total, train_iterator, test_iterator, msel, rnd, cloner.deepClone(cr.getStatManager()));
                Future<OneBagResults> submit = executor.submit(worker);
                bagResults.add(submit);
            }
        }

        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

        for (int i = 1; i <= bagResults.size(); i++) { // must start with 1 and end with the size
            Future<OneBagResults> future = bagResults.get(i - 1);
            try {
                OneBagResults results = future.get();
                if (!m_OptMode) {
                    addModelToForests(results.getModel(), i);
                }
                if (sett.shouldPerformRanking()) {
                    updateFeatureRankings(i, results.getFimportances());
                }

                ClusRun crSingle = results.getSingleRun();
                // Lines that are commented out in this if-section are already executed in induceOneBag
                if (checkToOutEnsemble(i) && (sett.getBagSelection().getIntVectorSorted()[0] == -1)) {
                    // crSingle.setInductionTime(m_SummTime);

                    postProcessForest(crSingle);
                    if (sett.shouldEstimateOOB()) {
                        if (i == m_NbMaxBags) {
                            m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "");
                        }
                        else {
                            m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "_" + i + "_");
                        }
                    }

                    // if (m_OptMode && (i != m_NbMaxBags)){
                    // crSingle.setTestSet(cr.getTestIter());
                    // crSingle.setTrainingSet(cr.getTrainingSet());
                    // outputBetweenForest(crSingle, m_BagClus, "_"+ i +"_");
                    // }
                }

            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            catch (ExecutionException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        // Restore the old maxDepth
        if (origMaxDepth != -1) {
            getSettings().getConstraints().setTreeMaxDepth(origMaxDepth);
        }

    }


    void initializeROS(ClusStatManager mgr, int bagNo) throws ClusException {
        if (mgr.getSettings().getEnsemble().isEnsembleROSEnabled()) {

            // get heuristic in use
            ClusHeuristic h = mgr.getHeuristic();

            // check if the attribute weights are set
            if (h.getClusteringAttributeWeights() == null)
                throw new RuntimeException("Heuristic does not support ROS!");

            // get subspace of targets that we have previously prepared
            boolean[] enabled = m_EnsembleROSInfo.getModelSubspaceBoolean(bagNo - 1);

            // push to already initialized heuristic
            h.setClusteringWeightsEnabledAttributes(enabled);

            // display info about used targets
            if (getSettings().getGeneral().getVerbose() > 0) {
                int[] trgts = m_EnsembleROSInfo.getOnlyTargets(m_EnsembleROSInfo.getModelSubspace(bagNo - 1));

                if (trgts.length > 15) {
                    System.err.println("Enabled targets: " + ClusEnsembleROSInfo.getEnabledCount(trgts) + " of " + trgts.length);
                }
                else {
                    System.err.println("Enabled targets: " + Arrays.toString(trgts));
                }
            }
        }
    }


    public OneBagResults induceOneBag(ClusRun cr, int i, int origMaxDepth, OOBSelection oob_sel, OOBSelection oob_total, TupleIterator train_iterator, TupleIterator test_iterator, BaggingSelection msel, ClusRandomNonstatic rnd, ClusStatManager mgr) throws ClusException, IOException, InterruptedException {
        long one_bag_time = ResourceInfo.getTime();
        SettingsEnsemble sett = cr.getStatManager().getSettings().getEnsemble();
        SettingsGeneral setg = cr.getStatManager().getSettings().getGeneral();

        if (setg.getVerbose() > 0)
            System.out.println("Bag: " + i);

        if (sett.isEnsembleRandomDepth()) {
            // Set random tree max depth
            getSettings().getConstraints().setTreeMaxDepth(GDProbl.randDepthWighExponentialDistribution(
                    // m_randTreeDepth.nextDouble(),
                    rnd.nextDouble(ClusRandomNonstatic.RANDOM_INT_RANFOR_TREE_DEPTH), origMaxDepth));
        }

        ClusRun crSingle = m_BagClus.partitionDataBasic(cr.getTrainingSet(), msel, cr.getSummary(), i);

        DepthFirstInduce ind;

        // ordinary bagging run
        if (getSchema().isSparse()) {
            ind = new DepthFirstInduceSparse(this, mgr, true);
        }
        else {
            ind = new DepthFirstInduce(this, mgr, true);
        }

        ind.initialize();

        crSingle.getStatManager().initClusteringWeights(); // TODO: when using rules: if (checkToOutEnsemble(i) && (getSettings().getBagSelection().getIntVectorSorted()[0] == -1)) ----> postprocess... To we need more than one stat manager?
        ind.getStatManager().initClusteringWeights(); // equivalent to mgr.initClusteringWeights();

        initializeROS(ind.getStatManager(), i);

        ClusModel model = ind.induceSingleUnpruned(crSingle, rnd);
        //m_SummTime += ResourceInfo.getTime() - one_bag_time;
        one_bag_time = ResourceInfo.getTime() - one_bag_time;

        //        int[] additionalModelsNodesLeaves = m_OForest.updateCounts((ClusNode) model);
        //        for(int ii = m_OForests.length - 1; ii >= 0; ii--){
        //        	if (getNbTrees(ii) >= i){
        //        		m_OForests[ii].updateCounts(additionalModelsNodesLeaves[0], additionalModelsNodesLeaves[1], additionalModelsNodesLeaves[2]);
        //        	} else{
        //        		break;        		
        //        	}
        //        }
        updateCounts((ClusNode) model, i);

        // OOB estimate for the parallel implementation is done in makeForestFromBags method <--- matejp: This is some
        // old parallelisation
        if (sett.shouldEstimateOOB() && (sett.getBagSelection().getIntVectorSorted()[0] == -1)) {
            m_OOBEstimation.updateOOBTuples(oob_sel, (RowData) cr.getTrainingSet(), model, i);
        }

        HashMap<String, double[][]> fimportances = new HashMap<String, double[][]>();
        if (m_FeatRank) {// franking
            // this suffices even in the case when we grow more than one forest 
            //if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_RFOREST) {
            if (sett.getRankingMethod() == SettingsEnsemble.RANKING_RFOREST) {
                fimportances = m_FeatureRankings[0].calculateRFimportance(model, cr, oob_sel, rnd, ind.getStatManager());
            }
            //else if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_GENIE3) {
            else if (sett.getRankingMethod() == SettingsEnsemble.RANKING_GENIE3) {
                // m_FeatureRanking.calculateGENIE3importance((ClusNode)model, cr);
                fimportances = m_FeatureRankings[0].calculateGENIE3importanceIteratively((ClusNode) model, ind.getStatManager());
            }
            //else if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_SYMBOLIC) {
            else if (sett.getRankingMethod() == SettingsEnsemble.RANKING_SYMBOLIC) {
                double[] weights = sett.getSymbolicWeights(); // m_BagClus.getSettings().getSymbolicWeights();
                if (weights == null) {
                    weights = new double[] { sett.getSymbolicWeight() }; //m_BagClus.getSettings().getSymbolicWeight() };
                }
                // m_FeatureRanking.calculateSYMBOLICimportance((ClusNode)model, weights, 0);
                fimportances = m_FeatureRankings[0].calculateSYMBOLICimportanceIteratively((ClusNode) model, weights);
            }
        }
        boolean canForgetTheRun = true;
        if (m_OptMode) {
            //        	m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);
            updatePredictionsForTuples(model, train_iterator, test_iterator, i);
            model = null;
        }
        // instead of adding the model here, we will do this in after all bags are completed to keep the order of the
        // models invariant
        // else{
        // m_OForest.addModelToForest(model);

        // this was commented out before parallelism ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
        // this was commented out before parallelism m_DForest.addModelToForest(defmod);
        // }

        // printing paths taken by each example in each tree (used in ICDM'11 paper on "Random forest based feature
        // induction")
        if (sett.isPrintEnsemblePaths()) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("tree_" + i + ".path")));
            ((ClusNode) model).numberCompleteTree();
            ((ClusNode) model).printPaths(pw, "", "", (RowData) cr.getTrainingSet(), oob_sel, false);
            if ((RowData) cr.getTestSet() != null) {
                ((ClusNode) model).printPaths(pw, "", "", (RowData) cr.getTestSet(), null, true);
            }
        }

        // Valid only when test set is supplied
        if (checkToOutEnsemble(i) && (sett.getBagSelection().getIntVectorSorted()[0] == -1)) {
            //crSingle.setInductionTime(m_SummTime);
            crSingle.setInductionTime(one_bag_time);
            canForgetTheRun = false;

            // Lines that are commented out are executed AT THE END of induceBagging

            // postProcessForest(crSingle);
            // if (Settings.shouldEstimateOOB()){
            // if (i == m_NbMaxBags){
            // m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData)cr.getTrainingSet(),
            // m_BagClus, "");
            // } else{
            // m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData)cr.getTrainingSet(),
            // m_BagClus, "_"+ i +"_");
            // }
            // }
            //
            if (m_OptMode && (i != m_NbMaxBags)) {
                if (m_NbThreads > 1) {
                    giveParallelisationWarning(ClusEnsembleInduce.m_PARALLEL_TRAP_optimization);
                }
                //                crSingle.setTestSet(cr.getTestIter());
                //                crSingle.setTrainingSet(cr.getTrainingSet());
                //                outputBetweenForest(crSingle, m_BagClus, "_" + i + "_");
            }
        }

        if ((sett.getBagSelection().getIntVectorSorted()[0] != -1) || (sett.isPrintEnsembleModelFiles())) {
            ClusModelCollectionIO io = new ClusModelCollectionIO();
            ClusModelInfo orig_info = crSingle.addModelInfo("Original");
            orig_info.setModel(model);
            m_BagClus.saveModels(crSingle, io);
            io.save(m_BagClus.getSettings().getGeneric().getFileAbsolute(cr.getStatManager().getSettings().getGeneric().getAppName() + "_bag" + i + ".model"));
        }

        if (canForgetTheRun) {
            crSingle.deleteData();
            crSingle.setModels(new ArrayList<ClusModelInfo>());
        }

        return new OneBagResults(model, fimportances, crSingle, oob_total, one_bag_time);
    }


    public void makeForestFromBags(ClusRun cr, TupleIterator train_iterator, TupleIterator test_iterator) throws ClusException, IOException {
        try {
            OOBSelection oob_total = null; // = total OOB selection
            OOBSelection oob_sel = null; // = current OOB selection
            BaggingSelection msel = null;
            System.out.println("Start loading models");

            Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
            int[] seeds = new int[m_NbMaxBags];
            for (int i = 0; i < m_NbMaxBags; i++) {
                seeds[i] = bagSeedGenerator.nextInt();
            }
            for (int i = 1; i <= m_NbMaxBags; i++) {
                System.out.println("Loading model for bag " + i);
                ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);

                ClusModelCollectionIO io = ClusModelCollectionIO.load(m_BagClus.getSettings().getGeneric().getFileAbsolute(getSettings().getGeneric().getAppName() + "_bag" + i + ".model"));
                ClusModel orig_bag_model = io.getModel("Original");
                if (orig_bag_model == null) { throw new ClusException(cr.getStatManager().getSettings().getGeneric().getAppName() + "_bag" + i + ".model file does not contain model named 'Original'"); }

                //m_OForest.updateCounts((ClusNode) orig_bag_model);
                updateCounts((ClusNode) orig_bag_model, i);

                if (m_OptMode) {
                    //m_Optimization.updatePredictionsForTuples(orig_bag_model, train_iterator, test_iterator);
                    updatePredictionsForTuples(orig_bag_model, train_iterator, test_iterator, i);
                }
                else {
                    //m_OForest.addModelToForest(orig_bag_model);
                    addModelToForests(orig_bag_model, i);
                }
                if (getSettings().getEnsemble().shouldEstimateOOB()) { // OOB estimate is on
                    // the same bags will be generated for the corresponding models!!!
                    msel = new BaggingSelection(cr.getTrainingSet().getNbRows(), getSettings().getEnsemble().getEnsembleBagSize(), rnd);
                    oob_sel = new OOBSelection(msel);
                    if (i == 1) { // initialization
                        oob_total = new OOBSelection(msel);
                    }
                    else
                        oob_total.addToThis(oob_sel);
                    m_OOBEstimation.updateOOBTuples(oob_sel, (RowData) cr.getTrainingSet(), orig_bag_model, i);
                }

                if (checkToOutEnsemble(i)) {
                    postProcessForest(cr);
                    if (getSettings().getEnsemble().shouldEstimateOOB()) {
                        if (i == m_NbMaxBags)
                            m_OOBEstimation.postProcessForestForOOBEstimate(cr, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "");
                        else
                            m_OOBEstimation.postProcessForestForOOBEstimate(cr, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "_" + i + "_");
                    }
                    if (m_OptMode && i != m_NbMaxBags) {
                        //                        outputBetweenForest(cr, m_BagClus, "_" + i + "_");
                    }
                }
                cr.setModels(new ArrayList<ClusModelInfo>());// do not store the models

                // Dragi, IJS - we don't store the predictions of the default models
                /*
                 * ClusModel def_bag_model = io.getModel("Default");
                 * if (def_bag_model == null) {
                 * throw new ClusException(cr.getStatManager().getSettings().getAppName() + "_bag" + i +
                 * ".model file does not contain model named 'Default'");
                 * }
                 * m_DForest.addModelToForest(def_bag_model);
                 */
            }
        }
        catch (ClassNotFoundException e) {
            throw new ClusException("Error: not all of the _bagX.model files were found");
        }
    }


    public void induceBaggingSubspaces(ClusRun cr) throws ClusException, IOException {
        int nbrows = cr.getTrainingSet().getNbRows();

        long summ_time = 0; // = ResourceInfo.getTime();

        TupleIterator train_iterator = m_OptMode ? cr.getTrainIter() : null; // = train set iterator
        TupleIterator test_iterator = m_OptMode ? cr.getTestIter() : null; // = test set iterator

        OOBSelection oob_total = null; // = total OOB selection
        OOBSelection oob_sel = null; // = current OOB selection

        Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
        int[] seeds = new int[m_NbMaxBags];
        for (int i = 0; i < m_NbMaxBags; i++) {
            seeds[i] = bagSeedGenerator.nextInt();
        }
        for (int i = 1; i <= m_NbMaxBags; i++) {
            long one_bag_time = ResourceInfo.getTime();
            if (getSettings().getGeneral().getVerbose() > 0)
                System.out.println("Bag: " + i);

            ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);
            BaggingSelection msel = new BaggingSelection(nbrows, getSettings().getEnsemble().getEnsembleBagSize(), rnd);

            ClusRun crSingle = m_BagClus.partitionDataBasic(cr.getTrainingSet(), msel, cr.getSummary(), i);
            // parallelisation !!!
            ClusEnsembleInduce.setRandomSubspaces(selectRandomSubspaces(cr.getStatManager().getSchema().getDescriptiveAttributes(), cr.getStatManager().getSettings().getEnsemble().getNbRandomAttrSelected(), ClusRandomNonstatic.RANDOM_SELECTION, rnd));
            DepthFirstInduce ind;
            if (getSchema().isSparse()) {
                ind = new DepthFirstInduceSparse(this);
            }
            else {
                ind = new DepthFirstInduce(this);
            }
            ind.initialize();

            crSingle.getStatManager().initClusteringWeights();
            ind.initializeHeuristic();

            ClusModel model = ind.induceSingleUnpruned(crSingle, rnd);
            summ_time += ResourceInfo.getTime() - one_bag_time;

            //m_OForest.updateCounts((ClusNode) model);
            updateCounts((ClusNode) model, i);

            if (getSettings().getEnsemble().shouldEstimateOOB()) { // OOB estimate is on
                oob_sel = new OOBSelection(msel);
                if (i == 1)
                    oob_total = new OOBSelection(msel);
                else
                    oob_total.addToThis(oob_sel);
                m_OOBEstimation.updateOOBTuples(oob_sel, (RowData) cr.getTrainingSet(), model, i);
            }

            if (m_OptMode) {
                //m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);
                updatePredictionsForTuples(model, train_iterator, test_iterator, i);
            }
            else {
                //m_OForest.addModelToForest(model);
                addModelToForests(model, i);
                ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
                //  m_DForest.addModelToForest(defmod);
            }

            if (checkToOutEnsemble(i)) {
                crSingle.setInductionTime(summ_time);
                postProcessForest(crSingle);
                if (getSettings().getEnsemble().shouldEstimateOOB()) {
                    if (i == m_NbMaxBags)
                        m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "");
                    else
                        m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData) cr.getTrainingSet(), m_BagClus, "_" + i + "_");
                }
                if (m_OptMode && (i != m_NbMaxBags)) {
                    //                    crSingle.setTestSet(cr.getTestIter());
                    //                    crSingle.setTrainingSet(cr.getTrainingSet());
                    //                    outputBetweenForest(crSingle, m_BagClus, "_" + i + "_");
                }
            }
            crSingle.deleteData();
            crSingle.setModels(new ArrayList<ClusModelInfo>());
        }
    }


    public void induceExtraTrees(ClusRun cr) throws ClusException, IOException, InterruptedException {
        long indTimeSequential = 0;

        TupleIterator train_iterator = m_OptMode ? cr.getTrainIter() : null; // = train set iterator
        TupleIterator test_iterator = m_OptMode ? cr.getTestIter() : null; // = test set iterator

        Random bagSeedGenerator = new Random(getSettings().getGeneral().getRandomSeed());
        int[] seeds = new int[m_NbMaxBags];
        for (int i = 0; i < m_NbMaxBags; i++) {
            seeds[i] = bagSeedGenerator.nextInt();
        }

        Cloner cloner = new Cloner();
        ClusStatManager[] statManagerClones = new ClusStatManager[m_NbMaxBags];
        for (int i = 0; i < statManagerClones.length; i++) {
            statManagerClones[i] = cloner.deepClone(cr.getStatManager()); // must be cloned here
        }

        ExecutorService executor = Executors.newFixedThreadPool(m_NbThreads);
        ArrayList<Future<OneBagResults>> bagResults = new ArrayList<Future<OneBagResults>>();

        for (int i = 1; i <= m_NbMaxBags; i++) {
            ClusRandomNonstatic rnd = new ClusRandomNonstatic(seeds[i - 1]);
            InduceExtraTreeCallable worker = new InduceExtraTreeCallable(this, cr, i, train_iterator, test_iterator, rnd, statManagerClones[i - 1]); // <-- induceExtraTree(cr, i, train_iterator, test_iterator, rnd, summ_time, one_bag_time, statManagerClones[i - 1]);
            Future<OneBagResults> submit = executor.submit(worker);
            bagResults.add(submit);
        }

        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

        for (int i = 1; i <= bagResults.size(); i++) { // must start with 1 and end with the size
            Future<OneBagResults> future = bagResults.get(i - 1);
            try {
                OneBagResults results = future.get();

                if (!m_OptMode) {
                    addModelToForests(results.getModel(), i);
                }
                if (getSettings().getEnsemble().shouldPerformRanking()) {
                    // m_FeatureRanking.putAttributesInfos(results.getFimportances());
                    updateFeatureRankings(i, results.getFimportances());

                }

                indTimeSequential += results.getInductionTime();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            catch (ExecutionException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        cr.setInductionTimeSequential(indTimeSequential);

    }


    public OneBagResults induceOneExtraTree(ClusRun cr, int i, TupleIterator train_iterator, TupleIterator test_iterator, ClusRandomNonstatic rnd, ClusStatManager mgr) throws ClusException, IOException, InterruptedException {
        long one_bag_time = ResourceInfo.getTime();
        if (getSettings().getGeneral().getVerbose() > 0)
            System.out.println("Bag: " + i);

        ClusRun crSingle = new ClusRun(cr.getTrainingSet(), cr.getSummary());
        // ClusEnsembleInduce.setRandomSubspaces(cr.getStatManager().getSchema().getDescriptiveAttributes(),
        // cr.getStatManager().getSettings().getNbRandomAttrSelected());
        DepthFirstInduce ind;
        if (getSchema().isSparse()) {
            ind = new DepthFirstInduceSparse(this, mgr, true);
        }
        else {
            ind = new DepthFirstInduce(this, mgr, true);
        }

        ind.initialize();
        crSingle.getStatManager().initClusteringWeights();
        ind.getStatManager().initClusteringWeights();

        initializeROS(ind.getStatManager(), i);

        ClusModel model = ind.induceSingleUnpruned(crSingle, rnd);

        one_bag_time = ResourceInfo.getTime() - one_bag_time;

        //        int[] additionalModelsNodesLeaves = m_OForest.updateCounts((ClusNode) model);
        //        for(int ii = m_OForests.length - 1; ii >= 0; ii--){
        //        	if (getNbTrees(ii) >= i){
        //        		m_OForests[ii].updateCounts(additionalModelsNodesLeaves[0], additionalModelsNodesLeaves[1], additionalModelsNodesLeaves[2]);
        //        	} else{
        //        		break;        		
        //        	}
        //        }
        updateCounts((ClusNode) model, i);

        HashMap<String, double[][]> fimportances = new HashMap<String, double[][]>();
        if (m_FeatRank) {// franking genie3
            // if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_RFOREST)
            // m_FeatureRanking.calculateRFimportance(model, cr, oob_sel);
            //if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_GENIE3) {
            if (getSettings().getEnsemble().getRankingMethod() == SettingsEnsemble.RANKING_GENIE3) {
                // m_FeatureRanking.calculateGENIE3importance((ClusNode)model, cr);
                fimportances = m_FeatureRankings[0].calculateGENIE3importanceIteratively((ClusNode) model, ind.getStatManager());
            }
            //else if (m_BagClus.getSettings().getRankingMethod() == Settings.RANKING_SYMBOLIC) {
            else if (getSettings().getEnsemble().getRankingMethod() == SettingsEnsemble.RANKING_SYMBOLIC) {
                double[] weights = getSettings().getEnsemble().getSymbolicWeights(); //m_BagClus.getSettings().getSymbolicWeights();
                if (weights == null) {
                    weights = new double[] { getSettings().getEnsemble().getSymbolicWeight() }; //m_BagClus.getSettings().getSymbolicWeight() };
                }
                // m_FeatureRanking.calculateSYMBOLICimportance((ClusNode)model, weights, 0);
                fimportances = m_FeatureRankings[0].calculateSYMBOLICimportanceIteratively((ClusNode) model, weights);
            }
            else {
                System.err.println("The following feature ranking methods are implemented for Extra trees:");
                System.err.println("Genie3");
                System.err.println("Symbolic");
                System.err.println("But you have chosen ranking method " + getSettings().getEnsemble().getRankingTypeName(getSettings().getEnsemble().getRankingMethod()));
                System.err.println("No ranking will be computed.");
                m_FeatRank = false;

            }
        }

        if (m_OptMode) {
            updatePredictionsForTuples(model, train_iterator, test_iterator, i);
            // m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);        	
            model = null;
        }
        else {
            // m_OForest.addModelToForest(model); <-- THIS IS DONE IN induceExtraTrees

            // ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
            // m_DForest.addModelToForest(defmod);
        }

        // Valid only when test set is supplied
        if (m_OptMode && (i != m_NbMaxBags) && checkToOutEnsemble(i)) {
            if (m_NbThreads > 1) {
                giveParallelisationWarning(ClusEnsembleInduce.m_PARALLEL_TRAP_optimization);
            }
            //            crSingle.setInductionTime(one_bag_time);
            //            postProcessForest(crSingle);
            //            crSingle.setTestSet(cr.getTestIter());
            //            crSingle.setTrainingSet(cr.getTrainingSet());
            //            outputBetweenForest(crSingle, m_BagClus, "_" + i + "_");
        }

        crSingle.deleteData();
        crSingle.setModels(new ArrayList<ClusModelInfo>());

        return new OneBagResults(model, fimportances, crSingle, null, one_bag_time);
    }


    // Checks whether we reached a limit
    public boolean checkToOutEnsemble(int idx) {
        for (int i = 0; i < m_OutEnsembleAt.length; i++)
            if (m_OutEnsembleAt[i] == idx)
                return true;
        return false;
    }


    public void postProcessForest(ClusRun cr) throws ClusException {
        //      ClusModelInfo def_info = cr.addModelInfo("Default");
        //if (m_OptMode)
        //    m_DForest = null;

        //        def_info.setModel(ClusDecisionTree.induceDefault(cr));

        //        ClusModelInfo orig_info = cr.addModelInfo("Original");
        //        orig_info.setModel(m_OForest);

        String partialForestName = "Forest with %d trees";

        for (int i = 0; i < m_OForests.length; i++) {
            int nbTrees = m_OForests[i].getNbModels();
            ClusModelInfo modelInfo = cr.addModelInfo(String.format(partialForestName, nbTrees));
            modelInfo.setModel(m_OForests[i]);
        }

        // Application of Thresholds for HMC
        if (getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            double[] thresholds = getSettings().getHMLC().getClassificationThresholds().getDoubleVector();
            // setting the printing preferences in the HMC mode

            //m_OForest.setPrintModels(getSettings().getEnsemble().isPrintEnsembleModels());
            for (int i = 0; i < m_OForests.length; i++) {
                m_OForests[i].setPrintModels(getSettings().getEnsemble().isPrintEnsembleModels());
            }

            //if (!m_OptMode) m_DForest.setPrintModels(getSettings().getEnsemble().isPrintEnsembleModels());

            if (thresholds != null) {
                for (int forest = 0; forest < m_OForests.length; forest++) {
                    for (int i = 0; i < thresholds.length; i++) {
                        String basicName = String.format(partialForestName, m_OForests[forest].getNbModels());
                        String thresholdedName = String.format(Locale.ENGLISH, "%s(T = %.1f)", basicName, thresholds[i]);
                        ClusModelInfo pruned_info = cr.addModelInfo(thresholdedName); //("T(" + thresholds[i] + ")");
                        ClusForest new_forest = m_OForests[forest].cloneForestWithThreshold(thresholds[i]);
                        new_forest.setPrintModels(getSettings().getEnsemble().isPrintEnsembleModels());
                        pruned_info.setShouldWritePredictions(false);
                        pruned_info.setModel(new_forest);
                    }
                }
            }
        }

        // If we want to convert trees to rules but not use
        // any of the rule learning tehniques anymore (if CoveringMethod != RulesFromTree)
        if (getSettings().getTree().rulesFromTree() != SettingsOutput.CONVERT_RULES_NONE
                && getSettings().getRules().getCoveringMethod() != SettingsRules.COVERING_METHOD_RULES_FROM_TREE) {
            //m_OForest.convertToRules(cr, false);
            for (int i = 0; i < m_OForests.length; i++) {
                m_OForests[i].convertToRules(cr, false);
            }
        }
    }


    public synchronized void outputBetweenForest(ClusRun cr, Clus cl, String addname) throws IOException, ClusException {
        // if (m_OptMode){
        // ClusEnsembleInduceOptimization.roundPredictions();
        // }
        Settings sett = cr.getStatManager().getSettings();
        ClusSchema schema = cr.getStatManager().getSchema();
        ClusOutput output = new ClusOutput(sett.getGeneric().getAppName() + addname + ".out", schema, sett);
        ClusSummary summary = cr.getSummary();
        getStatManager().computeTrainSetStat((RowData) cr.getTrainingSet());
        cl.calcError(cr, null, null); // Calc error
        if (summary != null) {
            for (int i = ClusModel.ORIGINAL; i < cr.getNbModels(); i++) {
                ClusModelInfo summ_info = cr.getModelInfo(i);
                ClusErrorList test_err = summ_info.getTestError();
                summ_info.setTestError(test_err);
            }
        }
        cl.calcExtraTrainingSetErrors(cr);
        output.writeHeader();
        output.writeOutput(cr, true, getSettings().getOutput().isOutTrainError());
        output.close();
        cl.getClassifier().saveInformation(sett.getGeneric().getAppName());
        ClusModelCollectionIO io = new ClusModelCollectionIO();
        cl.saveModels(cr, io);
        io.save(cl.getSettings().getGeneric().getFileAbsolute(cr.getStatManager().getSettings().getGeneric().getAppName() + addname + ".model"));
    }


    /**
     * Maximum number of bags for memory optimization
     */
    public static int getMaxNbBags() {
        return ClusEnsembleInduce.m_NbMaxBags;
    }


    public int getNbTrees(int forestIndex) {
        return m_OutEnsembleAt[forestIndex];
    }


    /**
     * Selects random subspaces.
     * 
     * @param attrs
     *        -- For which attributes
     * @param select
     *        -- How many
     */
    public static ClusAttrType[] selectRandomSubspaces(ClusAttrType[] attrs, int select, int randomizerVersion, ClusRandomNonstatic rand) {
        int origsize = attrs.length;
        int[] samples = new int[origsize];
        int rnd;
        boolean randomize = true;
        int i = 0;
        if (rand == null) {
            while (randomize) {
                rnd = ClusRandom.nextInt(randomizerVersion, origsize);
                if (samples[rnd] == 0) {
                    samples[rnd]++;
                    i++;
                }
                randomize = i != select;
            }
        }
        else {
            while (randomize) {
                rnd = rand.nextInt(randomizerVersion, origsize);
                if (samples[rnd] == 0) {
                    samples[rnd]++;
                    i++;
                }
                randomize = i != select;
            }
        }

        ClusAttrType[] result = new ClusAttrType[select];
        int res = 0;
        for (int k = 0; k < origsize; k++) {
            if (samples[k] != 0) {
                result[res] = attrs[k];
                res++;
            }
        }
        return result;
    }


    public static ClusAttrType[] getRandomSubspaces() {
        return m_RandomSubspaces;
    }


    public static void setRandomSubspaces(ClusAttrType[] attrs) {
        m_RandomSubspaces = attrs;
    }

    //    public static void setRandomSubspaces(ClusAttrType[] attrs, int select, ClusRandomNonstatic rnd) {
    //        m_RandomSubspaces = ClusEnsembleInduce.selectRandomSubspaces(attrs, select, ClusRandomNonstatic.RANDOM_SELECTION, rnd);
    //    }


    /**
     * Memory optimization
     */
    public static boolean isOptimized() {
        return m_OptMode;
    }


    public ClusFeatureRanking getEnsembleFeatureRanking(int i) {
        return m_FeatureRankings[i];
    }


    public synchronized static void giveParallelisationWarning(int reason) {
        if (!m_WarningsGiven[reason]) {
            String message;
            switch (reason) {
                case ClusEnsembleInduce.m_PARALLEL_TRAP_BestFirst_getDescriptiveAttributes:
                    message = "clus.ext.bestfirst.BestFirstInduce.getDescriptiveAttributes() has been called. This may not work in parallel setting.";
                    break;
                case ClusEnsembleInduce.m_PARALLEL_TRAP_DepthFirst_getDescriptiveAttributes:
                    message = "clus.algo.tdidt.DepthFirstInduce.getDescriptiveAttributes(ClusRandomNonstatic) has been called. This may not work in parallel setting.";
                    break;
                case ClusEnsembleInduce.m_PARALLEL_TRAP_staticRandom:
                    message = "Static random has been called. This may not work in parallel setting.";
                    break;
                case ClusEnsembleInduce.m_PARALLEL_TRAP_optimization:
                    message = "Memory usage optimization is on. You have reached a place where there might be some unexpected behaviour in parallel setting because of that.";
                    break;
                default:
                    throw new RuntimeException("Wrong reason for giveParallelisationWarning: " + reason);
            }
            System.err.println("Warning:");
            System.err.println(message);
            System.err.println("There will be no additional warnings for this.");
            m_WarningsGiven[reason] = true;

        }
    }


    /**
     * Updates the counts of nodes, leaves and trees in the forests, by adding the corresponding statistics
     * of a new tree to the current statistics in forest.
     * 
     * @param model
     * @param treeNumber
     */
    private void updateCounts(ClusNode model, int treeNumber) {

        //models, nodes, leaves };

        int[] additionalModelsNodesLeaves = ClusForest.countNodesLeaves(model); // m_OForest.updateCounts((ClusNode) model);     

        for (int ii = m_OForests.length - 1; ii >= 0; ii--) {
            if (getNbTrees(ii) >= treeNumber) {
                m_OForests[ii].updateCounts(additionalModelsNodesLeaves[0], additionalModelsNodesLeaves[1], additionalModelsNodesLeaves[2]);
            }
            else {
                break;
            }
        }
    }


    /**
     * Updated the feature ranking importances for all rankings, by adding the importances from a new tree.
     * 
     * @param treeIndex
     * @param fimportances
     * @throws InterruptedException
     */
    private void updateFeatureRankings(int treeIndex, HashMap<String, double[][]> fimportances) throws InterruptedException {
        //    	m_FeatureRanking.putAttributesInfos(fimportances);
        for (int forest = m_OForests.length - 1; forest >= 0; forest--) {
            if (getNbTrees(forest) >= treeIndex) {
                m_FeatureRankings[forest].putAttributesInfos(fimportances);
            }
            else {
                break;
            }
        }
    }


    /**
     * Adds model to forests.
     * 
     * @param model
     * @param treeNumber
     */
    private void addModelToForests(ClusModel model, int treeNumber) {
        //m_OForest.addModelToForest(model);
        for (int forest = m_OForests.length - 1; forest >= 0; forest--) {
            if (getNbTrees(forest) >= treeNumber) {
                m_OForests[forest].addModelToForest(model);
            }
            else {
                break;
            }
        }
    }


    private ClusEnsembleInduceOptimization initializeOptimization(TupleIterator train_iterator, TupleIterator test_iterator) throws IOException, ClusException {
        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION) {
            return new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, getSettings());
        }
        else if (m_Mode == ClusStatManager.MODE_CLASSIFY) {
            return new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, getSettings());
        }
        else {
            int[] values = new int[] { ClusStatManager.MODE_HIERARCHICAL, ClusStatManager.MODE_REGRESSION, ClusStatManager.MODE_CLASSIFY, m_Mode };
            String line1 = "Optimization supported only for the following modes:";
            String line2 = String.format("MODE_HIERARCHICAL = %d, MODE_REGRESSION = %d and MODE_CLASSIFY = %d", values[0], values[1], values[2]);
            String line3 = String.format("Unfortunately: m_Mode = %d", values[3]);
            String message = String.join("\n", line1, line2, line3);
            throw new ClusException(message);
        }
        // old version: additional argument in constructors:
        // test_iterator != null ? cr.getTrainingSet().getNbRows() + cr.getTestSet().getNbRows()) : cr.getTrainingSet().getNbRows();
    }


    private void updatePredictionsForTuples(ClusModel model, TupleIterator train_iterator, TupleIterator test_iterator, int treeNumber) throws IOException, ClusException {
        //m_Optimization.updatePredictionsForTuples(model, train_iterator, test_iterator);
        for (int forest = m_OForests.length - 1; forest >= 0; forest--) {
            if (getNbTrees(forest) >= treeNumber) {
                m_Optimizations[forest].updatePredictionsForTuples(model, train_iterator, test_iterator);
            }
            else {
                break;
            }
        }
    }

}

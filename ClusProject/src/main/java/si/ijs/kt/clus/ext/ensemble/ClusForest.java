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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.algo.rules.ClusRulesFromTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.hierarchical.HierClassTresholdPruner;
import si.ijs.kt.clus.ext.hierarchical.HierSingleLabelStat;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsEnsemble;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.processor.ClusEnsemblePredictionWriter;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.GeneticDistanceStat;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.RegressionStatBase;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


/**
 * Ensemble of decision trees.
 *
 */
public class ClusForest implements ClusModel, Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    /** A list of decision trees in the forest (or empty if memory optimisation is used). */
    ArrayList<ClusModel> m_Forest;
    /** Number of threes in the forest, may not be equal to {@code m_Forest.size()} because of memory optimisation. */
    private int m_NbModels = 0;
    /** The sum of nodes over the trees in the forest */
    private int m_NbNodes = 0;
    /** The sum of leaves over the trees in the forest */
    private int m_NbLeaves = 0;
    ClusEnsembleROSInfo m_TargetSubspaceInfo; // Info about target subspacing method

    //added 13/1/2014 by Jurica Levatic
    /** Individual votes of trees in ensemble, used for calculation of confidence of predictions in self-training */
    ArrayList<ClusStatistic> m_Votes;
    /**
     * // FIXME: A little hack to manipulate model info from outside, this should be implemented otherwise (e.g.,
     * ClusModel for Self-training which wraps around this model)
     */
    String m_modelInfo = "";
    /** To store random forest proximities, used in self-training */
    HashMap<Integer, Double> m_Proximities;
    int m_Mode;
    // - end added by Jurica

    private ClusReadWriteLock m_NbModelsLock = new ClusReadWriteLock();
    private ClusReadWriteLock m_NbNodesLock = new ClusReadWriteLock();
    private ClusReadWriteLock m_NbLeavesLock = new ClusReadWriteLock();

    ClusStatistic m_Stat;
    boolean m_PrintModels;
    String m_AttributeList;
    String m_AppName;

    private ClusEnsembleInduceOptimization m_Optimization;

    protected Settings m_Settings;
    protected ClusStatManager m_StatManager;


    public ClusForest(ClusStatManager statmgr) {
        m_Forest = new ArrayList<ClusModel>();

        m_Settings = statmgr.getSettings();
        m_StatManager = statmgr;
    }


    public ClusForest(ClusStatManager statmgr, ClusEnsembleInduceOptimization opt) {
        this(statmgr);

        int mode = statmgr.getMode();

        switch (mode) {
            case ClusStatManager.MODE_CLASSIFY:
                if (statmgr.getSettings().getMLC().getSectionMultiLabel().isEnabled()) {
                    m_Stat = new ClassificationStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET), statmgr.getSettings().getMLC().getMultiLabelThreshold());
                }
                else {
                    m_Stat = new ClassificationStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
                }
                break;

            case ClusStatManager.MODE_REGRESSION:
                m_Stat = new RegressionStat(statmgr.getSettings(), statmgr.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
                break;

            case ClusStatManager.MODE_CLASSIFY_AND_REGRESSION:
                m_Stat = statmgr.getStatistic(ClusAttrType.ATTR_USE_TARGET); //FIXME: Probably all statistics could be initialized like this? i.e., there is no need for checking mode?
                break;

            case ClusStatManager.MODE_HIERARCHICAL:
                if (statmgr.getSettings().getHMLC().getHierSingleLabel()) {
                    m_Stat = new HierSingleLabelStat(statmgr.getSettings(), statmgr.getHier(), statmgr.getCompatibility());
                }
                else {
                    m_Stat = new WHTDStatistic(statmgr.getSettings(), statmgr.getHier(), statmgr.getCompatibility());
                }
                break;

            case ClusStatManager.MODE_PHYLO:
                m_Stat = new GeneticDistanceStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
                break;

            default:
                System.err.println(getClass().getName() + " initForest(): Error initializing the statistic " + statmgr.getMode());
                break;
        }

        m_AppName = statmgr.getSettings().getGeneric().getFileAbsolute(statmgr.getSettings().getGeneric().getAppName());
        m_AttributeList = "";
        ClusAttrType[] cat = ClusSchema.vectorToAttrArray(statmgr.getSchema().collectAttributes(ClusAttrType.ATTR_USE_DESCRIPTIVE, ClusAttrType.THIS_TYPE));
        if (statmgr.getSettings().getOutput().isOutputPythonModel()) {
            for (int ii = 0; ii < cat.length - 1; ii++)
                m_AttributeList = m_AttributeList.concat(cat[ii].getName() + ", ");
            m_AttributeList = m_AttributeList.concat(cat[cat.length - 1].getName());
        }
        m_Optimization = opt;

    }


    private Settings getSettings() {
        return m_Settings;
    }


    public void setOptimization(ClusEnsembleInduceOptimization opt) {
        m_Optimization = opt;
    }


    public void setEnsembleROSInfo(ClusEnsembleROSInfo tinfo) {
        m_TargetSubspaceInfo = tinfo;
    }


    public synchronized void addModelToForest(ClusModel model) {
        m_Forest.add(model);
    }


    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.applyModelProcessors(tuple, mproc);
        }
    }


    public void attachModel(HashMap table) throws ClusException {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.attachModel(table);
        }
    }


    public int getID() {
        // TODO Auto-generated method stub
        return 0;
    }


    public int getNbModels() {
        m_NbModelsLock.readingLock();
        int ans = m_NbModels;
        m_NbModelsLock.readingUnlock();
        return ans;
    }


    private void increaseNbModels(int n) {
        m_NbModelsLock.writingLock();
        m_NbModels += n;
        m_NbModelsLock.writingUnlock();

    }


    private int getNbNodes() {
        m_NbNodesLock.readingLock();
        int ans = m_NbNodes;
        m_NbNodesLock.readingUnlock();
        return ans;
    }


    private void increaseNbNodes(int n) {
        m_NbNodesLock.writingLock();
        m_NbNodes += n;
        m_NbNodesLock.writingUnlock();
    }


    private int getNbLeaves() {
        m_NbLeavesLock.readingLock();
        int ans = m_NbLeaves;
        m_NbLeavesLock.readingUnlock();
        return ans;
    }


    private void increaseNbLeaves(int n) {
        m_NbLeavesLock.writingLock();
        m_NbLeaves += n;
        m_NbLeavesLock.writingUnlock();
    }


    public int[] updateCounts(ClusNode model) {
        int models = 1;
        int nodes = model.getNbNodes();
        int leaves = model.getNbLeaves();
        updateCounts(models, nodes, leaves);
        return new int[] { models, nodes, leaves };
    }


    /**
     * Used for more efficient updating if the corresponding tree is a part of more than one forest.
     * 
     * @param nbModels
     *        Should equal 1, i.e., a tree is one model
     * @param nbNodes
     *        number of nodes in the tree
     * @param nbLeaves
     *        number of leaves in the tree
     */
    public void updateCounts(int nbModels, int nbNodes, int nbLeaves) {
        increaseNbModels(nbModels);
        increaseNbNodes(nbNodes);
        increaseNbLeaves(nbLeaves);
    }


    /**
     * Set some additional model info (used in self-training, which wraps around ensembles)
     * 
     * @param additionalInfo
     */
    public void setModelInfo(String additionalInfo) {
        m_modelInfo = additionalInfo;
    }


    public String getModelInfo() {
        //        int sumOfLeaves = 0;
        //        for (int i = 0; i < getNbModels(); i++)
        //            sumOfLeaves += ((ClusNode) getModel(i)).getNbLeaves();
        //
        //        int sumOfNodes = 0;
        //        for (int i = 0; i < getNbModels(); i++)
        //            sumOfNodes += ((ClusNode) getModel(i)).getNbNodes();

        String targetSubspaces = "";
        if (m_TargetSubspaceInfo != null) {
            String indent = "\n\t\t\t\t";

            targetSubspaces = indent + m_TargetSubspaceInfo.getAverageNumberOfTargetsUsedInfo() + indent + m_TargetSubspaceInfo.getCoverageInfo() + indent + m_TargetSubspaceInfo.getCoverageNormalizedInfo();
        }

        String result = String.format("FOREST with %d models (Total nodes: %d and leaves: %d)%s\n", getNbModels(), getNbNodes(), getNbLeaves(), targetSubspaces);

        if (getSettings().getEnsemble().isPrintEnsembleModelInfo()) {
            for (int i = 0; i < getNbModels(); i++) {
                result += "\n\t Model " + (i + 1) + ": " + getModel(i).getModelInfo();

                if (m_TargetSubspaceInfo != null) {
                    result += "\tTargets: " + m_TargetSubspaceInfo.getInfo(i);
                }
            }
        }

        return m_modelInfo + result;
    }


    public int getModelSize() {
        return m_Forest.size(); // Maybe something else ?!
    }


    public ClusStatistic predictWeighted(DataTuple tuple) {
        if (ClusEnsembleInduce.m_EnsembleROSScope != SettingsEnsemble.ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_NONE) {
            switch (ClusEnsembleInduce.m_EnsembleROSScope) {
                case SettingsEnsemble.ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_SUBSET_AVERAGING: // only use subspaces for prediction averaging
                    return ClusEnsembleInduce.isOptimized() ? predictWeightedStandardSubspaceAveragingOpt(tuple) : predictWeightedStandardSubspaceAveraging(tuple);

                case SettingsEnsemble.ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_SMARTERWAY:
                    throw new RuntimeException("NOT YET IMPLEMENTED!");

                default: // case Settings.ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_TOTAL_AVERAGING: // just use all predictions
                    return ClusEnsembleInduce.isOptimized() ? predictWeightedOpt(tuple) : predictWeightedStandard(tuple);
            }
        }
        if (ClusOOBErrorEstimate.isOOBCalculation())
            return predictWeightedOOB(tuple);
        if (!ClusEnsembleInduce.isOptimized())
            return predictWeightedStandard(tuple);
        else
            return predictWeightedOpt(tuple);

        /*
         * ClusModel model;
         * ArrayList votes = new ArrayList();
         * for (int i = 0; i < m_Forest.size(); i++){
         * model = (ClusModel)m_Forest.get(i);
         * votes.add(model.predictWeighted(tuple));
         * if (tuple.getWeight() != 1.0) System.out.println("Tuple "+tuple.getIndex()+" = "+tuple.getWeight());
         * }
         * m_Stat.vote(votes);
         * return m_Stat;
         */
    }


    public ClusStatistic predictWeightedStandard(DataTuple tuple) {
        ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
        for (int i = 0; i < m_Forest.size(); i++) {
            votes.add(m_Forest.get(i).predictWeighted(tuple));
            // if (tuple.getWeight() != 1.0) System.out.println("Tuple "+tuple.getIndex()+" = "+tuple.getWeight());
        }

        m_Stat.reset();
        //remember votes
        m_Votes = votes;
        m_Stat.vote(votes);
        ClusEnsemblePredictionWriter.setVotes(votes);
        return m_Stat;
    }


    //added 13/1/2014 by Jurica Levatic
    /*
     * Returns individual votes of base methods in ensemble for the latest prediction made
     */
    public ArrayList<ClusStatistic> getVotes() {
        return m_Votes;
    }


    /**
     * Votes from models for which the tuple was Out Of Bag.
     *
     * @param tuple
     * @return ArrayList containing votes or null if tuple was not OOB for any of the models
     */
    public ArrayList getOOBVotes(DataTuple tuple) {
        if (ClusOOBErrorEstimate.containsPredictionForTuple(tuple)) { return (ArrayList) ClusOOBErrorEstimate.getVotesForTuple(tuple); }

        return null;
    }


    public boolean containsOOBForTuple(DataTuple tuple) {
        return ClusOOBErrorEstimate.containsPredictionForTuple(tuple);
    }


    public ClusStatistic predictWeightedOOB(DataTuple tuple) {

        if (ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_HIERARCHICAL || ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_REGRESSION)
            return predictWeightedOOBRegressionHMC(tuple);
        if (ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_CLASSIFY)
            return predictWeightedOOBClassification(tuple);

        System.err.println(this.getClass().getName() + ".predictWeightedOOB(DataTuple) - Error in Setting the Mode");
        return null;
    }


    public ClusStatistic predictWeightedOOBRegressionHMC(DataTuple tuple) {
        double[] predictions = null;
        if (ClusOOBErrorEstimate.containsPredictionForTuple(tuple))
            predictions = ClusOOBErrorEstimate.getPredictionForRegressionHMCTuple(tuple);
        else {
            System.err.println(this.getClass().getName() + ".predictWeightedOOBRegressionHMC(DataTuple) - Missing Prediction For Tuple");
            System.err.println("Tuple Hash = " + tuple.hashCode());
        }
        m_Stat.reset();
        ((RegressionStatBase) m_Stat).m_Means = new double[predictions.length];
        for (int j = 0; j < predictions.length; j++)
            ((RegressionStatBase) m_Stat).m_Means[j] = predictions[j];
        if (ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_HIERARCHICAL)
            m_Stat = (WHTDStatistic) m_Stat;
        else
            m_Stat = (RegressionStat) m_Stat;
        m_Stat.computePrediction();
        return m_Stat;
    }


    public ClusStatistic predictWeightedOOBClassification(DataTuple tuple) {
        double[][] predictions = null;
        if (ClusOOBErrorEstimate.containsPredictionForTuple(tuple))
            predictions = ClusOOBErrorEstimate.getPredictionForClassificationTuple(tuple);
        else {
            System.err.println(this.getClass().getName() + ".predictWeightedOOBClassification(DataTuple) - Missing Prediction For Tuple");
            System.err.println("Tuple Hash = " + tuple.hashCode());
        }
        m_Stat.reset();

        ((ClassificationStat) m_Stat).m_ClassCounts = new double[predictions.length][];
        for (int m = 0; m < predictions.length; m++) {
            ((ClassificationStat) m_Stat).m_ClassCounts[m] = new double[predictions[m].length];
            for (int n = 0; n < predictions[m].length; n++) {
                ((ClassificationStat) m_Stat).m_ClassCounts[m][n] = predictions[m][n];
            }
        }
        m_Stat.computePrediction();
        for (int k = 0; k < m_Stat.getNbAttributes(); k++)
            ((ClassificationStat) m_Stat).m_SumWeights[k] = 1.0;// the m_SumWeights variable is not used for OOB error
                                                                // estimate or feature ranking
        return m_Stat;
    }


    public ClusStatistic predictWeightedOpt(DataTuple tuple) { // TODO: paralelno pazi matejp
        int position = m_Optimization.locateTuple(tuple);
        int predlength = m_Optimization.getPredictionLength(position);
        m_Stat.reset();
        if (m_StatManager.getMode() == ClusStatManager.MODE_REGRESSION || m_StatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            ((RegressionStatBase) m_Stat).m_Means = new double[predlength];
            for (int i = 0; i < predlength; i++) {
                ((RegressionStatBase) m_Stat).m_Means[i] = ((ClusEnsembleInduceOptRegHMLC) m_Optimization).getPredictionValue(position, i);
            }
            m_Stat.computePrediction();
            return m_Stat;
        }
        if (m_StatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
            ((ClassificationStat) m_Stat).m_ClassCounts = new double[predlength][];
            for (int j = 0; j < predlength; j++) {
                ((ClassificationStat) m_Stat).m_ClassCounts[j] = ((ClusEnsembleInduceOptClassification) m_Optimization).getPredictionValueClassification(position, j);
            }
            m_Stat.computePrediction();
            for (int k = 0; k < m_Stat.getNbAttributes(); k++) {
                ((ClassificationStat) m_Stat).m_SumWeights[k] = 1.0;// the m_SumWeights variable is not used in mode optimize m_Stat.setTrainingStat(ClusEnsembleInduceOptClassification.getTrainingStat());
            }
            return m_Stat;
        }

        throw new RuntimeException("clus.ext.ensembles.ClusForest.predictWeightedOpt(DataTuple): unhandled ClusStatManager.getMode() case!");
    }


    /**
     * The same as predict weighted standard, but also calculates random forest proximities
     */
    public ClusStatistic predictWeightedStandardAndGetProximities(DataTuple tuple) {
        m_Proximities = new HashMap<Integer, Double>();

        ClusNode model;
        ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
        List<Integer> leafTuples;
        Integer tupleIndex = null;
        double incrementValue = 1.0 / m_Forest.size(); //proximitiy is incremented in each step for this value, ensures proximities in [0,1]

        for (int i = 0; i < m_Forest.size(); i++) {
            model = (ClusNode) m_Forest.get(i);
            leafTuples = new LinkedList<Integer>();
            votes.add(model.predictWeightedAndGetLeafTuples(tuple, leafTuples));

            //calculate proximities
            for (int j = 0; j < leafTuples.size(); j++) {
                tupleIndex = leafTuples.get(j);
                if (m_Proximities.containsKey(tupleIndex)) {
                    m_Proximities.put(tupleIndex, m_Proximities.get(tupleIndex) + incrementValue);
                }
                else {
                    m_Proximities.put(tupleIndex, incrementValue);
                }
            }
        }
        //remember votes
        m_Stat.reset();
        m_Votes = votes;

        m_Stat.vote(votes);
        ClusEnsemblePredictionWriter.setVotes(votes);
        return m_Stat;
    }


    /**
     * The same as predict weighted OOB, but also calculates random forest proximities
     */
    public ClusStatistic predictWeightedOOBAndGetProximities(DataTuple tuple) {
        m_Proximities = new HashMap<Integer, Double>();

        ClusNode model;
        ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
        List<Integer> leafTuples;
        double incrementValue = 1.0 / m_Forest.size(); //proximitiy is incremented in each step for this value, ensures proximities in [0,1]
        Integer tupleIndex;

        for (int i = 0; i < m_Forest.size(); i++) {
            model = (ClusNode) m_Forest.get(i);

            if (ClusOOBErrorEstimate.isOOBForTree(tuple, i + 1)) { //get proximities only from trees where example was OOB, models are enumerated starting from 1, so we add 1               
                leafTuples = new LinkedList<Integer>();
                votes.add(model.predictWeightedAndGetLeafTuples(tuple, leafTuples));

                //calculate proximities
                for (int j = 0; j < leafTuples.size(); j++) {
                    tupleIndex = leafTuples.get(j);
                    if (m_Proximities.containsKey(tupleIndex)) {
                        m_Proximities.put(tupleIndex, (double) m_Proximities.get(tupleIndex) + incrementValue);
                    }
                    else {
                        m_Proximities.put(tupleIndex, incrementValue);
                    }
                }
            }
        }

        //remember votes
        m_Stat.reset();
        m_Votes = votes;

        m_Stat.vote(votes);
        ClusEnsemblePredictionWriter.setVotes(votes);
        return m_Stat;
    }


    /**
     * Store indices of tuples in leaf nodes. Used to calculate RForest
     * proximities
     * (https://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#prox)
     *
     * After initialization call
     * predictWeightedStandardAndGetProximities(DataTuple t) to calculate
     * proximities to the tuples initialized with this function.
     *
     * @param data
     */
    public void initializeProximities(DataTuple t) {
        for (int j = 0; j < m_Forest.size(); j++) {
            ((ClusNode) m_Forest.get(j)).incrementProximities(t);

        }
    }


    public HashMap<Integer, Double> getProximities() {
        return m_Proximities;
    }


    /** used for ROS ensembles */
    public ClusStatistic predictWeightedStandardSubspaceAveraging(DataTuple tuple) {
        ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
        for (int i = 0; i < m_Forest.size(); i++) {
            votes.add(m_Forest.get(i).predictWeighted(tuple));
        }
        m_Stat.vote(votes, m_TargetSubspaceInfo);
        ClusEnsemblePredictionWriter.setVotes(votes);
        return m_Stat;
    }


    /** used for OPTIMIZED ROS ensembles */
    public ClusStatistic predictWeightedStandardSubspaceAveragingOpt(DataTuple tuple) {
        // when ensembles are optimized, the running average takes into account the subspaces, so predictWeightedOpt() should return correct results

        return predictWeightedOpt(tuple);
    }


    public void printModel(PrintWriter wrt) {
        // This could be better organized
        if (getSettings().getEnsemble().isPrintEnsembleModels()) {
            ClusModel model;
            for (int i = 0; i < m_Forest.size(); i++) {
                model = (ClusModel) m_Forest.get(i);
                if (m_PrintModels)
                    thresholdToModel(i, getThreshold());// This will be enabled only in HMLC mode
                wrt.write("Model " + (i + 1) + ": \n");
                wrt.write("\n");
                model.printModel(wrt);
                wrt.write("\n");
            }
        }
        else
            wrt.write("Forest with " + getNbModels() + " models\n");

    }


    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        // This could be better organized
        if (getSettings().getEnsemble().isPrintEnsembleModels()) {
            ClusModel model;
            for (int i = 0; i < m_Forest.size(); i++) {
                model = (ClusModel) m_Forest.get(i);
                if (m_PrintModels)
                    thresholdToModel(i, getThreshold());// This will be enabled only in HMLC mode
                wrt.write("Model " + (i + 1) + ": \n");
                wrt.write("\n");
                model.printModel(wrt);
                wrt.write("\n");
            }
        }
        else
            wrt.write("Forest with " + getNbModels() + " models\n");

    }


    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = (ClusModel) m_Forest.get(i);
            model.printModelAndExamples(wrt, info, examples);
        }
    }


    public void printModelToPythonScript(PrintWriter wrt) {
        printForestToPython();
    }


    @Override
    public JsonObject getModelJSON() {
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info) {
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples) {
        return null;
    }


    public void printForestToPython() {
        // create a separate .py file
        try {
            File pyscript = new File(m_AppName + "_models.py");
            PrintWriter wrtr = new PrintWriter(new FileOutputStream(pyscript));
            wrtr.println("# Python code of the trees in the ensemble");
            wrtr.println();
            for (int i = 0; i < m_Forest.size(); i++) {
                ClusModel model = (ClusModel) m_Forest.get(i);
                wrtr.println("#Model " + (i + 1));
                wrtr.println("def clus_tree_" + (i + 1) + "(" + m_AttributeList + "):");
                model.printModelToPythonScript(wrtr);
                wrtr.println();
            }
            wrtr.flush();
            wrtr.close();
            System.out.println("Model to Python Code written to: " + pyscript.getName());
        }
        catch (IOException e) {
            System.err.println(this.getClass().getName() + ".printForestToPython(): Error while writing models to python script");
            e.printStackTrace();
        }
    }


    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean ex) {
        // TODO Auto-generated method stub
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = (ClusModel) m_Forest.get(i);
            model.printModelToQuery(wrt, cr, starttree, startitem, ex);
        }
    }


    public ClusModel prune(int prunetype) {
        // TODO Auto-generated method stub
        return null;
    }


    public void retrieveStatistics(ArrayList list) {
        // TODO Auto-generated method stub
    }


    public void showForest() {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            System.out.println("***************************");
            model = (ClusModel) m_Forest.get(i);
            ((ClusNode) model).printTree();
            System.out.println("***************************");
        }
    }


    /**
     * @param idx
     *        The index of the decision tree. Thus this is NOT the type enumeration introduced in ClusModel.
     * @return
     */
    public ClusModel getModel(int idx) {
        return (ClusModel) m_Forest.get(idx);
    }


    /**
     * @return Number of decision trees in the ensemble.
     */
    @Deprecated
    public int getNbModelsOld() {
        // for now same as getModelSize();
        return m_Forest.size();
    }


    public ClusStatistic getStat() {
        return m_Stat;
    }


    public void setStat(ClusStatistic stat) {
        m_Stat = stat;
    }


    public void thresholdToModel(int model_nb, double threshold) {
        try {
            HierClassTresholdPruner pruner = new HierClassTresholdPruner(null);
            pruner.pruneRecursive((ClusNode) getModel(model_nb), threshold);
        }
        catch (ClusException e) {
            System.err.println(getClass().getName() + " thresholdToModel(): Error while applying threshold " + threshold + " to model " + model_nb);
            e.printStackTrace();
        }
    }


    /**
     * Return the list of decision trees = the ensemble.
     */
    public ArrayList<ClusModel> getModels() {
        return m_Forest;
    }


    public void setModels(ArrayList<ClusModel> models) {
        m_Forest = models;
    }


    // this is only for Hierarchical ML Classification
    public ClusForest cloneForestWithThreshold(double threshold) {
        ClusForest clone = new ClusForest(m_StatManager);
        clone.setModels(getModels());
        WHTDStatistic stat = (WHTDStatistic) getStat().cloneStat();
        stat.copyAll(getStat());
        stat.setThreshold(threshold);
        clone.setStat(stat);
        // some additional work has to be done in the case of optimization
        clone.updateCounts(m_NbModels, m_NbNodes, m_NbLeaves);
        clone.setOptimization(m_Optimization);
        return clone;
    }


    public void setPrintModels(boolean print) {
        m_PrintModels = print;
    }


    public boolean isPrintModels() {
        return m_PrintModels;
    }


    public double getThreshold() {
        return ((WHTDStatistic) getStat()).getThreshold();
    }


    public void removeModels() {
        m_Forest.clear();
    }


    /**
     * Converts the forest to rule set and adds the model.
     * Used only for getting information about the forest - not for creating the rule set itself
     * (CoveringMethod = RulesFromTree)
     * 
     * @param cr
     * @param addOnlyUnique
     *        Add only unique rules to rule set. Do NOT use this if you want to count something
     *        on the original forest.
     * @return rule set.
     * @throws ClusException
     * @throws IOException
     */
    public void convertToRules(ClusRun cr, boolean addOnlyUnique) {
        /**
         * The class for transforming single trees to rules
         */
        ClusRulesFromTree treeTransform = new ClusRulesFromTree(true, cr.getStatManager().getSettings().getTree().rulesFromTree()); // Parameter
        // always
        // true
        ClusRuleSet ruleSet = new ClusRuleSet(cr.getStatManager()); // Manager from super class

        // ClusRuleSet ruleSet = new ClusRuleSet(m_Clus.getStatManager());

        // Get the trees and transform to rules
        // int numberOfUniqueRules = 0;

        for (int iTree = 0; iTree < getNbModels(); iTree++) {
            // Take the root node of the tree
            ClusNode treeRootNode = (ClusNode) getModel(iTree);

            // Transform the tree into rules and add them to current rule set
            // numberOfUniqueRules +=
            ruleSet.addRuleSet(treeTransform.constructRules(treeRootNode, cr.getStatManager()), addOnlyUnique);
        }

        ruleSet.addDataToRules((RowData) cr.getTrainingSet());

        ClusModelInfo rules_info = cr.addModelInfo("Rules-" + cr.getModelInfo(ClusModel.ORIGINAL).getName());
        rules_info.setModel(ruleSet);
    }

}

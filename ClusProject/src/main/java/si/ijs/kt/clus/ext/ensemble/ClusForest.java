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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.algo.rules.ClusRulesFromTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.ext.hierarchical.HierClassTresholdPruner;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsOutput.PythonModelType;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.processor.ClusEnsemblePredictionWriter;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.GeneticDistanceStat;
import si.ijs.kt.clus.statistic.HierSingleLabelStat;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.RegressionStatBase;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;
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
    ClusEnsembleROSInfo m_TargetSubspaceInfo = null; // ROS ensembles (info about target subspacing)

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
//    String m_AttributeList;
    
    String m_AppName;

    private ClusEnsembleInduceOptimization m_Optimization;

    protected Settings m_Settings;
    protected ClusStatManager m_StatManager;
    
    private static String m_PythonFileTreePattern = "_trees";
    private String m_PythonFileEnsemblePattern = "_ensemble_%dtrees";

	private HashMap<String, Integer> m_DescriptiveIndex;


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
                    m_Stat = new ClassificationStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(AttributeUseType.Target), statmgr.getSettings().getMLC().getMultiLabelThreshold());
                }
                else {
                    m_Stat = new ClassificationStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(AttributeUseType.Target));
                }
                break;

            case ClusStatManager.MODE_REGRESSION:
                m_Stat = new RegressionStat(statmgr.getSettings(), statmgr.getSchema().getNumericAttrUse(AttributeUseType.Target));
                break;

            case ClusStatManager.MODE_CLASSIFY_AND_REGRESSION:
                m_Stat = statmgr.getStatistic(AttributeUseType.Target); //FIXME: Probably all statistics could be initialized like this? i.e., there is no need for checking mode?
                break;

            case ClusStatManager.MODE_HIERARCHICAL:
                if (statmgr.getSettings().getHMLC().getHierSingleLabel()) {
                    m_Stat = new HierSingleLabelStat(statmgr.getSettings(), statmgr.getHier());
                }
                else {
                    m_Stat = new WHTDStatistic(statmgr.getSettings(), statmgr.getHier());
                }
                break;

            case ClusStatManager.MODE_PHYLO:
                m_Stat = new GeneticDistanceStat(statmgr.getSettings(), statmgr.getSchema().getNominalAttrUse(AttributeUseType.Target));
                break;

            default:
                System.err.println(getClass().getName() + " initForest(): Error initializing the statistic " + statmgr.getMode());
                break;
        }

        m_AppName = statmgr.getSettings().getGeneric().getFileAbsolute(statmgr.getSettings().getGeneric().getAppName());
//        m_AttributeList = "";
        m_DescriptiveIndex = ClusUtil.getDiscriptiveAttributesIndices(statmgr);
        m_Optimization = opt;

    }

    public HashMap<String, Integer> getDescriptiveIndices() {
    	return m_DescriptiveIndex;
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


    @Override
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException, ClusException {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.applyModelProcessors(tuple, mproc);
        }
    }


    @Override
    public void attachModel(HashMap table) throws ClusException {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.attachModel(table);
        }
    }


    @Override
    public int getID() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public int getDescriptiveIndex(String name){
        return m_DescriptiveIndex.get(name);
    }


    public int getNbModels() throws InterruptedException {
        m_NbModelsLock.readingLock();
        int ans = m_NbModels;
        m_NbModelsLock.readingUnlock();
        return ans;
    }


    private void increaseNbModels(int n) throws InterruptedException {
        m_NbModelsLock.writingLock();
        m_NbModels += n;
        m_NbModelsLock.writingUnlock();

    }


    private int getNbNodes() throws InterruptedException {
        m_NbNodesLock.readingLock();
        int ans = m_NbNodes;
        m_NbNodesLock.readingUnlock();
        return ans;
    }


    private void increaseNbNodes(int n) throws InterruptedException {
        m_NbNodesLock.writingLock();
        m_NbNodes += n;
        m_NbNodesLock.writingUnlock();
    }


    private int getNbLeaves() throws InterruptedException {
        m_NbLeavesLock.readingLock();
        int ans = m_NbLeaves;
        m_NbLeavesLock.readingUnlock();
        return ans;
    }


    private void increaseNbLeaves(int n) throws InterruptedException {
        m_NbLeavesLock.writingLock();
        m_NbLeaves += n;
        m_NbLeavesLock.writingUnlock();
    }


    public static int[] countNodesLeaves(ClusNode model) {
        int models = 1;
        int nodes = model.getNbNodes();
        int leaves = model.getNbLeaves();
        //updateCounts(models, nodes, leaves);
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
     * @throws InterruptedException 
     */
    public void updateCounts(int nbModels, int nbNodes, int nbLeaves) throws InterruptedException {
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


    @Override
    public String getModelInfo() throws InterruptedException {
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

        String result = String.format("FOREST with %d models (Total nodes: %d and leaves: %d)%s", getNbModels(), getNbNodes(), getNbLeaves(), targetSubspaces);

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


    @Override
    public int getModelSize() {
        return m_Forest.size(); // Maybe something else ?!
    }


    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException, InterruptedException {
        if (getSettings().getEnsemble().isEnsembleROSEnabled()) {
            switch (getSettings().getEnsemble().getEnsembleROSVotingType()) {
                case SubspaceAveraging: // only use subspaces for prediction averaging
                    return ClusEnsembleInduce.isOptimized() ? predictWeightedStandardSubspaceAveragingOpt(tuple) : predictWeightedStandardSubspaceAveraging(tuple);

                case SmarterWay:
                    throw new RuntimeException("NOT YET IMPLEMENTED!");

                case TotalAveraging:
                default:
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


    public ClusStatistic predictWeightedStandard(DataTuple tuple) throws ClusException, InterruptedException {
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
     * @throws InterruptedException 
     */
    public ArrayList getOOBVotes(DataTuple tuple) throws InterruptedException {
        if (ClusOOBErrorEstimate.containsPredictionForTuple(tuple)) { return ClusOOBErrorEstimate.getVotesForTuple(tuple); }

        return null;
    }


    public boolean containsOOBForTuple(DataTuple tuple) throws InterruptedException {
        return ClusOOBErrorEstimate.containsPredictionForTuple(tuple);
    }


    public ClusStatistic predictWeightedOOB(DataTuple tuple) throws ClusException, InterruptedException {

        if (ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_HIERARCHICAL || ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_REGRESSION)
            return predictWeightedOOBRegressionHMC(tuple);
        if (ClusEnsembleInduce.m_Mode == ClusStatManager.MODE_CLASSIFY)
            return predictWeightedOOBClassification(tuple);

        System.err.println(this.getClass().getName() + ".predictWeightedOOB(DataTuple) - Error in Setting the Mode");
        return null;
    }


    public ClusStatistic predictWeightedOOBRegressionHMC(DataTuple tuple) throws ClusException, InterruptedException {
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
            m_Stat = m_Stat;
        else
            m_Stat = m_Stat;
        m_Stat.computePrediction();
        return m_Stat;
    }


    public ClusStatistic predictWeightedOOBClassification(DataTuple tuple) throws ClusException, InterruptedException {
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


    public ClusStatistic predictWeightedOpt(DataTuple tuple) throws ClusException { // TODO: paralelno pazi matejp
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
     * @throws ClusException 
     */
    public ClusStatistic predictWeightedStandardAndGetProximities(DataTuple tuple) throws ClusException {
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
     * @throws ClusException 
     * @throws InterruptedException 
     */
    public ClusStatistic predictWeightedOOBAndGetProximities(DataTuple tuple) throws ClusException, InterruptedException {
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
                        m_Proximities.put(tupleIndex, m_Proximities.get(tupleIndex) + incrementValue);
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


    /** used for ROS ensembles 
     * @throws ClusException 
     * @throws InterruptedException */
    public ClusStatistic predictWeightedStandardSubspaceAveraging(DataTuple tuple) throws ClusException, InterruptedException {
        ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
        for (int i = 0; i < m_Forest.size(); i++) {
            votes.add(m_Forest.get(i).predictWeighted(tuple));
        }
        m_Stat.vote(votes, m_TargetSubspaceInfo);
        ClusEnsemblePredictionWriter.setVotes(votes);
        return m_Stat;
    }


    /** used for OPTIMIZED ROS ensembles 
     * @throws ClusException */
    public ClusStatistic predictWeightedStandardSubspaceAveragingOpt(DataTuple tuple) throws ClusException {
        // when ensembles are optimized, the running average takes into account the subspaces, so predictWeightedOpt() should return correct results

        return predictWeightedOpt(tuple);
    }


    @Override
    public void printModel(PrintWriter wrt) throws InterruptedException {
        // This could be better organized
        if (getSettings().getEnsemble().isPrintEnsembleModels()) {
            ClusModel model;
            for (int i = 0; i < m_Forest.size(); i++) {
                model = m_Forest.get(i);
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


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) throws InterruptedException {
        // This could be better organized
        if (getSettings().getEnsemble().isPrintEnsembleModels()) {
            ClusModel model;
            for (int i = 0; i < m_Forest.size(); i++) {
                model = m_Forest.get(i);
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


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.printModelAndExamples(wrt, info, examples);
        }
    }

    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> dict) {
        printForestToPython(PythonModelType.Function);
    }
    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> dict, String modelIdentifier) {
    	printModelToPythonScript(wrt, dict);
    }
    

    /**
     * Adds an ensemble predictor to python models file.
     * 
     */
    public void writePythonEnsembleFile(PrintWriter wrtr, String treeFile, PythonModelType pyModelType) {
        wrtr.println(String.format("def ensemble_%d(xs):", m_NbModels));
		wrtr.println(String.format("\tbase_predictions = [None for _ in range(%d)]", m_NbModels));
		wrtr.println("\tfor i in range(len(base_predictions)):");
		wrtr.println(String.format("\t\ttree = eval(\"%s.tree_{}\".format(i + 1))", treeFile));
		switch (pyModelType) {
		case Function:
			wrtr.println("\t\tbase_predictions[i] = tree(xs)");
			break;
		case Object:
			wrtr.println("\t\tbase_predictions[i] = tree.predict(xs)");
			break;
		default:
			throw new RuntimeException("This will never happen:)");
		}
		wrtr.println("\treturn aggregate(base_predictions)");
		wrtr.println();
		wrtr.println();
    }
    
    public static String getTreeFile(String appName) {
    	return ClusUtil.fileName(appName) + m_PythonFileTreePattern;
    }
    
       
    public static void writePythonAggregation(PrintWriter wrtr, String treeFile, int mode) {
    	wrtr.println(String.format("import %s", treeFile));
        wrtr.print("\n\n"); // two empty lines
    	String aggregation;
        switch(mode) {
        	case ClusStatManager.MODE_CLASSIFY:
        		aggregation = 
            	   "def aggregate(predictions):\n" + 
            		"    n = len(predictions)\n" + 
            		"    m = len(predictions[0])\n" + 
            		"    counts = [{} for _ in range(m)]\n" + 
            		"    for i in range(n):\n" + 
            		"        for j in range(m):\n" + 
            		"            pred = predictions[i][j]\n" + 
            		"            if pred not in counts[j]:\n" + 
            		"                counts[j][pred] = 0\n" + 
            		"            counts[j][pred] += 1\n" + 
            		"    return [max(counts[j], key=lambda pred: counts[j][pred]) for j in range(m)]";
        	   break;
        	case ClusStatManager.MODE_REGRESSION:
        		aggregation =
        			"def aggregate(predictions):\n" + 
        			"    n = len(predictions)\n" + 
        			"    m = len(predictions[0])\n" + 
        			"    sums = [0 for _ in range(m)]\n" + 
        			"    for i in range(n):\n" + 
        			"        for j in range(m):\n" + 
        			"            sums[j] += predictions[i][j]\n" + 
        			"    return [sums[j] / n for j in range(m)]";
        		break;
        	default:
        		System.err.println("Unsupported mode, you will have to write your own aggregation function.");
        		aggregation =
        			"def aggregate(predictions):\n" +
        			"    return None";
        }
        wrtr.println(aggregation);
        wrtr.println();
        wrtr.println();
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
    
    public static String pythonTreeFunctionDefinition(int treeIndex) {
    	return "def tree_" + treeIndex + "(xs):";
    }
    
    private String getPythonForestTreesFileName() {
    	return m_AppName + m_PythonFileTreePattern + ".py";
    }

    /**
     * Prints the trees of the forest into a python file.
     */
    public void printForestToPython(PythonModelType type) {
        try {
            File pyscript = new File(getPythonForestTreesFileName());
            PrintWriter wrtr = new PrintWriter(new FileOutputStream(pyscript));
            wrtr.println("# Python code of the trees in the ensemble");
            wrtr.println();
            if (type == PythonModelType.Object) {
            	wrtr.println("from tree_as_object import *\n\n");
            }
            for (int i = 0; i < m_Forest.size(); i++) {
                ClusModel model = m_Forest.get(i);
                printOneTree(wrtr, (ClusNode) model, i + 1, type);
            }
            wrtr.close();
            System.out.println(String.format("Python trees for the model %s written to: ", getModelInfo()) + pyscript.getPath());
        }
        catch (IOException e) {
            System.err.println(this.getClass().getName() + ".printForestToPython(): Error while writing models to python script");
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    
    public void printOneTree(PrintWriter wrtr, ClusNode tree, int treeIndex, PythonModelType type) {
    	wrtr.println("# Model " + treeIndex);
    	switch(type) {
    	case Function:
    		wrtr.println(pythonTreeFunctionDefinition(treeIndex));
            tree.printModelToPythonScript(wrtr, m_DescriptiveIndex);
            break;
    	case Object:
    		tree.printModelToPythonScript(wrtr, m_DescriptiveIndex, Integer.toString(treeIndex));
    		break;
    	default:
    		throw new RuntimeException("Wrong PythonModelType: " + type);
    	}        
    }
    
    
    /**
     * Used when optimization of ensembles is ON. Reads the temporary files and joins them into one file,
     * so that the output is the same as in the case of {@link #printForestToPython()}.
     * @param cr
     */
    public void joinPythonForestInOneFile(ClusRun cr) {
    	File pyscript = new File(getPythonForestTreesFileName());
        PrintWriter wrtr;
		try {
			wrtr = new PrintWriter(new FileOutputStream(pyscript));
			wrtr.println("# Python code of the trees in the ensemble");
		    wrtr.println(); 
		    if (cr.getStatManager().getSettings().getOutput().getPythonModelType() == PythonModelType.Object) {
            	wrtr.println("from tree_as_object import *\n\n");
            }
		    for(int i = 1; i <= m_NbModels; i++) {
		    	String inputFile = ClusEnsembleInduce.getTemporaryPythonTreeFileName(cr, i);
		    	String treeString = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
		    	wrtr.print(treeString);
		    	// delete temporary file
		    	File temp = new File(inputFile);
		    	if(! temp.delete()) {
		    		System.err.println("Warning: the file " + temp + " was not deleted.");
		    	}
		    }
		    wrtr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean ex) {
        // TODO Auto-generated method stub
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            model = m_Forest.get(i);
            model.printModelToQuery(wrt, cr, starttree, startitem, ex);
        }
    }


    @Override
    public ClusModel prune(int prunetype) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void retrieveStatistics(ArrayList list) {
        // TODO Auto-generated method stub
    }


    public void showForest() {
        ClusModel model;
        for (int i = 0; i < m_Forest.size(); i++) {
            System.out.println("***************************");
            model = m_Forest.get(i);
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
        return m_Forest.get(idx);
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
    public ClusForest cloneForestWithThreshold(double threshold) throws InterruptedException {
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
     * @throws InterruptedException 
     * @throws ClusException
     * @throws IOException
     */
    public void convertToRules(ClusRun cr, boolean addOnlyUnique) throws InterruptedException, ClusException {
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

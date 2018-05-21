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

package si.ijs.kt.clus.algo.tdidt;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.Global;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.processor.ClusModelProcessor;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.selection.OOBSelection;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.UniqueNodeIdentifier;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.jeans.tree.MyNode;
import si.ijs.kt.clus.util.jeans.util.MyArray;
import si.ijs.kt.clus.util.jeans.util.StringUtils;
import si.ijs.kt.clus.util.jeans.util.compound.IntObject;


public class ClusNode extends MyNode implements ClusModel {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public final static int YES = 0;
    public final static int NO = 1;
    public final static int UNK = 2;

    public int m_ID;
    public NodeTest m_Test;
    public ClusStatistic m_ClusteringStat;
    public ClusStatistic m_TargetStat;
    public transient Object m_Visitor;
    public long m_Time;
    public NodeTest[] m_Alternatives; // contains all alternatives to m_Test that give the same split
    public NodeTest[] m_OppositeAlternatives; // contains all alternatives to m_Test that give the opposite split
    public String m_AlternativesString; // contains a string of true and opposite alternatives, sorted according to
                                        // attribute number
                                        // - adde by Jurica Levatic (E8_IJS), March 2014
    public List<Integer> m_LeafTuples; //contains hash codes of tuples which ended in a leaf
    // - end added by Jurica
    private UniqueNodeIdentifier m_UniqueNodeIdentifier; // matejp: used because hashCode & equals do not form a considerable good team for leaves
    
    
    private void setUniqueNodeIdentifier(int value) {
    	m_UniqueNodeIdentifier = new UniqueNodeIdentifier(value);
    }
    
    private UniqueNodeIdentifier getUniqueNodeIdentifier() {
		return m_UniqueNodeIdentifier;
	}


    @Override
    public MyNode cloneNode() throws ClusException {
        ClusNode clone = new ClusNode();
        clone.m_Test = m_Test;
        clone.m_ClusteringStat = m_ClusteringStat;

        if (m_TargetStat == null)
            clone.m_TargetStat = m_TargetStat;
        else {
            // Celine replaced previous line by following three lines (07/10/2011)
            // reason: subtree raising in C45 pruning did not output correct nb of examples in each leaf of the original
            // tree
            clone.m_TargetStat = m_TargetStat.cloneStat();
            clone.m_TargetStat.copy(m_TargetStat);
            clone.m_TargetStat.calcMean();
        }

        clone.m_Alternatives = m_Alternatives;
        clone.m_OppositeAlternatives = m_OppositeAlternatives;
        clone.m_AlternativesString = m_AlternativesString;
        return clone;
    }


    private String getHorizontalLineText() {
        String corner = "+";
        String dash = "-";
        return corner + dash + dash;
    }


    private String getVerticalLineText() {
        return "|";
    }


    private String getSpaces(int howmany) {
        StringBuilder sb = new StringBuilder(howmany);
        for (int i = 0; i < howmany; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }


    private String getSpacesNo() {
        return getSpaces(7);
    }


    public String getSpacesYes() {
        return getSpaces(6);
    }


    public String getSpacesUnk() {
        return getSpacesNo();
    }


    public ClusNode cloneNodeWithVisitor() throws ClusException {
        ClusNode clone = (ClusNode) cloneNode();
        clone.setVisitor(getVisitor());
        return clone;
    }


    public final ClusNode cloneTreeWithVisitors(ClusNode n1, ClusNode n2) throws ClusException {
        if (n1 == this) {
            return n2;
        }
        else {
            ClusNode clone = (ClusNode) cloneNode();
            clone.setVisitor(getVisitor());
            int arity = getNbChildren();
            clone.setNbChildren(arity);
            for (int i = 0; i < arity; i++) {
                ClusNode node = (ClusNode) getChild(i);
                clone.setChild(node.cloneTreeWithVisitors(n1, n2), i);
            }
            return clone;
        }
    }


    public final ClusNode cloneTreeWithVisitors() throws ClusException {
        ClusNode clone = (ClusNode) cloneNode();
        clone.setVisitor(getVisitor());
        int arity = getNbChildren();
        clone.setNbChildren(arity);
        for (int i = 0; i < arity; i++) {
            ClusNode node = (ClusNode) getChild(i);
            clone.setChild(node.cloneTreeWithVisitors(), i);
        }
        return clone;
    }


    public void inverseTests() {
        if (getNbChildren() == 2) {
            setTest(getTest().getBranchTest(ClusNode.NO));
            ClusNode ch1 = (ClusNode) getChild(0);
            ClusNode ch2 = (ClusNode) getChild(1);
            ch1.inverseTests();
            ch2.inverseTests();
            setChild(ch2, 0);
            setChild(ch1, 1);
        }
        else {
            for (int i = 0; i < getNbChildren(); i++) {
                ClusNode node = (ClusNode) getChild(i);
                node.inverseTests();
            }
        }
    }


    public ClusNode[] getChildren() {
        ClusNode[] temp = new ClusNode[m_Children.size()];
        for (int i = 0; i < m_Children.size(); i++)
            temp[i] = (ClusNode) getChild(i);
        return temp;
    }


    public double checkTotalWeight() {
        if (atBottomLevel()) {
            return getClusteringStat().getTotalWeight();
        }
        else {
            double sum = 0.0;
            for (int i = 0; i < getNbChildren(); i++) {
                ClusNode child = (ClusNode) getChild(i);
                sum += child.checkTotalWeight();
            }
            if (Math.abs(getClusteringStat().getTotalWeight() - sum) > 1e-6) {
                System.err.println("ClusNode::checkTotalWeight() error: " + getClusteringStat().getTotalWeight() + " <> " + sum);
            }
            return sum;
        }
    }


    public final void setVisitor(Object visitor) {
        m_Visitor = visitor;
    }


    public final Object getVisitor() {
        return m_Visitor;
    }


    public final void clearVisitors() {
        m_Visitor = null;
        int arity = getNbChildren();
        for (int i = 0; i < arity; i++) {
            ClusNode child = (ClusNode) getChild(i);
            child.clearVisitors();
        }
    }


    @Override
    public final int getID() {
        return m_ID;
    }

    @Override
    public boolean equals(Object other) {
        ClusNode o = (ClusNode) other;
        if (m_Test != null && o.m_Test != null) {  		// two internal nodes: same tests and same children
            if (!m_Test.equals(o.m_Test)) {
            	return false;
            }
        }
        else if (m_Test != null || o.m_Test != null) {  // internal node is not equal to a leaf
            	return false;
        } else {										// two leaves should give the at least same predictions, but this is not good enough for use in HashMap!
        	System.err.println("Warning: you are comparing two leaves. This always results in true.");
        }
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            if (!getChild(i).equals(o.getChild(i)))
                return false;
        }
        return true;
    } 

    
    @Override
    public int hashCode() {
        int hashCode = 1234;
        if (m_Test != null) {
            hashCode += m_Test.hashCode();
        }
        else {
            hashCode += 4567;
        }
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            hashCode += getChild(i).hashCode();
        }
        return hashCode;
    }


    /***************************************************************************
     * Inspectors concerning test
     ***************************************************************************/

    public final boolean hasBestTest() {
        return m_Test != null;
    }


    public final NodeTest getTest() {
        return m_Test;
    }


    public final void setTest(NodeTest test) {
        m_Test = test;
    }


    public final String getTestString() {
        return m_Test != null ? m_Test.getString() : "None";
    }


    public final void testToNode(CurrentBestTestAndHeuristic best) {
        setTest(best.updateTest());
    }


    @Override
    public int getModelSize() {
        return getNbNodes();
    }


    @Override
    public String getModelInfo() {
        return "Nodes = " + getNbNodes() + " (Leaves: " + getNbLeaves() + ")";
    }


    public final boolean hasUnknownBranch() {
        return m_Test.hasUnknownBranch();
    }


    public NodeTest[] getAlternatives() {
        return m_Alternatives;
    }


    public NodeTest[] getOppositeAlternatives() {
        return m_OppositeAlternatives;
    }


    /***************************************************************************
     * Insprectors concenring statistics
     ***************************************************************************/

    public final ClusStatistic getClusteringStat() {
        return m_ClusteringStat;
    }


    public final ClusStatistic getTargetStat() {
        return m_TargetStat;
    }


    public final double getTotWeight() {
        return m_ClusteringStat.getTotalWeight();
    }


    // Weight of unknown examples over total weight
    public final double getUnknownFreq() {
        return m_Test.getUnknownFreq();
    }


    /***************************************************************************
     * Mutators
     ***************************************************************************/

    public final void setClusteringStat(ClusStatistic stat) {
        m_ClusteringStat = stat;
    }


    public final void setTargetStat(ClusStatistic stat) {
        m_TargetStat = stat;
    }


    public final void computePrediction() throws ClusException {
        if (getClusteringStat() != null) {
            getClusteringStat().calcMean();
        }
        if (getTargetStat() != null) {
            getTargetStat().calcMean();
        }
    }


    public final int updateArity() {
        int arity = m_Test.updateArity();
        setNbChildren(arity);
        return arity;
    }


    public final ClusNode afterInduce(ClusStatManager mgr) throws ClusException {
        if (mgr == null) throw new RuntimeException("ClusStatManager = null.");

        // treshold optimization
        if (mgr.getSettings().getMLC().shouldRunThresholdOptimization()) { 
            optimizeTresholds();
        }
        else {
            updateTree();
        }

        safePrune();
        return this;
    }

    
    private void optimizeTresholds() throws ClusException {
        double lower = 0.0, upper = 1.0;
        double middle = lower + (upper - lower) / 2;
        int nbRelevantLabels = countTrueRelevant();
        int nbPredictedRelevantNow = -1;
        while (upper - lower > 0.0005) {
            middle = lower + (upper - lower) / 2;
            updateThresholds(middle);
            updateTree();
            nbPredictedRelevantNow = countPredictedRelevant();
            if (nbPredictedRelevantNow == nbRelevantLabels) {
                break;
            }
            else if (nbPredictedRelevantNow < nbRelevantLabels) { // threshold too high
                upper = middle;
            }
            else { // too low
                lower = middle;
            }
        }
        if (nbPredictedRelevantNow == nbRelevantLabels) {
            // nothing to do here, since the thresholds are already set
            // to optimal value and the tree is updated accordingly
        }
        else {
            double defaultThreshold = 0.5; // try the default threshold also: typically, this seems to be at least a close to optimal value
            double[] candidates = new double[] { defaultThreshold, lower, upper }; // default the first: we want to choose it if nothing is strictly better
            int optimalDiff = Integer.MAX_VALUE;
            double optimalThreshold = -10.0;
            for (double candidate : candidates) {
                updateThresholds(candidate);
                int currentCount = countPredictedRelevant();
                int currentDiff = Math.abs(currentCount - nbRelevantLabels);
                if (currentDiff < optimalDiff) {
                    optimalThreshold = candidate;
                    optimalDiff = currentDiff;
                }
            }
            updateThresholds(optimalThreshold);
            updateTree();
        }
    }

    public void updateThresholds(double threshold) {
        if (getTargetStat() instanceof ClassificationStat) {
            ClassificationStat targetStat = (ClassificationStat) getTargetStat();
            targetStat.setThresholds(threshold);
        }
        else if (getTargetStat() instanceof WHTDStatistic) {
            WHTDStatistic targetStat = (WHTDStatistic) getTargetStat();
            targetStat.setThresholds(threshold);
        }
        else {
            throw new RuntimeException("Unknown type of target statistics.");
        }
        if (m_Test != null) { // is not leaf
            int nbChildren = getNbChildren();
            ClusNode info;
            for (int i = 0; i < nbChildren; i++) {
                info = (ClusNode) getChild(i);
                info.updateThresholds(threshold);
            }
        }
    }


    public int countTrueRelevant() {
        int nbRelevantLabels = 0;
        if (getTargetStat() instanceof ClassificationStat) {
            ClassificationStat targetStat = (ClassificationStat) getTargetStat();
            for (int target = 0; target < targetStat.m_ClassCounts.length; target++) {
                nbRelevantLabels += targetStat.m_ClassCounts[target][0];
            }
        }
        else if (getTargetStat() instanceof WHTDStatistic) {
            WHTDStatistic targetStat = (WHTDStatistic) getTargetStat();
            boolean[] evalClasses = targetStat.getHier().getEvalClassesVector();
            double[] sumValues = targetStat.getSumValues();
            for (int i = 0; i < evalClasses.length; i++) {
                if (evalClasses[i]) {
                    nbRelevantLabels += sumValues[i];
                }
            }
        }
        else {
            throw new RuntimeException("Unknown type of target statistics.");
        }
        return nbRelevantLabels;
    }


    public int countPredictedRelevant() {
        int nbPredictedRelevant = 0;
        if (m_Test == null) { // is leaf
            if (getTargetStat() instanceof ClassificationStat) {
                ClassificationStat targetStat = (ClassificationStat) getTargetStat();
                for (int target = 0; target < targetStat.m_ClassCounts.length; target++) {
                    if (targetStat.m_ClassCounts[target][0] / targetStat.m_SumWeights[target] >= targetStat.m_Thresholds[target]) {
                        nbPredictedRelevant += (int) targetStat.m_SumWeights[target]; // TODO: does not work for weighted sum of examples!
                    }
                }
            }
            else if (getTargetStat() instanceof WHTDStatistic) {
                WHTDStatistic targetStat = (WHTDStatistic) getTargetStat();
                boolean[] evalClasses = targetStat.getHier().getEvalClassesVector();
                double[] sumValues = targetStat.getSumValues();
                double sumWeight = targetStat.getSumWeight();
                double[] thresholds = targetStat.getThresholds();
                for (int i = 0; i < evalClasses.length; i++) {
                    if (evalClasses[i] && sumValues[i] / sumWeight >= thresholds[i]) {
                        nbPredictedRelevant += sumWeight;
                    }
                }
            }
        }
        else {
            int nbChildren = getNbChildren();
            ClusNode info;
            for (int i = 0; i < nbChildren; i++) {
                info = (ClusNode) getChild(i);
                nbPredictedRelevant += info.countPredictedRelevant();
            }
        }
        return nbPredictedRelevant;
    }


    public final void cleanup() {
        if (m_ClusteringStat != null)
            m_ClusteringStat.setSDataSize(0);
        if (m_TargetStat != null)
            m_TargetStat.setSDataSize(0);
    }


    public void makeLeaf() {
        m_Test = null;
        cleanup();
        removeAllChildren();
    }


    public final void updateTree() throws ClusException {
        cleanup();
        computePrediction();
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            info.updateTree();
        }
    }


    public void setAlternatives(ArrayList<NodeTest> alt) {
        m_Alternatives = new NodeTest[alt.size()];
        for (int i = 0; i < alt.size(); i++)
            m_Alternatives[i] = alt.get(i);
    }


    public void setOppositeAlternatives(ArrayList<NodeTest> alt) {
        m_OppositeAlternatives = new NodeTest[alt.size()];
        for (int i = 0; i < alt.size(); i++)
            m_OppositeAlternatives[i] = alt.get(i);
    }


    public void setAlternativesString(String str) {
        m_AlternativesString = str;
    }


    /***************************************************************************
     * Code for safe package si.ijs.kt.clus.pruning the tree
     ***************************************************************************/

    // Test if two nodes predict the same
    public final boolean samePrediction(ClusNode other) {
        return m_TargetStat.samePrediction(other.m_TargetStat);
    }


    // Test if all children are leaves that predict the same
    public final boolean allSameLeaves() {
        int nb_c = getNbChildren();
        if (nb_c == 0)
            return false;
        ClusNode cr = (ClusNode) getChild(0);
        if (!cr.atBottomLevel())
            return false;
        for (int i = 1; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            if (!info.atBottomLevel())
                return false;
            if (!info.samePrediction(cr))
                return false;
        }
        return true;
    }


    public void pruneByTrainErr(ClusAttributeWeights scale) {
        if (!atBottomLevel()) {
            double errorsOfSubtree = estimateErrorAbsolute(scale);
            double errorsOfLeaf = getTargetStat().getError(scale);
            if (errorsOfSubtree >= errorsOfLeaf - 1E-3) {
                makeLeaf();
            }
            else {
                for (int i = 0; i < getNbChildren(); i++) {
                    ClusNode child = (ClusNode) getChild(i);
                    child.pruneByTrainErr(scale);
                }
            }
        }
    }


    // Safe prune this tree (using predictions in leaves)
    public final void safePrune() {
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            info.safePrune();
        }
        if (allSameLeaves()) {
            makeLeaf();
        }
    }


    public final boolean allInvalidLeaves() {
        int nb_c = getNbChildren();
        if (nb_c == 0)
            return false;
        for (int i = 0; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            if (!info.atBottomLevel())
                return false;
            if (info.getTargetStat().isValidPrediction())
                return false;
        }
        return true;
    }


    public final void pruneInvalid() {
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            info.pruneInvalid();
        }
        if (allInvalidLeaves())
            makeLeaf();
    }


    @Override
    public ClusModel prune(int prunetype) throws ClusException {
        if (prunetype == PRUNE_INVALID) {
            ClusNode pruned = (ClusNode) cloneTree();
            pruned.pruneInvalid();
            return pruned;
        }
        return this;
    }


    //    /***************************************************************************
    //     * Multi score code - this should be made more general!
    //     ***************************************************************************/
    //
    //    public final void multiScore(MultiScore score) {
    //        m_ClusteringStat = new MultiScoreStat(m_ClusteringStat, score);
    //        int nb_c = getNbChildren();
    //        for (int i = 0; i < nb_c; i++) {
    //            ClusNode info = (ClusNode) getChild(i);
    //            info.multiScore(score);
    //        }
    //    }

    /***************************************************************************
     * Code to attach another dataset to the tree
     ***************************************************************************/

    @Override
    public final void attachModel(HashMap table) throws ClusException {
        int nb_c = getNbChildren();
        if (nb_c > 0)
            m_Test.attachModel(table);
        for (int i = 0; i < nb_c; i++) {
            ClusNode info = (ClusNode) getChild(i);
            info.attachModel(table);
        }
    }


    /***************************************************************************
     * Code for making predictions
     * @throws ClusException 
     ***************************************************************************/

    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException {
        if (atBottomLevel()) {
            return getTargetStat();
        }
        else {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                ClusNode info = (ClusNode) getChild(n_idx);
                return info.predictWeighted(tuple);
            }
            else {
                int nb_c = getNbChildren();
                ClusNode ch_0 = (ClusNode) getChild(0);
                ClusStatistic ch_0s = ch_0.predictWeighted(tuple);
                ClusStatistic stat = ch_0s.cloneSimple();
                stat.addPrediction(ch_0s, m_Test.getProportion(0));
                for (int i = 1; i < nb_c; i++) {
                    ClusNode ch_i = (ClusNode) getChild(i);
                    ClusStatistic ch_is = ch_i.predictWeighted(tuple);
                    stat.addPrediction(ch_is, m_Test.getProportion(i));
                }
                stat.computePrediction();
                return stat;
            }
        }
    }


    public ClusStatistic clusterWeighted(DataTuple tuple) throws ClusException {
        if (atBottomLevel()) {
            return getClusteringStat();
        }
        else {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                ClusNode info = (ClusNode) getChild(n_idx);
                return info.clusterWeighted(tuple);
            }
            else {
                int nb_c = getNbChildren();
                ClusStatistic stat = getClusteringStat().cloneSimple();
                for (int i = 0; i < nb_c; i++) {
                    ClusNode node = (ClusNode) getChild(i);
                    ClusStatistic nodes = node.clusterWeighted(tuple);
                    stat.addPrediction(nodes, m_Test.getProportion(i));
                }
                stat.computePrediction();
                return stat;
            }
        }
    }


    /**
     * Store indices of tuples in leaf nodes. Used to
     * calculate RForest proximities (https://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#prox).
     * 
     * @param tuple
     */
    public void incrementProximities(DataTuple tuple) {
        if (atBottomLevel()) { //leaf
            if (m_LeafTuples == null) {
                m_LeafTuples = new LinkedList<Integer>();
            }
            if (!m_LeafTuples.contains(tuple.getIndex())) {
                m_LeafTuples.add(tuple.getIndex());
            }
        }
        else {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) { // normal
                ClusNode info = (ClusNode) getChild(n_idx);
                info.incrementProximities(tuple);
            }
            else { // has missing
                int nb_c = getNbChildren();
                for (int i = 0; i < nb_c; i++) {
                    ClusNode ch_i = (ClusNode) getChild(i);
                    ch_i.incrementProximities(tuple);
                }
            }
        }
    }


    /**
     * Make prediction and store indices of tuples which ended in
     * the same leaf as the given tuple in the leafTuples variable.
     * This is used to calculate RForest proximities
     * https://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#prox
     * 
     * @param tuple
     *        Tuple to predict
     * @param leafTuples
     *        hash codes of tuples which are in the same leaf where
     *        the tuple ended will be stored in this variable.

     * @throws ClusException 
     */
    public ClusStatistic predictWeightedAndGetLeafTuples(DataTuple tuple, List<Integer> leafTuples) throws ClusException {
        if (atBottomLevel()) {
            if (this.m_LeafTuples != null) {
                leafTuples.addAll(this.m_LeafTuples);
            }

            return getTargetStat();
        }
        else {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                ClusNode info = (ClusNode) getChild(n_idx);
                return info.predictWeightedAndGetLeafTuples(tuple, leafTuples);
            }
            else {
                int nb_c = getNbChildren();
                ClusNode ch_0 = (ClusNode) getChild(0);
                ClusStatistic ch_0s = ch_0.predictWeightedAndGetLeafTuples(tuple, leafTuples);
                ClusStatistic stat = ch_0s.cloneSimple();
                stat.addPrediction(ch_0s, m_Test.getProportion(0));
                for (int i = 1; i < nb_c; i++) {
                    ClusNode ch_i = (ClusNode) getChild(i);
                    ClusStatistic ch_is = ch_i.predictWeightedAndGetLeafTuples(tuple, leafTuples);
                    stat.addPrediction(ch_is, m_Test.getProportion(i));
                }
                stat.computePrediction();
                return stat;
            }
        }
    }


    public final void applyModelProcessor(DataTuple tuple, ClusModelProcessor proc) throws IOException, ClusException {
        int nb_c = getNbChildren();
        if (nb_c == 0 || proc.needsInternalNodes())
            proc.modelUpdate(tuple, this);
        if (nb_c != 0) {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                ClusNode info = (ClusNode) getChild(n_idx);
                info.applyModelProcessor(tuple, proc);
            }
            else {
                for (int i = 0; i < nb_c; i++) {
                    ClusNode node = (ClusNode) getChild(i);
                    double prop = m_Test.getProportion(i);
                    node.applyModelProcessor(tuple.multiplyWeight(prop), proc);
                }
            }
        }
    }


    @Override
    public final void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException, ClusException {
        int nb_c = getNbChildren();
        for (int i = 0; i < mproc.size(); i++) {
            ClusModelProcessor proc = (ClusModelProcessor) mproc.elementAt(i);
            if (nb_c == 0 || proc.needsInternalNodes())
                proc.modelUpdate(tuple, this);
        }
        if (nb_c != 0) {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                ClusNode info = (ClusNode) getChild(n_idx);
                info.applyModelProcessors(tuple, mproc);
            }
            else {
                for (int i = 0; i < nb_c; i++) {
                    ClusNode node = (ClusNode) getChild(i);
                    double prop = m_Test.getProportion(i);
                    node.applyModelProcessors(tuple.multiplyWeight(prop), mproc);
                }
            }
        }
    }


    /***************************************************************************
     * Change the total statistic of the tree?
     * @throws ClusException 
     ***************************************************************************/

    public final void initTargetStat(ClusStatManager smgr, RowData subset) throws ClusException {
        m_TargetStat = smgr.createTargetStat();
        subset.calcTotalStatBitVector(m_TargetStat);
    }


    public final void initClusteringStat(ClusStatManager smgr, RowData subset) throws ClusException {
        m_ClusteringStat = smgr.createClusteringStat();
        subset.calcTotalStatBitVector(m_ClusteringStat);
    }


    public final void initTargetStat(ClusStatManager smgr, ClusStatistic train, RowData subset) throws ClusException {
        m_TargetStat = smgr.createTargetStat();
        m_TargetStat.setTrainingStat(train);
        subset.calcTotalStatBitVector(m_TargetStat);
    }


    public final void initClusteringStat(ClusStatManager smgr, ClusStatistic train, RowData subset) throws ClusException {
        m_ClusteringStat = smgr.createClusteringStat();
        m_ClusteringStat.setTrainingStat(train);
        subset.calcTotalStatBitVector(m_ClusteringStat);
    }


    public final void reInitTargetStat(RowData subset) throws ClusException {
        if (m_TargetStat != null) {
            m_TargetStat.reset();
            subset.calcTotalStatBitVector(m_TargetStat);
        }
    }


    /*
     * public final void reInitTargetStat(RowData subset) {
     * if (m_TargetStat != null) {
     * ClusStatistic st = m_TargetStat.cloneStat();
     * st.reset();
     * subset.calcTotalStatBitVector(st);
     * m_TargetStat = st;
     * }
     * }
     */

    public final void reInitClusteringStat(RowData subset) throws ClusException {
        if (m_ClusteringStat != null) {
            m_ClusteringStat.reset();
            subset.calcTotalStatBitVector(m_ClusteringStat);
        }
    }


    public final void initTotStats(ClusStatistic stat) {
        m_ClusteringStat = stat.cloneStat();
        int nb_c = getNbChildren();
        for (int i = 0; i < nb_c; i++) {
            ClusNode node = (ClusNode) getChild(i);
            node.initTotStats(stat);
        }
    }


    public final void numberTree() {
        numberTree(new IntObject(1, null));
    }


    public final void numberTree(IntObject count) {
        int arity = getNbChildren();
        if (arity > 0) {
            m_ID = 0;
            for (int i = 0; i < arity; i++) {
                ClusNode child = (ClusNode) getChild(i);
                child.numberTree(count);
            }
        }
        else {
            m_ID = count.getValue();
            count.incValue();
        }
    }


    public final void numberCompleteTree() {
        numberCompleteTree(new IntObject(1, null));
    }


    public final void numberCompleteTree(IntObject count) {
        m_ID = count.getValue();
        count.incValue();
        int arity = getNbChildren();
        for (int i = 0; i < arity; i++) {
            ClusNode child = (ClusNode) getChild(i);
            child.numberCompleteTree(count);
        }
    }


    /*
     * Returns the total number of nodes (incl leaves) in the tree rooted at this node
     */
    public final int getTotalTreeSize() {
        int childrensize = 0;
        int arity = getNbChildren();
        for (int i = 0; i < arity; i++) {
            ClusNode child = (ClusNode) getChild(i);
            childrensize += child.getTotalTreeSize();
        }
        return (childrensize + 1);
    }


    public final void addChildStats() throws ClusException {
        int nb_c = getNbChildren();
        if (nb_c > 0) {
            ClusNode ch0 = (ClusNode) getChild(0);
            ch0.addChildStats();
            ClusStatistic stat = ch0.getClusteringStat();
            ClusStatistic root = stat.cloneSimple();
            root.addPrediction(stat, 1.0);
            for (int i = 1; i < nb_c; i++) {
                ClusNode node = (ClusNode) getChild(i);
                node.addChildStats();
                root.addPrediction(node.getClusteringStat(), 1.0);
            }
            root.calcMean();
            setClusteringStat(root);
        }
    }


    public double estimateErrorAbsolute(ClusAttributeWeights scale) {
        return estimateErrorRecursive(this, scale);
    }


    public double estimateError(ClusAttributeWeights scale) {
        return estimateErrorRecursive(this, scale) / getTargetStat().getTotalWeight();
    }


    public double estimateClusteringSS(ClusAttributeWeights scale) {
        return estimateClusteringSSRecursive(this, scale);
    }


    public double estimateTargetSS(ClusAttributeWeights scale) {
        return estimateTargetSSRecursive(this, scale);
    }


    public double estimateClusteringVariance(ClusAttributeWeights scale) {
        return estimateClusteringSSRecursive(this, scale) / getClusteringStat().getTotalWeight();
    }


    public static double estimateClusteringSSRecursive(ClusNode tree, ClusAttributeWeights scale) {
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getClusteringStat();
            return total.getSVarS(scale);
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateClusteringSSRecursive(child, scale);
            }
            return result;
        }
    }


    public static double estimateTargetSSRecursive(ClusNode tree, ClusAttributeWeights scale) {
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getTargetStat();
            return total.getSVarS(scale);
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateTargetSSRecursive(child, scale);
            }
            return result;
        }
    }


    public static double estimateErrorRecursive(ClusNode tree, ClusAttributeWeights scale) {
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getTargetStat();
            return total.getError(scale);
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateErrorRecursive(child, scale);
            }
            return result;
        }
    }


    // if all the weight are equal to one
    // cpt count the number of leaf
    public static double estimateErrorRecursive(ClusNode tree) {
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getTargetStat();
            // System.out.println("CLUSNODE error at leaf is "+total.getErrorRel());
            return total.getError();
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateErrorRecursive(child);
            }
            return result;
        }
    }


    public int getNbLeaf() {
        int nbleaf = 0;
        if (atBottomLevel()) {
            nbleaf++;
        }
        else {
            for (int i = 0; i < getNbChildren(); i++) {
                ClusNode child = (ClusNode) getChild(i);
                nbleaf += child.getNbLeaf();
            }
        }
        return nbleaf;
    }


    /***************************************************************************
     * Printing the tree ?
     ***************************************************************************/

    // FIXME - what for NominalTests with only two possible outcomes?

    @Override
    public void printModel(PrintWriter wrt) {
        printTree(wrt, StatisticPrintInfo.getInstance(), "");
    }


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        printTree(wrt, info, "");
    }


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        printTree(wrt, info, "", examples, true);
    }

    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> dict) {
    	printTreeToPythonScriptIterative(wrt, "\t", dict);
        wrt.println();
        wrt.println();
    }
    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> dict, String name) {
    	printTreeToPythonTreeObject(wrt, dict, name);
        wrt.println();
        wrt.println();
    }

    @Override
    public JsonObject getModelJSON() {
        return getModelJSON(null);
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info) {
        return getModelJSON(info, null);
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples) {
        int arity = getNbChildren();
        if (arity > 0) {
            JsonObject node = new JsonObject();
            String testString = m_Test.getTestString();
            if (m_Alternatives != null) {
                for (int i = 0; i < m_Alternatives.length; i++) {
                    testString += " and " + m_Alternatives[i];
                }
            }
            node.addProperty("test_string", testString);
            JsonArray children = new JsonArray();
            node.add("children", children);
            StringWriter distributionStringWriter = new StringWriter();
            PrintWriter distributionWriter = new PrintWriter(distributionStringWriter);
            writeDistributionForInternalNode(distributionWriter, info);
            node.addProperty("distribution", distributionStringWriter.toString());
            if (examples != null) {
                node.add("summary", examples.getSummaryJSON());
            }
            if (m_TargetStat == null) {
                node.addProperty("target_stat", "?");
            }
            else {
                node.addProperty("target_stat", m_TargetStat.getString(info));
            }

            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) {

                RowData examples0 = null;
                RowData examples1 = null;
                if (examples != null) {
                    if ((m_Alternatives != null) || (m_OppositeAlternatives != null)) {
                        // in the case of alternative tests, the classification is done based on how many of the total
                        // tests predict left or right branch
                        examples0 = examples.applyAllAlternativeTests(m_Test, m_Alternatives, m_OppositeAlternatives, 0);
                        examples1 = examples.applyAllAlternativeTests(m_Test, m_Alternatives, m_OppositeAlternatives, 1);
                    }
                    else {
                        examples0 = examples.apply(m_Test, 0);
                        examples1 = examples.apply(m_Test, 1);
                    }
                }

                JsonObject yes_branch = ((ClusNode) getChild(YES)).getModelJSON(info, examples0);
                yes_branch.addProperty("branch_label", "Yes");
                JsonObject no_branch = ((ClusNode) getChild(NO)).getModelJSON(info, examples1);
                no_branch.addProperty("branch_label", "No");
                children.add(yes_branch);
                children.add(no_branch);
                if (hasUnknownBranch()) {
                    JsonObject unk_branch = ((ClusNode) getChild(UNK)).getModelJSON(info, examples0);
                    children.add(unk_branch);
                }

            }
            else {
                for (int i = 0; i < arity; i++) {
                    ClusNode child = (ClusNode) getChild(i);
                    String branchlabel = m_Test.getBranchLabel(i);
                    RowData examplesi = null;
                    if (examples != null) {
                        examplesi = examples.apply(m_Test, i);
                    }
                    JsonObject ch = child.getModelJSON(info, examplesi);
                    ch.addProperty("branch_label", branchlabel);
                    children.add(ch);
                }
            }
            return node;
        }
        else {// on the leaves
            JsonObject leaf = new JsonObject();
            if (m_TargetStat == null) {
                leaf.addProperty("target_stat", "?");
            }
            else {
                leaf.addProperty("target_stat", m_TargetStat.getString(info));
            }
            if (getID() != 0 && info.SHOW_INDEX) {
                leaf.addProperty("id", getID());
            }
            if (info.SHOW_KEY && examples != null && examples.getNbRows() > 0)
                leaf.addProperty("example_ids", examples.printIDs(""));
            if (examples != null && examples.getNbRows() > 0) {
                leaf.add("summary", examples.getSummaryJSON());
            }

            return leaf;
        }
    }


    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
        int lastmodel = cr.getNbModels() - 1;
        System.out.println("The number of models to print is:" + lastmodel);
        String[][] tabitem = new String[lastmodel + 1][10000]; // table of item
        int[][] tabexist = new int[lastmodel + 1][10000]; // table of booleen for each item
        Global.set_treecpt(starttree);
        Global.set_itemsetcpt(startitem);
        //ClusModelInfo m = cr.getModelInfo(0);// cr.getModelInfo(lastmodel);

        if (exhaustive) {
            for (int i = 0; i < cr.getNbModels(); i++) {
                ClusModelInfo mod = cr.getModelInfo(i);
                ClusNode tree = (ClusNode) cr.getModel(i);
                if (tree.getNbChildren() != 0) {
                    tree.printTreeInDatabase(wrt, tabitem[i], tabexist[i], 0, "all_trees");
                }
                // print the statitistics here (the format depend on the needs of the plsql program)
                if (tree.getNbNodes() <= 1) { // we only look for the majority class in the data
                    double error_rate = (tree.m_ClusteringStat).getErrorRel();
                    wrt.println("#" + (tree.m_ClusteringStat).getPredictedClassName(0));
                    wrt.println(mod.getModelSize() + ", " + error_rate + ", " + (1 - error_rate));
                }
                else {
                    // writer.println("INSERT INTO trees_charac VALUES(T1,"+size+error+accuracy+constraint);
                    wrt.println(mod.getModelSize() + ", " + (mod.m_TrainErr).getErrorClassif() + ", " + (mod.m_TrainErr).getErrorAccuracy());
                }
                Global.inc_treecpt();
            } // end for
        } // end if
        else { // greedy search
            ClusModelInfo mod = cr.getModelInfo(lastmodel);
            ClusNode tree = (ClusNode) cr.getModel(lastmodel);
            tabitem[lastmodel][0] = "null";
            tabexist[lastmodel][0] = 1;
            wrt.println("INSERT INTO trees_sets VALUES(" + Global.get_itemsetcpt() + ", '" + tabitem[lastmodel][0] + "', " + tabexist[lastmodel][0] + ")");
            wrt.println("INSERT INTO greedy_trees VALUES(" + Global.get_treecpt() + ", " + Global.get_itemsetcpt() + ",1)");
            Global.inc_itemsetcpt();
            if (tree.getNbChildren() != 0) {
                printTreeInDatabase(wrt, tabitem[lastmodel], tabexist[lastmodel], 1, "greedy_trees");
            }
            wrt.println("INSERT INTO trees_charac VALUES(" + Global.get_treecpt() + ", " + mod.getModelSize() + ", " + (mod.m_TrainErr).getErrorClassif() + ", " + (mod.m_TrainErr).getErrorAccuracy() + ", NULL)");
            Global.inc_treecpt();
        }
    }


    public final void printTree() {
        PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
        printTree(wrt, StatisticPrintInfo.getInstance(), "");
        wrt.flush();
    }


    /**
     * 
     * @param writer
     * @param treeIndex
     */
    public void printMultiLabelThresholds(PrintWriter writer, int treeIndex) {
        double[] thresholds;
        if (m_TargetStat instanceof ClassificationStat) {
            thresholds = ((ClassificationStat) m_TargetStat).m_Thresholds;
        }
        else if (m_TargetStat instanceof WHTDStatistic) {
            thresholds = ((WHTDStatistic) m_TargetStat).getThresholds();
        }
        else {
            throw new RuntimeException("Wrong type of target statistics.");
        }
        if (treeIndex >= 0) {
            writer.print(String.format("Tree %s: ", treeIndex + 1));
        }
        if (thresholds != null) {
            writer.print("[");
            for (int i = 0; i < thresholds.length; i++) {
                writer.print(ClusFormat.FOUR_AFTER_DOT.format(thresholds[i]) + (i == thresholds.length - 1 ? "]" + System.lineSeparator() : ", "));
            }
        }
        else {
            writer.print("null" + System.lineSeparator());
        }

    }


    public final void writeDistributionForInternalNode(PrintWriter writer, StatisticPrintInfo info) {
        if (info.INTERNAL_DISTR) {
            if (m_TargetStat != null) {
                writer.print(": " + m_TargetStat.getString(info));
            }
        }
        writer.println();
    }


    public final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix) {
        printTree(writer, info, prefix, null, true);
    }


    public final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix, RowData examples, boolean is_root) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) {
                writer.print(m_Test.getTestString());
                showAlternatives(writer);

                RowData examples0 = null;
                RowData examples1 = null;
                if (examples != null) {
                    if ((m_Alternatives != null) || (m_OppositeAlternatives != null)) {
                        // in the case of alternative tests, the classification is done based on how many of the total
                        // tests predict left or right branch
                        examples0 = examples.applyAllAlternativeTests(m_Test, m_Alternatives, m_OppositeAlternatives, 0);
                        examples1 = examples.applyAllAlternativeTests(m_Test, m_Alternatives, m_OppositeAlternatives, 1);
                    }
                    else {
                        examples0 = examples.apply(m_Test, 0);
                        examples1 = examples.apply(m_Test, 1);
                    }
                }

                writeDistributionForInternalNode(writer, info);
                //writer.print(prefix + "+--yes: ");
                writer.print(prefix + getHorizontalLineText() + "yes: ");
                //((ClusNode) getChild(YES)).printTree(writer, info, prefix + "|       ", examples0, false);
                ((ClusNode) getChild(YES)).printTree(writer, info, prefix + getVerticalLineText() + getSpacesYes(), examples0, false);
                //writer.print(prefix + "+--no:  ");
                writer.print(prefix + getHorizontalLineText() + "no:  ");

                if (hasUnknownBranch()) {
                    //((ClusNode) getChild(NO)).printTree(writer, info, prefix + "|       ", examples1, false);
                    ((ClusNode) getChild(NO)).printTree(writer, info, prefix + getVerticalLineText() + getSpacesNo(), examples1, false);
                    //writer.print(prefix + "+--unk: ");

                    writer.print(prefix + getHorizontalLineText() + "unk: ");
                    //((ClusNode) getChild(UNK)).printTree(writer, info, prefix + "        ", examples0, false);
                    ((ClusNode) getChild(UNK)).printTree(writer, info, prefix + getSpacesUnk(), examples0, false);
                }
                else {
                    // ((ClusNode) getChild(NO)).printTree(writer, info, prefix + "        ", examples1, false);
                    ((ClusNode) getChild(NO)).printTree(writer, info, prefix + getSpacesNo(), examples1, false);
                }
            }
            else {
                writer.println(m_Test.getTestString());
                for (int i = 0; i < arity; i++) {
                    ClusNode child = (ClusNode) getChild(i);
                    String branchlabel = m_Test.getBranchLabel(i);
                    RowData examplesi = null;
                    if (examples != null) {
                        examples.apply(m_Test, i);
                    }
                    //writer.print(prefix + "+--" + branchlabel + ": ");
                    writer.print(prefix + getHorizontalLineText() + branchlabel + ": ");
                    String suffix = StringUtils.makeString(' ', branchlabel.length() + 4);
                    if (i != arity - 1) {
                        //child.printTree(writer, info, prefix + "|" + suffix, examplesi, false);
                        child.printTree(writer, info, prefix + getVerticalLineText() + suffix, examplesi, false);
                    }
                    else {
                        child.printTree(writer, info, prefix + " " + suffix, examplesi, false);
                    }
                }
            }
        }
        else {// on the leaves
            if (m_TargetStat == null) {
                writer.print("?");
            }
            else {
                writer.print(m_TargetStat.getString(info));
            }
            if (getID() != 0 && info.SHOW_INDEX)
                writer.println(" (" + getID() + ")");
            else
                writer.println();
            if (info.SHOW_KEY && examples != null && examples.getNbRows() > 0)
                writer.println(prefix + "ExampleIDs: " + examples.printIDs(prefix));
            else if (examples != null && examples.getNbRows() > 0) {
                // writer.println(examples.toString(prefix));
                writer.println(prefix + "Summary:");
                writer.println(examples.getSummary(prefix));
            }
        }
    }


    /*
     * Prints for each example the path that is followed in the tree, both with node identifiers, and in boolean format
     * (used in ICDM 2011 paper on "random forest feature induction")
     * only binary trees are supported
     */
    public final void printPaths(PrintWriter writer, String pathprefix, String numberprefix, RowData examples, OOBSelection oob_sel, boolean testset) {
        String newnumberprefix;
        if (numberprefix.equals("")) {
            newnumberprefix = "" + getID();
        }
        else {
            newnumberprefix = numberprefix + "_" + getID();
        }
        int arity = getNbChildren();
        if (arity > 0) {
            if (arity == 2) {

                RowData examples0 = null;
                RowData examples1 = null;
                RowData examplesMin1 = null;
                if (examples != null) {
                    examples0 = examples.apply(m_Test, 0);
                    examples1 = examples.apply(m_Test, 1);
                    examplesMin1 = examples.apply(m_Test, -1); // ook -1 testen en die toevoegen aan zowel examples0 en
                                                               // examples1 voor missingvalues
                }
                examples0.add(examplesMin1);
                examples1.add(examplesMin1);
                ((ClusNode) getChild(YES)).printPaths(writer, pathprefix + "0", newnumberprefix, examples0, oob_sel, testset);
                ((ClusNode) getChild(NO)).printPaths(writer, pathprefix + "1", newnumberprefix, examples1, oob_sel, testset);

            }
            else {
                System.out.println("PrintPaths error: only binary trees supported");
            }
        }
        else { // at the leaves
            if (examples != null) {
                for (int i = 0; i < examples.getNbRows(); i++) {
                    int exampleindex = examples.getTuple(i).getIndex();
                    if (testset) {
                        writer.println(exampleindex + "  " + pathprefix + " " + newnumberprefix + "  TEST");
                    }
                    else if (oob_sel != null) {
                        boolean oob = oob_sel.isSelected(exampleindex);
                        if (oob) {
                            writer.println(exampleindex + "  " + pathprefix + " " + newnumberprefix + "  OOB");
                        }
                        else
                            writer.println(exampleindex + "  " + pathprefix + " " + newnumberprefix);
                    }
                    else
                        writer.println(exampleindex + "  " + pathprefix + " " + newnumberprefix);
                    writer.flush();
                }
            }

        }
    }


    /* to print the tree directly into an IDB : Elisa Fromont 13/06/2007 */
    public final void printTreeInDatabase(PrintWriter writer, String tabitem[], int tabexist[], int cpt, String typetree) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) { // the tree is binary
                // in case the test is postive
                tabitem[cpt] = m_Test.getTestString();
                tabexist[cpt] = 1;
                cpt++;
                ((ClusNode) getChild(YES)).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                cpt--;// to remove the last test on the father : now the test is negative
                // in ca se the test is negative
                tabitem[cpt] = m_Test.getTestString();
                // System.out.println("cpt = "+cpt+", tabitem = "+tabitem[cpt]);
                tabexist[cpt] = 0;
                cpt++;
                if (hasUnknownBranch()) {
                    ((ClusNode) getChild(NO)).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);

                    ((ClusNode) getChild(UNK)).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                }
                else {
                    ((ClusNode) getChild(NO)).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                }
            } // end if arity- delta ==2

            else { // arity -delta =/= 2 the tree is not binary
                   // Has not beeen modified for databse purpose yet !!!!!!
                writer.println("arity-delta different 2");
                for (int i = 0; i < arity; i++) {
                    ClusNode child = (ClusNode) getChild(i);
                    String branchlabel = m_Test.getBranchLabel(i);
                    writer.print("+--" + branchlabel + ": ");
                    if (i != arity - 1) {
                        child.printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                    }
                    else {
                        child.printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                    }
                } // end for
            } // end else arity -delta =/= 2
        } // end if arity >0 0

        else {// if arity =0 : on a leaf
            if (m_TargetStat == null) {
                writer.print("?");
            }
            else {
                tabitem[cpt] = m_TargetStat.getPredictedClassName(0);
                tabexist[cpt] = 1;
                writer.print("#"); // nb leaf
                for (int i = 0; i <= (cpt - 1); i++) {
                    writer.print(printTestNode(tabitem[i], tabexist[i]) + ", ");
                }
                writer.println(printTestNode(tabitem[cpt], tabexist[cpt]));
                cpt++;
            }
        } // end else if arity =0

    }


    public String printTestNode(String a, int pres) {
        if (pres == 1) {
            return a;
        }
        else {
            return ("not(" + a + ")");
        }
    }


    public final void printTreeToPythonScript(PrintWriter writer, String prefix, HashMap<String, Integer> dict) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) {
                int index = dict.get(m_Test.getType().getName());
                String atrNameReplacement = String.format("xs[%d]", index);
                String testString = "if " + m_Test.getPythonTestString(atrNameReplacement) + ":";
                writer.println(prefix + testString);
                ((ClusNode) getChild(YES)).printTreeToPythonScript(writer, prefix + "\t", dict);
                writer.println(prefix + "else:");
                if (hasUnknownBranch()) {
                    System.err.println("has unknown branch");
                    writer.println(prefix + "Let there be syntax errors. This is unknown branch!");
                }
                else {
                    ((ClusNode) getChild(NO)).printTreeToPythonScript(writer, prefix + "\t", dict);
                }
            }
            else {
            	System.err.println("arity - delta != 2 becauese arity: " + arity + " and delta: " + delta);
                writer.println(prefix + "Let there be syntax errors. Situation: here arity - delta != 2");
            }
        }
        else {
            if (m_TargetStat != null) {
                writer.println(prefix + "return " + m_TargetStat.getArrayOfStatistic());
                // System.out.println(m_TargetStat.getClass());
            } else {
            	System.err.println("m_TargetStat == null");
            	writer.println(prefix + "return null, because m_TargetStat is null");
            }
        }
    }
    
    
    /**
     * The iterative version of the method for printing Python code. This should be called only at the root node of the tree.
     * Two stages:<br>
     * <ul>
     * <li>create hash map node: number of visits, of all nodes in the tree </li>
     * <li>perform depth first search and print Python trees</li>
     * </ul>
     * @param writer
     * @param prefix
     * @param dict
     */
    public final void printTreeToPythonScriptIterative(PrintWriter writer, String prefix, HashMap<String, Integer> dict) {
    	// Hash map node: number of visits
    	HashMap<UniqueNodeIdentifier, Integer> tree_nodes = new HashMap<UniqueNodeIdentifier, Integer>();
    	int identifier = 0;
    	Stack<ClusNode> temp = new Stack<>();
    	temp.push(this);
    	ClusNode current;
    	while(!temp.isEmpty()) {
    		current = temp.pop();
    		current.setUniqueNodeIdentifier(identifier);
    		identifier++;
    		if (tree_nodes.containsKey(current.getUniqueNodeIdentifier())) {
    			System.out.println(current + " exists");
    		}
    		tree_nodes.put(current.getUniqueNodeIdentifier(), 0);
    		int children = current.getNbChildren();
    		if(children != 0 && children != 2) {
    			System.err.println("Warning: not a binary tree! Python code won't work properly with high probability.");
    		}
    		for(int i = 0; i < children; i++) {
    			temp.push((ClusNode) current.getChild(i));
    		}
     	}
    	// print the code
    	current = this;
    	int current_indentation = prefix.length() - prefix.replace("\t", "").length();
    	while(current != null) {
    		int arity = current.getNbChildren();
    		String current_prefix = StringUtils.makeString('\t', current_indentation);
    		if (arity > 0) {
    			// internal node
    			int visits = tree_nodes.get(current.getUniqueNodeIdentifier()).intValue();
    			tree_nodes.put(current.getUniqueNodeIdentifier(), visits + 1);
        		if (visits == 0) {
        			// print if test and follow the YES branch
        			String atrNameReplacement = String.format("xs[%d]", dict.get(current.m_Test.getType().getName()));
                    String testString = "if " + current.m_Test.getPythonTestString(atrNameReplacement) + ":";
                    writer.println(current_prefix + testString);
                    current = (ClusNode) current.getChild(YES);
                    current_indentation++;
        		} else if(visits == 1) {
        			// print else and follow the NO branch
        			writer.println(current_prefix + "else:");
        			current = (ClusNode) current.getChild(NO);
        			current_indentation++;
        		} else if(visits == 2) {
        			// go up
        			current = (ClusNode) current.m_Parent;
                    current_indentation--;
        		} else {
        			System.err.println("Warning: more than two visits of the node " + this.toString());
        		}
    		} else {
    			// leaf
                if (m_TargetStat != null) {
                    writer.println(current_prefix + "return " + current.m_TargetStat.getArrayOfStatistic());
                } else {
                	System.err.println("m_TargetStat == null");
                	writer.println(current_prefix + "return null, because m_TargetStat is null");
                }
                // go up
                current = (ClusNode) current.m_Parent;
                current_indentation--;
    		}
    	}
    }
    
        
    public final void printTreeToPythonTreeObject(PrintWriter writer, HashMap<String, Integer> dict, String modelIdentifier) {
    	HashMap<UniqueNodeIdentifier, Integer> children_left = new HashMap<UniqueNodeIdentifier, Integer>();  // {nodeID: number of children that have not been processed yet, ...}
    	int identifier = 0;
    	Stack<ClusNode> temp = new Stack<>();
    	temp.push(this);
    	ClusNode current;
    	while(!temp.isEmpty()) {
    		current = temp.pop();
    		current.setUniqueNodeIdentifier(identifier);
    		identifier++;
    		if (children_left.containsKey(current.getUniqueNodeIdentifier())) {
    			System.out.println(current + " exists");
    		}
    		int children = current.getNbChildren();
    		if(children != 0 && children != 2) {
    			System.err.println("Warning: not a binary tree! Python code won't work properly with high probability.");
    		}
    		children_left.put(current.getUniqueNodeIdentifier(), children);
    		for(int i = 0; i < children; i++) {
    			temp.push((ClusNode) current.getChild(i));
    		}
     	}
    	temp = new Stack<>();
    	temp.push(this);
    	while(!temp.isEmpty()) {
    		current = temp.peek();
    		UniqueNodeIdentifier currentID = current.getUniqueNodeIdentifier();
    		boolean isProcessed = false;
    		if(current.atBottomLevel()) {
    			// write leaf line
    			writer.println(pythonTreeLeafNodeLine(current, modelIdentifier));
    			isProcessed = true;
    		} else if(children_left.get(currentID) == 0){
    			// write internal line
    			writer.println(pythonTreeInternalNodeLine(current, modelIdentifier, dict.get(current.m_Test.getType().getName())));
    			isProcessed = true;
    		} else if (children_left.get(currentID) == current.getNbChildren()){
    			// process children first
    			for (int i = 0; i < current.getNbChildren(); i++) {
    				temp.push((ClusNode) current.getChild(i));
    			}
    		} else {
    			System.err.println("printTreeToPythonTree:\n    Probably something went wrong: the #processed children is neither #children nor 0!");
    		}
    		if (isProcessed) {
    			if (current.getParent() != null) {
    				UniqueNodeIdentifier parentID = ((ClusNode) current.getParent()).getUniqueNodeIdentifier(); 
    				int previous = children_left.get(parentID);
    				children_left.put(parentID, previous - 1);
    			}
    			temp.pop();
    		}
    	}
    	writer.println(String.format("tree_%s = Tree(%s)", modelIdentifier, pythonNodeName(this, modelIdentifier)));
    }
    
   private static String pythonNodeName(ClusNode node, String modelIdentifier) {
	   return String.format("node_%s_%d", modelIdentifier, node.getUniqueNodeIdentifier().getID());
   }
    
   private static String pythonTreeInternalNodeLine(ClusNode node, String modelIdentifier, int inputAttrIndex) {
	   String[] children = new String[node.getNbChildren()];
	   for(int i = 0; i < children.length; i++) {
		   children[i] = pythonNodeName((ClusNode) node.getChild(i), modelIdentifier);
	   }
	   String childrenArg = String.format("children=%s", Arrays.toString(children));
	   String frequenciesArg = String.format("branch_frequencies=%s", Arrays.toString(node.getTest().getProportions()));
	   String testArg = String.format("test=BinaryNodeTest(%d, test_function=lambda t: t%s)", inputAttrIndex, node.m_Test.getPythonTestString(""));
	   return String.format("%s = TreeNode(%s, %s, %s)", pythonNodeName(node, modelIdentifier), childrenArg, frequenciesArg, testArg);
   }
    
    private String pythonTreeLeafNodeLine(ClusNode leaf, String modelIdentifier) {
    	String statArg;
    	if (m_TargetStat instanceof RegressionStat) {
    		statArg = String.format("prediction_statistics=RegressionStat(%s)", leaf.m_TargetStat.getArrayOfStatistic());
    	} else {
    		throw new RuntimeException("Python code for your target-type statistics not implemented.");
    	}
    	return String.format("%s = TreeNode(%s)", pythonNodeName(leaf, modelIdentifier), statArg);
    }



    public final void showAlternatives(PrintWriter writer) {
        if (m_AlternativesString != null) {
            writer.print(m_AlternativesString);
        }
    }


    @Override
    public String toString() {
        try {
            if (hasBestTest())
                return getTestString();
            else
                return m_TargetStat.getSimpleString();
        }
        catch (Exception e) {
            return "null clusnode ";
        }
    }


    /**
     * Returns the majority class for this node.(not so good name)
     */
    public ClusStatistic predictWeightedLeaf(DataTuple tuple) {
        return getTargetStat();
    }


    @Override
    public void retrieveStatistics(ArrayList<ClusStatistic> list) {
        if (m_ClusteringStat != null)
            list.add(m_ClusteringStat);
        if (m_TargetStat != null)
            list.add(m_TargetStat);
        int arity = getNbChildren();
        for (int i = 0; i < arity; i++) {
            ClusNode child = (ClusNode) getChild(i);
            child.retrieveStatistics(list);
        }
    }


    public int getLargestBranchIndex() {
        double max = 0.0;
        int max_idx = -1;
        for (int i = 0; i < getNbChildren(); i++) {
            ClusNode child = (ClusNode) getChild(i);
            double child_w = child.getTotWeight();
            if (ClusUtil.grOrEq(child_w, max)) {
                max = child_w;
                max_idx = i;
            }
        }
        return max_idx;
    }


    public void adaptToData(RowData data) throws ClusException {
        // sort data into tree
        NodeTest tst = getTest();
        for (int i = 0; i < getNbChildren(); i++) {
            ClusNode child = (ClusNode) getChild(i);
            RowData subset = data.applyWeighted(tst, i);
            child.adaptToData(subset);
        }
        // recompute statistics
        reInitTargetStat(data);
        reInitClusteringStat(data);
    }
}

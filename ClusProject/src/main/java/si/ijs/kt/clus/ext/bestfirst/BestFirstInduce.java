/*************************************************************************
 * Clus - Software for Predictive Clustering -- *
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

package si.ijs.kt.clus.ext.bestfirst;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.algo.split.FindBestTest;
import si.ijs.kt.clus.algo.split.NominalSplit;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce.ParallelTrap;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble;
import si.ijs.kt.clus.main.settings.section.SettingsGeneric;
import si.ijs.kt.clus.main.settings.section.SettingsTree.TreeOptimizeValues;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.exception.ClusException;


public class BestFirstInduce extends ClusInductionAlgorithm {

    protected FindBestTest m_FindBestTest;
    protected ClusNode m_Root;


    public BestFirstInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
        super(schema, sett);
        m_FindBestTest = new FindBestTest(getStatManager());
    }


    public BestFirstInduce(ClusInductionAlgorithm other) {
        super(other);
        m_FindBestTest = new FindBestTest(getStatManager());
    }


    public BestFirstInduce(ClusInductionAlgorithm other, NominalSplit split) {
        super(other);
        m_FindBestTest = new FindBestTest(getStatManager(), split);
    }


    @Override
    public void initialize() throws ClusException, IOException {
        super.initialize();
    }


    public FindBestTest getFindBestTest() {
        return m_FindBestTest;
    }


    public CurrentBestTestAndHeuristic getBestTest() {
        return m_FindBestTest.getBestTest();
    }


    public boolean initSelectorAndStopCrit(ClusNode node, RowData data) {
        int max = getSettings().getConstraints().getTreeMaxDepth();
        if (max != -1 && node.getLevel() >= max) { return true; }
        return m_FindBestTest.initSelectorAndStopCrit(node.getClusteringStat(), data);
    }


    public ClusAttrType[] getDescriptiveAttributes() {
        /** FIXME: parallelisation problems: static methods + null argument */
        ClusEnsembleInduce.giveParallelisationWarning(ParallelTrap.BestFirst_getDescriptiveAttributes);

        ClusSchema schema = getSchema();
        SettingsEnsemble sett = getSettings().getEnsemble();

        if (!sett.isEnsembleMode()) {
            return schema.getDescriptiveAttributes();
        }
        else {
            switch (sett.getEnsembleMethod()) {
                case Bagging:
                    return schema.getDescriptiveAttributes();
                case RForest:
                    ClusAttrType[] attrsAll = schema.getDescriptiveAttributes();
                    // ClusEnsembleInduce.setRandomSubspaces(attrsAll, schema.getSettings().getNbRandomAttrSelected(),
                    // null);
                    ClusEnsembleInduce.setRandomSubspaces(ClusEnsembleInduce.selectRandomSubspaces(attrsAll, schema.getSettings().getEnsemble().getNbRandomAttrSelected(), ClusRandom.RANDOM_SELECTION, null));
                    return ClusEnsembleInduce.getRandomSubspaces();
                case RSubspaces:
                    return ClusEnsembleInduce.getRandomSubspaces();
                case BagSubspaces:
                    return ClusEnsembleInduce.getRandomSubspaces();
                case RFeatSelection: // SettingsEnsemble.ENSEMBLE_METHOD_RFOREST_NO_BOOTSTRAP:
                    ClusAttrType[] attrsAll1 = schema.getDescriptiveAttributes();
                    // ClusEnsembleInduce.setRandomSubspaces(attrsAll1, schema.getSettings().getNbRandomAttrSelected(),
                    // null);
                    ClusEnsembleInduce.setRandomSubspaces(ClusEnsembleInduce.selectRandomSubspaces(attrsAll1, schema.getSettings().getEnsemble().getNbRandomAttrSelected(), ClusRandom.RANDOM_SELECTION, null));
                    return ClusEnsembleInduce.getRandomSubspaces();
                default:
                    return schema.getDescriptiveAttributes();
            }
        }
    }


    public void filterAlternativeSplits(ClusNode node, RowData data, RowData[] subsets) {
        CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();

        int arity = node.getTest().updateArity();
        ArrayList v = best.getAlternativeBest(); // alternatives: all tests that result in same heuristic
                                                 // value
        for (int k = 0; k < v.size(); k++) {
            NodeTest nt = (NodeTest) v.get(k);
            int altarity = nt.updateArity();
            // remove alternatives that have different arity than besttest
            if (altarity != arity) {
                v.remove(k);
                k--;
                ClusLogger.info("Alternative split with different arity: " + nt.getString());
            }
            else {
                // arity altijd 2 hier
                // exampleindices van subset[0] bijhouden, van alle alternatives zowel van subset[0] als subset[1]
                // kijken of de indices gelijk zijn
                int nbsubset0 = subsets[0].getNbRows();
                int indices[] = new int[nbsubset0];
                for (int m = 0; m < nbsubset0; m++) {
                    indices[m] = subsets[0].getTuple(m).getIndex();
                }
                boolean same = false;
                for (int l = 0; l < altarity; l++) {
                    RowData altrd = data.applyWeighted(nt, l);
                    if (altrd.getNbRows() == nbsubset0) {
                        int nbsame = 0;
                        for (int m = 0; m < nbsubset0; m++) {
                            if (altrd.getTuple(m).getIndex() == indices[m]) {
                                nbsame++;
                            }
                        }
                        if (nbsame == nbsubset0) {
                            same = true;
                            if (l != 0) {
                                /*
                                 * we have the same subsets, but the opposite split, hence we change the test to
                                 * not(test)
                                 */
                                String test = v.get(k).toString();
                                String newtest = "not(" + test + ")";

                                v.set(k, new String(newtest));
                            }
                        }
                    }
                }
                if (!same) {
                    v.remove(k);
                    k--;
                    ClusLogger.info("Alternative split with different ex in subsets: " + nt.getString());
                }

            }
        }
        node.setAlternatives(v);
        // if (removed) ClusLogger.info("Alternative splits were possible");
    }


    public void makeLeaf(ClusNode node) {
        node.makeLeaf();
        if (getSettings().getTree().hasTreeOptimize(TreeOptimizeValues.NoClusteringStats)) {
            node.setClusteringStat(null);
        }
    }


    private int getBestNodeToSplit(ArrayList<ClusNode> listNodes) {

        int pos = 0;
        double best = Double.NEGATIVE_INFINITY;
        double valueHeuristic;

        for (int j = 0; j < listNodes.size(); j++) {

            NodeTest bestTestNode = listNodes.get(j).getTest();

            valueHeuristic = bestTestNode.getHeuristicValue();
            // System.out.print("\nCurrent: ");
            // System.out.print(valueHeuristic);
            // System.out.print("\n");
            if (best < valueHeuristic) {
                best = valueHeuristic;
                pos = j;
            }
        }

        return pos;
    }


    public void inducePre(ClusNode node, RowData data) throws Exception {

        ArrayList<ClusNode> listNodes = new ArrayList<ClusNode>();

        ArrayList<RowData> subsets = new ArrayList<RowData>();

        ArrayList<RowData> dataLeaves = new ArrayList<RowData>();

        subsets.add(0, data);
        dataLeaves.add(0, data);

        // Initialize selector and perform various stopping criteria
        if (initSelectorAndStopCrit(node, data)) {
            makeLeaf(node);
            return;
        }

        // Find best test
        ClusAttrType[] attrs = getDescriptiveAttributes();
        for (int i = 0; i < attrs.length; i++) {
            ClusAttrType at = attrs[i];
            if (at instanceof NominalAttrType)
                m_FindBestTest.findNominal((NominalAttrType) at, data, null);
            else
                m_FindBestTest.findNumeric((NumericAttrType) at, data, null);
        }

        // Partition data + recursive calls
        CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();

        if (best.hasBestTest()) {
            // start_time = System.currentTimeMillis();

            node.testToNode(best);

            listNodes.add(node);

            // EncodingCost EncCost = new EncodingCost(dataLeaves,attrs);

            // System.out.print("Encoding cost is \t\t\t\t\t"+EncCost.getEncodingCostValue()+"\n");

            induce(listNodes, subsets, dataLeaves, 0);

            // EncCost = new EncodingCost(dataLeaves,attrs);
            // System.out.print("Encoding cost is \t\t\t\t\t"+EncCost.getEncodingCostValue()+"\n");

        }
        else {
            makeLeaf(node);
        }

        return;

    }


    public void induce(ArrayList<ClusNode> listNodes, ArrayList<RowData> subsets, ArrayList<RowData> dataLeaves, int bestTestIndex) throws Exception {
        // ClusLogger.info("nonsparse induce");

        // System.out.print("Best node index is "+bestTestIndex+"\n");

        // Node to be extended, and data corresponding to the node
        ClusNode node = listNodes.get(bestTestIndex);
        RowData data = subsets.get(bestTestIndex);

        // Output best test
        if (getSettings().getGeneral().getVerbose() > 0)
            ClusLogger.info("Test: " + node.getTestString() + " -> " + node.getTest().getHeuristicValue());

        // Extending the node

        // arity has the number of children
        int arity = node.updateArity();

        // get test that was found to be the best one for the node
        NodeTest test = node.getTest();

        // Removing the node that has just been extended
        listNodes.remove(bestTestIndex);

        // Removing the data - it is stored in the local variable "data"
        subsets.remove(bestTestIndex);
        dataLeaves.remove(bestTestIndex); // we remove it because we are extending the node

        // Spliting data
        RowData[] subsetsLocal = new RowData[arity];
        for (int j = 0; j < arity; j++) {
            subsets.add(data.applyWeighted(test, j));
            dataLeaves.add(subsets.size() - 1, subsets.get(subsets.size() - 1));
            subsetsLocal[j] = subsets.get(subsets.size() - 1);
        }

        // >>>>> The best test needs to be calculated again, so that we can find the alternative tests

        // Initialize selector and perform various stopping criteria
        if (initSelectorAndStopCrit(node, data)) {
            makeLeaf(node); // this will never be the case because we have already tested it!!!!
        }

        // Find best test
        ClusAttrType[] attrs = getDescriptiveAttributes();

        for (int i = 0; i < attrs.length; i++) {
            ClusAttrType at = attrs[i];
            if (at instanceof NominalAttrType)
                m_FindBestTest.findNominal((NominalAttrType) at, data, null);
            else
                m_FindBestTest.findNumeric((NumericAttrType) at, data, null);
        }

        // in case we want to print alternative splits
        if (getSettings().getTree().showAlternativeSplits()) {
            filterAlternativeSplits(node, data, subsetsLocal);
        }

        // >>>>> finished the block where we are finding the alternative tests

        if (node != m_Root && getSettings().getTree().hasTreeOptimize(TreeOptimizeValues.NoInodeStats)) {
            // Don't remove statistics of root node; code below depends on them
            node.setClusteringStat(null);
            node.setTargetStat(null);
        }

        for (int j = 0; j < arity; j++) {

            // create a new node
            ClusNode child = new ClusNode();
            node.setChild(child, j);

            child.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), subsetsLocal[j]);
            child.initTargetStat(m_StatManager, m_Root.getTargetStat(), subsetsLocal[j]);

            // Initialize selector and perform various stopping criteria
            if (initSelectorAndStopCrit(child, subsetsLocal[j])) {
                subsets.remove(subsetsLocal[j]);

                // what we are doing here is moving the leaf node to the end of the list
                dataLeaves.remove(subsetsLocal[j]);
                dataLeaves.add(subsetsLocal[j]);

                makeLeaf(child);
            }
            else {

                // Find best test
                attrs = getDescriptiveAttributes();
                for (int i = 0; i < attrs.length; i++) {
                    ClusAttrType at = attrs[i];
                    if (at instanceof NominalAttrType)
                        m_FindBestTest.findNominal((NominalAttrType) at, subsetsLocal[j], null);
                    else
                        m_FindBestTest.findNumeric((NumericAttrType) at, subsetsLocal[j], null);
                }

                // find the best test
                CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();

                if (best.hasBestTest()) {
                    child.testToNode(best);
                    listNodes.add(child);
                }
                else {
                    subsets.remove(subsetsLocal[j]);

                    // what we are doing here is moving the leaf node to the end of the list
                    dataLeaves.remove(subsetsLocal[j]);
                    dataLeaves.add(subsetsLocal[j]);

                    makeLeaf(child);
                }
            }
        }

        // check if we still need to extend some node
        if (listNodes.size() > 0) {

            // CategoryUtility CU = new CategoryUtility(dataLeaves,attrs);

            // System.out.print("CU is \t\t\t\t\t"+CU.getCategoryUtilityValue()+"\n");

            // System.out.print("CU Modified is \t\t\t\t\t\t\t"+CU.getModifiedCategoryUtilityValue()+"\n");

            // EncodingCost EncCost = new EncodingCost(dataLeaves,attrs);

            // System.out.print("Encoding cost is \t\t\t\t\t"+EncCost.getEncodingCostValue()+"\n");

            bestTestIndex = getBestNodeToSplit(listNodes);
            induce(listNodes, subsets, dataLeaves, bestTestIndex);
        }

    }


    @Deprecated
    public void rankFeatures(ClusNode node, RowData data) throws Exception {
        // Find best test
        PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream("ranking.csv")));
        ClusAttrType[] attrs = getDescriptiveAttributes();
        for (int i = 0; i < attrs.length; i++) {
            ClusAttrType at = attrs[i];
            initSelectorAndStopCrit(node, data);
            if (at instanceof NominalAttrType)
                m_FindBestTest.findNominal((NominalAttrType) at, data, null);
            else
                m_FindBestTest.findNumeric((NumericAttrType) at, data, null);
            CurrentBestTestAndHeuristic cbt = m_FindBestTest.getBestTest();
            if (cbt.hasBestTest()) {
                NodeTest test = cbt.updateTest();
                wrt.print(cbt.m_BestHeur);
                wrt.print(",\"" + at.getName() + "\"");
                wrt.println(",\"" + test + "\"");
            }
        }
        wrt.close();
    }


    public void initSelectorAndSplit(ClusStatistic stat) throws ClusException {
        m_FindBestTest.initSelectorAndSplit(stat);
    }


    public void setInitialData(ClusStatistic stat, RowData data) throws ClusException {
        m_FindBestTest.setInitialData(stat, data);
    }


    public void cleanSplit() {
        m_FindBestTest.cleanSplit();
    }


    public ClusNode induceSingleUnpruned(RowData data) throws Exception {
        m_Root = null;
        // Begin of induction process
        // int nbr = 0;
        while (true) {
            // nbr++;
            // Init root node
            m_Root = new ClusNode();
            m_Root.initClusteringStat(m_StatManager, data);
            m_Root.initTargetStat(m_StatManager, data);
            m_Root.getClusteringStat().showRootInfo();
            initSelectorAndSplit(m_Root.getClusteringStat());
            setInitialData(m_Root.getClusteringStat(), data);
            // Induce the tree
            inducePre(m_Root, data);
            // rankFeatures(m_Root, data);
            // Refinement finished
            if (SettingsGeneric.EXACT_TIME == false)
                break;
        }
        m_Root.afterInduce(null);

        cleanSplit();
        return m_Root;
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws Exception {

        return induceSingleUnpruned((RowData) cr.getTrainingSet());
    }
}

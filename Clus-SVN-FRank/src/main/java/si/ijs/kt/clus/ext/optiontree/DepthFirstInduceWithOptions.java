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

package si.ijs.kt.clus.ext.optiontree;

import java.io.IOException;
import java.util.ArrayList;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsGeneric;
import si.ijs.kt.clus.main.settings.SettingsTree;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;


public class DepthFirstInduceWithOptions extends ClusInductionAlgorithm {

    public MyNode m_Root;

    public FindBestTests m_FindBestTests;


    //private int MODE = 0; // 0 = Kohavi, Kunz / 1 = Ikonomovska 

    private DepthFirstInduceWithOptions(ClusInductionAlgorithm other) {
        super(other);
    }


    public void initialize() throws ClusException, IOException {
        super.initialize();
    }


    public DepthFirstInduceWithOptions(ClusSchema schema, Settings sett) throws ClusException, IOException {
        super(schema, sett);
    }


    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
        return induceSingleUnpruned((RowData) cr.getTrainingSet());
    }


    private void makeLeaf(MyNode node) {
        node.makeLeaf();
        //node.m_TargetStat.calcMean(); // TODO WTH why do I need this?
        if (getSettings().getTree().hasTreeOptimize(SettingsTree.TREE_OPTIMIZE_NO_CLUSTERING_STATS)) {
            node.setClusteringStat(null);
        }
    }


    private void induce(ClusSplitNode node, RowData data) throws ClusException {
        if (initSelectorAndStopCrit(node, data)) {
            makeLeaf(node);
            return;
        }

        m_FindBestTests = new FindBestTests(m_StatManager);
        initSelectorAndSplit(m_Root.getClusteringStat());
        setInitialData(m_Root.getClusteringStat(), data);

        ClusAttrType[] attrs = getDescriptiveAttributes();
        for (int i = 0; i < attrs.length; i++) {
            ClusAttrType at = attrs[i];
            if (at instanceof NominalAttrType)
                m_FindBestTests.addBestNominalTest((NominalAttrType) at, data, node.getClusteringStat());
            else if (at instanceof NumericAttrType)
                m_FindBestTests.addBestNumericTest((NumericAttrType) at, data, node.getClusteringStat());
        }

        m_FindBestTests.sort();

        ArrayList<TestAndHeuristic> candidates = new ArrayList<TestAndHeuristic>();
        TestAndHeuristic bestTest = null;
        bestTest = m_FindBestTests.getBestTest();
        if (bestTest != null && bestTest.getHeuristicValue() != Double.NEGATIVE_INFINITY) {
            candidates.add(bestTest);
            ArrayList<TestAndHeuristic> bestTests = m_FindBestTests.getBestTests(getSettings().getOptionTree().getOptionMaxNumberOfOptionsPerNode());
            if (node.getLevel() < getSettings().getOptionTree().getOptionMaxDepthOfOptionNode()) {
                for (int i = 1; i < bestTests.size(); i++) {
                    TestAndHeuristic currentTest = bestTests.get(i);
                    if (currentTest.getHeuristicValue() / bestTest.getHeuristicValue() >= 1 - getSettings().getOptionTree().getOptionEpsilon() * Math.pow(getSettings().getOptionTree().getOptionDecayFactor(), node.getLevel())) {
                        candidates.add(currentTest);
                    }
                }
            }
        }

        if (candidates.size() == 0) {
            makeLeaf(node);
            return;
        }
        else if (candidates.size() == 1) {
            TestAndHeuristic best = m_FindBestTests.getBestTest();

            node.testToNode(best);
            // Output best test
            if (getSettings().getGeneric().getVerbose() > 0)
                System.out.println("Test: " + node.getTestString() + " -> " + best.getHeuristicValue());
            // Create children
            int arity = node.updateArity();
            NodeTest test = node.getTest();
            RowData[] subsets = new RowData[arity];
            for (int j = 0; j < arity; j++) {
                subsets[j] = data.applyWeighted(test, j);
            }

            if (node != m_Root && getSettings().getTree().hasTreeOptimize(SettingsTree.TREE_OPTIMIZE_NO_INODE_STATS)) {
                // Don't remove statistics of root node; code below depends on them
                node.setClusteringStat(null);
                node.setTargetStat(null);
            }

            for (int j = 0; j < arity; j++) {
                ClusSplitNode child = new ClusSplitNode();
                node.setChild(child, j);
                child.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), subsets[j]);
                child.initTargetStat(m_StatManager, m_Root.getTargetStat(), subsets[j]);
                induce(child, subsets[j]);
            }
        }
        else {
            ClusOptionNode optionNode = new ClusOptionNode();
            optionNode.setStatManager(m_StatManager);

            if (getSettings().getGeneric().getVerbose() > 0)
                System.out.println("New option node.");

            if (node != m_Root) {
                node.getParent().setChild(node.getParent().getChildIndex(node), optionNode);
            }
            else {
                optionNode.setClusteringStat(m_Root.getClusteringStat());
                optionNode.setTargetStat(m_Root.getTargetStat());
                m_Root = optionNode;
            }

            optionNode.setHeuristicRatios(new double[candidates.size()]);

            for (int i = 0; i < candidates.size(); i++) {
                TestAndHeuristic tnh = candidates.get(i);
                ClusSplitNode newNode = new ClusSplitNode();
                newNode.setStatManager(m_StatManager);
                optionNode.addChild(newNode);
                optionNode.setHeuristicRatio(i, tnh.getHeuristicValue() / bestTest.getHeuristicValue());
                newNode.testToNode(tnh);

                if (getSettings().getGeneric().getVerbose() > 0)
                    System.out.println("Test: " + newNode.getTestString() + " -> " + tnh.getHeuristicValue());

                newNode.setTest(tnh.updateTest());
                newNode.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), data);
                newNode.initTargetStat(m_StatManager, m_Root.getTargetStat(), data);

                int arity = newNode.updateArity();
                RowData[] subsets = new RowData[arity];
                for (int j = 0; j < arity; j++) {
                    subsets[j] = data.applyWeighted(newNode.getTest(), j);
                }

                for (int j = 0; j < arity; j++) {
                    ClusSplitNode child = new ClusSplitNode();
                    newNode.setChild(child, j);
                    child.setStatManager(m_StatManager);
                    child.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), subsets[j]);
                    child.initTargetStat(m_StatManager, m_Root.getTargetStat(), subsets[j]);
                    induce(child, subsets[j]);
                }
            }
        }
    }


    private ClusAttrType[] getDescriptiveAttributes() {
        ClusSchema schema = getSchema();
        return schema.getDescriptiveAttributes();
    }


    private MyNode induceSingleUnpruned(RowData data) throws ClusException, IOException {
        // Begin of induction process
        while (true) {
            // Init root node
            m_Root = new ClusSplitNode();
            m_Root.initClusteringStat(m_StatManager, data);
            m_Root.initTargetStat(m_StatManager, data);
            m_Root.getClusteringStat().showRootInfo();
            // Induce the tree
            induce((ClusSplitNode) m_Root, data);
            // rankFeatures(m_Root, data);
            // Refinement finished
            if (SettingsGeneric.EXACT_TIME == false)
                break;
        }

        return m_Root;
    }


    public void induceAll(ClusRun cr) throws ClusException, IOException {
        ClusModelInfo def_info = cr.addModelInfo(ClusModel.DEFAULT);
        def_info.setModel(ClusDecisionTree.induceDefault(cr));

        ClusModel model = induceSingleUnpruned(cr);
        ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL);
        model_info.setModel(model);
    }


    private boolean initSelectorAndStopCrit(ClusSplitNode node, RowData data) {
        // if (data.getSumWeights() <= 50) return true;
        int max = getSettings().getConstraints().getTreeMaxDepth();
        if (max != -1 && node.getLevel() >= max)
            return true;
        if (getSettings().getModel().getMinimalNbExamples() > 0 && data.getSumWeights() < getSettings().getModel().getMinimalNbExamples())
            return true;

        return false;
    }


    private void initSelectorAndSplit(ClusStatistic stat) throws ClusException {
        m_FindBestTests.initSelectorAndSplit(stat);
    }


    private void setInitialData(ClusStatistic stat, RowData data) throws ClusException {
        m_FindBestTests.setInitialData(stat, data);
    }
}

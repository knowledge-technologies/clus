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

/*
 * Created on May 1, 2005
 */

package si.ijs.kt.clus.algo.tdidt;

import java.io.IOException;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.multiscore.MultiScore;
import si.ijs.kt.clus.ext.constraint.ClusConstraintFile;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;


public class ConstraintDFInduce extends DepthFirstInduce {

    protected boolean m_FillIn;
    protected String m_ConstrFile;


    public ConstraintDFInduce(ClusSchema schema, Settings sett, boolean fillin) throws ClusException, IOException {
        super(schema, sett);
        m_FillIn = fillin;
        m_ConstrFile = sett.getConstraints().getConstraintFile();
    }


    public ConstraintDFInduce(ClusInductionAlgorithm other) {
        super(other, null);
    }


    public void fillInStatsAndTests(ClusNode node, RowData data) throws Exception {
        NodeTest test = node.getTest();
        if (test == null) {
            // No test, so this is a leaf node
            return;
        }
        if (!test.hasConstants()) {
            // no constants in test, find optimal split constant
            if (initSelectorAndStopCrit(node, data)) {
                node.makeLeaf();
                return;
            }
            ClusAttrType at = test.getType();
            if (at instanceof NominalAttrType)
                getFindBestTest().findNominal((NominalAttrType) at, data, null);
            else
                getFindBestTest().findNumeric((NumericAttrType) at, data, null);
            CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();
            if (best.hasBestTest()) {
                node.testToNode(best);
                if (getSettings().getGeneral().getVerbose() > 0)
                    System.out.println("Fill in Test: " + node.getTestString() + " -> " + best.getHeuristicValue());
            }
            else {
                node.makeLeaf();
                return;
            }
        }
        else {
            double tot_weight = 0.0;
            double unk_weight = 0.0;
            double tot_no_unk = 0.0;
            double[] branch_weight = new double[test.getNbChildren()];
            for (int i = 0; i < data.getNbRows(); i++) {
                DataTuple tuple = data.getTuple(i);
                int pred = test.predictWeighted(tuple);
                if (pred == NodeTest.UNKNOWN) {
                    unk_weight += tuple.getWeight();
                }
                else {
                    branch_weight[pred] += tuple.getWeight();
                    tot_no_unk += tuple.getWeight();
                }
                tot_weight += tuple.getWeight();
            }
            for (int i = 0; i < test.getNbChildren(); i++) {
                test.setProportion(i, branch_weight[i] / tot_no_unk);
            }
            test.setUnknownFreq(unk_weight / tot_weight);
        }
        NodeTest best_test = node.getTest();
        for (int j = 0; j < node.getNbChildren(); j++) {
            ClusNode child = (ClusNode) node.getChild(j);
            RowData subset = data.applyWeighted(best_test, j);
            child.initTargetStat(m_StatManager, subset);
            child.initClusteringStat(m_StatManager, subset);
            fillInStatsAndTests(child, subset);
        }
    }


    public void induceRecursive(ClusNode node, RowData data) throws Exception {
        if (node.atBottomLevel()) {
            induce(node, data, null); // PARALELNO: TODO ...
        }
        else {
            NodeTest test = node.getTest();
            for (int j = 0; j < node.getNbChildren(); j++) {
                ClusNode child = (ClusNode) node.getChild(j);
                RowData subset = data.applyWeighted(test, j);
                induceRecursive(child, subset);
            }
        }
    }


    public ClusNode createRootNode(RowData data, ClusStatistic cstat, ClusStatistic tstat) throws Exception {
        ClusConstraintFile file = ClusConstraintFile.getInstance();
        ClusNode root = file.getClone(m_ConstrFile);
        root.setClusteringStat(cstat);
        root.setTargetStat(tstat);
        fillInStatsAndTests(root, data);
        return root;
    }


    public ClusNode fillInInTree(RowData data, ClusNode tree, ClusStatistic cstat, ClusStatistic tstat) throws Exception {
        ClusNode root = tree.cloneTreeWithVisitors();
        root.setClusteringStat(cstat);
        root.setTargetStat(tstat);
        fillInStatsAndTests(root, data);
        return root;
    }


    public ClusNode fillInInduce(ClusRun cr, ClusNode node, MultiScore score) throws Exception {
        RowData data = (RowData) cr.getTrainingSet();
        ClusStatistic cstat = createTotalClusteringStat(data);
        ClusStatistic tstat = createTotalTargetStat(data);
        initSelectorAndSplit(cstat);
        ClusNode root = fillInInTree(data, node, cstat, tstat);
        root.afterInduce(getStatManager());
        cleanSplit();
        return root;
    }


    /*
     * public ClusNode induce(ClusRun cr, MultiScore score) throws ClusException {
     * RowData data = (RowData)cr.getTrainingSet();
     * ClusStatistic cstat = createTotalClusteringStat(data);
     * ClusStatistic tstat = createTotalTargetStat(data);
     * initSelectorAndSplit(cstat);
     * ClusNode root = createRootNode(data, cstat, tstat);
     * if (!m_FillIn) {
     * // Call induce on each leaf
     * induceRecursive(root, data);
     * }
     * root.postProc(score);
     * cleanSplit();
     * return root;
     * }
     */

    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws Exception {
        RowData data = (RowData) cr.getTrainingSet();
        ClusStatistic cstat = createTotalClusteringStat(data);
        ClusStatistic tstat = createTotalTargetStat(data);
        initSelectorAndSplit(cstat);
        ClusNode root = createRootNode(data, cstat, tstat);
        m_Root = root;
        if (!m_FillIn) {
            // Call induce on each leaf
            induceRecursive(root, data);
        }
        root.afterInduce(null);
        cleanSplit();
        return root;
    }
}

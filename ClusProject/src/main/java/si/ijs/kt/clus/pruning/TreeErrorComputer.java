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
 * Created on Jul 22, 2005
 */

package si.ijs.kt.clus.pruning;

import java.io.IOException;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.processor.ClusModelProcessor;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;


public class TreeErrorComputer extends ClusModelProcessor {

    public static void recursiveInitialize(ClusNode node, ErrorVisitor visitor) {
        /* Create array for each node */
        node.setVisitor(visitor.createInstance());
        /* Recursively visit children */
        for (int i = 0; i < node.getNbChildren(); i++) {
            ClusNode child = (ClusNode) node.getChild(i);
            recursiveInitialize(child, visitor);
        }
    }


    @Override
    public void modelUpdate(DataTuple tuple, ClusModel model) throws IOException, ClusException {
        ClusNode tree = (ClusNode) model;
        ErrorVisitor visitor = (ErrorVisitor) tree.getVisitor();
        visitor.testerr.addExample(tuple, tree.getTargetStat());
    }


    @Override
    public boolean needsModelUpdate() {
        return true;
    }


    @Override
    public boolean needsInternalNodes() {
        return true;
    }


    public static ClusError computeErrorOptimized(ClusNode tree, RowData test, ClusErrorList error, boolean miss) throws ClusException {
        error.reset();
        error.setNbExamples(test.getNbRows());
        ClusError child_err = error.getFirstError().getErrorClone();
        TreeErrorComputer.computeErrorOptimized(tree, test, child_err, miss);
        return child_err;
    }


    public static void computeErrorOptimized(ClusNode tree, RowData test, ClusError error, boolean miss) throws ClusException {
        // if (miss) {
        computeErrorStandard(tree, test, error);
        // } else {
        // computeErrorSimple(tree, error);
        // // Debug?
        // // ClusError clone = error.getErrorClone();
        // // computeErrorStandard(tree, test, clone);
        // // System.out.println("Simple = "+error.getModelError()+" standard = "+clone.getModelError());
        // }
    }


    public static ClusError computeClusteringErrorStandard(ClusNode tree, RowData test, ClusErrorList error) throws ClusException {
        error.reset();
        error.setNbExamples(test.getNbRows());
        ClusError child_err = error.getFirstError().getErrorClone();
        TreeErrorComputer.computeClusteringErrorStandard(tree, test, child_err);
        return child_err;
    }


    public static void computeClusteringErrorStandard(ClusNode tree, RowData test, ClusError error) throws ClusException {
        for (int i = 0; i < test.getNbRows(); i++) {
            DataTuple tuple = test.getTuple(i);
            ClusStatistic pred = tree.clusterWeighted(tuple);
            error.addExample(tuple, pred);
        }
    }


    public static void computeErrorStandard(ClusNode tree, RowData test, ClusError error) throws ClusException {
        for (int i = 0; i < test.getNbRows(); i++) {
            DataTuple tuple = test.getTuple(i);
            ClusStatistic pred = tree.predictWeighted(tuple);
            error.addExample(tuple, pred);
        }
    }


    public static void computeErrorNode(ClusNode node, RowData test, ClusError error) throws ClusException {
        ClusStatistic pred = node.getTargetStat();
        for (int i = 0; i < test.getNbRows(); i++) {
            DataTuple tuple = test.getTuple(i);
            error.addExample(tuple, pred);
        }
    }


    public static void initializeTestErrorsData(ClusNode tree, RowData test, ClusError error) throws IOException, ClusException {
        TreeErrorComputer comp = new TreeErrorComputer();
        initializeTestErrors(tree, error);
        for (int i = 0; i < test.getNbRows(); i++) {
            DataTuple tuple = test.getTuple(i);
            tree.applyModelProcessor(tuple, comp);
        }
    }


    public static void initializeTestErrors(ClusNode node, ClusError error) {
        ErrorVisitor visitor = (ErrorVisitor) node.getVisitor();
        visitor.testerr = error.getErrorClone(error.getParent());
        for (int i = 0; i < node.getNbChildren(); i++) {
            ClusNode child = (ClusNode) node.getChild(i);
            initializeTestErrors(child, error);
        }
    }


    public static void computeErrorSimple(ClusNode node, ClusError sum) {
        if (node.atBottomLevel()) {
            ErrorVisitor visitor = (ErrorVisitor) node.getVisitor();
            sum.add(visitor.testerr);
        }
        else {
            for (int i = 0; i < node.getNbChildren(); i++) {
                ClusNode child = (ClusNode) node.getChild(i);
                computeErrorSimple(child, sum);
            }
        }
    }
}

/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Created on Jul 22, 2005
 */
package clus.pruning;

import java.io.IOException;

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.error.*;
import clus.model.*;
import clus.model.processor.*;
import clus.statistic.*;

public class TreeErrorComputer extends ClusModelProcessor {

	public static void recursiveInitialize(ClusNode node, ErrorVisitor visitor) {
		/* Create array for each node */
		node.setVisitor(visitor.createInstance());
		/* Recursively visit children */
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			recursiveInitialize(child, visitor);
		}
	}

	public void modelUpdate(DataTuple tuple, ClusModel model) throws IOException {
		ClusNode tree = (ClusNode)model;
		ErrorVisitor visitor = (ErrorVisitor)tree.getVisitor();
		visitor.testerr.addExample(tuple, tree.getTargetStat());
	}

	public boolean needsModelUpdate() {
		return true;
	}

	public boolean needsInternalNodes() {
		return true;
	}

	public static ClusError computeErrorOptimized(ClusNode tree, RowData test, ClusErrorList error, boolean miss) {
		error.reset();
		error.setNbExamples(test.getNbRows());
		ClusError child_err = error.getFirstError().getErrorClone();
		TreeErrorComputer.computeErrorOptimized(tree, test, child_err, miss);
		return child_err;
	}

	public static void computeErrorOptimized(ClusNode tree, RowData test, ClusError error, boolean miss) {
//		if (miss) {
			computeErrorStandard(tree, test, error);
//		} else {
//			computeErrorSimple(tree, error);
//			// Debug?
//			// ClusError clone = error.getErrorClone();
//			// computeErrorStandard(tree, test, clone);
//			// System.out.println("Simple = "+error.getModelError()+" standard = "+clone.getModelError());
//		}
	}

	public static ClusError computeClusteringErrorStandard(ClusNode tree, RowData test, ClusErrorList error) {
		error.reset();
		error.setNbExamples(test.getNbRows());
		ClusError child_err = error.getFirstError().getErrorClone();
		TreeErrorComputer.computeClusteringErrorStandard(tree, test, child_err);
		return child_err;
	}

	public static void computeClusteringErrorStandard(ClusNode tree, RowData test, ClusError error) {
		for (int i = 0; i < test.getNbRows(); i++) {
			DataTuple tuple = test.getTuple(i);
			ClusStatistic pred = tree.clusterWeighted(tuple);
			error.addExample(tuple, pred);
		}
	}

	public static void computeErrorStandard(ClusNode tree, RowData test, ClusError error) {
		for (int i = 0; i < test.getNbRows(); i++) {
			DataTuple tuple = test.getTuple(i);
			ClusStatistic pred = tree.predictWeighted(tuple);
			error.addExample(tuple, pred);
		}
	}

	public static void computeErrorNode(ClusNode node, RowData test, ClusError error) {
		ClusStatistic pred = node.getTargetStat();
		for (int i = 0; i < test.getNbRows(); i++) {
			DataTuple tuple = test.getTuple(i);
			error.addExample(tuple, pred);
		}
	}

	public static void initializeTestErrorsData(ClusNode tree, RowData test, ClusError error) throws IOException {
		TreeErrorComputer comp = new TreeErrorComputer();
		initializeTestErrors(tree, error);
		for (int i = 0; i < test.getNbRows(); i++) {
			DataTuple tuple = test.getTuple(i);
			tree.applyModelProcessor(tuple, comp);
		}
	}

	public static void initializeTestErrors(ClusNode node, ClusError error) {
		ErrorVisitor visitor = (ErrorVisitor)node.getVisitor();
		visitor.testerr = error.getErrorClone(error.getParent());
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			initializeTestErrors(child, error);
		}
	}

	public static void computeErrorSimple(ClusNode node, ClusError sum) {
		if (node.atBottomLevel()) {
			ErrorVisitor visitor = (ErrorVisitor)node.getVisitor();
			sum.add(visitor.testerr);
		} else {
			for (int i = 0; i < node.getNbChildren(); i++) {
				ClusNode child = (ClusNode)node.getChild(i);
				computeErrorSimple(child, sum);
			}
		}
	}
}

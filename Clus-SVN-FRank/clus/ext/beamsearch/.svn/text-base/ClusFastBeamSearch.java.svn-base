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
 * Created on Apr 25, 2005
 */
package clus.ext.beamsearch;

import clus.*;
import clus.algo.split.CurrentBestTestAndHeuristic;
import clus.algo.split.FindBestTest;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.type.*;
import clus.main.*;
import clus.model.test.*;
import clus.util.*;

import java.io.*;
import java.util.ArrayList;

/*

The optimizations over ClusBeamSearch.java the following:

(1) It computes the heuristic value of the refinements of a given leaf
only once (refinement = new test node with two child leaves that can be
placed instead of the leaf). This can be done because these values do
not depend on the rest of the tree. You just add the value of the entire
tree before the refinement and subtract the value of the leaf that is
being refined. This is done in the line:

double heuristic = test.getHeuristicValue() + offset;

The possible refinements for each leaf are stored in an object of the
class ClusBeamAttrSelector in each leaf. It has an array storing the best
split for each attribute. The "non-fast" version, on the other hand,
repeats the compuation of all heuristics each time the leaf is considered
for refinement.

(2) If a new test is introduced in a tree, then the data of the node where
the test is added must be split between the two new leaves based on the
outcome of the test. The "fast" version postpones this step, i.e., it does
not do this each time a new tree is added to the beam. The reason is that
other trees might kick it out of the beam later and then this partitioning
step (which takes some time) would be done in vain. In the "fast" version,
these splits are therefore only done for the remaining trees after the entire
beam of trees is refined. This is done in the updateModelRefinement()
method.

*/

public class ClusFastBeamSearch extends ClusBeamSearch {

	ClusBeamSizeConstraints m_Constr;

	public ClusFastBeamSearch(Clus clus) throws IOException, ClusException {
		super(clus);
		m_Constr = new ClusBeamSizeConstraints();
	}

	public ClusBeam initializeBeam(ClusRun run) throws ClusException, IOException {
		ClusBeam beam = super.initializeBeam(run);
		ClusBeamModel model = beam.getBestModel();
		initModelRecursive((ClusNode)model.getModel(), (RowData)run.getTrainingSet());
		return beam;
	}

	public void initModelRecursive(ClusNode node, RowData data) {
		if (node.atBottomLevel()) {
			ClusBeamAttrSelector attrsel = new ClusBeamAttrSelector();
			attrsel.setData(data);
			node.setVisitor(attrsel);
		} else {
			NodeTest test = node.getTest();
			for (int j = 0; j < node.getNbChildren(); j++) {
				ClusNode child = (ClusNode)node.getChild(j);
				RowData subset = data.applyWeighted(test, j);
				initModelRecursive(child, subset);
			}
		}
	}

	public void computeGlobalHeuristic(NodeTest test, RowData data, CurrentBestTestAndHeuristic sel) {
		sel.reset(2);
		data.calcPosAndMissStat(test, ClusNode.YES, sel.getPosStat(), sel.getMissStat());
		double global_heur = m_Heuristic.calcHeuristic(sel.getTotStat(), sel.getPosStat(), sel.getMissStat());
		test.setHeuristicValue(global_heur);
	}

	public void refineGivenLeaf(ClusNode leaf, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) {
		ClusBeamAttrSelector attrsel = (ClusBeamAttrSelector)leaf.getVisitor();
		if (attrsel.isStopCrit()) {
			/* stopping criterion already succeeded for this node */
			if (m_Verbose) System.out.print("[S:"+leaf.getClusteringStat()+"]");
			return;
		}
		RowData data = attrsel.getData();
		if (!attrsel.hasEvaluations()) {
			if (m_Induce.initSelectorAndStopCrit(leaf, data)) {
				/* stopping criterion succeeds */
				attrsel.setStopCrit(true);
				return;
			}
			CurrentBestTestAndHeuristic sel = m_Induce.getBestTest();
			FindBestTest find = m_Induce.getFindBestTest();
			m_Heuristic.setTreeOffset(0.0);
			attrsel.newEvaluations(attrs.length);
			for (int i = 0; i < attrs.length; i++) {
				sel.resetBestTest();
				ClusAttrType at = attrs[i];
				if (at instanceof NominalAttrType) find.findNominal((NominalAttrType)at, data);
				else find.findNumeric((NumericAttrType)at, data);
				// found good test for attribute ?
				if (sel.hasBestTest()) {
					NodeTest test = sel.updateTest();
					if (hasAttrHeuristic()) {
						// has attribute heuristic -> recompute global heuristic
						computeGlobalHeuristic(test, data, sel);
					}
					attrsel.setBestTest(i, test);
				}
			}
		}
		double offset = root.getValue() - m_Heuristic.computeLeafAdd(leaf);
		NodeTest[] besttests = attrsel.getBestTests();
		if (m_Verbose) System.out.println("[M:"+beam.getMinValue()+"]");
		for (int i = 0; i < besttests.length; i++) {
			NodeTest test = besttests[i];
			if (test != null) {
				double beam_min_value = beam.getMinValue();
				double heuristic = test.getHeuristicValue() + offset;
				if (heuristic >= beam_min_value) {
					if (m_Verbose) System.out.print("[+]");
					ClusNode ref_leaf = (ClusNode)leaf.cloneNode();
					ref_leaf.setTest(test);
					// visitor is removed in updateModelRefinement() !
					ref_leaf.setVisitor(leaf.getVisitor());
					if (Settings.VERBOSE > 0) System.out.println("Test: "+ref_leaf.getTestString()+" -> "+ref_leaf.getTest().getHeuristicValue()+" ("+ref_leaf.getTest().getPosFreq()+")");
					int arity = ref_leaf.updateArity();
					for (int j = 0; j < arity; j++) {
						ClusNode child = new ClusNode();
						ref_leaf.setChild(child, j);
					}
					ClusNode root_model = (ClusNode)root.getModel();
					ClusNode ref_tree = root_model.cloneTreeWithVisitors(leaf, ref_leaf);
					ClusBeamModel new_model = new ClusBeamModel(heuristic, ref_tree);
					new_model.setRefinement(ref_leaf);
					new_model.setParentModelIndex(getCurrentModel());
					beam.addModel(new_model);
					setBeamChanged(true);
				} else {
					if (m_Verbose) System.out.print("[-:"+heuristic+"]");
				}
			}
		}
	}

	public void refineModel(ClusBeamModel model, ClusBeam beam, ClusRun run) throws IOException {
		ClusNode tree = (ClusNode)model.getModel();
		ClusBeamModel new_model = model.cloneModel();
		/* Create new model because value can be different */
		new_model.setValue(sanityCheck(model.getValue(), tree));
		if (isBeamPostPrune()) {
			ClusNode clone = tree.cloneTreeWithVisitors();
			m_Constr.enforce(clone, m_MaxTreeSize);
/*			if (m_Constr.isModified()) {
				System.out.println();
				System.out.println("Previous:");
				tree.printTree();
				System.out.println("Modified:");
				clone.printTree();
				ClusNode clone2 = tree.cloneTreeWithVisitors();
				m_Constr.setDebug(true);
				m_Constr.enforce(clone2, m_MaxTreeSize);
				System.exit(0);
			} */
			if (m_Constr.isFinished()) {
				model.setFinished(true);
				return;
			}
			if (m_Constr.isModified()) {
				new_model.setModel(clone);
				new_model.setValue(estimateBeamMeasure(clone));
			}
		} else {
			if (m_MaxTreeSize >= 0) {
				int size = tree.getNbNodes();
				if (size + 2 > m_MaxTreeSize) {
					model.setFinished(true);
					return;
				}
			}
		}
		RowData train = (RowData)run.getTrainingSet();
		ClusAttrType[] attrs = train.getSchema().getDescriptiveAttributes();
		refineEachLeaf((ClusNode)new_model.getModel(), new_model, beam, attrs);
	}

	public void updateModelRefinement(ClusBeamModel model) {
		/* Get data into children of last refinement */
		ClusNode leaf = (ClusNode)model.getRefinement();
		if (leaf == null) return;
		ClusBeamAttrSelector attrsel = (ClusBeamAttrSelector)leaf.getVisitor();
		RowData data = attrsel.getData();
		ClusStatManager mgr = m_Induce.getStatManager();
		for (int j = 0; j < leaf.getNbChildren(); j++) {
			ClusNode child = (ClusNode)leaf.getChild(j);
			ClusBeamAttrSelector casel = new ClusBeamAttrSelector();
			RowData subset = data.applyWeighted(leaf.getTest(), j);
			child.initTargetStat(mgr, subset);
			child.initClusteringStat(mgr, subset);
			casel.setData(subset);
			child.setVisitor(casel);
		}
		leaf.setVisitor(null);
		model.setRefinement(null);
	}

	public void refineBeam(ClusBeam beam, ClusRun run) throws IOException {
		super.refineBeam(beam, run);
		ArrayList models = beam.toArray();
		for (int i = 0; i < models.size(); i++) {
			ClusBeamModel model = (ClusBeamModel)models.get(i);
			updateModelRefinement(model);
		}
	}
}

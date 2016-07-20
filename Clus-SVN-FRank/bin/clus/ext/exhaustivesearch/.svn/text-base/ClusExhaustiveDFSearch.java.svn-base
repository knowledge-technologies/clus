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
 * Created on July 31, 2006
 */

package clus.ext.exhaustivesearch;

import java.io.*;
import java.util.*;

import jeans.util.*;
import jeans.math.*;

import clus.model.test.*;
import clus.Clus;
import clus.algo.split.CurrentBestTestAndHeuristic;
import clus.algo.split.FindBestTest;
import clus.algo.tdidt.ClusNode;
import clus.algo.tdidt.ConstraintDFInduce;
import clus.ext.constraint.ClusConstraintFile;
import clus.ext.beamsearch.*;
import clus.main.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusExhaustiveDFSearch extends ClusExhaustiveSearch {

	protected int m_cTree;

	public ClusExhaustiveDFSearch(Clus clus) throws ClusException, IOException {
		super(clus);
	}

	public int getCTree() {
		return m_cTree;
	}

	public void setCTree(int i) {
		m_cTree = i;
	}

	public void incCTree() {
		m_cTree++;
	}

	public ClusBeamModel getRootNode(ClusRun run) throws ClusException {
		ClusStatManager smanager = m_BeamInduce.getStatManager();
		Settings sett = smanager.getSettings();
		sett.setMinimalWeight(1); //the minimal number of covered example in a leaf is 1
		/* Create single leaf node */
		RowData train = (RowData)run.getTrainingSet();
		ClusStatistic stat = m_Induce.createTotalClusteringStat(train);
		stat.calcMean();
		m_Induce.initSelectorAndSplit(stat);
		initSelector(m_Induce.getBestTest());
		System.out.println("Root statistic: "+stat);
		ClusNode root = null;
		/* Has syntactic constraints? */
		String constr_file = sett.getConstraintFile();
		if (StringUtils.unCaseCompare(constr_file, Settings.NONE)) {//no constraint
			root = new ClusNode();
			root.setClusteringStat(stat);
		} else {
			ClusConstraintFile file = ClusConstraintFile.getInstance();
			root = file.getClone(constr_file);
			root.setClusteringStat(stat);
			m_Induce.fillInStatsAndTests(root, train);
		}
		//System.out.println("the beam is initialized to :");root.printTree();
		/* Compute total weight */
		double weight = root.getClusteringStat().getTotalWeight();
		setTotalWeight(weight);
		/* Evaluate the quality estimate */
		double value = estimateBeamMeasure(root);
		/* Add tree to beam */
		return new ClusBeamModel(value, root);
	}

	/*
	 * @leaf : the leaf to refine
	 * @root : the entire model which encapsuled the tree (contains the value, etc...)
	 * @beam : the entire beam
	 * @attr : the attribute that can be used for to refine the leaf
	 *
	 * Everything that concerns setting the test is in the ClusNode Class
	 */

	public void refineGivenLeafExhaustiveDF(ClusNode leaf, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs, ClusRun run) throws IOException {
		MyArray arr = (MyArray)leaf.getVisitor();
		RowData data = new RowData(arr.getObjects(), arr.size());
		if (m_Induce.initSelectorAndStopCrit(leaf, data)) {
			// stopping criterion is met (save this for further reference?)
			System.out.println("stopping criterion reached in refineGivenLeafExhaustive");
			return;
		}
		if((leaf.getClusteringStat()).getError() == 0.0){
		//System.out.println("pure leaf");
			return;
		}
		// init base value for heuristic
		CurrentBestTestAndHeuristic sel = m_Induce.getBestTest();
		FindBestTest find = m_Induce.getFindBestTest();
		double base_value = root.getValue();
		double leaf_add = m_Heuristic.computeLeafAdd(leaf);
		m_Heuristic.setTreeOffset(base_value - leaf_add);
		int nbNewLeaves = 0;
		ClusNode[] newLeaves = new ClusNode[attrs.length];
		// find good splits
		for (int i = 0; i < attrs.length; i++) {
			// reset selector for each attribute
			sel.resetBestTest();
			// process attribute
			ClusAttrType at = attrs[i];
			//System.out.println("Attribute: "+at.getName());
			// look for the best avlue of the attribute the attribute
			if (at instanceof NominalAttrType) find.findNominal((NominalAttrType)at, data);
			else find.findNumeric((NumericAttrType)at, data);
			// found good test for attribute (the test type has been changed in the finnominal function)?
			if (sel.hasBestTest()) {
				ClusNode ref_leaf = (ClusNode)leaf.cloneNode();
				ref_leaf.testToNode(sel);
				// output best test
				if (Settings.VERBOSE > 0) System.out.println("Test: "+ref_leaf.getTestString()+" -> "+sel.m_BestHeur+" ("+ref_leaf.getTest().getPosFreq()+")");
				newLeaves[nbNewLeaves++] = ref_leaf;
			}
		}
		ClusStatManager mgr = m_Induce.getStatManager();
		for (int i = 0; i < nbNewLeaves; i++) {
			ClusNode ref_leaf = newLeaves[i];
			int arity = ref_leaf.updateArity();
			NodeTest test = ref_leaf.getTest();
			for (int j = 0; j < arity; j++) {
				ClusNode child = new ClusNode();
				ref_leaf.setChild(child, j);
				RowData subset = data.applyWeighted(test, j);
				child.initClusteringStat(mgr, subset);
				// the following two calls could be removed, but are useful for printing the trees
				child.initTargetStat(mgr, subset);
				child.getTargetStat().calcMean();
			}
			// create new model
			ClusNode root_model = (ClusNode)root.getModel();
			ClusNode ref_tree = (ClusNode)root_model.cloneTree(leaf, ref_leaf);
			double new_heur = estimateBeamMeasure(ref_tree);
			//the default created model is unrefined
			ClusBeamModel new_model = new ClusBeamModel(new_heur, ref_tree);
			new_model.setParentModelIndex(getCurrentModel());
			refineModel(new_model, beam, run);
		}
	}

	/*
	 * Used to go down the tree to each leaf and then refine each leafs
	 */

	public void refineEachLeafDF(ClusNode tree, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs, ClusRun run) throws IOException {
		int nb_c = tree.getNbChildren();
		//System.out.println("Tree to refine:");
		//tree.printTree();
		if (nb_c == 0) {
	   		refineGivenLeafExhaustiveDF(tree, root, beam, attrs, run);
		} else {
	    	for (int i = nb_c-1; i >= 0; i--) {
	    		ClusNode child = (ClusNode)tree.getChild(i);
	    		refineEachLeafDF(child, root, beam, attrs, run);
	    		if (child.getNbChildren() > 0) {
	    			break;
	    		}
	    	}
	    }
	}

	public static ArrayList getErrorPerleaf(ClusNode tree, MDouble sumLeftLeaves) {
		ArrayList listRightLeaves = new ArrayList();
		//System.out.println("Tree:");
		//tree.printTree();
		getErrorPerleaf(tree, sumLeftLeaves, listRightLeaves);
		Collections.sort(listRightLeaves);
		/*System.out.println("SumLeftLeaves: "+sumLeftLeaves.getDouble());
		System.out.print("ListRightLeaves: ");
		for (int i = 0; i < listRightLeaves.size(); i++) {
			if (i != 0) System.out.print(",");
			System.out.print(listRightLeaves.get(i));
		}
		System.out.println();	*/
		return listRightLeaves;
	}

	public static void getErrorPerleaf(ClusNode tree, MDouble sumLeftLeaves, ArrayList listRightLeaves) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ClusStatistic total = tree.getClusteringStat();
			listRightLeaves.add(new Double(total.getError()));
		} else {
			boolean foundRightMost = false;
	    	for (int i = nb_c-1; i >= 0; i--) {
	    		ClusNode child = (ClusNode)tree.getChild(i);
	    		if (foundRightMost) {
	    			getSumLeftLeavesError(child, sumLeftLeaves);
	    		} else {
	    			getErrorPerleaf(child, sumLeftLeaves, listRightLeaves);
	    		}
	    		if (child.getNbChildren() > 0) {
	    			foundRightMost = true;
	    		}
	    	}
	    }
	}

	public static void getSumLeftLeavesError(ClusNode tree, MDouble sumLeftLeaves) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			ClusStatistic total = tree.getClusteringStat();
			sumLeftLeaves.addDouble(total.getError());
		} else {
			for (int i = 0; i < nb_c; i++) {
	    		ClusNode child = (ClusNode)tree.getChild(i);
	    		getSumLeftLeavesError(child, sumLeftLeaves);
	    	}
	    }
	}

	public void refineModel(ClusBeamModel model, ClusBeam beam, ClusRun run) throws IOException {
		ClusNode tree = (ClusNode)model.getModel();
		int size = tree.getNbNodes();
		/* Does current tree satisfy constraints */
		//System.out.println("The number of tree is "+getCTree());
		incCTree();
		//System.out.println("Clus ExhausDF MaxError is "+m_MaxError);
		//System.out.println("Clus ExhausDF error of the tree is "+(ClusNode.estimateErrorRecursive(tree)/m_TotalWeight));
		//if (size == 1) {System.out.println("Clus ExhausDF error (before testing the constraint) of the tree of size 1 is" +(tree.m_ClusteringStat).getErrorRel());}

		boolean tree_ok = true;
		if (m_MaxTreeSize > 0 && size > m_MaxTreeSize) tree_ok = false;
		if ((size > 1) && (m_MaxError > 0 && ClusNode.estimateErrorRecursive(tree)/m_TotalWeight > m_MaxError)) tree_ok = false;
		if ((size == 1) && (m_MaxError > 0 && (tree.m_ClusteringStat).getErrorRel() > m_MaxError)) {tree_ok = false;}
		if (tree_ok) {
			//System.out.println("model "+getCTree()+" must be added");
			beam.addModel(model);
		}
		//System.out.println("Tree size: "+size+" err: "+ClusNode.estimateErrorRecursive(tree)/m_TotalWeight+" ok: "+tree_ok);
		//tree.printTree();
		/* Compute size */
		if (m_MaxTreeSize >= 0) { //if the size is infinite, it equals -1
			if (size + 2 > m_MaxTreeSize) {
				return;
			}
		}


		//we assume that the test as binary
		if(m_MaxError >0 && m_MaxTreeSize>0){//default m_MaxError =0
			int NbpossibleSplit = (m_MaxTreeSize - tree.getModelSize())/2;
			//System.out.println("the number of possible split is still "+NbpossibleSplit);

			MDouble sumLeftLeaves = new MDouble();
			ArrayList error = getErrorPerleaf(tree, sumLeftLeaves);
			//System.out.println("the number of leaf is "+error.length);
			if(error.size() > NbpossibleSplit){
				double minerror = sumLeftLeaves.getDouble();
				for(int i = 0; i < (error.size()-NbpossibleSplit);i++){
					//System.out.println(" leaf "+i+", error = "+error[i]);
					minerror += ((Double)error.get(i)).doubleValue();
				}
				double minerrorrel = minerror/m_TotalWeight;
				//System.out.println("the minimum relative error is : "+minerrorrel);
				if(minerrorrel > m_MaxError){
					//tree.printTree();
					//System.out.println("PRUNE WITH ERROR");
					return;
				}
			}
		}


		/* Sort the data into tree */
		RowData train = (RowData)run.getTrainingSet();
		m_Coll.initialize(tree, null);
		int nb_rows = train.getNbRows();
		for (int i = 0; i < nb_rows; i++) {
			DataTuple tuple = train.getTuple(i);
			tree.applyModelProcessor(tuple, m_Coll);
		}
		/* Data is inside tree, try to refine each leaf */
		ClusAttrType[] attrs = train.getSchema().getDescriptiveAttributes();
		refineEachLeafDF(tree, model, beam, attrs, run);
		/* Remove data from tree */
		tree.clearVisitors();
	}

	public ClusBeam exhaustiveSearch(ClusRun run) throws ClusException, IOException {
		//int cpt_tree_evaluation = 0;
		reset();
		System.out.println("Starting exhaustive depth first search :");
		m_Induce = new ConstraintDFInduce(m_BeamInduce);
		ClusBeam beam = new ClusBeam(-1, false);
		ClusBeamModel current = getRootNode(run);
		refineModel(current, beam, run);
		setBeam(beam);
		ArrayList arraybeamresult = beam.toArray();
		System.out.println("The number of resulting model is "+arraybeamresult.size());
		/*for (int j = 0; j < arraybeamresult.size(); j++) {
			System.out.println("model"+j+": ");
			ClusBeamModel m = (ClusBeamModel)arraybeamresult.get(j);
			ClusNode node = (ClusNode)m.getModel();
    		node.printTree();
		}*/
		System.out.println("The number of tree evaluated during search is "+getCTree());
		return beam;
	}
}


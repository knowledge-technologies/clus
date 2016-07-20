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

import jeans.util.cmdline.CMDLineArgs;
import jeans.util.*;
import jeans.math.*;
import jeans.io.*;

import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.model.test.*;
import clus.model.modelio.*;
import clus.Clus;
import clus.algo.*;
import clus.algo.split.CurrentBestTestAndHeuristic;
import clus.algo.split.FindBestTest;
import clus.algo.tdidt.*;
import clus.algo.tdidt.processor.BasicExampleCollector;
import clus.ext.constraint.*;
import clus.ext.beamsearch.*;
import clus.main.*;
import clus.heuristic.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusExhaustiveSearch extends ClusInductionAlgorithmType {

	public final static int HEURISTIC_ERROR = 0;
	public final static int HEURISTIC_SS = 1;

//	public final static int m_MaxSteps = 100000;
	protected BasicExampleCollector m_Coll = new BasicExampleCollector();
	protected ConstraintDFInduce m_Induce;
	protected ClusExhaustiveInduce m_BeamInduce;
	protected boolean m_BeamChanged;
	protected int m_CurrentModel;
	protected int m_MaxTreeSize;
	protected double m_MaxError;
	protected double m_TotalWeight;
	protected ArrayList m_BeamStats;
	protected ClusBeam m_Beam;
	protected boolean m_BeamPostPruning;
	protected ClusBeamHeuristic m_Heuristic;
	protected ClusHeuristic m_AttrHeuristic;
	protected boolean m_Verbose;

	public ClusExhaustiveSearch(Clus clus) throws ClusException, IOException {
		super(clus);
	}

	public void reset() {
		m_Beam = null;
		m_BeamChanged = false;
		m_CurrentModel = -1;
		m_TotalWeight = 0.0;
		m_BeamStats = new ArrayList();
	}

	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
		schema.addIndices(ClusSchema.ROWS);
		m_BeamInduce = new ClusExhaustiveInduce(schema, sett, this);
		m_BeamInduce.getStatManager().setBeamSearch(true);
		return m_BeamInduce;
	}

	public void initializeHeuristic() {
		ClusStatManager smanager = m_BeamInduce.getStatManager();
		Settings sett = smanager.getSettings();
		m_MaxTreeSize = sett.getMaxSize();
		m_MaxError = sett.getMaxErrorConstraint(0);
		System.out.println("ClusExhaustiveSearch : The Max Error is "+m_MaxError);
		m_BeamPostPruning = sett.isBeamPostPrune();
		m_Heuristic = (ClusBeamHeuristic)smanager.getHeuristic();
   		int attr_heur = sett.getBeamAttrHeuristic();
   		if (attr_heur != Settings.HEURISTIC_DEFAULT) {
   			m_AttrHeuristic = smanager.createHeuristic(attr_heur);
   			m_Heuristic.setAttrHeuristic(m_AttrHeuristic);
   		}
	}

	public final boolean isBeamPostPrune() {
		return m_BeamPostPruning;
	}

	public double computeLeafAdd(ClusNode leaf) {
		return m_Heuristic.computeLeafAdd(leaf);
	}

	public double estimateBeamMeasure(ClusNode tree) {
		return m_Heuristic.estimateBeamMeasure(tree);
	}

	public void initSelector(CurrentBestTestAndHeuristic sel) {
		if (hasAttrHeuristic()) {
			sel.setHeuristic(m_AttrHeuristic);
		}
	}

	public final boolean hasAttrHeuristic() {
		return m_AttrHeuristic != null;
	}

	public ClusBeam initializeBeamExhaustive(ClusRun run) throws ClusException {
		ClusStatManager smanager = m_BeamInduce.getStatManager();
		Settings sett = smanager.getSettings();
		sett.setMinimalWeight(1); //the minimal number of covered example in a leaf is 1
		ClusBeam beam = new ClusBeam(-1,false); //infinite size and we add everything
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
		beam.addModel(new ClusBeamModel(value, root));
		System.out.println("the number of children from the root node is :"+root.getNbChildren());
		return beam;
	}

	/*
	 * @leaf : the leaf to refine
	 * @root : the entire model which encapsuled the tree (contains the value, etc...)
	 * @beam : the entire beam
	 * @attr : the attribute that can be used for to refine the leaf
	 *
	 * Everything that concerns setting the test is in the ClusNode Class
	 */

	public void refineGivenLeafExhaustive(ClusNode leaf, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) {
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
		// find good splits
		for (int i = 0; i < attrs.length; i++) {
			// reset selector for each attribute
			sel.resetBestTest();
			double beam_min_value = Double.NEGATIVE_INFINITY;
			sel.setBestHeur(beam_min_value);
			// process attribute
			ClusAttrType at = attrs[i];
			// System.out.println("Attribute: "+at.getName());
			// look for the best avlue of the attribute the attribute
			if (at instanceof NominalAttrType) find.findNominal((NominalAttrType)at, data);
			else find.findNumeric((NumericAttrType)at, data);
			// found good test for attribute (the test type has been changed in the finnominal function)?
			if (sel.hasBestTest()) {
				ClusNode ref_leaf = (ClusNode)leaf.cloneNode();
				ref_leaf.testToNode(sel);
				// output best test
				if (Settings.VERBOSE > 0) System.out.println("Test: "+ref_leaf.getTestString()+" -> "+sel.m_BestHeur+" ("+ref_leaf.getTest().getPosFreq()+")");
				// create child nodes
				ClusStatManager mgr = m_Induce.getStatManager();
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
				double new_heur = sanityCheck(sel.m_BestHeur, ref_tree);
				//the default created model is unrefined
				ClusBeamModel new_model = new ClusBeamModel(new_heur, ref_tree);
				new_model.setParentModelIndex(getCurrentModel());
				beam.addModel(new_model);
				setBeamChanged(true);
				// Uncomment the following to print each model that is added to the beam
				//System.out.println("this new model, has been added to the beam : ");
				//((ClusNode)new_model.getModel()).printTree();
			}
			//else{System.out.println("no sel.hasBestTest()");}
		}// end for each attribute
	}

	/*
	 * Used to go down the tree to each leaf and then refine each leafs
	 */

	public void refineEachLeaf(ClusNode tree, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) {
		int nb_c = tree.getNbChildren();
		if (nb_c == 0) {
			refineGivenLeafExhaustive(tree, root, beam, attrs);
		} else {
			for (int i = 0; i < nb_c; i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				//System.out.print("refining leaf : ");child.printTree();
				refineEachLeaf(child, root, beam, attrs);
			}
		}
	}


	public void refineModel(ClusBeamModel model, ClusBeam beam, ClusRun run) throws IOException {
		ClusNode tree = (ClusNode)model.getModel();
		/* Compute size */
		if (m_MaxTreeSize >= 0) { //if the size is infinite, it equals -1
			int size = tree.getNbNodes();
			if (size + 2 > m_MaxTreeSize) {
				return;
			}
		}

		//we assume that the test are binary
		if(m_MaxError >0 && m_MaxTreeSize>0){//default m_MaxError =0
			int NbpossibleSplit = (m_MaxTreeSize - tree.getModelSize())/2;
			//System.out.println("the number of possible split is still "+NbpossibleSplit);
			double[] error = getErrorPerleaf(tree);
			//System.out.println("the number of leaf is "+error.length);
			double minerror = 0.0;
			if(error.length > NbpossibleSplit){
				for(int i = 0; i < ((error.length)-NbpossibleSplit);i++){
					//System.out.println(" leaf "+i+", error = "+error[i]);
					minerror+= error[i];
				}
				double minerrorrel = minerror/m_TotalWeight;
				//System.out.println("the minimum relative error is : "+minerrorrel);
				if(minerrorrel > m_MaxError){
					tree.printTree();
					System.out.println("PRUNE WITH ERROR");
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
		refineEachLeaf(tree, model, beam, attrs);
		/* Remove data from tree */
		tree.clearVisitors();
	}

	public	static double[] getErrorPerleaf(ClusNode tree){
		int nbleaf = tree.getNbLeaf();
		double[] resulterror = new double[nbleaf];
		if (tree.atBottomLevel()) {
			ClusStatistic total = tree.getClusteringStat();
			resulterror[0] = total.getError();
			//System.out.println("ClusNode : error is "+resulterror[0]);
			}
		else {
			ClusNode child0 = (ClusNode)tree.getChild(0);
			resulterror = getErrorPerleaf(child0);
				for (int i = 1; i < tree.getNbChildren(); i++) {
					ClusNode childi = (ClusNode)tree.getChild(i);
					double [] errori = getErrorPerleaf(childi);
					resulterror = concatarraysorted(resulterror, errori);
				}
			}
		return resulterror;
	}

	public static double [] concatarraysorted(double [] array1, double [] array2){
	int size_array =  array1.length+ array2.length ;
	double [] array = new double[size_array];
		for (int i=0; i< array1.length;i++){
			array[i] = array1[i];
		}
		for(int i=0; i< array2.length;i++){
		  array[i+array1.length]  = array2[i];
		}
		Arrays.sort(array);
		return array;
	}


	public void refineBeamExhaustive(ClusBeam beam, ClusRun run) throws IOException {
		setBeamChanged(false);
		ArrayList models = beam.toArray();
		//System.out.println("the size of the beam is :"+models.size());
		for (int i = 0; i < models.size(); i++) {
			System.out.print("Refining model: "+i);
			setCurrentModel(i);
			ClusBeamModel model = (ClusBeamModel)models.get(i);
			if (!(model.isRefined() || model.isFinished())) {
				if (m_Verbose) System.out.print("[*]");
				refineModel(model, beam, run);
				model.setRefined(true);
				model.setParentModelIndex(-1);
			}
			if (m_Verbose) {
				if (model.isRefined()) {
					System.out.println("[R]");
				}
				if (model.isFinished()) {
					System.out.println("[F]");
				}
			}
		}
	}


	public Settings getSettings() {
		return m_Clus.getSettings();
	}

	public void estimateBeamStats(ClusBeam beam) {
		SingleStat stat_heuristic = new SingleStat();
		SingleStat stat_size = new SingleStat();
		SingleStat stat_same_heur = new SingleStat();
		ArrayList lst = beam.toArray();
		HashSet tops = new HashSet();
		for (int i = 0; i < lst.size(); i++) {
			ClusBeamModel model = (ClusBeamModel)lst.get(i);
			stat_heuristic.addFloat(model.getValue());
			stat_size.addFloat(model.getModel().getModelSize());
			NodeTest top = ((ClusNode)model.getModel()).getTest();
			if (top != null) {
				if (!tops.contains(top)) {
					tops.add(top);
				}
			}
		}
		Iterator iter = beam.getIterator();
		while (iter.hasNext()) {
			ClusBeamTreeElem elem = (ClusBeamTreeElem)iter.next();
			stat_same_heur.addFloat(elem.getCount());
		}
		ArrayList stat = new ArrayList();
		stat.add(stat_heuristic);
		stat.add(stat_same_heur);
		stat.add(stat_size);
		stat.add(new Integer(tops.size()));
		m_BeamStats.add(stat);
	}

	public String getLevelStat(int i) {
		ArrayList stat = (ArrayList)m_BeamStats.get(i);
		StringBuffer buf = new StringBuffer();
		buf.append("Level: "+i);
		for (int j = 0; j < stat.size(); j++) {
			Object elem = stat.get(j);
			buf.append(", ");
			if (elem instanceof SingleStat) {
				SingleStat st = (SingleStat)elem;
				buf.append(st.getMean()+","+st.getRange());
			} else {
				buf.append(elem.toString());
			}
		}
		return buf.toString();
	}

	public void printBeamStats(int level) {
		System.out.println(getLevelStat(level));
	}

	public void saveBeamStats() {
		MyFile stats = new MyFile(getSettings().getAppName()+".bmstats");
		for (int i = 0; i < m_BeamStats.size(); i++) {
			stats.log(getLevelStat(i));
		}
		stats.close();
	}

	public void writeModel(ClusModelCollectionIO strm) throws IOException {
			saveBeamStats();
	    	ArrayList beam = getBeam().toArray();
	    	for (int i = 0; i < beam.size(); i++) {
	    		ClusBeamModel m = (ClusBeamModel)beam.get(i);
	    		ClusNode node = (ClusNode)m.getModel();
	    		node.updateTree();
	    		node.clearVisitors();
	    	}
	    	int pos = 1;
	    	for (int i = beam.size()-1; i >= 0; i--) {
	    		ClusBeamModel m = (ClusBeamModel)beam.get(i);
	    		ClusModelInfo info = new ClusModelInfo("B"+pos+": "+m.getValue());
	    		info.setScore(m.getValue());
	    		info.setModel(m.getModel());
				strm.addModel(info);
				pos++;
	    	}
	}

	public void setVerbose(boolean verb) {
		m_Verbose = verb;
	}

	public ClusBeam exhaustiveSearch(ClusRun run) throws ClusException, IOException {
		reset();
		System.out.println("Starting exhaustive search");
		m_Induce = new ConstraintDFInduce(m_BeamInduce);
		ClusBeam beam = initializeBeamExhaustive(run);
		ClusBeam beamresult = new ClusBeam(-1, false);
		int cpt_tree_evaluation = 0;
		int i = 0;
		setVerbose(true);
		while (true) {
			System.out.print("Step: ");
			if (i != 0) System.out.print(",");
			System.out.println(i);
			System.out.flush();
			refineBeamExhaustive(beam, run);
			if (isBeamChanged()) { //look for the change of the CluBeamInduce object
				estimateBeamStats(beam);
			} else {
				break;
			}
			i++;
		}
		ArrayList arraybeam = beam.toArray();
		for (int j = 0; j < arraybeam.size(); j++) {
			//System.out.print("copy the "+j+"th model of the beam : ");
    		ClusBeamModel m = (ClusBeamModel)arraybeam.get(j);
    		//m.setRefined(false); //WHY SHOULD I DO THAT ????
    		ClusNode tree = (ClusNode)m.getModel();

    		//System.out.println("Clus Exhaus MaxError is "+m_MaxError);
    		//System.out.println("Clus Exhaus error of the tree is "+(ClusNode.estimateErrorRecursive(tree)/m_TotalWeight));
    		cpt_tree_evaluation++;
    		if (m_MaxError<=0 || ((ClusNode.estimateErrorRecursive(tree)/m_TotalWeight) <= m_MaxError)){
    		beamresult.addModel(m);
    		//ClusNode node = (ClusNode)m.getModel();
    		//node.printTree();
    		//System.out.println("tree nb "+(beamresult.toArray()).size()+" added");
    		}
    	}
		System.out.println();
		setBeam(beamresult);
		ArrayList arraybeamresult = beamresult.toArray();
		System.out.println(" the model that fulfill the constraints are"+arraybeamresult.size());
		/*for (int j = 0; j < arraybeamresult.size(); j++) {
			System.out.println("model"+j+": ");
			ClusBeamModel m = (ClusBeamModel)arraybeamresult.get(j);
			ClusNode node = (ClusNode)m.getModel();
    		node.printTree();
		}*/
		System.out.println("The number of tree evaluated is "+cpt_tree_evaluation);
		return beamresult;
	}

	public void setBeam(ClusBeam beam) {
		m_Beam = beam;
	}

	public ClusBeam getBeam() {
		return m_Beam;
	}

	public boolean isBeamChanged() {
		return m_BeamChanged;
	}

	public void setBeamChanged(boolean change) {
		m_BeamChanged = change;
	}

	public int getCurrentModel() {
		return m_CurrentModel;
	}

	public void setCurrentModel(int model) {
		m_CurrentModel = model;
	}

	public void setTotalWeight(double weight) {
		m_TotalWeight = weight;
	}

	public double sanityCheck(double value, ClusNode tree) {
/*		int size = tree.getModelSize();
		System.out.println("Size = "+size);
		tree.printTree();
		System.out.println("Evaluating measure..."); */
		double expected = estimateBeamMeasure(tree);
		if (Math.abs(value-expected) > 1e-6) {
			System.out.println("Bug in heurisitc: "+value+" <> "+expected);
			PrintWriter wrt = new PrintWriter(System.out);
			tree.printModel(wrt);
			wrt.close();
			System.out.flush();
			System.exit(1);
		}
		return expected;
	}

	public void tryLogBeam(MyFile log, ClusBeam beam, String txt) {
		if (log.isEnabled()) {
			log.log(txt);
			log.log("*********************************************");
			beam.print(log.getWriter(), m_Clus.getSettings().getBeamBestN());
			log.log();
		}
	}


	public void pruneAll(ClusRun cr) throws ClusException, IOException {
	}

	public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
		return model;
	}
}


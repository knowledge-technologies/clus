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

package clus.algo.tdidt;

import clus.main.*;
import clus.util.*;
import clus.algo.*;
import clus.algo.split.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.model.*;
import clus.model.test.*;
import clus.statistic.*;
import clus.ext.ensembles.*;
import clus.heuristic.*;

import java.io.*;
import java.util.*;

public class DepthFirstInduce extends ClusInductionAlgorithm {

	protected FindBestTest m_FindBestTest;
	protected ClusNode m_Root;

	public DepthFirstInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
		m_FindBestTest = new FindBestTest(getStatManager());
	}

	public DepthFirstInduce(ClusInductionAlgorithm other) {
		super(other);
		m_FindBestTest = new FindBestTest(getStatManager());
	}

	public DepthFirstInduce(ClusInductionAlgorithm other, NominalSplit split) {
		super(other);
		m_FindBestTest = new FindBestTest(getStatManager(), split);
	}

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
		int max = getSettings().getTreeMaxDepth();
		if (max != -1 && node.getLevel() >= max) {
			return true;
		}
		return m_FindBestTest.initSelectorAndStopCrit(node.getClusteringStat(), data);
	}

	public ClusAttrType[] getDescriptiveAttributes() {
		ClusSchema schema = getSchema();
		Settings sett = getSettings();
		if (!sett.isEnsembleMode()) {
			return schema.getDescriptiveAttributes();
		} else {
			switch (sett.getEnsembleMethod()) {
			case Settings.ENSEMBLE_BAGGING:
				return schema.getDescriptiveAttributes();
			case Settings.ENSEMBLE_RFOREST:
				ClusAttrType[] attrsAll = schema.getDescriptiveAttributes();
				ClusEnsembleInduce.setRandomSubspaces(attrsAll, schema.getSettings().getNbRandomAttrSelected());
				//ClusEnsembleInduce.setRandomSubspacesProportionalToSparsity(attrsAll, schema.getSettings().getNbRandomAttrSelected());
				return ClusEnsembleInduce.getRandomSubspaces();
			case Settings.ENSEMBLE_RSUBSPACES:
				return ClusEnsembleInduce.getRandomSubspaces();
			case Settings.ENSEMBLE_BAGSUBSPACES:
				return ClusEnsembleInduce.getRandomSubspaces();
			case Settings.ENSEMBLE_NOBAGRFOREST:
				ClusAttrType[] attrsAll1 = schema.getDescriptiveAttributes();
				ClusEnsembleInduce.setRandomSubspaces(attrsAll1, schema.getSettings().getNbRandomAttrSelected());
				return ClusEnsembleInduce.getRandomSubspaces();
			default:
				return schema.getDescriptiveAttributes();
			}
		}
	}

	
	public void filterAlternativeSplits(ClusNode node, RowData data, RowData[] subsets) {
		boolean removed = false;
		CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();
		int arity = node.getTest().updateArity();
		ArrayList<NodeTest> alternatives = best.getAlternativeBest(); // alternatives: all tests that result in same heuristic value, in the end this will contain all true alternatives
		ArrayList<NodeTest> oppositeAlternatives = new ArrayList<NodeTest>(); // this will contain all tests that are alternatives, but where the left and right branches are switched
		String alternativeString = new String(); // this will contain the string of alternative tests (regular and opposite), sorted according to position
		for (int k = 0; k < alternatives.size(); k++) {
			NodeTest nt = (NodeTest) alternatives.get(k);
			int altarity = nt.updateArity();
			// remove alternatives that have different arity than besttest
			if (altarity != arity) {
				alternatives.remove(k);
				k--;
				System.out.println("Alternative split with different arity: " + nt.getString());
				removed = true;
			} 
			else {
				// we assume the arity is 2 here
				// exampleindices of one branch are stored
				int nbsubset0 = subsets[0].getNbRows();
				int indices[] = new int[nbsubset0];
				for (int m=0; m<nbsubset0; m++) {
					indices[m] = subsets[0].getTuple(m).getIndex();
				}
				// check for all (=2) alternative branches one of them contains the same indices
				boolean same = false;
				for (int l = 0; l < altarity; l++) {
					RowData altrd = data.applyWeighted(nt, l);
					if (altrd.getNbRows() == nbsubset0) {
						int nbsame = 0;
						for (int m=0; m<nbsubset0; m++) {
							if (altrd.getTuple(m).getIndex() == indices[m]) {
								nbsame++;
							}
						}
						if (nbsame == nbsubset0) { 
							// same subsets found
							same = true;
							if (l!=0) {
								// the same subsets, but the opposite split, hence we add the test to the opposite alternatives 
								alternativeString = alternativeString + " and not(" + alternatives.get(k).toString() + ")";
								alternatives.remove(k);
								k--;
								oppositeAlternatives.add(nt);							
							}
							else {
								// the same subsets, and the same split
								alternativeString = alternativeString + " and " + alternatives.get(k).toString();
							}
						}
					}
				}
				if (!same) {
					alternatives.remove(k);
					k--;
					System.out.println("Alternative split with different ex in subsets: " + nt.getString());
					removed = true;
				}
				
				}
			}
		node.setAlternatives(alternatives);
		node.setOppositeAlternatives(oppositeAlternatives);
		node.setAlternativesString(alternativeString);
//		if (removed) System.out.println("Alternative splits were possible");
	}

	public void makeLeaf(ClusNode node) {
		node.makeLeaf();
		if (getSettings().hasTreeOptimize(Settings.TREE_OPTIMIZE_NO_CLUSTERING_STATS)) {
			node.setClusteringStat(null);
		}
	}
	
	public void induce(ClusNode node, RowData data) {
		//System.out.println("nonsparse induce");
		// Initialize selector and perform various stopping criteria
		if (initSelectorAndStopCrit(node, data)) {
			makeLeaf(node);
			return;
		}
		// Find best test
		
//		long start_time = System.currentTimeMillis();
		
		ClusAttrType[] attrs = getDescriptiveAttributes();
		for (int i = 0; i < attrs.length; i++) {
			ClusAttrType at = attrs[i];
			if (at instanceof NominalAttrType) m_FindBestTest.findNominal((NominalAttrType)at, data);
			else m_FindBestTest.findNumeric((NumericAttrType)at, data);
		}
		
/*		long stop_time = System.currentTimeMillis();
		long elapsed = stop_time - start_time;
		m_Time += elapsed;*/
		
		// Partition data + recursive calls
		CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();
		if (best.hasBestTest()) {
//			start_time = System.currentTimeMillis();
			
			node.testToNode(best);
			// Output best test
			if (Settings.VERBOSE > 0) System.out.println("Test: "+node.getTestString()+" -> "+best.getHeuristicValue());
			// Create children
			int arity = node.updateArity();
			NodeTest test = node.getTest();
			RowData[] subsets = new RowData[arity];
			for (int j = 0; j < arity; j++) {
				subsets[j] = data.applyWeighted(test, j);
			}
			if (getSettings().showAlternativeSplits()) {
				filterAlternativeSplits(node, data, subsets);
			}
			if (node != m_Root && getSettings().hasTreeOptimize(Settings.TREE_OPTIMIZE_NO_INODE_STATS)) {
				// Don't remove statistics of root node; code below depends on them
				node.setClusteringStat(null);
				node.setTargetStat(null);
			}
			for (int j = 0; j < arity; j++) {
				ClusNode child = new ClusNode();
				node.setChild(child, j);
				child.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), subsets[j]);
				child.initTargetStat(m_StatManager, m_Root.getTargetStat(), subsets[j]);
				induce(child, subsets[j]);
			}
		} else {
			makeLeaf(node);
		}
	}

/*	public void inducePert(ClusNode node, RowData data) {			
		//System.out.println("nonsparse inducePert");
		// Initialize selector and perform various stopping criteria
		if (initSelectorAndStopCrit(node, data)) {
			makeLeaf(node);
			return;
		}
		// Find best test
		
//		System.out.println("Schema: " + getSchema().toString());
		
		ArrayList<Integer> tuplelist = data.getPertTuples();
		if (tuplelist.size()<2) {
			makeLeaf(node);
			return;
		}
		// we only check the first two tuples. In case of multi-class classification, this corresponds to two random(?) classes to split.
		int tuple1index = tuplelist.get(0);
		DataTuple tuple1 = data.getTuple(tuple1index);
		int tuple2index = tuplelist.get(1);
		DataTuple tuple2 = data.getTuple(tuple2index);
//		System.out.println("tuples chosen: " + tuple1index + " " + tuple1.m_Index + " and " + tuple2index + " " + tuple2.m_Index);
		ClusAttrType attr = tuple1.findDiscriminatingAttribute(tuple2);
//		System.out.println("attribute chosen: " + attr.toString());
		if (attr != null) {
			m_FindBestTest.findPert(attr, tuple1, tuple2);
		}
		else {
			// no discriminating attribute can be found, should not occur
			System.out.println("No discriminating attribute found for the two selected tuples. Making leaf...");
			makeLeaf(node);
			return;
		}
		
		// Partition data + recursive calls
		CurrentBestTestAndHeuristic best = m_FindBestTest.getBestTest();
		if (best.hasBestTest()) {
//			start_time = System.currentTimeMillis();
			
			node.testToNode(best);
			// Output best test
			if (Settings.VERBOSE > 0) System.out.println("Test: "+node.getTestString()+" -> "+best.getHeuristicValue());
			// Create children
			int arity = node.updateArity();
			NodeTest test = node.getTest();
			RowData[] subsets = new RowData[arity];
			for (int j = 0; j < arity; j++) {
				subsets[j] = data.applyWeighted(test, j);
			}
			if (getSettings().showAlternativeSplits()) {
				filterAlternativeSplits(node, data, subsets);
			}
			if (node != m_Root && getSettings().hasTreeOptimize(Settings.TREE_OPTIMIZE_NO_INODE_STATS)) {
				// Don't remove statistics of root node; code below depends on them
				node.setClusteringStat(null);
				node.setTargetStat(null);
			}
			for (int j = 0; j < arity; j++) {
				ClusNode child = new ClusNode();
				node.setChild(child, j);
				child.initClusteringStat(m_StatManager, m_Root.getClusteringStat(), subsets[j]);
				child.initTargetStat(m_StatManager, m_Root.getTargetStat(), subsets[j]);
				inducePert(child, subsets[j]);
			}
		} else {
			makeLeaf(node);
		}
	}*/
	
	
	public void rankFeatures(ClusNode node, RowData data) throws IOException {
		// Find best test
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream("ranking.csv")));
		ClusAttrType[] attrs = getDescriptiveAttributes();
		for (int i = 0; i < attrs.length; i++) {
			ClusAttrType at = attrs[i];
			initSelectorAndStopCrit(node, data);
			if (at instanceof NominalAttrType) m_FindBestTest.findNominal((NominalAttrType)at, data);
			else m_FindBestTest.findNumeric((NumericAttrType)at, data);			
			CurrentBestTestAndHeuristic cbt = m_FindBestTest.getBestTest();
			if (cbt.hasBestTest()) {
				NodeTest test = cbt.updateTest();
				wrt.print(cbt.m_BestHeur);
				wrt.print(",\""+at.getName()+"\"");
				wrt.println(",\""+test+"\"");
			}
		}
		wrt.close();
	}
	
	public void initSelectorAndSplit(ClusStatistic stat) throws ClusException {
		m_FindBestTest.initSelectorAndSplit(stat);
	}

	public void setInitialData(ClusStatistic stat, RowData data) throws ClusException {
		m_FindBestTest.setInitialData(stat,data);
	}

	public void cleanSplit() {
		m_FindBestTest.cleanSplit();
	}

	public ClusNode induceSingleUnpruned(RowData data) throws ClusException, IOException {
		m_Root = null;
		// Begin of induction process
		int nbr = 0;
		while (true) {
			nbr++;
			// Init root node
			m_Root = new ClusNode();
			m_Root.initClusteringStat(m_StatManager, data);
			m_Root.initTargetStat(m_StatManager, data);
			m_Root.getClusteringStat().showRootInfo();
			initSelectorAndSplit(m_Root.getClusteringStat());
			setInitialData(m_Root.getClusteringStat(),data);
			// Induce the tree
			data.addIndices();
			/*if (getSettings().isEnsembleMode() && getSettings().getEnsembleMethod() == getSettings().ENSEMBLE_PERT) {
				inducePert(m_Root, data);
			}
			else {*/
			induce(m_Root, data);
			/*}*/
			// rankFeatures(m_Root, data);
			// Refinement finished
			if (Settings.EXACT_TIME == false) break;
		}
		m_Root.postProc(null);

		cleanSplit();
		return m_Root;
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		return induceSingleUnpruned((RowData)cr.getTrainingSet());
	}
}

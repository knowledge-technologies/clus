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
 * Created on Apr 5, 2005
 */
package clus.ext.beamsearch;

import clus.algo.tdidt.ClusNode;
import clus.main.*;

import java.util.*;
import java.io.*;

public class ClusBeam {

	TreeMap m_Tree;
	Collection m_Values;
	int m_MaxWidth;
	int m_CrWidth;
	boolean m_RemoveEqualHeur;
	double m_MinValue = Double.NEGATIVE_INFINITY;
	double m_BeamSimilarity;

	public ClusBeam(int width, boolean rmEqHeur) {
		m_Tree = new TreeMap(); //trees in the beam
		m_Values = m_Tree.values();
		m_MaxWidth = width;
		m_RemoveEqualHeur = rmEqHeur;
	}

	//add a tree to the beam if it not already there
	public int addIfNotIn(ClusBeamModel model) {
		Double key = new Double(model.getValue());
		ClusBeamTreeElem found = (ClusBeamTreeElem)m_Tree.get(key);
		if (found == null) {
			m_Tree.put(key, new ClusBeamTreeElem(model));
			return 1;
		} else {
			if (m_RemoveEqualHeur) {
				found.setObject(model);
				return 0;
			} else {
				return found.addIfNotIn(model);
			}
		}
	}

	public void removeMin() {
		Object first_key = m_Tree.firstKey();
		ClusBeamTreeElem min_node = (ClusBeamTreeElem)m_Tree.get(first_key);
		if (min_node.hasList()) {
			min_node.removeFirst();
		} else {
			m_Tree.remove(first_key);
		}
	}

	public ClusBeamModel getBestAndSmallestModel() {
		ClusBeamTreeElem elem = (ClusBeamTreeElem)m_Tree.get(m_Tree.lastKey());
		if (elem.hasList()) {
			double value = Double.POSITIVE_INFINITY;
			ClusBeamModel result = null;
			ArrayList arr = elem.getOthers();
			for (int i = 0; i < arr.size(); i++) {
				ClusBeamModel model = (ClusBeamModel)arr.get(i);
				int size = model.getModel().getModelSize();
				if (size < value) {
					value = size;
					result = model;
				}
			}
			return result;
		} else {
			return (ClusBeamModel)elem.getObject();
		}
	}

	public ClusBeamModel getBestModel() {
		ClusBeamTreeElem elem = (ClusBeamTreeElem)m_Tree.get(m_Tree.lastKey());
		return (ClusBeamModel)elem.getAnObject();
	}

	public ClusBeamModel getWorstModel() {
		ClusBeamTreeElem elem = (ClusBeamTreeElem)m_Tree.get(m_Tree.firstKey());
		return (ClusBeamModel)elem.getAnObject();
	}

	public double computeMinValue() {
		return ((Double)m_Tree.firstKey()).doubleValue();
	}

	public void addModel(ClusBeamModel model) {
		double value = model.getValue();
		if(m_MaxWidth == -1){ //the size ot the beam is infinite
			//System.out.println("try to add model :");
			//ClusNode tree = (ClusNode)model.getModel();
			//tree.printTree();
			m_CrWidth += addIfNotIn(model);
			//if (addIfNotIn(model) == 1){System.out.println("we add a model");m_CrWidth +=1;}
		}
		else
		{
		if (m_CrWidth < m_MaxWidth) {
			m_CrWidth += addIfNotIn(model);
			if (m_CrWidth == m_MaxWidth) {
				m_MinValue = computeMinValue();
			}
		} else if (value >= m_MinValue) {
			if (addIfNotIn(model) == 1) {
				removeMin();
				double min = computeMinValue();
				// System.out.println("*** Removing model: "+min);
				m_MinValue = min;
			}
		}
		}
	}

	public void print(PrintWriter wrt, int best_n) {
		ArrayList lst = toArray();
		for (int i = 0; i < Math.min(best_n, lst.size()); i++) {
			if (i != 0) wrt.println();
			ClusBeamModel mdl = (ClusBeamModel)lst.get(lst.size()-i-1);
			ClusNode tree = (ClusNode)mdl.getModel();
			double error = Double.NaN; // tree.estimateError();
			wrt.println("Model: "+i+" value: "+mdl.getValue()+" error: "+error+" parent: "+mdl.getParentModelIndex());
			tree.printModel(wrt);
		}
	}

	public Iterator getIterator() {
		return m_Values.iterator();
	}

	public ArrayList toArray() {
		ArrayList lst = new ArrayList();
		Iterator iter = m_Values.iterator();
		while (iter.hasNext()) {
			ClusBeamTreeElem elem = (ClusBeamTreeElem)iter.next();
			elem.addAll(lst);
		}
		return lst;
	}

	public int getMaxWidth() {
		return m_MaxWidth;
	}

	public int getCrWidth() {
		return m_CrWidth;
	}

	public double getMinValue() {
		return m_MinValue;
	}

	public void print() {
/*		System.out.println("Beam:");
		m_Tree.printStructure();
		System.out.println("All:");
		m_Tree.printTree();
		System.out.println("Done"); */
	}

	/**Dragi
	 * This part is used only when we have similarity constraints
	 * @param model - candidate model
	 * @return 0 - no change in the beam (the candidate didn't entered the beam)
	 * @return 1 - change in the beam (the candidate entered the beam)
	 */
	public int removeMinUpdated(ClusBeamModel candidate){
		// until we reach the Beam Width we put all models in

		if (m_CrWidth < m_MaxWidth) {
			m_CrWidth += addIfNotIn(candidate);
//			String info = "BEAM SIZE = "+getCrWidth();
//			printBeamTrees(info);
			return 1;
		}
//		String info = "BEAM SIZE = "+getCrWidth();
//		printBeamTrees(info);
//		NumberFormat form = ClusFormat.makeNAfterDot(8);
//		double candidateSimilarity = Double.parseDouble(form.format(1 - candidate.getDistanceToBeam()/getCrWidth()));
		double candidateSimilarity = 1 - candidate.getDistanceToBeam()/getCrWidth();
		double currentMin = candidate.getValue() - Settings.BEAM_SIMILARITY * candidateSimilarity;
		ArrayList arr = toArray();
		double modelUpdatedHeur,modelSimilarity;
		int bsize = arr.size();
		int min_pos = bsize;
		for (int i = 0; i < bsize; i++){
			modelSimilarity = 1 - ((ClusBeamModel)arr.get(i)).getDistanceToBeam()/getCrWidth();
//			modelSimilarity = Double.parseDouble(form.format(1 - ((ClusBeamModel)arr.get(i)).getDistanceToBeam()/getCrWidth()));
			modelUpdatedHeur = ((ClusBeamModel)arr.get(i)).getValue() - Settings.BEAM_SIMILARITY *  modelSimilarity;
			if ((currentMin == modelUpdatedHeur)&& m_RemoveEqualHeur){
				//this is for handling the case when the updated heuristic is equal
				//with this the candidate doesn't enter the beam
				min_pos = bsize;
				break;
			}
			else if (currentMin > modelUpdatedHeur){
					min_pos = i;
					currentMin = modelUpdatedHeur;
					}
		}
		if (min_pos != bsize){
			TreeMap temp = new TreeMap();
			ClusBeamModel cbm;
			ClusBeamTreeElem found;
			for (int j = 0; j <= bsize; j++){
				if (j != min_pos){
					if (j !=bsize)	cbm = (ClusBeamModel)arr.get(j);
					else cbm = candidate;
					found = (ClusBeamTreeElem) temp.get(Double.valueOf(cbm.getValue()));
					if (found == null) temp.put(Double.valueOf(cbm.getValue()), new ClusBeamTreeElem(cbm));
					else found.addIfNotIn(cbm);
				}
			}
			m_Tree = temp;
			m_Values = m_Tree.values();
			m_MinValue = computeMinValue();
			return 1;
		}
		return 0;
	}

		public int removeMinUpdatedOpt(ClusBeamModel candidate, ClusBeamModelDistance distance){
			// until we reach the Beam Width we put all models in
			if (m_CrWidth < m_MaxWidth) {
				m_CrWidth += addIfNotIn(candidate);
//				String info = "BEAM SIZE = "+getCrWidth();
//				printBeamTrees(info);
				return 1;
			}
//			String info = "BEAM SIZE = "+getCrWidth();
//			printBeamTrees(info);
//			NumberFormat form = ClusFormat.makeNAfterDot(10);
//			double candidateSimilarity = Double.parseDouble(form.format(1 - candidate.getDistanceToBeam()/getCrWidth()));
			double candidateSimilarity = 1 - candidate.getDistanceToBeam()/getCrWidth();
			double currentMin = candidate.getValue() - Settings.BEAM_SIMILARITY * candidateSimilarity;
			ArrayList arr = toArray();
			int bsize = arr.size();
			int min_pos = bsize;
			double modelUpdatedHeur,modelDistance;

			for (int i = 0; i < bsize; i++){
//				modelDistance = Double.parseDouble(form.format(1 - ((ClusBeamModel)arr.get(i)).getDistanceToBeam()/getCrWidth()));
				modelDistance = 1 - ((ClusBeamModel)arr.get(i)).getDistanceToBeam()/getCrWidth();
				modelUpdatedHeur = ((ClusBeamModel)arr.get(i)).getValue() - Settings.BEAM_SIMILARITY * modelDistance;

				if ((currentMin == modelUpdatedHeur)&& m_RemoveEqualHeur){
					//this is for handling the case when the updated heuristic is equal
					//with this the candidate doesn't enter the beam
					min_pos = bsize;
					break;
				}
				else if (currentMin > modelUpdatedHeur){
						min_pos = i;
						currentMin = modelUpdatedHeur;
						}
			}
			distance.deductFromBeamOpt(this, candidate, min_pos);
			if (min_pos != bsize){
				TreeMap temp = new TreeMap();
				ClusBeamModel cbm;
				ClusBeamTreeElem found;
				for (int j = 0; j <= bsize; j++){
					if (j != min_pos){
						if (j !=bsize)	cbm = (ClusBeamModel)arr.get(j);
						else cbm = candidate;
						found = (ClusBeamTreeElem) temp.get(Double.valueOf(cbm.getValue()));
						if (found == null) temp.put(Double.valueOf(cbm.getValue()), new ClusBeamTreeElem(cbm));
						else found.addIfNotIn(cbm);
					}
				}
				m_Tree = temp;
				m_Values = m_Tree.values();
				m_MinValue = computeMinValue();
				return 1;
			}
			return 0;
		}


	/**Dragi
	 * check if the same tree is already in the beam
	 * @param model
	 * @return
	 */
	public boolean modelAlreadyIn(ClusBeamModel model){
		ArrayList arr = toArray();
		ClusBeamModel bmodel;
		for (int k = 0; k < arr.size(); k++){
			bmodel = (ClusBeamModel)arr.get(k);
			if (((ClusNode)bmodel.getModel()).equals(((ClusNode)model.getModel())))return true;
		}
		return false;
	}

	public void setBeamSimilarity(double similarity){
		m_BeamSimilarity = similarity;
	}

	public double getBeamSimilarity(){
		return m_BeamSimilarity;
	}
}

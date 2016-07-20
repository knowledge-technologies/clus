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

package clus.ext.beamsearch;

import clus.*;
import clus.algo.*;
import clus.algo.split.*;
import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.ensembles.*;
import clus.main.*;
import clus.util.*;
import clus.pruning.*;
import clus.model.*;
import clus.model.modelio.*;

import java.io.*;
import java.util.ArrayList;

public class ClusBeamInduce extends ClusInductionAlgorithm {

	protected NominalSplit m_Split;
	protected ClusBeamSearch m_Search;

	public ClusBeamInduce(ClusSchema schema, Settings sett, ClusBeamSearch search) throws ClusException, IOException {
		super(schema, sett);
		m_Search = search;
	}

	public void initializeHeuristic() {
		m_Search.initializeHeuristic();
	}

	public boolean isModelWriter() {
		return true;
	}

	public void writeModel(ClusModelCollectionIO strm) throws IOException {
		m_Search.writeModel(strm);
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		ClusNode root = m_Search.beamSearch(cr);
		root.updateTree();
		return root;
	}

	public void induceAll(ClusRun cr) throws ClusException, IOException {
		m_Search.beamSearch(cr);
		ClusModelInfo def_model = cr.addModelInfo(ClusModel.DEFAULT);
		def_model.setModel(ClusDecisionTree.induceDefault(cr));
		def_model.setName("Default");
		ArrayList lst = m_Search.getBeam().toArray();
		updateAllPredictions(lst);

		//the pruning is ON for all setings! This could be turned off when needed!
		if (getSettings().getBeamTreeMaxSize() <= -1) postPruneBeamModels(cr, lst);
		if (getSettings().getBeamSortOnTrainParameter()) sortModels(cr, lst);
//		if (!getSettings().isFastBS()) writeSimilarityFile(lst, cr);
		ClusBeamSimilarityOutput bsimout = new ClusBeamSimilarityOutput(getSettings());
		bsimout.appendToFile(lst, cr);
		boolean toForest = cr.getStatManager().getSettings().isBeamToForest();
		ClusForest bForest = new ClusForest(getStatManager());

		for (int i = 0; i < lst.size(); i++) {
			ClusBeamModel mdl = (ClusBeamModel)lst.get(lst.size()-i-1);
			ClusModelInfo model_info = cr.addModelInfo(i+1);
			ClusNode tree = (ClusNode)mdl.getModel();
			model_info.setModel(tree);
			model_info.setName("Beam "+(i+1));
			model_info.clearAll();
			if (toForest) bForest.addModelToForest((ClusModel)tree);
		}
		if (toForest){
			ClusModelInfo forest_info = cr.addModelInfo(lst.size()+1);
			forest_info.setModel(bForest);
			forest_info.setName("BeamToForest");
		}
	}

	/**Dragi, JSI
	 *Post Pruning of the models in the beam
	 *
	 * @param cr - ClusRun
	 * @param arr - List with the beam
	 * @throws ClusException
	 */
	public void postPruneBeamModels(ClusRun cr, ArrayList arr) throws ClusException{
		updateAllPredictions(arr);
		for (int i = 0; i < arr.size(); i++) {
			    PruneTree pruner = getStatManager().getTreePruner(null);
			    pruner.setTrainingData((RowData)cr.getTrainingSet());
			    ClusNode tree = (ClusNode)((ClusBeamModel)arr.get(i)).getModel();
			    pruner.prune(tree);
		}
	}

	public void updateAllPredictions(ArrayList arr) {
		for (int i = 0; i < arr.size(); i++) {
		    ClusNode tree = (ClusNode)((ClusBeamModel)arr.get(i)).getModel();
		    tree.updateTree();
		}
	}

	/**Dragi, JSI
	 * Sorts the beam according to train accuracy/correlation in descending order
	 * In case of equal train accuracy/correlation
	 * then the tree with greater heuristic score are put higher
	 * @param cr - Clus Run
	 * @param arr	- List with the beam
	 * @throws ClusException
	 * @throws ClusException
	 * @throws IOException
	 */
	public void sortModels(ClusRun cr, ArrayList arr) throws ClusException, IOException{
//		if (cr.getStatManager().getSettings().getBeamTreeMaxSize() <= -1) {
//			postPruneBeamModels(cr, arr);
//		}
		int size = arr.size();
		ClusBeamModel[] models = new ClusBeamModel[size];
		double[]err = new double [size];
		double[]heur = new double [size];
		for (int i = 0 ; i < size; i++){
			models[i] = (ClusBeamModel)arr.get(i);
			err[i] = Clus.calcModelError(cr.getStatManager(), (RowData)cr.getTrainingSet(), models[i].getModel());
			heur[i] = models[i].getValue();
		}
		ClusBeamModel cbm;
		double tmp;
		for (int j = 0; j < size -1; j++)
			for (int k = j+1; k < size; k++){
				if (err[j]>err[k]){
					cbm = models [j];
					models [j] = models [k];
					models [k] = cbm;
					tmp = err [j];
					err [j] = err [k];
					err [k] = tmp;
					tmp = heur [j];
					heur [j] = heur [k];
					heur [k] = tmp;
				}
				else if (err[j]==err[k]){
						if (heur[j]<heur[k]){
							cbm = models [j];
							models [j] = models [k];
							models [k] = cbm;
							tmp = err [j];
							err [j] = err [k];
							err [k] = tmp;
							tmp = heur [j];
							heur [j] = heur [k];
							heur [k] = tmp;
						}
				}
			}
		arr.clear();
		for (int m = 0; m < size; m++)arr.add(models[m]);
	}

}

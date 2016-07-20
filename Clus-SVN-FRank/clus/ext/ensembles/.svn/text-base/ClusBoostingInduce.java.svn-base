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

package clus.ext.ensembles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import jeans.util.array.MDoubleArray;

import clus.algo.*;
import clus.algo.tdidt.*;
import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.*;
import clus.main.*;
import clus.model.*;
import clus.statistic.ClusStatistic;
import clus.util.*;

// Method based on:
// "Improving Regressors using Boosting Techniques" by Harris Drucker

public class ClusBoostingInduce extends ClusInductionAlgorithm {

	Random m_Random = new Random(0);

	public ClusBoostingInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	public double[] computeNormalizedLoss(RowData trainData, ClusNode tree) {
		ClusAttributeWeights weights = getStatManager().getClusteringWeights();
		double[] L = new double[trainData.getNbRows()];
		for (int i = 0; i < trainData.getNbRows(); i++) {
			DataTuple tuple = trainData.getTuple(i);
			ClusStatistic prediction = tree.predictWeighted(tuple);
			L[i] = prediction.getSquaredDistance(tuple, weights);
		}
		double D = MDoubleArray.max(L);
		MDoubleArray.dotscalar(L, 1.0/D);
		return L;
	}

	public double computeAverageLoss(RowData trainData, double[] L) {
		double avg = 0.0;
		double tot_w = trainData.getSumWeights();
		for (int i = 0; i < trainData.getNbRows(); i++) {
			DataTuple tuple = trainData.getTuple(i);
			avg += L[i] * tuple.getWeight() / tot_w;
		}
		return avg;
	}

	public void updateWeights(RowData trainData, double[] L, double beta) {
		for (int i = 0; i < trainData.getNbRows(); i++) {
			DataTuple tuple = trainData.getTuple(i);
			tuple.setWeight(tuple.getWeight() * Math.pow(beta, 1 - L[i]));
		}
	}

	public ClusBoostingForest induceSingleUnprunedBoosting(ClusRun cr) throws ClusException, IOException {
		ClusBoostingForest result = new ClusBoostingForest(getStatManager());
		RowData trainData = ((RowData)cr.getTrainingSet()).shallowCloneData();
		DepthFirstInduce tdidt;
		if (getSchema().isSparse()) {
			tdidt = new DepthFirstInduceSparse(this);
		}
		else {
			tdidt = new DepthFirstInduce(this);
		}
		int[] outputEnsembleAt = getSettings().getNbBaggingSets().getIntVectorSorted();
		int nbTrees = outputEnsembleAt[outputEnsembleAt.length-1];
		int verbose = Settings.VERBOSE;
		for (int i = 0; i < nbTrees; i++) {
			if (verbose != 0) {
				System.out.println();
				System.out.println("Tree: "+i+" (of max: "+nbTrees+")");
			}
			RowData train = trainData.sampleWeighted(m_Random);
			ClusNode tree = tdidt.induceSingleUnpruned(train);
			double[] L = computeNormalizedLoss(trainData, tree);
			double Lbar = computeAverageLoss(trainData, L);
			double beta = Lbar / (1-Lbar);
			if (verbose != 0) {
				System.out.println("Average loss: "+Lbar+" beta: "+beta);
			}
			if (Lbar >= 0.5) break;
			updateWeights(trainData, L, beta);
			result.addModelToForest(tree, beta);
		}
		return result;
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		return induceSingleUnprunedBoosting(cr);
	}

	public void induceAll(ClusRun cr) throws ClusException, IOException {
		ClusBoostingForest model = induceSingleUnprunedBoosting(cr);
		ClusModelInfo default_model = cr.addModelInfo(ClusModel.DEFAULT);
		ClusModel def = ClusDecisionTree.induceDefault(cr);
		default_model.setModel(def);
		default_model.setName("Default");
		ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL);
		model_info.setName("Original");
		model_info.setModel(model);
		if (cr.getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL) {
			double[] thresholds = cr.getStatManager().getSettings().getClassificationThresholds().getDoubleVector();
			if (thresholds != null) {
				for (int i = 0; i < thresholds.length; i++) {
					ClusModelInfo pruned_info = cr.addModelInfo(ClusModel.PRUNED + i);
					ClusBoostingForest new_forest = model.cloneBoostingForestWithThreshold(thresholds[i]);
					new_forest.setPrintModels(Settings.isPrintEnsembleModels());
					pruned_info.setModel(new_forest);
					pruned_info.setName("T("+thresholds[i]+")");
				}
			}
		}
	}
}

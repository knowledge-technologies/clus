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

package addon.hmc.HMCAverageSingleClass;

import java.io.IOException;
import java.util.ArrayList;

import clus.main.*;
import clus.model.*;
import clus.model.modelio.*;
import clus.statistic.*;
import clus.util.*;
import clus.data.rows.*;
import clus.error.ClusErrorList;
import clus.ext.hierarchical.*;
import clus.Clus;

public class HMCAverageNodeWiseModels {

	protected int m_NbModels, m_TotSize;
	protected HMCAverageSingleClass m_Cls;
	protected double[][][] m_PredProb;

	public HMCAverageNodeWiseModels(HMCAverageSingleClass cls, double[][][] predprop) {
		m_Cls = cls;
		m_PredProb = predprop;
	}

	public int getNbModels() {
		return m_NbModels;
	}

	public int getTotalSize() {
		return m_TotSize;
	}

	public ClusStatManager getStatManager() {
		return m_Cls.getStatManager();
	}

	public Settings getSettings() {
		return m_Cls.getSettings();
	}

	public Clus getClus() {
		return m_Cls.getClus();
	}

	public boolean allParentsOk(ClassTerm term, boolean[] computed) {
		for (int j = 0; j < term.getNbParents(); j++) {
			ClassTerm parent = term.getParent(j);
			if (parent.getIndex() != -1 && !computed[parent.getIndex()]) return false;
		}
		return true;
	}

	public void processModels(ClusRun cr) throws ClusException, IOException, ClassNotFoundException {
		ClassHierarchy hier = getStatManager().getHier();
		boolean[] prob_computed = new boolean[hier.getTotal()];
		// All classes still need to be done
		ArrayList todo = new ArrayList();
		for (int i = 0; i < hier.getTotal(); i++) {
			ClassTerm term = hier.getTermAt(i);
			todo.add(term);
		}
		int nb_done = 0;
		while (nb_done < hier.getTotal()) {
			for (int i = todo.size()-1; i >= 0; i--) {
				ClassTerm term = (ClassTerm)todo.get(i);
				if (allParentsOk(term, prob_computed)) {
					// All parents are ok, so now do this class
					doOneClass(term, cr);
					// Ok, now we have done this class
					prob_computed[term.getIndex()] = true;
					todo.remove(i);
					nb_done++;
				}
			}
		}
	}

	public void updateErrorMeasures(ClusRun cr) throws ClusException, IOException {
		ClassHierarchy hier = getStatManager().getHier();
		HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
		for (int traintest = ClusModelInfoList.TRAINSET; traintest <= ClusModelInfoList.TESTSET; traintest++) {
			RowData data = cr.getDataSet(traintest);
			for (int exid = 0; exid < data.getNbRows(); exid++) {
				DataTuple tuple = data.getTuple(exid);
				ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
				for (int clidx = 0; clidx < hier.getTotal(); clidx++) {
					double predicted_weight = m_PredProb[traintest][exid][clidx];
					boolean actually_has_class = tp.hasClass(clidx);
					for (int j = 0; j < pruner.getNbResults(); j++) {
						// update corresponding HierClassWiseAccuracy
						boolean predicted_class = predicted_weight >= pruner.getThreshold(j)/100.0;
						HierClassWiseAccuracy acc = (HierClassWiseAccuracy)m_Cls.getEvalArray(traintest,j).getError(0);
						acc.nextPrediction(clidx, predicted_class, actually_has_class);
					}
				}
			}
			for (int j = 0; j < pruner.getNbResults(); j++) {
				ClusErrorList error = m_Cls.getEvalArray(traintest,j);
				error.setNbExamples(data.getNbRows(), data.getNbRows());
			}
		}
	}

	public void doOneClass(ClassTerm term, ClusRun cr) throws IOException, ClassNotFoundException, ClusException {
		String childName = term.toPathString("=");
		for (int j = 0; j < term.getNbParents(); j++) {
			ClassTerm parent = term.getParent(j);
			// Load model on arc between parent and this node "term"
			String nodeName = parent.toPathString("=");
			String name = getSettings().getAppName() + "-" + nodeName + "-" + childName;
			String toload = "hsc/model/" + name + ".model";
			System.out.println("Loading: "+toload);
			ClusModelCollectionIO io = ClusModelCollectionIO.load(toload);
			ClusModel model = io.getModel("Original");
			if (model == null) {
				throw new ClusException("Error: .model file does not contain model named 'Original'");
			}
			m_NbModels++;
			m_TotSize += model.getModelSize();
			getClus().getSchema().attachModel(model);
			// Make predictions with this model
			for (int traintest = ClusModelInfoList.TRAINSET; traintest <= ClusModelInfoList.TESTSET; traintest++) {
				RowData data = cr.getDataSet(traintest);
				for (int exid = 0; exid < data.getNbRows(); exid++) {
					updatePrediction(data, exid, traintest, model, parent, term);
				}
			}
		}
		// prediction(class) = avg(prediction(parent_i)*prediction(class|parent_i))
		int child_idx = term.getIndex();
		for (int traintest = ClusModelInfoList.TRAINSET; traintest <= ClusModelInfoList.TESTSET; traintest++) {
			RowData data = cr.getDataSet(traintest);
			for (int exid = 0; exid < data.getNbRows(); exid++) {
				m_PredProb[traintest][exid][child_idx] /= term.getNbParents();
			}
		}
	}

	public void updatePrediction(RowData data, int exid, int traintest, ClusModel model, ClassTerm parent, ClassTerm term) {
		DataTuple tuple = data.getTuple(exid);
		ClusStatistic prediction = model.predictWeighted(tuple);
		double[] predicted_distr = prediction.getNumericPred();
		double predicted_prob = predicted_distr[0];
		int parent_idx = parent.getIndex();
		int child_idx = term.getIndex();
		double parent_prob = parent_idx == -1 ? 1.0 : m_PredProb[traintest][exid][parent_idx];
		double child_prob = parent_prob * predicted_prob;
		if (child_prob < m_PredProb[traintest][exid][child_idx]) {
			m_PredProb[traintest][exid][child_idx] = child_prob;
		}
	}
}

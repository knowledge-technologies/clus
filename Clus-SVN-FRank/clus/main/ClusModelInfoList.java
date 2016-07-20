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

package clus.main;

import java.io.*;
import java.util.*;

import clus.error.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.util.*;
import clus.data.type.*;

public abstract class ClusModelInfoList implements Serializable {

	public final static int TRAINSET = 0;
	public final static int TESTSET = 1;
	public final static int VALIDATIONSET = 2;

	protected ClusModelInfo m_AllModelsMI = new ClusModelInfo("AllModels");
	protected ArrayList m_Models = new ArrayList();
	protected long m_IndTime, m_PrepTime, m_PruneTime;

/***************************************************************************
 * Iterating over models
 ***************************************************************************/

	public int getNbModels() {
		return m_Models.size();
	}

	/**
	 * @param i Usually ClusModel model type (Default, Original, Pruned).
	 * 			However, can also be something else. For example for ClusForest this is the index of decision tree.
	 * @return
	 */
	public ClusModelInfo getModelInfo(int i) {
		if (i >= m_Models.size()) return null;
		return (ClusModelInfo)m_Models.get(i);
	}

	/**
	 * @param i Usually ClusModel model type (Default, Original, Pruned).
	 * @param j If model "i" does not exist, return model "j".
	 *          Typical use: getModelInfoFallback(ClusModel.PRUNED, ClusModel.ORIGINAL);
	 * @return
	 */
	public ClusModelInfo getModelInfoFallback(int i, int j) {
		ClusModelInfo info = getModelInfo(i);
		if (info == null) info = getModelInfo(j);
		return info;
	}

	public ClusModelInfo getAllModelsMI() {
		return m_AllModelsMI;
	}

	/**
	 * @param i Usually ClusModel model type (Default, Original, Pruned).
	 * 			However, can also be something else. For example for ClusForest this is the index of decision tree.
	 * @return
	 */
	public void setModelInfo(int i, ClusModelInfo info) {
		m_Models.set(i, info);
	}

	/**
	 * @param i Usually ClusModel model type (Default, Original, Pruned).
	 * 			However, can also be something else. For example for ClusForest this is the index of decision tree.
	 * @return
	 */
	public ClusModel getModel(int i) {
		return getModelInfo(i).getModel();
	}

	/**
	 * @param i Usually ClusModel model type (Default, Original, Pruned).
	 * 			However, can also be something else. For example for ClusForest this is the index of decision tree.
	 * @return
	 */
	public String getModelName(int i) {
		return getModelInfo(i).getName();
	}

	public void setModels(ArrayList models) {
		m_Models = models;
	}

	public void showModelInfos() {
		for (int i = 0; i < getNbModels(); i++) {
			ClusModelInfo info = (ClusModelInfo)getModelInfo(i);
			System.out.println("Model "+i+" name: '"+info.getName()+"'");
		}
	}

	/***************************************************************************
	 * Adding models to it
	 ***************************************************************************/

	public ClusModelInfo initModelInfo(int i) {
		String name = "M" + (i + 1);
		if (i == ClusModel.DEFAULT) name = "Default";
		if (i == ClusModel.ORIGINAL) name = "Original";
		if (i == ClusModel.PRUNED) name = "Pruned";
		ClusModelInfo inf = new ClusModelInfo(name);
		initModelInfo(inf);
		return inf;
	}

	public void initModelInfo(ClusModelInfo inf) {
		inf.setSelectedErrorsClone(getTrainError(), getTestError(), getValidationError());
		inf.setStatManager(getStatManager());
	}

	public ClusModelInfo addModelInfo(String name) {
		ClusModelInfo inf = new ClusModelInfo(name);
		addModelInfo(inf);
		return inf;
	}

	public void addModelInfo(ClusModelInfo inf) {
		initModelInfo(inf);
		m_Models.add(inf);
	}

	public ClusModelInfo addModelInfo(int i) {
		while (i >= m_Models.size()) m_Models.add(null);
		ClusModelInfo inf = (ClusModelInfo)m_Models.get(i);
		if (inf == null) {
			inf = initModelInfo(i);
			m_Models.set(i, inf);
		}
		return inf;
	}

	public abstract ClusStatManager getStatManager();

	public abstract ClusErrorList getTrainError();

	public abstract ClusErrorList getTestError();

	public abstract ClusErrorList getValidationError();

/***************************************************************************
 * Functions for all models
 ***************************************************************************/

	public ArrayList cloneModels() {
		int nb_models = getNbModels();
		ArrayList clones = new ArrayList();
		for (int i = 0; i < nb_models; i++) {
			ClusModelInfo my = getModelInfo(i);
			if (my != null) my = my.cloneModelInfo(); 
			clones.add(my);
		}
		return clones;
	}

	public void deleteModels() {
		int nb_models = getNbModels();
		for (int i = 0; i < nb_models; i++) {
			ClusModelInfo my = getModelInfo(i);
			my.deleteModel();
		}
	}

	public void checkModelInfo() {
		int nb_models = getNbModels();
		for (int i = 0; i < nb_models; i++) {
			ClusModelInfo my = getModelInfo(i);
			my.check();
		}
	}

	public boolean hasModel(int i) {
		ClusModelInfo my = getModelInfo(i);
		return my.getNbModels() > 0;
	}

	public void copyAllModelsMIs() {
		ClusModelInfo allmi = getAllModelsMI();
		int nb_models = getNbModels();
		for (int i = 0; i < nb_models; i++) {
			ClusModelInfo my = getModelInfo(i);
			if (my != null) allmi.copyModelProcessors(my);
		}
	}

	public void initModelProcessors(int type, ClusSchema schema)  throws IOException, ClusException {
		ClusModelInfo allmi = getAllModelsMI();
		allmi.initAllModelProcessors(type, schema);
		for (int i = 0; i < getNbModels(); i++) {
			ClusModelInfo mi = getModelInfo(i);
			if (mi != null) mi.initModelProcessors(type, schema);
		}
	}

	public void initEnsemblePredictionsWriter(int type){
		ClusModelInfo mi = getModelInfo(ClusModel.ORIGINAL); // for ensembles we consider only the original at thsi point
		mi.initEnsemblePredictionWriter(type);
	}

	public void termModelProcessors(int type)  throws IOException, ClusException {
		ClusModelInfo allmi = getAllModelsMI();
		allmi.termAllModelProcessors(type);
		for (int i = 0; i < getNbModels(); i++) {
			ClusModelInfo mi = getModelInfo(i);
			if (mi != null) mi.termModelProcessors(type);
		}
	}

	public void termEnsemblePredictionsWriter(int type){
		ClusModelInfo mi = getModelInfo(ClusModel.ORIGINAL); // for ensembles we consider only the original at thsi point
		mi.terminateEnsemblePredictionWriter(type);
	}

/***************************************************************************
 * Induction time
 ***************************************************************************/

	public final void setInductionTime(long time) {
		m_IndTime = time;
	}

	public final long getInductionTime() {
		return m_IndTime;
	}

	public final void setPruneTime(long time) {
		m_PruneTime = time;
	}

	public final long getPruneTime() {
		return m_PruneTime;
	}

	public final void setPrepareTime(long time) {
		m_PrepTime = time;
	}

	public final long getPrepareTime() {
		return m_PrepTime;
	}
}

/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.main;

import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.util.ClusException;

public class ClusSummary extends ClusModelInfoList {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_Runs;
	protected int m_TotalRuns = 1;
	protected ClusErrorList m_TrainErr;
	protected ClusErrorList m_TestErr;
	protected ClusErrorList m_ValidErr;
	protected ClusStatManager m_StatMgr;

	public void resetAll() {
		m_Models.clear();
		m_IndTime = 0;
		m_PrepTime = 0;
		m_PruneTime = 0;
		m_PredictionTime = 0;
	}

	public void setStatManager(ClusStatManager mgr) {
		m_StatMgr = mgr;
	}

	@Override
	public ClusStatManager getStatManager() {
		return m_StatMgr;
	}

	@Override
	public ClusErrorList getTrainError() {
		return m_TrainErr;
	}

	@Override
	public ClusErrorList getTestError() {
		return m_TestErr;
	}

	@Override
	public ClusErrorList getValidationError() {
		return m_ValidErr;
	}

	public boolean hasTestError() {
		return m_TestErr != null;
	}

	public void setTrainError(ClusErrorList err) {
		m_TrainErr = err;
	}

	public void setTestError(ClusErrorList err) {
		m_TestErr = err;
	}

	public void setValidationError(ClusErrorList err) {
		m_ValidErr = err;
	}

	public int getNbRuns() {
		return m_Runs;
	}

	public int getTotalRuns() {
		return m_TotalRuns;
	}

	public void setTotalRuns(int tot) {
		m_TotalRuns = tot;
	}

	public ClusSummary getSummaryClone() throws ClusException {
		ClusSummary summ = new ClusSummary();
		summ.m_StatMgr = getStatManager();
		if (m_TestErr != null)
			summ.m_TestErr = m_TestErr.getErrorClone();
		if (m_TrainErr != null)
			summ.m_TrainErr = m_TrainErr.getErrorClone();
		if (m_ValidErr != null)
			summ.m_ValidErr = m_ValidErr.getErrorClone();
		summ.setModels(cloneModels());
		return summ;
	}

	public void addSummary(ClusRun cr) throws ClusException {
		m_Runs++;
		m_IndTime += cr.getInductionTime();
		m_PruneTime += cr.getPruneTime();
		m_PredictionTime += cr.getPredictionTime();
		m_PredictionTimeNbExamples += cr.getPredictionTimeNbExamples();
		
		m_PrepTime += cr.getPrepareTime();
		int nb_models = cr.getNbModels();
		for (int i = 0; i < nb_models; i++) {
			ClusModelInfo mi = cr.getModelInfo(i);
			if (mi != null) {
				ClusModelInfo my = addModelInfo(i);
				my.add(mi);
			}
		}
	}
}

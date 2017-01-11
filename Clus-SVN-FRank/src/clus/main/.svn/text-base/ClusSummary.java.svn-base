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

import clus.error.*;
import clus.model.ClusModelInfo;
import clus.util.ClusException;

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
		m_IndTime = 0; m_PrepTime = 0; m_PruneTime = 0;
	}

	public void setStatManager(ClusStatManager mgr) {
		m_StatMgr = mgr;
	}

	public ClusStatManager getStatManager() {
		return m_StatMgr;
	}

	public ClusErrorList getTrainError() {
		return m_TrainErr;
	}

	public ClusErrorList getTestError() {
		return m_TestErr;
	}

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

	public ClusSummary getSummaryClone() {
		ClusSummary summ = new ClusSummary();
		summ.m_StatMgr = getStatManager();
		summ.setModels(cloneModels());
		return summ;
	}

	public void addSummary(ClusRun cr) throws ClusException {
		m_Runs++;
		m_IndTime += cr.getInductionTime();
		m_PruneTime += cr.getPruneTime();
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

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

package clus.algo;

import clus.main.*;
import clus.data.ClusData;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.ClusStatistic;
import clus.util.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.model.modelio.*;

import java.io.*;

/**
 * Subclasses should implement:
 *
 *	public ClusModel induceSingleUnpruned(ClusRun cr);
 *
 * In addition, subclasses may also want to implement (to return more than one model):
 *
 * 	public void induceAll(ClusRun cr);
 *
 */

public abstract class ClusInductionAlgorithm {

	protected ClusSchema m_Schema;
	protected ClusStatManager m_StatManager;

	public ClusInductionAlgorithm(ClusSchema schema, Settings sett) throws ClusException, IOException {
		m_Schema = schema;
		m_StatManager = new ClusStatManager(schema, sett);
	}

	public ClusInductionAlgorithm(ClusInductionAlgorithm other) {
		m_Schema = other.m_Schema;
		m_StatManager = other.m_StatManager;
	}

	public ClusSchema getSchema() {
		return m_Schema;
	}

	public ClusStatManager getStatManager() {
		return m_StatManager;
	}

	/**
	 * Returns the settings given in the settings file (.s).
	 * @return The settings object.
	 */
	public Settings getSettings() {
		return m_StatManager.getSettings();
	}

	public void initialize() throws ClusException, IOException {
		m_StatManager.initStatisticAndStatManager();
	}

	public void getPreprocs(DataPreprocs pps) {
		m_StatManager.getPreprocs(pps);
	}

	public boolean isModelWriter() {
		return false;
	}

	public void writeModel(ClusModelCollectionIO strm) throws IOException {
	}

	public ClusData createData() {
		return new RowData(m_Schema);
	}

	public void induceAll(ClusRun cr) throws ClusException, IOException {
		ClusModel model = induceSingleUnpruned(cr);
		ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL);
		model_info.setModel(model);
	}

	public abstract ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException;

	public void initializeHeuristic() {
	}

	public ClusStatistic createTotalClusteringStat(RowData data) {
		ClusStatistic stat = m_StatManager.createClusteringStat();
		stat.setSDataSize(data.getNbRows());
		data.calcTotalStat(stat);
		stat.optimizePreCalc(data);
		return stat;
	}

	/**
	 * Compute the statistics for all the (rows in the) data.
	 */
	public ClusStatistic createTotalTargetStat(RowData data) {
		ClusStatistic stat = m_StatManager.createTargetStat();
		stat.setSDataSize(data.getNbRows());
		data.calcTotalStat(stat);
		stat.optimizePreCalc(data);
		return stat;
	}
}

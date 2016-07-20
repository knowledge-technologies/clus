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

/*
 * Created on Jan 18, 2006
 */

import java.io.*;
import java.util.*;

import jeans.util.MyArray;
import jeans.util.array.*;

import clus.ext.hierarchical.*;
import clus.data.rows.*;
import clus.main.*;
import clus.model.*;
import clus.statistic.*;
import clus.util.ClusException;
import clus.algo.tdidt.*;

public class HMCAverageTreeModel implements ClusModel {

	protected int m_DataSet, m_Trees, m_TotSize;
	protected WHTDStatistic m_Target;
	protected double[][][] m_PredProb;

	public HMCAverageTreeModel(ClusStatistic target, double[][][] predprop, int trees, int size) {
		m_Target = (WHTDStatistic)target;
		m_PredProb = predprop;
		m_Trees = trees;
		m_TotSize = size;
	}

	public ClusStatistic predictWeighted(DataTuple tuple) {
		WHTDStatistic stat = (WHTDStatistic)m_Target.cloneSimple();
		stat.setMeans(m_PredProb[m_DataSet][tuple.getIndex()]);
    	return stat;
	}

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
	}

	public int getModelSize() {
		return 0;
	}

	public String getModelInfo() {
		return "Combined model with "+m_Trees+" trees with "+m_TotSize+" nodes";
	}

	public void printModel(PrintWriter wrt) {
		wrt.println(getModelInfo());
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		printModel(wrt);
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
		printModel(wrt);
	}

	public void printModelToPythonScript(PrintWriter wrt) {
	}

	public void attachModel(HashMap table) throws ClusException {
	}

	public ClusModel prune(int prunetype) {
		return this;
	}

	public int getID() {
		return 0;
	}

	public void retrieveStatistics(ArrayList stats) {
	}

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int a, int b,boolean ex) {
	}

	public void setDataSet(int set) {
		m_DataSet = set;
	}
}

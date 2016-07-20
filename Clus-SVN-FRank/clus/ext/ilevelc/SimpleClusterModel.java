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

package clus.ext.ilevelc;

import java.io.PrintWriter;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.main.*;
import clus.statistic.ClusStatistic;
import clus.statistic.StatisticPrintInfo;

public class SimpleClusterModel extends ClusNode {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int[] m_Assign;
	protected ClusStatManager m_Manager;

	public SimpleClusterModel(int[] assign, ClusStatManager mgr) {
		m_Assign = assign;
		m_Manager = mgr;
	}

	public ClusStatistic predictWeighted(DataTuple tuple) {
		int idx = tuple.getIndex();
		int cl = m_Assign[idx];
		ILevelCStatistic stat = (ILevelCStatistic)m_Manager.getStatistic(ClusAttrType.ATTR_USE_CLUSTERING).cloneStat();
		stat.setClusterID(cl);
		stat.calcMean();
		return stat;
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		wrt.println("MPCKMeans()");
		if (m_Assign == null) {
			wrt.println("   Illegal");
		} else {
			wrt.println("   "+m_Assign.length);
		}
	}
}

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

import java.util.*;
import java.io.*;

import clus.data.rows.*;
import clus.data.type.*;
import clus.main.*;

public class COPKMeansCluster implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_Index;
	protected ClusStatManager m_Mgr;
	protected ArrayList m_Data = new ArrayList();
	protected ILevelCStatistic m_Center;

	public COPKMeansCluster(DataTuple tuple, ClusStatManager mgr) {
		m_Mgr = mgr;
		m_Data.add(tuple);
		m_Center = (ILevelCStatistic)mgr.getStatistic(ClusAttrType.ATTR_USE_CLUSTERING).cloneStat();
		updateCenter();
	}

	public ILevelCStatistic getCenter() {
		return m_Center;
	}

	public ClusStatManager getStatManager() {
		return m_Mgr;
	}

	public void clearData() {
		m_Data.clear();
	}

	public void addData(DataTuple tuple) {
		m_Data.add(tuple);
	}

	public void updateCenter() {
		m_Center.reset();
		for (int i = 0; i < m_Data.size(); i++) {
			DataTuple tuple = (DataTuple)m_Data.get(i);
			m_Center.updateWeighted(tuple, tuple.getWeight());
		}
		m_Center.calcMean();
	}

	public double computeDistance(DataTuple tuple) {
		double dist = 0.0;
		double[] num = m_Center.getNumericPred();
		for (int j = 0; j < m_Center.getNbAttributes(); j++) {
			NumericAttrType att = m_Center.getAttribute(j);
			double v1 = num[j];
			double v2 = tuple.getDoubleVal(att.getArrayIndex());
			dist += (v1-v2)*(v1-v2);
		}
		return Math.sqrt(dist);
	}

	public void setIndex(int i) {
		m_Index = i;
		m_Center.setClusterID(i);
	}
}

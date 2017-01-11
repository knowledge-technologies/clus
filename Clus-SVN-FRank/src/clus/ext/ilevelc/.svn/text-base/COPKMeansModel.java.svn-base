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

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.main.*;
import clus.statistic.*;

public class COPKMeansModel extends ClusNode {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_K, m_Iterations, m_CSets, m_AvgIter;
	protected boolean m_Illegal;
	protected double m_RandIndex;
	protected COPKMeansCluster[] m_Clusters;

	public void setK(int k) {
		m_K = k;
	}

	public int getModelSize() {
		return m_K;
	}

	public ClusStatistic predictWeighted(DataTuple tuple) {
		if (m_Illegal) {
			return null;
		} else {
			int best_cl = -1;
			double min_dist = Double.POSITIVE_INFINITY;
			for (int j = 0; j < m_K; j++) {
				double dist = m_Clusters[j].computeDistance(tuple);
				if (dist < min_dist) {
					best_cl = j;
					min_dist = dist;
				}
			}
			return m_Clusters[best_cl].getCenter();
		}
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		wrt.println("COPKMeans("+m_K+", iter = "+m_Iterations+", max = "+m_AvgIter+", csets = "+m_CSets+")");
		if (m_Illegal) {
			wrt.println("   Illegal");
		} else {
			for (int j = 0; j < m_K; j++) {
				wrt.println("  "+m_Clusters[j].getCenter().getString(info));
			}
		}
	}

	public String getModelInfo() {
		if (m_Illegal) {
			return "Rand Index = ?";
		} else {
			return "Rand Index = "+m_RandIndex;
		}
	}

	public void setCSets(int sets) {
		m_CSets = sets;
	}

	public int getCSets() {
		return m_CSets;
	}

	public void setAvgIter(int avg) {
		m_AvgIter = avg;
	}

	public void setIllegal(boolean illegal) {
		m_Illegal = illegal;
	}

	public void setRandIndex(double value) {
		m_RandIndex = value;
	}

	public void setClusters(COPKMeansCluster[] clusters) {
		m_Clusters = clusters;
	}

	public void setIterations(int i) {
		m_Iterations = i;
	}

	public int getIterations() {
		return m_Iterations;
	}

	public boolean isIllegal() {
		return m_Illegal;
	}
}

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

/*
 * Created on Apr 22, 2005
 */
package clus.ext.beamsearch;

import clus.algo.tdidt.ClusNode;
import clus.heuristic.ClusHeuristic;
import clus.statistic.ClusStatistic;

public abstract class ClusBeamHeuristic extends ClusHeuristic {

	protected double m_NbTrain;
	protected double m_TreeOffset;
	protected ClusStatistic m_Pos, m_Neg;
	protected ClusHeuristic m_AttrHeuristic;

	public ClusBeamHeuristic(ClusStatistic stat) {
		m_Pos = stat;
		m_Neg = stat.cloneStat();
	}

	public abstract double estimateBeamMeasure(ClusNode tree);

	public abstract double computeLeafAdd(ClusNode leaf);

	public void setTreeOffset(double value) {
		m_TreeOffset = value;
	}

	public void setRootStatistic(ClusStatistic stat) {
		m_NbTrain = stat.m_SumWeight;
	}

	public void setAttrHeuristic(ClusHeuristic heur) {
		m_AttrHeuristic = heur;
	}

	public String getAttrHeuristicString() {
		if (m_AttrHeuristic == null) return "";
		else return ", attribute heuristic = "+m_AttrHeuristic.getName();
	}
}

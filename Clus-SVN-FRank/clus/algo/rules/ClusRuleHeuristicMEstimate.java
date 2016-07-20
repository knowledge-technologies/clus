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
 * Created on May 3, 2005
 */
package clus.algo.rules;

import clus.heuristic.*;
import clus.main.*;
import clus.statistic.*;

public class ClusRuleHeuristicMEstimate extends ClusHeuristic {

	double m_MValue;
	double m_Prior;

	public ClusRuleHeuristicMEstimate(double m_value) {
		m_MValue = m_value;
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		double n_pos = c_pstat.m_SumWeight;
		// Acceptable?
		// if (n_pos < Settings.MINIMAL_WEIGHT) {
		if (n_pos-Settings.MINIMAL_WEIGHT < 1e-6) {
			return Double.NEGATIVE_INFINITY;
		}
		double correct = n_pos - c_pstat.getError();
		double m_estimate = (correct + m_MValue*m_Prior) / (n_pos + m_MValue);
		return m_estimate;
	}

	public void setRootStatistic(ClusStatistic stat) {
		m_Prior = (stat.getTotalWeight()-stat.getError()) / stat.getTotalWeight();
		System.out.println("Setting prior: "+m_Prior);
	}

	public String getName() {
		return "Rule Heuristic (M-Estimate, M = " + m_MValue + ")";
	}
}

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
 * Created on Apr 21, 2005
 */
package clus.heuristic;

import clus.main.Settings;
import clus.statistic.ClusStatistic;

import jeans.math.*;

public class ReducedErrorHeuristic extends ClusHeuristic {

	private double m_NbTrain;
	ClusStatistic m_Pos, m_Neg;

	public ReducedErrorHeuristic(ClusStatistic stat) {
		m_Pos = stat;
		m_Neg = stat.cloneStat();
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		double n_tot = c_tstat.m_SumWeight;
		double n_pos = c_pstat.m_SumWeight;
		double n_neg = n_tot - n_pos;
		// Acceptable?
		if (n_pos < Settings.MINIMAL_WEIGHT || n_neg < Settings.MINIMAL_WEIGHT) {
			return Double.NEGATIVE_INFINITY;
		}
		if (missing.m_SumWeight <= MathUtil.C1E_9) {
			double pos_error = c_pstat.getError();
			double neg_error = c_tstat.getErrorDiff(c_pstat);
			return -(pos_error + neg_error)/m_NbTrain;
		} else {
			double pos_freq = n_pos / n_tot;
			m_Pos.copy(c_pstat);
			m_Neg.copy(c_tstat);
			m_Neg.subtractFromThis(c_pstat);
			m_Pos.addScaled(pos_freq, missing);
			m_Neg.addScaled(1.0-pos_freq, missing);
			double pos_error = m_Pos.getError();
			double neg_error = m_Neg.getError();
			return -(pos_error + neg_error)/m_NbTrain;
		}
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic[] c_pstat, int nbsplit) {
		return Double.NEGATIVE_INFINITY;
	}

	public void setRootStatistic(ClusStatistic stat) {
		m_NbTrain = stat.m_SumWeight;
	}

	public String getName() {
		return "Reduced Error Heuristic";
	}
}

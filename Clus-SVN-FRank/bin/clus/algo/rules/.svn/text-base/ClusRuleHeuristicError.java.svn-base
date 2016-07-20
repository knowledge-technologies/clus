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
 * Created on May 2, 2005
 */
package clus.algo.rules;

import clus.heuristic.*;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.main.ClusStatManager;
import clus.data.attweights.*;
import clus.data.type.ClusAttrType;

public class ClusRuleHeuristicError extends ClusHeuristic {

	private ClusAttributeWeights m_TargetWeights;
	private ClusStatManager m_StatManager = null;

	public ClusRuleHeuristicError(ClusAttributeWeights prod) {
		m_TargetWeights = prod;
	}

	public ClusRuleHeuristicError(ClusStatManager stat_mgr, ClusAttributeWeights prod) {
		m_StatManager = stat_mgr;
		m_TargetWeights = prod;
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		double n_pos = c_pstat.m_SumWeight;
		// Acceptable?
		// if (n_pos < Settings.MINIMAL_WEIGHT) {
		if (n_pos-Settings.MINIMAL_WEIGHT < 1e-6) {
			return Double.NEGATIVE_INFINITY;
		}
		double pos_error = c_pstat.getError(m_TargetWeights);
		// Prefer rules that cover more examples
		double global_sum_w = m_StatManager.getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING).getTotalWeight();
		double heur_par = getSettings().getHeurCoveragePar();
		pos_error *= (1 + heur_par*global_sum_w/c_pstat.m_SumWeight);

		return -pos_error;
	}

	public String getName() {
		return "Rule Heuristic (Reduced Error)";
	}

  public Settings getSettings() {
    return m_StatManager.getSettings();
  }

}

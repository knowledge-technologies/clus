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

package clus.heuristic;

import clus.data.attweights.ClusAttributeWeights;
import clus.main.Settings;
import clus.statistic.ClusStatistic;

public class VarianceReductionHeuristicCompatibility extends ClusHeuristic {

	protected String m_BasicDist;
	

	public VarianceReductionHeuristicCompatibility(String basicdist, ClusStatistic negstat, ClusAttributeWeights targetweights) {
		m_BasicDist = basicdist;
		m_ClusteringWeights = targetweights;
	}

	public VarianceReductionHeuristicCompatibility(ClusStatistic negstat, ClusAttributeWeights targetweights) {
		m_BasicDist = negstat.getDistanceName();
		m_ClusteringWeights = targetweights;
	}
	public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
		// Acceptable?
		if (stopCriterion(tstat, pstat, missing)) {
			return Double.NEGATIVE_INFINITY;
		}
		// Compute |S|Var[S]
		double ss_tot = tstat.getSVarS(m_ClusteringWeights);
		double ss_pos = pstat.getSVarS(m_ClusteringWeights);
		double ss_neg = tstat.getSVarSDiff(m_ClusteringWeights, pstat);
		double value = FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos+ss_neg);
		if (Settings.VERBOSE >= 10) {
			System.out.println("TOT: "+tstat.getDebugString());
			System.out.println("POS: "+pstat.getDebugString());
			System.out.println("-> ("+ss_tot+", "+ss_pos+", "+ss_neg+") "+value);
		}
		// NOTE: This is here for compatibility reasons only
		if (value < 1e-6) return Double.NEGATIVE_INFINITY;
		return value;
	}

	public String getName() {
		return "Variance Reduction with Distance '"+m_BasicDist+"', ("+m_ClusteringWeights.getName()+") (FTest = "+FTest.getSettingSig()+")";
	}
}

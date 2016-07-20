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

import clus.statistic.*;

import jeans.math.*;

public class GainHeuristic extends ClusHeuristic {

	protected boolean m_GainRatio;

	public GainHeuristic(boolean gainratio) {
		m_GainRatio = gainratio;
	}

	public final boolean isGainRatio() {
		return m_GainRatio;
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		// Acceptable?
		if (stopCriterion(c_tstat, c_pstat, missing)) {
			return Double.NEGATIVE_INFINITY;
		}
		ClassificationStat tstat = (ClassificationStat)c_tstat;
		ClassificationStat pstat = (ClassificationStat)c_pstat;
		
		// Equal for all target attributes
		double n_tot = tstat.getTotalWeight();
		double n_pos = pstat.getTotalWeight();
		double n_neg = n_tot - n_pos;
		
		
		//System.out.print("Total nb examples "+n_tot+"\n");
		//System.out.print("Pos nb examples "+n_pos+"\n");
		//System.out.print("Neg nb examples "+n_neg+"\n");

		
	
		
		// Initialize entropy's
		//System.out.print("\nTotal\n");
		double tot_ent = tstat.entropy();
		//System.out.print("\nPositive\n");
		double pos_ent = pstat.entropy();
		double neg_ent = tstat.entropyDifference(pstat);
		
		double value=0;
		if(!tstat.getAttribute(0).getSchema().getSettings().considerUnlableInstancesInIGCalc()){
			// Gain?
			value = tot_ent - (n_pos*pos_ent + n_neg*neg_ent)/n_tot;
		}else{
			double n_tot_label = tstat.getSumWeights(0);
			double n_pos_label = pstat.getSumWeights(0);
		
			double n_neg_label = n_tot_label-n_pos_label;
			
			//System.out.print("Total nb labeled examples "+n_tot_label+"\n");
			//System.out.print("Pos nb labeled examples "+n_pos_label+"\n");

			value = tot_ent - ((n_pos-n_pos_label)*pos_ent + (n_neg-n_neg_label)*neg_ent)/(n_tot-n_tot_label);
		}
		
		if (value < MathUtil.C1E_6) return Double.NEGATIVE_INFINITY;
		if (m_GainRatio) {
			double si = ClassificationStat.computeSplitInfo(n_tot, n_pos, n_neg);
			if (si < MathUtil.C1E_6) return Double.NEGATIVE_INFINITY;
			return value / si;
		}
		return value;
	}

	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic[] c_pstat, int nbsplit) {
		// Acceptable?
		if (stopCriterion(c_tstat, c_pstat, nbsplit)) {
			return Double.NEGATIVE_INFINITY;
		}
		// Total Entropy
		ClassificationStat tstat = (ClassificationStat)c_tstat;
		double n_tot = tstat.getTotalWeight();
		double value = tstat.entropy();
		
		
		
		// Subset entropy
		for (int i = 0; i < nbsplit; i++) {
			ClassificationStat pstat = (ClassificationStat)c_pstat[i];
			double n_set = pstat.getTotalWeight();
			value -= n_set/n_tot*pstat.entropy();
		}
		if (value < MathUtil.C1E_6) {
			return Double.NEGATIVE_INFINITY;
		}
		if (m_GainRatio) {
			// Compute split information
			double si = 0;
			for (int i = 0; i < nbsplit; i++) {
				double n_set = c_pstat[i].getTotalWeight();
				if (n_set >= MathUtil.C1E_6) {
					double div = n_set/n_tot;
					si -= div*Math.log(div);
				}
			}
			si /= MathUtil.M_LN2;
			// Return calculated gainratio
			if (si < MathUtil.C1E_6) return Double.NEGATIVE_INFINITY;
			return value / si;
		}
		return value;
	}

	public String getName() {
		return m_GainRatio ? "Gainratio" : "Gain";
	}
}


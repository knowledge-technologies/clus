/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.heuristic;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.FTest;
import si.ijs.kt.clus.util.exception.ClusException;


public class VarianceReductionHeuristicEfficient extends ClusHeuristic {

    private ClusAttrType[] m_Attrs;


    public VarianceReductionHeuristicEfficient(ClusAttributeWeights prod, ClusAttrType[] attrs, Settings sett) {
        super(sett);

        m_ClusteringWeights = prod;
        m_Attrs = attrs;
    }


    //    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing){
    //    	// Acceptable?
    //        if (stopCriterion(tstat, pstat, missing)) { return Double.NEGATIVE_INFINITY; }
    //        // Compute |S|Var[S]
    //        double ss_tot = m_NumericSplitTotStatSVarS;
    //        double ss_pos = pstat.getSVarS(m_ClusteringWeights);
    //        double ss_neg = tstat.getSVarSDiff(m_ClusteringWeights, pstat);
    //        // printInfo(ss_tot, ss_pos, ss_neg, pstat);
    //        return FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos + ss_neg);
    //
    //    }
    //    
    @Override
    public boolean isEfficient() {
        return true;
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing, double ss_tot) {
        // Acceptable?
        if (stopCriterion(tstat, pstat, missing)) { return Double.NEGATIVE_INFINITY; }
        // Compute |S|Var[S]
        double ss_pos = pstat.getSVarS(m_ClusteringWeights);
        double ss_neg = tstat.getSVarSDiff(m_ClusteringWeights, pstat);
        // printInfo(ss_tot, ss_pos, ss_neg, pstat);
        return FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos + ss_neg);
        //
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        // Acceptable?
        if (stopCriterion(tstat, pstat, missing)) { return Double.NEGATIVE_INFINITY; }
        // Compute |S|Var[S]
        double ss_tot = tstat.getSVarS(m_ClusteringWeights);
        double ss_pos = pstat.getSVarS(m_ClusteringWeights);
        double ss_neg = tstat.getSVarSDiff(m_ClusteringWeights, pstat);
        // printInfo(ss_tot, ss_pos, ss_neg, pstat);
        return FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos + ss_neg);
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
        // Acceptable?
        if (stopCriterion(tstat, pstat, nbsplit)) { return Double.NEGATIVE_INFINITY; }
        // Compute |S|Var[S]
        double ss_sum = 0.0;
        for (int i = 0; i < nbsplit; i++) {
            ss_sum += pstat[i].getSVarS(m_ClusteringWeights);
        }
        double ss_tot = tstat.getSVarS(m_ClusteringWeights);
        return FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_sum);
    }


    @Override
    public String getName() {
        return "Variance Reduction (FTest = " + SettingsTree.FTEST_VALUE + ", " + m_ClusteringWeights.getName(m_Attrs) + ")";
    }


    public void printInfo(double ss_tot, double ss_pos, double ss_neg, ClusStatistic pstat) throws ClusException {
        pstat.calcMean();
        ClusLogger.info("C-pos: " + pstat);
        ClusLogger.info("SS-pos: " + ss_pos + " SS-neg: " + ss_neg + " -> " + (ss_tot - (ss_pos + ss_neg)));
    }
}

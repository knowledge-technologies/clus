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
import si.ijs.kt.clus.util.FTest;


public class VarianceReductionHeuristicInclMissingValues extends ClusHeuristic {

    private ClusAttrType[] m_Attrs;
    protected ClusStatistic m_Pos, m_Neg, m_Tot;


    public VarianceReductionHeuristicInclMissingValues(ClusAttributeWeights prod, ClusAttrType[] attrs, ClusStatistic stat, Settings sett) {
        super(sett);
        
        m_ClusteringWeights = prod;
        m_Attrs = attrs;
        m_Pos = stat.cloneStat();
        m_Neg = stat.cloneStat();
        m_Tot = stat.cloneStat();
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        double n_tot = tstat.getTotalWeight();
        double n_pos = pstat.getTotalWeight();
        double n_neg = n_tot - n_pos;
        // Acceptable?
        if (n_pos < SettingsTree.MINIMAL_WEIGHT || n_neg < SettingsTree.MINIMAL_WEIGHT) { return Double.NEGATIVE_INFINITY; }
        // Compute SS
        double pos_freq = n_pos / n_tot;
        m_Pos.copy(pstat);
        m_Neg.copy(tstat);
        m_Tot.copy(tstat);
        m_Tot.add(missing);
        m_Neg.subtractFromThis(pstat);
        m_Pos.addScaled(pos_freq, missing);
        m_Neg.addScaled(1.0 - pos_freq, missing);
        double s_ss_pos = m_Pos.getSVarS(m_ClusteringWeights);
        double s_ss_neg = m_Neg.getSVarS(m_ClusteringWeights);
        double s_ss_tot = m_Tot.getSVarS(m_ClusteringWeights);
        return FTest.calcVarianceReductionHeuristic(n_tot, s_ss_tot, s_ss_pos + s_ss_neg);
    }


    @Override
    public String getName() {
        return "Variance Reduction Including Missing Values (ftest: " + SettingsTree.FTEST_VALUE + ", " + m_ClusteringWeights.getName(m_Attrs) + ")";
    }
}

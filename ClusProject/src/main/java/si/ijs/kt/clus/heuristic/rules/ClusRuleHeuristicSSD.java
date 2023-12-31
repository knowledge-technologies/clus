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

package si.ijs.kt.clus.heuristic.rules;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


public class ClusRuleHeuristicSSD extends ClusHeuristic {

    protected RowData m_Data;
    protected String m_BasicDist;
    protected ClusStatistic m_NegStat;
    protected ClusStatManager m_StatManager;


    // Copied from SSDHeuristic.java
    public ClusRuleHeuristicSSD(ClusStatManager statManager, String basicdist, ClusStatistic negstat, ClusAttributeWeights targetweights, Settings sett) {
        super(sett);

        m_StatManager = statManager;
        m_BasicDist = basicdist;
        m_NegStat = negstat;
        m_ClusteringWeights = targetweights;
    }


    // Copied from SSDHeuristic.java
    @Override
    public void setData(RowData data) {
        m_Data = data;
    }


    // Larger values are better!
    // Only the second parameter make sense for rules, i.e., statistic for covered examples
    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) throws ClusException {
        double n_pos = pstat.getTotalWeight();
        // Acceptable?
        if (n_pos < SettingsTree.MINIMAL_WEIGHT) { return Double.NEGATIVE_INFINITY; }
        // Calculate value
        double offset = m_StatManager.getSettings().getRules().getHeurDispOffset();
        double def_value = getTrainDataHeurValue();
        // System.out.print("Inside calcHeuristic()");
        // ClusLogger.info(" - default SS: "+def_value);
        double value = pstat.getSVarS(m_ClusteringWeights, m_Data);
        // System.out.print("raw SS: "+value);
        // Normalization with the purpose of getting most of the single variances within the
        // [0,1] interval. This weight is in stdev units,
        // default value = 4 = (-2sigma,2sigma) should cover 95% of examples
        // This will only be important when combining different types of atts
        double norm = m_StatManager.getSettings().getRules().getVarBasedDispNormWeight();
        value = 1 / (norm * norm) * (1 - value / def_value) + offset;
        // Normalized version of 'value = def_value -value + offset'
        // ClusLogger.info(", combined disp. value: "+value);
        // Coverage part
        double train_sum_w = m_StatManager.getTrainSetStat(AttributeUseType.Clustering).getTotalWeight();
        double cov_par = m_StatManager.getSettings().getRules().getHeurCoveragePar();
        value *= Math.pow(n_pos / train_sum_w, cov_par);
        // ClusLogger.info(" cov: "+n_pos+"/"+train_sum_w+", final value: "+value); //+" -> -"+value);
        if (value < 1e-6)
            return Double.NEGATIVE_INFINITY;
        return value;
    }


    @Override
    public String getName() {
        return "SS Reduction for Rules (" + m_BasicDist + ", " + m_ClusteringWeights.getName() + ")";
    }
}

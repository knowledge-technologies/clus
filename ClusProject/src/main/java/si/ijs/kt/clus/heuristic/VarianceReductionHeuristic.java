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
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.FTest;


public class VarianceReductionHeuristic extends ClusHeuristic {

    protected RowData m_Data;
    protected String m_BasicDist;
    protected ClusStatistic m_NegStat;


    public VarianceReductionHeuristic(String basicdist, ClusStatistic negstat, ClusAttributeWeights targetweights, Settings sett) {
        super(sett);
        
        m_BasicDist = basicdist;
        
        m_NegStat = negstat;
        m_ClusteringWeights = targetweights;
    }


    public VarianceReductionHeuristic(ClusStatistic negstat, ClusAttributeWeights targetweights, Settings sett) {
        this(negstat.getDistanceName(), negstat, targetweights, sett);
    }


    @Override
    public void setData(RowData data) {
        m_Data = data;
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) throws ClusException {
        // Acceptable?
        if (stopCriterion(tstat, pstat, missing)) { return Double.NEGATIVE_INFINITY; }
        // Calculate |S|Var[S]
        double ss_tot = tstat.getSVarS(m_ClusteringWeights, m_Data);
        double ss_pos = pstat.getSVarS(m_ClusteringWeights, m_Data);
        m_NegStat.copy(tstat);
        m_NegStat.subtractFromThis(pstat);
        double ss_neg = m_NegStat.getSVarS(m_ClusteringWeights, m_Data);
        double value = FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos + ss_neg);
        if (getSettings().getGeneral().getVerbose() >= 10) {
            System.out.println("TOT: " + tstat.getDebugString());
            System.out.println("POS: " + pstat.getDebugString());
            System.out.println("NEG: " + m_NegStat.getDebugString());
            System.out.println("-> (" + ss_tot + ", " + ss_pos + ", " + ss_neg + ") " + value);
        }
        return value;
    }


    @Override
    public String getName() {
        return "Variance Reduction with Distance '" + m_BasicDist + "', (" + m_ClusteringWeights.getName() + ") (FTest = " + FTest.getSettingSig() + ")";
    }
}

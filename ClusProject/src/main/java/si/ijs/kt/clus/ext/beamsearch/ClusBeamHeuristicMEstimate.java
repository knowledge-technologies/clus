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

/*
 * Created on May 4, 2005
 */

package si.ijs.kt.clus.ext.beamsearch;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsBeamSearch;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.jeans.math.MathUtil;


public class ClusBeamHeuristicMEstimate extends ClusBeamHeuristic {

    protected double m_Prior, m_MValue;


    public ClusBeamHeuristicMEstimate(ClusStatistic stat, double mvalue, Settings sett) {
        super(stat, sett);

        m_MValue = mvalue;
    }


    @Override
    public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
        double n_tot = c_tstat.getTotalWeight();
        double n_pos = c_pstat.getTotalWeight();
        double n_neg = n_tot - n_pos;
        // Acceptable?
        if (n_pos < SettingsTree.MINIMAL_WEIGHT || n_neg < SettingsTree.MINIMAL_WEIGHT) { return Double.NEGATIVE_INFINITY; }
        if (missing.getTotalWeight() <= MathUtil.C1E_9) {
            double pos_error = c_pstat.getError();
            double neg_error = c_tstat.getErrorDiff(c_pstat);
            return m_TreeOffset - (pos_error + neg_error) / m_NbTrain - 2 * SettingsBeamSearch.SIZE_PENALTY;
        }
        else {
            double pos_freq = n_pos / n_tot;
            m_Pos.copy(c_pstat);
            m_Neg.copy(c_tstat);
            m_Neg.subtractFromThis(c_pstat);
            m_Pos.addScaled(pos_freq, missing);
            m_Neg.addScaled(1.0 - pos_freq, missing);
            double pos_error = m_Pos.getError();
            double neg_error = m_Neg.getError();
            return m_TreeOffset - (pos_error + neg_error) / m_NbTrain - 2 * SettingsBeamSearch.SIZE_PENALTY;
        }
    }


    @Override
    public double estimateBeamMeasure(ClusNode tree) {
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getClusteringStat();
            return -total.getError() / m_NbTrain - SettingsBeamSearch.SIZE_PENALTY;
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateBeamMeasure(child);
            }
            return result - SettingsBeamSearch.SIZE_PENALTY;
        }
    }


    @Override
    public double computeLeafAdd(ClusNode leaf) {
        return -leaf.getClusteringStat().getError() / m_NbTrain;
    }


    @Override
    public void setRootStatistic(ClusStatistic stat) {
        m_Prior = (stat.getTotalWeight() - stat.getError()) / stat.getTotalWeight();
        ClusLogger.info("Setting prior: " + m_Prior);
    }


    @Override
    public String getName() {
        return "Beam Heuristic (MEstimate = " + m_MValue + ")" + getAttrHeuristicString();
    }
}

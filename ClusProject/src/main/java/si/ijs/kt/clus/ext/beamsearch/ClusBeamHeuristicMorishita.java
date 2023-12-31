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
 * Created on Jul 28, 2005
 */

package si.ijs.kt.clus.ext.beamsearch;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStat;


public class ClusBeamHeuristicMorishita extends ClusBeamHeuristic {

    public ClusBeamHeuristicMorishita(ClusStatistic stat, Settings sett) {
        super(stat, sett);
    }


    public double computeMorishitaStat(ClusStatistic stat, ClusStatistic tstat) {
        RegressionStat stat_set = (RegressionStat) stat;
        RegressionStat stat_all = (RegressionStat) tstat;
        // Compute half of formula from Definition 2 of Morishita paper
        double result = 0.0;
        for (int i = 0; i < stat_set.getNbAttributes(); i++) {
            double term_i = stat_set.getMean(i) - stat_all.getMean(i);
            result += term_i * term_i;
        }
        return result * stat_set.getTotalWeight();
    }


    @Override
    public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
        double n_tot = c_tstat.getTotalWeight();
        double n_pos = c_pstat.getTotalWeight();
        double n_neg = n_tot - n_pos;
        // Acceptable?
        if (n_pos < SettingsTree.MINIMAL_WEIGHT || n_neg < SettingsTree.MINIMAL_WEIGHT) { return Double.NEGATIVE_INFINITY; }
        m_Neg.copy(c_tstat);
        m_Neg.subtractFromThis(c_pstat);
        // Does not take into account missing values!
        return computeMorishitaStat(c_pstat, c_tstat) + computeMorishitaStat(m_Neg, c_tstat);
    }


    public double estimateBeamMeasure(ClusNode tree, ClusNode parent) {
        if (tree.atBottomLevel()) {
            return computeMorishitaStat(tree.getClusteringStat(), parent.getClusteringStat());
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateBeamMeasure(child);
            }
            return result;
        }
    }


    @Override
    public double estimateBeamMeasure(ClusNode tree) {
        if (tree.atBottomLevel()) {
            return 0;
        }
        else {
            double result = 0.0;
            for (int i = 0; i < tree.getNbChildren(); i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                result += estimateBeamMeasure(child, tree);
            }
            return result;
        }
    }


    @Override
    public double computeLeafAdd(ClusNode leaf) {
        return 0.0;
    }


    @Override
    public String getName() {
        return "Beam Heuristic (Morishita)";
    }


    @Override
    public void setRootStatistic(ClusStatistic stat) {
        super.setRootStatistic(stat);
    }
}

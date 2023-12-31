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
import si.ijs.kt.clus.heuristic.stopCriterion.ClusStopCriterion;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


public abstract class ClusHeuristic {

    public final static double DELTA = 1e-6;
    // Access to the training data
    protected RowData m_TrainData;

    // Value of the heuristic on the training data
    protected double m_TrainDataHeurValue;

    protected ClusAttributeWeights m_ClusteringWeights;

    protected ClusStopCriterion m_StopCrit;

    /**
     * Used for more efficient split quality estimation. For now only in the case when
     * the heuristic is VarianceReductionHeuristicEfficient.
     */
    protected double m_NumericSplitTotStatSVarS;

    private Settings m_Settings;


    public ClusHeuristic(Settings sett) {
        m_Settings = sett;
    }


    public final Settings getSettings() {
        return m_Settings;
    }


    public void setSplitStatSVarS(double value) {
        m_NumericSplitTotStatSVarS = value;
    }


    public boolean isEfficient() {
        return false;
    }


    /**
     * This method should be overwritten in the classes of heuristics that are efficient. It is used in the cases when
     * we precompute the total variance.
     * 
     * @param tstat
     * @param pstat
     * @param missing
     * @param ss_tot
     * 
     * @throws ClusException
     */
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing, double ss_tot) throws ClusException {
        return calcHeuristic(tstat, pstat, missing);
    }


    public void setData(RowData data) {
    }


    public ClusAttributeWeights getClusteringAttributeWeights() {
        return m_ClusteringWeights;
    }


    public void setInitialData(ClusStatistic stat, RowData data) {
    }


    public void setRootStatistic(ClusStatistic stat) {
    }


    public abstract double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) throws ClusException;


    public abstract String getName();


    public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic[] c_pstat, int nbsplit) {
        return Double.NEGATIVE_INFINITY;
    }


    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        return m_StopCrit.stopCriterion(tstat, pstat, missing);
    }


    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
        return m_StopCrit.stopCriterion(tstat, pstat, nbsplit);
    }


    public static double nonZero(double val) {
        if (val < 1e-6)
            return Double.NEGATIVE_INFINITY;
        return val;
    }


    public RowData getTrainData() {
        return m_TrainData;
    }


    public void setTrainData(RowData data) {
        m_TrainData = data;
    }


    public double getTrainDataHeurValue() {
        return m_TrainDataHeurValue;
    }


    public void setTrainDataHeurValue(double value) {
        m_TrainDataHeurValue = value;
    }


    public void setStopCriterion(ClusStopCriterion stop) {
        m_StopCrit = stop;
    }


    /**
     * Flag that indicates whether the corresponding split is acceptable. The flag will only be checked after the best
     * test was chosen.
     * Useful e.g. if you want to find the best split according to the heuristic, but only keep that split if it is
     * acceptable, and otherwise make it a leaf.
     * The other stop criteria are checked before the best test is chosen.
     * Is set to true by default. Overridden in the phylogeny framework.
     */
    public boolean isAcceptable(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
        return true;
    }
}

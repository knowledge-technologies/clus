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

package si.ijs.kt.clus.ext.structuredTypes;

import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.SetDistance;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTimeSeries.TimeSeriesPrototypeComplexity;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.SumPairwiseDistancesStat;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class SetStatistic extends SumPairwiseDistancesStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    // m_RepresentativeMean is the time series representing the cluster

    // TODO: Investigate the usage of Medoid vs. mean?

    protected SetAttrType m_Attr;
    private ArrayList<Set> m_SetStack = new ArrayList<Set>();
    public Set m_RepresentativeMean = null;
    public Set m_RepresentativeMedoid = null;

    // public Set m_RepresentativeQuantitve=new Set("[]");

    protected double m_AvgDistances;


    public SetStatistic(Settings sett, SetAttrType attr, ClusDistance dist, TimeSeriesPrototypeComplexity efficeny) {
        super(sett, dist, efficeny);
        m_Attr = attr;
    }


    @Override
    public ClusStatistic cloneStat() {
        SetStatistic stat = new SetStatistic(getSettings(), m_Attr, m_Distance, m_Efficiency);
        stat.cloneFrom(this);
        return stat;
    }


    @Override
    public ClusStatistic cloneSimple() {
        SetStatistic stat = new SetStatistic(getSettings(), m_Attr, m_Distance, m_Efficiency);
        stat.m_RepresentativeMean = new Set(m_RepresentativeMean.size());
        stat.m_RepresentativeMedoid = new Set(m_RepresentativeMedoid.size());
        return stat;
    }


    @Override
    public void copy(ClusStatistic other) {
        SetStatistic or = (SetStatistic) other;
        super.copy(or);
        // m_Value = or.m_Value;
        // m_AvgDistances = or.m_AvgDistances;
        // m_AvgSqDistances = or.m_AvgSqDistances;
        m_SetStack.clear();
        m_SetStack.addAll(or.m_SetStack);
        // m_RepresentativeMean = or.m_RepresentativeMean;
        // m_RepresentativeMedoid = or.m_RepresentativeMedoid;
    }


    /**
     * Used for combining weighted predictions.
     */
    @Override
    public SetStatistic normalizedCopy() {
        SetStatistic copy = (SetStatistic) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        copy.m_SetStack.add(getSetPred());
        copy.m_RepresentativeMean.setValues(m_RepresentativeMean.getValues());
        copy.m_RepresentativeMedoid.setValues(m_RepresentativeMedoid.getValues());
        return copy;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        SetStatistic or = (SetStatistic) other;
        m_SumWeight += weight * or.m_SumWeight;
        Set pred = new Set(or.getSetPred());
        pred.setSetWeight(weight);
        m_SetStack.add(pred);
    }


    /*
     * Add a weighted time series to the statistic.
     */
    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        super.updateWeighted(tuple, idx);

        Set newSet = new Set((Set) tuple.getObjVal(this.getAttribute().getIndex()));
        newSet.setSetWeight(tuple.getWeight());
        m_SetStack.add(newSet);

    }


    public double calcDistance(Set ts1, Set ts2) throws ClusException {
        SetDistance dist = (SetDistance) getDistance();
        return dist.calcDistance(ts1, ts2);
    }


    /**
     * Currently only used to compute the default dispersion within rule heuristics.
     * 
     * @throws ClusException
     */
    @Override
    public double getDispersion(ClusAttributeWeights scale, RowData data) throws ClusException {
        return getSVarS(scale, data);
    }


    @Override
    public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights) throws ClusException {
        int idx = m_Attr.getIndex();
        Set actual = (Set) tuple.getObjVal(0);
        return calcDistance(m_RepresentativeMean, actual) * weights.getWeight(idx);
    }


    public void initNormalizationWeights(ClusAttributeWeights weights, boolean[] shouldNormalize) {
        int idx = m_Attr.getIndex();
        if (shouldNormalize[idx]) {
            double var = m_SVarS / getTotalWeight();
            double norm = var > 0 ? 1 / var : 1; // No normalization if variance = 0;
            weights.setWeight(m_Attr, norm);
        }
    }


    public void calcSumAndSumSqDistances(Set prototype) throws ClusException {
        m_AvgDistances = 0.0;
        int count = m_SetStack.size();
        for (int i = 0; i < count; i++) {
            double dist = calcDistance(prototype, m_SetStack.get(i));
            m_AvgDistances += dist;
        }
        m_AvgDistances /= count;
    }


    @Override
    public void calcMean() throws ClusException {
        // Medoid
        m_RepresentativeMedoid = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < m_SetStack.size(); i++) {
            double crDistance = 0.0;
            Set t1 = m_SetStack.get(i);
            for (int j = 0; j < m_SetStack.size(); j++) {
                Set t2 = m_SetStack.get(j);
                double dist = calcDistance(t1, t2);
                crDistance += dist * t2.getSetWeight();
            }
            if (crDistance < minDistance) {
                m_RepresentativeMedoid = m_SetStack.get(i);
                minDistance = crDistance;
            }
        }
        calcSumAndSumSqDistances(m_RepresentativeMedoid);

        double sumwi = 0.0;
        for (int j = 0; j < m_SetStack.size(); j++) {
            Set t1 = m_SetStack.get(j);
            sumwi += t1.getSetWeight();
        }
        double diff = Math.abs(m_SumWeight - sumwi);
        if (diff > 1e-6) {
            System.err.println("Error: Sanity check failed! - " + diff);
        }

    }


    @Override
    public void reset() {
        super.reset();
        m_SetStack.clear();
    }


    /*
     * [Aco]
     * for printing in the nodes
     * @see clus.statistic.ClusStatistic#getString(clus.statistic.StatisticPrintInfo)
     */
    @Override
    public String getString(StatisticPrintInfo info) {
        ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        if (m_RepresentativeMean != null) {
            buf.append("Mean: ");
            buf.append(m_RepresentativeMean);
            if (info.SHOW_EXAMPLE_COUNT) {
                buf.append(": ");
                buf.append(fr.format(m_SumWeight));
            }
            buf.append("; ");
        }
        else {

            buf.append("Medoid: ");
            buf.append(m_RepresentativeMedoid);
            if (info.SHOW_EXAMPLE_COUNT) {
                buf.append(": ");
                buf.append(fr.format(m_SumWeight));
                buf.append(", ");
                buf.append(fr.format(m_AvgDistances));
            }
            buf.append("; ");
        }
        /*
         * buf.append("Quantitive: ");
         * buf.append(m_RepresentativeQuantitve.toString());
         * if (info.SHOW_EXAMPLE_COUNT) {
         * buf.append(": ");
         * buf.append(fr.format(m_SumWeight));
         * }
         * buf.append("; ");
         */
        return buf.toString();
    }


    @Override
    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        schema.addAttrType(new SetAttrType(prefix + "-p-Set"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-Distance"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-Size"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-AvgDist"));
    }


    @Override
    public String getPredictWriterString(DataTuple tuple) throws ClusException {
        StringBuffer buf = new StringBuffer();
        buf.append(m_RepresentativeMedoid.toString());
        double dist = calcDistanceToCentroid(tuple);
        buf.append(",");
        buf.append(dist);
        buf.append(",");
        buf.append(getTotalWeight());
        buf.append(",");
        buf.append(m_AvgDistances);
        return buf.toString();
    }


    public Set getRepresentativeMean() {
        return m_RepresentativeMean;
    }


    public Set getRepresentativeMedoid() {
        return m_RepresentativeMedoid;
    }


    public Set getSetPred() {
        return m_RepresentativeMedoid;
    }


    public SetAttrType getAttribute() {
        return m_Attr;
    }
}

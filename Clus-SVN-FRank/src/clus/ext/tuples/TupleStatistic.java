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

package clus.ext.tuples;

import java.text.NumberFormat;
import java.util.*;

import clus.data.attweights.*;
import clus.data.rows.*;
import clus.data.schema.ClusSchema;
import clus.data.type.*;
import clus.data.type.structured.TupleAttrType;
import clus.distance.ClusDistance;
import clus.main.settings.Settings;
import clus.statistic.*;
import clus.util.*;


public class TupleStatistic extends SumPairwiseDistancesStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected TupleAttrType m_Attr;
    private ArrayList<Tuple> m_TupleStack = new ArrayList<Tuple>();
    public Tuple m_RepresentativeMean = new Tuple("[]"); // this is the time series representing the cluster
    public Tuple m_RepresentativeMedoid = new Tuple("[]");

    protected double m_AvgDistances;


    public TupleStatistic(Settings sett, TupleAttrType attr, ClusDistance dist, int efflvl) {
        super(sett, dist, efflvl);
        m_Attr = attr;
    }


    public ClusStatistic cloneStat() {
        TupleStatistic stat = new TupleStatistic(getSettings(), m_Attr, m_Distance, m_Efficiency);
        stat.cloneFrom(this);
        return stat;
    }


    public ClusStatistic cloneSimple() {
        TupleStatistic stat = new TupleStatistic(getSettings(), m_Attr, m_Distance, m_Efficiency);
        stat.m_RepresentativeMean = new Tuple(m_RepresentativeMean.length());
        stat.m_RepresentativeMedoid = new Tuple(m_RepresentativeMedoid.length());
        return stat;
    }


    public void copy(ClusStatistic other) {
        TupleStatistic or = (TupleStatistic) other;
        super.copy(or);
        //m_Value = or.m_Value;
        //m_AvgDistances = or.m_AvgDistances;
        //m_AvgSqDistances = or.m_AvgSqDistances;
        m_TupleStack.clear();
        m_TupleStack.addAll(or.m_TupleStack);
        // m_RepresentativeMean = or.m_RepresentativeMean;
        // m_RepresentativeMedoid = or.m_RepresentativeMedoid;
    }


    /**
     * Used for combining weighted predictions.
     */
    public TupleStatistic normalizedCopy() {
        TupleStatistic copy = (TupleStatistic) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        copy.m_TupleStack.add(getTuplePred());
        copy.m_RepresentativeMean.setValues(m_RepresentativeMean.getValues());
        copy.m_RepresentativeMedoid.setValues(m_RepresentativeMedoid.getValues());
        return copy;
    }


    public void addPrediction(ClusStatistic other, double weight) {
        TupleStatistic or = (TupleStatistic) other;
        m_SumWeight += weight * or.m_SumWeight;
        Tuple pred = new Tuple(or.getTuplePred());
        pred.setTSWeight(weight);
        m_TupleStack.add(pred);
    }


    /*
     * Add a weighted time series to the statistic.
     */
    public void updateWeighted(DataTuple tuple, int idx) {
        super.updateWeighted(tuple, idx);
        Tuple newTuple = new Tuple((Tuple) tuple.m_Objects[this.getAttribute().getArrayIndex()]);
        newTuple.setTSWeight(tuple.getWeight());
        m_TupleStack.add(newTuple);
    }


    public double calcDistance(Tuple t1, Tuple t2) {
        TupleDistance dist = (TupleDistance) getDistance();
        return dist.calcDistance(t1, t2);
    }


    public double calcDistance(DataTuple t1, DataTuple t2) {
        return ((TupleDistance) m_Distance).calcDistance(t1, t2);
    }


    /**
     * Currently only used to compute the default dispersion within rule heuristics.
     */
    public double getDispersion(ClusAttributeWeights scale, RowData data) {
        return getSVarS(scale, data);
    }


    public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights) {
        int idx = m_Attr.getIndex();
        Tuple actual = (Tuple) tuple.getObjVal(0);
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


    public void calcSumAndSumSqDistances(Tuple prototype) {
        m_AvgDistances = 0.0;
        int count = m_TupleStack.size();
        for (int i = 0; i < count; i++) {
            double dist = calcDistance(prototype, (Tuple) m_TupleStack.get(i));
            m_AvgDistances += dist;
        }
        m_AvgDistances /= count;
    }


    /*
     * [Aco]
     * this is executed in the end
     * @see clus.statistic.ClusStatistic#calcMean()
     */
    public void calcMean() {
        // Medoid
        m_RepresentativeMedoid = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < m_TupleStack.size(); i++) {
            double crDistance = 0.0;
            Tuple t1 = (Tuple) m_TupleStack.get(i);
            for (int j = 0; j < m_TupleStack.size(); j++) {
                Tuple t2 = (Tuple) m_TupleStack.get(j);
                double dist = calcDistance(t1, t2);
                crDistance += dist * t2.geTSWeight();
                if (Double.isNaN(dist)) {
                    System.err.println("ooops!!!");

                }
            }
            if (crDistance < minDistance) {
                m_RepresentativeMedoid = (Tuple) m_TupleStack.get(i);
                minDistance = crDistance;
            }
        }
        calcSumAndSumSqDistances(m_RepresentativeMedoid);

        double sumwi = 0.0;
        for (int j = 0; j < m_TupleStack.size(); j++) {
            Tuple t1 = (Tuple) m_TupleStack.get(j);
            sumwi += t1.geTSWeight();
        }
        double diff = Math.abs(m_SumWeight - sumwi);
        if (diff > 1e-6) {
            System.err.println("Error: Sanity check failed! - " + diff);
        }
    }


    public void reset() {
        super.reset();
        m_TupleStack.clear();
    }


    /*
     * [Aco]
     * for printing in the nodes
     * @see clus.statistic.ClusStatistic#getString(clus.statistic.StatisticPrintInfo)
     */
    public String getString(StatisticPrintInfo info) {
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        buf.append("Mean: ");
        buf.append(m_RepresentativeMean.toString());
        if (info.SHOW_EXAMPLE_COUNT) {
            buf.append(": ");
            buf.append(fr.format(m_SumWeight));
        }
        buf.append("; ");

        buf.append("Medoid: ");
        buf.append(m_RepresentativeMedoid.toString());
        if (info.SHOW_EXAMPLE_COUNT) {
            buf.append(": ");
            buf.append(fr.format(m_SumWeight));
            buf.append(", ");
            buf.append(fr.format(m_AvgDistances));
        }
        buf.append("; ");
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


    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        schema.addAttrType(new TupleAttrType(prefix + "-p-Tuple"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-Distance"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-Size"));
        schema.addAttrType(new NumericAttrType(prefix + "-p-AvgDist"));
    }


    public String getPredictWriterString(DataTuple tuple) {
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


    public Tuple getRepresentativeMean() {
        return m_RepresentativeMean;
    }


    public Tuple getRepresentativeMedoid() {
        return m_RepresentativeMedoid;
    }


    public Tuple getTuplePred() {
        return m_RepresentativeMedoid;
    }


    public TupleAttrType getAttribute() {
        return m_Attr;
    }


    public double getError(ClusAttributeWeights scale) {
        return getSVarS(scale);
    }

}

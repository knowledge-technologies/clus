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

package si.ijs.kt.clus.ext.timeseries;

import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.data.type.primitive.TimeSeriesAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.primitive.timeseries.TimeSeriesDist;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTimeSeries.TimeSeriesPrototypeComplexity;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.SumPairwiseDistancesStat;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class TimeSeriesStat extends SumPairwiseDistancesStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    // m_RepresentativeMean is the time series representing the cluster

    // TODO: Investigate the usage of Medoid vs. mean?

    protected TimeSeriesAttrType m_Attr;
    private ArrayList<TimeSeries> m_TimeSeriesStack = new ArrayList<TimeSeries>();
    public TimeSeries m_RepresentativeMean = new TimeSeries("[]");
    public TimeSeries m_RepresentativeMedoid = new TimeSeries("[]");

    // public TimeSeries m_RepresentativeQuantitve=new TimeSeries("[]");

    protected double m_AvgDistances;


    public TimeSeriesStat(Settings sett, TimeSeriesAttrType attr, ClusDistance dist, TimeSeriesPrototypeComplexity efflvl) {
        super(sett, dist, efflvl);
        m_Attr = attr;
    }


    @Override
    public ClusStatistic cloneStat() {
        TimeSeriesStat stat = new TimeSeriesStat(this.m_Settings, m_Attr, m_Distance, m_Efficiency);
        stat.cloneFrom(this);
        // checkInvariant()
        return stat;
    }


    @Override
    public ClusStatistic cloneSimple() {
        TimeSeriesStat stat = new TimeSeriesStat(this.m_Settings, m_Attr, m_Distance, m_Efficiency);
        stat.m_RepresentativeMean = new TimeSeries(m_RepresentativeMean.length());
        stat.m_RepresentativeMedoid = new TimeSeries(m_RepresentativeMedoid.length());
        // checkInvariant()
        return stat;
    }


    @Override
    public void copy(ClusStatistic other) {
        TimeSeriesStat or = (TimeSeriesStat) other;
        super.copy(or);
        // m_Value = or.m_Value;
        // m_AvgDistances = or.m_AvgDistances;
        // m_AvgSqDistances = or.m_AvgSqDistances;
        m_TimeSeriesStack.clear();
        m_TimeSeriesStack.addAll(or.m_TimeSeriesStack);
        // m_RepresentativeMean = or.m_RepresentativeMean;
        // m_RepresentativeMedoid = or.m_RepresentativeMedoid;
        // checkInvariant()
    }


    /**
     * Used for combining weighted predictions.
     */
    @Override
    public TimeSeriesStat normalizedCopy() {
        TimeSeriesStat copy = (TimeSeriesStat) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        copy.m_TimeSeriesStack.add(getTimeSeriesPred());
        copy.m_RepresentativeMean.setValues(m_RepresentativeMean.getValues());
        copy.m_RepresentativeMedoid.setValues(m_RepresentativeMedoid.getValues());
        // checkInvariant()
        return copy;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        TimeSeriesStat or = (TimeSeriesStat) other;
        m_SumWeight += weight; // matejp changed from weight * or.m_SumWeight; since this brakes the invariant of sum of
                               // weights in stack == m_SumWeights
        TimeSeries pred = new TimeSeries(or.getTimeSeriesPred());
        pred.setTSWeight(weight);
        m_TimeSeriesStack.add(pred);
        // checkInvariant()
    }


    /*
     * Add a weighted time series to the statistic.
     */
    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        super.updateWeighted(tuple, idx);
        TimeSeries newTimeSeries = new TimeSeries((TimeSeries) tuple.getObjVal(m_Attr.getArrayIndex())); // new
                                                                                                         // TimeSeries((TimeSeries)
                                                                                                         // tuple.m_Objects[0]);
        newTimeSeries.setTSWeight(tuple.getWeight());
        m_TimeSeriesStack.add(newTimeSeries);
        // checkInvariant()
    }


    public double calcDistance(TimeSeries ts1, TimeSeries ts2) throws ClusException {
        TimeSeriesDist dist = (TimeSeriesDist) getDistance();
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
        TimeSeries actual = (TimeSeries) tuple.getObjVal(0);
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


    public void calcSumAndSumSqDistances(TimeSeries prototype) throws ClusException {
        m_AvgDistances = 0.0;
        int count = m_TimeSeriesStack.size();
        for (int i = 0; i < count; i++) {
            double dist = calcDistance(prototype, m_TimeSeriesStack.get(i));
            m_AvgDistances += dist;
        }
        m_AvgDistances /= count;
    }


    /**
     * @author matejp
     *         Computes the medoid of the time series instances. Copied from the body of {@link #calcMean()}.

     * @throws ClusException
     */
    private TimeSeries calcMedoidTS() throws ClusException {
        TimeSeries medoid = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < m_TimeSeriesStack.size(); i++) {
            double crDistance = 0.0;
            TimeSeries t1 = m_TimeSeriesStack.get(i);
            for (int j = 0; j < m_TimeSeriesStack.size(); j++) {
                TimeSeries t2 = m_TimeSeriesStack.get(j);
                double dist = calcDistance(t1, t2);
                crDistance += dist * t2.geTSWeight();
            }
            if (crDistance < minDistance) {
                medoid = m_TimeSeriesStack.get(i);
                minDistance = crDistance;
            }
        }
        return medoid;
    }


    /**
     * @author matejp
     *         Computes the mean of time series. Copied from the body of {@link #calcMean()}. If not all time series are
     *         of equal length,
     *         returns null.

     */
    private TimeSeries calcMeanTS() {
        TimeSeries mean = new TimeSeries("[]");
        if (m_TimeSeriesStack.size() == 0) { return null; }
        mean.setSize(m_TimeSeriesStack.get(0).length());
        // check for the lengths
        for (int j = 0; j < m_TimeSeriesStack.size(); j++) {
            if (m_TimeSeriesStack.get(j).length() != mean.length()) {
                if (m_Settings.getGeneral().getVerbose() > 1) {
                    ClusLogger.info("TSs should be of the same length, returning null as the mean value.");
                }
                return null;
            }
        }
        // compute mean
        for (int i = 0; i < mean.length(); i++) {
            double sum = 0.0;
            for (int j = 0; j < m_TimeSeriesStack.size(); j++) {
                TimeSeries t1 = m_TimeSeriesStack.get(j);
                sum += t1.getValue(i) * t1.geTSWeight();
            }
            mean.setValue(i, sum / m_SumWeight);
        }
        return mean;
    }


    /*
     * [Aco]
     * this is executed in the end
     * @see clus.statistic.ClusStatistic#calcMean()
     */
    @Override
    public void calcMean() throws ClusException {
        // Medoid
        m_RepresentativeMedoid = calcMedoidTS();
        calcSumAndSumSqDistances(m_RepresentativeMedoid);
        // Mean
        if (m_Attr.isEqualLength()) {
            m_RepresentativeMean = calcMeanTS();
        }
        double sumwi = 0.0;
        for (int j = 0; j < m_TimeSeriesStack.size(); j++) {
            TimeSeries t1 = m_TimeSeriesStack.get(j);
            sumwi += t1.geTSWeight();
        }
        double diff = Math.abs(m_SumWeight - sumwi);
        if (diff > 1e-6) {
            System.err.println("Error: Sanity check failed! - " + diff);
        }

        // Qualitative distance
        /*
         * double[][] m_RepresentativeQualitativeMatrix = new
         * double[m_RepresentativeMean.length()][m_RepresentativeMean.length()];
         * for(int i=0;i<m_RepresentativeMean.length();i++){
         * for(int j=0;j<m_RepresentativeMean.length();j++){
         * m_RepresentativeQualitativeMatrix[i][j]=0;
         * }
         * }
         * for(int i=0; i<TimeSeriesStack.size();i++){
         * TimeSeries newTemeSeries = (TimeSeries)TimeSeriesStack.get(i);
         * for (int j = 0; j < newTemeSeries.length(); j++) {
         * for (int k = 0; k < newTemeSeries.length(); k++) {
         * m_RepresentativeQualitativeMatrix[j][k]+=Math.signum(newTemeSeries.getValue(k) - newTemeSeries.getValue(j));
         * }
         * }
         * }
         * double tmpMaxValue=(double)(m_RepresentativeQualitativeMatrix.length - 1);
         * m_RepresentativeQuantitve=new TimeSeries(m_RepresentativeQualitativeMatrix.length);
         * for (int i=0;i<m_RepresentativeQualitativeMatrix.length;i++){
         * int numSmaller=0;
         * int numBigger=0;
         * for (int j=0; j<m_RepresentativeQualitativeMatrix.length;j++){
         * if (m_RepresentativeQualitativeMatrix[i][j]>0){numBigger++;}
         * if (m_RepresentativeQualitativeMatrix[i][j]<0){numSmaller++;}
         * }
         * m_RepresentativeQuantitve.setValue(i,((double)(numSmaller+tmpMaxValue-numBigger))/2);
         * }
         * m_RepresentativeQuantitve.rescale(m_RepresentativeMedoid.min(),m_RepresentativeMedoid.max());
         */
    }


    @Override
    public boolean samePrediction(ClusStatistic other) {
        TimeSeriesStat otherTS = (TimeSeriesStat) other;

        TimeSeries thisMean = this.calcMeanTS();
        TimeSeries otherMean = otherTS.calcMeanTS();

        if (!TimeSeries.areEqual(thisMean, otherMean)) { return false; }
        TimeSeries thisMedoid;
        try {
            thisMedoid = this.calcMedoidTS();
            TimeSeries otherMedoid = otherTS.calcMedoidTS();
            return TimeSeries.areEqual(thisMedoid, otherMedoid);
        }
        catch (ClusException e) {
            
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void reset() {
        super.reset();
        m_TimeSeriesStack.clear();
        // checkInvariant()
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


    @Override
    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        schema.addAttrType(new TimeSeriesAttrType(prefix + "-p-TimeSeries"));
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


    public TimeSeries getRepresentativeMean() {
        return m_RepresentativeMean;
    }


    public TimeSeries getRepresentativeMedoid() {
        return m_RepresentativeMedoid;
    }


    @Override
    public TimeSeries getTimeSeriesPred() {
        return m_RepresentativeMedoid;
    }


    public TimeSeriesAttrType getAttribute() {
        return m_Attr;
    }


    public double stackWeight() {
        double sum = 0.0;
        for (TimeSeries ts : m_TimeSeriesStack) {
            sum += ts.geTSWeight();
        }
        return sum;
    }


    @Override
    public void add(ClusStatistic other) {
        super.add(other);
        m_TimeSeriesStack.addAll(((TimeSeriesStat) other).m_TimeSeriesStack);
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        super.subtractFromThis(other);
        // filter time series stack
        ArrayList<TimeSeries> newStack = new ArrayList<TimeSeries>();
        boolean[] shouldRemove = new boolean[m_TimeSeriesStack.size()];
        TimeSeriesStat otherTS = (TimeSeriesStat) other;
        int found = 0;
        for (TimeSeries ts1 : otherTS.m_TimeSeriesStack) {
            for (int i = 0; i < m_TimeSeriesStack.size(); i++) {
                if (!shouldRemove[i] && TimeSeries.areEqual(ts1, m_TimeSeriesStack.get(i))) { // !shouldRemove[i] to
                                                                                              // prevent removing the
                                                                                              // same ts twice
                    shouldRemove[i] = true;
                    found++;
                    break;
                }
            }
        }
        if (found != otherTS.m_TimeSeriesStack.size())
            System.err.println("Removal candidates: " + otherTS.m_TimeSeriesStack.size() + " found: " + found);
        for (int i = 0; i < shouldRemove.length; i++) {
            if (!shouldRemove[i]) {
                newStack.add(m_TimeSeriesStack.get(i));
            }
        }
        m_TimeSeriesStack = newStack;

    }


    /**
     * For debugging purposes. Do not change the body, since matejp would like to be able to put a breakpoint in the
     * return false; line.
     * 

     */
    public boolean checkInvariant() {
        if (!ClusUtil.eq(stackWeight(), m_SumWeight, ClusUtil.MICRO)) {
            return false;
        }
        else {
            return true;
        }
    }
}

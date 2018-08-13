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

package si.ijs.kt.clus.statistic;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.NotImplementedException;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.cols.ColTarget;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.ext.beamsearch.ClusBeam;
import si.ijs.kt.clus.ext.ensemble.ClusOOBWeights;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROSForestInfo;
import si.ijs.kt.clus.ext.timeseries.TimeSeries;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;


/**
 * Statistics about the data set. Target attributes, nominal or real attributes, weights etc.
 *
 */
public abstract class ClusStatistic implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    /** The weighted sum of all examples */
    protected double m_SumWeight;
    protected double m_SumWeightLabeled;
    public int m_NbExamples;

    public static boolean INITIALIZEPARTIALSUM = true;

    protected Settings m_Settings; // this is here to reduce the usage of static references


    public ClusStatistic(Settings sett) {
        m_Settings = sett;
    }


    public abstract ClusStatistic cloneStat();


    /**
     * Statistic with only memory allocated for storing mean
     * / not variance, see e.g. RegressionStat.java
     */
    public ClusStatistic cloneSimple() {
        return cloneStat();
    }


    /** Clone this statistic by taking the given weight into account */
    public ClusStatistic copyNormalizedWeighted(double weight) {
        System.err.println(getClass().getName() + ": copyNormalizedWeighted(): Not yet implemented");
        return null;
    }


    /**
     * @return The number of all attributes in this statistic. If the statistic
     *         is a target statistic, it returns the number of target attributes.
     */
    public int getNbAttributes() {
        return getNbNominalAttributes() + getNbNumericAttributes();
    }


    /**
     * @return The number of all nominal attributes in a statistic.
     */
    public int getNbNominalAttributes() {
        return 0;
    }


    /**
     * @return The number of all numeric attributes in a statistic.
     */
    public int getNbNumericAttributes() {
        return 0;
    }


    public abstract int getNbStatisticComponents();


    public void printDebug() {
    }


    public void setSDataSize(int nbex) {
    }


    public void setTrainingStat(ClusStatistic train) {
    }


    public abstract void setParentStat(ClusStatistic parent);


    public abstract ClusStatistic getParentStat();


    public void optimizePreCalc(RowData data) throws ClusException { // only clus.statistic.SumPairwiseDistancesStat
                                                                     // actually has its own implementation of the
                                                                     // method
    }


    public void showRootInfo() { // only clus.ext.hierarchical.WHTDStatistic actually has its own implementation of the
                                 // method
    }


    public boolean isValidPrediction() {
        return true;
    }


    public void update(ColTarget target, int idx) {
        System.err.println(getClass().getName() + ": update(ColTarget target, int idx): Not yet implemented");
    }


    public abstract void updateWeighted(DataTuple tuple, int idx);


    public void updateWeighted(DataTuple tuple, double weight) {
    }


    public abstract void calcMean() throws ClusException;


    public abstract String getString(StatisticPrintInfo info);


    public abstract String getPredictedClassName(int idx);


    public abstract String getArrayOfStatistic();


    public void computePrediction() throws ClusException {
        calcMean();
    }


    public String getString() {
        return getString(StatisticPrintInfo.getInstance());
    }


    public String getPredictString() {
        return getString();
    }


    public abstract void reset();


    public abstract void copy(ClusStatistic other);


    /** Adds to this target prediction the effect of other statistics with weight considered */
    public abstract void addPrediction(ClusStatistic other, double weight);


    public abstract void add(ClusStatistic other);


    public void addData(RowData data) {
        for (int i = 0; i < data.getNbRows(); i++) {
            updateWeighted(data.getTuple(i), 1);
        }
    }


    public void addScaled(double scale, ClusStatistic other) {
        System.err.println(getClass().getName() + ": addScaled(): Not yet implemented");
    }


    public void resetToSimple(double weight) {
        System.err.println(getClass().getName() + ": resetToSimple(): Not yet implemented");
    }


    public abstract void subtractFromThis(ClusStatistic other);


    public abstract void subtractFromOther(ClusStatistic other);


    public void setData(RowData data) {
        throw new NotImplementedException();
    }


    public void setSplitIndex(int i) {
        // splitIndex = i;
    }


    public void setPrevIndex(int i) {
        // prevIndex = i;
    }


    public void initializeSum() {
        throw new NotImplementedException();
    }


    public double[] getNumericPred() {
        throw new NotImplementedException();
    }


    public TimeSeries getTimeSeriesPred() {
        throw new NotImplementedException();
    }


    public int[] getNominalPred() {
        throw new NotImplementedException();
    }


    public String getString2() {
        return "";
    }


    public String getClassString() {
        return getString();
    }


    public String getSimpleString() {
        return ClusFormat.ONE_AFTER_DOT.format(m_SumWeight);
    }


    public final double getTotalWeight() {
        return m_SumWeight;
    }


    /**
     * Provides sum of weights of target attributes
     * 
     * @return sum of weights of target attributes, or NaN if statistic doesn't contain target attributes (i.e.,
     *         unsupervised learning is performed)
     */
    public double getTargetSumWeights() {
        System.err.println(getClass().getName() + ": getTargetSumWeights(): Not yet implemented");
        return getTotalWeight();
    }


    public int getNbExamples() {
        return m_NbExamples;
    }


    public String getDebugString() {
        return String.valueOf(m_SumWeight);
    }


    public boolean samePrediction(ClusStatistic other) {
        throw new NotImplementedException();
    }


    /*
     * getError() and getErrorDiff() methods
     * - with scaling
     * - without scaling (only works for classification now!)
     **/

    public double getError() {
        return getError(null);
    }


    public double getErrorRel() { // 0<error<1
        return getError(null);
    }


    public double getErrorDiff(ClusStatistic other) {
        return getErrorDiff(null, other);
    }


    public double getError(ClusAttributeWeights scale, RowData data) {
        return getError(scale);
    }


    public double getError(ClusAttributeWeights scale) {
        // ClusLogger.info("ClusStatistic :getError");
        throw new NotImplementedException();
    }


    public double getErrorDiff(ClusAttributeWeights scale, ClusStatistic other) {
        throw new NotImplementedException();
    }


    /*
     * getSS() and getSSDiff() methods, always with scaling
     * also version available that needs access to the data
     **/

    public double getSVarS(ClusAttributeWeights scale) {
        throw new NotImplementedException();
    }


    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
        throw new NotImplementedException();
    }


    public double getSVarS(ClusAttributeWeights scale, RowData data) throws ClusException {
        return getSVarS(scale);
    }


    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other, RowData data) {
        return getSVarSDiff(scale, other);
    }


    public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights) throws ClusException {
        return Double.POSITIVE_INFINITY;
    }


    /**
     * Currently only used to compute the default dispersion within rule heuristics.
     * 
     * @throws ClusException
     */
    public double getDispersion(ClusAttributeWeights scale, RowData data) throws ClusException {
        System.err.println(getClass().getName() + ": getDispersion(): Not implemented here!");
        return Double.POSITIVE_INFINITY;
    }


    public double getSquaredDistance(DataTuple tuple, ClusAttributeWeights weights) {
        return Double.POSITIVE_INFINITY;
    }


    public double getSquaredDistance(ClusStatistic other) {
        return Double.POSITIVE_INFINITY;
    }


    public static void reset(ClusStatistic[] stat) {
        for (int i = 0; i < stat.length; i++)
            stat[i].reset();
    }


    @Override
    public String toString() {
        return getString();
    }


    public String getExtraInfo() {
        return null;
    }


    public void printDistribution(PrintWriter wrt) throws IOException {
        wrt.println(getClass().getName() + " does not implement printDistribution()");
    }


    public static void calcMeans(ClusStatistic[] stats) throws ClusException {
        for (int i = 0; i < stats.length; i++) {
            stats[i].calcMean();
        }
    }


    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
    }


    public String getPredictWriterString() {
        return getPredictString();
    }


    public String getPredictWriterString(DataTuple tuple) throws ClusException {
        return getPredictWriterString();
    }


    public void predictTuple(DataTuple prediction) {
        System.err.println(getClass().getName() + " does not implement predictTuple()");
    }


    public RegressionStat getRegressionStat() {
        return null;
    }


    public ClassificationStat getClassificationStat() {
        return null;
    }


    // In multi-label classification: predicted set of classes is union of
    // predictions of individual rules
    public void unionInit() {
    }


    public void unionDone() {
    }


    public void union(ClusStatistic other) {
    }


    public abstract void vote(ArrayList<ClusStatistic> votes);

    public abstract void vote(ArrayList<ClusStatistic> votes, ClusOOBWeights weights);
    
    public abstract void vote(ArrayList<ClusStatistic> votes, ClusROSForestInfo ROSForestInfo);

    public abstract void vote(ArrayList<ClusStatistic> votes, ClusOOBWeights weights, ClusROSForestInfo ROSForestInfo);
    
    public ClusStatistic normalizedCopy() {
        return null;
    }


    public ClusDistance getDistance() {
        return null;
    }


    public double getCount(int idx, int cls) {
        System.err.println(getClass().getName() + " does not implement predictTuple()");
        return Double.POSITIVE_INFINITY;
    }


    public String getDistanceName() {
        return "Unknown Distance";
    }


    public void setBeam(ClusBeam beam) {
    }


    public double getSumWeight() {
        return m_SumWeight;
    }


    public final Settings getSettings() {
        return m_Settings;
    }


    // daniela
    public double calcItotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentIDistance(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcGtotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcGetisTotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcLISAtotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcGLocalTotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcLocalGetisTotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcGETIStotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentItotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcIwithNeighbourstotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentIwithNeighbourstotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentGDistance(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentPDistance(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcEquvalentGtotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcItotalD(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcPDistance(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcGtotalD(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcCItotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcMutivariateItotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcCwithNeighbourstotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcBivariateLee(Integer[] permutation) throws Exception {
        throw new NotImplementedException();
    }


    public double calcMultiIwithNeighbours(Integer[] permutation) throws Exception {
        throw new NotImplementedException();
    }


    public double calcCIwithNeighbours(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcLeewithNeighbours(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcPtotal(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcCItotalD(Integer[] permutation) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcDHtotalD(Integer[] permutation) {
        throw new NotImplementedException();
    }
    // end daniela
}

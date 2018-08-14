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
import java.util.ArrayList;

import org.apache.commons.math3.distribution.TDistribution;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusOOBWeights;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROSForestInfo;
import si.ijs.kt.clus.ext.ensemble.ros.ClusROSModelInfo;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleVotingType;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public abstract class RegressionStatBase extends ClusStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int m_NbAttrs;
    protected NumericAttrType[] m_Attrs;
    public double[] m_Means;


    public void setNbAttributes(int value) {
        m_NbAttrs = value;
    }


    public RegressionStatBase(Settings sett, NumericAttrType[] attrs) {
        this(sett, attrs, false);
    }


    public RegressionStatBase(Settings sett, NumericAttrType[] attrs, boolean onlymean) {
        super(sett);

        m_Attrs = attrs;
        m_NbAttrs = attrs.length;
        if (onlymean) {
            m_Means = new double[m_NbAttrs];
        }
    }


    @Override
    public int getNbAttributes() {
        return m_NbAttrs;
    }


    public NumericAttrType[] getAttributes() {
        return m_Attrs;
    }


    public NumericAttrType getAttribute(int idx) {
        return m_Attrs[idx];
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        RegressionStatBase or = (RegressionStatBase) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_Means[i] += weight * or.m_Means[i];
        }
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        updateWeighted(tuple, tuple.getWeight());
    }


    @Override
    public void computePrediction() {
        // do not need to call calcMean() here
    }


    public abstract void calcMean(double[] means);


    @Override
    public void calcMean() {
        if (m_Means == null) {
            m_Means = new double[m_NbAttrs];
        }
        calcMean(m_Means);
    }


    public void setMeans(double[] means) {
        m_Means = means;
    }


    public abstract double getMean(int i);


    public abstract double getSVarS(int i);


    public double getVariance(int i) {
        return m_SumWeight != 0.0 ? getSVarS(i) / m_SumWeight : 0.0;
    }


    public double getStandardDeviation(int i) {
        return Math.sqrt(getSVarS(i) / (m_SumWeight - 1));
    }


    public double getScaledSS(int i, ClusAttributeWeights scale) {
        return getSVarS(i) * scale.getWeight(getAttribute(i));
    }


    public double getScaledVariance(int i, ClusAttributeWeights scale) {
        return getVariance(i) * scale.getWeight(getAttribute(i));
    }


    public double getRootScaledVariance(int i, ClusAttributeWeights scale) {
        return Math.sqrt(getScaledVariance(i, scale));
    }


    public double[] getRootScaledVariances(ClusAttributeWeights scale) {
        int nb = getNbAttributes();
        double[] res = new double[nb];
        for (int i = 0; i < res.length; i++) {
            res[i] = getRootScaledVariance(i, scale);
        }
        return res;
    }


    /**
     * Currently only used to compute the default dispersion within rule heuristics.
     */
    @Override
    public double getDispersion(ClusAttributeWeights scale, RowData data) {
        System.err.println(getClass().getName() + ": getDispersion(): Not yet implemented!");
        return Double.POSITIVE_INFINITY;
    }


    /**
     * Computes a 2-sample t statistic, without the hypothesis of equal sub-population variances,
     * and returns the p-value of a t test.
     * t = (m1 - m2) / sqrt(var1/n1 + var2/n2);
     * 
     * @param att
     *        attribute index
     * @return t p-value
     * @throws MathException
     */
    public double getTTestPValue(int att, ClusStatManager stat_manager) {
        double global_mean = ((CombStat) stat_manager.getTrainSetStat()).m_RegStat.getMean(att);
        double global_var = ((CombStat) stat_manager.getTrainSetStat()).m_RegStat.getVariance(att);
        double global_n = ((CombStat) stat_manager.getTrainSetStat()).getTotalWeight();
        double local_mean = getMean(att);
        double local_var = getVariance(att);
        double local_n = getTotalWeight();
        double t = Math.abs(local_mean - global_mean) / Math.sqrt(local_var / local_n + global_var / global_n);
        double degreesOfFreedom = 0;
        degreesOfFreedom = df(local_var, global_var, local_n, global_n);
        // DistributionFactory distributionFactory = DistributionFactory.newInstance();
        // TDistribution tDistribution = distributionFactory.createTDistribution(degreesOfFreedom);
        TDistribution tDistribution = new TDistribution(degreesOfFreedom);
        return 1.0 - tDistribution.cumulativeProbability(-t, t);
    }


    /**
     * Computes approximate degrees of freedom for 2-sample t-test.
     * source: math.commons.stat.inference.TTestImpl
     *
     * @param v1
     *        first sample variance
     * @param v2
     *        second sample variance
     * @param n1
     *        first sample n
     * @param n2
     *        second sample n
     * @return approximate degrees of freedom
     */
    protected double df(double v1, double v2, double n1, double n2) {
        return (((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) / ((v1 * v1) / (n1 * n1 * (n1 - 1d)) + (v2 * v2) / (n2 * n2 * (n2 - 1d)));
    }


    /**
     * @return Array for all the attributes.
     */
    @Override
    public double[] getNumericPred() {
        return m_Means;
    }


    @Override
    public String getPredictedClassName(int idx) {
        return "";
    }


    @Override
    public int getNbNumericAttributes() {
        return m_NbAttrs;
    }


    @Override
    public double getError(ClusAttributeWeights scale) {
        return getSVarS(scale);
    }


    @Override
    public double getErrorDiff(ClusAttributeWeights scale, ClusStatistic other) {
        return getSVarSDiff(scale, other);
    }


    public double getRMSE(ClusAttributeWeights scale) {
        return Math.sqrt(getSVarS(scale) / getTotalWeight());
    }


    public void initNormalizationWeights(ClusAttributeWeights weights, boolean[] shouldNormalize) {
        for (int i = 0; i < m_NbAttrs; i++) {
            int idx = m_Attrs[i].getIndex();
            if (shouldNormalize[idx]) {
                double var = getVariance(i);
                double norm = var > 0 ? 1 / var : 1; // No normalization if variance = 0;
                // if (m_NbAttrs < 15) ClusLogger.info(" Normalization for: "+m_Attrs[i].getName()+" = "+norm);
                weights.setWeight(m_Attrs[i], norm);
            }
        }
    }


    /*
     * Compute squared Euclidean distance between tuple's target attributes and this statistic's mean.
     **/
    @Override
    public double getSquaredDistance(DataTuple tuple, ClusAttributeWeights weights) {
        double sum = 0.0;
        for (int i = 0; i < getNbAttributes(); i++) {
            NumericAttrType type = getAttribute(i);
            double dist = type.getNumeric(tuple) - m_Means[i];
            sum += dist * dist * weights.getWeight(type);
        }
        return sum / getNbAttributes();
    }


    /*
     * Compute squared distance between each of the tuple's target attributes and this statistic's mean.
     **/
    public double[] getPointwiseSquaredDistance(DataTuple tuple, ClusAttributeWeights weights) {
        double[] distances = new double[getNbAttributes()];
        for (int i = 0; i < getNbAttributes(); i++) {
            NumericAttrType type = getAttribute(i);
            distances[i] = type.getNumeric(tuple) - m_Means[i];
            distances[i] *= distances[i];
        }
        return distances;
    }


    @Override
    public String getArrayOfStatistic() {
        ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(fr.format(m_Means[i]));
        }
        buf.append("]");
        return buf.toString();
    }


    @Override
    public String getPredictString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(String.valueOf(m_Means[i]));
        }
        return buf.toString();
    }


    @Override
    public String getDebugString() {
        ClusNumberFormat fr = ClusFormat.THREE_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(fr.format(getMean(i)));
        }
        buf.append("]");
        buf.append("[");
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(fr.format(getVariance(i)));
        }
        buf.append("]");
        return buf.toString();
    }


    @Override
    public void printDistribution(PrintWriter wrt) throws IOException {
        ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        for (int i = 0; i < m_Attrs.length; i++) {
            wrt.print(StringUtils.printStr(m_Attrs[i].getName(), 35));
            wrt.print(" [");
            wrt.print(fr.format(getMean(i)));
            wrt.print(",");
            wrt.print(fr.format(getVariance(i)));
            wrt.println("]");
        }
    }


    @Override
    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        for (int i = 0; i < m_NbAttrs; i++) {
            ClusAttrType type = m_Attrs[i].cloneType();
            type.setName(prefix + "-p-" + type.getName());
            schema.addAttrType(type);
        }
    }


    @Override
    public String getPredictWriterString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            if (m_Means != null) {
                buf.append("" + m_Means[i]);
            }
            else {
                buf.append("?");
            }
        }
        return buf.toString();
    }


    @Override
    public void predictTuple(DataTuple prediction) {
        for (int i = 0; i < m_NbAttrs; i++) {
            NumericAttrType type = m_Attrs[i];
            type.setNumeric(prediction, m_Means[i]);
        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes) {
        reset();
        m_Means = new double[m_NbAttrs];
        int nb_votes = votes.size();
        for (int model = 0; model < nb_votes; model++) {
            RegressionStatBase vote = (RegressionStatBase) votes.get(model);
            for (int target = 0; target < m_NbAttrs; target++) {
                m_Means[target] += vote.getMean(target) / nb_votes;
            }
        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes, ClusOOBWeights weights) {
        reset();
        EnsembleVotingType evt = getSettings().getEnsemble().getEnsembleVotingType();

        m_Means = new double[m_NbAttrs];
        int nb_votes = votes.size();
        for (int model = 0; model < nb_votes; model++) {
            RegressionStatBase vote = (RegressionStatBase) votes.get(model);
            switch (evt) {
                case OOBModelWeighted:
                    for (int target = 0; target < m_NbAttrs; target++) {
                        m_Means[target] += vote.getMean(target) * weights.getModelWeight(model);
                    }
                    break;

                case OOBTargetWeighted:
                    for (int target = 0; target < m_NbAttrs; target++) {
                        m_Means[target] += vote.getMean(target) * weights.getComponentWeight(model, target);
                    }

                    break;

                default:
                    throw new RuntimeException("OOB voting not defined! si.ijs.kt.clus.statistic.RegressionStatBase.vote(ArrayList<ClusStatistic>, ClusOOBWeights)");
            }

        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes, ClusROSForestInfo ROSForestInfo) {
        reset();
        m_Means = new double[m_NbAttrs];
        double[] coverage = ROSForestInfo.getCoverage();

        for (int model = 0; model < votes.size(); model++) {
            RegressionStatBase vote = (RegressionStatBase) votes.get(model);

            ClusROSModelInfo info = ROSForestInfo.getROSModelInfo(model);

            for (Integer target : info.getTargets()) {
                m_Means[target] += vote.getMean(target) / coverage[target];
            }
        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes, ClusOOBWeights weights, ClusROSForestInfo ROSForestInfo) {
        reset();
        m_Means = new double[m_NbAttrs];

        EnsembleVotingType evt = getSettings().getEnsemble().getEnsembleVotingType();

        for (int model = 0; model < votes.size(); model++) {
            RegressionStatBase vote = (RegressionStatBase) votes.get(model);

            ClusROSModelInfo info = ROSForestInfo.getROSModelInfo(model);
            switch (evt) {
                case OOBModelWeighted:
                    for (Integer target : info.getTargets()) {
                        m_Means[target] += vote.getMean(target) * weights.getComponentWeight(model, target);
                    }
                    break;

                case OOBTargetWeighted:
                    for (Integer target : info.getTargets()) {
                        m_Means[target] += vote.getMean(target) * weights.getComponentWeight(model, target);
                    }
                    break;

                default:
                    throw new RuntimeException("OOB voting not defined! si.ijs.kt.clus.statistic.RegressionStatBase.vote(ArrayList<ClusStatistic>, ClusOOBWeights, ClusROSForestInfo)");
            }
        }
    }
}

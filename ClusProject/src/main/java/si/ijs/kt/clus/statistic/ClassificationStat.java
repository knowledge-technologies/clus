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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.heuristic.GISHeuristic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.math.MathUtil;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


/**
 * Classification statistics about the data. A child of ClusStatistic.
 *
 */
public class ClassificationStat extends ClusStatistic implements ComponentStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public int m_NbTarget;
    private int m_NbAttrs; // daniela  // matejp: Inspect the code and you will see that m_NbAttrs = 0 for ever and ever
    public NominalAttrType[] m_Attrs;
    public ClassificationStat m_Training;
    public ClassificationStat m_ParentStat; //statistic of the parent node

    // * Class counts with [Target index][Class]
    public double[][] m_ClassCounts;
    public double[] m_SumWeights;
    public int[] m_MajorityClasses;
    // Thresholds used in making predictions in multi-label classification
    public double[] m_Thresholds;

    // daniela  matejp: public --> private, static INIT_PART_SUM ---> no longer static.
    public double m_I;
    public RowData m_data;
    public RowData temp_data;
    public double[] m_SumValuesSpatial; //daniela
    private int splitIndex;
    private int prevIndex;

    public double[] previousSumW;
    public double[] previousSumX;
    public double[] previousSumXR;
    public double[] previousSumWXX;
    public double[] previousSumWX;
    public double[] previousSumX2;
    public double[] previousSumWXXR;
    public double[] previousSumWXR;
    public double[] previousSumWR;
    public double[] previousSumX2R;
    public static boolean INITIALIZEPARTIALSUM = true;

    public class Distance {

        double index;
        double target;
        double distance;


        Distance(double index, double target, double distance) {
            this.index = index;
            this.target = target;
            this.distance = distance;
        }
    }

    public class DistanceB {

        double index;
        double target1;
        double target2;
        double distance;


        DistanceB(double index, double target1, double target2, double distance) {
            this.index = index;
            this.target1 = target1;
            this.target2 = target2;
            this.distance = distance;
        }
    }
    // end daniela


    /**
     * Constructor for this class.
     */
    public ClassificationStat(Settings sett, NominalAttrType[] nomAtts) {
        super(sett);

        initialize(nomAtts);
    }


    /**
     * Constructor for this class in the case of multi-label setting.
     * 
     * @param nomAtts
     * @param multiLabelThreshold
     */
    public ClassificationStat(Settings sett, NominalAttrType[] nomAtts, INIFileNominalOrDoubleOrVector multiLabelThreshold) {
        this(sett, nomAtts);

        double[] thresholds = multiLabelThreshold.getDoubleVector();
        if (thresholds != null) {
            setThresholds(thresholds);
        }
        else {
            setThresholds(multiLabelThreshold.getDouble());
        }
    }


    private void initialize(NominalAttrType[] nomAtts) {
        m_NbTarget = nomAtts.length;
        m_SumWeights = new double[m_NbTarget];
        m_ClassCounts = new double[m_NbTarget][];
        for (int i = 0; i < m_NbTarget; i++) {
            m_ClassCounts[i] = new double[nomAtts[i].getNbValues()];
        }
        m_Attrs = nomAtts;

        // daniela
        previousSumX = new double[2];
        previousSumXR = new double[2];
        previousSumW = new double[2];
        previousSumWXX = new double[2];
        previousSumWX = new double[2];
        previousSumX2 = new double[2];

        previousSumWR = new double[2];
        previousSumWXXR = new double[2];
        previousSumWXR = new double[2];
        previousSumX2R = new double[2];
        // end daniela
    }


    @Override
    public void setTrainingStat(ClusStatistic train) {
        m_Training = (ClassificationStat) train;
    }


    /**
     * 
     * @param thresholds
     *        Every target has its own threshold for predictions.
     */
    public void setThresholds(double[] thresholds) {
        m_Thresholds = new double[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {
            m_Thresholds[i] = thresholds[i];
        }
    }


    /**
     * 
     * @param threshold
     *        All targets have the same threshold.
     */
    public void setThresholds(double threshold) {
        m_Thresholds = new double[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {
            m_Thresholds[i] = threshold;
        }
    }


    @Override
    public int getNbNominalAttributes() {
        return m_NbTarget;
    }


    public NominalAttrType[] getAttributes() {
        return m_Attrs;
    }


    @Override
    public ClusStatistic cloneStat() {
        ClassificationStat res = new ClassificationStat(m_Settings, m_Attrs);
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        if (m_Thresholds != null) {
            res.m_Thresholds = Arrays.copyOf(m_Thresholds, m_Thresholds.length);
        }
        return res;
    }


    public void initSingleTargetFrom(double[] distro) {
        m_ClassCounts[0] = distro;
        m_SumWeight = 0.0;
        for (int i = 0; i < distro.length; i++) {
            m_SumWeight += distro[i];
        }
        Arrays.fill(m_SumWeights, m_SumWeight);
    }


    /**
     * Returns the nominal attribute.
     * Added because RegressionStat has this method.
     * 
     * @param idx
     *        index of attribute.
     * @return NominalAttrType
     */
    public NominalAttrType getAttribute(int idx) {
        return m_Attrs[idx];
    }


    @Override
    public void reset() {
        m_NbExamples = 0;
        m_SumWeight = 0.0;
        m_SumWeightLabeled = 0.0;
        Arrays.fill(m_SumWeights, 0.0);
        for (int i = 0; i < m_NbTarget; i++) {
            Arrays.fill(m_ClassCounts[i], 0.0);
        }
    }


    /**
     * Resets the SumWeight and majority class count to weight and all other
     * class counts to zero.
     */
    @Override
    public void resetToSimple(double weight) {
        m_NbExamples = 0;
        m_SumWeight = weight;
        Arrays.fill(m_SumWeights, weight);
        for (int i = 0; i < m_NbTarget; i++) {
            double[] clcts = m_ClassCounts[i];
            for (int j = 0; j < clcts.length; j++) {
                if (j == m_MajorityClasses[i]) {
                    clcts[j] = weight;
                }
                else {
                    clcts[j] = 0.0;
                }
            }
        }
    }


    @Override
    public void copy(ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight = or.m_SumWeight;
        m_SumWeightLabeled = or.m_SumWeightLabeled;
        m_NbExamples = or.m_NbExamples;
        System.arraycopy(or.m_SumWeights, 0, m_SumWeights, 0, m_NbTarget);
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            System.arraycopy(or.m_ClassCounts[i], 0, my, 0, my.length);
        }
    }


    /**
     * Used for combining weighted predictions.
     */
    @Override
    public ClassificationStat normalizedCopy() {
        ClassificationStat copy = (ClassificationStat) cloneStat();
        copy.copy(this);
        for (int i = 0; i < m_NbTarget; i++) {
            for (int j = 0; j < m_ClassCounts[i].length; j++) {
                copy.m_ClassCounts[i][j] /= m_SumWeights[i];
            }
        }
        Arrays.fill(copy.m_SumWeights, 1.0);
        copy.m_SumWeight = 1.0;
        return copy;
    }


    @Override
    public boolean samePrediction(ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        for (int i = 0; i < m_NbTarget; i++)
            if (m_MajorityClasses[i] != or.m_MajorityClasses[i])
                return false;
        return true;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight += weight * or.m_SumWeight;
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            double[] your = or.m_ClassCounts[i];
            for (int j = 0; j < my.length; j++)
                my[j] += weight * your[j];
            m_SumWeights[i] += weight * or.m_SumWeights[i];
        }
    }


    @Override
    public void add(ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight += or.m_SumWeight;
        m_SumWeightLabeled += or.m_SumWeightLabeled;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            double[] your = or.m_ClassCounts[i];
            for (int j = 0; j < my.length; j++)
                my[j] += your[j];
            m_SumWeights[i] += or.m_SumWeights[i];
        }
    }


    @Override
    public void addScaled(double scale, ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight += scale * or.m_SumWeight;
        m_SumWeightLabeled += scale * or.m_SumWeightLabeled;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            double[] your = or.m_ClassCounts[i];
            for (int j = 0; j < my.length; j++)
                my[j] += scale * your[j];
            m_SumWeights[i] += scale * or.m_SumWeights[i];
        }
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight -= or.m_SumWeight;
        m_SumWeightLabeled -= or.m_SumWeightLabeled;
        m_NbExamples -= or.m_NbExamples;
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            double[] your = or.m_ClassCounts[i];
            for (int j = 0; j < my.length; j++)
                my[j] -= your[j];
            m_SumWeights[i] -= or.m_SumWeights[i];
        }
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        ClassificationStat or = (ClassificationStat) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_SumWeightLabeled = or.m_SumWeightLabeled - m_SumWeightLabeled;
        m_NbExamples = or.m_NbExamples - m_NbExamples;
        for (int i = 0; i < m_NbTarget; i++) {
            double[] my = m_ClassCounts[i];
            double[] your = or.m_ClassCounts[i];
            for (int j = 0; j < my.length; j++)
                my[j] = your[j] - my[j];
            m_SumWeights[i] = or.m_SumWeights[i] - m_SumWeights[i];
        }
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        updateWeighted(tuple, tuple.getWeight());
    }


    @Override
    public void updateWeighted(DataTuple tuple, double weight) {
        m_NbExamples++;
        m_SumWeight += weight;
        boolean hasLabel = false;
        for (int i = 0; i < m_NbTarget; i++) {
            int val = m_Attrs[i].getNominal(tuple);
            // System.out.println("val: "+ val);
            if (val != m_Attrs[i].getNbValues()) {
                m_ClassCounts[i][val] += weight;
                m_SumWeights[i] += weight;

                if (!hasLabel && m_Attrs[i].isTarget())
                    hasLabel = true;
            }
        }
        if (hasLabel)
            m_SumWeightLabeled += weight;
    }


    /**
     * Returns the majority class for a given attribute. If m_Thresholds are not defined, we return the majority class
     * (equivalent to threshold = 0.5), otherwise,
     * it is currently guaranteed that this is MLC-case, and the label is considered relevant if P(label) >= threshold.
     * Usually, this can be computed as
     * <p>
     * 
     * {@code m_ClassCounts[attr][0] / m_SumWeights[attr]}
     * <p>
     * but in voting procedure (see, e.g. {@link #voteProbDistr(ArrayList)}, the counts are
     * normalized whereas the sums of weights are not, hence, the label is considered relevant IFF
     * <p>
     * 
     * {@code clcts[0] / (clcts[0] + clcts[1]) >= m_Thresholds[attr]}
     * 
     * @param attr
     *        Index of the attribute, whose majority class is returned.
     * @return The index that corresponds to the value of majority class.
     */
    public int getMajorityClass(int attr) {
        int m_class = -1;
        double m_max = Double.NEGATIVE_INFINITY;
        double[] clcts = m_ClassCounts[attr];
        for (int i = 0; i < clcts.length; i++) {
            if (clcts[i] > m_max) {
                m_class = i;
                m_max = clcts[i];
            }
        }
        if (m_max <= MathUtil.C1E_9 && m_Training != null) {
            // no examples covered -> m_max = null -> use whole training set majority class
            switch (getSettings().getTree().getMissingTargetAttrHandling()) {
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                    return m_Training.getMajorityClass(attr);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                    return m_ParentStat.getMajorityClass(attr);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                    return 0;
                default:
                    return m_Training.getMajorityClass(attr);
            }
        }
        else {
            if (m_Thresholds != null) { // IFF multi label
                return clcts[0] / (clcts[0] + clcts[1]) >= m_Thresholds[attr] ? 0 : 1; // label is relevant (class index
                                                                                       // 0) IFF we exceed the threshold
            }
            else {
                return m_class;
            }
        }

    }


    public int getMajorityClassDiff(int attr, ClassificationStat other) {
        if (m_Thresholds != null) { throw new RuntimeException("Not implemented for MLC."); }
        int m_class = -1;
        double m_max = Double.NEGATIVE_INFINITY;
        double[] clcts1 = m_ClassCounts[attr];
        double[] clcts2 = other.m_ClassCounts[attr];
        for (int i = 0; i < clcts1.length; i++) {
            double diff = clcts1[i] - clcts2[i];
            if (diff > m_max) {
                m_class = i;
                m_max = diff;
            }
        }
        if (m_max <= MathUtil.C1E_9 && m_Training != null) {
            // no examples covered -> m_max = null -> use whole training set majority class
            switch (getSettings().getTree().getMissingTargetAttrHandling()) {
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                    return m_Training.getMajorityClass(attr);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                    return m_ParentStat.getMajorityClass(attr);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                    return 0;
                default:
                    return m_Training.getMajorityClass(attr);
            }
        }
        return m_class;
    }

    // /**
    // * Computes relative frequency of a given label. Used in MLC only.
    // * @param attr index of the attribute
    // * @return Relative frequency of the label indexed by {@code attr} if there is at least one example with known
    // value for this label.
    // * Otherwise, Double.Nan is returned.
    // */
    // public double computeRelativeFrequencyOfLabel(int attr){
    // double sum = m_ClassCounts[attr][0] + m_ClassCounts[attr][1];
    // if(sum > 0){
    // return m_ClassCounts[attr][0] / sum;
    // } else{
    // return Double.NaN;
    // }
    // }
    // /**
    // * Will be removed.
    // * @return
    // */
    // public double[] getRelativeFrequenciesLabels(){
    // double[] freqs = new double[m_NbTarget];
    // for(int i = 0; i < freqs.length; i++){
    // freqs[i] = computeRelativeFrequencyOfLabel(i);
    // }
    // return freqs;
    // }


    //daniela
    public double entropy() {
        double sum = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {
            sum += entropy(i);
        }
        return sum;
    }


    public double entropyDifference(ClassificationStat other) {
        double sum = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {
            sum += entropyDifference(i, other);
        }
        return sum;
    }
    //end daniela


    // ENTROPY
    public double entropy(int attr) {
        double total = m_SumWeights[attr];

        if (total < MathUtil.C1E_6) {
            return 0.0;
        }
        else {
            double acc = 0.0;
            double[] clcts = m_ClassCounts[attr];
            for (int i = 0; i < clcts.length; i++) {
                if (clcts[i] != 0.0) {
                    double prob = clcts[i] / total;
                    acc += prob * Math.log(prob);
                }
            }
            return -acc / MathUtil.M_LN2;
        }
    }


    public double modifiedEntropy(int attr) {

        double total = m_SumWeights[attr];

        // System.out.print("Class has "+total+" examples\n");

        if (total < MathUtil.C1E_6) {
            return 0.0;
        }
        else {
            double acc = 0.0;
            double[] clcts = m_ClassCounts[attr];
            for (int i = 0; i < clcts.length; i++) {
                if (clcts[i] != 0.0) {

                    double prob = (clcts[i] + 1) / (total + clcts.length);
                    acc += prob * Math.log(prob);
                }
            }
            return -acc / MathUtil.M_LN2;
        }
    }


    public double entropyDifference(ClassificationStat other, ClusAttributeWeights scale) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return entropyDifferenceTargetSubspace(other, scale);

        double sum = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {

            if (other.getAttribute(i).getSchema().getSettings().getTree().checkEntropyType("StandardEntropy")) {
                sum += entropyDifference(i, other);
            }
            else {
                sum += modifiedEntropyDifference(i, other);
            }
        }
        return sum;
    }


    public double entropyDifferenceTargetSubspace(ClassificationStat other, ClusAttributeWeights scale) {
        double sum = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            if (other.getAttribute(i).getSchema().getSettings().getTree().checkEntropyType("StandardEntropy")) {
                sum += entropyDifference(i, other);
            }
            else {
                sum += modifiedEntropyDifference(i, other);
            }
        }
        return sum;
    }


    public double entropy(ClusAttributeWeights scale) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return entropyTargetSubspace(scale);

        double sum = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {
            if (getAttribute(i).getSchema().getSettings().getTree().checkEntropyType("StandardEntropy")) {
                sum += entropy(i);
            }
            else {
                sum += modifiedEntropy(i);
            }
        }
        return sum;
    }


    public double entropyTargetSubspace(ClusAttributeWeights scale) {
        double sum = 0.0;

        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            if (getAttribute(i).getSchema().getSettings().getTree().checkEntropyType("StandardEntropy")) {
                sum += entropy(i);
            }
            else {
                sum += modifiedEntropy(i);
            }
        }
        return sum;
    }


    double entropyDifference(int attr, ClassificationStat other) {
        double acc = 0.0;
        double[] clcts = m_ClassCounts[attr];
        double[] otcts = other.m_ClassCounts[attr];
        double total = m_SumWeights[attr] - other.m_SumWeights[attr];
        for (int i = 0; i < clcts.length; i++) {
            double diff = clcts[i] - otcts[i];
            if (diff != 0.0)
                acc += diff / total * Math.log(diff / total);
        }
        return -acc / MathUtil.M_LN2;
    }


    double modifiedEntropyDifference(int attr, ClassificationStat other) {
        double acc = 0.0;
        double[] clcts = m_ClassCounts[attr];
        double[] otcts = other.m_ClassCounts[attr];
        double total = m_SumWeights[attr] - other.m_SumWeights[attr];
        for (int i = 0; i < clcts.length; i++) {
            double diff = clcts[i] - otcts[i];
            if (diff != 0.0)
                acc += ((diff + 1) / (total + clcts.length)) * Math.log((diff + 1) / (total + clcts.length));
        }
        return -acc / MathUtil.M_LN2;
    }


    // GINI
    public double gini(int attr) {
        double total = m_SumWeights[attr];
        if (total <= MathUtil.C1E_9) {
            // no examples -> assume training set distribution
            // return m_Training.gini(attr); // gives error with HSC script
            //return 0.0;
            return getEsimatedGini(attr);
        }
        else {
            // System.err.println(": here2");
            double sum = 0.0;
            double[] clcts = m_ClassCounts[attr];
            for (int i = 0; i < clcts.length; i++) {
                double prob = clcts[i] / total;
                sum += prob * prob;
            }
            return 1.0 - sum;
        }
    }


    public double giniDifference(int attr, ClassificationStat other) {
        double wDiff = m_SumWeights[attr] - other.m_SumWeights[attr];
        if (wDiff <= MathUtil.C1E_9) {
            // no examples -> assume training set distribution
            // return m_Training.gini(attr);
            return other.getEsimatedGini(attr);
        }
        else {
            double sum = 0.0;
            double[] clcts = m_ClassCounts[attr];
            double[] otcts = other.m_ClassCounts[attr];
            for (int i = 0; i < clcts.length; i++) {
                double diff = clcts[i] - otcts[i];
                sum += (diff / wDiff) * (diff / wDiff);
            }
            return 1.0 - sum;
        }
    }


    /**
     * If there is no examples for an attribute i, gini is estimated according to setting given in settings file
     * 
     * @param i
     *        index of an attribute
     * @return
     */
    public double getEsimatedGini(int i) {
        switch (getSettings().getTree().getMissingClusteringAttrHandling()) {
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                if (m_Training == null)
                    return Double.NaN;
                return m_Training.gini(i);
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                //System.out.println(this.getTotalWeight());
                if (m_ParentStat == null)
                    return Double.NaN; // the case if for attribute i all examples gave missing values (if there is no parent stat, it means we reached the root node)
                return m_ParentStat.gini(i);
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                return Double.NaN;
            default:
                if (m_Training == null)
                    return Double.NaN;
                return m_Training.gini(i);
        }
    }


    // ERROR
    @Override
    public double getError(ClusAttributeWeights scale) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return getErrorTargetSubspace(scale);

        double result = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {
            int maj = getMajorityClass(i);
            result += m_SumWeights[i] - m_ClassCounts[i][maj];
        }
        return result / m_NbTarget;
    }


    public double getErrorTargetSubspace(ClusAttributeWeights scale) {
        double result = 0.0;
        int cnt = 0;
        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            int maj = getMajorityClass(i);
            result += m_SumWeights[i] - m_ClassCounts[i][maj];
        }
        return result / cnt;
    }


    @Override
    public double getErrorRel() {
        // System.out.println("ClassificationStat getErrorRel");
        // System.out.println("ClassificationStat nb example in the leaf "+m_SumWeight);
        return getError() / getTotalWeight();
    }


    @Override
    public double getErrorDiff(ClusAttributeWeights scale, ClusStatistic other) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return getErrorDiffTargetSubspace(scale, other);

        double result = 0.0;
        ClassificationStat or = (ClassificationStat) other;
        for (int i = 0; i < m_NbTarget; i++) {
            int maj = getMajorityClassDiff(i, or);
            double diff_maj = m_ClassCounts[i][maj] - or.m_ClassCounts[i][maj];
            double diff_total = m_SumWeights[i] - or.m_SumWeights[i];
            result += diff_total - diff_maj;
        }
        return result / m_NbTarget;
    }


    public double getErrorDiffTargetSubspace(ClusAttributeWeights scale, ClusStatistic other) {
        double result = 0.0;
        ClassificationStat or = (ClassificationStat) other;
        int cnt = 0;
        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            int maj = getMajorityClassDiff(i, or);
            double diff_maj = m_ClassCounts[i][maj] - or.m_ClassCounts[i][maj];
            double diff_total = m_SumWeights[i] - or.m_SumWeights[i];
            result += diff_total - diff_maj;
        }
        return result / cnt;
    }


    // VARIANCE REDUCTION
    @Override
    public double getSVarS(ClusAttributeWeights scale) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return getSVarSTargetSubspace(scale);

        // System.err.println(": here");
        double result = 0.0, SVarS;
        int nbEffectiveAttrs = m_NbTarget;
        //double sum = m_SumWeight;
        for (int i = 0; i < m_NbTarget; i++) {
            SVarS = getSVarS(i);
            if (Double.isNaN(SVarS)) {
                nbEffectiveAttrs--;
            }
            else {
                // System.out.println(gini(i) + " " + scale.getWeight(m_Attrs[i]) + " " + sum);
                result += getSVarS(i) * scale.getWeight(m_Attrs[i]);
            }
        }

        if (nbEffectiveAttrs == 0) { return Double.POSITIVE_INFINITY; }

        return result / nbEffectiveAttrs;
    }


    @Override
    public double getSVarS(int i) {
        return gini(i) * m_SumWeights[i];
    }


    public double getSVarSTargetSubspace(ClusAttributeWeights scale) {
        double result = 0.0;
        // double sum = m_SumWeight;
        int cnt = 0;

        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            result += getSVarS(i) * scale.getWeight(m_Attrs[i]);
        }
        return result / cnt;
    }


    @Override
    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
        if (getSettings().getEnsemble().isEnsembleROSEnabled())
            return getSVarSDiffTargetSubspace(scale, other);

        ClassificationStat or = (ClassificationStat) other;
        double result = 0.0, SVarSDiff;
        double sum = m_SumWeight - other.m_SumWeight;
        int nbEffectiveAttrs = m_NbTarget;
        ClassificationStat cother = (ClassificationStat) other;
        for (int i = 0; i < m_NbTarget; i++) {
            SVarSDiff = giniDifference(i, cother);
            if (Double.isNaN(SVarSDiff)) {
                nbEffectiveAttrs--;
            }
            else {
                result += SVarSDiff * scale.getWeight(m_Attrs[i]) * (m_SumWeights[i] - or.m_SumWeights[i]);
            }
        }

        if (nbEffectiveAttrs == 0) { return Double.POSITIVE_INFINITY; }

        return result / nbEffectiveAttrs;
    }


    public double getSVarSDiffTargetSubspace(ClusAttributeWeights scale, ClusStatistic other) {
        double result = 0.0;
        double sum = m_SumWeight - other.m_SumWeight;
        int cnt = 0;
        ClassificationStat cother = (ClassificationStat) other;
        for (int i = 0; i < m_NbTarget; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            result += giniDifference(i, cother) * scale.getWeight(m_Attrs[i]) * sum;
        }
        return result / cnt;
    }


    public double getSumWeight(int attr) {
        return m_SumWeights[attr];
    }


    public double getProportion(int attr, int cls) {
        double total = m_SumWeights[attr];
        if (total <= MathUtil.C1E_9) {
            // no examples -> assume training set distribution | matejp: there is a bug here ... Pray that the program does not visit this branch:)
            return m_Training.getProportion(attr, cls);
        }
        else {
            return m_ClassCounts[attr][cls] / total;
        }
    }


    public static double computeSplitInfo(double sum_tot, double sum_pos, double sum_neg) {
        if (sum_pos == 0.0)
            return -1.0 * sum_neg / sum_tot * Math.log(sum_neg / sum_tot) / MathUtil.M_LN2;
        if (sum_neg == 0.0)
            return -1.0 * sum_pos / sum_tot * Math.log(sum_pos / sum_tot) / MathUtil.M_LN2;
        return -(sum_pos / sum_tot * Math.log(sum_pos / sum_tot) + sum_neg / sum_tot * Math.log(sum_neg / sum_tot)) / MathUtil.M_LN2;
    }


    public boolean isCalcMean() {
        return m_MajorityClasses != null;
    }


    @Override
    public void calcMean() {
        m_MajorityClasses = new int[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {
            m_MajorityClasses[i] = getMajorityClass(i);
        }
    }


    // daniela
    public void calcMeanSpatial() {
        m_MajorityClasses = new int[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {
            m_MajorityClasses[i] = getMajorityClass(i);
        }
    }
    // end daniela


    /**
     * Used in RankingLoss.addExaple methods.
     * 
     * @return The list of scores (for all labels LabelI) numberOfSampelsWithLabelI / numberOfKnownSamples, where
     *         numberOfKnownSamples is
     *         the number of samples that have known value (0 or 1) for label I.
     */
    public double[] calcScores() {
        double[] scores = new double[m_NbTarget];
        for (int attr = 0; attr < m_NbTarget; attr++) {
            scores[attr] = m_ClassCounts[attr][0] / m_SumWeights[attr]; // Value "1" is always the first: see
                                                                        // NominalAttrType constructor.
        }
        return scores;
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
     * Computes a G statistic and returns the p-value of a G test.
     * G = 2*SUM(obs*ln(obs/exp))
     * 
     * @param att
     *        attribute index
     * @return p-value
     * @throws MathException
     */
    public double getGTestPValue(int att, ClusStatManager stat_manager) {
        double global_n = ((CombStat) stat_manager.getTrainSetStat()).getTotalWeight();
        double local_n = getTotalWeight();
        double ratio = local_n / global_n;
        double global_counts[] = ((CombStat) stat_manager.getTrainSetStat()).m_ClassStat.getClassCounts(att);
        double local_counts[] = getClassCounts(att);
        double g = 0;
        for (int i = 0; i < global_counts.length; i++) {
            if ((local_counts[i] > 0) && (global_counts[i] > 0)) {
                g += 2 * local_counts[i] * Math.log(local_counts[i] / (global_counts[i] * ratio));
            }
        }
        double degreesOfFreedom = ((double) global_counts.length) - 1;
        //DistributionFactory distributionFactory = DistributionFactory.newInstance();
        //ChiSquaredDistribution chiSquaredDistribution = distributionFactory.createChiSquareDistribution(degreesOfFreedom);
        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(degreesOfFreedom);
        return 1 - chiSquaredDistribution.cumulativeProbability(g);
    }


    /**
     * Computes a G statistic and returns the result of a G test.
     * G = 2*SUM(obs*ln(obs/exp))
     * 
     * @param att
     *        attribute index
     * @return p-value
     * @throws MathException
     */
    public boolean getGTest(int att, ClusStatManager stat_manager) {
        double global_n = ((CombStat) stat_manager.getTrainSetStat()).getTotalWeight();
        double local_n = getTotalWeight();
        double ratio = local_n / global_n;
        double global_counts[] = ((CombStat) stat_manager.getTrainSetStat()).m_ClassStat.getClassCounts(att);
        double local_counts[] = getClassCounts(att);
        double g = 0;
        for (int i = 0; i < global_counts.length; i++) {
            if ((local_counts[i] > 0) && (global_counts[i] > 0)) {
                g += 2 * local_counts[i] * Math.log(local_counts[i] / (global_counts[i] * ratio));
            }
        }
        int df = global_counts.length - 1;
        double chi2_crit = stat_manager.getChiSquareInvProb(df);
        return (g > chi2_crit);
    }


    @Override
    public double[] getNumericPred() {
        return null;
    }


    @Override
    public int[] getNominalPred() {
        return m_MajorityClasses;
    }


    @Override
    public String getString2() {
        StringBuffer buf = new StringBuffer();
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        buf.append(fr.format(m_SumWeight));
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public String getArrayOfStatistic() {
        StringBuffer buf = new StringBuffer();
        if (m_MajorityClasses != null) {
            buf.append("[");
            for (int i = 0; i < m_NbTarget; i++) {
                if (i != 0)
                    buf.append(",");
                buf.append("\"" + m_Attrs[i].getValue(m_MajorityClasses[i]) + "\"");
            }
            buf.append("]");
        }
        return buf.toString();

    }


    /**
     * Prints the statistics - predictions : weighted sum of all examples
     */
    @Override
    public String getString(StatisticPrintInfo info) {
        StringBuffer buf = new StringBuffer();
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        if (m_MajorityClasses != null) {// print the name of the majority class
            buf.append("[");
            for (int i = 0; i < m_NbTarget; i++) {
                if (i != 0)
                    buf.append(",");
                buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
            }
            buf.append("]");
        }
        else {
            buf.append("?");
        }
        if (info.SHOW_DISTRIBUTION) {
            for (int j = 0; j < m_NbTarget; j++) {
                buf.append(" [");
                for (int i = 0; i < m_ClassCounts[j].length; i++) {
                    if (i != 0)
                        buf.append(",");
                    buf.append(m_Attrs[j].getValue(i));
                    buf.append(":");
                    buf.append(fr.format(m_ClassCounts[j][i]));
                }
                buf.append("]");
            } // end for
            if (info.SHOW_EXAMPLE_COUNT) {
                buf.append(":");
                buf.append(fr.format(m_SumWeight));
            }
        } // end if show distribution
        else {
            // print stat on the majority classes
            if (m_MajorityClasses != null) {
                buf.append(" [");
                for (int i = 0; i < m_NbTarget; i++) {
                    if (i != 0)
                        buf.append(",");
                    buf.append(m_ClassCounts[i][m_MajorityClasses[i]]);
                }
                // added colon here to make trees print correctly
                buf.append("]: ");
                buf.append(fr.format(m_SumWeight));
            }
        }
        return buf.toString();
    }


    @Override
    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        for (int i = 0; i < m_NbTarget; i++) {
            ClusAttrType type = m_Attrs[i].cloneType();
            type.setName(prefix + "-p-" + type.getName());
            schema.addAttrType(type);
        }
        for (int i = 0; i < m_NbTarget; i++) {
            for (int j = 0; j < m_ClassCounts[i].length; j++) {
                String value = m_Attrs[i].getValue(j);
                ClusAttrType type = new NumericAttrType(prefix + "-p-" + m_Attrs[i].getName() + "-" + value);
                schema.addAttrType(type);
            }
        }
    }


    @Override
    public String getPredictWriterString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_NbTarget; i++) {
            if (i != 0)
                buf.append(",");
            if (m_MajorityClasses != null) {
                buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
            }
            else {
                buf.append("?");
            }
        }
        for (int i = 0; i < m_NbTarget; i++) {
            for (int j = 0; j < m_ClassCounts[i].length; j++) {
                buf.append(",");
                buf.append("" + m_ClassCounts[i][j]);
            }
        }
        return buf.toString();
    }


    public int getNbClasses(int idx) {
        return m_ClassCounts[idx].length;
    }


    @Override
    public double getCount(int idx, int cls) {
        return m_ClassCounts[idx][cls];
    }


    // changed elisa 13/06/2007, changed back by celine 16/12/2008
    @Override
    public String getPredictedClassName(int idx) {
        // return m_Attrs[idx].getName()+" = "+m_Attrs[idx].getValue(m_MajorityClasses[idx]);
        return m_Attrs[idx].getValue(getMajorityClass(idx));
    }


    public String getClassName(int idx, int cls) {
        return m_Attrs[idx].getValue(cls);
    }


    public void setCount(int idx, int cls, double count) {
        m_ClassCounts[idx][cls] = count;
    }


    @Override
    public String getSimpleString() {
        return getClassString() + " : " + super.getSimpleString();
    }


    @Override
    public String getClassString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_NbTarget; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
        }
        return buf.toString();
    }


    // public int getNbPseudoTargets() {
    // int nbTarget = 0;
    // for (int i = 0; i < m_NbTarget; i++) {
    // nbTarget += m_Attrs[i].getNbValues();
    // }
    // return nbTarget;
    // }

    public void initNormalizationWeights(ClusAttributeWeights weights, boolean[] shouldNormalize) {
        for (int i = 0; i < m_NbTarget; i++) {
            int idx = m_Attrs[i].getIndex();
            if (shouldNormalize[idx]) {
                double var = gini(i);
                double norm = var > 0 ? 1 / var : 1; // No normalization if variance = 0;
                // if (m_NbTarget < 15) System.out.println(" Normalization for: "+m_Attrs[i].getName()+" = "+norm);
                weights.setWeight(m_Attrs[i], norm);
            }
        }
    }


    /**
     * Returns class counts for given target
     * 
     * @param i
     *        Target index
     * @return [Class index]
     */
    public double[] getClassCounts(int i) {
        return m_ClassCounts[i];
    }


    /**
     * Returns class counts for all targets
     * 
     * @return [Target index][Class index]
     */
    public double[][] getClassCounts() {
        return m_ClassCounts;
    }


    @Override
    public String toString() {
        return getString();
    }


    @Override
    public void printDistribution(PrintWriter wrt) throws IOException {
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        for (int i = 0; i < m_Attrs.length; i++) {
            wrt.print(StringUtils.printStr(m_Attrs[i].getName(), 35));
            wrt.print(" [");
            double sum = 0.0;
            for (int j = 0; j < m_ClassCounts[i].length; j++) {
                if (j != 0)
                    wrt.print(",");
                wrt.print(m_Attrs[i].getValue(j) + ":");
                wrt.print(fr.format(m_ClassCounts[i][j]));
                sum += m_ClassCounts[i][j];
            }
            wrt.println("]: " + fr.format(sum));
        }
    }


    @Override
    public void predictTuple(DataTuple prediction) {
        for (int i = 0; i < m_NbTarget; i++) {
            NominalAttrType type = m_Attrs[i];
            type.setNominal(prediction, m_MajorityClasses[i]);
        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes) {

        switch (getSettings().getEnsemble().getClassificationVoteType()) {
            case SettingsEnsemble.VOTING_TYPE_MAJORITY:
                voteMajority(votes);
                break;
            case SettingsEnsemble.VOTING_TYPE_PROBAB_DISTR:
                voteProbDistr(votes);
                break;
            default:
                voteProbDistr(votes);
        }
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes, ClusEnsembleROSInfo targetSubspaceInfo) {
        switch (getSettings().getEnsemble().getClassificationVoteType()) {
            case SettingsEnsemble.VOTING_TYPE_MAJORITY:
                voteMajority(votes, targetSubspaceInfo);
                break;
            case SettingsEnsemble.VOTING_TYPE_PROBAB_DISTR:
                voteProbDistr(votes, targetSubspaceInfo);
                break;
            default:
                voteProbDistr(votes, targetSubspaceInfo);
        }
    }


    public void voteMajority(ArrayList<ClusStatistic> votes) {
        reset();
        int nb_votes = votes.size();
        m_SumWeight = nb_votes;
        Arrays.fill(m_SumWeights, nb_votes);
        for (int j = 0; j < nb_votes; j++) {
            ClassificationStat vote = (ClassificationStat) votes.get(j);
            for (int i = 0; i < m_NbTarget; i++) {
                m_ClassCounts[i][vote.getNominalPred()[i]]++;
            }
        }
        calcMean();
    }


    public void voteMajority(ArrayList<ClusStatistic> votes, ClusEnsembleROSInfo targetSubspaceInfo) {
        reset();
        int nb_votes = votes.size();
        m_SumWeight = nb_votes;
        Arrays.fill(m_SumWeights, nb_votes);
        for (int j = 0; j < nb_votes; j++) {
            ClassificationStat vote = (ClassificationStat) votes.get(j);
            int[] enabled = targetSubspaceInfo.getOnlyTargets(targetSubspaceInfo.getModelSubspace(j));

            for (int i = 0; i < m_NbTarget; i++) {
                if (enabled[i] == 1) {
                    m_ClassCounts[i][vote.getNominalPred()[i]]++;
                }
            }
        }
        calcMean();
    }


    public void voteProbDistr(ArrayList<ClusStatistic> votes) {
        reset();
        int nb_votes = votes.size();
        for (int j = 0; j < nb_votes; j++) {
            ClassificationStat vote = (ClassificationStat) votes.get(j);
            m_SumWeight += vote.m_SumWeight;
            for (int attr = 0; attr < m_NbTarget; attr++) {
                m_SumWeights[attr] += vote.m_SumWeights[attr]; // Warning: here, the sum(m_ClassCounts[attr]) !=
                                                               // m_SumWeights[attr]
                double[] my = m_ClassCounts[attr];
                for (int i = 0; i < my.length; i++) {
                    my[i] += vote.getProportion(attr, i);
                }
            }
        }
        calcMean();
    }


    public void voteProbDistr(ArrayList<ClusStatistic> votes, ClusEnsembleROSInfo targetSubspaceInfo) {
        reset();
        int nb_votes = votes.size();
        for (int j = 0; j < nb_votes; j++) {
            ClassificationStat vote = (ClassificationStat) votes.get(j);
            int[] enabled = targetSubspaceInfo.getOnlyTargets(targetSubspaceInfo.getModelSubspace(j));
            m_SumWeight += vote.m_SumWeight;
            for (int attr = 0; attr < m_NbTarget; attr++) {
                if (enabled[attr] == 1) {
                    m_SumWeights[attr] += vote.m_SumWeights[attr];
                    double[] my = m_ClassCounts[attr];
                    for (int i = 0; i < my.length; i++) {
                        my[i] += vote.getProportion(attr, i);
                    }
                }
            }
        }
        calcMean();
    }


    @Override
    public ClassificationStat getClassificationStat() {
        return this;
    }


    public double[][] getProbabilityPrediction() {
        double[][] result = new double[m_NbTarget][];
        for (int i = 0; i < m_NbTarget; i++) {// for each target
            double total = 0.0;
            for (int k = 0; k < m_ClassCounts[i].length; k++)
                total += m_ClassCounts[i][k]; // get the number of instances

            if (total == 0) //if there are no labeled examples in this node
                result[i] = getEstimatedProbabilityPrediction(i);
            else {
                result[i] = new double[m_ClassCounts[i].length];
                for (int j = 0; j < result[i].length; j++)
                    result[i][j] = m_ClassCounts[i][j] / total;// store the frequencies
            }
        }
        return result;
    }


    /**
     * Probability prediction for i^th target
     * 
     * @param i
     *        index of a target
     * @return
     */
    public double[] getProbabilityPrediction(int i) {
        double[] result = new double[m_ClassCounts[i].length];
        double total = 0.0;

        for (int k = 0; k < m_ClassCounts[i].length; k++)
            total += m_ClassCounts[i][k]; // get the number of instances

        if (total == 0) // if there are no labeled examples in this node
            return getEstimatedProbabilityPrediction(i);

        for (int j = 0; j < result.length; j++)
            result[j] = m_ClassCounts[i][j] / total;// store the frequencies

        return result;
    }


    public double[] getEstimatedProbabilityPrediction(int i) {
        switch (getSettings().getTree().getMissingClusteringAttrHandling()) {
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                return m_Training.getProbabilityPrediction(i);
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                return m_ParentStat.getProbabilityPrediction(i);
            case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                return new double[m_ClassCounts[i].length]; // only zeros
            default:
                return m_Training.getProbabilityPrediction(i);
        }

    }


    @Override
    public double getSquaredDistance(ClusStatistic other) {
        double[][] these = getProbabilityPrediction();
        ClassificationStat o = (ClassificationStat) other;
        double[][] others = o.getProbabilityPrediction();
        double result = 0.0;
        for (int i = 0; i < m_NbTarget; i++) {// for each target
            double distance = 0.0;
            for (int j = 0; j < these[i].length; j++) {// for each class from the target
                distance += (these[i][j] - others[i][j]) * (these[i][j] - others[i][j]);
            }
            result += distance / these[i].length;
        }
        return result / m_NbTarget;
    }


    /*
     * Compute squared distance between each of the tuple's target attributes and this statistic's mean.
     **/
    public double[] getPointwiseSquaredDistance(ClusStatistic other) {
        double[][] these = getProbabilityPrediction();
        ClassificationStat o = (ClassificationStat) other;
        double[][] others = o.getProbabilityPrediction();
        double[] distances = new double[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {//for each target
            double distance = 0.0;
            for (int j = 0; j < these[i].length; j++) {//for each class from the target
                distance += (these[i][j] - others[i][j]) * (these[i][j] - others[i][j]);
            }
            distances[i] = distance / these[i].length;
        }
        return distances;
    }


    public double getSumWeights(int attr) {
        return (m_SumWeights[attr]);
    }


    @Override
    public int getNbStatisticComponents() {
        return m_NbTarget;
    }


    // daniela
    @Override
    public void setData(RowData data) {
        m_data = data;
    }


    @Override
    public void setParentStat(ClusStatistic parent) {
        m_ParentStat = (ClassificationStat) parent;
    }


    @Override
    public double getTargetSumWeights() {
        return m_SumWeightLabeled;
    }


    @Override
    public ClusStatistic getParentStat() {
        return m_ParentStat;
    }


    @Override
    public void setSplitIndex(int i) {
        splitIndex = i;
    }


    public int getSplitIndex() {
        return splitIndex;
    }


    @Override
    public void setPrevIndex(int i) {
        prevIndex = i;
    }


    public int getPrevIndex() {
        return prevIndex;
    }


    @Override
    public void initializeSum() {
        Arrays.fill(previousSumX, 0.0);
        Arrays.fill(previousSumXR, 0.0);
        Arrays.fill(previousSumW, 0.0);
        Arrays.fill(previousSumWXX, 0.0);
        Arrays.fill(previousSumWX, 0.0);
        Arrays.fill(previousSumX2, 0.0);
        Arrays.fill(previousSumWR, 0.0);
        Arrays.fill(previousSumWXXR, 0.0);
        Arrays.fill(previousSumWXR, 0.0);
        Arrays.fill(previousSumX2R, 0.0);
    }


    public RowData getData() {
        return m_data;
    }


    @Override
    public double calcPDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(j);
                    double xj = type.getNominal(exj);
                    long xxi = (long) xt.getNumeric(exi);
                    long yyi = (long) xt.getNumeric(exj);
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            upsum[k] += (xi - means[k]) * (xj - means[k]);
                    } //else upsum[k] += (xi-means[k])*(xi-means[k]); 
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (downsum[k]); //I for each target         
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1; //Double.NEGATIVE_INFINITY;
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+" means: "+means[0]);
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long xxi = (long) xt.getNumeric(exi);
                    long yyi = (long) xt.getNumeric(exj);
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            upsumR[k] += (xi - meansR[k]) * (xj - meansR[k]);
                    } //else upsumR[k] += (xi-meansR[k])*(xi-meansR[k]); 
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }
            if (downsumR[k] != 0.0 && upsumR[k] != 0.0) {
                ikkR[k] = (upsumR[k]) / (downsumR[k]); //I for each target          
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                avgikR = 1; //Double.NEGATIVE_INFINITY;
        }
        avgikR /= schema.getNbTargetAttributes();
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+" means: "+meansR[0]);
        double IR = avgikR; //end right side
        double I = (IL + IR * (N - M)) / m_data.getNbRows();//System.out.println("Join Moran I: "+I);
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcPtotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            upsum[k] += (xi - means[k]) * (xj - means[k]);
                    } //else upsum[k] += (xi-means[k])*(xi-means[k]); 
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (downsum[k]); //I for each target         
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1; //Double.NEGATIVE_INFINITY;
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+" means: "+means[0]);
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            upsumR[k] += (xi - meansR[k]) * (xj - meansR[k]);
                    } //else upsumR[k] += (xi-meansR[k])*(xi-meansR[k]); 
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }
            if (downsumR[k] != 0.0 && upsumR[k] != 0.0) {
                ikkR[k] = (upsumR[k]) / (downsumR[k]); //I for each target          
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                avgikR = 1; //Double.NEGATIVE_INFINITY;
        }
        avgikR /= schema.getNbTargetAttributes();
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+" means: "+meansR[0]);
        double IR = avgikR; //end right side
        double I = (IL + IR * (N - M)) / m_data.getNbRows();//System.out.println("Join Moran I: "+I);
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcMultiIwithNeighbours(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        if (schema.getNbTargetAttributes() == 1) {
            throw new ClusException("Error calculating Bivariate Heuristics with only one target!");
        }
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double W = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;

                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example j     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount)
                            distances[j] = new Distance(permutation[j], tj, d);
                        else {
                            if (permutation[j] < NeighCount)
                                distances[j] = new Distance(permutation[j], tj, d);
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (int a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }

                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    int NN;
                    if ((N - M) < NeighCount)
                        NN = N - M;
                    else
                        NN = (M + NeighCount);

                    for (int j = M; j < NN; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsum[k] += w[permutation[i]][permutation[j]] * (ti - means[k]) * (distances[j].target - means[k]); //m_distances.get(permutation[i]*N+permutation[j]) [permutation[i]][permutation[j]]
                        W += w[permutation[i]][permutation[j]];
                    }
                    downsum[k] += ((ti - means[k]) * (ti - means[k]));
                }
            }
            downsum[k] /= (N - M + 0.0);
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1; //Double.NEGATIVE_INFINITY;

        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Moran Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }

        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double WR = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a = 0;
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example permutation[j]     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount) {
                            distances[a] = new Distance(permutation[j], tj, d);
                            a++;
                        }
                        else {
                            if (a < NeighCount) {
                                distances[a] = new Distance(permutation[j], tj, d);
                                a++;
                            }
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j = 0;
                    while ((distances.length > j) && (distances[j] != null) && j < NeighCount) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsumR[k] += w[permutation[i]][permutation[j]] * (ti - meansR[k]) * (distances[j].target - meansR[k]); //m_distances.get(permutation[i]*N+permutation[j]) [permutation[i]][permutation[j]]
                        WR += w[permutation[i]][permutation[j]];
                        j++;
                    }
                    downsumR[k] += ((ti - meansR[k]) * (ti - meansR[k]));
                }
            }

            downsumR[k] /= (N - M + 0.0);
            if (downsumR[k] != 0.0 && upsumR[k] != 0.0) {
                ikkR[k] = (upsumR[k]) / (WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }
        //System.out.println("Moran Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //end right side

        double I = (IL + IR * (N - M)) / m_data.getNbRows();
        //System.out.println("Join Moran I: "+I);

        double scaledI = 1 + I; //scale I [0,2]
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcBivariateLee(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        if (schema.getNbTargetAttributes() == 1) {
            throw new ClusException("Error calculating Bivariate Heuristics with only one target!");
        }
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[][] upsum = new double[schema.getNbTargetAttributes()][(N - M)];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                means[k] += xxi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0;
        double upsum0 = 0;
        double upsum1 = 1;
        double downsum0 = 0;
        double downsum1 = 0;
        double WL = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xi[k] = type.getNumeric(exi);
            }
            double W = 0.0;
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xj[k] = type.getNumeric(exj);
                }
                double w = 0;
                long indexI = permutation[i];
                long indexJ = permutation[j];
                if (permutation[i] != permutation[j]) {
                    if (permutation[i] > permutation[j]) {
                        indexI = permutation[j];
                        indexJ = permutation[i];
                    }
                    long indexMap = indexI * (NR) + indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    //upsum[0][permutation[i]] += w*xj[0]; upsum[1][permutation[i]] += w*xj[1]; W+=w*w;   
                    upsum[0][permutation[i]] += w * (xj[0] - means[0]);
                    upsum[1][permutation[i]] += w * (xj[1] - means[1]);
                    W += w;
                }
                else {
                    //upsum[0][permutation[i]] += xi[0]; upsum[1][permutation[i]] +=xi[1];W+=1; 
                    upsum[0][permutation[i]] += xi[0] - means[0];
                    upsum[1][permutation[i]] += xi[1] - means[1];
                    W += 1;
                }
            }
            //upsum0+= (upsum[0][permutation[i]]-means[0]); upsum1+= (upsum[1][permutation[i]]-means[1]);
            WL += W * W;
            upsum0 += upsum[0][permutation[i]] * upsum[1][permutation[i]];
            downsum0 += (xi[0] - means[0]) * (xi[0] - means[0]);
            downsum1 += (xi[1] - means[1]) * (xi[1] - means[1]);
        }
        if (upsum0 != 0.0 && upsum1 != 0.0 && downsum0 != 0.0 && downsum1 != 0.0)
            avgik = ((N - M + 0.0) * upsum0 * upsum1) / (WL * Math.sqrt(downsum0 * downsum1));
        else
            avgik = 0;
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" means: "+means[0]);
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double IR = 0;
        double upsumR0 = 0;
        double upsumR1 = 1;
        double downsumR0 = 0;
        double downsumR1 = 0;
        double WRR = 0.0;
        double[][] upsumR = new double[schema.getNbTargetAttributes()][(N - M)];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xiR = new double[schema.getNbTargetAttributes()];
        double[] xjR = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                meansR[k] += xxi;
            }
            meansR[k] /= (N - M);
        }
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xiR[k] = type.getNumeric(exi);
            }
            double WR = 0.0;
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xjR[k] = type.getNumeric(exj);
                }
                double w = 0;
                long indexI = permutation[i];
                long indexJ = permutation[j];
                if (permutation[i] != permutation[j]) {
                    if (permutation[i] > permutation[j]) {
                        indexI = permutation[j];
                        indexJ = permutation[i];
                    }
                    long indexMap = indexI * (NR) + indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsumR[0][(permutation[i] - M)] += w * (xjR[0] - meansR[0]);
                    upsumR[1][(permutation[i] - M)] += w * (xjR[1] - meansR[1]);
                    WR += w;
                }
                else {
                    upsumR[0][(permutation[i] - M)] += xiR[0] - meansR[0];
                    upsumR[1][(permutation[i] - M)] += xiR[1] - meansR[1];
                    WR += 1;
                }
            }
            WRR += WR * WR;
            upsumR0 += upsumR[0][(permutation[i] - M)] * upsumR[1][(permutation[i] - M)];
            downsumR0 += (xiR[0] - meansR[0]) * (xiR[0] - meansR[0]);
            downsumR1 += (xiR[1] - meansR[1]) * (xiR[1] - meansR[1]);
        }
        if (upsumR0 != 0.0 && upsumR1 != 0.0 && downsumR0 != 0.0 && downsumR1 != 0.0)
            IR = ((N - M + 0.0) * upsumR0 * upsumR1) / (WRR * Math.sqrt(downsumR0 * downsumR1));
        else
            IR = 0;
        //System.out.println("Right Moran I: "+IR+"ex: "+(N-M)+"WR "+WR+" means: "+meansR[0]);      

        double I = (IL + IR * (N - M)) / NR;
        //System.out.println("Join Moran I: "+I);
        double scaledI = 1 + I; //scale I [0,2]
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcLeewithNeighbours(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        if (schema.getNbTargetAttributes() == 1) {
            throw new ClusException("Error calculating Bivariate Heuristics with only one target!");
        }
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[][] upsum = new double[schema.getNbTargetAttributes()][(N - M)];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double m = type.getNumeric(exi);
                means[k] += m;
            }
            means[k] /= (N - M);
        }
        double avgik = 0;
        double upsum0 = 0;
        double upsum1 = 1;
        double downsum0 = 0;
        double downsum1 = 0;
        double WL = 0.0;
        int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
        int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
        int NN;
        if ((N - M) < NeighCount)
            NN = N - M;
        else
            NN = (M + NeighCount);
        if (NeighCount > 0) {
            double[][] w = new double[N][N]; //matrica  
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double yyi = 0.0;
                double xxi = 0.0;
                double W = 0;
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k]; //target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord         
                    xi[k] = type.getNumeric(exi);
                    xxi = xt.getNumeric(exi);
                    yyi = yt.getNumeric(exi);
                }
                double biggestd = Double.POSITIVE_INFINITY;
                int biggestindex = Integer.MAX_VALUE;
                DistanceB distances[] = new DistanceB[NeighCount];
                double yyj = 0.0;
                double xxj = 0.0;

                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                        ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];//target
                        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                        ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord     
                        xj[k] = type.getNumeric(exj);
                        xxj = xt.getNumeric(exi);
                        yyj = yt.getNumeric(exi);
                    }
                    double d = Math.sqrt((xxi - xxj) * (xxi - xxj) + (yyi - yyj) * (yyi - yyj));
                    if ((N - M) < NeighCount)
                        distances[j] = new DistanceB(permutation[j], xj[0], xj[1], d);
                    else {
                        if (permutation[j] < NeighCount)
                            distances[j] = new DistanceB(permutation[j], xj[0], xj[1], d);
                        else {
                            biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                            biggestindex = 0;
                            for (int a = 1; a < NeighCount; a++)
                                if (distances[a].distance > biggestd) {
                                    biggestd = distances[a].distance;
                                    biggestindex = a;
                                }
                            if (d < biggestd) {
                                distances[biggestindex].index = permutation[j];
                                distances[biggestindex].distance = d;
                                distances[biggestindex].target1 = xj[0];
                                distances[biggestindex].target2 = xj[1];
                            }
                        }
                    }
                }

                for (int j = M; j < NN; j++) {
                    if (distances[j].distance == 0.0)
                        w[permutation[i]][permutation[j]] = 1;
                    else {
                        switch (spatialMatrix) {
                            case 0:
                                w[permutation[i]][permutation[j]] = 1;
                                break; //binary 
                            case 1:
                                w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                break; //euclidian
                            case 2:
                                w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                break; //modified
                            case 3:
                                w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                break; //gausian
                            default:
                                w[permutation[i]][permutation[j]] = 1;
                                break;
                        }
                    }
                    upsum[0][permutation[i]] += w[permutation[i]][permutation[j]] * (xj[0] - means[0]);
                    upsum[1][permutation[i]] += w[permutation[i]][permutation[j]] * (xj[1] - means[1]);
                    W += w[permutation[i]][permutation[j]];
                }

                WL += W * W;
                upsum0 += upsum[0][permutation[i]] * upsum[1][permutation[i]];
                downsum0 += (xi[0] - means[0]) * (xi[0] - means[0]);
                downsum1 += (xi[1] - means[1]) * (xi[1] - means[1]);
            }
        }
        if (upsum0 != 0.0 && upsum1 != 0.0 && downsum0 != 0.0 && downsum1 != 0.0)
            avgik = ((N - M + 0.0) * upsum0 * upsum1) / (WL * Math.sqrt(downsum0 * downsum1));
        else
            avgik = 0;
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+WL+" means: "+means[0]);
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[][] upsumR = new double[schema.getNbTargetAttributes()][(N - M)];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xRi = new double[schema.getNbTargetAttributes()];
        double[] xRj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double m = type.getNumeric(exi);
                meansR[k] += m;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        double upsumR0 = 0;
        double upsumR1 = 1;
        double downsumR0 = 0;
        double downsumR1 = 0;
        double WRR = 0.0;
        if (NeighCount > 0) {
            double[][] w = new double[N][N]; //matrica  
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double yyi = 0.0;
                double xxi = 0.0;
                double WR = 0;
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k]; //target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord         
                    xRi[k] = type.getNumeric(exi);
                    xxi = xt.getNumeric(exi);
                    yyi = yt.getNumeric(exi);
                }
                double biggestd = Double.POSITIVE_INFINITY;
                int biggestindex = Integer.MAX_VALUE;
                int a = 0;
                DistanceB distances[] = new DistanceB[NeighCount];
                double yyj = 0.0;
                double xxj = 0.0;

                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                        ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];//target
                        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                        ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord     
                        xRj[k] = type.getNumeric(exj);
                        xxj = xt.getNumeric(exi);
                        yyj = yt.getNumeric(exi);
                    }
                    double d = Math.sqrt((xxi - xxj) * (xxi - xxj) + (yyi - yyj) * (yyi - yyj));

                    if ((N - M) < NeighCount) {
                        distances[a] = new DistanceB(permutation[j], xRj[0], xRj[1], d);
                        a++;
                    }
                    else {
                        if (a < NeighCount) {
                            distances[a] = new DistanceB(permutation[j], xRj[0], xRj[1], d);
                            a++;
                        }
                        else {
                            biggestindex = 0;
                            biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                            for (a = 1; a < NeighCount; a++)
                                if (distances[a].distance > biggestd) {
                                    biggestd = distances[a].distance;
                                    biggestindex = a;
                                }
                            if (d < biggestd) {
                                distances[biggestindex].index = permutation[j];
                                distances[biggestindex].distance = d;
                                distances[biggestindex].target1 = xRj[0];
                                distances[biggestindex].target2 = xRj[1];
                            }
                        }
                    }
                }

                int j = 0;
                while ((distances.length > j) && (distances[j] != null) && j < NeighCount) {
                    if (distances[j].distance == 0.0)
                        w[permutation[i]][permutation[j]] = 1;
                    else {
                        switch (spatialMatrix) {
                            case 0:
                                w[permutation[i]][permutation[j]] = 1;
                                break; //binary 
                            case 1:
                                w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                break; //euclidian
                            case 2:
                                w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                break; //modified
                            case 3:
                                w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                break; //gausian
                            default:
                                w[permutation[i]][permutation[j]] = 1;
                                break;
                        }
                    }
                    upsumR[0][permutation[i]] += w[permutation[i]][permutation[j]] * (xRj[0] - meansR[0]);
                    upsumR[1][permutation[i]] += w[permutation[i]][permutation[j]] * (xRj[1] - meansR[1]);
                    WR += w[permutation[i]][permutation[j]];
                }
                WRR += WR * WR;
                upsumR0 += upsumR[0][permutation[i]] * upsumR[1][permutation[i]];
                downsumR0 += (xRi[0] - meansR[0]) * (xRi[0] - meansR[0]);
                downsumR1 += (xRi[1] - meansR[1]) * (xRi[1] - meansR[1]);
            }
        }
        if (upsumR0 != 0.0 && upsumR1 != 0.0 && downsumR0 != 0.0 && downsumR1 != 0.0)
            avgikR = ((N - M + 0.0) * upsumR0 * upsumR1) / (WRR * Math.sqrt(downsumR0 * downsumR1));
        else
            avgikR = 0;
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+"WR "+WRR+" means: "+meansR[0]);
        double IR = avgikR * (N - M);
        double I = (IL + IR * (N - M)) / m_data.getNbRows();
        //System.out.println("Join Moran I: "+I);

        double scaledI = 1 + I; //scale I [0,2]
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcMutivariateItotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        if (schema.getNbTargetAttributes() == 1) {
            throw new ClusException("Error calculating Bivariate Heuristics with only one target!");
        }
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double upsum = 0.0;
        double downsumAll = 1.0;
        double ikk = 0.0;
        double W = 0.0;
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                means[k] += xxi;
            }
            means[k] /= (N - M);
        }
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xi[k] = type.getNumeric(exi);
            }
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xj[k] = type.getNumeric(exj);
                }
                double w = 0;
                long indexI = permutation[i];
                long indexJ = permutation[j];
                if (permutation[i] != permutation[j]) {
                    if (permutation[i] > permutation[j]) {
                        indexI = permutation[j];
                        indexJ = permutation[i];
                    }
                    long indexMap = indexI * (NR) + indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsum += w * (xi[0] - means[0]) * (xj[1] - means[1]);
                    W += w;
                }
            }
            for (int k = 0; k < schema.getNbTargetAttributes(); k++)
                downsum[k] += ((xi[k] - means[k]) * (xi[k] - means[k]));
        }
        for (int k = 0; k < schema.getNbTargetAttributes(); k++)
            downsumAll *= downsum[k];
        if (downsumAll > 0.0 && upsum != 0.0) {
            ikk = ((N - M + 0.0) * upsum) / (W * Math.sqrt(downsumAll));
        }
        else
            ikk = 0; //Double.NEGATIVE_INFINITY;
        //System.out.println("Moran Left I:"+ikk+" up "+upsum+" d "+downsumAll+" MN "+(N-M));   //I for each target 
        double IL = ikk * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double upsumR = 0.0;
        double downsumAllR = 1.0;
        double ikkR = 0.0;
        double WR = 0.0;
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xiR = new double[schema.getNbTargetAttributes()];
        double[] xjR = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                meansR[k] += xxi;
            }
            meansR[k] /= (N - M);
        }
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xiR[k] = type.getNumeric(exi);
            }
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xjR[k] = type.getNumeric(exj);
                }
                double w = 0;
                long indexI = permutation[i];
                long indexJ = permutation[j];
                if (permutation[i] != permutation[j]) {
                    if (permutation[i] > permutation[j]) {
                        indexI = permutation[j];
                        indexJ = permutation[i];
                    }
                    long indexMap = indexI * (NR) + indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsumR += w * (xiR[0] - meansR[0]) * (xjR[1] - meansR[1]);
                    WR += w;
                }
            }
            for (int k = 0; k < schema.getNbTargetAttributes(); k++)
                downsumR[k] += ((xiR[k] - meansR[k]) * (xiR[k] - meansR[k]));
        }
        for (int k = 0; k < schema.getNbTargetAttributes(); k++)
            downsumAllR *= downsumR[k];
        if (downsumAllR > 0.0 && upsumR != 0.0) {
            ikkR = ((N - M + 0.0) * upsumR) / (WR * Math.sqrt(downsumAllR));
        }
        else
            ikkR = 0; //Double.NEGATIVE_INFINITY;
        //System.out.println("Moran R I:"+ikkR+" up "+upsumR+" d "+downsumAllR+" MN "+(N-M));   //I for each target 
        double IR = ikkR; //end right side
        double I = (IL + IR * (N - M)) / N;
        //System.out.println("Join Moran I: "+I);
        //if (I>1) I=1;
        //if (I<-1) I=-1;   
        //scale I [0,2]
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    //Connectivity Index (CI) for Graph Data with a distance file
    @Override
    public double calcCItotalD(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double num, den;
        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[] D = new double[(N - M)];
        double[] W = new double[(N - M)];
        double[][] w = new double[(N - M)][(N - M)];
        double[] ikk = new double[m_NbAttrs];
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < m_NbAttrs; k++) {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                long xxi = (long) xt.getNumeric(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    long yyi = (long) xt.getNumeric(exj);
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w[permutation[i]][permutation[j]] = temp;
                        else
                            w[permutation[i]][permutation[j]] = 0;
                        D[permutation[i]] += w[permutation[i]][permutation[j]]; //za sekoj node
                    }
                }
            }
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++)
                    if (permutation[i] != permutation[j] && D[permutation[j]] != 0)
                        W[permutation[i]] += Math.sqrt(D[permutation[j]]);
                if (D[permutation[i]] != 0)
                    ikk[k] += Math.sqrt(D[permutation[i]]);
            }
            avgik += ikk[k]; //average of the all targets
        }
        avgik /= m_NbAttrs;
        //System.out.println("Left Moran I: "+ikk[0]+"ex: "+(N-M));
        double IL = avgik; //*(N-M);  
        if (Double.isNaN(IL))
            IL = 0.0;

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double IR = 0;
        double[] DR = new double[(N - M)];
        double[] WR = new double[(N - M)];
        double[][] wr = new double[(N - M)][(N - M)];
        double[] ikkR = new double[m_NbAttrs];
        for (int k = 0; k < m_NbAttrs; k++) {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                long xxi = (long) xt.getNumeric(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    long yyi = (long) xt.getNumeric(exj);
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            wr[(permutation[i] - M)][(permutation[j] - M)] = temp;
                        else
                            wr[(permutation[i] - M)][(permutation[j] - M)] = 0;
                        DR[(permutation[i] - M)] += wr[(permutation[i] - M)][(permutation[j] - M)]; //za sekoj node
                    }
                }
            }
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++)
                    if (permutation[i] != permutation[j] && DR[(permutation[j] - M)] != 0)
                        WR[(permutation[i] - M)] += wr[(permutation[i] - M)][(permutation[j] - M)] / Math.sqrt(DR[(permutation[j] - M)]);
                if (DR[(permutation[i] - M)] != 0)
                    ikkR[k] += WR[(permutation[i] - M)] / Math.sqrt(DR[(permutation[i] - M)]);
            }
            IR += ikkR[k]; //average of the all targets
        }
        IR /= m_NbAttrs;
        if (Double.isNaN(IR))
            IR = 0.0;
        //System.out.println("Right Moran I: "+IR+"ex: "+(N-M));
        double I = (IL + IR) / m_data.getNbRows();
        //System.out.println("Join Moran I: "+I+" eLeft: "+(NR-(N-M))+" eRight: "+(N-M)+" "+IL+" "+IR);     
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    //Connectivity Index (CI) for Graph Data 
    @Override
    public double calcCItotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[] D = new double[(N - M)];
        double[] W = new double[(N - M)];
        double[][] w = new double[(N - M)][(N - M)];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w[permutation[i]][permutation[j]] = temp;
                        else
                            w[permutation[i]][permutation[j]] = 0;
                        D[permutation[i]] += w[permutation[i]][permutation[j]]; //za sekoj node
                    }
                }
            }
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++)
                    if (permutation[i] != permutation[j] && D[permutation[j]] != 0)
                        W[permutation[i]] += w[permutation[i]][permutation[j]] / Math.sqrt(D[permutation[j]]);
                if (D[permutation[i]] != 0)
                    ikk[k] += W[permutation[i]] / Math.sqrt(D[permutation[i]]);
                ikk[k] += W[permutation[i]] / Math.sqrt(D[permutation[i]]);
            }
            avgik += ikk[k]; //average of the all targets
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+ikk[0]+"ex: "+(N-M));
        double IL = avgik; //*(N-M);  
        if (Double.isNaN(IL))
            IL = 0.0;

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double IR = 0;
        double[] DR = new double[(N - M)];
        double[] WR = new double[(N - M)];
        double[][] wr = new double[(N - M)][(N - M)];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            wr[(permutation[i] - M)][(permutation[j] - M)] = temp;
                        else
                            wr[(permutation[i] - M)][(permutation[j] - M)] = 0;
                        DR[(permutation[i] - M)] += wr[(permutation[i] - M)][(permutation[j] - M)]; //za sekoj node
                    }
                }
            }
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++)
                    if (permutation[i] != permutation[j] && DR[(permutation[j] - M)] != 0)
                        WR[(permutation[i] - M)] += wr[(permutation[i] - M)][(permutation[j] - M)] / Math.sqrt(DR[(permutation[j] - M)]);
                ikkR[k] += WR[(permutation[i] - M)] / Math.sqrt(DR[(permutation[i] - M)]);
            }
            IR += ikkR[k]; //average of the all targets
        }
        IR /= schema.getNbTargetAttributes();
        if (Double.isNaN(IR))
            IR = 0.0;
        //System.out.println("Right Moran I: "+IR+"ex: "+(N-M));
        double I = (IL + IR) / m_data.getNbRows();
        //System.out.println("Join Moran I: "+I+" eLeft: "+(NR-(N-M))+" eRight: "+(N-M)+" "+IL+" "+IR);

        //if (I>1) I=1;
        //if (I<-1) I=-1;   
        //scale I [0,2]
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


   


    //Connectivity Index (CI) for Graph Data with neigh.
    @Override
    public double calcCIwithNeighbours(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] ikk = new double[schema.getNbTargetAttributes()];
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double[] D = new double[(N - M)];
            double[] W = new double[(N - M)];
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                Distance distances[] = new Distance[NeighCount];
                double biggestd = Double.POSITIVE_INFINITY;
                int biggestindex = Integer.MAX_VALUE;
                int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                int NN;
                if ((N - M) < NeighCount)
                    NN = N - M;
                else
                    NN = (M + NeighCount);
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example j     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount)
                            distances[j] = new Distance(permutation[j], tj, d);
                        else {
                            if (permutation[j] < NeighCount)
                                distances[j] = new Distance(permutation[j], tj, d);
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (int a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }

                    for (int j = M; j < NN; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        D[permutation[i]] += w[permutation[i]][permutation[j]]; //za sekoj node
                    }
                }
                for (int i = M; i < N; i++) {
                    //System.out.println(i+" end "+D[i]);
                    for (int j = M; j < NN; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        W[permutation[i]] += w[permutation[i]][permutation[j]] / Math.sqrt(D[permutation[j]]);
                    }
                    ikk[k] += W[permutation[i]] / Math.sqrt(D[permutation[i]]);
                }
            }
            avgik += ikk[k];
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left CI:"+degree[0]+" MN "+(N-M));    //I for each target 
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double[] DR = new double[(N - M)];
            double[] WR = new double[(N - M)];
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                Distance distances[] = new Distance[NeighCount];
                double biggestd = Double.POSITIVE_INFINITY;
                int biggestindex = Integer.MAX_VALUE;
                int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();

                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a = 0;
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example j     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount) {
                            distances[a] = new Distance(permutation[j], tj, d);
                            a++;
                        }
                        else {
                            if (a < NeighCount) {
                                distances[a] = new Distance(permutation[j], tj, d);
                                a++;
                            }
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }
                    int j = 0;
                    while (j < NeighCount) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        DR[permutation[i]] += w[permutation[i]][permutation[j]]; //za sekoj node
                    }
                }
                for (int i = M; i < N; i++) {
                    for (int j = M; j < N; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        WR[permutation[i]] += w[permutation[i]][permutation[j]] / Math.sqrt(DR[permutation[j]]);
                    }
                    ikkR[k] += WR[permutation[i]] / Math.sqrt(DR[permutation[i]]);
                }
            }
            avgikR += ikkR[k];
        }
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //System.out.println("Right CI:"+avgikR+" MN "+(N-M));  //I for each target 
        //end right side        
        double I = (IL + IR * (N - M)) / m_data.getNbRows();
        //System.out.println("Join CI: "+I);

        double scaledI = 1 + I; //scale I [0,2]
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    //Geary C wit neigh.
    @Override
    public double calcCwithNeighbourstotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double W = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;

                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example permutation[j]     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount)
                            distances[j] = new Distance(permutation[j], tj, d);
                        else {
                            if (j < NeighCount)
                                distances[j] = new Distance(permutation[j], tj, d);
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (int a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    int NN;
                    if ((N - M) < NeighCount)
                        NN = N - M;
                    else
                        NN = (M + NeighCount);

                    for (int j = M; j < NN; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsum[k] += w[permutation[i]][permutation[j]] * (ti - distances[j].target) * (ti - distances[j].target);
                        W += w[permutation[i]][permutation[j]];
                    }
                    downsum[k] += ((ti - means[k]) * (ti - means[k]));
                }
            }
            downsum[k] /= (N - M - 1 + 0.0);
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (2 * W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1; //Double.NEGATIVE_INFINITY;

        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Geary Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }

        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double WR = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a = 0;
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example permutation[j]     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount) {
                            distances[a] = new Distance(permutation[j], tj, d);
                            a++;
                        }
                        else {
                            if (a < NeighCount) {
                                distances[a] = new Distance(permutation[j], tj, d);
                                a++;
                            }
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j = 0;
                    while ((distances.length > j) && (distances[j] != null) && j < NeighCount) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsumR[k] += w[permutation[i]][permutation[j]] * (ti - distances[j].target) * (ti - distances[j].target);
                        WR += w[permutation[i]][permutation[j]];
                        j++;
                    }
                    downsumR[k] += ((ti - meansR[k]) * (ti - meansR[k]));
                }
            }

            downsumR[k] /= (N - M - 1 + 0.0);
            if (downsumR[k] != 0.0 && upsumR[k] != 0.0) {
                ikkR[k] = (upsumR[k]) / (2 * WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }
        //System.out.println("Geary Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;

        double I = 2 - ((IL + IR * (N - M)) / m_data.getNbRows());//scaledG=2-G [0,2] 
        //System.out.println("Join Geary G: "+I);       
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;
    }


    //Moran Global I with neigh.
    @Override
    public double calcIwithNeighbourstotal(Integer[] permutation) throws ClusException {
        //calcLeewithNeighbours
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double W = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;

                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example permutation[j]     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount)
                            distances[j] = new Distance(permutation[j], tj, d);
                        else {
                            if (permutation[j] < NeighCount)
                                distances[j] = new Distance(permutation[j], tj, d);
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (int a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                        //Privatedistances[j] = distances[j];
                    }
                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    int NN;
                    if ((N - M) < NeighCount)
                        NN = N - M;
                    else
                        NN = (M + NeighCount);

                    for (int j = M; j < NN; j++) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsum[k] += w[permutation[i]][permutation[j]] * (ti - means[k]) * (distances[j].target - means[k]); //m_distances.get(permutation[i]*N+permutation[j]) [permutation[i]][permutation[j]]
                        W += w[permutation[i]][permutation[j]];
                    }
                    downsum[k] += ((ti - means[k]) * (ti - means[k]));
                }
            }
            downsum[k] /= (N - M + 0.0);
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1; //Double.NEGATIVE_INFINITY;

        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Moran Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }

        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int) schema.getSettings().getTree().getNumNeightbours();
            double WR = 0.0;
            if (NeighCount > 0) {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[] = new Distance[NeighCount];
                    DataTuple exi = m_data.getTuple(permutation[i]); //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a = 0;
                    double biggestd = Double.POSITIVE_INFINITY;
                    int biggestindex = Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]); //example permutation[j]     
                        double tj = type.getNumeric(exj);
                        double xj = xt.getNumeric(exj);
                        double yj = yt.getNumeric(exj);
                        double d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                        if ((N - M) < NeighCount) {
                            distances[a] = new Distance(permutation[j], tj, d);
                            a++;
                        }
                        else {
                            if (a < NeighCount) {
                                distances[a] = new Distance(permutation[j], tj, d);
                                a++;
                            }
                            else {
                                biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                                biggestindex = 0;
                                for (a = 1; a < NeighCount; a++)
                                    if (distances[a].distance > biggestd) {
                                        biggestd = distances[a].distance;
                                        biggestindex = a;
                                    }
                                if (d < biggestd) {
                                    distances[biggestindex].index = permutation[j];
                                    distances[biggestindex].target = tj;
                                    distances[biggestindex].distance = d;
                                }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j = 0;
                    while ((distances.length > j) && (distances[j] != null) && j < NeighCount) {
                        if (distances[j].distance == 0.0)
                            w[permutation[i]][permutation[j]] = 1;
                        else {
                            switch (spatialMatrix) {
                                case 0:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break; //binary 
                                case 1:
                                    w[permutation[i]][permutation[j]] = 1 / distances[j].distance;
                                    break; //euclidian
                                case 2:
                                    w[permutation[i]][permutation[j]] = (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount)) * (1 - (distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //modified
                                case 3:
                                    w[permutation[i]][permutation[j]] = Math.exp(-(distances[j].distance * distances[j].distance) / (NeighCount * NeighCount));
                                    break; //gausian
                                default:
                                    w[permutation[i]][permutation[j]] = 1;
                                    break;
                            }
                        }
                        upsumR[k] += w[permutation[i]][permutation[j]] * (ti - meansR[k]) * (distances[j].target - meansR[k]); //m_distances.get(permutation[i]*N+permutation[j]) [permutation[i]][permutation[j]]
                        WR += w[permutation[i]][permutation[j]];
                        j++;
                    }
                    downsumR[k] += ((ti - meansR[k]) * (ti - meansR[k]));
                }
            }

            downsumR[k] /= (N - M + 0.0);
            if (downsumR[k] != 0.0 && upsumR[k] != 0.0) {
                ikkR[k] = (upsumR[k]) / (WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }
        //System.out.println("Moran Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //end right side

        double I = (IL + IR * (N - M)) / m_data.getNbRows();
        //System.out.println("Join Moran I: "+I);

        double scaledI = 1 + I; //scale I [0,2]
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    //calculate EquvalentI with neighbours
    @Override
    public double calcEquvalentIwithNeighbourstotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double[] a = new double[schema.getNbTargetAttributes()];
        double[] b = new double[schema.getNbTargetAttributes()];
        double[] c = new double[schema.getNbTargetAttributes()];
        double[] d = new double[schema.getNbTargetAttributes()];
        double[] e = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        double avgik = 0;
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        int vkupenBrojElementiVoOvojSplit = 0;
        int vkupenBrojElementiVoCelataSuma = 0;
        if (splitIndex > 0) {
            N = splitIndex;
            M = prevIndex;
            vkupenBrojElementiVoCelataSuma = NR;
            vkupenBrojElementiVoOvojSplit = N - M;
        }
        else {
            M = -splitIndex;
            vkupenBrojElementiVoOvojSplit = N - M;
        }
        /*
         * for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
         * a[k]=previousSumW[k];
         * b[k]=previousSumWXX[k];
         * c[k]=previousSumWX[k];
         * d[k]=previousSumX[k];
         * e[k]=previousSumX2[k];
         * int NeighCount = (int)schema.getSettings().getNumNeightbours(); //30;
         * double[][] w = new double[N][N]; //matrica
         * double W = 0.0;
         * if (NeighCount>0)
         * {
         * for (int i = M; i < N; i++) {
         * Distance distances[]=new Distance[NeighCount];
         * ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
         * DataTuple exi = m_data.getTuple(permutation[i]);
         * double ti = type.getNumeric(exi);
         * ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; //x coord
         * ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
         * double xi = xt.getNumeric(exi);
         * double yi = yt.getNumeric(exi);
         * double biggestd=Double.POSITIVE_INFINITY;
         * int biggestindex=Integer.MAX_VALUE;
         * d[k] += xi;
         * e[k] += xi*xi;
         * for (int j = M; j <N; j++) {
         * DataTuple exj = m_data.getTuple(j);
         * double tj = type.getNumeric(exj);
         * double xj = xt.getNumeric(exj);
         * double yj = yt.getNumeric(exj);
         * double tempd = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));
         * if (vkupenBrojElementiVoOvojSplit<NeighCount) distances[j]=new Distance(j,tj,tempd);
         * else {
         * if (j<NeighCount) distances[j]=new Distance(j,tj,tempd);
         * else{
         * biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
         * biggestindex = 0;
         * for (int aa=1; aa<NeighCount; aa++)
         * if (distances[aa].distance > biggestd)
         * {biggestd = distances[aa].distance;
         * biggestindex = aa;
         * }
         * if (tempd < biggestd)
         * {
         * distances[biggestindex].index = j;
         * distances[biggestindex].target = tj;
         * distances[biggestindex].distance = tempd;
         * }
         * }
         * //System.out.println("index,dist: "+j+", "+d);
         * }
         * }
         * int spatialMatrix = schema.getSettings().getSpatialMatrix();
         * for (int j = M; j <N; j++) {
         * DataTuple exj = m_data.getTuple(j);
         * double tj = type.getNumeric(exj);
         * if (i==j) w[i][j]=0;
         * else if ((distances[j].distance==0.0) && (i!=j)) w[i][j]=1;
         * else{
         * switch (spatialMatrix) {
         * case 0: w[i][j]=0; break; //binary
         * case 1: w[i][j]=1/distances[j].distance; break; //euclidian
         * default: w[i][j]=0; break;
         * }
         * W+=w[i][j];
         * }
         * a[k] += 2*w[i][j];
         * b[k] += 2*w[i][j]*ti*tj;
         * c[k] += w[i][j]*tj;
         * c[k] += w[i][j]*tj;
         * }
         * previousSumW[k] =a[k];
         * previousSumWXX[k]=b[k];
         * previousSumWX[k] =c[k];
         * previousSumX[k] =d[k];
         * previousSumX2[k] =e[k];
         * }
         * }
         * if (a[k]!=0.0 && e[k]!=(d[k]/(vkupenBrojElementiVoCelataSuma))){
         * ikk[k]=(vkupenBrojElementiVoCelataSuma)*(b[k] - (2*d[k]*c[k]/(vkupenBrojElementiVoCelataSuma)) +
         * (d[k]*d[k]*a[k]/((vkupenBrojElementiVoCelataSuma)*(vkupenBrojElementiVoCelataSuma))))/(a[k]*(e[k]-(d[k]/(
         * vkupenBrojElementiVoCelataSuma)))); //I for each target
         * avgik+=ikk[k]; //average of the both targets
         * }else avgik = 1; //Double.NEGATIVE_INFINITY;
         * }
         */
        avgik /= schema.getNbTargetAttributes();
        double I = avgik;
        if (I > 1)
            I = 1;
        if (I < -1)
            I = -1;
        //scale I [0,2]
        double scaledI = 1 - I;
        //System.out.println("scaledI: "+scaledI+"I: "+I);
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }
    //calculate EquvalentI with Distance file


    //calculate Equvalent Geary C with Distance file
    @Override
    public double calcEquvalentGDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double avgik = 0;
        double avgikR = 0;
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            M = prevIndex;
            N = splitIndex;
            if (INITIALIZEPARTIALSUM) {
                INITIALIZEPARTIALSUM = false;
                M = 0;
                for (int i = M; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    meansR[k] += xi;
                    previousSumX2R[k] += xi * xi;
                    previousSumXR[k] += xi;
                    for (int j = M; j < NR; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumWR[k] += w;
                            previousSumWXXR[k] += w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumWR[k] += 1;
                            previousSumWXXR[k] += (xi - xj) * (xi - xj);
                        }
                    }
                }
            }
            //else
            boolean flagRightAllEqual = true;
            boolean flagLeftAllEqual = true;
            {
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    means[k] += xi;
                    previousSumX2[k] += xi * xi;
                    previousSumX[k] += xi;
                    meansR[k] -= xi;
                    previousSumX2R[k] -= xi * xi;
                    previousSumXR[k] -= xi;
                }

                //left (0-old)(old-new)
                flagLeftAllEqual = true;
                double oldX = type.getNominal(m_data.getTuple(0));
                for (int i = 1; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (xi != oldX) // to check that partition does not contain values which are all the same
                    {
                        flagLeftAllEqual = false;
                        break;
                    }
                }

                for (int i = 0; i < M; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumW[k] += w;
                            previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumW[k] += 1;
                            previousSumWXX[k] += (xi - xj) * (xi - xj);
                        }
                    }
                }
                //left (old-new)(0-old)
                for (int i = M; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumW[k] += w;
                            previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumW[k] += 1;
                            previousSumWXX[k] += (xi - xj) * (xi - xj);
                        }
                    }
                }
                //left (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumW[k] += w;
                            previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumW[k] += 1;
                            previousSumWXX[k] += (xi - xj) * (xi - xj);
                        }
                    }
                }

                //right side (new-end)(old-new) 
                flagRightAllEqual = true;
                oldX = type.getNominal(m_data.getTuple(N));
                for (int i = N; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (oldX != xi)
                        flagRightAllEqual = false;
                    for (int j = M; j < N; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        if (xi != oldX)
                            flagRightAllEqual = false;

                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumWR[k] -= w;
                            previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumWR[k] -= 1;
                            previousSumWXXR[k] -= (xi - xj) * (xi - xj);
                        }
                    }
                }

                //right (old-new)(new-end)
                for (int i = M; i < N; i++) {
                    for (int j = N; j < NR; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumWR[k] -= w;
                            previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumWR[k] -= 1;
                            previousSumWXXR[k] -= (xi - xj) * (xi - xj);
                        }
                    }
                }
                //right (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI != indexJ) {
                            if (indexI > indexJ) {
                                indexI = yyi;
                                indexJ = xxi;
                            }
                            String indexMap = indexI + "#" + indexJ;
                            Double temp = GISHeuristic.m_distancesS.get(indexMap);
                            if (temp != null)
                                w = temp;
                            else
                                w = 0;
                            previousSumWR[k] -= w;
                            previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                        }
                        else {
                            previousSumWR[k] -= 1;
                            previousSumWXXR[k] -= (xi - xj) * (xi - xj);
                        }
                    }
                }
            }

            //System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);

            vkupenBrojElementiVoOvojSplit = N;
            num[k] = ((vkupenBrojElementiVoOvojSplit - 1) * previousSumWXX[k]);
            den[k] = (2 * previousSumW[k] * (previousSumX2[k] - vkupenBrojElementiVoOvojSplit * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit)));
            if (den[k] != 0 && num[k] != 0 && !flagLeftAllEqual)
                ikk[k] = num[k] / den[k]; //I for each target
            else
                ikk[k] = 1;

            vkupenBrojElementiVoOvojSplit = NR - N;
            numR[k] = ((vkupenBrojElementiVoOvojSplit - 1) * previousSumWXXR[k]);
            denR[k] = (2 * previousSumWR[k] * (previousSumX2R[k] - (((previousSumXR[k] * previousSumXR[k])) / vkupenBrojElementiVoOvojSplit)));
            if (denR[k] != 0 && numR[k] != 0 && !flagRightAllEqual)
                ikkR[k] = numR[k] / denR[k]; //I for each target
            else
                ikkR[k] = 1;

            avgikR += ikkR[k];
            avgik += ikk[k];
            //System.out.println("Left Geary C: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(splitIndex)+" W: "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
            //System.out.println("Right Geary C: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-splitIndex)+" WR "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);

        } //targets
        avgik /= schema.getNbTargetAttributes();
        avgikR /= schema.getNbTargetAttributes();

        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 2 - I;
        if (Double.isNaN(scaledI)) {
            throw new ClusException("err!");
        }
        //System.out.println(scaledI);
        return scaledI;
    }


    @Override
    public double calcEquvalentIDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double avgik = 0;
        double avgikR = 0;
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            M = prevIndex;
            N = splitIndex;
            if (INITIALIZEPARTIALSUM) { //Annalisa: to check that you need to inizialize the partial sums
                INITIALIZEPARTIALSUM = false;
                M = 0;
                for (int i = M; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    meansR[k] += xi;
                    previousSumX2R[k] += xi * xi;
                    previousSumXR[k] += xi;
                    for (int j = M; j < NR; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] += w;
                        previousSumWXXR[k] += w * xi * xj;
                        previousSumWXR[k] += w * xj;
                        //System.out.println(previousSumWR[0]+" "+w+" "+xi);
                    }
                }
                //System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
                //System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
            }
            //else
            boolean flagRightAllEqual = true;
            boolean flagLeftAllEqual = true;
            {
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    means[k] += xi;
                    previousSumX2[k] += xi * xi;
                    previousSumX[k] += xi;
                    meansR[k] -= xi;
                    previousSumX2R[k] -= xi * xi;
                    previousSumXR[k] -= xi;
                }

                //left (0-old)(old-new)
                flagLeftAllEqual = true;
                double oldX = type.getNumeric(m_data.getTuple(0));
                for (int i = 1; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (xi != oldX) // Annalisa : to check that partition does not contain values which are all the same
                    {
                        flagLeftAllEqual = false;
                        break;
                    }
                }

                for (int i = 0; i < M; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWXX[k] += w * xi * xj;
                        previousSumWX[k] += w * xj;
                    }
                }
                //left (old-new)(0-old)
                for (int i = M; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWX[k] += w * xj;
                        previousSumWXX[k] += w * xi * xj;
                    }
                }
                //left (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWX[k] += w * xj;
                        previousSumWXX[k] += w * xi * xj;
                    }
                }

                //right side (new-end)(old-new) 
                flagRightAllEqual = true;
                oldX = type.getNumeric(m_data.getTuple(N));
                for (int i = N; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (oldX != xi)
                        flagRightAllEqual = false;
                    for (int j = M; j < N; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        if (xi != oldX)
                            flagRightAllEqual = false;

                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
                //right (old-new)(new-end)
                for (int i = M; i < N; i++) {
                    for (int j = N; j < NR; j++) {
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        long xxi = (long) xt.getNumeric(exi);
                        long yyi = (long) xt.getNumeric(exj);
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
                //right (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    long xxi = (long) xt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long yyi = (long) xt.getNumeric(exj);
                        double w = 0;
                        long indexI = xxi;
                        long indexJ = yyi;
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
            }

            //System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);

            vkupenBrojElementiVoOvojSplit = N;
            num[k] = vkupenBrojElementiVoOvojSplit * (previousSumWXX[k] - 2 * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * previousSumWX[k] + (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * previousSumW[k]);
            den[k] = previousSumW[k] * (previousSumX2[k] - vkupenBrojElementiVoOvojSplit * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit));
            if (den[k] != 0 && num[k] != 0 && !flagLeftAllEqual)
                ikk[k] = num[k] / den[k]; //I for each target
            else
                ikk[k] = 1;

            vkupenBrojElementiVoOvojSplit = NR - N;
            numR[k] = (vkupenBrojElementiVoOvojSplit) * (previousSumWXXR[k] - (2 * previousSumWXR[k] * (previousSumXR[k] / vkupenBrojElementiVoOvojSplit)) + (((previousSumXR[k] / vkupenBrojElementiVoOvojSplit) * (previousSumXR[k] / vkupenBrojElementiVoOvojSplit)) * previousSumWR[k]));
            denR[k] = (previousSumWR[k] * (previousSumX2R[k] - (((previousSumXR[k] * previousSumXR[k])) / vkupenBrojElementiVoOvojSplit)));
            if (denR[k] != 0 && numR[k] != 0 && !flagRightAllEqual)
                ikkR[k] = numR[k] / denR[k]; //I for each target
            else
                ikkR[k] = 1;

            avgikR += ikkR[k];
            avgik += ikk[k];
            //System.out.println("Left Moran I: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(splitIndex)+" W: "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
            //System.out.println("Right Moran I: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-splitIndex)+" WR "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);

        } //targets
        avgik /= schema.getNbTargetAttributes();
        avgikR /= schema.getNbTargetAttributes();

        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        if (Double.isNaN(scaledI)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }
    //calculate EquvalentI to Global Moran I -Annalisa


    @Override
    public double calcEquvalentGtotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double avgik = 0;
        double avgikR = 0;
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            M = prevIndex;
            N = splitIndex;
            if (INITIALIZEPARTIALSUM) { //Annalisa: to check that you need to inizialize the partial sums
                INITIALIZEPARTIALSUM = false;
                M = 0;
                for (int i = M; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    meansR[k] += xi;
                    previousSumX2R[k] += xi * xi;
                    previousSumXR[k] += xi;
                    for (int j = M; j < NR; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] += w;
                        previousSumWXXR[k] += w * (xi - xj) * (xi - xj);
                    }
                }
                //System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
                //System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
            }
            //else
            boolean flagRightAllEqual = true;
            boolean flagLeftAllEqual = true;
            {
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    means[k] += xi;
                    previousSumX2[k] += xi * xi;
                    previousSumX[k] += xi;
                    meansR[k] -= xi;
                    previousSumX2R[k] -= xi * xi;
                    previousSumXR[k] -= xi;
                }

                //left (0-old)(old-new)
                flagLeftAllEqual = true;
                double oldX = type.getNumeric(m_data.getTuple(0));
                for (int i = 1; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (xi != oldX) // Annalisa : to check that partition does not contain values which are all the same
                    {
                        flagLeftAllEqual = false;
                        break;
                    }
                }

                for (int i = 0; i < M; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);

                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                    }
                }
                //left (old-new)(0-old)
                for (int i = M; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                    }
                }
                //left (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long indexI = permutation[i];
                        long indexJ = permutation[j];
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWXX[k] += w * (xi - xj) * (xi - xj);
                    }
                }

                //right side (new-end)(old-new) 
                flagRightAllEqual = true;
                oldX = type.getNumeric(m_data.getTuple(N));
                for (int i = N; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (oldX != xi)
                        flagRightAllEqual = false;
                    for (int j = M; j < N; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);

                        if (xi != oldX)
                            flagRightAllEqual = false;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                    }
                }
                //right (old-new)(new-end)
                for (int i = M; i < N; i++) {
                    for (int j = N; j < NR; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                    }
                }
                //right (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long indexI = permutation[i];
                        long indexJ = permutation[j];
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * (xi - xj) * (xi - xj);
                    }
                }
            }
            //System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);

            vkupenBrojElementiVoOvojSplit = N;
            num[k] = ((vkupenBrojElementiVoOvojSplit - 1) * previousSumWXX[k]);
            den[k] = (2 * previousSumW[k] * (previousSumX2[k] - vkupenBrojElementiVoOvojSplit * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit)));
            if (den[k] != 0 && num[k] != 0 && !flagLeftAllEqual)
                ikk[k] = num[k] / den[k]; //I for each target
            else
                ikk[k] = 1;

            vkupenBrojElementiVoOvojSplit = NR - N;
            numR[k] = ((vkupenBrojElementiVoOvojSplit - 1) * previousSumWXXR[k]);
            denR[k] = (2 * previousSumWR[k] * (previousSumX2R[k] - (((previousSumXR[k] * previousSumXR[k])) / vkupenBrojElementiVoOvojSplit)));
            if (denR[k] != 0 && numR[k] != 0 && !flagRightAllEqual)
                ikkR[k] = numR[k] / denR[k]; //I for each target
            else
                ikkR[k] = 1;

            avgikR += ikkR[k];
            avgik += ikk[k];
            //System.out.println("Left Geary C: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(splitIndex)+" W: "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
            //System.out.println("Right Geary C: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-splitIndex)+" WR "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);

        } //targets
        avgik /= schema.getNbTargetAttributes();
        avgikR /= schema.getNbTargetAttributes();

        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 2 - I;
        if (Double.isNaN(scaledI)) {
            throw new ClusException("err!");
        }
        //System.out.println(scaledI);
        return scaledI;
    }


    @Override
    public double calcEquvalentItotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double avgik = 0;
        double avgikR = 0;
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            M = prevIndex;
            N = splitIndex;
            if (INITIALIZEPARTIALSUM) { //Annalisa: to check that you need to inizialize the partial sums
                INITIALIZEPARTIALSUM = false;
                M = 0;
                for (int i = M; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    meansR[k] += xi;
                    previousSumX2R[k] += xi * xi;
                    previousSumXR[k] += xi;
                    for (int j = M; j < NR; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] += w;
                        previousSumWXXR[k] += w * xi * xj;
                        previousSumWXR[k] += w * xj;
                        //System.out.println(previousSumWR[0]+" "+w+" "+xi);
                    }
                }
                //System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
                //System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
            }
            //else
            boolean flagRightAllEqual = true;
            boolean flagLeftAllEqual = true;
            {
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    means[k] += xi;
                    previousSumX2[k] += xi * xi;
                    previousSumX[k] += xi;
                    meansR[k] -= xi;
                    previousSumX2R[k] -= xi * xi;
                    previousSumXR[k] -= xi;
                }

                //left (0-old)(old-new)
                flagLeftAllEqual = true;
                double oldX = type.getNumeric(m_data.getTuple(0));
                for (int i = 1; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (xi != oldX) // Annalisa : to check that partition does not contain values which are all the same
                    {
                        flagLeftAllEqual = false;
                        break;
                    }
                }

                for (int i = 0; i < M; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);

                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWXX[k] += w * xi * xj;
                        previousSumWX[k] += w * xj;
                    }
                }
                //left (old-new)(0-old)
                for (int i = M; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWX[k] += w * xj;
                        previousSumWXX[k] += w * xi * xj;
                    }
                }
                //left (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long indexI = permutation[i];
                        long indexJ = permutation[j];
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[k] += w;
                        previousSumWX[k] += w * xj;
                        previousSumWXX[k] += w * xi * xj;
                    }
                }

                //right side (new-end)(old-new) 
                flagRightAllEqual = true;
                oldX = type.getNumeric(m_data.getTuple(N));
                for (int i = N; i < NR; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    if (oldX != xi)
                        flagRightAllEqual = false;
                    for (int j = M; j < N; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);

                        if (xi != oldX)
                            flagRightAllEqual = false;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
                //right (old-new)(new-end)
                for (int i = M; i < N; i++) {
                    for (int j = N; j < NR; j++) {
                        long indexMap = permutation[i] * (NR) + permutation[j];
                        double w = 0;
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        DataTuple exi = m_data.getTuple(permutation[i]);
                        double xi = type.getNominal(exi);
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
                //right (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    double xi = type.getNominal(exi);
                    for (int j = M; j < N; j++) {
                        DataTuple exj = m_data.getTuple(permutation[j]);
                        double xj = type.getNominal(exj);
                        double w = 0;
                        long indexI = permutation[i];
                        long indexJ = permutation[j];
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        if (permutation[i] > permutation[j])
                            indexMap = permutation[j] * (NR) + permutation[i];
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[k] -= w;
                        previousSumWXXR[k] -= w * xi * xj;
                        previousSumWXR[k] -= w * xj;
                    }
                }
            }

            //System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);

            vkupenBrojElementiVoOvojSplit = N;
            num[k] = vkupenBrojElementiVoOvojSplit * (previousSumWXX[k] - 2 * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * previousSumWX[k] + (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * previousSumW[k]);
            den[k] = previousSumW[k] * (previousSumX2[k] - vkupenBrojElementiVoOvojSplit * (previousSumX[k] / vkupenBrojElementiVoOvojSplit) * (previousSumX[k] / vkupenBrojElementiVoOvojSplit));
            if (den[k] != 0 && num[k] != 0 && !flagLeftAllEqual)
                ikk[k] = num[k] / den[k]; //I for each target
            else
                ikk[k] = 1;

            vkupenBrojElementiVoOvojSplit = NR - N;
            numR[k] = (vkupenBrojElementiVoOvojSplit) * (previousSumWXXR[k] - (2 * previousSumWXR[k] * (previousSumXR[k] / vkupenBrojElementiVoOvojSplit)) + (((previousSumXR[k] / vkupenBrojElementiVoOvojSplit) * (previousSumXR[k] / vkupenBrojElementiVoOvojSplit)) * previousSumWR[k]));
            denR[k] = (previousSumWR[k] * (previousSumX2R[k] - (((previousSumXR[k] * previousSumXR[k])) / vkupenBrojElementiVoOvojSplit)));
            if (denR[k] != 0 && numR[k] != 0 && !flagRightAllEqual)
                ikkR[k] = numR[k] / denR[k]; //I for each target
            else
                ikkR[k] = 1;

            avgikR += ikkR[k];
            avgik += ikk[k];
            //System.out.println("Left Moran I: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(splitIndex)+" W: "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
            //System.out.println("Right Moran I: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-splitIndex)+" WR "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);

        } //targets
        avgik /= schema.getNbTargetAttributes();
        avgikR /= schema.getNbTargetAttributes();

        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        //System.out.println(scaledI);
        if (Double.isNaN(scaledI)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    @Override
    public double calcGtotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0;
        double W = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsum[k] += w * (xi - xj) * (xi - xj);
                        W += w;

                    }
                    else {
                        W += 1;
                        upsum[k] += (xi - xj) * (xi - xj);
                    }
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            if (downsum[k] != 0 && upsum[k] != 0) {
                ikk[k] = ((N - M - 1) * upsum[k]) / (2 * W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                ikk[k] = 0;
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Geary C:"+ikk[0]+"examples "+(N-M)+"W: "+W+"up "+upsum[0]+"down: "+downsum[0]);
        double IL = (N - M) * avgik;

        //right side G
        M = splitIndex;
        N = m_data.getNbRows();
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        double WR = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsumR[k] += w * (xi - xj) * (xi - xj);
                        WR += w;
                    }
                    else {
                        WR += 1;
                        upsum[k] += (xi - xj) * (xi - xj);
                    }
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }
            if (downsumR[k] != 0 && upsumR[k] != 0) {
                ikkR[k] = ((N - M - 1) * upsumR[k]) / (2 * WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                ikkR[k] = 0;
        }
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //System.out.println("R Geary C:"+ikkR[0]+"examples "+(N-M)+"WR: "+WR+"up "+upsumR[0]+"downR: "+downsumR[0]);
        //end right side

        //scaledG=2-G [0,2]
        double I = 2 - ((IL + IR * (N - M)) / NR);
        //System.out.println("Join Geary G: "+I);

        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    
    // global I calculation with a separate distance file
    @Override
    public double calcItotalD(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        double num, den;
        double avgik = 0;
        double W = 0.0;
        //temp_data=m_data;
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = m_Attrs[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                long xxi = (long) xt.getNumeric(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long yyi = (long) xt.getNumeric(exj);
                    double w = 0;
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double tmp = GISHeuristic.m_distancesS.get(indexMap);
                        if (tmp != null)
                            w = tmp;
                        else
                            w = 0;
                        upsum[k] += w * (xi - means[k]) * (xj - means[k]);
                        W += w;
                    }
                    else {
                        upsum[k] += (xi - means[k]) * (xi - means[k]);
                        W += 1;
                    }
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            num = ((N - M + 0.0) * upsum[k]);
            den = (W * downsum[k]);
            if (num != 0.0 && den != 0.0) {
                ikk[k] = num / den; //I for each target         
            }
            else
                ikk[k] = 1; //Double.NEGATIVE_INFINITY;
            avgik += ikk[k]; //average of the both targets
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("w: "+W+"means: "+means[0]+"Left Moran I: "+avgik+"ex: "+((N-M)));
        double IL = avgik * (N - M);

        //right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                long xxi = (long) xt.getNumeric(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long yyi = (long) xt.getNumeric(exj);
                    double w = 0;
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }
                        String indexMap = indexI + "#" + indexJ;
                        Double tmp = GISHeuristic.m_distancesS.get(indexMap);
                        if (tmp != null)
                            w = tmp;
                        else
                            w = 0;
                        upsumR[k] += w * (xi - meansR[k]) * (xj - meansR[k]); //m_distances.get(permutation[i]*N+permutation[j]) [permutation[i]][permutation[j]]
                        WR += w;
                    }
                    else {
                        upsumR[k] += (xi - meansR[k]) * (xi - meansR[k]);
                        WR += 1;
                    }
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }

            num = ((N - M + 0.0) * upsumR[k]);
            den = (W * downsumR[k]);
            if (num != 0.0 && den != 0.0) {
                ikkR[k] = num / den; //I for each target            
            }
            else
                ikkR[k] = 1; //Double.NEGATIVE_INFINITY;
            avgikR += ikkR[k]; //average of the both targets
        }
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //System.out.println("w: "+WR+"means: "+meansR[0]+"Right Moran I: "+avgikR+"ex: "+((N-M)));
        double I = (IL + IR * (N - M)) / m_data.getNbRows();
        double scaledI = 1 + I;
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return scaledI;
    }


    // global I
    @Override
    public double calcItotal(Integer[] permutation) throws ClusException { // matejp: all indices of tuples must be replaced by the corresponding permutation[tuple index]
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                int permI = permutation[i];
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permI);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0;
        double W = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                int permI = permutation[i];
                DataTuple exi = m_data.getTuple(permI);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    int permJ = permutation[j];
                    DataTuple exj = m_data.getTuple(permJ);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permI;
                    long indexJ = permJ;
                    if (permI != permJ) {
                        if (permI > permJ) {
                            indexI = permJ;
                            indexJ = permI;
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsum[k] += w * (xi - means[k]) * (xj - means[k]);
                        W += w;

                    }
                    else {
                        W += 1;
                        upsum[k] += (xi - means[k]) * (xj - means[k]);
                    }
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            if (downsum[k] != 0 && upsum[k] != 0) {
                ikk[k] = ((N - M) * upsum[k]) / (W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                ikk[k] = 1;
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Geary C:"+ikk[0]+"examples "+(N-M)+"W: "+W+"up "+upsum[0]+"down: "+downsum[0]);
        double IL = (N - M) * avgik;

        //right side G
        M = splitIndex;
        N = m_data.getNbRows();
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                int permI = permutation[i];
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permI);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        double WR = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                int permI = permutation[i];
                DataTuple exi = m_data.getTuple(permI);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    int permJ = permutation[j];
                    DataTuple exj = m_data.getTuple(permJ);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permI;
                    long indexJ = permJ;
                    if (permI != permJ) {
                        if (permI > permJ) {
                            indexI = permJ;
                            indexJ = permI;
                        }
                        long indexMap = indexI * (NR) + indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsumR[k] += w * (xi - means[k]) * (xj - means[k]);
                        WR += w;
                    }
                    else {
                        WR += 1;
                        upsum[k] += (xi - means[k]) * (xj - means[k]);
                    }
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }
            if (downsumR[k] != 0 && upsumR[k] != 0) {
                ikkR[k] = ((N - M) * upsumR[k]) / (WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                ikkR[k] = 0;
        }
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //System.out.println("R Geary C:"+ikkR[0]+"examples "+(N-M)+"WR: "+WR+"up "+upsumR[0]+"downR: "+downsumR[0]);
        //end right side

        //scaledG=2-G [0,2]
        double I = 1 + ((IL + IR * (N - M)) / NR);
        //System.out.println("Join Geary G: "+I);

        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    // global C calculation with a separate distance file
    @Override
    public double calcGtotalD(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        double avgik = 0;
        double W = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];

            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                long xxi = (long) xt.getNumeric(exi);

                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long yyi = (long) xt.getNumeric(exj);

                    double w = 0;
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }

                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsum[k] += w * (xi - xj) * (xi - xj);
                        W += w;
                    }
                    else {
                        W += 1;
                        upsum[k] += (xi - xj) * (xi - xj);
                    }
                }
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            if (downsum[k] != 0 && upsum[k] != 0) {
                ikk[k] = ((N - M - 1) * upsum[k]) / (2 * W * downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                ikk[k] = 0;
        }
        avgik /= schema.getNbTargetAttributes();
        //System.out.println("Left Geary C:"+avgik+"examples "+(N-M));  
        double IL = (N - M) * avgik;

        //right side G
        M = splitIndex;
        N = m_data.getNbRows();
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                meansR[k] += xi;
            }
            meansR[k] /= (N - M);
        }
        double avgikR = 0;
        double WR = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];

            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                long xxi = (long) xt.getNumeric(exi);

                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    long yyi = (long) xt.getNumeric(exj);

                    double w = 0;
                    long indexI = xxi;
                    long indexJ = yyi;
                    if (indexI != indexJ) {
                        if (indexI > indexJ) {
                            indexI = yyi;
                            indexJ = xxi;
                        }

                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        upsumR[k] += w * (xi - xj) * (xi - xj);
                        WR += w;
                    }
                    else {
                        WR += 1;
                        upsumR[k] += w * (xi - xj) * (xi - xj);
                    }
                }
                downsumR[k] += ((xi - meansR[k]) * (xi - meansR[k]));
            }
            if (downsumR[k] != 0 && upsumR[k] != 0) {
                ikkR[k] = ((N - M - 1) * upsumR[k]) / (2 * WR * downsumR[k]); //I for each target
                avgikR += ikkR[k]; //average of the both targets
            }
            else
                ikkR[k] = 0;
        }
        avgikR /= schema.getNbTargetAttributes();
        double IR = avgikR;
        //System.out.println("Right Geary C:"+IR+"exaples: "+(N-M));    
        //end right side

        //scaledG=2-G [0,2]
        double I = 2 - ((IL + IR * (N - M)) / NR);
        //System.out.println("Join Geary G: "+I);

        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    // global Getis
    @Override
    public double calcGetisTotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double avgik = 0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (N - M) + indexJ;
                        if (GISHeuristic.m_distances.get(indexMap) != null) {
                            w = GISHeuristic.m_distances.get(indexMap);
                        }
                        else {
                            w = 0;
                        }
                        upsum[k] += w * xi * xj;
                        downsum[k] += xi * xj;
                    }
                }
            }
            if (downsum[k] != 0.0 && upsum[k] != 0.0) {
                ikk[k] = (upsum[k]) / (downsum[k]); //I for each target
                avgik += ikk[k]; //average of the both targets
            }
            else
                avgik = 1;
        }
        //System.out.println("i:"+avgik);
        avgik /= schema.getNbTargetAttributes();
        double I = avgik;
        //System.out.println("Moran I:"+ikk[0]+" "+means[0]);   //I for each target     
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;
    }


    //local Moran I calculation
    @Override
    public double calcLISAtotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[][] upsum1 = new double[schema.getNbTargetAttributes()][N];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }

        //System.out.println("mean 0:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (N - M) + indexJ;
                        if (GISHeuristic.m_distances.get(indexMap) != null) {
                            w = GISHeuristic.m_distances.get(indexMap);
                        }
                        else {
                            w = 0;
                        }
                        upsum1[k][permutation[i]] += w * (xj - means[k]);
                        //System.out.println(upsum[k][permutation[i]]);  
                    }
                }
            }

            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                upsum[k][permutation[i]] = (xi - means[k]) * upsum1[k][permutation[i]];
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }
            downsum[k] /= (N - M + 0.0);
            for (int i = M; i < N; i++) {
                if (downsum[k] != 0.0 && upsum[k][i] != 0.0) {
                    ik[k][permutation[i]] = (upsum[k][permutation[i]]) / (downsum[k]); //I for each point 
                    ikk[k] += ik[k][permutation[i]]; //average of all points for each targets
                    //System.out.println("LISA0: "+ik[k][i]);   
                }
                else
                    ikk[k] = 0;
            }
            avgik += ikk[k];
        }
        avgik /= schema.getNbTargetAttributes(); //average of all targets
        double I = avgik;

        // System.out.println("Moran I:"+ikk[0]+" "+ikk[1]);    //I for each target     
        //System.out.println(W+" I1: "+upsum[0]+" "+downsum[0]+" "+ikk[0]+" "+means[0]);
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;
    }


    //local Geary C
    @Override
    public double calcGLocalTotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0;
        double W = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (N - M) + indexJ;
                        if (GISHeuristic.m_distances.get(indexMap) != null) {
                            w = GISHeuristic.m_distances.get(indexMap);
                        }
                        else {
                            w = 0;
                        }
                        upsum[k][permutation[i]] += w * (xi - xj) * (xi - xj);
                        //System.out.println("Upsum:"+xi+" "+xj+","+upsum[k][permutation[i]]);       
                        W += w;
                    }

                }
                //System.out.println("Geary C:"+k+" "+permutation[i]+","+upsum[k][permutation[i]]);                   
                downsum[k] += ((xi - means[k]) * (xi - means[k]));
            }

            for (int i = M; i < N; i++) {
                if (downsum[k] != 0.0 && upsum[k][permutation[i]] != 0.0) {
                    ik[k][permutation[i]] = ((N - M) * upsum[k][permutation[i]]) / (downsum[k]); //C for each point   
                    //System.out.println("Geary C:"+k+" "+permutation[i]+","+upsum[k][permutation[i]]+"/"+(N-M)/downsum[k]);  
                    ikk[k] += ik[k][permutation[i]]; //average of all points for each targets
                }
                else
                    ikk[k] = 0;
            }
            avgik += ikk[k];
        }
        avgik /= schema.getNbTargetAttributes(); //average of all targets
        double I = avgik;
        //System.out.println("Geary C:"+ikk[0]+","+ikk[1]); 
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    //local Getis
    @Override
    public double calcLocalGetisTotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[][] downsum = new double[schema.getNbTargetAttributes()][N];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0;
        double W = 0.0;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (N - M) + indexJ;
                        if (GISHeuristic.m_distances.get(indexMap) != null) {
                            w = GISHeuristic.m_distances.get(indexMap);
                        }
                        else {
                            w = 0;
                        }
                        upsum[k][permutation[i]] += w * xj;
                        downsum[k][permutation[i]] += xj;
                    }
                }
                if (upsum[k][permutation[i]] != 0.0) {
                    ik[k][permutation[i]] = (upsum[k][permutation[i]]) / (downsum[k][permutation[i]]); //Local Getis for each point    
                    ikk[k] += ik[k][permutation[i]]; //average of all points for each targets
                }
                else
                    ikk[k] = 0;
                avgik += ikk[k];
            }
        }
        avgik /= schema.getNbTargetAttributes(); //average of all targets
        double I = avgik;
        //System.out.println("Local Getis:"+ikk[0]+","+ikk[1]); 
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    //local Getis calculation=standardized z    
    @Override
    public double calcGETIStotal(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        int M = 0;
        int N = m_data.getNbRows();
        int NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }

        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[][] upsum1 = new double[schema.getNbTargetAttributes()][N];
        double[][] downsum1 = new double[schema.getNbTargetAttributes()][N];
        double[][] downsum2 = new double[schema.getNbTargetAttributes()][N];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];

        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                means[k] += xi;
            }
            means[k] /= (N - M);
        }
        //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0;
        double[] W = new double[N];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                double xi = type.getNominal(exi);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    double xj = type.getNominal(exj);
                    double w = 0;
                    long indexI = permutation[i];
                    long indexJ = permutation[j];
                    if (permutation[i] != permutation[j]) {
                        if (permutation[i] > permutation[j]) {
                            indexI = permutation[j];
                            indexJ = permutation[i];
                        }
                        long indexMap = indexI * (N - M) + indexJ;
                        if (GISHeuristic.m_distances.get(indexMap) != null) {
                            w = GISHeuristic.m_distances.get(indexMap);
                        }
                        else {
                            w = 0;
                        }
                        upsum1[k][permutation[i]] += w * xj;
                        downsum1[k][permutation[i]] += w * w;
                        W[permutation[i]] += w;
                    }
                }
                upsum[k][permutation[i]] = upsum1[k][permutation[i]] - means[k] * W[permutation[i]];
                downsum[k] += ((xi - means[k]) * (xi - means[k])); //downsum the same as geary's C
                downsum2[k][permutation[i]] = (N - M) * downsum1[k][permutation[i]] - W[permutation[i]] * W[permutation[i]];
                //System.out.println("downsum2:"+downsum2[k][permutation[i]]);
            }

            for (int i = M; i < N; i++) {
                if (downsum[k] != 0.0 && upsum[k][permutation[i]] != 0.0 && downsum2[k][permutation[i]] != 0.0) {
                    double im = downsum2[k][permutation[i]];
                    //System.out.println(im);
                    ik[k][permutation[i]] = upsum[k][permutation[i]] / (Math.sqrt((downsum[k] / ((N - M) * (N - M - 1))) * downsum2[k][permutation[i]])); //I for each point   
                    ikk[k] += ik[k][permutation[i]]; //average of all points for each targets
                    //System.out.println(k+" : "+downsum1[k][permutation[i]]+" , "+W[permutation[i]]+" , "+downsum2[k][permutation[i]]+" rez: "+ik[k][permutation[i]]+" br: "+(N-M));
                }
                else
                    ikk[k] = 0;
            }
            avgik += ikk[k];
        }
        avgik /= schema.getNbTargetAttributes(); //average of all targets
        double I = avgik;

        //System.out.println("Geary C:"+ikk[0]+","+ikk[1]); 
        if (Double.isNaN(I)) {
            throw new ClusException("err!");
        }
        return I;

    }


    public double getCalcItotal() {
        return m_I;
    }


    public void setCalcItotal(double ii) {
        m_I = ii;
    }
    // daniela end
}

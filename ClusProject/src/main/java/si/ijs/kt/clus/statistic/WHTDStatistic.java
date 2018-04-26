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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassTerm;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.ext.hierarchical.ClassesValue;
import si.ijs.kt.clus.heuristic.GISHeuristic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsGeneral;
import si.ijs.kt.clus.main.settings.section.SettingsHMLC;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;
import si.ijs.kt.clus.util.jeans.util.array.MIntArray;


public class WHTDStatistic extends RegressionStatBinaryNomiss {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    // protected static DistributionFactory m_Fac = DistributionFactory.newInstance();

    protected ClassHierarchy m_Hier;
    protected boolean[] m_DiscrMean;
    protected WHTDStatistic m_Global, m_Validation;
    protected double m_SigLevel;
    protected double m_Threshold = -1.0;
    protected int m_Compatibility;
    // Hackishe Porzion des Codes fuer die pooled AUPRC: all other parts of the code that serve this purpose will be
    // simply denoted by poolAUPRC case
    protected int m_Distance = -1;
    protected double[] m_P;

    // Thresholds used in making predictions in multi-label classification
    private double[] m_Thresholds;

    // daniela matejp: public -> private, commented out unused fields
    private RowData m_data;
    private int splitIndex;
    private int prevIndex;
    private double[] previousSumW;
    private double[] previousSumX;
    private double[] previousSumXR;
    private double[] previousSumWXX;
    private double[] previousSumWX;
    private double[] previousSumX2;
    private double[] previousSumWXXR;
    private double[] previousSumWXR;
    private double[] previousSumWR;
    private double[] previousSumX2R;
    private double[] m_Weights;
    public static boolean INITIALIZEPARTIALSUM = true;
    // daniela end


    /**
     * Constructor for poolAUPRC case
     * 
     * @param hier
     * @param comp
     * @param distance
     */
    public WHTDStatistic(Settings sett, ClassHierarchy hier, int comp, int distance) {
        this(sett, hier, false, comp, distance);
    }


    /**
     * Constructor for poolAUPRC case
     * 
     * @param hier
     * @param onlymean
     * @param comp
     * @param distance
     */
    public WHTDStatistic(Settings sett, ClassHierarchy hier, boolean onlymean, int comp, int distance) {
        // super(hier.getDummyAttrs(), onlymean);
        this(sett, hier, onlymean, comp);

        // m_Compatibility = comp;
        // m_Hier = hier;

        m_Distance = distance;
        m_P = new double[m_Hier.getTotal()];
    }


    public WHTDStatistic(Settings sett, ClassHierarchy hier, int comp) {
        this(sett, hier, false, comp);
    }


    public WHTDStatistic(Settings sett, ClassHierarchy hier, boolean onlymean, int comp) {
        super(sett, hier.getDummyAttrs(), onlymean);

        m_Compatibility = comp;
        m_Hier = hier;

        // daniela
        m_Weights = m_Hier.getWeights();
        previousSumX = new double[1];
        previousSumXR = new double[1];
        previousSumW = new double[1];
        previousSumWXX = new double[1];
        previousSumWX = new double[1];
        previousSumX2 = new double[1];
        previousSumWR = new double[1];
        previousSumWXXR = new double[1];
        previousSumWXR = new double[1];
        previousSumX2R = new double[1];
        // daniela end
    }


    public int getCompatibility() {
        return m_Compatibility;
    }


    @Override
    public void setTrainingStat(ClusStatistic train) {
        m_Training = (WHTDStatistic) train;
    }


    public void setValidationStat(WHTDStatistic valid) {
        m_Validation = valid;
    }


    public void setGlobalStat(WHTDStatistic global) {
        m_Global = global;
    }


    public void setSigLevel(double sig) {
        m_SigLevel = sig;
    }


    public void setThreshold(double threshold) {
        m_Threshold = threshold;
    }


    public double getThreshold() {
        return m_Threshold;
    }


    @Override
    public ClusStatistic cloneStat() {

        if (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) {// poolAUPRC case
            WHTDStatistic res = new WHTDStatistic(this.m_Settings, m_Hier, false, m_Compatibility, m_Distance);
            res.m_Training = m_Training;
            res.m_ParentStat = m_ParentStat;
            return res;
        }
        else {
            WHTDStatistic res = new WHTDStatistic(this.m_Settings, m_Hier, false, m_Compatibility);
            res.m_Training = m_Training;
            res.m_ParentStat = m_ParentStat;
            return res;
        }
    }


    @Override
    public ClusStatistic cloneSimple() {
        WHTDStatistic res = (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) ? new WHTDStatistic(this.m_Settings, m_Hier, true, m_Compatibility, m_Distance) : new WHTDStatistic(this.m_Settings, m_Hier, true, m_Compatibility);
        // poolAUPRC
        // case
        // :
        // normal
        // case
        res.m_Threshold = m_Threshold;
        res.m_Thresholds = m_Thresholds;
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        if (m_Validation != null) {
            res.m_Validation = (WHTDStatistic) m_Validation.cloneSimple();
            res.m_Global = m_Global;
            res.m_SigLevel = m_SigLevel;
        }
        return res;
    }


    public void copyAll(ClusStatistic other) {
        super.copy(other);
        WHTDStatistic my_other = (WHTDStatistic) other;
        m_Global = my_other.m_Global;
        m_Validation = my_other.m_Validation;
        m_SigLevel = my_other.m_SigLevel;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        WHTDStatistic or = (WHTDStatistic) other;
        super.addPrediction(other, weight);
        if (m_Validation != null) {
            m_Validation.addPrediction(or.m_Validation, weight);
        }
    }


    @Override
    public void updateWeighted(DataTuple tuple, double weight) {
        int sidx = m_Hier.getType().getArrayIndex();
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(sidx);

        if (tp.getNbClasses() > 0) { // if nbClasses == 0, example is unlabeled
            m_SumWeight += weight;
            // Add one to the elements in the tuple, zero to the others
            for (int j = 0; j < tp.getNbClasses(); j++) {
                ClassesValue val = tp.getClass(j);
                int idx = val.getIndex();
                // if (Settings.VERBOSE > 10) System.out.println("idx = "+idx+" weight = "+weight);
                m_SumValues[idx] += weight;
                if (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) {// poolAUPRC case
                    m_P[idx] += weight;
                }
            }
        }
    }


    public final ClassHierarchy getHier() {
        return m_Hier;
    }


    public final void setHier(ClassHierarchy hier) throws ClusException {
        if (m_Hier != null && m_Hier.getTotal() != hier.getTotal()) { throw new ClusException("Different number of classes in new hierarchy: " + hier.getTotal() + " <> " + m_Hier.getTotal()); }
        m_Hier = hier;
    }


    public int getNbPredictedClasses() {
        int count = 0;
        for (int i = 0; i < m_DiscrMean.length; i++) {
            if (m_DiscrMean[i]) {
                count++;
            }
        }
        return count;
    }


    public ClassesTuple computeMeanTuple() {
        return m_Hier.getTuple(m_DiscrMean);
    }


    public ClassesTuple computePrintTuple() {
        // Same tuple with intermediate elements indicated as such
        // Useful for printing the tree without the intermediate classes
        ClassesTuple printTuple = m_Hier.getTuple(m_DiscrMean);
        ArrayList<ClassesValue> added = new ArrayList<ClassesValue>();
        boolean[] interms = new boolean[m_Hier.getTotal()];
        printTuple.addIntermediateElems(m_Hier, interms, added);
        return printTuple;
    }


    @Override
    public void computePrediction() {
        ClassesTuple meantuple = m_Hier.getBestTupleMaj(m_Means, m_Threshold);
        m_DiscrMean = meantuple.getVectorBooleanNodeAndAncestors(m_Hier);
        performSignificanceTest();
    }


    @Override
    public void calcMean(double[] means) {
        if (getSettings().getHMLC().useMEstimate() && m_Training != null) {
            // Use m-estimate
            for (int i = 0; i < m_NbAttrs; i++) {
                means[i] = (m_SumValues[i] + m_Training.m_Means[i]) / (m_SumWeight + 1.0);
            }
        }
        else {
            // Use default definition (no m-estimate)
            for (int i = 0; i < m_NbAttrs; i++) {
                means[i] = getMean(i);
            }
        }
    }


    @Override
    public void predictTuple(DataTuple prediction) {
        prediction.setObjectVal(computeMeanTuple(), m_Hier.getType().getArrayIndex());
    }


    @Override
    public double getMean(int i) {
        if (getSettings().getHMLC().useMEstimate() && m_Training != null) {
            // Use m-estimate
            return (m_SumValues[i] + m_Training.m_Means[i]) / (m_SumWeight + 1.0);
        }

        if (m_SumWeight == 0.0) { // if there are no examples with known value for attribute i
            switch (getSettings().getTree().getMissingTargetAttrHandling()) {
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                    return m_Training.getMean(i);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                    return m_ParentStat.getMean(i);
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                    return 0;
                default:
                    return m_Training.getMean(i);
            }
        }

        // Use default definition (no m-estimate)
        return m_SumValues[i] / m_SumWeight;
    }


    @Override
    public void calcMean() {
        super.calcMean();
        computePrediction();
    }


    public int round(double value) {
        if (getCompatibility() == SettingsGeneral.COMPATIBILITY_CMB05) {
            return (int) value;
        }
        else {
            return (int) Math.round(value);
        }
    }


    public void performSignificanceTest() {
        if (m_Validation != null) {
            for (int i = 0; i < m_DiscrMean.length; i++) {
                if (m_DiscrMean[i]) {
                    /* Predicted class i, check sig? */
                    int pop_tot = round(m_Global.getTotalWeight());
                    int pop_cls = round(m_Global.getTotalWeight() * m_Global.m_Means[i]);
                    int rule_tot = round(m_Validation.getTotalWeight());
                    int rule_cls = round(m_Validation.getTotalWeight() * m_Validation.m_Means[i]);
                    int upper = Math.min(rule_tot, pop_cls);
                    int nb_other = pop_tot - pop_cls;
                    int min_this = rule_tot - nb_other;
                    int lower = Math.max(rule_cls, min_this);
                    if (rule_cls < min_this || lower > upper) {
                        System.err.println("BUG?");
                        System.out.println("rule = " + m_Validation.getTotalWeight() * m_Validation.m_Means[i]);
                        System.out.println("pop_tot = " + pop_tot + " pop_cls = " + pop_cls + " rule_tot = " + rule_tot + " rule_cls = " + rule_cls);
                    }
                    // HypergeometricDistribution dist = m_Fac.createHypergeometricDistribution(pop_tot, pop_cls,
                    // rule_tot);
                    HypergeometricDistribution dist = new org.apache.commons.math3.distribution.HypergeometricDistribution(pop_tot, pop_cls, rule_tot);

                    // try {
                    double stat = dist.cumulativeProbability(lower, upper);
                    if (stat >= m_SigLevel) {
                        m_DiscrMean[i] = false;
                    }
                    // }
                    // catch (MathException me) {
                    // System.err.println("Math error: " + me.getMessage());
                    // }
                }
            }
        }
    }


    public void setMeanTuple(ClassesTuple tuple) {
        setMeanTuple(tuple.getVectorBoolean(m_Hier));
    }


    public void setMeanTuple(boolean[] cls) {
        m_DiscrMean = new boolean[cls.length];
        System.arraycopy(cls, 0, m_DiscrMean, 0, cls.length);
        Arrays.fill(m_Means, 0.0);
        for (int i = 0; i < m_DiscrMean.length; i++) {
            if (m_DiscrMean[i])
                m_Means[i] = 1.0;
        }
    }


    public boolean[] getDiscretePred() {
        return m_DiscrMean;
    }


    /**
     * Compute squared Euclidean distance between tuple's target attributes and this statistic's mean.
     */
    @Override
    public double getSquaredDistance(DataTuple tuple, ClusAttributeWeights weights) {
        double sum = 0.0;
        boolean[] actual = new boolean[m_Hier.getTotal()];
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        tp.fillBoolArrayNodeAndAncestors(actual);
        for (int i = 0; i < m_Hier.getTotal(); i++) {
            NumericAttrType type = getAttribute(i);
            double actual_zo = actual[i] ? 1.0 : 0.0;
            double dist = actual_zo - m_Means[i];
            sum += dist * dist * weights.getWeight(type);
        }
        return sum / getNbAttributes();
    }


    // daniela
    // Compute squared Euclidean distance between tuple's target attributes and this statistic's mean.
    public double getSquaredDistanceH(DataTuple tuple, double[] m_Weights) {
        if (m_Means == null)
            calcMean();// this depends on the Compatibility
        double sum = 0.0;
        boolean[] actual = new boolean[m_Hier.getTotal()];
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        tp.fillBoolArrayNodeAndAncestors(actual);
        for (int i = 0; i < m_Hier.getTotal(); i++) {
            double actual_zo = actual[i] ? 1.0 : 0.0;
            double dist = actual_zo - m_Means[i];
            sum += dist * dist * m_Weights[i];
        }
        return sum; /// getNbAttributes();
    }


    // Compute squared Euclidean distance between two tuple's target attributes
    public double calcDistance(ClassesTuple t1, ClassesTuple t2) {
        double sum = 0.0;
        boolean[] actual1 = new boolean[m_Hier.getTotal()];
        boolean[] actual2 = new boolean[m_Hier.getTotal()];
        t1.fillBoolArrayNodeAndAncestors(actual1);
        t2.fillBoolArrayNodeAndAncestors(actual2);
        for (int i = 0; i < m_Hier.getTotal(); i++) {
            double actual_zo1 = actual1[i] ? 1.0 : 0.0;
            double actual_zo2 = actual2[i] ? 1.0 : 0.0;
            if (actual_zo1 != actual_zo2)
                sum += (actual_zo1 - actual_zo2) * (actual_zo1 - actual_zo2) * m_Weights[i];
        }
        return sum; /// getNbAttributes();
    }


    // RA
    @Override
    public double calcPtotal(Integer[] permutation) throws ClusException {
        double avgik = 0;
        double W = 0.0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        upsum += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        if (upsum != 0.0 && downsum != 0.0) {
            avgik = ((N - M + 0.0) * upsum) / (W * downsum);
        }
        else
            avgik = 1;
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        upsumR += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        if (upsumR != 0.0 && downsumR != 0.0)
            avgikR = ((N - M + 0.0) * upsumR) / (WR * downsumR);
        else
            avgikR = 1;
        double I = 1 + ((IL + avgikR * (N - M)) / m_data.getNbRows());
        if (Double.isNaN(I)) { throw new ClusException("err!"); }
        return I;
    }


    // global Geary C
    @Override
    public double calcGtotal(Integer[] permutation) throws ClusException {
        double num, den;
        double avgik = 0;
        double W = 0.0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                    upsum += w * calcDistance(tp, tp1);
                    W += w;
                }
                else {
                    upsum += calcDistance(tp, tp);
                    W += 1;
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M - 1.0) * upsum);
        den = (2 * W * downsum);
        if (num != 0.0 && den != 0.0) {
            avgik = num / den;
        }
        else
            avgik = 0;
        // System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" upsum: "+upsum+" downsum: "+downsum);
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                    upsumR += w * calcDistance(tp, tp1);
                    WR += w;
                }
                else {
                    upsumR += calcDistance(tp, tp);
                    WR += 1;
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M - 1.0) * upsumR);
        den = (2 * WR * downsumR);
        if (num != 0.0 && den != 0.0)
            avgikR = num / den;
        else
            avgikR = 0;
        // System.out.println("Right Moran I: "+avgikR+"ex: "+((N-M))+"w: "+WR+"means: "+" upsum: "+upsumR+" downsum:
        // "+downsumR);
        double scaledI = 1 + ((IL + avgikR * (N - M)) / m_data.getNbRows());
        // System.out.println();
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // calculate EquvalentI
    // Equvalent I
    @Override
    public double calcEquvalentItotal(Integer[] permutation) throws ClusException {
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        double avgik = 0.0;
        double avgikR = 0.0;
        double num, den, numR, denR, ikk, ikkR = 0.0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;
        M = prevIndex;
        N = splitIndex;

        if (INITIALIZEPARTIALSUM) { // Annalisa: to check that you need to inizialize the partial sums
            INITIALIZEPARTIALSUM = false;
            M = 0;
            for (int i = M; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                previousSumX2R[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                for (int j = M; j < NR; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    long indexMap = permutation[i] * (NR) + permutation[j];
                    double w = 0;
                    if (permutation[i] > permutation[j])
                        indexMap = permutation[j] * (NR) + permutation[i];
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    previousSumWR[0] += w;
                    previousSumWXXR[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            // System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
            // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            // System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
            // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        }
        // else
        boolean flagRightAllEqual = true;
        boolean flagLeftAllEqual = true;
        {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                previousSumX2[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                previousSumX2R[0] -= getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights));
            }

            // left (0-old)(old-new)
            flagLeftAllEqual = true;
            double oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(0), m_Weights));
            for (int i = 1; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX) // Annalisa : to check that partition does
                                                                            // not contain values which are all the same
                {
                    flagLeftAllEqual = false;
                    break;
                }
            }

            for (int i = 0; i < M; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                for (int j = M; j < N; j++) {
                    long indexMap = permutation[i] * (NR) + permutation[j];
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    if (permutation[i] > permutation[j])
                        indexMap = permutation[j] * (NR) + permutation[i];
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    previousSumW[0] += w;
                    previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            // left (old-new)(0-old)
            for (int i = M; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    long indexMap = permutation[i] * (NR) + permutation[j];
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    if (permutation[i] > permutation[j])
                        indexMap = permutation[j] * (NR) + permutation[i];
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    previousSumW[0] += w;
                    previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            // left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
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
                    previousSumW[0] += w;
                    previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }

            // right side (new-end)(old-new)
            flagRightAllEqual = true;
            oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(N), m_Weights));
            for (int i = N; i < NR; i++) {
                DataTuple exi = m_data.getTuple(i);
                if (oldX != Math.sqrt(getSquaredDistanceH(exi, m_Weights)))
                    flagRightAllEqual = false;
                for (int j = M; j < N; j++) {
                    long indexMap = permutation[i] * (NR) + permutation[j];
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX)
                        flagRightAllEqual = false;
                    if (permutation[i] > permutation[j])
                        indexMap = permutation[j] * (NR) + permutation[i];
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    previousSumWR[0] -= w;
                    previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    ;
                }
            }
            // right (old-new)(new-end)
            for (int i = M; i < N; i++) {
                for (int j = N; j < NR; j++) {
                    long indexMap = permutation[i] * (NR) + permutation[j];
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    if (permutation[i] > permutation[j])
                        indexMap = permutation[j] * (NR) + permutation[i];
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    previousSumWR[0] -= w;
                    previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            // right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
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
                    previousSumWR[0] -= w;
                    previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
        }

        // System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
        // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
        // System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
        // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);

        vkupenBrojElementiVoOvojSplit = N;
        num = vkupenBrojElementiVoOvojSplit * previousSumWXX[0];
        den = previousSumW[0] * previousSumX2[0];
        if (den != 0 && num != 0 && !flagLeftAllEqual)
            ikk = num / den;
        else
            ikk = 1;

        vkupenBrojElementiVoOvojSplit = NR - N;
        numR = vkupenBrojElementiVoOvojSplit * previousSumWXXR[0];
        denR = previousSumWR[0] * previousSumX2R[0];
        if (denR != 0 && numR != 0 && !flagRightAllEqual)
            ikkR = numR / denR;
        else
            ikkR = 1;

        avgikR += ikkR;
        avgik += ikk;
        // System.out.println("Left Moran I: "+ikk+"num "+num+"den "+den+" "+" NM: "+(splitIndex)+" W:
        // "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
        // System.out.println("Right Moran I: "+ikkR+"numR "+numR+"denR "+denR+" "+" NM: "+(NR-splitIndex)+" WR
        // "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);
        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // calculate Equvalent Geary C
    @Override
    public double calcEquvalentGtotal(Integer[] permutation) throws ClusException {
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        // double avgik = 0.0;
        // double avgikR = 0.0;
        double num, den, numR, denR, ikk, ikkR; // , W, WR = 0.0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;
        M = prevIndex;
        N = splitIndex;

        if (INITIALIZEPARTIALSUM) {
            INITIALIZEPARTIALSUM = false;
            M = 0;
            for (int i = M; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2R[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                for (int j = M; j < NR; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumWR[0] += w;
                        previousSumWXXR[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] += 1;
                        previousSumWXXR[0] += calcDistance(tp, tp);
                    }
                }
            }
            // System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
            // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            // System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
            // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        }
        // else
        boolean flagRightAllEqual = true;
        boolean flagLeftAllEqual = true;
        {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                previousSumX2R[0] -= getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights));
            }

            // left (0-old)(old-new)
            flagLeftAllEqual = true;
            double oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(0), m_Weights));
            for (int i = 1; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX) // to check that partition does not contain
                                                                            // values which are all the same
                {
                    flagLeftAllEqual = false;
                    break;
                }
            }

            for (int i = 0; i < M; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // left (old-new)(0-old)
            for (int i = M; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // right side (new-end)(old-new)
            flagRightAllEqual = true;
            oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(N), m_Weights));
            for (int i = N; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (oldX != Math.sqrt(getSquaredDistanceH(exi, m_Weights)))
                    flagRightAllEqual = false;
                for (int j = M; j < N; j++) {
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX)
                        flagRightAllEqual = false;
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
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
            // right (old-new)(new-end)
            for (int i = M; i < N; i++) {
                for (int j = N; j < NR; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
            // right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
        }

        // System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
        // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
        // System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
        // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        vkupenBrojElementiVoOvojSplit = N;
        num = (vkupenBrojElementiVoOvojSplit - 1) * previousSumWXX[0];
        den = 2 * previousSumW[0] * previousSumX2[0];
        if (den != 0 && num != 0 && !flagLeftAllEqual)
            ikk = num / den;
        else
            ikk = 0;

        vkupenBrojElementiVoOvojSplit = NR - N;
        numR = (vkupenBrojElementiVoOvojSplit - 1) * previousSumWXXR[0];
        denR = 2 * previousSumWR[0] * previousSumX2R[0];
        if (denR != 0 && numR != 0 && !flagRightAllEqual)
            ikkR = numR / denR;
        else
            ikkR = 0;

        // System.out.println("Left Moran I: "+ikk+"num "+num+"den "+den+" "+" NM: "+(splitIndex)+" W:
        // "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
        // System.out.println("Right Moran I: "+ikkR+"numR "+numR+"denR "+denR+" "+" NM: "+(NR-splitIndex)+" WR
        // "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);
        I = (ikk * N + ikkR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        // System.out.println(scaledI);
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // Moran I
    @Override
    public double calcItotal(Integer[] permutation) throws ClusException {
        double num, den;
        double avgik = 0;
        double W = 0.0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                    upsum += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    W += w;
                }
                else {
                    upsum += getSquaredDistanceH(exi, m_Weights);
                    W += 1;
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M + 0.0) * upsum);
        den = (W * downsum);
        if (num != 0.0 && den != 0.0) {
            avgik = num / den;
        }
        else
            avgik = 1;
        // System.out.println("w: "+W+"num: "+num+"den: "+den+"Left Moran I: "+avgik+"ex: "+((N-M)));
        // System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" upsum: "+num+" downsum: "+den);
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
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
                    upsumR += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    WR += w;
                }
                else {
                    upsumR += getSquaredDistanceH(exi, m_Weights);
                    WR += 1;
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M + 0.0) * upsumR);
        den = (WR * downsumR);
        if (num != 0.0 && den != 0.0)
            avgikR = num / den;
        else
            avgikR = 1;
        // System.out.println("Right Moran I: "+avgikR+"ex: "+((N-M))+"w: "+WR+" upsum: "+num+" downsum: "+den);
        double I = (IL + avgikR * (N - M)) / m_data.getNbRows();
        double scaledI = 1 + I;
        if (Double.isNaN(I)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // global I with distance file
    @Override
    public double calcItotalD(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        double num, den;
        double avgik = 0;
        double W = 0.0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        // long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                String yyi = xt.getString(exj);
                double w = 0;
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsum += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    W += w;
                }
                else {
                    upsum += getSquaredDistanceH(exi, m_Weights);
                    W += 1;
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M + 0.0) * upsum);
        den = (W * downsum);
        if (num != 0.0 && den != 0.0)
            avgik = num / den;
        else
            avgik = 1;
        // System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" upsum: "+upsum+" downsum: "+downsum);
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                String yyi = xt.getString(exj);
                double w = 0;
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsumR += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    WR += w;
                }
                else {
                    upsumR += getSquaredDistanceH(exi, m_Weights);
                    WR += 1;
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        num = (N - M + 0.0) * upsumR;
        den = WR * downsumR;
        if (num != 0.0 && den != 0.0)
            avgikR = num / den;
        else
            avgikR = 1;
        // System.out.println("Right Moran I: "+avgikR+"ex: "+((N-M))+"w: "+WR+" upsum: "+upsum+" downsum: "+downsum);
        double I = (IL + avgikR * (N - M)) / m_data.getNbRows();
        double scaledI = 1 + I;
        // System.out.println(scaledI);
        if (Double.isNaN(I)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // RA with distance file
    @Override
    public double calcPDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        double avgik = 0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
                String yyi = xt.getString(exj);
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        upsum += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        if (upsum != 0.0 && downsum != 0.0)
            avgik = upsum / downsum;
        else
            avgik = 1;
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
                String yyi = xt.getString(exj);
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        upsumR += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        if (upsumR != 0.0 && downsumR != 0.0)
            avgikR = upsumR / downsumR;
        else
            avgikR = 1;
        double I = 1 + ((IL + avgikR * (N - M)) / m_data.getNbRows());
        // System.out.println(I);
        if (Double.isNaN(I)) { throw new ClusException("err!"); }
        return I;
    }


    // Geary with distance file
    // global C calculation with a separate distance file
    @Override
    public double calcGtotalD(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        double num, den;
        double avgik = 0;
        double W = 0.0;
        double upsum = 0.0;
        double downsum = 0.0;
        double upsumR = 0.0;
        double downsumR = 0.0;
        int M = 0;
        int N = m_data.getNbRows();
        // long NR = m_data.getNbRows();
        if (splitIndex > 0) {
            N = splitIndex;
        }
        else {
            M = -splitIndex;
        }
        // left
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
                String yyi = xt.getString(exj);
                double w = 0;
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsum += w * calcDistance(tp, tp1);
                    W += w;
                }
                else {
                    upsum += calcDistance(tp, tp);
                    W += 1;
                }
            }
            downsum += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M - 1.0) * upsum);
        den = (2 * W * downsum);
        if (num != 0.0 && den != 0.0) {
            avgik = num / den;
        }
        else
            avgik = 0;
        // System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" upsum: "+num+" downsum: "+den);
        double IL = avgik * (N - M);

        // right side
        N = m_data.getNbRows();
        M = splitIndex;
        double avgikR = 0;
        double WR = 0.0;
        for (int i = M; i < N; i++) {
            DataTuple exi = m_data.getTuple(permutation[i]);
            String xxi = xt.getString(exi);
            boolean[] actual = new boolean[m_Hier.getTotal()];
            ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
            tp.fillBoolArrayNodeAndAncestors(actual);
            for (int j = M; j < N; j++) {
                DataTuple exj = m_data.getTuple(permutation[j]);
                boolean[] actual1 = new boolean[m_Hier.getTotal()];
                ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                tp1.fillBoolArrayNodeAndAncestors(actual1);
                String yyi = xt.getString(exj);
                double w = 0;
                String indexI = xxi;
                String indexJ = yyi;
                if (indexI != indexJ) {
                    String indexMap = indexI + "#" + indexJ;
                    Double temp = GISHeuristic.m_distancesS.get(indexMap);
                    if (temp != null)
                        w = temp;
                    else
                        w = 0;
                    upsumR += w * calcDistance(tp, tp1);
                    WR += w;
                }
                else {
                    upsumR += calcDistance(tp, tp);
                    WR += 1;
                }
            }
            downsumR += getSquaredDistanceH(exi, m_Weights);
        }
        num = ((N - M - 1.0) * upsumR);
        den = (2 * WR * downsumR);
        if (num != 0.0 && den != 0.0)
            avgikR = num / den;
        else
            avgikR = 0;
        // System.out.println("Right Moran I: "+avgikR+"ex: "+((N-M))+"w: "+WR+"means: "+" upsum: "+num+" downsum:
        // "+den);
        double scaledI = 1 + ((IL + avgikR * (N - M)) / m_data.getNbRows());
        // System.out.println(scaledI);
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // calculate EquvalentI with Distance file
    @Override
    public double calcEquvalentIDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        double num, den, numR, denR, ikk, ikkR = 0.0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;
        M = prevIndex;
        N = splitIndex;

        if (INITIALIZEPARTIALSUM) {
            INITIALIZEPARTIALSUM = false;
            M = 0;
            for (int i = M; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2R[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                for (int j = M; j < NR; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] += w;
                        previousSumWXXR[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumWR[0] += 1;
                        previousSumWXXR[0] += getSquaredDistanceH(exi, m_Weights);
                    }
                }
            }
            // System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
            // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            // System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
            // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        }
        // else
        boolean flagRightAllEqual = true;
        boolean flagLeftAllEqual = true;
        {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                previousSumX2R[0] -= getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights));
            }

            // left (0-old)(old-new)
            flagLeftAllEqual = true;
            double oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(0), m_Weights));
            for (int i = 1; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX) // to check that partition does not contain
                                                                            // values which are all the same
                {
                    flagLeftAllEqual = false;
                    break;
                }
            }

            for (int i = 0; i < M; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
            // left (old-new)(0-old)
            for (int i = M; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
            // left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
            // right side (new-end)(old-new)
            flagRightAllEqual = true;
            oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(N), m_Weights));
            for (int i = N; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (oldX != Math.sqrt(getSquaredDistanceH(exi, m_Weights)))
                    flagRightAllEqual = false;
                for (int j = M; j < N; j++) {
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX)
                        flagRightAllEqual = false;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
            // right (old-new)(new-end)
            for (int i = M; i < N; i++) {
                for (int j = N; j < NR; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
            // right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                }
            }
        }

        // System.out.println("Update SumLeft : sumX2 "+previousSumX2[0]+" sumW "+previousSumW[0]+" sumX2W
        // "+previousSumWXX[0]);
        // System.out.println("Update SumRight : sumX2 "+previousSumX2R[0]+" sumW "+previousSumWR[0]+" sumX2W
        // "+previousSumWXXR[0]);
        vkupenBrojElementiVoOvojSplit = N;
        num = vkupenBrojElementiVoOvojSplit * previousSumWXX[0];
        den = previousSumW[0] * previousSumX2[0];
        if (den != 0 && num != 0 && !flagLeftAllEqual)
            ikk = num / den; // I for each target
        else
            ikk = 1;

        vkupenBrojElementiVoOvojSplit = NR - N;
        numR = vkupenBrojElementiVoOvojSplit * previousSumWXXR[0];
        denR = previousSumWR[0] * previousSumX2R[0];
        if (denR != 0 && numR != 0 && !flagRightAllEqual)
            ikkR = numR / denR; // I for each target
        else
            ikkR = 1;
        // System.out.println("Left Moran I: "+ikk+"num "+num+"den "+den+" "+" NM: "+(splitIndex)+" W:
        // "+previousSumW[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
        // System.out.println("Right Moran I: "+ikkR+"numR "+numR+"denR "+denR+" "+" NM: "+(NR-splitIndex)+" WR
        // "+previousSumWR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);
        I = (ikk * N + ikkR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        // System.out.println(scaledI);
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // calculate EquvalentP with Distance file
    @Override
    public double calcEquvalentPDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        int vkupenBrojElementiVoCelataSuma = NR;
        double I = 0;
        double ikk, ikkR = 0.0;
        M = prevIndex;
        N = splitIndex;

        if (INITIALIZEPARTIALSUM) {
            INITIALIZEPARTIALSUM = false;
            M = 0;
            for (int i = M; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2R[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                for (int j = M; j < NR; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else {previousSumWXXR[0]+=getSquaredDistanceH(exi,m_Weights);}
                }
            }
            // System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
            // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            // System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
            // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        }
        // else
        boolean flagRightAllEqual = true;
        boolean flagLeftAllEqual = true;
        {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                previousSumX2R[0] -= getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights));
            }

            // left (0-old)(old-new)
            flagLeftAllEqual = true;
            double oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(0), m_Weights));
            for (int i = 1; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX) // to check that partition does not contain
                                                                            // values which are all the same
                {
                    flagLeftAllEqual = false;
                    break;
                }
            }

            for (int i = 0; i < M; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXX[0]+=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
            // left (old-new)(0-old)
            for (int i = M; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXX[0]+=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
            // left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXX[0]+=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
            // right side (new-end)(old-new)
            flagRightAllEqual = true;
            oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(N), m_Weights));
            for (int i = N; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (oldX != Math.sqrt(getSquaredDistanceH(exi, m_Weights)))
                    flagRightAllEqual = false;
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX)
                        flagRightAllEqual = false;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXXR[0]-=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
            // right (old-new)(new-end)
            for (int i = M; i < N; i++) {
                for (int j = N; j < NR; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXXR[0]-=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
            // right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            previousSumWXXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights) * getSquaredDistanceH(exj, m_Weights));
                    }
                    // else
                    // {previousSumWXXR[0]-=Math.sqrt(getSquaredDistanceH(exi,m_Weights)*getSquaredDistanceH(exj,m_Weights));}
                }
            }
        }

        if (previousSumX2[0] != 0 && previousSumWXX[0] != 0 && !flagLeftAllEqual)
            ikk = previousSumWXX[0] / previousSumX2[0];
        else
            ikk = 1;
        if (previousSumX2R[0] != 0 && previousSumWXXR[0] != 0 && !flagRightAllEqual)
            ikkR = previousSumWXXR[0] / previousSumX2R[0];
        else
            ikkR = 1;
        // System.out.println("Left Moran I: "+ikk+"num "+num+"den "+den+" "+" NM: "+(splitIndex)+" W:
        // "+previousSumW[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
        // System.out.println("Right Moran I: "+ikkR+"numR "+numR+"denR "+denR+" "+" NM: "+(NR-splitIndex)+" WR
        // "+previousSumWR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);
        I = (ikk * N + ikkR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 1 + I;
        // System.out.println(scaledI);
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    // Equvalent Geary with distance file
    // calculate Equvalent Geary C with Distance file
    @Override
    public double calcEquvalentGDistance(Integer[] permutation) throws ClusException {
        ClusSchema schema = m_data.getSchema();
        ClusAttrType xt = schema.getAllAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = 0;
        int NR = m_data.getNbRows();
        double I = 0;
        double avgik = 0.0;
        double avgikR = 0.0;
        double num, den, numR, denR; // , ikk, ikkR, W, WR = 0.0;
        int vkupenBrojElementiVoOvojSplit = N - M;
        int vkupenBrojElementiVoCelataSuma = NR;
        M = prevIndex;
        N = splitIndex;

        if (INITIALIZEPARTIALSUM) {
            INITIALIZEPARTIALSUM = false;
            M = 0;
            for (int i = M; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2R[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                for (int j = M; j < NR; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] += w;
                        previousSumWXXR[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] += 1;
                        previousSumWXXR[0] += calcDistance(tp, tp);
                    }
                }
            }
            // System.out.println("Init SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
            // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
            // System.out.println("Init SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
            // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        }
        // else
        boolean flagRightAllEqual = true;
        boolean flagLeftAllEqual = true;
        {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                previousSumX2[0] += getSquaredDistanceH(exi, m_Weights);
                previousSumX[0] += Math.sqrt(getSquaredDistanceH(exi, m_Weights));
                previousSumX2R[0] -= getSquaredDistanceH(exi, m_Weights);
                previousSumXR[0] -= Math.sqrt(getSquaredDistanceH(exi, m_Weights));
            }

            // left (0-old)(old-new)
            flagLeftAllEqual = true;
            double oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(0), m_Weights));
            for (int i = 1; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX) // to check that partition does not contain
                                                                            // values which are all the same
                {
                    flagLeftAllEqual = false;
                    break;
                }
            }

            for (int i = 0; i < M; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // left (old-new)(0-old)
            for (int i = M; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                for (int j = M; j < N; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumW[0] += w;
                        previousSumWXX[0] += w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumW[0] += 1;
                        previousSumWXX[0] += calcDistance(tp, tp);
                    }
                }
            }
            // right side (new-end)(old-new)
            flagRightAllEqual = true;
            oldX = Math.sqrt(getSquaredDistanceH(m_data.getTuple(N), m_Weights));
            for (int i = N; i < NR; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                if (oldX != Math.sqrt(getSquaredDistanceH(exi, m_Weights)))
                    flagRightAllEqual = false;
                for (int j = M; j < N; j++) {
                    double w = 0;
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    if (Math.sqrt(getSquaredDistanceH(exi, m_Weights)) != oldX)
                        flagRightAllEqual = false;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
            // right (old-new)(new-end)
            for (int i = M; i < N; i++) {
                for (int j = N; j < NR; j++) {
                    DataTuple exi = m_data.getTuple(permutation[i]);
                    String xxi = xt.getString(exi);
                    boolean[] actual = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                    tp.fillBoolArrayNodeAndAncestors(actual);
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
            // right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_data.getTuple(permutation[i]);
                String xxi = xt.getString(exi);
                boolean[] actual = new boolean[m_Hier.getTotal()];
                ClassesTuple tp = (ClassesTuple) exi.getObjVal(m_Hier.getType().getArrayIndex());
                tp.fillBoolArrayNodeAndAncestors(actual);
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_data.getTuple(permutation[j]);
                    boolean[] actual1 = new boolean[m_Hier.getTotal()];
                    ClassesTuple tp1 = (ClassesTuple) exj.getObjVal(m_Hier.getType().getArrayIndex());
                    tp1.fillBoolArrayNodeAndAncestors(actual1);
                    String yyi = xt.getString(exj);
                    double w = 0;
                    String indexI = xxi;
                    String indexJ = yyi;
                    if (indexI != indexJ) {
                        String indexMap = indexI + "#" + indexJ;
                        Double temp = GISHeuristic.m_distancesS.get(indexMap);
                        if (temp != null)
                            w = temp;
                        else
                            w = 0;
                        previousSumWR[0] -= w;
                        previousSumWXXR[0] -= w * calcDistance(tp, tp1);
                    }
                    else {
                        previousSumWR[0] -= 1;
                        previousSumWXXR[0] -= calcDistance(tp, tp);
                    }
                }
            }
        }

        // System.out.println("Update SumLeft : sumX "+previousSumX[0]+" sumX2 "+previousSumX2[0]+" sumW
        // "+previousSumW[0]+" sumX2W "+previousSumWXX[0]+" sumXW"+previousSumWX[0]);
        // System.out.println("Update SumRight : sumX "+previousSumXR[0]+" sumX2 "+previousSumX2R[0]+" sumW
        // "+previousSumWR[0]+" sumX2W "+previousSumWXXR[0]+" sumXW"+previousSumWXR[0]);
        vkupenBrojElementiVoOvojSplit = N;
        num = (vkupenBrojElementiVoOvojSplit - 1) * previousSumWXX[0];
        den = 2 * previousSumW[0] * previousSumX2[0];
        if (den != 0 && num != 0 && !flagLeftAllEqual) {
            // ikk = num / den;
        }
        else {
            // ikk = 0;
        }

        vkupenBrojElementiVoOvojSplit = NR - N;
        numR = (vkupenBrojElementiVoOvojSplit - 1) * previousSumWXXR[0];
        denR = 2 * previousSumWR[0] * previousSumX2R[0];
        if (denR != 0 && numR != 0 && !flagRightAllEqual) {
            // ikkR = numR / denR;
        }
        else {
            // ikkR = 0;
        }

        // System.out.println("Left Moran I: "+ikk+"num "+num+"den "+den+" "+" NM: "+(splitIndex)+" W:
        // "+previousSumW[0]+" wx:"+previousSumWX[0]+" wxx:"+previousSumWXX[0]+" xx:"+previousSumX2[0]);
        // System.out.println("Right Moran I: "+ikkR+"numR "+numR+"denR "+den+" "+" NM: "+(NR-splitIndex)+" WR
        // "+previousSumWR[0]+" wxR: "+previousSumWXR[0]+" wxx "+previousSumWXXR[0]+" xx:"+previousSumX2R[0]);
        I = (avgik * N + avgikR * (NR - N)) / vkupenBrojElementiVoCelataSuma;
        M = prevIndex;
        N = splitIndex;
        double scaledI = 2 - I;
        // System.out.println(scaledI);
        if (Double.isNaN(scaledI)) { throw new ClusException("err!"); }
        return scaledI;
    }


    @Override
    public void setData(RowData data) {
        m_data = data;
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
    // end daniela


    /**
     * Compute squared distance between each of the tuple's target attributes and this statistic's mean.
     */
    public double[] getPointwiseSquaredDistance(DataTuple tuple) {
        double[] distances = new double[m_Hier.getTotal()];
        boolean[] actual = new boolean[m_Hier.getTotal()];
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        tp.fillBoolArrayNodeAndAncestors(actual);
        for (int i = 0; i < m_Hier.getTotal(); i++) {
            // NumericAttrType type = getAttribute(i);
            double actual_zo = actual[i] ? 1.0 : 0.0;
            double dist = actual_zo - m_Means[i];
            distances[i] = dist * dist * m_Hier.getWeight(i);
        }

        return distances;
    }


    public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights, ClusStatManager statmanager) {
        double sum = 0.0;
        boolean[] actual = new boolean[m_Hier.getTotal()];
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        tp.fillBoolArrayNodeAndAncestors(actual);
        for (int i = 0; i < m_Hier.getTotal(); i++) {
            NumericAttrType type = getAttribute(i);
            double actual_zo = actual[i] ? 1.0 : 0.0;
            double dist = actual_zo - m_Means[i];
            WHTDStatistic tstat = (WHTDStatistic) statmanager.getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING);
            if (tstat.getVariance(i) != 0)
                dist = dist / Math.pow(tstat.getVariance(i), 0.5);
            sum += Math.abs(dist) * weights.getWeight(type);
        }
        return sum / getNbAttributes();
    }


    public void printTree() {
        m_Hier.print(ClusFormat.OUT_WRITER, m_SumValues);
        ClusFormat.OUT_WRITER.flush();
    }


    @Override
    public String getString(StatisticPrintInfo info) {
        String pred = null;
        if (m_Threshold >= 0.0) {
            pred = computePrintTuple().toStringHuman(getHier());
            return pred + " [" + ClusFormat.TWO_AFTER_DOT.format(getTotalWeight()) + "]";
        }
        else {
            ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
            StringBuffer buf = new StringBuffer();
            buf.append("[");
            for (int i = 0; i < getHier().getTotal(); i++) {
                if (i != 0)
                    buf.append(",");
                if (m_SumWeight == 0.0)
                    buf.append("?");
                else
                    buf.append(fr.format(getMean(i)));
            }
            buf.append("]");
            if (info.SHOW_EXAMPLE_COUNT) {
                buf.append(": ");
                buf.append(fr.format(m_SumWeight));
            }
            return buf.toString();
        }
    }


    @Override
    public String getPredictString() {
        return "[" + computeMeanTuple().toStringHuman(getHier()) + "]";
    }


    // public boolean isValidPrediction() {
    // return !m_MeanTuple.isRoot();
    // }

    @Override
    public void showRootInfo() {
        try {
            String hierarchyFile = m_Hier.getSettings().getGeneric().getAppName() + ".hierarchy";
            PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(hierarchyFile)));
            wrt.println("Hier #nodes: " + m_Hier.getTotal());
            wrt.println("Hier classes by level: " + MIntArray.toString(m_Hier.getClassesByLevel()));
            m_Hier.print(wrt, m_SumValues, null);
            wrt.close();
        }
        catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }


    public void printDistributionRec(PrintWriter out, ClassTerm node) {
        int idx = node.getIndex();
        ClassesValue val = new ClassesValue(node);
        out.println(val.toPathString() + ", " + m_Means[idx]);
        for (int i = 0; i < node.getNbChildren(); i++) {
            printDistributionRec(out, (ClassTerm) node.getChild(i));
        }
    }


    @Override
    public void printDistribution(PrintWriter wrt) throws IOException {
        wrt.println("Total: " + m_SumWeight);
        ClassTerm root = m_Hier.getRoot();
        for (int i = 0; i < root.getNbChildren(); i++) {
            printDistributionRec(wrt, (ClassTerm) root.getChild(i));
        }
    }


    public void getExtraInfoRec(ClassTerm node, double[] discrmean, StringBuffer out) {
        if (m_Validation != null) {
            int i = node.getIndex();
            if (discrmean[i] > 0.5) {
                /* Predicted class i, check sig? */
                int pop_tot = round(m_Global.getTotalWeight());
                int pop_cls = round(m_Global.getTotalWeight() * m_Global.m_Means[i]);
                int rule_tot = round(m_Validation.getTotalWeight());
                int rule_cls = round(m_Validation.getTotalWeight() * m_Validation.m_Means[i]);
                int upper = Math.min(rule_tot, pop_cls);
                int nb_other = pop_tot - pop_cls;
                int min_this = rule_tot - nb_other;
                int lower = Math.max(rule_cls, min_this);
                // HypergeometricDistribution dist = m_Fac.createHypergeometricDistribution(pop_tot, pop_cls, rule_tot);
                org.apache.commons.math3.distribution.HypergeometricDistribution dist = new org.apache.commons.math3.distribution.HypergeometricDistribution(pop_tot, pop_cls, rule_tot);

                // try {
                double stat = dist.cumulativeProbability(lower, upper);
                out.append(node.toStringHuman(getHier()) + ":");
                out.append(" pop_tot = " + String.valueOf(pop_tot));
                out.append(" pop_cls = " + String.valueOf(pop_cls));
                out.append(" rule_tot = " + String.valueOf(rule_tot));
                out.append(" rule_cls = " + String.valueOf(rule_cls));
                out.append(" upper = " + String.valueOf(upper));
                out.append(" prob = " + ClusFormat.SIX_AFTER_DOT.format(stat));
                // out.append(" siglevel = "+m_SigLevel);
                out.append("\n");
                // }
                // catch (MathException me) {
                // System.err.println("Math error: " + me.getMessage());
                // }
            }
        }
        for (int i = 0; i < node.getNbChildren(); i++) {
            getExtraInfoRec((ClassTerm) node.getChild(i), discrmean, out);
        }
    }


    @Override
    public String getExtraInfo() {
        StringBuffer res = new StringBuffer();
        ClassesTuple meantuple = m_Hier.getBestTupleMaj(m_Means, 50.0);
        double[] discrmean = meantuple.getVectorNodeAndAncestors(m_Hier);
        for (int i = 0; i < m_Hier.getRoot().getNbChildren(); i++) {
            getExtraInfoRec((ClassTerm) m_Hier.getRoot().getChild(i), discrmean, res);
        }
        return res.toString();
    }


    @Override
    public void addPredictWriterSchema(String prefix, ClusSchema schema) {
        ClassHierarchy hier = getHier();
        for (int i = 0; i < m_NbAttrs; i++) {
            ClusAttrType type = m_Attrs[i].cloneType();
            ClassTerm term = hier.getTermAt(i);
            type.setName(prefix + "-p-" + term.toStringHuman(hier));
            schema.addAttrType(type);
        }
    }


    @Override
    public void unionInit() {
        m_DiscrMean = new boolean[m_Means.length];
    }


    @Override
    public void union(ClusStatistic other) {
        boolean[] discr_mean = ((WHTDStatistic) other).m_DiscrMean;
        for (int i = 0; i < m_DiscrMean.length; i++) {
            if (discr_mean[i])
                m_DiscrMean[i] = true;
        }
    }


    @Override
    public void unionDone() {
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes) {
        reset();
        m_Means = new double[m_NbAttrs];
        WHTDStatistic vote;
        int nb_votes = votes.size();
        for (int j = 0; j < nb_votes; j++) {
            vote = (WHTDStatistic) votes.get(j);
            for (int i = 0; i < m_NbAttrs; i++) {
                m_Means[i] += vote.m_Means[i] / nb_votes;
            }
        }
        computePrediction();
    }


    /**
     * Used for the hierarchical rules heuristic
     */
    @Override
    public double getDispersion(ClusAttributeWeights scale, RowData data) {
        return getSVarS(scale);
    }


    @Override
    public String getDistanceName() {
        return "Hierarchical Weighted Euclidean Distance";
    }


    @Override
    public double getSVarS(int i) { // poolAUPRC case
        throw new RuntimeException("getSVarS(int i)");
    }


    @Override
    public double getSVarS(ClusAttributeWeights scale) {
        if (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) { // poolAUPRC case
            ArrayList<Integer> classInd = new ArrayList<Integer>(m_NbAttrs);
            for (int i = 0; i < m_NbAttrs; i++) {
                classInd.add(i, i);
            }
            final double[] positives = Arrays.copyOf(m_P, m_P.length);
            classInd.sort(new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) { // sort decreasingly
                    if (positives[o1] < positives[o2]) {
                        return 1;
                    }
                    else if (positives[o1] > positives[o2]) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });
            // compute pooled PR-curve
            double conditionPositives = 0.0;
            for (int i = 0; i < positives.length; i++) {
                conditionPositives += positives[i];
            }
            double prev = Double.NaN;
            double TP = 0.0, FP = 0.0;
            boolean first = true;
            ArrayList<double[]> PRcurve = new ArrayList<double[]>();
            for (int i = 0; i < positives.length; i++) {
                double threshold = positives[classInd.get(i)]; // the real threshold is positives[classInd.get(i)] /
                                                               // m_SumWeight ??????????????
                if (threshold != prev && !first) {
                    PRcurve.add(new double[] { TP / conditionPositives, TP / (TP + FP) });
                }
                TP += threshold;
                FP += m_SumWeight - threshold;

                prev = threshold;
                first = false;
            }
            PRcurve.add(new double[] { TP / conditionPositives, TP / (TP + FP) }); // maybe, this was added twice, but
                                                                                   // area will stay the same
            return computeArea(PRcurve);
        }
        else {
            return super.getSVarS(scale);
        }
    }


    @Override
    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
        if (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) { // poolAUPRC case
            ArrayList<Integer> classInd = new ArrayList<Integer>(m_NbAttrs);
            for (int i = 0; i < m_NbAttrs; i++) {
                classInd.add(i, i);
            }
            final double[] positives = new double[m_NbAttrs];
            for (int i = 0; i < m_NbAttrs; i++) {
                positives[i] = m_P[i] - ((WHTDStatistic) other).m_P[i];
            }
            classInd.sort(new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) { // sort decreasingly
                    if (positives[o1] < positives[o2]) {
                        return 1;
                    }
                    else if (positives[o1] > positives[o2]) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });
            // compute pooled PR-curve
            double conditionPositives = 0.0;
            for (int i = 0; i < positives.length; i++) {
                conditionPositives += positives[i];
            }
            double prev = Double.NaN;
            double TP = 0.0, FP = 0.0;
            boolean first = true;
            ArrayList<double[]> PRcurve = new ArrayList<double[]>();
            for (int i = 0; i < positives.length; i++) {
                double threshold = positives[classInd.get(i)]; // the real threshold is positives[classInd.get(i)] /
                                                               // m_SumWeight ??????????????
                if (threshold != prev && !first) {
                    PRcurve.add(new double[] { TP / conditionPositives, TP / (TP + FP) });
                }
                TP += threshold;
                FP += m_SumWeight - threshold;

                prev = threshold;
                first = false;
            }
            PRcurve.add(new double[] { TP / conditionPositives, TP / (TP + FP) }); // maybe, this was added twice, but
                                                                                   // area will stay the same
            return computeArea(PRcurve);
        }
        else {
            return super.getSVarSDiff(scale, other);
        }
    }


    /**
     * Computes the area under the (pooled) curve. This is a copy of
     * {@code clus.error.ROCAndPRCurve.computeArea(ArrayList)}. Used in poolAUPRC case
     * 
     * @param curve
     * @return The area under the curve
     */
    public static double computeArea(ArrayList<double[]> curve) {
        double area = 0.0;
        if (curve.size() > 0) {
            double[] prev = curve.get(0);
            for (int i = 1; i < curve.size(); i++) {
                double[] pt = curve.get(i);
                area += 0.5 * (pt[1] + prev[1]) * (pt[0] - prev[0]);
                prev = pt;
            }
        }
        return area;
    }


    @Override
    public void copy(ClusStatistic other) {
        if (m_Distance == SettingsHMLC.HIERDIST_NO_DIST) { // poolAUPRC case
            WHTDStatistic or = (WHTDStatistic) other;
            m_SumWeight = or.m_SumWeight;
            m_NbExamples = or.m_NbExamples;
            System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
            System.arraycopy(or.m_P, 0, m_P, 0, m_NbAttrs); // additional line compared to else case
        }
        else {
            super.copy(other);
        }
    }


    public void setThresholds(double threshold) {
        m_Thresholds = new double[m_Hier.getWeights().length];
        for (int i = 0; i < m_Thresholds.length; i++) {
            m_Thresholds[i] = threshold;
        }
    }


    public double[] getThresholds() {
        return m_Thresholds;
    }


    @Override
    public boolean samePrediction(ClusStatistic other) {
        WHTDStatistic rstat = (WHTDStatistic) other;

        for (int i = 0; i < m_NbAttrs; i++) {
            if (getMean(i) != rstat.getMean(i))
                return false;
        }

        return true;
    }


    /**
     * Provides sum of weights of target attributes
     * 
     * @return sum of weights of target attributes, or NaN if statistic doesn't contain target attributes (i.e.,
     *         unsupervised learning is performed)
     */
    @Override
    public double getTargetSumWeights() {
        return getTotalWeight(); // FIXME not sure if this is correct, in HMC there is no partially labeled examples,
                                 // they are either labeled or unlabeled?
    }


    /**
     * Get weight of labeled examples, if there are
     * 
     * @return
     */
    public double getWeightLabeled() {
        if (m_SumWeight == 0.0) {
            switch (getSettings().getTree().getMissingClusteringAttrHandling()) {
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                    return m_Training.getWeightLabeled();
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
                    return m_ParentStat.getWeightLabeled();
                case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
                    return 0;
                default:
                    return m_Training.getWeightLabeled();
            }
        }

        return m_SumWeight;
    }
}

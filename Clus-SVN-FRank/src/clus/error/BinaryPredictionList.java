
package clus.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import clus.jeans.util.compound.DoubleBooleanCount;
import clus.main.settings.Settings;


// Avril Lavigne in matejp se sprasujeta: Why do you have to /.../ make things so complicated?
/**
 * This class could be used in any kind of classification setting (e.g., hierarchical multilabel) and basically stores
 * the statistics,
 * which enables as to compute the number of TP, TN, FP and FN (T - true, F - false, P - positives, and N - negatives)
 * for any given threshold.
 * <p>
 * 
 * The class is used for constructing ROC- and PR-curves, where the threshold vary, hence it basically stores the
 * triplets
 * 
 * <p>
 * {@code (probability(X), truth, count)}
 * <p>
 * 
 * where
 * <ul>
 * <li>{@code 0.0 <= probability(X) <= 1.0} denotes our estimate of probability of presence of some fixed label X (or
 * positive class),</li>
 * <li>{@code truth = true | false} denotes the ground truth, and
 * <li>{@code count} denotes the number of examples seen, with the first two properties.</li>
 * </ul>
 *
 */
public class BinaryPredictionList implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
    /** The number of positive examples seen */
    protected int m_NbPos;
    /** The number of negative examples seen */
    protected int m_NbNeg; 
    /** Either empty list or sorted list of values of m_ValueSet */
    protected transient ArrayList<DoubleBooleanCount> m_Values = new ArrayList<DoubleBooleanCount>(); 
    /**
     * Hash map with key: value pairs that are of form (hash of) x: x
     */
    protected transient HashMap<DoubleBooleanCount, DoubleBooleanCount> m_ValueSet = new HashMap<DoubleBooleanCount, DoubleBooleanCount>();

    /**
     * Updates the binary-classification statistics.
     * 
     * @param actual
     *        Ground truth, denotes the presence of a given label (or belonging to positive class)
     * @param predicted
     *        Ranges between {@code 0} and {@code 1} and gives the estimate of probability that the given example is
     *        labelled by the label (or belonging to positive class)
     *        <p>
     * 
     *        See {@code jeans.util.compound.DoubleBoolean.hashCode()} to understand, why this actually works properly.
     */
    public void addExample(boolean actual, double predicted) {
        DoubleBooleanCount value = new DoubleBooleanCount(predicted, actual);
        DoubleBooleanCount prevValue = (DoubleBooleanCount) m_ValueSet.get(value);
        if (prevValue != null) {
            prevValue.inc();
        }
        else {
            m_ValueSet.put(value, value);
        }
        if (actual)
            m_NbPos++;
        else
            m_NbNeg++;
    }


    public void addInvalid(boolean actual) {
        if (actual)
            m_NbPos++;
        else
            m_NbNeg++;
    }


    /**
     * Sorts the {@code jeans.util.compound.DoubleBoolean}s from the {@code m_ValueSet}. Used for ROC- and PR-curves
     * construction.
     */
    public void sort() {
        m_Values.clear();
        m_Values.addAll(m_ValueSet.values());
        Collections.sort(m_Values);
    }


    public int size() {
        return m_Values.size();
    }


    public DoubleBooleanCount get(int i) {
        return (DoubleBooleanCount) m_Values.get(i);
    }


    public void clear() {
        m_NbPos = 0;
        m_NbNeg = 0;
        m_Values.clear();
        m_ValueSet.clear();
    }


    public void clearData() {
        m_Values.clear();
    }


    public int getNbPos() {
        return m_NbPos;
    }


    public int getNbNeg() {
        return m_NbNeg;
    }


    public double getFrequency() {
        return (double) m_NbPos / (m_NbPos + m_NbNeg);
    }


    public boolean hasBothPosAndNegEx() {
        return m_NbPos != 0 && m_NbNeg != 0;
    }


    public void add(BinaryPredictionList other) {
        m_NbPos += other.getNbPos();
        m_NbNeg += other.getNbNeg();
        Iterator values = other.m_ValueSet.values().iterator();
        while (values.hasNext()) {
            DoubleBooleanCount otherValue = (DoubleBooleanCount) values.next();
            DoubleBooleanCount myValue = (DoubleBooleanCount) m_ValueSet.get(otherValue);
            if (myValue != null) {
                myValue.inc(otherValue);
            }
            else {
                DoubleBooleanCount newValue = new DoubleBooleanCount(otherValue);
                m_ValueSet.put(newValue, newValue);
            }
        }
    }


    public void copyActual(BinaryPredictionList other) {
        m_NbPos = other.getNbPos();
        m_NbNeg = other.getNbNeg();
    }
}

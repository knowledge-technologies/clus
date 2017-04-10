
package clus.algo.Relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import javax.print.attribute.standard.PresentationDirection;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.StringAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.ensembles.ClusEnsembleFeatureRanking;
import clus.ext.hierarchical.ClassHierarchy;
import clus.ext.hierarchical.ClassTerm;
import clus.ext.hierarchical.ClassesAttrType;
import clus.ext.hierarchical.ClassesTuple;
import clus.ext.timeseries.DTWTimeSeriesDist;
import clus.ext.timeseries.QDMTimeSeriesDist;
import clus.ext.timeseries.TSCTimeSeriesDist;
import clus.ext.timeseries.TimeSeries;
import clus.jeans.math.MathUtil;
import clus.main.Settings;
import clus.util.ClusException;


/**
 * 
 * @author matejp
 *
 */
public class ClusReliefFeatureRanking extends ClusEnsembleFeatureRanking {
	private static final int DESCRIPTIVE_SPACE = 0;
	private static final int TARGET_SPACE = 1;
	private static final int[] SPACE_TYPES = new int[]{DESCRIPTIVE_SPACE, TARGET_SPACE};

    /** number of neighbours in the importances calculation */
    private int m_NbNeighbours;
    /** number of iterations in the importances calculation */
    private int m_NbIterations;
    /** Tells, whether the contributions of the neighbours are weighted */
    private boolean m_WeightNeighbours;
    /** {@code >= 0}, see {@link m_NeighbourWeights}, {@code m_Sigma == 0 <=> m_WeightNeighbours == false} */
    private double m_Sigma;
    /**
     * Weights for the contributions of the nearest neighbours. Value on the i-th place is
     * {@code exp((- m_Sigma * i)^2)}.
     */
    private double[] m_NeighbourWeights;

    /** {array of descriptive attributes, array of target attributes} */
    private ClusAttrType[][] m_DescriptiveTargetAttr = new ClusAttrType[2][];

    /** number of descriptive attributes */
    private int m_NbDescriptiveAttrs;
    /** number of target attributes */
    private int m_NbTargetAttrs;

    /** {numericAttributeName: minimalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMins;
    /** {numericAttributeName: maximalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMaxs;
    /** number of examples in the data */
    private int m_NbExamples;
    /** distance in the case of missing values */
    double m_bothMissing = 1.0;
    /**
     * Tells whether the next instance generator is deterministic (if and only if {@code m_NbIterations == m_NbExamples}
     * ).
     */
    boolean m_isDeterministic;
    /** Random generator for sampling of the next instance. It is used iff not m_isDeterministic */
    Random m_rnd = new Random(1234);
    /** standard classification or general case */
    boolean m_isStandardClassification;
    /** number of target values: if m_isStandardClassification: the number of classes, else: 1 */
    int m_NbTargetValues;
    /** relative frequencies of the target values, used in standard classification */
    double[] m_targetProbabilities;
    /** type of the time series distance */
    int m_TimeSeriesDistance;
    /** Hash map for hierarchical attributes: {attributeName: hierarchy, ... } */
    private HashMap<String, ClassHierarchy> m_Hierarchies = new HashMap<String, ClassHierarchy>();
    /** The weights, used in Weighted Euclidean distance, for each hierarchical attribute */
    private HashMap<String, Double> m_HierarUpperBounds = new HashMap<String, Double>();


    /**
     * Constructor for the {@code ClusReliefFeatureRanking}, with the standard parameters of (R)Relief(F).
     * 
     * @param neighbours
     *        The number of neighbours that is used in the feature importance calculation. Constraints:
     *        <p>
     *        {@code 0 < neighbours <= number of instances in the dataset}
     * @param iterations
     *        The number of iterations in the feature importance calculation. Constraints:
     *        <p>
     *        {@code 0 < iterations <= number of instances in the dataset}
     * @param weightNeighbours
     *        If {@code weightNeighbours}, then the contribution of the {@code i}-th nearest neighbour in the feature
     *        importance calculation are weighted with factor {@code exp((- sigma * i) ** 2)}.
     * @param sigma
     *        The rate of quadratic exponential decay. Note that Weka's sigma is the inverse of our {@code sigma}.
     */
    public ClusReliefFeatureRanking(int neighbours, int iterations, boolean weightNeighbours, double sigma) {
        super();
        this.m_NbNeighbours = neighbours;
        this.m_NbIterations = iterations;
        this.m_WeightNeighbours = weightNeighbours;
        this.m_Sigma = m_WeightNeighbours ? sigma : 0.0;
        m_NeighbourWeights = new double[m_NbNeighbours];
        if (m_WeightNeighbours) {
            for (int neigh = 0; neigh < m_NbNeighbours; neigh++) {
                m_NeighbourWeights[neigh] = Math.exp(-(m_Sigma * neigh) * (m_Sigma * neigh));
            }
        }
        else {
            Arrays.fill(m_NeighbourWeights, 1.0);
        }
        
        ArrayList<String> rankings = new ArrayList<String>();
        rankings.add("overAll");
        setReliefFimpHeader(rankings);
    }


    /**
     * Calculates the feature importances for a given dataset.
     * 
     * @param data
     *        The dataset, whose features are importances calculated for.
     * @throws ClusException
     * @throws InterruptedException
     */
    public void calculateReliefImportance(RowData data) throws ClusException, InterruptedException {
        m_TimeSeriesDistance = data.m_Schema.getSettings().m_TimeSeriesDistance.getValue();
        setReliefDescription(m_NbNeighbours, m_NbIterations);
        m_NbExamples = data.getNbRows();
        m_isDeterministic = m_NbExamples == m_NbIterations;

        // Initialise descriptive and target attributes if necessary
        int attrType;
        for (int space : SPACE_TYPES) {
            attrType = space == DESCRIPTIVE_SPACE ? ClusAttrType.ATTR_USE_DESCRIPTIVE : ClusAttrType.ATTR_USE_TARGET;
            if (m_DescriptiveTargetAttr[space] == null)
                m_DescriptiveTargetAttr[space] = data.m_Schema.getAllAttrUse(attrType);
        }
        m_NbDescriptiveAttrs = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE].length;
        m_NbTargetAttrs = m_DescriptiveTargetAttr[1].length;
        m_isStandardClassification = m_NbTargetAttrs == 1 && m_DescriptiveTargetAttr[TARGET_SPACE][0] instanceof NominalAttrType;
        m_NbTargetValues = m_isStandardClassification ? ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][0]).getNbValues() : 1;

        // class counts
        if (m_isStandardClassification) {
            m_targetProbabilities = new double[m_NbTargetValues + 1]; // one additional place for missing values
            NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[1][0];
            for (int example = 0; example < m_NbExamples; example++) {
                m_targetProbabilities[attr.getNominal(data.getTuple(example))] += 1.0;
            }
            if (m_NbExamples > m_targetProbabilities[m_NbTargetValues]) { // otherwise: m_TargetProbabilities = {0, 0, ... , 0, m_NbExamples}
                // Normalise probabilities: examples with unknown targets are ignored
                // The formula for standard classification class weighting still holds, i.e. sum over other classes of
                // P(other class) / (1 - P(class)) equals 1
                for (int value = 0; value < m_NbTargetValues; value++) {
                    m_targetProbabilities[value] /= m_NbExamples - m_targetProbabilities[m_NbTargetValues];
                }
            }
        }

        // compute min and max of numeric attributes
        m_numMins = new HashMap<String, Double>();
        m_numMaxs = new HashMap<String, Double>();
        double value;
        String attrName;
        for (int space : SPACE_TYPES) {
            attrType = space == DESCRIPTIVE_SPACE ? ClusAttrType.ATTR_USE_DESCRIPTIVE : ClusAttrType.ATTR_USE_TARGET;
            for (NumericAttrType numAttr : data.m_Schema.getNumericAttrUse(attrType)) {
                attrName = numAttr.getName();
                m_numMins.put(attrName, Double.POSITIVE_INFINITY);
                m_numMaxs.put(attrName, Double.NEGATIVE_INFINITY);
                for (int example = 0; example < m_NbExamples; example++) {
                    value = numAttr.getNumeric(data.getTuple(example));
                    if (value < m_numMins.get(attrName)) { // equivalent to ... && value != Double.POSITIVE_INFINITY
                        m_numMins.put(attrName, value);
                    }
                    if (value > m_numMaxs.get(attrName) && value != Double.POSITIVE_INFINITY) {
                        m_numMaxs.put(attrName, value);
                    }
                }
            }
        }

        // check for hierarchical attributes
        for (int space : SPACE_TYPES) {
            for (int attrInd = 0; attrInd < m_DescriptiveTargetAttr[space].length; attrInd++) {
                if (m_DescriptiveTargetAttr[space][attrInd] instanceof ClassesAttrType) {
                    ClassesAttrType attr = (ClassesAttrType) m_DescriptiveTargetAttr[space][attrInd];
                    attrName = attr.getName();
                    m_Hierarchies.put(attrName, attr.getHier());
                    computeDepthsOfTerms(attr);
                    boolean[][] presentAndFinal = getPresentTerms(attr.getHier(), data);
                    double upperDistanceBound = upperBound(attr, presentAndFinal);
                    m_HierarUpperBounds.put(attrName, upperDistanceBound);

                }
            }
        }

        // attribute relevance estimation
        double[] sumDistAttr = new double[m_NbDescriptiveAttrs];
        double sumDistTarget = 0.0;
        double[] sumDistAttrTarget = new double[m_NbDescriptiveAttrs];
        DataTuple tuple;
        int tupleInd;
        NearestNeighbour[][] nearestNeighbours;
        ClusAttrType attr;
        double successfulItearions = 0.0;
        for (int iteration = 0; iteration < m_NbIterations; iteration++) {
            // CHOOSE TUPLE AND COMPUTE NEAREST NEIGHBOURS
            tupleInd = nextInstance(iteration);
            tuple = data.getTuple(tupleInd);
            if (!(m_isStandardClassification && m_DescriptiveTargetAttr[TARGET_SPACE][0].isMissing(tuple))) {
                successfulItearions++;
                nearestNeighbours = findNearestNeighbours(tupleInd, data);
                // CALCULATE THE SUMS OF DISTANCES
                for (int targetValue = 0; targetValue < m_NbTargetValues; targetValue++) {
                    double tempSumDistTarget = 0.0;
                    double[] tempSumDistAttr = new double[m_NbDescriptiveAttrs];
                    double[] tempSumDistAttrTarget = new double[m_NbDescriptiveAttrs];
                    double sumNeighbourWeights = 0.0;
                    for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
                        sumNeighbourWeights += m_NeighbourWeights[neighbour];
                    }
                    for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
                        if (!m_isStandardClassification) {
                            tempSumDistTarget += nearestNeighbours[targetValue][neighbour].m_targetDistance * (m_NeighbourWeights[neighbour] / sumNeighbourWeights);
                        }
                        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                            attr = m_DescriptiveTargetAttr[0][attrInd];
                            double distAttr = calcDistance1D(tuple, data.getTuple(nearestNeighbours[targetValue][neighbour].m_indexInDataSet), attr);
                            if (m_isStandardClassification) {
                                int tupleTarget = ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][0]).getNominal(tuple);
                                tempSumDistAttr[attrInd] += (targetValue == tupleTarget ? -distAttr : m_targetProbabilities[targetValue] / (1.0 - m_targetProbabilities[tupleTarget]) * distAttr) * (m_NeighbourWeights[neighbour] / sumNeighbourWeights);
                            }
                            else {
                                tempSumDistAttr[attrInd] += distAttr * (m_NeighbourWeights[neighbour] / sumNeighbourWeights);
                                tempSumDistAttrTarget[attrInd] += distAttr * nearestNeighbours[targetValue][neighbour].m_targetDistance * (m_NeighbourWeights[neighbour] / sumNeighbourWeights);
                            }
                        }
                    }
                    sumDistTarget += tempSumDistTarget;
                    for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                        sumDistAttr[attrInd] += tempSumDistAttr[attrInd];
                        sumDistAttrTarget[attrInd] += tempSumDistAttrTarget[attrInd];
                    }
                }
            }
        }
        // UPDATE IMPORTANCES
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
            attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][attrInd];
            double[] info = getAttributeInfo(attr.getName());
            if (m_isStandardClassification) {
                info[2] += sumDistAttr[attrInd] / successfulItearions;
            }
            else {
                info[2] += sumDistAttrTarget[attrInd] / sumDistTarget - (sumDistAttr[attrInd] - sumDistAttrTarget[attrInd]) / (successfulItearions - sumDistTarget);
            }
            putAttributeInfo(attr.getName(), info);
        }
    }


    /**
     * Computes the nearest neighbours of example with index {@code tupleInd} in the dataset {@code data}.
     * 
     * @param tupleInd
     *        Row index of the example in the dataset {@code data}, whose nearest neighbours are computed.
     * @param data
     *        The dataset
     * @return An array of {@code m_NbTargetValues} arrays of {@link NearestNeighbour}s. Each of the arrays belongs to
     *         one target value and
     *         is sorted decreasingly with respect to the distance(neighbour, considered tuple).
     * @throws ClusException
     */
    public NearestNeighbour[][] findNearestNeighbours(int tupleInd, RowData data) throws ClusException {
        DataTuple tuple = data.getTuple(tupleInd);
        int[][] neighbours = new int[m_NbTargetValues][m_NbNeighbours]; // current candidates
        double[] distances = new double[m_NbExamples]; // distances[i] = distance(tuple, data.getTuple(i))
        int[] whereToPlaceNeigh = new int[m_NbTargetValues];
        int targetValue;

        for (int i = 0; i < m_NbExamples; i++) {
            distances[i] = calcDistance(tuple, data.getTuple(i), DESCRIPTIVE_SPACE); // in descriptive space
        }
        boolean sortingNeeded;
        boolean isSorted[] = new boolean[m_NbTargetValues]; // isSorted[target value]: tells whether the neighbours for
                                                            // target value are sorted
        for (int i = 0; i < m_NbExamples; i++) {
            sortingNeeded = false;
            if (i != tupleInd) {
                targetValue = m_isStandardClassification ? m_DescriptiveTargetAttr[TARGET_SPACE][0].getNominal(data.getTuple(i)) : 0;
                if (targetValue < m_NbTargetValues) { // non-missing
                    if (whereToPlaceNeigh[targetValue] < m_NbNeighbours) {
                        neighbours[targetValue][whereToPlaceNeigh[targetValue]] = i;
                        whereToPlaceNeigh[targetValue]++;
                        if (whereToPlaceNeigh[targetValue] == m_NbNeighbours) { // the list of neighbours has just
                                                                                // become full ---> sort it
                            for (int ind1 = 0; ind1 < m_NbNeighbours; ind1++) { // O(NbNeighbours^2) ...
                                for (int ind2 = ind1 + 1; ind2 < m_NbNeighbours; ind2++) {
                                    if (distances[neighbours[targetValue][ind1]] < distances[neighbours[targetValue][ind2]]) {
                                        int temp = neighbours[targetValue][ind1];
                                        neighbours[targetValue][ind1] = neighbours[targetValue][ind2];
                                        neighbours[targetValue][ind2] = temp;
                                    }
                                }
                            }
                            isSorted[targetValue] = true;
                        }
                    }
                    else {
                        sortingNeeded = true;
                    }
                }
                else {
                    // nothing to do here
                }
                if (sortingNeeded) {
                    if (distances[i] >= distances[neighbours[targetValue][0]]) {
                        continue;
                    }
                    int j; // here the branch prediction should kick-in
                    for (j = 1; j < m_NbNeighbours && distances[i] < distances[neighbours[targetValue][j]]; j++) {
                        neighbours[targetValue][j - 1] = neighbours[targetValue][j];
                    }
                    neighbours[targetValue][j - 1] = i;
                    isSorted[targetValue] = true;
                }

            }
        }
        NearestNeighbour[][] nearestNeighbours = new NearestNeighbour[m_NbTargetValues][];
        for (int value = 0; value < m_NbTargetValues; value++) {
            nearestNeighbours[value] = new NearestNeighbour[whereToPlaceNeigh[value]];
            if (!isSorted[value]) {
                for (int ind1 = 0; ind1 < whereToPlaceNeigh[value]; ind1++) {
                    for (int ind2 = ind1 + 1; ind2 < whereToPlaceNeigh[value]; ind2++) {
                        if (distances[neighbours[value][ind1]] < distances[neighbours[value][ind2]]) {
                            int temp = neighbours[value][ind1];
                            neighbours[value][ind1] = neighbours[value][ind2];
                            neighbours[value][ind2] = temp;
                        }
                    }
                }
            }

            for (int i = 0; i < whereToPlaceNeigh[value]; i++) {
                nearestNeighbours[value][whereToPlaceNeigh[value] - i - 1] = new NearestNeighbour(neighbours[value][i], distances[neighbours[value][i]], calcDistance(tuple, data.getTuple(neighbours[value][i]), TARGET_SPACE));
            }
        }
        return nearestNeighbours;
    }


    /**
     * Distance between tuples in the subspace {@code space}.
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param space
     *        0 or 1; if 0, subspace is descriptive space and target space otherwise.
     * @return Distance between {@code t1} and {@code t2} in the given subspace.
     * @throws ClusException
     */
    public double calcDistance(DataTuple t1, DataTuple t2, int space) throws ClusException {
        double dist = 0.0;
        int dimensions = space == DESCRIPTIVE_SPACE ? m_NbDescriptiveAttrs : m_NbTargetAttrs;
        ClusAttrType attr;
        for (int attrInd = 0; attrInd < dimensions; attrInd++) {
            attr = m_DescriptiveTargetAttr[space][attrInd];
            dist += calcDistance1D(t1, t2, attr);
        }
        return dist / dimensions;
    }


    /**
     * Calculates the distance between to tuples in a given component {@code attr}.
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param attr
     *        The attribute/dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return distance({@code attr.value(t1), attr.value(t2)})
     * @throws ClusException
     */
    public double calcDistance1D(DataTuple t1, DataTuple t2, ClusAttrType attr) throws ClusException {
        if (attr.isNominal()) {
            return calculateNominalDist1D(t1, t2, (NominalAttrType) attr);
        }
        else if (attr.isNumeric()) {
            double normFactor = m_numMaxs.get(attr.getName()) - m_numMins.get(attr.getName());
            if (normFactor == 0.0) { // if and only if the attribute has only one value ... Distance will be zero and
                                     // does not depend on normFactor
                normFactor = 1.0;
            }
            return calculateNumericDist1D(t1, t2, (NumericAttrType) attr, normFactor);
        }
        else if (attr instanceof TimeSeriesAttrType) {
            return calculateTimeSeriesDist1D(t1, t2, (TimeSeriesAttrType) attr);
        }
        else if (attr instanceof StringAttrType) {
            return calculateStringDist1D(t1, t2, (StringAttrType) attr);
        }
        else if (attr instanceof ClassesAttrType) {
            return calculateHierarchicalDist1D(t1, t2, (ClassesAttrType) attr);
        }
        else {
            throw new ClusException("Unknown attribute type for attribute " + attr.getName() + ": " + attr.getClass().toString());
        }

    }


    // TODO: make handling of missing values reliefish for all dist.

    /**
     * Calculates distance between the nominal values of the component {@code attr}. In the case of missing values, we
     * follow Weka's solution
     * and not the paper Theoretical and Empirical Analysis of ReliefF and RReliefF, by Robnik Sikonja and Kononenko
     * (time complexity ...).
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param attr
     *        The nominal attribute/dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return distance({@code attr.value(t1), attr.value(t2)})
     */
    public double calculateNominalDist1D(DataTuple t1, DataTuple t2, NominalAttrType attr) {
        int v1 = attr.getNominal(t1);
        int v2 = attr.getNominal(t2);
        if (v1 >= attr.m_NbValues || v2 >= attr.m_NbValues) { // at least one missing
            return 1.0 - 1.0 / attr.m_NbValues;
        }
        else {
            return v1 == v2 ? 0.0 : 1.0;
        }
    }


    /**
     * Calculates distance between the numeric values of the component {@code attr}. In the case of missing values, we
     * follow Weka's solution
     * and not the paper Theoretical and Empirical Analysis of ReliefF and RReliefF, by Robnik Sikonja and Kononenko
     * (time complexity ...).
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param attr
     *        The numeric attribute/dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @param normalizationFactor
     *        Typically,
     *        <p>
     *        {@code normalizationFactor = 1 / (m_numMaxs[attr.name()] - m_numMins[attr.name()])}.
     * @return If {@code v1} and {@code v2} are the numeric values of the attribute {@code attr} for the instances
     *         {@code t1} and {@code t2},
     *         the value
     *         <p>
     *         {@code |v1 - v2| / normalizationFactor}
     *         <p>
     *         is returned.
     */
    public double calculateNumericDist1D(DataTuple t1, DataTuple t2, NumericAttrType attr, double normalizationFactor) {
        double v1 = attr.getNumeric(t1);
        double v2 = attr.getNumeric(t2);
        double t;
        if (t1.hasNumMissing(attr.getArrayIndex())) {
            if (t2.hasNumMissing(attr.getArrayIndex())) {
                t = m_bothMissing;
            }
            else {
                t = (v2 - m_numMins.get(attr.getName())) / normalizationFactor;
                t = Math.max(t, 1.0 - t);
            }
        }
        else {
            if (t2.hasNumMissing(attr.getArrayIndex())) {
                t = (v1 - m_numMins.get(attr.getName())) / normalizationFactor;
                t = Math.max(t, 1.0 - t);
            }
            else {
                t = Math.abs(v1 - v2) / normalizationFactor;
            }
        }
        return t;

    }


    /**
     * Computes distance between the time series values of the component {@code attr}.
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param attr
     *        The time series attribute/dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return distance({@code attr.value(t1), attr.value(t2)})
     * @throws ClusException
     */
    public double calculateTimeSeriesDist1D(DataTuple t1, DataTuple t2, TimeSeriesAttrType attr) throws ClusException {
        TimeSeries ts1 = attr.getTimeSeries(t1);
        TimeSeries ts2 = attr.getTimeSeries(t2);

        switch (m_TimeSeriesDistance) {
            case Settings.TIME_SERIES_DISTANCE_MEASURE_DTW:
                return new DTWTimeSeriesDist(attr).calcDistance(t1, t2);
            case Settings.TIME_SERIES_DISTANCE_MEASURE_QDM:
                if (ts1.length() == ts2.length()) {
                    return new QDMTimeSeriesDist(attr).calcDistance(t1, t2);
                }
                else {
                    throw new ClusException("QDM Distance is not implemented for time series with different length");
                }
            case Settings.TIME_SERIES_DISTANCE_MEASURE_TSC:
                return new TSCTimeSeriesDist(attr).calcDistance(t1, t2);
            default:
                throw new ClusException("ClusReliefFeatureRanking.m_TimeSeriesDistance was not set to any known value.");
        }

    }


    /**
     * Computes Levenshtein's distance between the string values of the component {@code attr}.
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @param attr
     *        The string attribute/dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return Levenshtein distance between {@code attr.value(t1)} and {@code attr.value(t2)}.
     */
    public double calculateStringDist1D(DataTuple t1, DataTuple t2, StringAttrType attr) {
        return new Levenshtein(t1, t2, attr).getDist();
    }


    /**
     * Calculates the weighted Euclidean distance between the two classes in the hierarchy, where the weight of
     * a label is {@code m_HierarWeight^depth}. The distance is at the end normalised by the upper bound
     * {@code m_HierarDistBound}.
     * 
     * @param t1
     *        First tuple
     * @param t2
     *        Second tuple
     * @param attr
     *        The dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return Normalised weighted Euclidean distance.
     */
    public double calculateHierarchicalDist1D(DataTuple t1, DataTuple t2, ClassesAttrType attr) {
        String name = attr.getName();
        ClassHierarchy hier = m_Hierarchies.get(name);
        int sidx = hier.getType().getArrayIndex();
        ClassesTuple tp1 = (ClassesTuple) t1.getObjVal(sidx);
        ClassesTuple tp2 = (ClassesTuple) t2.getObjVal(sidx);
        HashSet<Integer> symmetricDifferenceOfLabelSets = new HashSet<Integer>();

     // add labels for t1
        for (int j = 0; j < tp1.getNbClasses(); j++) {
            int labelIndex = tp1.getClass(j).getIndex();
            symmetricDifferenceOfLabelSets.add(labelIndex);
        }
        // remove the intersection labels of t1 and t2, and add labels(t2) - labels(t1)
        for (int j = 0; j < tp2.getNbClasses(); j++) {
            int labelIndex = tp2.getClass(j).getIndex();
            if (symmetricDifferenceOfLabelSets.contains(labelIndex)) {
                symmetricDifferenceOfLabelSets.remove(labelIndex);
            }
            else {
                symmetricDifferenceOfLabelSets.add(labelIndex);
            }
        }
        double dist = weightedEuclideanInt(hier, symmetricDifferenceOfLabelSets);
        return dist / m_HierarUpperBounds.get(name);
    }
    
    private double weightedEuclideanInt(ClassHierarchy hier, HashSet<Integer> symmetricDifferenceOfLabelSets){
    	double dist = 0.0;
        for (int labelIndex : symmetricDifferenceOfLabelSets) {
            dist += hier.getWeight(labelIndex);
        }
        return dist;
    }
    
    private double weightedEuclideanTerm(ClassHierarchy hier, HashSet<ClassTerm> symmetricDifferenceOfLabelSets){
    	double dist = 0.0;
        for (ClassTerm term : symmetricDifferenceOfLabelSets) {
            dist += hier.getWeight(term.getIndex());
        }
        return dist;
    }
    
    private double weightedEuclidean(ClassHierarchy hier, ClassTerm finalTerm1, ClassTerm finalTerm2){
    	return weightedEuclideanTerm(hier, MathUtil.symmetricDifference(finalTerm1.getAllAncestors(true), finalTerm2.getAllAncestors(true)));
    }
    
    
    public void sortFeatureRanks() {
        Iterator iter = m_AllAttributes.keySet().iterator();
        while (iter.hasNext()) {
            String attr = (String) iter.next();
            // double score = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
            double score = ((double[]) m_AllAttributes.get(attr))[2];
            ArrayList attrs = new ArrayList();
            if (m_FeatureRanks.containsKey(score))
                attrs = (ArrayList) m_FeatureRanks.get(score);
            attrs.add(attr);
            m_FeatureRanks.put(score, attrs);
        }
    }


    /**
     * Computes the index of the chosen example in the iteration {@code iteration}.
     * 
     * @param iteration
     *        Index of the iteration.
     * @return The index of the chosen example in the given iteration.
     */
    private int nextInstance(int iteration) {
        if (m_isDeterministic) {
            return iteration;
        }
        else {
            return (int) (m_rnd.nextDouble() * m_NbExamples);
        }
    }


    /**
     * Computes the depth of each term in the hierarchy, which can be DAG or tree-shaped.
     * 
     * @param attr
     *        The attribute, which the hierarchy belongs to.
     */
    private void computeDepthsOfTerms(ClassesAttrType attr) {
        ClassHierarchy hier = m_Hierarchies.get(attr.getName());
        ArrayList<ClassTerm> toProcess = new ArrayList<ClassTerm>();
        HashMap<ClassTerm, Integer> numberOfProcessedParents = new HashMap<ClassTerm, Integer>();
        for (int i = 0; i < hier.getTotal(); i++) {
            ClassTerm term = hier.getTermAt(i);
            numberOfProcessedParents.put(term, term.getNbParents());
            if (numberOfProcessedParents.get(term) == 0) {
                toProcess.add(term);
            }
        }
        toProcess.add(hier.getRoot());
        while (toProcess.size() > 0) {
            ClassTerm term = toProcess.remove(toProcess.size() - 1);
            // compute the depth
            if (term.getNbParents() == 0) {
                term.setDepth(-1.0); // root
            }
            else {
                double avgDepth = 0.0;
                for (int i = 0; i < term.getNbParents(); i++) {
                    avgDepth += term.getParent(i).getDepth();
                }
                avgDepth /= term.getNbParents();
                term.setDepth(avgDepth + 1.0);
            }
            // see the children
            for (int i = 0; i < term.getNbChildren(); i++) {
                ClassTerm child = (ClassTerm) term.getChild(i);
                int nbParentsToProcess = numberOfProcessedParents.get(child);
                if (nbParentsToProcess == 1) {
                    toProcess.add(child);
                }
                numberOfProcessedParents.put(child, nbParentsToProcess - 1);
            }
        }

    }


    /**
     * Finds the maximal depth of the subtree with a given root. Works also for DAGs.
     * 
     * @param root
     *        The root of the subtree/subDAG.
     * @return
     */
    private double maxDepthSubtree(ClassTerm root) {
        if (root.getNbChildren() == 0) {
            return root.getDepth();
        }
        else {
            double maxDepth = 0.0;
            for (int child = 0; child < root.getNbChildren(); child++) {
                maxDepth = Math.max(maxDepth, maxDepthSubtree((ClassTerm) root.getChild(child)));
            }
            return maxDepth;
        }
    }


    /**
     * If the hierarchy {@code hier} is tree-shaped, this efficiently (in linear time) computes the maximal distance {@code d(x, y)} over the vectors
     * {@code x} and {@code y} that correspond to the labels in the hierarchy and are present in the data.<br>
     * If the hierarchy is only DAG, then the time complexity is O(squared number of different labels in data).
     * 
     * @param attr
     *        Hierarchical attribute under consideration
     * @param presentAndFinalClasses An array of 3 arrays:
     * <ul>
     * <li>[..., is the i-th label present in the data, ...] </li>
     * <li>[..., is the i-th label the most specific for some example in the data, ...] </li>
     * <li>[is the number of different most specific labels in the data at least two]</li>
     * </ul>
     * @return {@code max d(x, y)} over the label sets x and y in the dataset.
     */
    private double upperBound(ClassesAttrType attr, boolean[][] presentAndFinalClasses) {
    	boolean atLeastTwoClasses = presentAndFinalClasses[2][0];
    	if(atLeastTwoClasses){
	        ClassHierarchy hier = m_Hierarchies.get(attr.getName());
	        if(hier.isTree()){
	        	return upperBoundTree(hier, presentAndFinalClasses[0], presentAndFinalClasses[1]);
	        } else {
	        	return upperBoundDAG(hier, presentAndFinalClasses[0], presentAndFinalClasses[1]);
	        } 
    	} else{
        	return 1.0; // does not matter what is the normalisation constant, the distance will be always 0
        }

        
    }
    
    /**
     * Recursively finds the two most distant terms of the hierarchy that represent the final classes in the data, it two such terms exist. 
     * @param root The current root of the hierarchy.
     * @param presentClasses see upperBound
     * @param finalClasses see upperBound
     * @return
     */
    private ArrayList<ClassTerm> findDistantTerms(ClassHierarchy hier, ClassTerm root, boolean[] presentClasses, boolean[] finalClasses){
    	ArrayList<ClassTerm> extremeTerms = new ArrayList<ClassTerm>();
    	int rootInd = root.getIndex();
    	if(rootInd >= 0 && !presentClasses[rootInd]){
    		// the part of the hierarchy rooted here, is not in the data --> empty list
    		return extremeTerms;
    	}
    	ArrayList<ArrayList<ClassTerm>> childrenResults = new ArrayList<ArrayList<ClassTerm>>();
    	int nbCh = root.getNbChildren();
    	for(int i = 0; i < nbCh; i++){
    		ArrayList<ClassTerm> resultCh = findDistantTerms(hier, (ClassTerm) root.getChild(i), presentClasses, finalClasses);
    		if(resultCh.size() > 0){
    			childrenResults.add(resultCh);
    		}
    	}
    	
    	boolean rootIsApproprite = rootInd >= 0 && finalClasses[rootInd];
    	int nbChildrenCandidates = childrenResults.size();
    	// find the indices of <= 2 children that have the deepest first term
		int notAnIndex = -1234;
		// double[] depths = new double[]{-10.0,-10.0};  // initial value does not matter
		int[] inds = new int[]{notAnIndex, notAnIndex}; // something that is not an index (-1 not good choice because of the root term)
		for(int i = 0; i < nbChildrenCandidates; i++){
			double thisDepth = childrenResults.get(i).get(0).getDepth();
			int whereTo = 0;			
			while(whereTo < inds.length){				
				if (inds[whereTo] == notAnIndex || thisDepth > childrenResults.get(inds[whereTo]).get(0).getDepth() + MathUtil.C1E_6){
					break;
				}
				whereTo++;
			}
			// move everything worse than the current one place to right: if whereTo = the last position or even worse, nothing happens
			for(int worseItemInd = inds.length - 2; worseItemInd >= whereTo ; worseItemInd--){
				inds[worseItemInd + 1] = inds[worseItemInd];
			}
			if (whereTo < inds.length){
				inds[whereTo] = i;
				}    		
    	}
    	
    	if (nbChildrenCandidates == 0){
    		// empty + possibly the root
    		if(rootIsApproprite){
    			extremeTerms.add(root);
    		}
    	} else if (nbChildrenCandidates == 1){
    		extremeTerms.add(childrenResults.get(0).get(0)); // always in
    		if(childrenResults.get(0).size() == 1 && rootIsApproprite){  // add root, if root is final
    			extremeTerms.add(root); 
    		} else if(childrenResults.get(0).size() == 2 && rootIsApproprite){ 	// add the other child term or root
    			double distChildRootComb = weightedEuclidean(hier, childrenResults.get(0).get(0), root);
    			double distChildComb = weightedEuclidean(hier, childrenResults.get(0).get(0), childrenResults.get(0).get(1));
    			if(distChildRootComb > distChildComb){
    				extremeTerms.add(root);
    			} else{
    				extremeTerms.add(childrenResults.get(0).get(1));
    			}
    		} else if(childrenResults.get(0).size() == 2){ // add the other child term
    			extremeTerms.add(childrenResults.get(0).get(1));
    		}
    	} else{ // root cannot be an option, either two deepest 1st components or one of the children
    		double maxDistDiffChildren = weightedEuclidean(hier, childrenResults.get(inds[0]).get(0),
    															 childrenResults.get(inds[1]).get(0));
    		double maxInnerDistChild = 0.0;
    		int optimal = -1;
    		for(int i = 0; i < nbChildrenCandidates; i++){
    			if (childrenResults.get(i).size() > 1){
	    			double dist = weightedEuclidean(hier, childrenResults.get(i).get(0), childrenResults.get(i).get(1));
	    			if(dist > maxInnerDistChild + MathUtil.C1E_9){
	    				optimal = i;
	    				maxInnerDistChild = dist;
	    			}
    			}
    		}
    		if(maxDistDiffChildren > maxInnerDistChild){
    			extremeTerms.add(childrenResults.get(inds[0]).get(0));
    			extremeTerms.add(childrenResults.get(inds[1]).get(0));
    		} else{
    			extremeTerms.addAll(childrenResults.get(optimal));
    		}
    	}
    	return extremeTerms;
    }
    
    /**
     * Finds the upper bound of the weighted Euclidean distance for a given data. Covers the following cases:<br>
     * <ul>
     * <li> any type of tree hierarchy (e.g. line) </li>
     * <li> at least two terms of the hierarchy must be present in the data</li>
     * </ul>
     * @param hier The hierarchy
     * @param presentClasses true at that components that correspond to terms that are present in the data
     * @param finalClasses true at that components that correspond to the deepest term for some example in the data
     * @return
     */
    private double upperBoundTree(ClassHierarchy hier, boolean[] presentClasses, boolean[] finalClasses){
    	double bound = 0.0;
        ClassTerm root = hier.getRoot();
        
        ArrayList<ClassTerm> extremeTerms = findDistantTerms(hier, root, presentClasses, finalClasses);  // must have length two!
        bound = weightedEuclidean(hier, extremeTerms.get(0), extremeTerms.get(1));        
        return bound;
    }
      
    /**
     * We iterate over all pairs of different final classes and return the maximal distance achieved.
     * @param hier
     * @param presentClasses
     * @param finalClasses
     * @return
     */
    private double upperBoundDAG(ClassHierarchy hier, boolean[] presentClasses, boolean[] finalClasses){
    	ArrayList<Integer> finalClassesOnly = new ArrayList<Integer>();
    	for(int termInd = 0; termInd < finalClasses.length; termInd++){
    		if(finalClasses[termInd]){
    			finalClassesOnly.add(termInd);
    		}
    	}
    	double bound = 0.0;
    	for(int i = 0; i < finalClassesOnly.size(); i++){
    		ClassTerm term1 = hier.getTermAt((finalClassesOnly.get(i)));
    		for(int j = i + 1; j < finalClassesOnly.size(); j++){
    			ClassTerm term2 = hier.getTermAt((finalClassesOnly.get(j)));
    			double dist = weightedEuclidean(hier, term1, term2);
    			if(dist > bound + MathUtil.C1E_9){
    				bound = dist;
    			}
    		}
    	}	
    	return bound;
    }


    /**
     * Computes the geometric sum {@code 1 + q + ... + q^lastPower}.
     * 
     * @param q
     * @param lastPower
     * @return
     */
    private double geometricSum(double q, double lastPower) {
        return (1 - Math.pow(q, lastPower + 1)) / (1 - q);
    }


    /**
     * The weight, used in Weighted Euclidean distance.
     * 
     * @param hier
     * @return The the biggest among the weights of the labels that are smaller than 1.0.
     */
    private double getWeight(ClassHierarchy hier) {
        double[] weights = hier.getWeights();
        double maxW = 0.0;
        for (double w : weights) {
            if (w < 1.0 - MathUtil.C1E_6 && w > maxW) {
                maxW = w;
            }
        }
        return maxW;
    }
    
    /**
     * Finds the indices of the classes in the hierarchy that at least one example belongs to. 
     * @param hier
     * @param data
     * @return answer[class index] = true iff at least one example belongs to class.
     */
    private boolean[][] getPresentTerms(ClassHierarchy hier, RowData data){
    	int hierarchySize = hier.getTotal();
    	boolean[] present = new boolean[hierarchySize];
    	boolean[] finalClasses = new boolean[hierarchySize];
    	int nbFinal = 0;
    	int sidx = hier.getType().getArrayIndex();
    	boolean isTree = hier.isTree();
    	// we can break the loop if all classes are present, since in that case,
    	// the upper bound for d(x1, x2) is achieved in two leaves unless hierarchy is a line
    	// root ---> a1 ---> a2 ---> ... ---> an ... Better safe than sorry ... We will not break the loop.
    	for(int i = 0; i < m_NbExamples; i++){ //  && nbPresent < hierarchySize
    		ClassesTuple tp = (ClassesTuple) data.getTuple(i).getObjVal(sidx);
    		int nbClasses = tp.getNbClasses();
    		int lastClassIndex = -123;
    		if(isTree){
    			lastClassIndex = tp.getClass(nbClasses - 1).getIndex();
    		} else{
    			lastClassIndex = mostSpecificTermIndex(tp);
    		}
    		if(!finalClasses[lastClassIndex]){
    			finalClasses[lastClassIndex] = true;
    			nbFinal++;
    		}
    		if(present[lastClassIndex]){ // if DAG, the leaf may not be the last in the list
    			continue;  // we have already processed an example that covers all classes of the current
    		}
    		for(int j = 0; j < nbClasses; j++){
    			int ind = tp.getClass(j).getIndex();
    			if (! present[ind]){
    				present[ind] = true;
    				
    			}
    		}
    	}
    	return new boolean[][]{present, finalClasses, new boolean[]{nbFinal > 1}};
    }
    
    private static int mostSpecificTermIndex(ClassesTuple tp){
    	int ind = 0;
    	HashMap<Integer, Integer> termIndices = new HashMap<Integer, Integer>();
    	int nbClasses = tp.getNbClasses();
    	for(int i = 0; i < nbClasses; i++){
    		termIndices.put(tp.getClass(i).getIndex(), i);
    	}
    	boolean continueSearch = true;
    	while (continueSearch){
    		continueSearch = false;
    		ClassTerm term = tp.getClass(ind).getTerm();
    		if (term.atBottomLevel()){
    			return term.getIndex();
    		}
    		for(int i = 0; i < term.getNbChildren(); i++){
    			int j = ((ClassTerm) term.getChild(i)).getIndex();
    			if(termIndices.containsKey(j)){
    				ind = termIndices.get(j);
    				continueSearch = true;
    				break;
    			}
    		}
    	}
    	return tp.getClass(ind).getIndex();
    }
    
    private HashSet<ClassTerm> blabla(ClassHierarchy hier){
    	HashSet<ClassTerm> present = new HashSet<ClassTerm>();
    	ClassTerm current;
    	// We will not recurse:)
    	Stack<ClassTerm> stack = new Stack<ClassTerm>();
    	stack.push(hier.getRoot());
    	
    	return present;
    }
    
    

}


package clus.algo.Relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import javax.print.attribute.standard.PresentationDirection;

import clus.algo.Relief.distances.HierarchicalMultiLabelDistance;
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
    public static double BOTH_MISSING_DIST = 1.0;
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
    
    private HierarchicalMultiLabelDistance m_HierarDist = new HierarchicalMultiLabelDistance();
    


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
                if (m_DescriptiveTargetAttr[space][attrInd].isClasses()) {
                    ClassesAttrType attr = (ClassesAttrType) m_DescriptiveTargetAttr[space][attrInd];
                    m_HierarDist.processAttribute(attr, data);
                }
            }
        }

        // attribute relevance estimation
        double[] sumDistAttr = new double[m_NbDescriptiveAttrs];
        double sumDistTarget = 0.0;
        double[] sumDistAttrTarget = new double[m_NbDescriptiveAttrs];
        // per target relevance estimation
        boolean performPerTargetRanking = data.m_Schema.getSettings().shouldPerformRanking() && false;
        double[][] sumDistAttrPerTarget = new double[m_NbTargetAttrs][m_NbDescriptiveAttrs];
        double[] sumDistTargetPerTarget = new double[m_NbTargetAttrs];
        double[][] sumDistAttrTargetPerTarget = new double[m_NbTargetAttrs][m_NbDescriptiveAttrs];
                
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
                
                // overall ranking
                for (int targetValue = 0; targetValue < m_NbTargetValues; targetValue++) {
                    double tempSumDistTarget = 0.0;
                    double[] tempSumDistAttr = new double[m_NbDescriptiveAttrs];
                    double[] tempSumDistAttrTarget = new double[m_NbDescriptiveAttrs];
                    double sumNeighbourWeights = 0.0;
                    for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
                        sumNeighbourWeights += m_NeighbourWeights[neighbour];
                    }
                    for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
                    	double neighbourWeight = m_NeighbourWeights[neighbour] / sumNeighbourWeights;
                        if (!m_isStandardClassification) {
                            tempSumDistTarget += nearestNeighbours[targetValue][neighbour].getTargetDistance() * neighbourWeight;
                        }
                        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                            attr = m_DescriptiveTargetAttr[0][attrInd];
                            double distAttr = calcDistance1D(tuple, data.getTuple(nearestNeighbours[targetValue][neighbour].getIndexInDataset()), attr);
                            if (m_isStandardClassification) {
                                int tupleTarget = ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][0]).getNominal(tuple);
                                tempSumDistAttr[attrInd] += (targetValue == tupleTarget ? -distAttr : m_targetProbabilities[targetValue] / (1.0 - m_targetProbabilities[tupleTarget]) * distAttr) * neighbourWeight;
                            }
                            else {
                                tempSumDistAttr[attrInd] += distAttr * neighbourWeight;
                                tempSumDistAttrTarget[attrInd] += distAttr * nearestNeighbours[targetValue][neighbour].getTargetDistance() * neighbourWeight;
                            }
                        }                       
                    }
                    sumDistTarget += tempSumDistTarget;
                    for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                        sumDistAttr[attrInd] += tempSumDistAttr[attrInd];
                        sumDistAttrTarget[attrInd] += tempSumDistAttrTarget[attrInd];
                    }
                }
                
                if(performPerTargetRanking){
                	// per-target ranking - regression case: the neighbours are the same
                	for(int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++){
                        double tempSumDistTargetPerTarget = 0.0;
                        double[] tempSumDistAttrPerTarget = new double[m_NbDescriptiveAttrs];
                        double[] tempSumDistAttrTargetPerTarget = new double[m_NbDescriptiveAttrs];
                        double sumNeighbourWeights = 0.0;
                        // this for loop unnecessary
                        for (int neighbour = 0; neighbour < nearestNeighbours[0].length; neighbour++) {
                            sumNeighbourWeights += m_NeighbourWeights[neighbour];
                        }
                        for (int neighbour = 0; neighbour < nearestNeighbours[0].length; neighbour++) {
                        	double neighbourWeight = m_NeighbourWeights[neighbour] / sumNeighbourWeights;
                            tempSumDistTargetPerTarget += nearestNeighbours[0][neighbour].getPerTargetDistance(targetInd) * neighbourWeight;                        	
                            for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                                attr = m_DescriptiveTargetAttr[0][attrInd];
                                double distAttr = calcDistance1D(tuple, data.getTuple(nearestNeighbours[0][neighbour].getIndexInDataset()), attr);
                                tempSumDistAttrPerTarget[attrInd] += distAttr * neighbourWeight;
                                tempSumDistAttrTargetPerTarget[attrInd] += distAttr * nearestNeighbours[0][neighbour].getPerTargetDistance(targetInd) * neighbourWeight;
                            } 
                        }
                        sumDistTargetPerTarget[targetInd] += tempSumDistTargetPerTarget;
                        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                            sumDistAttrPerTarget[targetInd][attrInd] += tempSumDistAttrPerTarget[attrInd];
                            sumDistAttrTargetPerTarget[targetInd][attrInd] += tempSumDistAttrTargetPerTarget[attrInd];
                        }
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
            if(performPerTargetRanking){
            	for(int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++){
            		double p1 = sumDistAttrTargetPerTarget[targetInd][attrInd] / sumDistTargetPerTarget[targetInd];
            		double p2 = (sumDistAttrPerTarget[targetInd][attrInd] - sumDistAttrTargetPerTarget[targetInd][attrInd]) / (successfulItearions - sumDistTargetPerTarget[targetInd]);
            		info[2 + targetInd] += p1 - p2;
            	}
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
        else if (attr.isClasses()) {
            return calculateHierarchicalDist1D(t1, t2, (ClassesAttrType) attr);
        }
        else if (attr.isTimeSeries()) {
            return calculateTimeSeriesDist1D(t1, t2, (TimeSeriesAttrType) attr);
        }
        else if (attr.isString()) {
            return calculateStringDist1D(t1, t2, (StringAttrType) attr);
        }
        else {
            throw new ClusException("Unknown attribute type for attribute " + attr.getName() + ": " + attr.getClass().toString());
        }

    }


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
                t = BOTH_MISSING_DIST;
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
     * Calculates the distance between hierarchical sets of labels of data tuples t1 and t2.
     * @param t1
     * @param t2
     * @param attr
     * @return
     */
    public double calculateHierarchicalDist1D(DataTuple t1, DataTuple t2, ClassesAttrType attr){
    	return m_HierarDist.calculateDist(t1, t2, attr);
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
}

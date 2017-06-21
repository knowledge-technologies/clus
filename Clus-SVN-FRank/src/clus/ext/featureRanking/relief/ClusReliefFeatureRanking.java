
package clus.ext.featureRanking.relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.StringAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.featureRanking.ClusFeatureRanking;
import clus.ext.featureRanking.relief.distances.HierarchicalMultiLabelDistance;
import clus.ext.hierarchical.ClassesAttrType;
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
public class ClusReliefFeatureRanking extends ClusFeatureRanking {
	private static final int DESCRIPTIVE_SPACE = 0;
	private static final int TARGET_SPACE = 1;
	private static final int[] SPACE_TYPES = new int[]{DESCRIPTIVE_SPACE, TARGET_SPACE};
	
	private static final int DISTANCE_ATTR = 0;
	private static final int DISTANCE_TARGET = 1;
	private static final int DISTANCE_ATTR_TARGET = 2;
	private static final int[] DISTACNE_TYPES = new int[]{DISTANCE_ATTR, DISTANCE_TARGET, DISTANCE_ATTR_TARGET};

    /** Numbers of neighbours in the importances calculation */
    private int[] m_NbNeighbours;
    
    /** Maximal element of m_NbNeighbours */
    private int m_MaxNbNeighbours;
    
    /** Numbers of iterations in the importances calculation */
    private int[] m_NbIterations;
    
    /** Maximal element of m_NbIterations   */
    private int m_MaxNbIterations;
    
    /** Tells, whether the contributions of the neighbours are weighted */
    private boolean m_WeightNeighbours;
    
    /** {@code >= 0}, see {@link m_NeighbourWeights}, {@code m_Sigma == 0 <=> {@link #m_WeightNeighbours} == false} */
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
    
    /** 1 if we compute the ranking for the 'overall' target, and 1 + {@link #m_NbTargetAttrs} otherwise */
    private int m_NbGeneralisedTargetAttrs;

    /** {numericAttributeName: minimalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMins;
    
    /** {numericAttributeName: maximalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMaxs;
    
    /** number of examples in the data */
    private int m_NbExamples;
    
    /** distance in the case of missing values */
    public static double BOTH_MISSING_DIST = 1.0;

    /** Random generator for sampling of the next instance. It is used iff not {@link #m_isDeterministic} */
    private Random m_rnd;
    
    /** m_IsStandardClassification[index]: are we in the standard classification ({@code true}) or general (regression) case ({@code false}),
     * for the target with the index of {@code index - 1} if {@code index > 0}, and overall rankings otherwise. */
    private boolean[] m_IsStandardClassification;
    
    /** m_NbTargetValues[index]: if {link #m_IsStandardClassification}[index]: the number of classes, for the target with index {@code index},
     * otherwise: 1. */
    private int[] m_NbTargetValues;
        
    /** m_targetProbabilities[index]: relative frequencies of the target values, used in standard classification,
     * for the target with the index of {@code index - 1} if {@code index > 0}, and overall target otherwise.<br> If not {@link #m_IsStandardClassification}[index],
     * then m_targetProbabilities[index] = null. */
    private double[][] m_targetProbabilities;
        
    /** type of the time series distance */
    private int m_TimeSeriesDistance;
    
    /** tells, whether to perform per-target rankings also */
    private boolean m_PerformPerTargetRanking;
    
    /** m_SumDistAttr[target index][number of neighbours][attribute]: current sum of distances between attribute values, for the given number of neighbours and attribute.
     * If {@code target index} is 0, this is for overall ranking(s). Otherwise, this is for the ranking(s) for the target with the index {@code target index - 1}. */
    private double[][][] m_SumDistAttr;
    
    /** m_SumDistAttr[target index][number of neighbours]: for the given number of neighbours , this is an analogue of {@link #m_SumDistAttr} for
     * the distances between target values. */
    private double[][] m_SumDistTarget;
    
    /** m_SumDistAttr[target index][number of neighbours][attribute]: the analogue of {@link #m_SumDistAttr}, for the products of
     *  distances between attribute values and distances between target values */
    private double[][][] m_SumDistAttrTarget;        
        
    /** Hierarchical multi-label distance handler */
    private HierarchicalMultiLabelDistance m_HierarDist = new HierarchicalMultiLabelDistance();
    
    /** Counter for approximate proportion of the finished iterations. */
    private int m_Percents = 0;
    
    /** {@code m_NearestNeighbours[target index][tuple index]}: the nearest neighbours for a given tuple as returned by {@link #findNearestNeighbours(int, RowData, int, boolean)}.
     * If  {@code target index = 0}, these are the neighbours used in the computation of the overall ranking(s). Otherwise, these are the neighbours used in the computations of the ranking(s)
     * for target with the index {@code target index - 1}.
     */
    private HashMap<Integer, NearestNeighbour[][]>[] m_NearestNeighbours;


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
    public ClusReliefFeatureRanking(RowData data, int[] neighbours, int[] iterations, boolean weightNeighbours, double sigma, int seed) {
        super();
        m_NbNeighbours = neighbours;
        m_MaxNbNeighbours = m_NbNeighbours[m_NbNeighbours.length - 1];
        m_NbIterations = iterations;
        m_MaxNbIterations = m_NbIterations[m_NbIterations.length - 1];
        m_WeightNeighbours = weightNeighbours;
        m_Sigma = m_WeightNeighbours ? sigma : 0.0;
        m_NeighbourWeights = new double[m_MaxNbNeighbours];

        m_rnd = new Random(seed);
        initialize(data);
    }
    
    
    /**
     * Initialises some fields etc.
     * @param data
     */
    private void initialize(RowData data){
    	if(Settings.VERBOSE > 0){
    		System.out.println("Preprocessing steps ...");
    	}
        if (m_WeightNeighbours) {
            for (int neigh = 0; neigh < m_MaxNbNeighbours; neigh++) {
                m_NeighbourWeights[neigh] = Math.exp(-(m_Sigma * neigh) * (m_Sigma * neigh));
            }
        }
        else {
            Arrays.fill(m_NeighbourWeights, 1.0);
        }
    	
    	m_TimeSeriesDistance = data.m_Schema.getSettings().m_TimeSeriesDistance.getValue();
        setReliefDescription(m_NbNeighbours, m_NbIterations);
        m_NbExamples = data.getNbRows();

        // Initialise descriptive and target attributes if necessary
        int attrType;
        for (int space : SPACE_TYPES) {
            attrType = space == DESCRIPTIVE_SPACE ? ClusAttrType.ATTR_USE_DESCRIPTIVE : ClusAttrType.ATTR_USE_TARGET;
            if (m_DescriptiveTargetAttr[space] == null)
                m_DescriptiveTargetAttr[space] = data.m_Schema.getAllAttrUse(attrType);
        }
        m_NbDescriptiveAttrs = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE].length;
        m_NbTargetAttrs = m_DescriptiveTargetAttr[1].length;
        m_PerformPerTargetRanking = data.m_Schema.getSettings().shouldPerformRankingPerTarget();
        m_NbGeneralisedTargetAttrs = 1 + (m_PerformPerTargetRanking ? m_NbTargetAttrs : 0);
        setNbFeatureRankings();
        
        m_IsStandardClassification = computeStandardClassification();        
        m_NbTargetValues = nbTargetValues(data);
        m_targetProbabilities = new double[m_NbGeneralisedTargetAttrs][];
        for(int targetIndex = -1; targetIndex < m_targetProbabilities.length - 1; targetIndex++){
        	if(m_IsStandardClassification[targetIndex + 1]){
        		m_targetProbabilities[targetIndex + 1] = nominalClassCounts(data, targetIndex);
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
        
        // attribute relevance estimation: current statistics
        m_SumDistAttr = new double[m_NbGeneralisedTargetAttrs][m_NbNeighbours.length][m_NbDescriptiveAttrs];
        m_SumDistTarget = new double[m_NbGeneralisedTargetAttrs][m_NbNeighbours.length];
        m_SumDistAttrTarget = new double[m_NbGeneralisedTargetAttrs][m_NbNeighbours.length][m_NbDescriptiveAttrs];
    }
    
    
    /**
     * Computes and sets the number of feature rankings to be performed.
     */
    private void setNbFeatureRankings(){
    	ArrayList<String> rankings = new ArrayList<String>();
    	ArrayList<String> prefixes = new ArrayList<String>();
    	prefixes.add("overall");
    	if (m_PerformPerTargetRanking){
    		for(int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++){
    			prefixes.add(m_DescriptiveTargetAttr[TARGET_SPACE][targetInd].getName());
    		}
    	}
    	for(String prefix : prefixes){
			for(int iterInd = 0; iterInd < m_NbIterations.length; iterInd++){
				for(int neighInd = 0; neighInd < m_NbNeighbours.length; neighInd++){
	    			rankings.add(String.format("%sIter%dNeigh%d", prefix, m_NbIterations[iterInd], m_NbNeighbours[neighInd]));
	    		}
	    	}
    	}
    	setReliefFimpHeader(rankings);
    	setNbFeatureRankings(prefixes.size() * m_NbNeighbours.length * m_NbIterations.length);    	
    }
    
    /**
     * Returns the index of the (per-target) ranking, computed with specified number of iterations, neighbours and target index.
     * Greater or equal to zero.
     * @param iterationsIndex The index of the number of iterations in {@link #m_NbIterations}
     * @param neighboursIndex The index of the number of neighbours in {@link #m_NbNeighnours}
     * @param targetIndex If non-negative, then this is the index of the target and we are looking for a index of a per-target ranking.
     * If -1, then we are looking for the index of the overall ranking.
     * @return
     */
    private int rankingIndex(int iterationsIndex, int neighboursIndex, int targetIndex){
    	int perTargetShift = (targetIndex + 1) * m_NbIterations.length * m_NbNeighbours.length;
    	return perTargetShift + iterationsIndex * m_NbNeighbours.length + neighboursIndex;
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
        if(Settings.VERBOSE > 0){
        	System.out.println("Calculating importances ...");
        }
        DataTuple tuple;
        int tupleInd;
        int nbTargets = m_PerformPerTargetRanking ? 1 + m_NbTargetAttrs : 1;
        double[] successfulIterations = new double[nbTargets];
        
        int[] theOrder = randomPermutation(m_NbExamples);

        int insufficientNbNeighbours = 0;
        int numIterInd = 0;
        boolean[] shouldUpdate = new boolean[nbTargets]; // [overall] or [overall, target1, target2, ...]
        for (int iteration = 0; iteration < m_MaxNbIterations; iteration++) {
        	printProgress(iteration);	
            // CHOOSE TUPLE AND COMPUTE NEAREST NEIGHBOURS
            tupleInd = theOrder[iteration];
            tuple = data.getTuple(tupleInd);
            NearestNeighbour[][] nearestNeighboursOverall = findNearestNeighbours(tupleInd, data, -1);
            NearestNeighbour[][] nearestNeighbours = null;
            // UPDATE OVERALL (AND PER-TARGET) RANKING STATISTICS
            for(int targetIndex = -1; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++){
            	int trueIndex = getTrueTargetIndex(targetIndex);
            	if(!(m_IsStandardClassification[targetIndex + 1] && m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex].isMissing(tuple))){
            		successfulIterations[targetIndex + 1]++;
            		if(m_IsStandardClassification[targetIndex + 1] && targetIndex >= 0){  // STC that is not overall needs update of the neighbours
            			nearestNeighbours = findNearestNeighbours(tupleInd, data, targetIndex);
            		} else {
            			nearestNeighbours = nearestNeighboursOverall;
            		}
            		insufficientNbNeighbours += updateDistanceStatistics(data, tuple, nearestNeighbours, targetIndex);
            		shouldUpdate[targetIndex + 1] = true;
            	}
            }
            
            
//            
//            if (!(m_IsStandardClassification[0] && m_DescriptiveTargetAttr[TARGET_SPACE][0].isMissing(tuple))) {
//            	successfulIterations[0]++;
//                nearestNeighbours = findNearestNeighbours(tupleInd, data, -1);             
//                insufficientNbNeighbours += updateDistanceStatistics(data, tuple, nearestNeighbours, -1);
//                shouldUpdate[0] = true;
//                // neighboursFound = true;
//
//            }            
//            //  RANKING
//            if(m_PerformPerTargetRanking){
//            	for(int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++){
//            		if(!(m_IsStandardClassification[targetInd + 1] && m_DescriptiveTargetAttr[TARGET_SPACE][targetInd].isMissing(tuple))){
//            			successfulIterations[targetInd + 1]++;
//            			 // Condition here equivalent to ... || !neighboursFound, because: this target is regression ==> overall is regression
//            			NearestNeighbour[][] nearestNeighboursPerTarget = m_IsStandardClassification[targetInd + 1] ? findNearestNeighbours(tupleInd, data, targetInd) : nearestNeighbours;
//            			insufficientNbNeighbours += updateDistanceStatistics(data, tuple, nearestNeighboursPerTarget, targetInd);
//            			shouldUpdate[targetInd + 1] = true;
//            		}
//            	}
//            }
            // IMPORTANCE UPDATE
            if(iteration + 1 == m_NbIterations[numIterInd]){
            	updateImportances(data, numIterInd, successfulIterations, shouldUpdate);
            	numIterInd++;
            	shouldUpdate = new boolean[nbTargets];
            }
        }

        if(insufficientNbNeighbours > 0){
        	System.err.println("Maximal number of neighbours: " + m_MaxNbNeighbours);
        	System.err.println("Number of cases when we could not find that many neighbours: " + insufficientNbNeighbours);
        }
    }
    
    /**
     * Updates the importances of the attributes. It is called when the current number of iterations is the element of
     * {@link #m_NbIterations}. 
     * @param data The dataset under consideration.
     * @param numIterInd {@link #m_NbIterations}[numIterInd] equals the current number of iterations
     * @param successfulItearions {@code successfulItearions}[target + 1] equals the number of successful overall (if target = -1)
     * or per-target (for the target with index target) iterations.<p> Due to the missing values, some of these may not equal {@link #m_NbIterations}[numIterInd].
     * @param shouldUpdate Tells, whether the given overall/per-target ranking statistics should be updated. Has the same structure as {@code successfulItearions}.
     * @throws InterruptedException
     */
    private void updateImportances(RowData data, int numIterInd, double successfulItearions[], boolean[] shouldUpdate) throws InterruptedException{
    	for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
    		ClusAttrType attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][attrInd];
            double[] info = getAttributeInfo(attr.getName());
            for(int targetIndex = -1; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++){
            	if (shouldUpdate[targetIndex + 1]){
	            	boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
	            	for(int nbNeighInd = 0; nbNeighInd < m_NbNeighbours.length; nbNeighInd++){	                		
	            		int rankingInd = rankingIndex(numIterInd, nbNeighInd, targetIndex);
	            		
	            		double sumDistAttr = m_SumDistAttr[targetIndex + 1][nbNeighInd][attrInd];
	            		double sumDistTarget = m_SumDistTarget[targetIndex + 1][nbNeighInd];
	            		double sumDistAttrTarget = m_SumDistAttrTarget[targetIndex + 1][nbNeighInd][attrInd];
	            		
	                    if (isStdClassification) {
	                        info[2 + rankingInd] += sumDistAttr / successfulItearions[targetIndex + 1];
	                    }
	                    else {
	                    	double p1 = sumDistAttrTarget / sumDistTarget;
	                    	double p2 = (sumDistAttr - sumDistAttrTarget) / (successfulItearions[targetIndex + 1] - sumDistTarget); 
	                        info[2 + rankingInd] += p1 - p2;
	                    }
	            	}
	            }
            }
        	putAttributeInfo(attr.getName(), info);
    	}
    }
        
    /**
     * In each iteration, this method updates distance statistics after the neighbours of a chosen tuple are found.
     * @param data
     * @param tuple
     * @param nearestNeighbours
     * @param targetIndex
     * @param isPerTarget
     * @return
     * @throws ClusException
     */
    private int updateDistanceStatistics(RowData data, DataTuple tuple, NearestNeighbour[][] nearestNeighbours, int targetIndex) throws ClusException{
    	int tempInsufficientNbNeighbours = 0;
    	int nbTargetValues = m_NbTargetValues[targetIndex + 1];
        for (int targetValue = 0; targetValue < nbTargetValues; targetValue++) {
        	// The sums sum_neigh w_neigh * d, where w is non-normalised weight and d is d_class * d_attr or d_class etc. 
            double tempSumDistTarget = 0.0;
            double[] tempSumDistAttr = new double[m_NbDescriptiveAttrs];
            double[] tempSumDistAttrTarget = new double[m_NbDescriptiveAttrs];
            double sumNeighbourWeights = 0.0;
            
            boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
            int trueIndex = getTrueTargetIndex(targetIndex);
            int numNeighInd = 0;
            for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
            	if (nearestNeighbours[targetValue].length < m_MaxNbNeighbours){
            		tempInsufficientNbNeighbours++;
            	}
            	sumNeighbourWeights += m_NeighbourWeights[neighbour];
            	double neighWeightNonnormalized = m_NeighbourWeights[neighbour];
            	NearestNeighbour neigh = nearestNeighbours[targetValue][neighbour];
            	double targetDistance = 0.0;
            	// TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            	if (targetIndex >= 0 && !isStdClassification){ // <---> regression case, when we took the neighbours from overall ranking.
            		targetDistance = calcDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex]);
            	} else{
            		targetDistance = neigh.getTargetDistance();
            	}
            	
                if (!isStdClassification) {
                    tempSumDistTarget += targetDistance * neighWeightNonnormalized;
                }
                for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                	ClusAttrType attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][attrInd];
                    double distAttr = calcDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), attr) * neighWeightNonnormalized;        
                    if (isStdClassification) {
                    	int tupleTarget = ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex]).getNominal(tuple); 
                    	if (targetValue == tupleTarget){
                        	tempSumDistAttr[attrInd] -= distAttr;
                        }
                    	else{
                        	double pTupleTarget = m_targetProbabilities[targetIndex + 1][tupleTarget];
                            double pNeighTarget = m_targetProbabilities[targetIndex + 1][targetValue];
                            tempSumDistAttr[attrInd] += pNeighTarget / (1.0 - pTupleTarget) * distAttr; 
                        }
                    }
                    else {
                        tempSumDistAttr[attrInd] += distAttr;
                        tempSumDistAttrTarget[attrInd] += distAttr * targetDistance;
                    }
                }
                
                if(neighbour + 1 == m_NbNeighbours[numNeighInd]){
                	double normalizedTempDistTarget = tempSumDistTarget / sumNeighbourWeights;
                	m_SumDistTarget[targetIndex + 1][numNeighInd] += normalizedTempDistTarget;
                    for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                    	double normalizedTempDistAttr = tempSumDistAttr[attrInd] / sumNeighbourWeights;
                    	double normalizedTempTistAttrTarget = tempSumDistAttrTarget[attrInd] / sumNeighbourWeights;
                		m_SumDistAttr[targetIndex + 1][numNeighInd][attrInd] += normalizedTempDistAttr;
                		m_SumDistAttrTarget[targetIndex + 1][numNeighInd][attrInd] += normalizedTempTistAttrTarget;
                    }
                    numNeighInd++; // if numNeighInd == m_NbNeighbours.lenght, the neighbour for-loop has ended just now ... no index out of range
                }
            }
        }
        return tempInsufficientNbNeighbours;
    }
    
    
    private boolean[] computeStandardClassification(){
    	boolean[] isPerTarget = new boolean[m_NbGeneralisedTargetAttrs];
    	isPerTarget[0] = m_NbTargetAttrs == 1 && m_DescriptiveTargetAttr[TARGET_SPACE][0] instanceof NominalAttrType; // overall ranking
    	for(int targetIndex = 1; targetIndex < m_NbGeneralisedTargetAttrs; targetIndex++){
    		isPerTarget[targetIndex] = m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex - 1] instanceof NominalAttrType;
    	}
    	return isPerTarget;    	
    }
    
    private int[] nbTargetValues(RowData data){
    	int[] nbValues = new int[m_IsStandardClassification.length];
    	for(int targetIndex = 0; targetIndex < nbValues.length; targetIndex++){
    		nbValues[targetIndex] = m_IsStandardClassification[targetIndex] ? ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex - 1]).getNbValues() : 1;
    	}
    	return nbValues;
    }
    
    /**
     * Computes normalised counts of each class value, for a given target.
     * @param data
     * @param nominalTargetIndex if -1, this is the overall ranking, hence STC is the task at hand. Otherwise, we are computing statistics
     * that are used in per-target ranking.
     * @return A list with number of examples with a particular value of the target. In the last place, we have the number of examples
     * whose value of the target is missing. The counts for non-missing values are normalised by the number of examples with non-missing value for the target.
     */
    private double[] nominalClassCounts(RowData data, int nominalTargetIndex){
    	int nbValues = m_NbTargetValues[nominalTargetIndex + 1];
        double[] targetProbabilities = new double[nbValues + 1]; // one additional place for missing values
        int trueIndex = m_IsStandardClassification[0] ? nominalTargetIndex + 1 : nominalTargetIndex;
        NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex];
        for (int example = 0; example < m_NbExamples; example++) {
            targetProbabilities[attr.getNominal(data.getTuple(example))] += 1.0;
        }
        if (m_NbExamples + MathUtil.C1E_9 > targetProbabilities[nbValues]) { // otherwise: targetProbabilities = {0, 0, ... , 0, m_NbExamples}
            // Normalise probabilities: examples with unknown targets are effectively ignored
            // The formula for standard classification class weighting still holds, i.e., sum over other classes of
            // the terms P(other class) / (1 - P(class)), equals 1
            for (int value = 0; value < nbValues; value++) {
                targetProbabilities[value] /= m_NbExamples - targetProbabilities[nbValues];
            }
        }
        return targetProbabilities;
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
    public NearestNeighbour[][] findNearestNeighbours(int tupleInd, RowData data, int targetIndex) throws ClusException {
        DataTuple tuple = data.getTuple(tupleInd);
        boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
        int nbTargetValues = m_NbTargetValues[targetIndex + 1];
        int trueIndex = m_IsStandardClassification[0] ? targetIndex + 1 : targetIndex;  // -1 ---> 0 in the case of STC
        
        int[][] neighbours = new int[nbTargetValues][m_MaxNbNeighbours]; // current candidates
        double[] distances = new double[m_NbExamples]; // distances[i] = distance(tuple, data.getTuple(i))
        int[] whereToPlaceNeigh = new int[nbTargetValues];
        int targetValue;        

        for (int i = 0; i < m_NbExamples; i++) {
            distances[i] = calcDistance(tuple, data.getTuple(i), DESCRIPTIVE_SPACE);
        }
        boolean sortingNeeded;
        boolean isSorted[] = new boolean[nbTargetValues]; // isSorted[target value]: tells whether the neighbours for target value are sorted
        for (int i = 0; i < m_NbExamples; i++) {
            sortingNeeded = false;
            if (i != tupleInd) {
                targetValue = isStdClassification ? m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex].getNominal(data.getTuple(i)) : 0;
                if (targetValue < nbTargetValues) { // non-missing
                    if (whereToPlaceNeigh[targetValue] < m_MaxNbNeighbours) {
                        neighbours[targetValue][whereToPlaceNeigh[targetValue]] = i;
                        whereToPlaceNeigh[targetValue]++;
                        if (whereToPlaceNeigh[targetValue] == m_MaxNbNeighbours) { // the list of neighbours has just
                                                                                // become full ---> sort it
                            for (int ind1 = 0; ind1 < m_MaxNbNeighbours; ind1++) { // O(NbNeighbours^2) ...
                                for (int ind2 = ind1 + 1; ind2 < m_MaxNbNeighbours; ind2++) {
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
                    for (j = 1; j < m_MaxNbNeighbours && distances[i] < distances[neighbours[targetValue][j]]; j++) {
                        neighbours[targetValue][j - 1] = neighbours[targetValue][j];
                    }
                    neighbours[targetValue][j - 1] = i;
                    isSorted[targetValue] = true;
                }

            }
        }
        NearestNeighbour[][] nearestNeighbours = new NearestNeighbour[nbTargetValues][];
        for (int value = 0; value < nbTargetValues; value++) {
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
            	int datasetIndex = neighbours[value][i];
            	double descriptiveSpaceDist = distances[neighbours[value][i]];
            	double targetSpaceDist = 0.0;
            	boolean isPerTarget = targetIndex >= 0;
            	if(isPerTarget){
            		targetSpaceDist = calcDistance1D(tuple, data.getTuple(datasetIndex), m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex]);
            	} else{
            		targetSpaceDist = calcDistance(tuple, data.getTuple(datasetIndex), TARGET_SPACE);
            	}
                nearestNeighbours[value][whereToPlaceNeigh[value] - i - 1] = new NearestNeighbour(datasetIndex, descriptiveSpaceDist, targetSpaceDist);
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
     * Computes a random permutation with Fisher–Yates algorithm.
     * 
     * @param examples The number of examples that we will place in a random order.     * 
     * @return A permutation given as a list whose i-th element is the index of the example
     * that is processed in i-th iteration. 
     */
    private int[] randomPermutation(int examples) {
    	int[] permuted = new int[examples];
    	// fill
    	for(int i = 0; i < permuted.length; i++){
    		permuted[i] = i;
    	}
    	// shuffle
	    for (int i = permuted.length - 1; i > 0; i--) {
	      int ind = m_rnd.nextInt(i + 1);
	      int temp = permuted[ind];
	      permuted[ind] = permuted[i];
	      permuted[i] = temp;
	    }
	    return permuted;
    }
   
    public void setReliefFimpHeader(ArrayList<String> names){
    	setFimpHeader(fimpTableHeader(names));    	
    }
    
    public void setReliefDescription(int[] neighbours, int[] iterations) {
    	String first = "Ranking method: Relief with all combinations of";
    	String second = String.format("numbers of neighbours: %s", Arrays.toString(neighbours));
    	String third = String.format("numbers of iterations: %s", Arrays.toString(iterations));
        setRankingDescription(String.join("\n", new String[]{first, second, third}));
    }
    
    
    private void printProgress(int iteration){
    	double proportion = 100 * (double)(iteration + 1) / ((double) m_MaxNbIterations); 
    	if(Settings.VERBOSE > 0 && Settings.VERBOSE < 3){
    		while(m_Percents < proportion && m_Percents < 100){
    			System.out.print(".");
    			m_Percents++;
    			if(m_Percents / 10 * 10 == m_Percents){
    				System.out.println(String.format(" %3d percents", m_Percents));
    			}
    		}
    	} else if (Settings.VERBOSE > 4){
    		System.out.println("iteration " + iteration);
    	}
    }
    
    /**
     * Converts {@code -1 <= target index <} {@link #m_NbTargetAttrs} to the index that corresponds to given
     * target in the {@link #m_DescriptiveTargetAttr}[{@link #TARGET_SPACE}]
     * @param targetIndex
     * @return
     */
    private int getTrueTargetIndex(int targetIndex){
    	if(m_IsStandardClassification[0]){
    		return targetIndex + 1;
    	} else{
    		return targetIndex;
    	}
    }
}

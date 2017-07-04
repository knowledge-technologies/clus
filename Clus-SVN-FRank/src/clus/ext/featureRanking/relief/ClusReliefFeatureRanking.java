
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
import clus.main.settings.Settings;
import clus.main.settings.SettingsTimeSeries;
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

    /** {numericAttributeName: minimalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMins;
    
    /** {numericAttributeName: maximalValueOfTheAttribute, ...} */
    private HashMap<String, Double> m_numMaxs;
    
    /** number of examples in the data */
    private int m_NbExamples;
    
    /** distance in the case of missing values */
    public static double BOTH_MISSING_DIST = 1.0;

    /** Random generator for sampling of the next instance. It is used iff not m_isDeterministic */
    private Random m_rnd;
    
    /** standard classification or general (regression) case */
    private boolean m_isStandardClassification;
    
    /** standard classification or general (regression) case for per-target rankings */
    private boolean[] m_isStandardClassificationPerTarget;
    
    /** number of target values: if m_isStandardClassification: the number of classes, else: 1 */
    private int m_NbTargetValues;
    
    /** number of target values per target: if {@link #m_isStandardClassificationPerTarget}[target]: the number of classes, else: 1 */
    private int[] m_NbTargetValuesPerTarget;
    
    /** relative frequencies of the target values, used in standard classification */
    private double[] m_targetProbabilities;
    
    /** relative frequencies of the target values, used in per-target standard classification */
    private double[][] m_targetProbabilitiesPerTarget;
    
    /** type of the time series distance */
    private int m_TimeSeriesDistance;
    
    /** tells, whether to perform per-target rankings also */
    private boolean m_performPerTargetRanking;
    
    /** m_SumDistAttr[number of neighbours][attribute]: current sum of distances between attribute values, for the given number of neighbours and attribute */
    private double[][] m_SumDistAttr;
    
    /** m_SumDistAttr[number of neighbours]: current sum of distances between target values, for the given number of neighbours */
    private double[] m_SumDistTarget;
    
    /** m_SumDistAttr[number of neighbours][attribute]: current sum of products of distances between attribute values and distances between target values,
     * for the given number of neighbours and attribute */
    private double[][] m_SumDistAttrTarget;        
    
    /** per-target analogue of {@link #m_SumDistAttr} */     
    private double[][][] m_SumDistAttrPerTarget;
    
    /** per-target analogue of {@link #m_SumDistTarget} */
    private double[][] m_SumDistTargetPerTarget;
    
    /** per-target analogue of {@link #m_SumDistAttrTarget} */
    private double[][][] m_SumDistAttrTargetPerTarget;
    
    private HierarchicalMultiLabelDistance m_HierarDist = new HierarchicalMultiLabelDistance();
    
    private int m_Percents = 0;
    


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
    public ClusReliefFeatureRanking(RowData data, int[] neighbours, int[] iterations, boolean weightNeighbours, double sigma, int seed, Settings sett) {
        super(sett);
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
     * Computes and sets the number of feature rankings to be performed.
     */
    private void setNbFeatureRankings(){
    	ArrayList<String> rankings = new ArrayList<String>();
    	ArrayList<String> prefixes = new ArrayList<String>();
    	prefixes.add("overall");
    	if (m_performPerTargetRanking){
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
        if(getSettings().getGeneric().getVerbose() > 0){
        	System.out.println("Calculating importances ...");
        }
        DataTuple tuple;
        int tupleInd;
        int nbTargets = m_performPerTargetRanking ? 1 + m_NbTargetAttrs : 1;
        double[] successfulIterations = new double[nbTargets];
        
        int[] theOrder = randomPermutation(m_NbExamples);

        int insufficientNbNeighbours = 0;
        int numIterInd = 0;
        boolean[] shouldUpdate = new boolean[nbTargets]; // [overall] or [overall, target1, target2, ...]
        for (int iteration = 0; iteration < m_MaxNbIterations; iteration++) {
        	double proportion = 100 * (double)(iteration + 1) / ((double) m_MaxNbIterations); 
        	if(getSettings().getGeneric().getVerbose() > 0 && getSettings().getGeneric().getVerbose() < 3){
        		while(m_Percents < proportion && m_Percents < 100){
        			System.out.print(".");
        			m_Percents++;
        			if(m_Percents / 10 * 10 == m_Percents){
        				System.out.println(String.format(" %3d percents", m_Percents));
        			}
        		}
        	} else if (getSettings().getGeneric().getVerbose() > 4){
        		System.out.println("iteration " + iteration);
        	}
        	
            // CHOOSE TUPLE AND COMPUTE NEAREST NEIGHBOURS
            tupleInd = theOrder[iteration];
            tuple = data.getTuple(tupleInd);
            // boolean neighboursFound = false;
            
            NearestNeighbour[][] nearestNeighbours = new NearestNeighbour[0][0]; // only to prevent 'is never initialised' error
            // OVERALL RANKING
            if (!(m_isStandardClassification && m_DescriptiveTargetAttr[TARGET_SPACE][0].isMissing(tuple))) {
            	successfulIterations[0]++;
                nearestNeighbours = findNearestNeighbours(tupleInd, data, 0, false);             
                insufficientNbNeighbours += updateDistanceStatistics(data, tuple, nearestNeighbours, 0, false);
                shouldUpdate[0] = true;
                // neighboursFound = true;

            }            
            // PER-TARGET RANKING
            if(m_performPerTargetRanking){
            	for(int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++){
            		if(!(m_isStandardClassificationPerTarget[targetInd] && m_DescriptiveTargetAttr[TARGET_SPACE][targetInd].isMissing(tuple))){
            			successfulIterations[targetInd + 1]++;
            			 // Condition here equivalent to ... || !neighboursFound, because: this target is regression ==> overall is regression
            			NearestNeighbour[][] nearestNeighboursPerTarget = m_isStandardClassificationPerTarget[targetInd] ? findNearestNeighbours(tupleInd, data, targetInd, true) : nearestNeighbours;
            			insufficientNbNeighbours += updateDistanceStatistics(data, tuple, nearestNeighboursPerTarget, targetInd, true);
            			shouldUpdate[targetInd + 1] = true;
            		}
            	}
            }
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
            int upperBound = m_performPerTargetRanking ? m_NbTargetAttrs : 0;
            for(int targetIndex = -1; targetIndex < upperBound; targetIndex++){
            	if (shouldUpdate[targetIndex + 1]){
	            	boolean isPerTarget = targetIndex >= 0;
	            	boolean isStdClassification = getIsStandardClassification(targetIndex, isPerTarget);
	            	for(int neighInd = 0; neighInd < m_NbNeighbours.length; neighInd++){	                		
	            		int rankingInd = rankingIndex(numIterInd, neighInd, targetIndex);
	            		
	            		double sumDistAttr = getDistanceStatistic(DISTANCE_ATTR, attrInd, neighInd, targetIndex, isPerTarget);
	            		double sumDistTarget = getDistanceStatistic(DISTANCE_TARGET, attrInd, neighInd, targetIndex, isPerTarget);
	            		double sumDistAttrTarget = getDistanceStatistic(DISTANCE_ATTR_TARGET, attrInd, neighInd, targetIndex, isPerTarget);
	            		
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
     * Returns the given type of distance statistics for the given attribute, number of neighbours, target index (needed only when isPerTarget is true)
     * @param type an element of {@link #DISTACNE_TYPES}.
     * @param attrIndex
     * @param nbNeighboursIndex
     * @param targetIndex
     * @param isPerTarget
     * @return
     */
    private double getDistanceStatistic(int type, int attrIndex, int nbNeighboursIndex, int targetIndex, boolean isPerTarget){
    	double ans = 0.0;
    	switch(type){
    	case DISTANCE_ATTR:
    		ans = isPerTarget ? m_SumDistAttrPerTarget[targetIndex][nbNeighboursIndex][attrIndex] : m_SumDistAttr[nbNeighboursIndex][attrIndex];
    		break;
    	case DISTANCE_TARGET:
    		ans = isPerTarget ? m_SumDistTargetPerTarget[targetIndex][nbNeighboursIndex] : m_SumDistTarget[nbNeighboursIndex];
    		break;
    	case DISTANCE_ATTR_TARGET:
    		ans = isPerTarget ? m_SumDistAttrTargetPerTarget[targetIndex][nbNeighboursIndex][attrIndex] : m_SumDistAttrTarget[nbNeighboursIndex][attrIndex];
    		break;
    	default:
    		throw new RuntimeException(String.format("The specified statistics type %s is not the element of allowed types %s.", type, Arrays.toString(DISTACNE_TYPES)));    			
    	}
    	return ans;
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
    private int updateDistanceStatistics(RowData data, DataTuple tuple, NearestNeighbour[][] nearestNeighbours, int targetIndex, boolean isPerTarget) throws ClusException{
    	int tempInsufficientNbNeighbours = 0;
    	int nbTargetValues = isPerTarget ? m_NbTargetValuesPerTarget[targetIndex] : m_NbTargetValues;
        for (int targetValue = 0; targetValue < nbTargetValues; targetValue++) {
        	// The sums sum_neigh w_neigh * d, where w is non-normalised weight and d is d_class * d_attr or d_class etc. 
            double tempSumDistTarget = 0.0;
            double[] tempSumDistAttr = new double[m_NbDescriptiveAttrs];
            double[] tempSumDistAttrTarget = new double[m_NbDescriptiveAttrs];
            double sumNeighbourWeights = 0.0;
            
            boolean isStdClassification = getIsStandardClassification(targetIndex, isPerTarget);            
            int numNeighInd = 0;
            for (int neighbour = 0; neighbour < nearestNeighbours[targetValue].length; neighbour++) {
            	if (nearestNeighbours[targetValue].length < m_MaxNbNeighbours){
            		tempInsufficientNbNeighbours++;
            	}
            	sumNeighbourWeights += m_NeighbourWeights[neighbour];
            	double neighWeightNonnormalized = m_NeighbourWeights[neighbour];
            	NearestNeighbour neigh = nearestNeighbours[targetValue][neighbour];
            	double targetDistance = 0.0;
            	if (isPerTarget && !isStdClassification){ // <---> regression case, when we took the neighbours from overall ranking
            		targetDistance = calcDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex]);
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
                    	int tupleTarget = ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex]).getNominal(tuple); 
                    	if (targetValue == tupleTarget){
                        	tempSumDistAttr[attrInd] -= distAttr;
                        }
                    	else{
                        	double pTupleTarget = isPerTarget ? m_targetProbabilitiesPerTarget[targetIndex][tupleTarget] : m_targetProbabilities[tupleTarget];
                            double pNeighTarget = isPerTarget ? m_targetProbabilitiesPerTarget[targetIndex][targetValue] : m_targetProbabilities[targetValue];
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
                	if (isPerTarget){
                		m_SumDistTargetPerTarget[targetIndex][numNeighInd] += normalizedTempDistTarget;
                	}
                	else{
                		m_SumDistTarget[numNeighInd] += normalizedTempDistTarget;
                	}
                    for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
                    	double normalizedTempDistAttr = tempSumDistAttr[attrInd] / sumNeighbourWeights;
                    	double normalizedTempTistAttrTarget = tempSumDistAttrTarget[attrInd] / sumNeighbourWeights;
                    	if (isPerTarget){
                    		m_SumDistAttrPerTarget[targetIndex][numNeighInd][attrInd] += normalizedTempDistAttr;
                    		m_SumDistAttrTargetPerTarget[targetIndex][numNeighInd][attrInd] += normalizedTempTistAttrTarget;
                    	}
                    	else{
                    		m_SumDistAttr[numNeighInd][attrInd] += normalizedTempDistAttr;
                    		m_SumDistAttrTarget[numNeighInd][attrInd] += normalizedTempTistAttrTarget;
                    	}
                        
                    }
                    numNeighInd++; // if numNeighInd == m_NbNeighbours.lenght, the neighbour for-loop has ended just now ... no index out of range
                }
            }
        }
        return tempInsufficientNbNeighbours;
    }
    
    /**
     * Initialises some fields etc.
     * @param data
     */
    private void initialize(RowData data){
    	if(getSettings().getGeneric().getVerbose() > 0){
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
    	
    	m_TimeSeriesDistance = getSettings().getTimeSeries().getTimeSeriesDistance();
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
        m_performPerTargetRanking = data.m_Schema.getSettings().getEnsemble().shouldPerformRankingPerTarget();
        setNbFeatureRankings();
        
        m_isStandardClassification = computeStandardClassification(0, false);        
        m_NbTargetValues = nbTargetValues(data, 0, false);
        if (m_isStandardClassification) {
            m_targetProbabilities = nominalClassCounts(data, 0, false);
        }
        
        
        if(m_performPerTargetRanking){
        	m_isStandardClassificationPerTarget = new boolean[m_NbTargetAttrs];
        	m_NbTargetValuesPerTarget = new int[m_NbTargetAttrs];
        	m_targetProbabilitiesPerTarget = new double[m_NbTargetAttrs][];
        	for(int targetIndex = 0; targetIndex < m_NbTargetAttrs; targetIndex++){
        		m_isStandardClassificationPerTarget[targetIndex] = computeStandardClassification(targetIndex, true);
        		m_NbTargetValuesPerTarget[targetIndex] = nbTargetValues(data, targetIndex, true);
        		if(m_isStandardClassificationPerTarget[targetIndex]){
        			m_targetProbabilitiesPerTarget[targetIndex] = nominalClassCounts(data, targetIndex, true);
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
        
        // attribute relevance estimation: current statistics
        m_SumDistAttr = new double[m_NbNeighbours.length][m_NbDescriptiveAttrs];
        m_SumDistTarget = new double[m_NbNeighbours.length];
        m_SumDistAttrTarget = new double[m_NbNeighbours.length][m_NbDescriptiveAttrs];        
        
        // per target relevance estimation        
        m_SumDistAttrPerTarget = new double[m_NbTargetAttrs][m_NbNeighbours.length][m_NbDescriptiveAttrs];
        m_SumDistTargetPerTarget = new double[m_NbTargetAttrs][m_NbNeighbours.length];
        m_SumDistAttrTargetPerTarget = new double[m_NbTargetAttrs][m_NbNeighbours.length][m_NbDescriptiveAttrs];

    }
    
    private boolean computeStandardClassification(int targetIndex, boolean isPerTarget){
    	boolean isNominal = m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex] instanceof NominalAttrType;
    	return isPerTarget ? isNominal : isNominal && m_NbTargetAttrs == 1;
    }
    
    private boolean getIsStandardClassification(int targetIndex, boolean isPerTarget){
    	return isPerTarget ? m_isStandardClassificationPerTarget[targetIndex] : m_isStandardClassification;
    }
    
    private int nbTargetValues(RowData data, int targetIndex, boolean isPerTarget){
    	boolean condition = isPerTarget ? m_isStandardClassificationPerTarget[targetIndex] : m_isStandardClassification; 
    	return condition ? ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex]).getNbValues() : 1;
    }
    
    private double[] nominalClassCounts(RowData data, int nominalTargetIndex, boolean isPerTarget){
    	int nbValues = isPerTarget ? m_NbTargetValuesPerTarget[nominalTargetIndex] : m_NbTargetValues;
        double[] targetProbabilities = new double[nbValues + 1]; // one additional place for missing values
        NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][nominalTargetIndex];
        for (int example = 0; example < m_NbExamples; example++) {
            targetProbabilities[attr.getNominal(data.getTuple(example))] += 1.0;
        }
        if (m_NbExamples > targetProbabilities[nbValues]) { // otherwise: targetProbabilities = {0, 0, ... , 0, m_NbExamples}
            // Normalise probabilities: examples with unknown targets are ignored
            // The formula for standard classification class weighting still holds, i.e. sum over other classes of
            // P(other class) / (1 - P(class)) equals 1
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
    public NearestNeighbour[][] findNearestNeighbours(int tupleInd, RowData data, int targetIndex, boolean isPerTarget) throws ClusException {
        DataTuple tuple = data.getTuple(tupleInd);
        boolean isStdClassification = isPerTarget ? m_isStandardClassificationPerTarget[targetIndex] : m_isStandardClassification;
        int nbTargetValues = isPerTarget ? m_NbTargetValuesPerTarget[targetIndex] : m_NbTargetValues;
        
        int[][] neighbours = new int[nbTargetValues][m_MaxNbNeighbours]; // current candidates
        double[] distances = new double[m_NbExamples]; // distances[i] = distance(tuple, data.getTuple(i))
        int[] whereToPlaceNeigh = new int[nbTargetValues];
        int targetValue;        

        for (int i = 0; i < m_NbExamples; i++) {
            distances[i] = calcDistance(tuple, data.getTuple(i), DESCRIPTIVE_SPACE); // in descriptive space
        }
        boolean sortingNeeded;
        boolean isSorted[] = new boolean[nbTargetValues]; // isSorted[target value]: tells whether the neighbours for target value are sorted
        for (int i = 0; i < m_NbExamples; i++) {
            sortingNeeded = false;
            if (i != tupleInd) {
                targetValue = isStdClassification ? m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex].getNominal(data.getTuple(i)) : 0;
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
            	if(isPerTarget){
            		targetSpaceDist = calcDistance1D(tuple, data.getTuple(datasetIndex), m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex]);
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
            case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_DTW:
                return new DTWTimeSeriesDist(attr).calcDistance(t1, t2);
            case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_QDM:
                if (ts1.length() == ts2.length()) {
                    return new QDMTimeSeriesDist(attr).calcDistance(t1, t2);
                }
                else {
                    throw new ClusException("QDM Distance is not implemented for time series with different length");
                }
            case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_TSC:
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
}

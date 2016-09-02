package clus.algo.Relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.StringAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.ensembles.ClusEnsembleFeatureRanking;
import clus.ext.timeseries.DTWTimeSeriesDist;
import clus.ext.timeseries.QDMTimeSeriesDist;
import clus.ext.timeseries.TSCTimeSeriesDist;
import clus.ext.timeseries.TimeSeries;
import clus.main.Settings;
import clus.util.ClusException;

public class ClusReliefFeatureRanking extends ClusEnsembleFeatureRanking{
	private int m_NbNeighbours;
	private int m_NbIterations;

	
	private ClusAttrType[][] m_DescriptiveTargetAttr = new ClusAttrType[2][]; 
	
	private int m_NbDescriptiveAttrs, m_NbTargetAttrs;
	
	
	private HashMap<String, Double> m_numMins, m_numMaxs;	// min and max for each numeric attribute
	private int m_NbExamples;	
	double m_oneMissing = 0.5;								// distances in the case of missing values
	double m_bothMissing = 1.0;	
	boolean m_isDeterministic;								// next instance generator (if m_NbIterations < m_NbExamples)
	Random m_rnd = new Random(1234);
	boolean m_isStandardClassification;						// standard classification or general case
	int m_NbTargetValues;									// number of target values: if m_isStandardClassification: self explanatory, else: = 1
	double[] m_targetProbabilities;
	
	int m_TimeSeriesDistance;
	
	boolean debug = false;
	

	
	public ClusReliefFeatureRanking(int neighbours, int iterations){
		super();
		this.m_NbNeighbours = neighbours;
		this.m_NbIterations = iterations;
	}
	
	public void calculateReliefImportance(RowData data) throws ClusException {
		m_TimeSeriesDistance = data.m_Schema.getSettings().m_TimeSeriesDistance.getValue();		
		setReliefDescription(m_NbNeighbours, m_NbIterations);
		m_NbExamples = data.getNbRows();
		m_isDeterministic = m_NbExamples == m_NbIterations;
		
		// initialize descriptive and target attributes if necessary
		int attrType;
		for(int space = 0; space < 2; space++){
			attrType = space == 0 ? ClusAttrType.ATTR_USE_DESCRIPTIVE : ClusAttrType.ATTR_USE_TARGET;
			if(m_DescriptiveTargetAttr[space] == null) m_DescriptiveTargetAttr[space] = data.m_Schema.getAllAttrUse(attrType);
		}
		m_NbDescriptiveAttrs = m_DescriptiveTargetAttr[0].length;
		m_NbTargetAttrs =  m_DescriptiveTargetAttr[1].length;
		m_isStandardClassification = m_NbTargetAttrs == 1 && m_DescriptiveTargetAttr[1][0] instanceof NominalAttrType;
		m_NbTargetValues = m_isStandardClassification ? ((NominalAttrType) m_DescriptiveTargetAttr[1][0]).getNbValues() : 1;
		
		// class counts
		if(m_isStandardClassification){
			m_targetProbabilities = new double[m_NbTargetValues + 1]; // one additional place for missing values
			NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[1][0];
			for(int example = 0; example < m_NbExamples; example++){
				m_targetProbabilities[attr.getNominal(data.getTuple(example))] += 1.0;
			}
			for(int value = 0; value < m_NbTargetValues; value++){
				m_targetProbabilities[value] /= m_NbExamples;
			}
		}
		
		// compute min and max of numeric attributes	
		m_numMins = new HashMap<String, Double>();
		m_numMaxs = new HashMap<String, Double>();
		double value;
		String attrName;
		for(int space = 0; space < 2; space++){
			if(space == 0) attrType = ClusAttrType.ATTR_USE_DESCRIPTIVE;
			else attrType = ClusAttrType.ATTR_USE_TARGET;
			for(NumericAttrType numAttr : data.m_Schema.getNumericAttrUse(attrType)){
				attrName = numAttr.getName();
				m_numMins.put(attrName, Double.POSITIVE_INFINITY);
				m_numMaxs.put(attrName, Double.NEGATIVE_INFINITY);
				for(int example = 0; example < m_NbExamples; example++){
					value = numAttr.getNumeric(data.getTuple(example));
					if(value < m_numMins.get(attrName)) m_numMins.put(attrName, value);
					if(value > m_numMaxs.get(attrName)) m_numMaxs.put(attrName, value);
				}
			}			
		}
		System.out.println("min: " + m_numMins);
		System.out.println("max: " + m_numMaxs);
		
		System.out.println("attrs: " + Arrays.deepToString(m_DescriptiveTargetAttr));

		// attribute relevance
		double[] sumDistAttr = new double[m_NbDescriptiveAttrs];
		double sumDistTarget = 0.0;
		double[] sumDistAttrTarget = new double[m_NbDescriptiveAttrs];
		DataTuple tuple;
		int tupleInd;
		NearestNeighbour[][] nearestNeighbours;
		ClusAttrType attr;
		for(int iteration = 0; iteration < m_NbIterations; iteration++){
			// CHOOSE TUPLE AND COMPUTE NEAREST NEIGHBOURS
			tupleInd = nextInstance(iteration);
			tuple = data.getTuple(tupleInd);
			if(debug)System.out.println("Tuple: " + tuple.toString());
			nearestNeighbours = findNearestNeighbours(tupleInd, data);
			// CALCULATE IMPORTANCES
			for(int targetValue = 0; targetValue < m_NbTargetValues; targetValue++){
				for(int neighbour = 0; neighbour < m_NbNeighbours; neighbour++){
					if(!m_isStandardClassification){
						sumDistTarget += nearestNeighbours[targetValue][neighbour].m_targetDistance;
					}
					for(int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++){
						attr = m_DescriptiveTargetAttr[0][attrInd];
						double distAttr = calcDistance1D(tuple, data.getTuple(nearestNeighbours[targetValue][neighbour].m_indexInDataSet), attr);						
						if(m_isStandardClassification){
							int tupleTarget = ((NominalAttrType) m_DescriptiveTargetAttr[1][0]).getNominal(tuple);
							sumDistAttr[attrInd] += targetValue == tupleTarget ? -distAttr: m_targetProbabilities[targetValue] / (1.0 - m_targetProbabilities[tupleTarget]) * distAttr;							
						} else{							
							sumDistAttr[attrInd] += distAttr;
							sumDistAttrTarget[attrInd] += distAttr * nearestNeighbours[targetValue][neighbour].m_targetDistance;
						}	
					}
				}
			}
		}
		// UPDATE IMPORTANCES
		for(int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++){
			attr = m_DescriptiveTargetAttr[0][attrInd];
			double [] info = getAttributeInfo(attr.getName());
			if(m_isStandardClassification){
				info[2] += sumDistAttr[attrInd] / (m_NbNeighbours * m_NbIterations);
			} else{
				info[2] += sumDistAttrTarget[attrInd] / sumDistTarget - (sumDistAttr[attrInd] - sumDistAttrTarget[attrInd]) / (m_NbNeighbours * m_NbIterations - sumDistTarget);
			}
			putAttributeInfo(attr.getName(), info);
		}		
	}
	
	/**
	 * Computes the nearest neighbours of example with index {@code tupleInd} in the dataset {@code data}.
	 * @param tupleInd
	 * @param data
	 * @return
	 * @throws ClusException
	 */
	public NearestNeighbour[][] findNearestNeighbours(int tupleInd, RowData data) throws ClusException{
		DataTuple tuple = data.getTuple(tupleInd);
		int[][] neighbours = new int[m_NbTargetValues][m_NbNeighbours]; 		// current candidates
		double[] distances = new double[m_NbExamples];							// distances[i] = distance(tuple, data.getTuple(i))	TODO: mal prostora porabmo, ce je pdoatk. mnozica stevilcna ...
		int[] whereToPlaceNeigh = new int[m_NbTargetValues];
		int targetValue;

		for(int i = 0; i < m_NbExamples;i++){
			distances[i] = calcDistance(tuple, data.getTuple(i), 0); // descriptive space
		}
		if(debug){
			System.out.println("  scores: " + Arrays.toString(distances));
		}
		boolean sortingNeeded;
	    for (int i = 0; i < m_NbExamples; i++){
	    	sortingNeeded = false;
	        if(i != tupleInd){
	        	targetValue = m_isStandardClassification ? m_DescriptiveTargetAttr[1][0].getNominal(data.getTuple(i)) : 0;
      		
        		if(targetValue < m_NbTargetValues){ // non-missing
        			if (whereToPlaceNeigh[targetValue] < m_NbNeighbours){
	        			neighbours[targetValue][whereToPlaceNeigh[targetValue]] = i;
	        			whereToPlaceNeigh[targetValue]++;
	        			if(whereToPlaceNeigh[targetValue] == m_NbNeighbours){ // the list of neighbours has just became full ---> sort it
	        			    for (int ind1 = 0; ind1 < m_NbNeighbours; ind1++) { // spremeni, ce je NbNeighbours velka stvar ...
	        			        for (int ind2 = ind1 + 1; ind2 < m_NbNeighbours; ind2++) {
	        			            if (distances[neighbours[targetValue][ind1]] < distances[neighbours[targetValue][ind2]]) {
	        			                int temp = neighbours[targetValue][ind1];
	        			                neighbours[targetValue][ind1] = neighbours[targetValue][ind2];
	        			                neighbours[targetValue][ind2] = temp;
	        			            }	
	        			        }
	        			    }
	        				
	        			}
	        			if(debug) System.out.println("    after first nbNeigh: " + Arrays.toString(neighbours[targetValue]));	        			
        			} else{
        				sortingNeeded = true;
        			}
        		} else{
        			// nothing to do here
        		}        		
        		if(sortingNeeded){
    		        if (distances[i] >= distances[neighbours[targetValue][0]]) {
    		            continue;
    		        }
    		        int j; // here the branch prediction should kick-in
    		        for (j = 1; j < m_NbNeighbours && distances[i] < distances[neighbours[targetValue][j]]; j++) {
    		            neighbours[targetValue][j - 1] = neighbours[targetValue][j];
    		        }
    		        neighbours[targetValue][j - 1] = i;
    		        if(debug) System.out.println("    after additional sorting: " + Arrays.toString(neighbours[targetValue]));	
        		}

	        }
	    }
		if(debug){
			System.out.println("   nearest: " + Arrays.deepToString(neighbours));
			System.out.println();
		}
		NearestNeighbour[][] nearestNeighbours = new NearestNeighbour[m_NbTargetValues][m_NbNeighbours];
		for(int value = 0; value < m_NbTargetValues; value++){
			for(int i = 0; i < m_NbNeighbours; i++){
				nearestNeighbours[value][i] = new NearestNeighbour(neighbours[value][i], distances[neighbours[value][i]], calcDistance(tuple, data.getTuple(neighbours[value][i]), 1));
			}
		}
		return nearestNeighbours;
	}
	
	/**
	 * Distance between tuples in the subspace {@code space}.
	 * @param t1
	 * @param t2
	 * @param space if 0, subspace is descriptive space, else target space
	 * @return
	 * @throws ClusException 
	 */
    public double calcDistance(DataTuple t1, DataTuple t2, int space) throws ClusException {
        double dist = 0.0;
        int dimensions = space == 0 ? m_NbDescriptiveAttrs : m_NbTargetAttrs;
        ClusAttrType attr;
    	for(int attrInd = 0; attrInd < dimensions; attrInd++){
    		attr = m_DescriptiveTargetAttr[space][attrInd];
    		dist += calcDistance1D(t1, t2, attr);
    	}
        return dist / dimensions;
    }
    /**
     * Calculates the distance between to tuples in a given component {@code attr}. 
     * @param t1
     * @param t2
     * @param attr
     * @return
     * @throws ClusException 
     */
    public double calcDistance1D(DataTuple t1, DataTuple t2, ClusAttrType attr) throws ClusException{
		if(attr instanceof NominalAttrType){
			return calculateNominalDist1D(t1, t2, (NominalAttrType) attr);
		} else if(attr instanceof NumericAttrType){
			return calculateNumericDist1D(t1, t2, (NumericAttrType) attr, m_numMaxs.get(attr.getName()) - m_numMins.get(attr.getName()));
		} else if(attr instanceof TimeSeriesAttrType) {
			return calculateTimeSeriesDist1D(t1, t2, (TimeSeriesAttrType) attr);
		} else if(attr instanceof StringAttrType){
			return calculateStringDist1D(t1, t2, (StringAttrType) attr);
		} else{
			return 0.0;
		}
    	
    }
	
    // TODO: make handling of missing values reliefish for all dist.
    /**
     * Calculates distance between the nominal values of the component {@code attr}.
     * @param t1
     * @param t2
     * @param attr
     * @return
     */
    public double calculateNominalDist1D(DataTuple t1, DataTuple t2, NominalAttrType attr){
		int v1 = attr.getNominal(t1);
		int v2 = attr.getNominal(t2);
		if(v1 >= attr.m_NbValues){
			if(v2 >= attr.m_NbValues){	// both missing
				return m_bothMissing;
			} else{ 		   			// the first missing
				return m_oneMissing;
			}
		} else{
			if(v2 >= attr.m_NbValues){	// the second missing
				return m_oneMissing;			   
			} else{						// none of them missing
				return v1 == v2 ? 0.0 : 1.0;
			}
		}
    }
    
    /**
     * Calculates distance between the numeric values of the component {@code attr}.
     * @param t1
     * @param t2
     * @param attr
     * @param normalizationFactor
     * @return
     */
    public double calculateNumericDist1D(DataTuple t1, DataTuple t2, NumericAttrType attr, double normalizationFactor){
		double v1 = attr.getNumeric(t1);
		double v2 = attr.getNumeric(t2);
		if(t1.hasNumMissing(attr.getArrayIndex())){
			if(t2.hasNumMissing(attr.getArrayIndex())){
				return m_bothMissing;
			} else{
				return m_oneMissing;
			}
		} else{
			if(t2.hasNumMissing(attr.getArrayIndex())){
				return m_oneMissing;
			} else{
				return Math.abs(v1 - v2) / normalizationFactor;
			}
		}
    	
    }
    
    /**
     * Computes distance between the time series values of the component {@code attr}.
     * @param t1
     * @param t2
     * @param attr
     * @return
     * @throws ClusException 
     */
    public double calculateTimeSeriesDist1D(DataTuple t1, DataTuple t2, TimeSeriesAttrType attr) throws ClusException{
    	TimeSeries ts1 = attr.getTimeSeries(t1);
    	TimeSeries ts2 = attr.getTimeSeries(t2);
    	
		switch (m_TimeSeriesDistance) {
		case Settings.TIME_SERIES_DISTANCE_MEASURE_DTW:
			return new DTWTimeSeriesDist(attr).calcDistance(t1, t2);
		case Settings.TIME_SERIES_DISTANCE_MEASURE_QDM:
			if (ts1.length() == ts2.length()) {
				return new QDMTimeSeriesDist(attr).calcDistance(t1, t2);
			} else {
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
     * @param t1
     * @param t2
     * @param attr
     * @return
     */
    public double calculateStringDist1D(DataTuple t1, DataTuple t2, StringAttrType attr){
    	return new Levenshtein(t1, t2, attr).getDist();    	
    }
    
	public void sortFeatureRanks(){
		Iterator iter = m_AllAttributes.keySet().iterator();
		while (iter.hasNext()){
			String attr = (String)iter.next();
//			double score = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
			double score = ((double[])m_AllAttributes.get(attr))[2];
			ArrayList attrs = new ArrayList();
			if (m_FeatureRanks.containsKey(score))
				attrs = (ArrayList)m_FeatureRanks.get(score);
			attrs.add(attr);
			m_FeatureRanks.put(score, attrs);
		}
	}
	
	/**
	 * Returns the index of the chosen example in the iteration {@code iteration}.
	 * @param iteration
	 * @return
	 */
	private int nextInstance(int iteration){
		if(m_isDeterministic){
			return iteration;
		} else{
			return (int) (m_rnd.nextDouble() * m_NbExamples);
		}
	}
}

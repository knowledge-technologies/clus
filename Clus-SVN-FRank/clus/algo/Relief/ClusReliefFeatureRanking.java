package clus.algo.Relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.ensembles.ClusEnsembleFeatureRanking;
import clus.ext.ensembles.ClusEnsembleInduce;

public class ClusReliefFeatureRanking extends ClusEnsembleFeatureRanking{
	private int m_NbNeighbours;
	private int m_NbIterations;
	// {descriptive attributes, target attributes} for each type of attributes
	private NominalAttrType[][] m_DescrTargetNomAttr = new NominalAttrType[2][];
	private NumericAttrType[][] m_DescrTargetNumAttr = new NumericAttrType[2][];
	private TimeSeriesAttrType[][] m_DescrTargetTSAttr = new TimeSeriesAttrType[2][];
	
	private int[] m_NbDescrTargetAttrs = new int[]{10,5};
	
	// min and max for each numeric attribute
	private double[][] m_numMins, m_numMaxs;
	private int m_NbExamples;
	// distances in the case of missing values
	double m_oneMissing = 0.5;
	double m_bothMissing = 1.0;
	// next instance generator (if m_NbIterations < m_NbExamples)
	boolean m_isDeterministic;
	Random m_rnd = new Random(1234);

	
	public ClusReliefFeatureRanking(int neighbours, int iterations){
		super();
		this.m_NbNeighbours = neighbours;
		this.m_NbIterations = iterations;
	}
	
	public void calculateReliefImportance(RowData data) {
		setReliefDescription(m_NbNeighbours, m_NbIterations);
		m_NbExamples = data.getNbRows();
		m_isDeterministic = m_NbExamples > m_NbIterations;
		// initialize descriptive and target attributes if necessary
		int attrType;
		for(int space = 0; space < 2; space++){
			if(space == 0) attrType = ClusAttrType.ATTR_USE_DESCRIPTIVE;
			else attrType = ClusAttrType.ATTR_USE_TARGET;
			
			if(m_DescrTargetNomAttr[space] == null) m_DescrTargetNomAttr[space] = data.m_Schema.getNominalAttrUse(attrType);
			if(m_DescrTargetNumAttr[space] == null) m_DescrTargetNumAttr[space] = data.m_Schema.getNumericAttrUse(attrType);
			if(m_DescrTargetTSAttr[space] == null) m_DescrTargetTSAttr[space] = data.m_Schema.getTimeSeriesAttrUse(attrType);
		}
		// compute min and max of numeric attributes
		m_numMins = new double[][]{new double[m_DescrTargetNumAttr[0].length], new double[m_DescrTargetNumAttr[1].length]};
		m_numMaxs = new double[][]{new double[m_DescrTargetNumAttr[0].length], new double[m_DescrTargetNumAttr[1].length]};
		for(int space = 0; space < m_numMins.length; space++){
			Arrays.fill(m_numMins[space], Double.POSITIVE_INFINITY);
			Arrays.fill(m_numMaxs[space], Double.NEGATIVE_INFINITY);
		}
		double value;
		NumericAttrType numAttr;
		for(int space = 0; space < m_numMins.length; space++){
			for(int attr = 0; attr < m_DescrTargetNumAttr[space].length; attr++){
				numAttr = m_DescrTargetNumAttr[space][attr];
				for(int example = 0; example < m_NbExamples; example++){
					value = numAttr.getNumeric(data.getTuple(example));
					if(value < m_numMins[space][attr]) m_numMins[space][attr] = value;
					if(value > m_numMaxs[space][attr]) m_numMaxs[space][attr] = value;
				}
			}			
		}
		System.out.println(Arrays.deepToString(m_numMins));
		System.out.println(Arrays.deepToString(m_numMaxs));
		System.out.println("nomi: " + Arrays.deepToString(m_DescrTargetNomAttr));
		System.out.println("numi: " + Arrays.deepToString(m_DescrTargetNumAttr));
		
		// attribute relevance
		DataTuple tuple;
		int tupleInd;
		NearestNeighbour[] nearestNeighbours; 
		for(int iteration = 0; iteration < m_NbIterations; iteration++){
			tupleInd = nextInstance(iteration);
			tuple = data.getTuple(tupleInd);
			System.out.println("Tuple: " + tuple.toString());
			nearestNeighbours = findNearestNeighbours(tupleInd, data);
			// update importances
			for(int attr = 0; attr < m_DescrTargetNomAttr[0].length; attr++){ // nominal
				double [] info = getAttributeInfo(m_DescrTargetNomAttr[0][attr].getName());
				System.out.println("Importance of " +m_DescrTargetNomAttr[0][attr].getName() + "is currently " + info[2]);
				double distAttr = 0.0;
				double sumDistAttr = 0.0;
				double sumDistTarget = 0.0;
				double sumDistAttrTarget = 0.0;
				for(int neighbour = 0; neighbour < m_NbNeighbours; neighbour++){
					distAttr = calculateNominalDist1D(tuple, data.getTuple(nearestNeighbours[neighbour].m_indexInDataSet), m_DescrTargetNomAttr[0][attr]);
					sumDistAttr += distAttr;
					sumDistTarget += nearestNeighbours[neighbour].m_targetDistance;
					sumDistAttrTarget += distAttr * nearestNeighbours[neighbour].m_targetDistance;					
				}
				info[2] += (sumDistAttrTarget / sumDistAttr - (sumDistAttr - sumDistAttrTarget) / (m_NbNeighbours - sumDistTarget)) / m_NbIterations;
				putAttributeInfo(m_DescrTargetNomAttr[0][attr].getName(), info);
				System.out.println("Importance of " +m_DescrTargetNomAttr[0][attr].getName() + "updated to " + info[2]);
			}
			for(int attr = 0; attr < m_DescrTargetNumAttr[0].length; attr++){ // numeric
				double [] info = getAttributeInfo(m_DescrTargetNumAttr[0][attr].getName());
				System.out.println("Importance of " +m_DescrTargetNumAttr[0][attr].getName() + "is currently " + info[2]);
				double distAttr = 0.0;
				double sumDistAttr = 0.0;
				double sumDistTarget = 0.0;
				double sumDistAttrTarget = 0.0;
				for(int neighbour = 0; neighbour < m_NbNeighbours; neighbour++){
					distAttr = calculateNumericDist1D(tuple, data.getTuple(nearestNeighbours[neighbour].m_indexInDataSet), m_DescrTargetNumAttr[0][attr], m_numMaxs[0][attr] - m_numMins[0][attr]);
					sumDistAttr += distAttr;
					sumDistTarget += nearestNeighbours[neighbour].m_targetDistance;
					sumDistAttrTarget += distAttr * nearestNeighbours[neighbour].m_targetDistance;					
				}
				info[2] += (sumDistAttrTarget / sumDistAttr - (sumDistAttr - sumDistAttrTarget) / (m_NbNeighbours - sumDistTarget)) / m_NbIterations;
				putAttributeInfo(m_DescrTargetNumAttr[0][attr].getName(), info);
				System.out.println("Importance of " +m_DescrTargetNumAttr[0][attr].getName() + "updated to " + info[2]);
			}
			
		}
		
	}
	

	public NearestNeighbour[] findNearestNeighbours(int tupleInd, RowData data){
		DataTuple tuple = data.getTuple(tupleInd);
		int[] neighbours = new int[m_NbNeighbours]; 		// current candidates
		double[] distances = new double[m_NbExamples];	// distances[i] = distance(tuple, data.getTuple(i))	TODO: mal prostora porabmo, ce je pdoatk. mnozica stevilcna ...
		
		boolean debug = true;

		for(int i = 0; i < m_NbExamples;i++){
			distances[i] = calcDistance(tuple, data.getTuple(i), 0); // descriptive space
		}
		if(debug){
			System.out.println("  scores: " + Arrays.toString(distances));
		}
	    for (int i = 0; i < m_NbNeighbours; i++){
	        if(i != tupleInd) neighbours[i] = i;
	    }
	    if(tupleInd < m_NbNeighbours) neighbours[tupleInd] = m_NbNeighbours;
	    // sorting candidates so distances[candidates[0]] is the largest: TODO: spremeni, ce je NbNeighbours velka stvar ...
	    for (int i = 0; i < m_NbNeighbours; i++) {
	        for (int j = i+1; j < m_NbNeighbours; j++) {
	            if (distances[neighbours[i]] < distances[neighbours[j]]) {
	                int temp = neighbours[i];
	                neighbours[i] = neighbours[j];
	                neighbours[j] = temp;
	            }	
	        }
	    }
	    if(debug) System.out.println("    after first nbNeigh: " + Arrays.toString(neighbours));
	    for (int i = m_NbNeighbours + (tupleInd < m_NbNeighbours ? 1 : 0); i < m_NbExamples; i++) {
	    	if(i != tupleInd){
		        if (distances[i] > distances[neighbours[0]]) {
		            continue;
		        }
		        int j; // here the branch prediction should kick-in
		        for (j = 1; j < m_NbNeighbours && distances[i] < distances[neighbours[j]]; j++) {
		            neighbours[j - 1] = neighbours[j];
		        }
		        neighbours[j - 1] = i;
	    	}
	    }
		if(debug){
			System.out.println("   nearest: " + Arrays.toString(neighbours));
			System.out.println();
		}
		NearestNeighbour[] nearestNeighbours =new NearestNeighbour[m_NbNeighbours];
		for(int i = 0; i < m_NbNeighbours; i++){
			nearestNeighbours[i] = new NearestNeighbour(neighbours[i], distances[neighbours[i]], calcDistance(tuple, data.getTuple(neighbours[i]), 1));
		}
		return nearestNeighbours;
	}
	
	/**
	 * Distance between tuples in the subspace space
	 * @param t1
	 * @param t2
	 * @param space if 0, subspace is descriptive space, else target space
	 * @return
	 */
    public double calcDistance(DataTuple t1, DataTuple t2, int space) {
        double dist = 0.0;
    	// zaenkrat samo te tri
        dist += calculateNominalDist(t1, t2, space);
        dist += calculateNumericDist(t1, t2, space);
        dist += calculateTimeSeriesDist(t1, t2, space); 
        return dist / m_NbDescrTargetAttrs[space];
    }
	
    // TODO: make handling of missing values reliefish for all dist.
    
    /**
     * Calculates the distance in the nominal subspace of the space.
     * @param t1
     * @param t2
     * @param spaceType if 0, we are in the descriptive space, if 1, we are in the target space
     * @return
     */
    public double calculateNominalDist(DataTuple t1, DataTuple t2, int spaceType){
    	double dist = 0.0;
		for(NominalAttrType attr : m_DescrTargetNomAttr[spaceType]){
			dist += calculateNominalDist1D(t1, t2, attr);
		}
		return dist;
    }
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
     * Calculates the distance in the numeric subspace of the space.
     * @param t1
     * @param t2
     * @param spaceType: see calculateNominalDistance
     * @return
     */
    public double calculateNumericDist(DataTuple t1, DataTuple t2, int spaceType){
    	double dist = 0.0;
    	for(int attr = 0; attr < m_DescrTargetNumAttr[spaceType].length; attr++){
    		dist += calculateNumericDist1D(t1, t2, m_DescrTargetNumAttr[spaceType][attr], m_numMaxs[spaceType][attr] - m_numMins[spaceType][attr]);
    	}
    	return dist;
    }
    public double calculateNumericDist1D(DataTuple t1, DataTuple t2, NumericAttrType numAttr, double normalizationFactor){
		double v1 = numAttr.getNumeric(t1);
		double v2 = numAttr.getNumeric(t2);
		if(t1.hasNumMissing(numAttr.getArrayIndex())){
			if(t2.hasNumMissing(numAttr.getArrayIndex())){
				return m_bothMissing;
			} else{
				return m_oneMissing;
			}
		} else{
			if(t2.hasNumMissing(numAttr.getArrayIndex())){
				return m_oneMissing;
			} else{
				return Math.abs(v1 - v2) / normalizationFactor;
			}
		}
    	
    }
    
    
    
    public double calculateTimeSeriesDist(DataTuple t1, DataTuple t2, int spaceType){
    	return 0.0;
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
			return(int) (m_rnd.nextDouble() * m_NbExamples);
		}
	}
}

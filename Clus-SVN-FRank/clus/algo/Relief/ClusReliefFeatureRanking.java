package clus.algo.Relief;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.ensembles.ClusEnsembleFeatureRanking;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.selection.OOBSelection;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusReliefFeatureRanking extends ClusEnsembleFeatureRanking{
	private int m_NbNeighbours;
	private int m_NbIterations;
	// {descriptive attributes, target attributes} for each type of attributes
	private NominalAttrType[][] m_DescrTargetNomAttr = new NominalAttrType[2][];
	private NumericAttrType[][] m_DescrTargetNumAttr = new NumericAttrType[2][];
	private TimeSeriesAttrType[][] m_DescrTargetTSAttr = new TimeSeriesAttrType[2][];
	// min and max for each numeric attribute
	private double[][] m_numMins, m_numMaxs;
	private int m_NbExamples;

	
	public ClusReliefFeatureRanking(int neighbours, int iterations){ // TODO: pazi: neigbours > samples ...
		super();
		this.m_NbNeighbours = neighbours;
		this.m_NbIterations = iterations;
	}
	
	public void calculateReliefImportance(RowData data) {
		m_NbExamples = data.getNbRows();
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
		
		// attribute relevance
		Random rnd = new Random(123); // TODO: determinisicno, ce nbExamples == nbRows ...
		DataTuple tuple;
		int[] nearestNeighbours; 
		for(int iteration = 0; iteration < m_NbIterations; iteration++){
			tuple = data.getTuple((int) (rnd.nextDouble() * m_NbExamples));
			System.out.println("Tuple: " + tuple.toString());
			nearestNeighbours = findNearestNeighbours(tuple, data);
			System.out.println("nearest neighb: " + Arrays.toString(nearestNeighbours));
			
		}
		
	}
	
	public int[] findNearestNeighbours(DataTuple tuple, RowData data){
		int[] neighbours = new int[m_NbNeighbours]; 		// current candidates
		double[] distances = new double[m_NbExamples];	// distances[i] = distance(tuple, data.getTuple(i))	TODO: mal prostora porabmo, ce je pdoatk. mnozica stevilcna ...
		
		boolean debug = true;

		for(int i = 0; i < m_NbExamples;i++){
			distances[i] = calcDistance(tuple, data.getTuple(i), 0);
		}
		if(debug){
			System.out.println("  scores: " + Arrays.toString(distances));
		}
	    for (int i = 0; i < m_NbNeighbours; i++){
	        neighbours[i] = i;
	    }		
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
	    // remaining part of dataset
	    for (int i = m_NbNeighbours; i < m_NbNeighbours; i++) {
	        if (distances[i] > distances[neighbours[0]]) {
	            continue;
	        }
	        // moving all larger candidates to the left, to keep the array sorted
	        int j; // here the branch prediction should kick-in
	        for (j = 1; j < m_NbNeighbours && distances[i] < distances[neighbours[j]]; j++) {
	            neighbours[j - 1] = neighbours[j];
	        }
	        // inserting the new item
	        neighbours[j - 1] = i;
	    }
		if(debug){
			System.out.println("   " + Arrays.toString(neighbours));
			System.out.println();
		}
		return neighbours;
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
        return dist;
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
    	int v1, v2;
    	double dist = 0.0;
    	double oneMissing = 0.5;
		for(NominalAttrType attr : m_DescrTargetNomAttr[spaceType]){
			v1 = attr.getNominal(t1);
			v2 = attr.getNominal(t2);
			if(v1 >= attr.m_NbValues){
				if(v2 >= attr.m_NbValues){	// both missing
					dist += 1.0;
				} else{ 		   			// the first missing
					dist += oneMissing;
				}
			} else{
				if(v2 >= attr.m_NbValues){	// the second missing
					dist += oneMissing;			   
				} else{						// none of them missing
					dist += v1 == v2 ? 0.0 : 1.0;
				}
			}
		}
		return dist;
    }
    /**
     * Calculates the distance in the numeric subspace of the space.
     * @param t1
     * @param t2
     * @param spaceType: see calculateNominalDistance
     * @return
     */
    public double calculateNumericDist(DataTuple t1, DataTuple t2, int spaceType){
    	double v1, v2;
    	double dist = 0.0;
    	double oneMissing = 0.5;
    	NumericAttrType numAttr;
    	for(int attr = 0; attr < m_DescrTargetNumAttr[spaceType].length; attr++){
    		numAttr = m_DescrTargetNumAttr[spaceType][attr];
    		v1 = numAttr.getNumeric(t1);
    		v2 = numAttr.getNumeric(t2);
    		if(t1.hasNumMissing(numAttr.getArrayIndex())){
    			if(t2.hasNumMissing(numAttr.getArrayIndex())){
    				dist += 1.0;
    			} else{
    				dist +=  oneMissing;
    			}
    		} else{
    			if(t2.hasNumMissing(numAttr.getArrayIndex())){
    				dist += oneMissing;
    			} else{
    				dist += Math.abs(v1 - v2) / (m_numMaxs[spaceType][attr] - m_numMins[spaceType][attr]);
    			}
    		}
    	}
    	return dist;
    }
    public double calculateTimeSeriesDist(DataTuple t1, DataTuple t2, int spaceType){
    	return 0.0;
    }
    
	public void calculateRFimportance(ClusModel model, ClusRun cr, OOBSelection oob_sel) throws ClusException{
		ArrayList<String> attests = new ArrayList<String>();
		fillWithAttributesInTree((ClusNode)model, attests);
		RowData tdata = (RowData)((RowData)cr.getTrainingSet()).deepCloneData();
		double[][] oob_errs = calcAverageErrors((RowData)tdata.selectFrom(oob_sel), model, cr);
		for (int z = 0; z < attests.size(); z++){//for the attributes that appear in the tree
			String current_attribute = (String)attests.get(z);
			double [] info = getAttributeInfo(current_attribute);
			double type = info[0];
			double position = info[1];
			RowData permuted = createRandomizedOOBdata(oob_sel, (RowData)tdata.selectFrom(oob_sel), (int)type, (int)position);
			double[][] permuted_oob_errs = calcAverageErrors((RowData)permuted, model, cr);
			for(int i = 0; i < oob_errs.length; i++){
				info[2 + i] += oob_errs[i][1] * (oob_errs[i][0] - permuted_oob_errs[i][0])/oob_errs[i][0];
			}
			putAttributeInfo(current_attribute, info);
		}
	}
}

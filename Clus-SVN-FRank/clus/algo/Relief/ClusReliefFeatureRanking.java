package clus.algo.Relief;

import java.util.Arrays;
import java.util.Random;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.ext.ensembles.ClusEnsembleFeatureRanking;
import clus.main.Settings;
import clus.statistic.ClusStatistic;

public class ClusReliefFeatureRanking extends ClusEnsembleFeatureRanking{
	private int m_NbNeighbours;
	private int m_NbIterations;
	// {descriptive attributes, target attributes} for each type of attributes
	private NominalAttrType[][] m_DescrTargetNomAttr = new NominalAttrType[2][];
	private NumericAttrType[][] m_DescrTargetNumAttr = new NumericAttrType[2][];
	private TimeSeriesAttrType[][] m_DescrTargetTSAttr = new TimeSeriesAttrType[2][];
	// min and max for each numeric attribute
	private double[][] m_numMins, m_numMaxs;

	
	public ClusReliefFeatureRanking(int neighbours, int iterations){
		super();
		this.m_NbNeighbours = neighbours;
		this.m_NbIterations = iterations;
	}
	
	public void calculateReliefImportance(RowData data) {
		int nbExamples = data.getNbRows();
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
				for(int example = 0; example < nbExamples; example++){
					value = numAttr.getNumeric(data.getTuple(example));
					if(value < m_numMins[space][attr]) m_numMins[space][attr] = value;
					if(value > m_numMaxs[space][attr]) m_numMaxs[space][attr] = value;
				}
			}			
		}
		
		
		Random rnd = new Random(123); // TODO: determinisicno, ce nbExamples == nbRows ...
		DataTuple tuple;
		for(int iteration = 0; iteration < m_NbIterations; iteration++){
			tuple = data.getTuple((int) (rnd.nextDouble() * nbExamples));
			System.out.println(tuple.toString());
			
		}
		
	}
	
    public double[] calcDistance(DataTuple t1, DataTuple t2) {
        double[] dist = new double[2]; // {distance in descriptive space, distance in target space}
        for(int space = 0; space < 2; space++){
        	// zaenkrat samo te tri
	        dist[space] += calculateNominalDist(t1, t2, space);
	        dist[space] += calculateNumericDist(t1, t2, space);
	        dist[space] += calculateTimeSeriesDist(t1, t2, space); 
        }
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
    
//	public double calcDistanceOnAttr(DataTuple t1, DataTuple t2, ClusAttrType attr){
//		if( attr instanceof NumericAttrType ){
//			/*
//			 * If one of values is missing, return value 1-(others value) to
//			 * ensure maximum difference.
//			 * If both are missing, return 1.
//			 * If both are present, return absolute value.
//			 */
//			if( attr.isMissing(t2) )
//				if( attr.isMissing(t1) ) // both missing
//					return m_AttrWeighting.getWeight(attr);
//				else // t2 missing
//					return Math.max(attr.getNumeric(t1), 1-attr.getNumeric(t1))*m_AttrWeighting.getWeight(attr);
//			else
//				if( attr.isMissing(t1) ) // t1 missing
//					return Math.max(attr.getNumeric(t2), 1-attr.getNumeric(t2))*m_AttrWeighting.getWeight(attr);
//				else // both present
//					return Math.abs(attr.getNumeric(t2)- attr.getNumeric(t1))*m_AttrWeighting.getWeight(attr);
//		}else
//			if( attr instanceof NominalAttrType ){
//				/*
//				 * If both values are present end share same value, return 0.
//				 * Otherwise return 1 (weighted).
//				 */
//				return attr.getNominal(t2) == attr.getNominal(t1) &&
//						!attr.isMissing(t2) &&
//						!attr.isMissing(t1)
//						? 0 : m_AttrWeighting.getWeight(attr);
//			}else{
//				throw new IllegalArgumentException(this.getClass().getName() + ":calcDistanceOnAttr() - Distance not supported!");
//			}
//	}
}

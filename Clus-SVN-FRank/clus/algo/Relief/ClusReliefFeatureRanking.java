package clus.algo.Relief;

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
	private NominalAttrType[] m_DescrNomAttr, m_TargetNomAttr;
	private NumericAttrType[] m_DescrNumAttr, m_TargetNumAttr;
	private TimeSeriesAttrType[] m_DescrTSAttr, m_TargetTSAttr;

	
	
	public ClusReliefFeatureRanking(int neighbours, int iterations){
		super();
		this.m_NbNeighbours = neighbours;
		this.m_NbIterations = iterations;
	}
	
	public void calculateReliefImportance(RowData data) {
		if(m_DescrNomAttr == null) m_DescrNomAttr = data.m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
		if(m_DescrNumAttr == null) m_DescrNumAttr = data.m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
		if(m_DescrTSAttr == null) m_DescrTSAttr = data.m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
		
		if(m_TargetNomAttr == null) m_TargetNomAttr = data.m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if(m_TargetNumAttr == null) m_TargetNumAttr = data.m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if(m_TargetTSAttr == null) m_TargetTSAttr = data.m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_TARGET);
		
		int nbExamples = data.getNbRows();
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
	
    
    public double calculateNominalDist(DataTuple t1, DataTuple t2, int spaceType){
    	return 0.0;
    }
    public double calculateNumericDist(DataTuple t1, DataTuple t2, int spaceType){
    	return 0.0;
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

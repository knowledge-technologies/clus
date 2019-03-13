package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.exception.ClusException;

public abstract class Statistics {

	public abstract void updateTempStatistics(int targetIndex, boolean isStdClassification, DataTuple tuple, RowData data,
	        NearestNeighbour neigh, double neighWeightNonnormalized, int trueIndex,
	        int targetValue) throws ClusException;
	
	public abstract void updateStatistics(int targetIndex, int numNeighInd, double sumNeighbourWeights);
	
	public abstract void resetTempFields();
	
	public abstract double computeImportances(int targetIndex, int nbNeighInd, int attrInd, boolean isStdClassification, double successfulItearions[]);

}

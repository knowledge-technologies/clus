package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.exception.ClusException;

public class Variance extends Statistics {
	
	private double[][][] mVariances;
	
	private double[] tempS1;
	private double[] tempS2;
	

	public Variance(ClusReliefFeatureRanking relief, int nbTargets, int nbDiffNbNeighbours, int nbDescriptiveAttributes) {
		initializeSuperFields(relief, nbDescriptiveAttributes);
		mVariances = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];
		
		tempS1 = new double[nbDescriptiveAttributes];
		tempS2 = new double[nbDescriptiveAttributes];
		
		if (nbTargets > 1) {
			throw new RuntimeException("The number of targets " + nbTargets + " is not 1.");
		}
	}

	@Override
	public void updateTempStatistics(int targetIndex, boolean isStdClassification, DataTuple tuple, RowData data,
	        NearestNeighbour neigh, double neighWeightNonnormalized, int trueIndex, int targetValue)
	        throws ClusException {
		for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
			ClusAttrType attr = mRelief.getDescriptiveAttribute(attrInd);
			double distAttr = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), attr) * neighWeightNonnormalized;
			tempS1[attrInd] += distAttr;
		}
	}

	@Override
	public void updateStatistics(int targetIndex, int numNeighInd, double sumNeighbourWeights) {
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
        	mVariances[targetIndex + 1][numNeighInd][attrInd] += tempS1[attrInd] / sumNeighbourWeights;
        }
	}

	@Override
	public void resetTempFields() {
		Arrays.fill(tempS1, 0.0);
	}

	@Override
	public double computeImportances(int targetIndex, int nbNeighInd, int attrInd, boolean isStdClassification,
	        double[] successfulItearions) {
		return 1.0 / (mVariances[targetIndex + 1][nbNeighInd][attrInd] / successfulItearions[targetIndex + 1]);
	}

}

package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.exception.ClusException;

public class DistanceSimplified extends Statistics {
	/**
	 * m_SumDistAttr[target index][number of neighbours][attribute]: current sum of
	 * products (1 - distAttr)(1 - distClustering),
	 * for the given number of neighbours and attribute.
	 * If {@code target index} is 0, this is for overall ranking(s).
	 * Otherwise, this is for the ranking(s) for the target with the index
	 * {@code target index - 1}.
	 */
	private double[][][] m_SumDistAttrTarget;
	
	private double[] tempSumDistAttrTarget;

	public DistanceSimplified(ClusReliefFeatureRanking relief, int nbTargets, int nbDiffNbNeighbours,
	        int nbDescriptiveAttributes) {
		initializeSuperFields(relief, nbDescriptiveAttributes);
		m_SumDistAttrTarget = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];
		
		tempSumDistAttrTarget = new double[nbDescriptiveAttributes];
	}

	@Override
	public void updateTempStatistics(int targetIndex, boolean isStdClassification, DataTuple tuple, RowData data,
	        NearestNeighbour neigh, double neighWeightNonnormalized, int trueIndex, int targetValue)
	        throws ClusException {
		double targetDistance = 0.0;
		if (targetIndex >= 0 && !isStdClassification) {
			targetDistance = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), mRelief.getTargetAttribute(trueIndex));
		} else {
			targetDistance = mRelief.computeDistance(tuple, data.getTuple(neigh.getIndexInDataset()),
			        ClusReliefFeatureRanking.TARGET_SPACE);
		}
		for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
			ClusAttrType attr = mRelief.getDescriptiveAttribute(attrInd);
			double distAttr = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), attr) * neighWeightNonnormalized;
			tempSumDistAttrTarget[attrInd] += (1.0 - distAttr) * targetDistance;
			}
	}

	@Override
	public void updateStatistics(int targetIndex, int numNeighInd, double sumNeighbourWeights) {
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
            m_SumDistAttrTarget[targetIndex + 1][numNeighInd][attrInd] += tempSumDistAttrTarget[attrInd] / sumNeighbourWeights;
        }
	}

	@Override
	public void resetTempFields() {
		Arrays.fill(tempSumDistAttrTarget, 0.0);
	}

	@Override
	public double computeImportances(int targetIndex, int nbNeighInd, int attrInd, boolean isStdClassification,
	        double[] successfulItearions) {
		return m_SumDistAttrTarget[targetIndex + 1][nbNeighInd][attrInd] / successfulItearions[targetIndex + 1];
	}

}

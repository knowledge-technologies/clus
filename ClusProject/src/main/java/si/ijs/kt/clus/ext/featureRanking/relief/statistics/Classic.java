package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.exception.ClusException;

public class Classic extends Statistics {
	/**
	 * m_SumDistAttr[target index][number of neighbours][attribute]: current sum of
	 * distances between attribute values, for the given number of neighbours and
	 * attribute. If {@code target index} is 0, this is for overall ranking(s).
	 * Otherwise, this is for the ranking(s) for the target with the index
	 * {@code target index - 1}.
	 */
	private double[][][] m_SumDistAttr;

	/**
	 * m_SumDistAttr[target index][number of neighbours]: for the given number of
	 * neighbours , this is an analogue of {@link #m_SumDistAttr} for the distances
	 * between target values.
	 */
	private double[][] m_SumDistTarget;

	/**
	 * m_SumDistAttr[target index][number of neighbours][attribute]: the analogue of
	 * {@link #m_SumDistAttr}, for the products of distances between attribute
	 * values and distances between target values
	 */
	private double[][][] m_SumDistAttrTarget;

	// temp fields
	double tempSumDistTarget;
	double[] tempSumDistAttr;
	double[] tempSumDistAttrTarget;

	public Classic(ClusReliefFeatureRanking relief, int nbTargets, int nbDiffNbNeighbours,
	        int nbDescriptiveAttributes) {
		initializeSuperFields(relief, nbDescriptiveAttributes);
		m_SumDistAttr = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];
		m_SumDistTarget = new double[nbTargets][nbDiffNbNeighbours];
		m_SumDistAttrTarget = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];

		tempSumDistTarget = 0.0;
		tempSumDistAttr = new double[nbDescriptiveAttributes];
		tempSumDistAttrTarget = new double[nbDescriptiveAttributes];
	}

	@Override
	public void updateTempStatistics(int targetIndex, boolean isStdClassification, DataTuple tuple, RowData data,
	        NearestNeighbour neigh, double neighWeightNonnormalized, int trueIndex,
	        int targetValue) throws ClusException {	
		double targetDistance = 0.0;
		if (targetIndex >= 0 && !isStdClassification) {
			// regression per-target case: we took the neighbours from overall ranking,
			// but need d_target(tuple, neigh).
			targetDistance = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), mRelief.getTargetAttribute(trueIndex));
		} else {
			targetDistance = mRelief.computeDistance(tuple, data.getTuple(neigh.getIndexInDataset()),
			        ClusReliefFeatureRanking.TARGET_SPACE);
		}
		if (!isStdClassification) {
			tempSumDistTarget += targetDistance * neighWeightNonnormalized;
		}
		for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
			ClusAttrType attr = mRelief.getDescriptiveAttribute(attrInd);
			double distAttr = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), attr)
			        * neighWeightNonnormalized;
			if (isStdClassification) {
				int tupleTarget = ((NominalAttrType) mRelief.getTargetAttribute(trueIndex)).getNominal(tuple); // m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex]
				if (targetValue == tupleTarget) {
					tempSumDistAttr[attrInd] -= distAttr;
				} else {
					double pTupleTarget = mRelief.getTargetProbability(targetIndex, tupleTarget); // m_TargetProbabilities[targetIndex
					                                                                              // + 1][tupleTarget];
					double pNeighTarget = mRelief.getTargetProbability(targetIndex, targetValue); // m_TargetProbabilities[targetIndex
					                                                                              // + 1][targetValue];
					tempSumDistAttr[attrInd] += pNeighTarget / (1.0 - pTupleTarget) * distAttr;
				}
			} else {
				tempSumDistAttr[attrInd] += distAttr;
				tempSumDistAttrTarget[attrInd] += distAttr * targetDistance;
			}
		}
	}
	
	@Override
	public void updateStatistics(int targetIndex, int numNeighInd, double sumNeighbourWeights) {
		double normalizedTempDistTarget = tempSumDistTarget / sumNeighbourWeights;
        m_SumDistTarget[targetIndex + 1][numNeighInd] += normalizedTempDistTarget;
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
            double normalizedTempDistAttr = tempSumDistAttr[attrInd] / sumNeighbourWeights;
            double normalizedTempTistAttrTarget = tempSumDistAttrTarget[attrInd] / sumNeighbourWeights;
            m_SumDistAttr[targetIndex + 1][numNeighInd][attrInd] += normalizedTempDistAttr;
            m_SumDistAttrTarget[targetIndex + 1][numNeighInd][attrInd] += normalizedTempTistAttrTarget;
        }
	}

	@Override
	public double computeImportances(int targetIndex, int nbNeighInd, int attrInd, boolean isStdClassification,
	        double successfulItearions[]) {
		double sumDistAttr = m_SumDistAttr[targetIndex + 1][nbNeighInd][attrInd];
		double sumDistTarget = m_SumDistTarget[targetIndex + 1][nbNeighInd];
		double sumDistAttrTarget = m_SumDistAttrTarget[targetIndex + 1][nbNeighInd][attrInd];

		if (isStdClassification) {
			return sumDistAttr / successfulItearions[targetIndex + 1];
		} else {
			double p1 = sumDistAttrTarget / sumDistTarget;
			double p2 = (sumDistAttr - sumDistAttrTarget) / (successfulItearions[targetIndex + 1] - sumDistTarget);
			return p1 - p2;
		}
	}

	@Override
	public void resetTempFields() {
		tempSumDistTarget = 0.0;
		Arrays.fill(tempSumDistAttr, 0.0);
		Arrays.fill(tempSumDistAttrTarget, 0.0);
	}

}

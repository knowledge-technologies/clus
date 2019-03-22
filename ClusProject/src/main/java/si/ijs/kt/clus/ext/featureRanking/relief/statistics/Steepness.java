package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import java.util.Arrays;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;
import org.jblas.Solve;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.exception.ClusException;

public class Steepness extends Statistics {
	private double[][][] m_Coefficient;
	
	private double[] tempNormX;
	private double[] tempXtY;
	
	private double[] tempDistTarget;
	private double[][] tempDistAttribute;
	
	private double[] m_NbConstant;
	
	private boolean m_IsTLS;
	private int m_NeighIndex;

	public Steepness(ClusReliefFeatureRanking relief, int nbTargets, int nbDiffNbNeighbours, int nbDescriptiveAttributes, int maxNbNeighbours, boolean isTotalLeastSquares) {
		initializeSuperFields(relief, nbDescriptiveAttributes);
		m_NbConstant = new double[nbDescriptiveAttributes];
		m_Coefficient = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];
		tempNormX = new double[nbDescriptiveAttributes];
		tempXtY = new double[nbDescriptiveAttributes];
		tempDistTarget = new double[maxNbNeighbours];
		tempDistAttribute = new double[nbDescriptiveAttributes][maxNbNeighbours];
		m_IsTLS = isTotalLeastSquares;
		m_NeighIndex = 0;
	}

	@Override
	public void updateTempStatistics(int targetIndex, boolean isStdClassification, DataTuple tuple, RowData data,
	        NearestNeighbour neigh, double neighWeightNonnormalized, int trueIndex, int targetValue)
	        throws ClusException {
		double targetDistance = 0.0;
		if (targetIndex >= 0 && !isStdClassification) {
			targetDistance = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), mRelief.getTargetAttribute(trueIndex));
		} else {
			targetDistance = mRelief.computeDistance(tuple, data.getTuple(neigh.getIndexInDataset()), ClusReliefFeatureRanking.TARGET_SPACE);
		}
		//if (m_IsTLS) {
		tempDistTarget[m_NeighIndex] = targetDistance;
		//}
		for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
			ClusAttrType attr = mRelief.getDescriptiveAttribute(attrInd);
			double distAttr = mRelief.computeDistance1D(tuple, data.getTuple(neigh.getIndexInDataset()), attr);
//			if (m_IsTLS) {
			tempDistAttribute[attrInd][m_NeighIndex] = distAttr;
//			} else {
			tempNormX[attrInd] += distAttr * distAttr;
			tempXtY[attrInd] += distAttr * targetDistance;
//			}
		}		
		m_NeighIndex++;
	}

	@Override
	public void updateStatistics(int targetIndex, int numNeighInd, double sumNeighbourWeights) {
		double[] distT = Arrays.copyOf(tempDistTarget, m_NeighIndex);
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
        	double[] distA = Arrays.copyOf(tempDistAttribute[attrInd], m_NeighIndex);
        	if (tempNormX[attrInd] > 0) {
        		if (m_IsTLS) {
        			m_Coefficient[targetIndex + 1][numNeighInd][attrInd] += optimalViaTotalSquares(distA, distT); 
        		} else {
        			m_Coefficient[targetIndex + 1][numNeighInd][attrInd] += optimalViaStandardSquares(distA, distT);
//        			m_Coefficient[targetIndex + 1][numNeighInd][attrInd] += 
//        					tempXtY[attrInd] / tempNormX[attrInd];
        		}
        	} else {
        		m_NbConstant[attrInd] += 1.0;
        	}
        }
	}

	@Override
	public void resetTempFields() {
		Arrays.fill(tempNormX, 0.0);
		Arrays.fill(tempXtY, 0.0);
		
		Arrays.fill(tempDistTarget, 0.0);
		for (double[] row : tempDistAttribute) {
			Arrays.fill(row, 0.0);
		}
		m_NeighIndex = 0;
	}

	@Override
	public double computeImportances(int targetIndex, int nbNeighInd, int attrInd, boolean isStdClassification,
	        double[] successfulItearions) {
		double k = m_Coefficient[targetIndex + 1][nbNeighInd][attrInd] / (successfulItearions[targetIndex + 1] - m_NbConstant[attrInd]);
//		double k2 = -1.0 * Math.pow(k - 2.0, 2.0);
//		return Math.exp(k2);
		return k;
	}
	
	private static double optimalViaTotalSquares(double[] xs, double[] ys) {
		DoubleMatrix A = new DoubleMatrix(new double[][] {xs, ys}).transpose();
		DoubleMatrix[] svd = Singular.fullSVD(A);
		if (svd[2].get(1, 1) == 0) {
			System.out.println(A);
		}
		return -svd[2].get(0, 1) / svd[2].get(1, 1); 		
	}
	
	private static double optimalViaStandardSquares(double[] xs, double[] ys) {
		double[] ones = new double[xs.length];
		Arrays.fill(ones, 1.0);
		DoubleMatrix A = new DoubleMatrix(new double[][] {xs, ones}).transpose();
		DoubleMatrix b = new DoubleMatrix(ys);
		DoubleMatrix solution = Solve.solveLeastSquares(A, b);  // [k; b] where y = k x + b
		return Math.max(solution.get(0, 0), 0.0); 		
	}
	
	public static void main(String[] a) {
		double[] x = new double[] {1.0, 2.0, 3.0};
		double[] y = new double[] {3.0, 5.0, 7.0};
		System.out.println(optimalViaStandardSquares(x, y));
	}

}

package si.ijs.kt.clus.ext.featureRanking.relief.statistics;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.linsol.qr.AdjLinearSolverQr_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Singular;
import org.jblas.Solve;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;

public class Steepness extends Statistics {
	private double[][][] m_Coefficient;
	private double[][][] mDistAttribute;
	private double[][] mDistTarget;
	
	private double[] tempNormX;
	private double[] tempXtY;
	
	private double[] tempDistTarget;
	private double[][] tempDistAttribute;
	
	private double[][] m_NbConstant;
	
	private boolean m_IsTLS, m_IsMacro;
	private int m_NeighIndex;
	private int[][] mWhereTo;
	private int[] mWhereToTarget;

	public Steepness(ClusReliefFeatureRanking relief, int nbTargets, int nbDiffNbNeighbours, int nbDescriptiveAttributes, int maxNbNeighbours, boolean isTotalLeastSquares, boolean isMacro) {
		initializeSuperFields(relief, nbDescriptiveAttributes);
		m_NbConstant = new double[nbDiffNbNeighbours][nbDescriptiveAttributes];
		m_Coefficient = new double[nbTargets][nbDiffNbNeighbours][nbDescriptiveAttributes];
		tempNormX = new double[nbDescriptiveAttributes];
		tempXtY = new double[nbDescriptiveAttributes];
		tempDistTarget = new double[maxNbNeighbours];
		tempDistAttribute = new double[nbDescriptiveAttributes][maxNbNeighbours];
		mDistAttribute = new double[nbDiffNbNeighbours][nbDescriptiveAttributes][relief.getMaxNbIterations()];
		mDistTarget = new double[nbDiffNbNeighbours][relief.getMaxNbIterations()];
		mWhereTo = new int[nbDiffNbNeighbours][nbDescriptiveAttributes];
		mWhereToTarget = new int[nbDiffNbNeighbours];
		m_IsTLS = isTotalLeastSquares;
		m_IsMacro = isMacro;
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
		if (targetIndex != -1) {
			throw new RuntimeException("Wrong target index: " + targetIndex);
		}
		double[] distT = Arrays.copyOf(tempDistTarget, m_NeighIndex);
		if (m_IsMacro) {
			mDistTarget[numNeighInd][mWhereToTarget[numNeighInd]] = ClusUtil.mean(distT);
			mWhereToTarget[numNeighInd]++;
		}
        for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) { 	
    		double[] distA = Arrays.copyOf(tempDistAttribute[attrInd], m_NeighIndex);
    		if (m_IsMacro) {
    			// compute the average distance over the neighbours and store it
    			mDistAttribute[numNeighInd][attrInd][mWhereTo[numNeighInd][attrInd]] = ClusUtil.mean(distA);
				mWhereTo[numNeighInd][attrInd]++;
    		} else {
    			// compute k
    			double k = m_IsTLS ? optimalViaTotalSquares(distA, distT) :  optimalViaStandardSquares(distA, distT);
    			m_Coefficient[targetIndex + 1][numNeighInd][attrInd] += k;
    		}
//    		if (tempNormX[attrInd] == 0) {
//        		m_NbConstant[numNeighInd][attrInd] += 1.0;
//        	}
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
		double k;
		if (m_IsMacro) {
			if (mWhereTo[nbNeighInd][attrInd] + m_NbConstant[nbNeighInd][attrInd] != mDistAttribute[nbNeighInd][attrInd].length) {
				throw new RuntimeException(String.format("Something went wrong with the indices: %d + %d != %d", mWhereTo[nbNeighInd][attrInd], m_NbConstant[nbNeighInd][attrInd], mDistAttribute[nbNeighInd][attrInd].length));
			}
			if (mWhereToTarget[nbNeighInd] != mDistTarget[nbNeighInd].length) {
				throw new RuntimeException(String.format("Something went wrong with the indices: %d != %d", mWhereToTarget[nbNeighInd], mDistTarget[nbNeighInd].length));
			}
			double[] distA = Arrays.copyOf(mDistAttribute[nbNeighInd][attrInd], mWhereTo[nbNeighInd][attrInd]);
			double[] distT = mDistTarget[nbNeighInd];
			k = m_IsTLS ? optimalViaTotalSquares(distA, distT) :  optimalViaStandardSquares(distA, distT);
		} else {
			k = m_Coefficient[targetIndex + 1][nbNeighInd][attrInd] / (successfulItearions[targetIndex + 1] - m_NbConstant[nbNeighInd][attrInd]);
		}
		// This is only normalisation ...
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
		DoubleMatrix A = new DoubleMatrix(new double[][] {xs}).transpose(); // , ones
		DoubleMatrix b = new DoubleMatrix(ys);
		DoubleMatrix solution = Solve.solveLeastSquares(A, b);  // [k; b] where y = k x + b
		return Math.max(solution.get(0, 0), 0.0); 		
	}
	
//	public static void main(String[] a) {		
//		int N = 30607;
//		int M = 2;// 3200;
//		int J = 3;//11;
//		int K = 3;//160;
//		int n_we = K * J + 1 + M;
//		System.out.println("Start: " + System.currentTimeMillis());
//		SimpleMatrix A = SimpleMatrix.random_DDRM(N * J, n_we, 0.0, 1.0, new Random()); // DoubleMatrix.rand(N * J, n_we);
//		SimpleMatrix b = SimpleMatrix.random_DDRM(N * J, 1, 0.0, 1.0, new Random()); //DoubleMatrix.rand(N * J, 1);
//		
//		for (int n = 0; n < N; n++) {
//			SimpleMatrix P = SimpleMatrix.random_DDRM(n_we, J, 0.0, 1.0, new Random()); // DoubleMatrix.rand(n_we, J);
//			updateRange(P, 0, K * J, 0, J, 0.0);
//			// P.put(range(K * J), range(J), DoubleMatrix.zeros(K * J, J));
//			for (int j = 0; j < J; j++) {
//				// P.put(range(j * K, (j + 1) * K), range(j, j + 1), DoubleMatrix.rand(K, 1));
//				updateRange(P, j * K, (j + 1) * K, j, j + 1, new Random());
//			}
//			// A.put(range(n * J, (n + 1) * J), range(n_we), P.transpose());
//			updateRange(A, n * J, 0, P.transpose());
//		}
//		// DoubleMatrix solution = Solve.solveLeastSquares(A, b);
//		AdjLinearSolverQr_DDRM s = new AdjLinearSolverQr_DDRM();
//		s.setA(A.getDDRM());
//		DMatrixRMaj x = new DMatrixRMaj(N * J, 1);
//		s.solve(b.getDDRM(), x);
//		System.out.println("Stop: " + System.currentTimeMillis());
//		
//	}
	
	public static void updateRange(SimpleMatrix m, int row0, int row1, int col0, int col1, double value) {
		for (int r = row0; r < row1; r++) {
			for (int c = col0; c < col1; c++) {
				m.set(r, c, value);
			}
		}
	}
	
	public static void updateRange(SimpleMatrix m, int row0, int row1, int col0, int col1, Random random) {
		for (int r = row0; r < row1; r++) {
			for (int c = col0; c < col1; c++) {
				m.set(r, c, random.nextDouble());
			}
		}
	}
	
	public static void updateRange(SimpleMatrix m, int row0, int col0, SimpleMatrix values) {
		for (int r = 0; r < values.numRows(); r++) {
			for (int c = 0; c < values.numCols(); c++) {
				m.set(r + row0, c + col0, values.get(r,  c));
			}
		}
	}
	
	public static int[] range(int i) {
		return IntStream.rangeClosed(0, i - 1).toArray();
	}
	
	public static int[] range(int i, int j) {
		return IntStream.rangeClosed(i, j - 1).toArray();
	}
}

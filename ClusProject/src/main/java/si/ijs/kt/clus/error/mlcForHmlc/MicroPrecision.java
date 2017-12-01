
package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.util.ClusUtil;

public class MicroPrecision implements MlcHmlcSubError {

	protected int[] m_NbTruePositives, m_NbFalsePositives;

	public MicroPrecision(int dim) {
		m_NbTruePositives = new int[dim];
		m_NbFalsePositives = new int[dim];
	}

	@Override
	public double getModelError(int dim) {
		int truePositives = 0, falsePositives = 0;
		for (int i = 0; i < dim; i++) {
			truePositives += m_NbTruePositives[i];
			falsePositives += m_NbFalsePositives[i];
		}

		if (ClusUtil.isZero(truePositives + falsePositives)) {
			return ClusUtil.NaN;
		} else {
			return ((double) truePositives) / (truePositives + falsePositives);
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		int dim = actual.length;
		for (int i = 0; i < dim; i++) {
			if (predictedThresholded[i]) { // predicted positive
				if (actual[i]) {
					m_NbTruePositives[i]++;
				} else {
					m_NbFalsePositives[i]++;
				}
			}
		}
	}

	@Override
	public String getName() {
		return "MicroPrecision";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		MicroPrecision o = (MicroPrecision) other;

		for (int i = 0; i < m_NbTruePositives.length; i++) {
			m_NbTruePositives[i] += o.m_NbTruePositives[i];
			m_NbFalsePositives[i] += o.m_NbFalsePositives[i];
		}
	}
}

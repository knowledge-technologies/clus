
package si.ijs.kt.clus.error.mlcForHmlc;

import java.io.Serializable;

import si.ijs.kt.clus.util.ClusUtil;

public class MicroFOne implements MlcHmlcSubError, Serializable {

	protected int[] m_NbTruePositives, m_NbFalsePositives, m_NbFalseNegatives;

	public MicroFOne(int dim) {
		m_NbTruePositives = new int[dim];
		m_NbFalsePositives = new int[dim];
		m_NbFalseNegatives = new int[dim];
	}

	@Override
	public double getModelError(int dim) {
		int truePositives = 0, falsePositives = 0, falseNegatives = 0;
		for (int i = 0; i < dim; i++) {
			truePositives += m_NbTruePositives[i];
			falsePositives += m_NbFalsePositives[i];
			falseNegatives += m_NbFalseNegatives[i];
		}
		double precision = ((double) truePositives) / (truePositives + falsePositives);
		double recall = ((double) truePositives) / (truePositives + falseNegatives);

		precision = ClusUtil.isNaNOrZero(precision) ? ClusUtil.NaN : precision;
		recall = ClusUtil.isNaNOrZero(recall) ? ClusUtil.NaN : recall;

		if (ClusUtil.isNaNOrZero(precision + recall)) {
			return ClusUtil.NaN;
		} else {
			return 2.0 * precision * recall / (precision + recall);
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		int dim = actual.length;
		for (int i = 0; i < dim; i++) {
			if (actual[i]) { // label relevant
				if (predictedThresholded[i]) {
					m_NbTruePositives[i]++;
				} else {
					m_NbFalseNegatives[i]++;
				}
			} else if (predictedThresholded[i]) {
				m_NbFalsePositives[i]++;
			}
		}
	}

	@Override
	public String getName() {
		return "MicroFOne";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		MicroFOne o = (MicroFOne) other;

		for (int i = 0; i < m_NbTruePositives.length; i++) {
			m_NbTruePositives[i] += o.m_NbTruePositives[i];
			m_NbFalsePositives[i] += o.m_NbFalsePositives[i];
			m_NbFalseNegatives[i] += o.m_NbFalseNegatives[i];
		}
	}
}

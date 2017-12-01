
package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.util.ClusUtil;

public class MacroRecall implements ComponentError, MlcHmlcSubError {

	protected int[] m_NbTruePositives, m_NbFalseNegatives;

	public MacroRecall(int dim) {
		m_NbTruePositives = new int[dim];
		m_NbFalseNegatives = new int[dim];
	}

	@Override
	public double getModelError(int dim) {
		double avg = 0.0;
		for (int i = 0; i < dim; i++) {
			avg += getModelErrorComponent(i);
		}
		return avg / dim;
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
			}
		}
	}

	@Override
	public String getName() {
		return "MacroRecall";
	}

	@Override
	public double getModelErrorComponent(int i) {

		if (ClusUtil.isNaNOrZero(m_NbTruePositives[i] + m_NbFalseNegatives[i])) {
			return ClusUtil.NaN;
		} else {
			return ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalseNegatives[i]);
		}
	}

	@Override
	public void add(MlcHmlcSubError other) {
		MacroRecall o = (MacroRecall) other;

		for (int i = 0; i < m_NbTruePositives.length; i++) {
			m_NbTruePositives[i] += o.m_NbTruePositives[i];
			m_NbFalseNegatives[i] += o.m_NbFalseNegatives[i];
		}
	}
}

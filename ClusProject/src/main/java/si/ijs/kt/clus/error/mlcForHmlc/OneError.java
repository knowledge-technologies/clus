
package si.ijs.kt.clus.error.mlcForHmlc;

import java.io.Serializable;

import si.ijs.kt.clus.util.ClusUtil;

public class OneError implements MlcHmlcSubError, Serializable {

	private int m_NbWrong;
	private int m_NbKnown;

	public OneError() {
		m_NbWrong = 0;
		m_NbKnown = 0;
	}

	@Override
	public double getModelError(int dim) {
		if (ClusUtil.isZero(m_NbKnown)) {
			return ClusUtil.NaN;
		} else {
			return ((double) m_NbWrong) / m_NbKnown;
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		double[] scores = predicted;
		int maxScoreLabel = -1;
		double maxScore = -1.0; // something < 0
		for (int i = 0; i < actual.length; i++) {
			if (scores[i] > maxScore) {
				maxScoreLabel = i;
				maxScore = scores[i];
			}
		}
		if (maxScoreLabel >= 0) { // at least one label value is non-missing
			if (actual[maxScoreLabel] != predictedThresholded[maxScoreLabel]) {
				m_NbWrong++;
			}
			m_NbKnown++;
		}
	}

	@Override
	public String getName() {
		return "OneError";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		OneError o = (OneError) other;
		m_NbWrong += o.m_NbWrong;
		m_NbKnown += o.m_NbKnown;
	}
}


package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.util.ClusUtil;

public class MLAccuracy implements MlcHmlcSubError {

	private double m_JaccardSum;
	private int m_NbKnown;

	public MLAccuracy() {
		m_JaccardSum = 0.0;
		m_NbKnown = 0;
	}

	@Override
	public double getModelError(int dim) {
		if (ClusUtil.isZero(m_NbKnown)) {
			return ClusUtil.NaN;
		} else {
			return m_JaccardSum / m_NbKnown;
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		int intersection = 0, union = 0;
		for (int i = 0; i < actual.length; i++) {
			if (actual[i] && predictedThresholded[i]) { // both relevant
				union++;
				intersection++;
			} else if (actual[i] || predictedThresholded[i]) { // precisely one relevant
				union++;
			}
		}
		m_JaccardSum += union != 0 ? ((double) intersection) / union : 1.0; // take care of the degenerated case
		m_NbKnown++;
	}

	@Override
	public String getName() {
		return "MLAccuracy";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		MLAccuracy o = (MLAccuracy) other;
		m_JaccardSum += o.m_JaccardSum;
		m_NbKnown += o.m_NbKnown;
	}
}

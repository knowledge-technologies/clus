
package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.util.ClusUtil;

public class HammingLoss implements MlcHmlcSubError {

	private int m_NbWrong;
	private int m_NbKnown;

	public HammingLoss() {
		m_NbWrong = 0;
		m_NbKnown = 0;
	}

	@Override
	public double getModelError(int dimensions) {
		if (ClusUtil.isZero(m_NbKnown)) {
			return ClusUtil.NaN;
		} else {
			return ((double) m_NbWrong) / m_NbKnown / dimensions;
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		for (int i = 0; i < actual.length; i++) {
			if (actual[i] != predictedThresholded[i]) {
				m_NbWrong += 1;
			}
		}
		m_NbKnown++;
	}

	@Override
	public String getName() {
		return "HammingLoss";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		m_NbWrong += ((HammingLoss) other).m_NbWrong;
		m_NbKnown += ((HammingLoss) other).m_NbKnown;
	}
}


package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.util.ClusUtil;

public class Coverage implements MlcHmlcSubError {

	protected int m_RankSum;
	protected int m_NbKnown;
	protected int m_NbRelevantLabels;

	public Coverage() {
		m_RankSum = 0;
		m_NbKnown = 0;
		m_NbRelevantLabels = 0;
	}

	@Override
	public double getModelError(int dim) {
		if (ClusUtil.isZero(m_NbKnown)) {
			return ClusUtil.NaN;
		} else {
			return ((double) m_RankSum) / m_NbKnown;
		}

	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		double[] scores = predicted;
		int dim = predicted.length;
		double minScore = Double.POSITIVE_INFINITY;
		int minScoreLabel = -1;
		int relevantLabels = 0;
		for (int i = 0; i < dim; i++) {
			if (actual[i]) { // label is relevant
				relevantLabels++;
				if (minScore > scores[i]) {
					minScore = scores[i];
					minScoreLabel = i;
				}
			}
		}
		if (minScoreLabel >= 0) { // at least one relevant
			int rank = 0; // should be 1, but we will add this 1 when i == minScoreLabel
			for (int i = 0; i < dim; i++) {
				if (scores[i] >= scores[minScoreLabel]) {
					rank++;
				}
			}
			m_RankSum += rank;
			m_NbKnown++;
			m_NbRelevantLabels += relevantLabels;
		}
	}

	@Override
	public String getName() {
		return "Coverage";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		m_RankSum += ((Coverage) other).m_RankSum;
		m_NbKnown += ((Coverage) other).m_NbKnown;
		m_NbRelevantLabels += ((Coverage) other).m_NbRelevantLabels;
	}

}

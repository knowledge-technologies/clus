
package si.ijs.kt.clus.error.mlcForHmlc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import si.ijs.kt.clus.util.ClusUtil;

public class RankingLoss implements MlcHmlcSubError, Serializable {

	private double m_NonnormalisedLoss;
	private int m_NbKnown;

	public RankingLoss() {
		m_NonnormalisedLoss = 0.0;
		m_NbKnown = 0;
	}

	@Override
	public double getModelError(int dim) {
		if (ClusUtil.isZero(m_NbKnown)) {
			return ClusUtil.NaN;
		} else {
			return m_NonnormalisedLoss / m_NbKnown;
		}
	}

	@Override
	public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
		final double[] scores = predicted;
		int wrongPairs = 0;
		int nbIrrelevant = 0, nbRelevant = 0;
		ArrayList<Integer> indicesOfKnownValues = new ArrayList<Integer>();
		for (int i = 0; i < actual.length; i++) {
			indicesOfKnownValues.add(i);
		}

		Collections.sort(indicesOfKnownValues, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return -Double.compare(scores[o1], scores[o2]);
			}
		});

		for (int i = 0; i < indicesOfKnownValues.size(); i++) { // possible improvement: break, when you reach the
																// relevant label with the lowest score
			if (actual[indicesOfKnownValues.get(i)]) {
				wrongPairs += nbIrrelevant;
				nbRelevant++;
			} else {
				nbIrrelevant++;
			}
		}
		if (nbRelevant > 0 && nbIrrelevant > 0) {
			m_NonnormalisedLoss += ((double) wrongPairs) / (nbRelevant * nbIrrelevant);
		}
		m_NbKnown++;

	}

	@Override
	public String getName() {
		return "RankingLoss";
	}

	@Override
	public void add(MlcHmlcSubError other) {
		RankingLoss o = (RankingLoss) other;
		m_NonnormalisedLoss += o.m_NonnormalisedLoss;
		m_NbKnown += o.m_NbKnown;
	}
}

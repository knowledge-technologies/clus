package clus.heuristic;

import clus.statistic.ClusStatistic;

public class ClusStopCriterionMinNbExamples implements ClusStopCriterion {

	protected int m_MinExamples;

	public ClusStopCriterionMinNbExamples(int minExamples) {
		m_MinExamples = minExamples;
	}

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
		int n_pos = pstat.getNbExamples();
		int n_neg = tstat.getNbExamples() - n_pos;
		return n_pos < m_MinExamples || n_neg < m_MinExamples;
	}

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
		for (int i = 0; i < nbsplit; i++) {
			if (pstat[i].getNbExamples() < m_MinExamples) {
				return true;
			}
		}
		return false;
	}
}

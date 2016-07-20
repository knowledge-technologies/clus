package clus.heuristic;

import clus.statistic.ClusStatistic;

public class ClusStopCriterionMinWeight implements ClusStopCriterion {

	protected double m_MinWeight;

	public ClusStopCriterionMinWeight(double minWeight) {
		m_MinWeight = minWeight;
	}

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
		double w_pos = pstat.getTotalWeight();
		double w_neg = tstat.getTotalWeight() - w_pos;
		return w_pos < m_MinWeight || w_neg < m_MinWeight;
	}

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
		for (int i = 0; i < nbsplit; i++) {
			if (pstat[i].getTotalWeight() < m_MinWeight) {
				return true;
			}
		}
		return false;
	}
}

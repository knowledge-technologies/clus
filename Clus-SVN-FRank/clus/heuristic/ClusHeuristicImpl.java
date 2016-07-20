package clus.heuristic;

import clus.statistic.ClusStatistic;

public abstract class ClusHeuristicImpl extends ClusHeuristic {

	protected ClusStatistic m_NegStat;

	public ClusHeuristicImpl(ClusStatistic negstat) {
		m_NegStat = negstat;
	}

	public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
		m_NegStat.copy(tstat);
		m_NegStat.subtractFromThis(pstat);
		return calcHeuristic(tstat, pstat, m_NegStat, missing);
	}

	public abstract double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic nstat, ClusStatistic missing);
}

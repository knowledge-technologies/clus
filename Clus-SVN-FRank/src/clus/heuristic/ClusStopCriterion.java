package clus.heuristic;

import clus.statistic.ClusStatistic;

public interface ClusStopCriterion {

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing);

	public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit);

}

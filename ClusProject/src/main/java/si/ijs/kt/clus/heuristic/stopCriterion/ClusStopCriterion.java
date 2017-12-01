
package si.ijs.kt.clus.heuristic.stopCriterion;

import si.ijs.kt.clus.statistic.ClusStatistic;


public interface ClusStopCriterion {

    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing);


    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit);

}

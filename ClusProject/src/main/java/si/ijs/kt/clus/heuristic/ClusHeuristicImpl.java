
package si.ijs.kt.clus.heuristic;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;


public abstract class ClusHeuristicImpl extends ClusHeuristic {

    protected ClusStatistic m_NegStat;


    public ClusHeuristicImpl(ClusStatistic negstat, Settings sett) {
        super(sett);

        m_NegStat = negstat;
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        m_NegStat.copy(tstat);
        m_NegStat.subtractFromThis(pstat);
        return calcHeuristic(tstat, pstat, m_NegStat, missing);
    }


    public abstract double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic nstat, ClusStatistic missing);
}

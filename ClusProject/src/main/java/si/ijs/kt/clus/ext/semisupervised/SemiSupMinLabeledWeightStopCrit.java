
package si.ijs.kt.clus.ext.semisupervised;

import si.ijs.kt.clus.heuristic.stopCriterion.ClusStopCriterion;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.CombStat;

//TODO: Jurica: This was implemented in Clus by somebody else, I don't know where it is used, or what it does 
public class SemiSupMinLabeledWeightStopCrit implements ClusStopCriterion {

    protected double m_MinWeight;


    public SemiSupMinLabeledWeightStopCrit(double minWeight) {
        m_MinWeight = minWeight;
    }


    @Override
    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        CombStat ctstat = (CombStat) tstat;
        CombStat cpstat = (CombStat) pstat;
        int lastClass = ctstat.getNbNominalAttributes() - 1;
        double w_pos = cpstat.getClassificationStat().getSumWeight(lastClass);
        double w_neg = ctstat.getClassificationStat().getSumWeight(lastClass) - w_pos;
        return w_pos < m_MinWeight || w_neg < m_MinWeight;
    }


    @Override
    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
        CombStat ctstat = (CombStat) tstat;
        int lastClass = ctstat.getNbNominalAttributes() - 1;
        for (int i = 0; i < nbsplit; i++) {
            CombStat cstat = (CombStat) pstat[i];
            if (cstat.getClassificationStat().getSumWeight(lastClass) < m_MinWeight) { return true; }
        }
        return false;
    }
}
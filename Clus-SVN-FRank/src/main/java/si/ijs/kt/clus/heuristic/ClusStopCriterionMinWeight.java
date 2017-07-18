
package si.ijs.kt.clus.heuristic;

import si.ijs.kt.clus.statistic.ClusStatistic;


public class ClusStopCriterionMinWeight implements ClusStopCriterion {

    protected double m_MinWeight;


    public ClusStopCriterionMinWeight(double minWeight) {
        m_MinWeight = minWeight;
    }


    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        double w_pos = pstat.getTotalWeight();
        double w_neg = tstat.getTotalWeight() - w_pos;
        double wt_pos = pstat.getTargetSumWeights(); //weight of target atts
        double wt_neg = tstat.getTargetSumWeights() - wt_pos; //weight of target atts
        // FIXME: Can these conditions be written nicer?
        return w_pos < m_MinWeight || //weight of examples in the positive branch must be >= minWeight
                w_neg < m_MinWeight || //weight of examples in the negative branch must be >= minWeight
                (wt_pos == 0 && wt_neg < m_MinWeight) || //allows weight 0 of labeled examples in the positive branch, if the weight of labeled in negative branch is >= minWeight
                (wt_neg == 0 && wt_pos < m_MinWeight) || //allows weight 0 of labeled examples in the negative branch, if the weight of labeled in positive branch is >= minWeight
                (wt_pos > 0 && wt_pos < m_MinWeight) || //if weight of labeled examples in positive branch is > 0, then it must be at least >= minWeight
                (wt_neg > 0 && wt_neg < m_MinWeight); //if weight of labeled examples in negative branch is > 0, then it must be at least >= minWeight
    }


    public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
        for (int i = 0; i < nbsplit; i++) {
            if (pstat[i].getTotalWeight() < m_MinWeight) { return true; }
        }
        return false;
    }
}

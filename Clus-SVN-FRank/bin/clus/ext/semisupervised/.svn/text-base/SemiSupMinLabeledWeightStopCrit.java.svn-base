package clus.ext.semisupervised;

import clus.heuristic.ClusStopCriterion;
import clus.statistic.ClusStatistic;
import clus.statistic.CombStat;

public class SemiSupMinLabeledWeightStopCrit implements ClusStopCriterion {

		protected double m_MinWeight;

		public SemiSupMinLabeledWeightStopCrit(double minWeight) {
			m_MinWeight = minWeight;
		}

		public boolean stopCriterion(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
			CombStat ctstat = (CombStat)tstat;
			CombStat cpstat = (CombStat)pstat;
			int lastClass = ctstat.getNbNominalAttributes()-1;			
			double w_pos = cpstat.getClassificationStat().getSumWeight(lastClass);
			double w_neg = ctstat.getClassificationStat().getSumWeight(lastClass) - w_pos;
			return w_pos < m_MinWeight || w_neg < m_MinWeight;
		}

		public boolean stopCriterion(ClusStatistic tstat, ClusStatistic[] pstat, int nbsplit) {
			CombStat ctstat = (CombStat)tstat;
			int lastClass = ctstat.getNbNominalAttributes()-1;			
			for (int i = 0; i < nbsplit; i++) {
				CombStat cstat = (CombStat)pstat[i];
				if (cstat.getClassificationStat().getSumWeight(lastClass) < m_MinWeight) {
					return true;
				}
			}
			return false;
		}
}
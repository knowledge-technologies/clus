package clus.ext.hierarchical;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.type.ClusAttrType;
import clus.heuristic.ClusHeuristic;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.statistic.ClusStatistic;

public class ClusRuleHeuristicHierarchical extends ClusHeuristic {

	protected ClusAttributeWeights m_TargetWeights;
	protected ClusStatManager m_StatManager;

	public ClusRuleHeuristicHierarchical(ClusStatManager stat_mgr, ClusAttributeWeights prod) {
		m_StatManager = stat_mgr;
		m_TargetWeights = prod;
	}

	/**
	 * This heuristic calculates:
	 * ( |S|.Var(S) - |Sr|.Var(Sr) ) . Coverage(r)
	 */
	public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat,
			ClusStatistic missing) {

		double n_pos = c_pstat.m_SumWeight;
		if (n_pos-Settings.MINIMAL_WEIGHT < 1e-6) { // (n_pos < Settings.MINIMAL_WEIGHT)
			return Double.NEGATIVE_INFINITY;
		}

		// Calculate |S|.Var(S) - |Sr|.Var(Sr)

		//WHTDStatistic tstat = (WHTDStatistic) m_StatManager.getTrainSetStat(); // Geeft classcastexception (is blijkbaar een CombStat)
		//WHTDStatistic tstat = (WHTDStatistic) m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET); // (is altijd 0...)

		//WHTDStatistic tstat = (WHTDStatistic) m_StatManager.getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING);
		//double totalValue = tstat.getSS(m_TargetWeights);
		double totalValue = getTrainDataHeurValue(); // optimization of the previous two lines

		double ruleValue = c_pstat.getSVarS(m_TargetWeights);
		double value = totalValue - ruleValue;


		//System.out.println("Difference made by rule: " + totalValue + " - " + ruleValue);

		// Coverage(r) part
		double train_sum_w = m_StatManager.getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING).getTotalWeight();
	    double coverage = (n_pos/train_sum_w);
	    double cov_par = m_StatManager.getSettings().getHeurCoveragePar();
	    coverage = Math.pow(coverage, cov_par);
	    value = value * coverage;

	    //System.out.println("Totale Heuristiek: " + value + " Coverage: " + coverage);
		return value;
	}

	public String getName() {
		return "RuleHeuristicHierarchical";
	}

}

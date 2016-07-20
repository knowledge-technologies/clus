
package clus.ext.hierarchical;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.main.Settings;
import clus.statistic.*;

public class HierSumPairwiseDistancesStat extends WHTDStatistic {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected SumPairwiseDistancesStat m_PairwiseDistStat;

	public HierSumPairwiseDistancesStat(ClassHierarchy hier, ClusDistance dist, int comp) {
		super(hier, comp);
		m_PairwiseDistStat = new SumPairwiseDistancesStat(dist);
	}

	public ClusStatistic cloneStat() {
		ClusDistance dist = m_PairwiseDistStat.getDistance();
		return new HierSumPairwiseDistancesStat(m_Hier, dist, m_Compatibility);
	}

	public void setSDataSize(int nbex) {
		m_PairwiseDistStat.setSDataSize(nbex);
	}

	public double getSVarS(ClusAttributeWeights scale, RowData data) {
		return m_PairwiseDistStat.getSVarS(scale, data);
	}

	public void updateWeighted(DataTuple tuple, int idx) {
		super.updateWeighted(tuple, idx);
		m_PairwiseDistStat.updateWeighted(tuple, idx);
	}

	public void reset() {
		super.reset();
		m_PairwiseDistStat.reset();
	}

	public void copy(ClusStatistic other) {
		HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat)other;
		super.copy(or);
		m_PairwiseDistStat.copy(or.m_PairwiseDistStat);
	}

	public void add(ClusStatistic other) {
		HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat)other;
		super.add(or);
		m_PairwiseDistStat.add(or.m_PairwiseDistStat);
	}

	public void addScaled(double scale, ClusStatistic other) {
		System.err.println("HierSumPairwiseDistancesStat: addScaled not implemented");
	}

	public void subtractFromThis(ClusStatistic other) {
		HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat)other;
		super.subtractFromThis(other);
		m_PairwiseDistStat.subtractFromThis(or.m_PairwiseDistStat);
	}

	public void subtractFromOther(ClusStatistic other) {
		HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat)other;
		super.subtractFromOther(other);
		m_PairwiseDistStat.subtractFromOther(or.m_PairwiseDistStat);
	}

	public String getDistanceName() {
		return m_PairwiseDistStat.getDistanceName();
	}
}

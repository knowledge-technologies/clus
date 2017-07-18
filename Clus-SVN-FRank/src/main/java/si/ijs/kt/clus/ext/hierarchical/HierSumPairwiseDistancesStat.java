
package si.ijs.kt.clus.ext.hierarchical;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.SumPairwiseDistancesStat;


public class HierSumPairwiseDistancesStat extends WHTDStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected SumPairwiseDistancesStat m_PairwiseDistStat;


    public HierSumPairwiseDistancesStat(Settings sett, ClassHierarchy hier, ClusDistance dist, int comp) {
        super(sett, hier, comp);
        m_PairwiseDistStat = new SumPairwiseDistancesStat(sett, dist);
    }


    public ClusStatistic cloneStat() {
        ClusDistance dist = m_PairwiseDistStat.getDistance();
        return new HierSumPairwiseDistancesStat(this.m_Settings, m_Hier, dist, m_Compatibility);
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
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.copy(or);
        m_PairwiseDistStat.copy(or.m_PairwiseDistStat);
    }


    public void add(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.add(or);
        m_PairwiseDistStat.add(or.m_PairwiseDistStat);
    }


    public void addScaled(double scale, ClusStatistic other) {
        System.err.println("HierSumPairwiseDistancesStat: addScaled not implemented");
    }


    public void subtractFromThis(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.subtractFromThis(other);
        m_PairwiseDistStat.subtractFromThis(or.m_PairwiseDistStat);
    }


    public void subtractFromOther(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.subtractFromOther(other);
        m_PairwiseDistStat.subtractFromOther(or.m_PairwiseDistStat);
    }


    public String getDistanceName() {
        return m_PairwiseDistStat.getDistanceName();
    }
}
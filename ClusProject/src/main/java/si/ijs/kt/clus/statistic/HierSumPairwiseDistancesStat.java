
package si.ijs.kt.clus.statistic;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public class HierSumPairwiseDistancesStat extends WHTDStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected SumPairwiseDistancesStat m_PairwiseDistStat;


    public HierSumPairwiseDistancesStat(Settings sett, ClassHierarchy hier, ClusDistance dist) {
        super(sett, hier);
        m_PairwiseDistStat = new SumPairwiseDistancesStat(sett, dist);
    }


    @Override
    public ClusStatistic cloneStat() {
        ClusDistance dist = m_PairwiseDistStat.getDistance();
        return new HierSumPairwiseDistancesStat(this.m_Settings, m_Hier, dist);
    }


    @Override
    public void setSDataSize(int nbex) {
        m_PairwiseDistStat.setSDataSize(nbex);
    }


    @Override
    public double getSVarS(ClusAttributeWeights scale, RowData data) throws ClusException {
        return m_PairwiseDistStat.getSVarS(scale, data);
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        super.updateWeighted(tuple, idx);
        m_PairwiseDistStat.updateWeighted(tuple, idx);
    }


    @Override
    public void reset() {
        super.reset();
        m_PairwiseDistStat.reset();
    }


    @Override
    public void copy(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.copy(or);
        m_PairwiseDistStat.copy(or.m_PairwiseDistStat);
    }


    @Override
    public void add(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.add(or);
        m_PairwiseDistStat.add(or.m_PairwiseDistStat);
    }


    @Override
    public void addScaled(double scale, ClusStatistic other) {
        System.err.println("HierSumPairwiseDistancesStat: addScaled not implemented");
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.subtractFromThis(other);
        m_PairwiseDistStat.subtractFromThis(or.m_PairwiseDistStat);
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        HierSumPairwiseDistancesStat or = (HierSumPairwiseDistancesStat) other;
        super.subtractFromOther(other);
        m_PairwiseDistStat.subtractFromOther(or.m_PairwiseDistStat);
    }


    @Override
    public String getDistanceName() {
        return m_PairwiseDistStat.getDistanceName();
    }
}

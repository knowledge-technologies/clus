
package si.ijs.kt.clus.ext.beamsearch;

import java.util.ArrayList;

import org.apache.commons.lang.NotImplementedException;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsBeamSearch;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;


public class ClusBeamSimRegrStat extends RegressionStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public double[] m_SumPredictions;
    public double[] m_SumSqPredictions;
    private ClusBeam m_Beam;


    public ClusBeamSimRegrStat(Settings sett, NumericAttrType[] attrs, ClusBeam beam) {
        super(sett, attrs);
        
        m_SumPredictions = new double[m_NbAttrs];
        m_SumSqPredictions = new double[m_NbAttrs];
        m_Beam = beam;
    }


    public ClusBeamSimRegrStat(Settings sett, NumericAttrType[] attrs, boolean onlymean, ClusBeam beam) {
        super(sett, attrs, onlymean);
        
        if (!onlymean) {
            m_SumPredictions = new double[m_NbAttrs];
            m_SumSqPredictions = new double[m_NbAttrs];
            m_Beam = beam;
        }
    }


    @Override
    public void calcMean(double[] means) {
        super.calcMean(means);
    }


    @Override
    public double getMean(int i) {
        return super.getMean(i);
    }


    @Override
    public double getSVarS(int i) {
        return super.getSVarS(i);
    }


    @Override
    public void add(ClusStatistic other) {
        super.add(other);
        ClusBeamSimRegrStat or = (ClusBeamSimRegrStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumPredictions[i] += or.m_SumPredictions[i];
            m_SumSqPredictions[i] += or.m_SumSqPredictions[i];
        }
    }


    public void updateWeighted(SparseDataTuple tuple, double weight) {
        throw new NotImplementedException();
    }


    @Override
    public void updateWeighted(DataTuple tuple, double weight) {
        super.updateWeighted(tuple, weight);
        ArrayList<ClusBeamModel> models = m_Beam.toArray();
        double[] vals = new double[m_NbAttrs];
        double[] valssq = new double[m_NbAttrs];
        for (int k = 0; k < models.size(); k++) {
            ClusBeamModel cbm = models.get(k);
            RegressionStat rstat = (RegressionStat) cbm.getPredictionForTuple(tuple);
            for (int i = 0; i < m_NbAttrs; i++) {
                vals[i] += rstat.getMean(i);
                valssq[i] += rstat.getMean(i) * rstat.getMean(i);
            }
        }
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumPredictions[i] += weight * vals[i];
            m_SumSqPredictions[i] += weight * valssq[i];
        }
    }


    @Override
    public double getSVarS(ClusAttributeWeights scale) {
        double result = super.getSVarS(scale);
        double similarity = 0.0;
        for (int i = 0; i < m_NbAttrs; i++) {
            double mean = super.getMean(i);
            double firstterm = m_SumWeight * m_Beam.getCrWidth() * mean * mean;
            double secondterm = 2 * mean * getSumPredictions(i);
            double thirdterm = getSumSqPredictions(i);
            similarity += firstterm - secondterm + thirdterm;
            similarity *= scale.getWeight(m_Attrs[i]);// apply same weights as for normal mode
        }
        similarity /= m_NbAttrs; // average for the targets
        similarity /= m_Beam.getCrWidth(); // average for the beam size
        // beta times similarity
        result += SettingsBeamSearch.BEAM_SIMILARITY * similarity;
        return result;
    }


    @Override
    public ClusStatistic cloneStat() {
        return new ClusBeamSimRegrStat(this.m_Settings, m_Attrs, false, m_Beam);
    }


    @Override
    public ClusStatistic cloneSimple() {
        return new ClusBeamSimRegrStat(this.m_Settings, m_Attrs, true, m_Beam);
    }


    @Override
    public void copy(ClusStatistic other) {
        super.copy(other);
        ClusBeamSimRegrStat or = (ClusBeamSimRegrStat) other;
        System.arraycopy(or.m_SumPredictions, 0, m_SumPredictions, 0, m_NbAttrs);
        System.arraycopy(or.m_SumSqPredictions, 0, m_SumSqPredictions, 0, m_NbAttrs);
    }


    @Override
    public String getString(StatisticPrintInfo info) {
        return super.getString(info);
    }


    @Override
    public void reset() {
        super.reset();
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumPredictions[i] = 0.0;
            m_SumSqPredictions[i] = 0.0;
        }
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        super.subtractFromOther(other);
        ClusBeamSimRegrStat or = (ClusBeamSimRegrStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumPredictions[i] = or.m_SumPredictions[i] - m_SumPredictions[i];
            m_SumSqPredictions[i] = or.m_SumSqPredictions[i] - m_SumSqPredictions[i];
        }
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        super.subtractFromThis(other);
        ClusBeamSimRegrStat or = (ClusBeamSimRegrStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumPredictions[i] -= or.m_SumPredictions[i];
            m_SumSqPredictions[i] -= or.m_SumSqPredictions[i];
        }
    }


    public double getSumPredictions(int i) {
        return m_SumPredictions[i];
    }


    public double[] getSumPredictions() {
        return m_SumPredictions;
    }


    public double getSumSqPredictions(int i) {
        return m_SumSqPredictions[i];
    }


    public double[] getSumSqPredictions() {
        return m_SumSqPredictions;
    }


    @Override
    public void setBeam(ClusBeam beam) {
        m_Beam = beam;
    }
}

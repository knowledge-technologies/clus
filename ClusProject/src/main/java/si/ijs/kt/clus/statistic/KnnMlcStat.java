package si.ijs.kt.clus.statistic;

import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;

public class KnnMlcStat extends ClassificationStat {

	public KnnMlcStat(Settings sett, NominalAttrType[] nomAtts) {
		super(sett, nomAtts);
	}

	public KnnMlcStat(Settings sett, NominalAttrType[] nomAtts, INIFileNominalOrDoubleOrVector multiLabelThreshold) {
		super(sett, nomAtts, multiLabelThreshold);
	}
	
	/**
	 * @param 
	 */
    @Override
    public void updateWeighted(DataTuple quasiTuple, double quasiWeight) {
        m_NbExamples++;
        m_SumWeight += quasiWeight;
        boolean hasLabel = false;
        for (int i = 0; i < m_NbTarget; i++) {
            int val = m_Attrs[i].getNominal(quasiTuple);
            if (val != m_Attrs[i].getNbValues()) {
                m_ClassCounts[i][val] += quasiWeight;
                m_SumWeights[i] += quasiWeight;

                if (!hasLabel && m_Attrs[i].isTarget())
                    hasLabel = true;
            }
        }
        if (hasLabel)
            m_SumWeightLabeled += quasiWeight;
    }
    
    @Override
    public ClusStatistic cloneStat() {
    	KnnMlcStat res = new KnnMlcStat(m_Settings, m_Attrs);
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        if (m_Thresholds != null) {
            res.m_Thresholds = Arrays.copyOf(m_Thresholds, m_Thresholds.length);
        }
        return res;
    }

}

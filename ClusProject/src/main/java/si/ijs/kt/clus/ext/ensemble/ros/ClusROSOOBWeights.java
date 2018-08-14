
package si.ijs.kt.clus.ext.ensemble.ros;

import java.util.HashMap;

import si.ijs.kt.clus.ext.ensemble.ClusOOBWeights;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSVotingType;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleVotingType;


/**
 * Class that holds OOB weights for ROS ensembles.
 * 
 * @author martinb
 */
public class ClusROSOOBWeights extends ClusOOBWeights {

    private ClusROSForestInfo m_ROSForestInfo = null;
    private EnsembleROSVotingType m_ROSVotingType = null;


    public ClusROSOOBWeights(EnsembleVotingType et, EnsembleROSVotingType rosVT) {
        super(et);

        m_ROSVotingType = rosVT;
    }


    @Override
    protected void calculateAggregateWeights() {
        // aggregated errors (calculated very similarly as component errors but not with the same numbers!)

        if (m_ROSVotingType.equals(EnsembleROSVotingType.TotalAveraging)) {
            super.calculateAggregateWeights();
        }
        else {
            m_ComponentWeights = new HashMap<>();

            double[] componentSums = new double[m_ComponentErrors.get(0).length];
            double[] vals = null;

            for (int j = 0; j < m_ComponentErrors.size(); j++) {
                ClusROSModelInfo info = m_ROSForestInfo.getROSModelInfo(j);
                for (int i : info.getTargets()) {
                    componentSums[i] += 1 / m_AggregatedError.get(j);
                }
            }

            for (int j = 0; j < m_AggregatedError.size(); j++) {
                vals = new double[m_ComponentErrors.get(j).length];

                for (int i = 0; i < vals.length; i++) {
                    vals[i] = 1 / m_AggregatedError.get(j) / componentSums[i];
                }
                m_ComponentWeights.put(j, vals);
            }
        }
    }


    @Override
    protected void calculateComponentWeights() {
        // component-wise errors

        m_ComponentWeights = new HashMap<>();

        double[] componentSums = new double[m_ComponentErrors.get(0).length];
        double[] components = null, vals = null;

        for (int j = 0; j < m_ComponentErrors.size(); j++) {
            components = m_ComponentErrors.get(j);

            ClusROSModelInfo info = m_ROSForestInfo.getROSModelInfo(j);
            for (int i : info.getTargets()) {
                componentSums[i] += 1 / components[i];
            }
        }

        for (int j = 0; j < m_ComponentErrors.size(); j++) {
            vals = new double[m_ComponentErrors.get(j).length];
            components = m_ComponentErrors.get(j);
            for (int i = 0; i < vals.length; i++) {
                vals[i] = 1 / components[i] / componentSums[i];
            }
            m_ComponentWeights.put(j, vals);
        }
    }


    public ClusROSOOBWeights getNew(int numberOfModels, ClusROSForestInfo info) {

        m_ROSForestInfo = info;

        if (numberOfModels == m_AggregatedError.size()) {
            if (m_AggregatedWeight == null || m_ComponentWeights == null) {
                calculateWeights();
            }
            return this;
        }

        ClusROSOOBWeights weights = new ClusROSOOBWeights(m_EnsembleVotingType, m_ROSVotingType);
        for (int i = 0; i < numberOfModels; i++) {
            weights.setErrors(i, m_AggregatedError.get(i), m_ComponentErrors.get(i));
        }

        weights.m_ROSForestInfo = m_ROSForestInfo.getNew(numberOfModels);

        weights.calculateWeights();

        return weights;
    }
}

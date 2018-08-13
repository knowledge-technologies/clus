
package si.ijs.kt.clus.ext.ensemble.ros;

import java.util.ArrayList;
import java.util.HashMap;

import si.ijs.kt.clus.ext.ensemble.ClusOOBWeights;


public class ClusROSOOBWeights extends ClusOOBWeights {

    private ClusROSForestInfo m_ROSForestInfo = null;


    public ClusROSOOBWeights() {
    }


    public void setROSForestInfo(ClusROSForestInfo info) {
        m_ROSForestInfo = info;
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


    @Override
    public ClusROSOOBWeights getNew(int numberOfModels) {

        if (numberOfModels == m_AggregatedError.size()) {
            if (m_AggregatedWeight == null || m_ComponentWeights == null) {
                if (m_ROSForestInfo != null) {
                    calculateWeights();
                }
            }
            return this;
        }

        ClusROSOOBWeights weights = new ClusROSOOBWeights();
        for (int i = 0; i < numberOfModels; i++) {
            weights.setErrors(i, m_AggregatedError.get(i), m_ComponentErrors.get(i));
            if (m_ROSForestInfo != null) {
                weights.setROSForestInfo(m_ROSForestInfo.getNew(numberOfModels));
            }
        }
        if (m_ROSForestInfo != null) {
            weights.calculateWeights();
        }

        return weights;
    }


    @Override
    public double getComponentWeight(int baseModelNumber, int component) {
        return m_ComponentWeights.get(baseModelNumber)[component];
    }


    public double getModelWeight(int baseModelNumber, ArrayList<Integer> indices) {

        return 1d;
    }
}


package si.ijs.kt.clus.ext.ensemble;

import java.util.HashMap;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleVotingType;


/**
 * Class that holds OOB weights.
 * Used for ensemble voting.
 * 
 * @author martinb
 */
public class ClusOOBWeights {

    /* Error averaged over all component errors */
    protected HashMap<Integer, Double> m_AggregatedError;
    /* Component-wise errors */
    protected HashMap<Integer, double[]> m_ComponentErrors;

    /* Weights calculated from averaged errors */
    protected HashMap<Integer, Double> m_AggregatedWeight;

    /* Weights calculated from component-wise errors */
    protected HashMap<Integer, double[]> m_ComponentWeights;

    /* Selected voting type */
    protected EnsembleVotingType m_EnsembleVotingType = null;

    /* Alpha parameter. Used to add additional weight to attributes. */
    protected double m_Alpha = 1d;


    public ClusOOBWeights(EnsembleVotingType et) {
        m_EnsembleVotingType = et;
        m_AggregatedError = new HashMap<>();
        m_ComponentErrors = new HashMap<>();
    }


    public ClusOOBWeights getNew(int numberOfModels) {
        if (numberOfModels == m_AggregatedError.size()) {
            if (m_AggregatedWeight == null || m_ComponentWeights == null) {
                calculateWeights();
            }
            return this;
        }

        ClusOOBWeights weights = new ClusOOBWeights(m_EnsembleVotingType);
        for (int i = 0; i < numberOfModels; i++) {
            weights.setErrors(i, m_AggregatedError.get(i), m_ComponentErrors.get(i));
        }
        weights.calculateWeights();

        return weights;
    }


    public void setErrors(int baseModelNumber, double aggregatedError, double[] componentErrors) {
        m_AggregatedError.put(baseModelNumber, aggregatedError);
        m_ComponentErrors.put(baseModelNumber, componentErrors);
    }


    public void setErrors(int baseModelNumber, ClusError error) {
        double[] components = new double[error.getDimension()];

        for (int i = 0; i < components.length; i++) {
            components[i] = error.getModelErrorComponent(i);
        }

        setErrors(baseModelNumber, error.getModelError(), components);
    }


    /**
     * Normalize errors to get weights
     * 
     * Loss functions (less is better):
     * => RMSE for regression
     * => 1-CA for classification
     * 
     * Weights are proportionate to the loss function: 1/error
     */
    public void calculateWeights() {
        switch (m_EnsembleVotingType) {
            case OOBModelWeighted:
                calculateAggregateWeights();
                break;

            case OOBTargetWeighted:
                calculateComponentWeights();
                break;

            default:
                throw new RuntimeException("Selected voting scheme is not OOB-based.");
        }

    }


    protected void calculateAggregateWeights() {
        // aggregated errors

        m_AggregatedWeight = new HashMap<>();

        double sum = m_AggregatedError.values().parallelStream().mapToDouble(d -> 1 / d).sum();
        for (int i = 0; i < m_AggregatedError.size(); i++) {
            m_AggregatedWeight.put(i, 1 / m_AggregatedError.get(i) / sum);
        }
    }


    protected void calculateComponentWeights() {
        // component-wise errors

        m_ComponentWeights = new HashMap<>();

        double[] componentSums = new double[m_ComponentErrors.get(0).length];
        double[] components = null, vals = null;

        for (int j = 0; j < m_ComponentErrors.size(); j++) {
            components = m_ComponentErrors.get(j);
            for (int i = 0; i < components.length; i++) {
                if (components[i] > 0d) {
                    componentSums[i] += 1 / components[i];
                }
                else {
                    componentSums[i] += 1 / ClusHeuristic.DELTA; // if model error is zero (good) then give a large weight to that model on that target
                }
            }
        }

        for (int j = 0; j < m_ComponentErrors.size(); j++) {
            vals = new double[m_ComponentErrors.get(j).length];
            components = m_ComponentErrors.get(j);
            for (int i = 0; i < vals.length; i++) {
                if (components[i] > 0d) {
                    vals[i] = 1 / components[i] / componentSums[i];
                }
                else {
                    vals[i] = 1 / ClusHeuristic.DELTA / componentSums[i];
                }

            }
            m_ComponentWeights.put(j, vals);
        }
    }


    public double getModelWeight(int baseModelNumber) {
        return m_AggregatedWeight.get(baseModelNumber);
    }


    public double getComponentWeight(int baseModelNumber, int component) {
        return m_ComponentWeights.get(baseModelNumber)[component];
    }

}

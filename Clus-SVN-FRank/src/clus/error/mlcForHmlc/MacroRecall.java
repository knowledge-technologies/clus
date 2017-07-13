package clus.error.mlcForHmlc;

import clus.error.common.ComponentError;

public class MacroRecall implements ComponentError, MlcHmlcSubError {
    protected int[] m_NbTruePositives, m_NbFalseNegatives;
    
    public MacroRecall(int dim) {
        m_NbTruePositives = new int[dim];
        m_NbFalseNegatives = new int[dim];
    }
    
    @Override
    public double compute(int dim) {
        double avg = 0.0;
        for (int i = 0; i < dim; i++) {
            avg += getModelErrorComponent(i);
        }
        return avg / dim;
    }

    @Override
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        int dim = actual.length;
        for (int i = 0; i < dim; i++) {
            if (actual[i]) { // label relevant
                if (predictedThresholded[i]) {
                    m_NbTruePositives[i]++;
                }
                else {
                    m_NbFalseNegatives[i]++;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "MacroRecall";
    }

    @Override
    public double getModelErrorComponent(int i) {
        return ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalseNegatives[i]);
    }

}

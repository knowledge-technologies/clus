package clus.error.mlcForHmlc;


import clus.error.common.ComponentError;
 

public class MacroPrecision implements MlcHmlcSubError, ComponentError {
    protected int[] m_NbTruePositives, m_NbFalsePositives;
    
    public MacroPrecision(int dim) {
        m_NbTruePositives = new int[dim];
        m_NbFalsePositives = new int[dim];
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
            if (predictedThresholded[i]) { // predicted positive
                if (actual[i]) {
                    m_NbTruePositives[i]++;
                }
                else {
                    m_NbFalsePositives[i]++;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "MacroPrecision";
    }

    @Override
    public double getModelErrorComponent(int i) {
        return ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalsePositives[i]);
    }

}

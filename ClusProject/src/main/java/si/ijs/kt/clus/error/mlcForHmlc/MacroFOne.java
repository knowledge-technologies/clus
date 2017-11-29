
package si.ijs.kt.clus.error.mlcForHmlc;

import si.ijs.kt.clus.error.common.ComponentError;


public class MacroFOne implements MlcHmlcSubError, ComponentError {

    protected int[] m_NbTruePositives, m_NbFalsePositives, m_NbFalseNegatives;


    public MacroFOne(int dim) {
        m_NbTruePositives = new int[dim];
        m_NbFalsePositives = new int[dim];
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
        int dim = predictedThresholded.length;
        for (int i = 0; i < dim; i++) {
            if (actual[i]) { // label relevant
                if (predictedThresholded[i]) {
                    m_NbTruePositives[i]++;
                }
                else {
                    m_NbFalseNegatives[i]++;
                }
            }
            else if (predictedThresholded[i]) {
                m_NbFalsePositives[i]++;
            }
        }
    }


    @Override
    public String getName() {
        return "MacroFOne";
    }


    @Override
    public double getModelErrorComponent(int i) {
        double prec = ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalsePositives[i]);
        double recall = ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalseNegatives[i]);
        return 2.0 * prec * recall / (prec + recall);
    }


    @Override
    public void add(MlcHmlcSubError other) {
        MacroFOne o = (MacroFOne) other;

        for (int i = 0; i < m_NbTruePositives.length; i++) {
            m_NbTruePositives[i] += o.m_NbTruePositives[i];
            m_NbFalsePositives[i] += o.m_NbFalsePositives[i];
            m_NbFalseNegatives[i] += o.m_NbFalseNegatives[i];
        }
    }

}

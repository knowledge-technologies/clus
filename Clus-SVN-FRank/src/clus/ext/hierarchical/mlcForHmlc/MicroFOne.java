package clus.ext.hierarchical.mlcForHmlc;

public class MicroFOne implements MlcHmlcSubError {
    protected int[] m_NbTruePositives, m_NbFalsePositives, m_NbFalseNegatives;
    
    public MicroFOne(int dim) {
        m_NbTruePositives = new int[dim];
        m_NbFalsePositives = new int[dim];
        m_NbFalseNegatives = new int[dim];
    }
    @Override
    public double compute(int dim) {
        int truePositives = 0, falsePositives = 0, falseNegatives = 0;
        for (int i = 0; i < dim; i++) {
            truePositives += m_NbTruePositives[i];
            falsePositives += m_NbFalsePositives[i];
            falseNegatives += m_NbFalseNegatives[i];
        }
        double precision = ((double) truePositives) / (truePositives + falsePositives);
        double recall = ((double) truePositives) / (truePositives + falseNegatives);
        return 2.0 * precision * recall / (precision + recall);
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
            else if (predictedThresholded[i]) {
                m_NbFalsePositives[i]++;
            }
        }
    }

    @Override
    public String getName() {
        return "MicroFOne";
    }

}

package clus.ext.hierarchical.mlcForHmlc;

public class MLPrecision implements MlcHmlcSubError {
    private double m_PrecisionSum;
    private int m_NbKnown;
    
    public MLPrecision() {
        m_PrecisionSum = 0.0;
        m_NbKnown = 0;
    }
    
    @Override
    public double compute(int dim) {
        return m_PrecisionSum / m_NbKnown;
    }

    @Override
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
        for (int i = 0; i < actual.length; i++) {
            if (predictedThresholded[i]) {
                nbRelevantPredicted++;
                if (actual[i]) {
                    intersection++;
                }
            }
            if (actual[i]) {
                nbRelevant++;
            }
        }
        m_PrecisionSum += nbRelevantPredicted != 0 ? ((double) intersection) / nbRelevantPredicted : (nbRelevant == 0 ? 1.0 : 0.0);
        m_NbKnown++;
    }

    @Override
    public String getName() {
        return "MLPrecision";
    }

}

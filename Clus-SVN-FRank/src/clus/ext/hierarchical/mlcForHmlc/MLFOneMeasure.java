package clus.ext.hierarchical.mlcForHmlc;

public class MLFOneMeasure implements MlcHmlcSubError {
    private double m_F1Sum;
    private int m_NbKnown;
    
    public MLFOneMeasure() {
        m_F1Sum = 0.0;
        m_NbKnown = 0;
    }
    
    @Override
    public double compute(int dim) {
        return m_F1Sum / m_NbKnown;
    }

    @Override
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i]) {
                nbRelevant++;
                if (predictedThresholded[i]) {
                    intersection++;
                }
            }
            if (predictedThresholded[i]) {
                nbRelevantPredicted++;
            }
        }
        m_F1Sum += nbRelevant + nbRelevantPredicted > 0 ? 2.0 * intersection / (nbRelevant + nbRelevantPredicted) : 1.0; 
        m_NbKnown++;
    }

    @Override
    public String getName() {
        return "MLFOneMeasure";
    }

}

package clus.error.mlcForHmlc;

public class MLRecall implements MlcHmlcSubError {
    private double m_RecallSum;
    private int m_NbKnown;

    public MLRecall() {
        m_RecallSum = 0.0;
        m_NbKnown = 0;
    }
    
    @Override
    public double compute(int dim) {
        return m_RecallSum / m_NbKnown;
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
        m_RecallSum += nbRelevant != 0 ? ((double) intersection) / nbRelevant : (nbRelevantPredicted == 0 ? 1.0 : 0.0);                                                                                               // cases
        m_NbKnown++;
    }

    @Override
    public String getName() {
        return "MLRecall";
    }

}

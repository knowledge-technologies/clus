
package si.ijs.kt.clus.error.mlcForHmlc;

public class MicroRecall implements MlcHmlcSubError {

    protected int[] m_NbTruePositives, m_NbFalseNegatives;


    public MicroRecall(int dim) {
        m_NbTruePositives = new int[dim];
        m_NbFalseNegatives = new int[dim];
    }


    @Override
    public double compute(int dim) {
        int truePositives = 0, falseNegatives = 0;
        for (int i = 0; i < dim; i++) {
            truePositives += m_NbTruePositives[i];
            falseNegatives += m_NbFalseNegatives[i];
        }
        return ((double) truePositives) / (truePositives + falseNegatives);
    }


    @Override
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        for (int i = 0; i < actual.length; i++) {
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
        return "MicroRecall";
    }


    @Override
    public void add(MlcHmlcSubError other) {
        MicroRecall o = (MicroRecall) other;

        for (int i = 0; i < m_NbTruePositives.length; i++) {
            m_NbTruePositives[i] += o.m_NbTruePositives[i];
            m_NbFalseNegatives[i] += o.m_NbFalseNegatives[i];
        }
    }
}

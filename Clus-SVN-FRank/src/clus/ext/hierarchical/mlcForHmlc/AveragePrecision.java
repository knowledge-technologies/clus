package clus.ext.hierarchical.mlcForHmlc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AveragePrecision implements MlcHmlcSubError {

    private double m_NonnormalisedPrec;
    private int m_NbKnown;
    
    public AveragePrecision() {
        m_NonnormalisedPrec = 0.0;
        m_NbKnown = 0;
    }
    
    @Override
    public double compute(int dim) {
        return m_NonnormalisedPrec / m_NbKnown;
    }

    @Override
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        final double[] scores = predicted;
        int dim = scores.length;
        boolean[] isRelevant = actual;
        ArrayList<Integer> indicesOfKnownValues= new ArrayList<Integer>();
        for (int i = 0; i < dim; i++) {
            indicesOfKnownValues.add(i);
        }
        // sort with respect to the scores
        Collections.sort(indicesOfKnownValues, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -Double.compare(scores[o1], scores[o2]);
            }
        });
        int nbOfRelevant = 0;
        double u = 0.0;
        if (indicesOfKnownValues.size() > 0) {
            for (int i = 0; i < indicesOfKnownValues.size(); i++) {
                if (isRelevant[indicesOfKnownValues.get(i)]) {
                    nbOfRelevant++;
                    u += ((double) nbOfRelevant) / (i + 1);
                }
            }
            m_NonnormalisedPrec += u / nbOfRelevant;
            m_NbKnown++;
        }
    }

    @Override
    public String getName() {
        return "AveragePrecision";
    }

}

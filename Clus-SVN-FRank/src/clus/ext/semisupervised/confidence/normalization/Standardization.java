package clus.ext.semisupervised.confidence.normalization;

import java.util.Arrays;
import java.util.Map;

/**
 * Standardizes per-target scores to 0.5 mean and 0.125 standard deviation. This 
 * ensures that 99% of values will be in [0,1]. The values greater than 1, or 
 * smaller than 0, will be clamped to 1, or 0, respectively. 
 * @author jurical
 */
public class Standardization implements Normalization {

    int nbTargetAttributes;
    boolean lessIsBetter = true;

    public Standardization(int nbTargetAttributes) {
        this.nbTargetAttributes = nbTargetAttributes;
    }

    @Override
    public void normalize(Map<Integer, double[]> perTargetScores) {

        double[] means = new double[nbTargetAttributes];
        double[] stdDevs = new double[nbTargetAttributes];

        Arrays.fill(means, 0);
        Arrays.fill(stdDevs, 0);

        double[] instancePerTargetScores;
        double nbExamples = 0;

        //calculate means and standard deviations for all target variables
        //calculation is done incrementally

        for (Integer key : perTargetScores.keySet()) {
            instancePerTargetScores = perTargetScores.get(key);
            nbExamples++;

            for (int i = 0; i < instancePerTargetScores.length; i++) {
                //incremental standard deviation
                if (nbExamples > 1) {
                    stdDevs[i] = (nbExamples - 2) / (nbExamples - 1) * stdDevs[i] + (1 / nbExamples) * Math.pow(instancePerTargetScores[i] - means[i], 2);
                }

                //incremental mean
                means[i] = (instancePerTargetScores[i] + (nbExamples - 1) * means[i]) / nbExamples;
            }
        }
        
        for (int i = 0; i < nbTargetAttributes; i++) {
            stdDevs[i] = Math.sqrt(stdDevs[i]);
        }

        double standardizedValue;

        for (Integer key : perTargetScores.keySet()) {
            instancePerTargetScores = perTargetScores.get(key);

            for (int i = 0; i < nbTargetAttributes; i++) {
                if(stdDevs[i] == 0) // FIXME: not sure if this is right, however it can happen that stddev is 0, e.g., all examples get the same prediction
                    standardizedValue = 0;
                else
                 standardizedValue = ((instancePerTargetScores[i] - means[i]) / stdDevs[i]) * 0.125 + 0.5; // standardise to 0.5 mean and 0.125 standard deviation

                //clamp values greater than 1 to 1, and smaller than 0 to 0
                if (standardizedValue > 1) {
                    perTargetScores.get(key)[i] = 1;
                } else if (standardizedValue < 0) {
                    perTargetScores.get(key)[i] = 0;
                } else {
                    if(lessIsBetter)
                        perTargetScores.get(key)[i] = 1 - standardizedValue;
                    else perTargetScores.get(key)[i] = standardizedValue;
                }    
            }

        }
    }

    @Override
    public void setIsLessBetter(boolean lessIsBetter) {
        this.lessIsBetter = lessIsBetter;
    }
}

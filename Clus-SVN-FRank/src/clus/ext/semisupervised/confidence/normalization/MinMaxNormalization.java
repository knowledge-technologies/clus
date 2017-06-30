package clus.ext.semisupervised.confidence.normalization;

import java.util.Arrays;
import java.util.Map;

/**
 * Normalises per-target confidence scores to [0,1]. Normalization for each 
 * target is performed independently.
 * @author jurical
 */
public class MinMaxNormalization implements Normalization {

    int nbTargetAttributes;
    boolean lessIsBetter = true;

    public MinMaxNormalization(int nbTargetAttributes) {
        this.nbTargetAttributes = nbTargetAttributes;
    }

    /**
     * The score x is normalised as follows: x_norm = (x - min)/(max - min)
     * @param perTargetScores 
     */
    @Override
    public void normalize(Map<Integer, double[]> perTargetScores) {

        double[] min = new double[nbTargetAttributes], max = new double[nbTargetAttributes];

        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, Double.MIN_VALUE);

        double[] instancePerTargetScores;

        for (Integer key : perTargetScores.keySet()) {
            instancePerTargetScores = perTargetScores.get(key);
            //store min and max for each target, we need this for normalization
            for (int j = 0; j < instancePerTargetScores.length; j++) {
                //maybe if else
                if (instancePerTargetScores[j] > max[j]) {
                    max[j] = instancePerTargetScores[j];
                }
                if (instancePerTargetScores[j] < min[j]) {
                    min[j] = instancePerTargetScores[j];
                }
            }
        }

        //what if max = min? Not sure what to do
        for(int j = 0; j < min.length; j++) {
            if(min[j] == max [j]) {
                min[j] = min[j]/2;
            } // FIXME: if min = max we will put it as 0.5... not sure if this is correct
        }
        
        if (lessIsBetter) { //normalize to [0,1] and subtract from 1
            for (Integer key : perTargetScores.keySet()) {
                instancePerTargetScores = perTargetScores.get(key);
                for (int j = 0; j < instancePerTargetScores.length; j++) {
                    instancePerTargetScores[j] = 1 - ((instancePerTargetScores[j] - min[j]) / (max[j] - min[j]));
                }
            }
        }
        else { //normalize to [0,1]
            for (Integer key : perTargetScores.keySet()) {
                instancePerTargetScores = perTargetScores.get(key);
                for (int j = 0; j < instancePerTargetScores.length; j++) {
                    instancePerTargetScores[j] = (instancePerTargetScores[j] - min[j]) / (max[j] - min[j]);
                }
            }   
        }

        //free some memory
        min = null;
        max = null;
    }

    @Override
    public void setIsLessBetter(boolean lessIsBetter) {
        this.lessIsBetter = lessIsBetter;
    }
}

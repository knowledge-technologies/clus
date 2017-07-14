package clus.ext.semisupervised.confidence.aggregation;

import clus.ext.semisupervised.Helper;

/**
 * Averages per-target scores, but ignores zeros. 
 * Can be used for HMC, because there zeros are implicit, i.e., they are not explicitly predicted
 * @author jurica
 */
public class AverageHMC implements Aggregation {

    @Override
    public double aggregate(double[] perTargetScores) {
        return Helper.averageIgnoreZeros(perTargetScores);
    }
    
}

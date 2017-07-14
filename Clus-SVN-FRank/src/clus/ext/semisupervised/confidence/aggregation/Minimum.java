package clus.ext.semisupervised.confidence.aggregation;

import clus.ext.semisupervised.Helper;

/**
 * Returns minimum of per-target reliability scores, i.e., an example is 
 * considered as reliable as its least reliable component 
 * @author jurical
 */
public class Minimum implements Aggregation {

    @Override
    public double aggregate(double[] perTargetScores) {
        return Helper.min(perTargetScores);
    }
    
}
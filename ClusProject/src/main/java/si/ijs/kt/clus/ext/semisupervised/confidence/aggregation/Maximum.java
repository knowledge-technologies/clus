package si.ijs.kt.clus.ext.semisupervised.confidence.aggregation;

import si.ijs.kt.clus.ext.semisupervised.Helper;

/**
 * Returns maximum of per-target reliability scores, i.e., an example is 
 * considered as reliable as its most reliable component 
 * @author jurical
 */
public class Maximum implements Aggregation {

    @Override
    public double aggregate(double[] perTargetScores) {
        return Helper.max(perTargetScores);
    }
    
}

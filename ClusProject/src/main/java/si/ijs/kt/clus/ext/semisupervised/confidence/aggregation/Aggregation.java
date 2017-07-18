package si.ijs.kt.clus.ext.semisupervised.confidence.aggregation;

/**
 *
 * @author jurical
 */
public interface Aggregation {
    
    /**
     * Class for aggregating per-target reliability scores into a single score
     * @param perTargetScores
     * @return 
     */
    public double aggregate(double[] perTargetScores);    
}

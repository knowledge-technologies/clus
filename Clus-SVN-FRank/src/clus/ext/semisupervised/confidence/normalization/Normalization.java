package clus.ext.semisupervised.confidence.normalization;

import java.util.Map;

/**
 *
 * @author jurical
 */
/**
 * Class for normalization of per-target confidence scores
 *
 * @author jurical
 */
public interface Normalization {
    
    /**
     *
     * @param perTargetScores Per-target confidence scores
     * @param confidenceScores Array where the final confidence scores will be
     * stored
     * @param nbTargetAttributes The number of target attributes
     */
    public void normalize(Map<Integer, double[]> perTargetScores);

    /**
     * Set to true if less is better, of false if more is better
     * @param lessIsBetter 
     */
    public void setIsLessBetter(boolean lessIsBetter);
}

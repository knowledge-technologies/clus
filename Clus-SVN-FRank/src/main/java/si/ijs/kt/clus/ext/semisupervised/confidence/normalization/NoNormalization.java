package si.ijs.kt.clus.ext.semisupervised.confidence.normalization;

import java.util.Map;

/**
 * Does nothing, no normalization is performed
 * @author jurical
 */
public class NoNormalization implements Normalization {

    @Override
    public void normalize(Map<Integer, double[]> perTargetScores) {
    }

    @Override
    public void setIsLessBetter(boolean lessIsBetter) {   
    }
    
}
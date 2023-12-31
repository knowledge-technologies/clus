package si.ijs.kt.clus.ext.hierarchical.thresholdcalibration;

import si.ijs.kt.clus.statistic.WHTDStatistic;

/**
 *
 * @author jurical
 */
public interface HierThresholdCalibration {
    
    public abstract double getThreshold();
    
    public abstract void addExample(WHTDStatistic stat);   
    
}

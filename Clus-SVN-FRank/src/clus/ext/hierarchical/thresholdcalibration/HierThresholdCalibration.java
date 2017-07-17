package clus.ext.hierarchical.thresholdcalibration;

import clus.ext.hierarchical.WHTDStatistic;

/**
 *
 * @author jurical
 */
public interface HierThresholdCalibration {
    
    public abstract double getThreshold();
    
    public abstract void addExample(WHTDStatistic stat);   
    
}

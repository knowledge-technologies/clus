package si.ijs.kt.clus.ext.hierarchical.thresholdcalibration;

import java.util.LinkedHashMap;

import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;

/**
 * Threshold calibration method by choosing the threshold that minimizes the
 * difference in label cardinality between the training data and the predictions
 * for the test data.
 *
 * @author jurical
 */
public class calibrateByLabelCardinality implements HierThresholdCalibration {

    double m_Step, m_labelCardinalityTrain;
    int nbExamples;
    LinkedHashMap<Double, Integer> cardinalities;

    public calibrateByLabelCardinality(double step, double labelCardinalityTrain) {
        m_Step = step;
        m_labelCardinalityTrain = labelCardinalityTrain;
        nbExamples = 0;

        cardinalities = new LinkedHashMap<Double, Integer>();

        cardinalities.put(100.0, 0);
        for (double d = 100 - m_Step; d > 0; d -= m_Step) {
            cardinalities.put(d, 0);
        }
        cardinalities.put(0.0, 0);
        
    }

    @Override
    public double getThreshold() {
        double bestThreshold = 0, bestCardinality = Double.MAX_VALUE, tempCardinality;

        for (double threshold : cardinalities.keySet()) {
            tempCardinality = 1.0 * cardinalities.get(threshold) / nbExamples;
            if (Math.abs((1.0 * cardinalities.get(threshold) / nbExamples) - m_labelCardinalityTrain) < Math.abs(bestCardinality - m_labelCardinalityTrain)) {
                bestThreshold = threshold;
                bestCardinality = tempCardinality;
            }
        }

        return bestThreshold;
    }

    @Override
    public void addExample(WHTDStatistic stat) {
        
        for (double threshold : cardinalities.keySet()) {
            stat.setThreshold(threshold);
            stat.computePrediction();
            cardinalities.put(threshold, cardinalities.get(threshold) + stat.getNbPredictedClasses());
        }

        nbExamples++;
    }
    
    public double getCardinality(double th) {
        return 1.0 * cardinalities.get(th) / nbExamples;
    }
}

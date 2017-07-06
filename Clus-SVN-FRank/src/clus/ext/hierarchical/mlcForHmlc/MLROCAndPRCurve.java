package clus.ext.hierarchical.mlcForHmlc;

import clus.error.BinaryPredictionList;
import clus.error.ROCAndPRCurve;

public abstract class MLROCAndPRCurve {
    protected static final int averageAUROC = 0;
    protected static final int averageAUPRC = 1;
    protected static final int weightedAUPRC = 2;
    protected static final int pooledAUPRC = 3;

    protected double m_AreaROC, m_AreaPR;
    protected double[] m_Thresholds;

    protected transient boolean m_ExtendPR;
    protected transient BinaryPredictionList m_Values;

    protected BinaryPredictionList[] m_ClassWisePredictions;
    protected ROCAndPRCurve[] m_ROCAndPRCurves;

    protected double m_AverageAUROC = -1.0, m_AverageAUPRC = -1.0, m_WAvgAUPRC = -1.0, m_PooledAUPRC = -1.0;
    
    public MLROCAndPRCurve(int dim) {
        m_ClassWisePredictions = new BinaryPredictionList[dim];
        m_ROCAndPRCurves = new ROCAndPRCurve[dim];
        for (int i = 0; i < dim; i++) {
            BinaryPredictionList predlist = new BinaryPredictionList();
            m_ClassWisePredictions[i] = predlist;
            m_ROCAndPRCurves[i] = new ROCAndPRCurve(predlist);
        }
    }
    
    public double getModelError(int typeOfCurve) {
        computeAll();
        switch (typeOfCurve) {
            case averageAUROC:
                return m_AverageAUROC;
            case averageAUPRC:
                return m_AverageAUPRC;
            case weightedAUPRC:
                return m_WAvgAUPRC;
            case pooledAUPRC:
                return m_PooledAUPRC;
        }
        throw new RuntimeException("Unknown type of curve: typeOfCurve" + typeOfCurve);
    }
    
    public void computeAll() {
        int dim = m_ROCAndPRCurves.length;
        BinaryPredictionList pooled = new BinaryPredictionList();
        ROCAndPRCurve pooledCurve = new ROCAndPRCurve(pooled);

        for (int i = 0; i < dim; i++) {
            m_ClassWisePredictions[i].sort();
            m_ROCAndPRCurves[i].computeCurves();
            m_ROCAndPRCurves[i].clear();
            pooled.add(m_ClassWisePredictions[i]);
            m_ClassWisePredictions[i].clearData();
        }
        pooled.sort();
        pooledCurve.computeCurves();
        pooledCurve.clear();
        // Compute averages
        int cnt = 0;
        double sumAUROC = 0.0;
        double sumAUPRC = 0.0;
        double sumAUPRCw = 0.0;
        double sumFrequency = 0.0;
        for (int i = 0; i < dim; i++) {
            double freq = m_ClassWisePredictions[i].getFrequency();
            sumAUROC += m_ROCAndPRCurves[i].getAreaROC();
            sumAUPRC += m_ROCAndPRCurves[i].getAreaPR();
            sumAUPRCw += freq * m_ROCAndPRCurves[i].getAreaPR();
            sumFrequency += freq;
            cnt++;
        }
        m_AverageAUROC = sumAUROC / cnt;
        m_AverageAUPRC = sumAUPRC / cnt;
        m_WAvgAUPRC = sumAUPRCw / sumFrequency;
        m_PooledAUPRC = pooledCurve.getAreaPR();
    }
    
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded) {
        double[] probabilities = predicted; 
        for (int i = 0; i < actual.length; i++) {
            m_ClassWisePredictions[i].addExample(actual[i], probabilities[i]);
        }
    }
    
    
}

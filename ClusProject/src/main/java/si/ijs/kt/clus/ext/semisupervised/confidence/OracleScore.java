package si.ijs.kt.clus.ext.semisupervised.confidence;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStatBase;

/**
 * Provides reliability scores on the basis of 'actual error', which is not 
 * attainable in practice, i.e., if true unlabeled data are used. The unlabeled instances 
 * provided need to have labels. 
 * @author jurical
 */
public class OracleScore extends PredictionConfidence {

    double m_HmcThreshold = 0.5;
    
    public OracleScore(ClusStatManager statManager, int normalizationType, int aggregationType) {
        super(statManager, normalizationType, aggregationType);
    }

    /**
     * Returns per-target confidence scores
     * @param model
     * @param tuple Tuple is assumed to have true labels 
     * @return 
     */
    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) {
        
        ClusStatistic stat = model.predictWeighted(tuple); 
        
        if(m_StatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
            stat.computePrediction();
            ClassificationStat tempStat = (ClassificationStat)stat.cloneStat();
            ClassificationStat tempStat2 = (ClassificationStat) stat;
            tempStat.reset();
            tempStat.updateWeighted(tuple, tuple.getWeight());
            tempStat.computePrediction();
            double[] distances = new double[m_NbTargets];
            
            for(int i = 0; i < tempStat.m_MajorityClasses.length; i++)
                distances[i] = (tempStat.m_MajorityClasses[i] - tempStat2.m_MajorityClasses[i])*(tempStat.m_MajorityClasses[i] - tempStat2.m_MajorityClasses[i]);
                
            return distances;
        }
        
        if(m_StatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            ((WHTDStatistic)stat).setThreshold(m_HmcThreshold);
            stat.computePrediction();

            DataTuple tempTuple = tuple.deepCloneTuple();
            stat.predictTuple(tempTuple);
                      
            WHTDStatistic tempStat  = (WHTDStatistic)stat.cloneStat();
            tempStat.reset();
            tempStat.setThreshold(m_HmcThreshold);
            tempStat.updateWeighted(tempTuple, tempTuple.getWeight());
            tempStat.calcMean();
            
            return tempStat.getPointwiseSquaredDistance(tuple);
        }
        
        if(m_StatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
            stat.computePrediction();
            return ((RegressionStatBase) stat).getPointwiseSquaredDistance(tuple, m_StatManager.getNormalizationWeights());
        }
        
        throw new UnsupportedOperationException("Not supported yet.");        
    }

    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /***
     * Threshold for prediction in the context of hierarchical multi-label classification
     * @param t 
     */
    public void setHmcThreshold(double t){
        m_HmcThreshold = t;
    }
            
}

package si.ijs.kt.clus.ext.semisupervised.confidence;

import java.util.Random;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.ClusException;

/**
 * Returns random numbers as reliability scores, two modes are possible:
 * RANDOM_UNIFORM: random numbers are generated uniformly in [0,1]
 * RANDOM_GAUSSIAN: random numbers are normally distribution in [0,1] with mean 
 * 0.5 and std. dev. 0.125 (ensures 99% values are in [0,1])
 * @author jurical
 */
public class RandomScore extends PredictionConfidence {

    public static int RANDOM_UNIFORM = 1;
    public static int RANDOM_GAUSSIAN = 2;
    public int type;
    
    public RandomScore(ClusStatManager statManager, int type) {
        super(statManager, 0, 0);
        this.type = type;
    }
    
    @Override
    public void calculateConfidenceScores(ClusModel model, RowData unlabeledData) throws ClusException {
        
        int nb_unlabeled = unlabeledData.getNbRows();
        
        //we initialize arrays only once 
        if(m_ConfidenceScores == null) {       
            m_ConfidenceScores = new double[nb_unlabeled];
        }
        
        Random rnd = new Random();
        
        for (int i = 0; i < nb_unlabeled; i++) {
            DataTuple tuple = unlabeledData.getTuple(i);

            //we will not consider instances with null instances weight = 0
            if (tuple != null && tuple.getWeight() > 0) {
                 if(type == RANDOM_UNIFORM) {
                    m_ConfidenceScores[i] = rnd.nextDouble();
                 }
                 if(type == RANDOM_GAUSSIAN) {
                     m_ConfidenceScores[i] = rnd.nextGaussian() * 0.125 + 0.5; //generate normally distribuited numbers with mean 0.5 and std. dev. 0.125
                     if(m_ConfidenceScores[i] > 1) {
                         m_ConfidenceScores[i] = 1;
                     }
                     if(m_ConfidenceScores[i] < 0) {
                         m_ConfidenceScores[i] = 0;
                     }
                 }
                 
            }
            else{
                m_ConfidenceScores[i] = Double.NaN;
            }
        }        
    }

    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

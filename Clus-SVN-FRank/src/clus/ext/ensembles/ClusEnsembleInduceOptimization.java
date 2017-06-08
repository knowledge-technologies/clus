
package clus.ext.ensembles;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import clus.data.rows.DataTuple;
import clus.data.rows.TupleIterator;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;


public abstract class ClusEnsembleInduceOptimization implements Serializable{


    // protected int[] m_HashCodeTuple; // train + test tuples: m_HashCodeTuple[i] = hash of the i-th tuple
	/** The keys are (hashes of) DataTuples, which are mapped to the position of the tuple in the predictions */
    protected HashMap<DataTuple, Integer> m_TuplePositions;
    
    
    /** The number of updates of predictions. In any moment, this should equal the number models, used to update the predictions. */
    protected int m_NbUpdates = 0;
    
    protected ClusReadWriteLock m_NbUpdatesLock = new ClusReadWriteLock();
    protected ClusReadWriteLock m_AvgPredictionsLock = new ClusReadWriteLock();
    
    /** For ROS ensembles */
    protected ClusEnsembleROSInfo m_EnsembleROSInfo = null;

    public ClusEnsembleInduceOptimization(TupleIterator train, TupleIterator test) throws IOException, ClusException {
        // m_HashCodeTuple = new int[nb_tuples];
    	m_TuplePositions = new HashMap<DataTuple, Integer>();
        int count = 0;
        if (train != null) {
            train.init();
            DataTuple train_tuple = train.readTuple();
            while (train_tuple != null) {
                // m_HashCodeTuple[count] = train_tuple.hashCode();
            	m_TuplePositions.put(train_tuple, count);
                count++;
                train_tuple = train.readTuple();
            }
        }
        if (test != null) {
            test.init();
            DataTuple test_tuple = test.readTuple();
            while (test_tuple != null) {
                // m_HashCodeTuple[count] = test_tuple.hashCode();
                m_TuplePositions.put(test_tuple, count);
                count++;
                test_tuple = test.readTuple();
            }
        }
    }

//    public ClusEnsembleInduceOptimization(TupleIterator train, TupleIterator test) throws IOException, ClusException {
//        ArrayList<Integer> tuple_hash = new ArrayList<Integer>();
//        if (train != null) {
//            train.init();
//            DataTuple train_tuple = train.readTuple();
//            while (train_tuple != null) {
//                tuple_hash.add(train_tuple.hashCode());
//                train_tuple = train.readTuple();
//            }
//            train.init();
//        }
//        
//        if (test != null) {
//            test.init();// restart the iterator
//            DataTuple test_tuple = test.readTuple();
//            while (test_tuple != null) {
//                tuple_hash.add(test_tuple.hashCode());
//                test_tuple = test.readTuple();
//            }
//            test.init();// restart the iterator
//        }
//        int nb_tuples = tuple_hash.size();
//        // m_HashCodeTuple = new int[nb_tuples];
//        for (int k = 0; k < nb_tuples; k++)
//            m_HashCodeTuple[k] = tuple_hash.get(k);
//    }


    public int locateTuple(DataTuple tuple) {
//        int position = -1;
//        boolean found = false;
//        int i = 0;
//        // search for the tuple
//        while (!found && i < m_HashCodeTuple.length) {
//            if (m_HashCodeTuple[i] == tuple.hashCode()) {
//                position = i;
//                found = true;
//            }
//            i++;
//        }
//        return position;
    	return m_TuplePositions.get(tuple);
    }
    
    
    public abstract void initPredictions(ClusStatistic stat, ClusEnsembleROSInfo ensembleROSInfo);

    public abstract void updatePredictionsForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException;
    //@Deprecated
    //public abstract void initModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException;
    //@Deprecated
    //public abstract void addModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test, int nb_models) throws IOException, ClusException;
    
    

    /**
     * The average predictions {@code avg_predictions} of {@code nb_models - 1}, and the predictions {@code predictions} of one additional model
     * are combined into the average predictions of all {@code nb_models}.
     * @param avg_predictions
     * @param predictions
     * @param nb_models
     * @return
     */
    public static double[] incrementPredictions(double[] avg_predictions, double[] predictions, double nb_models) {
        // the current averages are stored in the avg_predictions
        int plength = avg_predictions.length;
        double[] result = new double[plength];
        for (int i = 0; i < plength; i++)
            result[i] = avg_predictions[i] + (predictions[i] - avg_predictions[i]) / nb_models;
        return result;
    }

    public double[][] incrementPredictions(double[][] sum_predictions, double[][] predictions, int nb_models) {
        // the current sums are stored in sum_predictions
        double[][] result = new double[sum_predictions.length][];
        
        if (Settings.isEnsembleROSEnabled() && Settings.getEnsembleROSScope() == Settings.ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_SUBSET_AVERAGING) {
            int[] enabled = m_EnsembleROSInfo.getOnlyTargets(m_EnsembleROSInfo.getModelSubspace(nb_models-1)); // get enabled targets for the model
            
            for (int i = 0; i < sum_predictions.length; i++) {
                if (enabled[i] == 1) {
                    result[i] = new double[sum_predictions[i].length];
                    // if target is enables, then update results[i][j]
                    for (int j = 0; j < sum_predictions[i].length; j++) {
                        result[i][j] = sum_predictions[i][j] + (predictions[i][j] - sum_predictions[i][j]) / m_EnsembleROSInfo.getCoverageOpt(i);
                    }
                }
                else { // target not enabled for this model; retain what we have until now
                    result[i] = sum_predictions[i]; 
                }
            }
        }
        else {
            for (int i = 0; i < sum_predictions.length; i++) {
                result[i] = new double[sum_predictions[i].length];
                for (int j = 0; j < sum_predictions[i].length; j++) {
                    result[i][j] = sum_predictions[i][j] + (predictions[i][j] - sum_predictions[i][j]) / nb_models;
                }
            }
        }

        return result;
    }

    /**
     * Returns the componentwise sum of the current sums and another predictions
     * @param sum_predictions The current sums
     * @param predictions
     * @return
     */
    public static double[][] incrementPredictions(double[][] sum_predictions, double[][] predictions) {
        double[][] result = new double[sum_predictions.length][];
        for (int i = 0; i < sum_predictions.length; i++) {
            result[i] = new double[sum_predictions[i].length];
            for (int j = 0; j < sum_predictions[i].length; j++) {
                result[i][j] = sum_predictions[i][j] + predictions[i][j];
            }
        }
        return result;
    }


    /**
     * Transform the class counts to majority vote (the one with max votes gets 1)
     * @param m_Counts
     * @return
     */
    public static double[][] transformToMajority(double[][] m_Counts) {
        int[] maxPerTarget = new int[m_Counts.length];
        for (int i = 0; i < m_Counts.length; i++) {
            maxPerTarget[i] = -1;
            double m_max = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < m_Counts[i].length; j++) {
                if (m_Counts[i][j] > m_max) {
                    maxPerTarget[i] = j;
                    m_max = m_Counts[i][j];
                }
            }
        }
        double[][] result = new double[m_Counts.length][];// all values set to zero
        for (int m = 0; m < m_Counts.length; m++) {
            result[m] = new double[m_Counts[m].length];
            result[m][maxPerTarget[m]]++; // the positions of max class will be 1
        }
        return result;
    }


    /**
     * Transform the class counts to probability distributions.
     * @param m_Counts
     * @return
     */
    public static double[][] transformToProbabilityDistribution(double[][] m_Counts) {
        double[] sumPerTarget = new double[m_Counts.length];
        for (int i = 0; i < m_Counts.length; i++)
            for (int j = 0; j < m_Counts[i].length; j++)
                sumPerTarget[i] += m_Counts[i][j];
        double[][] result = new double[m_Counts.length][];

        for (int m = 0; m < m_Counts.length; m++) {
            result[m] = new double[m_Counts[m].length];
            for (int n = 0; n < m_Counts[m].length; n++) {
                result[m][n] = m_Counts[m][n] / sumPerTarget[m];
            }
        }
        return result;
    }


    public abstract int getPredictionLength(int tuple); //{/ i.e., get number of targets
//        if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL || ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION)
//            return ClusEnsembleInduceOptRegHMLC.getPredictionLength(tuple);
//        if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY)
//            return ClusEnsembleInduceOptClassification.getPredictionLength(tuple);
//        return -1;
//    }


//    public double getPredictionValue(int tuple, int attribute) {
//        if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL || ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION)
//            return ClusEnsembleInduceOptRegHMLC.getPredictionValue(tuple, attribute);
//        return -1;
//    }


//    public double[] getPredictionValueClassification(int tuple, int attribute) {
//        if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY)
//            return ClusEnsembleInduceOptClassification.getPredictionValueClassification(tuple, attribute);
//        return null;
//    }


    public abstract void roundPredictions(); // {
//        System.out.println("Rounding up predictions!");
//        if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY)
//            ClusEnsembleInduceOptClassification.roundPredictions();
//        else if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL || ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION)
//            ClusEnsembleInduceOptRegHMLC.roundPredictions();
//        else {
//            System.err.println("Illegal call of the method roundPredictions from class ClusEnsembleInduceOptimization!");
//            System.err.println("Execution proceeds without rounding up of the predictions.");
//        }
//    }
    
//    protected int getNumberOfUpdates(){
//		m_NbUpdatesLock.readingLock();
//    	int ans = m_NbUpdates;
//    	m_NbUpdatesLock.readingUnlock();
//    	return ans;
//    }
//    
//    protected void increaseNumberOfUpdatesByOne(){
//		m_NbUpdatesLock.writingLock();
//    	m_NbUpdates++;
//    	m_NbUpdatesLock.writingUnlock();
//    }

}

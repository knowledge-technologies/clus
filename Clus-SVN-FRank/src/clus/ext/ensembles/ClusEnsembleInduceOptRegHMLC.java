
package clus.ext.ensembles;

import java.io.IOException;

import clus.data.rows.DataTuple;
import clus.data.rows.TupleIterator;
import clus.main.settings.Settings;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStatBase;
import clus.util.ClusException;
import clus.util.ClusFormat;


public class ClusEnsembleInduceOptRegHMLC extends ClusEnsembleInduceOptimization {

    private double[][] m_AvgPredictions;   
    
//    public ClusEnsembleInduceOptRegHMLC(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException, ClusException {
//        super(train, test, nb_tuples);
//    }


    public ClusEnsembleInduceOptRegHMLC(TupleIterator train, TupleIterator test, Settings sett) throws IOException, ClusException {
        super(train, test, sett);
    }


    public void initPredictions(ClusStatistic stat, ClusEnsembleROSInfo ensembleROSInfo) {
        m_AvgPredictions = new double[m_TuplePositions.size()][stat.getNbAttributes()]; // m_HashCodeTuple.length
        
        super.m_EnsembleROSInfo = ensembleROSInfo;
    }


    public synchronized void updatePredictionsForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException{
		m_NbUpdatesLock.writingLock();
		m_AvgPredictionsLock.writingLock();		
		m_NbUpdates++;		
		
		// for ROS
        if (getSettings().getEnsemble().isEnsembleROSEnabled()) {
            int[] enabledTargets = m_EnsembleROSInfo.getOnlyTargets(m_EnsembleROSInfo.getModelSubspace(m_NbUpdates-1)); // model (m_NbUpdates-1) uses enabledTargets
            m_EnsembleROSInfo.incrementCoverageOpt(enabledTargets);
            
            System.err.println("TODO: ROS not implemented for optimized ensembles for MTR");
            System.exit(-1);
            
        }
        
        if (train != null) {
            train.init();
            DataTuple train_tuple = train.readTuple();
            while (train_tuple != null) {
                int position = locateTuple(train_tuple);
                RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(train_tuple);
                m_AvgPredictions[position] = (m_NbUpdates == 1) ? stat.getNumericPred() : incrementPredictions(m_AvgPredictions[position], stat.getNumericPred(), m_NbUpdates);
                train_tuple = train.readTuple();
            }
            train.init();
        }
        if (test != null) {
            test.init();
            DataTuple test_tuple = test.readTuple();
            while (test_tuple != null) {
                int position = locateTuple(test_tuple);
                if(m_NbUpdates == 1){
                	RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(test_tuple);
                	m_AvgPredictions[position] = stat.getNumericPred();
                }
                else{
                	ClusStatistic stat = model.predictWeighted(test_tuple);
                    m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], stat.getNumericPred(), m_NbUpdates);
                }
                
                test_tuple = test.readTuple();
            }
            test.init();
        }		
		
		m_AvgPredictionsLock.writingUnlock();
		m_NbUpdatesLock.writingUnlock();    	
    }
    
//    @Deprecated
//    public void initModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException {
//        if (train != null) {
//            train.init();
//            DataTuple train_tuple = train.readTuple();
//            while (train_tuple != null) {
//                int position = locateTuple(train_tuple);
//                RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(train_tuple);
//                m_AvgPredictions[position] = stat.getNumericPred();
//                train_tuple = train.readTuple();
//            }
//            train.init();
//        }
//        if (test != null) {
//            test.init();
//            DataTuple test_tuple = test.readTuple();
//            while (test_tuple != null) {
//                int position = locateTuple(test_tuple);
//                RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(test_tuple);
//                m_AvgPredictions[position] = stat.getNumericPred();
//                test_tuple = test.readTuple();
//            }
//            test.init();
//        }
//    }

//    @Deprecated
//    public void addModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test, int nb_models) throws IOException, ClusException {
//        if (train != null) {
//            train.init();
//            DataTuple train_tuple = train.readTuple();
//            while (train_tuple != null) {
//                int position = locateTuple(train_tuple);
//                RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(train_tuple);
//                m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], stat.getNumericPred(), nb_models);
//                train_tuple = train.readTuple();
//            }
//            train.init();
//        }
//        if (test != null) {
//            test.init();
//            DataTuple test_tuple = test.readTuple();
//            while (test_tuple != null) {
//                int position = locateTuple(test_tuple);
//                ClusStatistic stat = model.predictWeighted(test_tuple);
//                m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], stat.getNumericPred(), nb_models);
//                test_tuple = test.readTuple();
//            }
//            test.init();
//        }
//    }


    public int getPredictionLength(int tuple) {
        return m_AvgPredictions[tuple].length;
    }


    public double getPredictionValue(int tuple, int attribute) {
        return m_AvgPredictions[tuple][attribute];
    }


    public void roundPredictions() {
        // System.out.println("Rounding up predictions!");
        for (int i = 0; i < m_AvgPredictions.length; i++) {
            for (int j = 0; j < m_AvgPredictions[i].length; j++) {
                // System.out.println("Before: " + m_AvgPredictions[i][j]);
                m_AvgPredictions[i][j] = Double.parseDouble(ClusFormat.FOUR_AFTER_DOT.format(m_AvgPredictions[i][j]));
                // System.out.println("After: " + m_AvgPredictions[i][j]);
            }
        }
    }
}


package clus.ext.ensembles;

import java.io.IOException;
import java.util.Set;

import clus.data.rows.DataTuple;
import clus.data.rows.TupleIterator;
import clus.main.settings.Settings;
import clus.main.settings.SettingsEnsemble;
import clus.model.ClusModel;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;
import clus.util.ClusFormat;


public class ClusEnsembleInduceOptClassification extends ClusEnsembleInduceOptimization {

    private double[][][] m_AvgPredictions;

//    public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException, ClusException {
//        super(train, test, nb_tuples);
//    }


    public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test, Settings sett) throws IOException, ClusException {
        super(train, test, sett);
    }


    public void initPredictions(ClusStatistic stat, ClusEnsembleROSInfo ensembleROSInfo) {
        ClassificationStat nstat = (ClassificationStat) stat;
        m_AvgPredictions = new double[m_TuplePositions.size()][nstat.getNbAttributes()][]; // m_HashCodeTuple.length
        for (int i = 0; i < nstat.getNbAttributes(); i++) {
            m_AvgPredictions[m_TuplePositions.size() - 1][i] = new double[nstat.getNbClasses(i)]; // m_HashCodeTuple.length - 1
        }
        
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
		}
		
        updateTuplesWithModel(train, model);
        updateTuplesWithModel(test, model);
        
        m_AvgPredictionsLock.writingUnlock();
        m_NbUpdatesLock.writingUnlock();
        
        
//		if (train != null) {
//            train.init();
//            DataTuple train_tuple = train.readTuple();
//            while (train_tuple != null) {
//                int position = locateTuple(train_tuple);
//                ClassificationStat stat = (ClassificationStat) model.predictWeighted(train_tuple);
//                double[][] counts = stat.getClassCounts().clone();
//                switch (Settings.m_ClassificationVoteType.getValue()) {// default is Probability distribution vote
//                    case Settings.VOTING_TYPE_MAJORITY:
//                        counts = transformToMajority(counts);
//                        break;
//                    case Settings.VOTING_TYPE_PROBAB_DISTR:
//                        counts = transformToProbabilityDistribution(counts);
//                        break;
//                    default:
//                        counts = transformToProbabilityDistribution(counts);
//                }
//                m_AvgPredictions[position] = (m_NbUpdates == 1) ? counts : incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);
//                train_tuple = train.readTuple();
//            }
//            train.init();
//        }
//
//        if (test != null) {
//            test.init();
//            DataTuple test_tuple = test.readTuple();
//            while (test_tuple != null) {
//                int position = locateTuple(test_tuple);
//                ClassificationStat stat = (ClassificationStat) model.predictWeighted(test_tuple);
//                double[][] counts = stat.getClassCounts().clone();
//                switch (Settings.m_ClassificationVoteType.getValue()) {// default is Probability distribution vote
//                    case Settings.VOTING_TYPE_MAJORITY:
//                        counts = transformToMajority(counts);
//                        break;
//                    case Settings.VOTING_TYPE_PROBAB_DISTR:
//                        counts = transformToProbabilityDistribution(counts);
//                        break;
//                    default:
//                        counts = transformToProbabilityDistribution(counts);
//                }
//                m_AvgPredictions[position] = (m_NbUpdates == 1) ? counts : incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);
//                test_tuple = test.readTuple();
//            }
//            test.init();
//        }


    }
    
    private void updateTuplesWithModel(TupleIterator iterator, ClusModel model) throws IOException, ClusException {
        if (iterator != null) {
            iterator.init();
            DataTuple tuple = iterator.readTuple();
            
            double[][] tmp = ((ClassificationStat)model.predictWeighted(tuple)).getClassCounts().clone();
            
            double[][] zeros = new double[tmp.length][tmp[0].length];
            for (int i = 0; i < zeros.length; i++) for (int j = 0; j < zeros[i].length; j++) zeros[i][j] = 0.0;
            
            while (tuple != null) {
                int position = locateTuple(tuple);
                
                // initialize values
                if (m_NbUpdates == 1) m_AvgPredictions[position] = zeros.clone();
                
                ClassificationStat stat = (ClassificationStat) model.predictWeighted(tuple);
                double[][] counts = stat.getClassCounts().clone();
                switch (getSettings().getEnsemble().getClassificationVoteType()) {// default is Probability distribution vote
                    case SettingsEnsemble.VOTING_TYPE_MAJORITY:
                        counts = transformToMajority(counts);
                        break;
                    case SettingsEnsemble.VOTING_TYPE_PROBAB_DISTR:
                        counts = transformToProbabilityDistribution(counts);
                        break;
                    default:
                        counts = transformToProbabilityDistribution(counts);
                }
                
                //m_AvgPredictions[position] = (m_NbUpdates == 1) ? counts : incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);
                m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);    

                tuple = iterator.readTuple();
            }
            iterator.init();
        }
    }
    

    public int getPredictionLength(int tuple) {
        return m_AvgPredictions[tuple].length;
    }

    public double[] getPredictionValueClassification(int tuple, int attribute) {
        return m_AvgPredictions[tuple][attribute];
    }

    public void roundPredictions() {
        //System.err.println("Rounding up predictions!");
        for (int i = 0; i < m_AvgPredictions.length; i++) {
            for (int j = 0; j < m_AvgPredictions[i].length; j++) {
                for (int k = 0; k < m_AvgPredictions[i][j].length; k++) {
                    //System.err.println("Before: " + m_AvgPredictions[i][j][k]);
                    m_AvgPredictions[i][j][k] = Double.parseDouble(ClusFormat.FOUR_AFTER_DOT.format(m_AvgPredictions[i][j][k]));
                    //System.err.println("After: " + m_AvgPredictions[i][j][k]);
                }
            }
        }
    }

    
    
//  @Deprecated
//  public void initModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException {
//      if (train != null) {
//          train.init();
//          DataTuple train_tuple = train.readTuple();
//          while (train_tuple != null) {
//              int position = locateTuple(train_tuple);
//              ClassificationStat stat = (ClassificationStat) model.predictWeighted(train_tuple);
//              double[][] counts = stat.getClassCounts().clone();
//              switch (Settings.m_ClassificationVoteType.getValue()) { // default is Probability distribution Vote
//                  case Settings.VOTING_TYPE_MAJORITY:
//                      counts = transformToMajority(counts);
//                      break;
//                  case Settings.VOTING_TYPE_PROBAB_DISTR:
//                      counts = transformToProbabilityDistribution(counts);
//                      break;
//                  default:
//                      counts = transformToProbabilityDistribution(counts);
//              }
//              m_AvgPredictions[position] = counts;
//              // System.out.println(train_tuple.toString());
//              // System.out.println(stat.getString());
//              // System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
//              // m_AvgPredictions[position][0][2]);
//              train_tuple = train.readTuple();
//          }
//          train.init();
//      }
//      if (test != null) {
//          test.init();
//          DataTuple test_tuple = test.readTuple();
//          while (test_tuple != null) {
//              int position = locateTuple(test_tuple);
//              ClassificationStat stat = (ClassificationStat) model.predictWeighted(test_tuple);
//              double[][] counts = stat.getClassCounts().clone();
//              switch (Settings.m_ClassificationVoteType.getValue()) { // default is Probability distribution Vote
//                  case Settings.VOTING_TYPE_MAJORITY:
//                      counts = transformToMajority(counts);
//                      break;
//                  case Settings.VOTING_TYPE_PROBAB_DISTR:
//                      counts = transformToProbabilityDistribution(counts);
//                      break;
//                  default:
//                      counts = transformToProbabilityDistribution(counts);
//              }
//              m_AvgPredictions[position] = counts;
//              // System.out.println(test_tuple.toString());
//              // System.out.println(stat.getString());
//              // System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
//              // m_AvgPredictions[position][0][2]);
//              test_tuple = test.readTuple();
//          }
//          test.init();
//      }
//  }
  
//  @Deprecated
//  public void addModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test, int nb_models) throws IOException, ClusException {
//      if (train != null) {
//          train.init();
//          DataTuple train_tuple = train.readTuple();
//          while (train_tuple != null) {
//              int position = locateTuple(train_tuple);
//              ClassificationStat stat = (ClassificationStat) model.predictWeighted(train_tuple);
//              double[][] counts = stat.getClassCounts().clone();
//              switch (Settings.m_ClassificationVoteType.getValue()) { // default is Probability distribution Vote
//                  case Settings.VOTING_TYPE_MAJORITY:
//                      counts = transformToMajority(counts);
//                      break;
//                  case Settings.VOTING_TYPE_PROBAB_DISTR:
//                      counts = transformToProbabilityDistribution(counts);
//                      break;
//                  default:
//                      counts = transformToProbabilityDistribution(counts);
//              }
//              m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, nb_models);
//              // System.out.println(train_tuple.toString());
//              // System.out.println(stat.getString());
//              // System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
//              // m_AvgPredictions[position][0][2]);
//
//              train_tuple = train.readTuple();
//          }
//          train.init();
//      }
//      if (test != null) {
//          test.init();
//          DataTuple test_tuple = test.readTuple();
//          while (test_tuple != null) {
//              int position = locateTuple(test_tuple);
//              ClassificationStat stat = (ClassificationStat) model.predictWeighted(test_tuple);
//              double[][] counts = stat.getClassCounts().clone();
//              switch (Settings.m_ClassificationVoteType.getValue()) { // default is Probability distribution Vote
//                  case Settings.VOTING_TYPE_MAJORITY:
//                      counts = transformToMajority(counts);
//                      break;
//                  case Settings.VOTING_TYPE_PROBAB_DISTR:
//                      counts = transformToProbabilityDistribution(counts);
//                      break;
//                  default:
//                      counts = transformToProbabilityDistribution(counts);
//              }
//              m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, nb_models);
//              // System.out.println(test_tuple.toString());
//              // System.out.println(stat.getString());
//              // System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
//              // m_AvgPredictions[position][0][2]);
//
//              test_tuple = test.readTuple();
//          }
//          test.init();
//      }
//  }
}

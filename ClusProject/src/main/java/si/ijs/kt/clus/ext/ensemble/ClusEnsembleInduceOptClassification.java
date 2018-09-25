
package si.ijs.kt.clus.ext.ensemble;

import java.io.IOException;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;


public class ClusEnsembleInduceOptClassification extends ClusEnsembleInduceOptimization {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private double[][][] m_AvgPredictions;

    //    public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException, ClusException {
    //        super(train, test, nb_tuples);
    //    }


    public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test, Settings sett) throws IOException, ClusException {
        super(train, test, sett);
    }


    @Override
    public void initPredictions(ClusStatistic stat, ClusEnsembleROSInfo ensembleROSInfo) {
        ClassificationStat nstat = (ClassificationStat) stat;
        m_AvgPredictions = new double[m_TuplePositions.size()][nstat.getNbAttributes()][];
        for (int tuple = 0; tuple < m_AvgPredictions.length; tuple++) {
	        for (int i = 0; i < nstat.getNbAttributes(); i++) {
	            m_AvgPredictions[tuple][i] = new double[nstat.getNbClasses(i)];
	        }
        }

        super.m_EnsembleROSInfo = ensembleROSInfo;
    }


    @Override
    public synchronized void updatePredictionsForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException, InterruptedException {
        m_NbUpdatesLock.writingLock();
        m_AvgPredictionsLock.writingLock();
        m_NbUpdates++;

        // for ROS
        if (getSettings().getEnsemble().isEnsembleROSEnabled()) {
            int[] enabledTargets = m_EnsembleROSInfo.getOnlyTargets(m_EnsembleROSInfo.getModelSubspace(m_NbUpdates - 1)); // model (m_NbUpdates-1) uses enabledTargets
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


    private void updateTuplesWithModel(TupleIterator iterator, ClusModel model) throws IOException, ClusException, InterruptedException {
        if (iterator != null) {
            iterator.init();
            DataTuple tuple = iterator.readTuple();

            double[][] tmp = ((ClassificationStat) model.predictWeighted(tuple)).getClassCounts().clone();

            double[][] zeros = new double[tmp.length][tmp[0].length];
            for (int i = 0; i < zeros.length; i++)
                for (int j = 0; j < zeros[i].length; j++)
                    zeros[i][j] = 0.0;

            while (tuple != null) {
                int position = locateTuple(tuple);

                // initialize values
                if (m_NbUpdates == 1)
                    m_AvgPredictions[position] = zeros.clone();

                ClassificationStat stat = (ClassificationStat) model.predictWeighted(tuple);
                double[][] counts = stat.getClassCounts().clone();
                switch (getSettings().getEnsemble().getEnsembleVotingType()) {// default is Probability distribution vote
                    case Majority:
                        counts = transformToMajority(counts);
                        break;
                    case ProbabilityDistribution:
                    default:
                        counts = transformToProbabilityDistribution(counts);
                        break;
                }

                //m_AvgPredictions[position] = (m_NbUpdates == 1) ? counts : incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);
                m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, m_NbUpdates);

                tuple = iterator.readTuple();
            }
            iterator.init();
        }
    }


    @Override
    public int getPredictionLength(int tuple) {
        return m_AvgPredictions[tuple].length;
    }


    public double[] getPredictionValueClassification(int tuple, int attribute) {
        return m_AvgPredictions[tuple][attribute];
    }


    @Override
    public void roundPredictions() {
        //System.err.println("Rounding up predictions!");
        for (int i = 0; i < m_AvgPredictions.length; i++) {
            for (int j = 0; j < m_AvgPredictions[i].length; j++) {
                for (int k = 0; k < m_AvgPredictions[i][j].length; k++) {
                    //System.err.println("Before: " + m_AvgPredictions[i][j][k]);
//                    m_AvgPredictions[i][j][k] = Double.parseDouble(ClusFormat.FOUR_AFTER_DOT.format(m_AvgPredictions[i][j][k]));
                	m_AvgPredictions[i][j][k] = ClusUtil.roundToSignificantFigures(m_AvgPredictions[i][j][k], SIGNIFICANT_DIGITS_IN_PREDICTIONS);
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
    //              // ClusLogger.info(train_tuple.toString());
    //              // ClusLogger.info(stat.getString());
    //              // ClusLogger.info(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
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
    //              // ClusLogger.info(test_tuple.toString());
    //              // ClusLogger.info(stat.getString());
    //              // ClusLogger.info(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
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
    //              // ClusLogger.info(train_tuple.toString());
    //              // ClusLogger.info(stat.getString());
    //              // ClusLogger.info(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
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
    //              // ClusLogger.info(test_tuple.toString());
    //              // ClusLogger.info(stat.getString());
    //              // ClusLogger.info(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " +
    //              // m_AvgPredictions[position][0][2]);
    //
    //              test_tuple = test.readTuple();
    //          }
    //          test.init();
    //      }
    //  }
}

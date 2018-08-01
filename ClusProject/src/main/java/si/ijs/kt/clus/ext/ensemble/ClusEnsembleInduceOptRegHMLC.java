
package si.ijs.kt.clus.ext.ensemble;

import java.io.IOException;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStatBase;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;


public class ClusEnsembleInduceOptRegHMLC extends ClusEnsembleInduceOptimization {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private double[][] m_AvgPredictions;

    // public ClusEnsembleInduceOptRegHMLC(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException,
    // ClusException {
    // super(train, test, nb_tuples);
    // }


    public ClusEnsembleInduceOptRegHMLC(TupleIterator train, TupleIterator test, Settings sett) throws IOException, ClusException {
        super(train, test, sett);
    }


    @Override
    public void initPredictions(ClusStatistic stat, ClusEnsembleROSInfo ensembleROSInfo) {
        m_AvgPredictions = new double[m_TuplePositions.size()][stat.getNbAttributes()]; // m_HashCodeTuple.length

        super.m_EnsembleROSInfo = ensembleROSInfo;
    }


    @Override
    public synchronized void updatePredictionsForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException, InterruptedException {
        m_NbUpdatesLock.writingLock();
        m_AvgPredictionsLock.writingLock();
        m_NbUpdates++;

        // for ROS
        if (getSettings().getEnsemble().isEnsembleROSEnabled()) { throw new ClusException("si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduceOptRegHMLC.updatePredictionsForTuples(ClusModel, TupleIterator, TupleIterator): ROS not implemented for optimized ensembles.");
        // FIXME: ROS implement optimized MTR
        /*
         * switch (getSettings().getEnsemble().getEnsembleROSAlgorithmType()) {
         * case FixedSubspaces:
         * // model (m_NbUpdates-1) uses enabledTargets
         * int[] enabledTargets = m_EnsembleROSInfo.getOnlyTargets(m_EnsembleROSInfo.getModelSubspace(m_NbUpdates - 1));
         * m_EnsembleROSInfo.incrementCoverageOpt(enabledTargets);
         * case DynamicSubspaces:
         * throw new
         * ClusException("si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduceOptRegHMLC.updatePredictionsForTuples(ClusModel, TupleIterator, TupleIterator) not implemented"
         * );
         * }
         */
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
                if (m_NbUpdates == 1) {
                    RegressionStatBase stat = (RegressionStatBase) model.predictWeighted(test_tuple);
                    m_AvgPredictions[position] = stat.getNumericPred();
                }
                else {
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


    @Override
    public int getPredictionLength(int tuple) {
        return m_AvgPredictions[tuple].length;
    }


    public double getPredictionValue(int tuple, int attribute) {
        return m_AvgPredictions[tuple][attribute];
    }


    @Override
    public void roundPredictions() {
        // System.out.println("Rounding up predictions!");
        for (int i = 0; i < m_AvgPredictions.length; i++) {
            for (int j = 0; j < m_AvgPredictions[i].length; j++) {
                // System.out.println("Before: " + m_AvgPredictions[i][j]);
                // m_AvgPredictions[i][j] =
                // Double.parseDouble(ClusFormat.FOUR_AFTER_DOT.format(m_AvgPredictions[i][j]));
                m_AvgPredictions[i][j] = ClusUtil.roundToSignificantFigures(m_AvgPredictions[i][j], SIGNIFICANT_DIGITS_IN_PREDICTIONS);
                // System.out.println("After: " + m_AvgPredictions[i][j]);
            }
        }
    }
}

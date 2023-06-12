
package si.ijs.kt.clus.ext.semisupervised.confidence.regression;

import java.util.ArrayList;
import java.util.Map;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.semisupervised.Helper;
import si.ijs.kt.clus.ext.semisupervised.confidence.PredictionConfidence;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLAggregation;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLNormalization;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Calculates the confidence of predictions as standard deviation of votes of
 * the trees in random forest tree ensemble. High variance equals small reliability,
 * and vice versa.
 * This also handles HMC, since HMC is implemented via RegressionStat
 * 
 * @author jurical
 *
 */
public class VarianceScore extends PredictionConfidence {

    Map<Integer, double[]> stdDevs;
    int nb_unlabeled;


    public VarianceScore(ClusStatManager statManager, SSLNormalization normalizationType, SSLAggregation aggregationType) throws ClusException {
        super(statManager, normalizationType, aggregationType);

        if (!statManager.getSettings().getEnsemble().isEnsembleMode()) { throw new ClusException("If prediction confidence uses Standard Deviation then ensembles have to be used. Please use -forest option"); }
    }


    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) throws ClusException, InterruptedException {

        model.predictWeighted(tuple);

        // calculate standard deviations of the predictions
        return processVotes(((ClusForest) model).getVotes());
    }


    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) throws InterruptedException {
        return processVotes(model.getOOBVotes(tuple));
    }


    /**
     * Procees votes of the trees
     * 
     * @param votes
     *        Votes of a ClusForest model

     */
    private double[] processVotes(ArrayList votes) {
        switch (m_StatManager.getTargetMode()) {
            case REGRESSION:
                return processVotesRegression(votes);
            case HIERARCHICAL:
                return processVotesHMC(votes);
        }

        return processVotesRegression(votes);
    }


    private double[] processVotesRegression(ArrayList votes) {
        double[][] predicts = new double[getNbTargetAttributes()][votes.size()];
        RegressionStat stat;

        for (int i = 0; i < votes.size(); i++) {

            stat = (RegressionStat) votes.get(i);

            for (int j = 0; j < stat.getNbAttributes(); j++) {
                // if((stat.m_SumWeights == null)) {
                // ClusLogger.info("Bug");
                // //FIXME: There is some bug, if an example has many missing values then some trees return null instead
                // of vote
                // }
                predicts[j][i] = stat.getMean(j);
            }
        }

        return calcStDev(predicts);
    }


    private double[] processVotesHMC(ArrayList votes) {
        double[] m_Means = new double[getNbTargetAttributes()];
        WHTDStatistic vote;
        int nb_votes = votes.size();
        for (int j = 0; j < nb_votes; j++) {
            vote = (WHTDStatistic) votes.get(j);
            for (int i = 0; i < getNbTargetAttributes(); i++) {
                m_Means[i] += vote.m_Means[i] / nb_votes;
            }
        }

        // return calcStDev(predicts);
        return m_Means;
    }


    /**
     * Calculates per target standard deviations of votes of ensemble
     */
    private double[] calcStDev(double[][] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            // result[i] = Helper.stDevOpt(values[i]); <- this implementation of stdDev is buggy
            if (values[i].length <= 1)
                result[i] = 0; // if we have only one vote, we set std dev to 0 (it can happen for OOB predictions if we
                               // have ensemble with very few trees.
            else
                result[i] = Helper.getStdDev(values[i]);
        }
        return result;
    }
}
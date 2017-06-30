package clus.ext.semisupervised.confidence.regression;

import java.util.ArrayList;
import java.util.Map;
import clus.data.rows.DataTuple;
import clus.ext.ensembles.ClusForest;
import clus.ext.hierarchical.WHTDStatistic;
import clus.ext.semisupervised.Helper;
import clus.ext.semisupervised.confidence.PredictionConfidence;
import clus.ext.semisupervised.confidence.aggregation.AverageHMC;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.RegressionStat;
import clus.util.ClusException;

/**
 * Calculates the confidence of predictions as standard deviation of votes of 
 * the trees in random forest tree ensemble. High variance -> small reliability, 
 * and vice versa. 
 * This also handles HMC, since HMC is implemented via RegressionStat
 * @author jurical
 *
 */
public class VarianceScore extends PredictionConfidence {

    Map<Integer, double[]> stdDevs;
    int nb_unlabeled;

    public VarianceScore(ClusStatManager statManager, int normalizationType, int aggregationType) throws ClusException {
        super(statManager, normalizationType, aggregationType);

        if (!statManager.getSettings().isEnsembleMode()) {
            throw new ClusException("If prediction confidence uses Standard Deviation then ensembles have to be used. Please use -forest option");
        }
    }

    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) {

        model.predictWeighted(tuple);

        //calculate standard deviations of the predictions
        return processVotes(((ClusForest) model).getVotes());
    }

    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) {
        return processVotes(model.getOOBVotes(tuple));
    }

    /**
     * Procees votes of the trees
     * @param votes Votes of a ClusForest model
     * @return
     */
    private double[] processVotes(ArrayList votes) {
        switch (m_StatManager.getMode()) {
            case ClusStatManager.MODE_REGRESSION:
                return processVotesRegression(votes);
            case ClusStatManager.MODE_HIERARCHICAL:
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
//                if((stat.m_SumWeights == null)) {
//                    System.out.println("Bug");
//                    //FIXME: There is some bug, if an example has many missing values then some trees return null instead of vote
//                }
                predicts[j][i] = stat.getMean(j);
            }
        }

        return calcStDev(predicts);
    }

    private double[] processVotesHMC(ArrayList votes) {
        double[] m_Means = new double[getNbTargetAttributes()];
		WHTDStatistic vote;
		int nb_votes = votes.size();
		for (int j = 0; j < nb_votes; j++){
			vote = (WHTDStatistic) votes.get(j);
			for (int i = 0; i < getNbTargetAttributes(); i++){
				m_Means[i] += vote.m_Means[i] / nb_votes;
			}
		}

        //return calcStDev(predicts);
        return m_Means;
    }

    /**
     * Calculates per target standard deviations of votes of ensemble
     *
     * @param values
     * @return
     */
    private double[] calcStDev(double[][] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            //result[i] = Helper.stDevOpt(values[i]); <- this implementation of stdDev is buggy
        	if(values[i].length <= 1)
        		result[i] = 0; //if we have only one vote, we set std dev to 0 (it can happen for OOB predictions if we have ensemble with very few trees.
        	else
        		result[i] = Helper.getStdDev(values[i]);
        }
        return result;
    }
}
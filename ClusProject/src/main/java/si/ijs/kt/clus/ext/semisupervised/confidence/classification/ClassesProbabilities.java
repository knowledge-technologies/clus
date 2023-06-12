
package si.ijs.kt.clus.ext.semisupervised.confidence.classification;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.semisupervised.Helper;
import si.ijs.kt.clus.ext.semisupervised.confidence.PredictionConfidence;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLAggregation;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLNormalization;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Calculate the confidence of prediction for the multi-target
 * classification as follows: For each target, get the max classification
 * probability (i.e., majority) among possible class values.
 * 
 * @author jurical
 */
public class ClassesProbabilities extends PredictionConfidence {

    public ClassesProbabilities(ClusStatManager statManager, SSLNormalization normalizationType, SSLAggregation aggregationType) throws ClusException {
        super(statManager, normalizationType, aggregationType);
    }


    @Override
    public int getNbTargetAttributes() {
        return m_StatManager.getSchema().getNbTargetAttributes();
    }


    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) throws ClusException, InterruptedException {
        ClusStatistic stat = new ClassificationStat(m_StatManager.getSettings(), m_StatManager.getSchema().getNominalAttrUse(AttributeUseType.Target));
        ;
        stat = model.predictWeighted(tuple);
        double[][] classCounts;

        classCounts = ((ClassificationStat) stat).getClassCounts();

        double[] confidence = new double[classCounts.length];

        for (int j = 0; j < classCounts.length; j++) {
            // confidence is equal to the probability of majority class
            confidence[j] = Helper.max(classCounts[j]) / Helper.sum(classCounts[j]);
        }

        return confidence;
    }


    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) throws ClusException, InterruptedException {
        ClusStatistic stat = new ClassificationStat(m_StatManager.getSettings(), m_StatManager.getSchema().getNominalAttrUse(AttributeUseType.Target));
        ;
        stat = model.predictWeightedOOB(tuple);
        double[][] classCounts;

        classCounts = ((ClassificationStat) stat).getClassCounts();

        double[] confidence = new double[classCounts.length];

        for (int j = 0; j < classCounts.length; j++) {
            confidence[j] = Helper.max(classCounts[j]);
        }

        return confidence;
    }
}

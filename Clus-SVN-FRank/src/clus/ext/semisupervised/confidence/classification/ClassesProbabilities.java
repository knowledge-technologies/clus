package clus.ext.semisupervised.confidence.classification;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.ext.ensembles.ClusForest;
import clus.ext.semisupervised.Helper;
import clus.ext.semisupervised.confidence.PredictionConfidence;
import clus.ext.semisupervised.confidence.normalization.Normalization;
import clus.main.ClusStatManager;
import clus.model.ClusModel;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

/**
 * Calculate the confidence of prediction for the multi-target
 * classification as follows: For each target, get the max classification
 * probability (i.e., majority) among possible class values.
 * @author jurical
 */
public class ClassesProbabilities extends PredictionConfidence {

    /**
     *
     * @param statManager
     * @param Type \in {"average", "minimum"}
     * @throws ClusException
     */
    public ClassesProbabilities(ClusStatManager statManager, int normalizationType, int aggregationType) throws ClusException {
        super(statManager, normalizationType, aggregationType);
    }

    @Override
    public int getNbTargetAttributes() {
        return m_StatManager.getSchema().getNbTargetAttributes();
    }

    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) {
        ClusStatistic stat = new ClassificationStat(m_StatManager.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));;
        stat = model.predictWeighted(tuple);
        double[][] classCounts;
        
        classCounts = ((ClassificationStat) stat).getClassCounts();

        double[] confidence = new double[classCounts.length];

        for (int j = 0; j < classCounts.length; j++) {
            //confidence is equal to the probability of majority class
            confidence[j] = Helper.max(classCounts[j]) / Helper.sum(classCounts[j]);
        }
        
        return confidence;
    }

    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) {
        ClusStatistic stat = new ClassificationStat(m_StatManager.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));;
        stat = ((ClusForest)model).predictWeightedOOB(tuple);
        double[][] classCounts;
        
        classCounts = ((ClassificationStat) stat).getClassCounts();

        double[] confidence = new double[classCounts.length];

        for (int j = 0; j < classCounts.length; j++) {
            confidence[j] = Helper.max(classCounts[j]);
        }
        
        return confidence;
    }
}


package si.ijs.kt.clus.ext.semisupervised.confidence.regression;

import java.util.HashMap;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.hmlc.HierErrorMeasures;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.semisupervised.confidence.PredictionConfidence;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsHMLC.HierarchyMeasures;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLAggregation;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLNormalization;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Class which determines reliability score of an unlabeled example e_u as
 * follows: r(e_u) = sum_{e_l} w_l * oobError(e_l), where w_l is random forest
 * proximity of e_u to labeled example e_l, and oonError return out-of-bag error
 * of labeled example e_u.
 *
 * Random forest proximity (for more details see Breiman's web page) of two
 * examples x and y is defined as (#leafs where x and y are together)/#trees
 *
 * @author jurical
 */
public class RForestProximities extends PredictionConfidence {

    RowData m_trainingSet;
    int m_origLabeledMax;
    boolean proximitiesInitialized = false;


    public RForestProximities(ClusStatManager statManager, SSLNormalization normalizationType, SSLAggregation aggregationType) {
        super(statManager, normalizationType, aggregationType);
    }


    /**
     * Calculates expected error (i.e., reliability score) of an unlabeled
     * example e_u as follows: r(e_u) = sum_{e_l} w_l * oobError(e_l), where w_l
     * is proximity of e_u to labeled example e_l, and oonError return
     * out-of-bag error of labeled example e_u
     *
     * @param model
     * @return
     * @throws ClusException
     * @throws InterruptedException
     */
    private double[] calculateExpectedError(ClusModel model) throws ClusException, InterruptedException {

        HashMap<Integer, Double> proximities = ((ClusForest) model).getProximities();
        // double sumProximities = 0;
        double[] expectedOOBE = new double[getNbTargetAttributes()];
        ClusErrorList errListOOB;
        ClusError error;

        DataTuple tupleLabeled;
        for (int j = 0; j < m_origLabeledMax; j++) {
            tupleLabeled = m_trainingSet.getTuple(j);
            if (proximities.containsKey(tupleLabeled.getIndex())) {
                if (((ClusForest) model).containsOOBForTuple(tupleLabeled)) {

                    errListOOB = new ClusErrorList();

                    switch (m_StatManager.getMode()) {
                        // Pooled AUPRC (more is better)
                        case ClusStatManager.MODE_HIERARCHICAL:
                            error = new HierErrorMeasures(errListOOB, m_StatManager.getHier(), m_StatManager.getSettings().getHMLC().getRecallValues().getDoubleVector(), HierarchyMeasures.PooledAUPRC, m_StatManager.getSettings().getOutput().isWriteCurves(), m_StatManager.getSettings().getOutput().isGzipOutput());
                            break;

                        // RMSE (less is better)
                        case ClusStatManager.MODE_REGRESSION:
                            error = new RMSError(errListOOB, m_StatManager.getSchema().getNumericAttrUse(AttributeUseType.Target));
                            break;

                        case ClusStatManager.MODE_CLASSIFY:
                            error = new Accuracy(errListOOB, m_StatManager.getSchema().getNominalAttrUse(AttributeUseType.Target));
                            break;
                        default:
                            error = new RMSError(errListOOB, m_StatManager.getSchema().getNumericAttrUse(AttributeUseType.Target));
                    }

                    errListOOB.addError(error);
                    errListOOB.addExample(tupleLabeled, ((ClusForest) model).predictWeightedOOB(tupleLabeled));

                    for (int k = 0; k < getNbTargetAttributes(); k++) {
                        expectedOOBE[k] += proximities.get(tupleLabeled.getIndex()) * error.getModelErrorComponent(k);
                    }
                }
            }
        }

        return expectedOOBE;
    }


    @Override
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) throws ClusException, InterruptedException {
        ((ClusForest) model).predictWeightedStandardAndGetProximities(tuple);

        return calculateExpectedError(model);
    }


    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) throws ClusException, InterruptedException {
        model.predictWeightedOOBAndGetProximities(tuple);

        return calculateExpectedError(model);
    }


    @Override
    public void calculateConfidenceScores(ClusModel model, RowData unlabeledData) throws ClusException, InterruptedException {

        // initialize proximities
        if (!proximitiesInitialized) { // avoid initializing proximities twice for the same mode (e.g., if
                                       // calculateOOBConfidenceScores was called before this)
            for (int j = 0; j < m_origLabeledMax; j++) {
                ((ClusForest) model).initializeProximities(m_trainingSet.getTuple(j));
            }
        }

        super.calculateConfidenceScores(model, unlabeledData);

        proximitiesInitialized = false;
    }


    @Override
    public void calculateOOBConfidenceScores(ClusForest model, RowData data) throws ClusException, InterruptedException {
        // initialize proximities
        if (!proximitiesInitialized) { // avoid initializing proximities twice for the same mode (e.g., if
                                       // calculateOOBConfidenceScores was called before this)
            for (int j = 0; j < m_origLabeledMax; j++) {
                model.initializeProximities(m_trainingSet.getTuple(j));
            }
        }

        proximitiesInitialized = true;

        super.calculateOOBConfidenceScores(model, data);
    }


    public void setTrainingSet(RowData trainingSet, int origLabeledMax) {
        m_trainingSet = trainingSet;
        m_origLabeledMax = origLabeledMax;
    }
}

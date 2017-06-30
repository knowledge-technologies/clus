package clus.ext.semisupervised.confidence.regression;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.error.Accuracy;
import clus.error.ClusError;
import clus.error.ClusErrorList;
import clus.error.RMSError;
import clus.ext.ensembles.ClusForest;
import clus.ext.hierarchical.HierErrorMeasures;
import clus.ext.semisupervised.confidence.PredictionConfidence;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;
import java.util.HashMap;

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

    public RForestProximities(ClusStatManager statManager, int normalizationType, int aggregationType) {
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
     */
    private double[] calculateExpectedError(ClusModel model) {

        HashMap<Integer, Double> proximities = ((ClusForest) model).getProximities();
        //double sumProximities = 0;
        double[] expectedOOBE = new double[getNbTargetAttributes()];
        ClusErrorList errListOOB;
        ClusError error;

        DataTuple tupleLabeled;
        for (int j = 0; j < m_origLabeledMax; j++) {
            tupleLabeled = m_trainingSet.getTuple(j);
            if (proximities.containsKey(tupleLabeled.getIndex())) {
                if (((ClusForest) model).containsOOBForTuple(tupleLabeled)) {


                    errListOOB = new ClusErrorList();

                    switch (ClusStatManager.getMode()) {
                        //Pooled AUPRC (more is better)
                        case ClusStatManager.MODE_HIERARCHICAL:
                            error = new HierErrorMeasures(errListOOB, m_StatManager.getHier(), m_StatManager.getSettings().getRecallValues().getDoubleVector(), m_StatManager.getSettings().getCompatibility(), Settings.HIERMEASURE_POOLED_AUPRC, m_StatManager.getSettings().isWriteCurves());
                            break;

                        //RMSE (less is better)
                        case ClusStatManager.MODE_REGRESSION:
                            error = new RMSError(errListOOB, m_StatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
                            break;

                        case ClusStatManager.MODE_CLASSIFY:
                            error = new Accuracy(errListOOB, m_StatManager.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
                            break;
                        default:
                            error = new RMSError(errListOOB, m_StatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
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
    public double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) {
        ((ClusForest) model).predictWeightedStandardAndGetProximities(tuple);

        return calculateExpectedError(model);
    }

    @Override
    public double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) {
        ((ClusForest) model).predictWeightedOOBAndGetProximities(tuple);

        return calculateExpectedError(model);
    }

    @Override
    public void calculateConfidenceScores(ClusModel model, RowData unlabeledData) throws ClusException {

        //initialize proximities
        if (!proximitiesInitialized) { //avoid initializing proximities twice for the same mode (e.g., if calculateOOBConfidenceScores was called before this)
            for (int j = 0; j < m_origLabeledMax; j++) {
                ((ClusForest) model).initializeProximities(m_trainingSet.getTuple(j));
            }
        }

        super.calculateConfidenceScores(model, unlabeledData);

        proximitiesInitialized = false;
    }

    @Override
    public void calculateOOBConfidenceScores(ClusForest model, RowData data) throws ClusException {
        //initialize proximities
        if (!proximitiesInitialized) { //avoid initializing proximities twice for the same mode (e.g., if calculateOOBConfidenceScores was called before this)
            for (int j = 0; j < m_origLabeledMax; j++) {
                ((ClusForest) model).initializeProximities(m_trainingSet.getTuple(j));
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


package si.ijs.kt.clus.ext.semisupervised;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Doubles;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataPreprocs;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.ext.hierarchical.HierErrorMeasures;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.ext.hierarchical.thresholdcalibration.HierThresholdCalibration;
import si.ijs.kt.clus.ext.hierarchical.thresholdcalibration.calibrateByLabelCardinality;
import si.ijs.kt.clus.ext.semisupervised.confidence.OracleScore;
import si.ijs.kt.clus.ext.semisupervised.confidence.PredictionConfidence;
import si.ijs.kt.clus.ext.semisupervised.confidence.RandomScore;
import si.ijs.kt.clus.ext.semisupervised.confidence.classification.ClassesProbabilities;
import si.ijs.kt.clus.ext.semisupervised.confidence.regression.RForestProximities;
import si.ijs.kt.clus.ext.semisupervised.confidence.regression.VarianceScore;
import si.ijs.kt.clus.ext.semisupervised.utils.DoublesPair;
import si.ijs.kt.clus.ext.semisupervised.utils.IndiceValuePair;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsHMLC;
import si.ijs.kt.clus.main.settings.SettingsSSL;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;


public class ClusSelfTrainingInduce extends ClusSemiSupervisedInduce {

    ClusInductionAlgorithm m_Induce; //underlying algorithm for self-training
    PredictionConfidence m_PredConfidence;
    double m_Threshold; //threshold to instances will be put into training set
    int m_StoppingCriteria, m_UnlabeledCriteria, m_K, m_Iterations, m_maxAirbagTrials, m_OOBErrorCalculation, m_Mode, m_Normalization, m_Aggregation;
    boolean m_UseWeights;
    double[] m_ExhaustiveSearchThresholds;
    double HMCthreshold = 0; //used only in HMCmode



    public ClusSelfTrainingInduce(ClusSchema schema, Settings sett, ClusInductionAlgorithm clss_induce)
            throws ClusException,
            IOException {
        super(schema, sett);

        m_Induce = clss_induce;
        m_Mode = m_StatManager.getMode();

        //maybe initialize settings parameters
        initialize(getSchema(), getSettings());

        switch (m_Mode) {
            case ClusStatManager.MODE_REGRESSION:
                switch (sett.getSSL().getConfidenceMeasure()) {
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMUNIFORM:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_UNIFORM);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMGAUSSIAN:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_GAUSSIAN);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_ORACLE:
                        m_PredConfidence = new OracleScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_VARIANCE:
                        m_PredConfidence = new VarianceScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RFPROXIMITIES:
                        m_PredConfidence = new RForestProximities(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    default:
                        //default
                        m_PredConfidence = new VarianceScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                }
                break;
            case ClusStatManager.MODE_HIERARCHICAL:
                switch (sett.getSSL().getConfidenceMeasure()) {
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_CLASSESPROBABILITIES:
                        if (m_Aggregation == SettingsSSL.SSL_AGGREGATION_AVERAGE) {
                            m_Aggregation = SettingsSSL.SSL_AGGREGATION_AVERAGEIGNOREZEROS; //in HMC it probably makes sense to use average which ignores zeros, because usually there are a lot of zeros
                        }
                        m_PredConfidence = new VarianceScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_ORACLE:
                        m_PredConfidence = new OracleScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMUNIFORM:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_UNIFORM);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMGAUSSIAN:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_GAUSSIAN);
                        break;
                    default:
                        //default
                        m_PredConfidence = new VarianceScore(m_StatManager, m_Normalization, SettingsSSL.SSL_AGGREGATION_AVERAGEIGNOREZEROS);
                        break;
                }
                break;
            case ClusStatManager.MODE_CLASSIFY:
                switch (sett.getSSL().getConfidenceMeasure()) {
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_CLASSESPROBABILITIES:
                        m_PredConfidence = new ClassesProbabilities(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_ORACLE:
                        m_PredConfidence = new OracleScore(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMUNIFORM:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_UNIFORM);
                        break;
                    case SettingsSSL.SSL_CONFIDENCE_MEASURE_RANDOMGAUSSIAN:
                        m_PredConfidence = new RandomScore(m_StatManager, RandomScore.RANDOM_GAUSSIAN);
                        break;
                    default:
                        //default
                        m_PredConfidence = new ClassesProbabilities(m_StatManager, m_Normalization, m_Aggregation);
                        break;
                }
                break;
            default:
                throw new ClusException("Mode not yet supported, for now you can do SSL for multi-target regression, classification and HMC");
        }
    }



    public void initialize(ClusSchema schema, Settings settx) {
        SettingsSSL sett = settx.getSSL();

        m_UnlabeledCriteria = sett.getUnlabeledCriteria();
        m_StoppingCriteria = sett.getStoppingCriteria();
        m_Threshold = sett.getConfidenceThreshold();
        m_K = sett.getK();
        m_Iterations = sett.getIterations();
        m_PercentageLabeled = sett.getPercentageLabeled() / 100.0;
        m_UseWeights = sett.getUseWeights();
        m_maxAirbagTrials = sett.getAirbagTrails();
        m_ExhaustiveSearchThresholds = sett.getExhaustiveSearchThresholds();
        m_OOBErrorCalculation = sett.getOOBErrorCalculation();
        m_Normalization = sett.getSSLNormalization();
        m_Aggregation = sett.getSSLAggregation();
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr)
            throws ClusException,
            IOException,
            InterruptedException {

        partitionData(cr); //set training set, unlabeled set and testing set (if needed) 

        int origLabeledMax = m_TrainingSet.getNbRows(); //store the indice of the last originally labeled instance, we can use this later to limit the OOBError estimate only in the originally labeled data
        int nb_unlabeled = m_UnlabeledData.getNbRows();
        int unlabeledAdded = 1, unlabeledAddeddTotal = 0; //set unlabeledAdded to 1 to enter the loop initially 
        int iterations = 0;
        int airBagTrials = 0;
        boolean first = true;
        double OOBError = Double.MAX_VALUE, newOOBError;
        double originalError = 0;
        double labelCardinalityTrainHMC = 0; // used for threshold calibration in HMC
        PrintWriter writer = null; //with this we write performances at each iterations to a separate file
        String errorType = "";
        switch (m_Mode) {
            case ClusStatManager.MODE_HIERARCHICAL:
                errorType = "AUPRC";
                break;
            case ClusStatManager.MODE_REGRESSION:
                errorType = "RMSE";
                break;
            case ClusStatManager.MODE_CLASSIFY:
                errorType = "Accuracy";
                break;
            default:
                errorType = "RMSE";
        }

        ClusRun myClusRun = new ClusRun(cr);

        ClusModel oldModel = null;

        //initialize OOB error, for HMC and classification more is better
        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_CLASSIFY) {
            OOBError = Double.MIN_VALUE;
        }

        //calculate label cardinality of train set, used to select the threshold for classification
        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL) {
            for (int i = 0; i < m_TrainingSet.getNbRows(); i++) {
                DataTuple t = m_TrainingSet.getTuple(i);
                labelCardinalityTrainHMC += ((ClassesTuple) t.m_Objects[0]).getNbClasses();
            }
            labelCardinalityTrainHMC /= m_TrainingSet.getNbRows();
        }

        //turn on calculation of RForest proximities if needed
        if (cr.getStatManager().getSettings().getSSL().getConfidenceMeasure() == SettingsSSL.SSL_CONFIDENCE_MEASURE_RFPROXIMITIES) {
            ((RForestProximities) m_PredConfidence).setTrainingSet(m_TrainingSet, origLabeledMax);
        }

        // 
        // BEGIN - MAIN LOOP
        // StoppingCriteria are:
        // 1) if no new instances are added to the training set, stop (always applies)
        // 2) if the number of iterations is set, stop after specified number of iterations
        // 3) airbag criteria, if set
        while (unlabeledAdded > 0
                && !(m_StoppingCriteria == SettingsSSL.SSL_STOPPING_CRITERIA_ITERATIONS
                        && iterations >= m_Iterations)) {

            unlabeledAdded = 0;
            iterations++;

            System.out.println();
            System.out.println("SelfTraining iteration: " + iterations);
            System.out.println();

            //we have to reset ClusOOBErrorEstimate, because it has static variables
            if (getSettings().getEnsemble().shouldEstimateOOB()) {
                ((ClusEnsembleInduce) m_Induce).resetClusOOBErrorEstimate();
            }

            //train base model
            if (!(m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_EXHAUSTIVESEARCH) || first) {
                m_Model = m_Induce.induceSingleUnpruned(myClusRun);
            }

            //save default models, which is supervised model, i.e., trained only on labeled data
            if (first) {
                first = false;
                ClusModelInfo defInfo = cr.addModelInfo(ClusModel.DEFAULT);
                defInfo.setModel(m_Model);

                // - just for testing
                RowData tt = cr.getTestSet();

                originalError = calculateError(cr.getTestSet()).getModelError();
                writer = new PrintWriter(cr.getStatManager().getSettings().getGeneric().getAppName() + "_SelfTrainingErrors.csv", "UTF-8");
                writer.println("Iteration,Threshold,examplesAdded,examplesAddedTotal," + errorType + "SSL," + errorType + "Supervised," + errorType + "OOBLabeled," + errorType + "OOBTrainingSet," + errorType + "TrainingSet");
                //
            }

            // BEGIN - AIRBAG stopping criteria
            if (m_StoppingCriteria == SettingsSSL.SSL_STOPPING_CRITERIA_AIRBAG) {

                //calculate OOBError
                newOOBError = getOOBError(m_TrainingSet, origLabeledMax).getModelError();
                //    writer.println(unlabeledAdded + "," + newOOBError);

                //for HMC more is better, for regression less is better
                if ((m_Mode == ClusStatManager.MODE_CLASSIFY && newOOBError < OOBError)
                        || (m_Mode == ClusStatManager.MODE_HIERARCHICAL && newOOBError < OOBError)
                        || (m_Mode == ClusStatManager.MODE_REGRESSION && newOOBError > OOBError)) {
                    airBagTrials++;
                    if (airBagTrials > m_maxAirbagTrials) {
                        m_Model = oldModel;
                        //System.out.println("");
                        System.out.println("Stopping because of airbag.");
                        break; //this stops the main while loop
                    }
                }
                else {
                    //store last models which improved over the previous one
                    OOBError = newOOBError;
                    oldModel = m_Model;
                    airBagTrials = 0;
                }
            } // END - AIRBAG stopping criteria

            // BEGIN - Automatic threshold selection, AUTOMATICOOB procedure
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_AUTOMATICOOB
                    || m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_AUTOMATICOOBINITIAL) {

                m_PredConfidence.calculateOOBConfidenceScores((ClusForest) m_Model, m_TrainingSet); //get OOB confidence scores of labeled examples 

                int maxInd;
                DoublesPair[] confidencesOOBErrors;
                double[] OOBErrors;

                switch (m_OOBErrorCalculation) {
                    case SettingsSSL.SSL_OOB_ERROR_CALCULATION_LABELED_ONLY: //calculate OOB error only on original labeled part
                        maxInd = origLabeledMax;
                        confidencesOOBErrors = new DoublesPair[origLabeledMax];
                        OOBErrors = new double[origLabeledMax];
                        break;
                    case SettingsSSL.SSL_OOB_ERROR_CALCULATION_ALL_DATA: //calculate OOB error on all examples in the training set, i.e., unlabeled examples added to the training set are also considered
                        maxInd = m_TrainingSet.getNbRows();
                        confidencesOOBErrors = new DoublesPair[m_PredConfidence.getCounter()];
                        OOBErrors = new double[m_PredConfidence.getCounter()];
                        break;

                    default:
                        maxInd = origLabeledMax;
                        confidencesOOBErrors = new DoublesPair[origLabeledMax];
                        OOBErrors = new double[origLabeledMax];
                }

                int j = 0;

                for (int i = 0; i < maxInd; i++) { //calculate OOB errors             
                    ClusErrorList errListOOB = new ClusErrorList();
                    ClusError error = null;

                    if (m_Mode == ClusStatManager.MODE_HIERARCHICAL) {
                        error = new HierErrorMeasures(errListOOB, m_StatManager.getHier(),
                                m_StatManager.getSettings().getHMLC().getRecallValues().getDoubleVector(),
                                getSettings().getGeneral().getCompatibility(),
                                SettingsHMLC.HIERMEASURE_POOLED_AUPRC,
                                m_StatManager.getSettings().getOutput().isWriteCurves(),
                                m_StatManager.getSettings().getOutput().isGzipOutput());
                    }

                    if (m_Mode == ClusStatManager.MODE_REGRESSION) {
                        error = new RMSError(errListOOB, this.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
                    }

                    if (m_Mode == ClusStatManager.MODE_CLASSIFY) {
                        error = new Accuracy(errListOOB, this.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
                    }

                    errListOOB.addError(error);

                    //careful, it can happen that RF do not contain OOB estimates for all tuples (theoretically, it can happen that tuple is not OOB)
                    if (!Double.isNaN(m_PredConfidence.getConfidence(i))) {
                        //FIXME: now instance is predicted once here and once in m_PredictionConfidence, it should be predicted only once
                        errListOOB.addExample(m_TrainingSet.getTuple(i), ((ClusForest) m_Model).predictWeightedOOB(m_TrainingSet.getTuple(i)));

                        DoublesPair dPair = new DoublesPair(m_PredConfidence.getConfidence(i), error.getModelError());

                        confidencesOOBErrors[j] = dPair;
                        j++;
                    }
                }

                System.out.println(m_PredConfidence.getCounter());

                Arrays.sort(confidencesOOBErrors); //sort confidence scores

                OOBErrors = Helper.getArrayOfSecond(confidencesOOBErrors);

                org.apache.commons.math3.stat.inference.TTest tTest = new org.apache.commons.math3.stat.inference.TTest();
                boolean thresholdFound = false;
                double confidenceLevel = 0.05; //TODO: probably should move to settings
                double pValue;

                //the need to be at least two examples in a set for tTest to be performed, that's why we check i > 1 (for loop) and i > 2 (while loop) 
                for (int i = OOBErrors.length - 1; i > 1; i--) {
                    //check if we have more examples with the same confidence score, if yes, they all go together
                    while (i > 2 && confidencesOOBErrors[i].getFirst() == confidencesOOBErrors[i - 1].getFirst()) {
                        i--;
                    }

                    pValue = tTest.tTest(OOBErrors, Arrays.copyOfRange(OOBErrors, 0, i));
                    //pValue = tTest.tTest(OOBErrors, Arrays.copyOfRange(OOBErrors, i, OOBErrors.length));

                    if (pValue < confidenceLevel) {
                        System.out.println("AutomaticOOB: found threshold: " + confidencesOOBErrors[i].getFirst() + " (p-value = " + pValue + ")");
                        m_Threshold = confidencesOOBErrors[i].getFirst();
                        thresholdFound = true;
                        break;
                    }
                }

                if (!thresholdFound) {
                    System.out.println("AutomaticOOB: threshold not found, using 0.0");
                    m_Threshold = 0.0;
                    //confidenceLevel += 0.025;
                }

                if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_AUTOMATICOOBINITIAL) {
                    m_UnlabeledCriteria = SettingsSSL.SSL_UNLABELED_CRITERIA_THRESHOLD;
                }
            } // END - Automatic threshold selection

            // BEGIN -  find classification threshold such that the difference between label cardinality of labeled examples and predicted unlabeled examples is minimum
            //          Applicable only when Hierarchical multi-label classification (HMC) is performed
            if (m_StatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
                if (cr.getStatManager().getSettings().getSSL().shouldCalibrateHmcThreshold()) {
                    HierThresholdCalibration thCalib = new calibrateByLabelCardinality(5, labelCardinalityTrainHMC);
                    DataTuple tuple;
                    for (int i = 0; i < nb_unlabeled; i++) {
                        tuple = m_UnlabeledData.getTuple(i);
                        if (tuple != null && tuple.getWeight() > 0) {
                            thCalib.addExample((WHTDStatistic) m_Model.predictWeighted(m_UnlabeledData.getTuple(i)));
                        }
                    }
                    HMCthreshold = thCalib.getThreshold();
                    //System.out.println("best th: " + HMCthreshold + "   card: " + ((calibrateByLabelCardinality)thCalib).getCardinality(HMCthreshold))
                }
                else {
                    HMCthreshold = 50;//m_Threshold * 100; //majority class
                } // END -  Find classification threshold
                if (m_StatManager.getSettings().getSSL().getConfidenceMeasure() == SettingsSSL.SSL_CONFIDENCE_MEASURE_ORACLE)
                    ((OracleScore) m_PredConfidence).setHmcThreshold(HMCthreshold);
            }

            m_PredConfidence.calculateConfidenceScores(m_Model, m_UnlabeledData); //calculate confidence scores on unlabeled examples 

            //
            // BEGIN - SELECTION OF THE UNLABELED EXAMPLES WHICH WILL BE MOVED TO THE TRAINING SET 
            //
            List<DataTuple> unlabeledForTrain = new ArrayList(); //instances which will be put from unlabeled set to the training set according to UnlabeledCriteria 

            // BEGIN -  K PERCENTAGE MOST AVERAGE
            //          The threshold is set to average confidence score of the K percentage most confident examples (set initially, and then this threshold is used throughout iterations)
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_KPERCENTAGEMOSTAVERAGE) {
                //compute the number of examples from percentage
                IndiceValuePair[] topK = Helper.greatestKelements(m_PredConfidence.getConfidenceScores(), (int) ((nb_unlabeled - unlabeledAddeddTotal) * (m_K / 100.0)));
                double threshold = 0;

                //calculate confidence score
                for (int i = 0; i < topK.length; i++) {
                    threshold += topK[i].getValue();
                }

                m_Threshold = threshold / topK.length;

                //we calculate new threshold only initially, therefore we set unlabeled criteria from now on to Threshold
                m_UnlabeledCriteria = SettingsSSL.SSL_UNLABELED_CRITERIA_THRESHOLD;

                System.out.println("Threshold selected: " + m_Threshold);
            } // END - K PERCENTAGE MOST AVERAGE 

            // BEGIN -  THRESHOLD, OR AUTOMATICOOB
            //          Unlabeled instances with confidence greater than the specified threshold will be added to the training set
            //          The Threshold may be determined previously in automatic fashion by AUTOMATICOOB procedure  
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_THRESHOLD
                    || m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_AUTOMATICOOB) {

                for (int i = 0; i < nb_unlabeled; i++) {
                    if (m_PredConfidence.getConfidence(i) >= m_Threshold) {
                        //add unlabeled with predicted target value(s) example to the train set 
                        DataTuple t = m_UnlabeledData.getTuple(i).deepCloneTuple();

                        processUnlabeledExample(t, i);

                        //add example to the train set
                        unlabeledForTrain.add(t);

                        unlabeledAdded++;
                    }
                }
            } // END -  THRESHOLD, OR AUTOMATICOOB

            // BEGIN -  K MOST CONFIDENT
            //          K most confident unlabeled examples are added to the training set
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_KMOSTCONFIDENT) {

                IndiceValuePair[] topK = Helper.greatestKelements(m_PredConfidence.getConfidenceScores(), m_K);

                int index;

                for (int i = 0; i < topK.length; i++) {
                    //get index of the an example
                    index = topK[i].getIndice();

                    //should we check also if the confidence is above threshold???
                    //if (m_PredConfidence.getConfidence(index) >= m_Threshold) {
                    DataTuple t = m_UnlabeledData.getTuple(index).deepCloneTuple();

                    processUnlabeledExample(t, index);

                    //add example to the train set
                    unlabeledForTrain.add(t);

                    unlabeledAdded++;
                    //}
                }
            } // END -  K MOST CONFIDENT

            // BEGIN -  K PERCENTAGE MOST CONFIDENT
            //          K perfent of most confident unlabeled examples are added to the training set
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_KPERCENTAGEMOSTCONFIDENT) {

                //compute the number of examples from percentage
                IndiceValuePair[] topK = Helper.greatestKelements(m_PredConfidence.getConfidenceScores(), (int) ((nb_unlabeled /*- unlabeledAddeddTotal*/) * (m_K / 100.0)));

                int index;

                for (int i = 0; i < topK.length; i++) {
                    //get index of the an example
                    index = topK[i].getIndice();

                    //should we check also if the confidence is above threshold???
                    //if (m_PredConfidence.getConfidence(index) >= m_Threshold) {
                    DataTuple t = m_UnlabeledData.getTuple(index).deepCloneTuple();

                    processUnlabeledExample(t, index);

                    //add example to the train set
                    unlabeledForTrain.add(t);

                    unlabeledAdded++;
                    //}
                }
            } // END -  K PERCENTAGE MOST CONFIDENT

            //
            // END - SELECTION OF THE UNLABELED EXAMPLES WHICH WILL BE MOVED TO THE TRAINING SET 
            //
            // BEGIN - EXHAUSTIVE SEARCH
            // 1) for each threshold, build a model
            // 2) evaluate model with OOBError
            // 3) if we are able to find better model than current, advance to next iteration
            // 3.1) TODO: maybe just use the best model we are able to found??? regardless if the OOB error is higher than current???
            // 4) otherwise stop iterating
            if (m_UnlabeledCriteria == SettingsSSL.SSL_UNLABELED_CRITERIA_EXHAUSTIVESEARCH) {

                Boolean betterFound, firstIteration = true;
                ClusModel bestModel = m_Model;
                double conf, bestOOB = Double.NaN;
                int examplesAddedPerThreshold;
                RowData bestTrainingSet;
                ClusStatistic stat;

                //thresholds given in settings file should be sorted from biggest to smallest
                Arrays.sort(m_ExhaustiveSearchThresholds);
                Collections.reverse(Doubles.asList(m_ExhaustiveSearchThresholds));

                do {
                    betterFound = false;
                    bestTrainingSet = m_TrainingSet.shallowCloneData();
                    double bestThreshold = 0;

                    if (firstIteration) {
                        bestOOB = getOOBError(m_TrainingSet, origLabeledMax).getModelError(); //calculate OOB Error only on original labeled data
                        firstIteration = false;
                    }

                    int bestUnlabeledAdded = 0;
                    int i = 0, best_i = 0;

                    //sort confidence scores
                    IndiceValuePair[] confidenceScoresSorted = new IndiceValuePair[nb_unlabeled];
                    for (int j = 0; j < nb_unlabeled; j++) {
                        confidenceScoresSorted[j] = new IndiceValuePair(j, m_PredConfidence.getConfidence(j));
                    }
                    Arrays.sort(confidenceScoresSorted, Collections.reverseOrder());

                    unlabeledAdded = 0;
                    conf = confidenceScoresSorted[0].getValue();

                    System.out.println("Started exhaustive search (current OOBError = " + bestOOB + ")");
                    System.out.println();

                    for (double threshold : m_ExhaustiveSearchThresholds) {
                        //it would we useful to have confidences sorted(0 - biggest conf, max - lower conf), so we dont have to loop over entire set of unlabeld examples every time
                        //from top to bottom
                        examplesAddedPerThreshold = 0;

                        while (conf >= threshold && i < nb_unlabeled) {
                            DataTuple t = m_UnlabeledData.getTuple(confidenceScoresSorted[i].getIndice());
                            //compute prediction
                            stat = m_Model.predictWeighted(t); //model.predictWeighted(tuple);
                            stat.computePrediction();
                            stat.predictTuple(t);

                            //add example to the train set
                            m_TrainingSet.add(t);

                            examplesAddedPerThreshold++;
                            i++;

                            if (i < nb_unlabeled) {
                                conf = confidenceScoresSorted[i].getValue();
                            }
                        }

                        //train new model only if some unlabeled data were added (i.e., confidence was greater than threshold at least once)
                        if (examplesAddedPerThreshold > 0) {

                            ((ClusEnsembleInduce) m_Induce).resetClusOOBErrorEstimate();
                            m_Model = m_Induce.induceSingleUnpruned(myClusRun);

                            unlabeledAdded += examplesAddedPerThreshold;

                            //ClusError errTest = calculateError(myClusRun.getTestSet());
                            ClusError errOOB;
                            switch (m_OOBErrorCalculation) {
                                case SettingsSSL.SSL_OOB_ERROR_CALCULATION_LABELED_ONLY:
                                    errOOB = getOOBError(m_TrainingSet, origLabeledMax);
                                    break;
                                case SettingsSSL.SSL_OOB_ERROR_CALCULATION_ALL_DATA:
                                    errOOB = getOOBError(m_TrainingSet, m_TrainingSet.getNbRows());
                                    break;
                                default:
                                    errOOB = getOOBError(m_TrainingSet, origLabeledMax);
                            }

                            OOBError = errOOB.getModelError();

                            //writerTest.println(errTest.getModelError() + "," + errTrain.getModelError() + "," + OOBError);
                            System.out.println("Trying threshold: " + threshold);
                            System.out.println("OOBError: " + OOBError);
                            System.out.println("Examples added: " + unlabeledAdded);
                            System.out.println("Training set size: " + m_TrainingSet.getNbRows());
                            System.out.println();

                            if (OOBError < bestOOB) {
                                bestOOB = OOBError;
                                bestModel = m_Model;
                                betterFound = true;
                                best_i = i;
                                bestThreshold = threshold;
                                bestUnlabeledAdded = unlabeledAdded;
                                bestTrainingSet = m_TrainingSet.shallowCloneData();
                            }
                            System.out.println("Th: " + threshold + " BtSize: " + bestTrainingSet.getNbRows());
                        }
                    }

                    m_TrainingSet = bestTrainingSet;
                    myClusRun.setTrainingSet(m_TrainingSet);

                    unlabeledAdded = bestUnlabeledAdded;

                    if (betterFound) {
                        System.out.println("Found threshold: " + bestThreshold + " (new OOBError = " + bestOOB + ")");
                        m_Model = bestModel;
                        //remove unlabeled examples which were moved to training set from unlabeled set
                        for (int j = 0; j < best_i; j++) {
                            //m_UnlabeledData.getTuple(confidenceScoresSorted[j].getIndice()).setWeight(0);
                            m_UnlabeledData.setTuple(null, confidenceScoresSorted[j].getIndice());
                        }

                        m_PredConfidence.calculateConfidenceScores(m_Model, m_UnlabeledData);
                        iterations++;
                        unlabeledAddeddTotal += unlabeledAdded;

                        writer.println(iterations + "," + bestThreshold + "," + unlabeledAdded + "," + unlabeledAddeddTotal + "," + calculateError(cr.getTestSet()).getModelError() + "," + originalError + "," + getOOBError(m_TrainingSet, origLabeledMax).getModelError() + "," + getOOBError(m_TrainingSet, m_TrainingSet.getNbRows()).getModelError() + "," + calculateError(m_TrainingSet).getModelError());
                        writer.flush();

                    }
                    else {
                        System.out.println("Threshold not found");
                    }
                }
                while (betterFound);
            } // END - EXHAUSTIVE SEARCH

            unlabeledAddeddTotal += unlabeledAdded;

            //add selected unlabeled examples to the training set
            for (DataTuple dataTuple : unlabeledForTrain) {
                m_TrainingSet.add(dataTuple);
            }

            System.out.println("Unlabeled added: " + unlabeledAdded);

            writer.println(iterations + "," + m_Threshold + "," + unlabeledAdded + "," + unlabeledAddeddTotal + "," + calculateError(cr.getTestSet()).getModelError() + "," + originalError + "," + getOOBError(m_TrainingSet, origLabeledMax).getModelError() + "," + getOOBError(m_TrainingSet, m_TrainingSet.getNbRows()).getModelError() + "," + calculateError(m_TrainingSet).getModelError());
            writer.flush();
        } // END - MAIN LOOP

        writer.close();

        System.out.println();
        System.out.println(
                "Total self-training iterations performed: " + iterations);
        System.out.println(
                "Unlabeled examples added: " + unlabeledAddeddTotal);
        System.out.println();
        ClusModelInfo origInfo = cr.addModelInfo(ClusModel.ORIGINAL);
        String additionalInfo;
        additionalInfo = "Semi-supervised Self-training\n\t Iterations performed = " + iterations + "\n\t Unlabeled examples added: " + unlabeledAddeddTotal + "\n\t Base model: ";

        ((ClusForest) m_Model).setModelInfo(additionalInfo);

        origInfo.setModel(m_Model);

        return m_Model;
    }


    /**
     * Computes prediction for the example and eliminates example from the
     * unlabeled set
     *
     * @param t
     *        Example which should be processed
     * @param index
     *        Index of the example in the unlabeled set
     */
    private void processUnlabeledExample(DataTuple t, int index) {

        ClusStatistic stat = m_Model.predictWeighted(t); //model.predictWeighted(tuple);

        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL) {
            ((WHTDStatistic) stat).setThreshold(HMCthreshold);
        }

        stat.computePrediction();
        stat.predictTuple(t);
        //eliminate tuple t from unlabeled set
        m_UnlabeledData.setTuple(null, index);

        //set weight to unlabeled example (if needed), weight is equal to prediction confidence
        if (m_UseWeights) {
            t.setWeight(t.getWeight() * m_PredConfidence.getConfidence(index));
        }
    }


    @Override
    public void initialize() throws ClusException, IOException {

        //initialize underlying algorithm
        m_Induce.initialize();
    }


    public ClusSchema getSchema() {
        return m_Induce.getSchema();
    }


    public ClusStatManager getStatManager() {
        return m_Induce.getStatManager();
    }


    /**
     * Returns the settings given in the settings file (.s).
     *
     * @return The settings object.
     */
    public Settings getSettings() {
        return getStatManager().getSettings();
    }


    public void getPreprocs(DataPreprocs pps) {
        getStatManager().getPreprocs(pps);
    }


    @Override
    public void initializeHeuristic() {

        //initialize heuristic of underlying algorithm
        m_Induce.initializeHeuristic();
    }
}

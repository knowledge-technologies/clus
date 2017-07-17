
package clus.ext.semisupervised;

import clus.algo.ClusInductionAlgorithm;
import clus.data.ClusSchema;
import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataPreprocs;
import clus.data.rows.DataTuple;
import clus.ext.ensembles.ClusForest;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.settings.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Self-training that operates without confidence score
 * Implemented on the basis of: Culp and Michailidis, An iterative algorithm for extending learners to a semi-supervised
 * setting, Journal of Computational and Graphical Statistics, 2008
 * 
 * @author jurical
 */
public class ClusSelfTrainingFTFInduce extends ClusSemiSupervisedInduce {

    //underlying algorithm for self-training
    ClusInductionAlgorithm m_Induce;
    double m_Threshold;
    int m_Iterations;


    public ClusSelfTrainingFTFInduce(ClusSchema schema, Settings sett, ClusInductionAlgorithm clss_induce)
            throws ClusException,
            IOException {
        super(schema, sett);

        m_Induce = clss_induce;

        //maybe initialize settings parameters
        initialize(getSchema(), getSettings());
    }


    public void initialize(ClusSchema schema, Settings sett) {
        m_Threshold = sett.getSSL().getConfidenceThreshold();
        m_Iterations = sett.getSSL().getIterations();
        m_PercentageLabeled = sett.getSSL().getPercentageLabeled() / 100.0;
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException {

        partitionData(cr);

        int iterations = 0;
        double deltaUnlabeled = m_Threshold + 1; // + 1 to allow to enter the loop initially 
        boolean first = true;
        int origLabeledMax = m_TrainingSet.getNbRows();

        ClusRun myClusRun = new ClusRun(cr);
        ClusAttributeWeights targetWeights = cr.getStatManager().getNormalizationWeights();

        PrintWriter writer = null;
        double originalError = 0;

        while (iterations <= m_Iterations && deltaUnlabeled > m_Threshold) {

            iterations++;

            System.out.println();
            System.out.println("SelfTrainingFTF iteration: " + iterations);
            System.out.println();

            m_Model = m_Induce.induceSingleUnpruned(myClusRun);

            //save default models, which is supervised model, i.e., trained only on labeled data
            if (first) {
                first = false;
                ClusModelInfo defInfo = cr.addModelInfo(ClusModel.DEFAULT);
                defInfo.setModel(m_Model);

                // - just for testing
                originalError = calculateError(cr.getTestSet()).getModelError();
                writer = new PrintWriter(cr.getStatManager().getSettings().getGeneric().getAppName() + "_SelfTrainingFTFErrors.csv", "UTF-8");
                writer.println("DeltaUnlabeled,errorSSL,errorSupervised,errorOOBLabeled,errorOOBTrainingSet,errorTrainingSet,UnlabeledModelError");
                //

                /*
                 * predict and add unlabeled examples to the training set
                 */
                for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
                    DataTuple t = m_UnlabeledData.getTuple(i);

                    ClusStatistic stat = m_Model.predictWeighted(t);
                    stat.computePrediction();
                    stat.predictTuple(t);

                    m_TrainingSet.add(t);
                }

            }
            else {

                deltaUnlabeled = 0; //reset distance               

                for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
                    DataTuple t = m_UnlabeledData.getTuple(i);
                    DataTuple temp = t.deepCloneTuple();

                    ClusStatistic stat = m_Model.predictWeighted(t);

                    stat.computePrediction();
                    stat.predictTuple(t);

                    deltaUnlabeled += stat.getSquaredDistance(temp, targetWeights);
                }

            }
            ClusRun tempRun = new ClusRun(myClusRun);
            tempRun.setTrainingSet(m_UnlabeledData);
            ClusModel tempModel = m_Induce.induceSingleUnpruned(tempRun);

            writer.println(deltaUnlabeled + "," + calculateError(cr.getTestSet()).getModelError() + "," + originalError + "," + getOOBError(m_TrainingSet, origLabeledMax).getModelError() + "," + getOOBError(m_TrainingSet, m_TrainingSet.getNbRows()).getModelError() + "," + calculateError(m_TrainingSet).getModelError() + "," + calculateError(tempModel, m_TrainingSet, origLabeledMax).getModelError());

        }

        writer.close();

        ClusModelInfo origInfo = cr.addModelInfo(ClusModel.ORIGINAL);
        String additionalInfo;
        additionalInfo = "Semi-supervised Self-training FTF\n\t Iterations performed = " + iterations + "\n\t Base model: ";
        ((ClusForest) m_Model).setModelInfo(additionalInfo);

        origInfo.setModel(m_Model);
        return m_Model;
    }


    @Override
    public void initialize() throws ClusException, IOException {

        //initialize underlying algorithm
        m_Induce.initialize();
    }


    @Override
    public void initializeHeuristic() {

        //initialize heuristic of underlying algorithm
        m_Induce.initializeHeuristic();
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
}

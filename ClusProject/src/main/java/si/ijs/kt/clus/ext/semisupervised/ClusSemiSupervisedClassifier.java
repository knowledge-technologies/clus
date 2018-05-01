
package si.ijs.kt.clus.ext.semisupervised;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsSSL;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


public class ClusSemiSupervisedClassifier extends ClusInductionAlgorithmType {

    protected ClusInductionAlgorithmType m_clss;


    public ClusSemiSupervisedClassifier(Clus clus, ClusInductionAlgorithmType clss) {
        super(clus);

        // store classifier which will be used to build self-training
        m_clss = clss;
    }


    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings settx, CMDLineArgs cargs) throws ClusException, IOException {

        SettingsSSL sett = settx.getSSL();

        switch (sett.getSemiSupervisedMethod()) {
            case SelfTraining:
                return new ClusSelfTrainingInduce(schema, settx, m_clss.createInduce(schema, settx, cargs));

            case SelfTrainingFTF:
                return new ClusSelfTrainingFTFInduce(schema, settx, m_clss.createInduce(schema, settx, cargs));

            case PCT:
                return new ClusSemiSupervisedPCTs(m_clss.createInduce(schema, settx, cargs));
        }
        // by default return self training
        return new ClusSelfTrainingInduce(schema, settx, m_clss.createInduce(schema, settx, cargs));
    }


    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException, InterruptedException {
        m_clss.pruneAll(cr);
    }


    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException, InterruptedException {
        cr.addModelInfo(ClusModel.DEFAULT);
        ClusModelInfo def_info = cr.getModelInfo(ClusModel.DEFAULT);
        def_info.setModel(ClusDecisionTree.induceDefault(cr));
        m_clss.postProcess(cr);
    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * Call induceAll of the underlying algorithm (this is needed for the self-training algorithm, for general case this
     * should be re-implemented
     * (non-Javadoc)
     * @see clus.algo.ClusInductionAlgorithmType#induceAll(clus.main.ClusRun)
     */
    // public void induceAll(ClusRun cr) throws ClusException, IOException
    // {
    // m_clss.induceAll(cr);
    // }
    @Override
    public void printInfo() {
        Settings sett = getSettings();
        System.out.println("SSL Method: " + sett.getSSL().getSemiSupervisedMethod().toString());
        System.out.print("Base method: ");
        m_clss.printInfo();
    }
    // public ClusInductionAlgorithm getInduce() {
    // //we should probably test if self-training is induced, otherwise we don't return the underlying method
    // return m_clss.getClus().getInduce();
    // }
}

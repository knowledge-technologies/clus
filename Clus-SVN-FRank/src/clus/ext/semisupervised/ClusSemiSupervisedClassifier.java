package clus.ext.semisupervised;

import java.io.IOException;
import clus.jeans.util.cmdline.CMDLineArgs;
import clus.Clus;
import clus.algo.ClusInductionAlgorithm;
import clus.algo.ClusInductionAlgorithmType;
import clus.data.type.ClusSchema;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;

public class ClusSemiSupervisedClassifier extends ClusInductionAlgorithmType {

    protected ClusInductionAlgorithmType m_clss;

    public ClusSemiSupervisedClassifier(Clus clus, ClusInductionAlgorithmType clss) {
        super(clus);

        //store classifier which will be used to build self-training
        m_clss = clss;
    }

    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema,
            Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        // TODO Auto-generated method stub

        switch (sett.getSemiSupervisedMethod()) {
            case Settings.SSL_METHOD_SELF_TRAINING:
                return new ClusSelfTrainingInduce(schema, sett, m_clss.createInduce(schema, sett, cargs));
            case Settings.SSL_METHOD_SELF_TRAINING_FTF:
                return new ClusSelfTrainingFTFInduce(schema, sett, m_clss.createInduce(schema, sett, cargs));
            case Settings.SSL_METHOD_PCT:
                return new ClusSemiSupervisdPCTs(m_clss.createInduce(schema, sett, cargs));
        }
        // by default return self training
        return new ClusSelfTrainingInduce(schema, sett, m_clss.createInduce(schema, sett, cargs));
    }

    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {
        m_clss.pruneAll(cr);
    }
    
    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException {
        cr.addModelInfo(ClusModel.DEFAULT);
        m_clss.postProcess(cr);
    }

    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr)
            throws ClusException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * Call induceAll of the underlying algorithm (this is needed for the self-training algorithm, for general case this should be re-implemented
     * (non-Javadoc)
     * @see clus.algo.ClusInductionAlgorithmType#induceAll(clus.main.ClusRun)
     */
    //		public void induceAll(ClusRun cr) throws ClusException, IOException 
    //		{
    //			m_clss.induceAll(cr);	
    //		}
    public void printInfo() {
        Settings sett = getSettings();
        System.out.println("SSL Metohd: " + sett.SSL_METHOD[sett.getSemiSupervisedMethod()]);
        System.out.print("Base method: ");
        m_clss.printInfo();
    }
//	public ClusInductionAlgorithm getInduce() {
//		//we should probably test if self-training is induced, otherwise we don't return the underlying method
//		return m_clss.getClus().getInduce();
//	}
}

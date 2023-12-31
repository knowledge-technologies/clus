
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


public class Relief extends ClusInductionAlgorithmType {

    double[] m_Weights;


    public Relief(Clus clus) {
        super(clus);
        
    }


    // public void updateWeights(){
    // m_Weights = new double[2];
    // m_Weights[0] = 2.1;
    // m_Weights[1] = 2.21;
    // }

    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        return new ReliefInduce(schema, sett);
    }


    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {
        

    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        
        return null;
    }


    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException {
        
        
    }

}

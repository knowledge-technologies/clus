
package clus.ext.featureRanking.relief;

import java.io.IOException;

import clus.Clus;
import clus.algo.ClusInductionAlgorithm;
import clus.algo.ClusInductionAlgorithmType;
import clus.data.type.ClusSchema;
import clus.jeans.util.cmdline.CMDLineArgs;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;


public class Relief extends ClusInductionAlgorithmType {

    double[] m_Weights;


    public Relief(Clus clus) {
        super(clus);
        // TODO Auto-generated constructor stub
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
        // TODO Auto-generated method stub

    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

}


package clus.ext.featureRanking.relief;

import java.io.IOException;

import clus.algo.ClusInductionAlgorithm;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;


public class ReliefInduce extends ClusInductionAlgorithm {

    protected ClusNode m_Root;
    protected ClusReliefFeatureRanking m_FeatureRanking;


    public ReliefInduce(ClusInductionAlgorithm other) {
        super(other);
        // TODO Auto-generated constructor stub
    }


    public ReliefInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
        super(schema, sett);
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException {
    	int[] nbNeighbours = cr.getStatManager().getSettings().getReliefNbNeighboursValue();
    	int[] nbIterations = cr.getStatManager().getSettings().getReliefNbIterationsValue();
    	boolean shouldWeight = cr.getStatManager().getSettings().getReliefWeightNeighbours();
    	double sigma = cr.getStatManager().getSettings().getReliefWeightingSigma();
    	
        ReliefModel reliefModel = new ReliefModel(nbNeighbours, nbIterations, shouldWeight, sigma, (RowData) cr.getTrainingSet());

        m_FeatureRanking = new ClusReliefFeatureRanking(reliefModel.getNbNeighbours(), reliefModel.getNbIterations(), reliefModel.getWeightNeighbours(), reliefModel.getSigma());
        m_FeatureRanking.initializeAttributes(cr.getStatManager().getSchema().getDescriptiveAttributes(), reliefModel.getNbRankings());
        m_FeatureRanking.calculateReliefImportance(reliefModel.getData());

        m_FeatureRanking.createFimp(cr);
        
        return reliefModel;
    }


    public ClusNode induceSingleUnpruned(RowData data) throws ClusException, IOException {
        m_Root = null;

        // while (true) {
        //
        // // Init root node
        // m_Root = new ClusNode();
        // m_Root.initClusteringStat(m_StatManager, data);
        // m_Root.initTargetStat(m_StatManager, data);
        // m_Root.getClusteringStat().showRootInfo();
        //// initSelectorAndSplit(m_Root.getClusteringStat());
        //// setInitialData(m_Root.getClusteringStat(),data);
        // // Induce the tree
        // data.addIndices();
        //
        // induce(m_Root, data);
        //
        // // rankFeatures(m_Root, data);
        // // Refinement finished
        // if (Settings.EXACT_TIME == false) break;
        // }

        return m_Root;
    }

}

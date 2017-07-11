
package clus.ext.featureRanking.relief;

import java.io.IOException;

import clus.algo.ClusInductionAlgorithm;
import clus.algo.tdidt.ClusNode;
import clus.data.ClusSchema;
import clus.data.rows.RowData;
import clus.main.ClusRun;
import clus.main.settings.Settings;
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
    	int[] nbNeighbours = cr.getStatManager().getSettings().getRelief().getReliefNbNeighboursValue();
    	int[] nbIterations = cr.getStatManager().getSettings().getRelief().getReliefNbIterationsValue(cr.getTrainingSet().getNbRows());
    	boolean shouldWeight = cr.getStatManager().getSettings().getRelief().getReliefWeightNeighbours();
    	double sigma = cr.getStatManager().getSettings().getRelief().getReliefWeightingSigma();
    	int randomSeed = cr.getStatManager().getSettings().getGeneral().getRandomSeed();
    	
        ReliefModel reliefModel = new ReliefModel(nbNeighbours, nbIterations, shouldWeight, sigma, (RowData) cr.getTrainingSet());

        m_FeatureRanking = new ClusReliefFeatureRanking(reliefModel.getData(), reliefModel.getNbNeighbours(), reliefModel.getNbIterations(), reliefModel.getWeightNeighbours(), reliefModel.getSigma(), randomSeed, getSettings());
        m_FeatureRanking.initializeAttributes(cr.getStatManager().getSchema().getDescriptiveAttributes(), m_FeatureRanking.getNbFeatureRankings());
        m_FeatureRanking.calculateReliefImportance(reliefModel.getData());

        m_FeatureRanking.createFimp(cr, 0);
        
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

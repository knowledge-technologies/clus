
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsRelief.MissingTargetHandling;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.exception.ClusException;


public class ReliefInduce extends ClusInductionAlgorithm {

    protected ClusNode m_Root;
    protected ClusReliefFeatureRanking m_FeatureRanking;


    public ReliefInduce(ClusInductionAlgorithm other) {
        super(other);
        
    }


    public ReliefInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
        super(schema, sett);
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException, ExecutionException {
    	Settings settings = cr.getStatManager().getSettings();
        int[] nbNeighbours = settings.getRelief().getReliefNbNeighboursValue();
        int[] nbIterations = settings.getRelief().getReliefNbIterationsValue(cr.getTrainingSet().getNbRows());
        boolean shouldWeight = settings.getRelief().getReliefWeightNeighbours();
        double sigma = settings.getRelief().getReliefWeightingSigma();
        int randomSeed = settings.getGeneral().getRandomSeed();
        MissingTargetHandling missingTargetHandling = settings.getRelief().getMissingTargetHandling();
        double[] sslWinterval = amountOfSupervisionInterval(settings);
        
        ReliefModel reliefModel = new ReliefModel(nbNeighbours, nbIterations, shouldWeight, sigma, (RowData) cr.getTrainingSet());

        m_FeatureRanking = new ClusReliefFeatureRanking(reliefModel.getData(), reliefModel.getNbNeighbours(), reliefModel.getNbIterations(), reliefModel.getWeightNeighbours(), reliefModel.getSigma(), randomSeed, getSettings(),
        		missingTargetHandling, sslWinterval, cr);
        m_FeatureRanking.initializeAttributes(cr.getStatManager().getSchema().getDescriptiveAttributes(), m_FeatureRanking.getNbFeatureRankings());
        m_FeatureRanking.computeReliefImportance(reliefModel.getData());

        // String fimpNameAppendix = getSettings().getMLC().getSectionMultiLabel().isEnabled() ?
        // m_FeatureRanking.getMultilabelDistance() : "";
        m_FeatureRanking.createFimp(cr);

        return reliefModel;
    }

    public ClusNode induceSingleUnpruned(RowData data) throws ClusException, IOException {
        m_Root = null;
        return m_Root;
    }
    
    private static double[] amountOfSupervisionInterval(Settings settings) {
    	double[] amountsOfSupervision = settings.getSSL().getSSLPossibleWeights();
    	double min = 1.0, max = 0.0; // [0, 1] is the biggest interval
    	for (double w : amountsOfSupervision) {
    		if (w < min) min = w;
    		if (w > max) max = w;
    	}
    	return new double[] {min, max};
    	
    }

}

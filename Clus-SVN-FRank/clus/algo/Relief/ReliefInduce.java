package clus.algo.Relief;

import java.io.IOException;

import clus.algo.ClusInductionAlgorithm;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;

public class ReliefInduce extends ClusInductionAlgorithm{
	protected ClusNode m_Root;

	public ReliefInduce(ClusInductionAlgorithm other) {
		super(other);
		// TODO Auto-generated constructor stub
	}
	
	public ReliefInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	@Override
	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		return induceSingleUnpruned((RowData)cr.getTrainingSet());
	}
	
	public ClusNode induceSingleUnpruned(RowData data) throws ClusException, IOException {
		m_Root = null;

		ReliefModel reliefModel = new ReliefModel();
		
		
		
		
//		while (true) {
//
//			// Init root node
//			m_Root = new ClusNode();
//			m_Root.initClusteringStat(m_StatManager, data);
//			m_Root.initTargetStat(m_StatManager, data);
//			m_Root.getClusteringStat().showRootInfo();
////			initSelectorAndSplit(m_Root.getClusteringStat());
////			setInitialData(m_Root.getClusteringStat(),data);
//			// Induce the tree
//			data.addIndices();
//
//			induce(m_Root, data);
//
//			// rankFeatures(m_Root, data);
//			// Refinement finished
//			if (Settings.EXACT_TIME == false) break;
//		}


		return m_Root;
	}

}


package si.ijs.kt.clus.ext.ensemble.container;

import java.util.HashMap;

import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleRanking;
import si.ijs.kt.clus.model.ClusModel;
//import si.ijs.kt.clus.selection.OOBSelection;


public class OneBagResults {

    private ClusModel m_Model;
    private HashMap<EnsembleRanking, HashMap<String, double[][]>> m_Fimportances;
    private ClusRun m_SingleRun;
    private long m_InductionTime;
    private int m_ModelIndex;

    public OneBagResults(ClusModel model, HashMap<EnsembleRanking, HashMap<String, double[][]>> fimportances, ClusRun crSingle, long inductionTime, int modelIndex) {
        m_Model = model;
        m_Fimportances = fimportances;
        m_SingleRun = crSingle;
        m_InductionTime = inductionTime;
        m_ModelIndex = modelIndex;        
    }

    public ClusModel getModel() {
        return m_Model;
    }

    public HashMap<EnsembleRanking, HashMap<String, double[][]>> getFimportances() {
        return m_Fimportances;
    }

    public ClusRun getSingleRun() {
        return m_SingleRun;
    }
    
    public long getInductionTime() {
        return m_InductionTime;
    }
    
    public int getModelIndex() {
    	return m_ModelIndex;
    }

}


package clus.ext.ensembles.containers;

import java.util.HashMap;

import clus.main.ClusRun;
import clus.model.ClusModel;
import clus.selection.OOBSelection;


public class OneBagResults {

    private ClusModel m_Model;
    private HashMap<String, double[][]> m_Fimportances;
    private ClusRun m_SingleRun;
    private OOBSelection m_OOBTotal;
    private long m_InductionTime;

    public OneBagResults(ClusModel model, HashMap<String, double[][]> fimportances, ClusRun crSingle, OOBSelection oob_total, long inductionTime) {
        m_Model = model;
        m_Fimportances = fimportances;
        m_SingleRun = crSingle;
        m_OOBTotal = oob_total;
        m_InductionTime = inductionTime;
    }


    public ClusModel getModel() {
        return m_Model;
    }


    public HashMap<String, double[][]> getFimportances() {
        return m_Fimportances;
    }


    public ClusRun getSingleRun() {
        return m_SingleRun;
    }


    public OOBSelection getOOBTotal() {
        return m_OOBTotal;
    }
    
    public long getInductionTime() {
        return m_InductionTime;
    }

}

package clus.ext.ensembles.pairs;

import java.util.HashMap;

import clus.model.ClusModel;

public class ModelFimportancesPair {
	private ClusModel m_Model;
	private HashMap<String, double[]> m_Fimportances;
	
	public ModelFimportancesPair(ClusModel model, HashMap<String, double[]> fimportances){
		m_Model = model;
		m_Fimportances = fimportances;		
	}
	
	public ClusModel getModel(){
		return m_Model;
	}
	
	public HashMap<String, double[]> getFimportances(){
		return m_Fimportances;
	}

}

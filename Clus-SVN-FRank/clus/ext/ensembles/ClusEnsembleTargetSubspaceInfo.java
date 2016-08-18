
package clus.ext.ensembles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import weka.core.Utils;

/**
 * @author martinb
 *
 */
public class ClusEnsembleTargetSubspaceInfo implements Serializable {

	private static final long serialVersionUID = -8192393874827673204L;
	ArrayList<int[]> m_AllSubspaces;
	double[] m_Coverage;
	ClusSchema m_Schema;
	public ClusEnsembleTargetSubspaceInfo(ClusSchema schema, ArrayList<int[]> info)
	{
		m_AllSubspaces = info;
		m_Schema = schema;
		calculateCoverage();
	}
	
	public int[] getModelSubspace(int i)
	{
		return m_AllSubspaces.get(i);
	}
	
	public boolean[] getModelSubspaceBoolean(int subspaceIdx) {
		int[] e = getModelSubspace(subspaceIdx);
		boolean[] b = new boolean[e.length];
		for (int j = 0; j < e.length; j++) b[j] = e[j] != 0;
		
		return b;
	}
	
	public int[] getOnlyTargets(int[] enabled) {
		ClusAttrType[] targets = m_Schema.getTargetAttributes();
		int[] result = new int[targets.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = enabled[targets[i].getIndex()];
		}
		
		return result;
	}
	
	void calculateCoverage()
	{
		m_Coverage = new double[m_Schema.getNbTargetAttributes()];
		Arrays.fill(m_Coverage, 0.0);
		
		for (int i = 0; i < m_AllSubspaces.size(); i++) {
			int[] vals = getOnlyTargets(getModelSubspace(i));
			
			for (int t = 0; t < vals.length; t++) {
				if (vals[t] == 1) {
					m_Coverage[t]++;
				}
			}
		}
	}
	
	public double[] getCoverage() {
		return m_Coverage;
	}
	
	public double[] getCoverageNormalized() {
		double[] d = new double[m_Coverage.length];
		
		for (int t = 0; t < m_Coverage.length; t++) d[t] = Utils.roundDouble(m_Coverage[t] / m_AllSubspaces.size(), 3);
		
		return d;
	}
	
	public String getInfo(int i) {
		
		int[] vals = getOnlyTargets(getModelSubspace(i));
		String sb="";
		if (vals.length > 15) {
			sb = ClusEnsembleTargetSubspaceInfo.getEnabledCount(vals) + " of " + vals.length;
		} 
		else {
			sb = "";
			ClusAttrType[] targets = m_Schema.getTargetAttributes();
			
			for (int t = 0; t < vals.length; t++) {
				if (vals[t] == 1) {
					//sb += (m_AllTargetIDs[t]+1) + "\t";
					sb += (targets[t].getIndex()+1) + "\t";
				}
				else {
					sb += "-\t";
				}
			}
		}
		return sb;
	}
	
	
	
	public String getCoverageNormalizedInfo() {
		return String.format("Target coverage normalized: %s", Arrays.toString(getCoverageNormalized()).replace(",", " "));
	}
	
	
	public String getCoverageInfo() {
		return String.format("Target coverage: %s", Arrays.toString(m_Coverage).replace(",", " "));
	}

	public static int getEnabledCount(int[] enabled) {
		int cnt = 0;
		for (int i = 0; i < enabled.length; i++) {
			if (enabled[i] == 1) { 
				cnt++;
			}
		}
		return cnt;
	}}

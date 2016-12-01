package clus.error;

import java.io.PrintWriter;
import java.text.NumberFormat;

import clus.data.type.NominalAttrType;
import clus.util.ClusFormat;

public class MLweightedAUPRC extends MLROCAndPRCurve {
	protected final int m_Measure = weightedAUPRC;

	public MLweightedAUPRC(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
	}
	
	public double getModelError(){
		return getModelError(m_Measure);
	}
	public String getName() {
		return "weightedAUPRC";
	}
	public void showModelError(PrintWriter out, int detail){
		NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
		computeAll();
		out.println(getName() + " " + fr1.format(m_WAvgAUPRC));
	}
	
	public ClusError getErrorClone(ClusErrorList par) {
		return new MLweightedAUPRC(par, m_Attrs);
	}
}

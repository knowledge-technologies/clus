package clus.error;

import java.io.PrintWriter;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 * 
 * Subset accuracy is used in multi-label classification scenario.
 */
public class SubsetAccuracy extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_NbCorrect;	// nubmer of samples, for which prediction(sample) = target(sample), where prediction (target) of a sample is the predicted (true) label set. 
	protected int m_NbKnown;	// number of the examples seen 
	
	public SubsetAccuracy(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_NbCorrect = 0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return false;
	}

	public void reset() {
		m_NbCorrect = 0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		SubsetAccuracy sa = (SubsetAccuracy)other;
		m_NbCorrect += sa.m_NbCorrect;
		m_NbKnown += sa.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getSubsetAccuracy(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return ((double) m_NbCorrect)/ m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "SubsetAccuracy";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new SubsetAccuracy(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred();
		NominalAttrType attr;
		boolean atLeastOneKnown = false;
		boolean correctPrediction = true;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if (attr.getNominal(tuple) != predicted[i]) {
					correctPrediction = false;
					break;
				}
			}
		}
		if(atLeastOneKnown){
			if(correctPrediction){
				m_NbCorrect++;
			}
			m_NbKnown++;
		}
	}

	public void addExample(DataTuple tuple, DataTuple pred) {
		NominalAttrType attr;
		boolean atLeastOneKnown = false;
		boolean correctPrediction = true;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if (attr.getNominal(tuple) != attr.getNominal(pred)) {
					correctPrediction = false;
					break;
				}
			}
		}
		if(atLeastOneKnown){
			if(correctPrediction){
				m_NbCorrect++;
			}
			m_NbKnown++;
		}
	}
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}

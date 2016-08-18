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
 * Hamming is used in multi-label classification scenario.
 */
public class MLAccuracy extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_JaccardSum;	// sum of |prediction(sample_i) INTERSECTION target(sample_i)| / ||prediction(sample_i) UNION target(sample_i)||,
									// where prediction (target) of a sample_i is the predicted (true) label set. 
	protected int m_NbKnown;		// number of the examples seen with at least one known target valueO
	
	public MLAccuracy(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_JaccardSum = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return false;
	}

	public void reset() {
		m_JaccardSum = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		MLAccuracy mlca = (MLAccuracy)other;
		m_JaccardSum += mlca.m_JaccardSum;
		m_NbKnown += mlca.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getMLAccuracy(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return m_JaccardSum / m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "MLAccuracy";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MLAccuracy(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred(); // predicted[i] == 0 IFF label_i is predicted to be relevant for the example
		NominalAttrType attr;
		int intersection = 0, union = 0;
		boolean atLeastOneKnown = false;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if(attr.getNominal(tuple) == 0 && predicted[i] == 0){ // both relevant
					union++;
					intersection++;
				} else if(!(attr.getNominal(tuple) == 1 && predicted[i] == 1)){ // precisely one relevant
					union++;
				}
			}
		}
		if(atLeastOneKnown){
			m_JaccardSum += union != 0 ? ((double) intersection) / union : 1.0; // take care of degenerated case
			m_NbKnown++;			
		}		
	}

	public void addExample(DataTuple tuple, DataTuple pred) {
		NominalAttrType attr;
		int intersection = 0, union = 0;
		boolean atLeastOneKnown = false;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if(attr.getNominal(tuple) == 0 && attr.getNominal(pred) == 0){ // both relevant
					union++;
					intersection++;
				} else if(!(attr.getNominal(tuple) == 1 && attr.getNominal(pred) == 1)){ // precisely one relevant
					union++;
				}
			}
		}
		if(atLeastOneKnown){
			m_JaccardSum += union != 0 ? ((double) intersection) / union : 1.0; // take care of degenerated case
			m_NbKnown++;			
		}	
	}
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}

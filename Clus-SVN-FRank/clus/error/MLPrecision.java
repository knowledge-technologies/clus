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
 * MLPrecision is used in multi-label classification scenario.
 */
public class MLPrecision extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_PrecisionSum;	// sum of |prediction(sample_i) INTERSECTION target(sample_i)| / |prediction(sample_i)|,
										// where prediction (target) of a sample_i is the predicted (true) label set.
	
	protected int m_NbKnown;			// number of the examples seen with at least one known target value
	
	public MLPrecision(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_PrecisionSum = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return false;
	}

	public void reset() {
		m_PrecisionSum = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		MLPrecision mlcr = (MLPrecision)other;
		m_PrecisionSum += mlcr.m_PrecisionSum;
		m_NbKnown += mlcr.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getMLPrecision(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return m_PrecisionSum / m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "MLPrecision";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MLPrecision(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred(); // predicted[i] == 0 IFF label_i is predicted to be relevant for the example
		NominalAttrType attr;
		int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
		boolean atLeastOneKnown = false;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if(predicted[i] == 0){
					nbRelevantPredicted++;
					if(attr.getNominal(tuple) == 0){
						intersection++;
					}
				}
				if(attr.getNominal(tuple) == 0){
					nbRelevant++;
				}
			}
		}
		if(atLeastOneKnown){
			m_PrecisionSum += nbRelevantPredicted != 0 ? ((double) intersection) / nbRelevantPredicted : (nbRelevant == 0 ? 1.0 : 0.0); // take care of degenerated cases
			m_NbKnown++;			
		}		
	}

	public void addExample(DataTuple tuple, DataTuple pred) {
		NominalAttrType attr;
		int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
		boolean atLeastOneKnown = false;
		for (int i = 0; i < m_Dim; i++) {
			attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				atLeastOneKnown = true;
				if(attr.getNominal(tuple) == 0){
					nbRelevant++;
					if(attr.getNominal(pred) == 0){
						intersection++;
					}
				}
				if(attr.getNominal(pred) == 0){
					nbRelevantPredicted++;
				}
			}
		}
		if(atLeastOneKnown){
			m_PrecisionSum += nbRelevant != 0 ? ((double) intersection) / nbRelevant : (nbRelevantPredicted == 0 ? 1.0 : 0.0); // take care of degenerated cases
			m_NbKnown++;			
		}	
	}
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}


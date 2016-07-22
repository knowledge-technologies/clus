package clus.error;

import java.io.PrintWriter;
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClusStatistic;

/**
 * Hamming loss: popravu natanko tiste metode / polja, kjer pise POPRAVU
 * @author matejp
 *
 */
public class HammingLoss extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	//POPRAVU
	protected int m_NbWrong;// sum of |prediction(sample_i) SYMMETRIC DIFFERENCE target(sample_i)|, where prediction (target) of a sample_i is the predicted (true) label set. 
	protected int m_NbKnown;// number of the examples seen 
	
	//POPRAVU
	public HammingLoss(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_NbWrong = 0;
		m_NbKnown = 0;
	}
	//POPRAVU
	public boolean shouldBeLow() {
		return true;
	}
	//POPRAVU
	public void reset() {
		m_NbWrong = 0;
		m_NbKnown = 0;
	}
	//POPRAVU
	public void add(ClusError other) {
		HammingLoss ham = (HammingLoss)other;
		m_NbWrong += ham.m_NbWrong;
		m_NbKnown += ham.m_NbKnown;
	}
	//NAMESKO PUSTU
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
	//POPRAVU
	public double getHammingLoss(int i) {
		return getModelErrorComponent(i);
	}
	//POPRAVU
	public double getModelError() {
		return ((double) m_NbWrong)/ m_Dim / m_NbKnown;
	}
	//POPRAVU
	public String getName() {
		return "HammingLoss";
	}
	//Accuracy --> HammingLoss: Dragi?:)
	public ClusError getErrorClone(ClusErrorList par) {
		return new HammingLoss(par, m_Attrs);
	}
	//POPRAVU
	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred();
		for (int i = 0; i < m_Dim; i++) {
			NominalAttrType attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				if (attr.getNominal(tuple) != predicted[i]) {
					m_NbWrong++;
				}
			}
		}
		m_NbKnown++;
	}
	//POPRAVU
	public void addExample(DataTuple tuple, DataTuple pred) {
		for (int i = 0; i < m_Dim; i++) {
			NominalAttrType attr = getAttr(i);
			if (!attr.isMissing(tuple)) {
				if (attr.getNominal(tuple) != attr.getNominal(pred)) {
					m_NbWrong++;
				}
			}
		}
		m_NbKnown++;
	}
	//NAMENSKO PUSTU
	public void addInvalid(DataTuple tuple) {
	}

}

package clus.error;

import java.io.PrintWriter;
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 *
 */
public class RankingLoss extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_NonnormalisedLoss;// sum over samples sample_i of the terms t_i = |D_i| / (|Y_i| |L \ Y_i|), where L is the set of labels, Y_i is the predicted set of labels,
										 // and D_i is the set of pairs (l1, l2), such that l1 is falsely positive and l2 is falsely negative.
										 // If Y_i is either empty set or it equals L, then t_i = 0
	protected int m_NbKnown;// number of the examples seen 
	
	public RankingLoss(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_NonnormalisedLoss = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return true;
	}

	public void reset() {
		m_NonnormalisedLoss = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		RankingLoss rl = (RankingLoss)other;
		m_NonnormalisedLoss += rl.m_NonnormalisedLoss;
		m_NbKnown += rl.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
	// A MA TO SPLOH SMISU?
	public double getRankingLoss(int i) {
		return getModelErrorComponent(i);
	}

	public double getModelError() {
		return m_NonnormalisedLoss/ m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "RankingLoss";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new RankingLoss(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred();
		int setD = 0, setY = 0;
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
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}

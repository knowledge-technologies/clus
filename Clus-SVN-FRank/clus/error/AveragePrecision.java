package clus.error;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 *
 * Average precision is used in multi-label classification scenario.
 */
public class AveragePrecision extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_NonnormalisedPrec;	// sum over samples sample_i of the terms t_i = 1 / |Y_i| * sum_(lambda_j in Y_i) u_ij, where u_ij = |L_ij| / rank(lambda_j),
											// where Y_i is the true set of labels that are relevant for sample_i and L_ij is the set of labels that have lower or equal
											// rank than lambda_ij.

	protected int m_NbKnown;				// number of the examples seen with at least one known target value
	
	public AveragePrecision(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_NonnormalisedPrec = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return false;
	}

	public void reset() {
		m_NonnormalisedPrec = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		AveragePrecision rl = (AveragePrecision)other;
		m_NonnormalisedPrec += rl.m_NonnormalisedPrec;
		m_NbKnown += rl.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getAveragePrecision(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return m_NonnormalisedPrec/ m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "AveragePrecision";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new AveragePrecision(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		final double[] scores = ((ClassificationStat) pred).calcScores();
		ArrayList<Integer> indicesOfKnownValues = new ArrayList<Integer>();
		Boolean[] isRelevant = new Boolean[m_Dim];
		NominalAttrType attr;
		// find (indices of) known values and relevant labels
		for(int i = 0; i< m_Dim; i++){
			attr = getAttr(i);
			if(!attr.isMissing(tuple)){
				indicesOfKnownValues.add(i);
				if(attr.getNominal(tuple) == 0){
					isRelevant[i] = true;
				}
			}
		}
		// sort with respect to the scores
		Collections.sort(indicesOfKnownValues, new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				return -Double.compare(scores[o1], scores[o2]);
			}
		});
		int nbOfRelevant = 0;
		double u = 0.0;
		if(indicesOfKnownValues.size() > 0){
			for(int i = 0; i < indicesOfKnownValues.size(); i++){
				if(isRelevant[indicesOfKnownValues.get(i)]){
					nbOfRelevant++;
					u += nbOfRelevant / (i + 1);					
				}
			}
			m_NonnormalisedPrec += u / nbOfRelevant;  
			m_NbKnown++;
		}

	}

	public void addExample(DataTuple tuple, DataTuple pred){
		try {
			throw new Exception("Yet to implement AveragePrecision.addExample(DataTuple tuple, DataTuple pred)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}


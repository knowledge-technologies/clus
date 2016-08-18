package clus.error;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 *
 * Coverage is used in multi-label classification scenario.
 */
public class Coverage extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_RankSum;			// sum over samples sample_i of the terms rank(label_i), where label_i is that element from the set of relevant labels for sample_i,
										// which has the lowest score. The rank is the number of labels with higher or equal score than label_i.
	
	protected int m_NbKnown;			// number of the examples seen
	protected int m_NbRelevantLabels; 	// sum over samples sample_i of the terms |{j | label_j is relevant for sample_i}|
	
	public Coverage(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_RankSum = 0;
		m_NbKnown = 0;
		m_NbRelevantLabels = 0;
	}

	public boolean shouldBeLow() {
		return true;
	}

	public void reset() {
		m_RankSum = 0;
		m_NbKnown = 0;
		m_NbRelevantLabels = 0;
	}

	public void add(ClusError other) {
		Coverage cv = (Coverage)other;
		m_RankSum += cv.m_RankSum;
		m_NbKnown += cv.m_NbKnown;
		m_NbRelevantLabels += cv.m_NbRelevantLabels;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getCoverage(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return ((double) m_RankSum)/ m_NbKnown;
	}
	
	public double getLabelCardinality(){
		return ((double) m_NbRelevantLabels) / m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()) + "(label cardinality: " + ClusFormat.FOUR_AFTER_DOT.format(getLabelCardinality()) + ")");
	}

	public String getName() {
		return "Coverage";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new Coverage(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		double[] scores = ((ClassificationStat) pred).calcScores();
		double minScore = Double.MAX_VALUE;
		int minScoreLabel = -1;
		int relevantLabels = 0;
		NominalAttrType attr;
		for(int i = 0; i< m_Dim; i++){
			attr = getAttr(i);
			if(!attr.isMissing(tuple)){
				if(attr.getNominal(tuple) == 0){ // label is relevant
					relevantLabels++;
					if(minScore > scores[i]){
						minScore = scores[i];
						minScoreLabel = i;
					}
				}
			}
		}
		if(minScoreLabel >= 0){	// at least one relevant
			int rank = 0;	// should be 1, but we will add this 1 when i == minScoreLabel
			for(int i = 0; i < m_Dim; i++){
				attr = getAttr(i);
				if(!attr.isMissing(tuple) && scores[i] >= scores[minScoreLabel]){ // ignore missing values
					rank++;
				}
			}
			m_RankSum += rank;
			m_NbKnown++;			
			m_NbRelevantLabels += relevantLabels;
		}

	}

	public void addExample(DataTuple tuple, DataTuple pred){
		try {
			throw new Exception("Yet to implement Coverage.addExample(DataTuple tuple, DataTuple pred)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}
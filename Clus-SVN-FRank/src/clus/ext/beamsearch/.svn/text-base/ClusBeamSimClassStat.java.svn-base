package clus.ext.beamsearch;

import java.util.ArrayList;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.rows.SparseDataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;

public class ClusBeamSimClassStat extends ClassificationStat{
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	
	public double[][] m_SumPredictions;
	public double[][] m_SumSqPredictions;
	private ClusBeam m_Beam;
	
	public ClusBeamSimClassStat(NominalAttrType[] nomAtts, ClusBeam beam) {
		super(nomAtts);
		m_Beam = beam;
		m_SumPredictions = replicateEmpty(m_ClassCounts);
		m_SumSqPredictions = replicateEmpty(m_ClassCounts);
	}

	public ClusStatistic cloneStat() {
		return new ClusBeamSimClassStat(m_Attrs,m_Beam);
	}

	public void reset() {
		super.reset();
		for (int i = 0; i < m_NbTarget; i++) {
			for (int j = 0; j < m_SumPredictions[i].length; j++) {
				m_SumPredictions[i][j] = 0.0;
				m_SumSqPredictions[i][j] = 0.0;
			}
		}
	}

	public void copy(ClusStatistic other) {
		super.copy(other);
		ClusBeamSimClassStat or = (ClusBeamSimClassStat)other;
		double[] my;
		double[] your;
		for (int i = 0; i < m_NbTarget; i++) {
			my = m_SumPredictions[i];
			your = or.m_SumPredictions[i];
			System.arraycopy(your, 0, my, 0, my.length);
			my = m_SumSqPredictions[i];
			your = or.m_SumSqPredictions[i];
			System.arraycopy(your, 0, my, 0, my.length);
		}
	}

	public void addPrediction(ClusStatistic other, double weight) {
		super.addPrediction(other, weight);
		ClusBeamSimClassStat or = (ClusBeamSimClassStat)other;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] mysum = m_SumPredictions[i];
			double[] yoursum = or.m_SumPredictions[i];
			double[] mysumsq = m_SumSqPredictions[i];
			double[] yoursumsq = or.m_SumSqPredictions[i];
			for (int j = 0; j < mysum.length; j++) {
				mysum[j] += weight*yoursum[j];
				mysumsq[j] += weight*yoursumsq[j];
			}
		}
	}

	public void add(ClusStatistic other) {
		super.add(other);
		ClusBeamSimClassStat or = (ClusBeamSimClassStat)other;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] mysum = m_SumPredictions[i];
			double[] yoursum = or.m_SumPredictions[i];
			double[] mysumsq = m_SumSqPredictions[i];
			double[] yoursumsq = or.m_SumSqPredictions[i];
			for (int j = 0; j < mysum.length; j++) {
				mysum[j] += yoursum[j];
				mysumsq[j] += yoursumsq[j];
			}
		}
	}

	public void subtractFromThis(ClusStatistic other) {
		super.subtractFromThis(other);
		ClusBeamSimClassStat or = (ClusBeamSimClassStat)other;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] mysum = m_SumPredictions[i];
			double[] yoursum = or.m_SumPredictions[i];
			double[] mysumsq = m_SumPredictions[i];
			double[] yoursumsq = or.m_SumPredictions[i];
			for (int j = 0; j < mysum.length; j++) {
				mysum[j] -= yoursum[j];
				mysumsq[j] -= yoursumsq[j];
			}
		}
	}

	public void subtractFromOther(ClusStatistic other) {
		super.subtractFromOther(other);
		ClusBeamSimClassStat or = (ClusBeamSimClassStat)other;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] mysum = m_SumPredictions[i];
			double[] yoursum = or.m_SumPredictions[i];
			double[] mysumsq = m_SumPredictions[i];
			double[] yoursumsq = or.m_SumPredictions[i];
			for (int j = 0; j < mysum.length; j++) {
				mysum[j] = yoursum[j] - mysum[j];
				mysumsq[j] = yoursumsq[j] - mysumsq[j];
			}
		}
	}

	public void updateWeighted(DataTuple tuple, int idx) {
		updateWeighted(tuple, tuple.getWeight());
	}
	
	public void updateWeighted(SparseDataTuple tuple, int idx) {
		updateWeighted(tuple, tuple.getWeight());
	}

	public void updateWeighted(DataTuple tuple, double weight) {
		super.updateWeighted(tuple, weight);
		ArrayList<ClusBeamModel> models = m_Beam.toArray();
		double[][] vals = replicateEmpty(m_SumPredictions);
		double[][] valssq = replicateEmpty(m_SumSqPredictions);
		for (int k = 0; k < models.size(); k++){
			ClusBeamModel cbm = models.get(k);
			ClassificationStat cstat = (ClassificationStat)cbm.getPredictionForTuple(tuple);
			double[][] cvals = cstat.getProbabilityPrediction();
			for (int i = 0; i < m_NbTarget; i++){
				for (int j = 0; j < cvals[i].length; j++){
					vals[i][j] += cvals[i][j];
					valssq[i][j] += cvals[i][j] * cvals[i][j];
				}
			}
		}
		for (int m = 0; m < m_SumPredictions.length; m++) {
			for (int n = 0; n < m_SumPredictions[m].length; n++){
				m_SumPredictions [m][n] += weight*vals[m][n];
				m_SumSqPredictions [m][n] += weight*valssq[m][n];				
			}
		}
	}

	public double getSVarS(ClusAttributeWeights scale) {
		double result = super.getSVarS(scale);
		double similarity = 0.0;
		double[][] probdistr = getProbabilityPrediction();
		//the predicted probabilities
		for (int i = 0; i < m_NbTarget; i++) {
			double firstterm = 0.0;
			double secondterm = 0.0;
			double thirdterm = 0.0;
			for (int j = 0; j < m_SumPredictions[i].length; j++){//for each class
				firstterm += probdistr[i][j] * probdistr[i][j];
				secondterm += 2 * probdistr[i][j] * getSumPrediction(i, j);
				thirdterm += getSumSqPrediction(i, j);
			}
			similarity += firstterm - secondterm + thirdterm;
			similarity *= scale.getWeight(m_Attrs[i]);//apply same weights as for normal mode
		}
		similarity /= m_NbTarget; //average for the targets
		similarity /= m_Beam.getCrWidth(); //average for the beam size
		//beta times similarity
		result += Settings.BEAM_SIMILARITY * similarity;
		return result;
	}

	public double[][] replicateEmpty(double[][] values){
		double[][] result = new double[values.length][];		
		for (int i = 0; i < values.length; i++)
			result[i] = new double[values[i].length];
		return result;
	}

	
	public double getSumPrediction(int target, int classval){
		return m_SumPredictions[target][classval];
	}
	
	public double[] getSumPrediction(int target){
		return m_SumPredictions[target];
	}
	
	public double getSumSqPrediction(int target, int classval){
		return m_SumSqPredictions[target][classval];
	}
	
	public double[] getSumSqPrediction(int target){
		return m_SumSqPredictions[target];
	}
	
	public void setBeam(ClusBeam beam){
		m_Beam = beam;
	}
	
}

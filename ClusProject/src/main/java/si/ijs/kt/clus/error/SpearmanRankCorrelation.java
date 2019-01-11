package si.ijs.kt.clus.error;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNumericError;
import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.format.ClusNumberFormat;

public class SpearmanRankCorrelation extends ClusNumericError implements ComponentError {
	private ArrayList<ArrayList<Double>> m_Predictions;
	private ArrayList<ArrayList<Double>> m_TrueValues;
	private double[] m_Correlations;
	private boolean m_CorrelationsUpToDate;
	private static final double PLACEHOLDER = -2506.1991; // something out of the [-1, 1] range

	public SpearmanRankCorrelation(ClusErrorList par, NumericAttrType[] num) {
		this(par, num, "");
	}

	public SpearmanRankCorrelation(ClusErrorList par, NumericAttrType[] num, String info) {
		super(par, num);
		m_Predictions = new ArrayList<ArrayList<Double>>();
		m_TrueValues = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < m_Dim; i++) {
			m_Predictions.add(new ArrayList<Double>());
			m_TrueValues.add(new ArrayList<Double>());
		}
		m_Correlations = new double[m_Dim];
		Arrays.fill(m_Correlations, SpearmanRankCorrelation.PLACEHOLDER);
		m_CorrelationsUpToDate = false;
		setAdditionalInfo(info);
	}

	// matejp: Commented out, because Pearson does not have this; feel free to change this
	// public SpearmanRankCorrelation(ClusErrorList par, int nbnum) {
	// super(par, nbnum);
	// // TODO Auto-generated constructor stub
	// }

	@Override
	public void addExample(double[] real, double[] predicted) {
		for (int i = 0; i < m_Dim; i++) {
			m_TrueValues.get(i).add(real[i]);
			m_Predictions.get(i).add(predicted[i]);
		}
		m_CorrelationsUpToDate = false;
	}

	@Override
	public void addExample(DataTuple tuple, ClusStatistic pred) {
		double[] predicted = pred.getNumericPred();
		double[] real = new double[m_Dim];
		for (int i = 0; i < m_Dim; i++) {
			real[i] = getAttr(i).getNumeric(tuple);
		}
		addExample(real, predicted);
	}

	@Override
	public void addExample(DataTuple real, DataTuple pred) {
		double[] real_values = new double[m_Dim];
		double[] predicted_values = new double[m_Dim];
		for (int i = 0; i < m_Dim; i++) {
			real_values[i] = getAttr(i).getNumeric(real);
			predicted_values[i] = getAttr(i).getNumeric(pred);
		}
		addExample(real_values, predicted_values);
	}

	@Override
	public void addInvalid(DataTuple tuple) {
	}

	@Override
	public void add(ClusError other) {
		SpearmanRankCorrelation o_spearman = (SpearmanRankCorrelation) other;
		for (int j = 0; j < o_spearman.m_Predictions.size(); j++) {
			double[] real = new double[m_Dim];
			double[] predicted = new double[m_Dim];
			for (int i = 0; i < m_Dim; i++) {
				real[i] = o_spearman.m_TrueValues.get(j).get(i);
				predicted[i] = o_spearman.m_Predictions.get(j).get(i);
			}
			addExample(real, predicted);
		}
	}

	@Override
	public void showModelError(PrintWriter out, int detail) {
		ClusNumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		double avg_corr = 0.0;
		double corr;
		for (int i = 0; i < m_Dim; i++) {
			if (i != 0) {
				buf.append(",");
			}
			corr = getModelErrorComponent(i);
			buf.append(fr.format(corr));
			avg_corr += corr;
		}
		avg_corr /= m_Dim;
		buf.append("]: " + fr.format(avg_corr));
		out.println(buf.toString());
	}

	@Override
	public double getModelErrorComponent(int i) {
		if (!m_CorrelationsUpToDate) {
			computeSpearmanRankCorrelations();
		}
		return m_Correlations[i];
	}

	@Override
	public boolean shouldBeLow() {
		return false;
	}

	@Override
	public String getName() {
		return "Spearman Rank Correlation";
	}

	public boolean hasSummary() {
		return false;
	}

	@Override
	public ClusError getErrorClone(ClusErrorList par) {
		return new SpearmanRankCorrelation(par, m_Attrs, getAdditionalInfo());
	}
	
    private void computeSpearmanRankCorrelations() {
    	int n = m_Predictions.get(0).size();
    	for(int i = 0; i < m_Dim; i++) {
    		double[] temp_true = new double[n];
    		double[] temp_pred = new double[n];
    		for (int j = 0; j < n; j++) {
    			temp_true[j] = m_TrueValues.get(i).get(j);
    			temp_pred[j] = m_Predictions.get(i).get(j);
    		}
    		double[] ranks_true = ClusUtil.getRanks(temp_true);
    		double[] ranks_pred = ClusUtil.getRanks(temp_pred);
    		m_Correlations[i] = ClusUtil.correlation(ranks_true, ranks_pred);
    	}
    	m_CorrelationsUpToDate = true;
    }
}

package clus.error;

import java.io.PrintWriter;
import java.text.NumberFormat;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;

public class MSNominalError extends ClusNominalError {

	public final static long serialVersionUID = 1L;

	protected ClusAttributeWeights m_Weights;
	protected double[] m_SumErr;
	protected double[] m_SumSqErr;
	protected boolean m_PrintAllComps = true;

	public MSNominalError(ClusErrorList par, NominalAttrType[] nom, ClusAttributeWeights weights) {
		super(par, nom);
		m_Weights = weights;
		m_SumErr = new double[m_Dim];
		m_SumSqErr = new double[m_Dim];
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MSNominalError(par, m_Attrs, m_Weights);
	}

	public String getName() {
		if (m_Weights == null) return "Mean squared error (MSE) for Nominal Attributes";
		else return "Weighted mean squared error (MSE) for Nominal Attributes ("+m_Weights.getName(m_Attrs)+")";
	}

	public double getModelErrorComponent(int i) {
		int nb = getNbExamples();
		double err = nb != 0.0 ? m_SumErr[i]/nb : 0.0;
		if (m_Weights != null) err *= m_Weights.getWeight(getAttr(i));
		return err;
	}

	public double getModelError() {
		double ss_tree = 0.0;
		int nb = getNbExamples();
		if (m_Weights != null) {
			for (int i = 0; i < m_Dim; i++) {
				ss_tree += m_SumErr[i]*m_Weights.getWeight(getAttr(i));
			}
			return nb != 0.0 ? ss_tree/nb/m_Dim : 0.0;
		} else{
			for (int i = 0; i < m_Dim; i++) {
				ss_tree += m_SumErr[i];
			}
			return nb != 0.0 ? ss_tree/nb/m_Dim : 0.0;
		}
	}

	public double getModelErrorStandardError() {
		double sum_err = 0.0;
		double sum_sq_err = 0.0;
		for (int i = 0; i < m_Dim; i++) {
			if (m_Weights != null) {
				sum_err += m_SumErr[i];
				sum_sq_err += m_SumSqErr[i];
			} else {
				sum_err += m_SumErr[i]*m_Weights.getWeight(getAttr(i));
				sum_sq_err += m_SumSqErr[i]*sqr(m_Weights.getWeight(getAttr(i)));
			}
		}
		double n = getNbExamples() * m_Dim;
		if (n <= 1) {
			return Double.POSITIVE_INFINITY;
		} else {
			double ss_x = (n * sum_sq_err - sqr(sum_err)) / (n * (n-1));
			return Math.sqrt(ss_x / n);
		}
	}

	public final static double sqr(double value) {
		return value*value;
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		ClassificationStat stat = pred.getClassificationStat();
		for (int i = 0; i < m_Dim; i++) {
			NominalAttrType type = m_Attrs[i];
			int value = type.getNominal(tuple);
			for (int j = 0; j < type.getNbValues(); j++) {
				double zeroOne = (value == j) ? 1.0 : 0.0;
				double prop = stat.getProportion(i, j);
				double err = sqr(zeroOne - prop);
				//double err = sqr(zeroOne - stat.getCount(i, j)/stat.getTotalWeight()); // note from celine: this line was used a while, because the line above did not work with ensembles and OOB errors (now it seems to work). However, it did not yield the exact same results...
				m_SumErr[i] += err;
				m_SumSqErr[i] += sqr(err);
			}
		}
	}

	public void addInvalid(DataTuple tuple) {
	}

	public void reset() {
		for (int i = 0; i < m_Dim; i++) {
			m_SumErr[i] = 0.0;
			m_SumSqErr[i] = 0.0;
		}
	}

	public void add(ClusError other) {
		MSNominalError oe = (MSNominalError)other;
		for (int i = 0; i < m_Dim; i++) {
			m_SumErr[i] += oe.m_SumErr[i];
			m_SumSqErr[i] += oe.m_SumSqErr[i];
		}
	}

	public void showModelError(PrintWriter out, int detail) {
		NumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		if (m_PrintAllComps) {
			buf.append("[");
			for (int i = 0; i < m_Dim; i++) {
				if (i != 0) buf.append(",");
				buf.append(fr.format(getModelErrorComponent(i)));
			}
			if (m_Dim > 1) buf.append("]: ");
			else buf.append("]");
		}
		if (m_Dim > 1 || !m_PrintAllComps) {
			buf.append(fr.format(getModelError()));
		}
		out.println(buf.toString());
	}

	public void showSummaryError(PrintWriter out, boolean detail) {
		NumberFormat fr = getFormat();
		out.println(getPrefix() + "Mean over components MSE: "+fr.format(getModelError()));
	}

	public double computeLeafError(ClusStatistic stat) {
		ClassificationStat cstat = (ClassificationStat)stat;
		return cstat.getSVarS(m_Weights) * cstat.getNbAttributes();
	}
}

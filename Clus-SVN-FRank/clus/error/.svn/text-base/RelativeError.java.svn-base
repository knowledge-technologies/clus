package clus.error;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.type.NumericAttrType;
import clus.statistic.ClusStatistic;

public class RelativeError extends ClusNumericError{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected double[] m_SumRelErr;

	public RelativeError(ClusErrorList par, NumericAttrType[] num) {
		this(par, num, null, true);
		m_SumRelErr = new double[m_Dim];
	}

	public RelativeError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
		this(par, num, weights, true);
		m_SumRelErr = new double[m_Dim];
	}

	public RelativeError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
		super(par, num);
		m_SumRelErr = new double[m_Dim];
	}

	@Override
	public void addExample(double[] real, double[] predicted) {
		for (int i = 0; i < m_Dim; i++) {
			double err = (real[i] - predicted[i])/real[i];
			m_SumRelErr[i] += err;

		}
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		double[] predicted = pred.getNumericPred();
		for (int i = 0; i < m_Dim; i++) {
			double err = Math.abs(getAttr(i).getNumeric(tuple) - predicted[i])/getAttr(i).getNumeric(tuple);
			m_SumRelErr[i] += err;
		}
	}

	public void addExample(DataTuple real, DataTuple pred) {
		for (int i = 0; i < m_Dim; i++) {
				double real_i = getAttr(i).getNumeric(real);
				double predicted_i = getAttr(i).getNumeric(pred);
				double err = Math.abs(real_i - predicted_i)/real_i;
				System.out.println(real_i);


				m_SumRelErr[i] += err;

		}
	}

	@Override
	public ClusError getErrorClone(ClusErrorList par) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getModelErrorComponent(int i) {
		int nb = getNbExamples();
	//	System.out.println(m_SumRelErr[i]);
		double err = nb != 0.0 ? m_SumRelErr[i]/nb : 0.0;
		System.out.println(err);

		return err;
	}

	@Override
	public String getName() {

		return "Relative Error";
	}




}

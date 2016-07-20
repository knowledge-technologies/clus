/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Created on May 12, 2005
 */
package clus.error;

import java.io.PrintWriter;
import java.text.NumberFormat;
import clus.data.attweights.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.main.*;
import clus.statistic.*;

// import jeans.util.array.*;

public class MSError extends ClusNumericError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


	protected int[]	m_nbEx;
	protected double[] m_SumErr;
	protected double[] m_SumSqErr;
	protected ClusAttributeWeights m_Weights;
	protected boolean m_PrintAllComps;

	public MSError(ClusErrorList par, NumericAttrType[] num) {
		this(par, num, null, true);
	}

	public MSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
		this(par, num, weights, true);
	}

	public MSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
		super(par, num);
		m_nbEx = new int[m_Dim];
		m_SumErr = new double[m_Dim];
		m_SumSqErr = new double[m_Dim];
		m_Weights = weights;
		m_PrintAllComps = printall;
	}

	public void reset() {
		for (int i = 0; i < m_Dim; i++) {
			m_SumErr[i] = 0.0;
			m_SumSqErr[i] = 0.0;
			m_nbEx[i] = 0;
		}
	}

	public void setWeights(ClusAttributeWeights weights) {
		m_Weights = weights;
	}

	public double getModelErrorComponent(int i) {

		//int nb = getNbExamples();
		int nb = m_nbEx[i];

		//System.out.println(m_SumErr[i]);
		double err = nb != 0.0 ? m_SumErr[i]/nb : 0.0;
		if (m_Weights != null) err *= m_Weights.getWeight(getAttr(i));

		return err;
	}

	public double getModelError() {
		double ss_tree = 0.0;
		int nb = 0;
		for(int i=0;i<m_Dim;i++){
			nb+= m_nbEx[i];
		}
		if (m_Weights != null) {
			for (int i = 0; i < m_Dim; i++) {
				ss_tree += m_SumErr[i]*m_Weights.getWeight(getAttr(i));
			}
			return nb != 0.0 ? ss_tree/nb : 0.0;
		} else {
			for (int i = 0; i < m_Dim; i++) {
				ss_tree += m_SumErr[i];
			}
			return nb != 0.0 ? ss_tree/nb : 0.0;
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
		//double n = getNbExamples() * m_Dim;
		double n = 0;
		for(int i=0;i<m_Dim;i++){
			n+= m_nbEx[i];
		}

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

	public void addExample(double[] real, double[] predicted) {
		for (int i = 0; i < m_Dim; i++) {
			double err = sqr(real[i] - predicted[i]);
			System.out.println(err);
			if(!Double.isInfinite(err) && !Double.isNaN(err)){
				m_SumErr[i] += err;
				m_SumSqErr[i] += sqr(err);
				m_nbEx[i]++;
			}
		}
	}
	
	public void addExample(double[] real, boolean[] predicted) {
		for (int i = 0; i < m_Dim; i++) {
			double predicted_i = predicted[i] ? 1.0 : 0.0;
			double err = sqr(real[i] - predicted_i);
			System.out.println(err);
			if(!Double.isInfinite(err) && !Double.isNaN(err)){
				m_SumErr[i] += err;
				m_SumSqErr[i] += sqr(err);
				m_nbEx[i]++;
			}
		}
	}	

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		double[] predicted = pred.getNumericPred();
		for (int i = 0; i < m_Dim; i++) {
			double err = sqr(getAttr(i).getNumeric(tuple) - predicted[i]);
			if(!Double.isInfinite(err) && !Double.isNaN(err)){
				m_SumErr[i] += err;
				m_SumSqErr[i] += sqr(err);
				m_nbEx[i]++;
			}
		}
	}

	public void addExample(DataTuple real, DataTuple pred) {
		for (int i = 0; i < m_Dim; i++) {
				double real_i = getAttr(i).getNumeric(real);
				double predicted_i = getAttr(i).getNumeric(pred);
				double err = sqr(real_i - predicted_i);
				if(!Double.isInfinite(err) && !Double.isNaN(err)){
					m_SumErr[i] += err;
					m_SumSqErr[i] += sqr(err);
					m_nbEx[i]++;
				}
		}
	}
	public void addInvalid(DataTuple tuple) {
	}

	public void add(ClusError other) {
		MSError oe = (MSError)other;
		for (int i = 0; i < m_Dim; i++) {
			m_SumErr[i] += oe.m_SumErr[i];
			m_SumSqErr[i] += oe.m_SumSqErr[i];
			m_nbEx[i]+= oe.m_nbEx[i];
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

	public String getName() {
		if (m_Weights == null) return "Mean squared error (MSE)";
		else return "Weighted mean squared error (MSE) ("+m_Weights.getName(m_Attrs)+")";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MSError(par, m_Attrs, m_Weights, m_PrintAllComps);
	}

	public double computeLeafError(ClusStatistic stat) {
		RegressionStat rstat = (RegressionStat)stat;
		return rstat.getSVarS(m_Weights) * rstat.getNbAttributes();
	}
}

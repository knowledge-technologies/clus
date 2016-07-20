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

package clus.statistic;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;

import jeans.math.MathUtil;
import jeans.util.StringUtils;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.*;

import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.util.*;
import clus.data.cols.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.data.attweights.*;
import clus.error.ClusNumericError;

public class RegressionStat extends RegressionStatBase {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public double[] m_SumValues;
	public double[] m_SumWeights;
	public double[] m_SumSqValues;
	public RegressionStat m_Training;

	public RegressionStat(NumericAttrType[] attrs) {
		this(attrs, false);
	}

	public RegressionStat(NumericAttrType[] attrs, boolean onlymean) {
		super(attrs, onlymean);
		if (!onlymean) {
			m_SumValues = new double[m_NbAttrs];
			m_SumWeights = new double[m_NbAttrs];
			m_SumSqValues = new double[m_NbAttrs];
		}
	}

	public void setTrainingStat(ClusStatistic train) {
		m_Training = (RegressionStat)train;
	}	
	
	public ClusStatistic cloneStat() {
		RegressionStat res = new RegressionStat(m_Attrs, false);
		res.m_Training = m_Training;
		return res;
	}

	public ClusStatistic cloneSimple() {
		RegressionStat res = new RegressionStat(m_Attrs, true);
		res.m_Training = m_Training;
		return res;
	}

	/** Clone this statistic by taking the given weight into account.
	 *  This is used for example to get the weighted prediction of default rule. */
	public ClusStatistic copyNormalizedWeighted(double weight) {
//		RegressionStat newStat = (RegressionStat) cloneSimple();
		RegressionStat newStat = (RegressionStat) normalizedCopy();
		for (int iTarget = 0; iTarget < newStat.getNbAttributes(); iTarget++ ){
			newStat.m_Means[iTarget] = weight * newStat.m_Means[iTarget];
		}
		return (ClusStatistic) newStat;
	}

	public void reset() {
		m_SumWeight = 0.0;
		m_NbExamples = 0;
		Arrays.fill(m_SumWeights, 0.0);
		Arrays.fill(m_SumValues, 0.0);
		Arrays.fill(m_SumSqValues, 0.0);
	}

	public void copy(ClusStatistic other) {
		RegressionStat or = (RegressionStat)other;
		m_SumWeight = or.m_SumWeight;
		m_NbExamples = or.m_NbExamples;
		System.arraycopy(or.m_SumWeights, 0, m_SumWeights, 0, m_NbAttrs);
		System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
		System.arraycopy(or.m_SumSqValues, 0, m_SumSqValues, 0, m_NbAttrs);
	}

	/**
	 * Used for combining weighted predictions.
	 */
	public RegressionStat normalizedCopy() {
		RegressionStat copy = (RegressionStat)cloneSimple();
		copy.m_NbExamples = 0;
		copy.m_SumWeight = 1;
		calcMean(copy.m_Means);
		return copy;
	}

	public void add(ClusStatistic other) {
		RegressionStat or = (RegressionStat)other;
		m_SumWeight += or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumWeights[i] += or.m_SumWeights[i];
			m_SumValues[i] += or.m_SumValues[i];
			m_SumSqValues[i] += or.m_SumSqValues[i];
		}
	}

	public void addScaled(double scale, ClusStatistic other) {
		RegressionStat or = (RegressionStat)other;
		m_SumWeight += scale * or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumWeights[i] += scale * or.m_SumWeights[i];
			m_SumValues[i] += scale * or.m_SumValues[i];
			m_SumSqValues[i] += scale * or.m_SumSqValues[i];
		}
	}

	public void subtractFromThis(ClusStatistic other) {
		RegressionStat or = (RegressionStat)other;
		m_SumWeight -= or.m_SumWeight;
		m_NbExamples -= or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumWeights[i] -= or.m_SumWeights[i];
			m_SumValues[i] -= or.m_SumValues[i];
			m_SumSqValues[i] -= or.m_SumSqValues[i];
		}
	}

	public void subtractFromOther(ClusStatistic other) {
		RegressionStat or = (RegressionStat)other;
		m_SumWeight = or.m_SumWeight - m_SumWeight;
		m_NbExamples = or.m_NbExamples - m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumWeights[i] = or.m_SumWeights[i] - m_SumWeights[i];
			m_SumValues[i] = or.m_SumValues[i] - m_SumValues[i];
			m_SumSqValues[i] = or.m_SumSqValues[i] - m_SumSqValues[i];
		}
	}

	public void updateWeighted(DataTuple tuple, double weight) {
		m_NbExamples++;
		m_SumWeight += weight;
		for (int i = 0; i < m_NbAttrs; i++) {
			double val = m_Attrs[i].getNumeric(tuple);
			if (val != Double.POSITIVE_INFINITY) {
				m_SumWeights[i] += weight;
				m_SumValues[i] += weight*val;
				m_SumSqValues[i] += weight*val*val;
			}
		}
	}

	public void calcMean(double[] means) {
		for (int i = 0; i < m_NbAttrs; i++) {
			// If divider zero, return zero
			means[i] = m_SumWeights[i] != 0.0 ? m_SumValues[i] / m_SumWeights[i] : 0.0;
		}
	}

	public double getMean(int i) {
		return m_SumWeights[i] != 0.0 ? m_SumValues[i] / m_SumWeights[i] : 0.0;
	}

	public double getSumValues(int i) {
		return m_SumValues[i];
	}

	public double getSumWeights(int i) {
		return m_SumWeights[i];
	}

	public double getSVarS(int i) {
		double n_tot = m_SumWeight;
		double k_tot = m_SumWeights[i];
		double sv_tot = m_SumValues[i];
		double ss_tot = m_SumSqValues[i];
		if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
			return m_Training.getSVarS(i);
		} else {
			return (k_tot > 1.0) ? ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot/k_tot*sv_tot/k_tot : 0.0;
		}
	}

	public double getSVarS(ClusAttributeWeights scale) {
		double result = 0.0;
		for (int i = 0; i < m_NbAttrs; i++) {
			double n_tot = m_SumWeight;
			double k_tot = m_SumWeights[i];
			double sv_tot = m_SumValues[i];
			double ss_tot = m_SumSqValues[i];
			if (k_tot == n_tot) {
				result += (ss_tot - sv_tot*sv_tot/n_tot)*scale.getWeight(m_Attrs[i]);
			} else {
				if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
					result += m_Training.getSVarS(i)*scale.getWeight(m_Attrs[i]);
				} else {
					result += (ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot/k_tot*sv_tot/k_tot)*scale.getWeight(m_Attrs[i]);
				}
			}
		}
		return result / m_NbAttrs;
	}

	public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
		double result = 0.0;
		RegressionStat or = (RegressionStat)other;
		for (int i = 0; i < m_NbAttrs; i++) {
			double n_tot = m_SumWeight - or.m_SumWeight;
			double k_tot = m_SumWeights[i] - or.m_SumWeights[i];
			double sv_tot = m_SumValues[i] - or.m_SumValues[i];
			double ss_tot = m_SumSqValues[i] - or.m_SumSqValues[i];
			if (k_tot == n_tot) {
				result += (ss_tot - sv_tot*sv_tot/n_tot)*scale.getWeight(m_Attrs[i]);
			} else {
				if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
					result += m_Training.getSVarS(i)*scale.getWeight(m_Attrs[i]);
				} else {
					result += (ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot/k_tot*sv_tot/k_tot)*scale.getWeight(m_Attrs[i]);
				}
			}
		}
		return result / m_NbAttrs;
	}

	public String getString(StatisticPrintInfo info) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < m_NbAttrs; i++) {
			if (i != 0) buf.append(",");
			double tot = getSumWeights(i);
			if (tot == 0) buf.append("?");
			else buf.append(fr.format(getSumValues(i)/tot));
		}
		buf.append("]");
		if (info.SHOW_EXAMPLE_COUNT_BYTARGET) {
			buf.append(": [");
			for (int i = 0; i < m_NbAttrs; i++) {
				if (i != 0) buf.append(",");
				buf.append(fr.format(m_SumWeights[i]));
			}
			buf.append("]");
		} else if (info.SHOW_EXAMPLE_COUNT) {
			buf.append(": ");
			buf.append(fr.format(m_SumWeight));
		}
		return buf.toString();
	}

	public void printDebug() {
		for (int i = 0; i < getNbAttributes(); i++) {
			double n_tot = m_SumWeight;
			double k_tot = m_SumWeights[i];
			double sv_tot = m_SumValues[i];
			double ss_tot = m_SumSqValues[i];
			System.out.println("n: "+n_tot+" k: "+k_tot);
			System.out.println("sv: "+sv_tot);
			System.out.println("ss: "+ss_tot);
			double mean = sv_tot / n_tot;
			double var = ss_tot - n_tot*mean*mean;
			System.out.println("mean: "+mean);
			System.out.println("var: "+var);
		}
		System.out.println("err: "+getError());
	}

	public RegressionStat getRegressionStat() {
		return this;
	}
	
	
	public double getSquaredDistance(ClusStatistic other) {
		double result = 0.0;
		RegressionStat o = (RegressionStat)other;
		for (int i = 0; i < m_NbAttrs; i++){
			double distance = getMean(i) - o.getMean(i);
			result += distance * distance;
		}
		return result;
	}
	
}

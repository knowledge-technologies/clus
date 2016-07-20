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

public class RegressionStatBinaryNomiss extends RegressionStatBase {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public double[] m_SumValues;

	public RegressionStatBinaryNomiss(NumericAttrType[] attrs) {
		this(attrs, false);
	}

	public RegressionStatBinaryNomiss(NumericAttrType[] attrs, boolean onlymean) {
		super(attrs, onlymean);
		if (!onlymean) {
			m_SumValues = new double[m_NbAttrs];
		}
	}

	public ClusStatistic cloneStat() {
		return new RegressionStatBinaryNomiss(m_Attrs, false);
	}

	public ClusStatistic cloneSimple() {
		return new RegressionStatBinaryNomiss(m_Attrs, true);
	}

	/** Clone this statistic by taking the given weight into account.
	 *  This is used for example to get the weighted prediction of default rule. */
	public ClusStatistic copyNormalizedWeighted(double weight) {
//		RegressionStat newStat = (RegressionStat) cloneSimple();
		RegressionStatBinaryNomiss newStat = (RegressionStatBinaryNomiss) normalizedCopy();
		for (int iTarget = 0; iTarget < newStat.getNbAttributes(); iTarget++ ){
			newStat.m_Means[iTarget] = weight * newStat.m_Means[iTarget];
		}
		return (ClusStatistic) newStat;
	}

	public void reset() {
		m_SumWeight = 0.0;
		m_NbExamples = 0;
		Arrays.fill(m_SumValues, 0.0);
	}

	public void copy(ClusStatistic other) {
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		m_SumWeight = or.m_SumWeight;
		m_NbExamples = or.m_NbExamples;
		System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
	}

	/**
	 * Used for combining weighted predictions.
	 */
	public RegressionStatBinaryNomiss normalizedCopy() {
		RegressionStatBinaryNomiss copy = (RegressionStatBinaryNomiss)cloneSimple();
		copy.m_NbExamples = 0;
		copy.m_SumWeight = 1;
		calcMean(copy.m_Means);
		return copy;
	}

	public void add(ClusStatistic other) {
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		m_SumWeight += or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumValues[i] += or.m_SumValues[i];
		}
	}

	public void addScaled(double scale, ClusStatistic other) {
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		m_SumWeight += scale * or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumValues[i] += scale * or.m_SumValues[i];
		}
	}

	public void subtractFromThis(ClusStatistic other) {
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		m_SumWeight -= or.m_SumWeight;
		m_NbExamples -= or.m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumValues[i] -= or.m_SumValues[i];
		}
	}

	public void subtractFromOther(ClusStatistic other) {
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		m_SumWeight = or.m_SumWeight - m_SumWeight;
		m_NbExamples = or.m_NbExamples - m_NbExamples;
		for (int i = 0; i < m_NbAttrs; i++) {
			m_SumValues[i] = or.m_SumValues[i] - m_SumValues[i];
		}
	}

	public void calcMean(double[] means) {
		for (int i = 0; i < m_NbAttrs; i++) {			
			means[i] = getMean(i);
		}
	}

	public double getMean(int i) {
		// If divider zero, return zero		
		return m_SumWeight != 0.0 ? m_SumValues[i] / m_SumWeight : 0.0;
	}

	public double getSVarS(int i) {
		double n_tot = m_SumWeight;
		double sv_tot = m_SumValues[i];
		return sv_tot - sv_tot*sv_tot/n_tot;
	}

	public double getSVarS(ClusAttributeWeights scale) {
		double result = 0.0;
		for (int i = 0; i < m_NbAttrs; i++) {
			double n_tot = m_SumWeight;
			double sv_tot = m_SumValues[i];
			result += (sv_tot - sv_tot*sv_tot/n_tot)*scale.getWeight(m_Attrs[i]);
		}
		return result / m_NbAttrs;
	}

	public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
		double result = 0.0;
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss)other;
		for (int i = 0; i < m_NbAttrs; i++) {
			double n_tot = m_SumWeight - or.m_SumWeight;
			double sv_tot = m_SumValues[i] - or.m_SumValues[i];
			result += (sv_tot - sv_tot*sv_tot/n_tot)*scale.getWeight(m_Attrs[i]);
		}
		return result / m_NbAttrs;
	}

	public String getString(StatisticPrintInfo info) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < m_NbAttrs; i++) {
			if (i != 0) buf.append(",");
			buf.append(fr.format(getMean(i)));
		}
		buf.append("]");
		if (info.SHOW_EXAMPLE_COUNT) {
			buf.append(": ");
			buf.append(fr.format(m_SumWeight));
		}
		return buf.toString();
	}
}

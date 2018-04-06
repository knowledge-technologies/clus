/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.statistic;

import java.text.NumberFormat;
import java.util.Arrays;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.util.format.ClusFormat;


public class RegressionStatBinaryNomiss extends RegressionStatBase implements ComponentStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double[] m_SumValues;

    public WHTDStatistic m_ParentStat; //statistic of the parent node
    public WHTDStatistic m_Training;

    public RegressionStatBinaryNomiss(Settings sett, NumericAttrType[] attrs) {
        this(sett, attrs, false);
    }


    public RegressionStatBinaryNomiss(Settings sett, NumericAttrType[] attrs, boolean onlymean) {
        super(sett, attrs, onlymean);
        if (!onlymean) {
            m_SumValues = new double[m_NbAttrs];
        }
    }


    @Override
    public ClusStatistic cloneStat() {
        return new RegressionStatBinaryNomiss(this.m_Settings, m_Attrs, false);
    }


    @Override
    public ClusStatistic cloneSimple() {
        return new RegressionStatBinaryNomiss(this.m_Settings, m_Attrs, true);
    }


    /**
     * Clone this statistic by taking the given weight into account.
     * This is used for example to get the weighted prediction of default rule.
     */
    @Override
    public ClusStatistic copyNormalizedWeighted(double weight) {
        // RegressionStat newStat = (RegressionStat) cloneSimple();
        RegressionStatBinaryNomiss newStat = normalizedCopy();
        for (int iTarget = 0; iTarget < newStat.getNbAttributes(); iTarget++) {
            newStat.m_Means[iTarget] = weight * newStat.m_Means[iTarget];
        }
        return newStat;
    }


    @Override
    public void reset() {
        m_SumWeight = 0.0;
        m_NbExamples = 0;
        Arrays.fill(m_SumValues, 0.0);
    }


    @Override
    public void copy(ClusStatistic other) {
        RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
        m_SumWeight = or.m_SumWeight;
        m_NbExamples = or.m_NbExamples;
        System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
    }


    /**
     * Used for combining weighted predictions.
     */
    @Override
    public RegressionStatBinaryNomiss normalizedCopy() {
        RegressionStatBinaryNomiss copy = (RegressionStatBinaryNomiss) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        calcMean(copy.m_Means);
        return copy;
    }


    @Override
    public void add(ClusStatistic other) {
        RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
        m_SumWeight += or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumValues[i] += or.m_SumValues[i];
        }
    }


    @Override
    public void addScaled(double scale, ClusStatistic other) {
        RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
        m_SumWeight += scale * or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumValues[i] += scale * or.m_SumValues[i];
        }
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
        m_SumWeight -= or.m_SumWeight;
        m_NbExamples -= or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumValues[i] -= or.m_SumValues[i];
        }
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_NbExamples = or.m_NbExamples - m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumValues[i] = or.m_SumValues[i] - m_SumValues[i];
        }
    }


    @Override
    public void calcMean(double[] means) {
        for (int i = 0; i < m_NbAttrs; i++) {
            means[i] = getMean(i);
        }
    }


    @Override
    public double getMean(int i) {
        // If divider zero, return zero
        return m_SumWeight != 0.0 ? m_SumValues[i] / m_SumWeight : 0.0;
    }


    @Override
    public double getSVarS(int i) {
        double n_tot = m_SumWeight;
        double sv_tot = m_SumValues[i];
        return sv_tot - sv_tot * sv_tot / n_tot;
    }


    @Override
    public double getSVarS(ClusAttributeWeights scale) {
        double result = 0.0, sv_tot;
        double n_tot = m_SumWeight;
        
        if(n_tot == 0.0)
            return getEstimatedSVarS(scale) / m_NbAttrs;
        
        for (int i = 0; i < m_NbAttrs; i++) {
            sv_tot = m_SumValues[i];
            result += (sv_tot - sv_tot * sv_tot / n_tot) * scale.getWeight(m_Attrs[i]);
        }
        return result / m_NbAttrs;
    }


    @Override
    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
		double result = 0.0, sv_tot;
		RegressionStatBinaryNomiss or = (RegressionStatBinaryNomiss) other;
		double n_tot = m_SumWeight - or.m_SumWeight;

		if (n_tot == 0.0)
			return or.getEstimatedSVarS(scale) / m_NbAttrs;

		for (int i = 0; i < m_NbAttrs; i++) {
			sv_tot = m_SumValues[i] - or.m_SumValues[i];
			result += (sv_tot - sv_tot * sv_tot / n_tot) * scale.getWeight(m_Attrs[i]);
		}
		return result / m_NbAttrs;
    }

    /** The case where are examples have missing value for the i-th attribute, i.e., variance can not be calculated, so it is estimated */
	public double getEstimatedSVarS(ClusAttributeWeights scale) {
		switch (getSettings().getTree().getMissingClusteringAttrHandling()) {
		case SettingsTree.MISSING_ATTRIBUTE_HANDLING_TRAINING:
			if (m_Training == null)
				return Double.NaN;
			return m_Training.getSVarS(scale);
		case SettingsTree.MISSING_ATTRIBUTE_HANDLING_PARENT:
			if (m_ParentStat == null)
				return Double.NaN; // the case if for attribute i all examples
									// gave missing values (if there is no
									// parent stat, it means we reached the root
									// node)
			return m_ParentStat.getSVarS(scale);
		case SettingsTree.MISSING_ATTRIBUTE_HANDLING_NONE:
			return Double.NaN;
		default:
			if (m_Training == null)
				return Double.NaN;
			return m_Training.getSVarS(scale);
		}
	}
    
    @Override
    public String getString(StatisticPrintInfo info) {
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(fr.format(getMean(i)));
        }
        buf.append("]");
        if (info.SHOW_EXAMPLE_COUNT) {
            buf.append(": ");
            buf.append(fr.format(m_SumWeight));
        }
        return buf.toString();
    }

    public double[] getSumValues(){
        return m_SumValues;
    }

    @Override
    public int getNbStatisticComponents() {
        return m_SumValues.length;
    }
    
    @Override
    public void setParentStat(ClusStatistic parent) {
        m_ParentStat = (WHTDStatistic) parent;
    }

    @Override
    public ClusStatistic getParentStat() {
        return m_ParentStat;
    }
}

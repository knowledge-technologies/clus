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

// daniela

package clus.statistic;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.type.primitive.NumericAttrType;
import clus.main.settings.Settings;
import clus.util.jeans.math.MathUtil;


public class GISStat extends RegressionStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
    public int m_NbTarget;
    public double[] m_SumValues;
    public double[] m_SumWeights;
    public double[] m_SumSqValues;
    public GISStat m_Training;


    public GISStat(Settings sett, NumericAttrType[] attrs) {
        this(sett, attrs, false);
    }


    public GISStat(Settings sett, NumericAttrType[] attrs, boolean onlymean) {
        super(sett, attrs, onlymean);

        if (!onlymean) {
            m_SumValues = new double[m_NbAttrs];
            m_SumWeights = new double[m_NbAttrs];
            m_SumSqValues = new double[m_NbAttrs];
            m_NbTarget = attrs.length;
        }
    }


    public void setTrainingStat(ClusStatistic train) {
        m_Training = (GISStat) train;
    }


    public ClusStatistic cloneStat() {
        GISStat res = new GISStat(getSettings(), m_Attrs, false);
        res.m_Training = m_Training;
        res.m_NbTarget = m_NbTarget;
        return res;
    }


    public ClusStatistic cloneSimple() {
        GISStat res = new GISStat(getSettings(), m_Attrs, true);
        res.m_Training = m_Training;
        res.m_NbTarget = m_NbTarget;
        return res;
    }


    /**
     * Clone this statistic by taking the given weight into account.
     * This is used for example to get the weighted prediction of default rule.
     */
    public ClusStatistic copyNormalizedWeighted(double weight) {
        //    GISStat newStat = (GISStat) cloneSimple();
        GISStat newStat = (GISStat) normalizedCopy();
        for (int iTarget = 0; iTarget < newStat.getNbAttributes(); iTarget++) {
            newStat.m_Means[iTarget] = weight * newStat.m_Means[iTarget];
        }
        return (ClusStatistic) newStat;
    }


    public void copy(ClusStatistic other) {
        GISStat or = (GISStat) other;
        m_SumWeight = or.m_SumWeight;
        m_NbExamples = or.m_NbExamples;
        m_NbTarget = or.m_NbTarget;
        System.arraycopy(or.m_SumWeights, 0, m_SumWeights, 0, m_NbAttrs);
        System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
        System.arraycopy(or.m_SumSqValues, 0, m_SumSqValues, 0, m_NbAttrs);
    }


    /**
     * Used for combining weighted predictions.
     */
    public GISStat normalizedCopy() {
        GISStat copy = (GISStat) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        calcMean(copy.m_Means);
        return copy;
    }


    public void add(ClusStatistic other) {
        GISStat or = (GISStat) other;
        m_SumWeight += or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] += or.m_SumWeights[i];
            m_SumValues[i] += or.m_SumValues[i];
            m_SumSqValues[i] += or.m_SumSqValues[i];
        }
    }


    public void addScaled(double scale, ClusStatistic other) {
        GISStat or = (GISStat) other;
        m_SumWeight += scale * or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] += scale * or.m_SumWeights[i];
            m_SumValues[i] += scale * or.m_SumValues[i];
            m_SumSqValues[i] += scale * or.m_SumSqValues[i];
        }
    }


    public void subtractFromThis(ClusStatistic other) {
        GISStat or = (GISStat) other;
        m_SumWeight -= or.m_SumWeight;
        m_NbExamples -= or.m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] -= or.m_SumWeights[i];
            m_SumValues[i] -= or.m_SumValues[i];
            m_SumSqValues[i] -= or.m_SumSqValues[i];
        }
    }


    public void subtractFromOther(ClusStatistic other) {
        GISStat or = (GISStat) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_NbExamples = or.m_NbExamples - m_NbExamples;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] = or.m_SumWeights[i] - m_SumWeights[i];
            m_SumValues[i] = or.m_SumValues[i] - m_SumValues[i];
            m_SumSqValues[i] = or.m_SumSqValues[i] - m_SumSqValues[i];
        }
    }


    public double getVarLisa(int i) {
        double n_tot = m_SumWeight;
        double k_tot = m_SumWeights[i];
        double sv_tot = m_SumValues[i];
        double ss_tot = m_SumSqValues[i];
        if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
            return m_Training.getSVarS(i);
        }
        else {
            return (k_tot > 1.0) ? ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot / k_tot * sv_tot / k_tot : 0.0;
        }
    }


    public double getVarLisa(ClusAttributeWeights scale) {
        double result = 0.0;
        for (int i = 0; i < m_NbAttrs; i++) {
            double n_tot = m_SumWeight;
            double k_tot = m_SumWeights[i];
            double sv_tot = m_SumValues[i];
            double ss_tot = m_SumSqValues[i];

            if (k_tot == n_tot) {
                result += (ss_tot - sv_tot * sv_tot / n_tot) * scale.getWeight(m_Attrs[i]);
            }
            else {
                if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
                    result += m_Training.getSVarS(i) * scale.getWeight(m_Attrs[i]);
                }
                else {
                    result += (ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot / k_tot * sv_tot / k_tot) * scale.getWeight(m_Attrs[i]);
                }
            }
        }
        return result / m_NbAttrs;
    }


    public double getVarLisaDiff(ClusAttributeWeights scale, ClusStatistic other) {
        double result = 0.0;
        RegressionStat or = (RegressionStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            double n_tot = m_SumWeight - or.m_SumWeight;
            double k_tot = m_SumWeights[i] - or.getSumWeights(i); //or.m_SumWeights[i];   matejp was here
            double sv_tot = m_SumValues[i] - or.getSumValues(i); //or.m_SumValues[i];
            double ss_tot = m_SumSqValues[i] - or.getSumSqValues(i); //or.m_SumSqValues[i];
            if (k_tot == n_tot) {
                result += (ss_tot - sv_tot * sv_tot / n_tot) * scale.getWeight(m_Attrs[i]);
            }
            else {
                if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
                    result += m_Training.getSVarS(i) * scale.getWeight(m_Attrs[i]);
                }
                else {
                    result += (ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot / k_tot * sv_tot / k_tot) * scale.getWeight(m_Attrs[i]);
                }
            }
        }
        return result / m_NbAttrs;
    }


    public GISStat getGISStat() {
        return this;
    }

}

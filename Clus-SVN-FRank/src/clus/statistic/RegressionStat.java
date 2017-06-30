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

package clus.statistic;

import java.text.NumberFormat;
import java.util.Arrays;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.type.NumericAttrType;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import clus.jeans.math.MathUtil;
import clus.main.Settings;
import clus.util.ClusFormat;


public class RegressionStat extends RegressionStatBase implements ComponentStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private double[] m_SumValues;
    private double[] m_SumWeights;
    private double[] m_SumSqValues;
    private RegressionStat m_Training;
    private ClusHMTRHierarchy m_HMTRHierarchy;
    public RegressionStat m_ParentStat; //statistic of the parent node

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
    
    public RegressionStat(NumericAttrType[] attrs, ClusHMTRHierarchy hier, boolean onlymean) {
        this(attrs, onlymean);
        m_HMTRHierarchy = hier;
    }
    
    public RegressionStat(NumericAttrType[] attrs, ClusHMTRHierarchy hier) {
        this(attrs, hier, false);
    }
    
    
    public void setSumValues(int index, double value){
    	m_SumValues[index] = value;
    }
    
    public void resetSumValues(int length){
    	m_SumValues = new double[length];
    }
    
    public void setSumWeights(int index, double value){
    	 m_SumWeights[index] = value;
    }
    
    public void resetSumWeights(int length){
    	 m_SumWeights = new double[length];
    }

    public void setTrainingStat(ClusStatistic train) {
        m_Training = (RegressionStat) train;
    }    

    public ClusStatistic cloneStat() {
        RegressionStat res;
        
        if(Settings.isHMTREnabled()) {
            res = new RegressionStat(m_Attrs, m_HMTRHierarchy, false);
        } else {
            res = new RegressionStat(m_Attrs, false);
        }
        
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        return res;
    }


    public ClusStatistic cloneSimple() {
        RegressionStat res;
        
        if(Settings.isHMTREnabled()) {
            res = new RegressionStat(m_Attrs, m_HMTRHierarchy, true);
        } else {
            res = new RegressionStat(m_Attrs, true);
        }
        
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        return res;
    }


    /**
     * Clone this statistic by taking the given weight into account.
     * This is used for example to get the weighted prediction of default rule.
     */
    public ClusStatistic copyNormalizedWeighted(double weight) {
        // RegressionStat newStat = (RegressionStat) cloneSimple();
        RegressionStat newStat = (RegressionStat) normalizedCopy();
        for (int iTarget = 0; iTarget < newStat.getNbAttributes(); iTarget++) {
            newStat.m_Means[iTarget] = weight * newStat.m_Means[iTarget];
        }
        return (ClusStatistic) newStat;
    }


    public void reset() {
        m_SumWeight = 0.0;
        m_NbExamples = 0;
        m_SumWeightLabeled = 0.0;
        Arrays.fill(m_SumWeights, 0.0);
        Arrays.fill(m_SumValues, 0.0);
        Arrays.fill(m_SumSqValues, 0.0);
    }


    public void copy(ClusStatistic other) {
        RegressionStat or = (RegressionStat) other;
        m_SumWeight = or.m_SumWeight;
        m_NbExamples = or.m_NbExamples;
        m_SumWeightLabeled = or.m_SumWeightLabeled;
        System.arraycopy(or.m_SumWeights, 0, m_SumWeights, 0, m_NbAttrs);
        System.arraycopy(or.m_SumValues, 0, m_SumValues, 0, m_NbAttrs);
        System.arraycopy(or.m_SumSqValues, 0, m_SumSqValues, 0, m_NbAttrs);
    }


    /**
     * Used for combining weighted predictions.
     */
    public RegressionStat normalizedCopy() {
        RegressionStat copy = (RegressionStat) cloneSimple();
        copy.m_NbExamples = 0;
        copy.m_SumWeight = 1;
        calcMean(copy.m_Means);
        return copy;
    }


    public void add(ClusStatistic other) {
        RegressionStat or = (RegressionStat) other;
        m_SumWeight += or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        m_SumWeightLabeled += or.m_SumWeightLabeled;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] += or.m_SumWeights[i];
            m_SumValues[i] += or.m_SumValues[i];
            m_SumSqValues[i] += or.m_SumSqValues[i];
        }
    }


    public void addScaled(double scale, ClusStatistic other) {
        RegressionStat or = (RegressionStat) other;
        m_SumWeight += scale * or.m_SumWeight;
        m_NbExamples += or.m_NbExamples;
        m_SumWeightLabeled += scale * or.m_SumWeightLabeled;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] += scale * or.m_SumWeights[i];
            m_SumValues[i] += scale * or.m_SumValues[i];
            m_SumSqValues[i] += scale * or.m_SumSqValues[i];
        }
    }


    public void subtractFromThis(ClusStatistic other) {
        RegressionStat or = (RegressionStat) other;
        m_SumWeight -= or.m_SumWeight;
        m_NbExamples -= or.m_NbExamples;
        m_SumWeightLabeled -= or.m_SumWeightLabeled;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] -= or.m_SumWeights[i];
            m_SumValues[i] -= or.m_SumValues[i];
            m_SumSqValues[i] -= or.m_SumSqValues[i];
        }
    }


    public void subtractFromOther(ClusStatistic other) {
        RegressionStat or = (RegressionStat) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_NbExamples = or.m_NbExamples - m_NbExamples;
        m_SumWeightLabeled += or.m_SumWeightLabeled - m_SumWeightLabeled;
        for (int i = 0; i < m_NbAttrs; i++) {
            m_SumWeights[i] = or.m_SumWeights[i] - m_SumWeights[i];
            m_SumValues[i] = or.m_SumValues[i] - m_SumValues[i];
            m_SumSqValues[i] = or.m_SumSqValues[i] - m_SumSqValues[i];
        }
    }


    public void updateWeighted(DataTuple tuple, double weight) {
        m_NbExamples++;
        m_SumWeight += weight;
        boolean hasLabel = false;
        for (int i = 0; i < m_NbAttrs; i++) {
            double val = m_Attrs[i].getNumeric(tuple);
            if (val != Double.POSITIVE_INFINITY) {
                m_SumWeights[i] += weight;
                m_SumValues[i] += weight * val;
                m_SumSqValues[i] += weight * val * val; // TODO tu lahko prisparas en korak, ce si zapomnis weight * val
                
                if(!hasLabel && m_Attrs[i].isTarget())
                    hasLabel = true;
            }
        }
        
        if(hasLabel)
            m_SumWeightLabeled += weight;
    }


    public void calcMean(double[] means) {
        for (int i = 0; i < m_NbAttrs; i++) {
            // If divider zero, return zero
            means[i] = m_SumWeights[i] != 0.0 ? m_SumValues[i] / m_SumWeights[i] : 0.0;
        }
    }


	public double getMean(int i) {
        //added by Jurica (if we call getMean() and m_SumWeights or m_SumValues are not initialized (e.g. after cloneSimple) it chrashes)
        if(m_SumValues == null || m_SumWeights == null) {
            return m_Means[i];
        }
        // - end added by Jurica
        
        //If divider zero, i.e., for i-th attribute we have no labeled examples or all missing values
        if(m_SumWeights[i] == 0) {
            switch(Settings.getMissingTargetAttrHandling()) {
                case Settings.MISSING_ATTRIBUTE_HANDLING_TRAINING : 
                    if(m_Training == null)
                        return Double.NaN; // for attribute i, all values are missing
                    return m_Training.getMean(i);
                case Settings.MISSING_ATTRIBUTE_HANDLING_PARENT :
                    if(m_ParentStat == null)
                        return Double.NaN; // for attribute i, all values are missing
                    return m_ParentStat.getMean(i);
                case Settings.MISSING_ATTRIBUTE_HANDLING_NONE : return 0;
                default:
                    if(m_Training == null)
                        return Double.NaN; // for attribute i, all values are missing
                    return m_Training.getMean(i);
            }
        }
            
        return m_SumValues[i] / m_SumWeights[i];
	}


    public double getSumValues(int i) {
        return m_SumValues[i];
    }


    public double getSumWeights(int i) {
        return m_SumWeights[i];
    }
    
    public RegressionStat getTraining(){
    	return m_Training;
    }

    /** The case where are examples have missing value for the i-th attribute, i.e., variance can not be calculated, so it is estimated */
    public double getEstimatedSVarS(int i) {
        switch (Settings.getMissingClusteringAttrHandling()) {
            case Settings.MISSING_ATTRIBUTE_HANDLING_TRAINING:
                if(m_Training == null)
                    return Double.NaN;
                return m_Training.getSVarS(i);
            case Settings.MISSING_ATTRIBUTE_HANDLING_PARENT:
                if(m_ParentStat == null)
                    return Double.NaN; // the case if for attribute i all examples gave missing values (if there is no parent stat, it means we reached the root node)
                return m_ParentStat.getSVarS(i);
            case Settings.MISSING_ATTRIBUTE_HANDLING_NONE:
                return Double.NaN;
            default:
                if(m_Training == null)
                    return Double.NaN;
                return m_Training.getSVarS(i);
        }
    }

    /**
     * Should the variance be calculated, or estimated (for example if all examples have missing values)
     * @param k_tot Total number of examples with non-missing values for i-th attribute
     * @param n_tot Total number of examples with non-missing values for all attributes
     * @return
     */
    public boolean shouldEstimate(double k_tot, double n_tot) {
    	//Condition for the old variance equation
    	//return k_tot <= 1 && k_tot != n_tot || n_tot <= MathUtil.C1E_9; //k_tot == 1 is allowed only if k_tot==n_tot
    	
    	//Condition for the new variance equation
    	return k_tot <= MathUtil.C1E_9;
    }
    
    public double getSVarS(int i) {
        double n_tot = m_SumWeight;
        double k_tot = m_SumWeights[i];
        double sv_tot = m_SumValues[i];
        double ss_tot = m_SumSqValues[i];
        
        if(shouldEstimate(k_tot, n_tot)) 
            return getEstimatedSVarS(i);
        
        return getSVarS(n_tot, k_tot, sv_tot, ss_tot);
        
        //if (k_tot <= MathUtil.C1E_9 && m_Training != null) {
        //    return m_Training.getSVarS(i);
        //}
        //else {
        //    return (k_tot > 1.0) ? ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot / k_tot * sv_tot / k_tot : 0.0;
        //}
    }
    
    public double getSVarSDiff(int i, RegressionStat or) {
    	double n_tot = m_SumWeight - or.m_SumWeight;
        double k_tot = m_SumWeights[i] - or.m_SumWeights[i];
        double sv_tot = m_SumValues[i] - or.m_SumValues[i];
        double ss_tot = m_SumSqValues[i] - or.m_SumSqValues[i];
        
        if(shouldEstimate(k_tot, n_tot))
            return or.getEstimatedSVarS(i);
        
        return getSVarS(n_tot, k_tot, sv_tot, ss_tot);
    }
    
    public double getSVarS(double n_tot, double k_tot, double sv_tot, double ss_tot) {                    
		// TODO: Jurica: This is the new equation for variance, which we agreed
		// upon meeting in Tomaz's office, below is the old "weird" equation. I
		// did not manage to test new the equation, however, in supervised case
		// it should be the same as the old one (i.e., if there is no missing values)
		// FIXME: with the new equation n_tot is not needed anymore
    	return ss_tot - sv_tot * sv_tot / k_tot;
    	
        // Old equation
        //if(k_tot == n_tot)
        //	return ss_tot - sv_tot * sv_tot / n_tot;
    	
    	//return ss_tot * (n_tot - 1) / (k_tot - 1) - n_tot * sv_tot / k_tot * sv_tot / k_tot;
   }


    public double getSVarS_ROS(ClusAttributeWeights scale) {
        double result = 0.0;
        int cnt = 0;
        for (int i = 0; i < m_NbAttrs; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            result += getSVarS(i) * scale.getWeight(m_Attrs[i]);  
        }

        return result / cnt;
    }


    public double getSVarS(ClusAttributeWeights scale) {
        if (Settings.isEnsembleROSEnabled()) return getSVarS_ROS(scale);
        if (Settings.isHMTREnabled()) return getSVarSHMTR(scale);

        double result = 0.0, var;
        int nbEffectiveAttrs = m_NbAttrs;
        for (int i = 0; i < m_NbAttrs; i++) {       
        	var = getSVarS(i);
            if (Double.isNaN(var)) {
                nbEffectiveAttrs--;
            } 
            else {
                result += var * scale.getWeight(m_Attrs[i]);
            }
        }

        if (nbEffectiveAttrs == 0) {
            return Double.POSITIVE_INFINITY;
        }

        return result / nbEffectiveAttrs;
    }

    public double getSVarSHMTR(ClusAttributeWeights scale) {

        double result = 0.0;
        for (int i = 0; i < m_NbAttrs; i++) {      	
            result = getSVarS(i)*scale.getWeight(m_Attrs[i])*m_HMTRHierarchy.getWeight(m_Attrs[i].getName());        
        }
        return result / m_NbAttrs;
    }
    
    public double getSVarSDiff_ROS(ClusAttributeWeights scale, ClusStatistic other) {
        double result = 0.0;
        int cnt = 0;
        RegressionStat or = (RegressionStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            if (!scale.getEnabled(m_Attrs[i].getIndex()))
                continue;

            cnt++;
            result += getSVarSDiff(i, or) * scale.getWeight(m_Attrs[i]);
        }
        return result / cnt;
    }

    public double getSVarSDiffHMTR(ClusAttributeWeights scale, ClusStatistic other) {

        double result = 0.0;
        RegressionStat or = (RegressionStat)other;
        for (int i = 0; i < m_NbAttrs; i++) {
            result += getSVarSDiff(i, or)*scale.getWeight(m_Attrs[i])*m_HMTRHierarchy.getWeight(m_Attrs[i].getName());   
        }
        return result / m_NbAttrs;
    }
    
    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
        if (Settings.isEnsembleROSEnabled()) return getSVarSDiff_ROS(scale, other);
        if (Settings.isHMTREnabled()) return getSVarSDiffHMTR(scale, other);

        double result = 0.0, var;
        RegressionStat or = (RegressionStat) other;

        int nbEffectiveAttrs = m_NbAttrs;
        for (int i = 0; i < m_NbAttrs; i++) {
            var = getSVarSDiff(i, or);
            if (Double.isNaN(var)) {
                nbEffectiveAttrs--;
            } else {
                result += var * scale.getWeight(m_Attrs[i]);
            }
        }

        if (nbEffectiveAttrs == 0) {
            return Double.POSITIVE_INFINITY;
        }

        return result / nbEffectiveAttrs;
    }


    public String getString(StatisticPrintInfo info) {
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0; i < m_NbAttrs; i++) {
            if (i != 0)
                buf.append(",");
            double tot = getSumWeights(i);
            if (tot == 0)
                buf.append("?");
            else
                buf.append(fr.format(getSumValues(i) / tot));
        }
        buf.append("]");
        if (info.SHOW_EXAMPLE_COUNT_BYTARGET) {
            buf.append(": [");
            for (int i = 0; i < m_NbAttrs; i++) {
                if (i != 0)
                    buf.append(",");
                buf.append(fr.format(m_SumWeights[i]));
            }
            buf.append("]");
        }
        else if (info.SHOW_EXAMPLE_COUNT) {
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
            System.out.println("n: " + n_tot + " k: " + k_tot);
            System.out.println("sv: " + sv_tot);
            System.out.println("ss: " + ss_tot);
            double mean = sv_tot / n_tot;
            double var = ss_tot - n_tot * mean * mean;
            System.out.println("mean: " + mean);
            System.out.println("var: " + var);
        }
        System.out.println("err: " + getError());
    }


    public RegressionStat getRegressionStat() {
        return this;
    }


    public double getSquaredDistance(ClusStatistic other) {
        double result = 0.0;
        RegressionStat o = (RegressionStat) other;
        for (int i = 0; i < m_NbAttrs; i++) {
            double distance = getMean(i) - o.getMean(i);
            result += distance * distance;
        }
        return result;
    }


	@Override
	public int getNbStatisticComponents() {
		return m_SumWeights.length;
	}
	
	 @Override
     public void setParentStat(ClusStatistic parent) {
         m_ParentStat = (RegressionStat) parent;
     }
     
     @Override
     public double getTargetSumWeights() {
         return m_SumWeightLabeled;
     }
     
     @Override
     public boolean samePrediction(ClusStatistic other) {
         RegressionStat rstat = (RegressionStat) other;
         
         for (int i = 0; i < m_NbAttrs; i++) {
             if(getMean(i) != rstat.getMean(i))
                 return false;
         }
         
         return true;
     }

	 @Override
	 public ClusStatistic getParentStat() {
	     return m_ParentStat;
	 }

}

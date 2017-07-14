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
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import clus.heuristic.GISHeuristic;
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
    
    //daniela
    // matejp: removed static in INITIALIZE_PARTIAL_SUM, and made every field in this daniela block private
//    private double[] m_ElementsChild; 
    private double m_I;
//    private double[] m_NodeData;
    private RowData m_Data; 
    private RowData m_TempData;
    private double[] m_SumValuesSpatial;
    private int m_SplitIndex;
    private int m_PrevIndex;
    
    private double[] m_PreviousSumW ;
    private double[] m_PreviousSumX ;
    private double[] m_PreviousSumXR ;
    private double[] m_PreviousSumWXX;
    private double[] m_PreviousSumWX ;
    private double[] m_PreviousSumX2;
    private double[] m_PreviousSumWXXR;
    private double[] m_PreviousSumWXR ;
    private double[] m_PreviousSumWR;
    private double[] m_PreviousSumX2R;
    
    private boolean INITIALIZE_PARTIAL_SUM = true;
    
    public class Distance {
        double index;
        double target;
        double distance;
        Distance(double index, double target, double distance){
            this.index=index;
            this.target=target;
            this.distance=distance;
        } 
    }
    
    public class DistanceB {
        double index;
        double target1;
        double target2;
        double distance;
        DistanceB(double index, double target1, double target2, double distance){
            this.index=index;
            this.target1=target1;
            this.target2=target2;
            this.distance=distance;
        } 
    }
    //daniela end

    public RegressionStat(NumericAttrType[] attrs) {
        this(attrs, false);
    }

    public RegressionStat(NumericAttrType[] attrs, boolean onlymean) {
        super(attrs, onlymean);
        if (!onlymean) {
            m_SumValues = new double[m_NbAttrs];
            m_SumWeights = new double[m_NbAttrs];
            m_SumSqValues = new double[m_NbAttrs];
            
            // daniela
            m_PreviousSumX = new double[2];
            m_PreviousSumXR  = new double[2];
            m_PreviousSumW   = new double[2]; 
            m_PreviousSumWXX = new double[2];
            m_PreviousSumWX  = new double[2];
            m_PreviousSumX2  = new double[2];
            
            m_PreviousSumWR   = new double[2]; 
            m_PreviousSumWXXR = new double[2];
            m_PreviousSumWXR  = new double[2];
            m_PreviousSumX2R  = new double[2];
            // end daniela
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
                m_SumSqValues[i] += weight * val * val; // TODO speed up: one step less if we save weight * val
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
    
    public double getSumSqValues(int i){
        return m_SumSqValues[i];
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

//  this is commented out since there were 0 references to both methods in Daniela's version
//    // daniela
//    public double getVarLisa() {
//        double N = m_Training.getNbExamples();
//        return 1;
//    }
//
//    public double getVarLisaDiff(ClusStatistic tstat, ClusStatistic pstat) {
//        double result = 0.0;        
//        //suma po site deca
//        //result = tstat* tstat.getVarLisa()+pstat* pstat.getVarLisa(); levo+desno na primer
//    return result / m_Training.getNbExamples();
//    // daniela end
    
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
	

	// daniela
    public void calcMeanSpatial(double[] means) {
        ClusSchema schema = m_TempData.getSchema();
        for (int k = 0; k < m_NbAttrs; k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k]; //target
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
            ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord 
            int NeighCount =3; 
            int N=(int)schema.getSettings().getMinimalNbExamples();
            for (int i = 0; i < N; i++) {
                Distance distances[]=new Distance[NeighCount];
                DataTuple exi = m_Data.getTuple(i);  //get the testing instances !!!
//                double ti = type.getNumeric(exi);
                double xi = xt.getNumeric(exi);
                double yi = yt.getNumeric(exi);
                double biggestd=Double.POSITIVE_INFINITY; 
                int biggestindex=Integer.MAX_VALUE;
                for (int j = 0; j < N; j++) {
                    DataTuple exj = m_TempData.getTuple(j); //training instances
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if (N < NeighCount) distances[j] = new Distance(j,tj,d);
                    else {
                        if (j < NeighCount) distances[j] = new Distance(j,tj,d);
                        else{
                            biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                            biggestindex = 0;
                            for (int a=1; a<NeighCount; a++)
                                if (distances[a].distance > biggestd){
                                    biggestd = distances[a].distance;
                                    biggestindex = a;
                                }
                            if (d < biggestd){
                                distances[biggestindex].index = j;
                                distances[biggestindex].target = tj;
                                distances[biggestindex].distance = d;
                            }
                        }
                    }
                }
                for (int ii = 0; ii < NeighCount; ii++) {
                    m_SumValuesSpatial[i] += distances[ii].target;  // for this test instance/example this is the prediction, but I do not know how to assign it
                }
            }
            means[k] = m_SumWeights[k] != 0.0 ? m_SumValues[k] / m_SumWeights[k] : 0.0; //old ones
        }
    }
    
    public double calcEquvalentPDistance(Integer[] permutation) {
    try{
        throw new Exception("This method shoud be implemented. Exiting...");
    }catch(Exception e){
        e.printStackTrace();
        System.exit(1);
    }
    return 0;
    }
    
    public double calcPDistance(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];      
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];  
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(permutation[j]);
                    double xj = type.getNumeric(exj);
                    long xxi = (long)xt.getNumeric(exi);        
                    long yyi = (long)xt.getNumeric(exj);    
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){                        
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }       
                        String indexMap=indexI +"#"+indexJ;
                        Double temp= GISHeuristic.m_distancesS.get(indexMap);
                        if (temp!=null) upsum[k] += (xi-means[k])*(xj-means[k]); 
                    } //else upsum[k] += (xi-means[k])*(xi-means[k]); 
                }
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }           
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=upsum[k]/downsum[k]; //I for each target         
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+" means: "+means[0]);
        double IL=avgik*(N-M);  
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        double avgikR = 0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(permutation[j]);
                    double xj = type.getNumeric(exj);
                    long xxi = (long)xt.getNumeric(exi);     
                    long yyi = (long)xt.getNumeric(exj);
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){                        
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }       
                        String indexMap=indexI +"#"+indexJ;
                        Double temp= GISHeuristic.m_distancesS.get(indexMap);
                        if (temp!=null) upsumR[k] += (xi-meansR[k])*(xj-meansR[k]); 
                    } //else upsumR[k] += (xi-meansR[k])*(xi-meansR[k]); 
                }
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k]));
            }           
            if (downsumR[k]!=0.0 && upsumR[k]!=0.0){
                ikkR[k]=upsumR[k]/downsumR[k]; //I for each target          
                avgikR+=ikkR[k]; //average of the both targets
            }else avgikR = 1; //Double.NEGATIVE_INFINITY;
        }       
        avgikR/=schema.getNbTargetAttributes(); 
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+" means: "+meansR[0]);
        double IR=avgikR;   //end right side
        double I=(IL+IR*(N-M))/m_Data.getNbRows();//System.out.println("Join Moran I: "+I);
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    
    public double calcPtotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];      
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        double avgik = 0; //Double.NEGATIVE_INFINITY
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];  
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(permutation[j]);
                    double xj = type.getNumeric(exj);                                       
                    long indexI=permutation[i];
                    long indexJ=permutation[j];
                    if (permutation[i]!=permutation[j]){
                        if (permutation[i]>permutation[j]){
                            indexI=permutation[j];
                            indexJ=permutation[i];
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) upsum[k] += (xi-means[k])*(xj-means[k]); 
                    } //else upsum[k] += (xi-means[k])*(xi-means[k]); 
                }
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }           
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=(upsum[k])/(downsum[k]); //I for each target         
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+" means: "+means[0]);
        double IL=avgik*(N-M);  
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        double avgikR = 0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(permutation[j]);
                    double xj = type.getNumeric(exj);
                    long indexI=permutation[i];
                    long indexJ=permutation[j];
                    if (permutation[i]!=permutation[j]){
                        if (permutation[i]>permutation[j]){
                            indexI=permutation[j];
                            indexJ=permutation[i];
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) upsumR[k] += (xi-meansR[k])*(xj-meansR[k]); 
                    } //else upsumR[k] += (xi-meansR[k])*(xi-meansR[k]); 
                }
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k]));
            }           
            if (downsumR[k]!=0.0 && upsumR[k]!=0.0){
                ikkR[k]=(upsumR[k])/(downsumR[k]); //I for each target          
                avgikR+=ikkR[k]; //average of the both targets
            }else avgikR = 1; //Double.NEGATIVE_INFINITY;
        }       
        avgikR/=schema.getNbTargetAttributes(); 
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+" means: "+meansR[0]);
        double IR=avgikR;   //end right side
        double I=(IL+IR*(N-M))/m_Data.getNbRows();//System.out.println("Join Moran I: "+I);
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    
    public double calcMultiIwithNeighbours(Integer[] permutation){
        ClusSchema schema = m_Data.getSchema();
        if (schema.getNbTargetAttributes()==1) {System.out.println("Error calculating Bivariate Heuristics with only one target!");System.exit(1);}     
        int M = 0;
        int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }       
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double W = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrix
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(permutation[j]); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) distances[j]=new Distance(permutation[j],tj,d);
                    else {
                        if (permutation[j]<NeighCount) distances[j]=new Distance(permutation[j],tj,d);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = permutation[j];
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    int NN;
                    if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount); 
                    
                    for (int j = M; j < NN; j++) {      
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsum[k] += w[permutation[i]][permutation[j]]*(ti-means[k])*(distances[j].target-means[k]); //m_distances.get(i*N+j) [i][permutation[j]]
                    W+=w[permutation[i]][permutation[j]];
                    }   
                downsum[k]+=((ti-means[k])*(ti-means[k]));
                }
            }       
            downsum[k]/=(N-M+0.0);
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=(upsum[k])/(W*downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        
            }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Moran Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL=avgik*(N-M);
    
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        
        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double WR = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a=0; 
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                    else {
                        if (a<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j=0;
                    while ((distances.length>j) && (distances[j]!=null) && j < NeighCount) {                             
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1;
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsumR[k] += w[permutation[i]][permutation[j]]*(ti-meansR[k])*(distances[j].target-meansR[k]); //m_distances.get(i*N+j) [permutation[i]][permutation[j]]
                    WR+=w[permutation[i]][permutation[j]];
                    j++;
                    }   
                downsumR[k]+=((ti-meansR[k])*(ti-meansR[k]));
                }
            }       
    
            downsumR[k]/=(N-M+0.0);
            if (downsumR[k]!=0.0 && upsumR[k]!=0.0){
                ikkR[k]=(upsumR[k])/(WR*downsumR[k]); //I for each target
                avgikR+=ikkR[k]; //average of the both targets
            }else avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }       
        //System.out.println("Moran Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR/=schema.getNbTargetAttributes();
        double IR=avgikR;
        //end right side
        
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        //System.out.println("Join Moran I: "+I);
        
        double scaledI=1+I; //scale I [0,2]
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    
    public double calcBivariateLee(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        if (schema.getNbTargetAttributes()==1) {System.out.println("Error calculating Bivariate Heuristics with only one target!");System.exit(1);}     
        int M = 0;int N = m_Data.getNbRows();long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[][] upsum = new double[schema.getNbTargetAttributes()][(N-M)];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                means[k] += xxi;
            }
            means[k]/=(N-M);
        }
        double avgik=0;double upsum0=0; double upsum1=1;double downsum0=0; double downsum1=0;double WL=0.0; 
        for (int i = M; i <N; i++) {
            DataTuple exi = m_Data.getTuple(permutation[i]);
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            xi[k] = type.getNumeric(exi);}
            double W=0.0; 
            for (int j =M; j < N; j++) {
                DataTuple exj = m_Data.getTuple(j);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xj[k] = type.getNumeric(exj);}
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) w=temp;else w=0;    
                        //upsum[0][permutation[i]] += w*xj[0]; upsum[1][i] += w*xj[1]; W+=w*w;   
                        upsum[0][i] += w*(xj[0]-means[0]); upsum[1][i] += w*(xj[1]-means[1]); W+=w;     
                    }  
                    else{
                        //upsum[0][i] += xi[0]; upsum[1][i] +=xi[1];W+=1; 
                        upsum[0][i] += xi[0]-means[0]; upsum[1][i] +=xi[1]-means[1];W+=1; 
                        }
                }       
                //upsum0+= (upsum[0][permutation[i]]-means[0]); upsum1+= (upsum[1][permutation[i]]-means[1]);
                WL+=W*W; 
                upsum0+= upsum[0][permutation[i]]*upsum[1][permutation[i]];
                downsum0+= (xi[0]-means[0])*(xi[0]-means[0]); downsum1+= (xi[1]-means[1])*(xi[1]-means[1]);
        }           
        if (upsum0!=0.0 && upsum1!=0.0 && downsum0!=0.0 && downsum1!=0.0)
            avgik=((N-M+0.0)*upsum0*upsum1)/(WL*Math.sqrt(downsum0*downsum1)); else avgik = 0; 
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" means: "+means[0]);
        double IL=avgik*(N-M);  
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double IR=0;double upsumR0=0; double upsumR1=1; double downsumR0=0;double downsumR1=0;double WRR=0.0; 
        double[][] upsumR = new double[schema.getNbTargetAttributes()][(N-M)];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xiR = new double[schema.getNbTargetAttributes()];
        double[] xjR = new double[schema.getNbTargetAttributes()];  
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                meansR[k] += xxi;
            }
            meansR[k]/=(N-M);
        }
        for (int i = M; i <N; i++) {
        DataTuple exi = m_Data.getTuple(permutation[i]);
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
        ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
        xiR[k] = type.getNumeric(exi);}
        double WR=0.0; 
            for (int j =M; j < N; j++) {
                DataTuple exj = m_Data.getTuple(j);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xjR[k] = type.getNumeric(exj);}
                double w=0;long indexI=i;long indexJ=j;
                if (i!=j){
                    if (i>j){indexI=j;indexJ=i;}        
                    long indexMap = indexI*(NR)+indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp;else w=0;        
                    upsumR[0][(i-M)] += w*(xjR[0]-meansR[0]); upsumR[1][(i-M)] += w*(xjR[1]-meansR[1]); WR+=w; 
                }  
                else{upsumR[0][(i-M)] += xiR[0]-meansR[0]; upsumR[1][(i-M)] +=xiR[1]-meansR[1];WR+=1; }
            }       
            WRR+=WR*WR; 
            upsumR0+= upsumR[0][(i-M)]*upsumR[1][(i-M)];        
            downsumR0+= (xiR[0]-meansR[0])*(xiR[0]-meansR[0]); downsumR1+= (xiR[1]-meansR[1])*(xiR[1]-meansR[1]);
        }           
        if (upsumR0!=0.0 && upsumR1!=0.0 && downsumR0!=0.0 && downsumR1!=0.0)
            IR=((N-M+0.0)*upsumR0*upsumR1)/(WRR*Math.sqrt(downsumR0*downsumR1)); else IR = 0; 
        //System.out.println("Right Moran I: "+IR+"ex: "+(N-M)+"WR "+WR+" means: "+meansR[0]);      
        
        double I=(IL+IR*(N-M))/NR;
        //System.out.println("Join Moran I: "+I);
        double scaledI=1+I; //scale I [0,2]
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    public double calcLeewithNeighbours(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        if (schema.getNbTargetAttributes()==1) {System.out.println("Error calculating Bivariate Heuristics with only one target!");System.exit(1);}     
        int M = 0;int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[][] upsum = new double[schema.getNbTargetAttributes()][(N-M)];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double m = type.getNumeric(exi);
                means[k] += m;
            }
            means[k]/=(N-M);
        }
        double avgik=0;double upsum0=0; double upsum1=1;double downsum0=0; double downsum1=0;double WL=0.0;         
        int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
        int spatialMatrix = schema.getSettings().getSpatialMatrix();int NN;
        if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount); 
        if (NeighCount>0){
            double[][] w = new double[N][N]; //matrica  
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double yyi=0.0;double xxi=0.0;double W=0;
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k]; //target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord         
                    xi[k] = type.getNumeric(exi);xxi = xt.getNumeric(exi);yyi = yt.getNumeric(exi);}
                double biggestd=Double.POSITIVE_INFINITY;int biggestindex=Integer.MAX_VALUE;
                DistanceB distances[]=new DistanceB[NeighCount];double yyj=0.0;double xxj=0.0;  
                
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];//target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord     
                    xj[k] = type.getNumeric(exj);xxj = xt.getNumeric(exi);yyj = yt.getNumeric(exi);}
                    double d = Math.sqrt((xxi-xxj)*(xxi-xxj)+(yyi-yyj)*(yyi-yyj));  
                    if ((N-M)<NeighCount) distances[j]=new DistanceB(j,xj[0],xj[1],d);
                    else {
                        if (j<NeighCount) distances[j]=new DistanceB(j,xj[0],xj[1],d);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                                {biggestd = distances[a].distance;  biggestindex = a;}
                             if (d < biggestd)
                             {
                             distances[biggestindex].index = j; distances[biggestindex].distance = d;
                             distances[biggestindex].target1 = xj[0]; distances[biggestindex].target2 = xj[1];
                             }
                          }
                        }
                    }
                    
                    for (int j = M; j < NN; j++) {      
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }
                    upsum[0][permutation[i]] += w[permutation[i]][permutation[j]]*(xj[0]-means[0]); upsum[1][permutation[i]] += w[permutation[i]][permutation[j]]*(xj[1]-means[1]); W+=w[permutation[i]][permutation[j]];
                    }
                    
                    WL+=W*W; 
                    upsum0+= upsum[0][permutation[i]]*upsum[1][permutation[i]];
                    downsum0+= (xi[0]-means[0])*(xi[0]-means[0]); downsum1+= (xi[1]-means[1])*(xi[1]-means[1]);
                }
            }       
        if (upsum0!=0.0 && upsum1!=0.0 && downsum0!=0.0 && downsum1!=0.0)
            avgik=((N-M+0.0)*upsum0*upsum1)/(WL*Math.sqrt(downsum0*downsum1)); else avgik = 0; 
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+WL+" means: "+means[0]);
        double IL=avgik*(N-M);  
    
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[][] upsumR = new double[schema.getNbTargetAttributes()][(N-M)];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xRi = new double[schema.getNbTargetAttributes()];
        double[] xRj = new double[schema.getNbTargetAttributes()];                  
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double m = type.getNumeric(exi);
                meansR[k] += m;
            }
            meansR[k]/=(N-M);
        }
        double avgikR=0;double upsumR0=0; double upsumR1=1;double downsumR0=0; double downsumR1=0;double WRR=0.0;       
        if (NeighCount>0){
            double[][] w = new double[N][N]; //matrica  
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double yyi=0.0;double xxi=0.0;double WR=0;
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k]; //target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord         
                    xRi[k] = type.getNumeric(exi);xxi = xt.getNumeric(exi);yyi = yt.getNumeric(exi);}
                double biggestd=Double.POSITIVE_INFINITY;int biggestindex=Integer.MAX_VALUE; int a=0;
                DistanceB distances[]=new DistanceB[NeighCount];double yyj=0.0;double xxj=0.0;  
                
                for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];//target
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord     
                    xRj[k] = type.getNumeric(exj);xxj = xt.getNumeric(exi);yyj = yt.getNumeric(exi);}
                    double d = Math.sqrt((xxi-xxj)*(xxi-xxj)+(yyi-yyj)*(yyi-yyj)); 
                    
                    if ((N-M)<NeighCount) {distances[a]=new DistanceB(j,xRj[0],xRj[1],d); a++;}
                    else {
                        if (a<NeighCount) {distances[a]=new DistanceB(j,xRj[0],xRj[1],d); a++;}  
                        else{
                         biggestindex = 0; biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         for (a=1; a<NeighCount; a++)
                            if (distances[a].distance > biggestd)
                                {biggestd = distances[a].distance; biggestindex = a;}
                            if (d < biggestd)
                            {distances[biggestindex].index = j; distances[biggestindex].distance = d;
                             distances[biggestindex].target1 = xRj[0]; distances[biggestindex].target2 = xRj[1];}
                         }
                    }
                }
    
                int j=0;
                while ((distances.length>j) && (distances[j]!=null) && j < NeighCount) {                             
                if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1;
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                         }
                        }               
                    upsumR[0][permutation[i]] += w[permutation[i]][permutation[j]]*(xRj[0]-meansR[0]); upsumR[1][permutation[i]] += w[permutation[i]][permutation[j]]*(xRj[1]-meansR[1]); WR+=w[permutation[i]][permutation[j]];
                }
                WRR+=WR*WR; 
                upsumR0+= upsumR[0][permutation[i]]*upsumR[1][permutation[i]];
                downsumR0+= (xRi[0]-meansR[0])*(xRi[0]-meansR[0]); downsumR1+= (xRi[1]-meansR[1])*(xRi[1]-meansR[1]);
            }
        }       
        if (upsumR0!=0.0 && upsumR1!=0.0 && downsumR0!=0.0 && downsumR1!=0.0)
            avgikR=((N-M+0.0)*upsumR0*upsumR1)/(WRR*Math.sqrt(downsumR0*downsumR1)); else avgikR = 0; 
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+"WR "+WRR+" means: "+meansR[0]);
        double IR=avgikR*(N-M); 
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        //System.out.println("Join Moran I: "+I);
        
        double scaledI=1+I; //scale I [0,2]
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    
    public double calcMutivariateItotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        if (schema.getNbTargetAttributes()==1) {System.out.println("Error calculating Bivariate Heuristics with only one target!");System.exit(1);}     
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double upsum = 0.0; double downsumAll = 1.0;double ikk = 0.0; double W = 0.0;
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] xi = new double[schema.getNbTargetAttributes()];
        double[] xj = new double[schema.getNbTargetAttributes()];           
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                means[k] += xxi;
            }
            means[k]/=(N-M);
        }
        for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xi[k] = type.getNumeric(exi);}
                for (int j =M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xj[k] = type.getNumeric(exj);}
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }       
                    long indexMap = indexI*(NR)+indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp;else w=0;    
                    upsum+= w*(xi[0]-means[0])*(xj[1]-means[1]); 
                    W+=w;   
                    }  
                }       
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) 
                    downsum[k]+=((xi[k]-means[k])*(xi[k]-means[k]));
        } 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) 
            downsumAll*=downsum[k];             
        if (downsumAll>0.0 && upsum!=0.0){
            ikk=((N-M+0.0)*upsum)/(W*Math.sqrt(downsumAll));        
        }else ikk = 0; //Double.NEGATIVE_INFINITY;
        //System.out.println("Moran Left I:"+ikk+" up "+upsum+" d "+downsumAll+" MN "+(N-M));   //I for each target 
        double IL=ikk*(N-M);    
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double upsumR = 0.0; double downsumAllR = 1.0;double ikkR = 0.0; double WR = 0.0;
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] xiR = new double[schema.getNbTargetAttributes()];
        double[] xjR = new double[schema.getNbTargetAttributes()];          
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xxi = type.getNumeric(exi);
                meansR[k] += xxi;
            }
            meansR[k]/=(N-M);
        }
        for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                xiR[k] = type.getNumeric(exi);}
                for (int j =M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    for (int k = 0; k < schema.getNbTargetAttributes(); k++){
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    xjR[k] = type.getNumeric(exj);}
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }       
                    long indexMap = indexI*(NR)+indexJ;
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp;else w=0;    
                    upsumR+= w*(xiR[0]-meansR[0])*(xjR[1]-meansR[1]); 
                    WR+=w;      
                    }  
                }       
                for (int k = 0; k < schema.getNbTargetAttributes(); k++) 
                    downsumR[k]+=((xiR[k]-meansR[k])*(xiR[k]-meansR[k]));
        } 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) 
            downsumAllR*=downsumR[k];               
        if (downsumAllR>0.0 && upsumR!=0.0){
            ikkR=((N-M+0.0)*upsumR)/(WR*Math.sqrt(downsumAllR));        
        }else ikkR = 0; //Double.NEGATIVE_INFINITY;
        //System.out.println("Moran R I:"+ikkR+" up "+upsumR+" d "+downsumAllR+" MN "+(N-M));   //I for each target 
        double IR=ikkR; //end right side
        double I=(IL+IR*(N-M))/N;
        //System.out.println("Join Moran I: "+I);
        //if (I>1) I=1;
        //if (I<-1) I=-1;   
        //scale I [0,2]
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    
    //Connectivity Index (CI) for Graph Data with a distance file
    public double calcCItotalD(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
        int M = 0;
        int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] D = new double[(N-M)];
        double[] W = new double[(N-M)];
        double[][] w=new double [(N-M)][(N-M)];
        double[] ikk = new double[schema.getNbTargetAttributes()];      
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i <N; i++) {
            DataTuple exi = m_Data.getTuple(permutation[i]);
            long xxi = (long)xt.getNumeric(exi);    
                for (int j =M; j < N; j++) {
                DataTuple exj = m_Data.getTuple(j);
                long yyi = (long)xt.getNumeric(exj);    
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w[permutation[i]][permutation[j]]=temp;else w[permutation[i]][permutation[j]]=0;
                    D[permutation[i]]+=w[permutation[i]][permutation[j]]; //za sekoj node
                    }  
                }
            }
            for (int i = M; i <N; i++) {
                for (int j = M; j <N; j++) 
                    if (i!=j && D[permutation[j]]!=0) W[permutation[i]]+=Math.sqrt(D[permutation[j]]); 
                if (D[permutation[i]]!=0) ikk[k]+=Math.sqrt(D[permutation[i]]);
            }                   
                avgik+=ikk[k]; //average of the all targets
        }       
        avgik/=schema.getNbTargetAttributes(); 
        //System.out.println("Left Moran I: "+ikk[0]+"ex: "+(N-M));
        double IL=avgik; //*(N-M);  
        if (Double.isNaN(IL)) IL=0.0;
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex; double IR = 0; 
        double[] DR = new double[(N-M)]; double[] WR = new double[(N-M)];
        double[][] wr=new double [(N-M)][(N-M)];
        double[] ikkR = new double[schema.getNbTargetAttributes()];                         
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i <N; i++) {
            DataTuple exi = m_Data.getTuple(permutation[i]);
            long xxi = (long)xt.getNumeric(exi);    
                for (int j =M; j < N; j++) {                
                DataTuple exj = m_Data.getTuple(j);
                long yyi = (long)xt.getNumeric(exj);    
                long indexI=xxi;
                long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null && DR[(i-M)]!=0) wr[(i-M)][(j-M)]=temp;else wr[(i-M)][(j-M)]=0;
                    if (DR[(i-M)]!=0) DR[(i-M)]+=wr[(i-M)][(j-M)]; //za sekoj node
                    }  
                }
            }
            for (int i = M; i <N; i++) {
                for (int j = M; j <N; j++) 
                    if (i!=j && DR[(j-M)]!=0) WR[(i-M)]+=wr[(i-M)][(j-M)]/Math.sqrt(DR[(j-M)]); 
                if (DR[(i-M)]!=0) ikkR[k]+=WR[(i-M)]/Math.sqrt(DR[(i-M)]); 
            }                   
                IR+=ikkR[k]; //average of the all targets
        }       
        IR/=schema.getNbTargetAttributes();
        if (Double.isNaN(IR)) IR=0.0;
        //System.out.println("Right Moran I: "+IR+"ex: "+(N-M));
        double I=(IL+IR)/m_Data.getNbRows();
        //System.out.println("Join Moran I: "+I+" eLeft: "+(NR-(N-M))+" eRight: "+(N-M)+" "+IL+" "+IR);     
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;                     
    }
    
    //Connectivity Index (CI) for Graph Data 
    public double calcCItotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] D = new double[(N-M)];
        double[] W = new double[(N-M)];
        double[][] w=new double [(N-M)][(N-M)];
        double[] ikk = new double[schema.getNbTargetAttributes()];      
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i <N; i++) {
                for (int j =M; j < N; j++) {                
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }   
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) w[permutation[i]][permutation[j]]=temp;else w[permutation[i]][permutation[j]]=0;
                        D[permutation[i]]+=w[permutation[i]][permutation[j]]; //za sekoj node
                    }  
                }
            }
            for (int i = M; i <N; i++) {
                for (int j = M; j <N; j++) 
                    if (i!=j && D[permutation[j]]!=0) W[permutation[i]]+=w[permutation[i]][permutation[j]]/Math.sqrt(D[permutation[j]]); 
                if (D[permutation[i]]!=0) ikk[k]+=W[permutation[i]]/Math.sqrt(D[permutation[i]]);
            }                   
                avgik+=ikk[k]; //average of the all targets
        }       
        avgik/=schema.getNbTargetAttributes(); //System.out.println("Left Moran I: "+ikk[0]+"ex: "+(N-M));
        double IL=avgik; //*(N-M);  
        if (Double.isNaN(IL)) IL=0.0;
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex; double IR = 0; 
        double[] DR = new double[(N-M)]; double[] WR = new double[(N-M)];
        double[][] wr=new double [(N-M)][(N-M)];
        double[] ikkR = new double[schema.getNbTargetAttributes()];                         
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i <N; i++) {
                for (int j =M; j < N; j++) {                
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }   
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) wr[(i-M)][(j-M)]=temp;else wr[(i-M)][(j-M)]=0;
                        DR[(i-M)]+=wr[(i-M)][(j-M)]; //za sekoj node
                    }  
                }
            }
            for (int i = M; i <N; i++) {
                for (int j = M; j <N; j++) 
                    if (i!=j && DR[(j-M)]!=0) WR[(i-M)]+=wr[(i-M)][(j-M)]/Math.sqrt(DR[(j-M)]); 
                if (DR[(i-M)]!=0) ikkR[k]+=WR[(i-M)]/Math.sqrt(DR[(i-M)]); 
            }                   
                IR+=ikkR[k]; //average of the all targets
        }       
        IR/=schema.getNbTargetAttributes();
        if (Double.isNaN(IR)) IR=0.0; //System.out.println("Right Moran I: "+IR+"ex: "+(N-M));
        double I=(IL+IR)/m_Data.getNbRows(); //System.out.println("Join Moran I: "+I+" eLeft: "+(NR-(N-M))+" eRight: "+(N-M)+" "+IL+" "+IR);
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    
    //Connectivity Index (CI) for Graph Data with neigh.
    public double calcCIwithNeighbours(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double[] D = new double[(N-M)]; double[] W = new double[(N-M)]; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                Distance distances[]=new Distance[NeighCount];
                double biggestd=Double.POSITIVE_INFINITY; 
                int biggestindex=Integer.MAX_VALUE;
                int spatialMatrix = schema.getSettings().getSpatialMatrix();
                int NN;
                if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount); 
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) distances[j]=new Distance(j,tj,d);
                    else {
                        if (j<NeighCount) distances[j]=new Distance(j,tj,d);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    
                    for (int j = M; j < NN; j++) {      
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    D[permutation[i]]+=w[permutation[i]][permutation[j]]; //za sekoj node
                    }   
                    //System.out.println(i+" end "+D[permutation[i]]);
                }
                for (int i = M; i < N; i++) {
                    //System.out.println(i+" end "+D[permutation[i]]);
                    for (int j = M; j < NN; j++) {      
                        if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                        else{
                            switch (spatialMatrix) {
                                case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                                case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                                case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                                case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                                default: w[permutation[i]][permutation[j]]=1; break;
                                }
                            }               
                        W[permutation[i]]+=w[permutation[i]][permutation[j]]/Math.sqrt(D[permutation[j]]); 
                        } 
                    ikk[k]+=W[permutation[i]]/Math.sqrt(D[permutation[i]]);
                }
            }   
        avgik+=ikk[k];
        }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left CI:"+degree[0]+" MN "+(N-M));    //I for each target 
        double IL=avgik*(N-M);
    
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double[] DR = new double[(N-M)]; double[] WR = new double[(N-M)]; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                Distance distances[]=new Distance[NeighCount];
                double biggestd=Double.POSITIVE_INFINITY; 
                int biggestindex=Integer.MAX_VALUE;
                int spatialMatrix = schema.getSettings().getSpatialMatrix();
    
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a=0; 
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                    else {
                        if (a<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    int j=0;
                    while (j < NeighCount) {        
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1;
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    DR[permutation[i]]+=w[permutation[i]][permutation[j]]; //za sekoj node
                    }   
                }
                for (int i = M; i < N; i++) {
                    for (int j = M; j < N; j++) {       
                        if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                        else{
                            switch (spatialMatrix) {
                                case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                                case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                                case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                                case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                                default: w[permutation[i]][permutation[j]]=1; break;
                                }
                            }               
                        WR[permutation[i]]+=w[permutation[i]][permutation[j]]/Math.sqrt(DR[permutation[j]]); 
                        } 
                    ikkR[k]+=WR[permutation[i]]/Math.sqrt(DR[permutation[i]]);
                }   
            }   
            avgikR+=ikkR[k];
        }       
        avgikR/=schema.getNbTargetAttributes();     
        double IR=avgikR;
        //System.out.println("Right CI:"+avgikR+" MN "+(N-M));  //I for each target 
        //end right side        
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        //System.out.println("Join CI: "+I);
        
        double scaledI=1+I; //scale I [0,2]
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    //Geary C wit neigh.
    public double calcCwithNeighbourstotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }       
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double W = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) distances[j]=new Distance(j,tj,d);
                    else {
                        if (j<NeighCount) distances[j]=new Distance(j,tj,d);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    int NN;
                    if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount); 
                    
                    for (int j = M; j < NN; j++) {      
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsum[k] += w[permutation[i]][permutation[j]]*(ti-distances[j].target)*(ti-distances[j].target); 
                    W+=w[permutation[i]][permutation[j]];
                    }   
                downsum[k]+=((ti-means[k])*(ti-means[k]));
                }
            }       
            downsum[k]/=(N-M-1+0.0);
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=(upsum[k])/(2*W*downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        
            }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Geary Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL=avgik*(N-M);
    
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        
        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double WR = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a=0; 
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                    else {
                        if (a<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j=0;
                    while ((distances.length>j) && (distances[j]!=null) && j < NeighCount) {                             
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1;
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsumR[k] += w[permutation[i]][permutation[j]]*(ti-distances[j].target)*(ti-distances[j].target); 
                    WR+=w[permutation[i]][permutation[j]];
                    j++;
                    }   
                downsumR[k]+=((ti-meansR[k])*(ti-meansR[k]));
                }
            }       
    
            downsumR[k]/=(N-M-1+0.0);
            if (downsumR[k]!=0.0 && upsumR[k]!=0.0){
                ikkR[k]=(upsumR[k])/(2*WR*downsumR[k]); //I for each target
                avgikR+=ikkR[k]; //average of the both targets
            }else avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }       
        //System.out.println("Geary Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR/=schema.getNbTargetAttributes();
        double IR=avgikR;
        
        double I=2-((IL+IR*(N-M))/m_Data.getNbRows());//scaledG=2-G [0,2] 
        //System.out.println("Join Geary G: "+I);       
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;       
    }
    
    //Moran Global I with neigh.
    public double calcIwithNeighbourstotal(Integer[] permutation) {
        //calcLeewithNeighbours
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }       
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double W = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) distances[j]=new Distance(j,tj,d);
                    else {
                        if (j<NeighCount) distances[j]=new Distance(j,tj,d);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    //Privatedistances[j] = distances[j];
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    int NN;
                    if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount); 
                    
                    for (int j = M; j < NN; j++) {      
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1; 
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsum[k] += w[permutation[i]][permutation[j]]*(ti-means[k])*(distances[j].target-means[k]); //m_distances.get(i*N+j) [permutation[i]][permutation[j]]
                    W+=w[permutation[i]][permutation[j]];
                    }   
                downsum[k]+=((ti-means[k])*(ti-means[k]));
                }
            }       
            downsum[k]/=(N-M+0.0);
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=(upsum[k])/(W*downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        
            }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Moran Left I:"+avgik+" up "+upsum[0]+" down "+downsum[0]+" MN "+(N-M));   //I for each target 
        double IL=avgik*(N-M);
    
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        
        double avgikR = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); 
            double WR = 0.0; 
            if (NeighCount>0)
            {
                double[][] w = new double[N][N]; //matrica  
                for (int i = M; i < N; i++) {
                    Distance distances[]=new Distance[NeighCount];
                    DataTuple exi = m_Data.getTuple(permutation[i]);  //example i
                    ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                    ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                    double ti = type.getNumeric(exi);
                    double xi = xt.getNumeric(exi);
                    double yi = yt.getNumeric(exi);
                    int a=0; 
                    double biggestd=Double.POSITIVE_INFINITY; 
                    int biggestindex=Integer.MAX_VALUE;
                    for (int j = M; j < N; j++) {
                    DataTuple exj = m_Data.getTuple(j); //example j     
                    double tj = type.getNumeric(exj);       
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double d = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if ((N-M)<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                    else {
                        if (a<NeighCount) {distances[a]=new Distance(j,tj,d); a++;}
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (a=1; a<NeighCount; a++)
                             if (distances[a].distance > biggestd)
                             {biggestd = distances[a].distance;
                              biggestindex = a;
                             }
                            if (d < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = d;
                            }
                            }
                        }
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                    //int NN;
                    //if ((N-M)<NeighCount) NN= N-M; else NN= (M+NeighCount);   
                    //M NN
                    int j=0;
                    while ((distances.length>j) && (distances[j]!=null) && j < NeighCount) {                             
                    if (distances[j].distance==0.0) w[permutation[i]][permutation[j]]=1;
                    else{
                        switch (spatialMatrix) {
                            case 0:  w[permutation[i]][permutation[j]]=1;   break;  //binary 
                            case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                            case 2:  w[permutation[i]][permutation[j]]=(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount))*(1-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break; //modified
                            case 3:  w[permutation[i]][permutation[j]]=Math.exp(-(distances[j].distance*distances[j].distance)/(NeighCount*NeighCount)); break;  //gausian
                            default: w[permutation[i]][permutation[j]]=1; break;
                            }
                        }               
                    upsumR[k] += w[permutation[i]][permutation[j]]*(ti-meansR[k])*(distances[j].target-meansR[k]); //m_distances.get(i*N+j) [permutation[i]][permutation[j]]
                    WR+=w[permutation[i]][permutation[j]];
                    j++;
                    }   
                downsumR[k]+=((ti-meansR[k])*(ti-meansR[k]));
                }
            }       
    
            downsumR[k]/=(N-M+0.0);
            if (downsumR[k]!=0.0 && upsumR[k]!=0.0){
                ikkR[k]=(upsumR[k])/(WR*downsumR[k]); //I for each target
                avgikR+=ikkR[k]; //average of the both targets
            }else avgikR = 1; //Double.NEGATIVE_INFINITY;       
        }       
        //System.out.println("Moran Right I:"+ikkR[0]+" up "+upsumR[0]+" down "+downsumR[0]+" MN "+(N-M));  //I for each target 
        avgikR/=schema.getNbTargetAttributes();
        double IR=avgikR;
        //end right side
        
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        //System.out.println("Join Moran I: "+I);
        
        double scaledI=1+I; //scale I [0,2]
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    //calculate EquvalentI with neighbours
    public double calcEquvalentIwithNeighbourstotal() {
        ClusSchema schema = m_Data.getSchema();     
//        double[] a = new double[schema.getNbTargetAttributes()]; 
//        double[] b = new double[schema.getNbTargetAttributes()];
//        double[] c = new double[schema.getNbTargetAttributes()];
//        double[] d = new double[schema.getNbTargetAttributes()];
//        double[] e = new double[schema.getNbTargetAttributes()];        
//        double[] ikk = new double[schema.getNbTargetAttributes()];
        
        double avgik = 0;   
//        int M = 0;
//        int N = 0;
//        int NR = m_Data.getNbRows();
//        int vkupenBrojElementiVoOvojSplit = 0;
//        int vkupenBrojElementiVoCelataSuma = 0;
        if (m_SplitIndex>0){ 
//            N=m_SplitIndex;
//            M=m_PrevIndex;
//            vkupenBrojElementiVoCelataSuma = NR;
//            vkupenBrojElementiVoOvojSplit=N-M;          
        }else{
//             M=-m_SplitIndex;
//             vkupenBrojElementiVoOvojSplit=N-M;
        }
    /*  for (int k = 0; k < schema.getNbTargetAttributes(); k++) {          
            a[k]=m_PreviousSumW[k]; 




            b[k]=m_PreviousSumWXX[k];
            c[k]=m_PreviousSumWX[k]; 
            d[k]=m_PreviousSumX[k]; 
            e[k]=m_PreviousSumX2[k];
            int NeighCount = (int)schema.getSettings().getNumNeightbours(); //30; 
            double[][] w = new double[N][N]; //matrica      
            double W = 0.0; 
            if (NeighCount>0)
            {           
            for (int i = M; i < N; i++) {
                Distance distances[]=new Distance[NeighCount];
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double ti = type.getNumeric(exi);
                ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];  //x coord
                ClusAttrType yt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[1]; //y coord
                double xi = xt.getNumeric(exi);
                double yi = yt.getNumeric(exi);
                double biggestd=Double.POSITIVE_INFINITY; 
                int biggestindex=Integer.MAX_VALUE;
                d[k] += xi;
                e[k] += xi*xi;
                for (int j = M; j <N; j++) {    
                    DataTuple exj = m_Data.getTuple(j);
                    double tj = type.getNumeric(exj);                   
                    double xj = xt.getNumeric(exj);
                    double yj = yt.getNumeric(exj);
                    double tempd = Math.sqrt((xi-xj)*(xi-xj)+(yi-yj)*(yi-yj));  
                    if (vkupenBrojElementiVoOvojSplit<NeighCount) distances[j]=new Distance(j,tj,tempd);
                    else {
                        if (j<NeighCount) distances[j]=new Distance(j,tj,tempd);
                        else{
                         biggestd = distances[0].distance; // go through the knn list and replace the biggest one if possible
                         biggestindex = 0;
                         for (int aa=1; aa<NeighCount; aa++)
                             if (distances[aa].distance > biggestd)
                             {biggestd = distances[aa].distance;
                              biggestindex = aa;
                             }
                            if (tempd < biggestd)
                            {
                             distances[biggestindex].index = j;
                             distances[biggestindex].target = tj;
                             distances[biggestindex].distance = tempd;
                            }
                            }
                        //System.out.println("index,dist: "+j+", "+d);
                        }
                    }
                    int spatialMatrix = schema.getSettings().getSpatialMatrix();
                
                    for (int j = M; j <N; j++) {
                    DataTuple exj = m_Data.getTuple(j); 
                    double tj = type.getNumeric(exj);
                    if (i==j) w[permutation[i]][permutation[j]]=0; 
                    else if ((distances[j].distance==0.0) && (i!=j)) w[permutation[i]][permutation[j]]=1; 
                        else{
                                switch (spatialMatrix) {
                                case 0:  w[permutation[i]][permutation[j]]=0;   break;  //binary 
                                case 1:  w[permutation[i]][permutation[j]]=1/distances[j].distance; break;  //euclidian
                                default: w[permutation[i]][permutation[j]]=0; break;
                                }
                             W+=w[permutation[i]][permutation[j]];
                            }               
                        a[k] += 2*w[permutation[i]][permutation[j]];
                        b[k] += 2*w[permutation[i]][permutation[j]]*ti*tj;
                        c[k] += w[permutation[i]][permutation[j]]*tj;
                        c[k] += w[permutation[i]][permutation[j]]*tj;
                    }   
    
                m_PreviousSumW[k]  =a[k]; 




                m_PreviousSumWXX[k]=b[k];
                m_PreviousSumWX[k] =c[k];
                m_PreviousSumX[k]  =d[k];
                m_PreviousSumX2[k] =e[k];
                }
            }
            if (a[k]!=0.0 && e[k]!=(d[k]/(vkupenBrojElementiVoCelataSuma))){
                ikk[k]=(vkupenBrojElementiVoCelataSuma)*(b[k] - (2*d[k]*c[k]/(vkupenBrojElementiVoCelataSuma)) + (d[k]*d[k]*a[k]/((vkupenBrojElementiVoCelataSuma)*(vkupenBrojElementiVoCelataSuma))))/(a[k]*(e[k]-(d[k]/(vkupenBrojElementiVoCelataSuma)))); //I for each target
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; //Double.NEGATIVE_INFINITY;
        }       
    */
        avgik/=schema.getNbTargetAttributes();
        double I=avgik;
        if (I>1) I=1;
        if (I<-1) I=-1; 
        //scale I [0,2]
        double scaledI=1-I;
        //System.out.println("scaledI: "+scaledI+"I: "+I);
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;         
    }
    //calculate Equvalent Geary C with Distance file
    public double calcEquvalentGDistance(Integer[] permutation) {        
        ClusSchema schema = m_Data.getSchema();             
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];       
        double avgik = 0;double avgikR=0; int M = 0;int N = 0;int NR = m_Data.getNbRows(); double I=0;
        int vkupenBrojElementiVoOvojSplit = N-M;int vkupenBrojElementiVoCelataSuma = NR;        
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            M=m_PrevIndex; N=m_SplitIndex;  
            if(INITIALIZE_PARTIAL_SUM){ 
                INITIALIZE_PARTIAL_SUM=false;
                M=0;
                for (int i = M; i < NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);    
                meansR[k] += xi;
                m_PreviousSumX2R[k] +=xi*xi;
                m_PreviousSumXR[k]+=xi;
                for (int j = M; j <NR; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);                
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;
                    m_PreviousSumWR[k]+=w;
                    m_PreviousSumWXXR[k] +=w*(xi-xj)*(xi-xj);
                    }
                    else {m_PreviousSumWR[k]+=1;m_PreviousSumWXXR[k] +=(xi-xj)*(xi-xj);}
                }
                }
            }
            //else
            boolean flagRightAllEqual=true;
            boolean flagLeftAllEqual=true;
            {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
                m_PreviousSumX2[k] +=xi*xi;
                m_PreviousSumX[k]+=xi;
                meansR[k] -= xi;
                m_PreviousSumX2R[k] -=xi*xi;
                m_PreviousSumXR[k]-=xi;
            }
            
            //left (0-old)(old-new)
            flagLeftAllEqual=true;
            double oldX=type.getNumeric(m_Data.getTuple(0));
            for (int i = 1; i<N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
            if(xi!=oldX) // to check that partition does not contain values which are all the same
                {
                flagLeftAllEqual=false;
                break;
                }
            }
            
            for (int i = 0; i<M; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                for (int j = M; j<N; j++) {
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);   
                long yyi = (long)xt.getNumeric(exj);
                double w=0;
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;
                m_PreviousSumW[k]+=w;
                m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);
                }
                else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=(xi-xj)*(xi-xj);}
                }
            }
            //left (old-new)(0-old)
            for (int i = M; i<N; i++) {
                for (int j = 0; j<M; j++) {
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;
                m_PreviousSumW[k]+=w;
                m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);
                }
                else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=(xi-xj)*(xi-xj);}
                }
            }
            //left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);           
                for (int j = M; j <N; j++) {    
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long xxi = (long)xt.getNumeric(exi);        
                    long yyi = (long)xt.getNumeric(exj);
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){    
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;
                    m_PreviousSumW[k]+=w;
                    m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);  
                    }
                    else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=(xi-xj)*(xi-xj);}
                }
            }
    
            //right side (new-end)(old-new) 
            flagRightAllEqual=true;
            oldX=type.getNumeric(m_Data.getTuple(N));
            for (int i = N; i<NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                if(oldX!=xi)
                    flagRightAllEqual=false;
                for (int j = M; j<N; j++) {
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);               
                if(xi!=oldX)
                    flagRightAllEqual=false;
                
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;

                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                }
                else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=(xi-xj)*(xi-xj);}
                }
            }
    
            //right (old-new)(new-end)
            for (int i = M; i<N; i++) {
                for (int j = N; j<NR; j++) {                
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;   
                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                }
                else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=(xi-xj)*(xi-xj);}
                }
            }
            //right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);       
                long xxi = (long)xt.getNumeric(exi);                
                for (int j = M; j <N; j++) {                        
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   
                    m_PreviousSumWR[k]-=w;
                    m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                    }
                    else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=(xi-xj)*(xi-xj);}
                }
            }
            }
            
            //System.out.println("Update SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
    
            vkupenBrojElementiVoOvojSplit=N;
            num[k]=((vkupenBrojElementiVoOvojSplit-1)* m_PreviousSumWXX[k]);
            den[k]=(2*m_PreviousSumW[k]*(m_PreviousSumX2[k]-vkupenBrojElementiVoOvojSplit*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)));
            if(den[k]!=0 && num[k]!=0 && !flagLeftAllEqual) ikk[k]=num[k]/den[k]; //I for each target
                else ikk[k]= 0;
            
            vkupenBrojElementiVoOvojSplit=NR-N;
            numR[k]=((vkupenBrojElementiVoOvojSplit-1)*m_PreviousSumWXXR[k]);
            denR[k]=(2*m_PreviousSumWR[k]*(m_PreviousSumX2R[k]-(((m_PreviousSumXR[k]*m_PreviousSumXR[k]))/vkupenBrojElementiVoOvojSplit)));
            if(denR[k]!=0 && numR[k]!=0 && !flagRightAllEqual) ikkR[k]=numR[k]/denR[k]; //I for each target
                else ikkR[k]=0;         
    
        avgikR+=ikkR[k]; 
        avgik+=ikk[k];
        //System.out.println("Left Geary C: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(m_SplitIndex)+" W: "+m_PreviousSumW[0]+" wx:"+m_PreviousSumWX[0]+" wxx:"+m_PreviousSumWXX[0]+" xx:"+m_PreviousSumX2[0]);
        //System.out.println("Right Geary C: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-m_SplitIndex)+" WR "+m_PreviousSumWR[0]+" wxR: "+m_PreviousSumWXR[0]+" wxx "+m_PreviousSumWXXR[0]+" xx:"+m_PreviousSumX2R[0]);
    
        }//targets
        avgik/=schema.getNbTargetAttributes();
        avgikR/=schema.getNbTargetAttributes();     
        
        I=(avgik*N+avgikR*(NR-N))/vkupenBrojElementiVoCelataSuma;
        M=m_PrevIndex; N=m_SplitIndex;  
        double scaledI=2-I;
        if (Double.isNaN(scaledI)){
            System.out.println("err!");
            System.exit(-1);
        }
        //System.out.println(scaledI);
        return scaledI;
        }
    //calculate EquvalentI with Distance file
    public double calcEquvalentIDistance(Integer[] permutation) {        
        ClusSchema schema = m_Data.getSchema();             
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];       
        double avgik = 0;double avgikR=0; int M = 0;int N = 0;int NR = m_Data.getNbRows(); double I=0;
        int vkupenBrojElementiVoOvojSplit = N-M;int vkupenBrojElementiVoCelataSuma = NR;        
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];
            M=m_PrevIndex; N=m_SplitIndex;  
            if(INITIALIZE_PARTIAL_SUM){ 
                INITIALIZE_PARTIAL_SUM=false;
                M=0;
                for (int i = M; i < NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);    
                meansR[k] += xi;
                m_PreviousSumX2R[k] +=xi*xi;
                m_PreviousSumXR[k]+=xi;
                for (int j = M; j <NR; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);                
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;
                    //System.out.println("init: "+indexI +"#"+indexJ+" "+w);
                    m_PreviousSumWR[k]+=w;
                    m_PreviousSumWXXR[k] +=w*xi*xj;
                    m_PreviousSumWXR[k] +=w*xj;   
                    }
                    else {m_PreviousSumWR[k]+=1;m_PreviousSumWXXR[k] +=xi*xj;m_PreviousSumWXR[k] +=xj;}
                }
                }
                //System.out.println("Init SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
                //System.out.println("Init SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
            }
            //else
            boolean flagRightAllEqual=true;
            boolean flagLeftAllEqual=true;
            {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
                m_PreviousSumX2[k] +=xi*xi;
                m_PreviousSumX[k]+=xi;
                meansR[k] -= xi;
                m_PreviousSumX2R[k] -=xi*xi;
                m_PreviousSumXR[k]-=xi;
            }
            
            //left (0-old)(old-new)
            flagLeftAllEqual=true;
            double oldX=type.getNumeric(m_Data.getTuple(0));
            for (int i = 1; i<N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
            if(xi!=oldX) // to check that partition does not contain values which are all the same
                {
                flagLeftAllEqual=false;
                break;
                }
            }
            
            for (int i = 0; i<M; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                for (int j = M; j<N; j++) {
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);   
                long yyi = (long)xt.getNumeric(exj);
                double w=0;
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;
                m_PreviousSumW[k]+=w;
                m_PreviousSumWXX[k] +=w*xi*xj;
                m_PreviousSumWX[k] +=w*xj;
                }
                else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=xi*xj;m_PreviousSumWX[k] +=xj;}
                }
            }
            //left (old-new)(0-old)
            for (int i = M; i<N; i++) {
                for (int j = 0; j<M; j++) {
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;
                m_PreviousSumW[k]+=w;
                m_PreviousSumWX[k] +=w*xj;
                m_PreviousSumWXX[k] +=w*xi*xj;
                }
                else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=xi*xj;m_PreviousSumWX[k] +=xj;}
                }
            }
            //left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);           
                for (int j = M; j <N; j++) {    
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long xxi = (long)xt.getNumeric(exi);        
                    long yyi = (long)xt.getNumeric(exj);
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){    
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;
                    m_PreviousSumW[k]+=w;


                    m_PreviousSumWX[k] +=w*xj;
                    m_PreviousSumWXX[k] +=w*xi*xj;    
                    }
                    else {m_PreviousSumW[k]+=1;m_PreviousSumWXX[k] +=xi*xj;m_PreviousSumWX[k] +=xj;}
                }
            }
    
            //right side (new-end)(old-new) 
            flagRightAllEqual=true;
            oldX=type.getNumeric(m_Data.getTuple(N));
            for (int i = N; i<NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                if(oldX!=xi)
                    flagRightAllEqual=false;
                for (int j = M; j<N; j++) {
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);               
                if(xi!=oldX)
                    flagRightAllEqual=false;
                
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;



                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*xi*xj;
                m_PreviousSumWXR[k] -=w*xj;
                }
                else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=xi*xj;m_PreviousSumWXR[k] -=xj;}
                }
            }
    
            //right (old-new)(new-end)
            for (int i = M; i<N; i++) {
                for (int j = N; j<NR; j++) {                
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);        
                long yyi = (long)xt.getNumeric(exj);
                long indexI=xxi;
                long indexJ=yyi;
                if (indexI!=indexJ){
                    if (indexI>indexJ){
                        indexI=yyi;
                        indexJ=xxi;
                    }
                String indexMap =indexI +"#"+indexJ;
                Double temp= GISHeuristic.m_distancesS.get(indexMap);
                if (temp!=null) w=temp; else w=0;   



                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*xi*xj;
                m_PreviousSumWXR[k] -=w*xj;
                }
                else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=xi*xj;m_PreviousSumWXR[k] -=xj;}
                }
            }
            //right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);       
                long xxi = (long)xt.getNumeric(exi);                
                for (int j = M; j <N; j++) {                        
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                    String indexMap =indexI +"#"+indexJ;
                    Double temp= GISHeuristic.m_distancesS.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   



                    m_PreviousSumWR[k]-=w;
                    m_PreviousSumWXXR[k] -=w*xi*xj;
                    m_PreviousSumWXR[k] -=w*xj;
                    }

                    else {m_PreviousSumWR[k]-=1;m_PreviousSumWXXR[k] -=xi*xj;m_PreviousSumWXR[k] -=xj;}
                }
            }
            }
            
            //System.out.println("Update SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
    
            vkupenBrojElementiVoOvojSplit=N;
            num[k]=vkupenBrojElementiVoOvojSplit* (m_PreviousSumWXX[k]-2*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*m_PreviousSumWX[k]+(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*m_PreviousSumW[k]);
            den[k]=m_PreviousSumW[k]*(m_PreviousSumX2[k]-vkupenBrojElementiVoOvojSplit*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit));
            if(den[k]!=0 && num[k]!=0 && !flagLeftAllEqual) ikk[k]=num[k]/den[k]; //I for each target
                else ikk[k]= 1;
            
            vkupenBrojElementiVoOvojSplit=NR-N;
            numR[k]=(vkupenBrojElementiVoOvojSplit)*(m_PreviousSumWXXR[k]-(2*m_PreviousSumWXR[k]*(m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit))+(((m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit))*m_PreviousSumWR[k]));
            denR[k]=(m_PreviousSumWR[k]*(m_PreviousSumX2R[k]-(((m_PreviousSumXR[k]*m_PreviousSumXR[k]))/vkupenBrojElementiVoOvojSplit)));
            if(denR[k]!=0 && numR[k]!=0 && !flagRightAllEqual) ikkR[k]=numR[k]/denR[k]; //I for each target
                else ikkR[k]=1;         
    
        avgikR+=ikkR[k]; 
        avgik+=ikk[k];
        //System.out.println("Left Moran I: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(m_SplitIndex)+" W: "+m_PreviousSumW[0]+" wx:"+m_PreviousSumWX[0]+" wxx:"+m_PreviousSumWXX[0]+" xx:"+m_PreviousSumX2[0]);
        //System.out.println("Right Moran I: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-m_SplitIndex)+" WR "+m_PreviousSumWR[0]+" wxR: "+m_PreviousSumWXR[0]+" wxx "+m_PreviousSumWXXR[0]+" xx:"+m_PreviousSumX2R[0]);
    
        }//targets
        avgik/=schema.getNbTargetAttributes();
        avgikR/=schema.getNbTargetAttributes();     
        
        I=(avgik*N+avgikR*(NR-N))/vkupenBrojElementiVoCelataSuma;
        M=m_PrevIndex; N=m_SplitIndex;  
        
        double scaledI=1+I;
        if (Double.isNaN(scaledI)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;
        }
    //calculate EquvalentI to Geary C -Annalisa
    public double calcEquvalentGtotal(Integer[] permutation) {   
            ClusSchema schema = m_Data.getSchema();             
            double[] ikk = new double[schema.getNbTargetAttributes()];
            double[] ikkR = new double[schema.getNbTargetAttributes()];
            double[] num = new double[schema.getNbTargetAttributes()];
            double[] den = new double[schema.getNbTargetAttributes()];
            double[] numR = new double[schema.getNbTargetAttributes()];
            double[] denR = new double[schema.getNbTargetAttributes()];
            double[] means = new double[schema.getNbTargetAttributes()];
            double[] meansR = new double[schema.getNbTargetAttributes()];       
            double avgik = 0;double avgikR=0; int M = 0;int N = 0;int NR = m_Data.getNbRows(); double I=0;
            int vkupenBrojElementiVoOvojSplit = N-M;int vkupenBrojElementiVoCelataSuma = NR;        
            
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                M=m_PrevIndex; N=m_SplitIndex;  
                if(INITIALIZE_PARTIAL_SUM){ //Annalisa: to check that you need to inizialize the partial sums
                    INITIALIZE_PARTIAL_SUM=false;
                    M=0;
                    for (int i = M; i < NR; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    meansR[k] += xi;


                    m_PreviousSumX2R[k] +=xi*xi;
                    m_PreviousSumXR[k]+=xi;
                    for (int j = M; j <NR; j++) {
                        DataTuple exj = m_Data.getTuple(j);
                        double xj = type.getNumeric(exj);
                        long indexMap = i*(NR)+j;                   
                        double w=0;
                        if (i>j)
                            indexMap= j*(NR)+i;     
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) w=temp; else w=0;   

                        m_PreviousSumWR[k]+=w;
                        m_PreviousSumWXXR[k] +=w*(xi-xj)*(xi-xj);
                    }
                    }
                    //System.out.println("Init SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
                    //System.out.println("Init SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
                }
                //else
                boolean flagRightAllEqual=true;
                boolean flagLeftAllEqual=true;
                {
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    means[k] += xi;


                    m_PreviousSumX2[k] +=xi*xi;
                    m_PreviousSumX[k]+=xi;
                    meansR[k] -= xi;


                    m_PreviousSumX2R[k] -=xi*xi;
                    m_PreviousSumXR[k]-=xi;
                }
                
                //left (0-old)(old-new)
                flagLeftAllEqual=true;
                double oldX=type.getNumeric(m_Data.getTuple(0));
                for (int i = 1; i<N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                if(xi!=oldX) // Annalisa : to check that partition does not contain values which are all the same
                    {
                    flagLeftAllEqual=false;
                    break;
                    }
                }
                
                for (int i = 0; i<M; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    for (int j = M; j<N; j++) {
                    long indexMap = i*(NR)+j;                   
                    double w=0;
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   
                    m_PreviousSumW[k]+=w;
                    m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);
                    }
                }
                //left (old-new)(0-old)
                for (int i = M; i<N; i++) {
                    for (int j = 0; j<M; j++) {
                    long indexMap = i*(NR)+j;                   
                    double w=0;
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   
                    m_PreviousSumW[k]+=w;
                    m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);
                    }
                }
                //left (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);           
                    for (int j = M; j <N; j++) {    
                        DataTuple exj = m_Data.getTuple(j);
                        double xj = type.getNumeric(exj);
                        double w=0;
                        long indexI=i;
                        long indexJ=j;
                            if (i>j){
                                indexI=j;
                                indexJ=i;
                            }
                        long indexMap = indexI*(NR)+indexJ; 
                        if (i>j)
                            indexMap= j*(NR)+i; 
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) w=temp; else w=0;   
                        m_PreviousSumW[k]+=w;

                        m_PreviousSumWXX[k] +=w*(xi-xj)*(xi-xj);              
                    }
                }
    
                //right side (new-end)(old-new) 
                flagRightAllEqual=true;
                oldX=type.getNumeric(m_Data.getTuple(N));
                for (int i = N; i<NR; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    if(oldX!=xi)
                        flagRightAllEqual=false;
                    for (int j = M; j<N; j++) {
                    long indexMap = i*(NR)+j;                   
                    double w=0;
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    
                    if(xi!=oldX)
                        flagRightAllEqual=false;
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   

                    m_PreviousSumWR[k]-=w;
                    m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                    }
                }
                //right (old-new)(new-end)
                for (int i = M; i<N; i++) {
                    for (int j = N; j<NR; j++) {
                    long indexMap = i*(NR)+j;                   
                    double w=0;
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   

                    m_PreviousSumWR[k]-=w;
                    m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                    }
                }
                //right (old-new)(old-new)
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);           
                    for (int j = M; j <N; j++) {    
                        DataTuple exj = m_Data.getTuple(j);
                        double xj = type.getNumeric(exj);
                        double w=0;
                        long indexI=i;
                        long indexJ=j;
                            if (i>j){
                                indexI=j;
                                indexJ=i;
                            }
                        long indexMap = indexI*(NR)+indexJ; 
                        if (i>j)
                            indexMap= j*(NR)+i; 
                        Double temp = GISHeuristic.m_distances.get(indexMap);
                        if (temp!=null) w=temp; else w=0;   

                        m_PreviousSumWR[k]-=w;
                        m_PreviousSumWXXR[k] -=w*(xi-xj)*(xi-xj);
                    }
                }           
                }
                
                //System.out.println("Update SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
                //System.out.println("Update SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
    
                vkupenBrojElementiVoOvojSplit=N;
                num[k]=((vkupenBrojElementiVoOvojSplit-1)*m_PreviousSumWXX[k]);
                den[k]=(2*m_PreviousSumW[k]*(m_PreviousSumX2[k]-vkupenBrojElementiVoOvojSplit*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)));
                if(den[k]!=0 && num[k]!=0 && !flagLeftAllEqual) ikk[k]=num[k]/den[k]; //I for each target
                    else ikk[k]= 1;
                
                vkupenBrojElementiVoOvojSplit=NR-N;
                numR[k]=((vkupenBrojElementiVoOvojSplit-1)*m_PreviousSumWXXR[k]); 
                denR[k]=(2*m_PreviousSumWR[k]*(m_PreviousSumX2R[k]-(((m_PreviousSumXR[k]*m_PreviousSumXR[k]))/vkupenBrojElementiVoOvojSplit)));
                if(denR[k]!=0 && numR[k]!=0 && !flagRightAllEqual) ikkR[k]=numR[k]/denR[k]; //I for each target
                    else ikkR[k]=1;         
    
            avgikR+=ikkR[k]; 
            avgik+=ikk[k];
            //System.out.println("Left Geary C: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(m_SplitIndex)+" W: "+m_PreviousSumW[0]+" wx:"+m_PreviousSumWX[0]+" wxx:"+m_PreviousSumWXX[0]+" xx:"+m_PreviousSumX2[0]);
            //System.out.println("Right Geary C: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-m_SplitIndex)+" WR "+m_PreviousSumWR[0]+" wxR: "+m_PreviousSumWXR[0]+" wxx "+m_PreviousSumWXXR[0]+" xx:"+m_PreviousSumX2R[0]);
    
            }//targets
            avgik/=schema.getNbTargetAttributes();
            avgikR/=schema.getNbTargetAttributes();     
            
            I=(avgik*N+avgikR*(NR-N))/vkupenBrojElementiVoCelataSuma;
            M=m_PrevIndex; N=m_SplitIndex;  
            double scaledI=2-I;
            if (Double.isNaN(scaledI)){
                System.out.println("err!");
                System.exit(-1);
            }
            //System.out.println(scaledI);
            return scaledI; 
        }
        
    //calculate EquvalentI to Global Moran I -Annalisa
    public double calcEquvalentItotal(Integer[] permutation) {       
        ClusSchema schema = m_Data.getSchema();             
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
        double[] num = new double[schema.getNbTargetAttributes()];
        double[] den = new double[schema.getNbTargetAttributes()];
        double[] numR = new double[schema.getNbTargetAttributes()];
        double[] denR = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];       
        double avgik = 0;double avgikR=0; int M = 0;int N = 0;int NR = m_Data.getNbRows(); double I=0;
        int vkupenBrojElementiVoOvojSplit = N-M;int vkupenBrojElementiVoCelataSuma = NR;        
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            M=m_PrevIndex; N=m_SplitIndex;  
            if(INITIALIZE_PARTIAL_SUM){ //Annalisa: to check that you need to inizialize the partial sums
                INITIALIZE_PARTIAL_SUM=false;
                M=0;
                for (int i = M; i < NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;


                m_PreviousSumX2R[k] +=xi*xi;
                m_PreviousSumXR[k]+=xi;
                for (int j = M; j <NR; j++) {
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long indexMap = i*(NR)+j;                   
                    double w=0;
                    if (i>j)
                        indexMap= j*(NR)+i;     
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   



                    m_PreviousSumWR[k]+=w;
                    m_PreviousSumWXXR[k] +=w*xi*xj;
                    m_PreviousSumWXR[k] +=w*xj;
                }
                }
                //System.out.println("Init SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
                //System.out.println("Init SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
            }
            //else
            boolean flagRightAllEqual=true;
            boolean flagLeftAllEqual=true;
            {
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;


                m_PreviousSumX2[k] +=xi*xi;
                m_PreviousSumX[k]+=xi;
                meansR[k] -= xi;


                m_PreviousSumX2R[k] -=xi*xi;
                m_PreviousSumXR[k]-=xi;
            }
            
            //left (0-old)(old-new)
            flagLeftAllEqual=true;
            double oldX=type.getNumeric(m_Data.getTuple(0));
            for (int i = 1; i<N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
            if(xi!=oldX) // Annalisa : to check that partition does not contain values which are all the same
                {
                flagLeftAllEqual=false;
                break;
                }
            }
            
            for (int i = 0; i<M; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j = M; j<N; j++) {
                long indexMap = i*(NR)+j;                   
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                
                if (i>j)
                    indexMap= j*(NR)+i; 
                Double temp = GISHeuristic.m_distances.get(indexMap);
                if (temp!=null) w=temp; else w=0;   
                m_PreviousSumW[k]+=w;


                m_PreviousSumWXX[k] +=w*xi*xj;
                m_PreviousSumWX[k] +=w*xj;
                }
            }
            //left (old-new)(0-old)
            for (int i = M; i<N; i++) {
                for (int j = 0; j<M; j++) {
                long indexMap = i*(NR)+j;                   
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                if (i>j)
                    indexMap= j*(NR)+i; 
                Double temp = GISHeuristic.m_distances.get(indexMap);
                if (temp!=null) w=temp; else w=0;   
                m_PreviousSumW[k]+=w;


                m_PreviousSumWX[k] +=w*xj;
                m_PreviousSumWXX[k] +=w*xi*xj;
                }
            }
            //left (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);           
                for (int j = M; j <N; j++) {    
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                    long indexMap = indexI*(NR)+indexJ; 
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   
                    m_PreviousSumW[k]+=w;


                    m_PreviousSumWX[k] +=w*xj;
                    m_PreviousSumWXX[k] +=w*xi*xj;                
                }
            }
    
            //right side (new-end)(old-new) 
            flagRightAllEqual=true;
            oldX=type.getNumeric(m_Data.getTuple(N));
            for (int i = N; i<NR; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                if(oldX!=xi)
                    flagRightAllEqual=false;
                for (int j = M; j<N; j++) {
                long indexMap = i*(NR)+j;                   
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                
                if(xi!=oldX)
                    flagRightAllEqual=false;
                if (i>j)
                    indexMap= j*(NR)+i; 
                Double temp = GISHeuristic.m_distances.get(indexMap);
                if (temp!=null) w=temp; else w=0;   



                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*xi*xj;
                m_PreviousSumWXR[k] -=w*xj;
                }
            }
            //right (old-new)(new-end)
            for (int i = M; i<N; i++) {
                for (int j = N; j<NR; j++) {
                long indexMap = i*(NR)+j;                   
                double w=0;
                DataTuple exj = m_Data.getTuple(j);
                double xj = type.getNumeric(exj);
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                if (i>j)
                    indexMap= j*(NR)+i; 
                Double temp = GISHeuristic.m_distances.get(indexMap);
                if (temp!=null) w=temp; else w=0;   



                m_PreviousSumWR[k]-=w;
                m_PreviousSumWXXR[k] -=w*xi*xj;
                m_PreviousSumWXR[k] -=w*xj;
                }
            }
            //right (old-new)(old-new)
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);           
                for (int j = M; j <N; j++) {    
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                    long indexMap = indexI*(NR)+indexJ; 
                    if (i>j)
                        indexMap= j*(NR)+i; 
                    Double temp = GISHeuristic.m_distances.get(indexMap);
                    if (temp!=null) w=temp; else w=0;   



                    m_PreviousSumWR[k]-=w;
                    m_PreviousSumWXXR[k] -=w*xi*xj;
                    m_PreviousSumWXR[k] -=w*xj;
                }
            }           
            }
            
            //System.out.println("Update SumLeft : sumX "+m_PreviousSumX[0]+" sumX2 "+m_PreviousSumX2[0]+" sumW "+m_PreviousSumW[0]+" sumX2W "+m_PreviousSumWXX[0]+" sumXW"+m_PreviousSumWX[0]);
            //System.out.println("Update SumRight : sumX "+m_PreviousSumXR[0]+" sumX2 "+m_PreviousSumX2R[0]+" sumW "+m_PreviousSumWR[0]+" sumX2W "+m_PreviousSumWXXR[0]+" sumXW"+m_PreviousSumWXR[0]);
    
            vkupenBrojElementiVoOvojSplit=N;
            num[k]=vkupenBrojElementiVoOvojSplit* (m_PreviousSumWXX[k]-2*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*m_PreviousSumWX[k]+(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*m_PreviousSumW[k]);
            den[k]=m_PreviousSumW[k]*(m_PreviousSumX2[k]-vkupenBrojElementiVoOvojSplit*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumX[k]/vkupenBrojElementiVoOvojSplit));
            if(den[k]!=0 && num[k]!=0 && !flagLeftAllEqual) ikk[k]=num[k]/den[k]; //I for each target
                else ikk[k]= 1;
            
            vkupenBrojElementiVoOvojSplit=NR-N;
            numR[k]=(vkupenBrojElementiVoOvojSplit)*(m_PreviousSumWXXR[k]-(2*m_PreviousSumWXR[k]*(m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit))+(((m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit)*(m_PreviousSumXR[k]/vkupenBrojElementiVoOvojSplit))*m_PreviousSumWR[k]));
            denR[k]=(m_PreviousSumWR[k]*(m_PreviousSumX2R[k]-(((m_PreviousSumXR[k]*m_PreviousSumXR[k]))/vkupenBrojElementiVoOvojSplit)));
            if(denR[k]!=0 && numR[k]!=0 && !flagRightAllEqual) ikkR[k]=numR[k]/denR[k]; //I for each target
                else ikkR[k]=1;         
    
        avgikR+=ikkR[k]; 
        avgik+=ikk[k];
        //System.out.println("Left Moran I: "+ikk[0]+"num "+num[0]+"den "+den[0]+" "+" NM: "+(m_SplitIndex)+" W: "+m_PreviousSumW[0]+" wx:"+m_PreviousSumWX[0]+" wxx:"+m_PreviousSumWXX[0]+" xx:"+m_PreviousSumX2[0]);
        //System.out.println("Right Moran I: "+ikkR[0]+"numR "+numR[0]+"denR "+denR[0]+" "+" NM: "+(NR-m_SplitIndex)+" WR "+m_PreviousSumWR[0]+" wxR: "+m_PreviousSumWXR[0]+" wxx "+m_PreviousSumWXXR[0]+" xx:"+m_PreviousSumX2R[0]);
    
        }//targets
        avgik/=schema.getNbTargetAttributes();
        avgikR/=schema.getNbTargetAttributes();     
        
        I=(avgik*N+avgikR*(NR-N))/vkupenBrojElementiVoCelataSuma;
        M=m_PrevIndex; N=m_SplitIndex;  
        double scaledI=1+I;
        if (Double.isNaN(scaledI)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI; 
    }
    //global I 
    public double calcItotal(Integer[] permutation) { // matejp: all tuple indices <index> are replaced by permutation[<index>]
        ClusSchema schema = m_Data.getSchema();
        double num,den;
        m_TempData=m_Data;
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];      
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[permutation[i]]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        
        //System.out.println("Left Moran I: "+"ex: "+N+" "+M+" means: "+means[0]);
        
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        double W = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                int permI = permutation[i];
                DataTuple exi = m_Data.getTuple(permI);
                double xi = type.getNumeric(exi);
                for (int j = M; j < N; j++) {
                    int permJ = permutation[permutation[j]];
                    DataTuple exj = m_Data.getTuple(permJ);
                    double xj = type.getNumeric(exj);                   
                    double w=0;
                    long indexI=permI;
                    long indexJ=permJ;
                    if (permI!=permJ){
                        if (permI>permJ){
                            indexI=permJ;
                            indexJ=permI;
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) w=temp; else w=0;   
                        upsum[k] += w*(xi-means[k])*(xj-means[k]); 
                        W+=w;   
                    }  
                    else{
                        upsum[k] += (xi-means[k])*(xi-means[k]); 
                        W+=1; }
                }       
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }   
            num=((N-M+0.0)*upsum[k]);
            den=(W*downsum[k]);
            if (num!=0.0 && den!=0.0){
                ikk[k]=num/den; //I for each target         
            }else ikk[k] = 1; //Double.NEGATIVE_INFINITY;
            avgik+=ikk[k]; //average of the both targets
            //System.out.println("w: "+W+"num: "+num+"den: "+den+"Left Moran I: "+avgik+"ex: "+((N-M)));
        }       
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left Moran I: "+avgik+"ex: "+(N-M)+"W "+W+" means: "+means[0]);
        double IL=avgik*(N-M);  
        
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        double avgikR = 0; 
        double WR = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                int permI = permutation[i];
                DataTuple exi = m_Data.getTuple(permI);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {
                    int permJ = permutation[j];
                    DataTuple exj = m_Data.getTuple(permJ);
                    double xj = type.getNumeric(exj);
                    double w=0; 
                    long indexI=permI;
                    long indexJ=permJ;
                    if (permI!=permJ){
                        if (permI>permJ){
                            indexI=permJ;
                            indexJ=permI;
                        }
        
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) w=temp; else w=0;   
                        upsumR[k] += w*(xi-meansR[k])*(xj-meansR[k]); 
                        WR+=w;  
                    }  
                    else{
                        upsumR[k] += (xi-meansR[k])*(xi-meansR[k]); 
                        WR+=1; }
                }           
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k]));
            }       
    
            num=(((N-M)+0.0)*upsumR[k]);
            den=(WR*downsumR[k]);
            if (num!=0.0 && den!=0.0){
                ikkR[k]=num/den; //I for each target            
            }else ikkR[k] = 1; //Double.NEGATIVE_INFINITY;
            avgikR+=ikkR[k]; //average of the both targets
            //System.out.println("w: "+WR+"num: "+num+"den: "+den+"Right Moran I: "+avgikR+"ex: "+((N-M)));
        }       
        avgikR/=schema.getNbTargetAttributes();
        //System.out.println("Right Moran I: "+avgikR+"ex: "+(N-M)+"W "+WR+" means: "+meansR[0]);
        double IR=avgikR;   //end right side
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    // global I calculation with a separate distance file
    public double calcItotalD(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        double num,den;
        double avgik = 0; double W = 0.0; 
        m_TempData=m_Data;
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        //left 
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];  
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
                            
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0];           
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);                
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);                    
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){                        
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }       
                        
                        String indexMap =indexI +"#"+indexJ;
                        Double tmp= GISHeuristic.m_distancesS.get(indexMap);
                        if (tmp!=null)w=tmp;else w=0;   
                        upsum[k] += w*(xi-means[k])*(xj-means[k]); 
                        W+=w;
                        //System.out.println(w+" "+indexI+" "+indexJ);
                    }
                    else {
                    upsum[k] += (xi-means[k])*(xi-means[k]); 
                    W+=1;}
                }
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }           
            num=((N-M+0.0)*upsum[k]);
            den=(W*downsum[k]);
            if (num!=0.0 && den!=0.0){
                ikk[k]=num/den; //I for each target         
            }else ikk[k] = 1; 
            avgik+=ikk[k]; //average of multiple targets
            //System.out.println("w: "+W+"num "+num+"den "+den+"up: "+upsum[0]+"down: "+downsum[0]+"Left Moran I: "+avgik+"ex: "+((N-M)));
        }       
        avgik/=schema.getNbTargetAttributes();
        double IL=avgik*(N-M);  
        num=0; den=0;
        //right side
        N = m_Data.getNbRows(); M=m_SplitIndex;double avgikR = 0;
//        double WR = 0.0; 
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];             
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(i);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
                
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; 
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(i);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);                
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);                    
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;                    
                    if (indexI!=indexJ){                    
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                        String indexMap = indexI +"#"+indexJ;
                        Double tmp= GISHeuristic.m_distancesS.get(indexMap);
                        if (tmp!=null)w=tmp;else w=0;
                        upsumR[k] += w*(xi-meansR[k])*(xj-meansR[k]); 
                        //System.out.println(indexMap+" "+upsumR[0]);
//                        WR+=w;      
                    }
                    else
                    {               
                        upsumR[k] += (xi-meansR[k])*(xi-meansR[k]);
//                        WR+=1;
                    }
                }
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k])); 
            }       
    
            num=((N-M+0.0)*upsumR[k]);
            den=(W*downsumR[k]);
            if (num!=0.0 && den!=0.0){
                ikkR[k]=num/den; //I for each target            
            }else ikkR[k] = 1; //Double.NEGATIVE_INFINITY;
            avgikR+=ikkR[k]; //average of the both targets
            //System.out.println("wR: "+WR+"num "+num+"den "+den+"upsum: "+upsumR[0]+"downsum: "+downsumR[0]+"Right Moran I: "+avgikR+"ex: "+((N-M)));
        }       
        avgikR/=schema.getNbTargetAttributes(); 
        double IR=avgikR;   
        //System.out.println("w: "+WR+"means: "+meansR[0]+"Right Moran I: "+avgikR+"ex: "+((N-M)));
        double I=(IL+IR*(N-M))/m_Data.getNbRows();
        double scaledI=1+I;
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return scaledI;             
    }
    
    // global C
    public double calcGtotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
    
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        double avgik = 0;
        double W = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) w=temp; else w=0;   
                        upsum[k] += w*(xi-xj)*(xi-xj); 
                        W+=w;
                        
                    } else {W+=1;upsum[k] += (xi-xj)*(xi-xj);}
                }
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }
            if (downsum[k]!=0 && upsum[k]!=0){
                ikk[k]=((N-M-1)*upsum[k])/(2*W*downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            } else ikk[k]=0;
        }
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left Geary C:"+ikk[0]+"examples "+(N-M)+"W: "+W+"up "+upsum[0]+"down: "+downsum[0]);  
        double IL=(N-M)*avgik;
    
        //right side G
        M=m_SplitIndex; N = m_Data.getNbRows();
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        double avgikR = 0;
        double WR = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(NR)+indexJ;
                        Double temp= GISHeuristic.m_distances.get(indexMap); 
                        if (temp!=null) w=temp; else w=0;
                        upsumR[k] += w*(xi-xj)*(xi-xj); 
                        WR+=w;
                    } else {WR+=1;upsumR[k] += (xi-xj)*(xi-xj);} 
                }
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k]));
            }
            if (downsumR[k]!=0 && upsumR[k]!=0){
                ikkR[k]=((N-M-1)*upsumR[k])/(2*WR*downsumR[k]); //I for each target
                avgikR+=ikkR[k]; //average of the both targets
            } else ikkR[k]=0;
        }
        avgikR/=schema.getNbTargetAttributes();
        double IR=avgikR;
        //System.out.println("Right Geary C:"+IR+"examples: "+(N-M)+"WR: "+WR+"upR "+upsumR[0]+"downR: "+downsumR[0]);
        //end right side
        
        //scaledG=2-G [0,2]
        double I=2-((IL+IR*(N-M))/NR);
        //System.out.println("Join Geary G: "+I);
        
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;
            
    }
    // global C calculation with a separate distance file
    public double calcGtotalD(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
    
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        double avgik = 0;
        double W = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; 
            
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);
                
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);
                    
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                        
                        String indexMap =indexI +"#"+indexJ;
                        Double temp=GISHeuristic.m_distancesS.get(indexMap);
                        if (temp!=null) w=temp; else w=0;
                        upsum[k] += w*(xi-xj)*(xi-xj); 
                        W+=w;
                    } else {W+=1;upsum[k] += (xi-xj)*(xi-xj);}
                }
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }
            if (downsum[k]!=0 && upsum[k]!=0){
                ikk[k]=((N-M-1)*upsum[k])/(2*W*downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            } else ikk[k]=0;
        }
        avgik/=schema.getNbTargetAttributes();
        //System.out.println("Left Geary C:"+avgik+"examples "+(N-M));  
        double IL=(N-M)*avgik;
    
        //right side G
        M=m_SplitIndex; N = m_Data.getNbRows();
        double[] upsumR = new double[schema.getNbTargetAttributes()];
        double[] downsumR = new double[schema.getNbTargetAttributes()];
        double[] meansR = new double[schema.getNbTargetAttributes()];
        double[] ikkR = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                meansR[k] += xi;
            }
            meansR[k]/=(N-M);
        }
        double avgikR = 0;
        double WR = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            ClusAttrType xt = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_GIS)[0]; 
            
            for (int i = M; i <N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                long xxi = (long)xt.getNumeric(exi);
                
                for (int j =M; j < N; j++) {                
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    long yyi = (long)xt.getNumeric(exj);
                    
                    double w=0;
                    long indexI=xxi;
                    long indexJ=yyi;
                    if (indexI!=indexJ){
                        if (indexI>indexJ){
                            indexI=yyi;
                            indexJ=xxi;
                        }
                        
                        String indexMap =indexI +"#"+indexJ;
                        Double temp=GISHeuristic.m_distancesS.get(indexMap);
                        if (temp!=null) w=temp; else w=0;   
                        upsumR[k] += w*(xi-xj)*(xi-xj); 
                        WR+=w;
                    } else {WR+=1; upsumR[k] += (xi-xj)*(xi-xj);}
                }
                downsumR[k]+=((xi-meansR[k])*(xi-meansR[k]));
            }
            if (downsumR[k]!=0 && upsumR[k]!=0){
                ikkR[k]=((N-M-1)*upsumR[k])/(2*WR*downsumR[k]); //I for each target
                avgikR+=ikkR[k]; //average of the both targets
            } else ikkR[k]=0;
        }
        avgikR/=schema.getNbTargetAttributes();
        double IR=avgikR;
        //System.out.println("Right Geary C:"+IR+"exaples: "+(N-M));    
        //end right side
        
        //scaledG=2-G [0,2]
        double I=2-((IL+IR*(N-M))/NR);
        //System.out.println("Join Geary G: "+I);
        
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;
            
    }
    
    // global Getis
    public double calcGetisTotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[] upsum = new double[schema.getNbTargetAttributes()];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double avgik = 0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j = M; j < N; j++) {               
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(N-M)+indexJ;                            
                        if (GISHeuristic.m_distances.get(indexMap)!=null){
                            w=GISHeuristic.m_distances.get(indexMap);
                            }else{w=0;}                     
                        upsum[k] += w*xi*xj; 
                        downsum[k]+=xi*xj; 
                    }           
                }
            }           
            if (downsum[k]!=0.0 && upsum[k]!=0.0){
                ikk[k]=(upsum[k])/(downsum[k]); //I for each target
                avgik+=ikk[k]; //average of the both targets
            }else avgik = 1; 
        }       
        //System.out.println("i:"+avgik);
        avgik/=schema.getNbTargetAttributes();
        double I=avgik;
        //System.out.println("Moran I:"+ikk[0]+" "+means[0]);   //I for each target     
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;           
    }
    
    
    //local Moran I calculation
    public double calcLISAtotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[][] upsum1 = new double[schema.getNbTargetAttributes()][N];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
                    
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        
        //System.out.println("mean 0:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0; //Double.NEGATIVE_INFINITY;
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
//                DataTuple exi = m_Data.getTuple(permutation[i]);
//                double xi = type.getNumeric(exi);
                for (int j = M; j < N; j++) {               
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(N-M)+indexJ;                            
                        if (GISHeuristic.m_distances.get(indexMap)!=null){
                            w=GISHeuristic.m_distances.get(indexMap);
                            }else{w=0;} 
                        upsum1[k][permutation[i]] += w*(xj-means[k]); 
                        //System.out.println(upsum[k][permutation[i]]);  
                    }           
                }               
            }           
            
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                upsum[k][permutation[i]] = (xi-means[k])*upsum1[k][permutation[i]];
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }
            downsum[k]/=(N-M+0.0);
            for (int i = M; i < N; i++) {
            if (downsum[k]!=0.0 && upsum[k][permutation[i]]!=0.0){
                ik[k][permutation[i]]=(upsum[k][permutation[i]])/(downsum[k]); //I for each point 
                ikk[k]+=ik[k][permutation[i]]; //average of all points for each targets
                //System.out.println("LISA0: "+ik[k][permutation[i]]);   
                } else ikk[k] = 0;
            }
            avgik+=ikk[k]; 
        }
        avgik/=schema.getNbTargetAttributes(); //average of all targets
        double I=avgik;
        
        // System.out.println("Moran I:"+ikk[0]+" "+ikk[1]);    //I for each target     
         //System.out.println(W+" I1: "+upsum[0]+" "+downsum[0]+" "+ikk[0]+" "+means[0]);
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;           
    }   
    
    //local Geary C
    public double calcGLocalTotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[] downsum = new double[schema.getNbTargetAttributes()];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0;
//        double W = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                for (int j = M; j < N; j++) {               
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(N-M)+indexJ;                            
                        if (GISHeuristic.m_distances.get(indexMap)!=null){
                            w=GISHeuristic.m_distances.get(indexMap);
                            }else{w=0;} 
                        upsum[k][permutation[i]] += w*(xi-xj)*(xi-xj); 
                        //System.out.println("Upsum:"+xi+" "+xj+","+upsum[k][permutation[i]]);       
//                        W+=w;
                    }  
            
                }
                //System.out.println("Geary C:"+k+" "+i+","+upsum[k][permutation[i]]);                   
                downsum[k]+=((xi-means[k])*(xi-means[k]));
            }
            
            for (int i = M; i < N; i++) {
                if (downsum[k]!=0.0 && upsum[k][permutation[i]]!=0.0){
                    ik[k][permutation[i]]=((N-M)*upsum[k][permutation[i]])/(downsum[k]); //C for each point   
                    //System.out.println("Geary C:"+k+" "+i+","+upsum[k][permutation[i]]+"/"+(N-M)/downsum[k]);  
                    ikk[k]+=ik[k][permutation[i]]; //average of all points for each targets
                    } else ikk[k] = 0;
                }
                avgik+=ikk[k]; 
            }
            avgik/=schema.getNbTargetAttributes(); //average of all targets
            double I=avgik;
        //System.out.println("Geary C:"+ikk[0]+","+ikk[1]); 
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;
            
    }
    //local Getis
    public double calcLocalGetisTotal(Integer[] permutation) {
        ClusSchema schema = m_Data.getSchema();
        int M = 0;
        int N = m_Data.getNbRows();
//        long NR = m_Data.getNbRows();
        if (m_SplitIndex>0){
            N=m_SplitIndex;
        }else{
            M=-m_SplitIndex;
        }
        
        double[][] upsum = new double[schema.getNbTargetAttributes()][N];
        double[][] downsum = new double[schema.getNbTargetAttributes()][N];
        double[] means = new double[schema.getNbTargetAttributes()];
        double[] ikk = new double[schema.getNbTargetAttributes()];
        double[][] ik = new double[schema.getNbTargetAttributes()][N];
        
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            for (int i = M; i < N; i++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                DataTuple exi = m_Data.getTuple(permutation[i]);
                double xi = type.getNumeric(exi);
                means[k] += xi;
            }
            means[k]/=(N-M);
        }
        //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
        double avgik = 0;
//        double W = 0.0; 
        for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
            ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
            for (int i = M; i < N; i++) {
//                DataTuple exi = m_Data.getTuple(permutation[i]);
//                double xi = type.getNumeric(exi);
                for (int j = M; j < N; j++) {               
                    DataTuple exj = m_Data.getTuple(j);
                    double xj = type.getNumeric(exj);
                    double w=0;
                    long indexI=i;
                    long indexJ=j;
                    if (i!=j){
                        if (i>j){
                            indexI=j;
                            indexJ=i;
                        }
                        long indexMap = indexI*(N-M)+indexJ;                            
                        if (GISHeuristic.m_distances.get(indexMap)!=null){
                            w=GISHeuristic.m_distances.get(indexMap);
                            }else{w=0;} 
                        upsum[k][permutation[i]] += w*xj; 
                        downsum[k][permutation[i]]+=xj;
                    }  
                }
                if (upsum[k][permutation[i]]!=0.0){
                    ik[k][permutation[i]]=(upsum[k][permutation[i]])/(downsum[k][permutation[i]]); //Local Getis for each point    
                    ikk[k]+=ik[k][permutation[i]]; //average of all points for each targets
                    } else ikk[k] = 0;
                avgik+=ikk[k]; 
            }
            }
            avgik/=schema.getNbTargetAttributes(); //average of all targets
            double I=avgik;
        //System.out.println("Local Getis:"+ikk[0]+","+ikk[1]); 
        if (Double.isNaN(I)){
            System.out.println("err!");
            System.exit(-1);
        }
        return I;
            
    }
        
    //local Getis calculation=standardized z    
    public double calcGETIStotal(Integer[] permutation) {
            ClusSchema schema = m_Data.getSchema();
            int M = 0;
            int N = m_Data.getNbRows();
//            int NR = m_Data.getNbRows();
            if (m_SplitIndex>0){
                N=m_SplitIndex;
            }else{
                M=-m_SplitIndex;
            }
            
            double[][] upsum = new double[schema.getNbTargetAttributes()][N];
            double[][] upsum1 = new double[schema.getNbTargetAttributes()][N];
            double[][] downsum1 = new double[schema.getNbTargetAttributes()][N];
            double[][] downsum2 = new double[schema.getNbTargetAttributes()][N];
            double[][] ik = new double[schema.getNbTargetAttributes()][N];
            double[] downsum = new double[schema.getNbTargetAttributes()];
            double[] means = new double[schema.getNbTargetAttributes()];
            double[] ikk = new double[schema.getNbTargetAttributes()];
                        
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                for (int i = M; i < N; i++) {
                    ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    means[k] += xi;
                }
                means[k]/=(N-M);
            }
            //System.out.println("G1:"+means[0]+". Split was on "+(N+",M:"+M)+" examples out of "+N +" examples. ");
            double avgik = 0;
            double[] W = new double[N]; 
            for (int k = 0; k < schema.getNbTargetAttributes(); k++) {
                ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)[k];
                for (int i = M; i < N; i++) {
                    DataTuple exi = m_Data.getTuple(permutation[i]);
                    double xi = type.getNumeric(exi);
                    for (int j = M; j < N; j++) {               
                        DataTuple exj = m_Data.getTuple(j);
                        double xj = type.getNumeric(exj);
                        double w=0;
                        long indexI=i;
                        long indexJ=j;
                        if (i!=j){
                            if (i>j){
                                indexI=j;
                                indexJ=i;
                            }
                            long indexMap = indexI*(N-M)+indexJ;                            
                            if (GISHeuristic.m_distances.get(indexMap)!=null){
                                w=GISHeuristic.m_distances.get(indexMap);
                            }else{w=0;} 
                            upsum1[k][permutation[i]]+= w*xj; 
                            downsum1[k][permutation[i]]+= w*w; 
                            W[permutation[i]]+=w;
                        }               
                    }
                    upsum[k][permutation[i]] = upsum1[k][permutation[i]]- means[k]*W[permutation[i]];
                    downsum[k]+=((xi-means[k])*(xi-means[k]));  //downsum the same as geary's C
                    downsum2[k][permutation[i]] = (N-M)*downsum1[k][permutation[i]]-W[permutation[i]]*W[permutation[i]];
                    //System.out.println("downsum2:"+downsum2[k][permutation[i]]);
                }
                
                for (int i = M; i < N; i++) {
                    if (downsum[k]!=0.0 && upsum[k][permutation[i]]!=0.0 && downsum2[k][permutation[i]]!=0.0){
//                        double im= downsum2[k][permutation[i]];
                        //System.out.println(im);
                        ik[k][permutation[i]]=upsum[k][permutation[i]]/(Math.sqrt((downsum[k]/((N-M)*(N-M-1)))*downsum2[k][permutation[i]])); //I for each point   
                        ikk[k]+=ik[k][permutation[i]]; //average of all points for each targets
                        } else ikk[k] = 0;
                    }
                    avgik+=ikk[k]; 
                }
                avgik/=schema.getNbTargetAttributes(); //average of all targets
                double I=avgik;
                
            //System.out.println("Geary C:"+ikk[0]+","+ikk[1]); 
            if (Double.isNaN(I)){
                System.out.println("err!");
                System.exit(-1);
            }
            return I;
                
        }
        
    public double getCalcItotal() {
        return m_I;
    }
    public void setCalcItotal(double ii){
        m_I = ii;
    }
    
    public void setData(RowData data) {
        m_Data = data;  
    }
    
    public void setSplitIndex(int i) {
        m_SplitIndex = i;
    }
    public int getSplitIndex() {
        return m_SplitIndex;
    }
    public void setPrevIndex(int i) {
        m_PrevIndex = i;
    }
    public int getPrevIndex() {
        return m_PrevIndex;
    }
    
    public void initializeSum() {
        Arrays.fill(m_PreviousSumX, 0.0);
        Arrays.fill(m_PreviousSumXR, 0.0);
        Arrays.fill(m_PreviousSumW, 0.0);     
        Arrays.fill(m_PreviousSumWXX, 0.0);
        Arrays.fill(m_PreviousSumWX, 0.0);
        Arrays.fill(m_PreviousSumX2, 0.0);
        Arrays.fill(m_PreviousSumWR, 0.0);        
        Arrays.fill(m_PreviousSumWXXR, 0.0);
        Arrays.fill(m_PreviousSumWXR, 0.0);
        Arrays.fill(m_PreviousSumX2R, 0.0);
    }
    
    public RowData getData() {
        return m_Data;
    }
    // end daniela

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

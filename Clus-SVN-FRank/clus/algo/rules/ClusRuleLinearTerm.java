/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2010                                                    *
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
 * Created on February 9, 2010
 */
package clus.algo.rules;

import java.io.PrintWriter;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NumericAttrType;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;
import clus.statistic.StatisticPrintInfo;
import clus.tools.optimization.ImplicitLinearTerms;

/** A linear term that has been included in the rule set.
 * The weights of these linear terms can also be optimized with GD algorithm.
 * @author Timo Aho
 */
public class ClusRuleLinearTerm extends ClusRule {

	// What is this?
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
	
	/** Class variable. Offset values (means of descriptive attributes) for all the linear terms */
//	static private double[] C_offSetValues = null;
	/** Class variable. Standard deviation  values (of descriptive attributes) for all the linear terms */
//	static private double[] C_stdDevValues = null;
	/** Class variable. Standard deviation  values of TARGET attributes for all the linear terms */
//	static private double[] C_targetStdDevs = null;
	/** Class variable. For linear term truncate, maximum value found for descriptive attributes in training */
	static private double[] C_maxValues = null;
	/** Class variable. For linear term truncate, minimum value found for descriptive attributes in training */
	static private double[] C_minValues = null;
	static private ClusStatManager C_statManager = null;
	
	/** Class variable. If linear terms are added implicitly to the rule set, they are here. */
	static private ImplicitLinearTerms C_implicitTerms = null;
	
	/** Initializes class values for all linear terms. Initialize with this always! */ 
	static public void initializeClass(RowData data, ClusStatManager statMgr) {
		C_statManager = statMgr;		
	
		// Compute first the maximum and minimum for the training data
		double[][] linearTermsMinAndMaxes = calcMinAndMaxForTheSet(data, C_statManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE));
		C_minValues = linearTermsMinAndMaxes[0];
		C_maxValues = linearTermsMinAndMaxes[1];

		C_implicitTerms = null;

//		C_offSetValues = null;
//		C_stdDevValues = null;
//		if (settings.isOptNormalizeLinearTerms()) {
//			// If scaling is used, we now compute the scaling that linear terms are going to use
//			// After optimization this scaling may be moved to the weight.
//			double[][] meansAndStdDevs = calcStdDevsForTheSet(data, C_statManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE));
//			
//			C_offSetValues = meansAndStdDevs[0];
//			C_stdDevValues = meansAndStdDevs[1];
//			
//			// We also scale the values according to target std values.
//			// Mean could also be used, but it can be handled with the first rule anyway.
//			double[][] targetMeansAndStdDevs = calcStdDevsForTheSet(data, C_statManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
//			C_targetStdDevs = targetMeansAndStdDevs[1];
//		}
	}
	
	/** Offset values (means of descriptive attributes) for all the linear terms.
	 */
	private static double getOffSetValue(int iDescAttr) {
		return RuleNormalization.getDescMean(iDescAttr);
	}

	/** Standard deviation values (of descriptive attributes) for all the linear terms 
	 */
	private static double getDescStdDev(int iDescAttr) {
		return RuleNormalization.getDescStdDev(iDescAttr);
	}

	/** Standard deviation values of TARGET attributes for all the linear terms
	 */
	private static double getTargStdDev(int iTargAttr) {
		return RuleNormalization.getTargStdDev(iTargAttr);
	}

	/** For linear term truncate, maximum value found for descriptive attributes in training */
	private static double getMaxValue(int iDescAttr) {
		return C_maxValues[iDescAttr];
	}
	
	/** For linear term truncate, minimum value found for descriptive attributes in training */
	private static double getMinValue(int iDescAttr) {
		return C_minValues[iDescAttr];
	}
	
	/** Return new linear term with weight */
	static public ClusRule createLinTerm(int iDescriptDim, int iTargetDim, double weight) {
		ClusRuleLinearTerm newTerm = new ClusRuleLinearTerm(C_statManager, iDescriptDim, iTargetDim);
		newTerm.setOptWeight(weight);
		return newTerm;
		
	}
	
	/** Returns implicit linear terms if this is set in the settings file. */ 
	static protected ImplicitLinearTerms returnImplicitLinearTermsIfNeeded(RowData data)
	{
		if (data.getSchema().getSettings().getOptAddLinearTerms() != Settings.OPT_GD_ADD_LIN_YES_SAVE_MEMORY)
			return null;
		
		double[][] values = new double[2][];
//		values[0] = C_offSetValues;
//		values[1] = C_stdDevValues;
//		values[2] = C_targetStdDevs;
		values[0] = C_maxValues;
		values[1] = C_minValues;
		C_implicitTerms = new ImplicitLinearTerms(data, C_statManager);//, values);
		return C_implicitTerms;
	}
	
	/** When the real linear terms are added to RuleSet, the implicit ones can be deleted */ 
	static protected void DeleteImplicitLinearTerms() {
		C_implicitTerms.DeleteImplicitLinearTerms();
		C_implicitTerms = null;
	}
	

	
	
	
	
	
	

		


	/** If the rule is in fact a linear term, this is the index of the term that is used */
	private int m_descriptiveDimForLinearTerm = 0;
	/** If the rule is in fact a linear term, this is the target index that is predicted with this */
	private int m_targetDimForLinearTerm = 0;

	/** Is the scaling of linear terms used in all the predictions. If not, used only for
	 * undefined predictions. This boolean is needed for moving the scaling and shifting
	 * to the weights etc.*/
	private boolean m_scaleLinearTerm = false;
	
	
	/** A constructor for creating linear terms.
	 * @par linearTermDimension Descriptive dimension that the linear term is based on.
	 * @par linearTermTargetDim Target dimension this linear term is predicting.
	 * @par maxValue Maximum value of the dimension in the data set. Used for truncation.
	 * @par minValue Minimum value of the dimension in the data set. Used for truncation.
	 * @par offSetValue Value used for shifting  the linear prediction. */
	public ClusRuleLinearTerm(ClusStatManager statManager, int iDescriptDim, int iTargetDim)
	{
		super(statManager);
			
		m_descriptiveDimForLinearTerm = iDescriptDim;
		m_targetDimForLinearTerm = iTargetDim;
		m_scaleLinearTerm = statManager.getSettings().isOptNormalizeLinearTerms(); // may be changed false later
		
		// Change rule attributes
		m_TargetStat = statManager.createTargetStat();

		int nbTargets = statManager.getStatistic(ClusAttrType.ATTR_USE_TARGET).getNbAttributes();
		if (!(m_TargetStat instanceof RegressionStat))
			System.err.println("Error: Using linear terms is implemented for regression only.");
		
		RegressionStat stat = (RegressionStat) m_TargetStat;
		stat.m_Means = new double[nbTargets];
		stat.m_Means[iTargetDim] = 1; // It does not matter what the value is.
		stat.m_NbAttrs = nbTargets;
		stat.m_SumValues = new double[nbTargets];
		stat.m_SumWeights = new double[nbTargets];
		stat.m_SumValues[iTargetDim] = 1;
		stat.m_SumWeights[iTargetDim] = 1;
	}
		
	 /** Returns the prediction by this linear term for the tuple. */
	public ClusStatistic predictWeighted(DataTuple tuple) {
		if (!(m_TargetStat instanceof RegressionStat))
			System.err.println("Error: Using linear terms for optimization is implemented for regression only.");
		
		RegressionStat stat = ((RegressionStat) m_TargetStat);
		
//		double value = (C_statManager.getSchema().
//				getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))[m_descriptiveDimForLinearTerm].getNumeric(tuple);
	
		double pred = attributeToLinTermPrediction(getSettings(), tuple, m_descriptiveDimForLinearTerm,
				m_targetDimForLinearTerm, stat.m_NbAttrs, m_scaleLinearTerm);
		
		if (Double.isNaN(pred)) {
			// Mark all the target values as NaN. Otherwise causes problems in optimization.
			for (int i = 0; i < stat.m_NbAttrs; i++) {
				stat.m_Means[i] = Double.NaN;
				stat.m_SumValues[i] = Double.NaN;
				stat.m_SumWeights[i] = 1;
			}				
		} else {
			// If defined prediction, clear predictions (do not leave NaNs)
			for (int i = 0; i < stat.m_NbAttrs; i++) {
				stat.m_Means[i] = 0;
				stat.m_SumValues[i] = stat.m_Means[i];
				stat.m_SumWeights[i] = 1;
			}
			stat.m_Means[m_targetDimForLinearTerm] = pred;
			stat.m_SumValues[m_targetDimForLinearTerm] = stat.m_Means[m_targetDimForLinearTerm];
		}

		return m_TargetStat;
	}
	
	 /** Returns the prediction for a linear term given a descriptive attribute value.
	  * I.e. makes all the needed transformations for the descriptive attribute value.*/
	public static double attributeToLinTermPrediction(Settings sett, DataTuple tuple,
														int iDescrDim, int iTarDim, int nbOfTargets,
														boolean scaleLinearTerm) {
		double descrValue = (C_statManager.getSchema().
				getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))[iDescrDim].getNumeric(tuple);

		if (Double.isNaN(descrValue) || Double.isInfinite(descrValue)){
			if (sett.getOptNormalizeLinearTerms() == Settings.OPT_LIN_TERM_NORM_CONVERT) {
				descrValue = getOffSetValue(iDescrDim);
			} else {				
				descrValue = Double.NaN;
			}
		}
		// Truncated version - the linear term holds only on the range of training set.
		if (sett.isOptLinearTermsTruncate() &&
				!Double.isNaN(getMaxValue(iDescrDim)) 
				&& !Double.isNaN(getMinValue(iDescrDim))) {
			descrValue = Math.max(Math.min(descrValue, getMaxValue(iDescrDim)),
					getMinValue(iDescrDim));
		}


		if (sett.isOptNormalizeLinearTerms() && scaleLinearTerm) { 
			descrValue -= getOffSetValue(iDescrDim); // Shift 
			descrValue /= 2*getDescStdDev(iDescrDim); // scale
			
			// Cast the linear term value according to the target
			descrValue *= 2*getTargStdDev(iTarDim);
		}
		
//		return !Double.isNaN(descrValue) ? descrValue : 0;
		return descrValue;
	}	
	
	/** Does the term cover the given tuple */
	public boolean covers(DataTuple tuple) {
		if (getSettings().getOptNormalizeLinearTerms() == Settings.OPT_LIN_TERM_NORM_CONVERT) {
			return true; // Always covers, otherwise problems with converting.
		}

		double value = (C_statManager.getSchema().
				getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))[m_descriptiveDimForLinearTerm].getNumeric(tuple);
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}
	
	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		wrt.println("Linear term for the numerical attribute with index "+ m_descriptiveDimForLinearTerm 
				+ " predicting target index " + m_targetDimForLinearTerm);
			
		if (getSettings().isOptLinearTermsTruncate()) {
			wrt.println("The prediction is truncated on the interval [" 
						+ C_minValues[m_descriptiveDimForLinearTerm] + "," + C_maxValues[m_descriptiveDimForLinearTerm] + "].");
		}

		if (getSettings().getOptNormalizeLinearTerms() == Settings.OPT_LIN_TERM_NORM_CONVERT) {
			//wrt.println("Linear term prediction was scaled and shifted by (x-average)/(2*standard deviation) during normalization.");
			wrt.println("Linear term prediction was scaled and shifted by (x-average)*(standard deviation of target)/(standard deviation of descriptive) during normalization.");
		} else if (getSettings().getOptNormalizeLinearTerms() == Settings.OPT_LIN_TERM_NORM_YES) {
			//wrt.println("Linear term prediction is scaled and shifted by (x-average)/(2*standard deviation)");
			wrt.println("Linear term prediction is scaled and shifted by (x-average)*(standard deviation of target)/(standard deviation of descriptive)");
		}
		if (getSettings().isOptNormalizeLinearTerms()){
			wrt.println("      Standard deviation (targ) : " + getTargStdDev(m_targetDimForLinearTerm));
			wrt.println("      Standard deviation (descr): " + getDescStdDev(m_descriptiveDimForLinearTerm));
			wrt.println("      Average                   : " + getOffSetValue(m_descriptiveDimForLinearTerm));
		}

		commonPrintForRuleTypes(wrt, info);
	}
	
	/** Is this a regular rule or some other type of learner (e.g. linear term) */
	public boolean isRegularRule() {
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Calculate min and max for all numerical attributes. Used for limiting the
	 * linear terms not to extend predictions too much.
	 * @return A double[][] array where [0] are mins and [1] are maxes
	 */
	static private double[][] calcMinAndMaxForTheSet(RowData data, NumericAttrType[] numTypes){

		//		NumericAttrType[] numTypes = ;

		//		for (int iDim = 0; iDim < numTypes.length; iDim++){

		// Statistics for numeric types.

		double[] mins = new double[numTypes.length];
		double[] maxs = new double[numTypes.length];
		//** Some of the values are not valid. These should not be used for computing variance etc. *//
		double[] nbOfValidValues = new double[numTypes.length];


		for (int iDim = 0; iDim < numTypes.length; iDim++){
			mins[iDim] = Double.POSITIVE_INFINITY;
			maxs[iDim] = Double.NEGATIVE_INFINITY;
		}

		// Computing
		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
			DataTuple tuple = data.getTuple(iRow);

			for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
				double value = numTypes[jNumAttrib].getNumeric(tuple);
				if (!Double.isNaN(value) && !Double.isInfinite(value)) {// Value given
					if (value > maxs[jNumAttrib])
						maxs[jNumAttrib] = value;
					if (value < mins[jNumAttrib])
						mins[jNumAttrib] = value;
					nbOfValidValues[jNumAttrib]++;
				}
			}
		}

		for (int iDim = 0; iDim < numTypes.length; iDim++){
			// If there have been no valid value, both min and max are in infinity. This is not allowed
			// In this case we put this to undefined
			if (mins[iDim] == Double.POSITIVE_INFINITY && maxs[iDim] == Double.NEGATIVE_INFINITY) {
				mins[iDim] = maxs[iDim] = Double.NaN;
			}
		}

		double[][] minAndMax = new double[2][];
		minAndMax[0] = mins;
		minAndMax[1] = maxs;
		return minAndMax;
	}
	
//	/** Compute standard deviation and mean for each of the given attributes.
//	 * @return Index 0 is mean, index 1 std dev. */ 
//	static protected double[][] calcStdDevsForTheSet(RowData data, NumericAttrType[] numTypes) {
//		return Clus.calcStdDevsForTheSet(data, numTypes);
//	}
	
//	/** Compute standard deviation and mean for each of the given attributes.
//	 * @return Index 0 is mean, index 1 std dev. */ 
//	static protected double[][] calcStdDevsForTheSet(RowData data, NumericAttrType[] numTypes) {
//
//		// ** Some of the values are not valid. These should not be used for
//		// computing variance etc. *//
//		//double[] nbOfValidValues = new double[numTypes.length];
//		int[] nbOfValidValues = new int[numTypes.length];
//
//		// First means
//		double[] means = new double[numTypes.length];
//		
//		/** Floating point computation is not to be trusted. It is important to track if variance = 0,
//		 * (we are otherwise getting huge factors with 1/std dev). 
//		 */
//		boolean[] varIsNonZero = new boolean[numTypes.length];
//		
//		// Check if variance = 0 i.e. all values are the same
//		double[] prevAcceptedValue = new double[numTypes.length];
//		
//		for (int i = 0; i < prevAcceptedValue.length; i++)
//			prevAcceptedValue[i] = Double.NaN;
//
//		// Computing the means	
//		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
//			DataTuple tuple = data.getTuple(iRow);
//
//			for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
//				double value = numTypes[jNumAttrib].getNumeric(tuple);
//				if (!Double.isNaN(value) && !Double.isInfinite(value)) { // Value not given
//					
//					// Check if variance is zero
//					if (!Double.isNaN(prevAcceptedValue[jNumAttrib]) &&
//							prevAcceptedValue[jNumAttrib] != value)
//						varIsNonZero[jNumAttrib] = true;
//					
//					prevAcceptedValue[jNumAttrib] = value;
//					means[jNumAttrib] += value;
//					nbOfValidValues[jNumAttrib]++;
//				}
//				if (jNumAttrib == 20)
//					System.out.println("DEBUG: loytyi");
//			}
//		}
//		
//		// Divide with the number of examples
//		for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
//			if (nbOfValidValues[jNumAttrib] == 0) {
//				nbOfValidValues[jNumAttrib] = 1; // Do not divide with zero
//			}
//			if (!varIsNonZero[jNumAttrib]) // if variance = 0, do not do any floating point computation
//				means[jNumAttrib] = prevAcceptedValue[jNumAttrib];
//			else
//				means[jNumAttrib] /= nbOfValidValues[jNumAttrib];
//		}
//		
//		/** Variance for each of the attributes*/
//		double[] variance = new double[numTypes.length];
//		
//
//		// Computing the variances
//		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
//			DataTuple tuple = data.getTuple(iRow);
//
//			for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
//				double value = numTypes[jNumAttrib].getNumeric(tuple);
//				if (!Double.isNaN(value) && !Double.isInfinite(value)) // Value not given
//					variance[jNumAttrib] += Math.pow(value - means[jNumAttrib], 2.0);
//				if (jNumAttrib == 20)
//					System.out.println("DEBUG: loytyi");
//			}
//		}
//
//		double[] stdDevs = new double[numTypes.length];
//		
//		
//		
//		// Divide with the number of examples
//		for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
//			//if (variance[jNumAttrib] == 0) {
//			if (!varIsNonZero[jNumAttrib]) {
//				// If variance is zero, all the values are the same, so division
//				// is not needed.
//				variance[jNumAttrib] = 0.25; // And the divider will be
//												// 2*1/sqrt(4)= 1
//				System.out.println("Warning: Variance of attribute "  + jNumAttrib +" is zero.");
//			} else {
//				variance[jNumAttrib] /= nbOfValidValues[jNumAttrib];
//			}
//
//			stdDevs[jNumAttrib] = Math.sqrt(variance[jNumAttrib]);
//		}
//		
//		double[][] meanAndStdDev = new double[2][];
//		meanAndStdDev[0] = means;
//		meanAndStdDev[1] = stdDevs;
//		return meanAndStdDev;	
//	}

	public double[] convertToPlainTerm(double[] addToDefaultPred, double defaultWeight) {
		//Normalization was of type w0*DEFAULT + w1 (x_8-AVG_8)/(2*stdDev)
		// = w0*DEFAULT + [w1/(2*stdDev)]x_8-w1*AVG_8/(2*stdDev)
		// = w0*DEFAULT + [w1/(2*stdDev)]x_8-w0*w1*AVG_8/(w0*2*stdDev)
		// = w0*[DEFAULT-w1*AVG_8/(w0*2*stdDev)] + [w1/(2*stdDev)]x_8
		// Include the shifting to the average term
		addToDefaultPred[m_targetDimForLinearTerm] -= 
			getOptWeight()*getOffSetValue(m_descriptiveDimForLinearTerm)
              		*2*getTargStdDev(m_targetDimForLinearTerm) // scaling for target
			        / (defaultWeight*2*getDescStdDev(m_descriptiveDimForLinearTerm));

		// Include scaling to the linear term weight 
		setOptWeight(getOptWeight()*2*getTargStdDev(m_targetDimForLinearTerm)
		                          /(2*getDescStdDev(m_descriptiveDimForLinearTerm)));
		
		// Remove the use of scaling in the rule
		m_scaleLinearTerm = false;
		
		return addToDefaultPred;
	}
}

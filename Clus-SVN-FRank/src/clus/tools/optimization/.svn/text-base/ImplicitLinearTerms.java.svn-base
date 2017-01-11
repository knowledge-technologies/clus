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
package clus.tools.optimization;

import clus.algo.rules.ClusRuleLinearTerm;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.ClusStatManager;
import clus.main.Settings;

/** Class for including all the linear terms implicitly in the weight optimization procedure.
 * Adding linear terms explicitly to the rule set may take huge amount of memory because of the number
 * of these terms. After the optimization usually most of the weights of linear terms are zero, so they are removed
 * anyway. Thus we are not really creating these linear terms before the optimization.
 * This memory usage reduction method is not used on default.
 * @author Timo Aho 
 */
public class ImplicitLinearTerms {

	/** Data for implicit linear term predictions */
	private RowData m_linearTermPredictions = null;
	private ClusStatManager m_StatManager = null;

//	/** For linear term truncate, maximum value found for descriptive attributes in training */
//	private double[] m_maxValues = null;
//	/** For linear term truncate, minimum value found for descriptive attributes in training */
//	private double[] m_minValues = null;
	
	/** Create implicit linear terms
	 * @param data
	 * @param statMgr
	 * @param values Scaling values and truncate values for the linear terms.
	 */
	public ImplicitLinearTerms(RowData data, ClusStatManager statMgr){
		//, double[][] values) {
		m_linearTermPredictions = data;
		m_StatManager = statMgr;
		
//		m_offSetValues = values[0];
//		m_stdDevValues = values[1];
//		m_targetStdDevs = values[2];
//		m_maxValues = values[0];
//		m_minValues = values[1];
		
		if (Settings.VERBOSE > 0) {
			int nbTargets = (m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET)).getNbAttributes();
			int nbDescrAttr = statMgr.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE).length; 

			System.out.println("\tIn optimization using implicitly the predictions of "+ nbDescrAttr +" linear terms for each target, total " + nbDescrAttr*nbTargets
				         + " terms.");
		}
	}

	public void DeleteImplicitLinearTerms() {
		m_linearTermPredictions = null;
		m_StatManager = null;
//		m_maxValues = null;
//		m_minValues = null;
	}
	
//	/** Offset values (means of descriptive attributes) for all the linear terms.
//	 */
//	private static double getOffSetValue(int iDescAttr) {
//		return RuleNormalization.getDescMean(iDescAttr);
//	}
//
//	/** Standard deviation values (of descriptive attributes) for all the linear terms 
//	 */
//	private static double getDescStdDev(int iDescAttr) {
//		return RuleNormalization.getDescStdDev(iDescAttr);
//	}
//
//	/** Standard deviation values of TARGET attributes for all the linear terms
//	 */
//	private static double getTargStdDev(int iTargAttr) {
//		return RuleNormalization.getTargStdDev(iTargAttr);
//	}
	
	/** If linear terms are not added excplicitly to the rule set (to save memory), this function
	 * returns the prediction. If the value is NaN, 0 is returned
	 * @param iLinTerm Index of linear term.
	 * @param instance Data row that we are going to predict.
	 * @param iTarget Which target value of the prediction is asked.
	 * @param nbOfTargets How many targets we have.
	 * @return The prediction.
	 */
	public double predict(int iLinTerm, DataTuple instance, int iTarget, int nbOfTargets) {
		/** The target attribute that descriptive attribute is included. Other attributes are 0 */
		int iLinTermTargetAttr = iLinTerm%nbOfTargets;

		// If we are asking non effective target, the value is always 0.
		if (iLinTermTargetAttr != iTarget) return 0;
		
		/** Which descriptive attribute value will be the target */
		int iDescriptiveAttr = (int) Math.floor((double)iLinTerm/nbOfTargets);
		
//		double value = (m_linearTermPredictions.getSchema().
//				getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))[iDescriptiveAttr].getNumeric(instance);

		double pred = ClusRuleLinearTerm.attributeToLinTermPrediction(getSettings(), instance, iDescriptiveAttr,
				iLinTermTargetAttr, nbOfTargets, true); // Always scale linear terms
		
		return !Double.isNaN(pred) ? pred : 0;
	}	

	private Settings getSettings() {
		return m_StatManager.getSettings();
	}
}

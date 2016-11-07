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

package clus.error;

import java.io.PrintWriter;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 * 
 * Hierarchical Euclidean distances is used in hierarchical multi-label classification scenario.
 */
public class HierarchicalEuclidean extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_SumErrors;	// sum of d(prediction(sample_i), target(sample_i)), where prediction (target) of a sample_i is the predicted (true) hierarchy and d is weighted euclidean distance
									// normalized by the number of labels: d(a, b) = sqrt(1 / nb_labels * sum_i w_i * (a_i - b_i)^2)
	protected int m_NbKnown;// number of the examples seen 
	
	public HierarchicalEuclidean(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_SumErrors = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return true;
	}

	public void reset() {
		m_SumErrors = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		HierarchicalEuclidean he = (HierarchicalEuclidean)other;
		m_SumErrors += he.m_SumErrors;
		m_NbKnown += he.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU? Ne, najbrz ne.
//	public double getHierarchicalEuclideanLoss(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return m_SumErrors / m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "HierarchicalEuclidean";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new HierarchicalEuclidean(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
//		int[] predicted = pred.getNominalPred();
//		NominalAttrType attr;
//		boolean atLeastOneKnown = false;
//		for (int i = 0; i < m_Dim; i++) {
//			attr = getAttr(i);
//			if (!attr.isMissing(tuple)) {
//				atLeastOneKnown = true;
//				if (attr.getNominal(tuple) != predicted[i]) {
//					m_NbWrong++;
//				}
//			}
//		}
	}

	public void addExample(DataTuple tuple, DataTuple pred) {
//		boolean atLeastOneKnown = false;
//		NominalAttrType attr;
//		for (int i = 0; i < m_Dim; i++) {
//			attr = getAttr(i);
//			if (!attr.isMissing(tuple)) {
//				atLeastOneKnown = true;
//				if (attr.getNominal(tuple) != attr.getNominal(pred)) {
//					m_NbWrong++;
//				}
//			}
//		}
	}
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}


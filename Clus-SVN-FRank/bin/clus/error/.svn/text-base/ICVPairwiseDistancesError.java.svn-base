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

import java.io.*;

import clus.error.*;
import clus.algo.rules.ClusRule;
import clus.algo.rules.ClusRuleSet;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.type.ClusSchema;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.test.*;
import clus.statistic.*;

public class ICVPairwiseDistancesError extends ClusError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_Value, m_ValueWithDefault;
	protected ClusDistance m_Dist;

	public ICVPairwiseDistancesError(ClusErrorList par, ClusDistance dist) {
		super(par);
		m_Dist = dist;
	}

	public static double computeICVPairwiseDistances(ClusDistance dist, RowData data) {
		double sum = 0.0;
		double sumWiDiag = 0.0;
		double sumWiTria = 0.0;
		int nb = data.getNbRows();
		for (int j = 0; j < nb; j++) {
			DataTuple t1 = data.getTuple(j);
			double w1 = t1.getWeight();
			for (int i = 0; i < j; i++) {
				DataTuple t2 = data.getTuple(i);
				double wi = w1 * t2.getWeight();
				double d = dist.calcDistance(t1, t2);
				sum += wi * d;
				sumWiTria += wi;
			}
			sumWiDiag += w1 * w1;
		}
		return sum / (2 * sumWiTria + sumWiDiag);
	}

	public void computeRecursive(ClusNode node, RowData data) {
		int nb = node.getNbChildren();
		if (nb == 0) {
			double variance = computeICVPairwiseDistances(m_Dist, data);
			double sumweight = data.getSumWeights();
			m_Value += sumweight * variance;
		} else {
			NodeTest tst = node.getTest();
			for (int i = 0; i < node.getNbChildren(); i++) {
				ClusNode child = (ClusNode)node.getChild(i);
				RowData subset = data.applyWeighted(tst, i);
				computeRecursive(child, subset);
			}
		}
	}

	public void computeForRule(ClusRule rule, ClusSchema schema) {
		RowData covered = new RowData(rule.getData(), schema);
		m_Value = computeICVPairwiseDistances(m_Dist, covered);
	}

	public void computeForRuleSet(ClusRuleSet set, ClusSchema schema) {
		double sumWeight = 0.0;
		for (int i = 0; i < set.getModelSize(); i++) {
			RowData covered = new RowData(set.getRule(i).getData(), schema);
			double weight = covered.getSumWeights();
			m_Value += weight*computeICVPairwiseDistances(m_Dist, covered);
			sumWeight += weight;
		}
		m_ValueWithDefault = m_Value;
		m_Value /= sumWeight;
		RowData defaultData = new RowData(set.getDefaultData(), schema);
		double defWeight = defaultData.getSumWeights();
		m_ValueWithDefault += defWeight * computeICVPairwiseDistances(m_Dist, defaultData);
		sumWeight += defWeight;
		m_ValueWithDefault /= sumWeight;
	}

	public void compute(RowData data, ClusModel model) {
		if (model instanceof ClusNode) {
			ClusNode tree = (ClusNode)model;
			computeRecursive(tree, data);
			m_Value /= data.getSumWeights();
		} else if (model instanceof ClusRuleSet) {
			computeForRuleSet((ClusRuleSet)model, data.getSchema());
		} else if (model instanceof ClusRule) {
			computeForRule((ClusRule)model, data.getSchema());
		}
	}

	public void showModelError(PrintWriter wrt, int detail) {
		StringBuffer res = new StringBuffer();
		res.append(String.valueOf(m_Value));
		if (m_ValueWithDefault != 0.0) {
			res.append(" (with default: "+m_ValueWithDefault+")");
		}
		wrt.println(res.toString());
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new ICVPairwiseDistancesError(getParent(), m_Dist);
	}

	public String getName() {
		return "ICV-Pairwise-Distances";
	}
}

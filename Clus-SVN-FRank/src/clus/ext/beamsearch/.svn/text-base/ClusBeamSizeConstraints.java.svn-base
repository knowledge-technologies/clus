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

/*
 * Created on Apr 25, 2005
 */
package clus.ext.beamsearch;

import clus.algo.tdidt.*;
import clus.statistic.*;

import java.util.*;

public class ClusBeamSizeConstraints {

	protected int m_LeafCount, m_StopCount;
	protected boolean m_IsModified;
	protected boolean m_Debug;

	public void enforce(ClusNode root, int size) {
		reset();
		initVisitors(root, size);
		computeCostUsingConstraints(root, size);
		ClusBeamSizeConstraintInfo info = (ClusBeamSizeConstraintInfo)root.getVisitor();
		pruneUsingConstraints(root, size, info.realcost[size]);
		pruneNonMarkedNodes(root);
		removeVisitors(root);
	}

	public boolean isModified() {
		return m_IsModified;
	}

	public boolean isFinished() {
		return m_StopCount >= m_LeafCount;
	}

	public void reset() {
		m_LeafCount = 0;
		m_StopCount = 0;
		m_IsModified = false;
	}

	public static double computeLowerBound(ClusStatistic stat, int l) {
		ClassificationStat cs = (ClassificationStat)stat;
		double[] clcnts = cs.getClassCounts(0);
		double[] c2 = new double[clcnts.length];
		System.arraycopy(clcnts, 0, c2, 0, c2.length);
		Arrays.sort(c2);
		int s = (l-1)/2;
		double result = 0.0;
		for (int i = s+2; i <= c2.length; i++) {
			result += c2[c2.length - i];
		}
		return result;
	}

	public static void computeCostUsingConstraints(ClusNode node, int l) {
		ClusBeamSizeConstraintInfo info = (ClusBeamSizeConstraintInfo)node.getVisitor();
		if (info.computed[l]) {
			return;
		}
		boolean is_leaf = node.atBottomLevel();
		if (l < 3 || (is_leaf && ((ClusBeamAttrSelector)info.visitor).isStopCrit())) {
			info.realcost[l] = info.lowcost[l] = node.getClusteringStat().getError();
		} else if (is_leaf) {
			info.realcost[l] = node.getClusteringStat().getError();
			info.lowcost[l] = computeLowerBound(node.getClusteringStat(), l);
		} else {
			info.realcost[l] = info.lowcost[l] = node.getClusteringStat().getError();
			ClusNode ch1 = (ClusNode)node.getChild(0);
			ClusNode ch2 = (ClusNode)node.getChild(1);
			ClusBeamSizeConstraintInfo i1 = (ClusBeamSizeConstraintInfo)ch1.getVisitor();
			ClusBeamSizeConstraintInfo i2 = (ClusBeamSizeConstraintInfo)ch2.getVisitor();
			for (int k1 = 1; k1 <= l-2; k1++) {
				int k2 = l - k1 - 1;
				computeCostUsingConstraints(ch1, k1);
				computeCostUsingConstraints(ch2, k2);
				double realcost1 = i1.realcost[k1];
				double realcost2 = i2.realcost[k2];
				if (realcost1 + realcost2 < info.realcost[l]) {
					info.realcost[l] = realcost1 + realcost2;
				}
				double lowcost1 = i1.lowcost[k1];
				double lowcost2 = i2.lowcost[k2];
				if (lowcost1 + lowcost2 < info.lowcost[l]) {
					info.lowcost[l] = lowcost1 + lowcost2;
				}
			}
		}
		info.computed[l] = true;
	}

	public void pruneUsingConstraints(ClusNode node, int l, double b) {
		ClusBeamSizeConstraintInfo info = (ClusBeamSizeConstraintInfo)node.getVisitor();
		info.marked = true;
//		if (m_Debug) System.out.println("Bound: "+b);
//		if (m_Debug) System.out.println("Node: "+node+" Stat: "+node.getTotalStat());
		if (b <= info.bound[l]) {
//			if (m_Debug) System.out.println("b <= info.bound[l]: "+b+" <= "+info.bound[l]);
			return;
		}
		for (int i = 1; i <= l; i++) {
			if (b > info.bound[i]) info.bound[i] = b;
		}
		if (info.lowcost[l] > b || Math.abs(info.lowcost[l] - node.getClusteringStat().getError()) < 1e-12) {
//			if (m_Debug) System.out.println("info.lowcost[l] > b || info.lowcost[l] == node.getTotalStat().getError()"+info.lowcost[l]+" > b = "+b+" err = "+node.getTotalStat().getError());
			return;
		}
		if (l >= 3 && !node.atBottomLevel()) {
			for (int k1 = 1; k1 <= l-2; k1++) {
				int k2 = l - k1 - 1;
				ClusNode ch1 = (ClusNode)node.getChild(0);
				ClusNode ch2 = (ClusNode)node.getChild(1);
				ClusBeamSizeConstraintInfo i1 = (ClusBeamSizeConstraintInfo)ch1.getVisitor();
				ClusBeamSizeConstraintInfo i2 = (ClusBeamSizeConstraintInfo)ch2.getVisitor();
				if (i1.lowcost[k1] + i2.lowcost[k2] <= b) {
					double b1 = b - i2.lowcost[k2];
					double b2 = b - i1.lowcost[k1];
					pruneUsingConstraints(ch1, k1, b1);
					pruneUsingConstraints(ch2, k2, b2);
				}
			}
		}
	}

	public void pruneNonMarkedNodes(ClusNode node) {
		ClusBeamSizeConstraintInfo info = (ClusBeamSizeConstraintInfo)node.getVisitor();
		if (node.atBottomLevel()) {
			m_LeafCount++;
			ClusBeamAttrSelector attrsel = (ClusBeamAttrSelector)info.visitor;
			if (attrsel.isStopCrit()) m_StopCount++;
		} else {
			if (!info.marked) {
				ClusBeamAttrSelector attrsel = new ClusBeamAttrSelector();
				info.visitor = attrsel;
				node.makeLeaf();
				attrsel.setStopCrit(true);
				m_IsModified = true;
			} else {
				for (int i = 0; i < node.getNbChildren(); i++) {
					ClusNode child = (ClusNode)node.getChild(i);
					pruneNonMarkedNodes(child);
				}
			}
		}
	}

	public static void initVisitors(ClusNode node, int size) {
		ClusBeamSizeConstraintInfo info = new ClusBeamSizeConstraintInfo(size);
		info.visitor = node.getVisitor();
		node.setVisitor(info);
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			initVisitors(child, size);
		}
	}

	public static void removeVisitors(ClusNode node) {
		/* Restore old visitors */
		ClusBeamSizeConstraintInfo info = (ClusBeamSizeConstraintInfo)node.getVisitor();
		node.setVisitor(info.visitor);
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode child = (ClusNode)node.getChild(i);
			removeVisitors(child);
		}
	}

	public void setDebug(boolean debug) {
		m_Debug = debug;
	}
}

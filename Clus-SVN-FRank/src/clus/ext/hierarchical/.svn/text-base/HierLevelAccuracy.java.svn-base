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
 * Created on May 23, 2005
 */
package clus.ext.hierarchical;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

import clus.data.rows.*;
import clus.error.*;
import clus.main.Settings;
import clus.statistic.*;

public class HierLevelAccuracy extends ClusError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected ClassHierarchy m_Hier;
	protected double[] m_CorrectLevel;
	protected double[] m_CountLevel;
	protected double m_Correct;
	protected double m_Predicted;
	protected boolean[] m_ActualArr;
	protected boolean[] m_PredLevelErr;
	protected int m_MaxDepth;

	public HierLevelAccuracy(ClusErrorList par, ClassHierarchy hier) {
		super(par, hier.getMaxDepth());
		m_Hier = hier;
		m_CorrectLevel = new double[m_Dim];
		m_CountLevel = new double[m_Dim];
		m_ActualArr = new boolean[hier.getTotal()];
		m_PredLevelErr = new boolean[m_Dim];
	}

	/*
	public boolean shouldBeLow() {
		return false;
	}
	*/

	public void update(ClassTerm node, int depth, double[] predarr) {
		boolean has_pred = predarr[node.getIndex()] >= 0.5;
		boolean has_actual = m_ActualArr[node.getIndex()];
		if (has_pred || has_actual) {
			if (depth > m_MaxDepth) m_MaxDepth = depth;
		}
		if (has_pred != has_actual) {
			m_PredLevelErr[depth] = true;
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			update((ClassTerm)node.getChild(i), depth+1, predarr);
		}
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
/*		if (!((WHTDStatistic)pred).getMeanTuple().isRoot()) {
			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
			tp.toBoolVector(m_ActualArr);
			double[] predarr = ((WHTDStatistic)pred).getDiscretePred();
			Arrays.fill(m_PredLevelErr, false);
			m_MaxDepth = -1;
			// Now scans entiry hierarchy, could be made more eff.
			update(m_Hier.getRoot(), 0, predarr);
			boolean allok = true;
			for (int i = 0; i <= m_MaxDepth; i++) {
				if (m_PredLevelErr[i]) {
					allok = false;
				} else {
					m_CorrectLevel[i] += 1.0;
				}
				m_CountLevel[i] += 1.0;
			}
			if (allok) {
				m_Correct += 1.0;
			}
			m_Predicted += 1.0;
		}	*/
	}

	public void addInvalid(DataTuple tuple) {
	}

	public double getModelError() {
		int nb = getNbExamples();
		return nb == 0 ? 0.0 : 1.0 - m_Correct / nb;
	}

	public double getErrorComp(int i) {
		double nb = m_CountLevel[i];
		return nb == 0.0 ? 0.0 : m_CorrectLevel[i] / nb;
	}

	/*should be named getPrecision()*/
	public double getAccuracy() {
		return m_Predicted == 0.0 ? 0.0 : m_Correct / m_Predicted;
	}

	public double getRecall() {
		int nb = getNbExamples();
		return nb == 0 ? 0.0 : m_Predicted / nb;
	}

	/* should be named getRecall()*/
	public double getOverallAccuracy() {
		int nb = getNbExamples();
		return nb == 0 ? 0.0 : m_Correct / nb;
	}

	public String getName() {
		return "Hierarchical accuracy by level";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new HierLevelAccuracy(par, m_Hier);
	}

	public void reset() {
		Arrays.fill(m_CorrectLevel, 0.0);
		Arrays.fill(m_CountLevel, 0.0);
		m_Correct = 0.0;
		m_Predicted = 0.0;
	}

	public void add(ClusError other) {
		HierLevelAccuracy acc = (HierLevelAccuracy)other;
		m_Correct += acc.m_Correct;
		m_Predicted += acc.m_Predicted;
		for (int i = 0; i < m_Dim; i++) {
			m_CorrectLevel[i] += acc.m_CorrectLevel[i];
			m_CountLevel[i] += acc.m_CountLevel[i];
		}
	}

	public void showModelError(PrintWriter out, int detail) {
		NumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		// int nb = getNbExamples();
		for (int i = 0; i < m_Dim; i++) {
			if (i != 0) buf.append(",");
			buf.append(fr.format(getErrorComp(i)));
		}
		buf.append("]");
		buf.append(", Acc: ");
		buf.append(fr.format(getAccuracy()));
		buf.append(", Rec: ");
		buf.append(fr.format(getRecall()));
		buf.append(", AccAll: ");
		buf.append(fr.format(getOverallAccuracy()));
		out.println(buf.toString());
	}
}

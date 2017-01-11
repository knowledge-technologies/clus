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
 * Created on May 26, 2005
 */
package clus.ext.hierarchical;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;

import clus.util.*;
import clus.data.rows.*;
import clus.error.*;
import clus.main.*;
import clus.statistic.*;

public class HierClassWiseAccuracy extends ClusError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected boolean m_NoTrivialClasses = false;
	protected ClassHierarchy m_Hier;
	protected double[] m_NbPosPredictions;
	protected double[] m_TP;
	protected double[] m_NbPosActual;
	protected boolean[] m_EvalClass;

	public HierClassWiseAccuracy(ClusErrorList par, ClassHierarchy hier) {
		super(par, hier.getTotal());
		m_Hier = hier;
		m_EvalClass = hier.getEvalClassesVector();
		m_NbPosPredictions = new double[m_Dim];
		m_TP = new double[m_Dim];
		m_NbPosActual = new double[m_Dim];
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		//System.out.println("tuple: "+tuple.toString());
		ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
		boolean[] predarr = ((WHTDStatistic)pred).getDiscretePred();
		for (int i = 0; i < m_Dim; i++) {
			if (predarr[i]) {
				/* Predicted this class, was it correct? */
				//System.out.println("Ex: "+tuple.toString());
				//System.out.println(pred.getClassString()+" "+m_Hier.getTermAt(i).toStringHuman(m_Hier));
				m_NbPosPredictions[i] += 1.0;
				if (tp.hasClass(i)) {
					m_TP[i] += 1.0;
				}
			}
		}
		tp.updateDistribution(m_NbPosActual, 1.0);
	}

	public void addInvalid(DataTuple tuple) {
		ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
		tp.updateDistribution(m_NbPosActual, 1.0);
	}

	public boolean isComputeForModel(String name) {
		if (name.equals("Default")) return false;
		if (name.equals("Original")) return false;
		return true;
	}

	public boolean isNoTrivialClasses() {
		return m_NoTrivialClasses;
	}

	public boolean isEvalClass(int idx) {
		if (isNoTrivialClasses()) {
			// Do not consider classes with default precision = 1.0
			if (m_NbPosActual[idx] == getNbTotal()) {
				return false;
			}
		}
		return m_EvalClass[idx];
	}

	public double getPrecision() {
		double tot_corr = getTP();
		double tot_pred = getSumNbPosPredicted();
		return tot_pred == 0.0 ? 0.0 : tot_corr/tot_pred;
	}

	public double getRecall() {
		double tot_corr = getTP();
		double tot_def = getSumNbPosActual();
		return tot_def == 0 ? 0.0 : tot_corr / tot_def;
	}

	public int getTP() {
		int tot_corr = 0;
		for (int i = 0; i < m_Dim; i++) {
			if (isEvalClass(i)) tot_corr += m_TP[i];
		}
		return tot_corr;
	}

	public int getFP() {
		int tot_pred = getSumNbPosPredicted();
		int tot_corr = getTP();
		return tot_pred - tot_corr;
	}

	public int getFN() {
		int tot_def = getSumNbPosActual();
		int tot_corr = getTP();
		return tot_def - tot_corr;
	}

	public int getSumNbPosActual() {
		int tot_def = 0;
		for (int i = 0; i < m_Dim; i++) {
			if (isEvalClass(i)) tot_def += m_NbPosActual[i];
		}
		return tot_def;
	}

	public int getSumNbPosPredicted() {
		int tot_pred = 0;
		for (int i = 0; i < m_Dim; i++) {
			if (isEvalClass(i)) tot_pred += m_NbPosPredictions[i];
		}
		return tot_pred;
	}

	public int getNbPosExamplesCheck(){
		return getTP() + getFN();
	}

	public double getMacroAvgPrecision() {
		int cnt = 0;
		double avg = 0.0;
		for (int i = 0; i < m_Dim; i++) {
			if (m_NbPosPredictions[i] != 0 && isEvalClass(i)) {
				cnt++;
				avg += m_TP[i] / m_NbPosPredictions[i];
			}
		}
		return cnt == 0 ? 0 : avg/cnt;
	}

	public void reset() {
		Arrays.fill(m_TP, 0.0);
		Arrays.fill(m_NbPosPredictions, 0.0);
		Arrays.fill(m_NbPosActual, 0.0);
	}

	public void add(ClusError other) {
		HierClassWiseAccuracy acc = (HierClassWiseAccuracy)other;
		for (int i = 0; i < m_Dim; i++) {
			m_TP[i] += acc.m_TP[i];
			m_NbPosPredictions[i] += acc.m_NbPosPredictions[i];
			m_NbPosActual[i] += acc.m_NbPosActual[i];
		}
	}

	// For errors computed on a subset of the examples, it is sometimes useful
	// to also have information about all the examples, this information is
	// passed via this method in the global error measure "global"
	public void updateFromGlobalMeasure(ClusError global) {
		HierClassWiseAccuracy other = (HierClassWiseAccuracy)global;
		System.arraycopy(other.m_NbPosActual, 0, m_NbPosActual, 0, m_NbPosActual.length);
	}

	// prints the evaluation results for each single predicted class
	// added a value for recall (next to def and acc)
	public void printNonZeroAccuraciesRec(NumberFormat fr, PrintWriter out, ClassTerm node, boolean[] printed) {
		int idx = node.getIndex();
		// avoid printing a given node several times
		if (printed[idx]) return;
		printed[idx] = true;
		if (m_NbPosPredictions[idx] != 0.0 && isEvalClass(idx)) {
			int nb = getNbTotal();
			double def = nb == 0 ? 0.0 : m_NbPosActual[idx]/nb;
			//added a test
			double prec = m_NbPosPredictions[idx] == 0.0 ? 0.0 : m_TP[idx]/m_NbPosPredictions[idx];
			//this line is added
			double rec = m_NbPosActual[idx] == 0.0 ? 0.0 : m_TP[idx]/m_NbPosActual[idx];
			//added some more lines for calculationg, TP, FP, nbPosExamples
			int TP = (int)m_TP[idx];
			int FP = (int)(m_NbPosPredictions[idx] - m_TP[idx]); //TODO: some kind of checking?
			int nbPos = (int)m_NbPosActual[idx];
			ClassesValue val = new ClassesValue(node);
			//adapted output somewhat for clarity
			out.print("      "+val.toStringWithDepths(m_Hier));
			out.print(", def: "+fr.format(def));
			out.print(", prec: "+fr.format(prec));
			out.print(", rec: "+fr.format(rec));
			out.print(", TP: "+fr.format(TP)+", FP: "+fr.format(FP)+", nbPos: "+fr.format(nbPos));
			out.println();
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			printNonZeroAccuraciesRec(fr, out, (ClassTerm)node.getChild(i), printed);
		}
	}

	public void printNonZeroAccuracies(NumberFormat fr, PrintWriter out, ClassHierarchy hier) {
		boolean[] printed = new boolean[hier.getTotal()];
		ClassTerm node = hier.getRoot();
		for (int i = 0; i < node.getNbChildren(); i++) {
			printNonZeroAccuraciesRec(fr, out, (ClassTerm)node.getChild(i), printed);
		}
	}

	// does it make sense to make averages of TP, FP and nbPos (look into this: methods implemented but not used)
	public void showModelError(PrintWriter out, int detail) {
		NumberFormat fr1 = getFormat();
		NumberFormat fr2 = ClusFormat.SIX_AFTER_DOT;
		out.print("precision: "+fr2.format(getPrecision()));
		out.print(", recall: "+fr2.format(getRecall()));
		out.print(", coverage: "+fr2.format(getCoverage()));
		out.print(", TP: "+getTP()+", FP: "+getFP()+", nbPos: "+getSumNbPosActual());
		out.println();
		printNonZeroAccuracies(fr1, out, m_Hier);
	}

	public String getName() {
		return "Hierarchical accuracy by class";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new HierClassWiseAccuracy(par, m_Hier);
	}

	public void nextPrediction(int cls, boolean predicted_class, boolean actually_has_class) {
		if (predicted_class) {
			/* Predicted this class, was it correct? */
			m_NbPosPredictions[cls] += 1.0;
			if (actually_has_class) {
				m_TP[cls] += 1.0;
			}
		}
		if (actually_has_class) m_NbPosActual[cls] += 1.0;
	}
}

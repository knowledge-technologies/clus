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
import jeans.util.*;

import clus.data.rows.DataTuple;
import clus.data.type.*;
import clus.main.*;
import clus.statistic.ClusStatistic;
import clus.util.*;

public class ContingencyTable extends ClusNominalError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected final static String REAL_PRED = "REAL\\PRED";

	protected int[][][] m_ContTable;

	public ContingencyTable(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_ContTable = new int[m_Dim][][];
		for (int i = 0; i < m_Dim; i++) {
			// also add a column for "?" for the semi-supervised setting
			int size = m_Attrs[i].getNbValuesInclMissing();
			m_ContTable[i] = new int[size][size];
		}
	}

	public boolean isMultiLine() {
		return true;
	}
	
	public int calcNbTotal(int k) {
		int sum = 0;
		int size = m_Attrs[k].getNbValues();
		int[][] table = m_ContTable[k];
		for (int i = 0; i < size; i++) {		
			for (int j = 0; j < size; j++) {
				sum += table[i][j];
			}
		}
		return sum;
	}

	public int calcNbCorrect(int k) {
		int sum = 0;
		int size = m_Attrs[k].getNbValues();
		int[][] table = m_ContTable[k];
		for (int j = 0; j < size; j++) {
			sum += table[j][j];
		}
		return sum;
	}	

	public double calcXSquare(int k) {
		int size = m_Attrs[k].getNbValues();
		int[] ri = new int[size];
		int[] cj = new int[size];
		for (int j = 0; j < size; j++) {
			ri[j] = sumRow(k, j);
			cj[j] = sumColumn(k, j);
		}
		double xsquare = 0.0;
		int nb = getNbExamples();
		int[][] table = m_ContTable[k];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				double eij = (double)ri[i]*cj[j]/nb;
				double err = (double)table[i][j] - eij;
				if (err != 0.0)	xsquare += err*err/eij;
			}
		}
		return xsquare;
	}

	public double calcCramerV(int k) {
		int q = m_Attrs[k].getNbValues();
		int n = calcNbTotal(k);
		double div = (double)n*(q-1);
		return Math.sqrt(calcXSquare(k)/div);
	}

	public double calcAccuracy(int k) {
		return (double)calcNbCorrect(k)/calcNbTotal(k);
	}

	public double calcDefaultAccuracy(int i) {
		return 0.0;
	}

	public double getModelErrorComponent(int i) {
		return calcAccuracy(i);
	}

	public double getModelComponent() {
		double sum = 0.0;
		for (int i = 0; i < m_Dim; i++) {
			sum += calcAccuracy(i);
		}
		return sum/m_Dim;
	}

	public void showAccuracy(PrintWriter out, int i) {
		int nbcorr = calcNbCorrect(i);
		int nbtot = calcNbTotal(i);
		double acc = (double)nbcorr/nbtot;
		out.print("Accuracy: "+ClusFormat.SIX_AFTER_DOT.format(acc));
		//+" = "+nbcorr+"/"+nbtot);
		out.println();
	}

	public void add(ClusError other) {
		ContingencyTable cont = (ContingencyTable)other;
		for (int i = 0; i < m_Dim; i++) {
			int t1[][] = m_ContTable[i];
			int t2[][] = cont.m_ContTable[i];
			int size = t1.length;
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					t1[j][k] += t2[j][k];
				}
			}
		}
	}

	public void showModelError(PrintWriter out, int detail) {
		if (detail == DETAIL_VERY_SMALL) {
			out.print(getPrefix()+"[");
			for (int i = 0; i < m_Dim; i++) {
				if (i != 0) out.print(",");
				double acc = calcAccuracy(i);
				out.print(ClusFormat.SIX_AFTER_DOT.format(acc));
			}
			out.println("]");
		} else {
			for (int i = 0; i < m_Dim; i++) {
				out.println();
				out.println(getPrefix()+"Attribute: "+m_Attrs[i].getName());
				showContTable(out, i);
			}
		}
	}

	public int sumColumn(int[][] table, int j) {
		int sum = 0;
		for (int i = 0; i < table.length; i++)
			sum += table[i][j];
		return sum;
	}

	public int sumRow(int[][] table, int i) {
		int sum = 0;
		for (int j = 0; j < table.length; j++)
			sum += table[i][j];
		return sum;
	}
	
	public int sumColumn(int k, int j) {
		int sum = 0;
		int size = m_Attrs[k].getNbValues();
		int[][] table = m_ContTable[k];				
		for (int i = 0; i < size; i++)
			sum += table[i][j];
		return sum;
	}

	public int sumRow(int k, int i) {
		int sum = 0;
		int size = m_Attrs[k].getNbValues();
		int[][] table = m_ContTable[k];				
		for (int j = 0; j < size; j++)
			sum += table[i][j];
		return sum;
	}

	public void showContTable(PrintWriter out, int i) {
		int[][] table = m_ContTable[i];
		int size = m_Attrs[i].getNbValues();
		if (m_Attrs[i].hasMissing()) {
			// also add a column for "?" for the semi-supervised setting
			size++;
		}
		// Calculate sizes
		int[] wds = new int[size+2];
		// First column
		wds[0] = REAL_PRED.length();
		for (int j = 0; j < size; j++) {
			wds[j+1] = m_Attrs[i].getValueOrMissing(j).length()+1;
		}
		// Middle columns
		for (int j = 0; j < size; j++) {
			wds[0] = Math.max(wds[0], m_Attrs[i].getValueOrMissing(j).length());
			for (int k = 0; k < size; k++) {
				String str = String.valueOf(table[j][k]);
				wds[k+1] = Math.max(wds[k+1], str.length()+1);
			}
			String str = String.valueOf(sumRow(table, j));
			wds[size+1] = Math.max(wds[size+1], str.length()+1);
		}
		// Bottom row
		for (int k = 0; k < size; k++) {
			String str = String.valueOf(sumColumn(table, k));
			wds[k+1] = Math.max(wds[k+1], str.length()+1);
		}
		// Total sum
		wds[size+1] = Math.max(wds[size+1], String.valueOf(getNbExamples()).length()+1);
		// Calculate line width
		int s = 0;
		for (int j = 0; j < size+2; j++) s += wds[j];
		String horiz = getPrefix()+"  "+StringUtils.makeString('-', s+(size+1)*2);
		// Header
		out.print(getPrefix()+"  ");
		printString(out, wds[0], REAL_PRED);
		out.print(" |");
		for (int j = 0; j < size; j++) {
			printString(out, wds[j+1], m_Attrs[i].getValueOrMissing(j));
			out.print(" |");
		}
		out.println();
		out.println(horiz);
		// Data rows
		for (int j = 0; j < size; j++) {
			out.print(getPrefix()+"  ");
			printString(out, wds[0], m_Attrs[i].getValueOrMissing(j));
			out.print(" |");
			for (int k = 0; k < size; k++) {
				printString(out, wds[k+1], String.valueOf(table[j][k]));
				out.print(" |");
			}
			printString(out, wds[size+1], String.valueOf(sumRow(table, j)));
			out.println();
		}
		out.println(horiz);
		out.print(getPrefix()+"  ");
		out.print(StringUtils.makeString(' ', wds[0]));
		out.print(" |");
		for (int k = 0; k < size; k++) {
			printString(out, wds[k+1], String.valueOf(sumColumn(table, k)));
			out.print(" |");
		}
		printString(out, wds[size+1], String.valueOf(getNbExamples()));
		out.println();
		out.print(getPrefix()+"  ");
		showAccuracy(out, i);
		out.print(getPrefix()+"  ");
		double cramer = calcCramerV(i);
		out.println("Cramer's coefficient: "+ClusFormat.SIX_AFTER_DOT.format(cramer));
		out.println();
	}

	public void showSummaryError(PrintWriter out, boolean detail) {
		if (!detail) {
			for (int i = 0; i < m_Dim; i++) {
				out.print(getPrefix()+"Attribute: "+m_Attrs[i].getName()+" - ");
				showAccuracy(out, i);
			}
		}
	}

	public void printString(PrintWriter out, int wd, String str) {
		out.print(StringUtils.makeString(' ', wd-str.length()));
		out.print(str);
	}

	public String getName() {
		return "Classification Error";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new ContingencyTable(par, m_Attrs);
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred();
		for (int i = 0; i < m_Dim; i++) {
			m_ContTable[i][getAttr(i).getNominal(tuple)][predicted[i]]++;
		}
	}

	public void addInvalid(DataTuple tuple) {
	}

// FIXME: do we still need these (?):

		public double get_error_classif(){
			//System.out.println("nb of examples : "+getNbExamples());
			//System.out.println("nb of correctly classify examples : "+calcNbCorrect(m_ContTable[0]));
		return (1 - get_accuracy());
		}

		public double get_TP(){
			return calcNbCorrect(0);
		}

		public double get_accuracy() {
			return calcAccuracy(0);
		}

		public double get_precision() {
			return 0.0;
		}

		public double get_recall() {
			return 0.0;
		}

		public double get_auc() {
			return 0.0;
		}
}

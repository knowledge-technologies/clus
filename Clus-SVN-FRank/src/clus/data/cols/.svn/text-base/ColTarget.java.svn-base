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

package clus.data.cols;

import clus.statistic.*;
import clus.selection.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;

public class ColTarget {

	public int m_NbNumeric, m_NbNominal, m_NbRows;
	public double[][] m_Numeric;
	public int[][] m_Nominal;
	public ClusNode[] m_Node;

	public ColTarget(ClusSchema schema) {
		// m_NbNumeric = schema.getNbTarNum();
		// m_NbNominal = schema.getNbTarNom();
	}

	public ClusNode[] getNodes() {
		return m_Node;
	}

	public int getNbAttributes() {
		return m_NbNumeric + m_NbNominal;
	}

	public int getNbNum() {
		return m_NbNumeric;
	}

	public int getNbNom() {
		return m_NbNominal;
	}

	public int getNbRows() {
		return m_NbRows;
	}

	public void setNbRows(int nb) {
		m_NbRows = nb;
	}

	public void setData(double[][] num, int[][] nom, int nbrows) {
		m_Numeric = num;
		m_Nominal = nom;
		m_NbRows = nbrows;
	}

	public void resize(int nbrows) {
		m_NbRows = nbrows;
		if (m_NbNumeric != 0) m_Numeric = new double[m_NbRows][m_NbNumeric];
		if (m_NbNominal != 0) m_Nominal = new int[m_NbRows][m_NbNominal];
	}

	public ColTarget select(ClusSelection sel, int nbsel) {
		double[][] numsubset = selectNumeric(sel, nbsel);
		int[][] nomsubset = selectNominal(sel, nbsel);
		setNbRows(m_NbRows - nbsel);
		ColTarget s_targ = null;
		s_targ.setData(numsubset, nomsubset, nbsel);
		return s_targ;
	}

	public double[][] selectNumeric(ClusSelection sel, int nbsel) {
		if (m_Numeric == null) return null;
		int s_data = 0;
		int s_subset = 0;
		double[][] data = m_Numeric;
		m_Numeric = new double[m_NbRows-nbsel][];
		double[][] subset = new double[nbsel][];
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) subset[s_subset++] = data[i];
			else m_Numeric[s_data++] = data[i];
		}
		return subset;
	}

	public int[][] selectNominal(ClusSelection sel, int nbsel) {
		if (m_Nominal == null) return null;
		int s_data = 0;
		int s_subset = 0;
		int[][] data = m_Nominal;
		m_Nominal = new int[m_NbRows-nbsel][];
		int[][] subset = new int[nbsel][];
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) subset[s_subset++] = data[i];
			else m_Nominal[s_data++] = data[i];
		}
		return subset;
	}

	public void insert(ColTarget target, ClusSelection sel, int nb_new) {
		insertNumeric(target, sel, nb_new);
		insertNominal(target, sel, nb_new);
		m_NbRows = nb_new;
	}

	public void insertNumeric(ColTarget target, ClusSelection sel, int nb_new) {
		if (m_Numeric == null) return;
		int s_data = 0;
		int s_subset = 0;
		double[][] data = m_Numeric;
		m_Numeric = new double[nb_new][];
		double[][] subset = ((ColTarget)target).m_Numeric;
		for (int i = 0; i < nb_new; i++) {
			if (sel.isSelected(i)) m_Numeric[i] = subset[s_subset++];
			else m_Numeric[i] = data[s_data++];
		}
	}

	public void insertNominal(ColTarget target, ClusSelection sel, int nb_new) {
		if (m_Nominal == null) return;
		int s_data = 0;
		int s_subset = 0;
		int[][] data = m_Nominal;
		m_Nominal = new int[nb_new][];
		int[][] subset = ((ColTarget)target).m_Nominal;
		for (int i = 0; i < nb_new; i++) {
			if (sel.isSelected(i)) m_Nominal[i] = subset[s_subset++];
			else m_Nominal[i] = data[s_data++];
		}
	}

	public void addToRoot(ClusNode info) {
		m_Node = new ClusNode[m_NbRows];
		for (int i = 0; i < m_NbRows; i++) {
			m_Node[i] = info;
		}
	}

	public void normalize() {
/*		RegressionStat stat = new RegressionStat(m_Schema);
		stat.calcTotal();
		for (int i = 0; i < m_NbNumeric; i++) {
			double mean = stat.getMean(i);
			double stdev = Math.sqrt(stat.getVariance(i));
			m_TransA[i] = stdev;
			m_TransB[i] = mean;
			for (int j = 0; j < m_NbRows; j++) {
				m_Numeric[j][i] = (m_Numeric[j][i] - mean)/stdev;
			}
		}*/
	}

	public void calcTotalStat(ClusStatistic stat) {
		for (int i = 0; i < m_NbRows; i++) stat.update(this, i);
	}

	public void setNumeric(int idx, int row, double data) {
		m_Numeric[row][idx] = data;
	}

	public void setNominal(int idx, int row, int data) {
		m_Nominal[row][idx] = data;
	}

	public double[] getNumeric(int i) {
		return m_Numeric[i];
	}

	public int[] getNominal(int i) {
		return m_Nominal[i];
	}
}

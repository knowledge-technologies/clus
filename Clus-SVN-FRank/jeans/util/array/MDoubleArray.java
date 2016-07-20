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

package jeans.util.array;

import jeans.util.MStreamTokenizer;

import java.io.*;

public class MDoubleArray {

	protected double[] m_hArray;
	protected int m_lSize;
	protected int m_lLen;

	public final static void add(double[] arr, double[] arr1) {
		for (int i = 0; i < arr.length; i++)
			arr[i] += arr1[i];
	}

	public final static void add(double[] arr, double[] arr1, double weight) {
		for (int i = 0; i < arr.length; i++)
			arr[i] += weight*arr1[i];
	}

	public final static void divide(double[] arr, double[] arr1) {
		for (int i = 0; i < arr.length; i++)
			if (arr1[i] != 0.0) arr[i] /= arr1[i];
	}

	public final static void subtractFromThis(double[] arr, double[] other) {
		for (int i = 0; i < arr.length; i++)
			arr[i] -= other[i];
	}

	public final static void subtractFromOther(double[] arr, double[] other) {
		for (int i = 0; i < arr.length; i++)
			arr[i] = other[i] - arr[i];
	}

	public final static void dotscalar(double[] arr, double fac) {
		for (int i = 0; i < arr.length; i++) arr[i] *= fac;
	}

	public final static double[] clone(double[] arr) {
		double[] clone = new double[arr.length];
		System.arraycopy(arr, 0, clone, 0, arr.length);
		return clone;
	}

	public void read(MStreamTokenizer tokens, char st, char ed) throws IOException {
		tokens.readChar(st);
		int idx = 0;
		int ch = tokens.getCharToken();
		if (ch == ed) {
			m_lLen = 0;
			return;
		}
		tokens.pushBackChar(ch);
		while (true) {
			// Read double
			double val = Double.parseDouble(tokens.readToken());
			if (idx >= m_lSize) grow();
			m_hArray[idx++] = val;
			// Read sep or end
			ch = tokens.getCharToken();
			if (ch == ed) {
				m_lLen = idx;
				return;
			}
		}
	}

	public double[] toArray() {
		double[] nArr = new double[m_lLen];
		System.arraycopy(m_hArray, 0, nArr, 0, m_lLen);
		return nArr;
	}

	public void clear() {
		m_lSize = 0;
		m_hArray = null;
	}

	public void grow() {
		int nSize = m_lSize * 3/2 + 10;
		double[] nArr = new double[nSize];
		if (m_hArray != null) System.arraycopy(m_hArray, 0, nArr, 0, m_lSize);
		m_lSize = nSize;
		m_hArray = nArr;
	}

	public static String toString(double[] arr) {
		String str = "[";
		for (int ctr = 0; ctr < arr.length; ctr++) {
			if (ctr != 0) str += ", ";
			str += String.valueOf(arr[ctr]);
		}
		return str+ "]";
	}

	public String toString() {
		String str = "[";
		for (int ctr = 0; ctr < m_lLen; ctr++) {
			if (ctr != 0) str += ", ";
			str += String.valueOf(m_hArray[ctr]);
		}
		return str+ "]";
	}

	public static double max(double[] values) {
		double m = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > m) m = values[i];
		}
		return m;
	}
}

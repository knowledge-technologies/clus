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

public class MyIntArray {

	private int[] m_Ints;
	private int m_Size;
	private int m_Mult, m_Div;

	public MyIntArray() {
		m_Ints = new int[0];
		m_Size = 0;
		m_Mult = 2;
		m_Div = 1;
	}

	public MyIntArray(int cap) {
		m_Ints = new int[cap];
		m_Size = 0;
		m_Mult = 2;
		m_Div = 1;
	}

	public MyIntArray(int cap, int mult, int div) {
		m_Ints = new int[cap];
		m_Size = 0;
		m_Mult = mult;
		m_Div = div;
	}

	public final int binarySearch(int key) {
		int low = 0;
		int high = m_Size-1;
		while (low <= high) {
			int mid =(low + high)/2;
			int midVal = m_Ints[mid];
			if (midVal < key) low = mid + 1;
			else if (midVal > key) high = mid - 1;
			else return mid;
		}
		return -(low + 1);
	}

	public static int[] remove(int e, int[] a) {
		int idx = 0;
		int[] r = new int[a.length-1];
		for (int i = 0; i < a.length; i++) if (a[i] != e) r[idx++] = a[i];
		return r;
	}

	public static int[] mergeSorted(int[] a1, int[] a2) {
		int idx = 0;
		int a1p = 0;
		int a2p = 0;
		int[] a3 = new int[a1.length + a2.length];
		while (a1p < a1.length && a2p < a2.length) {
			if (a1[a1p] <= a2[a2p]) a3[idx++] = a1[a1p++];
			else a3[idx++] = a2[a2p++];
		}
		for (int i = a1p; i < a1.length; i++) a3[idx++] = a1[i];
		for (int i = a2p; i < a2.length; i++) a3[idx++] = a2[i];
		return a3;
	}

	public static int isIntersectSorted(int[] a1, int[] a2) {
		int a1p = 0;
		int a2p = 0;
		while (a1p < a1.length && a2p < a2.length) {
			if (a1[a1p] == a2[a2p]) {
				return 1;
			} else 	if (a1[a1p] < a2[a2p]) {
				a1p++;
			} else {
				a2p++;
			}
		}
		return 0;
	}

	public static int[] intersectSorted(int[] a1, int[] a2) {
		int nb = 0;
		int a1p = 0;
		int a2p = 0;
		while (a1p < a1.length && a2p < a2.length) {
			if (a1[a1p] == a2[a2p]) {
				nb++; a1p++; a2p++;
			} else 	if (a1[a1p] < a2[a2p]) {
				a1p++;
			} else {
				a2p++;
			}
		}
		int idx = 0;
		a1p = a2p = 0;
		int[] r = new int[nb];
		if (nb > 0) {
			while (a1p < a1.length && a2p < a2.length) {
				if (a1[a1p] == a2[a2p]) {
					r[idx++] = a1[a1p];
					a1p++; a2p++;
				} else 	if (a1[a1p] < a2[a2p]) {
					a1p++;
				} else {
					a2p++;
				}
			}
		}
		return r;
	}

	public static String print(int[] arr) {
		if (arr.length == 0) return "[]";
		StringBuffer buff = new StringBuffer();
		buff.append('[');
		buff.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			buff.append(',');
			buff.append(arr[i]);
		}
		buff.append(']');
		return buff.toString();
	}

	public void removeMinusOnes() {
		int delta = 0;
		for (int i = 0; i < m_Size; i++) {
			while (i < m_Size && m_Ints[i+delta] == -1) {
				delta++;
				m_Size--;
			}
			if (i < m_Size) m_Ints[i] = m_Ints[i+delta];
		}
	}

	public final void addElement(int element) {
		int[] newints;
		if (m_Size == m_Ints.length) {
			newints = new int[m_Mult*m_Ints.length/m_Div +	1];
			System.arraycopy(m_Ints, 0, newints, 0, m_Size);
			m_Ints = newints;
		}
		m_Ints[m_Size++] = element;
	}

	public final int elementAt(int index) {
		return m_Ints[index];
	}

	public final int[] elements() {
		return m_Ints;
	}

	public final void insertElementAt(int element, int index) {
		int[] nObjs;
		if (m_Size < m_Ints.length) {
			System.arraycopy(m_Ints, index, m_Ints, index + 1, m_Size - index);
			m_Ints[index] = element;
		} else {
			nObjs = new int[m_Mult*m_Ints.length/m_Div +	1];
			System.arraycopy(m_Ints, 0, nObjs, 0, index);
			nObjs[index] = element;
			System.arraycopy(m_Ints, index, nObjs, index + 1, m_Size - index);
			m_Ints = nObjs;
		}
		m_Size++;
	}

	public final void removeElementAt(int index) {
		System.arraycopy(m_Ints, index + 1, m_Ints, index, m_Size - index - 1);
	}

	public final void removeAllElements() {
		m_Size = 0;
	}

	public final void setElementAt(int element, int index) {
		m_Ints[index] = element;
		if (index >= m_Size) m_Size = index+1;
	}

	public final int size() {
		return m_Size;
	}

	public final void setCapacity(int cap) {
		int[] newints = new int[cap];
		System.arraycopy(m_Ints, 0, newints, 0, Math.min(cap, m_Size));
		m_Ints = newints;
		if (m_Ints.length < m_Size) m_Size = m_Ints.length;
	}

	public final void swap(int first, int second) {
		int help = m_Ints[first];
		m_Ints[first] = m_Ints[second];
		m_Ints[second] = help;
	}

	public final void trimToSize() {
		int[] newints = new int[m_Size];
		System.arraycopy(m_Ints, 0, newints, 0, m_Size);
		m_Ints = newints;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < m_Size; i++) {
			if (i != 0) buf.append(",");
			buf.append(String.valueOf(m_Ints[i]));
		}
		buf.append("]");
		return buf.toString();
	}
}

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

package jeans.util.sort;

public class DoubleIndexSorter {

	protected static DoubleIndexSorter m_Instance;
	protected double[] m_Data;
	protected int[] m_Index;

	public static DoubleIndexSorter getInstance() {
		if (m_Instance == null) m_Instance = new DoubleIndexSorter();
		return m_Instance;
	}

	public final void setData(double[] data) {
		m_Data = data;
		int nb = data.length;
		m_Index = new int[nb];
		for (int i = 0; i < nb; i++) m_Index[i] = i;
	}

	public final void setData(double[] data, int[] index) {
		m_Data = data;
		m_Index = index;
	}

	public static double[] arrayclone(double[] data) {
		double[] res = new double[data.length];
		System.arraycopy(data, 0, res, 0, data.length);
		return res;
	}

	public static double[] unsort(double[] data, int[] index) {
		int nb = data.length;
		double[] ndata = new double[nb];
		for (int i = 0; i < nb; i++) {
			ndata[index[i]] = data[i];	// m_Index[i] is 'real' position of value
		}
		return ndata;
	}

	public final void sort() {
		quickSort(0, m_Data.length);
	}

	public final int[] getIndex() {
		return m_Index;
	}

    private final void quickSort(int off, int len) {
	// Insertion sort on smallest arrays
	if (len < 7) {
	    for (int i=off; i<len+off; i++)
		for (int j=i; j>off && m_Data[j-1]<=m_Data[j]; j--)
		    swap(j, j-1);
	    return;
	}

	// Choose a partition element, v
	int m = off + len/2;       // Small arrays, middle element
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {        // Big arrays, pseudomedian of 9
		int s = len/8;
		l = med3(l,     l+s, l+2*s);
		m = med3(m-s,   m,   m+s);
		n = med3(n-2*s, n-s, n);
	    }
	    m = med3(l, m, n); // Mid-size, med of 3
	}
	double v = m_Data[m];

	// Establish Invariant: v* (<v)* (>v)* v*
	int a = off, b = a, c = off + len - 1, d = c;
	while(true) {
	    while (b <= c && m_Data[b] > v) {
		if (m_Data[b] == v)
		    swap(a++, b);
		b++;
	    }
	    while (c >= b && m_Data[c] < v) {
		if (m_Data[c] == v)
		    swap(c, d--);
		c--;
	    }
	    if (b > c)
		break;
	    swap(b++, c--);
	}

	// Swap partition elements back to middle
	int s, n = off + len;
	s = Math.min(a-off, b-a  );  vecswap(off, b-s, s);
	s = Math.min(d-c,   n-d-1);  vecswap(b,   n-s, s);

	// Recursively sort non-partition-elements
	if ((s = b-a) > 1)
	    quickSort(off, s);
	if ((s = d-c) > 1)
	    quickSort(n-s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private final void swap(int a, int b) {
	double t = m_Data[a];
	m_Data[a] = m_Data[b];
	m_Data[b] = t;
	int i = m_Index[a];
	m_Index[a] = m_Index[b];
	m_Index[b] = i;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private final void vecswap(int a, int b, int n) {
	for (int i=0; i<n; i++, a++, b++) swap(a, b);
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private final int med3(int a, int b, int c) {
	return (m_Data[a] >= m_Data[b] ?
		(m_Data[b] >= m_Data[c] ? b : m_Data[a] >= m_Data[c] ? c : a) :
		(m_Data[b] <= m_Data[c] ? b : m_Data[a] <= m_Data[c] ? c : a));
    }

    public static void main(String[] args) {
    	int nb = 80;
	double[] arr = new double[nb];
	for (int i = 0; i < nb; i++) {
		arr[i] = Math.random();
	}
    	DoubleIndexSorter sr = DoubleIndexSorter.getInstance();
	sr.setData(arr);
	sr.sort();
	for (int i = 0; i < 80; i++) {
		System.out.println(arr[i]);
	}
    }
}

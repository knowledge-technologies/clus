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

public class MSorter {


    public static void quickSort(MSortable x, int off, int len) {
	// Insertion sort on smallest arrays
	if (len < 7) {
	    for (int i=off; i<len+off; i++)
		for (int j=i; j>off && x.getDouble(j-1)<=x.getDouble(j); j--)
		    x.swap(j, j-1);
	    return;
	}

	// Choose a partition element, v
	int m = off + len/2;       // Small arrays, middle element
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {        // Big arrays, pseudomedian of 9
		int s = len/8;
		l = med3(x, l,     l+s, l+2*s);
		m = med3(x, m-s,   m,   m+s);
		n = med3(x, n-2*s, n-s, n);
	    }
	    m = med3(x, l, m, n); // Mid-size, med of 3
	}
	double v = x.getDouble(m);

	// Establish Invariant: v* (<v)* (>v)* v*
	int a = off, b = a, c = off + len - 1, d = c;
	while(true) {
	    while (b <= c && x.getDouble(b) > v) {
		if (x.getDouble(b) == v)
		    x.swap(a++, b);
		b++;
	    }
	    while (c >= b && x.getDouble(c) < v) {
		if (x.getDouble(c) == v)
		    x.swap(c, d--);
		c--;
	    }
	    if (b > c)
		break;
	    x.swap(b++, c--);
	}

	// Swap partition elements back to middle
	int s, n = off + len;
	s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
	s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

	// Recursively sort non-partition-elements
	if ((s = b-a) > 1)
	    quickSort(x, off, s);
	if ((s = d-c) > 1)
	    quickSort(x, n-s, s);
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(MSortable x, int a, int b, int n) {
	for (int i=0; i<n; i++, a++, b++) x.swap(a, b);
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private static int med3(MSortable x, int a, int b, int c) {
    	double da = x.getDouble(a);
    	double db = x.getDouble(b);
    	double dc = x.getDouble(c);
	return (da >= db ?
		(db >= dc ? b : da >= dc ? c : a) :
		(db <= dc ? b : da <= dc ? c : a));
    }
}

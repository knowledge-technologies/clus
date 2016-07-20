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

import java.util.*;

public class MVectorSorter {

    	public static void quickSort(Vector vec, MComparator comp) {
		quickSort(vec, 0, vec.size()-1, comp);
	}

	public static void quickSort(Vector vec, int low, int high, MComparator comp) {
		if (low < high) {
			int mid = split(vec, low, high, comp);
			quickSort(vec, low, mid-1, comp);
			quickSort(vec, mid+1, high, comp);
		}
	}

	public static int split(Vector vec, int low, int high, MComparator comp) {
		int left = low;
		int right = high;
		while (left < right) {
			while (compareObjects(vec, right, low, comp) <= -1)
				right--;
			while (left < right && compareObjects(vec, left, low, comp) >= 0)
				left++;
			if (left < right) swapObjects(vec, left, right);
		}
		int mid = right;
		swapObjects(vec, mid, low);
		return mid;
	}

	public static int compareObjects(Vector vec, int idx1, int idx2, MComparator comp) {
		Object el1 = vec.elementAt(idx1);
		Object el2 = vec.elementAt(idx2);
		return comp.compare(el1, el2);
	}

	public static void swapObjects(Vector vec, int idx1, int idx2) {
		Object el = vec.elementAt(idx1);
		vec.setElementAt(vec.elementAt(idx2), idx1);
		vec.setElementAt(el, idx2);
	}
}

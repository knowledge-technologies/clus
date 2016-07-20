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

package jeans.util;

import java.util.*;
import java.io.*;

public class IntervalCollection implements Serializable {

	public final static long serialVersionUID = 1L;

	public final static IntervalCollection EMPTY = new IntervalCollection();

	protected final static String[] DELIMS = {"-",","};

	protected int m_Total;
	protected int m_CrInt;
	protected boolean m_Done;
	protected boolean m_Default;
	protected ArrayList m_Interval = new ArrayList();

	public IntervalCollection(String str) {
		if (str.toUpperCase().equals("DEFAULT")) {
			m_Default = true;
			return;
		}
		if (!str.toUpperCase().equals("NONE") && !str.toUpperCase().equals("EMPTY")) {
			ArrayList intervals = new ArrayList();
			MultiDelimStringTokenizer tok = new MultiDelimStringTokenizer(DELIMS);
			tok.setLine(str);
			int first = -1, last = -1;
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				int delim = tok.lastDelim();
				first = Integer.parseInt(token);
				if (delim == 1) {
					if (tok.hasMoreTokens()) {
						token = tok.nextToken();
						last = Integer.parseInt(token);
					} else {
						last = first;
					}
				} else {
					last = first;
				}
				intervals.add(new Interval(first, last));
			}
			initializeFromBits(toBits(intervals));
		}
	}

	protected IntervalCollection() {
	}

	public boolean isDefault() {
		return m_Default;
	}

	public void clear() {
		m_Default = false;
		m_Interval.clear();
		computeTotal();
	}

	public void addInterval(int first, int last) {
		m_Interval.add(new Interval(first, last));
		computeTotal();
	}

	public int getNbIntervals() {
		return m_Interval.size();
	}

	public Interval getInterval(int idx) {
		return (Interval)m_Interval.get(idx);
	}

	public void copyFrom(IntervalCollection other) {
		m_Interval.clear();
		for (int i = 0; i < other.getNbIntervals(); i++) {
			m_Interval.add(other.getInterval(i));
		}
		computeTotal();
	}

	public void subtract(IntervalCollection other) {
		int max = Math.max(getMaxIndex(), other.getMaxIndex());
		boolean[] bits = new boolean[max+1];
		toBits(bits);
		other.subtractFromBits(bits);
		initializeFromBits(bits);
	}

	public void add(boolean[] bits) {
		boolean[] this_bits = new boolean[Math.max(getMaxIndex()+1, bits.length)];
		toBits(this_bits);
		for (int i = 0; i < bits.length; i++) {
			if (bits[i]) this_bits[i] = true;
		}
		initializeFromBits(this_bits);
	}

	public void initializeFromBits(boolean[] bits) {
		m_Interval.clear();
		boolean is_in = false;
		int start = -1;
		for (int i = 0; i < bits.length; i++) {
			if (bits[i]) {
				if (!is_in) {
					start = i;
					is_in = true;
				}
			} else {
				if (is_in) {
					m_Interval.add(new Interval(start, i-1));
					is_in = false;
				}
			}
		}
		if (is_in) {
			m_Interval.add(new Interval(start, bits.length-1));
		}
		computeTotal();
	}

	public void toBits(boolean[] bits) {
		toBits(m_Interval, bits);
	}

	public static void toBits(ArrayList intervals, boolean[] bits) {
		for (int i = 0; i < intervals.size(); i++) {
			Interval interv = (Interval)intervals.get(i);
			interv.toBits(bits);
		}
	}

	public boolean[] toBits() {
		return toBits(m_Interval);
	}

	public static boolean[] toBits(ArrayList intervals) {
		boolean[] bits = new boolean[getMaxIndex(intervals)+1];
		toBits(intervals, bits);
		return bits;
	}

	public void subtractFromBits(boolean[] bits) {
		for (int i = 0; i < getNbIntervals(); i++) {
			Interval interv = getInterval(i);
			interv.subtractFromBits(bits);
		}
	}

	public int getMaxIndex() {
		return getMaxIndex(m_Interval);
	}

	public static int getMaxIndex(ArrayList intervals) {
		int max = 0;
		for (int i = 0; i < intervals.size(); i++) {
			Interval interv = (Interval)intervals.get(i);
			max = Math.max(max, interv.getLast());
		}
		return max;
	}

	public int getMinIndex() {
		return getMinIndex(m_Interval);
	}

	public static int getMinIndex(ArrayList intervals) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < intervals.size(); i++) {
			Interval interv = (Interval)intervals.get(i);
			min = Math.min(min, interv.getFirst());
		}
		return min;
	}

	public boolean isEmpty() {
		return m_Interval.size() == 0;
	}

	public void reset() {
		m_CrInt = -1;
		m_Done = gotoNext();
	}

	public boolean hasMoreInts() {
		return m_Done;
	}

	public int nextInt() {
		int res = getInterval(m_CrInt).get();
		m_Done = gotoNext();
		return res;
	}

	public boolean gotoNext() {
		if (m_CrInt != -1) getInterval(m_CrInt).incr();
		if (m_CrInt == -1 || getInterval(m_CrInt).atEnd()) {
			m_CrInt++;
			if (m_CrInt >= getNbIntervals()) return false;
			getInterval(m_CrInt).reset();
		}
		return true;
	}

	public String toString() {
		if (getNbIntervals() > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < getNbIntervals(); i++) {
				Interval interv = getInterval(i);
				if (i != 0) buf.append(",");
				buf.append(interv.toString());
			}
			return buf.toString();
		} else {
			return "None";
		}
	}

	protected void computeTotal() {
		int total = 0;
		for (int i = 0; i < m_Interval.size(); i++) {
			Interval interv = (Interval)m_Interval.get(i);
			total += interv.getSize();
		}
		m_Total = total;
	}
}

class Interval  implements Serializable {

	public final static long serialVersionUID = 1L;

	protected int m_First, m_Last;
	protected int m_Curr;

	public Interval(int first, int last) {
		m_First = first;
		m_Last = last;
	}

	public int getFirst() {
		return m_First;
	}

	public int getLast() {
		return m_Last;
	}

	public void reset() {
		m_Curr = m_First;
	}

	public void incr() {
		m_Curr++;
	}

	public int get() {
		return m_Curr;
	}

	public boolean atEnd() {
		return m_Curr > m_Last;
	}

	public int getSize() {
		return m_Last - m_First + 1;
	}

	public void toBits(boolean[] bits) {
		for (int i = m_First; i <= m_Last; i++) {
			bits[i] = true;
		}
	}

	public void subtractFromBits(boolean[] bits) {
		for (int i = m_First; i <= m_Last; i++) {
			bits[i] = false;
		}
	}

	public String toString() {
		if (m_First == m_Last) return String.valueOf(m_First);
		else return String.valueOf(m_First)+"-"+String.valueOf(m_Last);
	}

}

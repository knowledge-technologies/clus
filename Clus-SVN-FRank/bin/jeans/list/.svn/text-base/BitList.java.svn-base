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

package jeans.list;

import java.util.*;
import java.io.*;

public class BitList implements Serializable {

	public final static long serialVersionUID = 1;

	private final static int ADDRESS_BITS_PER_UNIT = 6;
	private final static int BITS_PER_UNIT = 1 << ADDRESS_BITS_PER_UNIT;
	private final static int BIT_INDEX_MASK = BITS_PER_UNIT - 1;

	private long m_Bits[];
	private int m_Size;

	public BitList() {
	}

	public BitList(int size) {
		resize(size);
	}

	public final void resize(int size) {
		m_Size = size;
		m_Bits = new long[(unitIndex(size-1) + 1)];
	}

	public final BitList cloneList() {
		return new BitList(m_Size);
	}

	public final int size() {
		return m_Size;
	}

	public final void setBit(int idx) {
		m_Bits[unitIndex(idx)] |= bit(idx);
	}

	public final boolean getBit(int idx) {
		return (m_Bits[unitIndex(idx)] & bit(idx)) != 0;
	}

	public final void reset() {
		Arrays.fill(m_Bits, 0);
	}

	public final void copy(BitList other) {
		int olen = other.m_Bits.length;
        if (m_Bits == null || olen != m_Bits.length) {
			m_Bits = new long[olen];
			m_Size = other.size();
		}
		System.arraycopy(other.m_Bits, 0, m_Bits, 0, olen);
	}

	public final void add(BitList other) {
		int nb = m_Bits.length;
		for (int i = 0; i < nb; i++)
			m_Bits[i] |= other.m_Bits[i];
	}


	//	a and (not b)
	//
	//	a/b  |  0  |  1  |
	//	------------------
	//	0    |  0  |  0  |
	//	1    |  1  |  0  |

	public final void subtractFromThis(BitList other) {
		int nb = m_Bits.length;
		for (int i = 0; i < nb; i++)
			m_Bits[i] &= (~ other.m_Bits[i]);
	}

	public final void subtractFromOther(BitList other) {
		int nb = m_Bits.length;
		for (int i = 0; i < nb; i++)
			m_Bits[i] = other.m_Bits[i] & (~ m_Bits[i]);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_Size; i++) {
			buf.append(getBit(i) ? '1' : '0');
		}
		return buf.toString();
	}

	public String toVector() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < m_Size; i++) {
			if (i != 0) buf.append(",");
			buf.append(getBit(i) ? '1' : '0');
		}
		buf.append("]");
		return buf.toString();
	}

	public final int getNbOnes() {
		int nb = 0;
		for (int i = 0; i < m_Size; i++) {
			if (getBit(i)) nb++;
		}
		return nb;
	}

	private final static int unitIndex(int bitIndex) {
		return bitIndex >> ADDRESS_BITS_PER_UNIT;
	}

	private final static long bit(int bitIndex) {
		return 1L << (bitIndex & BIT_INDEX_MASK);
	}

	public void copyAndSubtractFromThis(BitList bits, BitList bits2) {
		System.err.println("BitList.java:copyAndSubtractFromThis(): unimplemented method!");
		System.exit(-1);
	}
}

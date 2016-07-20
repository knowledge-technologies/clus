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

public class DisjointSetForest {

	protected int m_Size;
	protected int[] m_P;
	protected int[] m_Rank;
	protected int[] m_ComprTable;
	protected int[] m_Map;

	public DisjointSetForest(int size) {
		m_Size = size;
		m_P = new int[size];
		m_Rank = new int[size];
		m_ComprTable = new int[size];
		m_Map = new int[size];
		for (int i = 0; i < size; i++) {
			m_Map[i] = -1;
		}
	}

	public void makeSets(int nb) {
		for (int i = 0; i < nb; i++) {
			makeSet(i);
		}
	}

	public void makeSet(int x) {
		m_P[x] = x;
		m_Rank[x] = 0;
	}

	public void union(int x, int y) {
		link(findSet(x), findSet(y));
	}

	public void link(int x, int y) {
		if (m_Rank[x] > m_Rank[y]) {
			m_P[y] = x;
		} else {
			m_P[x] = y;
			if (m_Rank[x] == m_Rank[y]) {
				m_Rank[y]++;
			}
		}
	}

	public int findSet(int x) {
		// Find root
		int idx = 0;
		while (m_P[x] != x) {
			m_ComprTable[idx++] = x;
			x = m_P[x];
		}
		// Update pointers along path
		while (idx > 0) {
			m_P[m_ComprTable[--idx]] = x;
		}
		return x;
	}

	public int getComponent(int x) {
		return m_Map[findSet(x)];
	}

	public int numberComponents() {
		int idx = 0;
		for (int i = 0; i < m_Size; i++) {
			int x = findSet(i);
			if (m_Map[x] == -1) {
				m_Map[x] = idx++;
			}
		}
		return idx;
	}
}

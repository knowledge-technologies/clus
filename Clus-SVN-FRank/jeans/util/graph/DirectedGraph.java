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

package jeans.util.graph;

import jeans.util.list.*;

public class DirectedGraph {

	protected int m_NbNodes;
	protected boolean[][] m_AdjacencyMatrix;
	protected MyListIter[] m_Edges;

	public DirectedGraph(int nbNodes) {
		m_NbNodes = nbNodes;
		m_AdjacencyMatrix = new boolean[nbNodes][nbNodes];
		m_Edges = new MyListIter[nbNodes];
		for (int i = 0; i < nbNodes; i++) {
			m_Edges[i] = new MyListIter();
		}
	}

	public void addEdge(int i, int j) {
		if (!m_AdjacencyMatrix[i][j]) {
			m_AdjacencyMatrix[i][j] = true;
			m_Edges[i].addEnd(new MyIntList(j));
		}
	}

	public int size() {
		return m_NbNodes;
	}

	public MyListIter getEdges(int i) {
		return m_Edges[i];
	}

	public void print() {
		for (int i = 0; i < size(); i++) {
			MyListIter iter = getEdges(i);
			iter.reset();
			MyList elem = iter.getNext();
			if (elem != null) {
				System.out.print(String.valueOf(i)+": ");
				int idx = 0;
				while (elem != null) {
					if (idx != 0) System.out.print(", ");
					System.out.print(String.valueOf(((MyIntList)elem).getValue()));
					elem = iter.getNext();
					idx++;
				}
				System.out.println();
			}
		}
	}
}

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

package clus.selection;

public class XValGroup {

	protected int[] m_Elements;
	protected int m_NbElements;

	public XValGroup(int max) {
		m_Elements = new int[max];
	}

	public boolean add(int which, int max) {
		if (m_NbElements >= max) return false;
		m_Elements[m_NbElements++] = which;
		return true;
	}

	public int getNbElements() {
		return m_NbElements;
	}

	public int getElement(int idx) {
		return m_Elements[idx];
	}

	public void print() {
		System.out.print("[");
		for (int i = 0; i < getNbElements(); i++) {
			if (i != 0) System.out.print(",");
			System.out.print(getElement(i));
		}
		System.out.println("]");
	}
}

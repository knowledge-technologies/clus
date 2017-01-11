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

public class BitMapSelection extends ClusSelection {

	protected int m_NbSelected;
	protected boolean[] m_Selection;

	public BitMapSelection(int nbrows) {
		super(nbrows);
		m_Selection = new boolean[nbrows];
	}

	public int getNbSelected() {
		return m_NbSelected;
	}

	public boolean isSelected(int row) {
		return m_Selection[row];
	}

	public void select(int row) {
		if (!m_Selection[row]) {
			m_Selection[row] = true;
			m_NbSelected++;
		}
	}
}

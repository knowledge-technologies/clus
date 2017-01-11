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

public abstract class XValMainSelection {

	protected int m_NbFolds;
	protected int m_NbRows;

	public XValMainSelection(int nbfolds, int nbrows) {
		m_NbFolds = nbfolds;
		m_NbRows = nbrows;
	}

	public int getNbFolds() {
		return m_NbFolds;
	}

	public int getNbRows() {
		return m_NbRows;
	}

	public int getNbSelected(int fold) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++) if (getFold(i) == fold) nb++;
		return nb;
	}

	public boolean isSelected(int row, int fold) {
		return getFold(row) == fold;
	}

	public abstract int getFold(int row);
}

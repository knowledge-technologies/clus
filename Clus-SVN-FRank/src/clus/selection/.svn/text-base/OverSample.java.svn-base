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

import clus.util.*;

public class OverSample extends ClusSelection {

	protected int m_NbSelected;

	public OverSample(int nbrows, double sel) {
		super(nbrows);
		m_NbSelected = (int)Math.ceil((double)sel*nbrows);
	}

	public boolean supportsReplacement() {
		return true;
	}

	public int getIndex(int i) {
		return ClusRandom.nextInt(ClusRandom.RANDOM_SELECTION, m_NbRows);
	}

	public int getNbSelected() {
		return m_NbSelected;
	}

	public boolean isSelected(int row) {
		return false;
	}

}

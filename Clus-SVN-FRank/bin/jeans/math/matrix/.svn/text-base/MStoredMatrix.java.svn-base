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

package jeans.math.matrix;

public class MStoredMatrix extends MMatrix {

	protected int m_Rows, m_Cols;
	protected double m_Data[][];

	public MStoredMatrix(double[] row) {
		m_Rows = 1;
		m_Cols = row.length;
		m_Data = new double[1][];
		m_Data[0] = row;
	}

	public final double get(int r, int c) {
		if (c > r) return m_Data[c][r];
		else return m_Data[r][c];
	}

	public final int getRows() {
		return m_Rows;
	}

	public final int getCols() {
		return m_Cols;
	}
}

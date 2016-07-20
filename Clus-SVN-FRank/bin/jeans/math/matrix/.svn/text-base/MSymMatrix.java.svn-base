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

import java.io.Serializable;

public class MSymMatrix extends MMatrix implements Serializable {

	protected int m_Size;
	protected double m_Data[][];

	public MSymMatrix(int size) {
		m_Size = size;
		m_Data = createSymmetricData(size);
	}

	public int getSize() {
		return m_Size;
	}

	public final double xtAx(double[] x) {
		double res = 0.0;
		for (int i = 0; i < m_Size; i++) {
			double xi = x[i];
			double[] ai = m_Data[i];
			res += m_Data[i][i] * xi*xi;
			for (int j = 0; j < i; j++)
				res += 2 * ai[j] * xi * x[j];
		}
		return res;
	}

	public final double xtAx_delta(double[] x1, double[] x2) {
		double res = 0.0;
		for (int i = 0; i < m_Size; i++) {
			double xi = x1[i] - x2[i];
			double[] ai = m_Data[i];
			res += m_Data[i][i] * xi*xi;
			for (int j = 0; j < i; j++) {
				double xj = x1[j] - x2[j];
				res += 2 * ai[j] * xi * xj;
			}
		}
		return res;
	}

	public final double xtAx(MySparseVector x) {
		double res = 0.0;
		int nb = x.getNbNonZero();
		for (int vi = 0; vi < nb; vi++) {
			int i = x.getPosition(vi);
			double xi = x.getValue(vi);
			res += m_Data[i][i] * xi*xi;
			for (int vj = 0; vj < vi; vj++) {
				int j = x.getPosition(vj);
				double xj = x.getValue(vj);
				res += 2 * get(i,j) * xi * xj;
			}
		}
		return res;
	}

	public final void addRowWeighted(double[] x, int i, double w) {
		double[] ai = m_Data[i];
		for (int j = 0; j <= i; j++) {
			x[j] += w * ai[j];
		}
		for (int j = i+1; j < m_Size; j++) {
			x[j] += w * m_Data[j][i];
		}
	}

	public final double get(int r, int c) {
		if (c > r) return m_Data[c][r];
		else return m_Data[r][c];
	}

	public final double get_fast(int r, int c) {
		return m_Data[r][c];
	}

	public final double[] getRow(int r) {
		return m_Data[r];
	}

	public final void set(int r, int c, double val) {
		m_Data[r][c] = val;
	}

	public final void set_sym(int r, int c, double val) {
		if (c > r) m_Data[c][r] = val;
		else m_Data[r][c] = val;
	}

	public final void add_sym(int r, int c, double val) {
		if (c > r) m_Data[c][r] += val;
		else m_Data[r][c] += val;
	}

	public final int getRows() {
		return m_Size;
	}

	public final int getCols() {
		return m_Size;
	}

	private final static double[][] createSymmetricData(int size) {
		double[][] data = new double[size][];
		for (int i = 0; i < size; i++)
			data[i] = new double[i+1];
		return data;
	}


    public String toString(){
	String output = "";
	for (int i = 0; i < m_Size; i++){
	    output = output + toString(i);
	}
	return output;
    }



    public String toString(int row){
	String output =  "";
	int j;
	for (j = 0; j <=row; j ++) {
	    //System.out.println(j);
		output= output + m_Data[row][j] + " ";
	}
	for (j = row+1; j< m_Size; j++) {
	    //System.out.println(j);
	    output = output + m_Data[j][row]+" ";
	}
	output= output + "\n";
	return output;
    }




}

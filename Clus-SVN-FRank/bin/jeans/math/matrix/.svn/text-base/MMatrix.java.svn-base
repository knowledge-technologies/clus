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

import jeans.util.StringUtils;

import java.text.NumberFormat;
import java.io.PrintWriter;

public abstract class MMatrix {

	public abstract double get(int r, int c);

	public abstract int getRows();

	public abstract int getCols();

	public final static double dot(double[] x, double[] y) {
		double res = 0.0;
		for (int i = 0; i < x.length; i++)
			res += x[i]*y[i];
		return res;
	}

	public final static double dot_delta(double[] x1, double[] x2, double[] y1, double[] y2) {
		double res = 0.0;
		for (int i = 0; i < x1.length; i++)
			res += (x1[i]-x2[i])*(y1[i]-y2[i]);
		return res;
	}

	public final static double dot(MySparseVector x, double[] y) {
		double res = 0.0;
		int nb = x.getNbNonZero();
		for (int vi = 0; vi < nb; vi++) {
			int i = x.getPosition(vi);
			res += x.getValue(vi) * y[i];
		}
		return res;
	}

	public final double[][] toCPArray() {
		int rows = getRows();
		int cols = getCols();
		double[][] data = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = get(i, j);
			}
		}
		return data;
	}

	public final void print(PrintWriter wrt, NumberFormat format, int size) {
		if (getRows() == 1) {
			printRow(0, wrt, format, size);
			wrt.println();
		} else {
			for (int mi = 0; mi < getRows(); mi++) {
				if (mi == 0) wrt.print("[");
				else wrt.print(" ");
				printRow(mi, wrt, format, size);
				if (mi == getRows()-1) wrt.print("]");
				wrt.println();
			}
		}
	}

	private final void printRow(int mi, PrintWriter wrt, NumberFormat format, int size) {
		wrt.print("[");
		for (int mj = 0; mj < getCols(); mj++) {
			if (mj != 0) wrt.print(";");
			String strg = format.format(get(mi, mj));
			wrt.print(StringUtils.printStr(strg, size));
		}
		wrt.print("]");
	}
}

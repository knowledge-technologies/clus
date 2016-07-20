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

package clus.ext.hierarchical;

import java.util.*;
import java.io.*;

import clus.data.rows.*;

import jeans.util.array.*;


public class HierMatrixOutput {

	public static void writeExamples(RowData data, ClassHierarchy hier) {
		try {
			PrintWriter wrt = data.getSchema().getSettings().getFileAbsoluteWriter("examples.matrix");
			writeHeader(hier, wrt);
			ClassesAttrType type = hier.getType();
			int sidx = type.getArrayIndex();
			double[] vector = new double[hier.getTotal()];
			for (int i = 0; i < data.getNbRows(); i++) {
				Arrays.fill(vector, 0.0);
				DataTuple tuple = data.getTuple(i);
				ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
				for (int j = 0; j < tp.getNbClasses(); j++) {
					ClassesValue val = tp.getClass(j);
					vector[val.getIndex()] = 1.0;
				}
				wrt.println(MDoubleArray.toString(vector));
			}
			wrt.close();
		} catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}
	}

	public static void writeHeader(ClassHierarchy hier, PrintWriter wrt) {
		wrt.print("[");
		for (int i = 0; i < hier.getTotal(); i++) {
			if (i != 0) wrt.print(",");
			wrt.print(String.valueOf(hier.getWeight(i)));
		}
		wrt.println("]");
	}
}

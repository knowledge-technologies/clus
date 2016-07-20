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

package clus.data.rows;

import java.io.*;

import clus.data.type.ClusAttrType;
import clus.main.*;
import clus.data.type.*;

public class DataTuple implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected ClusSchema m_Schema;

	// Attributes can have several base types
	public int[] m_Ints; // nominal attributes
	public double[] m_Doubles;
	public Object[] m_Objects;

	// Each example can have a weight
	public double m_Weight;
	public int m_Index;

	// Hack for efficient xval, should be replaced later
	public int[] m_Folds;

	protected DataTuple() {
	}

	public DataTuple(ClusSchema schema) {
		// Initialize arrays for three base types
		int nb_int = schema.getNbInts();
		if (nb_int > 0) m_Ints = new int[nb_int];
		int nb_double = schema.getNbDoubles();
		if (nb_double > 0) m_Doubles = new double[nb_double];
		int nb_obj = schema.getNbObjects();
		if (nb_obj > 0) m_Objects = new Object[nb_obj];
		// Initialize weight
		m_Weight = 1.0;
		m_Schema = schema;
	}

	public final ClusSchema getSchema() {
		return m_Schema;
	}

	public DataTuple cloneTuple() {
		DataTuple res = new DataTuple();
		cloneTuple(res);
		return res;
	}
	
	public void cloneTuple(DataTuple res) {
		res.m_Ints = m_Ints;
		res.m_Doubles = m_Doubles;
		res.m_Objects = m_Objects;
		res.m_Weight = m_Weight;
		res.m_Index = m_Index;
		res.m_Folds = m_Folds;
		res.m_Schema = m_Schema;
	}

	public double euclDistance(DataTuple other)
	{
		double result = 0;
		for(int i =0;i<m_Doubles.length;i++)
		{
			double t = this.getDoubleVal(i) -other.getDoubleVal(i);
			t = t*t;
			result += t;
		}
		return Math.sqrt(result);
	}
	
	public DataTuple deepCloneTuple() {
		DataTuple res = new DataTuple();
		if (m_Ints != null) {
			res.m_Ints = new int[m_Ints.length];
			System.arraycopy(m_Ints, 0, res.m_Ints, 0, m_Ints.length);
		}
		if (m_Doubles != null) {
			res.m_Doubles = new double[m_Doubles.length];
			System.arraycopy(m_Doubles, 0, res.m_Doubles, 0, m_Doubles.length);
		}
		if (m_Objects != null) {
			res.m_Objects = new Object[m_Objects.length];
			System.arraycopy(m_Objects, 0, res.m_Objects, 0, m_Objects.length);
		}
		res.m_Weight = m_Weight;
		res.m_Index = m_Index;
		res.m_Folds = m_Folds;
		res.m_Schema = m_Schema;
		return res;
	}

	public final DataTuple changeWeight(double weight) {
		DataTuple res = cloneTuple();
		res.m_Weight = weight;
		return res;
	}

	public final DataTuple multiplyWeight(double weight) {
		DataTuple res = cloneTuple();
		res.m_Weight = m_Weight * weight;
		return res;
	}

	public final int getClassification() { // should not be used
		return -1;
	}

	public final boolean hasNumMissing(int idx) {
		return m_Doubles[idx] == Double.POSITIVE_INFINITY;
	}

	public final double getDoubleVal(int idx) {
		return m_Doubles[idx];
	}

	public final int getIntVal(int idx) {
		return m_Ints[idx];
	}

	public final Object getObjVal(int idx) {
		return m_Objects[idx];
	}

	public final void setIntVal(int value, int idx) {
		m_Ints[idx] = value;
	}

	public final void setDoubleVal(double value, int idx) {
		m_Doubles[idx] = value;
	}

	public final void setObjectVal(Object value, int idx) {
		m_Objects[idx] = value;
	}

	public final void setIndex(int idx) {
		m_Index = idx;
	}

	public final int getIndex() {
		return m_Index;
	}

	public final double getWeight() {
		return m_Weight;
	}

	public final void setWeight(double weight) {
		m_Weight = weight;
	}
	
	public final void setSchema(ClusSchema schema) {
		m_Schema = schema;
	}

	public void writeTuple(PrintWriter wrt) {
		int aidx = 0;
		ClusSchema schema = getSchema();
		for (int i = 0; i < schema.getNbAttributes(); i++) {
			ClusAttrType type = schema.getAttrType(i);
			if (!type.isDisabled()) {
				if (aidx != 0) wrt.print(",");
				wrt.print(type.getString(this));
				aidx++;
			}
		}
		wrt.println();
	}

	public String toString() {
		int aidx = 0;
		StringBuffer buf = new StringBuffer();
		ClusSchema schema = getSchema();
		if (schema != null) {
			for (int i = 0; i < schema.getNbAttributes(); i++) {
				ClusAttrType type = schema.getAttrType(i);
				if (!type.isDisabled()) {
					if (aidx != 0) buf.append(",");
					buf.append(type.getString(this));
					aidx++;
				}
			}
		} else {
			for (int i = 0; i < m_Objects.length; i++) {
				if (i != 0) buf.append(",");
				buf.append(m_Objects[i].toString());
			}
		}
		return buf.toString();
	}
}

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

package clus.data.type;

import java.io.*; 

import clus.io.*;
import clus.main.Settings;
import clus.util.*;
import clus.data.rows.*;
import clus.data.cols.*;
import clus.data.cols.attribute.*;
import clus.data.io.ClusReader;

/**
 * Attribute of numeric (continuous) value.
 */
public class NumericAttrType extends ClusAttrType {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int THIS_TYPE = 1;
	public final static String THIS_TYPE_NAME = "Numeric";

	public final static double MISSING = Double.POSITIVE_INFINITY;

	protected boolean m_Sparse;

	public NumericAttrType(String name) {
		super(name);
	}

	public ClusAttrType cloneType() {
		NumericAttrType at = new NumericAttrType(m_Name);
		cloneType(at);
		at.m_Sparse = m_Sparse;
		return at;
	}

	public boolean isSparse() {
		return m_Sparse;
	}

	public void setSparse(boolean sparse) {
		m_Sparse = sparse;
	}

	public int getTypeIndex() {
		return THIS_TYPE;
	}

	public String getTypeName() {
		return THIS_TYPE_NAME;
	}

	public int getValueType() {
		return VALUE_TYPE_DOUBLE;
	}

/*	public boolean addToData(ColData data) {
		if (!super.addToData(data)) {
			data.addAttribute(new NumericAttribute(this));
		}
		return true;
	}
*/
	public int getMaxNbStats() {
		// Positive statistic and missing value statistic
		return 2;
	}

	public String getString(DataTuple tuple) {
		double val = this.getNumeric(tuple);
// FIXME - SOON - STATUS_KEY attribute :-)
		if (getStatus() == STATUS_KEY) {
			return String.valueOf((int)val);
		} else {
			return val == MISSING ? "?" : String.valueOf(val);
		}
	}

	public boolean isMissing(DataTuple tuple) {
		return tuple.m_Doubles[m_ArrayIndex] == MISSING;
	}

	public double getNumeric(DataTuple tuple) {
		return tuple.getDoubleVal(m_ArrayIndex);
	}

	public void setNumeric(DataTuple tuple, double value) {
		tuple.setDoubleVal(value, m_ArrayIndex);
	}

	public int compareValue(DataTuple t1, DataTuple t2) {
		double v1 = t1.m_Doubles[m_ArrayIndex];
		double v2 = t2.m_Doubles[m_ArrayIndex];
		if (v1 == v2) return 0;
		return v1 > v2 ? 1 : -1;
	}

	public ClusAttribute createTargetAttr(ColTarget target) {
		return new NumericTarget(target, this, getArrayIndex());
	}

	public void writeARFFType(PrintWriter wrt) throws ClusException {
		wrt.print("numeric");
	}

	public ClusSerializable createRowSerializable() throws ClusException {
		return new MySerializable();
	}

	public class MySerializable extends ClusSerializable {

		public int m_NbZero, m_NbNeg, m_NbTotal;

		public boolean read(ClusReader data, DataTuple tuple) throws IOException {
			if (!data.readNoSpace()) return false;
			double val = data.getFloat();
			tuple.setDoubleVal(val, getArrayIndex());
			if (val == MISSING) {
				incNbMissing();
				m_NbZero++;
			}
			if (val == 0.0) {
				m_NbZero++;
			} else if (val < 0.0) {
				m_NbNeg++;
			}
			m_NbTotal++;
			return true;
		}

		public void term(ClusSchema schema) {
			// System.out.println("Attribute: "+getName()+" "+((double)100.0*m_NbZero/m_NbTotal));
			if (m_NbNeg == 0 && m_NbZero > m_NbTotal*5/10) {
				setSparse(true);
			}
		}
	}

}


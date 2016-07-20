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
import clus.data.io.ClusReader;
import clus.data.rows.*;

public class IntegerAttrType extends ClusAttrType {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int THIS_TYPE = 4;
	public final static String THIS_TYPE_NAME = "Integer";

	public IntegerAttrType(String name) {
		super(name);
	}

	public ClusAttrType cloneType() {
		IntegerAttrType at = new IntegerAttrType(m_Name);
		cloneType(at);
		return at;
	}

	public int getTypeIndex() {
		return THIS_TYPE;
	}

	public String getTypeName() {
		return THIS_TYPE_NAME;
	}

	public int getValueType() {
		return VALUE_TYPE_INT;
	}

	public String getString(DataTuple tuple) {
		return String.valueOf(tuple.m_Ints[m_ArrayIndex]);
	}

	public int compareValue(DataTuple t1, DataTuple t2) {
		int s1 = t1.m_Ints[m_ArrayIndex];
		int s2 = t2.m_Ints[m_ArrayIndex];
		return s1 == s2 ? 0 : 1;
	}

	public ClusSerializable createRowSerializable() throws ClusException {
		return new MySerializable();
	}

	// FIXME make serializable on level of superclass
	// With:
	//	* initialise()
	//	* setData()
	//
	// -> makes it possible to make derived attributes.

	public class MySerializable extends ClusSerializable {

		protected int m_Index;

		public boolean read(ClusReader data, DataTuple tuple) throws IOException {
			tuple.setIntVal(m_Index++, getArrayIndex());
			return true;
		}
	}
}


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

import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.main.Settings;
import clus.data.type.SparseNumericAttrType;
import java.io.PrintWriter;
import java.util.*;

public class SparseDataTuple extends DataTuple {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected HashMap m_Map = new HashMap();

	public SparseDataTuple(ClusSchema schema) {
		super(schema);
	}

	public SparseDataTuple() {	
	}	
	
	public void setDoubleValueSparse(Double val, Integer index) {
		m_Map.put(index, val);
	}

	public double getDoubleValueSparse(int index) {
		Double value = (Double)m_Map.get(new Integer(index));
		return value != null ? value.doubleValue() : 0.0;
	}
	
	public double getDoubleValueSparse(Integer index) {
		Double value = (Double)m_Map.get(index);
		return value != null ? value.doubleValue() : 0.0;
	}
	
	public Object[] getAttributeIndices() {
		return m_Map.keySet().toArray();
	}
	
	public void addExampleToAttributes() {
		Object[] indices = getAttributeIndices();
		for (int i=0; i<indices.length; i++) {
			int index = ((Integer)indices[i]).intValue();
			SparseNumericAttrType attr = (SparseNumericAttrType) getSchema().getAttrType(index);
			attr.addExample(this);
		}
	}
	
	public final SparseDataTuple cloneTuple() {
		SparseDataTuple res = new SparseDataTuple();
		cloneTuple(res);
		res.m_Map = m_Map;
		return res;
	}
	
	public SparseDataTuple deepCloneTuple() {
		SparseDataTuple res = new SparseDataTuple();
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
		res.m_Map = m_Map;
		return res;
	}
	
	public void writeTuple(PrintWriter wrt) {
		ClusSchema schema = getSchema();
		int aidx = 0;
		wrt.print("{");
		// Do all sparse attributes
		Iterator it = m_Map.keySet().iterator();
		while (it.hasNext()) {
			Integer idx = (Integer)it.next();
			ClusAttrType type = schema.getAttrType(idx.intValue());
			if (!type.isDisabled()) {
				if (aidx != 0) wrt.print(",");
				int nidx = idx.intValue()+1;
				wrt.print(nidx+" "+type.getString(this));
				aidx++;
			}
		}
		// Do all non-sparse attributes
		ClusAttrType[] type = schema.getNonSparseAttributes();
		for (int i = 0; i < type.length; i++) {
			if (!type[i].isDisabled()) {
				if (aidx != 0) wrt.print(",");
				int nidx = type[i].getIndex()+1;
				wrt.print(nidx+" "+type[i].getString(this));
				aidx++;
			}
		}
		wrt.println("}");
	}	
}

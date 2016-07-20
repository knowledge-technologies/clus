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

package clus.data.cols.attribute;

import java.io.*;
import jeans.util.*;
import jeans.util.sort.*;

import clus.main.*;
import clus.data.cols.*;
import clus.data.io.*;
import clus.data.type.*;
import clus.selection.*;

public class NumericAttribute extends NumericAttrBase {

	public int DEBUG = 0;

	public double[] m_Data;
	public int[] m_Index;
	protected int m_NbRows;

	public NumericAttribute(NumericAttrType type) {
		super(type);
	}

	public void resize(int rows) {
		m_NbRows = rows;
		m_Data = new double[rows];
	}

	public ClusAttribute select(ClusSelection sel, int nbsel) {
		int s_data = 0;
		int s_subset = 0;
		double[] data = m_Data;
		m_Data = new double[m_NbRows-nbsel];
		double[] subset = new double[nbsel];
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) subset[s_subset++] = data[i];
			else m_Data[s_data++] = data[i];
		}
		m_NbRows -= nbsel;
		NumericAttribute s_attr = new NumericAttribute(m_Type);
		s_attr.m_Data = subset;
		s_attr.m_NbRows = nbsel;
		return s_attr;
	}

	public void insert(ClusAttribute attr, ClusSelection sel, int nb_new) {
		int s_data = 0;
		int s_subset = 0;
		double[] data = m_Data;
		m_Data = new double[nb_new];
		double[] subset = ((NumericAttribute)attr).m_Data;
		for (int i = 0; i < nb_new; i++) {
			if (sel.isSelected(i)) m_Data[i] = subset[s_subset++];
			else m_Data[i] = data[s_data++];
		}
		m_NbRows = nb_new;
	}

	// Sort this attribute
	public void prepare() {
		DoubleIndexSorter sorter = DoubleIndexSorter.getInstance();
		sorter.setData(m_Data);
		sorter.sort();
		m_Index = sorter.getIndex();
		if (DEBUG == 1) {
			try {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("debug/attr-"+getName())));
				for (int i = 0; i < m_Data.length; i++)	writer.println(m_Data[i]+"\t"+m_Index[i]);
				writer.close();
			} catch (IOException e) {}
		}
	}

	// Unsort this attribute
	public void unprepare() {
		m_Data = DoubleIndexSorter.unsort(m_Data, m_Index);
		m_Index = null;
	}

	public void findBestTest(MyArray leaves, ColTarget target, ClusStatManager smanager) {
		// Reset positive statistic
		int nb = leaves.size();
		for (int i = 0; i < nb; i++) {
//			ClusNode inf = (ClusNode)leaves.elementAt(i);
//			inf.reset(2);
//			inf.m_PrevDouble = Double.NaN;
		}
		// For each attribute value
//		ClusNode[] infos = target.getNodes();
		// First parse missing values
		int first = 0;
		if (m_Type.hasMissing()) {
			while (first < m_NbRows && m_Data[first] == Double.POSITIVE_INFINITY) {
//				int idx = m_Index[first];
//				ClusNode inf = infos[idx];
//				if (!inf.m_Finished) inf.m_TestStat[1].update(target, idx);
				first++;
			}
			for (int i = 0; i < nb; i++) {
//				ClusNode inf = (ClusNode)leaves.elementAt(i);
//				inf.subtractMissing();
			}
		} else {
			for (int i = 0; i < nb; i++) {
//				ClusNode inf = (ClusNode)leaves.elementAt(i);
//				inf.copyTotal();
			}
		}
		// Only if different from previous
		for (int i = first; i < m_NbRows; i++) {
/*			ClusNode inf = infos[m_Index[i]];
			if (!inf.m_Finished) {
				double prev = inf.m_PrevDouble;
				if (m_Data[i] != prev) {
					if (prev != Double.NaN) inf.updateNumeric(m_Data[i], this);
					inf.m_PrevDouble = m_Data[i];
				}
				// Updata positive statistic
				inf.m_PosStat.update(target, m_Index[i]);
			}*/
		}
	}

	public void split(ColTarget target) {
		// For each attribute value
		// ClusNode[] infos = target.m_Node;
		// Move each example
		for (int i = 0; i < m_NbRows; i++) {
/*			int idx = m_Index[i];
			ClusNode inf = infos[idx];
			if (inf.m_SplitAttr == this) {
				// Predict child
				int n_idx = inf.m_BestTest.numericPredict(m_Data[i]);
				ClusNode node = inf.getChild(n_idx);
				// Move example to child
				infos[idx] = node;
				node.m_TotStat.update(target, idx);
			}*/
		}
	}

	public boolean read(ClusReader data, int row) throws IOException {
		if (!data.readNoSpace()) return false;
		double val = m_Data[row] = data.getFloat();
		if (val == Double.POSITIVE_INFINITY) m_Type.incNbMissing();
		return true;
	}
}


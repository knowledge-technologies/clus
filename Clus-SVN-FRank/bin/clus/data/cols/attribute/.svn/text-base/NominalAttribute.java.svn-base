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

import clus.main.*;
import clus.data.cols.*;
import clus.data.io.ClusReader;
import clus.data.type.*;
import clus.selection.*;

public class NominalAttribute extends NominalAttrBase {

	public int[] m_Data;

	protected int m_NbRows;
	protected final static boolean DEBUG = true;

	public NominalAttribute(NominalAttrType type) {
		super(type);
	}

	public void resize(int rows) {
		m_NbRows = rows;
		m_Data = new int[rows];
	}

	public ClusAttribute select(ClusSelection sel, int nbsel) {
		int s_data = 0;
		int s_subset = 0;
		int[] data = m_Data;
		m_Data = new int[m_NbRows-nbsel];
		int[] subset = new int[nbsel];
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) subset[s_subset++] = data[i];
			else m_Data[s_data++] = data[i];
		}
		m_NbRows -= nbsel;
		NominalAttribute s_attr = new NominalAttribute(m_Type);
		s_attr.m_Data = subset;
		s_attr.m_NbRows = nbsel;
		return s_attr;
	}

	public void insert(ClusAttribute attr, ClusSelection sel, int nb_new) {
		int s_data = 0;
		int s_subset = 0;
		int[] data = m_Data;
		m_Data = new int[nb_new];
		int[] subset = ((NominalAttribute)attr).m_Data;
		for (int i = 0; i < nb_new; i++) {
			if (sel.isSelected(i)) m_Data[i] = subset[s_subset++];
			else m_Data[i] = data[s_data++];
		}
		m_NbRows = nb_new;
	}

	public void findBestTest(MyArray leaves, ColTarget target, ClusStatManager smanager) {
		// Reset positive statistic
		int nb = leaves.size();
//		int nbvalues = m_Type.getNbValues();
//		int statsize = nbvalues + m_Type.intHasMissing();
		for (int i = 0; i < nb; i++) {
//			ClusNode inf = (ClusNode)leaves.elementAt(i);
//			inf.reset(statsize);
		}
		// For each attribute value
/*		ClusNode[] infos = target.getNodes();
		for (int i = 0; i < m_NbRows; i++) {
			ClusNode inf = infos[i];
			if (!inf.m_Finished) {
				inf.m_TestStat[m_Data[i]].update(target, i);
			}
		} */
		// Find best split
		findSplit(leaves, smanager);
	}

	public void findSplit(MyArray leaves, ClusStatManager smanager) {
/*		NominalSplit split;
		int nb = leaves.size();
		if (Settings.BINARY_SPLIT) split = new SubsetSplit();
		else split = new NArySplit();
		split.initialize(smanager.getStatistic());
		for (int i = 0; i < nb; i++) {
			ClusNode node = (ClusNode)leaves.elementAt(i);
			split.findSplit(node, m_Type, this);
		}*/
	}

	public void split(ColTarget target) {
		// For each attribute value
		// ClusNode[] infos = target.m_Node;
		// Move each example
/*		for (int i = 0; i < m_NbRows; i++) {
			ClusNode inf = infos[i];
			if (inf.m_SplitAttr == this) {
				// Predict child
				int n_idx = inf.m_BestTest.nominalPredict(m_Data[i]);
				ClusNode node = inf.getChild(n_idx);
				// Move example to child
				infos[i] = node;
				node.m_TotStat.update(target, i);
			}
		}*/
	}

	public boolean read(ClusReader data, int row) throws IOException {
		String value = data.readString();
		if (value == null) return false;
		if (value.equals("?")) {
			m_Type.incNbMissing();
			m_Data[row] = m_Type.getNbValues();
		} else {
			Integer i = (Integer)m_Type.getValueIndex(value);
			if (i != null) {
				m_Data[row] = i.intValue();
			} else {
				throw new IOException("Illegal value '"+value+"' for attribute "+getName()+" at row "+(row+1));
			}
		}
		return true;
	}
}

/*						if (DEBUG) {
							boolean first = true;
							System.out.print("  "+m_Type.getName()+" in {");
							for (int k = 0; k < nbvalues; k++) {
								if (isin[k] || k == j) {
									if (first) first = false;
									else System.out.print(",");
									System.out.print(m_Type.getValue(k));
								}
							}
							System.out.println("} = "+mheur+" t="+mstat.m_SumWeight+" p="+cstat.m_SumWeight);
						}
*/



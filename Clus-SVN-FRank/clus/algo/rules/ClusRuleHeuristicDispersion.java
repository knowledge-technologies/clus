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

/*
 * Created on August 4, 2006
 * ClusRuleHeuristicCompactnes replaced by this abstract class and its
 * sub-classes: *DispersionAdt, *DispersionMlt, *WRDispersionAdt, *WRDispersionMlt
 * Original ClusRuleHeuristicCompactnes created on June 23, 2005
 */
package clus.algo.rules;

import java.util.*;

import clus.heuristic.*;
import clus.main.ClusStatManager;
import clus.main.Settings;

public abstract class ClusRuleHeuristicDispersion extends ClusHeuristic {

	public ClusStatManager m_StatManager = null;
	public int[] m_DataIndexes;
	public int[][] m_DataIndexesPerVal;
	public ArrayList m_CoveredBitVectArray;
	public int m_NbTuples;

	public void setDataIndexes(int[] indexes) {
		m_DataIndexes = indexes;
	}

	public void setDataIndexes(boolean[] isin) {
		if ((m_DataIndexesPerVal != null) && (isin.length == m_DataIndexesPerVal.length)) {
			int size = 0;
			for (int i = 0; i < isin.length; i++) {
				if (isin[i]) {
					size += m_DataIndexesPerVal[i].length;
				}
			}
			int[] new_data_idx = new int[size];
			int pt = 0;
			for (int i = 0; i < m_DataIndexesPerVal.length; i++) {
				if (isin[i]) {
					System.arraycopy(m_DataIndexesPerVal[i],0,new_data_idx,pt,m_DataIndexesPerVal[i].length);
					pt += m_DataIndexesPerVal[i].length;
				}
			}
			setDataIndexes(new_data_idx);
		} else {
			System.err.println("ClusRuleHeuristicDispersion: setDataIndexes(boolean[])");
			System.exit(1); // Exception???
		}
	}

	public void setDataIndexesPerVal(int[][] indexes) {
		m_DataIndexesPerVal = indexes;
	}

	public int[][] getDataIndexesPerVal() {
		return m_DataIndexesPerVal;
	}

	public void initCoveredBitVectArray(int size) {
		m_CoveredBitVectArray = new ArrayList();
		m_NbTuples = size;
	}

	public void setCoveredBitVectArray(ArrayList bit_vect_array) {
		m_CoveredBitVectArray = bit_vect_array;
	}

  public Settings getSettings() {
    return m_StatManager.getSettings();
  }

}

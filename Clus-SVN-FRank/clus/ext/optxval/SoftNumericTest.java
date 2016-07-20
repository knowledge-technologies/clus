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

package clus.ext.optxval;

import jeans.util.sort.*;
import jeans.util.array.*;

import java.util.*;

import clus.model.test.*;
import clus.main.*;
import clus.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;
import clus.data.rows.*;

public class SoftNumericTest extends SoftTest {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected NumericAttrType m_Type;
	protected double[] m_Bounds;
	protected int[] m_Folds;
	protected int[][] m_BoundFolds;
	protected int[][] m_PosFolds;
	protected int[][] m_NegFolds;

	public SoftNumericTest(NodeTest test, int gsize) {
		NumericTest ntest = (NumericTest)test;
		m_Type = ntest.getNumType();
		m_Bounds = new double[gsize];
		m_Folds = new int[gsize];
		setArity(2);
	}

	public String getString() {
		StringBuffer buff = new StringBuffer();
		buff.append(m_Type.getName());
		buff.append(" > [");
		for (int i = 0; i < m_Bounds.length; i++) {
			if (i != 0) buff.append("; ");
			buff.append(ClusFormat.FOUR_AFTER_DOT.format(m_Bounds[i]));
			buff.append(" ");
			int[] folds = m_BoundFolds[i];
			for (int j = 0; j < folds.length; j++) {
				if (j != 0) buff.append(",");
				buff.append(folds[j]);
			}
		}
		buff.append("]");
		return buff.toString();
	}

	public String getPythonString() {
		return getString();
	}
	
	public int foldSetIntersects2(DataTuple data, int[] folds) {
		int[] dfolds = data.m_Folds;
		for (int i = 0; i < folds.length; i++) {
			if (dfolds[folds[i]] > 0) return 1;
		}
		return 0;
	}

	public int softPredictNb2(DataTuple tuple, int branch) {
		double value = tuple.m_Doubles[m_Type.getArrayIndex()];
		if (branch == ClusNode.YES) {
			if (value > m_Bounds[0]) return 1;
			if (value <= m_Bounds[m_Bounds.length-1]) return 0;
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return foldSetIntersects2(tuple, m_PosFolds[ps]);
		} else {
			if (value > m_Bounds[0]) return 0;
			if (value <= m_Bounds[m_Bounds.length-1]) return 1;
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return foldSetIntersects2(tuple, m_NegFolds[ps]);
		}
	}

	public int foldSetIntersects(DataTuple data, int[] folds) {
		int fold = data.m_Index;
		if (fold == -1) {
			return MyIntArray.isIntersectSorted(data.m_Folds, folds);
		} else {
			if (Arrays.binarySearch(folds, fold) >= 0) return folds.length > 1 ? 1 : 0;
			else return 1;
		}
	}

	public int softPredictNb(DataTuple tuple, int branch) {
		double value = tuple.m_Doubles[m_Type.getArrayIndex()];
		if (branch == ClusNode.YES) {
			if (value > m_Bounds[0]) return 1;
			if (value <= m_Bounds[m_Bounds.length-1]) return 0;
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return foldSetIntersects(tuple, m_PosFolds[ps]);
		} else {
			if (value > m_Bounds[0]) return 0;
			if (value <= m_Bounds[m_Bounds.length-1]) return 1;
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return foldSetIntersects(tuple, m_NegFolds[ps]);
		}
	}

	public int[] updateFoldSet(DataTuple data, int[] folds) {
		int[] rfolds;
		int fold = data.m_Index;
		if (fold == -1) {
			rfolds = MyIntArray.intersectSorted(data.m_Folds, folds);
		} else {
			if (Arrays.binarySearch(folds, fold) >= 0) {
				rfolds = MyIntArray.remove(fold, folds);
			} else {
				rfolds = folds;
			}
		}
		return rfolds;
	}

	public int updateFoldSet(RowData data, int idx, DataTuple tuple, int[] folds) {
		int[] res = updateFoldSet(tuple, folds);
		if (res.length > 0) {
			DataTuple clone = tuple.cloneTuple();
			clone.m_Index = -1;
			clone.m_Folds = res;
			data.setTuple(clone, idx++);
		}
		return idx;
	}

	public int softPredict(RowData res, DataTuple tuple, int idx, int branch) {
		double value = tuple.m_Doubles[m_Type.getArrayIndex()];
		if (branch == ClusNode.YES) {
			if (value <= m_Bounds[m_Bounds.length-1]) return idx;
			if (value > m_Bounds[0]) {
				res.setTuple(tuple, idx++);
				return idx;
			}
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return updateFoldSet(res, idx, tuple, m_PosFolds[ps]);
		} else {
			if (value > m_Bounds[0]) return idx;
			if (value <= m_Bounds[m_Bounds.length-1]) {
				res.setTuple(tuple, idx++);
				return idx;
			}
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return updateFoldSet(res, idx, tuple, m_NegFolds[ps]);
		}
	}

	public int updateFoldSet2(RowData data, int idx, DataTuple tuple, int[] folds) {
		if (foldSetIntersects2(tuple, folds) != 0) {
			DataTuple clone = tuple.cloneTuple();
			int[] origflds = tuple.m_Folds;
			clone.m_Folds = new int[origflds.length];
			for (int i = 0; i < folds.length; i++) {
				int ps = folds[i];
				clone.m_Folds[ps] = origflds[ps];
			}
			data.setTuple(clone, idx++);
		}
		return idx;
	}

	public int softPredict2(RowData res, DataTuple tuple, int idx, int branch) {
		double value = tuple.m_Doubles[m_Type.getArrayIndex()];
		if (branch == ClusNode.YES) {
			if (value <= m_Bounds[m_Bounds.length-1]) return idx;
			if (value > m_Bounds[0]) {
				res.setTuple(tuple, idx++);
				return idx;
			}
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return updateFoldSet2(res, idx, tuple, m_PosFolds[ps]);
		} else {
			if (value > m_Bounds[0]) return idx;
			if (value <= m_Bounds[m_Bounds.length-1]) {
				res.setTuple(tuple, idx++);
				return idx;
			}
			int ps = 1;
			while (value <= m_Bounds[ps]) ps++;
			return updateFoldSet2(res, idx, tuple, m_NegFolds[ps]);
		}
	}

	public void addTest(int idx, int fold, NodeTest test) {
		NumericTest ntest = (NumericTest)test;
		m_Bounds[idx] = ntest.getBound();
		m_Folds[idx] = fold;
	}

	public void sortIntervals() {
		DoubleIndexSorter sort = DoubleIndexSorter.getInstance();
		sort.setData(m_Bounds, m_Folds);
		sort.sort();
		// Count number of thresholds
		int nb = 0;
		double prev = Double.NaN;
		for (int i = 0; i < m_Bounds.length; i++) {
			if (m_Bounds[i] != prev) {
				nb++;
				prev = m_Bounds[i];
			}
		}
		// Resize arrays
		double[] old = m_Bounds;
		m_Bounds = new double[nb];
		m_BoundFolds = new int[nb][];
		// Fill arrays
		int idx = 0;
		prev = old[0];
		int nb_folds = 0;
		for (int i = 0; i <= old.length; i++) {
			if (i == old.length || old[i] != prev) {
				if (nb_folds > 0) {
					int[] folds = (m_BoundFolds[idx] = new int[nb_folds]);
					for (int j = 0; j < nb_folds; j++) folds[j] = m_Folds[i-j-1];
					Arrays.sort(folds);
					nb_folds = 0;
				}
				m_Bounds[idx++] = prev;
				if (i < old.length) prev = old[i];
			}
			nb_folds++;
		}
		// Clean folds
		m_Folds	= null;
		// Make pos and neg bounds
		m_PosFolds = new int[nb][];
		m_NegFolds = new int[nb][];
		int[] prev_v = m_PosFolds[nb-1] = m_BoundFolds[nb-1];
		for (int i = nb-2; i > 0; i--) {
			prev_v = m_PosFolds[i] = MyIntArray.mergeSorted(prev_v, m_BoundFolds[i]);
		}
		prev_v = m_NegFolds[1] = m_BoundFolds[0];
		for (int i = 2; i < nb; i++) {
			prev_v = m_NegFolds[i] = MyIntArray.mergeSorted(prev_v, m_BoundFolds[i-1]);
		}
	}

	public ClusAttrType getType() {
		return m_Type;
	}

	public void setType(ClusAttrType type) {
		m_Type = (NumericAttrType)type;
	}
}

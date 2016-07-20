
package jeans.util.array;

import java.util.Comparator;

public class MDoubleArrayComparator implements Comparator {

	protected int m_Index;

	public MDoubleArrayComparator(int idx) {
		m_Index = idx;
	}

	public int compare(Object arg0, Object arg1) {
		double arg0d = ((double[])arg0)[m_Index];
		double arg1d = ((double[])arg1)[m_Index];
		if (arg0d == arg1d) {
			return 0;
		} else {
			return arg0d > arg1d ? 1 : -1;
		}
	}
}

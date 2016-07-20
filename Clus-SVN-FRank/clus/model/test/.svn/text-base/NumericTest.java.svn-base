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

package clus.model.test;

import clus.main.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;
import clus.util.*;
import clus.data.rows.*;

public class NumericTest extends NodeTest {

	protected double m_Bound;
	protected NumericAttrType m_Type;

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public NumericTest(ClusAttrType attr, double bound, double posfreq) {
		m_Type = (NumericAttrType)attr;
		m_Bound = bound;
		setArity(2);
		setPosFreq(posfreq);
	}

	public NumericTest(ClusAttrType attr) {
		this(attr, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public boolean isInverseNumeric() {
		return false;
	}

	public final int getAttrIndex() {
		return m_Type.getArrayIndex();
	}

	public final NumericAttrType getNumType() {
		return m_Type;
	}

	public final double getBound() {
		return m_Bound;
	}

	public final void setBound(double bound) {
		m_Bound = bound;
	}

	public ClusAttrType getType() {
		return m_Type;
	}

	public void setType(ClusAttrType type) {
		m_Type = (NumericAttrType)type;
	}

	public String getString() {
		String value = m_Bound != Double.NEGATIVE_INFINITY ? String.valueOf(m_Bound) : "?";
		return m_Type.getName() + " > " + value;
	}

	public String getPythonString() {
		return getString();
	}
		
	public boolean hasConstants() {
		return m_Bound != Double.NEGATIVE_INFINITY;
	}

	public boolean equals(NodeTest test) {
		if (m_Type != test.getType()) return false;
		NumericTest ntest = (NumericTest)test;
		if (isInverseNumeric() != ntest.isInverseNumeric()) return false;
		return m_Bound == ntest.m_Bound;
	}

	public int hashCode() {
		long v = Double.doubleToLongBits(m_Bound);
		return m_Type.getIndex() + (int)(v^(v>>>32));
	}

	public int softEquals(NodeTest test) {
		if (m_Type != test.getType()) return N_EQ;
		NumericTest ntest = (NumericTest)test;
		if (m_Bound == ntest.m_Bound) return H_EQ;
		if (Math.abs(getPosFreq() - ntest.getPosFreq()) < 0.1) return S_EQ;
		return N_EQ;
	}

	public int numericPredict(double value) {
		if (value == Double.POSITIVE_INFINITY)
			return ClusRandom.nextDouble(ClusRandom.RANDOM_TEST_DIR) < getPosFreq() ?
			       ClusNode.YES : ClusNode.NO;
		return value > m_Bound ? ClusNode.YES : ClusNode.NO;
	}

	public int numericPredictWeighted(double value) {
		if (value == Double.POSITIVE_INFINITY) {
			return hasUnknownBranch() ? ClusNode.UNK : UNKNOWN;
		} else {
			return value > m_Bound ? ClusNode.YES : ClusNode.NO;
		}
	}

	public int predictWeighted(DataTuple tuple) {
		double val = m_Type.getNumeric(tuple);
		return numericPredictWeighted(val);
	}

	public NodeTest getBranchTest(int i) {
		if (i == ClusNode.YES) {
			return this;
		} else {
			return new InverseNumericTest(m_Type, getBound(), 1.0-getPosFreq());
		}
	}

	public NodeTest simplifyConjunction(NodeTest other) {
		if (getType().getIndex() != other.getType().getIndex()) {
			return null;
		} else {
			NumericTest onum = (NumericTest)other;
			if (isInverseNumeric() != onum.isInverseNumeric()) {
				return null;
			}
			double pos_freq = Math.min(getPosFreq(), onum.getPosFreq());
			if (isInverseNumeric()) {
				double new_bound = Math.min(getBound(), onum.getBound());
				return new InverseNumericTest(m_Type, new_bound, pos_freq);
			} else {
				double new_bound = Math.max(getBound(), onum.getBound());
				return new NumericTest(m_Type, new_bound, pos_freq);
			}
		}
	}
}

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

import java.io.Serializable;
import java.util.*;

import clus.util.*;
import clus.main.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;
import clus.data.rows.*;

public abstract class NodeTest implements Serializable {

	public final static int UNKNOWN = -1;

	public final static int N_EQ = 0;	// Not equal
	public final static int S_EQ = 1;	// Soft equal
	public final static int H_EQ = 2;	// Hard equal

	public boolean m_UnknownBranch;
	public double m_UnknownFreq;
	public double[] m_BranchFreq;
	public double m_HeuristicValue;

	public final boolean hasUnknownBranch()	{
		return m_UnknownBranch;
	}

/***************************************************************************
 * Prediction methods
 ***************************************************************************/

/*
	// In case of binary, first branch (0) is "yes"
	public abstract int predict(ClusAttribute attr, int idx);
*/

	// Prediction for tuples
	public abstract int predictWeighted(DataTuple tuple);

	// Prediction for nominal var
	public int nominalPredict(int value) {
		return -1;
	}

	// Prediction for numeric var
	public int numericPredict(double value) {
		return -1;
	}

	// Prediction for nominal var
	public int nominalPredictWeighted(int value) {
		return -1;
	}

	// Prediction for numeric var
	public int numericPredictWeighted(double value) {
		return -1;
	}

	public boolean isUnknown(DataTuple tuple) {
		return getType().isMissing(tuple);
	}

/***************************************************************************
 * Proportion of examples in different branches
 ***************************************************************************/

	// Get proportion of examples in branch
	public final double getProportion(int branch) {
		return m_BranchFreq[branch];
	}

	public final void setProportion(int branch, double prop) {
		m_BranchFreq[branch] = prop;
	}

	public final void setProportion(double[] prop) {
		m_BranchFreq = prop;
	}

	public final void setPosFreq(double prop) {
		m_BranchFreq[ClusNode.YES] = prop;
		m_BranchFreq[ClusNode.NO] = 1.0-prop;
	}

	public final double getPosFreq() {
		return m_BranchFreq[ClusNode.YES];
	}

	public final double getUnknownFreq() {
		return m_UnknownFreq;
	}

	public final void setUnknownFreq(double unk) {
		m_UnknownFreq = unk;
	}

/***************************************************************************
 * Arity methods
 ***************************************************************************/

	// Is test binary
	public final boolean isBinary() {
		return m_BranchFreq.length == 2;
	}

	// Binary tests are default
	public final int getNbChildren() {
		return m_BranchFreq.length;
	}

	public final void setArity(int arity) {
		m_BranchFreq = new double[arity];
	}

	public final int updateArity() {
//		if (getUnknownFreq() > 0.0) addUnknownBranch();
		return getNbChildren();
	}

	/*
	private void addUnknownBranch() {
		m_UnknownBranch = true;
		int arity = getNbChildren();
		double unkfreq = getUnknownFreq();
		double[] oldBFreq = m_BranchFreq;
		m_BranchFreq = new double[arity+1];
		for (int i = 0; i < arity; i++) {
			m_BranchFreq[i] = (1-unkfreq)*oldBFreq[i];
		}
		m_BranchFreq[arity] = unkfreq;
	}
	*/

/***************************************************************************
 * Special things
 ***************************************************************************/

	// Some tests must be processed before used
	public void preprocess(int mode) {
	}

	// Is this a soft test?
	public boolean isSoft() {
		return false;
	}

	// Soft equality
	public int softEquals(NodeTest test) {
		return equals(test) ? H_EQ : N_EQ;
	}

	// Returns true if this test has constants filled in
	// this is useful for the syntactic constraints
	public boolean hasConstants() {
		return true;
	}

	public NodeTest getBranchTest(int i) {
		return null;
	}

	public NodeTest simplifyConjunction(NodeTest other) {
		return null;
	}

/***************************************************************************
 * Equality test
 ***************************************************************************/

	// Equality test
	public abstract boolean equals(NodeTest test);

	public int hashCode() {
		return 1111;
	}

/***************************************************************************
 * Attribute type
 ***************************************************************************/

	// Attribute type to split on
	public abstract ClusAttrType getType();

	// Switch type
	public abstract void setType(ClusAttrType type);

/***************************************************************************
 * String methods
 ***************************************************************************/

	// String representation
	public abstract String getString();

	// String representation for Python
	public abstract String getPythonString();
	
	// String representation (for branch - not used for binary)
	public String getBranchString(int i) {
		return null;
	}

	public String getBranchLabel(int i) {
		return getBranchString(i);
	}

	public boolean hasBranchLabels() {
		return false;
	}

	public void setHeuristicValue(double value){
		m_HeuristicValue = value;
	}

	public double getHeuristicValue(){
		return m_HeuristicValue;
	}

	public final void attachModel(HashMap table) throws ClusException {
		ClusAttrType type = getType();
		ClusAttrType ntype = (ClusAttrType)table.get(type.getName());
		if (ntype == null) throw new ClusException("Attribute "+type.getName()+" not in dataset");
		ClusAttrType ctype = type.cloneType();
		// Clone type to avoid overwriting original type
		ctype.copyArrayIndex(ntype);
		setType(ctype);
	}

	public int getNbLines() {
		return 1;
	}

	public String getLine(int i) {
		return getString();
	}

	public final String getTestString() {
		String str = getString();
		if (Settings.SHOW_BRANCH_FREQ) {
			if (getPosFreq() != Double.NEGATIVE_INFINITY) {
				String bfr = ClusFormat.ONE_AFTER_DOT.format(getPosFreq()*100);
				str += " (" + bfr + "%)";
			}
		}
		if (Settings.SHOW_UNKNOWN_FREQ) {
			String unk = ClusFormat.ONE_AFTER_DOT.format(getUnknownFreq()*100);
			str += " (miss: " + unk + "%)";
		}
		return str;
	}

	public final String getPythonTestString() {
		String str = getPythonString();
		if (Settings.SHOW_BRANCH_FREQ) {
			if (getPosFreq() != Double.NEGATIVE_INFINITY) {
				String bfr = ClusFormat.ONE_AFTER_DOT.format(getPosFreq()*100);
				str += " (" + bfr + "%)";
			}
		}
		if (Settings.SHOW_UNKNOWN_FREQ) {
			String unk = ClusFormat.ONE_AFTER_DOT.format(getUnknownFreq()*100);
			str += " (miss: " + unk + "%)";
		}
		return str;
	}
	
	public final String getTestString(int idx) {
		String str = getBranchString(idx);
		if (Settings.SHOW_BRANCH_FREQ) {
			String bfr = ClusFormat.ONE_AFTER_DOT.format(getProportion(idx)*100);
			str += " (" + bfr + "%)";
		}
		if (idx == 0 && Settings.SHOW_UNKNOWN_FREQ) {
			String unk = ClusFormat.ONE_AFTER_DOT.format(getUnknownFreq()*100);
			str += " (miss:" + unk + "%)";
		}
		return str;
	}

	// Get string representation of test
	public final String toString() {
		return getString();
	}
}

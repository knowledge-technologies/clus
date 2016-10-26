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

package clus.error;

import java.io.*;
import java.text.*;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.attweights.*;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.*;
import clus.util.ClusException;

public abstract class ClusError implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int DETAIL_NEVER_VISIBLE = 0;
	public final static int DETAIL_SMALL = 1;
	public final static int DETAIL_ALWAYS_VISIBLE = 2;
	public final static int DETAIL_VERY_SMALL = 3;

	protected final static String DEFAULT_ERROR  = "Default error: ";
	protected final static String TREE_ERROR     = "Tree error: ";
	protected final static String RELATIVE_ERROR = "Relative error: ";

	protected final static String DEFAULT_POSTFIX  = " ";
	protected final static String TREE_POSTFIX     = "    ";
	protected final static String RELATIVE_POSTFIX = "";

	/** Number of dimensions to compute the error for */
	protected int m_Dim;
	protected ClusErrorList m_Parent;

	public ClusError(ClusErrorList par, int dim) {
		m_Dim = dim;
		m_Parent = par;
	}

	public ClusError(ClusErrorList par) {
		this(par, 0);
	}

	public boolean shouldBeLow() {
		// errors should normally be small to be good
		// measures such as accuracy should overwrite this method to return false (in the subclass)
		return true;
	}

	public boolean isMultiLine() {
		return false;
	}

	public boolean isComputeForModel(String name) {
		return true;
	}

	public void setWeights(ClusAttributeWeights weights) {
	}

/***************************************************************************
 * Information
 ***************************************************************************/

	public abstract String getName();

/***************************************************************************
 * Operations
 ***************************************************************************/

	public void reset() {
		System.err.println(getClass().getName()+": reset() not implemented!");
	}

	public void normalize(double[] fac) {
		System.err.println(getClass().getName()+": normalize() not implemented!");
	}

	public void add(ClusError other) {
		System.err.println(getClass().getName()+": add() not implemented!");
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		System.err.println(getClass().getName()+": addExample() not implemented!");
	}

	public void addExample(DataTuple real, DataTuple pred) {
		System.err.println(getClass().getName()+": addExample() not implemented!");
	}

	public void addInvalid(DataTuple tuple) {
		System.err.println(getClass().getName()+": addInvalid() not implemented!");
	}

	public void compute(RowData data, ClusModel model) throws ClusException {
		System.err.println(getClass().getName()+": compute() not implemented!");
	}

	// For errors computed on a subset of the examples, it is sometimes useful
	// to also have information about all the examples, this information is
	// passed via this method in the global error measure "global"
	public void updateFromGlobalMeasure(ClusError global) {
	}


/***************************************************************************
 * Inspectors
 ***************************************************************************/

	// getModelError() returns the error averaged over all targets
	public double getModelError() {
		return getModelErrorAdditive() / getNbExamples();
	}

	// getModelErrorAdditive() returns the error NOT divided by the number of examples
	public double getModelErrorAdditive() {
		System.out.println(getClass().getName()+"::getModelErrorAdditive() not implemented!");
		return 0.0;
	}

	public double getModelErrorStandardError() {
		return Double.POSITIVE_INFINITY;
	}

	public double getModelErrorComponent(int i) {
		return 0.0;
	}

	public abstract ClusError getErrorClone(ClusErrorList par);

	public ClusError getErrorClone() {
		return getErrorClone(getParent());
	}

	public int getDetailLevel() {
		return DETAIL_ALWAYS_VISIBLE;
	}

	public double get_error_classif(){
		// only implemeted in the ContengyTableClass
		return 0;
	}

	public double get_accuracy() {
		// only implemeted in the ContengyTableClass
		return 0.0;
	}

	public double get_precision() {
		// only implemeted in the ContengyTableClass
		return 0.0;
	}

	public double get_recall() {
		// only implemeted in the ContengyTableClass
		return 0.0;
	}

	public double get_auc() {
		// only implemeted in the ContengyTableClass
		return 0.0;
	}

/***************************************************************************
 * Display
 * @throws IOException 
 ***************************************************************************/

	public void showModelError(PrintWriter out, String bName, int detail) throws IOException {
        showModelError(out, detail);
	}
	
	public void showModelError(PrintWriter out, int detail) {
		for (int i = 0; i < m_Dim; i++) {
			if (i != 0) out.print(", ");
			out.print(getModelErrorComponent(i));
		}
		out.println();
	}

	public void showRelativeModelError(PrintWriter out, int detail) {
		showModelError(out, detail);
	}

/***************************************************************************
 * Old stuff - to be removed :-)
 ***************************************************************************/

	public ClusErrorList getParent() {
		return m_Parent;
	}

	public String getPrefix() {
		return m_Parent.getPrefix();
	}

	public int getNbExamples() {
		return m_Parent.getNbExamples();
	}

	public int getNbTotal() {
		return m_Parent.getNbTotal();
	}

	public int getNbCover() {
		return m_Parent.getNbCover();
	}

	public double getCoverage() {
		int nb = getNbTotal();
		return nb == 0 ? 0.0 : (double)getNbCover() / nb;
	}

	public NumberFormat getFormat() {
		return m_Parent.getFormat();
	}

	public int getDimension() {
		return m_Dim;
	}

	public String showDoubleArray(double[] arr) {
		NumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < arr.length; i++) {
			if (i != 0) buf.append(",");
			buf.append(fr.format(arr[i]));
		}
		buf.append("]");
		return buf.toString();
	}

	public String showDoubleArray(double[] arr1, double[] arr2) {
		NumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < arr1.length; i++) {
			double el = arr2[i] != 0.0 ? arr1[i] / arr2[i] : 0.0;
			if (i != 0) buf.append(",");
			buf.append(fr.format(el));
		}
		buf.append("]");
		return buf.toString();
	}

	public String showDoubleArray(double[] arr1, double div) {
		NumberFormat fr = getFormat();
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < arr1.length; i++) {
			double el = div != 0.0 ? arr1[i] / div : 0.0;
			if (i != 0) buf.append(",");
			buf.append(fr.format(el));
		}
		buf.append("]");
		return buf.toString();
	}

	public double computeLeafError(ClusStatistic stat) {
		System.out.println(getClass().getName()+"::computeLeafError() not yet implemented");
		return 0;
	}

	public double computeTreeErrorClusteringAbsolute(ClusNode tree) {
		if (tree.atBottomLevel()) {
			return computeLeafError(tree.getClusteringStat());
		} else {
			double result = 0.0;
			for (int i = 0; i < tree.getNbChildren(); i++) {
				ClusNode child = (ClusNode)tree.getChild(i);
				result += computeTreeErrorClusteringAbsolute(child);
			}
			return result;
		}
	}

//	public abstract void addExample(ClusData data, int idx, ClusStatistic pred);

}

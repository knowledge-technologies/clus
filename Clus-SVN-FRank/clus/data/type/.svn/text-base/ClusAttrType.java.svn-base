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
import clus.main.*;
import clus.util.*;
import clus.data.rows.*;
import clus.data.cols.*;
import clus.data.cols.attribute.*;

public abstract class ClusAttrType implements Serializable, Comparable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	// Attributes are sorted in arrays in same order as this: TARGET, OTHER CLUSTER, NORMAL, KEY
	public final static int STATUS_DISABLED = 0;
	public final static int STATUS_TARGET = 1;
	public final static int STATUS_CLUSTER_NO_TARGET = 2;
	public final static int STATUS_NORMAL = 3;
	public final static int STATUS_KEY = 4;
	public final static int NB_STATUS = 5;

	// Attribute use types
	public final static int ATTR_USE_ALL = 0;
	public final static int ATTR_USE_DESCRIPTIVE = 1;
	public final static int ATTR_USE_CLUSTERING = 2;
	public final static int ATTR_USE_TARGET = 3;
	public final static int ATTR_USE_KEY = 4;
	public final static int NB_ATTR_USE = 5;

	public final static int VALUE_TYPE_NONE = -1;
	public final static int VALUE_TYPE_INT = 0;
	public final static int VALUE_TYPE_DOUBLE = 1;
	public final static int VALUE_TYPE_OBJECT = 2;
	public final static int VALUE_TYPE_BITWISEINT = 3;
	public final static int NB_VALUE_TYPES = 4;

	public final static int NB_TYPES = 5;
	public final static int THIS_TYPE = -1;

	protected String m_Name;
	protected int m_Index, m_ArrayIndex;
	protected int m_NbMissing;
	protected ClusSchema m_Schema;
	protected int m_Status = STATUS_NORMAL;
	protected boolean m_IsDescriptive;
	protected boolean m_IsClustering;

	public ClusAttrType(String name) {
		m_Name = name;
		m_Index = -1;
		m_ArrayIndex = -1;
	}

	public void setSchema(ClusSchema schema) {
		m_Schema = schema;
	}

	public ClusSchema getSchema() {
		return m_Schema;
	}

	public Settings getSettings() {
		return m_Schema.getSettings();
	}

	public abstract ClusAttrType cloneType();

	public void cloneType(ClusAttrType type) {
		type.m_NbMissing = m_NbMissing;
		type.m_Status = m_Status;
		type.m_IsDescriptive = m_IsDescriptive;
		type.m_IsClustering = m_IsClustering;
	}

	public void copyArrayIndex(ClusAttrType type) {
		m_ArrayIndex = type.m_ArrayIndex;
	}

	public abstract int getTypeIndex();

	public abstract int getValueType();

	public abstract String getTypeName();

	public int intHasMissing() {
		return m_NbMissing > 0 ? 1 : 0;
	}

	public boolean hasMissing() {
		return m_NbMissing > 0;
	}

	public int getNbMissing() {
		return m_NbMissing;
	}

	public void incNbMissing() {
		m_NbMissing++;
	}

	public void setNbMissing(int nb) {
		m_NbMissing = nb;
	}

	public String getName() {
		return m_Name;
	}

	public void setName(String name) {
		m_Name = name;
	}

	public int getIndex() {
		return m_Index;
	}

	public void setIndex(int idx) {
		m_Index = idx;
	}

	public final int getArrayIndex() {
		return m_ArrayIndex;
	}

	public void setArrayIndex(int idx) {
		m_ArrayIndex = idx;
	}

	public int getStatus() {
		return m_Status;
	}

	public void setStatus(int status) {
		m_Status = status;
	}

	public boolean isTarget() {
		return m_Status == ClusAttrType.STATUS_TARGET;
	}

	public boolean isDisabled() {
		return m_Status == ClusAttrType.STATUS_DISABLED;
	}

	public boolean isKey() {
		return m_Status == ClusAttrType.STATUS_KEY;
	}

	public boolean isClustering() {
		return m_IsClustering;
	}

	public void setClustering(boolean clust) {
		m_IsClustering = clust;
	}

	public void setDescriptive(boolean descr) {
		m_IsDescriptive = descr;
	}

	public boolean isDescriptive() {
		return m_IsDescriptive;
	}

	public int getMaxNbStats() {
		return 0;
	}

	public void setReader(boolean start_stop) {
	}

	public ClusAttrType getType() {
		return this;
	}

	public void setNbRows(int nbrows) {
	}

	public int getNominal(DataTuple tuple) {
		return -1;
	}

	public double getNumeric(DataTuple tuple) {
		return Double.POSITIVE_INFINITY;
	}

	public boolean isMissing(DataTuple tuple) {
		return true;
	}

	public void updatePredictWriterSchema(ClusSchema schema) {
		schema.addAttrType(cloneType());
	}

	public String getPredictionWriterString(DataTuple tuple) {
		return getString(tuple);
	}

	public String getString(DataTuple tuple) {
		return "err";
	}

	public int compareValue(DataTuple t1, DataTuple t2) {
		return -5;
	}

	public void getPreprocs(DataPreprocs pps, boolean single) {
	}

	public ClusSerializable createRowSerializable() throws ClusException {
		throw new ClusException("Attribute "+getName()+" does not support row data");
	}

	public ClusAttribute createTargetAttr(ColTarget target) throws ClusException {
		throw new ClusException("Attribute "+getName()+" can not be target: incompatible type");
	}

	public String toString() {
		return getName();
	}

	public void initializeBeforeLoadingData() throws IOException, ClusException {
		// This method is called before loading the data, but after setting the attribute's status
		// For example, ext.hierarchical.ClassesAttrType uses this to initialize the class hierarchy.
	}

	public void initializeFrom(ClusAttrType other_type) {
		// Currently does nothing, but could copy status etc.
		// For example, ext.hierarchical.ClassesAttrType uses this to copy the class hierarchy.
	}

	public void writeARFFType(PrintWriter wrt) throws ClusException {
		throw new ClusException("Type: "+getClass().getName()+" can't be written to a .arff file");
	}

	/**
	 * Compares to ClusAttrTypes based on index, allowing them to be sorted.
	 */
	public int compareTo(Object o) {
		ClusAttrType c = (ClusAttrType) o;

		if(c.m_Index > this.m_Index)
			return 1;
		if(c.m_Index < this.m_Index)
			return -1;
		return 0;
	}
	
	public boolean isSparse() {
		return false;
	}
}


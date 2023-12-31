/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.data.rows;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;


public class DataTuple implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClusSchema m_Schema;

    // Attributes can have several base types
    /**
     * Nominal attributes of the tuple.
     */
    protected int[] m_Ints;
    public static final int DUMMY_INT = -123;

    /**
     * Numeric attributes of the tuple.
     */
    protected double[] m_Doubles;

    /**
     * Structured attributes of the tuple.
     */
    protected Object[] m_Objects;

    /**
     * Weight of the tuple.
     */
    public double m_Weight;

    /**
     * Unknown
     */
    public int m_Index;
    
    /**
     * 0-based index of the tuple in the dataset the tuple is part of.
     */
    private int m_DatasetIndex = -1;
    
    /** Tells whether the dataset that the tuple belongs to is training set. */
    private boolean m_IsTrain = false;
    /** Tells whether the dataset that the tuple belongs to is test set. */
    private boolean m_IsTest = false;

    /**
     * Hack for efficient xval, should be replaced later.
     */
    public int[] m_Folds;


    protected DataTuple() {
    }


    /**
     * Creates a dummy DataTuple of a given schema.
     * 
     * @param schema
     *        of the returned tuple
     */
    public DataTuple(ClusSchema schema) {
        // Initialize arrays for three base types
        int nb_int = schema.getNbInts();
        if (nb_int > 0) {
            m_Ints = new int[nb_int];
            Arrays.fill(m_Ints, DUMMY_INT);  // to ensure that all attribute values are specified in the sparse case
        }
        int nb_double = schema.getNbDoubles();
        if (nb_double > 0) {
            m_Doubles = new double[nb_double];
        }
        int nb_obj = schema.getNbObjects();
        if (nb_obj > 0) {
            m_Objects = new Object[nb_obj];
        }
        // Initialize weight
        m_Weight = 1.0;
        m_Schema = schema;
    }


    public DataTuple cloneTuple() {
        DataTuple res = new DataTuple();
        cloneTuple(res);
        return res;
    }


    /**
     * @param res
     *        where the tuple should be cloned to
     */
    public void cloneTuple(DataTuple res) {
        res.m_Ints = m_Ints;
        res.m_Doubles = m_Doubles;
        res.m_Objects = m_Objects;
        res.m_Weight = m_Weight;
        res.m_Index = m_Index;
        res.m_Folds = m_Folds;
        res.m_Schema = m_Schema;
        res.m_DatasetIndex = m_DatasetIndex;
        res.m_IsTrain = m_IsTrain;
        res.m_IsTest = m_IsTest;
    }


    /**
     * Calculates euclidean distance between numeric attributes of two tuples.
     * Does not check whether the tuples have the same schema or the same number
     * of numeric attributes.
     * 
     * @param other
     *        tuple
     * @return euclidean distance
     */
    public double euclDistance(DataTuple other) {
        double result = 0;
        for (int i = 0; i < m_Doubles.length; i++) {
            double t = this.getDoubleVal(i) - other.getDoubleVal(i);
            t = t * t;
            result += t;
        }
        return Math.sqrt(result);
    }


    public DataTuple deepCloneTuple() {
        DataTuple res = new DataTuple();
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
        return res;
    }


    /**
     * @param weight
     * @return cloned tuple with the specified weight
     */
    public final DataTuple changeWeight(double weight) {
        DataTuple res = cloneTuple();
        res.m_Weight = weight;
        return res;
    }


    /**
     * 
     * @param factor
     * @return cloned tuple with the original weight multiplied by the factor
     */
    public final DataTuple multiplyWeight(double factor) {
        DataTuple res = cloneTuple();
        res.m_Weight = m_Weight * factor;
        return res;
    }


    public final int getClassification() { // should not be used
        throw new RuntimeException("Should not be used");
        // return -1;
    }

    /**
     * Returns the number of missing values among target attributes 
     * @return 
     */
    public final int getNbMissingTargets() {
        ClusAttrType[] targets = m_Schema.getTargetAttributes();
        
        int missingNb = 0;
        
        for(int i = 0; i < targets.length; i++) {
            if(targets[i].isMissing(this)) {
                missingNb++;
            }
        }
        
        return missingNb;
    }
    
    /**
     * Returns the indices of the targets with missing values. 
     * @return 
     */
    public final ArrayList<Integer> getMissingTargets() {
        ClusAttrType[] targets = m_Schema.getTargetAttributes();
        
        ArrayList<Integer> missing = new ArrayList<>();
        
        for(int i = 0; i < targets.length; i++) {
            if(targets[i].isMissing(this)) {
                missing.add(i);
            }
        }
        return missing;
    }
    
    /**
     * DataTuple is considered unlabeled if it has missing values for all 
     * of its target attributes
     * @return 
     */
    public final boolean isUnlabeled() {
       return getNbMissingTargets() == m_Schema.getTargetAttributes().length;
    }
    
    /**
     * Set all target values to missing
     */
    public final void makeUnlabeled() {
    	ClusAttrType[] targets = m_Schema.getTargetAttributes();
  
        for(int i = 0; i < targets.length; i++) {
            targets[i].setToMissing(this);
        }
    }
    
    /**
     * DataTuple is considered partially unlabeled if it has missing values 
     * for any of its target attributes
     * @return 
     */
    public final boolean isPartiallyLabeled() {
        return getNbMissingTargets() > 0;
    }
    
    /**
     * Checks if the numeric attribute at a specified location is missing.
     * 
     * @param idx
     *        index of the numeric attribute to check
     * @return is the attribute missing?
     */
    public final boolean hasNumMissing(int idx) {
        return m_Doubles[idx] == Double.POSITIVE_INFINITY;
    }

    public final int[] getInts() {
    	return m_Ints == null ? new int[0] : m_Ints;
    }
    
    public final double[] getDoubles() {
    	return m_Doubles == null ? new double[0] : m_Doubles;
    }
    
    public final Object[] getObjects() {
    	return m_Objects == null ? new Object[0] : m_Objects;
    }

    public final ClusSchema getSchema() {
        return m_Schema;
    }

    public final double getDoubleVal(int idx) {
        return m_Doubles[idx];
    }


    public final int getIntVal(int idx) {
        return m_Ints[idx];
    }


    public final Object getObjVal(int idx) {
        return m_Objects[idx];
    }


    public final void setIntVal(int value, int idx) {
        m_Ints[idx] = value;
    }


    public final void setDoubleVal(double value, int idx) {
        m_Doubles[idx] = value;
    }


    public final void setObjectVal(Object value, int idx) {
        m_Objects[idx] = value;
    }


    public final void setIndex(int idx) {
        m_Index = idx;
    }


    public final int getIndex() {
        return m_Index;
    }
    
    public final void setDatasetIndex(int idx) {
        m_DatasetIndex = idx;
    }


    public final int getDatasetIndex() {
        return m_DatasetIndex;
    }
    
    public final boolean isTraining() {
    	return m_IsTrain;
    }
    
    public final boolean isTesting() {
    	return m_IsTest;
    }
    
    public final void setTraining(boolean value) {
    	m_IsTrain = value;
    }
    
    public final void setTesting(boolean value) {
    	m_IsTest = value;
    }


    public final double getWeight() {
        return m_Weight;
    }


    public final void setWeight(double weight) {
        m_Weight = weight;
    }


    public final void setSchema(ClusSchema schema) {
        m_Schema = schema;
    }


    public void writeTuple(PrintWriter wrt) {
        int aidx = 0;
        ClusSchema schema = getSchema();
        for (int i = 0; i < schema.getNbAttributes(); i++) {
            ClusAttrType type = schema.getAttrType(i);
            if (!type.isDisabled()) {
                if (aidx != 0)
                    wrt.print(",");
                wrt.print(type.getString(this));
                aidx++;
            }
        }
        wrt.println();
    }

    /**
     * Writes tuple's descriptive attributes in comma separated format
     * @param wrt 
     */
    public void writeDescriptive(PrintWriter wrt) {
        int aidx = 0;
	ClusSchema schema = getSchema();
	for (int i = 0; i < schema.getNbAttributes(); i++) {
		ClusAttrType type = schema.getAttrType(i);
		if (!type.isDisabled() && type.isDescriptive()) {
			if (aidx != 0) wrt.print(",");
			wrt.print(type.getString(this));
			aidx++;
		}
	}
	wrt.println();
    }
    
    /**
     * Writes tuple's target attributes in comma separated format
     * @param wrt 
     */
    public void writeTarget(PrintWriter wrt) {
        int aidx = 0;
	ClusSchema schema = getSchema();
	for (int i = 0; i < schema.getNbAttributes(); i++) {
		ClusAttrType type = schema.getAttrType(i);
		if (!type.isDisabled() && type.isTarget()) {
			if (aidx != 0) wrt.print(",");
			wrt.print(type.getString(this));
			aidx++;
		}
	}
	wrt.println();
    }

    @Override
    public String toString() {
        int aidx = 0;
        StringBuffer buf = new StringBuffer();
        ClusSchema schema = getSchema();
        if (schema != null) {
            for (int i = 0; i < schema.getNbAttributes(); i++) {
                ClusAttrType type = schema.getAttrType(i);
                if (!type.isDisabled()) {
                    if (aidx != 0)
                        buf.append(",");
                    buf.append(type.getString(this));
                    aidx++;
                }
            }
        }
        else {
            for (int i = 0; i < m_Objects.length; i++) {
                if (i != 0)
                    buf.append(",");
                buf.append(m_Objects[i].toString());
            }
        }
        return buf.toString();
    }
    
    
    public boolean isSparse() {
    	return false;
    }
}

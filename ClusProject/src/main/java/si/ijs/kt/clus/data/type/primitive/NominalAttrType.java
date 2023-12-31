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

package si.ijs.kt.clus.data.type.primitive;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;

import si.ijs.kt.clus.algo.kNN.distance.valentin.NominalStatistic;
import si.ijs.kt.clus.data.cols.ColTarget;
import si.ijs.kt.clus.data.cols.attribute.ClusAttribute;
import si.ijs.kt.clus.data.cols.attribute.NominalTarget;
import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.io.ClusSerializable;


/**
 * Attribute of nominal value.
 */
public class NominalAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public final static String POSITIVE_CLASS = "1";
    public final static String NEGATIVE_CLASS = "0";
    public final static String[] BINARY_NAMES = { POSITIVE_CLASS, NEGATIVE_CLASS };
    public final static int POSITIVE_CLASS_INDEX = ArrayUtils.indexOf(BINARY_NAMES, POSITIVE_CLASS);
    private NominalStatistic m_StatNominal;

    /**
     * Number of possible values for nominal type.
     */
    private int m_NbValues;
    protected String[] m_Values;
    protected transient Hashtable<String, Integer> m_Hash;


    public NominalAttrType(String name, String type) {
        super(name);
        int len = type.length();
        StringTokenizer tokens = new StringTokenizer(type.substring(1, len - 1), ",");
        ArrayList<String> values = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            String value = tokens.nextToken().trim();
            if (!value.equals("?"))
                values.add(value);
        }
        // sort values, so that @attribute attr {a,b,c} and @attribute attr {b,c,a} result in the same attribute
        Collections.sort(values);
        if (values.size() == 2 && values.get(0).equals("0") && values.get(1).equals("1")) {
            // Make sure that binary attributes have "1" first!
            values.set(0, "1");
            values.set(1, "0");
        }
        m_NbValues = values.size();
        m_Values = new String[m_NbValues];
        for (int i = 0; i < m_NbValues; i++) {
            m_Values[i] = values.get(i);
        }
        createHash();
    }
    

    // new for knn

    public void setStatistic(NominalStatistic stat) {
        m_StatNominal = stat;
    }


    public NominalStatistic getStatistic() {
        return m_StatNominal;
    }


    public String[] getValues() {
        return m_Values;
    }


    public NominalAttrType(String name, String[] values) { // matejp: Do not call this if you want to initialize MLC attribute properly, see NominalAttrType(String name, String type)
        super(name);
        m_NbValues = values.length;
        m_Values = values;
    }


    public NominalAttrType(String name, ArrayList<String> values) {
        super(name);
        m_NbValues = values.size();
        m_Values = values.toArray(new String[values.size()]);
    }


    public NominalAttrType(String name) {
        super(name);
        m_NbValues = 2;
        m_Values = BINARY_NAMES;
    }


    public NominalAttrType(String name, int nbvalues) {
        super(name);
        m_NbValues = nbvalues;
        m_Values = new String[nbvalues];
    }


    public final void setValue(int idx, String name) {
        m_Values[idx] = name;
    }


    @Override
    public ClusAttrType cloneType() {
        NominalAttrType at = new NominalAttrType(m_Name, m_Values);
        cloneType(at);
        return at;
    }


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.Nominal;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Int;
    }


    /**
     * Number of possible values for nominal type.
     */
    public final int getNbValues() {
        return m_NbValues;
    }


    public final int getNbValuesInclMissing() {
        return m_NbValues + (m_NbMissing > 0 ? 1 : 0);
    }


    public String getValue(int idx) {
        return m_Values[idx];
    }


    public String getValueOrMissing(int idx) {
        return idx < m_Values.length ? m_Values[idx] : "?";
    }


    public Integer getValueIndex(String value) {
        return m_Hash.get(value);
    }


    @Override
    public int getMaxNbStats() {
        // Add one for missing value index
        return m_NbValues + 1;
    }
    /*
     * public boolean addToData(ColData data) {
     * if (!super.addToData(data)) {
     * data.addAttribute(new NominalAttribute(this));
     * }
     * return true;
     * }
     */


    public void createHash() {
        m_Hash = new Hashtable<String, Integer>();
        for (int i = 0; i < m_NbValues; i++) {
            m_Hash.put(m_Values[i], new Integer(i));
        }
    }


    public String getTypeString() {
        StringBuffer res = new StringBuffer();
        res.append("{");
        for (int i = 0; i < m_NbValues; i++) {
            if (i != 0)
                res.append(",");
            res.append(m_Values[i]);
        }
        res.append("}");
        return res.toString();
    }


    @Override
    public String getString(DataTuple tuple) {
        int idx = this.getNominal(tuple);
        return idx >= m_NbValues ? "?" : m_Values[idx];
    }


    @Override
    public boolean isMissing(DataTuple tuple) {
        return this.getNominal(tuple) >= m_NbValues;
    }


    @Override
    public int getNominal(DataTuple tuple) {
        return tuple.getIntVal(m_ArrayIndex);
    }


    public void setNominal(DataTuple tuple, int intvalue) {
        tuple.setIntVal(intvalue, getArrayIndex());
    }


    @Override
    public int compareValue(DataTuple t1, DataTuple t2) {
        int i1 = this.getNominal(t1);
        int i2 = this.getNominal(t2);
        return i1 == i2 ? 0 : 1;
    }


    @Override
    public ClusAttribute createTargetAttr(ColTarget target) {
        return new NominalTarget(target, this, getArrayIndex());
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }


    @Override
    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print(getTypeString());
    }


    @Override
    public void setToMissing(DataTuple tuple) {
        setNominal(tuple, m_NbValues + 1);
    }

    public class MySerializable extends ClusSerializable {

        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            String value = data.readString();
            if (value == null)
                return false;
            if (value.equals("?")) {
                incNbMissing();
                setNominal(tuple, getNbValues());
            }
            else {
                Integer i = getValueIndex(value);
                if (i != null) {
                    setNominal(tuple, i.intValue());
                }
                else {
                    throw new IOException("Illegal value '" + value + "' for attribute " + getName() + " at row " + (data.getRow() + 1));
                }
            }
            return true;
        }
    }


    @Override
    public boolean isNominal() {
        return true;
    }

}

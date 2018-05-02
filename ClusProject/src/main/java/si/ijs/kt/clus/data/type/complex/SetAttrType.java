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

package si.ijs.kt.clus.data.type.complex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.structuredTypes.Set;
import si.ijs.kt.clus.io.ClusSerializable;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public class SetAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private String typeDefinition;
    private int numberOfPossibleValues;

    public static boolean m_isEqualLength = true;
    int m_Length = -1;


    public SetAttrType(String name) {
        super(name);
    }


    public SetAttrType(String name, String typeDefinition) {
        super(name);
        typeDefinition = typeDefinition.toUpperCase();
        this.setTypeDefinition(typeDefinition);
        if (typeDefinition.contains("NOMINAL[")) {
            int start = typeDefinition.indexOf("NOMINAL[");
            int end = typeDefinition.indexOf("]", start);
            String nominalDefinition = typeDefinition.substring(start, end);
            StringTokenizer st = new StringTokenizer(nominalDefinition, ",");
            this.numberOfPossibleValues = st.countTokens();
        }

        //TODO implement
    }


    public SetAttrType(String name, String typeDefinition, int numberOfPossibleValues) {
        this(name, typeDefinition);
        this.setNumberOfPossibleValues(numberOfPossibleValues);
    }


    @Override
    public ClusAttrType cloneType() {
        SetAttrType tsat = new SetAttrType(m_Name);
        return tsat;
    }


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.Set;
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Object;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    public Set getSet(DataTuple tuple) {
        return (Set) tuple.getObjVal(m_ArrayIndex);
    }


    public void setSet(DataTuple tuple, Set value) {
        tuple.setObjectVal(value, m_ArrayIndex);
    }


    @Override
    public String getString(DataTuple tuple) {
        Set ts_data = (Set) tuple.getObjVal(getArrayIndex());
        return ts_data.toString();
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }


    public boolean isEqualLength() {
        return m_isEqualLength;
    }


    @Override
    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print("Set");
    }


    @Override
    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
    }


    @Override
    public String getTypeDefinition() {
        return typeDefinition;
    }


    public void setNumberOfPossibleValues(int numberOfPossibleValues) {
        this.numberOfPossibleValues = numberOfPossibleValues;
    }


    public int getNumberOfPossibleValues() {
        return numberOfPossibleValues;
    }

    public class MySerializable extends ClusSerializable {

        public String getString(DataTuple tuple) {
            Set ts_data = (Set) tuple.getObjVal(0);
            Object[] data = ts_data.getValues();
            String str = "[";
            for (int k = 0; k < data.length; k++) {
                str.concat(String.valueOf(data[k]));
                if (k < (data.length - 1))
                    str.concat(", ");
            }
            str.concat("]");
            return str;
        }


        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            String str = data.readSet();
            if (str == null)
                return false;
            Set value = new Set(str, getTypeDefinition());
            tuple.setObjectVal(value, getArrayIndex());
            return true;
        }
    }
}

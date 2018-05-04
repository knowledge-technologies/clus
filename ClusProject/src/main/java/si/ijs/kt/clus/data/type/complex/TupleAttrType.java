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

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.structuredTypes.Tuple;
import si.ijs.kt.clus.io.ClusSerializable;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;


public class TupleAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    int m_Length = -1;


    public TupleAttrType(String name) {
        super(name);
    }


    public TupleAttrType(String name, String typeDefinition, ClusAttrType[] innerTypes) {
        super(name);
        typeDefinition = typeDefinition.toUpperCase();
        this.setTypeDefinition(typeDefinition);
        this.setInnerTypes(innerTypes);

        //TODO implement tuple type definition
    }


    @Override
    public ClusAttrType cloneType() {
        TupleAttrType tsat = new TupleAttrType(m_Name);
        return tsat;
    }


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.Tuple;
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Object;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    public Tuple getTuple(DataTuple tuple) {
        return (Tuple) tuple.getObjVal(m_ArrayIndex);
    }


    public void setTuple(DataTuple tuple, Tuple value) {
        tuple.setObjectVal(value, m_ArrayIndex);
    }


    @Override
    public String getString(DataTuple tuple) {
        Tuple ts_data = (Tuple) tuple.getObjVal(this.m_ArrayIndex);
        return ts_data.toString();
    }


    @Override
    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print("Tuple");
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }

    public class MySerializable extends ClusSerializable {

        public String getString(DataTuple tuple) {
            Tuple ts_data = (Tuple) tuple.getObjVal(0);
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
            String str = data.readTuple();
            if (str == null)
                return false;
            Tuple value = (Tuple) ClusAttrType.createDataObject(str, TupleAttrType.this);
            //Tuple value = new Tuple(str,TupleAttrType.this.getTypeDefinition());
            tuple.setObjectVal(value, TupleAttrType.this.getArrayIndex());
            if (m_Length == -1) {
                m_Length = value.length();
            }
            return true;
        }
    }

}

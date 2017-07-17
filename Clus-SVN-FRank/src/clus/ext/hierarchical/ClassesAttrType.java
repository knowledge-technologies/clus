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

package clus.ext.hierarchical;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import clus.data.ClusSchema;
import clus.data.io.ClusReader;
import clus.data.rows.DataPreprocs;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.primitive.NominalAttrType;
import clus.data.type.primitive.StringAttrType;
import clus.io.ClusSerializable;
import clus.main.settings.Settings;
import clus.main.settings.SettingsHMLC;
import clus.util.ClusException;
import clus.util.jeans.util.array.StringTable;


public class ClassesAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public final static int THIS_TYPE = CLASSES_ATR_TYPE;
    public final static String THIS_TYPE_NAME = "Classes";

    protected transient String[] m_Labels;
    protected transient StringTable m_Table = new StringTable();
    protected ClassHierarchy m_Hier;


    public ClassesAttrType(String name) {
        super(name);
        m_Hier = new ClassHierarchy(this);
    }


    public ClassesAttrType(String name, ClassHierarchy hier) {
        super(name);
        m_Hier = hier;
    }


    public ClassesAttrType(String name, String atype) {
        super(name);
        String classes = atype.substring("HIERARCHICAL".length()).trim();
        m_Labels = classes.split("\\s*\\,\\s*");
        m_Hier = new ClassHierarchy(this);
    }


    public StringTable getTable() {
        return m_Table;
    }


    public ClassHierarchy getHier() {
        return m_Hier;
    }

    @Override
    public boolean isMissing(DataTuple tuple) {
        return this.getValue(tuple).getNbClasses() == 0;
    }
    
    public ClusAttrType cloneType() {
        ClassesAttrType at = new ClassesAttrType(m_Name, m_Hier);
        cloneType(at);
        return at;
    }


    public int getTypeIndex() {
        return THIS_TYPE;
    }


    public String getTypeName() {
        return THIS_TYPE_NAME;
    }


    public int getValueType() {
        return VALUE_TYPE_OBJECT;
    }


    public ClassesTuple getValue(DataTuple t1) {
        return (ClassesTuple) t1.getObjVal(getArrayIndex());
    }
    
    public void setValue(DataTuple t1, ClassesTuple val) {
    	t1.setObjectVal(val, getArrayIndex());
    }
    
	@Override
	public void setToMissing(DataTuple t) {
		try {
			setValue(t, new ClassesTuple(ClassesValue.EMPTY_SET_INDICATOR, null));
		} catch (ClusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

    public void updatePredictWriterSchema(ClusSchema schema) {
        String name = getName();
        schema.addAttrType(new StringAttrType(name + "-a"));
        ClassHierarchy hier = getHier();
        String[] vals = { "1", "0" };
        for (int i = 0; i < hier.getTotal(); i++) {
            ClassTerm term = hier.getTermAt(i);
            schema.addAttrType(new NominalAttrType(name + "-a-" + term.toStringHuman(hier), vals));
        }
    }


    public String getPredictionWriterString(DataTuple tuple) {
        StringBuffer buf = new StringBuffer();
        buf.append(getString(tuple));
        buf.append(",");
        buf.append(getVectorString(tuple));
        return buf.toString();
    }


    public String getString(DataTuple tuple) {
        ClassesTuple ct = (ClassesTuple) tuple.m_Objects[m_ArrayIndex];
        return ct.toStringData(m_Hier);
    }


    public String getVectorString(DataTuple tuple) {
        ClassesTuple ct = (ClassesTuple) tuple.m_Objects[m_ArrayIndex];
        boolean[] vec = ct.getVectorBooleanNodeAndAncestors(m_Hier);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < vec.length; i++) {
            if (i != 0)
                buf.append(",");
            buf.append(vec[i] ? "1" : "0");
        }
        return buf.toString();
    }


    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }


    public void getPreprocs(DataPreprocs pps, boolean single) {
        // this builds the hierarchy based on the data
        // and adds intermediate class nodes to each example
        pps.addPreproc(new ClassHierarchyPreproc(this, true));
    }


    public void initializeBeforeLoadingData() throws IOException, ClusException {
        if (isDisabled()) {
            // No need to initialize class hierarchy of disabled attributes
            return;
        }

        SettingsHMLC sett = getSettings().getHMLC();
        if (sett.hasDefinitionFile()) {
            // Load hierarchy definition from a file
            m_Hier.loadDAG(sett.getDefinitionFile());
            m_Hier.initialize();
        }
        if (m_Labels != null) {
            // Load definition from labels in type specification in .arff
            if (sett.getHierType() == SettingsHMLC.HIERTYPE_DAG) {
                m_Hier.loadDAG(m_Labels);
            }
            else {
                for (int i = 0; i < m_Labels.length; i++) {
                    if (!m_Labels[i].equals(ClassesValue.EMPTY_SET_INDICATOR)) {
                        ClassesValue val = new ClassesValue(m_Labels[i], m_Table);
                        m_Hier.addClass(val);
                    }
                }
            }
            m_Hier.initialize();
        }
    }


    public void initializeFrom(ClusAttrType other_type) {
        ClassesAttrType other = (ClassesAttrType) other_type;
        m_Hier = other.getHier();
    }


    // Some attributes initialize differently based on some user settings
    // For HMC, this is whether it uses a tree or DAG representation
    public void initSettings(Settings sett) {

        if (sett.getHMLC().getHierType() == SettingsHMLC.HIERTYPE_DAG) {
            getHier().setHierType(ClassHierarchy.DAG);
        }
    }


    public void writeARFFType(PrintWriter wrt) throws ClusException {
        ArrayList list;
        if (getSettings().getHMLC().getHierType() == SettingsHMLC.HIERTYPE_DAG) {
            list = getHier().getAllParentChildTuples();
        }
        else {
            list = getHier().getAllPaths();
        }
        wrt.print("hierarchical ");
        for (int i = 0; i < list.size(); i++) {
            if (i != 0)
                wrt.print(",");
            wrt.print((String) list.get(i));
        }
    }

    public class MySerializable extends ClusSerializable {

        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            String val = data.readString();
            if (val == null)
                return false;
            ClassesTuple ct;
            try {
                ct = new ClassesTuple(val, m_Table);
                ct.setAllIntermediate(false);
                tuple.setObjectVal(ct, getArrayIndex());
            }
            catch (ClusException e) {
                throw new IOException("Error parsing attribute " + getName() + " '" + val + "' at row: " + (data.getRow() + 1));
            }
            return true;
        }
    }


    @Override
    public boolean isClasses() {
        return true;
    }
}

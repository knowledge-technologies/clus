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

package clus.data.type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import com.google.gson.JsonObject;

import clus.algo.kNN.distance.valentin.BasicDistance;
import clus.data.ClusSchema;
import clus.data.cols.ColTarget;
import clus.data.cols.attribute.ClusAttribute;
import clus.data.rows.DataPreprocs;
import clus.data.rows.DataTuple;
import clus.data.type.primitive.NominalAttrType;
import clus.data.type.primitive.NumericAttrType;
import clus.data.type.primitive.TimeSeriesAttrType;
import clus.data.type.structured.SetAttrType;
import clus.data.type.structured.TupleAttrType;
import clus.distance.ClusDistance;
import clus.distance.TupleDistance;
import clus.distance.timeseries.TimeSeriesDist;
import clus.ext.structuredDataTypes.Tuple;
import clus.ext.timeseries.TimeSeries;
import clus.io.ClusSerializable;
import clus.main.settings.Settings;
import clus.main.settings.SettingsTree;
import clus.util.ClusException;


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

    public final static int NOMINAL_ATR_TYPE = 0;
    public final static int NUMERIC_ATR_TYPE = 1;
    public final static int CLASSES_ATR_TYPE = 2;
    public final static int INDEX_ATR_TYPE = 2; // INDEX and CLASSES have the same value because THIS_TYPE of these two classes had the hard-coded value 2
    public final static int STRING_ATR_TYPE = 3;
    public final static int INTEGER_ATR_TYPE = 4;
    public final static int TIME_SERIES_ATR_TYPE = 5;

    protected String m_Name;
    protected int m_Index, m_ArrayIndex;
    protected int m_NbMissing;
    protected ClusSchema m_Schema;
    protected int m_Status = STATUS_NORMAL;
    protected boolean m_IsDescriptive;
    protected boolean m_IsClustering;
    private BasicDistance dist;
    private String typeDefinition;
    private ClusAttrType[] innerTypes;


    public ClusAttrType(String name) {
        m_Name = name;
        m_Index = -1;
        m_ArrayIndex = -1;
    }


    public ClusAttrType(String name, String typeDefinition) {
        this(name);
        this.setTypeDefinition(typeDefinition);
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
        throw new ClusException("Attribute " + getName() + " does not support row data");
    }


    public ClusAttribute createTargetAttr(ColTarget target) throws ClusException {
        throw new ClusException("Attribute " + getName() + " can not be target: incompatible type");
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
        throw new ClusException("Type: " + getClass().getName() + " can't be written to a .arff file");
    }

    //--------------------------------New-------------------------------------


    /**
     * Returns the distance between the 2 given tuples for this attribute.
     */
    public double getBasicDistance(DataTuple t1, DataTuple t2) {
        return dist.getDistance(this, t1, t2);
    }


    /**
     * Sets the BasicDistance for this AttributeType
     */
    public void setBasicDistance(BasicDistance bdist) {
        dist = bdist;
    }


    /**
     * Compares to ClusAttrTypes based on index, allowing them to be sorted.
     */
    public int compareTo(Object o) {
        ClusAttrType c = (ClusAttrType) o;

        if (c.m_Index > this.m_Index)
            return 1;
        if (c.m_Index < this.m_Index)
            return -1;
        return 0;
    }


    public boolean isSparse() {
        return false;
    }


    public boolean isNumeric() {
        return false;
    }


    public boolean isNominal() {
        return false;
    }


    public boolean isClasses() {
        return false;
    }


    public boolean isTimeSeries() {
        return false;
    }


    public boolean isString() {
        return false;
    }


    public static Object createDataObject(String object, ClusAttrType attrType) {

        if (attrType instanceof TupleAttrType) {

            object = object.substring(1);

            object = object.substring(0, object.length() - 1);
            char[] td = object.toCharArray();
            int cnt = 0;
            StringBuilder innerObject = new StringBuilder();
            ArrayList<String> innerObjects = new ArrayList<String>();
            for (int i = 0; i < td.length; i++) {
                if (td[i] == '[') {
                    cnt++;
                    innerObject.append(td[i]);
                }
                else if (td[i] == ']') {
                    cnt--;
                    innerObject.append(td[i]);
                }
                else if (td[i] == ',') {
                    if (cnt == 0) {
                        innerObjects.add(innerObject.toString());
                        innerObject = new StringBuilder();
                    }
                    else {
                        innerObject.append(td[i]);
                    }
                }
                else if (td[i] == ' ') {

                }
                else {
                    innerObject.append(td[i]);
                }

            }
            innerObjects.add(innerObject.toString());
            Object[] innerTupleObjects = new Object[innerObjects.size()];
            ClusAttrType[] innerTypes = attrType.getInnerTypes();
            int i = 0;
            for (String iType : innerObjects) {
                innerTupleObjects[i] = createDataObject(iType, innerTypes[i]);
                i++;
            }
            return new Tuple(innerTupleObjects);

        }
        else if (attrType instanceof TimeSeriesAttrType) {
            if (object.trim().equalsIgnoreCase("?"))
                return null;
            else
                return new TimeSeries(object);
        }
        else if (attrType instanceof NumericAttrType) { return new Double(object); }
        return null;
    }


    public static ClusAttrType createClusAttrType(String typeDef, String name) {

        String typeDefinition = typeDef.toUpperCase();
        typeDefinition = typeDefinition.trim();

        if (typeDefinition.startsWith("TUPLE[")) {

            typeDefinition = typeDefinition.substring(6);

            typeDefinition = typeDefinition.substring(0, typeDefinition.length() - 1);
            char[] td = typeDefinition.toCharArray();
            int cnt = 0;
            StringBuffer innerType = new StringBuffer();
            ArrayList<String> innerTypes = new ArrayList<String>();
            for (int i = 0; i < td.length; i++) {
                if (td[i] == '[') {
                    cnt++;
                    innerType.append(td[i]);
                }
                else if (td[i] == ']') {
                    cnt--;
                    innerType.append(td[i]);
                }
                else if (td[i] == ',') {
                    if (cnt == 0) {
                        innerTypes.add(innerType.toString());
                        innerType = new StringBuffer();
                    }
                    else {
                        innerType.append(td[i]);
                    }
                }
                else if (td[i] == ' ') {

                }
                else {
                    innerType.append(td[i]);
                }
            }

            innerTypes.add(innerType.toString());
            ClusAttrType[] innerClussAttrTypes = new ClusAttrType[innerTypes.size()];
            int i = 0;
            for (String iType : innerTypes) {
                innerClussAttrTypes[i++] = createClusAttrType(iType, "inner");
            }
            return new TupleAttrType(name, typeDef, innerClussAttrTypes);

        }
        else if (typeDefinition.startsWith("TIMESERIES")) {
            return new TimeSeriesAttrType(name, "TIMESERIES");
        }
        else if (typeDefinition.equalsIgnoreCase("NUMERIC")) { return new NumericAttrType(name); }

        return null;
    }


    public static ClusDistance createDistance(ClusAttrType type, Settings settings) {
        if (type instanceof TupleAttrType) {
            TupleAttrType tupleType = (TupleAttrType) type;
            ClusAttrType[] innertypes = tupleType.getInnerTypes();

            ClusDistance[] innerDistances = new ClusDistance[innertypes.length];
            int i = 0;
            for (ClusAttrType iType : innertypes) {
                innerDistances[i++] = createDistance(iType, settings);
            }

            TupleDistance tupleDist = null;
            switch (settings.getTree().getTupleDistance()) {
                case SettingsTree.TUPLEDISTANCES_EUCLIDEAN:
                    tupleDist = new clus.distance.tuples.EuclideanDistance((TupleAttrType) type, innerDistances);
                    break;
                case SettingsTree.TUPLEDISTANCES_MINKOWSKI:
                    tupleDist = new clus.distance.tuples.MinkowskiDistance(3, innerDistances);
                    break;
            }
            return tupleDist;
        }
        else if (type instanceof TimeSeriesAttrType) {

            TimeSeriesDist tsDistance = null;
            switch (settings.getTree().getTSDistance()) {
                case SettingsTree.TIME_SERIES_DISTANCE_MEASURE_DTW:
                    tsDistance = new clus.distance.timeseries.DTWTimeSeriesDist((TimeSeriesAttrType) type);
                    break;
                case SettingsTree.TIME_SERIES_DISTANCE_MEASURE_QDM:
                    tsDistance = new clus.distance.timeseries.QDMTimeSeriesDist((TimeSeriesAttrType) type);
                    break;

                case SettingsTree.TIME_SERIES_DISTANCE_MEASURE_TSC:
                    tsDistance = new clus.distance.timeseries.TSCTimeSeriesDist((TimeSeriesAttrType) type);
                    break;
            }
            return tsDistance;
        }
        return null;
    }


    public ClusAttrType[] getInnerTypes() {
        return innerTypes;
    }


    public void setInnerTypes(ClusAttrType[] innerTypes) {
        this.innerTypes = innerTypes;
    }


    public String getTypeDefinition() {
        return typeDefinition;
    }


    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
    }


    public JsonObject getAttributeJSON() {
        JsonObject elm = new JsonObject();

        elm.addProperty("attributeName", getName());

        if (this instanceof NumericAttrType) {
            elm.addProperty("attributeType", "numeric");
        }
        else if (this instanceof NominalAttrType) {
            elm.addProperty("attributeType", "nominal");
        }
        else if (this instanceof TimeSeriesAttrType) {
            elm.addProperty("attributeType", "timeseries");
        }
        else if (this instanceof TupleAttrType) {
            elm.addProperty("attributeType", "tuple");
        }
        else if (this instanceof SetAttrType) {
            elm.addProperty("attributeType", "set");
        }
        return elm;
    }
}

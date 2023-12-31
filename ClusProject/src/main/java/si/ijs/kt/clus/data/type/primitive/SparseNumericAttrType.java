
package si.ijs.kt.clus.data.type.primitive;

import java.io.IOException;
import java.util.ArrayList;

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.io.ClusSerializable;


public class SparseNumericAttrType extends NumericAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected Integer m_IntIndex;
    protected ArrayList<SparseDataTuple> m_Examples;
    protected double m_ExampleWeight; // total weight of examples which have non zero value for this attribute


    public SparseNumericAttrType(String name) {
        super(name);
        setSparse(true);
        m_Examples = new ArrayList<SparseDataTuple>();
    }


    public SparseNumericAttrType(NumericAttrType type) {
        super(type.getName());
        setIndex(type.getIndex());
        setSparse(true);
        m_Examples = new ArrayList<SparseDataTuple>();
    }


    @Override
    public SparseNumericAttrType cloneType() {
        SparseNumericAttrType at = new SparseNumericAttrType(m_Name);
        cloneType(at);
        at.setIndex(getIndex());
        at.m_Sparse = m_Sparse;
        at.setExamples(getExamples());
        at.m_ExampleWeight = m_ExampleWeight;
        return at;
    }


    @Override
    public void setIndex(int idx) {
        m_Index = idx;
        m_IntIndex = new Integer(idx);
    }


    @Override
    public ValueType getValueType() {
        return ValueType.None;
    }


    public ArrayList<SparseDataTuple> getExamples() {
        return m_Examples;
    }


    public double getExampleWeight() {
        return m_ExampleWeight;
    }


    public void setExamples(ArrayList<SparseDataTuple> ex) {
        m_Examples = ex;
    }


    public void resetExamples() {
        m_Examples = new ArrayList<SparseDataTuple>();
        m_ExampleWeight = 0.0;
    }


    public void addExample(SparseDataTuple tuple) {
        m_Examples.add(tuple);
        m_ExampleWeight += tuple.getWeight();
    }

    @Deprecated
    public ArrayList<SparseDataTuple> pruneExampleList(RowData data) {
        ArrayList<DataTuple> dataList = data.toArrayList();
        ArrayList<SparseDataTuple> newExamples = new ArrayList<>();
        for (int i = 0; i < m_Examples.size(); i++) {
            if (dataList.contains(m_Examples.get(i))) {
                newExamples.add(m_Examples.get(i));
            }
        }
        return newExamples;
    }


    @Override
    public double getNumeric(DataTuple tuple) {
        return ((SparseDataTuple) tuple).getDoubleValueSparse(getIndex());
    }


    @Override
    public boolean isMissing(DataTuple tuple) {
        return ((SparseDataTuple) tuple).getDoubleValueSparse(m_IntIndex) == MISSING;
    }

    protected final static Double[] DOUBLES = createPredefinedDoubles();


    protected static Double[] createPredefinedDoubles() {
        Double[] values = new Double[10];
        for (int i = 0; i < values.length; i++) {
            values[i] = new Double(i);
        }
        return values;
    }


    @Override
    public void setNumeric(DataTuple tuple, double value) {
        Double d_value = null;
//        for (int i = 0; i < DOUBLES.length; i++) {
//            if (DOUBLES[i].doubleValue() == value)
//                d_value = DOUBLES[i];
//        }
//        if (d_value == null)
            d_value = new Double(value);
        ((SparseDataTuple) tuple).setDoubleValueSparse(d_value, m_IntIndex);
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }

    public class MySerializable extends ClusSerializable {

        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            if (!data.readNoSpace())
                return false;
            double value = data.getFloat();
            setNumeric(tuple, value);

            return true;
        }
    }
}

package clus.data.type;

import java.io.IOException;

import clus.data.io.ClusReader;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.rows.SparseDataTuple;
import clus.io.ClusSerializable;
import clus.main.Settings;
import clus.util.ClusException;
import java.util.ArrayList;

public class SparseNumericAttrType extends NumericAttrType {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected Integer m_IntIndex;
	protected ArrayList<SparseDataTuple> m_Examples;
	protected double m_ExampleWeight;

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

	public SparseNumericAttrType cloneType() {
		SparseNumericAttrType at = new SparseNumericAttrType(m_Name);
		cloneType(at);
		at.setIndex(getIndex());
		at.m_Sparse = m_Sparse;
		at.setExamples(getExamples());
		at.m_ExampleWeight = m_ExampleWeight;
		return at;
	}

	public void setIndex(int idx) {
		m_Index = idx;
		m_IntIndex = new Integer(idx);
	}

	public int getValueType() {
		return VALUE_TYPE_NONE;
	}
	
	public ArrayList getExamples() {
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
	
	public ArrayList pruneExampleList(RowData data) {
		ArrayList<SparseDataTuple> dataList = data.toArrayList();
		ArrayList<SparseDataTuple> newExamples = new ArrayList<SparseDataTuple>();
		for (int i=0; i<m_Examples.size(); i++) {
			if (dataList.contains(m_Examples.get(i))) {
				newExamples.add((SparseDataTuple)m_Examples.get(i));
			}
		}
		return newExamples;
	}
	
	public double getNumeric(DataTuple tuple) {
		return ((SparseDataTuple)tuple).getDoubleValueSparse(getIndex());
	}

	public boolean isMissing(DataTuple tuple) {
		return ((SparseDataTuple)tuple).getDoubleValueSparse(m_IntIndex) == MISSING;
	}

	protected final static Double[] DOUBLES = createPredefinedDoubles();
	
	protected static Double[] createPredefinedDoubles() {
		Double[] values = new Double[10];
		for (int i = 0; i < values.length; i++) {
			values[i] = new Double(i);
		}
		return values;
	}
	
	public void setNumeric(DataTuple tuple, double value) {
		Double d_value = null;
		for (int i = 0; i < DOUBLES.length; i++) {
			if (DOUBLES[i].doubleValue() == value) d_value = DOUBLES[i];
		}
		if (d_value == null)d_value = new Double(value);
		((SparseDataTuple)tuple).setDoubleValueSparse(d_value, m_IntIndex);
	}

	public ClusSerializable createRowSerializable() throws ClusException {
		return new MySerializable();
	}

	public class MySerializable extends ClusSerializable {

		public boolean read(ClusReader data, DataTuple tuple) throws IOException {
			if (!data.readNoSpace()) return false;
			double value = data.getFloat();
			setNumeric(tuple, value);
//			System.out.println(" adding " + tuple.getIndex());
//			addExampleIndex(new Integer(tuple.getIndex()));			
			return true;
		}
	}
}

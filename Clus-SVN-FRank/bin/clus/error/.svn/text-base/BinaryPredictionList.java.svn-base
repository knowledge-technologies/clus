package clus.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;

import clus.main.Settings;

import jeans.util.compound.DoubleBooleanCount;

public class BinaryPredictionList implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_NbPos, m_NbNeg;
	protected transient ArrayList m_Values = new ArrayList();
	protected transient HashMap m_ValueSet = new HashMap();

	public void addExample(boolean actual, double predicted) {
		DoubleBooleanCount value = new DoubleBooleanCount(predicted, actual);
		DoubleBooleanCount prevValue = (DoubleBooleanCount)m_ValueSet.get(value);
		if (prevValue != null) {
			prevValue.inc();
		} else {
			m_ValueSet.put(value, value);
		}
		if (actual) m_NbPos++;
		else m_NbNeg++;
	}

	public void addInvalid(boolean actual) {
		if (actual) m_NbPos++;
		else m_NbNeg++;
	}

	public void sort() {
		m_Values.clear();
		m_Values.addAll(m_ValueSet.values());
		Collections.sort(m_Values);
	}
	
	public int size() {
		return m_Values.size();
	}

	public DoubleBooleanCount get(int i) {
		return (DoubleBooleanCount)m_Values.get(i);
	}

	public void clear() {
		m_NbPos = 0;
		m_NbNeg = 0;
		m_Values.clear();
		m_ValueSet.clear();
	}
	
	public void clearData() {
		m_Values.clear();
	}

	public int getNbPos() {
		return m_NbPos;
	}

	public int getNbNeg() {
		return m_NbNeg;
	}

	public double getFrequency() {
		return (double)m_NbPos / (m_NbPos + m_NbNeg);
	}

	public boolean hasBothPosAndNegEx() {
		return m_NbPos != 0 && m_NbNeg != 0;
	}

	public void add(BinaryPredictionList other) {
		m_NbPos += other.getNbPos();
		m_NbNeg += other.getNbNeg();
		Iterator values = other.m_ValueSet.values().iterator();
		while (values.hasNext()) {
			DoubleBooleanCount otherValue = (DoubleBooleanCount)values.next();
			DoubleBooleanCount myValue = (DoubleBooleanCount)m_ValueSet.get(otherValue);
			if (myValue != null) {
				myValue.inc(otherValue);
			} else {
				DoubleBooleanCount newValue = new DoubleBooleanCount(otherValue); 
				m_ValueSet.put(newValue, newValue);
			}
		}	
	}

	public void copyActual(BinaryPredictionList other) {
		m_NbPos = other.getNbPos();
		m_NbNeg = other.getNbNeg();
	}
}

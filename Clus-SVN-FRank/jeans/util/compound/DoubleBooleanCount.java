package jeans.util.compound;

public class DoubleBooleanCount extends DoubleBoolean {

	protected int m_Count = 1;

	public DoubleBooleanCount(double val, boolean bol) {
		super(val, bol);
	}

	public DoubleBooleanCount(DoubleBooleanCount other) {
		super(other.getDouble(), other.getBoolean());
		m_Count = other.getCount();
	}

	public void inc() {
		m_Count++;
	}

	public void inc(DoubleBooleanCount other) {
		m_Count += other.getCount();
	}

	public int getCount() {
		return m_Count;
	}
}

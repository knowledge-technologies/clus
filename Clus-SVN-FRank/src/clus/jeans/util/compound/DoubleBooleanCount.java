package clus.jeans.util.compound;
/**
 * 
 * Class that stores prediction statistics that are used when building ROC- and PR-curves.
 */

public class DoubleBooleanCount extends DoubleBoolean {

	protected int m_Count = 1;
	/**
	 * Constructor for this type.
	 * @param val (Estimate of) probability that a given example is labelled by a particular label ({@code 0.0 <= val <= 1.0}) 
	 * @param bol {@code true} if the example is labelled by the label and {@code false} otherwise <p>
	 * 
	 * The ramaining field {@code count} is set to {@code 1} and represents the number of examples seen that have the same {@code val} and {@code bol}. 
	 */
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

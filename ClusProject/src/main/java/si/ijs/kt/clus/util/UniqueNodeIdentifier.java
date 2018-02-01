package si.ijs.kt.clus.util;

public class UniqueNodeIdentifier {
	private final int m_ID;
	
	public UniqueNodeIdentifier(int value) {
		m_ID = value;
	}
	
	@Override
	public int hashCode() {
		return m_ID;
	}
	
	@Override
	public boolean equals(Object other) {
		return m_ID == ((UniqueNodeIdentifier) other).m_ID;
	}
	
}

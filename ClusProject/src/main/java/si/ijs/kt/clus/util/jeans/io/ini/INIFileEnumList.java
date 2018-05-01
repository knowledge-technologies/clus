
package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import si.ijs.kt.clus.main.settings.Settings;

public class INIFileEnumList<T extends Enum<T>> extends INIFileEntry {

	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	Class<T> m_EnumType;
	List<T> m_Values = null;

	@SuppressWarnings("unchecked")
	public INIFileEnumList(String name, T defaultValue) {
		super(name);
		m_EnumType = (Class<T>) defaultValue.getClass();

		m_Values = new ArrayList<>();
		m_Values.add(defaultValue);
	}

	@SuppressWarnings("unchecked")
	public INIFileEnumList(String name, List<T> values) {
		super(name);

		if (values == null) {
			throw new RuntimeException("INIFileEnumList(String, List<T>) --> List<T> is null!");
		} else {
			m_EnumType = (Class<T>) values.getClass();
			// m_EnumType = (Class<T>) values.get(0).getClass();
			m_Values = values;
		}
	}

	@Override
	public String getStringValue() {
		return "[" + m_Values.stream().map(T::toString).collect(Collectors.joining(", ")) + "]";
	}

	@Override
	public void setValue(String value) throws IOException {
		String[] entries = value.replace("[", "").replace("]", "").replace(" ", "").split(",");

		m_Values.clear(); // remove old values

		for (String entry : entries) {

			try {
				m_Values.add(Enum.valueOf(m_EnumType, entry));
			} catch (IllegalArgumentException e) {
				System.err.println(String.format("Value %s is illegal for the option %s", entry, m_hName));
				System.err.println(
						String.format("List of allowed values: " + Arrays.toString(m_EnumType.getEnumConstants())));
				throw e;
			}
		}
	}

	@Override
	public INIFileNode cloneNode() {
		return new INIFileEnumList<T>(getName(), m_Values);
	}

	public void setValue(List<T> values) {
		m_Values = values;
	}

	public List<T> getValue() {
		return m_Values;
	}

	public int getVectorSize() {
		return m_Values.size();
	}

	public boolean IsVector() {
		return m_Values.size() > 1;
	}
}

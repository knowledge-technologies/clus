package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.lang.Enum;
import java.util.Arrays;


public class INIFileEnum<T extends Enum<T>> extends INIFileEntry {
	
	T m_ChosenOption;
	Class<T> m_EnumType;
	
	public INIFileEnum(String name, Class<T> enumClass, T defaultValue) {
		super(name);
		m_EnumType = enumClass;
		m_ChosenOption = defaultValue;
	}

	@Override
	public String getStringValue() {
		return m_ChosenOption.toString();
	}

	@Override
	public void setValue(String value) throws IOException {
		try {
			m_ChosenOption = Enum.valueOf(m_EnumType, value);
		} catch(IllegalArgumentException e) {
			System.err.println(String.format("Value %s is illigal for the option %s", value, m_hName));
			System.err.println(String.format("List of allowed values: " + Arrays.toString(m_ChosenOption.getDeclaringClass().getEnumConstants())));
			throw e;
		}
		
	}

	@Override
	public INIFileNode cloneNode() {
		return new INIFileEnum<T>(getName(), m_EnumType, m_ChosenOption);
	}
	
	public T getChosenOption() {
		return m_ChosenOption;
	}
}

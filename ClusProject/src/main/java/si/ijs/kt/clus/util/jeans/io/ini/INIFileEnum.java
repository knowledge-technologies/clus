package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.lang.Enum;

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
		return m_EnumType.toString();
	}

	@Override
	public void setValue(String value) throws IOException {
		m_ChosenOption = Enum.valueOf(m_EnumType, value);
		
	}

	@Override
	public INIFileNode cloneNode() {
		return new INIFileEnum<T>(getName(), m_EnumType, m_ChosenOption);
	}
	
	public T getChosenOption() {
		return m_ChosenOption;
	}
}

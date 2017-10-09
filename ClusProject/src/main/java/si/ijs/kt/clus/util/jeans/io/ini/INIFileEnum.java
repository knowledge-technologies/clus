package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.lang.Enum;

public class INIFileEnum<T extends Enum<T>> extends INIFileEntry {
	
	T m_choseOption;
	Class<T> m_EnumType;
	
	public INIFileEnum(String name, Class<T> enumClass) {
		super(name);
		m_EnumType = enumClass;
	}

	@Override
	public String getStringValue() {
		return m_EnumType.toString();
	}

	@Override
	public void setValue(String value) throws IOException {
		m_choseOption = Enum.valueOf(m_EnumType, value);
		
	}

	@Override
	public INIFileNode cloneNode() {
		return new INIFileEnum<T>(getName(), m_EnumType);
	}
	
	public T getChosenOption() {
		return m_choseOption;
	}
}

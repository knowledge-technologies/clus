package si.ijs.kt.clus.util.tools.manual;

import java.util.Arrays;

public class SettingsFieldsDescriptor {
	private boolean m_isSettingsFile;
	private String[] m_OptionsNames;
	private String[] m_OptionsDefaults;
	
	
	public SettingsFieldsDescriptor(boolean isSettingsFile, String[] optionsNames, String[] optionsDefaults) {
		m_isSettingsFile = isSettingsFile;
		m_OptionsNames = Arrays.copyOf(optionsNames, optionsNames.length);
		m_OptionsDefaults = Arrays.copyOf(optionsDefaults, optionsDefaults.length);
	}
	
	public boolean getIsSettingsFile() {
		return m_isSettingsFile;
	}
	
	public String[] getOptionsNames() {
		return m_OptionsNames;
	}
	
	public String[] getOptionsDefaults() {
		return m_OptionsDefaults;
	}

}

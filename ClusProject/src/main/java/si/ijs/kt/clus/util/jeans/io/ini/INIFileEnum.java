
package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.util.Arrays;

import si.ijs.kt.clus.main.settings.Settings;


public class INIFileEnum<T extends Enum<T>> extends INIFileEntry {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private T m_ChosenOption;
    private Class<T> m_EnumType;


    @SuppressWarnings("unchecked")
    public INIFileEnum(String name, T defaultValue) {
        super(name);
        m_EnumType = (Class<T>) defaultValue.getClass();
        m_ChosenOption = defaultValue;
    }


    @Override
    public String getStringValue() {
        return m_ChosenOption.toString();
    }


    @Override
    public String toString() {
        return m_ChosenOption.toString();
    }


    @Override
    public void setValue(String value) throws IOException {
        try {
            m_ChosenOption = Enum.valueOf(m_EnumType, value);
        }
        catch (IllegalArgumentException e) {
            System.err.println(String.format("Value %s is illegal for the option %s", value, m_hName));
            System.err.println(String.format("List of allowed values: " + Arrays.toString(m_EnumType.getEnumConstants())));
            throw e;
        }
    }


    @Override
    public INIFileNode cloneNode() {
        return new INIFileEnum<T>(getName(), m_ChosenOption);
    }


    public void setValue(T value) {
        m_ChosenOption = value;
    }


    public T getValue() {
        return m_ChosenOption;
    }
}

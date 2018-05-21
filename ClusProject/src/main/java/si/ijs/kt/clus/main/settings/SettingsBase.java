
package si.ijs.kt.clus.main.settings;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public abstract class SettingsBase implements Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    /***********************************************************************
     * Simple constants *
     ***********************************************************************/
    public final static String DEFAULT = "Default";
    public final static String NONE = "None";
    public final static String[] NONELIST = { "None" };
    public final static String[] EMPTY = {};
    public final static String[] INFINITY = { "Infinity" };
    public final static String INFINITY_STRING = "Infinity";
    public final static int INFINITY_VALUE = 0;
    public final static double[] FOUR_ONES = { 1.0, 1.0, 1.0, 1.0 };
    public final static Charset CHARSET = StandardCharsets.UTF_8;

    protected int m_Position = 1; // position in the INI file

    protected INIFileSection m_Section;

    public SettingsBase(int position, String name) {
        m_Position = position;
        
        m_Section = new INIFileSection(name);
    }


    /**
     * Method should handle the INIFileSection initialization.
     */
    public abstract void create();


    /**
     * Method should handle initialization of named values.
     * E.g.: Use si.ijs.kt.clus.util.jeans.io.ini.INIFileInt.setNamedValue(int, String) and similar methods.
     * This method is not used by all settings sections. It is not abstract, so that it does not have to be implemented
     * by all those
     * sections that do not need it.
     */
    public void initNamedValues() {
    }


    /**
     * This method should be overriden to check for inconsistencies in the section settings.
     * It is called by <code>si.ijs.kt.clus.main.settings.Settings.validateSettingsCompatibility()</code> method.
     * If several sections are needed to determine if inconsistencies exist, implement the logic in
     * <code>si.ijs.kt.clus.main.settings.Settings.validateSettingsCombined()</code>.
     */
    public List<String> validateSettingsInternal() {
        return null;
    }


    /**
     * Returns the position of this section in the INI file
     * 
     * @return Position
     */
    public int getPosition() {
        return m_Position;
    }
    
    /** Returns INI section */
    public INIFileSection getSection() {
        return m_Section;
    }
}

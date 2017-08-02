
package si.ijs.kt.clus.main.settings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public abstract class SettingsBase {

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


    public SettingsBase(int position) {
        m_Position = position;
    }


    /**
     * Method should handle the INIFileSection initialization.
     * 
     * @return An initialized INI section.
     */
    public abstract INIFileSection create();


    /**
     * Method should handle initialization of named values.
     * E.g.: Use si.ijs.kt.clus.util.jeans.io.ini.INIFileInt.setNamedValue(int, String) and similar methods
     */
    public abstract void initNamedValues();


    /**
     * Returns the position of this section in the INI file
     * 
     * @return Position
     */
    public int getPosition() {
        return m_Position;
    }
}

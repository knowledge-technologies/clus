
package clus.main.settings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import clus.util.jeans.io.ini.INIFileSection;


public interface ISettings {

    
    /***********************************************************************
     * Simple constants *
     ***********************************************************************/

    public final String DEFAULT = "Default";
    public final String NONE = "None";
    public final String[] NONELIST = { "None" };
    public final String[] EMPTY = {};
    public final String[] INFINITY = { "Infinity" };
    public final String INFINITY_STRING = "Infinity";
    public final int INFINITY_VALUE = 0;
    public final double[] FOUR_ONES = { 1.0, 1.0, 1.0, 1.0 };
    public final Charset CHARSET = StandardCharsets.UTF_8;


    public INIFileSection create();
}

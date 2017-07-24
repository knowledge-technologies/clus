
package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsNominal implements SettingsBase {

    /***********************************************************************
     * Section: Nominal - Should move? *
     ***********************************************************************/

    protected INIFileDouble m_MEstimate;


    public double getMEstimate() {
        return m_MEstimate.getValue();
    }


    @Override
    public INIFileSection create() {
        INIFileSection nominal = new INIFileSection("Nominal");
        nominal.addNode(m_MEstimate = new INIFileDouble("MEstimate", 1.0));
        return nominal;
    }
}

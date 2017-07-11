
package clus.main.settings;

import clus.util.jeans.io.ini.INIFileDouble;
import clus.util.jeans.io.ini.INIFileSection;


public class SettingsNominal implements ISettings {

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

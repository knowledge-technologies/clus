
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsNominal extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsNominal(int position) {
        super(position);
    }

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

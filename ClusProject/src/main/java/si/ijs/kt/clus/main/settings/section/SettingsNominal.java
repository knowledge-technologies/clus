
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;


public class SettingsNominal extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsNominal(int position) {
        super(position, "Nominal");
    }

    /***********************************************************************
     * Section: Nominal - Should move? *
     ***********************************************************************/

    protected INIFileDouble m_MEstimate;


    public double getMEstimate() {
        return m_MEstimate.getValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_MEstimate = new INIFileDouble("MEstimate", 1.0));
    }
}


package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsDistances extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    // this class needs love

    public SettingsDistances(int position) {
        super(position);
    }

    /***********************************************************************
     * Section: Distances *
     ***********************************************************************/
    // Time Series covered already ...
    INIFileSection m_SectionDistances;
    // public final static String[] TIMESERIES_DISTANCE_TYPE = {"pearson", "warping", "qualitative"};
    //
    // public final static int TIMESERIES_DISTANCE_PEARSON = 0;
    // public final static int TIMESERIES_DISTANCE_WARPING = 1;
    // public final static int TIMESERIES_DISTANCE_QUALITATIVE = 2;
    //
    //
    //


    @Override
    public INIFileSection create() {
        m_SectionDistances = new INIFileSection("Distances");
        m_SectionDistances.setEnabled(false);

        return m_SectionDistances;
    }
}

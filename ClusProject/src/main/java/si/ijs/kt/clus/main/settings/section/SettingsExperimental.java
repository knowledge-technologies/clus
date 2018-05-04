
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;


public class SettingsExperimental extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsExperimental(int position) {
        super(position, "Experimental");
    }

    /***********************************************************************
     * Cross-validaiton *
     ***********************************************************************/

    public static boolean SHOW_XVAL_FOREST;
    public static boolean XVAL_OVERLAP = true;
    public static boolean IS_XVAL = false;

    protected INIFileInt m_SetsData;
    protected INIFileBool m_ShowForest;


    public int getBaggingSets() {
        return m_SetsData.getValue();
    }


    public boolean isShowXValForest() {
        return m_ShowForest.getValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_SetsData = new INIFileInt("NumberBags", 25));
        m_Section.addNode(m_ShowForest = new INIFileBool("XValForest", false));
        m_Section.setEnabled(false);
    }
}


package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsExperimental extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsExperimental(int position) {
        super(position);
        // TODO Auto-generated constructor stub
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
    public INIFileSection create() {

        INIFileSection exper = new INIFileSection("Experimental");
        exper.addNode(m_SetsData = new INIFileInt("NumberBags", 25));
        exper.addNode(m_ShowForest = new INIFileBool("XValForest", false));
        exper.setEnabled(false);

        return exper;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }

}

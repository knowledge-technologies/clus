
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsGeneral extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    /***********************************************************************
     * Section: General *
     ***********************************************************************/

    INIFileInt m_Verbose;// TODO: migrate to Log4J

    INIFileString m_RandomSeed;
    INIFileEnum<ResourceInfoLoad> m_ResourceInfoLoaded;


    public SettingsGeneral(int position) {
        super(position);
    }


    public int getVerbose() {
        return m_Verbose.getValue();
    }


    public int enableVerbose(int talk) {
        int prev = m_Verbose.getValue();
        m_Verbose.setValue(talk);
        return prev;
    }


    public boolean hasRandomSeed() {
        return !StringUtils.unCaseCompare(m_RandomSeed.getValue(), NONE);
    }


    public int getRandomSeed() {
        return Integer.parseInt(m_RandomSeed.getValue());
    }


    public ResourceInfoLoad getResourceInfoLoadedValue() {
        return m_ResourceInfoLoaded.getValue();
    }


    public INIFileEnum<ResourceInfoLoad> getResourceInfoLoaded() {
        return m_ResourceInfoLoaded;
    }

    /***********************************************************************
     * Section: General - ResourceInfo loaded *
     ***********************************************************************/

    public enum ResourceInfoLoad {Yes, No, Test};

    @Override
    public INIFileSection create() {
        INIFileSection settings = new INIFileSection("General");
        settings.addNode(m_Verbose = new INIFileInt("Verbose", 1));
        settings.addNode(m_RandomSeed = new INIFileString("RandomSeed", "0"));
        settings.addNode(m_ResourceInfoLoaded = new INIFileEnum<>("ResourceInfoLoaded", ResourceInfoLoad.No));

        return settings;
    }
}

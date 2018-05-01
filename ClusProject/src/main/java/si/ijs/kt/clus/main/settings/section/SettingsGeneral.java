
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
    INIFileEnum<Compatibility> m_Compatibility;

    INIFileString m_RandomSeed;
    INIFileEnum<ResourceInfoLoad> m_ResourceInfoLoaded;


    public SettingsGeneral(int position) {
        super(position);
    }


    public Compatibility getCompatibility() {
        return m_Compatibility.getValue();
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
        // System.out.println(m_RandomSeed.getValue());
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
     * Section: General - Compatibility mode *
     ***********************************************************************/

    public enum Compatibility {
        CMB05(0), MLJ08(1), Latest(2);

        private int compatibilityLevel = -1;


        Compatibility(int c) {
            compatibilityLevel = c;
        }


        public int getCompatibilityLevel() {
            return compatibilityLevel;
        }
    };

    /***********************************************************************
     * Section: General - ResourceInfo loaded *
     ***********************************************************************/

    public enum ResourceInfoLoad {Yes, No, Test};
    /*private final String[] RESOURCE_INFO_LOAD = { "Yes", "No", "Test" };

    public final static int RESOURCE_INFO_LOAD_YES = 0;
    public final static int RESOURCE_INFO_LOAD_NO = 1;
    public final static int RESOURCE_INFO_LOAD_TEST = 2;
*/

    @Override
    public INIFileSection create() {
        INIFileSection settings = new INIFileSection("General");
        settings.addNode(m_Verbose = new INIFileInt("Verbose", 1));
        settings.addNode(m_Compatibility = new INIFileEnum<>("Compatibility", Compatibility.Latest));

        settings.addNode(m_RandomSeed = new INIFileString("RandomSeed", "0"));
        //settings.addNode(m_ResourceInfoLoaded = new INIFileNominal("ResourceInfoLoaded", RESOURCE_INFO_LOAD, 1));
        settings.addNode(m_ResourceInfoLoaded = new INIFileEnum<>("ResourceInfoLoaded", ResourceInfoLoad.No));

        return settings;
    }
}

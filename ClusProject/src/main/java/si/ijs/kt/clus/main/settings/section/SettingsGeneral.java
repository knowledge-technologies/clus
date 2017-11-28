
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsGeneral extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    /***********************************************************************
     * Section: General *
     ***********************************************************************/

    INIFileInt m_Verbose;// TODO: migrate to Log4J
    INIFileNominal m_Compatibility;
    INIFileString m_RandomSeed;
    INIFileNominal m_ResourceInfoLoaded;


    public SettingsGeneral(int position) {
        super(position);
    }


    public int getCompatibility() {
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


    public int getResourceInfoLoadedValue() {
        return m_ResourceInfoLoaded.getValue();
    }


    public INIFileNominal getResourceInfoLoaded() {
        return m_ResourceInfoLoaded;
    }

    /***********************************************************************
     * Section: General - Compatibility mode *
     ***********************************************************************/

    private final String[] COMPATIBILITY = { "CMB05", "MLJ08", "Latest" };

    public final static int COMPATIBILITY_CMB05 = 0;
    public final static int COMPATIBILITY_MLJ08 = 1;
    public final static int COMPATIBILITY_LATEST = 2;

    /***********************************************************************
     * Section: General - ResourceInfo loaded *
     ***********************************************************************/

    private final String[] RESOURCE_INFO_LOAD = { "Yes", "No", "Test" };

    public final static int RESOURCE_INFO_LOAD_YES = 0;
    public final static int RESOURCE_INFO_LOAD_NO = 1;
    public final static int RESOURCE_INFO_LOAD_TEST = 2;


    @Override
    public INIFileSection create() {
        INIFileSection settings = new INIFileSection("General");
        settings.addNode(m_Verbose = new INIFileInt("Verbose", 1));
        settings.addNode(m_Compatibility = new INIFileNominal("Compatibility", COMPATIBILITY, COMPATIBILITY_LATEST));
        settings.addNode(m_RandomSeed = new INIFileString("RandomSeed", "0"));
        settings.addNode(m_ResourceInfoLoaded = new INIFileNominal("ResourceInfoLoaded", RESOURCE_INFO_LOAD, 1));

        return settings;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }

}

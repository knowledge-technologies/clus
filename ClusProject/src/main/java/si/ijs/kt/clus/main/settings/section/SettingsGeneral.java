
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
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
    INIFileBool m_DoNotInduce; // when running "Clus.jar -fold <n>", we can either induce a model with fold data or not.
    INIFileString m_LoggingProperties; // for logging properties


    public SettingsGeneral(int position) {
        super(position, "General");
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


    public boolean isDoNotInduce() {
        return m_DoNotInduce.getValue();
    }


    public String getLoggingProperties() {
        return m_LoggingProperties.getStringValue();
    }

    /***********************************************************************
     * Section: General - ResourceInfo loaded *
     ***********************************************************************/

    public enum ResourceInfoLoad {
        Yes, No, Test
    };


    @Override
    public void create() {
        m_Section.addNode(m_Verbose = new INIFileInt("Verbose", 1));
        m_Section.addNode(m_RandomSeed = new INIFileString("RandomSeed", "0"));
        m_Section.addNode(m_ResourceInfoLoaded = new INIFileEnum<>("ResourceInfoLoaded", ResourceInfoLoad.No));
        m_Section.addNode(m_DoNotInduce = new INIFileBool("DoNotInduce", false)); // this is useful when using -fold <n>
                                                                                  // command-line switch
        m_Section.addNode(m_LoggingProperties = new INIFileString("LoggingProperties", "logging.properties"));
    }
}

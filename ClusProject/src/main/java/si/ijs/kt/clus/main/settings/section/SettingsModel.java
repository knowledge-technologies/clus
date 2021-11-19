
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsModel extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    /***********************************************************************
     * Section: Model *
     ***********************************************************************/

    protected INIFileDouble m_MinW;
    protected INIFileDouble m_MinKnownW;
    protected INIFileInt m_MinNbEx;
    protected INIFileString m_TuneFolds;
    protected INIFileBool m_NominalSubsetTests;
    protected INIFileBool m_LoadFromModelFile;


    public SettingsModel(int position) {
        super(position, "Model");
    }


    public double getMinimalWeight() {
        return m_MinW.getValue();
    }


    public double getMinimalKnownWeight() {
        return m_MinKnownW.getValue();
    }


    public int getMinimalNbExamples() {
        return m_MinNbEx.getValue();
    }


    public void setMinimalWeight(double val) {
        m_MinW.setValue(val);
    }


    public String getTuneFolds() {
        return m_TuneFolds.getValue();
    }


    public boolean isNominalSubsetTests() {
        return m_NominalSubsetTests.getValue();
    }
    
    public boolean loadFromFile() {
    	return m_LoadFromModelFile.getValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_MinW = new INIFileDouble("MinimalWeight", 2.0));
        m_Section.addNode(m_MinNbEx = new INIFileInt("MinimalNumberExamples", 0));
        m_Section.addNode(m_MinKnownW = new INIFileDouble("MinimalKnownWeight", 0));
        m_Section.addNode(m_TuneFolds = new INIFileString("ParamTuneNumberFolds", "3"));
        m_Section.addNode(m_NominalSubsetTests = new INIFileBool("NominalSubsetTests", true));
        m_Section.addNode(m_LoadFromModelFile = new INIFileBool("LoadFromModelFile", false));
    }
}

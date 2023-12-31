
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsSIT extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsSIT(int position) {
        super(position, "SIT");
    }

    /***********************************************************************
     * Section: Selective inductive transfer *
     ***********************************************************************/
    protected INIFileString m_MainTarget;
    protected INIFileString m_Search;
    protected INIFileString m_Learner;
    protected INIFileBool m_Recursive;
    protected INIFileString m_Error;


    public String getError() {
        return m_Error.getValue();
    }


    public String getLearnerName() {
        return m_Learner.getValue();
    }


    // @deprecated
    public boolean getRecursive() {
        return m_Recursive.getValue();
    }


    public String getMainTarget() {
        return m_MainTarget.getValue();
    }


    public String getSearchName() {
        return m_Search.getValue();
    }


    public void setSearch(String b) {
        m_Search.setValue(b);
    }


    public void setMainTarget(String str) {
        m_MainTarget.setValue(str);
    }


    public void setSectionSITEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    @Override
    public void create() {
        m_Section.addNode(m_MainTarget = new INIFileString("Main_target", DEFAULT));
        m_Section.addNode(m_Recursive = new INIFileBool("Recursive", false));
        m_Section.addNode(m_Search = new INIFileString("Search", "OneTarget"));
        m_Section.addNode(m_Learner = new INIFileString("Learner", "ClusLearner"));
        m_Section.addNode(m_Error = new INIFileString("Error", "MSE"));

        m_Section.setEnabled(false);
    }
}

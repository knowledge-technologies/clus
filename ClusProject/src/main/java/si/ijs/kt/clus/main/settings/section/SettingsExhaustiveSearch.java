
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;


public class SettingsExhaustiveSearch extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsExhaustiveSearch(int position) {
        super(position, "Exhaustive");
    }

    /***********************************************************************
     * Section: Exhaustive search *
     ***********************************************************************/
    protected INIFileBool m_Exhaustive;
    protected INIFileInt m_StartTreeCpt;
    protected INIFileInt m_StartItemCpt;


    public void setSectionExhaustiveEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public boolean isExhaustiveSearch() {
        return m_Exhaustive.getValue();
    }


    public int getStartTreeCpt() {
        return m_StartTreeCpt.getValue();
    }


    public int getStartItemCpt() {
        return m_StartItemCpt.getValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_Exhaustive = new INIFileBool("Exhaustive", true));
        m_Section.addNode(m_StartTreeCpt = new INIFileInt("StartTreeCpt", 0));
        m_Section.addNode(m_StartItemCpt = new INIFileInt("StartItemCpt", 0));
        m_Section.setEnabled(false);
    }
}

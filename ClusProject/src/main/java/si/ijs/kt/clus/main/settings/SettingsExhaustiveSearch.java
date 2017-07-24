package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;

public class SettingsExhaustiveSearch implements SettingsBase {

    /***********************************************************************
     * Section: Exhaustive search *
     ***********************************************************************/

    protected INIFileSection m_SectionExhaustive;
    protected INIFileBool m_Exhaustive;
    protected INIFileInt m_StartTreeCpt;
    protected INIFileInt m_StartItemCpt;


    public void setSectionExhaustiveEnabled(boolean enable) {
        m_SectionExhaustive.setEnabled(enable);
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
    public INIFileSection create() {
        // 
        // added by elisa 1/08/2006
        m_SectionExhaustive = new INIFileSection("Exhaustive");
        m_SectionExhaustive.addNode(m_Exhaustive = new INIFileBool("Exhaustive", true));
        m_SectionExhaustive.addNode(m_StartTreeCpt = new INIFileInt("StartTreeCpt", 0));
        m_SectionExhaustive.addNode(m_StartItemCpt = new INIFileInt("StartItemCpt", 0));
        m_SectionExhaustive.setEnabled(false);
        
        return m_SectionExhaustive;
    }

}


package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsILevelC extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsILevelC(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: Instance level constraints *
     ***********************************************************************/

    protected INIFileSection m_SectionILevelC;
    protected INIFileString m_ILevelCFile;
    protected INIFileDouble m_ILevelCAlpha;
    protected INIFileInt m_ILevelNbRandomConstr;
    protected INIFileBool m_ILevelCCOPKMeans;
    protected INIFileBool m_ILevelCMPCKMeans;


    public boolean isSectionILevelCEnabled() {
        return m_SectionILevelC.isEnabled();
    }


    public boolean hasILevelCFile() {
        return !StringUtils.unCaseCompare(m_ILevelCFile.getValue(), NONE);
    }


    public String getILevelCFile() {
        return m_ILevelCFile.getValue();
    }


    public double getILevelCAlpha() {
        return m_ILevelCAlpha.getValue();
    }


    public int getILevelCNbRandomConstraints() {
        return m_ILevelNbRandomConstr.getValue();
    }


    public boolean isILevelCCOPKMeans() {
        return m_ILevelCCOPKMeans.getValue();
    }


    public boolean isILevelCMPCKMeans() {
        return m_ILevelCMPCKMeans.getValue();
    }


    @Override
    public INIFileSection create() {

        m_SectionILevelC = new INIFileSection("ILevelC");
        m_SectionILevelC.addNode(m_ILevelCAlpha = new INIFileDouble("Alpha", 0.5));
        m_SectionILevelC.addNode(m_ILevelCFile = new INIFileString("File", NONE));
        m_SectionILevelC.addNode(m_ILevelNbRandomConstr = new INIFileInt("NbRandomConstraints", 0));
        m_SectionILevelC.addNode(m_ILevelCCOPKMeans = new INIFileBool("RunCOPKMeans", false));
        m_SectionILevelC.addNode(m_ILevelCMPCKMeans = new INIFileBool("RunMPCKMeans", false));
        m_SectionILevelC.setEnabled(false);

        return m_SectionILevelC;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }
}

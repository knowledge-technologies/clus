package clus.main.settings;

import clus.util.jeans.io.ini.INIFileBool;
import clus.util.jeans.io.ini.INIFileInt;
import clus.util.jeans.io.ini.INIFileSection;

public class SettingsExperimental implements ISettings {

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

}

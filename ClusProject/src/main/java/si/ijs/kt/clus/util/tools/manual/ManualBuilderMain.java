package si.ijs.kt.clus.util.tools.manual;

import java.io.IOException;

public class ManualBuilderMain {
    public static final String MANUAL_DIR = "docs/manual/";
    public static final String MANUAL_OPTIONS_TABLES_DIR = MANUAL_DIR + "optionTables";

    public static void main(String[] args) throws IOException {   
        ManualBuilder mb = new ManualBuilder();
        
        mb.buildSettingsTables(MANUAL_OPTIONS_TABLES_DIR);
    }
    
}

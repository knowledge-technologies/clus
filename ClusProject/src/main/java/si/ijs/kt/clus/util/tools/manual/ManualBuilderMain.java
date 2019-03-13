
package si.ijs.kt.clus.util.tools.manual;

import java.io.IOException;


public class ManualBuilderMain {

    public static final String MANUAL_DIR = "docs/manual/";
    public static final String MANUAL_OPTIONS_LISTS_DIR = MANUAL_DIR + "optionListsHatchery";
    public static final String MANUAL_OPTIONS_LISTS_DIR_EXISTING = MANUAL_DIR + "optionListsForUse";


    public static void main(String[] args) throws IOException {
        ManualBuilder mb = new ManualBuilder();

        mb.buildSettingsTables(MANUAL_OPTIONS_LISTS_DIR);

        mb.mergeExistingTables(MANUAL_OPTIONS_LISTS_DIR, MANUAL_OPTIONS_LISTS_DIR_EXISTING);
        
        
        System.out.println(String.format("Look inside %s for merged files", MANUAL_OPTIONS_LISTS_DIR));
    }

}

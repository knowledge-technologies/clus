package si.ijs.kt.clus.util.tools.manual;

import java.io.File;
import java.util.Arrays;

public class ManualBuilderMain {
    public static final String MANUAL_DIR = "docs/manual/";

    public static void main(String[] args) {
        File naZaru = new File(MANUAL_DIR);
        if(naZaru.exists() && naZaru.isDirectory()){
            System.out.println("Obstaja");
        } else {
            System.out.println("ne bostas");
        }
        
        SettingsFieldsDescriptor x = new SettingsFieldsDescriptor(false, new String[] {"a"}, new String[] {"b", "c"});
        System.out.println(Arrays.toString(x.getOptionsDefaults()));
    }
}

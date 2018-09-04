
package si.ijs.kt.clus.util.tools.manual;

import java.util.ArrayList;


public class ManualSectionEntry {

    public static ArrayList<ManualEntry> parse(String contents) {
        
        String[] splitted = contents.split(System.lineSeparator());
        String line;
        
        for (int i = 0; i < splitted.length; i++) {
            line = splitted[i];
            if (line.startsWith("")) {
                splitted[i] = ""; // remove this line
            }
            
        }
        
        contents = contents.replace("", "");
        return new ArrayList<ManualEntry>();
    }
}

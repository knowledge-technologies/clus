
package si.ijs.kt.clus.util.tools.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManualSectionEntry {

    private String m_SectionName;
    private List<ManualEntry> m_Entries;

    private String sectionPattern = "\\\\section\\{(.*?)\\}";
    private String whateverPattern = "\\\\item \\\\optionNameStyle\\{(.*?){1,}\\\\end\\{itemize\\}";
    private Pattern pSection = Pattern.compile(sectionPattern);
    private Pattern pElement = Pattern.compile(whateverPattern, Pattern.MULTILINE | Pattern.DOTALL);


    public ManualSectionEntry(String contents) {
        parse(contents);
    }


    private void parse(String contents) {
        String[] splitted = contents.split(System.lineSeparator());
        String line;

        for (int i = 0; i < splitted.length; i++) {
            line = splitted[i];

            // disregard empty rows
            if (line.startsWith("")) {
                splitted[i] = "";
            }
        }

        // find section name
        Matcher m = pSection.matcher(contents);
        if (m.find()) {
            m_SectionName = m.group(1);
        }

        // find individual section items
        m = pElement.matcher(contents);
        List<ManualEntry> entries = new ArrayList<ManualEntry>();
        while (m.find()) {
            entries.add(new ManualEntry(m.group(0)));
        }
        m_Entries = entries;
    }


    /**
     * Merge this and other into this
     */
    public int merge(ManualSectionEntry other) {
        // merge individual entries
        int count = 0;
        for (ManualEntry me : m_Entries) {
            for (ManualEntry ome : other.m_Entries) {
                // try to merge
                count += me.merge(ome);
            }
        }
        return count;
    }
}

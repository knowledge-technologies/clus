
package si.ijs.kt.clus.util.tools.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManualSectionEntry {

    private List<ManualEntry> m_Entries;
    private String m_Original;
    private String whateverPattern = "\\\\item \\\\optionNameStyle\\{(.*?){1,}\\\\end\\{itemize\\}";
    private Pattern pElement = Pattern.compile(whateverPattern, Pattern.MULTILINE | Pattern.DOTALL);
    private final String REPLACEMENT = "It was me. I let the dogs out.";


    public ManualSectionEntry(String contents) {
        m_Original = contents;
        parse(contents);
    }


    private void parse(String contents) {
        // find individual section items
        Matcher m = pElement.matcher(contents);
        List<ManualEntry> entries = new ArrayList<ManualEntry>();

        while (m.find()) {
            entries.add(new ManualEntry(m.group(0)));
        }

        m_Original = m.replaceAll(REPLACEMENT);

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
                if (me.getName().equals(ome.getName())) {
                    // try to merge
                    count += me.merge(ome);
                }
            }
        }
        return count;
    }


    @Override
    public String toString() {
        String s = m_Original;
        for (ManualEntry me : m_Entries) {
            s = s.replaceFirst(REPLACEMENT, Matcher.quoteReplacement(me.toString()));
        }

        return s;
    }
}

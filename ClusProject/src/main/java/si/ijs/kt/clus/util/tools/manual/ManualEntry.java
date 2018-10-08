
package si.ijs.kt.clus.util.tools.manual;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManualEntry {

    private static final String m_OptionNameTemplate = "\\optionNameStyle{%s}:%n";
    private static final String m_PossibleValuesTemplate = "\\optionPossibleValues{}: ???%n";
    private static final String m_DefaultValueTemplate = "\\optionDefaultValue{}: %s%n";
    private static final String m_DescriptionTemplate = "\\optionDescription{}: ???%n";

    private static final String optionPattern = "\\\\optionNameStyle\\{(.*?)\\}";
    private static final String whateverPattern = "(?<=\\\\item \\\\%s\\{\\}\\:)(.*?){1,}(?=(\\\\item)|(\\\\end\\{itemize))";
    private static final Pattern pOption = Pattern.compile(optionPattern);
    private static final Pattern pPossible = Pattern.compile(String.format(whateverPattern, "optionPossibleValues"), Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern pDefault = Pattern.compile(String.format(whateverPattern, "optionDefaultValue"), Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern pDescription = Pattern.compile(String.format(whateverPattern, "optionDescri?ption"), Pattern.MULTILINE | Pattern.DOTALL);

    private String m_Name;
    private String m_Possible;
    private String m_Default;
    private String m_Description;


    public String getName() {
        return m_Name;
    }


    public String getPossible() {
        return m_Possible;
    }


    public String getDefault() {
        return m_Default;
    }


    public String getDescription() {
        return m_Description;
    }


    public ManualEntry(String contents) {
        parse(contents);
    }


    private void parse(String contents) {
        // find option name
        Matcher m = pOption.matcher(contents);
        if (m.find()) {
            m_Name = m.group(1);
        }

        // possible
        m = pPossible.matcher(contents);
        if (m.find()) {
            m_Possible = m.group(0);
        }

        // default
        m = pDefault.matcher(contents);
        if (m.find()) {
            m_Default = m.group(0);
        }

        // description
        m = pDescription.matcher(contents);
        if (m.find()) {
            m_Description = m.group(0);
        }
    }


    public static String getLatex(String name, String defaultValue) {
        return String.format(getEmpty(), name, defaultValue);
    }


    private static String getEmpty() {
        return
        /* */ "    \\item " + m_OptionNameTemplate +
        /* */ "           \\begin{itemize}%n" +
        /* */ "                \\item " + m_PossibleValuesTemplate +
        /* */ "                \\item " + m_DefaultValueTemplate +
        /* */ "                \\item " + m_DescriptionTemplate +
        /* */ "           \\end{itemize}";
    }


    public int merge(ManualEntry other) {
        if (!getName().equals(other.getName())) { return 0; }

        int cnt = 0;

        if (!getPossible().equals(other.getPossible())) {
            this.m_Possible = other.getPossible();
            cnt++;
        }

        if (!getDefault().equals(other.getDefault())) {
            this.m_Default = other.getDefault();
            cnt++;
        }

        if (!getDescription().equals(other.getDescription())) {
            this.m_Description = other.getDescription();
            cnt++;
        }

        return cnt;
    }
}

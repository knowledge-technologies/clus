
package si.ijs.kt.clus.util.tools.manual;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManualEntry {

    private static final String m_OptionNameTemplate = "\\optionNameStyle{%s}:";
    private static final String m_PossibleValuesTemplate = "\\optionPossibleValues{}: %s";
    private static final String m_DefaultValueTemplate = "\\optionDefaultValue{}: %s";
    private static final String m_DescriptionTemplate = "\\optionDescription{}: %s";

    private static final String optionPattern = "\\\\optionNameStyle\\{(.*?)\\}";
    private static final String miscPattern = "(?<=\\\\item \\\\%s\\{\\}\\:)(.*?){1,}(?=(\\\\item)|(\\\\end\\{itemize))";
    private static final Pattern pOption = Pattern.compile(optionPattern);
    private static final Pattern pPossible = Pattern.compile(String.format(miscPattern, "optionPossibleValues"), Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern pDefault = Pattern.compile(String.format(miscPattern, "optionDefaultValue"), Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern pDescription = Pattern.compile(String.format(miscPattern, "optionDescri?ption"), Pattern.MULTILINE | Pattern.DOTALL);

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
            m_Name = m.group(1).trim();
        }

        // possible
        m = pPossible.matcher(contents);
        if (m.find()) {
            m_Possible = m.group(0).trim();
        }

        // default
        m = pDefault.matcher(contents);
        if (m.find()) {
            m_Default = m.group(0).trim();
        }

        // description
        m = pDescription.matcher(contents);
        if (m.find()) {
            m_Description = m.group(0).trim();
        }
    }


    public static String getLatexЕmpty(String name, String defaultValue) {
        String none = "???";
        String s = /* */ "    \\item " + String.format(m_OptionNameTemplate, name) + System.lineSeparator() +
        /* */ "           \\begin{itemize}" + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_PossibleValuesTemplate, none) + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_DefaultValueTemplate, defaultValue) + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_DescriptionTemplate, none) + System.lineSeparator() +
        /* */ "           \\end{itemize}";

        return s;
    }


    @Override
    public String toString() {
        String s = /* */ "    \\item " + String.format(m_OptionNameTemplate, getName()) + System.lineSeparator() +
        /* */ "           \\begin{itemize}" + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_PossibleValuesTemplate, getPossible()) + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_DefaultValueTemplate, getDefault()) + System.lineSeparator() +
        /* */ "                \\item " + String.format(m_DescriptionTemplate, getDescription()) + System.lineSeparator() +
        /* */ "           \\end{itemize}";

        return s;
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

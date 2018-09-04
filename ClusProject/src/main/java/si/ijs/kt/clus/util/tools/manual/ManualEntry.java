
package si.ijs.kt.clus.util.tools.manual;

public class ManualEntry {

    private static final String m_Name = "\\optionNameStyle{%s}:%n";
    private static final String m_PossibleValues = "\\optionPossibleValues{}: ???%n";
    private static final String m_DefaultValue = "\\optionDefaultValue{}: %s%n";
    private static final String m_Description = "\\optionDescription{}: ???%n";


    
    public static ManualEntry parse(String contents) {
        
        
        return new ManualEntry();
    }
    
    
    
    
    public static String getLatex(String name, String defaultValue) {
        return String.format(getEmpty(), name, defaultValue);
    }


    private static String getEmpty() {
        return
        /* */ "    \\item " + m_Name +
        /* */ "           \\begin{itemize}%n" +
        /* */ "                \\item " + m_PossibleValues +
        /* */ "                \\item " + m_DefaultValue +
        /* */ "                \\item " + m_Description +
        /* */ "           \\end{itemize}";
    }
}

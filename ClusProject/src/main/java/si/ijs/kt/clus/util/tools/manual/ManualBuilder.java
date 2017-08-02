package si.ijs.kt.clus.util.tools.manual;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeMap;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEntry;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNode;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;

public class ManualBuilder {

    
    public void buildSettingsTables(String settingsTablesFolder) {
    	HashMap<String, TreeMap<Integer, String[]>> default_values = listAllSettings();
        for(String section : default_values.keySet()) {
            writeLatexTable(section, settingsTablesFolder, default_values.get(section));
        }
        
    }
    
    
    private HashMap<String, TreeMap<Integer, String[]>> listAllSettings() {
    	HashMap<String, TreeMap<Integer, String[]>> default_values = new HashMap<String, TreeMap<Integer, String[]>>(); // {section: {option: value, ...}, ...}
        Settings defaultSett = Settings.getDefaultSettings();
        for (Enumeration<INIFileNode> sectionEnum = defaultSett.getSectionsIterator(); sectionEnum.hasMoreElements();) {
            INIFileSection section = (INIFileSection) sectionEnum.nextElement();
            String secName = section.getName();
            if(!default_values.containsKey(secName)) {
                default_values.put(secName, new TreeMap<Integer, String[]>());
                System.out.println("[" + secName + "]");
            }
            Integer count = 0;
            for (Enumeration<INIFileNode> e = section.getNodes(); e.hasMoreElements();) {
                INIFileEntry entry = (INIFileEntry) e.nextElement();
                default_values.get(secName).put(count, new String[] {entry.getName(), entry.getStringValue()}); // [Rules]: DispersionWeights possibly ugly
                count = new Integer(count.intValue() + 1);
                //System.out.println("    " + entry.getName() + ": " +  entry.getStringValue());
            }          
        }
        return default_values;
    }
    
    
    private void writeLatexTable(String sectionName, String settingsTablesFolder, TreeMap<Integer, String[]> sectionDefaults) {
    	// create folders in the path if necessary
    	File folder = new File(settingsTablesFolder);
    	try {
    		folder.mkdirs(); // the procedure "if not exists: folder.mkdirs()" is considered dangerous
    	} catch(SecurityException e) {
    		if(!folder.exists()) {
    			throw e; // something went wrong with the creation
    		}
    	}
        try{
        	// write to file
        	String fileName = String.format("%s/options-%s.tex", settingsTablesFolder, sectionName);
        	
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            String[] preamble = new String[] {"\\documentclass{article}", "\\usepackage[utf8]{inputenc}", "\\usepackage{longtable}"};
            for(String line : preamble) {
                writer.println(line);
            }
            writer.println("\\begin{document}");
            writer.println();
            
            writer.println("\\begin{longtable}[c]{| l | l | l | p{0.25\\textwidth} |}");
            writer.println(String.format("    \\hline%n" +  
                                         "    \\multicolumn{4}{| c |}{Section %s} \\\\%n" + 
                                         "    \\hline%n" + 
                                         "    option & possible values & default & description \\\\%n" + 
                                         "    \\hline%n" + 
                                         "    \\endfirsthead%n", sectionName));
            writer.println(String.format("    \\hline%n" +  
                                         "    \\multicolumn{4}{| c |}{Section %s continued} \\\\%n" + 
                                         "    \\hline%n" + 
                                         "    option & description & default \\\\%n" + 
                                         "    \\hline%n" + 
                                         "    \\endhead%n", sectionName));
            writer.println(String.format("    \\hline%n    \\endfoot%n"));
            writer.println(String.format("    \\hline%n    \\endlastfoot%n"));
            
            for(Integer key : sectionDefaults.keySet()) {
            	String[] option_default = sectionDefaults.get(key);
            	writer.println(String.format("    %s &  & \\texttt{%s} &  \\\\", option_default[0], option_default[1]));
            }           
            writer.println();
            writer.println("\\end{longtable}");
            
            writer.println();
            writer.println("\\end{document}");
            
            writer.close();            
        } catch (IOException e) {
           
        }
    } 
}

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
            writeLatexTable(section, settingsTablesFolder, default_values.get(section), false);
        }
        System.out.println("Done.");
        
    }
    
    
    private HashMap<String, TreeMap<Integer, String[]>> listAllSettings() {
    	HashMap<String, TreeMap<Integer, String[]>> default_values = new HashMap<String, TreeMap<Integer, String[]>>(); // {section: {option: value, ...}, ...}
        Settings defaultSett = Settings.getDefaultSettings();
        for (Enumeration<INIFileNode> sectionEnum = defaultSett.getSectionsIterator(); sectionEnum.hasMoreElements();) {
            INIFileSection section = (INIFileSection) sectionEnum.nextElement();
            String secName = section.getName();
            if(!default_values.containsKey(secName)) {
                default_values.put(secName, new TreeMap<Integer, String[]>());
            }
            Integer count = 0;
            for (Enumeration<INIFileNode> e = section.getNodes(); e.hasMoreElements();) {
                INIFileEntry entry = (INIFileEntry) e.nextElement();
                default_values.get(secName).put(count, new String[] {entry.getName(), entry.getStringValue()});
                count = new Integer(count.intValue() + 1);
            }          
        }
        return default_values;
    }
    
    
    private void writeLatexTable(String sectionName, String settingsTablesFolder, TreeMap<Integer, String[]> sectionDefaults, boolean selfstandingTex) {
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
            
            if(selfstandingTex) {
            	String[] preamble = new String[] {"\\documentclass{article}", "\\usepackage[utf8]{inputenc}"};
            	for(String line : preamble) {
                    writer.println(line);
                }
                writer.println("\\begin{document}");
                writer.println();
            }
            
            writer.println("\\begin{itemize}");
            
            
            for(Integer key : sectionDefaults.keySet()) {
            	String[] option_default = sectionDefaults.get(key); // option name and its default value
            	String name = option_default[0];
            	String[] defaults = nicifyDefaultValue(option_default[1]);
            	for(int i = 0; i < defaults.length; i++) {
            		defaults[i] = String.format("\\optionDefaultValueStyle{%s}", defaults[i]);
            	}
            	String default_value = String.join(", ", defaults);
            	writer.println(String.format("    \\item \\optionNameStyle{%s}:%n"
            			                   + "           \\begin{itemize}%n"
            			                   + "                \\item \\optionPossibleValues{}: ???%n"
            			                   + "                \\item \\optionDefaultValue{}: %s%n"
            			                   + "                \\item \\optionDescrption{}: ???%n"
            			                   + "           \\end{itemize}", name, default_value));
            }
            
            writer.println("\\end{itemize}");
            
            if(selfstandingTex) {
            	writer.println();
            	writer.println("\\end{document}");
            }
                        
            writer.close();            
        } catch (IOException e) {
           
        }
    }
    
    
    private static String[] nicifyDefaultValue(String default_value) {
    	String result = default_value;
    	result = result.trim();
    	// EOL ---> comma
    	result = result.replaceAll("\r\n", ","); // Windows
    	result = result.replaceAll("\n", ",");   // Unix
    	result = result.replaceAll("\r", ",");   // Mac
    	result = result.replaceAll(",+", ",");
    	// tabs ---> space
    	result = result.replaceAll("\t", " ");
    	result = result.replaceAll(" +", " ");
    	// special Latex chars
    	String[] results;
    	if (result.startsWith("{")) {
    		result = result.replaceAll("\\{", "\\\\{");
    		result = result.replaceAll("\\}", "\\\\}");
    		results = new String[] {result}; // keep lists intact
    	} else {
    		results = result.split(",");
    	}
    	for(int i = 0; i < results.length; i++) {
    		results[i] = results[i].trim();
    	}
    	return results;
    }
}

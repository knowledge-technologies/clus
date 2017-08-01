package si.ijs.kt.clus.util.tools.manual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ManualBuilder {

	
	public void buildSettingsTables(String clusSettingsPackage, String settingsTablesFolder) {
		ArrayList<File> properSettingsFiles = new ArrayList<File>();
		File clusSettingsPackageAsDir = new File(clusSettingsPackage);
		if(!clusSettingsPackageAsDir.exists() && clusSettingsPackageAsDir.isDirectory()) {
			throw new RuntimeException(String.format("%s is not an existing directory."));
		}
		for(File candidate : clusSettingsPackageAsDir.listFiles()) {
			
		}
	}
	
	
	/**
	 * Checks whether the file exists and contains a method public INIFileSection create().
	 * @param settingsFileCandidate
	 * @return
	 * @throws IOException 
	 */
	private boolean isProperSettingsFile(File settingsFileCandidate) throws IOException {
		if(!settingsFileCandidate.exists()) {
			return false;
		} else {
			boolean isProper = false;
			ArrayList<String> settingsNames = new ArrayList<String>();
			ArrayList<String> settingsDeafults;
			
			BufferedReader br = new BufferedReader(new FileReader(settingsFileCandidate));
			try {
			    StringBuilder sb = new StringBuilder();
			    String line = br.readLine();

			    while (line != null) {
			        sb.append(line);
			        sb.append(System.lineSeparator());
			        line = br.readLine();
			    }
			    String everything = sb.toString();
			} finally {
			    br.close();
			}
			
			
			return false;
		}
		
	}
}

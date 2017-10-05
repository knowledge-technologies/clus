package si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.plexus.util.cli.EnhancedStringTokenizer;

public class SaveLoadNeighbours {
	/**
	 * The file, storing the nearest neighbours data:<br>
	 * START_TARGET1;index<br>
	 * START_TUPLE1;index<br>
	 * NB_TARGET_VALUES1;numberOfTargetValues
	 * NN(2;0.21;0.21)&...&NN(32;0.121;0.51)<br>
	 * ...<br>
	 * NN(4;0.31;0.11)&...&NN(112;0.1;0.1)<br>
	 * END_TUPLE1<br>
	 * START_TUPLE2;index<br>
	 * ...<br>
	 * END_TUPLE2<br>
	 * ...<br>
	 * END_TUPLEn<br>
	 * END_TARGET1<br>
	 * START_TARGET2;index<br>
	 * ...<br>
	 * where NN-lines are produced by the NearestNeighbour.toFileString() method. Each line corresponds to one target value.
	 */
	private String m_File;
	
	private static final String START_TARGET = "START_TARGET";
	private static final String END_TARGET = "START_TARGET";
	private static final String START_TUPLE = "START_TUPLE";
	private static final String END_TUPLE = "START_TUPLE";
	private static final String NB_TARGET_VALUES = "NB_TARGET_VALUES";


	public SaveLoadNeighbours(String m_NearestNeigbhoursFile) {
		m_File = m_NearestNeigbhoursFile;
	}
	
	
	public void saveToFile(HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		for(Integer targetInd : nearestNeighbours.keySet()) {
			lines.add(String.format("%s;%d", START_TARGET, targetInd));
			HashMap<Integer, NearestNeighbour[][]> neighboursTarget = nearestNeighbours.get(targetInd);
			for(Integer tupleInd : neighboursTarget.keySet()) {
				lines.add(START_TUPLE);
				NearestNeighbour[][] nnss = neighboursTarget.get(tupleInd);
				lines.add(String.format("%s;%d", NB_TARGET_VALUES, nnss.length));
				for(NearestNeighbour[] nns : nnss) {
					String[] nnsString = new String[nns.length];
					for(int i = 0; i < nns.length; i++) {
						nnsString[i] = nns[i].toFileString();
					}
					lines.add(String.join("&", nnsString));
				}
				lines.add(END_TUPLE);
			}
			lines.add(END_TARGET);
		}
		Files.write(Paths.get(m_File), lines, Charset.forName("UTF-8"));
	}
	
	public HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> loadFromFile() throws IOException {
		HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		
		BufferedReader br = new BufferedReader(new FileReader(m_File));

	    String line = br.readLine();
	    Integer targetInd;
	    Integer tupleInd;
	    NearestNeighbour[][] nnss;
	    int targetValueInd;
	    while (line != null) {
	    	if(line.startsWith(START_TARGET)) {
	    		
	    	} else if(line.startsWith(START_TUPLE)) {
	    		
	    	} else if(line.startsWith(NB_TARGET_VALUES)) {
	    		int nbTarVal = Integer.parseInt(line.substring(line.indexOf(";")));
	    		nnss = new NearestNeighbour[nbTarVal][];
	    		targetValueInd = 0;
	    	} else if(line.startsWith(END_TUPLE)) {
	    		
	    	} else if(line.startsWith(START_TARGET)) {
	    		
	    	}
	    	
	        line = br.readLine();
	    }

	    br.close();
		
		return nearestNeighbours;
		
	}

}

package si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SaveLoadNeighbours {
	/**
	 * The file, storing the nearest neighbours data:<br>
	 * START_TARGET1;index<br>
	 * START_TUPLE1;index<br>
	 * NB_TARGET_VALUES1;numberOfTargetValues<br>
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
	private String m_OutputFile;
	
	/** The file names that store the NN-s, which are given in the same way as in the {@link #m_OutputFile}.
	 * They are joined into one structure when loading. */
	private String[] m_InputFiles;
	
	private static final String START_TARGET = "START_TARGET";
	private static final String END_TARGET = "END_TARGET";
	private static final String START_TUPLE = "START_TUPLE";
	private static final String END_TUPLE = "END_TUPLE";
	private static final String NB_TARGET_VALUES = "NB_TARGET_VALUES";

	private static final String NN_SEPARATOR = "&";

	public SaveLoadNeighbours(String[] m_NearestNeighbourInputFiles, String m_NearestNeighbourOutputFile) {
		m_InputFiles = m_NearestNeighbourInputFiles;
		m_OutputFile = m_NearestNeighbourOutputFile;
	}
	
	
	public void saveNeighboursToFile(HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		for(Integer targetInd : nearestNeighbours.keySet()) {
			lines.add(String.format("%s;%d", START_TARGET, targetInd));
			HashMap<Integer, NearestNeighbour[][]> neighboursTarget = nearestNeighbours.get(targetInd);
			for(Integer tupleInd : neighboursTarget.keySet()) {
				lines.add(String.format("%s;%d", START_TUPLE, tupleInd));
				NearestNeighbour[][] nnss = neighboursTarget.get(tupleInd);
				lines.add(String.format("%s;%d", NB_TARGET_VALUES, nnss.length));
				for(NearestNeighbour[] nns : nnss) {
					String[] nnsString = new String[nns.length];
					for(int i = 0; i < nns.length; i++) {
						nnsString[i] = nns[i].toFileString();
					}
					lines.add(String.join(NN_SEPARATOR, nnsString));
				}
				lines.add(END_TUPLE);
			}
			lines.add(END_TARGET);
		}
		Files.write(Paths.get(m_OutputFile), lines, Charset.forName("UTF-8"));
		System.out.println("Nearest neighbours written to: " + m_OutputFile);
	}
	
	public HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> loadNeighboursFromFile(String nearestNeighboursFile) throws IOException {
		HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		Integer targetInd = -123;
	    Integer tupleInd = -123;
	    NearestNeighbour[][] nnss = new NearestNeighbour[0][0];
	    int targetValueInd = -123;
		
		BufferedReader br = new BufferedReader(new FileReader(nearestNeighboursFile));
	    String line = br.readLine();	    
	    while (line != null) {
	    	if(line.startsWith(START_TARGET)) {
	    		// start processing new target
	    		targetInd = intAfterSemicolon(line);
	    		nearestNeighbours.put(targetInd, new HashMap<Integer, NearestNeighbour[][]>());
	    	} else if(line.startsWith(START_TUPLE)) {
	    		// start processing new tuple
	    		tupleInd = intAfterSemicolon(line);
	    	} else if(line.startsWith(NB_TARGET_VALUES)) {
	    		// initialize NearestNeighbours[][] of the appropriate length, set target value index to 0
	    		int nbTarVal = intAfterSemicolon(line);
	    		nnss = new NearestNeighbour[nbTarVal][];
	    		targetValueInd = 0;
	    	} else if(line.startsWith("NN")) {
	    		// parse nearest neighbours for given target value, increase it 
	    		String[] nnsString = line.trim().split(NN_SEPARATOR);
	    		NearestNeighbour[] nns = new NearestNeighbour[nnsString.length];
	    		for(int i = 0; i < nns.length; i++) {
	    			nns[i] = new NearestNeighbour(nnsString[i]);
	    		}
	    		nnss[targetValueInd] = nns;
	    		targetValueInd++;	    		
	    	} else if(line.startsWith(END_TUPLE)) {
	    		// save the results for given tuple
	    		nearestNeighbours.get(targetInd).put(tupleInd, nnss);
	    	} else if(line.startsWith(END_TARGET)) {
	    		// nothing to do here
	    	}
	        line = br.readLine();
	    }
	    br.close();		
		return nearestNeighbours;
	}
	
	
	public HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> loadNeighboursFromFiles() throws IOException{
		HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		for(String inFile : m_InputFiles) {
			HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> partialNNs = loadNeighboursFromFile(inFile);
			for(Integer targetInd : partialNNs.keySet()) {
				if(!nearestNeighbours.containsKey(targetInd)) {
					nearestNeighbours.put(targetInd, new HashMap<Integer, NearestNeighbour[][]>());
				}
				HashMap<Integer, NearestNeighbour[][]> neighboursTargetPartial = partialNNs.get(targetInd);
				HashMap<Integer, NearestNeighbour[][]> neighboursTargetAll = nearestNeighbours.get(targetInd);
				for(Integer tupleInd : neighboursTargetPartial.keySet()) {
					if(neighboursTargetAll.containsKey(tupleInd)) {
						String warning = "Warning: Neighbours for tuple with index %d and target with index %d are computed in at least two input files.\n"
								       + "If the neighbours are not the same, an exception will be thrown.";
						System.err.println(String.format(warning, tupleInd.intValue(), targetInd.intValue()));
						if(!Arrays.deepEquals(neighboursTargetAll.get(tupleInd), neighboursTargetPartial.get(tupleInd))) {
							throw new RuntimeException("Different neighbours!\n" + Arrays.deepToString(neighboursTargetAll.get(tupleInd)) + "\n" + Arrays.deepToString(neighboursTargetPartial.get(tupleInd)));
						}
					} else {
						neighboursTargetAll.put(tupleInd, neighboursTargetPartial.get(tupleInd));
					}
				}
			}
		}		
		return nearestNeighbours;
	}
	
	/**
	 * Parses string of form {@code <description>;<integer>} to int value of the {@code <integer>.}
	 * @param myString
	 * @return
	 */
	private static int intAfterSemicolon(String myString) {
		return Integer.parseInt(myString.substring(myString.indexOf(";") + 1));	
	}

}

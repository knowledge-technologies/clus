package si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SaveLoadNeighbours {
	/**
	 * The file, storing the nearest neighbours data:<br>
	 * START_TARGET;index1<br>
	 * START_TUPLE;index1<br>
	 * NB_TARGET_VALUES;numberOfTargetValues1<br>
	 * NN(2;0.21;0.21)&...&NN(32;0.121;0.51)<br>
	 * ...<br>
	 * NN(4;0.31;0.11)&...&NN(112;0.1;0.1)<br>
	 * END_TUPLE<br>
	 * START_TUPLE;index2<br>
	 * ...<br>
	 * END_TUPLE2<br>
	 * ...<br>
	 * END_TUPLE<br>
	 * END_TARGET<br>
	 * START_TARGET;index2<br>
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
	public static final Integer DUMMY_TARGET = -1;

	public SaveLoadNeighbours(String[] m_NearestNeighbourInputFiles, String m_NearestNeighbourOutputFile) {
		m_InputFiles = m_NearestNeighbourInputFiles;
		m_OutputFile = m_NearestNeighbourOutputFile;
	}
	
	
	public void saveNeighboursToFile(HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours) throws IOException {
		PrintWriter writer = new PrintWriter(m_OutputFile, "UTF-8");
		ArrayList<String> lines = new ArrayList<String>();
		for(Integer targetInd : nearestNeighbours.keySet()) {
			System.out.println("Saving target index " + targetInd.toString());
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
			if(lines.size() > 1000) {
				writer.println(String.join("\n", lines));
				lines = new ArrayList<String>();
			}
		}
		if (lines.size() > 0) {
			writer.println(String.join("\n", lines));
			lines = new ArrayList<String>();
		}
		writer.close();
		System.out.println("Nearest neighbours written to: " + m_OutputFile);
	}
	
	public HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> loadNeighboursFromFile(String nearestNeighboursFile) throws IOException {
		System.out.println("Loading from " + nearestNeighboursFile);
		HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		int dummy = -123;
		Integer targetInd = dummy;
	    Integer tupleInd = dummy;
	    NearestNeighbour[][] nnss = new NearestNeighbour[0][0];
	    int targetValueInd = dummy;
	    int nbTargetValues = dummy;
		
		BufferedReader br = new BufferedReader(new FileReader(nearestNeighboursFile));
	    String line = br.readLine();	    
	    while (line != null) {
	    	if(line.startsWith(START_TARGET)) {
	    		System.out.println(line);
	    		// start processing new target
	    		targetInd = intAfterSemicolon(line);
	    		nearestNeighbours.put(targetInd, new HashMap<Integer, NearestNeighbour[][]>());
	    	} else if(line.startsWith(START_TUPLE)) {
	    		// start processing new tuple
	    		tupleInd = intAfterSemicolon(line);
	    	} else if(line.startsWith(NB_TARGET_VALUES)) {
	    		// initialize NearestNeighbours[][] of the appropriate length, set target value index to 0
	    		nbTargetValues = intAfterSemicolon(line);
	    		nnss = new NearestNeighbour[nbTargetValues][];
	    		targetValueInd = 0;
	    	} else if(0 <= targetValueInd) {  // naive condition line.startsWith("NN") does not work when no neighbours could be found for given tuple
	    		// parse nearest neighbours for given target value, increase it
	    		String[] nnsString;
	    		if (line.startsWith("NN")) {
	    			nnsString = line.trim().split(NN_SEPARATOR);
	    		} else {
	    			nnsString = new String[0];
	    			System.err.println(String.format("No neighbours for %dth tuple, %dth target and its %dth value (all three indices 0-based)", tupleInd, targetInd, targetValueInd));
	    		}	    		
	    		NearestNeighbour[] nns = new NearestNeighbour[nnsString.length];
	    		for(int i = 0; i < nns.length; i++) {
	    			nns[i] = new NearestNeighbour(nnsString[i]);
	    		}
	    		nnss[targetValueInd] = nns;
	    		targetValueInd++;		
	    		if(targetValueInd == nbTargetValues) {
	    			targetValueInd = dummy;
	    		}
	    	} else if(line.startsWith(END_TUPLE)) {
	    		// save the results for given tuple
	    		nearestNeighbours.get(targetInd).put(tupleInd, nnss);
	    		if(targetValueInd != dummy) {
	    			throw new RuntimeException("Something wrong with parsing file " + nearestNeighboursFile);
	    		}
	    		nbTargetValues = dummy;
	    	} else if(line.startsWith(END_TARGET)) {
	    		// nothing to do here
	    	}
	        line = br.readLine();
	    }
	    br.close();
	    System.out.println("Loaded");
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

	 */
	private static int intAfterSemicolon(String myString) {
		return Integer.parseInt(myString.substring(myString.indexOf(";") + 1));	
	}
	
	
	/**
	 * Used outside Relief, when we only need the nearest neighbours of each instance and targets do not have any influence.
	 * Thus, we check whether the structure<p> {targetIndex: {tupleIndex: [neighbours for the first target value, neighbours for target second value, ...], ...}, ...}
	 * <p> can be flattened
	 * to {tupleIndex: neigbours for the first target value, ...}.<p>
	 * Makes sure that only one target index is present and that it equals {{@link #DUMMY_TARGET}. Also, makes sure that each tuple has only the neighbours for one target value.<br>
	 * If any of these conditions are broken, an exception is thrown.
	 * 
	 * @param nnss Nearest neighbours in the Relief form.

	 */
	public static void assureIsFlatNearestNeighbours(HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> nnss){
		if(nnss.size() != 1) {
			throw new RuntimeException("Nearest neighbours cannot be safely flattened (more than one target value)!");
		}
		if (!nnss.containsKey(DUMMY_TARGET)) {
			throw new RuntimeException(String.format("Nearest neighbours cannot be safely flattened (missing expected target: %s)!", DUMMY_TARGET.toString()));
		}
		HashMap<Integer, NearestNeighbour[][]> temp = nnss.get(DUMMY_TARGET);
		for(Integer tupleIndex : temp.keySet()) {
			NearestNeighbour[][] nns = temp.get(tupleIndex);
			if(nns.length != 1) {
				throw new RuntimeException(String.format("Nearest neighbours cannot be safely flattened (tuple with index %s has computed neigbours for more than one target value)!", tupleIndex.toString()));
			}
		}		
	}
	

}

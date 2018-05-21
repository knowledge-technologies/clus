package si.ijs.kt.clus.ext.featureRanking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import si.ijs.kt.clus.util.tuple.Quadruple;

public class Fimp {
	private ArrayList<String> m_FimpHeader;
	private Quadruple<String, String, String[], String[]> m_TableHeaderColumnBlocks;
	private ArrayList<Quadruple<Integer, String, int[], double[]>> m_TableContentsColumnBlocks;

	private static String CONTENTS_SEPARATOR = "------";


	public Fimp(String fimp_file) throws IOException {
		m_FimpHeader = new ArrayList<String>();

		m_TableContentsColumnBlocks = new ArrayList<Quadruple<Integer, String, int[], double[]>>();

		BufferedReader br = new BufferedReader(new FileReader(fimp_file));
		// ranking description
		String previous = null;
		String line = br.readLine();
		while (!line.startsWith(CONTENTS_SEPARATOR)) {
			if (previous != null) {
				m_FimpHeader.add(previous);
			}
			previous = line;
			line = br.readLine();
		}
		// table header
		line = previous;
		String[] decomposedHeader = line.split(ClusFeatureRanking.FIMP_SEPARATOR);
		String attributeDatasetIndex = decomposedHeader[0];
		String attributeName = decomposedHeader[1];
		String[] rankNames = decomposeArrayStr(decomposedHeader[2]);
		String[] rankingNames = decomposeArrayStr(decomposedHeader[3]);
		m_TableHeaderColumnBlocks = new Quadruple<String, String, String[], String[]>(attributeDatasetIndex, attributeName, rankNames, rankingNames);
		// table contents
		line = br.readLine();
		while (line != null) {
			String[] indNameRanksRelevances = line.split(ClusFeatureRanking.FIMP_SEPARATOR);
			int ind = Integer.parseInt(indNameRanksRelevances[0]);
			String name = indNameRanksRelevances[1];
			int[] ranks = getArrayOfInts(indNameRanksRelevances[2]);
			double[] relevances = getArrayOfDoubles(indNameRanksRelevances[3]);
			m_TableContentsColumnBlocks.add(new Quadruple<Integer, String, int[], double[]>(ind, name, ranks, relevances));
			line = br.readLine();
		}
		br.close();
	}
	
	public Fimp(ArrayList<String> fimpHeader, Quadruple<String, String, String[], String[]> tableHeaderColumnBlocks, ArrayList<Quadruple<Integer, String, int[], double[]>> tableContentsColumnBlocks) {
		m_FimpHeader = fimpHeader;
		m_TableHeaderColumnBlocks = tableHeaderColumnBlocks;
		m_TableContentsColumnBlocks = tableContentsColumnBlocks;
	}
	
	public ArrayList<String> getFimpHeader(){
		return m_FimpHeader;
	}
	
	public Quadruple<String, String, String[], String[]> getTableHeaderColumnBlocks (){
		return m_TableHeaderColumnBlocks;
	}
	
	public ArrayList<Quadruple<Integer, String, int[], double[]>> getTableContentsColumnBlocks(){
		return m_TableContentsColumnBlocks;
	}
	

	/**
	 * Converts, e.g., "[I, love, Clus]" to ["I", "love", "Clus"]. Strings in the
	 * resulting array must not contain commas.
	 * 
	 * @param strArray

	 */
	public static String[] decomposeArrayStr(String strArray) {
		return strArray.substring(1, strArray.length() - 1).split(",");
	}

	/**
	 * Converts, e.g., "[1, 2, 3]" to [1, 2, 3].
	 * 
	 * @param strArray

	 */
	public static int[] getArrayOfInts(String strArray) {
		String[] decomposed = decomposeArrayStr(strArray);
		int[] parsed = new int[decomposed.length];
		for (int i = 0; i < parsed.length; i++) {
			parsed[i] = Integer.parseInt(decomposed[i].trim());
		}
		return parsed;
	}

	/**
	 * Converts, e.g., "[1.1, 2.1, 3.2]" to [1.1, 2.1, 3.2].
	 * 
	 * @param strArray

	 */
	public static double[] getArrayOfDoubles(String strArray) {
		String[] decomposed = decomposeArrayStr(strArray);
		double[] parsed = new double[decomposed.length];
		for (int i = 0; i < parsed.length; i++) {
			parsed[i] = Double.parseDouble(decomposed[i].trim());
		}
		return parsed;
	}
}

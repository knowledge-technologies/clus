package test.clus.data.rows;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import clus.Clus;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.NumericAttrType;
import clus.main.Settings;
import clus.util.ClusException;
import clus.jeans.util.cmdline.CMDLineArgs;
import test.BaseTestCase;

public class RowDataTest extends BaseTestCase {
	
	@Test
	public void smartSortTest() throws IOException, ClusException{

		// tests
		String[] settingsFiles = new String[]{
				String.format("%s/smartSort/easy.s", m_DataFolder),
				String.format("%s/smartSort/missing.s", m_DataFolder),
				String.format("%s/smartSort/sparse.s", m_DataFolder),
				String.format("%s/smartSort/sparseMissing.s", m_DataFolder),
				String.format("%s/smartSort/soil_qualityTrain.s", m_DataFolder)
											  };
		String[] firstArgs = new String[settingsFiles.length];
		Arrays.fill(firstArgs, "-silent");
		

		// solutions
		Integer[][][] solutions = new Integer[][][]{
			{
				{6, 5, 4, 3, 2, 1, 0},
				{0, 1, 2, 3, 4, 5, 6},				
				{6, 5, 1, 4, 3, 0, 2}
			},
			{
				{2, 5, 6, 4, 3, 1, 0},
				{0, 6, 1, 2, 3, 4, 5},
				{6, 5, 1, 4, 3, 0, 2},
				{0, 1, 2, 3, 4, 5, 6}
			},
			{
				{29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0},
				{9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29},
				{12, 1, 16, 28, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29}
			},
			{
				{3, 20, 28, 14, 29, 27, 24, 23, 22, 21, 25, 19, 18, 17, 15, 13, 12, 11, 10, 9, 7, 6, 5, 4, 2, 1, 0, 8, 16, 26},
				{9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29},
				{18, 12, 1, 16, 28, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29}
			},
			loadSmartSortSolution(String.format("%s/smartSort/soil_qualityTrainSolution.txt", m_DataFolder))
		};
		for(int i = 0; i < settingsFiles.length; i++){
			RowData data = loadData(settingsFiles[i]);			
			NumericAttrType[] attrs = data.m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);			
			for(int repeat = 0; repeat < 4; repeat++){
				for(int attr = 0; attr < attrs.length; attr++){
					NumericAttrType at = (NumericAttrType) data.m_Schema.getAttrType(attr);
					Integer[] sortedByAt = data.smartSort(at);
					assertArrayEquals(
							String.format("Sorted arrays should be equal: test => (%d, %d, %d)", i, repeat, at.getIndex()),
							solutions[i][at.getIndex()], 
							sortedByAt);					
				}
			}
		}
		
	}
	
	/**
	 * Loads the data set that is specified in the {@code settingsFile} under the section [Data], field 'File'.
	 * @param settingsFile name of the settings file
	 * @return
	 * @throws IOException
	 * @throws ClusException
	 */
	private RowData loadData(String settingsFile) throws IOException, ClusException{	
		Clus clus = new Clus();
		Settings sett = clus.getSettings();
		CMDLineArgs cargs = new CMDLineArgs(clus);
		cargs.process(new String[]{"-silent", settingsFile});
		sett.setAppName(cargs.getMainArg(0));
		
		clus.initSettings(cargs);
		clus.initialize(cargs);			
		return clus.getData();
	}
	/**
	 * Loads the solutions for {@code smartSortTest} tests. The {@code i}-th line of the file {@code solFile}, {@code 0 <= i}, belongs to the attribute that
	 * has index {@code i} in some given dataset. The line should be of form<p>
	 * {} (if the attribute is either not numeric or not descriptive) or<p>
	 * {3, 0, 4, 1, 2} (if the attribute is numeric and descriptive).<p>
	 * The elements of the list correspond to the indices ({@code >= 0}) of the instances in the dataset, which are sorted in decreasing order with respect to the attribute.
	 * <p>
	 * The lines with indices that are greater than the index of the last descriptive numeric attribute can be omitted.
	 * 
	 * 
	 * @param solFile
	 * @return
	 * @throws IOException
	 */
	private Integer[][] loadSmartSortSolution(String solFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(solFile));
		ArrayList<Integer[]> sols = new ArrayList<Integer[]>();
		try {
		    String line = br.readLine();
		    while (line != null) {
		        Integer[] sol = parseSmartSortSolutionLine(line);
		        sols.add(sol);
		        line = br.readLine();
		    }
		} catch (Exception e) {
		    br.close();
		    e.printStackTrace();
		}
		Integer[][] ans = new Integer[sols.size()][];
		for(int i = 0; i < ans.length; i++){
			ans[i] = sols.get(i);
		}
		return ans;
	}
	/**
	 * Converts a line in the solution file for {@code smartSortTest} to the {@code Integer[]}.
	 * @param line
	 * @return
	 */
	private Integer[] parseSmartSortSolutionLine(String line) {
		if(line.length() == 2) { // {}
			return new Integer[0];
		}
		String[] list = line.substring(1, line.length() - 1).split(","); // {1, 2, 323, 33, ... , 32}
		Integer[] ans = new Integer[list.length];
		for(int i = 0; i < list.length; i++){
			ans[i] = Integer.parseInt(list[i].trim());
		}
		return ans;
	}

}

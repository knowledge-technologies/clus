package si.ijs.kt.clus.ext.featureRanking.relief;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import si.ijs.kt.clus.BaseTestCase;
import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.ext.featureRanking.Fimp;
import si.ijs.kt.clus.util.tuple.Pair;
import si.ijs.kt.clus.util.tuple.Quadruple;

public class ClusReliefFeatureRankingTest extends BaseTestCase {
	static String m_Subfolder = m_DataFolder + "/ensembleAndRankingData";
	static HashSet<String> m_OldFiles;

	
	@BeforeClass
	public static void filesBeforeTests() {
		System.out.println("Computing current files ...");
		m_OldFiles = currentFiles(); 
	}
	
	@AfterClass
	public static void cleanNewFiles() {
		System.out.println("Clean up ...");
		HashSet<String> newFiles = currentFiles();		
		for(String file: newFiles) {
//			System.out.println(file);
			if(!m_OldFiles.contains(file)) {
//				System.out.println("will be deleted");
				(new File(file)).delete();
			} else {
//				System.out.println("wont be");
			}
		}
		System.out.println("Done.");
	}
	
	@Test
	public void computeReliefImportance() throws IOException {
		System.out.println("Testing ...");
		String format = m_Subfolder + "/test%s.%s";
		String[] tasks = new String[] {"Mixed", "MLCHammingLoss", "MTR", "TreeHMLC"};
		ArrayList<Pair<String, String>> settingsFimps = new ArrayList<Pair<String, String>>();
		for(String task: tasks) {
			settingsFimps.add(new Pair<String, String>(String.format(format, task, "s"), String.format(format, task, "fimpTruth")));
		}
		
		
		String[] args = new String[] {"-relief", ""};
		for(Pair<String, String> pair : settingsFimps) {
			String sFile = pair.getFirst();
			String fimpFile = pair.getSecond();
			args[1] = sFile;
			Clus.main(args);
			Fimp fimpComputed = new Fimp(sFile.replace(".s", ".fimp"));
			Fimp fimpTruth = new Fimp(fimpFile);
			
			// compare fimp headers
			ArrayList<String> fimpH1 = fimpComputed.getFimpHeader();
			ArrayList<String> fimpH2 = fimpTruth.getFimpHeader();
			assertEquals(String.format("Sizes of two headers do not match for .s file %s", sFile), fimpH1.size(), fimpH2.size());
			for(int i = 0; i < fimpH1.size(); i++) {
				String line1 = fimpH1.get(i);
				String line2 = fimpH2.get(i);
				assertEquals(String.format("Lines\n%s\nand\n%s\nare not equal.", line1, line2), line1, line2);
			}
			// compare fimp table headers
			Quadruple<String, String, String[], String[]> tableHeaderColumnBlocks1 = fimpComputed.getTableHeaderColumnBlocks();
			Quadruple<String, String, String[], String[]> tableHeaderColumnBlocks2 = fimpTruth.getTableHeaderColumnBlocks();
			String tmp1, tmp2;
			String[] tmps1, tmps2;
			tmp1 = tableHeaderColumnBlocks1.getFirst();
			tmp2 = tableHeaderColumnBlocks2.getFirst();
			assertEquals(String.format("First blocks do not match:\n%s\n%s", tmp1, tmp2), tmp1, tmp2);
			tmp1 = tableHeaderColumnBlocks1.getSecond();
			tmp2 = tableHeaderColumnBlocks2.getSecond();
			assertEquals(String.format("Second blocks do not match:\n%s\n%s", tmp1, tmp2), tmp1, tmp2);
			tmps1 = tableHeaderColumnBlocks1.getThird();
			tmps2 = tableHeaderColumnBlocks2.getThird();
			assertArrayEquals(String.format("Third blocks do not match:\n%s\n%s", Arrays.toString(tmps1), Arrays.toString(tmps2)), tmps1, tmps2);
			tmps1 = tableHeaderColumnBlocks1.getFourth();
			tmps2 = tableHeaderColumnBlocks2.getFourth();
			assertArrayEquals(String.format("Fourth blocks do not match:\n%s\n%s", Arrays.toString(tmps1), Arrays.toString(tmps2)), tmps1, tmps2);
			// compare fimp table contents
			ArrayList<Quadruple<Integer, String, int[], double[]>> contents1 = fimpComputed.getTableContentsColumnBlocks();
			ArrayList<Quadruple<Integer, String, int[], double[]>> contents2 = fimpTruth.getTableContentsColumnBlocks();
			assertEquals(String.format("Sizes of fimps' tables do not match for .s file %s", sFile), contents1.size(), contents2.size());
			double precision = 1E-12;
			for(int i = 0; i < contents1.size(); i++) {
				int ind1 = contents1.get(i).getFirst();
				String attr1 = contents1.get(i).getSecond();
				int[] ranks1 = contents1.get(i).getThird();
				double[] relevs1 = contents1.get(i).getFourth();
				
				int ind2 = contents2.get(i).getFirst();
				String attr2 = contents2.get(i).getSecond();
				int[] ranks2 = contents2.get(i).getThird();
				double[] relevs2 = contents2.get(i).getFourth();
				
				assertEquals(String.format("Error for %s:\n1st components of quadruple do not match (line index: %d)", sFile, i), ind1, ind2);
				assertEquals(String.format("Error for %s:\n:2nd components of quadruple do not match (line index: %d)", sFile, i), attr1, attr2);
				assertArrayEquals(String.format("Error for %s:\n3rd components of quadruple do not match (line index: %d)", sFile, i), ranks1, ranks2);
				assertArrayEquals(String.format("Error for %s:\n4th components of quadruple do not match (line index: %d)", sFile, i), relevs1, relevs2, precision);
			}
		}
		
		
	}
	
	private static HashSet<String> currentFiles() {
		File[] files = (new File(m_Subfolder)).listFiles();
		HashSet<String> paths =  new HashSet<String>();
		for (int i = 0; i < files.length; i++) {
			paths.add(files[i].getPath());
		}
		return paths;
	}


}

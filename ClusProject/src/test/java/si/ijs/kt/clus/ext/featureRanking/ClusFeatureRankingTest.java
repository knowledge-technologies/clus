package si.ijs.kt.clus.ext.featureRanking;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import si.ijs.kt.clus.BaseTestCase;
import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.util.tuple.Pair;
import si.ijs.kt.clus.util.tuple.Quadruple;
import si.ijs.kt.clus.util.tuple.Triple;

public class ClusFeatureRankingTest extends BaseTestCase {

	private String m_Subfolder = m_DataFolder + "/ensembleAndRankingData";
	private HashSet<String> m_OldFiles;

	
	public String getSubfolder() {
		return m_Subfolder;
	}
	
	public HashSet<String> getOldFiles(){
		return m_OldFiles;
	}
		
	public void setOldFiles(HashSet<String> o){
		m_OldFiles = o;
	}
	
	
	public void filesBeforeTests() {
		System.out.println("Computing current files ...");
		m_OldFiles = currentFiles();
	}

	public void cleanNewFiles() {
		System.out.println("Clean up ...");
		HashSet<String> newFiles = currentFiles();
		for (String file : newFiles) {
			if (!m_OldFiles.contains(file)) {
				(new File(file)).delete();
			}
		}
		System.out.println("Done.");
	}

	public void computeImportance(ArrayList<Triple<String, String, String>> settFimpTrueFimpNew, String[] args) throws IOException {
		for (Triple<String, String, String> triple : settFimpTrueFimpNew) {
			String sFile = triple.getFirst();
			String fimpFileTrue = triple.getSecond();
			String fimpFileNew = triple.getThird();
			
			args[2] = sFile;
			Clus.main(args);
			Fimp fimpComputed = new Fimp(fimpFileNew);
			Fimp fimpTruth = new Fimp(fimpFileTrue);

			// compare fimp headers
			ArrayList<String> fimpH1 = fimpComputed.getFimpHeader();
			ArrayList<String> fimpH2 = fimpTruth.getFimpHeader();
			assertEquals(fimpH1.size(), fimpH2.size(),
			        String.format("Sizes of two headers do not match for .s file %s", sFile));
			for (int i = 0; i < fimpH1.size(); i++) {
				String line1 = fimpH1.get(i);
				String line2 = fimpH2.get(i);
				assertEquals(line1, line2, String.format("Lines\n%s\nand\n%s\nare not equal.", line1, line2));
			}
			// compare fimp table headers
			Quadruple<String, String, String[], String[]> tableHeaderColumnBlocks1 = fimpComputed
			        .getTableHeaderColumnBlocks();
			Quadruple<String, String, String[], String[]> tableHeaderColumnBlocks2 = fimpTruth
			        .getTableHeaderColumnBlocks();
			String tmp1, tmp2;
			String[] tmps1, tmps2;
			tmp1 = tableHeaderColumnBlocks1.getFirst();
			tmp2 = tableHeaderColumnBlocks2.getFirst();
			assertEquals(tmp1, tmp2, String.format("First blocks do not match:\n%s\n%s", tmp1, tmp2));
			tmp1 = tableHeaderColumnBlocks1.getSecond();
			tmp2 = tableHeaderColumnBlocks2.getSecond();
			assertEquals(tmp1, tmp2, String.format("Second blocks do not match:\n%s\n%s", tmp1, tmp2));
			tmps1 = tableHeaderColumnBlocks1.getThird();
			tmps2 = tableHeaderColumnBlocks2.getThird();
			assertArrayEquals(tmps1, tmps2, String.format("Third blocks do not match:\n%s\n%s", Arrays.toString(tmps1),
			        Arrays.toString(tmps2)));
			tmps1 = tableHeaderColumnBlocks1.getFourth();
			tmps2 = tableHeaderColumnBlocks2.getFourth();
			assertArrayEquals(tmps1, tmps2, String.format("Fourth blocks do not match:\n%s\n%s", Arrays.toString(tmps1),
			        Arrays.toString(tmps2)));
			// compare fimp table contents
			ArrayList<Quadruple<Integer, String, int[], double[]>> contents1 = fimpComputed
			        .getTableContentsColumnBlocks();
			ArrayList<Quadruple<Integer, String, int[], double[]>> contents2 = fimpTruth.getTableContentsColumnBlocks();
			assertEquals(contents1.size(), contents2.size(),
			        String.format("Sizes of fimps' tables do not match for .s file %s", sFile));
			double precision = 1E-12;
			for (int i = 0; i < contents1.size(); i++) {
				int ind1 = contents1.get(i).getFirst();
				String attr1 = contents1.get(i).getSecond();
				int[] ranks1 = contents1.get(i).getThird();
				double[] relevs1 = contents1.get(i).getFourth();

				int ind2 = contents2.get(i).getFirst();
				String attr2 = contents2.get(i).getSecond();
				int[] ranks2 = contents2.get(i).getThird();
				double[] relevs2 = contents2.get(i).getFourth();

				assertEquals(ind1, ind2, String
				        .format("Error for %s:\n1st components of quadruple do not match (line index: %d)", sFile, i));
				assertEquals(attr1, attr2, String
				        .format("Error for %s:\n:2nd components of quadruple do not match (line index: %d)", sFile, i));
				assertArrayEquals(ranks1, ranks2, String
				        .format("Error for %s:\n3rd components of quadruple do not match (line index: %d)", sFile, i));
				assertArrayEquals(relevs1, relevs2, precision, String
				        .format("Error for %s:\n4th components of quadruple do not match (line index: %d)", sFile, i));
			}
		}
	}

	public HashSet<String> currentFiles() {
		File[] files = (new File(m_Subfolder)).listFiles();
		HashSet<String> paths = new HashSet<String>();
		for (int i = 0; i < files.length; i++) {
			paths.add(files[i].getPath());
		}
		return paths;
	}

}

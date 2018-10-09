
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;
import java.util.ArrayList;

import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRankingTest;
import si.ijs.kt.clus.util.tuple.Pair;
import si.ijs.kt.clus.util.tuple.Triple;

public class ClusReliefFeatureRankingTest {

	public static Pair<ArrayList<Triple<String, String, String>>, String[]> generateNames() throws IOException {
		String format = ClusFeatureRankingTest.getSubfolder() + "/test%s.%s";
		String[] tasks = new String[] { "Mixed", "MLCHammingLoss", "MTR", "TreeHMLC", "DagHMLC-RealWorld", "TreeHMLC-RealWorld", "MLC-RealWorld", "MTR-RealWorld", "STC-RealWorld" };
		ArrayList<Triple<String, String, String>> settingsFimps = new ArrayList<>();
		for (String task : tasks) {
			String sFile = String.format(format, task, "s");
			String fimpTruth = String.format(format, task, "fimpTruth");
			String fimpNew = sFile.replace(".s", ".fimp");
			settingsFimps.add(new Triple<String, String, String>(sFile, fimpTruth, fimpNew));
		}
		String[] args = new String[] { "-silent", "-relief", "" };
		return new Pair<ArrayList<Triple<String, String, String>>, String[]>(settingsFimps, args);
	}
	
	
}

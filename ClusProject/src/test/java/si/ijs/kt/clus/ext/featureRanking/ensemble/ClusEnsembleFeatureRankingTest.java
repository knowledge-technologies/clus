package si.ijs.kt.clus.ext.featureRanking.ensemble;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRankingTest;
import si.ijs.kt.clus.util.tuple.Triple;

public class ClusEnsembleFeatureRankingTest {

	private static ClusFeatureRankingTest m_Parent = new ClusFeatureRankingTest();

	@BeforeAll
	public static void filesBeforeTests() {
		m_Parent.setOldFiles(m_Parent.currentFiles());
	}

	@AfterAll
	public static void cleanNewFiles() {
		m_Parent.cleanNewFiles();
	}

	@Test
	public void computeEnsembleImportance() throws IOException {
		String formatSetting = m_Parent.getSubfolder() + "/test%s%s.s";
		String formatFimpTruth = m_Parent.getSubfolder() + "/test%s%sTrees%s.fimpTruth";
		String formatFimpNew = m_Parent.getSubfolder() + "/test%s%sTrees%s%s.fimp";
		String[] tasks = new String[] { "DagHMLC-RealWorld", "TreeHMLC-RealWorld", "MLC-RealWorld", "MTR-RealWorld",
		        "STC-RealWorld" };
//		String[] rankings = new String[]  { "Genie3", "RForest", "Symbolic" };
		String[][] rankingSubsets = new String[][] { { "Genie3" }, { "RForest" }, { "Symbolic" },
		        { "RForest", "Genie3" }, { "Symbolic", "Genie3" }, { "Symbolic", "RForest" },
		        { "Symbolic", "RForest", "Genie3" } };
		ArrayList<Triple<String, String, String>> settingsFimps = new ArrayList<>();
		for (String task : tasks) {
			for (String[] subset : rankingSubsets) {
				String sFile = String.format(formatSetting, task, String.join("", subset));
				for (String size : new String[] {"2", "4"}) {
					for (String ranking : subset) {
						String fimpTruth = String.format(formatFimpTruth, task, ranking, size);
						String fimpNew = String.format(formatFimpNew, task, String.join("", subset), size, ranking);
						settingsFimps.add(new Triple<String, String, String>(sFile, fimpTruth, fimpNew));
					}
				}
			}
		}
		String[] args = new String[] { "-silent", "-forest", "" };
		m_Parent.computeImportance(settingsFimps, args);
	}
}

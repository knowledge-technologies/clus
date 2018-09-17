
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRankingTest;
import si.ijs.kt.clus.util.tuple.Triple;

public class ClusReliefFeatureRankingTest {
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
	public void computeReliefImportance() throws IOException {
		String format = m_Parent.getSubfolder() + "/test%s.%s";
		String[] tasks = new String[] { "Mixed", "MLCHammingLoss", "MTR", "TreeHMLC", "DagHMLC-RealWorld",
		        "TreeHMLC-RealWorld", "MLC-RealWorld", "MTR-RealWorld", "STC-RealWorld" };
		ArrayList<Triple<String, String, String>> settingsFimps = new ArrayList<>();
		for (String task : tasks) {
			String sFile = String.format(format, task, "s");
			String fimpTruth = String.format(format, task, "fimpTruth");
			String fimpNew = sFile.replace(".s", ".fimp");
			settingsFimps.add(new Triple<String, String, String>(sFile, fimpTruth, fimpNew));
		}
		String[] args = new String[] { "-silent", "-relief", "" };
		m_Parent.computeImportance(settingsFimps, args);
	}
	
	
}

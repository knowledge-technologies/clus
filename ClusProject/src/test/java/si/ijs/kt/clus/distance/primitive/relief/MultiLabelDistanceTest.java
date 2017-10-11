package si.ijs.kt.clus.distance.primitive.relief;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import si.ijs.kt.clus.BaseTestCase;
import si.ijs.kt.clus.TestHelper;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.featureRanking.Fimp;
import si.ijs.kt.clus.main.settings.section.SettingsRelief;
import si.ijs.kt.clus.util.tuple.Triple;

public class MultiLabelDistanceTest extends BaseTestCase {

	@Test
	public void test() throws IOException {
		double precision = 1E-12;
		String sFile = m_DataFolder + "/mlcDistanceData/mlcDistance.s";
		RowData data = TestHelper.getRowData(sFile);
		ClusAttrType[] attrs = data.m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
		double[] labelProbabilities = loadLabelProbabilities(m_DataFolder + "/mlcDistanceData/mlcDistance.prob");
		
		MultiLabelDistance[] distances = new MultiLabelDistance[] {
					new MultiLabelDistance(SettingsRelief.MultilabelDistance.HammingLoss, attrs, labelProbabilities),
					new MultiLabelDistance(SettingsRelief.MultilabelDistance.MLAccuracy, attrs, labelProbabilities),
					new MultiLabelDistance(SettingsRelief.MultilabelDistance.MLFOne, attrs, labelProbabilities),
					new MultiLabelDistance(SettingsRelief.MultilabelDistance.SubsetAccuracy, attrs, labelProbabilities)
				};
		ArrayList<Triple<Integer, Integer, double[]>> testCases = new ArrayList<Triple<Integer, Integer, double[]>>(); // index first, index second, list of distances
		testCases.add(new Triple<Integer, Integer, double[]>(0, 0, new double[] {0.0, 0.0, 0.0, 0.0}));
		testCases.add(new Triple<Integer, Integer, double[]>(0, 1, new double[] {2.0 / 3.0, 1.0, 1.0, 1.0}));
		testCases.add(new Triple<Integer, Integer, double[]>(1, 2, new double[] {1.0 / 3.0, 0.5, 1.0 / 3.0, 1.0}));
		testCases.add(new Triple<Integer, Integer, double[]>(1, 3, new double[] {1.0 / 9.0, 1.0 / 6.0, 1.0 / 11.0, 1.0 / 3.0}));
		testCases.add(new Triple<Integer, Integer, double[]>(2, 3, new double[] {4.0 / 9.0, 2.0 / 3.0, 1.0 / 2.0, 1.0}));
		
		for(Triple<Integer, Integer, double[]> testCase : testCases) {
			int ind1 = testCase.getFirst();
			int ind2 = testCase.getSecond();
			double[] dist = testCase.getThird();
			for(int i = 0; i < distances.length; i++) {
				double trueD = dist[i];
				DataTuple t1 = data.getTuple(ind1);
				DataTuple t2 = data.getTuple(ind2);
				double computedD = distances[i].calculateDist(t1, t2);
				assertEquals(
						String.format("Distance %s between tuples %d: %s and %d: %s wrong", distances[i].toString(), ind1, t1.toString(), ind2, t2.toString()),
						trueD,
						computedD,
						precision
						);
			}
		}
	}
	
	private static double[] loadLabelProbabilities(String probFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(probFile));
		String line = br.readLine();
		br.close();
		return Fimp.getArrayOfDoubles(line);
	}

}

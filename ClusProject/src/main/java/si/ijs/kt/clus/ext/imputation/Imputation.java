package si.ijs.kt.clus.ext.imputation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import si.ijs.kt.clus.algo.kNN.KnnClassifier;
import si.ijs.kt.clus.algo.kNN.KnnModel;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.section.SettingsKNN;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.DistanceWeights;
import si.ijs.kt.clus.util.exception.ClusException;

public class Imputation {

	public Imputation() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static void impute(ClusRun cr, SettingsKNN settings) {
		// find the examples that need to be imputed
		RowData data = null; 
		try {
			data = (RowData) cr.getTrainingSet();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		HashMap<Integer, ArrayList<Integer>> missing = data.getMissingTargets();
		boolean isSparse = data.isSparse();
		int[] neededNeighbours = new int[missing.keySet().size()];
		int i = 0;
		for (int j : missing.keySet()) {
			neededNeighbours[i++] = j; 
		}
		Arrays.sort(neededNeighbours);
		// build the knn model
		int maxK = KnnClassifier.getMaxK(settings.getKNNk());
		ClusAttrType[] necessaryDescriptiveAttributes = KnnClassifier.getNecessaryDescriptiveAttributes(data);
		DistanceWeights distWeight = settings.getKNNDistanceWeights().get(0);
		KnnModel knn = null;
		try {
			knn = new KnnModel(cr, maxK, distWeight, maxK, isSparse, necessaryDescriptiveAttributes, neededNeighbours);
		} catch (ClusException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		// impute the values - jutar
		
	}

}

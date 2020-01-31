package si.ijs.kt.clus.ext.imputation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import si.ijs.kt.clus.algo.kNN.KnnClassifier;
import si.ijs.kt.clus.algo.kNN.KnnModel;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.section.SettingsKNN;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.DistanceWeights;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;

public class Imputation {

	public Imputation() {
		// TODO Auto-generated constructor stub
	}

	public static void impute(ClusRun cr, SettingsKNN settings) {
		// check the target types
		ClusAttrType[] targets = cr.getStatManager().getSchema().getAllAttrUse(AttributeUseType.Target);
		boolean allNominal = true;
		boolean allNumeric = true;
		for (ClusAttrType target : targets) {
			if (!target.isNominal()) {
				allNominal = false;
			}
			if (!target.isNumeric()) {
				allNumeric = false;
			}
		}
		if (!(allNominal || allNumeric)) {
			throw new RuntimeException("Target attributes should all be numeric or all nominal.");
		}
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
		// impute the values
		double[] predictedNum = null;
		int[] predictedNom = null;
		for (int example : missing.keySet()) {
			ClusStatistic prediction = null;
			DataTuple tuple = data.getTuple(example);
			try {
				prediction = knn.predictWeighted(tuple);
			} catch (ClusException e) {
				e.printStackTrace();
			}
			if (allNumeric) {
				predictedNum = prediction.getNumericPred();
			} else {
				predictedNom = prediction.getNominalPred();
			}
			for (int targetIndex : missing.get(example)) {
				if (allNumeric) {
					prediction.predictTupleOneComponent(tuple, targetIndex, predictedNum[targetIndex]);
				} else {
					prediction.predictTupleOneComponent(tuple, targetIndex, predictedNom[targetIndex]);
				}
			}
		}
		ClusLogger.info("Values imputed.");
	}
}

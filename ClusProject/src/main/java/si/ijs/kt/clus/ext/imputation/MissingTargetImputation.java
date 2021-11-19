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

public class MissingTargetImputation {

	public MissingTargetImputation() {
		// TODO Auto-generated constructor stub
	}
	
	public static void impute(ClusRun cr) {
		impute(cr, null);
	}

	public static void impute(ClusRun cr, HashMap<Integer, ArrayList<Integer>> missing) {
		// check the target types
		SettingsKNN settings = cr.getStatManager().getSettings().getKNN();
		ClusAttrType[] targets = cr.getStatManager().getSchema().getAllAttrUse(AttributeUseType.Target);
		boolean allNominal = true;
		boolean allNumeric = true;
		boolean allClasses = true;
		for (ClusAttrType target : targets) {
			if (!target.isNominal()) {
				allNominal = false;
			}
			if (!target.isNumeric()) {
				allNumeric = false;
			}
			if (!target.isClasses()) {
				allClasses = false;
			}
		}
		if (!(allNominal || allNumeric || allClasses)) {
			throw new RuntimeException("Targets should be all numeric or all nominal or all classes.");
		}
		// find the examples that need to be imputed
		RowData data = null;
		try {
			data = (RowData) cr.getTrainingSet();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (missing == null) {
			missing = data.getMissingTargets();
		}
		boolean isSparse = data.isSparse();
		int[] neededNeighbours = new int[missing.keySet().size()];
		int i = 0;
		for (int j : missing.keySet()) {
			neededNeighbours[i++] = j;
		}
		Arrays.sort(neededNeighbours);
		boolean singular = neededNeighbours.length == 1;
		ClusLogger.info(String.format("%d example%s need%s imputation.", neededNeighbours.length, singular ? "" : "s", singular ? "s" : ""));
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
		int iterations = 0;
		ArrayList<Integer> toProcess = new ArrayList<Integer>();
		for(int example : neededNeighbours) {
			toProcess.add(example);
		}
		while (toProcess.size() > 0) {
			ArrayList<Integer> toProcessNext = new ArrayList<>();
			double[] predictedNum = null;
			int[] predictedNom = null;
			for (int example : missing.keySet()) {
				ClusStatistic prediction = null;
				DataTuple tuple = data.getTuple(example);
				try {
					prediction = knn.predictWeighted(tuple, missing.get(example));
					if (prediction == null) {
						toProcessNext.add(example);
						continue;
					}
				} catch (ClusException e) {
					e.printStackTrace();
				}
				if (allNumeric) {
					predictedNum = prediction.getNumericPred();
				} else if (allNominal) {
					predictedNom = prediction.getNominalPred();
				}
				for (int targetIndex : missing.get(example)) {
					if (allNumeric) {
						prediction.predictTupleOneComponent(tuple, targetIndex, predictedNum[targetIndex]);
					} else if (allNominal){
						prediction.predictTupleOneComponent(tuple, targetIndex, predictedNom[targetIndex]);
					} else {
						prediction.predictTuple(tuple);
					}
				}
			}
			iterations++;
			if (toProcess.size() > toProcessNext.size()) {
				toProcess = toProcessNext;
			} else {
				break;
			}
		}
		if (toProcess.size() >  0) {
			System.err.println("Cannot impute the values in a finite number of steps. Number of examples with missing values: " + toProcess.size());
		}
		ClusLogger.info(String.format("Values imputed in %d iteration(s).", iterations));
	}
}

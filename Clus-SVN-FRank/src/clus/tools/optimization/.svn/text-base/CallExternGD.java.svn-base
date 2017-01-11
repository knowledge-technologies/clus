package clus.tools.optimization;

// This file is a Java Native Interface calling gdopt code in C++
// Timo Aho 14.6.2010
// See http://java.sun.com/developer/onlineTraining/Programming/JDCBook/jni.html

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import clus.algo.rules.ClusRuleSet;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.tools.optimization.OptProbl.OptParam;

/**
 * @author Timo Aho
 */
public class CallExternGD {
	// Native method declaration
	//   native double[] doNativeGDOptimization(double[] rulePredictions, double[] data, String settings);
	native double[] externalOptim(String name, double[] data,
			double[] rulePreds, boolean[] ruleCovers);

	//Load the library
	static {
		System.loadLibrary("GDInterface");
	}

	/**
	 * Returns weights 
	 * @param clusStatManager 
	 * @param rset 
	 * @param clusData 
	 */
	public static ArrayList<Double> main(ClusStatManager clusStatManager, OptProbl.OptParam optInfo, ClusRuleSet rset) {
		int nbOfWeights = optInfo.m_rulePredictions.length;
		int nbOfRules = nbOfWeights; // Only rules
		// We are ignoring any other base functions than rules here


		Settings set = clusStatManager.getSettings();
		int nbTargs = (clusStatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET)).getNbAttributes();
		// Parameter data matrix dimension. Do we give descriptive dims also?
		int nbDescrForDataMatrix = 0;
		int nbRows = optInfo.m_trueValues.length;

		//if (rules.m_implicitLinearTerms != null) {
		if (set.getOptAddLinearTerms() == Settings.OPT_GD_ADD_LIN_YES_SAVE_MEMORY) {
			nbDescrForDataMatrix = clusStatManager.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE).length;
			nbOfWeights += nbDescrForDataMatrix * nbTargs;
		}	

		// Optimization normalization
		double[] normFactors = OptProbl.initNormFactors(nbTargs, set);
		double[] targetAvg = OptProbl.initMeans(nbTargs);


		// Change the default prediction - it will otherwise be set to zero
		for (int iTarg = 0; iTarg < nbTargs; iTarg++) {
			if (optInfo.m_rulePredictions[0].m_prediction[iTarg][0] != targetAvg[iTarg]) {
				System.err.println("Error: Difference in main for target nb "+ iTarg
						+ ". The values are " + optInfo.m_rulePredictions[0].m_prediction[iTarg][0]
						+ " and " + targetAvg[iTarg]);
				System.exit(1);
			}
			optInfo.m_rulePredictions[0].m_prediction[iTarg][0] = Math.sqrt(normFactors[iTarg]); // e.g. 2* std dev 
		}

		OptParam trainingSet = optInfo;
		OptParam validationSet = null;
		// If early stopping criteria is chosen, reserve part of the training set for early stop testing.
		if (set.getOptGDEarlyStopAmount() > 0) {

			int nbDataTest = (int)Math.ceil(nbRows * set.getOptGDEarlyStopAmount());

			// Create the early stopping data variables.
			validationSet = new OptParam(optInfo.m_rulePredictions.length,
					optInfo.m_baseFuncPredictions.length,
					nbDataTest,
					nbTargs, optInfo.m_implicitLinearTerms);
			trainingSet = new OptParam(optInfo.m_rulePredictions.length,
					optInfo.m_baseFuncPredictions.length,
					nbRows-nbDataTest,
					nbTargs, optInfo.m_implicitLinearTerms);
			OptProbl.splitDataIntoValAndTrainSet(clusStatManager, optInfo, validationSet, trainingSet);
		}

		double[] weights = new double[nbOfWeights];  

		// Rule predictions
		double[] rulePreds = new double[nbOfRules*nbTargs];
		for (int iRule = 0; iRule < nbOfRules; iRule++) {
			for (int iTarg = 0; iTarg < nbTargs; iTarg++) {
				// These are same for both training and validation set
				rulePreds[iRule*nbTargs+iTarg] = trainingSet.m_rulePredictions[iRule].m_prediction[iTarg][0]/Math.sqrt(normFactors[iTarg]);
			}
		}
		// Covers
		// nbOfWeights, first training set, then validation set
		boolean[] ruleCovers = new boolean[nbOfRules*nbRows];

		int nbInstTrain = trainingSet.m_rulePredictions[0].m_cover.length();
		int nbInstVal = validationSet.m_rulePredictions[0].m_cover.length();
		for (int iRule = 0; iRule < nbOfRules; iRule++) {
			// Index for ruleCovers array
			int iIndex = 0;
			// index for TrainingSet and validationSet
			int iInst = 0;
			int iMaxInst = nbInstTrain;
			OptParam targetData = trainingSet;
			
			
			//We first go trhough training set, then validation set
			for (; iInst < iMaxInst; iInst++, iIndex++) {
				ruleCovers[iRule*nbRows+iIndex] = targetData.m_rulePredictions[iRule].m_cover.get(iInst);
				if (iInst == nbInstTrain-1){
					iInst = -1;
					targetData = validationSet;
					iMaxInst = nbInstVal;
				}
			}	
		}

		// Data for linear terms and true values
		double[] binData = new double[nbRows*(nbTargs+nbDescrForDataMatrix)];

		
		// Index for bindata array
		int iIndex = 0;
		// index for TrainingSet and validationSet
		int iInst = 0;
		int iMaxInst = nbInstTrain;
		OptParam targetData = trainingSet;

		for (; iInst < iMaxInst; iInst++, iIndex++) {


			for (int iDescrDim = 0; iDescrDim < nbDescrForDataMatrix; iDescrDim++) {
				binData[iIndex*(nbTargs+nbDescrForDataMatrix)+iDescrDim] =
					// Get the first (index [0])linear term for each descr dim 
					targetData.m_implicitLinearTerms.predict(iDescrDim*nbTargs,
							targetData.m_trueValues[iInst].m_dataExample, 0, nbTargs)
							/Math.sqrt(normFactors[0]);
			}
			// For true values the shifting has not yet been done, we have to do it here
			for (int iTarDim = 0; iTarDim < nbTargs; iTarDim++) {
				binData[iIndex*(nbTargs+nbDescrForDataMatrix)+nbDescrForDataMatrix+iTarDim] = 
					(targetData.m_trueValues[iInst].m_targets[iTarDim]-targetAvg[iTarDim])
					/Math.sqrt(normFactors[iTarDim]);
			}

			//We first go trhough training set, then validation set
			if (iInst == nbInstTrain-1){
				iInst = -1;
				targetData = validationSet;
				iMaxInst = nbInstVal;
			}

		}


		//Create class instance
		CallExternGD mappedFile=new CallExternGD();
		//      String settings = "nbOfIterations 10000\nminTVal 0.0\nlinTermsUsed 0\nnbOfTargs 2\nnbTrainData 3\nnbValData 2\nnbOfRules 3";
		String settings = "";
		if (set.getOptAddLinearTerms() == Settings.OPT_GD_ADD_LIN_YES_SAVE_MEMORY)
			settings += "linTermsUsed 1\n";
		settings += "nbOfTargs " + nbTargs +"\nnbTrainData "+ nbInstTrain + "\nnbValData " + nbInstVal +"\nnbOfRules " + nbOfRules +
		"\nnbOfDescrAttr " +nbDescrForDataMatrix;
		settings += "\nnbOfIterations " + set.getOptGDMaxIter() + "\nminTVal " + set.getOptGDGradTreshold();
		settings += "\nnbOfDiffTVal " + set.getOptGDNbOfTParameterTry() + "\nnbNonZeroWeights " + set.getOptGDMaxNbWeights() + "\n";

//		System.out.print("data, rulepreds, covers");
//		for (int i = 0; i < 10; i++)
//			System.out.print(" " + binData[i] + "\t" + rulePreds[i] + "\t" + ruleCovers[i] + "\n");
		weights=mappedFile.externalOptim(settings, binData, rulePreds, ruleCovers);

		
		weights[0] = undoNormalization(weights[0], rulePreds, nbTargs, targetAvg, rset, normFactors); // Undo the effect on default rule
		//		System.out.print("Toimii Javakin\n");
		ArrayList<Double> result = new ArrayList<Double>(nbOfWeights);
		for (int i = 0; i < nbOfWeights; i++) {
			result.add(weights[i]);
		}
		return result;
	}
	/*
	/** Is the predictor of given index a regular rule * / 
	static final protected boolean isRuleTerm(int index, OptProbl.OptParam param) {
		return index < param.m_rulePredictions.length;
	}
	
	static final private double linTermPred(int iLinTerm, int iInstance, int iTarget, int nbOfTargets){
		return 0;
	}
	
	static protected double getPredictionsWhenCovered(int iRule, int iInstance, 
			int iTarget, int iClass, OptProbl.OptParam param, double[] data) {
		if (!isRuleTerm(iRule, param)) {
			// Always save memory linears
			return linTermPred(iRule-param.m_rulePredictions.length,
					iInstance, iTarget,
					param.m_rulePredictions[0].m_prediction.length); // Nb of targets.
		} else {
			return param.m_rulePredictions[iRule].m_prediction[iTarget][iClass];
		}
	}
	*/
	
	// Returns new weight
	static final private double undoNormalization(double defaultRuleWeights, double[] defaultRulePreds, int nbTargs, double[] targMeans, ClusRuleSet rset, double[] normFactors) {
		double[] newDefault = new double[nbTargs];
		for (int iTarg = 0; iTarg < nbTargs; iTarg++) {
			 // Let's add the prediction got in the optimization to the average
			newDefault[iTarg] = defaultRulePreds[iTarg]*Math.sqrt(normFactors[iTarg]) // Undo normalization;
								* defaultRuleWeights // Include weight into the prediction
								+ targMeans[iTarg];
		}
		rset.getRule(0).setNumericPrediction(newDefault);
		return 1.0;
	}
}

//if (getSettings().getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY) {
//weights = callFriedmanBinary(getStatManager(), param);
//
//// The last one is so called intercept. Should be added to all predictions
//// Because of this we add a rule with prediction 1 and always true condition.
//// Thus the last weight will be for this.
//if (Settings.VERBOSE > 0) System.out.println("Adding intercept rule created by binary explicitly to rule set.");
//ClusRule interceptRule = new ClusRule(m_StatManager);
//interceptRule.m_TargetStat = getStatManager().createTargetStat();
//if (!(interceptRule.m_TargetStat instanceof RegressionStat))
//		System.err.println("Error: Using external binary for GD optimization is implemented for single target regression only.");
//((RegressionStat) interceptRule.m_TargetStat).m_Means = new double[1];
//((RegressionStat) interceptRule.m_TargetStat).m_Means[0] = 1; // Hopefully this is the prediction, CHECK!
//((RegressionStat) interceptRule.m_TargetStat).m_NbAttrs = 1;
//((RegressionStat) interceptRule.m_TargetStat).m_SumValues = new double[1];
//((RegressionStat) interceptRule.m_TargetStat).m_SumWeights = new double[1];
//((RegressionStat) interceptRule.m_TargetStat).m_SumValues[0] = 1;
//((RegressionStat) interceptRule.m_TargetStat).m_SumWeights[0] = 1;
//
//
//rset.m_Rules.add(interceptRule); // Adds the rule to the last position
//}
///** Use external binary (by Friedman) for GD optimization
//* Works only for regression currently!
//*/
//private ArrayList<Double> callFriedmanBinary(ClusStatManager stat_mgr, OptProbl.OptParam dataInformation) {
//
//String fname = getSettings().getDataFile();
//PrintWriter fPSBasePredictions = null;
//PrintWriter fPSInstances = null;
//PrintWriter fPSInputSettings  = null;
//double NUMBER_INF = 9E36;
//try {
//	/** Base predictions */
//	fPSBasePredictions = new PrintWriter(
//			new OutputStreamWriter(new FileOutputStream(fname+".predictor.dat")));
//	/** Actual examples */
//	fPSInstances = new PrintWriter(
//			new OutputStreamWriter(new FileOutputStream(fname+".responses.dat")));
//	/** Input for the binary */
//	fPSInputSettings = new PrintWriter(
//			new OutputStreamWriter(new FileOutputStream(fname+".input.txt")));
//} catch (Exception e) {
//	System.err.println("Something wrong in callExternalGDBinary - cannot create the files");
//	System.err.print(e.getMessage());
//	System.exit(1);
//	// TODO: handle exception
//}
//// Generate pathseeker input
//
//if (dataInformation.m_rulePredictions.length != 0){
//	System.err.println("Error: Use of external binary for optimization not implemented for rules correctly!");
//}
//// Predictions
//for (int iRule = 0 ; iRule < dataInformation.m_baseFuncPredictions.length; iRule++){
//	//			for (int iRule = 0 ; iRule < 10; iRule++){
//
//	for (int iInstance = 0; iInstance < dataInformation.m_baseFuncPredictions[iRule].length; iInstance++){
//		if (Double.isNaN(dataInformation.m_baseFuncPredictions[iRule][iInstance][0][0]))
//			fPSBasePredictions.write("" + 0); // If no prediction, prediction is 0
//		else if (Double.isInfinite(dataInformation.m_baseFuncPredictions[iRule][iInstance][0][0]))
//			fPSBasePredictions.write("" + NUMBER_INF);	// if prediction is infinity (for linear term), tell it
//
//		else
//			//fPSBasePredictions.write("" + (float)dataInformation.m_predictions[iRule][iInstance][0][0]);
//			fPSBasePredictions.write("" + dataInformation.m_baseFuncPredictions[iRule][iInstance][0][0]);
//
//		if (//iRule != dataInformation.m_predictions.length-1 ||
//				iInstance != dataInformation.m_baseFuncPredictions[iRule].length-1)
//			fPSBasePredictions.write(",");
//		//					if (iRule != 10-1 ||
//		//							iInstance != dataInformation.m_predictions[9].length-1)
//
//	}
////	if (iRule != dataInformation.m_predictions.length-1)
//		fPSBasePredictions.write("\n"); // A predictor printed
//}
//
//// Examples
//for (int iInstance = 0 ; iInstance < dataInformation.m_trueValues.length; iInstance++){
//	//fPSInstances.write("" + (float) dataInformation.m_trueValues[iInstance][0]);
//	fPSInstances.write("" + dataInformation.m_trueValues[iInstance].m_targets[0]);
//	if (iInstance != dataInformation.m_trueValues.length-1)
//		fPSInstances.write(",");
//}
//
//fPSInputSettings.println("@mode=regres");
//fPSInputSettings.println("@model_file=" + fname + ".model.pth");
//fPSInputSettings.println("@coeffs_file=" + fname + ".coeffs.pth");
////fPSInputSettings.println("@nvar=" + rset.getModelSize());
//fPSInputSettings.println("@nvar=" + dataInformation.m_baseFuncPredictions.length);
////fPSInputSettings.println("@nvar=" + 10);
//fPSInputSettings.println("@nobs=" + dataInformation.m_trueValues.length);
//fPSInputSettings.println("@format=csv");
//fPSInputSettings.println("@response_data=" + fname + ".responses.dat");
//fPSInputSettings.println("@pred_data=" + fname + ".predictor.dat");
//fPSInputSettings.println("@org=by_var");
//fPSInputSettings.println("@missing="+NUMBER_INF);
//fPSInputSettings.println("@obs_weights=equal");
//fPSInputSettings.println("@var_weights=equal");
////fPSInputSettings.println("@quantile=0.025"); //ROBUSTNESS of maximum and minimum value of linear terms (or functions)!
//fPSInputSettings.println("@quantile=0.0");
////fPSInputSettings.println("@alpha=" + getSettings().getOptHuberAlpha()); // 0.9 is enoughHUBER
//fPSInputSettings.println("@alpha=1.0"); // Squared loss
//fPSInputSettings.println("@numspect=0");
////fPSInputSettings.println("@constraints=all");
//fPSInputSettings.println("@constraints=none");
////fPSInputSettings.println("@nfold=10");
//if (getSettings().getOptGDEarlyStopAmount() == 0) { // No early stopping
//	fPSInputSettings.println("@nfold="+(-1.0)*dataInformation.m_trueValues.length); // only one as test set
//} else {
//	fPSInputSettings.println("@nfold="+(-1.0)/getSettings().getOptGDEarlyStopAmount());
//}
//fPSInputSettings.println("@start=" + getSettings().getOptGDGradTreshold());
//fPSInputSettings.println("@end=" + getSettings().getOptGDGradTreshold());
////fPSInputSettings.println("@numval=6");
//fPSInputSettings.println("@numval=1");
//fPSInputSettings.println("@modsel=a_roc");
//fPSInputSettings.println("@delnu="+ getSettings().getOptGDStepSize());
//fPSInputSettings.println("@maxstep=" + getSettings().getOptGDMaxIter()); // Maximum amount of iterations
//fPSInputSettings.println("@kfreq=100"); // Recomputing gradients and checking test risks!
//if (getSettings().getOptGDEarlyStopAmount() == 0) { // No early stopping
//	fPSInputSettings.println("@convfac="  + 10000000); // Treshold for early stop
//} else {
//	fPSInputSettings.println("@convfac="  + getSettings().getOptGDEarlyStopTreshold()); // Treshold for early stop
//}
//fPSInputSettings.println("@fast=no");
//if (getSettings().getOptGDExternalMethod() == Settings.GD_EXTERNAL_METHOD_GD)
//	fPSInputSettings.println("@impl=update"); // Fast implementation (GD) or brute force (brute)?
//else
//	fPSInputSettings.println("@impl=brute"); // Fast implementation (GD) or brute force (brute)?
//
//fPSInputSettings.println();
//// getSettings().getOptGDMaxNbWeights() cannot be defined!
//// getSettings().getOptGDLossFunction() cannot be defined!
//fPSBasePredictions.close();
//fPSInstances.close();
//fPSInputSettings.close();
//// Run pathseeker
//Scanner fCoefficients = null;
//
////try {
////	// The previous files has to be deleted
//////	Runtime.getRuntime().exec("cmd /c del " + fname + ".model.pth");
//////	Runtime.getRuntime().exec("cmd /c del " + fname + ".coeffs.pth");
////} catch (Exception e) {
////	// Do not do anything. The deletion is not important.
////	System.err.print(e.getMessage());
////}
//try {
//	Process psProcess = Runtime.getRuntime().exec("cmd /c PS_train.exe < "+fname+".input.txt");
//
//	BufferedReader stdInput = new BufferedReader(new
//			InputStreamReader(psProcess.getInputStream()));
//
//	BufferedReader stdError = new BufferedReader(new
//			InputStreamReader(psProcess.getErrorStream()));
//
//
//	System.out.println("\nHere is the standard output of the external command:\n");
//	String s = null;
//	while ((s = stdInput.readLine()) != null) {
//		System.out.println(s);
//		s = stdInput.readLine();
//	}
//
//	// read any errors from the attempted command
//
//	System.err.println("\nHere is the standard error of the external command (if any):\n");
//	while ((s = stdError.readLine()) != null) {
//		System.err.println(s);
//	}
//
//	if (psProcess.exitValue() != 0) {
//		System.err.println("Exit value of binary nonzero. Exiting. \n");
//		System.exit(1);
//	}
//	// Read pathseeker weights
//
//	fCoefficients = new Scanner(new BufferedReader(new InputStreamReader(
//			new FileInputStream(fname + ".coeffs.pth"))));
//
//} catch (Exception e) {
//	System.err.println("Something wrong in callExternalGDBinary - cannot run the binary.");
//	System.err.print(e.getMessage());
//	System.exit(1);
//	// TODO: handle exception
//}
//ArrayList<Double> weights = new ArrayList<Double>();
////String ddd = fCoefficients.next();
//
////		while (fCoefficients.hasNextDouble()) {
////			weights.add(new Double(fCoefficients.nextDouble()));
////		}
//
//while (fCoefficients.hasNext()) {
//	weights.add(new Double(fCoefficients.next()));
//}
//
//// The last one is so called intercept. Should be added to all predictions
//fCoefficients.close();
//
//return weights;
//}
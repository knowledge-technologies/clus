/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Created on 2006.3.29
 */
package clus.tools.optimization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

import clus.main.*;
import clus.algo.rules.ClusRuleSet;
import clus.algo.rules.RuleNormalization;
import clus.data.rows.DataTuple;
import clus.data.type.*;
import clus.statistic.*;
import clus.util.ClusFormat;

// Created 28.11.2008 from previous DeProbl class

/**
 * Class representing a optimization problem.
 * Can be used e.g. in Differential evolution and Gradient descent optimization
 * Includes loss functions and regularization
 * @author Timo Aho
 * @author Tea Tusar
 */
public class OptProbl {

	/** A regression prediction for initialization.
	 * It is negative so that this class is not selected in classification.
	 * Because default rule is used, this value should never go to loss function.
	 */
	static protected final double INVALID_PREDICTION = Double.NEGATIVE_INFINITY;

	/**
	 * Parameters for optimization algorithm.
	 * Predictions for rules and other base function and true values for all the instances of data.
	 * The default prediction is used, if no other rule covers the instance.
	 * See m_RulePred and m_TrueVal for what the indices mean.
	 */
	static public class OptParam {
		/** An empty class */
		public OptParam(int nbRule, int nbOtherBaseFunc, int nbInst, int nbTarg, ImplicitLinearTerms implicitLinTerms) {
			m_rulePredictions = new RulePred[nbRule]; for (int jRul=0; jRul<nbRule;jRul++) {m_rulePredictions[jRul]=new OptProbl.RulePred(nbInst,nbTarg);}
//			m_baseFuncPredictions = new double[nbOtherBaseFunc][nbInst][nbTarg][1];	m_trueValues = new double[nbInst][nbTarg];}
			m_baseFuncPredictions = new double[nbOtherBaseFunc][nbInst][nbTarg][1];	m_trueValues = new TrueValues[nbInst]; m_implicitLinearTerms = implicitLinTerms;}

		public OptParam(RulePred[] rulePredictions, double[][][][] predictions,	TrueValues[] trueValues, ImplicitLinearTerms implicitLinTerms){m_rulePredictions = rulePredictions; m_baseFuncPredictions = predictions; m_trueValues = trueValues;m_implicitLinearTerms = implicitLinTerms;}
		//public OptParam(RulePred[] rulePredictions, double[][][][] predictions,	double[][] trueValues ){m_rulePredictions = rulePredictions; m_baseFuncPredictions = predictions; m_trueValues = trueValues;}
		public RulePred[] m_rulePredictions;
		public double[][][][] m_baseFuncPredictions;
		public TrueValues[] m_trueValues;
		//public double[][] m_trueValues;
		public ImplicitLinearTerms m_implicitLinearTerms = null;
	}

	/** Number of weights/variables to optimize */
	private int m_NumVar;

	
	/**
	 * Predictions of rule type base functions.
	 * Includes the prediction and boolean array of covered instances.
	 */
	static public class RulePred {
		/** An empty class */
//		public RulePred(int nbInst, int nbTarg){m_cover = new boolean[nbInst];m_prediction = new double[nbTarg][1];}
//		public RulePred(boolean[] cover, double[][] prediction){m_cover = cover;m_prediction=prediction;}

/*		public RulePred(int nbInst, int nbTarg){m_cover = new WAHBitSet();m_prediction = new double[nbTarg][1];}
		public RulePred(WAHBitSet cover, double[][] prediction){m_cover = cover;m_prediction=prediction;}

		//public boolean[] m_cover; // [instance] that are covered
		public WAHBitSet m_cover; // [instance] that are covered
		public double[][] m_prediction; // [target index][class value]
*/

		public RulePred(int nbInst, int nbTarg){m_cover = new BitSet(nbInst);m_prediction = new double[nbTarg][1];}
		public RulePred(BitSet cover, double[][] prediction){m_cover = cover;m_prediction=prediction;}

		//public boolean[] m_cover; // [instance] that are covered
		public BitSet m_cover; // [instance] that are covered
		public double[][] m_prediction; // [target index][class value]
	}
	/**
	 * Rule indexes are always first. 
	 * Rule predictions [rule index]. Includes the prediction and boolean array of covered instances.
	 * Similar to m_BaseFuncPred but only for rules (constant prediction if covers).
	 */
	private RulePred[] m_RulePred;

	/**
	 * Other base function predictions [function index][instance][target index][class_value] is for 
	 * nominal attributes or [function index][instance][target index][0] for regression.
	 * The [target index] is always [0] for single target use.
	 * The class value dimension is dynamic for each target index.
	 * These base functions have always indices AFTER rules.
	 * For rules use the other array, it is faster!
	 */
	private double[][][][] m_BaseFuncPred;

	/** 
	 * If we want to save memory (amount of linear terms may be huge) we are not yet explicitly 
	 * adding the linear terms to the set. Instead we are asking from the linear term set for the prediction.
	 * This is used only if a switch is set on.
	 */
	private ImplicitLinearTerms m_LinTermMemSavePred = null;
	
	
	/**
	 * True values of the instances. Includes targets for single line (example)
	 * and maybe a reference (if implicit linear terms are used) for the line
	 * this represents.
	 */
	static public class TrueValues {
		/** An empty class */
		public TrueValues(int nbTarg){m_targets = new double[nbTarg];}
		public TrueValues(int nbTarg, DataTuple instance){m_targets = new double[nbTarg]; m_dataExample = instance;}
		/** Data tuple this true value is connected. May be null if not  needed */
		public DataTuple m_dataExample; 
		public double[] m_targets; // [target index]
	}
	

	/** True target values for the data points. [instance] */
	private TrueValues[] m_TrueVal;
/* * True target values for the data points. [instance][target index] */
//	private double[][] m_TrueVal;
	private ClusStatManager m_StatMgr;
	private boolean m_ClssTask;
	
	/** For normalization during optimization, the averages for each of the targets.
	 * If normalization is not used, means equal 0.*/
	private double[] m_TargetAvg;
	/** For normalization during optimization, the std devs for each of the targets */
	private double[] m_TargetNormFactor;
	
	/** Do we use save memory version of linear terms. A very often used function call */
	boolean m_saveMemoryLinears;

	/**
	 * Constructor for problem to be solved with optimization. Both classification and regression.
	 * @param stat_mgr Statistics
	 * @param dataInformation The true values and predictions for the instances. These are used by OptimProbl.
	 *                        The optimization procedure is based on this data information
	 * @param isClassification Is it classification or regression?
	 */
	public OptProbl(ClusStatManager stat_mgr, OptParam optInfo) {
		m_StatMgr = stat_mgr;

		m_saveMemoryLinears = getSettings().getOptAddLinearTerms() == Settings.OPT_GD_ADD_LIN_YES_SAVE_MEMORY;
		
		if (m_saveMemoryLinears){
			int nbOfTargetAtts = m_StatMgr.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET).length;
			int nbOfDescrAtts = m_StatMgr.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE).length;
			m_NumVar = (optInfo.m_baseFuncPredictions).length+(optInfo.m_rulePredictions).length
				+nbOfTargetAtts*nbOfDescrAtts;
		} else
			m_NumVar = (optInfo.m_baseFuncPredictions).length+(optInfo.m_rulePredictions).length;
		
		// May be null if not used
		m_LinTermMemSavePred = optInfo.m_implicitLinearTerms;
		m_BaseFuncPred = optInfo.m_baseFuncPredictions;
		m_RulePred = optInfo.m_rulePredictions;
		m_TrueVal = optInfo.m_trueValues;


		if (ClusStatManager.getMode() != ClusStatManager.MODE_REGRESSION &&
				ClusStatManager.getMode() != ClusStatManager.MODE_CLASSIFY)
		{
			System.err.println("Weight optimization: Mixed types of targets (reg/clas) not implemented. Assuming regression.\n ");
			//				"The targets are of different kind, i.e. they are not all for regression or for classifying.\n" +
			//				"Mixed targets are not yet implemented. The targets are considered as regression.\n" +
			//				"This error message may be due the clustering variables also.\n" +
			//				"The optimization may not work in this case also.\n");
		}

		m_ClssTask = (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY);

		
		
		// Compute data statistics
		
		if (!m_ClssTask) { // regression	
			m_TargetAvg = new double[getNbOfTargets()]; // Only zeroes
			
			// Normalization factors and means are only stored if they are used 
			if (getSettings().isOptNormalization()) {
//				double[] valuesFor0Variance = new double[getNbOfTargets()];
//				boolean[] varIsNonZero = checkZeroVariance(valuesFor0Variance);
//				double[] means = computeMeans(varIsNonZero, valuesFor0Variance);
//				m_TargetNormFactor = computeOptNormFactors(means, varIsNonZero, valuesFor0Variance);
				m_TargetNormFactor = initNormFactors(getNbOfTargets(), getSettings());
				if (getSettings().getOptNormalization() != Settings.OPT_NORMALIZATION_ONLY_SCALING)
					m_TargetAvg = initMeans(getNbOfTargets());
			}
		}
	}
	
	/** Splits data into validation and training sets randomly */
	static protected void splitDataIntoValAndTrainSet(ClusStatManager stat_mgr, OptParam origData,
			OptParam valData, OptParam trainData) {

		   Settings set = stat_mgr.getSettings();
		   int nbRows = origData.m_trueValues.length;

			int nbDataTest = (int)Math.ceil(nbRows * set.getOptGDEarlyStopAmount());

			// For random sample
			Random randGen = new Random(0);

			// Copy the prediction and true value references (no cloning)

			/** All the test set instances are added here so no duplication occurs */
			boolean[] selectedInstances = new boolean[nbRows];

			for (int iTestSetInstance = 0; iTestSetInstance < nbDataTest; iTestSetInstance++){

				// Take a random index
				// random number between [0,still available dataset[
				int newIndex = randGen.nextInt(nbRows-iTestSetInstance);

				
				
				
				/** Instance index in the data set to be added for test set */
				int iNewTestInstance = 0;
				// Search for the real index when duplicates are not taken into account
				// Thus skip the instance in indexOfUnUsedInstance if it is already taken
				for (int indexOfUnUsedInstance = 0; indexOfUnUsedInstance < newIndex; iNewTestInstance++) {
					if (!selectedInstances[iNewTestInstance])

						indexOfUnUsedInstance++;
				}
				// Still if we ended up to selected index, skip all the selected
				while (selectedInstances[iNewTestInstance])
					iNewTestInstance++;

				// Here we should have in iNewTestInstance the 'newIndex'th unused instance
				selectedInstances[iNewTestInstance] = true;

				valData.m_trueValues[iTestSetInstance] = origData.m_trueValues[iNewTestInstance];
				// To be safe, put original reference to null
				origData.m_trueValues[iNewTestInstance] = null;

				// Add the new instance for all the rules
				for (int iNonRule = 0; iNonRule < origData.m_baseFuncPredictions.length;iNonRule++){
					valData.m_baseFuncPredictions[iNonRule][iTestSetInstance] = origData.m_baseFuncPredictions[iNonRule][iNewTestInstance];
					// 	To be safe, put original reference to null
					origData.m_baseFuncPredictions[iNonRule][iNewTestInstance] = null;
				}

				for (int iRule = 0; iRule < origData.m_rulePredictions.length;iRule++){
					if (origData.m_rulePredictions[iRule].m_cover.get(iNewTestInstance))
						valData.m_rulePredictions[iRule].m_cover.set(iTestSetInstance);

				}

			}

			/** Index for the rest array */
			int iInstanceRestIndex = 0;
			int nbOfInstances = nbRows;
			for (int iInstance = 0; iInstance < nbOfInstances; iInstance++){

				if (!selectedInstances[iInstance]) {
					// Not used as test instance - add it
					trainData.m_trueValues[iInstanceRestIndex] =	origData.m_trueValues[iInstance];

					for (int iRule = 0; iRule < origData.m_baseFuncPredictions.length;iRule++){
						trainData.m_baseFuncPredictions[iRule][iInstanceRestIndex] =	origData.m_baseFuncPredictions[iRule][iInstance];
					}

					for (int iRule = 0; iRule < origData.m_rulePredictions.length;iRule++){

						if (origData.m_rulePredictions[iRule].m_cover.get(iInstance))
							trainData.m_rulePredictions[iRule].m_cover.set(iInstanceRestIndex);
					}

					iInstanceRestIndex++;
				}
			}

			for (int iRule = 0; iRule < origData.m_rulePredictions.length; iRule++){			
				trainData.m_rulePredictions[iRule].m_prediction = origData.m_rulePredictions[iRule].m_prediction;
				valData.m_rulePredictions[iRule].m_prediction = origData.m_rulePredictions[iRule].m_prediction;
			}

			if (iInstanceRestIndex != trainData.m_trueValues.length) {
				System.err.println("GDProbl error. Wrong amount of early stop data added");
				System.exit(1);
			}

			// Give the same std devs for this smaller part of data.

			// We are using Fitness function of  the problem. Let us put the reg penalty to 0 because we do not
			// want to use it
			set.setOptRegPar(0);
			set.setOptNbZeroesPar(0);
		
	}

	
	/**
	 * Fitness function over all targets.
	 * The classification prediction is voting without weights.
	 * Smaller is better.
	 * @param genes The current generation (population).
	 * @return fitness score
	 */

	final public double calcFitness(ArrayList<Double> genes) {
		return calcFitnessForTarget(genes, -1);
	}

	/**
	 * Fitness function for single target, not for all of them.
	 * The classification prediction is voting without weights.
	 * Smaller is better.
	 * @param genes The current generation (population).
	 * @param iTarget For which target we are computing the fitness. If -1 for all of them.
	 * @return fitness score
	 */
	public double calcFitnessForTarget(ArrayList<Double> genes, int iFitnessTarget) {
		// Only the target attributes are returned?
		ClusStatistic tar_stat = getTargetStat();

		// TODO mixture of nominal and numeric attributes. For multi target case some of the targets may be nominal, some numerical
		int nb_rows = getNbOfInstances(); // Number of instances
		int nb_covered = 0; // Number of rule covered instances
		int nb_targets = tar_stat.getNbAttributes();

		// For which targets we want to compute the fitness
		int indFirstTarget = 0; // On default we start from the first target
		int indLastTarget = tar_stat.getNbAttributes()-1; // On default we compute over all targets
		if (iFitnessTarget != -1) {
			// If a single target given - compute only over it
			indFirstTarget = iFitnessTarget;
			indLastTarget = iFitnessTarget;
		}


		/** Number of values for each target.  For regression classes are not needed, thus value is 1.*/
		int[] nb_values = new int[nb_targets];


		for (int iTarget = indFirstTarget; iTarget <= indLastTarget; iTarget++){
			if (isClassifTask()) {
				// Number of different values for the attribute
				nb_values[iTarget] = ((ClassificationStat)tar_stat).getAttribute(iTarget).getNbValues();
			} else {// regression
				nb_values[iTarget] = 1; // No classes are needed
			}
		}

		/** Prediction of the gene for the instances. */
		double pred[][]=new double[nb_rows][nb_targets];

		// An index over the instances
		for (int iInstance = 0; iInstance < nb_rows; iInstance++) {

			// Sum of class weights from different individuals for the prediction
			// For regression nb_values = 1 always.
			double[][] pred_sum = new double[nb_targets][];

			for (int iTarget = indFirstTarget; iTarget <= indLastTarget; iTarget++) {
				pred_sum[iTarget] = new double[nb_values[iTarget]];

				if (isClassifTask()) {
					//pred[iInstance][iTarget]= -1; // Initialize for invalid? class
					pred[iInstance][iTarget]= INVALID_PREDICTION; // Initialize for invalid? class
					for (int iValue = 0; iValue < nb_values[iTarget]; iValue++) {
						pred_sum[iTarget][iValue] = INVALID_PREDICTION;
					}
				} else
				{
					// For regression initialize to zero.
					pred[iInstance][iTarget]= 0;
					pred_sum[iTarget][0] = 0;
				}
			}

			boolean covered = false; // Is the instance covered

			// An index over the weights for the rules (variables to optimize)
			for (int iRule = 0; iRule < getNumVar(); iRule++) {
				if (genes.get(iRule).doubleValue() != 0) {
					// An index over the targets of an instance (for multi targeted environments)
					for (int iTarget = indFirstTarget; iTarget <= indLastTarget; iTarget++)
					{
						// An index over the possible values of nominal attribute. 1 for regression
						for (int iClass = 0; iClass < nb_values[iTarget]; iClass++) {
							if (isCovered(iRule,iInstance)) {
								covered = true;

								// For each nominal value and target, add variable
								// <current optimized parameter value>*<strenght of nominal value> OR
								// <current optimized parameter value>*<regression prediction for rule>
								// I.e. this is the real prediction function - weighted sum over the rules
								pred_sum[iTarget][iClass] += ((Double)genes.get(iRule)).doubleValue()
//					            					* m_RulePred[iRule][iInstance][iTarget][iClass];
													* getPredictionsWhenCovered(iRule,iInstance,iTarget,iClass);
								
							}
						}
					}
				}
			}

			// The prediction
			if (isClassifTask())
			{
				pred[iInstance] = predictClass(pred_sum);
			} else {
				// For regression, the prediction is the number we got
				pred[iInstance] = predictRegression(pred_sum);
			}

			if (covered) {
				nb_covered++; // One more instance covered
			}
		} // for over instances



		double loss = 0;
		// Loss function Loss(prediction, true value)
		// For classification the default is 0-1 loss
		if (isClassifTask()) {
			// Only one sensible loss type available for classification and it is not the default.
			if (getSettings().getOptDELossFunction() != Settings.OPT_LOSS_FUNCTIONS_01ERROR)
			{
				try{
					throw new Exception("DE optimization task is for classification, but the chosen loss " +
					"is mainly for regression. Use OptDELossFunction = 01Error to correct this.");
				} catch (Exception e){
					e.printStackTrace();
				}
				loss = loss(pred, iFitnessTarget);
			} else {
				//We want to care for covered instances only.
				loss = (loss(pred, iFitnessTarget)*nb_rows)/nb_covered;
			}
		} else {	// For regression
			loss = loss(pred, iFitnessTarget);
		}


		// Regularization for getting the weights as small as possible
		double reg_penalty = 0;
		if (getSettings().getOptRegPar() != 0.0) {
			reg_penalty = getSettings().getOptRegPar()*regularization(genes);
		}


		// Second Regularization (especially for DE): how many zeroes
		double nbOfZeroes_penalty = 0;
		if (getSettings().getOptNbZeroesPar() != 0.0) {
			nbOfZeroes_penalty = getSettings().getOptNbZeroesPar()*returnNbNonZeroes(genes);
		}

		//fitness = (1 - (acc / nb_covered*nb_targets)) + getSettings().getOptRegPar() *  reg_penalty;
		// TODO: regularization penalty should include dispersion, coverage?


		return loss+reg_penalty + nbOfZeroes_penalty;

	}

	/** Number of non zero weights for regularization purposes */
	private int returnNbNonZeroes(ArrayList<Double> genes) {

		int nbNonZeroes = 0;

		for (int j = 0; j < genes.size(); j++) {
			if (genes.get(j).doubleValue() != 0.0)
				nbNonZeroes++;
		}


		return nbNonZeroes;
	}

	/**
	 * Regression prediction.
	 * @param predictionSums The weighted sum of rules for all targets.
	 * 						 The second dimension not used i.e. [Target][1].
	 * @param nbOfTargets Number of targets
	 * @param nbOfValues Number of class values
	 * @return Array of prediction [target]
	 */
	private double[] predictRegression(double[][] predictionSums)
	{
		int nbOfTargets = predictionSums.length;
		double[] prediction = new double[nbOfTargets];
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++)
		{
			if (predictionSums[iTarget] != null) {
				// If internal normalization has been used, we need to use the offset parameter to 
				// shift the prediction to right place. If internal normalization is not used, the values should be zero		
				prediction[iTarget] = predictionSums[iTarget][0];
			}
		}

		return prediction;
	}


	/**
	 * Classification prediction.
	 * @param predictionSums The weighted sum of rules for different classes. [Target][Class value]
	 * @param nbOfTargets Number of targets
	 * @param nbOfValues Number of class values
	 * @return Array of prediction [target]
	 */
	protected double[] predictClass(double[][] predictionSums) {
		int nbOfTargets = predictionSums.length;
		double[] prediction = new double[nbOfTargets];

		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++)
		{
			double max = 0; // Maximum value so far
			int iMaxClass = 0; // The class with maximum value so far.

			// Which one of the classes has the highest voting, i.e. maximum value
			for (int iClass = 0; iClass < predictionSums[iTarget].length; iClass++) {
				if (predictionSums[iTarget][iClass] > max)
				{
					//prediction[iTarget] = (double)iClass;
					iMaxClass = iClass;
					max = predictionSums[iTarget][iClass];
				}
			}

//			if (predictionSums[iTarget][iMaxClass] == INVALID_PREDICTION) {
//				// We should come here only if for all the classes have INVALID_PREDICTION
//				// (This is because maximum value is -infinity = INVALID_PREDICTION)
//				// Thus no prediction is given (none of the nonzero weight rules cover the instance)
//				// Use the default rule instead
//				prediction[iTarget] = getDefaultPrediction(iTarget);
//			} else {

			// The default rule is included in the rule set. At least it always covers all the examples.
			prediction[iTarget] = (double)iMaxClass;
//			}
		}
		return prediction;
	}





	// int REGUL_GENERAL = // Give the regularization function as function object
	// int LOSS_GENERAL = // Give the loss function as function object

	/**
	 * Loss function for data set. Chooses the right loss function
	 * based on the settings file.
	 * @param trueValue The true values of targets. [Instance][Target]
	 * @param prediction The prediction. [Instance][Target]
	 * @param iTarget Over this target index we compute loss. If -1, over all examples
	 * @return Loss for the data.
	 */
	protected double loss(double[][] prediction, int iTarget){

		double loss = 0;
		switch (getSettings().getOptDELossFunction()) {
		case Settings.OPT_LOSS_FUNCTIONS_01ERROR:
			if (iTarget != -1)
				System.err.println("Loss over single target implemented only for squared loss!");
			loss = loss01(getTrueValues(), prediction);
			break;
		case Settings.OPT_LOSS_FUNCTIONS_RRMSE:
			if (iTarget != -1)
				System.err.println("Loss over single target implemented only for squared loss!");
			loss = lossRRMSE(getTrueValues(), prediction);
			break;
		case Settings.OPT_LOSS_FUNCTIONS_HUBER:
			if (iTarget != -1)
				System.err.println("Loss over single target implemented only for squared loss!");
			loss = lossHuber(getTrueValues(), prediction);
			break;
			//Default case
		case Settings.OPT_LOSS_FUNCTIONS_SQUARED:
		default:
			loss = lossSquared(prediction, iTarget);
			break;
		}

		return loss;
	}

	/**
	 * Squared distance loss function for data set.
	 * Is ok, because the data is normalized.
	 * @param trueValue The true values of targets. [Instance][Target]
	 * @param prediction The prediction. [Instance][Target]
	 * @return Loss for the data.
	 */
	private double lossSquared( double[][] prediction, int indTarget)
	{

		double loss = 0;
		int numberOfInstances = prediction.length;

		// If one target given
		if (indTarget != -1) {

			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{
				loss += Math.pow(getTrueValue(iInstance,indTarget)-prediction[iInstance][indTarget],2);
			}

		} else {
			int numberOfTargets = prediction[0].length;
			for (int jTarget = 0; jTarget < numberOfTargets; jTarget++)
			{
				double attributeLoss = 0; // Loss for one attribute.
				for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
				{
					// Missing values? if (!Double.isNaN(trueValue[iInstance][jTarget])) {
					//				if (iInstance == 68) {
					//					boolean debug = false;
					//					debug = true;
					//				}
					attributeLoss += Math.pow(getTrueValue(iInstance,jTarget)
					                           -prediction[iInstance][jTarget],2);
				}

				if (getSettings().isOptNormalization()) {
					attributeLoss /= getNormFactor(jTarget);
				}

				loss += ((double)1)/(2*numberOfTargets)*attributeLoss;			
			}
		}
		return loss/numberOfInstances; // Average loss over instances
	}

	/**
	 * Relative root mean squared error RRMSE loss function for data set.
	 * RRMSE is sum of squared errors divided by the variance.
	 * Relativeness is not useful, because the data is normalized.
	 * @param trueValue The true values of targets. [Instance][Target]
	 * @param prediction The prediction. [Instance][Target]
	 * @return Loss for the data.
	 */
	//Suggested by Zenko 2007, p. 27
	private double lossRRMSE(TrueValues[] trueValue, double[][] prediction){

		double loss = 0;
		int numberOfInstances = prediction.length;
		int numberOfTargets = prediction[0].length;

		for (int jTarget = 0; jTarget < numberOfTargets; jTarget++)
		{
			double attributeLoss = 0;
			double attribVariance = 0; // Variance of one attribute for true values
			double attribMean = 0; // Mean for an attribute true values.

			// Compute mean of true values
			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{
				attribMean += trueValue[iInstance].m_targets[jTarget];
			}
			attribMean = attribMean/numberOfInstances;

			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{
				attributeLoss += Math.pow(prediction[iInstance][jTarget]-trueValue[iInstance].m_targets[jTarget],2);
				attribVariance += Math.pow(attribMean-trueValue[iInstance].m_targets[jTarget],2);
			}

			loss += ((double)1)/numberOfTargets*Math.sqrt(attributeLoss/attribVariance);
		}

		return loss/numberOfInstances; // Average loss over instances
	}

	/**
	 * Huber 1962 loss function for data set.
	 * This is mainly the squared distance error. However for great distances it is smoothed.
	 * Thus it is robust to outliers.
	 * @param trueValue The true values of targets. [Instance][Target]
	 * @param prediction The prediction. [Instance][Target]
	 * @return Loss for the data.
	 */
	//Suggested by Friedman&Popescu 2007, p. 7
	private double lossHuber(TrueValues[] trueValue, double[][] prediction){

		double loss = 0;
		int numberOfInstances = prediction.length;

		// If no instances given, jump out
		if (numberOfInstances == 0)
			return 0;

		int numberOfTargets = prediction[0].length;

		/** For Huber 1962 loss function we need the the delta values for each target.
		 * Delta value depends on the alpha quantiles of the data.
		 */
		double	deltas[] = computeHuberDeltas(trueValue, prediction);

		for (int jTarget = 0; jTarget < numberOfTargets; jTarget++)
		{
			double attributeLoss = 0; // Loss for one attribute.
			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{
				if (Math.abs(trueValue[iInstance].m_targets[jTarget]-prediction[iInstance][jTarget]) < deltas[jTarget]) {
					attributeLoss += Math.pow(trueValue[iInstance].m_targets[jTarget]-prediction[iInstance][jTarget],2);
				} else { // Smoothed for distant objects
					attributeLoss += deltas[jTarget]*(Math.abs(trueValue[iInstance].m_targets[jTarget]-prediction[iInstance][jTarget])
											-deltas[jTarget]/2);
				}
			}

			loss += ((double)1)/numberOfTargets*attributeLoss;
		}

		return loss/numberOfInstances; // Average loss over instances

	}


	/**
	 * For Huber 1962 loss function we need the the delta values for each target.
	 * Delta value depends on the alpha quantiles of the data
	 *
	 */
	private double[] computeHuberDeltas(TrueValues[] trueValues,
			double[][] predictions) {

		int numberOfInstances = trueValues.length;
		int numberOfTargets = trueValues[0].m_targets.length;

		// Alpha quantile for how much of data is considered potential outliers
		double alpha = getSettings().getOptHuberAlpha();
		double deltas[] = new double[numberOfTargets];

		double targetDistances[] = new double[numberOfInstances];

		for (int jTarget = 0; jTarget < numberOfTargets; jTarget++){
			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{
				targetDistances[iInstance] = Math.abs(trueValues[iInstance].m_targets[jTarget]
				                                    - predictions[iInstance][jTarget]);
			}
			// Sort in ascending order
			Arrays.sort(targetDistances);

			// Find the value for which not more than alpha amount of doubles are less
			deltas[jTarget]= targetDistances[(int)Math.floor(numberOfInstances*alpha)];

		}
		return deltas;
	}

	/**
	 * 0-1 distance loss function for data set.
	 * Usually used only in classification.
	 * @param trueValue The true values of targets. [Instance][Target]
	 * @param prediction The prediction. [Instance][Target]
	 * @return Loss for the data.
	 */
	// TODO Squared error ramp loss could also be used
	//Suggested by Zenko 2007, p. 26
	private double loss01(TrueValues[] trueValue, double[][] prediction)
	{
		int accuracy = 0;
		int numberOfInstances = prediction.length;
		int numberOfTargets = prediction[0].length;

		for (int jTarget = 0; jTarget < numberOfTargets; jTarget++){
			for(int iInstance = 0; iInstance < numberOfInstances; iInstance++)
			{

				if (trueValue[iInstance].m_targets[jTarget] == prediction[iInstance][jTarget])
				{
					accuracy++;
				}
			}
		}

		// For all the target attributes, the weight is now 1/numberOfTargets
		return 1 - ((double)accuracy) / (numberOfInstances*numberOfTargets);
	}


	/**
	 *  Regularization penalty for the optimization function.
	 *  This reduces the size of genes so that they do not grow too much.
	 *  The power for the differences can be changed in settings file.
	 *  The default is lasso, power = 1. This keeps the weights zero if possible.
	 *  @param genes The genes, i.e. the weights.
	 *  @return Penalty for the weights.
	 */
	protected double regularization(ArrayList<Double> genes) {
		double reg_penalty = 0;

		for (int j = 0; j < genes.size(); j++) {
			// Lasso penalty, i.e. sum of absolute values of weights
			reg_penalty += Math.pow(Math.abs( ((Double)(genes.get(j))).doubleValue() ),
									getSettings().getOptDERegulPower());
		}

		return reg_penalty;
	}

	/** Number of variables to be optimized */
	final public int getNumVar() {
		return m_NumVar;
	}

	final protected Settings getSettings() {
		return m_StatMgr.getSettings();
	}

	final protected ClusStatistic getTargetStat() {
		return m_StatMgr.getStatistic(ClusAttrType.ATTR_USE_TARGET);
	}

	
	/** Does the base function cover the given example?
	 * This should be used instead of checkin if prediction is NaN.
	 */
	final protected boolean isCovered(int iRule, int iInstance) {
		if (iRule >= m_RulePred.length) {
			return m_saveMemoryLinears ? true : !Double.isNaN(m_BaseFuncPred[iRule-m_RulePred.length][iInstance][0][0]);
//			if (m_saveMemoryLinears) {
//				return true; // Not spare function call.
//			} else {
//				return !Double.isNaN(m_BaseFuncPred[iRule-m_RulePred.length][iInstance][0][0]);
//			}
		} else {
//			return m_RulePred[iRule].m_cover[iInstance];
			return m_RulePred[iRule].m_cover.get(iInstance);
		}
			
	}

	
	/** Value of base function prediction. Can be used also for nominal attributes
	 * when we already know that instance is covered! Use isCovered for this.
	 * If used right, gives always prediction.
	 */
	final protected double getPredictionsWhenCovered(int iRule, int iInstance, int iTarget, int iClass) {
		if (!isRuleTerm(iRule)) {
			if (m_saveMemoryLinears) {
				return m_LinTermMemSavePred.predict(iRule-m_RulePred.length,
						m_TrueVal[iInstance].m_dataExample, iTarget,
						m_RulePred[0].m_prediction.length); // Nb of targets.
			} else {
				return m_BaseFuncPred[iRule-m_RulePred.length][iInstance][iTarget][iClass];
			}
		} else {
			return m_RulePred[iRule].m_prediction[iTarget][iClass];
		}
	}
	
	/** Is the predictor of given index a regular rule */ 
	final protected boolean isRuleTerm(int index) {
		return index < m_RulePred.length;
	}
	/** Value of base function prediction in regression use
	 * when we already know that instance is covered! Use isCovered for this.
	 * If used right, gives always prediction.
	 */
	final protected double getPredictionsWhenCovered(int iRule, int iInstance, int iTarget) {
		return getPredictionsWhenCovered(iRule, iInstance, iTarget, 0);
	}

	// If mean not given, it is zero
	final protected double getTrueValue(int iInstance, int iTarget)  {
		return m_TrueVal[iInstance].m_targets[iTarget]-getMean(iTarget);
	}

	/**
	 * The loss function should be separate in the sense
	 * that it does not use member functions.
	 * OBSOLETE, does not take into account target avg even if it should!
	 * @return
	 */
	final private TrueValues[] getTrueValues() {
		return m_TrueVal;
	}

	final protected boolean isClassifTask() {
		return m_ClssTask;
	}

	final protected int getNbOfInstances() {
		return m_TrueVal.length;
	}

	protected int getNbOfTargets() {
		return getTargetStat().getNbAttributes();
	}

	/** Change the data used for learning. This is used if part of the data
	 * is used for e.g. testing. */
	final protected void changeData(OptParam newData) {
		m_BaseFuncPred = newData.m_baseFuncPredictions;
		m_RulePred = newData.m_rulePredictions;
//		m_defaultPred = newData.m_defaultPrediction;
		m_TrueVal = newData.m_trueValues;
	}

	/** Is prediction/true value valid value
	 */
	final protected boolean isValidValue(double pred) {
		return !Double.isInfinite(pred) && !Double.isNaN(pred);
	}

	/** Initializes means with global means, this is not always done, sometimes mean = 0 */
	static protected double[] initMeans(int nbTargs) {
		double[] means = new double[nbTargs];
		for (int iTarget = 0; iTarget < nbTargs; iTarget++) {
			means[iTarget] = RuleNormalization.getTargMean(iTarget);
		}
		return means;
	}

	/** Initializes norm factors based on precomputed std dev values. Norm factor is always
	 * a square of normalization factor of SINGLE term (because this is used for normalizing product
	 * of two terms) */
	static protected double[] initNormFactors(int nbTargs, Settings sett) {
		double[] scaleFactor = new double[nbTargs];
		for (int iTarget = 0; iTarget < nbTargs; iTarget++) {
			if (sett.getOptNormalization() == Settings.OPT_NORMALIZATION_YES_VARIANCE)
				scaleFactor[iTarget] = Math.pow(RuleNormalization.getTargStdDev(iTarget),4); //Math.pow(variance,2.0);
			else // std dev
				scaleFactor[iTarget] = 4*Math.pow(RuleNormalization.getTargStdDev(iTarget),2); //4*variance;
		}
		return scaleFactor;
	}

	
	/** For normalization during optimization, the scaling factor for each of the targets.
	 * For single prediction/true value Math.sqrt() of this should be used, because this is 
	 * meant for e.g. covariance computing. */	
	final protected double getNormFactor(int iTarget) {
		return m_TargetNormFactor[iTarget];
	}

	final protected double getMean(int iAttr) {
		return m_TargetAvg[iAttr];
	}
	
	/** Returns boolean array of rule covering */
	final protected BitSet getRuleCovers(int iRule) {
		return m_RulePred[iRule].m_cover;
	}
	
	/** Returns the index of next covered instance for the rule */
	final protected int getRuleNextCovered(int iRule, int iFromIndex) {
		return m_RulePred[iRule].m_cover.nextSetBit(iFromIndex);
	}
	
	/**
	 * Returns target dimension for linear term with this index.
	 * @param iLinTerm
	 * @return
	 */
	final protected int getLinTargetDim(int iLinTerm) {
		return (iLinTerm-m_RulePred.length)%getNbOfTargets();
	}
	/**
	 * Returns descriptive dimension for linear term with this index.
	 * @param iLinTerm
	 * @return
	 */
	final protected int getLinDescrDim(int iLinTerm) {
		return (int) Math.floor((double)(iLinTerm-m_RulePred.length)/getNbOfTargets());
	}
	/** Prepare predictions for normalization, the predictions might have to be changed */
	protected void preparePredictionsForNormalization() {
	
		// Preparations are needed only if some normalization with average shifting is used.
		if (!getSettings().isOptNormalization() 
				|| getSettings().getOptNormalization() == Settings.OPT_NORMALIZATION_ONLY_SCALING)
			return;
		
		// Change only the first rule, this changes the overall average of predictions
		// The first rule should now include the averages in practice, check this
		for (int iTarg = 0; iTarg < getNbOfTargets(); iTarg++) {
			if (getPredictionsWhenCovered(0, 0, iTarg) != getMean(iTarg)) {
				System.err.println("Error: Difference in preparePredictionsForNormalization for target nb "+ iTarg
						+ ". The values are " + getPredictionsWhenCovered(0, 0, iTarg)
						+ " and " + getMean(iTarg));
				System.exit(1);
			}
			m_RulePred[0].m_prediction[iTarg][0] = Math.sqrt(getNormFactor(iTarg)); // e.g. 2* std dev 
		}
		
		
		//DEBUG, after the changes, print these again!
		// If you want to check these, put early stop amount to 0
		if (GDProbl.m_printGDDebugInformation) {
			String fname = getSettings().getDataFile();

			PrintWriter wrt_pred = null;
			PrintWriter wrt_true = null;
			try {
				wrt_pred = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".gd-pred")));
				wrt_true = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".gd-true")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}

			printPredictionsToFile(wrt_pred);
			wrt_pred.close();
			printTrueValuesToFile(wrt_true);
			wrt_true.close();
		}
	}
	/** Changes rule set to undo the changes done to predictions */
	protected void changeRuleSetToUndoNormNormalization(ClusRuleSet rset) {
		// These are needed only if some normalization with average shifting is needed.
		if (!getSettings().isOptNormalization() 
				|| getSettings().getOptNormalization() == Settings.OPT_NORMALIZATION_ONLY_SCALING)
			return;

		double[] newPred = new double[getNbOfTargets()];
		for (int iTarg = 0; iTarg < getNbOfTargets(); iTarg++) {
			 // Let's add the prediction got in the optimization to the average
			newPred[iTarg] = getPredictionsWhenCovered(0, 0, iTarg)*rset.getRule(0).getOptWeight()
							+getMean(iTarg);
		}
		rset.getRule(0).setNumericPrediction(newPred);
		rset.getRule(0).setOptWeight(1.0);
	}
	
	

	private String printPred(int ruleIndex, int exampleIndex) {
		NumberFormat fr = ClusFormat.THREE_AFTER_DOT;
		String print = "[";
		for (int iTarg = 0; iTarg < getNbOfTargets(); iTarg++) {
			double pred = (double)getPredictionsWhenCovered(ruleIndex, exampleIndex, iTarg);
			if (getSettings().isOptNormalization()) {
				pred /= Math.sqrt(getNormFactor(iTarg)); // For single pred you have to take sqrt
			}
			print += "" + fr.format(pred);
			if (iTarg != getNbOfTargets()-1)
				print += "; ";
		}
		print += "]";
		return print;
	}
	
	/** Print predictions to output file. */
	protected void printPredictionsToFile(PrintWriter wrt) {
		if (getSettings().isOptNormalization()) {
			wrt.print("Norm factors: [");
			for (int iTarget = 0; iTarget < getNbOfTargets(); iTarget++) {
				wrt.print(getNormFactor(iTarget));
				if (iTarget != getNbOfTargets()-1)
					wrt.print("; ");
			}
			wrt.print("]\n");
		}
		for (int iRule = 0; iRule < getNumVar(); iRule++) {
			if (iRule < m_RulePred.length) {
				wrt.print("Rule nb " + iRule +": ");
				wrt.print(printPred(iRule, 0));
			} else {
				wrt.print("Term nb " + iRule +": ");
				// Print for all the instances
				for (int iInstance = 0; iInstance < this.getNbOfInstances(); iInstance++){
					wrt.print(isCovered(iRule, iInstance) ? printPred(iRule, 0):"[NA]");
				}
			}
			wrt.print("\n");
		}

	}


	/** Print true values to output file. */
	protected void printTrueValuesToFile(PrintWriter wrt) {
		NumberFormat fr = ClusFormat.THREE_AFTER_DOT;
		for (int iTrueVal = 0; iTrueVal < getNbOfInstances(); iTrueVal++) {

			wrt.print("[");
			for (int iTarg = 0; iTarg < getNbOfTargets(); iTarg++) {
				double val = (double)getTrueValue(iTrueVal, iTarg);
				if (getSettings().isOptNormalization()) {
					val /= Math.sqrt(getNormFactor(iTarg));
				}
				wrt.print(fr.format(val));
				if (iTarg != getNbOfTargets()-1)
					wrt.print("; ");
			}
			wrt.print("]\n");

		}
		wrt.print("\n");
	}

	
}

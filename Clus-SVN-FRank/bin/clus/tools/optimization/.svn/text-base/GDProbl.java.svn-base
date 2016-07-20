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

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

import clus.main.*;
import clus.tools.optimization.OptProbl.OptParam;
import clus.util.ClusFormat;

// Created 28.11.2008 from previous DeProbl class

/**
 * Class representing a gradient descent optimization problem.
 * This gives tool functions for the actual optimization algorithm.
 * @author Timo Aho
 */
public class GDProbl extends OptProbl {

	/** Do we print debugging information. */
	static protected boolean m_printGDDebugInformation = false;

	/**
	 * Covariances between weights. Are computed only if needed.
	 * [iFirstWeight][iSecondWeight]
	 */
	protected double[][] m_covariances;

	/**
	 *  Is covariance computed for this dimension?
	 *  To reduce computational costs, covariances for certain dimension are computed only if needed
	 *  Weight is always zero if the corresponding coefficient is zero.
	 *  This array SHOULD NOT be used for indication of nonzero weights.
	 *  The covariance may be computed but the weight nonzero if (and only if)
	 *  we are running the algorithm multiple times on the same predictions etc.
	 */
	protected boolean[] m_isCovComputed;

	/**
	 *  Is the weight nonzero for this run. If we are running optimization only once
	 *  this is same as m_isCovComputed.
	 */
	protected boolean[] m_isWeightNonZero;



	/**
	 * Number of rules with nonzero weights. I.e. how many trues in m_isWeightNonZero.
	 * The 'default rule' - the first rule that is always a constant is not counted!
	 */
	protected int m_nbOfNonZeroRules;

	/** Covariances between predictors and true values. If all the weights are zero,
	 * these are also the initial gradients before the first iteration.
	 * Weights are zero, so these do not have to be computed again 
	 * when running with same predictions and true values (different T values).
	 * These are also used when computing gradients from scratch.
	 */
	protected double[] m_predCovWithTrue;
	
	/** Computed negative gradient averages for each of the dimensions
	 * [iWeight]*/
	protected double[] m_gradients;

	/** Includes the weights that are banned.
	 * One value for each of the weights, if value > nbOfIterations, weight is banned.
	 */
	protected int[] m_bannedWeights;

	/** Step size for gradient descent */
	protected double m_stepSize;

//	// The following are for efficient step size computation if dynamic step size is used
//	/** Square of norm of gradient vector */
//	protected double m_gradientNormSquared;
//	
//	/** Gradient product with prediction vectors for each target */
//	protected double[] m_gradPredProduct;
//	
//	/** Mean of predictor predictions (over the data set) [predictor][target]*/
//	protected double[][] m_predictorMeans;
	
	
//	/** Separate test set for early stopping */
//	protected OptParam m_dataEarlyStop;

	/** New problem for computing fitness function with the early stop data */
	protected OptProbl m_earlyStopProbl;

	
	/**
	 * Constructor for problem to be solved with gradient descent. Currently only for regression.
	 * @param stat_mgr Statistics
	 * @param dataInformation The true values and predictions for the instances. These are used by OptimProbl.
	 *                        The optimization procedure is based on this data information
	 * @param isClassification Is it classification or regression?
	 */
	public GDProbl(ClusStatManager stat_mgr, OptParam optInfo) {
		super(stat_mgr, optInfo);

		// If needed, prepares for norm. Has to be done after normalization computation and before covariance computing.
		// May be safe to do this before splitting validation set (if default rule is used stored/used in validation set by mistake).
		preparePredictionsForNormalization();
		
		// If early stopping criteria is chosen, reserve part of the training set for early stop testing.
		if (getSettings().getOptGDEarlyStopAmount() > 0) {

			int nbDataTest = (int)Math.ceil(getNbOfInstances() * getSettings().getOptGDEarlyStopAmount());
			
			// Create the early stopping data variables.
			OptParam dataEarlyStop = new OptParam(optInfo.m_rulePredictions.length,
					optInfo.m_baseFuncPredictions.length,
					nbDataTest,
					getNbOfTargets(), optInfo.m_implicitLinearTerms);
			OptParam trainingSet = new OptParam(optInfo.m_rulePredictions.length,
					optInfo.m_baseFuncPredictions.length,
					getNbOfInstances()-nbDataTest,
					getNbOfTargets(), optInfo.m_implicitLinearTerms);
			splitDataIntoValAndTrainSet(stat_mgr, optInfo, dataEarlyStop, trainingSet);
		
			changeData(trainingSet);  // Change data for super class
			m_earlyStopProbl = new OptProbl(stat_mgr, dataEarlyStop);
		}


		int nbWeights = getNumVar();
//		int nbTargets = getNbOfTargets();

		//m_covariances = new double[nbWeights][nbWeights][nbTargets];
		m_covariances = new double[nbWeights][nbWeights];
		for (int i = 0; i < nbWeights; i++) {
			for (int j = 0; j < nbWeights; j++) {
//				for (int k = 0; k < nbTargets; k++)
//					m_covariances[i][j][k] = Double.NaN;
				m_covariances[i][j] = Double.NaN;
			}
		}
		m_isCovComputed = new boolean[nbWeights]; // Initial value is false

		initPredictorVsTrueValuesCovariances();
		
		// dynamic step size computation
		if (getSettings().isOptGDIsDynStepsize()) {
			computeDynStepSize();
		}
	}



	double m_dynStepLowerBound = 0;
	
	/** Initialize GD optimization for new run with same predictions and true values
	 * This can be used if some parameters change. Thus we e.g. do not compute covariances
	 * again
	 */
	public void initGDForNewRunWithSamePredictions() {
		int nbWeights = getNumVar();

		if (getSettings().getOptGDEarlyStopAmount() > 0) {
			m_minFitness = Double.POSITIVE_INFINITY;
			m_minFitWeights = new ArrayList<Double>(getNumVar());

			for (int iWeight = 0; iWeight < getNumVar(); iWeight++)
			{
				m_minFitWeights.add(new Double(0)); // Initialize
			}

		}

		m_isWeightNonZero = new boolean[nbWeights];

		if (getSettings().getOptGDMTGradientCombine() == Settings.OPT_GD_MT_GRADIENT_MAX_LOSS_VALUE) {
			m_bannedWeights = new int[nbWeights]; // Are used only for MaxLoss
		} else {
			m_bannedWeights = null;
		}
		m_gradients = new double[nbWeights];  // Initial value is zero

		m_nbOfNonZeroRules = 0;
		m_stepSize = getSettings().getOptGDStepSize();
		
		// dynamic step size computation
		if (getSettings().isOptGDIsDynStepsize()) {
			m_stepSize = m_dynStepLowerBound;
		}
	}


//	double m_dynStepSizeDrop = 0;
	private void computeDynStepSize() {

		for (int dimension = 0; dimension < getNumVar(); dimension++) {
			m_covariances[dimension][dimension] = computeCovFor2Preds(dimension, dimension);
		}
		
		
		double sum = 0;
		for (int dimension = 0; dimension < getNumVar(); dimension++) {
			sum += getWeightCov(dimension, dimension);
		}
		m_dynStepLowerBound = 1.0/sum;
		if (m_printGDDebugInformation) System.out.println("DEBUG: DynStepSize lower bound is " + m_dynStepLowerBound);

		// getSettings().getOptGDMaxIter()/m_earlyStopStep = nb of drops
		// TODO 100 Change to variable m_earlyStopStep
//		m_dynStepSizeDrop = Math.pow(m_dynStepLowerBound, 1.0/(getSettings().getOptGDMaxIter()/1000.0-1)); // first 1/10 of iterations used

	}

	
	/**
	 * Generates a zero vector.
	 */
	protected ArrayList<Double> getInitialWeightVector()
	{
		ArrayList<Double> result = new ArrayList<Double>(getNumVar());
		for (int i = 0; i < getNumVar(); i++) {
			result.add(new Double(0.0));
		}
		return result;
	}
	
	/* Returns average covariance between a prediction and true values */
	final protected double getCovForPrediction(int iPred) {
		return m_predCovWithTrue[iPred];
	}
	/** Compute covariances between predictors and true values. With all zero weight vector these are also
	 * initial gradients before first iteration.
	 */
	protected void initPredictorVsTrueValuesCovariances() {

		m_predCovWithTrue = new double[getNumVar()];
	
		// Compute all the covariances for current weights.
		for (int iPred = 0; iPred < getNumVar(); iPred++ ) {
			m_predCovWithTrue[iPred] = computePredVsTrueValueCov(iPred);
		}
	}

	/** Estimate of expected value of covariance for given prediction.
	 * The covariance of this prediction with its true value is returned.
	 */
	private double computePredVsTrueValueCov(int iPred) {
		double[] covs = new double[getNbOfTargets()];
		int nbOfTargets = getNbOfTargets();
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++) {
 			for (int iInstance = 0; iInstance < getNbOfInstances(); iInstance++) {
				double trueVal = getTrueValue(iInstance,iTarget);
				if (isValidValue(trueVal)) // Not a valid true value, rare but happens. Can happen for linear terms.
					//covs[iTarget] += trueVal*predictWithRule(iPred, iInstance,iTarget);
					covs[iTarget] +=
						trueVal*predictWithRule(iPred, iInstance,iTarget);
			}

			covs[iTarget] /= getNbOfInstances();
			if (getSettings().isOptNormalization()) {
				covs[iTarget] /= getNormFactor(iTarget);
			}
		}

		double avgCov = 0;
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++) {
			avgCov += covs[iTarget]/nbOfTargets;
		}
		return avgCov;
	}

	/**
	 * Return the right stored covariance
	 */
	// Only one corner is computed (the other is similar)
	final protected double getWeightCov(int iFirst, int iSecond) {
		int min = Math.min(iFirst, iSecond);
		int max = Math.max(iFirst, iSecond);
		//if (Double.isNaN(m_covariances[min][max][0]))
		if (Double.isNaN(m_covariances[min][max]))
			throw new Error("Asked covariance not yet computed. Something wrong in the covariances in GDProbl.");
		return m_covariances[min][max];
	}

	/** Compute the covariances for this weight/prediction dimension. This means that
	 * we compute all the pairs where this takes part.
	 * Because for covariance
	 * cov(a,b) = cov(b,a) compute only one of them.
	 * @par dimension The dimension for which the covariances are computed
	 */
	private void computeWeightCov(int dimension) {
		// Because of symmetry cov(dimension, b) is already computed if for some earlier phase b was dimension
		// Thus if covariances for b are computed, this does not have to be computed anymore.

		// The weights with index lower than this
		for (int iMin = 0; iMin < dimension; iMin++) {
			// If the covariances for the other part are already computed, this covariance is already
			// computed also. Thus skip this covariance.
			if (!m_isCovComputed[iMin]) {
//				if (!Double.isNaN(m_covariances[iMin][dimension][0]))
//					System.err.println("WARNING: Covariances are recalculated, waste of computation!");
				m_covariances[iMin][dimension] = computeCovFor2Preds(iMin, dimension);
			}
		}

		m_covariances[dimension][dimension] = computeCovFor2Preds(dimension, dimension);
		for (int iMax = dimension+1; iMax < getNumVar(); iMax++) {
			if (!m_isCovComputed[iMax]) {
//				if (!Double.isNaN(m_covariances[iMax][dimension][0]))
//					System.err.println("WARNING: Covariances are recalculated, waste of computation!");
				m_covariances[dimension][iMax] = computeCovFor2Preds(dimension, iMax);
			}
		}
	}

	/**
	 * Compute covariance between two predictions (base learner predictions).
	 * This is a help function for computeWeightCov
	 * NOTE: Second parameter index HAS to be greater or equal to first
	 * @param iPrevious Base learner index
	 * @param iLatter Base learner index. NOTE: iLatter >= iPrevious
	 * @return
	 */
//	private double[] computeCovFor2Preds(int iFirstRule, int iSecondRule) {
	private double computeCovFor2Preds(int iPrevious, int iLatter) {
//		double eka = 0;
		// If one of them is linear term, it has to be the later one
		if (isRuleTerm(iLatter)) {
			// Covariance for 2 rule predictions
			return computeCovFor2Rules(iPrevious, iLatter);
		} else if (isRuleTerm(iPrevious)) {
			// Covariance for rule and linear term
			return computeCovForRuleAndLin(iPrevious, iLatter);
		} else {
			// Covariance for 2 linear terms
			return computeCovFor2Lin(iPrevious, iLatter);
		}
		
/*		// General computation
		int nbOfTargets = getNbOfTargets();
		int nbOfInstances = getNbOfInstances();

		double[] covs = new double[nbOfTargets];

		//TODO first instances, then targets. CHECK if instance is covered, if not, skip
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++) {
			for (int iInstance = 0; iInstance < nbOfInstances; iInstance++) {
				covs[iTarget] += predictWithRule(iPrevious,iInstance,iTarget) *
				                 predictWithRule(iLatter,iInstance,iTarget);

				// The following is commented out because I think it is only a property of resource
				// tracking software. This can't take so much memory.
				// We optimize this function because this is the one that takes most of the time (70%).
				// We should use "predictWithRule" method, but this is slightly faster
//				double firstPred = getPredictions(iFirstRule,iInstance,iTarget);
//				double secondPred = getPredictions(iSecondRule,iInstance,iTarget);
//
//				// Is valid prediction or is not covered?
//				firstPred = Double.isNaN(firstPred) ? 0 : firstPred;
//				secondPred = Double.isNaN(secondPred) ? 0 : secondPred;
//
//				covs[iTarget] += firstPred * secondPred;
			}
			covs[iTarget] /= getNbOfInstances();
			
			if (getSettings().isOptNormalization()) {
				covs[iTarget] /= getNormFactor(iTarget);
			}
		}
		
		double avgCov = 0;
		
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++) {
			avgCov += covs[iTarget]/nbOfTargets;
		}
		return avgCov;
//		return covs;
*/	}

	/** For two linear terms.
	 * @param iPrevious
	 * @param iLatter Rule index. NOTE: iLatter >= iPrevious
	 * @return
	 */
	private double computeCovFor2Lin(int iPrevious, int iLatter) {
		int nbOfInstances = getNbOfInstances();
		int nbOfTargets = getNbOfTargets();
		
		// Go through only those dimensions that linear term is not always zero for
		int iTarget = getLinTargetDim(iPrevious);
		
		if (iTarget != getLinTargetDim(iLatter)) return 0;
		
		// Compute value
		double avgCov = 0;
		
		for (int iInstance = 0; iInstance < nbOfInstances; iInstance++) {
			avgCov += predictWithRule(iPrevious,iInstance,iTarget)*
							predictWithRule(iLatter,iInstance,iTarget);
		}
		avgCov /= (nbOfTargets*getNbOfInstances());
		
		if (getSettings().isOptNormalization()) {
			avgCov /= getNormFactor(iTarget);
		}

		// Store the value to all the places where this descriptive attribute is used.
		//TODO how to mark that these covariances are computed already!!!!
		// In practice takes so little time, that does not matter?
		return avgCov;
	}

	/** For rule and linear term we skip the instances rule does not cover.
	 * @param iRule Rule index.
	 * @param iLinear Linear term index. NOTE: iLinear >= iRule
	 * @return
	 */
	private double computeCovForRuleAndLin(int iRule, int iLinear) {
		int nbOfTargets = getNbOfTargets();
		
		// Go through only those dimensions that linear term is not always zero for
		int iTarget = getLinTargetDim(iLinear);
		
		double avgCov = 0;
		
		// Go through the covered examples always jumping to next covered
		for (int iInstance = getRuleNextCovered(iRule,0);
					iInstance >= 0; iInstance = getRuleNextCovered(iRule,iInstance+1)) {
			avgCov += getPredictionsWhenCovered(iLinear,iInstance,iTarget);
		}

		avgCov *= getPredictionsWhenCovered(iRule,0,iTarget);

		avgCov /= (nbOfTargets*getNbOfInstances());
		
		if (getSettings().isOptNormalization()) {
			avgCov /= getNormFactor(iTarget);
		}
		return avgCov;
	}

	/** For rules the prediction is always the same. We first count: how many
	 * instances both rules cover (prediction != 0). Then the covariance is
	 * pred1*pred2*([nb both rules cover]/[nb of instances])
	 * @param iPrevious
	 * @param iLatter Rule index. NOTE: iLatter >= iPrevious
	 * @return
	 */
	private double computeCovFor2Rules(int iPrevious, int iLatter) {
		BitSet prev = (BitSet) (getRuleCovers(iPrevious).clone());
		BitSet latter = getRuleCovers(iLatter);
		prev.and(latter); // prev is now a AND bitset of both of these

		int nbOfTargets = getNbOfTargets();
		double avgCov = 0;
		
		for (int iTarget = 0; iTarget < nbOfTargets; iTarget++) {
			double cov = 0;
			cov += getPredictionsWhenCovered(iPrevious,0,iTarget)*
					  getPredictionsWhenCovered(iLatter,0,iTarget);
			
			if (getSettings().isOptNormalization()) {
				cov /= getNormFactor(iTarget);
			}
			avgCov += cov/nbOfTargets;
		}
		avgCov *= ((double) prev.cardinality())/getNbOfInstances();
		
		return avgCov;
	}

	/** Returns the real prediction when this rule is used. If the rule does
	 * not give prediction for some target, default rule is used.
	 * ONLY FOR REGRESSION! Classification not implemented.
	 */
	final protected double predictWithRule(int iRule, int iInstance, int iTarget) {
		return isCovered(iRule,iInstance) ? getPredictionsWhenCovered(iRule, iInstance, iTarget) : 0;
	}

	/** Compute the gradients for weights from scratch */
	public void fullGradientComputation(ArrayList<Double> weights) {	
		// Compute all the gradients for current weights.
		for (int iWeight = 0; iWeight < weights.size(); iWeight++ ) {
			m_gradients[iWeight] = getGradient(iWeight, weights);
		}
		//if (getSettings().isOptGDIsDynStepsize()) computeDynamicStepSizeComputation();
	}
	
	/** Compute gradient for the given weight dimension */
	protected double getGradient(int iWeightDim, ArrayList<Double> weights) {

		double gradient = 0;
		switch (getSettings().getOptGDLossFunction()) {
		case Settings.OPT_LOSS_FUNCTIONS_01ERROR:
			//gradient = loss01(trueValue, prediction);
			//break;
		case Settings.OPT_LOSS_FUNCTIONS_HUBER:
		case Settings.OPT_LOSS_FUNCTIONS_RRMSE:
			//gradient = lossHuber(trueValue, prediction);
//			break;
			try {
				throw new Exception("0/1 or Huber loss function not yet implemented for Gradient descent.\n" +
						"Using squared loss.\n");
			} catch(Exception s) {
				s.printStackTrace();
			} //TODO Huber and alpha computing
			//Default case
		case Settings.OPT_LOSS_FUNCTIONS_SQUARED:
		default:
			gradient = gradientSquared(iWeightDim, weights);
			break;
		}

		return gradient;
	}

		
	/**
	 * Squared loss gradient. p. 18 in Friedman & Popescu, 2004
	 * @param iGradWeightDim Weight dimension for which the gradient is computed.
	 * @return Gradient average
	 */
	private double gradientSquared(int iGradWeightDim, ArrayList<Double> weights) {

		double gradient = getCovForPrediction(iGradWeightDim);

		for (int iWeight = 0; iWeight < getNumVar(); iWeight++){
			if (m_isWeightNonZero[iWeight]) {
				gradient -= weights.get(iWeight).doubleValue()
                					* getWeightCov(iWeight,iGradWeightDim);
			}
		}

		return gradient;
	}


	/** Recompute the gradients new iteration.
	 * This is lot of faster than computing everything from the scratch.
	 * @param changedWeightIndex The index of weights that have changed. Only these affect the change in the new gradient.
	 * 						  Friedman&Popescu p.18
	 */
	final protected void modifyGradients(int[] changedWeightIndex, ArrayList<Double> weights) {

//		switch (getSettings().getOptGDLossFunction()) {
//		case Settings.OPT_LOSS_FUNCTIONS_01ERROR:
//		case Settings.OPT_LOSS_FUNCTIONS_HUBER:
//		case Settings.OPT_LOSS_FUNCTIONS_RRMSE:
//			//TODO Huber and alpha computing
//			//Default case
//		case Settings.OPT_LOSS_FUNCTIONS_SQUARED:
//		default:
			modifyGradientSquared(changedWeightIndex);
//			break;
//		}
	}


	/** Recomputation of gradients for least squares loss function */
	public void modifyGradientSquared(int[] iChangedWeights) {

		// New gradients are computed with the old gradients.
		// Only the changed gradients are stored here
		// However since we use affective gradients which are not YET changed,
		// we can use directly them.
		
		// Could store only the changes to the gradients and do the actual change afterwards in another for loop
		double[] oldGradsOfChanged = new double[iChangedWeights.length];

		for (int iCopy = 0; iCopy < iChangedWeights.length; iCopy++) {
			oldGradsOfChanged[iCopy] = m_gradients[iChangedWeights[iCopy]];
		} 
				
		// The next few lines take about 80% of processor time. 
		
		/** If we reach linear terms with the first loop, we can skip
		 * most of the terms in the second loop (because cov is 0)
		 */
		boolean firstLinearTermReached = false;
		int nbOfTargs = getNbOfTargets();
		int nbOfChanged = iChangedWeights.length; // does this make more effective?
		int nbOfGrads = m_gradients.length;
		
		// Index over the other gradients that are affecting (THE WEIGHTS THAT ALTERED)
		for (int iiAffecting = 0; iiAffecting < nbOfChanged; iiAffecting++) {	
			if (!firstLinearTermReached && 
					!isRuleTerm(iChangedWeights[iiAffecting]))
				firstLinearTermReached = true;

			boolean secondLinearTermReached = false;
			double stepAmount = m_stepSize*oldGradsOfChanged[iiAffecting];
			// Index over the gradient we are changing (ALL GRADIENTS)
			for (int iWeightChange = 0; iWeightChange < nbOfGrads; iWeightChange++) {
				m_gradients[iWeightChange] -= getWeightCov(iChangedWeights[iiAffecting],iWeightChange)*stepAmount;
				
				if (firstLinearTermReached){
					if (secondLinearTermReached) {		
					// Skip to only those linear terms, for which cov(iWeightChange,iChangedWeights[iiAffecting])!= 0
						iWeightChange += nbOfTargs-1;					
					} else if (!isRuleTerm(iWeightChange)) {
						// Jump to the first linear term that has covariance nonzero with iiAffecting.
						// This is linear term number linTargetDim(). However this is already the first linear term and there is already
						// ++ because of for loop -> -2
						iWeightChange += (getLinTargetDim(iChangedWeights[iiAffecting])+nbOfTargs-1)%nbOfTargs;			

						secondLinearTermReached = true;
					}
				}

			}
		}
	}
	
	

	/** Return the gradients with maximum absolute value. For the weights we want to change
	 * The function is assuming (if max allowed rule nb is set) that index 0 of gradients includes
	 * the "default rule" which is not counted.
	 * @param nbOfIterations Used for detecting if weight is banned (not usually used)*/
	public int[] getMaxGradients(int nbOfIterations) {
		/** Maximum number of nonzero elements. Can we add more? */
		int maxElements = getSettings().getOptGDMaxNbWeights();
		boolean maxNbOfWeightReached = false;
		if (maxElements > 0 && m_nbOfNonZeroRules >= maxElements) {
			// If maximum number of nonzero elements is reached,
			// search for the biggest one among the nonzero weights

			maxNbOfWeightReached = true;
		}

		double maxGrad = 0; // Maximum gradient
		for (int iGrad = 0; iGrad < m_gradients.length; iGrad++) {
			if (m_bannedWeights != null && m_bannedWeights[iGrad] > nbOfIterations) {
				// The weight is banned
				continue;
			}
			// We choose the gradient if max nb of weights is reached only if it is
			// already nonzero
			if (Math.abs(m_gradients[iGrad]) > maxGrad
					&& (!maxNbOfWeightReached || m_isWeightNonZero[iGrad] || iGrad == 0))
				maxGrad = Math.abs(m_gradients[iGrad]);
		}

		ArrayList<Integer> iMaxGradients = new ArrayList<Integer>();

		// The least allowed item.
		double minAllowed = getSettings().getOptGDGradTreshold() * maxGrad;

		// Copy all the items that are greater to a returned array
		for (int iCopy = 0; iCopy < m_gradients.length; iCopy++) {
			if (m_bannedWeights != null && m_bannedWeights[iCopy] > nbOfIterations) {
				// The weight is banned
				continue;
			}
			if (Math.abs(m_gradients[iCopy]) >= minAllowed
					&& (!maxNbOfWeightReached || m_isWeightNonZero[iCopy]) || iCopy == 0) {
				iMaxGradients.add(iCopy);
				// If the treshold is 1, we only want to change one dimension at time
				// Default rule is not counted
				if (getSettings().getOptGDGradTreshold() == 1.0 && iCopy != 0)
					break;
			}
		}

		// If we have maximum amount of rules and treshold value is not 1, we may be
		// returning too many gradients and thus having too many rules. Thus select
		// only the ones needed.
		// However if maximum number of nonzero weights is already reached, we can't
		// take too much of these.
		if (maxElements > 0 && !maxNbOfWeightReached && getSettings().getOptGDGradTreshold() < 1.0) {

			// Gradients that are already nonzero. (count also default rule, because it is
			// not counted in maxgradients)
			int nbOfOldGrads = 0;

			// Count how many old ones we have in the current gradients
			for (int iGrad = 0; iGrad < iMaxGradients.size(); iGrad++) {
				if (m_isWeightNonZero[iMaxGradients.get(iGrad)] || iMaxGradients.get(iGrad) == 0) {
					nbOfOldGrads++;
				}
			}

			// if number of new rules in gradients is greater than maximum number of
			// allowed elements, we have to get rid of some of them.
			int nbOfAllowedNewGradients = maxElements - m_nbOfNonZeroRules;
			if (nbOfAllowedNewGradients < iMaxGradients.size()-nbOfOldGrads) {

				//ArrayList is slow for inserts in the middle.
				//LinkedList should be fast for iterating thru and inserting.
				LinkedList<Integer> iAllowedNewMaxGradients = new LinkedList<Integer>();

				for (int iGrad = 0; iGrad < iMaxGradients.size(); iGrad++) {

					// If the gradient is new.
					if (!m_isWeightNonZero[iMaxGradients.get(iGrad)] && iMaxGradients.get(iGrad) != 0) {

						// Insertion sort to iAllowed by the greatness of gradient
						ListIterator<Integer> iAllowed  = iAllowedNewMaxGradients.listIterator();
						while (iAllowed.hasNext()) {
							// If we already went too far
							if (Math.abs(m_gradients[iAllowed.next()])
							    <  Math.abs(m_gradients[iMaxGradients.get(iGrad)])) {
								iAllowed.previous();
								break;
							}
						}

						// We should now be on the insertion place
						iAllowed.add(iMaxGradients.get(iGrad));
						iMaxGradients.remove(iGrad);
						iGrad--;

						// To keep the list smaller
						if (iAllowedNewMaxGradients.size() > nbOfAllowedNewGradients) {
							iAllowedNewMaxGradients.removeLast();
						}
					}
				}

				// now we have in sorted order gradients in iAllowedNewMaxGradients. Let us add
				// only so much that is allowed
				ListIterator<Integer> iList  = iAllowedNewMaxGradients.listIterator();

				for (int addedElements = 0;
					addedElements < nbOfAllowedNewGradients; addedElements++ ) {
					iMaxGradients.add(iList.next());
				}

			}


		}

		// Efficient enough
		int[] iMaxGradientsArray = new int[iMaxGradients.size()];
		for (int iCopy = 0; iCopy < iMaxGradients.size(); iCopy++) {
			iMaxGradientsArray[iCopy]=iMaxGradients.get(iCopy);
		}
		return iMaxGradientsArray;
	}

	/**
	 * Compute the change of target weight because of the gradient
	 * @param iTargetWeight Weight index we want to change.
	 */
	final public double howMuchWeightChanges(int iTargetWeight) {
//		return m_stepSize* m_gradients[iTargetWeight];
		return m_stepSize* m_gradients[iTargetWeight];
	}

	/** Compute the needed covariances for the weight. Only called
	 * if we are going to change the weight. */
	public void computeCovariancesIfNeeded(int iWeight) {
		if (!m_isCovComputed[iWeight]){
			computeWeightCov(iWeight);
			m_isCovComputed[iWeight] = true; // Mark the covariance computed
		}
		// For multiple runs for same predictions, may not be same as before
		if (!m_isWeightNonZero[iWeight]) {
			m_isWeightNonZero[iWeight] = true;

			// Do not count first default rule as a nonzero weight.
			if (iWeight !=0) {
				m_nbOfNonZeroRules++;
			}
		}
	}
	/** In case of oscillation, make the step size shorter
	 * We should be changing the step size just enough not to prevent further oscillation */
	final public void dropStepSize(double amount) {
		if (amount >=1)
			System.err.println("Something wrong with dropStepSize. Argument >= 1.");

		//m_stepSize *= 0.1;
		m_stepSize *= amount; // We make the new step size a little smaller than is limit (because of rounding mistakes)
		
//		m_MaxStepSize = m_stepSize; //DEBUG TODO
	}

	/** List of old fitnesses for plateau detection (andy for debugging) */
	//protected ArrayList<Double> m_oldFitnesses;
	protected double m_minFitness;
	/** Weights when the Fitness was minimum */
	protected ArrayList<Double> m_minFitWeights;


	/** Returns best fitness so far. */
	public double getBestFitness() {
		return m_minFitness;
	}

	/** Early stopping is needed if the error rate is too much bigger than the smallest error rate
	 * we have had. */

	public boolean isEarlyStop(ArrayList<Double> weights) {
		double newFitness = m_earlyStopProbl.calcFitness(weights);

		if (newFitness < m_minFitness) {
			m_minFitness = newFitness;
			// Copy the weights
			for (int iWeight = 0; iWeight < weights.size(); iWeight++)
			{
				m_minFitWeights.set(iWeight, weights.get(iWeight).doubleValue());
			}
		}


		boolean stop = false;

		if (newFitness > getSettings().getOptGDEarlyStopTreshold()*m_minFitness) {
			stop = true;
			if (m_printGDDebugInformation)
				System.out.println("\nGD: Independent test set error increase detected - overfitting.\n");
		}

		return stop;
	}

	/** Restore the weight with minimum fitness. */
	public void restoreBestWeight(ArrayList<Double> targetWeights) {
		for (int iWeight = 0; iWeight < targetWeights.size(); iWeight++)
		{
			targetWeights.set(iWeight, m_minFitWeights.get(iWeight).doubleValue());
		}
	}

	//static ArrayList<Integer> turha = new ArrayList<Integer>();
	/** Return a maximum tree depth based on RulefFit (Friedman, Popescu. 2005) function used.
	 * Function is Pr(nbOfLeaves) = exp(-nbOfLeaves/(avgDepth/2))/(avgDepth-2).
	 * @param unifRand Random uniform number on which the number is based on.
	 * @param avgDepth Average depth (L in RuleFit).
	 * @return Random depth
	 */
	public static int randDepthWighExponentialDistribution(double unifRand, int avgDepth) {
		// Friedman is computing the number of terminal leaves with exponential distribution
		// To get the limit depth for this we need to compute
		// depth =  ceil(log(2+floor(-(L-2)/(lg(L-2))lg(unifRand)))) +1
		// Here L is the average and 2+floor(-(L-2)/(lg(L-2))lg(unifRand)) is the number of terminal leaves
		// However in Clus depth seems to be so that only root is on depth 0, and depth 1 has two leaves. Thus
		// we leave the last +1 out and compute ceil(log(2+floor(-(L-2)/(lg(L-2))lg(unifRand))))
		// Also we do not take the floor for computing amount of terminal nodes because this reduces the average
		// depth too much. Without it the average depth for value 3 is in reality 2.7
		int maxDepths = 0;

		if (unifRand == 0.0) {
			// This means that the limit depth should be infinite. Can't take logarithm of this
			maxDepths = -1;
		} else {
			int avgNbLeaves = (int)(Math.pow(2,avgDepth)); // The root depth is 0, thus not avgDepth-1.
			//int terminalNodes = (int) (2+Math.floor((double)(2-avgNbLeaves)/Math.log(avgNbLeaves-2)*Math.log(unifRand)));
			double terminalNodes = 2+(double)(2-avgNbLeaves)/Math.log(avgNbLeaves-2)*Math.log(unifRand);
			maxDepths = (int) Math.ceil(Math.log(terminalNodes)/Math.log(2.0)); // Binary logarithm. Root
		}
		//turha.add(new Integer(maxDepths));
		return maxDepths;
	}	
	
	/** Print gradients to output file. */
	public void printGradientsToFile(int iterNro, PrintWriter wrt) {
		if (!m_printGDDebugInformation)
			return;

		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		wrt.print("Iteration " + iterNro +":");
		for (int i = 0; i < m_gradients.length; i++) {
			wrt.print(fr.format((double)m_gradients[i])+"\t");
		}
		wrt.print("\n");
	}

	
}

/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2008 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

/*
 * Created on 27.11.2008
 */

package si.ijs.kt.clus.util.tools.optimization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsRules.OptimizationGDMTCombineGradient;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


/**
 * Class for gradient descent optimization.
 * 
 * @author Timo Aho
 *
 */
public class GDAlgorithm extends OptimizationAlgorithm {

    /** Includes the information about settings of optimization. Has all the helping functions and variables. */
    private GDProblem m_GDProbl;

    /** Current weights */
    protected ArrayList<Double> m_weights;

    /** After how many steps check if early stopping is ok already */
    protected int m_earlyStopStep;

    /** After finding the early stop, how many times we have tried to reduce the step size */
    protected int m_earlyStopStepsizeReducedNb;


    /**
     * Constructor for classification and regression optimization.
     * 
     * @param stat_mgr
     *        Statistics
     * @param dataInformation
     *        The true values and predictions for the instances. These are used by OptimProbl.
     *        The optimization procedure is based on this data information.
     *        Warning: The parameter may be modified!
     */
    public GDAlgorithm(ClusStatManager stat_mgr, OptimizationProblem.OptimizationParameter dataInformation, ClusRuleSet rset) {
        super(stat_mgr);
        m_GDProbl = new GDProblem(stat_mgr, dataInformation);
        initGDForNewRunWithSamePredictions();
        m_earlyStopStep = 100;

        // If you want to check these, put early stop amount to 0
        if (GDProblem.m_printGDDebugInformation) {
            String fname = getSettings().getData().getDataFile();

            PrintWriter wrt_pred = null;
            PrintWriter wrt_true = null;
            try {
                wrt_pred = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname + ".gd-pred")));
                wrt_true = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname + ".gd-true")));
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }

            m_GDProbl.printPredictionsToFile(wrt_pred);
            wrt_pred.close();
            m_GDProbl.printTrueValuesToFile(wrt_true);
            wrt_true.close();
        }

    }


    /**
     * Initialize GD optimization for new run with same predictions and true values
     * This can be used if some parameters change. Thus we e.g. do not compute covariances
     * again
     */
    public void initGDForNewRunWithSamePredictions() {
        m_GDProbl.initGDForNewRunWithSamePredictions();

        m_weights = m_GDProbl.getInitialWeightVector();

        // Oscillation detection
        m_prevChange = null;
        m_iPrevDimension = null;
        m_iNewDimension = null;
        m_newChange = null;
        m_minStepSizeReduction = 1;

        m_earlyStopStepsizeReducedNb = 0;

        // If weigths can be banned, oscillation is stored
        if (m_GDProbl.m_bannedWeights != null)
            m_iOscillatingWeights = new ArrayList<Integer>();
        else
            m_iOscillatingWeights = null;

    }


    /**
     * Optimize the weights for the given data with gradient descent algorithm.
     * This is the algorithm by Friedman, Popescu 2004
     * 
     * @throws Exception
     */
    @Override
    public ArrayList<Double> optimize() {
        ClusLogger.info("Gradient descent: Optimizing rule weights (" + getSettings().getRules().getOptGDMaxIter() + ") ");

        PrintWriter wrt_log = null;

        if (GDProblem.m_printGDDebugInformation) {
            try {
                wrt_log = new PrintWriter(new OutputStreamWriter(new FileOutputStream("gradDesc.log")));
            }
            catch (Exception e) {
                e.printStackTrace();
                ClusLogger.severe("Log file could not be opened. Logging omitted.");
            }
        }

        if (m_GDProbl.isClassifTask()) {
            try {
                throw new ClusException("Classification not yeat implemented for gradient descent. Skipping the optimization.");
            }
            catch (Exception s) {
                s.printStackTrace();
            }
            return null;
        }
        // Compute initial gradients
        m_GDProbl.fullGradientComputation(m_weights);

        int nbOfIterations = 0;
        while (nbOfIterations < getSettings().getRules().getOptGDMaxIter()) {

            if (nbOfIterations % (Math.ceil(getSettings().getRules().getOptGDMaxIter() / 50.0)) == 0) {
                ClusLogger.finer(String.format("%s %%", nbOfIterations * 100 / Math.ceil(getSettings().getRules().getOptGDMaxIter())));
            }
            if (nbOfIterations % m_earlyStopStep == 0) {

                if (getSettings().getRules().getOptGDEarlyStopAmount() > 0 && m_GDProbl.isEarlyStop(m_weights)) {
                    if (GDProblem.m_printGDDebugInformation) {
                        wrt_log.println("Increase in test fitness. Reducing step size or stopping.");
                    }

                    ClusLogger.fine("Overfitting after " + nbOfIterations + " iterations.");

                    if (!getSettings().getRules().isOptGDIsDynStepsize() && m_earlyStopStepsizeReducedNb < getSettings().getRules().getOptGDNbOfStepSizeReduce()) {
                        m_earlyStopStepsizeReducedNb++;
                        m_GDProbl.dropStepSize(0.1); // Drop stepsize to tenth.
                        m_GDProbl.restoreBestWeight(m_weights); // restoring the weight with minimum fitness
                        m_GDProbl.fullGradientComputation(m_weights);

                        ClusLogger.fine("Reducing step, continuing.");
                    }
                    else { // If dynamic step size, stop always
                        ClusLogger.info("Stopping.");

                        if (GDProblem.m_printGDDebugInformation) {
                            wrt_log.println("Early stopping detected after " + nbOfIterations + " iterations.");
                        }
                        break;
                    }
                }
            }

            // Print
            OutputLog(nbOfIterations, wrt_log); // Weights

            // 3)
            // Search for maximum gradients
            int[] maxGradients = m_GDProbl.getMaxGradients(nbOfIterations);

            boolean oscillation = false;

            // For detecting oscillation
            storeGradientsForOscillation(maxGradients);

            // b) // Take the gradient step on the peak dimension

            // First compute the change values and detect possible oscillation

            // Change of the weight according to this gradient.
            double[] valueChange = new double[maxGradients.length];

            boolean debugPrint = getSettings().getGeneral().getVerbose() > 0 && false; // DEBUG

            if (debugPrint) {
                System.out.println("\nDEBUG: Computing covariances, total " + maxGradients.length);
            }

            for (int iiGradient = 0; iiGradient < maxGradients.length; iiGradient++) {
                if (debugPrint) {
                    System.out.print(iiGradient % 10);
                }
                // c) We are changing the value of the weight. Let's compute the covariance if it is not yet done
                m_GDProbl.computeCovariancesIfNeeded(maxGradients[iiGradient]);

                // Change of the weight according to this gradient.
                valueChange[iiGradient] = m_GDProbl.howMuchWeightChanges(maxGradients[iiGradient]);

                // For detecting oscillation. If oscillation is already true, do not change it to false.
                if (nbOfIterations < 100) {
                    oscillation = (detectOscillation(iiGradient, valueChange[iiGradient]) || oscillation);
                }
            }
            if (debugPrint) {
                System.out.println("\nDEBUG: Computing covariances ended");
            }
            // If oscillation was detected we do not take real steps.
            // we reduce the step size until the new step is smaller than the first one.
            // It should be smaller because otherwise we are going even further from the optimal point.
            if (oscillation && !getSettings().getRules().isOptGDIsDynStepsize()) {
                if (GDProblem.m_printGDDebugInformation) {
                    wrt_log.println("Detected oscillation, reducing step size of: " + m_GDProbl.m_stepSize);
                }

                if (debugPrint) {
                    ClusLogger.info("DEBUG: Detected oscillation on iteration " + nbOfIterations + ", reducing step size of: " + m_GDProbl.m_stepSize);
                }
                // System.exit(1); //DEBUG
                // For MaxLoss combination the oscillation may be because we are finding some local
                // optimum point for some weight and it is the most significant. Let us put this weight
                // to a banned list for some iterations
                if (nbOfIterations > 10 && (getSettings().getRules().getOptGDMTGradientCombine().equals(OptimizationGDMTCombineGradient.MaxLoss) || getSettings().getRules().getOptGDMTGradientCombine().equals(OptimizationGDMTCombineGradient.MaxLossFast))) {
                    putOscillatingWeightsToBan(nbOfIterations);
                }
                else {
                    // This is needed if we are changing more than one dimension at time.
                    // The step size is made for the biggest oscillation and thus some of the other oscillating ones
                    // never come back to sensible values.
                    reversePreviousStep(); // Reverse the previous step. We also skip this step.
                    reduceStepSizeDueOscillation();
                }
                continue; // Start next iteration
            }
            else {
                // Store the current data for use in the next iterations.
                if (nbOfIterations < 100) {
                    storeTheOscillationData();
                }
            }

            // After it is sure no oscillation is detected, make the changes
            for (int iiGradient = 0; iiGradient < maxGradients.length; iiGradient++) {
                m_weights.set(maxGradients[iiGradient], m_weights.get(maxGradients[iiGradient]).doubleValue() + valueChange[iiGradient]);
            }

            // d) compute the gradients again for the weights that changed
            // This must not affect the computation during this iteration
            m_GDProbl.modifyGradients(maxGradients, m_weights);

            nbOfIterations++;
        } // While

        ClusLogger.info("Done!");

        if (getSettings().getRules().getOptGDEarlyStopAmount() > 0) {
            m_GDProbl.isEarlyStop(m_weights); // Check if current weights have better fitness than the best so far
            m_GDProbl.restoreBestWeight(m_weights); // Result are the weights with best fitness
        }

        if (GDProblem.m_printGDDebugInformation) {
            wrt_log.println("The result of optimization");
        }
        OutputLog(nbOfIterations, wrt_log);
        if (GDProblem.m_printGDDebugInformation) {
            wrt_log.close();
        }
        // System.err.println("CHANGING THE WEIGHTS< REMOVE THISE");
        // for (int i=0; i< m_weights.size(); i++)
        // m_weights.set(i,1.0); // default rule

        return m_weights;
    }

    /** If weight banning is in use, store the oscillation dimensions here */
    private ArrayList<Integer> m_iOscillatingWeights;


    /**
     * Puts oscillating weights to ban, so that we can continue on the rest of weights.
     * This is used only if for maxLoss combining something happens.
     */
    private void putOscillatingWeightsToBan(int iterationNb) {
        for (int iWeight = 0; iWeight < m_iOscillatingWeights.size(); iWeight++) {
            m_GDProbl.m_bannedWeights[m_iOscillatingWeights.get(iWeight)] = iterationNb + 50;
        }
        m_iOscillatingWeights.clear();
    }

    /** Maximum step size reduction found on this iteration */
    private double m_minStepSizeReduction;


    /** Reduce the step size. Take the biggest needed reduction and do that */
    private void reduceStepSizeDueOscillation() {
        // System.err.print("Dropping the step size.\n");

        // We are lowering the step size just enough to prevent further oscillation
        // We make the new step size a little smaller than is limit (because of rounding mistakes)
        m_GDProbl.dropStepSize(m_minStepSizeReduction * 0.99);
        m_minStepSizeReduction = 1; // Step done, reset this now
    }

    /** Information about the previous change for oscillation warning. The change value */
    private double[] m_prevChange;
    private double[] m_newChange;

    /** Information about the previous change for oscillation warning. The changed dimension. */
    private int[] m_iPrevDimension;
    private int[] m_iNewDimension;


    /** A iteration has been done and oscillation not detected - store the previous changes */
    private void storeTheOscillationData() {
        if (getSettings().getRules().isOptGDIsDynStepsize())
            return;
        m_prevChange = m_newChange.clone();
        m_iPrevDimension = m_iNewDimension.clone();
    }


    /**
     * Reverse previous (not this) step if oscillation found. Thus the state should be like it was before.
     * This has to be called before storeTheOscillationData.
     */
    private void reversePreviousStep() {
        // First reverse the weights
        for (int iiGradient = 0; iiGradient < m_iPrevDimension.length; iiGradient++) {
            m_weights.set(m_iPrevDimension[iiGradient], m_weights.get(m_iPrevDimension[iiGradient]).doubleValue() - m_prevChange[iiGradient]);
        }

        // Compute the gradients again. This should no be done so often that this costs very much. Thus we
        // Can compute them again from scratch.
        m_GDProbl.fullGradientComputation(m_weights);

        // Empty the oscillation control parameters (previous step was never done)
        m_prevChange = null;
        m_iPrevDimension = null;

        // Also this has to be cleared.
        if (m_iOscillatingWeights != null)
            m_iOscillatingWeights.clear();
    }


    /**
     * Stores the used gradients for oscillation detection later.
     * Also initialization tasks for oscillation.
     */
    private void storeGradientsForOscillation(int[] maxGradients) {
        if (getSettings().getRules().isOptGDIsDynStepsize())
            return;
        m_iNewDimension = maxGradients.clone();
        m_newChange = new double[maxGradients.length];
    }


    /**
     * Oscillation is detected if same dimension is changed such that
     * the direction is different and the new step is bigger than previous.
     * 
     * @param iiNewChange
     *        The index of MaxGradients/m_iNewDimension we are going on.
     * @param valueChange
     *        How much the weight is going to be changed
     * @return If oscillation was detected.
     */
    private boolean detectOscillation(int iiNewChange, double valueChange) {
        if (getSettings().getRules().isOptGDIsDynStepsize())
            return false;
        boolean detectOscillation = false;

        // Store the value for the change, dimensions have already been stored
        m_newChange[iiNewChange] = valueChange;

        // Go through the previous changes and check if some of them is for same dimension
        for (int iiPrevChange = 0; m_prevChange != null && iiPrevChange < m_prevChange.length; iiPrevChange++) {

            // Is the modified dimension same as before
            if (m_iPrevDimension[iiPrevChange] == m_iNewDimension[iiNewChange]) {
                // Does the change tell us about oscillation

                if (valueChange * m_prevChange[iiPrevChange] < 0 && Math.abs(valueChange) > Math.abs(m_prevChange[iiPrevChange])) {

                    // System.err.println("DEBUG: Found out oscillation for weight " + m_iPrevDimension[iiPrevChange] +
                    // " that was nb " +iiPrevChange + " last iteration and "+ iiNewChange + " now.");
                    // OSCILLATION DETECTED
                    if (m_iOscillatingWeights != null) {
                        m_iOscillatingWeights.add(m_iPrevDimension[iiPrevChange]);
                    }

                    // System.err.print("\nLikely oscillation detected in GDAlg.\n The dimension "
                    // + m_iPrevDimension[iiPrevChange]
                    // + " was changed consequently with change values: " + m_prevChange[iiPrevChange]
                    // + " and " + valueChange + ".\n");

                    // How much we need step size reduction
                    double needReduction = Math.abs(m_prevChange[iiPrevChange]) / Math.abs(valueChange);

                    // If the reduction is greater than any of the previous on this iteration, do it.
                    if (needReduction < m_minStepSizeReduction)
                        m_minStepSizeReduction = needReduction;

                    detectOscillation = true;
                    // System.exit(1);
                }

                break; // The dimension was found. do not check the latter ones.
            }
        }

        return detectOscillation;
    }


    /** Print the current weights output file. */
    public void OutputLog(int iterNro, PrintWriter wrt) {
        if (!GDProblem.m_printGDDebugInformation)
            return;

        ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        double trainingFitness = m_GDProbl.calcFitness(m_weights);
        double testFitness = 0;
        if (getSettings().getRules().getOptGDEarlyStopAmount() > 0)
            testFitness = m_GDProbl.m_earlyStopProbl.calcFitness(m_weights);

        wrt.print("Iteration " + iterNro + " ");
        if (getSettings().getRules().isOptGDIsDynStepsize())
            wrt.print("Step size: " + m_GDProbl.m_stepSize + " ");
        wrt.print("(" + fr.format(trainingFitness) + ", " + fr.format(testFitness) + "): ");
        // else
        // wrt.print("Iteration " + iterNro + ": ");
        for (int i = 0; i < m_weights.size(); i++) {
            wrt.print(fr.format((double) m_weights.get(i)) + "\t");
        }
        wrt.print("\n");
        // m_GDProbl.printGradientsToFile(nbOfIterations, wrt_log); //gradients //DEBUG
    }


    public double getBestFitness() {
        return m_GDProbl.getBestFitness();
    }


    @Override
    public void postProcess(ClusRuleSet rset) {
        m_GDProbl.changeRuleSetToUndoNormNormalization(rset);
    }
}


package clus.util.tools.optimization.sls;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.function.Function;

import clus.algo.rules.ClusRule;
import clus.algo.rules.ClusRuleSet;
import clus.algo.rules.Parallel;
import clus.algo.rules.Parallel.Operation;
import clus.algo.rules.probabilistic.ClusRuleProbabilisticRuleSetInduce;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.statistic.ClusStatistic;
import clus.util.ClusRandom;


public class OptSmoothLocalSearch {

    int m_maxRulesNb;
    Random m_randGen;
    ClusStatManager statManager;
    HashSet<Integer> A;// biased rules indices
    ClusRun m_mainClusRun;
    ClusRuleProbabilisticRuleSetInduce clusRuleProbabilisticRuleSetInduce;

    double[] estimates;
    double errorMargin;
    ClusRuleSet initialRuleSet;
    Function<ClusRuleSet, Double> objectiveFunction;
    ArrayList<Integer> m_indices = new ArrayList<Integer>();


    public OptSmoothLocalSearch(Integer m_maxRulesNb, Random m_randGen, ClusStatManager statManager, ClusRun m_mainClusRun, ClusRuleProbabilisticRuleSetInduce clusRuleProbabilisticRuleSetInduce) {
        this.m_maxRulesNb = m_maxRulesNb;
        this.m_randGen = m_randGen;
        this.statManager = statManager;
        this.m_mainClusRun = m_mainClusRun;
        this.clusRuleProbabilisticRuleSetInduce = clusRuleProbabilisticRuleSetInduce;
    }


    /**
     * Smooth Local Search optimization
     * 
     * @param initialRuleSet
     * @param delta
     * @param deltaPrime
     * @return
     */

    public ClusRuleSet SmoothLocalSearch(ClusRuleSet initialRuleSet, double delta, double deltaPrime, Function<ClusRuleSet, Double> objectiveFunction) {
        //System.out.println("Smooth Local Search optimization: started" );

        this.initialRuleSet = initialRuleSet;
        this.objectiveFunction = objectiveFunction;

        // prepare shuffle index

        m_indices = new ArrayList<Integer>();
        for (int i = 0; i < initialRuleSet.getModelSize(); i++)
            m_indices.add(i);
        A = new HashSet<Integer>();

        m_maxRulesNb = Math.min(m_indices.size(), m_maxRulesNb);

        // estimate of the scale of objective f
        // calculate tresholds only once
        ClusRuleSet rndSet = randomSampleWithBias(0.5); // probabilityBiased = 0.5; delta = 0

        double OPT = objectiveFunction.apply(rndSet);
        double probabilityBiased = (1 + delta) / 2;
        double probabilityPrimeBiased = (1 + deltaPrime) / 2;

        // error margin for estimates calculations
        errorMargin = (1 / Math.pow(initialRuleSet.getModelSize(), 2)) * OPT;
        estimates = new double[initialRuleSet.getModelSize()];

        long ecnt = 0, fcnt = 0, rcnt = 0;
        int sth = 1;

        boolean dowork = true;

        while (dowork) {
            //if (ecnt%sth==0 || fcnt%sth==0 || rcnt%sth==0) System.out.println(ecnt + " " + fcnt + " " + rcnt);

            //			System.out.println("SLS: Calculating estimates");
            //estimates = calculateEstimates(probabilityBiased);

            //			for(int i=0;i<estimates.length;i++) estimates[i] = 0;
            //			
            //			long startTime = System.currentTimeMillis();
            //			calculateEstimates(probabilityBiased);
            //			long estimatedTime = System.currentTimeMillis() - startTime; System.out.println("Time 1:" + estimatedTime);
            //			double sum = 0;
            //			for(int i=0;i<estimates.length;i++) sum+=estimates[i];
            //			System.out.println("SUM 1: " + sum);
            //			
            //			
            //			for(int i=0;i<estimates.length;i++) estimates[i] = 0;

            //			long startTime = System.currentTimeMillis();
            calculateEstimatesParallel(probabilityBiased);
            //			long estimatedTime = System.currentTimeMillis() - startTime; System.out.println("Time estimates:" + estimatedTime);
            //			sum = 0;
            //			for(int i=0;i<estimates.length;i++) sum+=estimates[i];
            //			System.out.println("SUM 2: " + sum);

            ecnt++;

            //			System.out.println("SLS: Finding biased rules");
            if (findBiasedRules()) {
                fcnt++;
                continue; // go recalculate estimates
            }

            //			System.out.println("SLS: Removing bad rules"); 
            if (removeBadRules()) {
                rcnt++;
                continue; // go recalculate estimates
            }

            dowork = false; // we have finished
        } // while block

        // return a random subset with bias deltaPrime on A
        rndSet = randomSampleWithBias(probabilityPrimeBiased);

        //if (ecnt%sth==0 || fcnt%sth==0 || rcnt%sth==0) System.out.println(ecnt + " " + fcnt + " " + rcnt);

        //System.out.println("Smooth local search optimization: finished");
        return rndSet;
    }


    private boolean removeBadRules() {
        for (int rule : A) {
            if (estimates[rule] < -2 * errorMargin) {
                // remove rule from biased set
                A.remove(rule);

                // go calculate estimates again
                return true;
            }
        }
        return false; // no new estimate calculation is needed
    }


    private boolean findBiasedRules() {
        for (int rule = 0; rule < initialRuleSet.getModelSize(); rule++) {
            if (!A.contains(rule) &&
                    estimates[rule] > 2 * errorMargin) {
                // add rule to biased set
                A.add(rule);

                // go calculate estimates again
                return true;
            }
        }
        return false; // no new estimate calculation is needed
    }


    /**
     * Calculate estimates for the SLS
     */
    private void calculateEstimates(double probabilityBiased) {
        ClusRule ruleToCheck;
        for (int rule = 0; rule < initialRuleSet.getModelSize(); rule++) {
            // current rule
            ruleToCheck = initialRuleSet.getRule(rule);

            // calculate estimate
            estimates[rule] = getEstimate(ruleToCheck, probabilityBiased);
        }

        //return estimates;
    }


    private void calculateEstimatesParallel(double probabilityBiased) {

        // Collection of items to process in parallel
        ArrayList<Integer> elems = new ArrayList<Integer>();
        for (int i = 0; i < initialRuleSet.getModelSize(); ++i) {
            elems.add(i);
        }
        
        final double prob = probabilityBiased;
        
        Parallel.For(elems,
                // The operation to perform with each item
                new Operation<Integer>() {

                    public void perform(Integer rule) {

                        //				ClusRuleSet rndSet;
                        //				ClusRuleSet setWithRuleToCheck;
                        //				ClusRuleSet setWithoutRuleToCheck;
                        ClusRule ruleToCheck;
                        //				
                        //		    	// get randomly sampled rule set with bias
                        //				rndSet = randomSampleWithBias(probabilityBiased);
                        //				setWithRuleToCheck = rndSet.cloneRuleSetNonUnique();
                        //				setWithoutRuleToCheck = rndSet.cloneRuleSetNonUnique();
                        //				
                        // current rule
                        //ruleToCheck = initialRuleSet.getRule(rule).cloneRule();
                        ruleToCheck = initialRuleSet.getRule(rule);

                        //				// add / remove ruleToCheck from two sets that we are estimating the quality of (add it uniquely)
                        //				setWithRuleToCheck.add(ruleToCheck);
                        //				setWithoutRuleToCheck.remove(ruleToCheck);
                        //				
                        //				setWithRuleToCheck.setTargetStat(initialRuleSet.m_TargetStat);
                        //				setWithoutRuleToCheck.setTargetStat(initialRuleSet.m_TargetStat);

                        // calculate estimate
                        estimates[rule] = getEstimate(ruleToCheck, prob);

                        //System.out.println("THREAD " + rule);

                    };
                });

    }


    /**
     * Randomly sample rules with bias
     * 
     * @param probabilityBiased
     * @param initialRuleSet2
     * @param initialRuleSet
     * @param a2
     * @param indicesOfBiasedRules
     * @param probabilityBiased
     *        (1+delta)/2 probabilityNormal = 1- (probabilityBiased)
     * @param calculateDefaultRuleAndPrototypes
     *        Should the method calculate default rule and prototypes?
     * @return A randomly sampled rule set with bias towards some of the elements
     */
    ClusRuleSet randomSampleWithBias(double probabilityBiased) {
        ClusRuleSet returnSet = new ClusRuleSet(statManager);
        double prob, probA;
        probA = 1 - probabilityBiased;

        // shuffle indices because we are truncating the iteration
        //Collections.shuffle(m_indices, m_randGen); // important to use the same rnd generator as with everything else, in order to get repeatable results

        int rule;
        for (int index = 0; index < m_maxRulesNb; index++) {
            rule = m_indices.get(index);
            // ask God
            prob = m_randGen.nextDouble();

            if ((A.contains(rule) && prob >= probA) ||
                    (!A.contains(rule) && prob >= probabilityBiased)) {
                //returnSet.add(initialRuleSet.getRule(rule));
                // we will not be using .add() because it checks for uniquiness, which we already have from the initialruleset
                returnSet.getRules().add(initialRuleSet.getRule(rule));
            }
        }

        //		// calculate default rule and prototypes for the sampled set 
        //returnSet.setTargetStat(initialRuleSet.getTargetStat());
        //clusRuleProbabilisticRuleSetInduce.calculateDefaultRuleAndPrototypesForRuleSet(returnSet);

        return returnSet;
    }


    /**
     * Randomly sample from rule set without bias
     * 
     * @param initialRuleSet
     * @return Randomly sampled rule set
     */
    ClusRuleSet randomSampleWithoutBias(ClusRuleSet rules) {
        ClusRuleSet returnSet = new ClusRuleSet(statManager);

        for (int rule = 0; rule < rules.getModelSize(); rule++) {
            // ask God
            if (m_randGen.nextDouble() >= 0.5) {
                //returnSet.add(rules.getRule(rule)); // we will not be using .add() because it checks for uniqueness, which we already have from the initialruleset
                returnSet.getRules().add(rules.getRule(rule));
            }
        }

        //		// calculate default rule and prototypes for the sampled set
        //clusRuleProbabilisticRuleSetInduce.calculateDefaultRuleAndPrototypesForRuleSet(returnSet);

        //returnSet.setTargetStat(initialRuleSet.getTargetStat()); // should this really be here?? the statistic is not correct for the rules that are in the set

        return returnSet;
    }


    /**
     * Calculate expected value for objectiveFunction given error margin and a rule set
     * 
     * @param probabilityBiased
     * @param rules
     *        Rule set to use for estimation calculation
     * @param errorMargin
     *        Treshold that is used as stopping criterion
     * @param objectiveFunction
     *        Objective function to use for value calculation
     * @return Expected value
     */
    double getEstimate(ClusRule ruleToCheck, double probabilityBiased) {
        ArrayList<Double> differenceValuesArray = new ArrayList<Double>(100);

        double sum = 0, tmp = 0, mean = 0;
        double error = errorMargin;
        double counter = 0, tmpSetEstimate1, tmpSetEstimate2;

        int numberOfIterationsBeforeCalculation = 1;

        ClusRuleSet rndSet;
        ClusRuleSet setWithRuleToCheck;
        ClusRuleSet setWithoutRuleToCheck;

        // estimate mean within errorMargin
        while (error >= errorMargin) {
            counter++;

            // get randomly sampled rule set with bias
            rndSet = randomSampleWithBias(probabilityBiased);
            setWithRuleToCheck = rndSet.cloneRuleSetNonUnique();
            setWithoutRuleToCheck = rndSet.cloneRuleSetNonUnique();

            // add / remove ruleToCheck from two sets that we are estimating the quality of (add it uniquely by using ClusRule.add() method)
            setWithRuleToCheck.add(ruleToCheck);
            setWithoutRuleToCheck.remove(ruleToCheck);

         // should this really be here?? the statistic is not correct for the rules that are in the set
            //setWithRuleToCheck.setTargetStat(initialRuleSet.getTargetStat());
            //setWithoutRuleToCheck.setTargetStat(initialRuleSet.getTargetStat());

            // sample random from rules
            //			tmpSetEstimate1 = objectiveFunction.apply(randomSampleWithoutBias(rulesWith)); // randomly sample without bias and estimate quality
            //			tmpSetEstimate2 = objectiveFunction.apply(randomSampleWithoutBias(rulesWithout));
            tmpSetEstimate1 = objectiveFunction.apply(setWithRuleToCheck); // estimate quality
            tmpSetEstimate2 = objectiveFunction.apply(setWithoutRuleToCheck);

            // get difference in objective function values
            tmp = tmpSetEstimate1 - tmpSetEstimate2;
            sum += tmp;
            differenceValuesArray.add(tmp);

            // calculate mean and SD every K iterations
            if (counter % numberOfIterationsBeforeCalculation == 0) {
                mean = sum / counter;
                tmp = 0;
                for (double d : differenceValuesArray) {
                    tmp += Math.pow((mean - d), 2);
                }

                tmp = Math.sqrt((1 / (counter - 1)) * tmp); // this is SD

                error = tmp / Math.sqrt(counter); // this is Std Error

                //System.out.println(Math.abs(error-errorMargin));

                //				// try to reset recalculation iteration interval so that we dont iterate forever
                //				numberOfIterationsBeforeCalculation = Math.max(
                //						1, 
                //						(int) ((numberOfIterationsBeforeCalculation / counter) * numberOfIterationsBeforeCalculation)
                //						);
                //				if (counter >numberOfIterationsBeforeCalculation) 
                //					System.err.println(counter);
            }
        }

        return mean;
    }
}

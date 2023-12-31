/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
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
 * Created on 2006.3.29
 */

package si.ijs.kt.clus.util.tools.optimization.de;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;
import si.ijs.kt.clus.util.tools.optimization.OptimizationAlgorithm;
import si.ijs.kt.clus.util.tools.optimization.OptimizationProblem;


/**
 * Differential evolution algorithm.
 *
 * @author Tea Tusar
 * @author Timo Aho Modified for multi target use 10.11.2008
 */
public class DEAlgorithm extends OptimizationAlgorithm {

    private DEProblem m_DeProbl;
    private DEPopulation m_Pop;
    private DEIndividual m_Best;


    /**
     * Constructor for classification and regression optimization.
     * 
     * @param stat_mgr
     *        Statistics
     * @param dataInformation
     *        The true values and predictions for the instances. These are used by OptimProbl.
     *        The optimization procedure is based on this data information
     *
     */
    public DEAlgorithm(ClusStatManager stat_mgr, OptimizationProblem.OptimizationParameter dataInformation, ClusRuleSet rset) {
        super(stat_mgr);
        m_DeProbl = new DEProblem(stat_mgr, dataInformation, rset);
        m_Pop = new DEPopulation(stat_mgr, m_DeProbl);
        // m_StatMgr = stat_mgr;
        // m_Probl = new OptimProbl(stat_mgr, parameters);
        // ClusStatistic tar_stat = m_StatMgr.getStatistic(AttributeUseType.Target);
    }


    @Override
    public ArrayList<Double> optimize() {
        int num_eval;
        System.out.print("\nDifferential evolution: Optimizing rule weights (" + getSettings().getRules().getOptDENumEval() + ") ");
        try {
            PrintWriter wrt_log = new PrintWriter(new OutputStreamWriter(new FileOutputStream("evol.log")));
            // PrintWriter wrt_pop = new PrintWriter(new OutputStreamWriter
            // (new FileOutputStream("evol.pop")));
            m_Pop.createFirstPop();
            num_eval = m_Pop.evaluatePop(0);
            m_Best = new DEIndividual();
            m_Best.copy(m_Pop.m_Inds.get(0));
            for (int i = 0; i < getSettings().getRules().getOptDEPopSize(); i++) {
                checkIfBest(m_Pop.m_Inds.get(i));
                OutputLog(m_Pop.m_Inds.get(i), i, wrt_log);
            }
            OutputPop();
            // The while loop is over number of individual evaluations, not separate iterations!
            while (num_eval < getSettings().getRules().getOptDENumEval()) {
                System.out.print(".");
                m_Pop.sortPopRandom();
                DEIndividual candidate = new DEIndividual();

                // Go trough all the population and try to find a candidate with crossing over.
                for (int i = 0; i < getSettings().getRules().getOptDEPopSize(); i++) {
                    candidate.setGenes(m_Pop.getCandidate(i)); // Get a crossed over candidate.
                    num_eval = candidate.evaluate(m_DeProbl, num_eval);
                    checkIfBest(m_Pop.m_Inds.get(i));
                    OutputLog(candidate, num_eval, wrt_log);
                    // Smaller fitness is better
                    if (candidate.m_Fitness < m_Pop.m_Inds.get(i).m_Fitness) {
                        m_Pop.m_Inds.get(i).copy(candidate);
                    }
                }
            }
            wrt_log.close();
            // wrt_pop.close();
            ClusLogger.info(" done!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return m_Best.getGenes();
    }


    public void OutputPop() throws IOException {
        /*
         * for (int i = 0; i < m_params.m_pop_size; i++) {
         * String tmp = ((DeInd)m_pop.m_inds.elementAt(i)).GetIndString();
         * m_file_pop.write(tmp);
         * m_file_pop.write('\n');
         * }
         */
    }


    /**
     * Checks if the individual is the new best. Replaces
     * if this is the case.
     */
    public void checkIfBest(DEIndividual ind) {
        if (m_Best.m_Fitness > ind.m_Fitness) {
            m_Best.copy(ind);
        }
    }


    /** Print the gene to output file. */
    public void OutputLog(DEIndividual ind, int index, PrintWriter wrt) {
    	ClusNumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        wrt.print("" + fr.format(index));
        wrt.print("\t");
        wrt.print("" + fr.format(m_Best.m_Fitness));
        wrt.print("\t");
        wrt.print(ind.getIndString());
        wrt.print("\n");
    }
}

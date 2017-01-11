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
package clus.tools.optimization.de;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import clus.algo.rules.ClusRuleSet;
import clus.main.*;
import clus.tools.optimization.OptProbl;
import clus.tools.optimization.OptAlg;
import clus.util.ClusFormat;

/**
 * Differential evolution algorithm.
 *
 * @author Tea Tusar
 * @author Timo Aho Modified for multi target use 10.11.2008
 */
public class DeAlg extends OptAlg {

	private DeProbl m_DeProbl;
	private DePop m_Pop;
	private DeInd m_Best;


  /**
   * Constructor for classification and regression optimization.
	 * @param stat_mgr Statistics
	 * @param dataInformation The true values and predictions for the instances. These are used by OptimProbl.
	 *                        The optimization procedure is based on this data information
	 *
	 */
	public DeAlg(ClusStatManager stat_mgr, OptProbl.OptParam dataInformation, ClusRuleSet rset) {
		super(stat_mgr);
		m_DeProbl = new DeProbl(stat_mgr, dataInformation, rset);
		m_Pop = new DePop(stat_mgr, m_DeProbl);
		//		m_StatMgr = stat_mgr;
//		m_Probl = new OptimProbl(stat_mgr, parameters);
//		ClusStatistic tar_stat = m_StatMgr.getStatistic(ClusAttrType.ATTR_USE_TARGET);
	}


	public ArrayList<Double> optimize() {
		int num_eval;
		System.out.print("\nDifferential evolution: Optimizing rule weights (" + getSettings().getOptDENumEval() + ") ");
		try {
			PrintWriter wrt_log = new PrintWriter(new OutputStreamWriter
                                (new FileOutputStream("evol.log")));
			// PrintWriter wrt_pop = new PrintWriter(new OutputStreamWriter
      //                           (new FileOutputStream("evol.pop")));
			m_Pop.createFirstPop();
			num_eval = m_Pop.evaluatePop(0);
			m_Best = new DeInd();
			m_Best.copy((DeInd)m_Pop.m_Inds.get(0));
			for (int i = 0; i < getSettings().getOptDEPopSize(); i++) {
				checkIfBest((DeInd)m_Pop.m_Inds.get(i));
				OutputLog((DeInd)m_Pop.m_Inds.get(i), i, wrt_log);
			}
			OutputPop();
			// The while loop is over number of individual evaluations, not separate iterations!
			while (num_eval < getSettings().getOptDENumEval()) {
				System.out.print(".");
				m_Pop.sortPopRandom();
				DeInd candidate = new DeInd();

				// Go trough all the population and try to find a candidate with crossing over.
				for (int i = 0; i < getSettings().getOptDEPopSize(); i++) {
					candidate.setGenes(m_Pop.getCandidate(i)); // Get a crossed over candidate.
					num_eval = candidate.evaluate(m_DeProbl, num_eval);
					checkIfBest((DeInd)m_Pop.m_Inds.get(i));
					OutputLog(candidate, num_eval, wrt_log);
					// Smaller fitness is better
					if (candidate.m_Fitness < ((DeInd)m_Pop.m_Inds.get(i)).m_Fitness) {
						((DeInd)m_Pop.m_Inds.get(i)).copy(candidate);
					}
				}
			}
			wrt_log.close();
			// wrt_pop.close();
			System.out.println(" done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m_Best.getGenes();
	}

	public void OutputPop() throws IOException {
		/*for (int i = 0; i < m_params.m_pop_size; i++) {
			String tmp = ((DeInd)m_pop.m_inds.elementAt(i)).GetIndString();
			m_file_pop.write(tmp);
			m_file_pop.write('\n');
		}*/
	}

	/** Checks if the individual is the new best. Replaces
	 * if this is the case.
	 */
	public void checkIfBest(DeInd ind) {
		if (m_Best.m_Fitness > ind.m_Fitness) {
			m_Best.copy(ind);
		}
	}

	/** Print the gene to output file. */
	public void OutputLog(DeInd ind, int index, PrintWriter wrt) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		wrt.print("" + fr.format(index));
		wrt.print("\t");
		wrt.print("" + fr.format(m_Best.m_Fitness));
		wrt.print("\t");
		wrt.print(ind.getIndString());
		wrt.print("\n");
	}
}

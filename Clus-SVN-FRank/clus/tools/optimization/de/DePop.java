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

import java.util.*;

import clus.main.Settings;
import clus.main.*;

/**
 * Class representing the population.
 * @author Tea Tusar
 */
public class DePop {

	public ArrayList<DeInd> m_Inds;
	private DeProbl m_Probl;
	private Random m_Rand;
	private ClusStatManager m_StatMgr;

	public DePop(ClusStatManager stat_mgr, DeProbl probl) {
		m_Probl = probl;
		m_StatMgr = stat_mgr;
		m_Rand = new Random(getSettings().getOptDESeed());
		m_Inds = new ArrayList<DeInd>(getSettings().getOptDEPopSize());
		for (int i = 0; i < getSettings().getOptDEPopSize(); i++) {
			DeInd ind = new DeInd();
			m_Inds.add(ind);
		}
	}

	public void createFirstPop() {
		for (int i = 0; i < getSettings().getOptDEPopSize(); i++)
			((DeInd)m_Inds.get(i)).setGenes(m_Probl.getRandVector(m_Rand));
	}

	/**
	 *  Evaluate population fitness with the population's function.
	 * @param num_eval How many individuals have already been evaluated? Usually 0.
	 * @return num_eval + no. evaluated individuals. That is, number of evaluated individuals so far.
	 */
	public int evaluatePop(int num_eval) {
		int result = num_eval;
		for (int i = 0; i < m_Inds.size(); i++) {
			result = ((DeInd)m_Inds.get(i)).evaluate(m_Probl, result);
		}
		return result;
	}

	/**
	 * Create a new candidate rule out of parent gene. The new candidate rule is made
	 * with crossing over. At least one variable is changed compared to the parent but probably more.
	 * @param parent Index of parent gene.
	 * @return New candidate gene.
	 */
	public ArrayList<Double> getCandidate(int parent) {
	  int i1, i2, i3;
	  int i, i_rand;

	  /** Result candidate gene */
	  ArrayList<Double> result = new ArrayList<Double>(m_Probl.getNumVar());
	  for (int k = 0; k < m_Probl.getNumVar(); k++) {
	  	result.add(k, (new Double(0.0)));
	  }

	  // Find separate random indexes i1 and i2 and i3 that are not the parent gene
	  do i1 = (int)(getSettings().getOptDEPopSize() * m_Rand.nextDouble());
	  while (i1 == parent);

	  do i2 = (int)(getSettings().getOptDEPopSize() * m_Rand.nextDouble());
	  while ((i2 == parent) || (i2 == i1));

	  do i3 = (int)(getSettings().getOptDEPopSize() * m_Rand.nextDouble());
	  while ((i3 == parent) || (i3 == i1) || (i3 == i2));

	  // Get a random index for gene array. That is, random variable in gene
	  i_rand = (int)(m_Probl.getNumVar() * m_Rand.nextDouble());
	  i = i_rand;

	  // I cycles through the variables. The alteration is made at least for i_rand but
	  // also randomly for other variables. For alteration we take the new value of (i1-i2)+i3.
	  for (int kVariable = 0; kVariable < m_Probl.getNumVar(); kVariable++) {

	    result.set(i,((DeInd)m_Inds.get(parent)).getGenes().get(i));

        // ****** Crossing over
	  	if (m_Rand.nextDouble() < (getSettings().getOptDECrossProb()) || (i == i_rand)) {
	    	result.set(i,new Double(
	    			getSettings().getOptDEWeight() *
				(((Double)((DeInd)m_Inds.get(i1)).getGenes().get(i)).doubleValue() -
				 ((Double)((DeInd)m_Inds.get(i2)).getGenes().get(i)).doubleValue()) +
				 ((Double)((DeInd)m_Inds.get(i3)).getGenes().get(i)).doubleValue()));
	  	}

	  	// ******** Mutations
	  	// If we are searching for lots of zero weights, there should be a mutation for putting the value to zero
	  	if (m_Rand.nextDouble() < getSettings().getOptDEProbMutationZero()) {
	  		result.set(i,new Double(0.0));
	  	}

	  	// We should also have a undo for the previous. Put a random number for it.
	  	if (m_Rand.nextDouble() < getSettings().getOptDEProbMutationNonZero()) {
			result.set(i,new Double(m_Probl.getRandValueInRange(m_Rand,i)));
	  	}

	    i = ++i % m_Probl.getNumVar(); // i = i mod variables.
	  }

	  return m_Probl.getRoundVector(result);
	}

	/**
	 * Take a new random permutation of population individuals.
	 */
	public void sortPopRandom() {
	  int i;
	  /** The result: Array of individuals with random permutation. */
	  ArrayList<DeInd> inds = new ArrayList<DeInd>(getSettings().getOptDEPopSize());
	  /** Array of old indexes of these individuals */
	  ArrayList<Integer> indexes = new ArrayList<Integer>(getSettings().getOptDEPopSize());

	  for (i = 0; i < getSettings().getOptDEPopSize(); i++) {
	  	inds.add(new DeInd());
	  	indexes.add(new Integer(i));
	  }

	  int n;

	  // Take the random permutation of m_Inds to inds
	  for (i = 0; i < getSettings().getOptDEPopSize(); i++) {
	  	 n = (int)(indexes.size() * m_Rand.nextDouble());
	  	 // Copy to inds array the individual in random index
	  	 ((DeInd)inds.get(i)).copy(
	  	 	(DeInd)m_Inds.get(
	  	 		((Integer)indexes.get(n)).intValue()));
	  	 indexes.remove(n);
	  }

	  // Copy the new permutation to m_Inds
	  for (i = 0; i < getSettings().getOptDEPopSize(); i++) {
	  	((DeInd)m_Inds.get(i)).copy((DeInd)inds.get(i));
	  }
	}

	public String getPopString() {
	  String result = "";
	  for (int i = 0; i < m_Inds.size(); i++) {
	  	result += ((DeInd)m_Inds.get(i)).getIndString();
	  }
	  return result;
	}

	public Settings getSettings() {
		return m_StatMgr.getSettings();
	}

}

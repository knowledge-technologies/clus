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

import java.text.NumberFormat;
import java.util.*;

import clus.util.ClusFormat;

/**
 * Class representing a DE individual
 * @author Tea Tusar
 */
public class DeInd {

	private ArrayList<Double> m_Genes;
	/**
	 * Fitness of this DE individual. Smaller is better.
	 */
	public double m_Fitness;

	public DeInd() {
		m_Genes = new ArrayList<Double>();
	}

	public void setGenes(ArrayList<Double> genes) {
		m_Genes = genes;
	}

	public ArrayList<Double> getGenes() {
		return m_Genes;
	}

	/**
	 * Re-evaluates this individuals fitness with the problems fitness function.
	 * @param probl Optimization problem to be evaluated.
	 * @param num_eval Tail recursive parameter for how many individuals evaluated.
	 * @return Return num_eval+1 as in tail recursion.
	 */
	public int evaluate(DeProbl probl, int num_eval) {
		m_Fitness = probl.calcFitness(m_Genes);
		return (num_eval + 1);
	}

	public String getIndString() {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		int i;
		String result = "";
		result += fr.format(m_Fitness) + "\t";
		for (i = 0; i < m_Genes.size(); i++) {
			result += fr.format(m_Genes.get(i)) + "\t";
		}
	  return result;
	}

	public DeInd copy(DeInd original) {
		m_Fitness = original.m_Fitness;
		m_Genes = original.m_Genes;
		return this;
	}

}

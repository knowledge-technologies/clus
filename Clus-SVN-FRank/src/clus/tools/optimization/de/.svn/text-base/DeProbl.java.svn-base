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

import clus.algo.rules.ClusRuleSet;
import clus.main.*;
import clus.statistic.*;

/**
 * Class representing a Differential evolution optimization problem.
 *
 * @author Tea Tusar
 * @author Timo Aho modified for multi target use 10.11.2008.
 */
public class DeProbl extends clus.tools.optimization.OptProbl {

	/** Min value of each variable range */
	private ArrayList<Double> m_VarMin;
	/** Max value of each variable range */
	private ArrayList<Double> m_VarMax;

	/**
	 * Constructor for problem to be solved with differential evolution. Both classification and regression.
	 * @param stat_mgr Statistics
	 * @param dataInformation The true values and predictions for the instances. These are used by OptimProbl.
	 *                        The optimization procedure is based on this data information
	 */
	public DeProbl(ClusStatManager stat_mgr, OptParam optInfo, ClusRuleSet rset) {
		super(stat_mgr, optInfo);

		m_VarMin = new ArrayList<Double>(getNumVar());
		m_VarMax = new ArrayList<Double>(getNumVar());

		for (int i = 0; i < getNumVar(); i++) {
			m_VarMin.add(new Double(0));
			m_VarMax.add(new Double(1));
		}
	}

	/**
	 * Generates a random solution. A vector of random values.
	 */
	public ArrayList<Double> getRandVector(Random rand)	{
		ArrayList<Double> result = new ArrayList<Double>(getNumVar());
		for (int i = 0; i < getNumVar(); i++) {
			result.add(new Double(getRandValueInRange(rand,i)));
		}
		return result;
	}

	/**
	 * Single random value within the range of variables.
	 */
	public double getRandValueInRange(Random rand, int indexOfValue) {
		// min + (max-min)*random
		return ((Double)m_VarMin.get(indexOfValue)).doubleValue() +
		       (((Double)m_VarMax.get(indexOfValue)).doubleValue() -
				((Double)m_VarMin.get(indexOfValue)).doubleValue()) *
				(double)rand.nextDouble();

	}

	public ArrayList<Double> getRoundVector(ArrayList<Double> genes) {
		ArrayList<Double> result = new ArrayList<Double>(getNumVar());
		for (int i = 0; i < getNumVar(); i++) {
			if (((Double)genes.get(i)).doubleValue() >=
				((Double)m_VarMax.get(i)).doubleValue())
				result.add(m_VarMax.get(i));
			else if (((Double)genes.get(i)).doubleValue() <=
					 ((Double)m_VarMin.get(i)).doubleValue())
				result.add(m_VarMin.get(i));
			else
				result.add(genes.get(i));
		}
		return result;
	}
}

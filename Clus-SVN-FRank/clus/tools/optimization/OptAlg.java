/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2008                                                    *
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
 * Created on 27.11.2008
 */
package clus.tools.optimization;

import java.util.ArrayList;

import clus.algo.rules.ClusRuleSet;
import clus.main.ClusStatManager;
import clus.main.Settings;

/**
 * Abstract super class for optimization of weights of base learners.
 * @author Timo Aho
 */
public abstract class OptAlg {

	private ClusStatManager m_StatMgr;

	/**
	 * Constructor for classification and regression optimization.
	 * @param stat_mgr Statistics
	 * @param dataInformation The true values and predictions for the instances. These are used by OptimProbl.
	 *                        The optimization procedure is based on this data information
	 */
	public OptAlg(ClusStatManager stat_mgr) {
		m_StatMgr = stat_mgr;
//		m_Probl = new DeProbl(stat_mgr, dataInformation);
	}

	/**
	 * Start the actual optimization.
	 * @return The weights for the base learners.
	 */
	abstract public ArrayList<Double> optimize();

	final protected Settings getSettings() {
		return m_StatMgr.getSettings();
	}

	public void postProcess(ClusRuleSet rset) {
	}

}

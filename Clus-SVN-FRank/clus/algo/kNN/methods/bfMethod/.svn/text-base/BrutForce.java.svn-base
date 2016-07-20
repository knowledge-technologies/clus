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

package clus.algo.kNN.methods.bfMethod;

import clus.algo.kNN.methods.NNStack; 
import clus.algo.kNN.methods.SearchAlgorithm;
import clus.algo.kNN.distance.SearchDistance;
import clus.data.rows.DataTuple;
import clus.main.ClusRun;
import clus.util.ClusException;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Mitja Pugelj
 */
public class BrutForce extends SearchAlgorithm{
//	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	private DataTuple[] m_List;
	private NNStack m_Stack;

	public BrutForce(ClusRun run, SearchDistance dist){
		super(run,dist);
	}

	public void build() throws ClusException, IOException {
		// does nothing at all
		m_List = getRun().getDataSet(ClusRun.TRAINSET).m_Data;
	}

	public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) {
		m_Stack = new NNStack(k);
		for (DataTuple d : m_List )
			m_Stack.addToStack(d, getDistance().calcDistance(tuple, d));
		return m_Stack.returnStack();
	}
}

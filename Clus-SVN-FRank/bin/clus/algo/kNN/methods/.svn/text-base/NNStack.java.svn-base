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

package clus.algo.kNN.methods;


import clus.data.rows.DataTuple; 
import java.util.LinkedList;

/**
 * @author Mitja Pugelj
 */

/**
 * Implements simple insertion algorithm for maintaining k nearest neighbors.
 */
public class NNStack {

	private int m_NbNeighbors;//Number of nearest neighbors we are searching = size of stack (+ extra guard element)
	private NN[] m_NearestNeighbors;// stack

	/**
	 * Construct new stack for storing k nearest neighbors.
	 * @param k
	 */
	public NNStack(int k){
		m_NbNeighbors = k;
		m_NearestNeighbors = new NN[k+1]; // +1 is guard element which simplifies addToStack method.
	}

	/**
	 * Add new DataTuple tuple with distance dist to stack (if and only if the
	 * distance is small enough regarding to tuples already on stack.
	 * @param tuple to be added
	 * @param dist distance of tuple to tuple to be classified
	 */
	public void addToStack(DataTuple tuple, double dist){
		for( int j = m_NbNeighbors-1; j >= 0 ; j--){
			if( m_NearestNeighbors[j] == null ) // degenerated case
				continue;
			if( m_NearestNeighbors[j].getDistance() > dist )
				m_NearestNeighbors[j+1] = m_NearestNeighbors[j];
			else{
				m_NearestNeighbors[j+1] = new NN(tuple,dist);
				return;
			}
		}
		m_NearestNeighbors[0] = new NN(tuple,dist);
	}

	/**
	 * Returns distance of farthest nearest neighbor.
	 * @return
	 */
	public double getWorstNearestDistance(){
		return (m_NearestNeighbors[m_NbNeighbors-1] == null ) ? Double.MAX_VALUE : m_NearestNeighbors[m_NbNeighbors-1].getDistance();
	}

	/**
	 * @return true if at least k candidates were added so far and false if we are
	 * desperate for more nearest neighbors
	 */
	public boolean enoughNeighbours(){
		return m_NearestNeighbors[m_NbNeighbors-1] != null;
	}

	/**
	 * Returns k nearest neighbors among those added to stack.
	 * @return
	 */
	public LinkedList<DataTuple> returnStack(){
		LinkedList<DataTuple> nns = new LinkedList<DataTuple>();
		for( int i = 0; i < m_NbNeighbors; i++)
			nns.add(m_NearestNeighbors[i].getTuple());
		return nns;
	}
}

class NN{
	protected DataTuple m_Tuple;
	protected double m_Distance;

	public NN(DataTuple tuple, double dist){
		m_Tuple = tuple;
		m_Distance = dist;
	}

	public DataTuple getTuple(){
		return m_Tuple;
	}

	public double getDistance(){
		return m_Distance;
	}
}

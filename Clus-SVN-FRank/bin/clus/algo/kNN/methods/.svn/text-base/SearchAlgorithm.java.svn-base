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

import clus.algo.kNN.distance.SearchDistance;  
import clus.data.rows.DataTuple;
import clus.main.ClusRun;
import clus.util.ClusException;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Mitja Pugelj
 */
public abstract class SearchAlgorithm {
    
	public static final int ALG_KD = 0;
    public static final int ALG_VP = 1;
    public static final int ALG_PA = 2;
    
    protected ClusRun m_Run;
    protected boolean m_Debug = false;
    protected SearchDistance m_SearchDistance;
    public static int[] operationsCount = new int[3];

    public SearchAlgorithm(ClusRun run, SearchDistance dist){
    	m_Run = run;
    	m_SearchDistance = dist;
    }

    /**
     * Building search structure on known samples.
     */
    public abstract void build() throws ClusException, IOException;

    /**
     * Returns k nearest neighbors.
     * @param k number of neighbors to return.
     * @param tuple sample to be classified
     * @return
     */
    public abstract LinkedList<DataTuple> returnNNs(DataTuple tuple, int k);

    /**
     * Check if DEBUG mode is on.
     * @return
     */
    public boolean isDEBUG(){
        return m_Debug;
    }

    /**
     * Set DEBUG variable. If this is set to true, extra information will be provided.
     * @param d
     */
    public void setDEBUG(boolean d){
    	m_Debug = d;
    }

    /**
     * Returns distance used in search algorithm.
     * @return
     */
    public SearchDistance getDistance(){
        return m_SearchDistance;
    }
    
    public ClusRun getRun(){
    	return m_Run;
    }
}

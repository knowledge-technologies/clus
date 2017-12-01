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

package si.ijs.kt.clus.algo.kNN.methods.bfMethod;

import java.io.IOException;
import java.util.LinkedList;

import si.ijs.kt.clus.algo.kNN.methods.NN;
import si.ijs.kt.clus.algo.kNN.methods.NNStack;
import si.ijs.kt.clus.algo.kNN.methods.SearchAlgorithm;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.distance.primitive.SearchDistance;
import si.ijs.kt.clus.main.ClusModelInfoList;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.util.ClusException;


/**
 * @author Mitja Pugelj, matejp
 */
public class BruteForce extends SearchAlgorithm {

    // private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    protected DataTuple[] m_ListTrain;
    protected DataTuple[] m_ListTest; // Used in Oracle
    private NNStack m_Stack;


    public BruteForce(ClusRun run, SearchDistance dist) {
        super(run, dist);
    }


    @Override
    public void build(int k) throws ClusException, IOException, InterruptedException {
        // does nothing at all
        m_ListTrain = getRun().getDataSet(ClusModelInfoList.TRAINSET).getData();
    }


    @Override
    public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) throws ClusException {
        m_Stack = new NNStack(k);
        for (DataTuple d : m_ListTrain)
            m_Stack.addToStack(d, getDistance().calcDistance(tuple, d));
        return m_Stack.returnStack();
    }
    
    public NN[] returnPureNNs(DataTuple tuple, int k) throws ClusException {
        m_Stack = new NNStack(k);
        for (DataTuple d : m_ListTrain)
            m_Stack.addToStack(d, getDistance().calcDistance(tuple, d));
        return m_Stack.returnPureStack();
    }
}

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

package si.ijs.kt.clus.algo.kNN.methods.kdTree;

import java.io.IOException;
// import java.util.Collection;
import java.util.LinkedList;

import si.ijs.kt.clus.algo.kNN.methods.NNStack;
import si.ijs.kt.clus.algo.kNN.methods.SearchAlgorithm;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.distance.primitive.SearchDistance;
import si.ijs.kt.clus.main.ClusModelInfoList;
// import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.util.ClusException;


/**
 * @author Mitja Pugelj
 */
public class KDTree extends SearchAlgorithm {

    private KDNode m_Root;// Root of tree.
    protected int m_MaxTuples = 1;// Maximum number of tuples in leaf. If node contains more tuples than specified, it
                                  // is splitted.
    private int m_kNeighbors = 1;// Number of nearest neighbors to find.
    // Data information
    protected ClusRun m_Run;
    protected NNStack m_Stack;


    public KDTree(ClusRun run, SearchDistance distance) {
        super(run, distance);
        m_Run = run;
    }


    /**
     * Builds tree.
     * 
     * @throws clus.util.ClusException
     * @throws java.io.IOException
     * @throws InterruptedException 
     */
    @Override
    public void build() throws ClusException, IOException, InterruptedException {
        RowData data = getRun().getDataSet(ClusModelInfoList.TRAINSET);
        // copy tuples into LinkedList for easier manipulation
        LinkedList<DataTuple> tuples = new LinkedList<DataTuple>();
        for (DataTuple tuple : data.getData()) // instead of m_Data
            tuples.add(tuple);
        // create root node
        m_Root = new KDNode(this, 0);
        m_Root.setTuples(tuples);
        // build tree
        m_Root.build(0);
    }


    /**
     * Return nearest k (getK()) tuples to specified tuple.
     * 
     * @param tuple
     * @return
     * @throws ClusException 
     */
    @Override
    public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) throws ClusException {
        m_kNeighbors = k;
        m_Stack = new NNStack(m_kNeighbors);
        m_Root.find(tuple);
        return m_Stack.returnStack();
    }


    /**
     * Returns number of neighbors we search.
     * 
     * @return
     */
    public int getK() {
        return m_kNeighbors;
    }


    public NNStack getStack() {
        return m_Stack;
    }


    @Override
    public ClusRun getRun() {
        return m_Run;
    }


    public int getMaxTuples() {
        return m_MaxTuples;
    }
}

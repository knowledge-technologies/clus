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

package si.ijs.kt.clus.model.processor;

import java.io.IOException;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.tree.CompleteTreeIterator;


public class CalcStatisticProcessor extends ClusModelProcessor {

    ClusStatistic m_Clone;


    public CalcStatisticProcessor(ClusStatistic clone) {
        m_Clone = clone;
    }


    @Override
    public boolean needsModelUpdate() {
        return true;
    }


    @Override
    public boolean needsInternalNodes() {
        return true;
    }


    @Override
    public void initialize(ClusModel model, ClusSchema schema) {
        CompleteTreeIterator iter = new CompleteTreeIterator((ClusNode) model);
        while (iter.hasMoreNodes()) {
            ClusNode node = (ClusNode) iter.getNextNode();
            ClusStatistic stat = m_Clone.cloneStat();
            node.setClusteringStat(stat);
            stat.setSDataSize(1);
        }
    }


    @Override
    public void terminate(ClusModel model) throws IOException, ClusException {
        CompleteTreeIterator iter = new CompleteTreeIterator((ClusNode) model);
        while (iter.hasMoreNodes()) {
            ClusNode node = (ClusNode) iter.getNextNode();
            node.getClusteringStat().calcMean();
        }
    }


    @Override
    public void modelUpdate(DataTuple tuple, ClusModel model) {
        ClusNode node = (ClusNode) model;
        node.getClusteringStat().updateWeighted(tuple, 0);
    }
}

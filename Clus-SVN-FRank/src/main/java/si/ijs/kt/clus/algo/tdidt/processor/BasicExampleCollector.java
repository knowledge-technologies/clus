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

package si.ijs.kt.clus.algo.tdidt.processor;

import java.io.IOException;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.processor.ClusModelProcessor;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public class BasicExampleCollector extends ClusModelProcessor {

    public boolean needsModelUpdate() {
        return true;
    }


    public void initialize(ClusModel model, ClusSchema schema) {
        ClusNode root = (ClusNode) model;
        recursiveInitialize(root);
    }


    public void terminate(ClusModel model) throws IOException {
    }


    public void modelUpdate(DataTuple tuple, ClusModel model) {
        ClusNode node = (ClusNode) model;
        MyArray visitor = (MyArray) node.getVisitor();
        visitor.addElement(tuple);
    }


    private void recursiveInitialize(ClusNode node) {
        if (node.atBottomLevel()) {
            node.setVisitor(new MyArray());
        }
        else {
            for (int i = 0; i < node.getNbChildren(); i++) {
                ClusNode child = (ClusNode) node.getChild(i);
                recursiveInitialize(child);
            }
        }
    }
}

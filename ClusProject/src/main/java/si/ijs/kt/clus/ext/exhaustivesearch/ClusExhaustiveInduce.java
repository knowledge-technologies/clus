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

package si.ijs.kt.clus.ext.exhaustivesearch;

import java.io.IOException;
import java.util.ArrayList;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.split.NominalSplit;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamModel;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.io.ClusModelCollectionIO;
import si.ijs.kt.clus.util.exception.ClusException;


public class ClusExhaustiveInduce extends ClusInductionAlgorithm {

    protected NominalSplit m_Split;
    protected ClusExhaustiveSearch m_Search;


    public ClusExhaustiveInduce(ClusSchema schema, Settings sett, ClusExhaustiveSearch search) throws ClusException, IOException {
        super(schema, sett);
        m_Search = search;
    }


    @Override
    public void initializeHeuristic() {
        m_Search.initializeHeuristic();
    }


    @Override
    public ClusData createData() {
        return new RowData(m_Schema);
    }


    @Override
    public boolean isModelWriter() {
        return true;
    }


    @Override
    public void writeModel(ClusModelCollectionIO strm) throws IOException, ClusException {
        m_Search.writeModel(strm);
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
        return null;
    }


    @Override
    public void induceAll(ClusRun cr) throws Exception {
        m_Search.exhaustiveSearch(cr);
        ClusModelInfo def_model = cr.addModelInfo(ClusModel.DEFAULT);
        def_model.setModel(ClusDecisionTree.induceDefault(cr));
        def_model.setName("Default");
        ArrayList lst = m_Search.getBeam().toArray();
        for (int i = 0; i < lst.size(); i++) {
            // ClusBeamModel mdl = (ClusBeamModel)lst.get(lst.size()-i-1);
            ClusBeamModel mdl = (ClusBeamModel) lst.get(i);
            ClusNode tree = (ClusNode) mdl.getModel();
            // tree.postProc(null);
            // ClusModelInfo model_info = cr.addModelInfo(i+1);
            ClusModelInfo model_info = cr.addModelInfo(i);
            model_info.setModel(tree);
            model_info.setName("Beam " + (i + 1));
            model_info.clearAll();
        }
    }
}

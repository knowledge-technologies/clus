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

package clus.algo.kNN.test;

import clus.Clus;
import clus.algo.ClusInductionAlgorithm;
import clus.algo.kNN.KnnClassifier;
import clus.data.type.ClusSchema;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.util.ClusException;
import java.io.IOException;
import jeans.util.cmdline.CMDLineArgs;

/**
 *
 * @author Mitja Pugelj
 */
public class oTestKnnClassifier extends KnnClassifier{

    public oTestKnnClassifier(Clus clus){
        super(clus);
    }

    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        ClusInductionAlgorithm induce =  new ClusInductionAlgorithmImpl(schema, sett);
        return induce;
    }

    private class ClusInductionAlgorithmImpl extends ClusInductionAlgorithm {

        public ClusInductionAlgorithmImpl(ClusSchema schema, Settings sett) throws ClusException, IOException {
            super(schema, sett);
        }

        public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
            oTestKnnModel model = new oTestKnnModel(cr,Integer.parseInt(Settings.kNN_k.getValue()));

            ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL);
            model_info.setModel(model);
            model_info.setName("Original");

            ClusModel defModel = induceDefaultModel(cr);
            ClusModelInfo defModelInfo = cr.addModelInfo(ClusModel.DEFAULT);
            defModelInfo.setModel(defModel);
            defModelInfo.setName("Default");
            return model;
        }

    }

}

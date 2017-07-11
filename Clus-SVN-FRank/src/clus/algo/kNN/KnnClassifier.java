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

package clus.algo.kNN;

import java.io.IOException;

import clus.Clus;
import clus.algo.ClusInductionAlgorithm;
import clus.algo.ClusInductionAlgorithmType;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.RowData;
import clus.data.schema.ClusSchema;
import clus.main.ClusRun;
import clus.main.settings.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.util.ClusException;
import clus.util.jeans.util.cmdline.CMDLineArgs;


/**
 *
 * @author Mitja Pugelj
 */
public class KnnClassifier extends ClusInductionAlgorithmType {

    public KnnClassifier(Clus clus) {
        super(clus);
    }


    


    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        ClusInductionAlgorithm induce = new ClusInductionAlgorithmImpl(schema, sett);
        return induce;
    }


    public void pruneAll(ClusRun cr) throws ClusException, IOException {

    }


    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        return model;
    }

    private class ClusInductionAlgorithmImpl extends ClusInductionAlgorithm {

        public ClusInductionAlgorithmImpl(ClusSchema schema, Settings sett) throws ClusException, IOException {
            super(schema, sett);
        }


        public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {

            String[] ks = getSettings().getKNN().getKNNk().split(",");
            String[] distWeight = getSettings().getKNN().getKNNDistanceWeight().split(",");
            int[] weights = new int[distWeight.length];
            int i = 0;
            for (String s : distWeight) {
                if (s.compareTo("1-d") == 0)
                    weights[i] = KnnModel.WEIGHTING_MINUS;
                else if (s.compareTo("1/d") == 0)
                    weights[i] = KnnModel.WEIGHTING_INVERSE;
                else
                    weights[i] = KnnModel.WEIGHTING_CONSTANT;
                i++;
            }
            // base model
            String model_name = "Default 1-nn model with no weighting"; // DO NOT CHANGE THE NAME!!!
            KnnModel model = new KnnModel(cr, 1, 1);
            ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL, model_name);
            model_info.setModel(model);
            model_info.setName(model_name);

            ClusModel defModel = induceDefaultModel(cr);
            ClusModelInfo defModelInfo = cr.addModelInfo(ClusModel.DEFAULT);
            defModelInfo.setModel(defModel);
            defModelInfo.setName("Default");

            int modelCnt = 2;

            for (String kt : ks) {
                i = -1;
                for (int w : weights) {
                    i++;
                    int k = Integer.parseInt(kt);
                    if (k == 1 && w == 1)
                        continue; // same as default model
                    KnnModel tmpmodel = new KnnModel(cr, k, w, model);
                    model_name = "Original " + k + "-nn model with " + weights[i] + " weighting";// DO NOT CHANGE THE
                                                                                                 // NAME!!!
                    ClusModelInfo tmpmodel_info = cr.addModelInfo(modelCnt++, model_name);
                    tmpmodel_info.setModel(tmpmodel);
                    tmpmodel_info.setName(model_name);
                }
            }

            return model;
        }
    }


    /**
     * Induced default model - prediction to majority class.
     * 
     * @param cr
     * @return
     */
    public static ClusModel induceDefaultModel(ClusRun cr) {
        ClusNode node = new ClusNode();
        RowData data = (RowData) cr.getTrainingSet();
        node.initTargetStat(cr.getStatManager(), data);
        node.computePrediction();
        node.makeLeaf();
        return node;
    }

}

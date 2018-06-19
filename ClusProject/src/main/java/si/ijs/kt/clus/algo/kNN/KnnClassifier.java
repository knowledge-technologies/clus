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

package si.ijs.kt.clus.algo.kNN;

import java.io.IOException;
import java.util.List;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.DistanceWeights;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


/**
 *
 * @author Mitja Pugelj, matejp
 */
public class KnnClassifier extends ClusInductionAlgorithmType {

    // DO NOT CHANGE THE NAME!!!
    public static String DEFAULT_MODEL_NAME_WITH_CONSTANT_WEIGHTS = "Default 1-nn model with " + DistanceWeights.Constant.toString() + " weights";
    private final String modelNameTemplate = "Original %s -nn model with %s weighting";


    public KnnClassifier(Clus clus) {
        super(clus);
    }


    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        ClusInductionAlgorithm induce = new ClusInductionAlgorithmImpl(schema, sett);
        return induce;
    }


    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {

    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        return model;
    }

    private class ClusInductionAlgorithmImpl extends ClusInductionAlgorithm {

        public ClusInductionAlgorithmImpl(ClusSchema schema, Settings sett) throws ClusException, IOException {
            super(schema, sett);
        }


        @Override
        public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException {

            int[] ks = getSettings().getKNN().getKNNk(); // .split(",");
            List<DistanceWeights> distWeight = getSettings().getKNN().getKNNDistanceWeights(); // .split(",");

            int maxK = 1;
            for (int k : ks) {
                maxK = Math.max(maxK, k);
            }

            // base model
            String model_name = DEFAULT_MODEL_NAME_WITH_CONSTANT_WEIGHTS;
            KnnModel model = new KnnModel(cr, 1, DistanceWeights.Constant, maxK);
            ClusModelInfo model_info = cr.addModelInfo(ClusModel.ORIGINAL, model_name);
            model_info.setModel(model);
            model_info.setName(model_name);

            ClusModel defModel = induceDefaultModel(cr);
            ClusModelInfo defModelInfo = cr.addModelInfo(ClusModel.DEFAULT);
            defModelInfo.setModel(defModel);
            defModelInfo.setName("Default");

            int modelCnt = 2;

            for (int k : ks) {
                for (DistanceWeights w : distWeight) {
                    if (k == 1 && w.equals(DistanceWeights.Constant))
                        continue; // same as default model
                    KnnModel tmpmodel = new KnnModel(cr, k, w, model);

                    model_name = String.format(modelNameTemplate, k, w.toString()); // DO NOT CHANGE THE NAME!!!

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

     * @throws ClusException
     * @throws InterruptedException
     */
    public static ClusModel induceDefaultModel(ClusRun cr) throws ClusException, InterruptedException {
        ClusNode node = new ClusNode();
        RowData data = (RowData) cr.getTrainingSet();
        node.initTargetStat(cr.getStatManager(), data);
        node.computePrediction();
        node.makeLeaf();
        return node;
    }


    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException {
        

    }
}

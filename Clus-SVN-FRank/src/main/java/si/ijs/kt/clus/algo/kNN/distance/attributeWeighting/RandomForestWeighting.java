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

package si.ijs.kt.clus.algo.kNN.distance.attributeWeighting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.ensembles.ClusEnsembleClassifier;
import si.ijs.kt.clus.ext.ensembles.ClusEnsembleInduce;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsEnsemble;
import si.ijs.kt.clus.util.ClusRandom;


/**
 * @author Mitja Pugelj
 */
public class RandomForestWeighting extends AttributeWeighting {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private double[] m_Weights;


    public RandomForestWeighting(ClusRun run, int nbBags) {
        this.generateFeatureRanking(run, nbBags);
    }


    public double getWeight(ClusAttrType attr) {
        return m_Weights[attr.getIndex()];
    }


    private void generateFeatureRanking(ClusRun run, int nbBags) {
        try {
            PrintStream oldOut = System.out;
            PrintStream ps = new PrintStream(new OutputStream() {

                public void write(int arg0) throws IOException {
                }
            });
            System.out.println("Ignoring RandomForest output.");
            System.setOut(ps);
            Settings orig_sett = run.getStatManager().getSettings();
            Clus new_clus = new Clus();

            Settings new_sett = new_clus.getSettings();
            new_sett.getGeneric().setDate(orig_sett.getGeneric().getDate());
            new_sett.getGeneric().setAppName(orig_sett.getGeneric().getAppName());
            new_sett.initialize(null, false);
            new_sett.getEnsemble().setEnsembleMode(true);
            new_sett.getAttribute().setTarget(orig_sett.getAttribute().getTarget());
            new_sett.getEnsemble().setEnsembleMethod(SettingsEnsemble.ENSEMBLE_RFOREST);
            new_sett.getEnsemble().setNbBags(nbBags); // TO-DO User Defined!
            new_sett.getEnsemble().setNbRandomAttrSelected(0); // Selects LOG of number of descriptive attributes
            new_sett.getEnsemble().setOOBestimate(true);
            new_sett.getEnsemble().setFeatureRankingMethod(orig_sett.getEnsemble().getRankingMethod());
            ClusRandom.initialize(new_sett);

            RowData trainData = (RowData) run.getTrainingSet().cloneData();
            ClusSchema schema = run.getStatManager().getSchema().cloneSchema();

            // Update schema based on settings
            new_sett.updateTarget(schema);
            schema.initializeSettings(new_sett);
            new_sett.getAttribute().setTarget(schema.getTarget().toString());
            new_sett.getAttribute().setDisabled(schema.getDisabled().toString());
            new_sett.getAttribute().setClustering(schema.getClustering().toString());
            new_sett.getAttribute().setDescriptive(schema.getDescriptive().toString());

            // null are the 'cargs'
            new_clus.recreateInduce(null, new ClusEnsembleClassifier(new_clus), schema, trainData);

            ClusEnsembleInduce ensemble = (ClusEnsembleInduce) new_clus.getInduce();
            new_sett.update(schema);
            new_sett.getRules().disableRuleInduceParams();
            new_clus.preprocess(); // necessary in order to link the labels to the class hierarchy in HMC (needs to be
                                   // before m_Induce.initialize())
                                   // new_clus.singleRun(new_clus.getClassifier());

            ensemble.induceBagging(run);
            int forestIndex = 0;
            ensemble.getEnsembleFeatureRanking(forestIndex).sortFeatureRanks(ensemble.getNbTrees(forestIndex));  // matejp changed this
            ensemble.getEnsembleFeatureRanking(forestIndex).convertRanksByName();
            System.setOut(oldOut);

            // The feature ranks can be retrieved using one of the following statements
            // check what you need and how you wish the ranks to be organized...
            // TreeMap ranks_sorted = ensemble.getFeatureRanks(); //sorted by the rank
            HashMap ranks_by_name = ensemble.getEnsembleFeatureRanking(0).getFeatureRanksByName();// key is the
                                                                                                 // AttributeName, and
                                                                                                 // the value is array
                                                                                                 // with the order in
                                                                                                 // the file and the
                                                                                                 // rank
            System.out.println(ranks_by_name);
            m_Weights = new double[schema.getDescriptiveAttributes().length];
            double dsum = 0.;
            // fill weights from ranks
            for (ClusAttrType attr : schema.getDescriptiveAttributes()) {
                Double d = (Double) ranks_by_name.get(attr.getName());
                m_Weights[attr.getIndex()] = d.doubleValue();
                dsum = dsum + d.doubleValue();
            }
            for (int i = 0; i < m_Weights.length; i++)
                System.out.println(m_Weights[i]);
            // normalize and "inverse" ranks (ranks to weights)
            for (int i = 0; i < m_Weights.length; i++)
                m_Weights[i] = 1 - m_Weights[i] / dsum;

        }
        catch (Exception e) {
            System.out.println("RandomForest weighting failed.");
            e.printStackTrace();
        }
    }

}

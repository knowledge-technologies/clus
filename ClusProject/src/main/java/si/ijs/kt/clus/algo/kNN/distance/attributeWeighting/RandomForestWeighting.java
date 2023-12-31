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

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleClassifier;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRanking;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleRanking;
import si.ijs.kt.clus.util.ClusLogger;
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


    @Override
    public double getWeight(ClusAttrType attr) {
        return m_Weights[attr.getIndex()];
    }


    private void generateFeatureRanking(ClusRun run, int nbBags) {
        try {
            PrintStream oldOut = System.out;
            PrintStream ps = new PrintStream(new OutputStream() {

                @Override
                public void write(int arg0) throws IOException {
                }
            });
            ClusLogger.info("Ignoring RandomForest output.");
            System.setOut(ps);
            Settings orig_sett = run.getStatManager().getSettings();
            Clus new_clus = new Clus();

            Settings new_sett = new_clus.getSettings();
            new_sett.getGeneric().setDate(orig_sett.getGeneric().getDate());
            new_sett.getGeneric().setAppName(orig_sett.getGeneric().getAppName());
            new_sett.initialize(null, false);
            new_sett.getEnsemble().setEnsembleMode(true);
            new_sett.getAttribute().setTarget(orig_sett.getAttribute().getTarget());
            new_sett.getEnsemble().setEnsembleMethod("RForest");
            new_sett.getEnsemble().setNbBags(nbBags); // TO-DO User Defined!
            new_sett.getEnsemble().setNbRandomAttrSelected(0); // Selects LOG of number of descriptive attributes
            new_sett.getEnsemble().setOOBestimate(true);
            new_sett.getEnsemble().setFeatureRankingMethod(EnsembleRanking.Genie3.toString());
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

            // matejp was here
            ensemble.induceBagging(run);
            int forestIndex = 0;
            ClusFeatureRanking franking = ensemble.getEnsembleFeatureRanking(forestIndex, EnsembleRanking.Genie3);
            franking.computeFinalScores(ensemble.getNbTrees(forestIndex));
            System.setOut(oldOut);


            m_Weights = new double[schema.getDescriptiveAttributes().length];
            double dsum = 0.;
            int rankingIndex = 0;
            // fill weights from ranks
            for (ClusAttrType attr : schema.getDescriptiveAttributes()) {
                double d = franking.getAttributeRelevance(attr.getName(), rankingIndex);
                m_Weights[attr.getIndex()] = d;
                dsum += d;
            }
            for (int i = 0; i < m_Weights.length; i++)
                ClusLogger.info(m_Weights[i]);
            // normalize and "inverse" ranks (ranks to weights)
            for (int i = 0; i < m_Weights.length; i++)
                m_Weights[i] = 1 - m_Weights[i] / dsum;

        }
        catch (Exception e) {
            ClusLogger.info("RandomForest weighting failed.");
            e.printStackTrace();
        }
    }

}

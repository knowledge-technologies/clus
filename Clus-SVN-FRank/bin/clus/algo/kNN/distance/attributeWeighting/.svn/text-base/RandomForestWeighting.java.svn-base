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


package clus.algo.kNN.distance.attributeWeighting;

import clus.Clus; 
import clus.data.type.ClusAttrType;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.util.ClusRandom;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.ext.ensembles.ClusEnsembleClassifier;
import clus.ext.ensembles.ClusEnsembleInduce;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * @author Mitja Pugelj
 */
public class RandomForestWeighting extends AttributeWeighting{
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	private double[] m_Weights;

	public RandomForestWeighting(ClusRun run, int nbBags){
		this.generateFeatureRanking(run,nbBags);
	}

	public double getWeight(ClusAttrType attr) {
		return m_Weights[attr.getIndex()];
	}

	private void generateFeatureRanking(ClusRun run, int nbBags){
		try{
			PrintStream oldOut = System.out;
			PrintStream ps = new PrintStream(new OutputStream() {
				public void write(int arg0) throws IOException { }
			});
			System.out.println("Ignoring RandomForest output.");
			System.setOut(ps);
			Settings orig_sett = run.getStatManager().getSettings();
			Clus new_clus = new Clus();

			Settings new_sett = new_clus.getSettings();
			new_sett.setDate(orig_sett.getDate());
			new_sett.setAppName(orig_sett.getAppName());
			new_sett.initialize(null, false);
			new_sett.setEnsembleMode(true);
			new_sett.setTarget(orig_sett.getTarget());
			new_sett.setEnsembleMethod(1);//1 is for RForest!!!
			new_sett.setNbBags(nbBags); //TO-DO User Defined!
			new_sett.setNbRandomAttrSelected(0); //Selects LOG of number of descriptive attributes
			new_sett.setOOBestimate(true);
			new_sett.setFeatureRanking(true);
			ClusRandom.initialize(new_sett);

			RowData trainData = (RowData)run.getTrainingSet().cloneData();
			ClusSchema schema = run.getStatManager().getSchema().cloneSchema();

			// Updata schema based on settings
			new_sett.updateTarget(schema);
			schema.initializeSettings(new_sett);
			new_sett.setTarget(schema.getTarget().toString());
			new_sett.setDisabled(schema.getDisabled().toString());
			new_sett.setClustering(schema.getClustering().toString());
			new_sett.setDescriptive(schema.getDescriptive().toString());

			//null are the 'cargs'
			new_clus.recreateInduce(null, new ClusEnsembleClassifier(new_clus), schema, trainData);

			ClusEnsembleInduce ensemble = (ClusEnsembleInduce)new_clus.getInduce();
			new_sett.update(schema);
			new_sett.disableRuleInduceParams();
			new_clus.preprocess(); // necessary in order to link the labels to the class hierarchy in HMC (needs to be before m_Induce.initialize())
			//new_clus.singleRun(new_clus.getClassifier());

			ensemble.induceBagging(run);
			ensemble.getEnsembleFeatureRanking().sortFeatureRanks();
			ensemble.getEnsembleFeatureRanking().convertRanksByName();
			System.setOut(oldOut);

			//The feature ranks can be retrieved using one of the following statements
			// check what you need and how you wish the ranks to be organized...
			// TreeMap ranks_sorted = ensemble.getFeatureRanks(); //sorted by the rank
			HashMap ranks_by_name = ensemble.getEnsembleFeatureRanking().getFeatureRanksByName();//key is the AttributeName, and the value is array with the order in the file and the rank
			System.out.println(ranks_by_name);
			m_Weights = new double[schema.getDescriptiveAttributes().length];
			double dsum = 0.;
			// fill weights from ranks
			for( ClusAttrType attr : schema.getDescriptiveAttributes() ){
				Double d = (Double) ranks_by_name.get(attr.getName());
				m_Weights[attr.getIndex()] = d.doubleValue();
				dsum = dsum + d.doubleValue();
			}
			for( int i = 0; i < m_Weights.length ; i++ )
				System.out.println(m_Weights[i]);
			// normalize and "inverse" ranks (ranks to weights)
			for( int i = 0; i < m_Weights.length ; i++ )
				m_Weights[i] = 1-m_Weights[i]/dsum;

		}catch( Exception e ){
			System.out.println("RandomForest weighting failed.");
			e.printStackTrace();
		}
	}

}

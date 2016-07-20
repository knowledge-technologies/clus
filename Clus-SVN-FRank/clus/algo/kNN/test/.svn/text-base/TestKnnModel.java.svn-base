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
import clus.algo.kNN.distance.ChebyshevDistance;
import clus.algo.kNN.methods.SearchAlgorithm;
import clus.algo.kNN.methods.bfMethod.BrutForce;
import clus.algo.kNN.distance.EuclideanDistance;
import clus.algo.kNN.distance.ManhattanDistance;
import clus.algo.kNN.distance.SearchDistance;
import clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import clus.algo.kNN.distance.attributeWeighting.NoWeighting;
import clus.algo.kNN.distance.attributeWeighting.RandomForestWeighting;
import clus.algo.kNN.distance.attributeWeighting.UserDefinedWeighting;
import clus.algo.kNN.distance.distanceWeighting.DistanceWeighting;
import clus.algo.kNN.distance.distanceWeighting.WeightConstant;
import clus.algo.kNN.distance.distanceWeighting.WeightMinus;
import clus.algo.kNN.distance.distanceWeighting.WeightOver;
import clus.algo.kNN.methods.kdTree.KDTree;
import clus.algo.kNN.methods.vpTree.VPTree;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.ext.hierarchical.WHTDStatistic;
import clus.ext.timeseries.TimeSeriesStat;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusDistance;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;
import clus.statistic.StatisticPrintInfo;
import clus.util.ClusException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import jeans.util.MyArray;

/**
 * @author Mitja Pugelj
 */

public class TestKnnModel implements ClusModel, Serializable{
	
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	private ClusRun m_ClusRun;
	protected ClusStatistic m_StatTemplate;
	private int m_NbNeighbors = 1;

	HashMap<String,SearchAlgorithm> m_Algorithms;
	public static HashMap<String,StopWatch> watches;


	public TestKnnModel(ClusRun cr) throws ClusException, IOException{
		m_ClusRun = cr;
		// settings file name; use name for .weight file
		String fName = m_ClusRun.getStatManager().getSettings().getAppName();
		m_NbNeighbors = Integer.parseInt(Settings.kNN_k.getValue());

		// Initialize attribute weighting according to settings file
		AttributeWeighting attrWe = new NoWeighting();;
		String weighting = Settings.kNN_attrWeight.getStringValue();
		boolean loadedWeighting = false;

		if( weighting.toLowerCase().compareTo("none") == 0 ){

		}else if( weighting.startsWith("RF") ){
			try{
				String[] wS = weighting.split(",");
				int nbBags = 100; // Default value
				if( wS.length == 2 )
					nbBags = Integer.parseInt(wS[1]);
				else
					Settings.kNN_attrWeight.setValue(weighting + "," + nbBags);
				attrWe = new RandomForestWeighting(m_ClusRun, nbBags);
			} catch( Exception e ){
				throw new ClusException("Error at reading attributeWeighting value. RF value detected, but error accured while reading number of bags.");
			}
		}else if( weighting.startsWith("[") && weighting.endsWith("]") ){
			try{
				String[] wS = weighting.substring(1, weighting.length()-1).split(",");
				double[] we = new double[wS.length];
				for( int i = 0; i < we.length ; i++ )
					we[i] = Double.parseDouble(wS[i]);
				attrWe = new UserDefinedWeighting(we);
			}catch( Exception e ){
				throw new ClusException("Error at reading attributeWeighting value. User defined entry detected, but value cannot be red..");
			}

		}else{
			// Probably file name.. try to load weighting.
			attrWe = AttributeWeighting.loadFromFile(fName+".weight");
			System.out.println(attrWe.toString());
			if( attrWe != null )
				loadedWeighting = true;
			else
				throw new ClusException("Unrecognized attributeWeighting value ("+weighting+")");
		}
		if( !(attrWe instanceof NoWeighting) && !loadedWeighting ){
			AttributeWeighting.saveToFile(attrWe,fName+".weight");
		}

		// Initialize distance according to settings file
		String dist = Settings.kNN_distance.getStringValue();
		SearchDistance searchDistance = new SearchDistance();
		ClusDistance distance;

		if( dist.toLowerCase().compareTo("chebyshev") == 0 ){
			distance = new ChebyshevDistance(searchDistance);
		}else if( dist.toLowerCase().compareTo("manhattan") == 0 ){
			distance = new ManhattanDistance(searchDistance);
		}else{
			distance = new EuclideanDistance(searchDistance);
		}
		searchDistance.setDistance(distance);
		searchDistance.setWeighting(attrWe);
		// Method testing code:
		m_Algorithms = new HashMap<String,SearchAlgorithm>();
		m_Algorithms.put("bf",new BrutForce(cr,searchDistance));
		m_Algorithms.put("kd",new KDTree(cr,searchDistance));
		m_Algorithms.put("vp",new VPTree(cr,searchDistance));
		if( watches == null ){
			watches = new HashMap<String,StopWatch>();
			watches.put("bf", new StopWatch());
			watches.put("kd", new StopWatch());
			watches.put("vp", new StopWatch());
			watches.put("bfB", new StopWatch());
			watches.put("kdB", new StopWatch());
			watches.put("vpB", new StopWatch());
		}
		for( String alg : m_Algorithms.keySet() ){
			watches.get(alg+"B").start();
			m_Algorithms.get(alg).build();
			watches.get(alg+"B").pause();
		}
		// debug info
		System.out.println("------------------------------------------------");
		System.out.println("Method comparison mode..");
		System.out.println(searchDistance.getBasicDistance().getClass());
		System.out.println(m_NbNeighbors);
		System.out.println(Settings.kNN_distanceWeight.getStringValue());
		System.out.println("------------------------------------------------");

		// save prediction template
		// @todo : should all this be repalced with:
		// statTemplate = cr.getStatManager().getStatistic(ClusAttrType.ATTR_USE_TARGET);
		if( cr.getStatManager().getMode() == ClusStatManager.MODE_CLASSIFY )
			m_StatTemplate = new ClassificationStat(m_ClusRun.getDataSet(ClusRun.TRAINSET).m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
		else if( cr.getStatManager().getMode() == ClusStatManager.MODE_REGRESSION )
			m_StatTemplate = new RegressionStat(m_ClusRun.getDataSet(ClusRun.TRAINSET).m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
		else if( cr.getStatManager().getMode() == ClusStatManager.MODE_TIME_SERIES ){
			// TimeSeriesAttrType attr = this.cr.getDataSet(ClusRun.TRAINSET).m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_TARGET)[0];
			// statTemplate = new TimeSeriesStat(attxr, new DTWTimeSeriesDist(attr), 0 );
			System.out.println("-------------");
			m_StatTemplate = cr.getStatManager().getStatistic(ClusAttrType.ATTR_USE_TARGET);
			System.out.println(m_StatTemplate.getDistanceName());
			System.out.println("----------------");
		}else if( cr.getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL ){
			m_StatTemplate = cr.getStatManager().getStatistic(ClusAttrType.ATTR_USE_TARGET);
			System.out.println("----------------------");
			System.out.println(m_StatTemplate.getDistanceName());
			System.out.println(m_StatTemplate.getClass());
			System.out.println("----------------------");
		}
	}

	public ClusStatistic predictWeighted(DataTuple tuple) {
		// Search for nearest k neighbours
		// LinkedList<DataTuple> nearest = search.returnNNs(tuple,this.k);
		double[] results = new double[m_Algorithms.size()];
		double mean = 0;
		int i = 0;
		LinkedList<DataTuple> nearest = null;
		for( String alg : m_Algorithms.keySet() ){
			StopWatch w = TestKnnModel.watches.get(alg);
			SearchAlgorithm a = m_Algorithms.get(alg);
			w.start();
			nearest = a.returnNNs(tuple, m_NbNeighbors);
			w.pause();
			// debuging - do methods return identical values?
			for( DataTuple t : nearest )
				results[i] += m_Algorithms.get(alg).getDistance().calcDistance(t, tuple);
			results[i] /= nearest.size();
			System.out.println(alg + ": " + i + " " + results[i]);
			mean += results[i];
			i++;
		}
		// debuging - do methods return identical values?
		mean /= results.length;
		for( i = 0; i < m_Algorithms.size() ; i++ ){
			if( results[i] - mean > 1e-8 ){
				System.out.println(this.getClass().getName() + ":predictWeighted() - Something went wrong!");
				System.out.println(results[i] - mean);
				System.out.println(results[i]);
				System.out.println(results[0]);
				System.out.println();
				System.out.println();
				System.exit(1);
			}
		}
		// Initialize distance weighting according to setting file
		DistanceWeighting weighting; String distWeight = Settings.kNN_distanceWeight.getStringValue();
		if ( distWeight.compareTo("1/d") == 0) weighting = new WeightOver(nearest, m_Algorithms.get("bf"), tuple);
		else if ( distWeight.compareTo("1-d") == 0) weighting = new WeightMinus(nearest, m_Algorithms.get("bf"), tuple);
		else weighting = new WeightConstant(nearest, m_Algorithms.get("bf"), tuple);
		// Vote
		ClusStatistic stat = m_StatTemplate.cloneStat();
		if( stat instanceof TimeSeriesStat ){
			for( DataTuple dt : nearest ){
				ClusStatistic dtStat = m_StatTemplate.cloneStat();
				dtStat.setSDataSize(1);
				dtStat.updateWeighted(dt, 0);
				dtStat.computePrediction();
				stat.addPrediction(dtStat, weighting.weight(dt));
			}
			stat.computePrediction();
		}else if( stat instanceof WHTDStatistic ){
			for( DataTuple dt : nearest )
				stat.updateWeighted(dt, weighting.weight(dt));
			stat.calcMean();
		}else{
			for( DataTuple dt : nearest )
				stat.updateWeighted(dt, weighting.weight(dt));
			stat.calcMean();
		}
		return stat;
	}

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
		
	}

	public int getModelSize() {
		return -1;
	}

	public String getModelInfo() {
		return "kNN model";
	}

	public void printModel(PrintWriter wrt) {
		wrt.println("No specific kNN model to write!");
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		wrt.println("No specific kNN model to write!");
		wrt.print(info.toString()); 
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
		throw new UnsupportedOperationException(this.getClass().getName() + ":printModelAndExamples() - Not supported yet for kNN.");
	}

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
		throw new UnsupportedOperationException(this.getClass().getName() + ":printModelToQuery() - Not supported yet for kNN.");
	}

	public void printModelToPythonScript(PrintWriter wrt) {
		throw new UnsupportedOperationException(this.getClass().getName() + ":printModelToPythonScript() - Not supported yet for kNN.");
	}

	public void attachModel(HashMap table) throws ClusException {
		throw new UnsupportedOperationException(this.getClass().getName() + ":attachModel - Not supported yet for kNN.");
	}

	public void retrieveStatistics(ArrayList list) {
		throw new UnsupportedOperationException(this.getClass().getName() + ":retrieveStatistics - Not supported yet for kNN.");
	}

	public ClusModel prune(int prunetype) {
		throw new UnsupportedOperationException(this.getClass().getName() + ":prune - Not supported yet for kNN.");
	}

	public int getID() {
		throw new UnsupportedOperationException(this.getClass().getName() + ":getID - Not supported yet for kNN.");
	}

	public static void debugInfo(Clus clus){
		try{
			System.out.println("--------------");
			System.out.println("K = " + Settings.kNN_k.getValue());
			System.out.println(clus.getSchema().getNbDescriptiveAttributes() + " " + clus.getData().m_Data.length);
			for( String key : TestKnnModel.watches.keySet() ){
				System.out.println(key + " - " + TestKnnModel.watches.get(key).readValue());
			}
			FileWriter f = new FileWriter("output.data",true);
			f.write(clus.getSchema().getRelationName() + "\t\t");
			f.write(clus.getSchema().getNbDescriptiveAttributes() + "\t");
			f.write(clus.getData().m_Data.length+"\t");
			f.write(TestKnnModel.watches.get("bf").readValue() + "\t");
			f.write(TestKnnModel.watches.get("kd").readValue() + "\t");
			f.write(TestKnnModel.watches.get("vp").readValue() + "\t");
			f.flush();
			f.write(TestKnnModel.watches.get("bfB").readValue() + "\t");
			f.write(TestKnnModel.watches.get("kdB").readValue() + "\t");
			f.write(TestKnnModel.watches.get("vpB").readValue() + "\t");
			f.write(SearchAlgorithm.operationsCount[KDTree.ALG_KD] + "\t");
			f.write(SearchAlgorithm.operationsCount[KDTree.ALG_VP] + "");
			f.write("\n");
			f.close();
		}catch( Exception e ){ e.printStackTrace(); }
	}
}

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


import clus.algo.kNN.distance.EuclideanDistance;
import clus.algo.kNN.methods.SearchAlgorithm;
import clus.algo.kNN.methods.bfMethod.BrutForce;
import clus.algo.kNN.distance.SearchDistance;
import clus.algo.kNN.methods.kdTree.KDTree;
import clus.algo.kNN.methods.vpTree.VPTree;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;
import clus.statistic.StatisticPrintInfo;
import clus.util.ClusException;
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

public class oTestKnnModel implements ClusModel, Serializable{
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    HashMap<String,SearchAlgorithm> m_Algorithms;
    public static HashMap<String,StopWatch> m_Watches;
    public static boolean m_Test = false;

    private int m_NbNeighbors = 1;
    protected  ClusStatistic statTemplate;

    public oTestKnnModel(ClusRun cr, int k) throws ClusException, IOException{
    	m_NbNeighbors = k;
        SearchDistance distance = new SearchDistance();
        distance.setDistance(new EuclideanDistance(distance));
        m_Algorithms = new HashMap<String,SearchAlgorithm>();
        /*
         * Select method for searching nearest neighbors.
         */
        m_Algorithms.put("bf",new BrutForce(cr,distance));
        m_Algorithms.put("kd",new KDTree(cr,distance));
        m_Algorithms.put("vp",new VPTree(cr,distance));
//        algorithms.put("pa",new PATree(cr,distance));

        if( m_Watches == null ){
        	m_Watches = new HashMap<String,StopWatch>();
        	m_Watches.put("bf", new StopWatch());
        	m_Watches.put("kd", new StopWatch());
        	m_Watches.put("vp", new StopWatch());
        	m_Watches.put("bfB", new StopWatch());
        	m_Watches.put("kdB", new StopWatch());
        	m_Watches.put("vpB", new StopWatch());
        }
        // build tree, pre-processing
        for( String alg : m_Algorithms.keySet() ){
        	m_Watches.get(alg+"B").start();
            m_Algorithms.get(alg).build();
            m_Watches.get(alg+"B").pause();
        }
        // save prediction template
        if( ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY )
            statTemplate = new ClassificationStat(cr.getDataSet(ClusRun.TRAINSET).m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
        else if( ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION )
            statTemplate = new RegressionStat(cr.getDataSet(ClusRun.TRAINSET).m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
    }

    public ClusStatistic predictWeighted(DataTuple tuple) {
        if( m_Test ){
            double[] results = new double[m_Algorithms.size()];
            double mean = 0;
            int i = 0;
            for( String alg : m_Algorithms.keySet() ){
                StopWatch w = oTestKnnModel.m_Watches.get(alg);
                SearchAlgorithm a = m_Algorithms.get(alg);
                w.start();
                LinkedList<DataTuple> tmpNearest = a.returnNNs(tuple, m_NbNeighbors);
                w.pause();
                for( DataTuple t : tmpNearest )
                    results[i] += m_Algorithms.get(alg).getDistance().calcDistance(t, tuple);
    
                results[i] /= tmpNearest.size();
//                System.out.println("\t"+results[i]);
                mean += results[i];
                i++;
            }

            mean /= results.length;
//            double diff = 0;
            for( i = 0; i < m_Algorithms.size() ; i++ ){
                if( results[i] - mean > 1e-8 ){
                    System.out.println(this.getClass().getName() + ": Something went wrong!!!");
                    System.out.println(results[i] - mean);
                    System.out.println(results[i]);
                    System.out.println(results[0]); 
                    System.out.println("");
                    System.out.println("");
                    System.exit(1);
                }
            }
        }
        // Clut things
        ClusStatistic stat = statTemplate.cloneStat();
        stat.updateWeighted(tuple, 1);
        stat.calcMean();
        return stat;
    }
    
    public static void setTest(boolean value){
    	m_Test = value;
    }
    
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
        throw new UnsupportedOperationException(this.getClass().getName() + ": applyModelProcessors is not supported yet.");
    }

    public int getModelSize() {
    	System.out.println("No specific model size for kNN model.");
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
}

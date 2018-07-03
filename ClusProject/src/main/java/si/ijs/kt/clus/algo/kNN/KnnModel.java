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
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.NoWeighting;
import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.RandomForestWeighting;
import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.UserDefinedWeighting;
import si.ijs.kt.clus.algo.kNN.distance.distanceWeighting.DistanceWeighting;
import si.ijs.kt.clus.algo.kNN.distance.distanceWeighting.WeightConstant;
import si.ijs.kt.clus.algo.kNN.distance.distanceWeighting.WeightMinus;
import si.ijs.kt.clus.algo.kNN.distance.distanceWeighting.WeightOver;
import si.ijs.kt.clus.algo.kNN.methods.SearchAlgorithm;
import si.ijs.kt.clus.algo.kNN.methods.bfMethod.BruteForce;
import si.ijs.kt.clus.algo.kNN.methods.bfMethod.OracleBruteForce;
import si.ijs.kt.clus.algo.kNN.methods.kdTree.KDTree;
import si.ijs.kt.clus.algo.kNN.methods.vpTree.VPTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.primitive.ChebyshevDistance;
import si.ijs.kt.clus.distance.primitive.EuclideanDistance;
import si.ijs.kt.clus.distance.primitive.ManhattanDistance;
import si.ijs.kt.clus.distance.primitive.SearchDistance;
import si.ijs.kt.clus.ext.timeseries.TimeSeriesStat;
import si.ijs.kt.clus.main.ClusModelInfoList;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.Distance;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.DistanceWeights;
import si.ijs.kt.clus.main.settings.section.SettingsKNN.SearchMethod;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


/**
 *
 * @author Mitja Pugelj, matejp
 */
public class KnnModel implements ClusModel, Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private SearchAlgorithm search;
    private DistanceWeights weightingOption;
    private ClusRun cr;
    protected ClusStatistic statTemplate;
    private int m_K = 1; // the number of nearest neighbours, see also https://www.youtube.com/watch?v=KqOsrniBooQ
    private int m_MaxK = 1; // maximal number of neighbours among the master itself and his 'workers': for efficient use
                            // of predictWeighted in Clus.calcError()
    private DataTuple m_CurrentTuple; // used for efficient use of predictWeighted in Clus.calcError()
    private LinkedList<DataTuple> m_CurrentNeighbours; // m_MaxK nearest neighbours of m_CurrentTuple
    private KnnModel m_Master = null;


    // Slave mode - this model is used only for voting, searching is done by master
    public KnnModel(ClusRun cr, int k, DistanceWeights weighting, KnnModel master) {
        this.cr = cr;
        this.m_K = k;
        this.m_MaxK = Math.max(this.m_K, master.m_MaxK);
        master.m_MaxK = this.m_MaxK;
        this.weightingOption = weighting;
        this.search = master.search;
        this.statTemplate = master.statTemplate;
        this.m_Master = master;
    }


    // Default constructor.
   	@SuppressWarnings("unused")
    public KnnModel(ClusRun cr, int k, DistanceWeights weighting, int maxK, boolean isSparse, ClusAttrType[] necessaryDescriptiveAttributes) throws ClusException, IOException, InterruptedException {
        this.cr = cr;
        this.m_K = k;
        this.m_MaxK = Math.max(Math.max(this.m_K, this.m_MaxK), maxK);
        this.weightingOption = weighting;
        // settings file name; use name for .weight file
        String fName = this.cr.getStatManager().getSettings().getGeneric().getAppName();
        // Initialize attribute weighting according to settings file
        AttributeWeighting attrWe = new NoWeighting();

        String attrWeighting = this.cr.getStatManager().getSettings().getKNN().getKNNAttrWeight();
        boolean loadedWeighting = false;
        if (attrWeighting.toLowerCase().compareTo("none") == 0) {

        }
        else if (attrWeighting.startsWith("RF")) {
            try {
                String[] wS = attrWeighting.split(",");
                int nbBags = 100; // Default value
                if (wS.length == 2)
                    nbBags = Integer.parseInt(wS[1]);
                else
                    this.cr.getStatManager().getSettings().getKNN().setKNNAttrWeight(attrWeighting + "," + nbBags);
                attrWe = new RandomForestWeighting(this.cr, nbBags);
            }
            catch (Exception e) {
                throw new ClusException("Error at reading attributeWeighting value. RF value detected, but error accured while reading number of bags.");
            }
        }
        else if (attrWeighting.startsWith("[") && attrWeighting.endsWith("]")) {
            try {
                String[] wS = attrWeighting.substring(1, attrWeighting.length() - 1).split(",");
                double[] we = new double[wS.length];
                for (int i = 0; i < we.length; i++)
                    we[i] = Double.parseDouble(wS[i]);
                attrWe = new UserDefinedWeighting(we);
            }
            catch (Exception e) {
                throw new ClusException("Error at reading attributeWeighting value. User defined entry detected, but value cannot be read.");
            }

        }
        else {
            // Probably file name.. try to load weighting.
            attrWe = AttributeWeighting.loadFromFile(fName + ".weight");
            System.out.println(attrWe.toString());
            if (attrWe != null)
                loadedWeighting = true;
            else
                throw new ClusException("Unrecognized attributeWeighting value (" + attrWeighting + ")");
        }

        if (!(attrWe instanceof NoWeighting) && !loadedWeighting) {
            AttributeWeighting.saveToFile(attrWe, fName + ".weight");
        }

        // Initialize distance according to settings file
        // int dist = this.cr.getStatManager().getSettings().getKNN().getKNNDistance();
        Distance dist = this.cr.getStatManager().getSettings().getKNN().getDistance();

        SearchDistance searchDistance = new SearchDistance();
        ClusDistance distance;

        switch(dist) {
	        case Euclidean:
	        	distance = new EuclideanDistance(searchDistance, isSparse, necessaryDescriptiveAttributes);
	        	break;
	        case Chebyshev:
	        	distance = new ChebyshevDistance(searchDistance, isSparse, necessaryDescriptiveAttributes);
	        	break;
	        case Manhattan:
	        	distance = new ManhattanDistance(searchDistance, isSparse, necessaryDescriptiveAttributes);
	        	break;
        	default:
        		throw new RuntimeException("Wrong distance.");
        }

        // initialize min values of numeric attributes: needed for normalization in the distance computation
        int[] data_types = new int[] { ClusModelInfoList.TRAINSET, ClusModelInfoList.TESTSET, ClusModelInfoList.VALIDATIONSET };
        double[] mins = null;
        double[] maxs = null;
        int nb_attrs = -1;
        for (int type = 0; type < data_types.length; type++) {
            RowData data = cr.getDataSet(type);
            ClusSchema schema = cr.getStatManager().getSchema();
            if (data != null) {
                if (mins == null) {
                    nb_attrs = schema.getNbAttributes();
                    mins = new double[nb_attrs];
                    Arrays.fill(mins, Double.POSITIVE_INFINITY);
                    maxs = new double[nb_attrs];
                    Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
                }
                // compute max and min for every numeric attribute
                for (int tuple_ind = 0; tuple_ind < data.getNbRows(); tuple_ind++) {
                    for (int i = 0; i < nb_attrs; i++) {
                        ClusAttrType attr_type = schema.getAttrType(i);
                        if (!attr_type.isDisabled() && attr_type instanceof NumericAttrType) {
                            double t = attr_type.getNumeric(data.getTuple(tuple_ind));
                            if (t < mins[i] && t != Double.POSITIVE_INFINITY) {
                                mins[i] = t;
                            }
                            if (t > maxs[i] && t != Double.POSITIVE_INFINITY) {
                                maxs[i] = t;
                            }
                        }
                    }
                }
            }
        }

        searchDistance.setDistance(distance);
        distance.setWeighting(attrWe);
        searchDistance.setNormalizationWeights(mins, maxs);

        // Select search method according to settings file.
        Settings sett = this.cr.getStatManager().getSettings();
        SearchMethod searchMethod = sett.getKNN().getSearchMethod();
        switch (searchMethod) {
            case VPTree:
                this.search = new VPTree(this.cr, searchDistance);
                break;
            case KDTree:
                this.search = new KDTree(this.cr, searchDistance);
                break;
            case BruteForce:
                this.search = new BruteForce(this.cr, searchDistance);
                break;
            case Oracle:
                this.search = new OracleBruteForce(this.cr, searchDistance);
                break;
            default:
                throw new RuntimeException("Wrong search method: " + searchMethod.toString());
        }

        // debug info
        if (this.cr.getStatManager().getSettings().getGeneral().getVerbose() >= 1) {
            System.out.println("Search method: " + this.search.getClass());
            System.out.println("Search distance: " + searchDistance.getBasicDistance().getClass());
            System.out.println("Number of neighbours: " + this.m_K);
            System.out.println("Distance weights: " + sett.getKNN().getKNNDistanceWeights());
            
        }

        // build tree, preprocessing
        this.search.build(m_MaxK);
        RowData train = this.cr.getDataSet(ClusModelInfoList.TRAINSET);
        RowData test = this.cr.getDataSet(ClusModelInfoList.TESTSET);
        if (searchMethod == SearchMethod.Oracle) {
            if (sett.getKNN().mustNotComputeTrainingError(train.getNbRows())) {
                sett.getOutput().setOutTrainError(false);
                System.err.println("Training error will not be computed, since we do not know the neighbours for each training instance.");
            }
            if (test != null && sett.getKNN().mustNotComputeTestError(test.getNbRows())) {
                sett.getOutput().setOutTestError(false);
                System.err.println("Testing error will not be computed, since we do not know the neighbours for each testing instance.");
            }
        }

        // save prediction template
        // @todo : should all this be repalced with:
        statTemplate = cr.getStatManager().getStatistic(AttributeUseType.Target);

        // if( cr.getStatManager().getMode() == ClusStatManager.MODE_CLASSIFY ){
        // if(cr.getStatManager().getSettings().getSectionMultiLabel().isEnabled()){
        // statTemplate = new
        // ClassificationStat(this.cr.getDataSet(ClusRun.TRAINSET).m_Schema.getNominalAttrUse(AttributeUseType.Target),
        // cr.getStatManager().getSettings().getMultiLabelTrheshold());
        // } else{
        // statTemplate = new
        // ClassificationStat(this.cr.getDataSet(ClusRun.TRAINSET).m_Schema.getNominalAttrUse(AttributeUseType.Target));
        // }
        // }
        // else if( cr.getStatManager().getMode() == ClusStatManager.MODE_REGRESSION )
        // statTemplate = new
        // RegressionStat(this.cr.getDataSet(ClusRun.TRAINSET).m_Schema.getNumericAttrUse(AttributeUseType.Target));
        // else if( cr.getStatManager().getMode() == ClusStatManager.MODE_TIME_SERIES ){
        // // TimeSeriesAttrType attr =
        // this.cr.getDataSet(ClusRun.TRAINSET).m_Schema.getTimeSeriesAttrUse(AttributeUseType.Target)[0];
        // // statTemplate = new TimeSeriesStat(attxr, new DTWTimeSeriesDist(attr), 0 );
        // System.out.println("-------------");
        // statTemplate = cr.getStatManager().getStatistic(AttributeUseType.Target);
        // System.out.println(statTemplate.getDistanceName());
        // System.out.println("----------------");
        // }else if( cr.getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL ){
        // statTemplate = cr.getStatManager().getStatistic(AttributeUseType.Target);
        // System.out.println("----------------------");
        // System.out.println(statTemplate.getDistanceName());
        // System.out.println(statTemplate.getClass());
        // System.out.println("----------------------");
        // }
    }


    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException {
        LinkedList<DataTuple> nearest = new LinkedList<DataTuple>(); // the first m_K neigbhours of the m_MaxK
                                                                     // neighbours: OK, because the neighbours are
                                                                     // sorted from the nearest to the farthest

        if (this.m_Master == null) { // this is the master
            this.m_CurrentNeighbours = this.search.returnNNs(tuple, this.m_MaxK);
            this.m_CurrentTuple = tuple;
            for (int neighbour = 0; neighbour < this.m_K; neighbour++) {
                nearest.add(this.m_CurrentNeighbours.get(neighbour));
            }
        }
        else { // this is a slave
            if (this.m_Master.m_CurrentTuple != tuple) { throw new RuntimeException("The neighbours were computed for tuple\n" + this.m_Master.m_CurrentTuple.toString() + "\nbut now, we are dealing with tuple\n" + tuple.toString()); }
            for (int neighbour = 0; neighbour < this.m_K; neighbour++) {
                nearest.add(this.m_Master.m_CurrentNeighbours.get(neighbour));
            }
        }

        // Initialize distance weighting according to setting file
        DistanceWeighting weighting;
        switch (this.weightingOption) {
            case OneOverD:
                weighting = new WeightOver(nearest, this.search, tuple);
                break;

            case OneMinusD:
                weighting = new WeightMinus(nearest, this.search, tuple);
                break;

            case Constant:
                weighting = new WeightConstant(nearest, this.search, tuple);
                break;

            default:
                throw new RuntimeException("DistanceWeights unknown!");
        }

        // Vote
        ClusStatistic stat = statTemplate.cloneStat();
        if (stat instanceof TimeSeriesStat) {
            for (DataTuple dt : nearest) {
                ClusStatistic dtStat = statTemplate.cloneStat();
                dtStat.setSDataSize(1);
                dtStat.updateWeighted(dt, 0);
                dtStat.computePrediction();
                stat.addPrediction(dtStat, weighting.weight(dt));
            }
            stat.computePrediction();
        }
        else if (stat instanceof WHTDStatistic) {
            for (DataTuple dt : nearest)
                stat.updateWeighted(dt, weighting.weight(dt));
            stat.calcMean();
        }
        else {
            for (DataTuple dt : nearest)
                stat.updateWeighted(dt, weighting.weight(dt));
            stat.calcMean();
        }
        return stat;
    }


    @Override
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
        // System.out.println("-----------");
    }


    @Override
    public int getModelSize() {
        System.out.println("No specific model size for kNN model.");
        return -1;
    }


    @Override
    public String getModelInfo() {
        return "kNN model weighted with " + this.weightingOption.toString() + " and " + m_K + " neighbors.";
    }


    @Override
    public void printModel(PrintWriter wrt) {
        wrt.println("No specific kNN model to write!");
    }


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        wrt.println("No specific kNN model to write!");
        wrt.print(info.toString());
    }


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":printModelAndExamples() - Not supported yet for kNN.");
    }


    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":printModelToQuery() - Not supported yet for kNN.");
    }


    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":printModelToPythonScript() - Not supported yet for kNN.");
    }


    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices, String modelIdentifier) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":printModelToPythonScript() - Not supported yet for kNN.");
    }


    @Override
    public JsonObject getModelJSON() {
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info) {
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples) {
        return null;
    }


    @Override
    public void attachModel(HashMap table) throws ClusException {
        throw new UnsupportedOperationException(this.getClass().getName() + ":attachModel - Not supported yet for kNN.");
    }


    @Override
    public void retrieveStatistics(ArrayList list) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":retrieveStatistics - Not supported yet for kNN.");
    }


    @Override
    public ClusModel prune(int prunetype) {
        throw new UnsupportedOperationException(this.getClass().getName() + ":prune - Not supported yet for kNN.");
    }


    @Override
    public int getID() {
        throw new UnsupportedOperationException(this.getClass().getName() + ":getID - Not supported yet for kNN.");
    }

}

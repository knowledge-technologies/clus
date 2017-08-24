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

package si.ijs.kt.clus.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public interface ClusModel {

    // MODEL TYPES, used at least for ClusModelInfoList functions
    /** The type of model returned. Default = the model that always predicts the mean of data set. */
    public static int DEFAULT = 0;
    /**
     * The type of model returned. Original = the real model but without pruning.
     * Often stored for comparison
     */
    public static int ORIGINAL = 1;
    /** The type of model returned. PRUNED = the real model after pruning. */
    public static int PRUNED = 2;
    public static int RULES = 2;

    // SOMETHING ELSE
    public static int PRUNE_INVALID = 0;

    public static int TRAIN = 0;
    public static int TEST = 1;


    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException, InterruptedException;


    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException, ClusException;


    public int getModelSize();


    public String getModelInfo() throws InterruptedException;


    public void printModel(PrintWriter wrt) throws InterruptedException;


    public void printModel(PrintWriter wrt, StatisticPrintInfo info) throws InterruptedException;


    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples);


    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive);


    public void printModelToPythonScript(PrintWriter wrt);


    public JsonObject getModelJSON();


    public JsonObject getModelJSON(StatisticPrintInfo info);


    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples);


    public void attachModel(HashMap table) throws ClusException;


    public void retrieveStatistics(ArrayList list);


    public ClusModel prune(int prunetype) throws ClusException;


    public int getID();
}

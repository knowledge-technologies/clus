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

package si.ijs.kt.clus.addon.hmc.HMCAverageSingleClass;

/*
 * Created on Jan 18, 2006
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public class HMCAverageTreeModel implements ClusModel {

    protected int m_DataSet, m_Trees, m_TotSize;
    protected WHTDStatistic m_Target;
    protected double[][][] m_PredProb;


    public HMCAverageTreeModel(ClusStatistic target, double[][][] predprop, int trees, int size) {
        m_Target = (WHTDStatistic) target;
        m_PredProb = predprop;
        m_Trees = trees;
        m_TotSize = size;
    }


    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) {
        WHTDStatistic stat = (WHTDStatistic) m_Target.cloneSimple();
        stat.setMeans(m_PredProb[m_DataSet][tuple.getIndex()]);
        return stat;
    }


    @Override
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
    }


    @Override
    public int getModelSize() {
        return 0;
    }


    @Override
    public String getModelInfo() {
        return "Combined model with " + m_Trees + " trees with " + m_TotSize + " nodes";
    }


    @Override
    public void printModel(PrintWriter wrt) {
        wrt.println(getModelInfo());
    }


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        printModel(wrt);
    }


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        printModel(wrt);
    }


    @Override
    public void printModelToPythonScript(PrintWriter wrt) {
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
    }


    @Override
    public ClusModel prune(int prunetype) {
        return this;
    }


    @Override
    public int getID() {
        return 0;
    }


    @Override
    public void retrieveStatistics(ArrayList stats) {
    }


    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int a, int b, boolean ex) {
    }


    public void setDataSet(int set) {
        m_DataSet = set;
    }
}

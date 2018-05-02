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

package si.ijs.kt.clus.ext.optiontree;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.Global;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.GeneticDistanceStat;
import si.ijs.kt.clus.statistic.HierSingleLabelStat;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public class ClusOptionNode extends MyNode {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private int m_ID;
    private double m_HeuristicRatios[];
    private String m_OptionNodeText = "[option node]";


    public double[] getHeuristicRatios() {
        return m_HeuristicRatios;
    }


    public void setHeuristicRatios(double[] m_HeuristicRatios) {
        this.m_HeuristicRatios = m_HeuristicRatios;
    }


    public void setHeuristicRatio(int position, double value) {
        this.m_HeuristicRatios[position] = value;
    }


    @Override
    public MyNode cloneNode() {
        ClusOptionNode clone = new ClusOptionNode();
        clone.m_ClusteringStat = m_ClusteringStat;
        clone.m_TargetStat = m_TargetStat;
        return clone;
    }


    @Override
    public int getID() {
        return m_ID;
    }


    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException, InterruptedException {
        if (atBottomLevel()) {
            throw new RuntimeException("clus.ext.optiontree.ClusOptionNode.predictWeighted(DataTuple): This should never happen");
        }
        else {
            int nb_c = getNbChildren();
            ArrayList<ClusStatistic> votes = new ArrayList<ClusStatistic>();
            for (int i = 0; i < nb_c; i++) {
                votes.add(getChild(i).predictWeighted(tuple));
            }
            ClusStatistic outStat = newClusStatistic();
            outStat.vote(votes);
            outStat.computePrediction();
            return outStat;
        }
    }


    protected ClusStatistic newClusStatistic() {
        ClusStatistic outStat = null;

        switch (m_StatManager.getMode()) {
            case ClusStatManager.MODE_CLASSIFY:
                outStat = new ClassificationStat(m_StatManager.getSettings(), m_StatManager.getSchema().getNominalAttrUse(AttributeUseType.Target));
                break;

            case ClusStatManager.MODE_REGRESSION:
                outStat = new RegressionStat(m_StatManager.getSettings(), m_StatManager.getSchema().getNumericAttrUse(AttributeUseType.Target));
                break;

            case ClusStatManager.MODE_HIERARCHICAL:
                if (m_StatManager.getSettings().getHMLC().getHierSingleLabel()) {
                    outStat = new HierSingleLabelStat(m_StatManager.getSettings(), m_StatManager.getHier());
                }
                else {
                    outStat = new WHTDStatistic(m_StatManager.getSettings(), m_StatManager.getHier());
                }
                break;

            case ClusStatManager.MODE_PHYLO:
                outStat = new GeneticDistanceStat(m_StatManager.getSettings(), m_StatManager.getSchema().getNominalAttrUse(AttributeUseType.Target));
                break;

            default:
                throw new RuntimeException(getClass().getName() + " newClusStatistic(): Error initializing the statistic " + m_StatManager.getMode());
        }

        return outStat;
    }


    @Override
    public void printModel(PrintWriter wrt) {
        printTree(wrt, StatisticPrintInfo.getInstance(), "");
    }


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        printTree(wrt, info, "");
    }


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        printTree(wrt, info, "", examples);
    }


    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices) {
        printTreeToPythonScript(wrt, "\t");
    }
    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices, String modelIdentifier) {
    	printModelToPythonScript(wrt, indices);
    }


    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
        int lastmodel = cr.getNbModels() - 1;
        System.out.println("The number of models to print is:" + lastmodel);
        String[][] tabitem = new String[lastmodel + 1][10000]; //table of item
        int[][] tabexist = new int[lastmodel + 1][10000]; //table of booleen for each item
        Global.set_treecpt(starttree);
        Global.set_itemsetcpt(startitem);
        //ClusModelInfo m = cr.getModelInfo(0);//cr.getModelInfo(lastmodel);

        if (exhaustive) {
            for (int i = 0; i < cr.getNbModels(); i++) {
                ClusModelInfo mod = cr.getModelInfo(i);
                MyNode tree = (MyNode) cr.getModel(i);
                if (tree.getNbChildren() != 0) {
                    tree.printTreeInDatabase(wrt, tabitem[i], tabexist[i], 0, "all_trees");
                }
                //   print the statitistics here (the format depend on the needs of the plsql program)
                if (tree.getNbNodes() <= 1) { //we only look for the majority class in the data
                    double error_rate = (tree.m_ClusteringStat).getErrorRel();
                    wrt.println("#" + (tree.m_ClusteringStat).getPredictedClassName(0));
                    wrt.println(mod.getModelSize() + ", " + error_rate + ", " + (1 - error_rate));
                }
                else {
                    //writer.println("INSERT INTO trees_charac VALUES(T1,"+size+error+accuracy+constraint);
                    wrt.println(mod.getModelSize() + ", " + (mod.m_TrainErr).getErrorClassif() + ", " + (mod.m_TrainErr).getErrorAccuracy());
                }
                Global.inc_treecpt();
            } //end for
        } //end if
        else { //greedy search
            ClusModelInfo mod = cr.getModelInfo(lastmodel);
            MyNode tree = (MyNode) cr.getModel(lastmodel);
            tabitem[lastmodel][0] = "null";
            tabexist[lastmodel][0] = 1;
            wrt.println("INSERT INTO trees_sets VALUES(" + Global.get_itemsetcpt() + ", '" + tabitem[lastmodel][0] + "', " + tabexist[lastmodel][0] + ")");
            wrt.println("INSERT INTO greedy_trees VALUES(" + Global.get_treecpt() + ", " + Global.get_itemsetcpt() + ",1)");
            Global.inc_itemsetcpt();
            if (tree.getNbChildren() != 0) {
                printTreeInDatabase(wrt, tabitem[lastmodel], tabexist[lastmodel], 1, "greedy_trees");
            }
            wrt.println("INSERT INTO trees_charac VALUES(" + Global.get_treecpt() + ", " + mod.getModelSize() + ", " + (mod.m_TrainErr).getErrorClassif() + ", " + (mod.m_TrainErr).getErrorAccuracy() + ", NULL)");
            Global.inc_treecpt();
        }
    }


    public final void printTree() {
        PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
        printTree(wrt, StatisticPrintInfo.getInstance(), "");
        wrt.flush();
    }


    public final void writeDistributionForInternalNode(PrintWriter writer, StatisticPrintInfo info) {
        if (info.INTERNAL_DISTR) {
            if (m_TargetStat != null) {
                writer.print(": " + m_TargetStat.getString(info));
            }
        }
        writer.println();
    }


    public final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix) {
        printTree(writer, info, prefix, null);
    }


    @Override
    public final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix, RowData examples) {
        int arity = getNbChildren();
        if (arity > 0) {
            writer.println(m_OptionNodeText);
            for (int i = 0; i < arity; i++) {
                writer.print(prefix + "+--o" + (i + 1) + " (" + m_HeuristicRatios[i] + "): ");
                if (i != arity - 1)
                    getChild(i).printTree(writer, info, prefix + "|       ", examples);
                else
                    getChild(i).printTree(writer, info, prefix + "        ", examples);
            }
        }
        else {//on the leaves
            throw new RuntimeException(getClass().getName() + ": should never happen");
        }
    }


    /* to print the tree directly into an IDB : Elisa Fromont 13/06/2007 */
    @Override
    public final void printTreeInDatabase(PrintWriter writer, String tabitem[], int tabexist[], int cpt, String typetree) {
        int arity = getNbChildren();
        if (arity > 0) {
            // TODO figure this out

            tabitem[cpt] = m_OptionNodeText;
            tabexist[cpt] = 1;
            cpt++;
            for (int i = 0; i < arity; i++) {
                getChild(i).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                cpt--;
            }
        } //end if arity >0 0
    }


    public String printTestNode(String a, int pres) {
        if (pres == 1) {
            return a;
        }
        else {
            return ("not(" + a + ")");
        }
    }


    public final void printTreeToPythonScript(PrintWriter writer, String prefix) {
        return;
    }


    @Override
    public int getMaxLeafDepth() {
        int nb = getNbChildren();
        if (nb == 0) {
            return 1;
        }
        else {
            int max = 0;
            for (int i = 0; i < nb; i++) {
                MyNode node = getChild(i);
                max = Math.max(max, node.getMaxLeafDepth());
            }
            return max;
        }
    }


    @Override
    public int getLevel() {
        int depth = 0;
        Node node = getParent();
        while (node != null) {
            if (node instanceof ClusSplitNode)
                depth++;
            node = node.getParent();
        }
        return depth;
    }


    @Override
    public String getModelInfo() {
        return "Nodes = " + getNbNodes() + " (Leaves: " + getNbLeaves() + ", OptionNodes: " + getNbOptionNodes() + ", Options: " + getNbOptions() + ")";
    }


    @Override
    public JsonObject getModelJSON() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void attachModel(HashMap table) throws ClusException {
        // TODO Auto-generated method stub
    }


    @Override
    public void retrieveStatistics(ArrayList list) {
        // TODO Auto-generated method stub
    }


    @Override
    public void applyModelProcessors(DataTuple tuple, MyArray mproc)
            throws IOException {
        // TODO Auto-generated method stub        
    }


    @Override
    public int getModelSize() {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public ClusModel prune(int prunetype) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void makeLeaf() {
        // TODO Auto-generated method stub        
    }
}

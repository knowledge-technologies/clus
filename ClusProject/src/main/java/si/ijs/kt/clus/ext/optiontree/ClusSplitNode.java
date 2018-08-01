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

package si.ijs.kt.clus.ext.optiontree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.Global;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;
import si.ijs.kt.clus.util.jeans.util.StringUtils;

public class ClusSplitNode extends MyNode {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    private final static int YES = 0;
    private final static int NO = 1;
    private final static int UNK = 2;
    
    private int m_ID;    
    private NodeTest m_Test;    
    
    @Override
    public int getID() {
        return m_ID;
    }
    
    @Override
    public final int getMaxLeafDepth() {
        int nb = getNbChildren();
        if (nb == 0) {
            return 1;
        } else {
            int max = 0;
            for (int i = 0; i < nb; i++) {
                MyNode node = getChild(i);
                max = Math.max(max, node.getMaxLeafDepth());
            }
            return max + 1;
        }
    }
    
    @Override
    public final int getLevel() {
        int depth = 0;
        Node node = getParent();
        while (node != null) {
            if (node instanceof ClusSplitNode) depth++;
            node = node.getParent();
        }
        return depth;
    }


    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) throws ClusException, InterruptedException {
        if (atBottomLevel()) {
        	getTargetStat().calcMean();
            return getTargetStat();
        } else {
            int n_idx = m_Test.predictWeighted(tuple);
            if (n_idx != -1) {
                MyNode info = getChild(n_idx);
                return info.predictWeighted(tuple);
            } else {
                int nb_c = getNbChildren();
                MyNode ch_0 = getChild(0);
                ClusStatistic ch_0s = ch_0.predictWeighted(tuple);
                ClusStatistic stat = ch_0s.cloneSimple();
                stat.addPrediction(ch_0s, m_Test.getProportion(0));
                for (int i = 1; i < nb_c; i++) {
                    MyNode ch_i = getChild(i);
                    ClusStatistic ch_is = ch_i.predictWeighted(tuple);
                    stat.addPrediction(ch_is, m_Test.getProportion(i));
                }
                stat.computePrediction();
                return stat;
            }
        }
    }
    
    @Override
    public String getModelInfo() {
        return "Nodes = "+getNbNodes()+" (Leaves: "+getNbLeaves()+", OptionNodes: "+getNbOptionNodes()+", Options: "+getNbOptions()+")";
    }

    public final String getTestString() {
        return m_Test != null ? m_Test.getString() : "None";
    }
    
    /***************************************************************************
     * Inspectors concerning test
     ***************************************************************************/

    private final boolean hasBestTest() {
        return m_Test != null;
    }

    /***************************************************************************
     * Printing the tree ?
     ***************************************************************************/

    // FIXME - what for NominalTests with only two possible outcomes?

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
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices){
        printTreeToPythonScript(wrt, "\t");  // This is probably obsolete, 18. 12. 2017, matejp
    }
    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices, String modelIdentifier){
    	printModelToPythonScript(wrt, indices);
    }

    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive){
        int lastmodel = cr.getNbModels()-1;
        System.out.println("The number of models to print is:"+lastmodel);
        String [][] tabitem = new String[lastmodel+1][10000]; //table of item
        int [][] tabexist = new int[lastmodel+1][10000]; //table of boolean for each item
        Global.set_treecpt(starttree);
        Global.set_itemsetcpt(startitem);
        //ClusModelInfo m = cr.getModelInfo(0);//cr.getModelInfo(lastmodel);

        if(exhaustive){
            for (int i = 0; i < cr.getNbModels(); i++) {
                ClusModelInfo mod = cr.getModelInfo(i);
                MyNode tree = (MyNode) cr.getModel(i);
                if (tree.getNbChildren() != 0) {
                     tree.printTreeInDatabase(wrt,tabitem[i],tabexist[i], 0,"all_trees");
                }
                //   print the statistics here (the format depend on the needs of the plsql program)
                if (tree.getNbNodes() <= 1) { //we only look for the majority class in the data
                     double error_rate = (tree.m_ClusteringStat).getErrorRel();
                     wrt.println("#"+(tree.m_ClusteringStat).getPredictedClassName(0));
                     wrt.println(mod.getModelSize()+", "+error_rate+", "+(1-error_rate));
                }else{
                    //writer.println("INSERT INTO trees_charac VALUES(T1,"+size+error+accuracy+constraint);
                    wrt.println(mod.getModelSize()+", "+(mod.m_TrainErr).getErrorClassif()+", "+(mod.m_TrainErr).getErrorAccuracy());
                }
                Global.inc_treecpt();
            }//end for
        }//end if
        else { //greedy search
            ClusModelInfo mod = cr.getModelInfo(lastmodel);
            MyNode tree = (MyNode)cr.getModel(lastmodel);
            tabitem[lastmodel][0] = "null";
            tabexist[lastmodel][0] = 1;
            wrt.println("INSERT INTO trees_sets VALUES("+Global.get_itemsetcpt()+", '"+tabitem[lastmodel][0]+"', "+tabexist[lastmodel][0]+")");
            wrt.println("INSERT INTO greedy_trees VALUES("+Global.get_treecpt()+", "+Global.get_itemsetcpt()+",1)");
            Global.inc_itemsetcpt();
            if(tree.getNbChildren() != 0){
                printTreeInDatabase(wrt,tabitem[lastmodel],tabexist[lastmodel], 1,"greedy_trees");
            }
            wrt.println("INSERT INTO trees_charac VALUES("+Global.get_treecpt()+", "+mod.getModelSize()+", "+(mod.m_TrainErr).getErrorClassif()+", "+(mod.m_TrainErr).getErrorAccuracy()+", NULL)");
            Global.inc_treecpt();
        }
    }

    private final void writeDistributionForInternalNode(PrintWriter writer, StatisticPrintInfo info) {
        if (info.INTERNAL_DISTR) {
            if (m_TargetStat != null) {
                writer.print(": "+m_TargetStat.getString(info));
            }
        }
        writer.println();
    }

    private final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix) {
        printTree( writer,  info,  prefix, null);
    }

    @Override
    public final void printTree(PrintWriter writer, StatisticPrintInfo info, String prefix, RowData examples) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) {
                writer.print(m_Test.getTestString());

                RowData examples0 = null;
                RowData examples1 = null;
                if (examples!=null){
                    examples0 = examples.apply(m_Test, 0);
                    examples1 = examples.apply(m_Test, 1);
                }           
                // showAlternatives(writer);
                writeDistributionForInternalNode(writer, info);
                // FIXME this needs to be dependent on node type
                writer.print(prefix + "+--yes: ");
                (getChild(YES)).printTree(writer, info, prefix+"|       ", examples0);
                writer.print(prefix + "+--no:  ");
                if (hasUnknownBranch()) {
                    (getChild(NO)).printTree(writer, info, prefix+"|       ", examples1);
                    writer.print(prefix + "+--unk: ");
                    (getChild(UNK)).printTree(writer, info, prefix+"        ", examples0);
                } else {
                    (getChild(NO)).printTree(writer, info, prefix+"        ", examples1);
                }
            } else {
                writer.println(m_Test.getTestString());
                for (int i = 0; i < arity; i++) {
                    MyNode child = getChild(i);
                    String branchlabel = m_Test.getBranchLabel(i);
                    RowData examplesi = null;
                    if (examples!=null){
                        examples.apply(m_Test, i);
                    }
                    writer.print(prefix + "+--" + branchlabel + ": ");
                    String suffix = StringUtils.makeString(' ', branchlabel.length()+4);
                    if (i != arity-1) {
                        child.printTree(writer, info, prefix+"|"+suffix,examplesi);
                    } else {
                        child.printTree(writer, info, prefix+" "+suffix,examplesi);
                    }
                }
            }
        } else {//on the leaves
            if (m_TargetStat == null) {
                writer.print("?");
            } else {
                writer.print(m_TargetStat.getString(info));
            }
            if (getID() != 0 && info.SHOW_INDEX) writer.println(" ("+getID()+")");
            else writer.println();
            if (examples!=null && examples.getNbRows()>0){
                writer.println(examples.toString(prefix));
                writer.println(prefix+"Summary:");
                writer.println(examples.getSummary(prefix));
            }

        }
    }

    private final boolean hasUnknownBranch() {
        return m_Test.hasUnknownBranch();
    }
    
    /*to print the tree directly into an IDB : Elisa Fromont 13/06/2007*/
    @Override
    public final void printTreeInDatabase(PrintWriter writer, String tabitem[], int tabexist[], int cpt, String typetree) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) { //the tree is binary
                
                // in case the test is positive
                tabitem[cpt] = m_Test.getTestString();
                tabexist[cpt] = 1;
                cpt++;
                getChild(YES).printTreeInDatabase(writer,tabitem, tabexist, cpt, typetree);
                cpt--;//to remove the last test on the father : now the test is negative
                // in case the test is negative
                tabitem[cpt]= m_Test.getTestString();
                tabexist[cpt] = 0;
                cpt++;
                if (hasUnknownBranch()) {
                    (getChild(NO)).printTreeInDatabase(writer,tabitem, tabexist, cpt, typetree);

                    (getChild(UNK)).printTreeInDatabase(writer,tabitem, tabexist, cpt, typetree);
                }
                else {
                    (getChild(NO)).printTreeInDatabase(writer, tabitem, tabexist, cpt, typetree);
                }
            }//end if arity- delta ==2

            else{ //arity -delta =/= 2  the tree is not binary
                //Has not been modified for database purpose yet !!!!!!
                writer.println("arity-delta different 2");
                for (int i = 0; i < arity; i++) {
                    MyNode child = getChild(i);
                    String branchlabel = m_Test.getBranchLabel(i);
                    writer.print("+--" + branchlabel + ": ");
                    if (i != arity-1) {
                        child.printTreeInDatabase(writer,tabitem, tabexist, cpt, typetree);
                    } else {
                        child.printTreeInDatabase(writer,tabitem, tabexist, cpt, typetree);
                    }
                }// end for
            }//end else arity -delta =/= 2
        } //end if arity >0 0

        else {// if arity =0 : on a leaf
            if (m_TargetStat == null) {
                writer.print("?");
            } else {
                tabitem[cpt] = m_TargetStat.getPredictedClassName(0);
                tabexist[cpt] = 1;
                writer.print("#"); //nb leaf
                for (int i =0; i <= (cpt-1); i++) {
                    writer.print(printTestNode(tabitem[i],tabexist[i])+", ");
                }
                writer.println(printTestNode(tabitem[cpt],tabexist[cpt]));
                cpt++;
            }
        }//end else if arity =0
    }

    private String printTestNode(String a, int pres){
        if(pres == 1) {return a;}
        else {return ("not("+a+")");}
    }

    private final void printTreeToPythonScript(PrintWriter writer, String prefix) {
        int arity = getNbChildren();
        if (arity > 0) {
            int delta = hasUnknownBranch() ? 1 : 0;
            if (arity - delta == 2) {
                writer.println(prefix+"if " +m_Test.getTestString()+":");
                // FIXME this needs to be dependent on node type
                // ((ClusNode)getChild(YES)).printTreeToPythonScript(writer, prefix+"\t");
                writer.println(prefix + "else: ");
                if (hasUnknownBranch()) {
                    //TODO anything to do???
                } else {
                    // FIXME this needs to be dependent on node type
                    // ((ClusNode)getChild(NO)).printTreeToPythonScript(writer, prefix+"\t");
                }
            } else {
                //TODO what to do?
            }
        } else {
            if (m_TargetStat != null) {
                writer.println(prefix+"return "+m_TargetStat.getArrayOfStatistic());
                System.out.println(m_TargetStat.getClass());
            }
        }
    }

    @Override
    public String toString() {
        try{
            if (hasBestTest()) return getTestString();
            else return m_TargetStat.getSimpleString();
        }
        catch(Exception e){return "null clusnode ";}
    }
    
    @Override
    public void makeLeaf() {
        m_Test = null;
        cleanup();
        removeAllChildren();        
    }

    private final void cleanup() {
        if (m_ClusteringStat != null) m_ClusteringStat.setSDataSize(0);
        if (m_TargetStat != null) m_TargetStat.setSDataSize(0);
    }
    
    public final void setTest(NodeTest test) {
        m_Test = test;
    }
    
    /***************************************************************************
     * Inspectors concerning statistics
     ***************************************************************************/

    @Override
    public MyNode cloneNode() {
        ClusSplitNode clone = new ClusSplitNode();
        clone.m_Test = m_Test;
        clone.m_ClusteringStat = m_ClusteringStat;
        clone.m_TargetStat = m_TargetStat;
        return clone;
    }

    public Node[] getChildren() {
        Node[] temp = new Node[m_Children.size()];
        for(int i=0; i<m_Children.size(); i++)
            temp[i] = getChild(i);
        return temp;
    }

//  public final void computePrediction() {
//  if (getClusteringStat() != null) getClusteringStat().calcMean();
//  if (getTargetStat() != null) getTargetStat().calcMean();
//}
    
//  public final void printTree() {
//  PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
//  printTree(wrt, StatisticPrintInfo.getInstance(), "");
//  wrt.flush();
//}
    
//  public final void showAlternatives(PrintWriter writer) {
//  if (m_Alternatives == null) return;
//  for (int i = 0; i < m_Alternatives.length; i++) {
//      writer.print(" and " + m_Alternatives[i]);
//  }
//}
    
//    /***************************************************************************
//     * Insprectors concenring test
//     ***************************************************************************/
    public final NodeTest getTest() {
        return m_Test;
    }

    @Override
    public int getModelSize() {
        return getNbNodes();
    }

    public final int updateArity() {
        int arity = m_Test.updateArity();
        setNbChildren(arity);
        return arity;
    }
    
    public final void testToNode(TestAndHeuristic tnh) {
        setTest(tnh.updateTest());
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
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
        
    }

    @Override
    public void attachModel(HashMap table) throws ClusException {
        
    }

    @Override
    public void retrieveStatistics(ArrayList list) {
        
    }

    @Override
    public ClusModel prune(int prunetype) {
        
        return null;
    }
}

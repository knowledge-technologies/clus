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

package si.ijs.kt.clus.ext.optxval;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusOutput;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStat;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsExperimental;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.resource.ResourceInfo;
import si.ijs.kt.clus.util.jeans.util.array.MDoubleArray;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;
import si.ijs.kt.clus.util.tools.debug.Debug;

// Nothing accesses this class matejp
@Deprecated
public class OptXVal {

    protected Clus m_Clus;


    public OptXVal(Clus clus) {
        m_Clus = clus;
    }


    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        schema.addIndices(ClusSchema.ROWS);
        int nb_num = schema.getNbNumericDescriptiveAttributes();
        sett.getExperimental();
        if (SettingsExperimental.XVAL_OVERLAP && nb_num > 0)
            return new OptXValIndOV(schema, sett);
        else
            return new OptXValIndNO(schema, sett);
    }


    public final void addFoldNrs(RowData set, XValMainSelection sel) {
        int nb = set.getNbRows();
        for (int i = 0; i < nb; i++) {
            int fold = sel.getFold(i);
            DataTuple tuple = set.getTuple(i);
            tuple.setIndex(fold + 1);
        }
    }


    public final static void showFoldsInfo(PrintWriter writer, Object root) {
        OptXValBinTree bintree = OptXValBinTree.convertTree(root);

        double[] fis = bintree.getFIs();
        double[] nodes = bintree.getNodes();
        double[] times = bintree.getTimes();
        MDoubleArray.divide(fis, nodes);

        writer.println("FoldsInfo");
        writer.println("Nodes:  " + MDoubleArray.toString(nodes));
        writer.println("f(i-1): " + MDoubleArray.toString(fis));
        writer.println("Time:   " + MDoubleArray.toString(times));
    }


    public final static void showForest(PrintWriter writer, OptXValNode root) {
        writer.println("XVal Forest");
        writer.println("***********");
        writer.println();
        showFoldsInfo(writer, root);
        writer.println();
        root.printTree(writer, "");
        writer.println();
    }


    public final void xvalRun(String appname, Date date) throws Exception {
        Settings sett = m_Clus.getSettings();
        ClusSchema schema = m_Clus.getSchema();
        RowData set = m_Clus.getRowDataClone();
        XValMainSelection sel = schema.getXValSelection(set);
        addFoldNrs(set, sel);
        OptXValInduce induce = (OptXValInduce) m_Clus.getInduce();
        induce.initialize(sel.getNbFolds());
        long time;
        if (Debug.debug == 1) {
            time = ResourceInfo.getCPUTime();
        }

        OptXValNode root = null;
        int nbr = 0;
        while (true) {
            root = induce.optXVal(set);
            nbr++;
            if (Debug.debug == 1) {
                if ((ResourceInfo.getCPUTime() - time) > 5000.0)
                    break;
            }

        }
        ClusSummary summary = m_Clus.getSummary();
        if (Debug.debug == 1) {
            if (Debug.debug == 1) {
                summary.setInductionTime((long) ClusStat.addToTotal(ResourceInfo.getCPUTime() - time, nbr));
            }

        }

        if (Debug.debug == 1) {
            ClusStat.addTimes(nbr);
        }

        // Output whole tree
        //MultiScore score = m_Clus.getMultiScore();
        ClusOutput output = new ClusOutput(appname + ".out", schema, sett);
        output.writeHeader();
        ClusNode tree = root.getTree(0);
        ClusRun cr = m_Clus.partitionData();
        tree.afterInduce(null);
        // m_Clus.storeAndPruneModel(cr, tree);
        // m_Clus.calcError(cr, null);
        output.writeOutput(cr, true);
        output.close();
        // Output xval trees
        output = new ClusOutput(appname + ".xval", schema, sett);
        output.writeHeader();
        
        sett.getExperimental();
        if (SettingsExperimental.SHOW_XVAL_FOREST)
            showForest(output.getWriter(), root);
        for (int i = 0; i < sel.getNbFolds(); i++) {
            XValSelection msel = new XValSelection(sel, i);
            cr = m_Clus.partitionData(msel, i + 1);
            tree = root.getTree(i + 1);
            tree.afterInduce(null);
            // m_Clus.storeAndPruneModel(cr, tree);
            // m_Clus.calcError(cr, summary);
            if (sett.getOutput().isOutputFoldModels())
                output.writeOutput(cr, false);
        }
        output.writeSummary(summary);
        output.close();
    }
}

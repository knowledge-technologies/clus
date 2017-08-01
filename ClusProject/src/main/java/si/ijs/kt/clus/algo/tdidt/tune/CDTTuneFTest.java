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

package si.ijs.kt.clus.algo.tdidt.tune;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.hierarchical.HierErrorMeasures;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.selection.ClusSelection;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.jeans.math.MathUtil;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


// added 18-05-06
// import si.ijs.kt.clus.ext.hierarchical.*;

public class CDTTuneFTest extends ClusDecisionTree {

    protected ClusInductionAlgorithmType m_Class;
    protected double[] m_FTests;


    public CDTTuneFTest(ClusInductionAlgorithmType clss) {
        super(clss.getClus());
        m_Class = clss;
    }


    public CDTTuneFTest(ClusInductionAlgorithmType clss, double[] ftests) {
        super(clss.getClus());
        m_Class = clss;
        m_FTests = ftests;
    }


    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        return m_Class.createInduce(schema, sett, cargs);
    }


    @Override
    public void printInfo() {
        System.out.println("TDIDT (Tuning F-Test)");
        System.out.println("Heuristic: " + getStatManager().getHeuristicName());
    }


    private final void showFold(int i) {
        if (getSettings().getGeneral().getVerbose() > 1) {
            if (i != 0)
                System.out.print(" ");
            System.out.print(String.valueOf(i + 1));
            System.out.flush();
        }
    }


    public ClusErrorList createTuneError(ClusStatManager mgr) {
        ClusErrorList parent = new ClusErrorList();
        if (mgr.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            int optimize = getSettings().getHMLC().getHierOptimizeErrorMeasure();
            parent.addError(new HierErrorMeasures(parent, mgr.getHier(), null, getSettings().getGeneral().getCompatibility(), optimize, false, getSettings().getOutput().isGzipOutput()));
            return parent;
        }
        NumericAttrType[] num = mgr.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
        NominalAttrType[] nom = mgr.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
        if (nom.length != 0) {
            parent.addError(new Accuracy(parent, nom));
        }
        if (num.length != 0) {
            // parent.addError(new PearsonCorrelation(parent, num));
            parent.addError(new RMSError(parent, num));
        }
        return parent;
    }


    public final ClusRun partitionDataBasic(ClusData data, ClusSelection sel, ClusSummary summary, int idx) throws IOException, ClusException, InterruptedException {
        ClusRun cr = new ClusRun(data.cloneData(), summary);
        if (sel != null) {
            if (sel.changesDistribution()) {
                ((RowData) cr.getTrainingSet()).update(sel);
            }
            else {
                ClusData val = cr.getTrainingSet().select(sel);
                cr.setTestSet(((RowData) val).getIterator());
            }
        }
        cr.setIndex(idx);
        cr.copyTrainingData();
        return cr;
    }


    public double doParamXVal(RowData trset, RowData pruneset) throws Exception {
        int prevVerb = getSettings().getGeneral().enableVerbose(0);
        ClusStatManager mgr = getStatManager();
        ClusSummary summ = new ClusSummary();
        summ.setStatManager(getStatManager());
        summ.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
        ClusRandom.initialize(getSettings());
        double avgSize = 0.0;
        if (pruneset != null) {
            ClusRun cr = new ClusRun(trset.cloneData(), summ);
            ClusModel model = m_Class.induceSingleUnpruned(cr);
            avgSize = model.getModelSize();
            cr.addModelInfo(ClusModel.ORIGINAL).setModel(model);
            cr.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
            m_Clus.calcError(pruneset.getIterator(), ClusModelInfo.TEST_ERR, cr, null);
            summ.addSummary(cr);
        }
        else {
            // Next does not always use same partition!
            // Random random = ClusRandom.getRandom(ClusRandom.RANDOM_PARAM_TUNE);
            Random random = new Random(0);
            int nbfolds = Integer.parseInt(getSettings().getModel().getTuneFolds());
            XValMainSelection sel = new XValRandomSelection(trset.getNbRows(), nbfolds, random);
            for (int i = 0; i < nbfolds; i++) {
                showFold(i);
                XValSelection msel = new XValSelection(sel, i);
                ClusRun cr = partitionDataBasic(trset, msel, summ, i + 1);
                ClusModel model = m_Class.induceSingleUnpruned(cr);
                avgSize += model.getModelSize();
                cr.addModelInfo(ClusModel.ORIGINAL).setModel(model);
                cr.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
                m_Clus.calcError(cr.getTestIter(), ClusModelInfo.TEST_ERR, cr, null);
                summ.addSummary(cr);
            }
            avgSize /= nbfolds;
            if (getSettings().getGeneral().getVerbose() > 1)
                System.out.println();
        }
        ClusModelInfo mi = summ.getModelInfo(ClusModel.ORIGINAL);
        getSettings().getGeneral().enableVerbose(prevVerb);
        ClusError err = mi.getTestError().getFirstError();
        if (getSettings().getGeneral().getVerbose() > 1) {
            PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
            wrt.print("Size: " + avgSize + ", ");
            wrt.print("Error: ");
            err.showModelError(wrt, ClusError.DETAIL_VERY_SMALL);
            wrt.flush();
        }
        return err.getModelError();
    }


    public void findBestFTest(RowData trset, RowData pruneset) throws Exception {
        int best_value = 0;
        boolean low = createTuneError(getStatManager()).getFirstError().shouldBeLow();
        double best_error = low ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        for (int i = 0; i < m_FTests.length; i++) {
            getSettings().getTree().setFTest(m_FTests[i], getSettings().getGeneral().getVerbose());
            if (getSettings().getGeneral().getVerbose() > 0)
                System.out.println("Try for F-test value = " + m_FTests[i]);
            double err = doParamXVal(trset, pruneset);
            if (getSettings().getGeneral().getVerbose() > 1)
                System.out.print("-> " + err);
            if (low) {
                if (err < best_error - MathUtil.C1E_16) {
                    best_error = err;
                    best_value = i;
                    if (getSettings().getGeneral().getVerbose() > 1)
                        System.out.println(" *");
                }
                else {
                    if (getSettings().getGeneral().getVerbose() > 1)
                        System.out.println();
                }
            }
            else {
                if (err > best_error + MathUtil.C1E_16) {
                    best_error = err;
                    best_value = i;
                    if (getSettings().getGeneral().getVerbose() > 1)
                        System.out.println(" *");
                }
                else {
                    if (getSettings().getGeneral().getVerbose() > 1)
                        System.out.println();
                }
            }
            if (getSettings().getGeneral().getVerbose() > 0)
                System.out.println();
        }
        getSettings().getTree().setFTest(m_FTests[best_value], getSettings().getGeneral().getVerbose());
        if (getSettings().getGeneral().getVerbose() > 0)
            System.out.println("Best F-test value is: " + m_FTests[best_value]);
    }


    @Override
    public void induceAll(ClusRun cr) throws ClusException, IOException {
        try {
            // Find optimal F-test value
            RowData valid = (RowData) cr.getPruneSet();
            RowData train = (RowData) cr.getTrainingSet();
            findBestFTest(train, valid);
            System.out.println();
            // Induce final model
            cr.combineTrainAndValidSets();
            ClusRandom.initialize(getSettings());
            m_Class.induceAll(cr);
        }
        catch (ClusException e) {
            System.err.println("Error: " + e);
        }
        catch (IOException e) {
            System.err.println("IO Error: " + e);
        }
        catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

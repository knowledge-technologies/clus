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

package si.ijs.kt.clus.error.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.main.ClusModelInfoList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


// FIXME : replace nbexamples by sumweight (not?) !

public class ClusErrorList implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int m_NbTotal;
    protected int m_NbExamples;
    protected int m_NbCover;
    protected ArrayList<ClusError> m_Error = new ArrayList<ClusError>();
    protected ArrayList<ClusError> m_ErrorWithNulls = new ArrayList<ClusError>();
    private HashMap<String, Integer> m_ErrorNameMap = new HashMap<String, Integer>(); // this is an index of
                                                                                      // errors->restriction: error
                                                                                      // names must be unique!


    public ClusErrorList() {
        m_NbTotal = -1;
    }


    public void setNbTotal(int nb) {
        m_NbTotal = nb;
    }


    public int getNbTotal() {
        return m_NbTotal == -1 ? m_NbExamples : m_NbTotal;
    }


    public void setNbExamples(int nb) {
        m_NbExamples = nb;
    }


    public void setNbExamples(int nb, int cover) {
        m_NbExamples = nb;
        m_NbCover = cover;
    }


    public void setWeights(ClusAttributeWeights weights) {
        for (int i = 0; i < m_Error.size(); i++) {
            ClusError err = m_Error.get(i);
            err.setWeights(weights);
        }
    }


    public void checkChildren() {
        int nb_e = m_Error.size();
        for (int i = 0; i < nb_e; i++) {
            ClusError err = m_Error.get(i);
            if (err.getParent() != this)
                ClusLogger.info("Child: " + err + " has incorrect parent: " + err.getParent() + " " + this);
        }
    }


    public void calcError(TupleIterator iter, ClusModel model) throws ClusException, IOException, InterruptedException {
        iter.init();
        DataTuple tuple = iter.readTuple();
        while (tuple != null) {
            ClusStatistic pred = model.predictWeighted(tuple);
            addExample(tuple, pred);
            tuple = iter.readTuple();
        }
        iter.close();
    }


    public ClusErrorList getErrorClone() throws ClusException {
        ClusErrorList res = new ClusErrorList();
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            res.addError(err.getErrorClone(res));
        }
        return res;
    }


    public ClusErrorList getErrorClone(String model) throws ClusException {
        ClusErrorList res = new ClusErrorList();
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            if (err.isComputeForModel(model)) {
                res.addError(err.getErrorClone(res));
            }
            else {
                res.addError(null);
            }
        }
        return res;
    }


    public void addError(ClusError err) {
        // If error list is sublist of other list, still allow indexing by introducing nulls
        if (err != null) {

            String name = err.getName();

            m_Error.add(err);

            if (!m_ErrorNameMap.containsKey(name)) {
                m_ErrorNameMap.put(name, m_Error.size() - 1);
            }
            else {
                System.err.println("Error names must be unique!");
                System.err.println(name + " already exists in the list. Exiting...");
                System.exit(1);
            }
        }
        m_ErrorWithNulls.add(err);
    }


    public void addErrors(ClusErrorList error) {
        for (int i = 0; i < error.getNbErrors(); i++) {
            addError(error.getError(i));
        }
    }


    public int getNbErrors() {
        return m_Error.size();
    }


    public ClusError getFirstError() {
        return getError(0);
    }


    public ClusError getError(int idx) {
        if (idx + 1 > m_Error.size()) {
            System.err.println("Unable to get error at index " + idx);
        }
        return m_Error.get(idx);
    }


    public ClusError getErrorOrNull(int idx) {
        return m_ErrorWithNulls.get(idx);
    }


    public void clear() {
        for (int i = 0; i < m_ErrorWithNulls.size(); i++) {
            m_ErrorWithNulls.set(i, null);
        }
    }


    public ClusError getErrorByName(String name) {
        if (m_ErrorNameMap.containsKey(name))
            return getError(m_ErrorNameMap.get(name));

        /*
         * for (int i = 0; i < m_Error.size(); i++) {
         * ClusError err = m_Error.get(i);
         * if (err.getName().equals(name))
         * return err;
         * }
         */

        return null;
    }


    public void compute(RowData data, ClusModelInfo model) throws ClusException {
        int nb = m_Error.size();
        for (int i = nb - 1; i >= 0; i--) {
            ClusError err = m_Error.get(i);
            if (err.isComputeForModel(model.getName())) {
                err.compute(data, model.getModel());
            }
            else {
                m_Error.remove(i);
            }
        }
        m_NbExamples = data.getNbRows();
        m_NbCover = m_NbExamples;
    }


    public void compute(RowData data, ClusModel model) throws ClusException {
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            err.compute(data, model);
        }
        m_NbExamples = data.getNbRows();
        m_NbCover = m_NbExamples;
    }


    public void reset() {
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            err.reset();
        }
        m_NbExamples = 0;
        m_NbCover = 0;
    }


    public void addExample(DataTuple tuple, ClusStatistic stat) throws ClusException {
        m_NbExamples++;
        int nb = m_Error.size();
        if (stat != null && stat.isValidPrediction()) {
            m_NbCover++;
            for (int i = 0; i < nb; i++) {
                ClusError err = m_Error.get(i);
                err.addExample(tuple, stat);
            }
        }
        else {
            for (int i = 0; i < nb; i++) {
                ClusError err = m_Error.get(i);
                err.addInvalid(tuple);
            }
        }
    }


    public void addExample(DataTuple real, DataTuple pred) throws ClusException {
        m_NbExamples++;
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            err.addExample(real, pred);
        }
    }


    public void addExample() {
        m_NbExamples++;
        m_NbCover++;
    }


    public void add(ClusErrorList par) {
        int nb = m_ErrorWithNulls.size();
        for (int i = 0; i < nb; i++) {
            ClusError my = m_ErrorWithNulls.get(i);
            if (my != null) {
                // ClusError your = par.getErrorOrNull(i);
                ClusError your = par.getErrorByName(my.getName());
                if (your != null) {
                    my.add(your);
                }
            }
        }
        m_NbExamples += par.getNbExamples();
        m_NbCover += par.getNbCover();
    }


    public void updateFromGlobalMeasure(ClusErrorList par) {
        int nb = m_Error.size();
        for (int i = 0; i < nb; i++) {
            ClusError err = m_Error.get(i);
            err.updateFromGlobalMeasure(par.getError(i));
        }
        setNbTotal(par.getNbExamples());
    }


    public double getErrorClassif() {
        ClusError err = getError(0);
        return err.get_error_classif();
    }


    public double getErrorAccuracy() {
        ClusError err = getError(0);
        return err.get_accuracy();
    }


    public double getErrorPrecision() {
        ClusError err = getError(0);
        return err.get_precision();
    }


    public double getErrorRecall() {
        ClusError err = getError(0);
        return err.get_recall();
    }


    public double getErrorAuc() {
        ClusError err = getError(0);
        return err.get_auc();
    }


    public void showError(PrintWriter out) {
        int nb = m_Error.size();
        out.println("Number of examples: " + getNbTotal() + " (covered: " + getNbCover() + ")");
        for (int i = 0; i < nb; i++) {
            ClusError err1 = getError(i);
            out.print(err1.getName() + ": ");
            err1.showModelError(out, ClusError.DETAIL_SMALL);
        }
    }


    public static boolean checkCoverage(ClusModelInfoList models, int type, int nb) {
        int nb_models = models.getNbModels();
        for (int j = 0; j < nb_models; j++) {
            if (models.getModelInfo(j) != null) {
                ClusErrorList parent = models.getModelInfo(j).getError(type);
                if (parent.getNbCover() != nb)
                    return false;
            }
        }
        return true;
    }


    public void showError(ClusModelInfoList models, int type, String bName, PrintWriter out, Settings sett) throws IOException {
        // public void showError(ClusModelInfoList models, int type, PrintWriter out) throws IOException {
        int nb = m_Error.size();
        ClusModelInfo definf = models.getModelInfo(ClusModel.DEFAULT);
        if (definf == null) {
            System.err.println("DEFAULT model is null!");
            return;
        }

        ClusErrorList defpar = definf.getError(type);
        out.println("Number of examples: " + defpar.getNbExamples());

        if (sett.getHMTR().isSectionHMTREnabled()) {
            out.println("HMTR weight: " + sett.getHMTR().getHMTRHierarchyWeight().getStringValue());
        }

        int nb_models = models.getNbModels();
        if (!checkCoverage(models, type, defpar.getNbExamples())) {
            out.println("Coverage:");
            for (int j = 0; j < nb_models; j++) {
                ClusModelInfo inf = models.getModelInfo(j);
                if (inf != null) {
                    ClusErrorList parent = inf.getError(type);
                    out.println("  " + inf.getName() + ": " + parent.getNbCover());
                }
            }
        }

        for (int i = 0; i < nb; i++) {
            if (sett.getHMTR().isSectionHMTREnabled() && i == (nb / 2)) {
                out.println();
                out.println("\t***** HMTR leaves only w = " + sett.getHMTR().getHMTRHierarchyWeight().getStringValue() + " *****");
                out.println();
            }

            ClusError err1 = getError(i);
            boolean has_models = false;
            for (int j = 0; j < nb_models; j++) {
                ClusModelInfo inf = models.getModelInfo(j);
                if (inf != null && inf.getError(type).getErrorByName(err1.getName()) != null) {
                    has_models = true;
                    break;
                }
            }
            if (has_models) {
                out.println(err1.getName());
                for (int j = 0; j < nb_models; j++) {
                    ClusModelInfo inf = models.getModelInfo(j);
                    if (inf != null) {
                        ClusError err2 = inf.getError(type).getErrorByName(err1.getName());
                        if (err2 != null) {
                            if (err2.isMultiLine()) {
                                out.print("   " + inf.getName() + ": ");
                            }
                            else {
                                out.print("   " + StringUtils.printStr(inf.getName(), 15) + ": ");
                            }
                            err2.showModelError(out, bName, ClusError.DETAIL_SMALL);
                        }
                    }
                }
            }
        }
    }


    public static void printExtraError(ClusModelInfoList models, int type, PrintWriter out) {
        int ctr = 0;
        int nb_models = models.getNbModels();
        for (int j = 0; j < nb_models; j++) {
            ClusModelInfo inf = models.getModelInfo(j);
            if (inf != null) {
                ClusErrorList parent = inf.getExtraError(type);
                if (parent != null && inf.hasModel()) {
                    int nb_err = parent.getNbErrors();
                    for (int i = 0; i < nb_err; i++) {
                        ClusError err = parent.getError(i);
                        out.print(err.getName() + ": " + StringUtils.printStr(inf.getName(), 15) + ": ");
                        err.showModelError(out, ClusError.DETAIL_SMALL);
                        ctr++;
                    }
                }
            }
        }
        if (ctr != 0)
            out.println();
    }


    /*
     * public void showErrorBrief(ClusModelInfoList models, int type, PrintWriter out) {
     * int nb = m_Error.size();
     * for (int i = 0; i < nb; i++) {
     * ClusError err1 = getError(i);
     * if (type == ClusModelInfo.TRAIN_ERR)
     * out.print("Train ");
     * else
     * out.print("Test ");
     * out.println(err1.getName());
     * int nb_models = models.getNbModels();
     * for (int j = 0; j < nb_models; j++) {
     * ClusModelInfo inf = models.getModelInfo(j);
     * ClusErrorList parent = inf.getError(type);
     * ClusError err2 = parent.getError(i);
     * out.print("   " + StringUtils.printStr(inf.getName(), 15) + ": ");
     * err2.showModelError(out, ClusError.DETAIL_VERY_SMALL);
     * }
     * }
     * }
     */

    public void showErrorBrief(ClusModelInfoList models, int type, PrintWriter out) {
        for (int i = 0; i < m_Error.size(); i++) {
            ClusError err1 = getError(i);
            if (type == ClusModelInfo.TRAIN_ERR) {
                out.print("Train ");
            }
            else {
                out.print("Test ");
            }

            out.println(err1.getName());
            int nb_models = models.getNbModels();
            for (int j = 0; j < nb_models; j++) {
                ClusModelInfo inf = models.getModelInfo(j);
                ClusErrorList parent = inf.getError(type);
                ClusError err2 = parent.getErrorByName(err1.getName());

                if (err2 != null) {
                    out.print("   " + StringUtils.printStr(inf.getName(), 15) + ": ");
                    err2.showModelError(out, ClusError.DETAIL_VERY_SMALL);
                }
            }
        }
    }


    public String getPrefix() {
        return "   ";
    }


    public int getNbExamples() {
        return m_NbExamples;
    }


    public int getNbCover() {
        return m_NbCover;
    }


    public ClusNumberFormat getFormat() {
        return ClusFormat.FOUR_AFTER_DOT;
        // return NumberFormat.getInstance();
    }

}

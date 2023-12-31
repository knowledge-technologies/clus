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

/*
 * Created on May 19, 2005
 */

package si.ijs.kt.clus.error.hmlc;

import java.util.Arrays;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.error.MSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.ext.hierarchical.ClassesValue;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.WHTDStatistic;


public class HierRMSError extends MSError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClassHierarchy m_Hier;
    protected double[] m_Scratch;
    protected boolean m_Root, m_ContPred;


    public HierRMSError(ClusErrorList par, ClusAttributeWeights weights, boolean root, boolean proto, ClassHierarchy hier) {
        this(par, weights, false, root, proto, hier);
    }


    public HierRMSError(ClusErrorList par, ClusAttributeWeights weights, boolean printall, boolean root, boolean proto, ClassHierarchy hier) {
        super(par, hier.getDummyAttrs(), weights, printall, "");
        m_Hier = hier;
        m_Root = root;
        m_ContPred = proto;
        m_Scratch = new double[m_Dim];
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        if (pred == null)
            return;
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(0);
        Arrays.fill(m_Scratch, 0.0);
        for (int i = 0; i < tp.getNbClasses(); i++) {
            ClassesValue val = tp.getClass(i);
            m_Scratch[val.getIndex()] = 1.0;
        }
        if (m_ContPred) {
            addExample(m_Scratch, pred.getNumericPred());
        }
        else {
            addExample(m_Scratch, ((WHTDStatistic) pred).getDiscretePred());
        }
    }


    @Override
    public double getModelError() {
        if (m_Root)
            return Math.sqrt(super.getModelError());
        else
            return super.getModelError();
    }


    @Override
    public double getModelErrorComponent(int i) {
        if (m_Root)
            return Math.sqrt(super.getModelErrorComponent(i));
        else
            return super.getModelErrorComponent(i);
    }


    @Override
    public String getName() {
        String root = m_Root ? "RMSE" : "MSE";
        String proto = m_ContPred ? "with continuous predictions" : "with discrete predictions";
        if (m_Weights == null)
            return "Hierarchical " + root + " " + proto;
        else
            return "Hierarchical weighted " + root + " (" + m_Weights.getName() + ") " + proto;
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new HierRMSError(par, m_Weights, m_PrintAllComps, m_Root, m_ContPred, m_Hier);
    }
}

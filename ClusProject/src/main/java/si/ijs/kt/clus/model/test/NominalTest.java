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

package si.ijs.kt.clus.model.test;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.jeans.util.sort.DoubleIndexSorter;


public class NominalTest extends NodeTest {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected NominalAttrType m_Type;
    protected double[] m_Sorted;
    protected int[] m_Index;


    public NominalTest(NominalAttrType type, double[] freq) {
        m_Type = type;
        setProportion(freq);
    }


    @Override
    public ClusAttrType getType() {
        return m_Type;
    }


    @Override
    public void setType(ClusAttrType type) {
        m_Type = (NominalAttrType) type;
    }


    @Override
    public String getString() {
        if (m_Type.getNbValues() > 2) {
            return m_Type.getName();
        }
        else {
            String val = m_Type.getValue(0);
            if (hasBranchLabels())
                return m_Type.getName();
            else
                return m_Type.getName() + " = " + val;
        }
    }


    @Override
    public String getPythonString(String xsElement) {
        if (m_Type.getNbValues() > 2) {
            return xsElement; // m_Type.getName();
        }
        else {
            String val = m_Type.getValue(0);
            if (hasBranchLabels())
                return xsElement;   // m_Type.getName();
            else
                return xsElement + " == " + "'" + val + "'"; // m_Type.getName()
        }
    }


    @Override
    public boolean equals(NodeTest test) {
        return m_Type == test.getType();
    }


    @Override
    public int hashCode() {
        return m_Type.getIndex();
    }


    @Override
    public void preprocess(int mode) {
        DoubleIndexSorter sorter = DoubleIndexSorter.getInstance();
        sorter.setData(m_Sorted = DoubleIndexSorter.arrayclone(m_BranchFreq));
        sorter.sort();
        m_Index = sorter.getIndex();
    }


    @Override
    public int predictWeighted(DataTuple tuple) {
        int val = m_Type.getNominal(tuple);
        return nominalPredictWeighted(val);
    }


    /*
     * public int predict(ClusAttribute attr, int idx) {
     * return nominalPredict(((NominalAttribute)attr).m_Data[idx]);
     * }
     */
    @Override
    public int nominalPredict(int value) {
        // Missing value ?
        int arity = getNbChildren();
        if (value == arity) {
            double cumul = 0.0;
            double val = ClusRandom.nextDouble(ClusRandom.RANDOM_TEST_DIR);
            for (int i = 0; i < arity - 1; i++) {
                cumul += m_Sorted[i];
                if (val < cumul)
                    return m_Index[i];
            }
            return m_Index[arity - 1];
        }
        return value;
    }


    @Override
    public int nominalPredictWeighted(int value) {
        // Missing value ?
        if (value == getNbChildren())
            return hasUnknownBranch() ? ClusNode.UNK : UNKNOWN;
        return value;
    }


    @Override
    public boolean hasBranchLabels() {
        return true;// m_Type.getNbValues() > 2;
    }


    @Override
    public String getBranchLabel(int i) {
        return m_Type.getValue(i);
    }


    @Override
    public String getBranchString(int i) {
        return m_Type.getName() + " = " + m_Type.getValue(i);
    }


    @Override
    public NodeTest getBranchTest(int i) {
        SubsetTest test = new SubsetTest(m_Type, 1);
        test.setValue(0, i);
        test.setPosFreq(getProportion(i));
        return test;
    }
}

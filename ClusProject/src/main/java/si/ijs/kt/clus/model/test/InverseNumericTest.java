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
 * Created on May 3, 2005
 */

package si.ijs.kt.clus.model.test;

import java.text.NumberFormat;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusRandom;


public class InverseNumericTest extends NumericTest {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public InverseNumericTest(ClusAttrType attr, double bound, double posfreq) {
        super(attr, bound, posfreq);
    }


    @Override
    public String getString() {
        String value = m_Bound != Double.NEGATIVE_INFINITY ? NumberFormat.getInstance().format(m_Bound) : "?";
        return m_Type.getName() + " <= " + value;
    }


    @Override
    public boolean isInverseNumeric() {
        return true;
    }


    @Override
    public int hashCode() {
        long v = Double.doubleToLongBits(m_Bound);
        return m_Type.getIndex() + (int) (v ^ (v >>> 32)) + 1;
    }


    @Override
    public int numericPredict(double value) {
        if (value == Double.POSITIVE_INFINITY)
            return ClusRandom.nextDouble(ClusRandom.RANDOM_TEST_DIR) < getPosFreq() ? ClusNode.YES : ClusNode.NO;
        return value <= m_Bound ? ClusNode.YES : ClusNode.NO;
    }


    @Override
    public int numericPredictWeighted(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return hasUnknownBranch() ? ClusNode.UNK : UNKNOWN;
        }
        else {
            return value <= m_Bound ? ClusNode.YES : ClusNode.NO;
        }
    }


    @Override
    public int predictWeighted(DataTuple tuple) {
        double val = m_Type.getNumeric(tuple);
        return numericPredictWeighted(val);
    }


    @Override
    public NodeTest getBranchTest(int i) {
        if (i == ClusNode.YES) {
            return this;
        }
        else {
            return new NumericTest(m_Type, getBound(), 1.0 - getPosFreq());
        }
    }
}

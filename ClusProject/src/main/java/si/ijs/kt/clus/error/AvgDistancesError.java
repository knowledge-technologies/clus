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

package si.ijs.kt.clus.error;

import java.io.PrintWriter;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


public class AvgDistancesError extends ClusError { // does not implements ComponentError

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_SumErr;
    protected ClusDistance m_Distance;


    public AvgDistancesError(ClusErrorList par, ClusDistance dist) {
        this(par, dist, "");
    }


    public AvgDistancesError(ClusErrorList par, ClusDistance dist, String info) {
        super(par);
        m_Distance = dist;

        setAdditionalInfo(info);
    }


    @Override
    public void reset() {
        m_SumErr = 0.0;
    }


    @Override
    public void add(ClusError other) {
        AvgDistancesError oe = (AvgDistancesError) other;
        m_SumErr += oe.m_SumErr;
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) throws ClusException {
        m_SumErr += m_Distance.calcDistanceToCentroid(tuple, pred);
    }


    @Override
    public double getModelErrorAdditive() {
        // return squared error not divided by the number of examples
        // optimized, e.g., by size constraint pruning
        return m_SumErr;
    }


    @Override
    public double getModelError() {
        return getModelErrorComponent(0);
    }


    @Override
    public boolean shouldBeLow() {
        return true;
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new AvgDistancesError(par, m_Distance, getAdditionalInfo());
    }


    @Override
    public void showModelError(PrintWriter wrt, int detail) {
        StringBuffer res = new StringBuffer();
        res.append(String.valueOf(getModelError()));
        wrt.println(res.toString());
    }


    @Override
    public String getName() {
        return "AvgDistancesError" + getAdditionalInfoFormatted();
    }


    @Override
    public double getModelErrorComponent(int i) {
        int nb = getNbExamples();
        double err = nb != 0 ? m_SumErr / nb : 0.0;
        return err;
    }
}

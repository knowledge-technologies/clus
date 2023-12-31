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

import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;


public class WAHNDError extends ClusError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    // m_Dim from ClusError isn't instantiated. Bad?
    // Probably not?

    protected double m_Weight;
    protected double m_TreeErr;
    protected double m_SumWeight;


    public WAHNDError(ClusErrorList par, double weight) {
        this(par, weight, "");
    }


    public WAHNDError(ClusErrorList par, double weight, String info) {
        super(par, 0);
        m_Weight = weight;

        setAdditionalInfo(info);
    }


    @Override
    public void add(ClusError other) {
        WAHNDError err = (WAHNDError) other;
        m_TreeErr += err.m_TreeErr;
        m_SumWeight += err.m_SumWeight;
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        out.println(m_TreeErr / m_SumWeight);
    }


    public void addExample(ClusData data, int idx, ClusStatistic pred) {
        ClusLogger.info("WAHNDError: addExample/3 not implemented");
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        double weight = tuple.getWeight();
        // ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
        // m_TreeErr += weight*calcSquaredDistance(tp, (WAHNDStatistic)pred);
        m_SumWeight += weight;
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }


    @Override
    public double getModelError() {
        return m_TreeErr / m_SumWeight;
    }


    @Override
    public void reset() {
        m_TreeErr = 0.0;
        m_SumWeight = 0.0;
    }


    @Override
    public String getName() {
        return "WAHND RE with parameter " + m_Weight + getAdditionalInfoFormatted();
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new WAHNDError(par, m_Weight, getAdditionalInfo());
    }


    @Override
    public boolean shouldBeLow() {
        throw new RuntimeException("should WAHNDError be high or low?");
    }
}

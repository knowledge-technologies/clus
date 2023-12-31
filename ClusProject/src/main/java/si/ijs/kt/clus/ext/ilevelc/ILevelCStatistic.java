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

package si.ijs.kt.clus.ext.ilevelc;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;


public class ILevelCStatistic extends RegressionStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected NumericAttrType[] m_Numeric;
    protected int m_ClusterID = -1;


    public ILevelCStatistic(Settings sett, NumericAttrType[] num) {
        super(sett, num);
        m_Numeric = num;
    }


    public void setClusterID(int id) {
        m_ClusterID = id;
    }


    public int getClusterID() {
        return m_ClusterID;
    }


    @Override
    public ClusStatistic cloneStat() {
        return new ILevelCStatistic(this.m_Settings, m_Numeric);
    }


    @Override
    public String getString(StatisticPrintInfo info) {
        String res = super.getString(info);
        return res + " L=" + getClusterID();
    }


    @Override
    public String getPredictWriterString(DataTuple tuple) {
        return "";
    }


    public void assignInstances(RowData data, int[] clusters) {
        for (int i = 0; i < data.getNbRows(); i++) {
            DataTuple tuple = data.getTuple(i);
            clusters[tuple.getIndex()] = getClusterID();
        }
    }
}

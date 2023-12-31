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

package si.ijs.kt.clus.statistic;

public class StatisticPrintInfo {

    public boolean SHOW_EXAMPLE_COUNT = true;

    public boolean SHOW_EXAMPLE_COUNT_BYTARGET = false;

    public boolean SHOW_DISTRIBUTION = false;

    public boolean SHOW_INDEX = false;

    public boolean INTERNAL_DISTR = false;

    public boolean SHOW_KEY = false;

    public static StatisticPrintInfo m_Instance = new StatisticPrintInfo();


    public static StatisticPrintInfo getInstance() {
        return m_Instance;
    }
}

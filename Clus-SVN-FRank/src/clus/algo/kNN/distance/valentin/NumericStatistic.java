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

package clus.algo.kNN.distance.valentin;

/**
 * This class stores some useful statistics for a Numeric Attribute
 * of certain data.
 */
public class NumericStatistic extends AttributeStatistic {

    private double m_Mean, m_Variance, m_Min, m_Max;


    public NumericStatistic() {
    }


    public double mean() {
        return m_Mean;
    }


    public void setMean(double m) {
        m_Mean = m;
    }


    public double variance() {
        return m_Variance;
    }


    public void setVariance(double v) {
        m_Variance = v;
    }


    public double min() {
        return m_Min;
    }


    public void setMin(double m) {
        m_Min = m;
    }


    public double max() {
        return m_Max;
    }


    public void setMax(double m) {
        m_Max = m;
    }
}

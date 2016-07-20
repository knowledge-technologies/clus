/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Created on May 12, 2005
 */
package jeans.math;

public class SingleStatList extends SingleStat {

	double[] m_Values;
	double m_X, m_Y;
	int m_Idx;

	public SingleStatList(int size) {
		m_Values = new double[size];
	}

	public double getTValueSigTest(SingleStatList other) {
		SingleStat stat = new SingleStat();
		for (int i = 0; i < m_Values.length; i++) {
			stat.addFloat(m_Values[i]-other.m_Values[i]);
		}
		return Math.abs(stat.getMean())/stat.getStdDefOfMean();
	}

	public void reset() {
		super.reset();
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i] = 0.0;
		}
		m_Idx = 0;
	}

	public void addFloat(double value) {
		super.addFloat(value);
		m_Values[m_Idx++] = value;
	}

	public double[] getValues() {
			return m_Values;
	}

	public void setX(double value) {
		m_X = value;
	}

	public double getX() {
		return m_X;
	}

	public void setY(double value) {
		m_Y = value;
	}

	public double getY() {
		return m_Y;
	}
}

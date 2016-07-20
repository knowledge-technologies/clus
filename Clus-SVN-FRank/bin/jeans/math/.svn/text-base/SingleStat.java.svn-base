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

package jeans.math;

/*
import org.apache.commons.math.distribution.*;
import org.apache.commons.math.*;
*/

public class SingleStat {

//	protected static DistributionFactory m_DistroFactory = DistributionFactory.newInstance();

	protected double m_Min = Double.POSITIVE_INFINITY;
	protected double m_Max = Double.NEGATIVE_INFINITY;
	protected double m_Sum, m_SumSQ;
	protected double m_Count;
//	protected TDistribution m_Distribution;

	public SingleStat() {
		m_Min = Double.POSITIVE_INFINITY;
		m_Max = Double.NEGATIVE_INFINITY;
	}

	public void reset() {
		m_Sum = 0.0;
		m_SumSQ = 0.0;
		m_Count = 0.0;
		m_Min = Double.POSITIVE_INFINITY;
		m_Max = Double.NEGATIVE_INFINITY;
//		m_Distribution = null;
	}

	public double getCount() {
		return m_Count;
	}

	public void addFloat(double value) {
		m_Sum += value;
		m_SumSQ += value*value;
		m_Count += 1.0;
		if (value < m_Min) m_Min = value;
		if (value > m_Max) m_Max = value;
	}

	public double getRange() {
		return Math.abs(getMax()-getMin());
	}

	public void addMean(SingleStat other) {
		addFloat(other.getMean());
	}

	public double getMin() {
		return m_Min;
	}

	public double getMax() {
		return m_Max;
	}

	public double getVariance() {
		return (m_SumSQ - m_Sum * m_Sum / m_Count) / m_Count;
	}

	public double getStdDev() {
		return Math.sqrt(getVariance() * m_Count / (m_Count - 1));
	}

	public double getStdDefOfMean() {
		return getStdDev() / Math.sqrt(m_Count);
	}

	public double getMean() {
		return m_Sum / m_Count;
	}

	public String toString() {
		return ""+getMean()+" ("+getStdDev()+") "+getMin()+"/"+getMax();
	}
/*
	public double getConfIntOfMean(double level) {
		double probability = 0.5 + level/2; // 1.0 - (1.0 - level)/2;
		if (m_Distribution == null) {
			m_Distribution = m_DistroFactory.createTDistribution(getCount()-1);
		}
		try {
			double t_value = m_Distribution.inverseCumulativeProbability(probability);
			return getStdDefOfMean() * t_value;
		} catch (MathException e) {
			System.err.println("Math error: "+e.getMessage());
			return 0.0;
		}
	}
*/
}

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

package jeans.graph.plot;

import java.awt.*;

public abstract class MGraphScale {

	public final static int TICK_SIZE = 3;

	protected String m_sLabel;
	protected int m_iGap;
	protected Color m_LabelColor = Color.yellow;
	protected double m_dStep, m_dRealStep;
	protected MDouble m_MinValue = new MDouble();
	protected MDouble m_MaxValue = new MDouble();

	public void setRounding(int nb) {
		m_MinValue.setRounding(nb);
		m_MaxValue.setRounding(nb);
	}

	public void setStep(double step) {
		m_dStep = step;
		m_dRealStep = m_dStep;
	}

	public void setGap(int gap) {
		m_iGap = gap;
	}

	public void setMinMax(float min, float max) {
		m_MinValue.setFloorValue(min);
		m_MaxValue.setCeilValue(max);
	}

	public float getRealMin() {
		return m_MinValue.getFloat();
	}

	public float getRealMax() {
		return m_MaxValue.getFloat();
	}

	public void setLabel(String label) {
		m_sLabel = label;
	}
}

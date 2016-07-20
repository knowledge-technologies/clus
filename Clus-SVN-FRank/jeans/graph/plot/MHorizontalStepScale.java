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

public class MHorizontalStepScale extends MGraphScale {

	public final static int LABEL_GAPX = 20;

	public MHorizontalStepScale() {
		m_iGap = LABEL_GAPX;
	}

	public boolean tryStep(FontMetrics fm, int x, int wd) {
		int wmin = m_MinValue.getWidth(fm);
		int wmax = m_MaxValue.getWidth(fm);
		int x1 = x + wmin/2;
		int x2 = x + wd - wmax/2;
		double value = m_MinValue.getFloat() + m_dRealStep;
		double max = m_MaxValue.getFloat();
		MDouble calc = new MDouble();
		while (value < max) {
			calc.setRoundValue(value);
			int vwd = calc.getWidth(fm);
			int pos = scaleX(x, wd, calc.getFloat());
			if (pos+vwd/2+m_iGap > x2) break;
			if (pos-vwd/2-m_iGap > x1) {
				x1 = pos+vwd/2;
			} else {
				return false;
			}
			value += m_dRealStep;
		}
		return true;
	}

	public void autoStep(FontMetrics fm, int x, int wd) {
		m_dRealStep = m_dStep;
		while (!tryStep(fm, x, wd)) m_dRealStep *= 2;
	}

	public int getHorzHeight(FontMetrics fm) {
		int hi = TICK_SIZE+4+fm.getHeight();
		if (m_sLabel != null) hi += fm.getHeight()+2;
		return hi;
	}

	public int getHorzLeftBound(FontMetrics fm) {
		return m_MinValue.getWidth(fm) / 2;
	}

	public int getHorzRightBound(FontMetrics fm) {
		return m_MaxValue.getWidth(fm) / 2;
	}

	public void draw(Component cnv, Graphics g, int x, int y, int wd, int hi) {
		FontMetrics fm = g.getFontMetrics();
		g.drawLine(x, y, x+wd, y);
		g.drawLine(x, y, x, y+TICK_SIZE);
		g.drawLine(x+wd, y, x+wd, y+TICK_SIZE);
		int wmin = m_MinValue.getWidth(fm);
		int wmax = m_MaxValue.getWidth(fm);
		m_MinValue.draw(fm, g, x-wmin/2, y+TICK_SIZE+3);
		m_MaxValue.draw(fm, g, x+wd-wmax/2, y+TICK_SIZE+3);
		int x1 = x + wmin/2;
		int x2 = x + wd - wmax/2;
		double value = m_MinValue.getFloat() + m_dRealStep;
		double max = m_MaxValue.getFloat();
		MDouble calc = new MDouble();
		while (value < max) {
			calc.setRoundValue(value);
			int vwd = calc.getWidth(fm);
			int pos = scaleX(x, wd, calc.getFloat());
			if (pos+vwd/2+m_iGap > x2) break;
			if (pos-vwd/2-m_iGap > x1) {
				g.drawLine(pos, y, pos, y+TICK_SIZE);
				calc.draw(fm, g, pos-vwd/2, y+TICK_SIZE+3);
				x1 = pos+vwd/2;
			}
			value += m_dRealStep;
		}
		if (m_sLabel != null) {
			Color old = g.getColor();
			g.setColor(m_LabelColor);
			int lwd = fm.stringWidth(m_sLabel);
			g.drawString(m_sLabel, x+wd/2-lwd/2, y+TICK_SIZE+3+fm.getAscent()+fm.getHeight());
			g.setColor(old);
		}
	}

	public int scaleX(int x, int wd, float f) {
		float f1 = m_MinValue.getFloat();
		float f2 = m_MaxValue.getFloat();
		float scale = (f-f1)/(f2-f1)*wd;
		return (int)Math.round(scale) + x;
	}
}

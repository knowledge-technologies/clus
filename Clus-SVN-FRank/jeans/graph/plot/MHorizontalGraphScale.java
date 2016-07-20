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

public class MHorizontalGraphScale extends MGraphScale {

	public final static int LABEL_GAPX = 20;

	public MHorizontalGraphScale() {
		m_iGap = LABEL_GAPX;
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
		int nx1 = x + wmin/2;
		int nx2 = x + wd - wmax/2;
		drawXRecursive(g, fm, x, y, wd, m_MinValue.getFloat(), m_MaxValue.getFloat(), nx1, nx2);
		if (m_sLabel != null) {
			Color old = g.getColor();
			g.setColor(m_LabelColor);
			int lwd = fm.stringWidth(m_sLabel);
			g.drawString(m_sLabel, x+wd/2-lwd/2, y+TICK_SIZE+3+fm.getAscent()+fm.getHeight());
			g.setColor(old);
		}
	}

	public void drawXRecursive(Graphics g, FontMetrics fm, int x, int y, int wd, float f1, float f2, int x1, int x2) {
		MDouble calc = new MDouble();
		calc.setRoundValue((f1+f2)/2);
		int pos = scaleX(x, wd, calc.getFloat());
		int vwd = calc.getWidth(fm);
		if (pos-vwd/2-m_iGap > x1 && pos+vwd/2+m_iGap < x2) {
			g.drawLine(pos, y, pos, y+TICK_SIZE);
			calc.draw(fm, g, pos-vwd/2, y+TICK_SIZE+3);
			drawXRecursive(g, fm, x, y, wd, f1, calc.getFloat(), x1, pos-vwd/2);
			drawXRecursive(g, fm, x, y, wd, calc.getFloat(), f2, pos+vwd/2, x2);
		}
	}

	public int scaleX(int x, int wd, float f) {
		float f1 = m_MinValue.getFloat();
		float f2 = m_MaxValue.getFloat();
		float scale = (f-f1)/(f2-f1)*wd;
		return (int)Math.round(scale) + x;
	}
}

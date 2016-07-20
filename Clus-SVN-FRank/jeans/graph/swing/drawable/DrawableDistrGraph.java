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

package jeans.graph.swing.drawable;

import jeans.graph.plot.MDistrInfo;

import java.awt.*;

public class DrawableDistrGraph extends Drawable {

	protected MDistrInfo m_hDistrInfo;
	protected float m_fTotal;

	public DrawableDistrGraph(int wd, int hi, MDistrInfo distInfo, float total) {
		this.wd = wd;
		this.hi = hi;
		m_hDistrInfo = distInfo;
		m_fTotal = total;
	}

	public void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs) {
		int mxp = xp-xofs;
		int myp = yp-yofs;
		int xprev = mxp;
		float value = 0.0f;
		for (int i = 0; i < m_hDistrInfo.getNbBins(); i++) {
			g.setColor(m_hDistrInfo.getBinColor(i));
			value += m_hDistrInfo.getBinCount(i);
			float delta = value*(float)wd/m_fTotal;
			int xnext = Math.min(wd, (int)Math.round(delta)) + mxp;
			g.fillRect(xprev, myp, xnext-xprev, hi);
			g.setColor(Color.black);
			g.drawLine(xprev, myp, xprev, myp+hi);
			xprev = xnext;
		}
		g.drawLine(xprev, myp, xprev, myp+hi);
		g.setColor(Color.black);
		g.drawRect(mxp,myp,wd,hi);
	}
}

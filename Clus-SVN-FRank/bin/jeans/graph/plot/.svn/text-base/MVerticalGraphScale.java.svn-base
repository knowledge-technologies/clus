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
import java.awt.image.*;
import jeans.graph.image.*;

public class MVerticalGraphScale extends MGraphScale {

	public final static int LABEL_GAPY = 60;

	protected Image m_VertLabel;

	public MVerticalGraphScale() {
		m_iGap = LABEL_GAPY;
	}

	public int getVertWidth(Component cnv, FontMetrics fm, int y, int hi) {
		int wd = m_MinValue.getWidth(fm);
		wd = Math.max(wd, m_MaxValue.getWidth(fm));
		int charh = fm.getHeight();
		int ny1 = y + charh / 2;
		int ny2 = y + hi - charh / 2;
		wd = calcVWRecursive(fm, y, hi, m_MinValue.getFloat(), m_MaxValue.getFloat(), ny1, ny2, wd);
		wd += 4 + TICK_SIZE;
		if (m_sLabel != null) {
			if (m_VertLabel == null) createVertLabel(cnv, fm);
			wd += m_VertLabel.getWidth(cnv) + 6;
		}
		return wd;
	}

	public int getVertUpperBound(FontMetrics fm) {
		return fm.getHeight()/2+6;
	}

	public int getVertLowerBound(FontMetrics fm) {
		return fm.getHeight()/2+3;
	}

	public void createVertLabel(Component cnv, FontMetrics fm) {
		int wd = fm.stringWidth(m_sLabel);
		int hi = fm.getHeight();
		// Create the image
		Image img = cnv.createImage(wd, hi);
		Graphics g = img.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0,0,wd,hi);
		g.setColor(m_LabelColor);
		g.drawString(m_sLabel, 0, fm.getAscent());
		// Rotate it
		ImageFilter filter = new TransformFilter(TransformFilter.Rot90);
		ImageProducer prod = new FilteredImageSource(img.getSource(), filter);
		m_VertLabel = cnv.createImage(prod);
	}

	public void draw(Component cnv, Graphics g, int x, int y, int wd, int hi) {
		FontMetrics fm = g.getFontMetrics();
		g.drawLine(x, y, x, y+hi);
		g.drawLine(x-TICK_SIZE, y, x, y);
		g.drawLine(x-TICK_SIZE, y+hi, x, y+hi);
		int charh = fm.getHeight();
		int delta = -fm.getAscent()+charh/2;
		int wmin = m_MinValue.getWidth(fm);
		int wmax = m_MaxValue.getWidth(fm);
		m_MaxValue.draw(fm, g, x-4-TICK_SIZE-wmax, y+delta);
		m_MinValue.draw(fm, g, x-4-TICK_SIZE-wmin, y+hi+delta);
		int ny1 = y + charh / 2;
		int ny2 = y + hi - charh / 2;
		drawYRecursive(g, fm, x, y, hi, m_MinValue.getFloat(), m_MaxValue.getFloat(), ny1, ny2);
		if (m_sLabel != null) {
			if (m_VertLabel == null) createVertLabel(cnv, fm);
			g.drawImage(m_VertLabel, 2, y+hi/2-m_VertLabel.getHeight(cnv)/2, cnv);
		}
	}

	public void drawYRecursive(Graphics g, FontMetrics fm, int x, int y, int hi, float f1, float f2, int y1, int y2) {
		MDouble calc = new MDouble();
		calc.setRoundValue((f1+f2)/2);
		int pos = scaleY(y, hi, calc.getFloat());
		int charh = fm.getHeight();
		if (pos-charh/2-m_iGap > y1 && pos+charh/2+m_iGap < y2) {
			int delta = -fm.getAscent()+charh/2;
			int wd = calc.getWidth(fm);
			g.drawLine(x-TICK_SIZE, pos, x, pos);
			calc.draw(fm, g, x-4-TICK_SIZE-wd, pos+delta);
			drawYRecursive(g, fm, x, y, hi, f1, calc.getFloat(), pos+charh/2, y2);
			drawYRecursive(g, fm, x, y, hi, calc.getFloat(), f2, y1, pos-charh/2);
		}
	}

	public int calcVWRecursive(FontMetrics fm, int y, int hi, float f1, float f2, int y1, int y2, int mwd) {
		MDouble calc = new MDouble();
		calc.setRoundValue((f1+f2)/2);
		int pos = scaleY(y, hi, calc.getFloat());
		int charh = fm.getHeight();
		if (pos-charh/2-m_iGap > y1 && pos+charh/2+m_iGap < y2) {
			mwd = Math.max(mwd, calc.getWidth(fm));
			mwd = calcVWRecursive(fm, y, hi, f1, calc.getFloat(), pos+charh/2, y2, mwd);
			mwd = calcVWRecursive(fm, y, hi, calc.getFloat(), f2, y1, pos-charh/2, mwd);
		}
		return mwd;
	}

	public int scaleY(int y, int hi, float f) {
		float f1 = m_MinValue.getFloat();
		float f2 = m_MaxValue.getFloat();
		float scale = (f-f1)/(f2-f1)*hi;
		return hi - (int)Math.round(scale) + y;
	}
}

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
import javax.swing.*;

public class MBarPlot extends JComponent {

	public final static long serialVersionUID = 1;

	Color[] m_hColors = {Color.red, Color.yellow, Color.blue};

	MVerticalGraphScale m_hVScale = new MVerticalGraphScale();
	MBarPlotModel m_hModel;
	MBarPlotModelGroup m_hGroup;
	JScrollBar m_hScroll;
	int m_iModel, m_iBar;


	public void setModel(MBarPlotModel model) {
		m_hModel = model;
		m_hGroup = null;
		updateScrolls();
	}

	public void setModel(MBarPlotModelGroup group) {
		m_hGroup = group;
		m_hModel = null;
		updateScrolls();
	}

	public void setScroll(JScrollBar scroll) {
		m_hScroll = scroll;
	}

	protected int getNbModels() {
		return m_hModel == null ? m_hGroup.getNbModels() : 1;
	}

	protected MBarPlotModel getModel(int idx) {
		return m_hModel == null ? m_hGroup.getModel(idx) : m_hModel;
	}

	protected void updateScrolls() {
		int nbbars = 0;
		if (m_hModel != null || m_hGroup != null) {
			for (int j = 0; j < getNbModels(); j++) {
				MBarPlotModel model = getModel(j);
				nbbars += model.getNbBars();
			}
		}
		if (m_hScroll != null) {
			m_hScroll.setMinimum(0);
			m_hScroll.setValue(0);
			m_hScroll.setMaximum(nbbars);
		}
	}

	public void reset() {
		m_iModel = 0;
		m_iBar = 0;
	}

	public boolean hasMore() {
		int nb = getNbModels();
		if (m_iModel >= nb) return false;
		if (m_iModel == nb-1) {
			MBarPlotModel model = getModel(m_iModel);
			return m_iBar < model.getNbBars();
		}
		return true;
	}

	public void skip() {
		MBarPlotModel model = getModel(m_iModel);
		if (m_iBar >= model.getNbBars()-1) {
			m_iBar = 0;
			m_iModel++;
		} else {
			m_iBar++;
		}
	}

	public MBarPlotModel getModel() {
		return getModel(m_iModel);
	}

	public int getBar() {
		return m_iBar;
	}

	public void paintComponent(Graphics g) {
 		// Init graphcs
                super.paintComponent(g);
		FontMetrics fm = g.getFontMetrics();
		Insets insets = getInsets();
                int m_wd = getWidth() - insets.left - insets.right;
                int m_hi = getHeight() - insets.top - insets.bottom;
		g.setColor(Color.black);
		g.fillRect(insets.left, insets.top, m_wd, m_hi);
		g.setColor(Color.green);
		if (m_hModel == null && m_hGroup == null) return;
		// Autoscale
		int nb = getNbModels();
		if (nb == 0) return;
		float ymin = Float.MAX_VALUE, ymax = -Float.MAX_VALUE;
		int mypos = 0;
		for (int j = 0; j < nb; j++) {
			MBarPlotModel model = getModel(j);
			model.setOffset(mypos);
			mypos += 5 + model.getNbBars()*13;
			for (int i = 0; i < model.getNbBars(); i++) {
				float y = model.getBarValue(i);
				if (y <= ymin) ymin = y;
				if (y >= ymax) ymax = y;
			}
		}
		m_hVScale.setMinMax(0.0f, ymax);
		ymin = m_hVScale.getRealMin();
		ymax = m_hVScale.getRealMax();
		// Calc sizes
		int y_min = Math.max(10+fm.getHeight(), m_hVScale.getVertUpperBound(fm)) + insets.top;
		int y_xas = m_hi + insets.top - 20;
		int v_wd  = m_hVScale.getVertWidth(this, fm, y_min, y_xas-y_min);
		int x_yas = v_wd+insets.left+5;
		int x_max = m_wd+insets.left-5;
		// Draw axes
		m_hVScale.draw(this, g, x_yas, y_min, x_max-x_yas, y_xas-y_min);
		// Skip til pos
		reset();
		int idx = 0;
		int deltax = 0;
		if (m_hScroll != null) {
			int pos = m_hScroll.getValue();
			while (hasMore() && idx < pos) {
				skip();	idx++;
			}
			if (hasMore())
				deltax = getModel().getOffset()+getBar()*13;
		}
		int nbbars_show = 0;
		int xpos = 0;
		while (hasMore()) {
			MBarPlotModel model = getModel();
			int bar = getBar();
			int xstart = x_yas+10+model.getOffset()-deltax;
			xpos = xstart+bar*13;
			if (xpos >= x_max)
				break;
			// Draw bar
			float y = model.getBarValue(bar);
			g.setColor(m_hColors[bar]);
			int ypos = calc(y, ymin, ymax, y_xas, y_min);
			g.fillRect(xpos, ypos, 10, y_xas-ypos);
			// Draw title
			String title = model.getTitle();
			int twd = fm.stringWidth(title);
			int tpos = xstart+13*model.getNbBars()/2-twd/2;
			if (tpos < xstart) tpos = xstart;
			g.setColor(Color.green);
			g.drawString(title, tpos, 5+fm.getAscent());
			// Done
			nbbars_show++;
			skip();
		}
		if (xpos >= x_max) {
			nbbars_show = Math.max(nbbars_show-1, 1);
			m_hScroll.setVisibleAmount(nbbars_show);
		}
		g.setColor(Color.green);
		g.drawLine(x_yas, y_xas, xpos+13+10, y_xas);
	}

	public void drawClass(Graphics g, int x, int y, int cl) {
		switch (cl) {
			case 0:
				g.drawLine(x-2, y, x+2, y);
				g.drawLine(x, y-2, x, y+2);
				break;
			case 1:
				g.drawOval(x-2, y-2, 4, 4);
				break;
			case 2:
				g.drawLine(x-2, y-2, x+2, y+2);
				g.drawLine(x+2, y-2, x-2, y+2);
				break;
			case 3:
				g.drawRect(x-2, y-2, 4, 4);
				break;
			case 4:
				g.drawLine(x-2, y-2, x+2, y-2);
				g.drawLine(x+2, y-2, x, y+2);
				g.drawLine(x, y+2, x-2, y-2);
				break;
			case 5:
				g.drawLine(x-2, y+2, x+2, y+2);
				g.drawLine(x+2, y+2, x, y-2);
				g.drawLine(x, y-2, x-2, y+2);
				break;
		}
	}

	public void drawBarClass(Graphics g, int x, int y, int w, int h, int cl) {
		g.drawRect(x, y, w, h);
		for (int yp = 0; yp < h-7; yp += 7)
			drawClass(g, x+w/2, y+h-yp-5, cl);
	}

	public void printPostScript(Graphics g) {
		int m_wd = 612-60;
//		int m_hi = 792-60;
		int m_hi = 200;
		if (m_hModel == null && m_hGroup == null) return;
		FontMetrics fm = g.getFontMetrics();
		// Autoscale
		int nb = getNbModels();
		if (nb == 0) return;
		float ymin = Float.MAX_VALUE, ymax = -Float.MAX_VALUE;
		int mypos = 0;
		for (int j = 0; j < nb; j++) {
			MBarPlotModel model = getModel(j);
			model.setOffset(mypos);
			mypos += 5 + model.getNbBars()*13;
			for (int i = 0; i < model.getNbBars(); i++) {
				float y = model.getBarValue(i);
				if (y <= ymin) ymin = y;
				if (y >= ymax) ymax = y;
			}
		}
		m_hVScale.setMinMax(0.0f, ymax);
		ymin = m_hVScale.getRealMin();
		ymax = m_hVScale.getRealMax();
		// Calc sizes
		int y_min = Math.max(10+fm.getHeight(), m_hVScale.getVertUpperBound(fm));
		int y_xas = m_hi - 20;
		int v_wd  = m_hVScale.getVertWidth(this, fm, y_min, y_xas-y_min);
		int x_yas = v_wd+10;
		int x_max = m_wd-10;
		// Draw axes
		m_hVScale.draw(this, g, x_yas, y_min, x_max-x_yas, y_xas-y_min);
		// Skip til pos
		reset();
		int idx = 0;
		int deltax = 0;
		if (m_hScroll != null) {
			int pos = m_hScroll.getValue();
			while (hasMore() && idx < pos) {
				skip();	idx++;
			}
			if (hasMore())
				deltax = getModel().getOffset()+getBar()*13;
		}
		int nbbars_show = 0;
		int xpos = 0;
		while (hasMore()) {
			MBarPlotModel model = getModel();
			int bar = getBar();
			int xstart = x_yas+10+model.getOffset()-deltax;
			xpos = xstart+bar*13;
			if (xpos >= x_max)
				break;
			// Draw bar
			float y = model.getBarValue(bar);
			int ypos = calc(y, ymin, ymax, y_xas, y_min);
			drawBarClass(g, xpos, ypos, 10, y_xas-ypos, bar);
			// Draw title
			String title = model.getTitle();
			int twd = fm.stringWidth(title);
			int tpos = xstart+13*model.getNbBars()/2-twd/2;
			if (tpos < xstart) tpos = xstart;
			g.drawString(title, tpos, 5+fm.getAscent());
			// Done
			nbbars_show++;
			skip();
		}
		g.drawLine(x_yas, y_xas, xpos+13+10, y_xas);
	}

	public void yLabel(String label) {
		m_hVScale.setLabel(label);
	}

	public int calc(float val, float min, float max, int imin, int imax) {
		float p = (val-min)/(max-min)*(imax-imin);
		return (int)Math.round(p) + imin;
	}
}

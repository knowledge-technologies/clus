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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DrawableScrollableCanvas extends JPanel {

	public final static long serialVersionUID = 1;

	protected Drawable m_ScrollTo;
	protected MyDrawableCnv m_hCanvas;
	protected JScrollBar m_hVert, m_hHorz;
	protected int m_AutoGap = 10;

	public DrawableScrollableCanvas() {
		setLayout(new BorderLayout(0,0));
		add(m_hCanvas = new MyDrawableCnv(), BorderLayout.CENTER);
		add(m_hVert = new JScrollBar(JScrollBar.VERTICAL,0,1,0,10000), BorderLayout.EAST);
		m_hVert.addAdjustmentListener(new MyVertListener());
		m_hVert.setVisible(false);
		add(m_hHorz = new JScrollBar(JScrollBar.HORIZONTAL,0,1,0,10000), BorderLayout.SOUTH);
		m_hHorz.addAdjustmentListener(new MyHorzListener());
		m_hHorz.setVisible(false);
		m_hCanvas.addComponentListener(new MyResizeListener());
	}

	public void setScrollTo(Drawable drawable) {
		m_ScrollTo = drawable;
	}

	public DrawableCanvas getCanvas() {
		return (DrawableCanvas)m_hCanvas;
	}

	public void paintAndScroll() {
		m_hCanvas.autoScrollTo();
		m_hCanvas.repaint();
	}

	public void setBackground(Color col) {
		super.setBackground(col);
		System.out.println("Setting background: "+m_hCanvas);
		if (m_hCanvas != null) m_hCanvas.setBackground(col);
	}

	private class MyDrawableCnv extends DrawableCanvas {

		public final static long serialVersionUID = 1;

		public void updateScrollBars() {
			Dimension picSize = getRenderSize();
			Dimension scrSize = getSize();
			if (picSize == null || scrSize == null) return;
			if (picSize.width > scrSize.width) {
				m_hHorz.setMaximum(picSize.width);
                		m_hHorz.setVisibleAmount(scrSize.width);
				m_hHorz.setVisible(true);
			} else {
				m_hHorz.setVisible(false);
				if (getXOrig() != 0) {
					m_hHorz.setValue(0);
					setXOrig(0);
					repaint();
				}
			}
			if (picSize.height > scrSize.height) {
				m_hVert.setMaximum(picSize.height);
				m_hVert.setVisibleAmount(scrSize.height);
				m_hVert.setVisible(true);
			} else {
				m_hVert.setVisible(false);
				if (getYOrig() != 0) {
					m_hVert.setValue(0);
					setYOrig(0);
					repaint();
				}
			}
		}

		public void render(Graphics2D g) {
			super.render(g);
			updateScrollBars();
			autoScrollTo();
		}

		public void autoScrollTo() {
			if (m_ScrollTo != null) {
				int deltax = 0;
				int deltay = 0;
				Dimension size = getSize();
				int top = m_ScrollTo.getY()-m_AutoGap;
				int left = m_ScrollTo.getX()-m_AutoGap;
				int right = m_ScrollTo.getRight()+m_AutoGap;
				int bottom = m_ScrollTo.getYBottom()+m_AutoGap;
				int scr_right = getXOrig()+size.width;
				int scr_bottom = getYOrig()+size.height;
				if (left < getXOrig()) deltax = Math.min(deltax, left-getXOrig());
				if (right > scr_right) deltax = Math.max(deltax,  right-scr_right);
				if (top < getYOrig()) deltay = Math.min(deltay, top-getYOrig());
				if (bottom > scr_bottom) deltay = Math.max(deltay, bottom-scr_bottom);
				if (deltax != 0) {
					m_hCanvas.addXOrig(deltax);
					m_hHorz.setValue(getXOrig());
				}
				if (deltay != 0) {
					m_hCanvas.addYOrig(deltay);
					m_hVert.setValue(getYOrig());
				}
				m_ScrollTo = null;
				if (deltax != 0 || deltay != 0) m_hCanvas.repaint();
			}
		}
	}

	private class MyResizeListener extends ComponentAdapter {

		public void componentResized(ComponentEvent e) {
			m_hCanvas.updateScrollBars();
		}
	}

	public class MyHorzListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			m_hCanvas.setXOrig(m_hHorz.getValue());
			m_hCanvas.repaint();
		}
	}

	public class MyVertListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			m_hCanvas.setYOrig(m_hVert.getValue());
			m_hCanvas.repaint();
		}
	}
}

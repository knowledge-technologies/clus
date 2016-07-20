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
import java.util.*;

public class DrawableCanvas extends JComponent {

	public final static long serialVersionUID = 1;

	public final static int STATE_RENDERERD = 0;
	public final static int STATE_RENDER_AND_SIZE = 1;
	public final static int STATE_RENDER = 2;

	public final static Stroke SINGLE_STROKE = new BasicStroke(1.0f);
	public final static Stroke DOUBLE_STROKE = new BasicStroke(2.0f);

	protected int total_x, total_y;
	protected int screen_x, screen_y;
	protected int net_x, net_y;
	protected int buf_x = 500;
	protected int buf_y = 500;
	protected int orig_x = 0;
	protected int orig_y = 0;

	protected boolean init = false;
	protected ArrayList net[][];
	protected Image back_img;
	protected Color m_BackColor = SystemColor.control;
	protected Color m_FrameColor = Color.black;
	protected int m_FrameSize = 0;
	protected ActionListener m_Rendered;

	protected DrawableTransform m_Transform;
	protected DrawableRenderer renderer;
	protected int renderstate = STATE_RENDERERD;
	protected boolean m_KeyFocus;

	public DrawableCanvas() {
		addMouseListener(new MyMouseListener());
	}

	public void setRenderDoneListener(ActionListener listener) {
		m_Rendered = listener;
	}

	public void setKeyFocusEnabled(boolean focus) {
		m_KeyFocus = focus;
	}

	public boolean isFocusTraversable() {
		return m_KeyFocus;
	}

	public void init(int bufwd, int bufhi, int totwd, int tothi) {
		buf_x = bufwd; buf_y = bufhi;
		init(totwd, tothi);
	}

	public void init(int totwd, int tothi) {
		total_x = totwd; total_y = tothi;
		net_x = ceilDiv(totwd, buf_x);
		net_y = ceilDiv(tothi, buf_y);
		createNet(net_x, net_y);
		init = true;
	}

	public void setRenderState(int state) {
		renderstate = state;
	}

	public boolean isRendered() {
		return renderstate == STATE_RENDERERD;
	}

	public void setGridSize(int bufwd, int bufhi) {
		buf_x = bufwd;
		buf_y = bufhi;
	}

	public void resizeNet(int wd, int hi) {
		init(wd, hi);
	}

	public void clear() {
		for (int ctr_x = 0; ctr_x < net_x; ctr_x++)
			for (int ctr_y = 0; ctr_y < net_y; ctr_y++)
				net[ctr_x][ctr_y].clear();
	}

	public void setRenderer(DrawableRenderer renderer) {
		this.renderer = renderer;
		this.renderstate = STATE_RENDER_AND_SIZE;
	}

	public static int ceilDiv(int num, int div) {
		if (num % div == 0) return num/div;
		else return num/div+1;
	}

	public void setOrigin(int x, int y) {
		orig_x = x;
		orig_y = y;
	}

	public void setXOrig(int x) {
		orig_x = x;
	}

	public void setYOrig(int y) {
		orig_y = y;
	}

	public void addXOrig(int x) {
		orig_x += x;
	}

	public void addYOrig(int y) {
		orig_y += y;
	}

	public int getXOrig() {
		return orig_x;
	}

	public int getYOrig() {
		return orig_y;
	}

	public Point getOrigin() {
		return new Point(orig_x, orig_y);
	}

	public Dimension getTotal() {
		return new Dimension(total_x, total_y);
	}

	public void setBackground(Image bkg) {
		if (bkg == null) return;
		back_img = bkg;
	}

	public void setBackground(Color col) {
		System.out.println("Background: "+col);
		m_BackColor = col;
	}

	public void setFrame(int size, Color col) {
		m_FrameSize = size;
		m_FrameColor = col;
	}

	public void initBackground(Graphics g, int dx, int dy, int pos_x, int pos_y, int wd, int hi) {
		g.setClip(0,0,wd,hi);
		if (back_img != null) {
			int xsiz = back_img.getWidth(this);
			int ysiz = back_img.getHeight(this);
			int xofs = -pos_x % xsiz + dx;
			int yofs = -pos_y % ysiz + dy;
			for (int xp = xofs; xp < wd; xp += xsiz)
				for (int yp = yofs; yp < hi; yp += ysiz)
					g.drawImage(back_img,xp,yp,this);
		} else {
			g.setColor(m_BackColor);
			g.fillRect(0,0,wd,hi);
		}
		if (m_FrameSize != 0) {
			g.setColor(m_FrameColor);
			Insets ins = getInsets();
			Dimension size = getSize();
			int mwd = size.width - ins.left - ins.right;
			int mhi = size.height - ins.top - ins.bottom;
			g.fillRect(ins.left, ins.top, mwd, m_FrameSize);
			g.fillRect(ins.left, ins.top+mhi-m_FrameSize, mwd, m_FrameSize);
			g.fillRect(ins.left, ins.top, m_FrameSize, mhi);
			g.fillRect(ins.left+mwd-m_FrameSize, ins.top, m_FrameSize, mhi);
		}
	}

	public ArrayList getRectangle(int x, int y) {
		int ctr_x = x/buf_x;
		int ctr_y = y/buf_y;
		return net[ctr_x][ctr_y];
	}

	public Point getDisplayedMaxPoint() {
		Dimension dim = getSize();
		int myx = Math.min(orig_x+dim.width, net_x*buf_x);
		int myy = Math.min(orig_y+dim.height, net_y*buf_y);
		return new Point(myx, myy);
	}

	public Point getPosition(Drawable drawable, int xp, int yp) {
		int xpos = xp, ypos = yp;
		Point pt = getDisplayedMaxPoint();
		Rectangle rect = drawable.getBoundRect();
		if (xpos+rect.width+3 > pt.x) xpos = pt.x-rect.width-3;
		if (ypos+rect.height+3 > pt.y) ypos = pt.y-rect.height-3;
		xpos = Math.max(5, xpos);
		ypos = Math.max(5, ypos);
		return new Point(xpos, ypos);
	}

	public Point getCenterCoords(Rectangle rect) {
		Point pt = getDisplayedMaxPoint();
		int wd = Math.abs(pt.x - orig_x);
		int hi = Math.abs(pt.y - orig_y);
		return new Point(orig_x+wd/2-rect.width/2, orig_y+hi/2-rect.height/2);
	}

	public void removeDrawable(Drawable drawable) {
		Rectangle rect = drawable.getBoundRect();
		int x2 = rect.x + rect.width;
		int y2 = rect.y + rect.height;
		//Calculate rect's
		int rx_1 = rect.x/buf_x;
		int rx_2 = x2/buf_x;
		int ry_1 = rect.y/buf_y;
		int ry_2 = y2/buf_y;
		for (int ctr_x = rx_1; ctr_x <= rx_2; ctr_x++)
			for (int ctr_y = ry_1; ctr_y <= ry_2; ctr_y++)
				net[ctr_x][ctr_y].remove(drawable);
	}

	public void removeDrawableSafe(Drawable drawable) {
		Rectangle rect = drawable.getBoundRect();
		int x2 = rect.x + rect.width;
		int y2 = rect.y + rect.height;
		//Calculate rect's
		int rx_1 = rect.x/buf_x;
		int rx_2 = x2/buf_x;
		int ry_1 = rect.y/buf_y;
		int ry_2 = y2/buf_y;
		if (rx_2 >= net_x) return;
		if (ry_2 >= net_y) return;
		for (int ctr_x = rx_1; ctr_x <= rx_2; ctr_x++) {
			for (int ctr_y = ry_1; ctr_y <= ry_2; ctr_y++) {
				net[ctr_x][ctr_y].remove(drawable);
			}
		}
	}

	public void removeDrawableAll(Drawable drawable) {
		for (int ctr_x = 0; ctr_x < net_x; ctr_x++)
			for (int ctr_y = 0; ctr_y < net_y; ctr_y++)
				net[ctr_x][ctr_y].remove(drawable);
	}

	public void addDrawable(Drawable drawable) {
		if (m_Transform != null) m_Transform.transform(drawable);
		Rectangle rect = drawable.getBoundRect();
		int x2 = rect.x + rect.width;
		int y2 = rect.y + rect.height;
		//Calculate rect's
		int rx_1 = rect.x/buf_x;
		int rx_2 = x2/buf_x;
		int ry_1 = rect.y/buf_y;
		int ry_2 = y2/buf_y;
		for (int ctr_x = rx_1; ctr_x <= rx_2; ctr_x++)
			for (int ctr_y = ry_1; ctr_y <= ry_2; ctr_y++)
				net[ctr_x][ctr_y].add(drawable);
	}

	public void addDrawableNoTrans(Drawable drawable) {
		Rectangle rect = drawable.getBoundRect();
		int x2 = rect.x + rect.width;
		int y2 = rect.y + rect.height;
		//Calculate rect's
		int rx_1 = rect.x/buf_x;
		int rx_2 = x2/buf_x;
		int ry_1 = rect.y/buf_y;
		int ry_2 = y2/buf_y;
		for (int ctr_x = rx_1; ctr_x <= rx_2; ctr_x++)
			for (int ctr_y = ry_1; ctr_y <= ry_2; ctr_y++)
				net[ctr_x][ctr_y].add(drawable);
	}

	public void setTransform(DrawableTransform trans) {
		m_Transform = trans;
	}

	public Dimension transformDimension(Dimension dim) {
		if (m_Transform == null) return dim;
		else return m_Transform.transformDimension(dim);
	}

	public void drawRectangle(Graphics2D g, int dx, int dy, int rec_x, int rec_y) {
		int delta_x = rec_x*buf_x-orig_x;
		int delta_y = rec_y*buf_y-orig_y;
		int clip_x = delta_x;
		int clip_y = delta_y;
		int clip_w = buf_x;
		int clip_h = buf_y;
		if (delta_x < 0) {
			clip_x = 0;
			clip_w += delta_x;
		}
		if (delta_y < 0) {
			clip_y = 0;
			clip_h += delta_y;
		}
		clip_w = Math.min(clip_w, screen_x-clip_x-dx);
		clip_h = Math.min(clip_h, screen_y-clip_y-dy);
		g.setClip(clip_x+dx, clip_y+dx, clip_w, clip_h);
		int m_orig_x = orig_x+dx;
		int m_orig_y = orig_y+dy;
		ArrayList curr = net[rec_x][rec_y];
		for (int i = 0; i < curr.size(); i++) {
			Drawable drawable = (Drawable)curr.get(i);
			drawable.draw(g, this, m_orig_x, m_orig_y);
		}
	}

	public Dimension getRenderSize() {
		if (renderer != null) return renderer.getSize();
		else return new Dimension(0,0);
	}

	public DrawableRenderer getRenderer() {
		return renderer;
	}

	public void render(Graphics2D g) {
		if (renderer != null) {
			FontMetrics fm = g.getFontMetrics();
			renderer.render(g, fm, this);
			Dimension size = renderer.getSize();
			renderer.removeAll(this);
			resizeNet(size.width, size.height);
			renderer.addAll(this);
			renderstate = STATE_RENDERERD;
			if (m_Rendered != null) m_Rendered.actionPerformed(null);
		} else {
			clear();
		}
	}

	public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (renderstate != STATE_RENDERERD) render(g2);
		Insets insets = getInsets();
                screen_x = getWidth() - insets.left - insets.right;
                screen_y = getHeight() - insets.top - insets.bottom;
		initBackground(g2, insets.left, insets.top, orig_x, orig_y, screen_x, screen_y);
		if (init) {
			int rx_1 = Math.min(orig_x/buf_x, net_x-1);
			int rx_2 = Math.min((orig_x+screen_x)/buf_x, net_x-1);
			int ry_1 = Math.min(orig_y/buf_y, net_y-1);
			int ry_2 = Math.min((orig_y+screen_y)/buf_y, net_y-1);
			for (int ctr_x = rx_1; ctr_x <= rx_2; ctr_x++)
				for (int ctr_y = ry_1; ctr_y <= ry_2; ctr_y++)
					drawRectangle(g2, insets.left, insets.top, ctr_x, ctr_y);
		}
/*              Dimension size = renderer.getSize();
                g.setColor(Color.black);
                g.setClip(insets.left, insets.top, size.width+1, size.height+1);
                g.drawRect(insets.left, insets.top, size.width, size.height);*/
	}

	private void createNet(int net_x, int net_y) {
		net = new ArrayList[net_x][net_y];
		for (int ctr_x = 0; ctr_x < net_x; ctr_x++)
			for (int ctr_y = 0; ctr_y < net_y; ctr_y++)
				net[ctr_x][ctr_y] = new ArrayList();
	}

	public class MyMouseListener extends MouseAdapter {

		public void mousePressed(MouseEvent evt) {
			if (m_KeyFocus) requestFocus();
			if (!init) return;
			int xp = evt.getX()+orig_x;
			int yp = evt.getY()+orig_y;
			int ctr_x = Math.min(xp/buf_x, net_x-1);
			int ctr_y = Math.min(yp/buf_y, net_y-1);
			ArrayList field = net[ctr_x][ctr_y];
			for (int ctr = field.size()-1; ctr >= 0; ctr--) {
				Drawable drawable = (Drawable)field.get(ctr);
				Rectangle rect = drawable.getBoundRect();
				if (rect.contains(new Point(xp,yp))) {
					drawable.mousePressed(DrawableCanvas.this,xp,yp,evt);
					return;
				}
			}
		}
	}
}


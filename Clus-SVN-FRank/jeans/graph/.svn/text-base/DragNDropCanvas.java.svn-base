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

package jeans.graph;

import java.awt.*;


public abstract class DragNDropCanvas extends BufferCanvas {

	protected final static int max_dx = 150;
	protected final static int max_dy = 150;
	protected Image m_image, m_buf_img;
	protected Graphics m_buf_grp;
	protected boolean is_dragging;
	protected int drag_x, drag_wd, drag_x0, image_w;
	protected int drag_y, drag_hi, drag_y0, image_h;


	public DragNDropCanvas(int wd, int hi) {
		super(wd,hi);
		is_dragging = false;
		m_image = null;
	}

	public void setDragImage(Image image) {
		m_image = image;
		image_w = m_image.getWidth(this);
		image_h = m_image.getHeight(this);
	}

	public Dimension getDragImageSize() {
		return new Dimension(image_w, image_h);
	}

	public boolean isDragging() {
		return is_dragging;
	}

	public Image makeImage(int wd, int hi) {
		return createImage(wd, hi);
	}

	public synchronized void dragTo(int xp, int yp) {
		if (is_dragging) {
			drag_x0 = Math.min(drag_x,xp);
			drag_y0 = Math.min(drag_y,yp);
			drag_wd = Math.abs(drag_x-xp)+image_w;
			drag_hi = Math.abs(drag_y-yp)+image_h;
			drag_x = xp;
			drag_y = yp;
		} else {
			is_dragging = true;
			drag_x0 = drag_x = xp;
			drag_y0 = drag_y = yp;
			drag_wd = image_w;
			drag_hi = image_h;
		}
		Graphics g = getGraphics();
		drawDragImage(g);
		if (drag_wd > 150 || drag_hi > 150) {
			aroundDragImage(g, bufSiz);
		}
	}

	public void drop() {
		is_dragging = false;
	}

	public void update(Graphics g) {
		Dimension dim = getSize();
		reSize(dim);
		if (!is_dragging) {
			g.drawImage(bufImg,0,0,this);
		} else {
			aroundDragImage(g, dim);
			drawDragImage(g);
		}
	}

	public void toScreen(Graphics g, Dimension dim) {
		if (!is_dragging) {
			g.drawImage(bufImg,0,0,this);
		} else {
			aroundDragImage(g, dim);
			drawDragImage(g);
		}
	}

	private void aroundDragImage(Graphics g, Dimension dim) {
		g.setClip(0,0,drag_x0,dim.height);
		g.drawImage(bufImg,0,0,this);
		int x_clip = drag_x0+drag_wd;
		g.setClip(x_clip,0,dim.width-x_clip,dim.height);
		g.drawImage(bufImg,0,0,this);
		g.setClip(drag_x0,0,drag_wd,drag_y0);
		g.drawImage(bufImg,0,0,this);
		int y_clip = drag_y0+drag_hi;
		g.setClip(drag_x0,y_clip,drag_wd,dim.height-y_clip);
		g.drawImage(bufImg,0,0,this);
	}

	private void drawDragImage(Graphics g) {
		if (m_buf_img == null) makeBufImg();
		m_buf_grp.setClip(0,0,drag_wd,drag_hi);
		m_buf_grp.drawImage(bufImg,-drag_x0,-drag_y0,this);
		m_buf_grp.drawImage(m_image,drag_x-drag_x0,drag_y-drag_y0,this);
		g.setClip(drag_x0,drag_y0,drag_wd,drag_hi);
		g.drawImage(m_buf_img,drag_x0,drag_y0,this);
	}

	private void makeBufImg() {
		m_buf_img = createImage(max_dx, max_dy);
		m_buf_grp = m_buf_img.getGraphics();
	}

}




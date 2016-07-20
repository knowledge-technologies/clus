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
import java.util.*;
import java.io.*;

import jeans.util.MStreamTokenizer;
import jeans.resource.MediaCache;
import jeans.graph.drawable.Drawable;
import jeans.graph.drawable.DrawableProvider;
import jeans.graph.drawable.DrawableLine;
import jeans.graph.drawable.DrawableImage;

public class TextScroller extends BufferCanvas implements DrawableProvider {

	public final static long serialVersionUID = 1;

	private int diff = 2;
	private int border = 2;
	private int yofs = 0;
	private int init_delay = 3000;
	private int next_delay = 50;
	private int incr_y = 1;
	private int max_y = 0;
	Dimension d = null;
	private boolean running = false;
	private Vector drawables = new Vector();
	private Color cr_color = Color.green;
	private Font cr_font = new Font("Times", Font.PLAIN, 12);
	private Toolkit toolkit = Toolkit.getDefaultToolkit();

	public TextScroller(int wd, int hi) {
		super(wd,hi);
	}

	public void paintIt(Graphics g, Dimension d) {
		this.d = d;
		updateRegion(bufGrp,border,d.height-border,d.width-2*border);
	}

	public void updateRegion(Graphics g, int y0, int y1, int w) {
		Color c = getBackground();
		int hi = y1-y0+1;
		g.setColor(c);
		g.fillRect(border,y0,w-2*border,hi);
		int size = drawables.size();
		if (size == 0) return;
		int cr_drawable = 0;
		Drawable drawable = (Drawable)drawables.elementAt(cr_drawable);
		//while (cr_drawable < size-1 && drawable.isInY(y0+yofs) == 1)
		//	drawable = (Drawable)drawables.elementAt(++cr_drawable);
		while (cr_drawable < size-1 && drawable.isInY(y1+yofs) != -1) {
			drawable.draw(this,0,yofs);
			drawable = (Drawable)drawables.elementAt(++cr_drawable);
		}
		if (drawable.isInY(y1+yofs) != -1)
			drawable.draw(this,0,yofs);
	}

	public Graphics getDGraphics() {
		return bufGrp;
	}

	public Canvas getDCanvas() {
		return this;
	}

	public FontMetrics getDMetrics(Font font) {
		return toolkit.getFontMetrics(font);
	}

	public void startScrolling() {
		running = true;
		Thread thread = new ScrollThread();
		thread.start();
	}

	public void stopScrolling() {
		running = false;
	}

	public void setScrollText(Reader reader) throws IOException {
		String token = null;
		MStreamTokenizer tokens = new MStreamTokenizer(reader);
		int xp = 5;
		int yp = 4;
		do {
			if (tokens.isNextToken(":")) {
				token = tokens.readToken();
				if (token.equals("COLOR")) setColor(tokens);
				if (token.equals("FONT")) setFont(tokens);
				if (token.equals("XTAB")) xp += setTab(tokens);
				if (token.equals("YTAB")) yp += setTab(tokens);
				if (token.equals("IMAGE")) {
					DrawableImage image = setImage(tokens);
					image.setPosition(xp, yp);
					Rectangle rect = image.getBoundRect(this);
					yp += rect.height + diff;
					drawables.addElement(image);
				}
			} else {
				token = tokens.readTillEol();
				if (token != null) {
					DrawableLine line = new DrawableLine(token, cr_font, false);
					line.setPosition(xp, yp);
					line.setColor(cr_color);
					Rectangle rect = line.getBoundRect(this);
					yp += rect.height + diff;
					drawables.addElement(line);
				}
			}
		} while (token != null);
		max_y = yp;
	}

	private void setColor(MStreamTokenizer tokens) throws IOException {
		String red = tokens.readToken();
		String green = tokens.readToken();
		String blue = tokens.readToken();
		int cred = 0;
		int cgreen = 0;
		int cblue = 0;
		try {
			cred = Integer.parseInt(red);
			cgreen = Integer.parseInt(green);
			cblue = Integer.parseInt(blue);
		} catch (NumberFormatException ex) {}
		cr_color = new Color(cred,cgreen,cblue);
	}

	private void setFont(MStreamTokenizer tokens) throws IOException {
		String name = tokens.readToken();
		String type = tokens.readToken();
		String size = tokens.readToken();
		int ftype = Font.PLAIN;
		int fsize = 12;
		if (type.equals("BOLD")) ftype = Font.BOLD;
		if (type.equals("ITALIC")) ftype = Font.ITALIC;
		try {
			fsize = Integer.parseInt(size);
		} catch (NumberFormatException ex) {}
		cr_font = new Font(name,ftype,fsize);
	}

	private int setTab(MStreamTokenizer tokens) throws IOException {
		String tab = tokens.readToken();
		int ftab = 0;
		try {
			ftab = Integer.parseInt(tab);
		} catch (NumberFormatException ex) {}
		return ftab;
	}

	private DrawableImage setImage(MStreamTokenizer tokens) throws IOException {
		String image = tokens.readToken();
		Image fimage = MediaCache.getInstance().getImage(image);
		return new DrawableImage(fimage);
	}

	private class ScrollThread extends Thread {

		public void run() {
			try {
				Thread.sleep(init_delay);
				while (running) {
					//Do scroll paint
					bufGrp.setClip(border,border,d.width-2*border,d.height-2*border);
					bufGrp.copyArea(border,border+incr_y,d.width-2*border,d.height-incr_y-border,0,-incr_y);
					bufGrp.setClip(border,d.height-incr_y-border,d.width-2*border,incr_y);
					updateRegion(bufGrp,d.height-incr_y-border,d.height-border,d.width);
					//Repaint
					repaint();
					//Adjust
					yofs += incr_y;
					if (yofs > max_y-d.height*70/100) {
						yofs = -d.height;
					}
					Thread.sleep(next_delay);
				}
			} catch (InterruptedException e) {}
		}

	}
}

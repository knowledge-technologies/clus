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

/*
 * Copyright (c) 1995, 1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jeans.graph.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;

public class TransformFilter extends ImageFilter {
	public final static int Rot90    = 0;
	public final static int Rot180   = 1;
	public final static int Rot270   = 2;
	public final static int FlipHorz = 3;
	public final static int FlipVert = 4;

	protected static ColorModel defaultRGB = ColorModel.getRGBdefault();
	protected int raster[];
	protected int dstW, dstH, srcW, srcH, type;

	public TransformFilter(int mytype) {
		type = mytype;
	}

	public void setDimensions(int width, int height) {
		if (type == Rot90 || type == Rot270) {
			srcH = dstW = height;
			srcW = dstH = width;
		} else {
			srcH = dstH = height;
			srcW = dstW = width;
		}
		raster = new int[srcW*srcH];
		consumer.setDimensions(dstW, dstH);
	}

	public void setColorModel(ColorModel model) {
		consumer.setColorModel(defaultRGB);
	}

	public void setHints(int hintflags) {
		consumer.setHints(TOPDOWNLEFTRIGHT
                          | COMPLETESCANLINES
                          | SINGLEPASS
                          | (hintflags & SINGLEFRAME));
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte pixels[], int off, int scansize) {
		int srcoff = off;
		int dstoff = y * srcW + x;
		for (int yc = 0; yc < h; yc++) {
			for (int xc = 0; xc < w; xc++) {
				raster[dstoff++] = model.getRGB(pixels[srcoff++] & 0xff);
			}
			srcoff += (scansize - w);
			dstoff += (srcW - w);
		}
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model,
					int pixels[], int off, int scansize) {
		int srcoff = off;
		int dstoff = y * srcW + x;
		if (model == defaultRGB) {
			for (int yc = 0; yc < h; yc++) {
				System.arraycopy(pixels, srcoff, raster, dstoff, w);
				srcoff += scansize;
				dstoff += srcW;
			}
		} else {
			for (int yc = 0; yc < h; yc++) {
				for (int xc = 0; xc < w; xc++) {
					raster[dstoff++] = model.getRGB(pixels[srcoff++]);
				}
				srcoff += (scansize - w);
				dstoff += (srcW - w);
			}
		}
	}

	public void rot90deg() {
		int pixels[] = new int[dstW];
		int addr, mxaddr;
		mxaddr = srcW*srcH;
		for (int dy = 0; dy < dstH; dy++) {
			for (int dx = 0; dx < dstW; dx++) {
				//pixels[dx] = raster[dx*srcW+dy];
				addr = dx*srcW+srcW-dy-1;
				if (addr >= 0 && addr < mxaddr) {
					pixels[dx] = raster[addr];
				} else {
					pixels[dx] = 0;
				}
			}
			consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
		}
	}

	public void rot180deg() {
		int pixels[] = new int[dstW];
		int addr, mxaddr;
		mxaddr = srcW*srcH;
		for (int dy = 0; dy < dstH; dy++) {
			for (int dx = 0; dx < dstW; dx++) {
				addr = (dstW-dx)+(dstH-dy)*srcW;
				if (addr >= 0 && addr < mxaddr) {
					pixels[dx] = raster[addr];
				} else {
					pixels[dx] = 0;
				}
			}
			consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
		}
	}

	public void rot270deg() {
		int pixels[] = new int[dstW];
		int addr, mxaddr;
		mxaddr = srcW*srcH;
		for (int dy = 0; dy < dstH; dy++) {
			for (int dx = 0; dx < dstW; dx++) {
				addr = dy + (srcH-dx)*srcW;
				if (addr >= 0 && addr < mxaddr) {
					pixels[dx] = raster[addr];
				} else {
					pixels[dx] = 0;
				}
			}
			consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
		}
	}

	public void fliphorz() {
		int pixels[] = new int[dstW];
		int addr, mxaddr;
		mxaddr = srcW*srcH;
		for (int dy = 0; dy < dstH; dy++) {
			for (int dx = 0; dx < dstW; dx++) {
				addr = (dstW-dx-1)+dy*srcW;
				if (addr >= 0 && addr < mxaddr) {
					pixels[dx] = raster[addr];
				} else {
					pixels[dx] = 0;
				}
			}
			consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
		}
	}

	public void flipvert() {
		int pixels[] = new int[dstW];
		int addr, mxaddr;
		mxaddr = srcW*srcH;
		for (int dy = 0; dy < dstH; dy++) {
			for (int dx = 0; dx < dstW; dx++) {
				addr = dx+(dstH-dy-1)*srcW;
				if (addr >= 0 && addr < mxaddr) {
					pixels[dx] = raster[addr];
				} else {
					pixels[dx] = 0;
				}
			}
			consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
		}
	}

	public void imageComplete(int status) {
		if (status == IMAGEERROR || status == IMAGEABORTED) {
			consumer.imageComplete(status);
			return;
		}
		switch (type) {
			case Rot90 :   rot90deg();  break;
			case Rot180:   rot180deg(); break;
			case Rot270:   rot270deg(); break;
			case FlipHorz: fliphorz();  break;
			case FlipVert: flipvert();  break;
		}
		consumer.imageComplete(status);
	}
}

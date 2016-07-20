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

public class ScaleImageFilter extends ImageFilter {

	protected static ColorModel defaultRGB = ColorModel.getRGBdefault();
	protected int raster[];
	protected int width, height;
	protected int zoom;

	public ScaleImageFilter(int zoom) {
		this.zoom = zoom;
	}

	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
		raster = new int[width*height];
		consumer.setDimensions(width*zoom/100, height*zoom/100);
	}

	public void setColorModel(ColorModel model) {
		consumer.setColorModel(defaultRGB);
	}

	public void setHints(int hintflags) {
		consumer.setHints(TOPDOWNLEFTRIGHT | COMPLETESCANLINES | SINGLEPASS | (hintflags & SINGLEFRAME));
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte pixels[], int off, int scansize) {
		int srcoff = off;
		int dstoff = y * width + x;
		for (int yc = 0; yc < h; yc++) {
			for (int xc = 0; xc < w; xc++) {
				raster[dstoff++] = model.getRGB(pixels[srcoff++] & 0xff);
			}
			srcoff += (scansize - w);
			dstoff += (width - w);
		}
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model,
					int pixels[], int off, int scansize) {
		int srcoff = off;
		int dstoff = y * width + x;
		if (model == defaultRGB) {
			for (int yc = 0; yc < h; yc++) {
				System.arraycopy(pixels, srcoff, raster, dstoff, w);
				srcoff += scansize;
				dstoff += width;
			}
		} else {
			for (int yc = 0; yc < h; yc++) {
				for (int xc = 0; xc < w; xc++) {
					raster[dstoff++] = model.getRGB(pixels[srcoff++]);
				}
				srcoff += (scansize - w);
				dstoff += (width - w);
			}
		}
	}

	public void imageComplete(int status) {
		if (status == IMAGEERROR || status == IMAGEABORTED) {
			consumer.imageComplete(status);
			return;
		}
		int scanwd = width*zoom/100;
		int scanhi = height*zoom/100;
		int[] pixels = new int[scanwd];
		for (int r = 0; r < scanhi; r++) {
			int srcoffs = r*100/zoom*width;
			for (int c = 0; c < scanwd; c++) {
				pixels[c] = raster[srcoffs+c*100/zoom];
			}
			consumer.setPixels(0, r, scanwd, 1, defaultRGB, pixels, 0, scanwd);
		}
		consumer.imageComplete(status);
	}
}

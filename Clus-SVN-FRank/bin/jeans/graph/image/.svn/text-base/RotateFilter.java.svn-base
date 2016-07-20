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

public class RotateFilter extends TransformFilter {

	private double sin, cos;
	private int o_x, o_y;
	private double coord[] = new double[2];

	public RotateFilter(double angle, int ox, int oy) {
		super(FlipHorz);
		o_x = ox;
		o_y = oy;
	        sin = Math.sin(angle);
	        cos = Math.cos(angle);
	}

    public void transform(double x, double y, double[] retcoord) {
        // Remember that the coordinate system is upside down so apply
        // the transform as if the angle were negated.
        // cos(-angle) =  cos(angle)
        // sin(-angle) = -sin(angle)
        retcoord[0] = cos * x + sin * y;
        retcoord[1] = cos * y - sin * x;
    }

    public void itransform(double x, double y, double[] retcoord) {
        // Remember that the coordinate system is upside down so apply
        // the transform as if the angle were negated.  Since inverting
        // the transform is also the same as negating the angle, itransform
        // is calculated the way you would expect to calculate transform.
        retcoord[0] = cos * x - sin * y;
        retcoord[1] = cos * y + sin * x;
    }

    public void imageComplete(int status) {
        if (status == IMAGEERROR || status == IMAGEABORTED) {
            consumer.imageComplete(status);
            return;
        }
        int pixels[] = new int[dstW];
	int mxaddr = srcW*srcH;
	for (int dy = 0; dy < dstH; dy++) {
		for (int dx = 0; dx < dstW; dx++) {
			transform(dx-o_x,dy-o_y,coord);
			int xp = (int)Math.round(coord[0]+o_x);
			int yp = (int)Math.round(coord[1]+o_y);
			int addr = yp*srcW + xp;
			if (addr >= 0 && addr < mxaddr) {
				pixels[dx] = raster[addr];
			} else {
				pixels[dx] = 0;
			}
		}
		consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
	}
        consumer.imageComplete(status);
    }

}

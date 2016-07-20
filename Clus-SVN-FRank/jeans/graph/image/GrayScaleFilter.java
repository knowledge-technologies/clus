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

package jeans.graph.image;

import java.awt.image.*;

/**
 * @author 	Jan Struyf
 * @version 	1.4, 1/8/98
 */
public class GrayScaleFilter extends RGBImageFilter {


    public GrayScaleFilter() {
	canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
	int red = (rgb >> 16) & 0xff;
	int green = (rgb >> 8) & 0xff;
	int blue = (rgb >> 8) & 0xff;
	int gray = (red+green+blue)/3;
	return (rgb & 0xff000000) | (gray << 16) | (gray << 8) | gray;
    }

}


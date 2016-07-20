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

import java.awt.*;
import java.awt.image.*;

/**
 * @author 	Jan Struyf
 * @version 	1.4, 1/8/98
 */
public class ColorChangeFilter extends RGBImageFilter {

    int scolor, tcolor;

    public ColorChangeFilter(Color a, Color b) {
	scolor = (a.getRed() << 16) | (a.getGreen() << 8) | (a.getBlue() << 0);
	tcolor = (b.getRed() << 16) | (b.getGreen() << 8) | (b.getBlue() << 0);
	canFilterIndexColorModel = true;
    }

    public ColorChangeFilter(String a, String b) {
	this(stringToColor(a),stringToColor(b));
    }

    public int filterRGB(int x, int y, int rgb) {
	int lorgb = rgb & 0x00ffffff;
	if (lorgb == scolor) lorgb = tcolor;
	return (rgb & 0xff000000) | lorgb;
    }

    public String toString() {
	return Integer.toString(scolor,16) + " -> " + Integer.toString(tcolor,16);
    }

    public static Color stringToColor(String strg)
				throws NumberFormatException {
	int ofs = 0;
	if (strg.startsWith("0x")) ofs = 2;
	else if (strg.startsWith("#")) ofs = 1;
	try {
		int red = Integer.parseInt(strg.substring(ofs,ofs+2),16);
		int green = Integer.parseInt(strg.substring(ofs+2,ofs+4),16);
		int blue = Integer.parseInt(strg.substring(ofs+4,ofs+6),16);
		return new Color(red,green,blue);
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new NumberFormatException();
	}
    }
}


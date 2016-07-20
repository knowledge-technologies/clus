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

public class StringIconPainter implements Painter {

	BufferCanvas cnv;
        Vector strings = new Vector();
        Image icon;
        int fillwidth = 0;
        Color background = Color.white;
        Color textcolor = Color.black;
        Color fillcolor = Color.black;

	public StringIconPainter(Image icon, BufferCanvas cnv) {
		this.cnv = cnv;
                this.icon = icon;
	}

	public void setFill(Color color, int width) {
		fillcolor = color;
		fillwidth = width;
	}

        public void addString(String string) {
                strings.addElement(string);
        }

        public void setBackground(Color color) {
           this.background = color;
        }

        public void setTextColor(Color color) {
           this.textcolor = color;
        }

	public void paint(Graphics graph, Canvas canvas) {
		Dimension dim = cnv.getBufferSize();
                int iw = icon.getWidth(canvas);
                int ih = icon.getHeight(canvas);
  		FontMetrics fm = graph.getFontMetrics();
                int nb_strs = strings.size();
		int hi = Math.max(nb_strs*fm.getHeight(), ih+10)+10+fillwidth*2;
                int wd = 0;
		if (nb_strs > 0) {
	                for (int ctr = 0; ctr < nb_strs; ctr++) {
        	                String string = (String)strings.elementAt(ctr);
                	        wd = Math.max(wd, fm.stringWidth(string));
	                }
        	        wd += iw+35+fillwidth*2;
		} else {
			wd += iw+20+fillwidth*2;
		}
		int x0 = (dim.width-wd)/2;
		int ylvl = dim.height/2;
                int y0 = ylvl - hi/2;
                //Draw background :o)
		graph.setColor(background);
		graph.fillRect(x0,y0,wd,hi);
		graph.setColor(textcolor);
		graph.drawRect(x0,y0,wd,hi);
		if (fillwidth != 0) {
			graph.setColor(fillcolor);
			graph.fillRect(x0+10,y0+10,wd-20,hi-20);
			x0 += fillwidth;
			y0 += fillwidth;
			wd -= 2*fillwidth;
			hi -= 2*fillwidth;
		}
                //Draw image
                graph.setColor(background);
                ImageUtil.draw3DRect(graph, x0+5, y0+5, iw+10, ih+10, 2, false);
                graph.drawImage(icon, x0+10, y0+10, canvas);
                //Draw strings
		graph.setColor(textcolor);
                for (int ctr = 0; ctr < nb_strs; ctr++) {
                        String string = (String)strings.elementAt(ctr);
                        graph.drawString(string,x0+iw+25,y0+5+ctr*fm.getHeight()+fm.getAscent());
                }
	}
}


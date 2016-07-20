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

public class StringPainter implements Painter {

	BufferCanvas cnv;
	String string;

	public StringPainter(String string, BufferCanvas cnv) {
		this.string = string;
		this.cnv = cnv;
	}

	public void paint(Graphics graph, Canvas canvas) {
		Dimension dim = cnv.getBufferSize();
		cnv.setFont(graph, dim.width, string);
		FontMetrics fm = graph.getFontMetrics();
		int hi = fm.getHeight()+6;
		int wd = fm.stringWidth(string)+10;
		int x0 = (dim.width-wd)/2;
		int ylvl = dim.height/2;
		graph.setColor(Color.white);
		graph.fillRect(x0,ylvl-hi,wd,hi);
		graph.setColor(Color.black);
		graph.drawRect(x0,ylvl-hi,wd,hi);
		graph.setColor(Color.red);
		graph.drawString(string,x0+5,ylvl-fm.getDescent()-3);
	}

}


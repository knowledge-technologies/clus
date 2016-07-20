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
 * Copyright (C) 1998 Jan Struyf
 *
 * Please mail me if you
 *	- 've found bugs
 *	- like this program
 *	- don't like a particular feature
 *	- would like somthing to be modified
 *
 * My email is: jan.struyf@student.kuleuven.ac.be
 *	        http://ace.ulyssis.student.kuleuven.ac.be/~jeans
 *
 * I always give it my best shot to make a program useful and solid, but
 * remeber that there is absolutely no warranty for using this program as
 * stated in the following terms:
 *
 * THERE IS NO WARRANTY FOR THIS PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW. THE COPYRIGHT HOLDER AND/OR OTHER PARTIES WHO MAY HAVE MODIFIED THE
 * PROGRAM, PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS
 * TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE
 * PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,
 * REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW WILL ANY COPYRIGHT HOLDER,
 * OR ANY OTHER PARTY WHO MAY MODIFY AND/OR REDISTRIBUTE THE PROGRAM,
 * BE LIABLE TO YOU FOR DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR
 * CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE
 * PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE OF THE
 * PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF SUCH HOLDER OR OTHER
 * PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * May the Force be with you... Just compile it & use it!
 */

package jeans.graph.plot;

import java.awt.*;
import javax.swing.*;

public class MultiPointLineGraph extends JComponent {

	public final static long serialVersionUID = 1;

	public final static Font font = new Font("Courrier",Font.PLAIN,12);

	int channels;
	int width;

	Color[] colors;
	float ydata[][];
	float xdata[];

	String xlabel, ylabel;

	MHorizontalGraphScale horz = new MHorizontalGraphScale();
	MVerticalGraphScale vert = new MVerticalGraphScale();

	public MultiPointLineGraph(int num, int width) {
		this.channels = num;
		this.width = width;
		colors = new Color[num];
		xdata = new float[width];
		ydata = new float[width][channels];
		for (int ctr = 0; ctr < num; ctr++) colors[ctr] = Color.black;
	}

	 public void paintComponent(Graphics g) {
 		// Init graphcs
                super.paintComponent(g);
		FontMetrics fm = g.getFontMetrics();
		Insets insets = getInsets();
                int m_wd = getWidth() - insets.left - insets.right;
                int m_hi = getHeight() - insets.top - insets.bottom;
		g.setColor(Color.black);
		g.fillRect(insets.left, insets.top, m_wd, m_hi);
		g.setColor(Color.green);
		// Autoscale
		float xmin = Float.MAX_VALUE, xmax = -Float.MAX_VALUE;
		float ymin = Float.MAX_VALUE, ymax = -Float.MAX_VALUE;
		for (int pt = 0; pt < width; pt++) {
			for (int ctr = 0; ctr < channels; ctr++) {
				float y = ydata[pt][ctr];
				if (y <= ymin) ymin = y;
				if (y >= ymax) ymax = y;
			}
			float x = xdata[pt];
			if (x <= xmin) xmin = x;
			if (x >= xmax) xmax = x;
		}
		// Horizontal axis
		horz.setMinMax(xmin, xmax);
		xmin = horz.getRealMin();
		xmax = horz.getRealMax();
		// Vertical axis
		vert.setMinMax(ymin, ymax);
		ymin = vert.getRealMin();
		ymax = vert.getRealMax();
		// Calc sizes
		int y_min = Math.max(3, vert.getVertUpperBound(fm)) + insets.top;
		int y_xas = m_hi + insets.top - horz.getHorzHeight(fm);
		int v_wd  = vert.getVertWidth(this, fm, y_min, y_xas-y_min);
		int x_yas = Math.max(v_wd, horz.getHorzLeftBound(fm))+insets.left;
		int x_max = m_wd+insets.left-5-horz.getHorzRightBound(fm);
		// Draw axes
		horz.draw(this, g, x_yas, y_xas, x_max-x_yas, y_xas-y_min);
		vert.draw(this, g, x_yas, y_min, x_max-x_yas, y_xas-y_min);
		//Draw the graphs
		for (int ctr = 0; ctr < channels; ctr++) {
			g.setColor(colors[ctr]);
			int xprev = calcX(0,xmin,xmax,x_yas,x_max);
			int yprev = calcY(ctr,0,ymin,ymax,y_min,y_xas);
			g.fillRect(xprev-2, yprev-2, 4, 4);
			for (int pt = 1; pt < width; pt++) {
				int xp = calcX(pt,xmin,xmax,x_yas,x_max);
				int yp = calcY(ctr,pt,ymin,ymax,y_min,y_xas);
				g.drawLine(xprev,yprev,xp,yp);
				g.fillRect(xp-2, yp-2, 4, 4);
				xprev = xp; yprev = yp;
			}
		}
	}

	public void xLabel(String label) {
		horz.setLabel(label);
	}

	public void yLabel(String label) {
		vert.setLabel(label);
	}

	public void addYPoint(int pos, int channel, float value) {
		ydata[pos][channel] = value;
	}

	public void addXPoint(int pos, float value) {
		xdata[pos] = value;
	}

	public void color(int which, Color color) {
		colors[which] = color;
	}

	public int calcX(int num, float xmin, float xmax, int min, int max) {
		float xp = (xdata[num]-xmin)/(xmax-xmin)*(max-min);
		return (int)Math.round(xp) + min;
	}

	public int calcY(int chan, int num, float ymin, float ymax, int min, int max) {
		float yp = (ydata[num][chan]-ymin)/(ymax-ymin);
		yp = (1-yp)*(max-min);
		return (int)yp + min;
	}

}





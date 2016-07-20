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

public class MultiLevelMatrixGraph extends JComponent {

	public final static long serialVersionUID = 1;

	public final static Font font = new Font("Courrier",Font.PLAIN,12);

	int channels, cols, rows;

	Color[] colors;
	float matrix[][][];

	String xlabel, ylabel;

	MHorizontalStepScale horz = new MHorizontalStepScale();
	MVerticalStepScale vert = new MVerticalStepScale();

	public MultiLevelMatrixGraph(int chan, int cols, int rows) {
		this.channels = chan;
		this.cols = cols;
		this.rows = rows;
		colors = new Color[chan];
		matrix = new float[cols][rows][chan];
		for (int ctr = 0; ctr < chan; ctr++) colors[ctr] = Color.black;
	}

	public void setXRange(float xmin, float xmax, float step) {
		horz.setStep(step);
		horz.setMinMax(xmin, xmax);
	}

	public void setYRange(float ymin, float ymax, float step) {
		vert.setStep(step);
		vert.setMinMax(ymin, ymax);
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
		float zmin = Float.MAX_VALUE, zmax = -Float.MIN_VALUE;
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				for (int ch = 0; ch < channels; ch++) {
					float z = matrix[col][row][ch];
					if (z <= zmin) zmin = z;
					if (z >= zmax) zmax = z;
				}
			}
		}
		// Calc sizes
		int x_max = m_wd-5-horz.getHorzRightBound(fm)+insets.left;
		int y_xas = m_hi - horz.getHorzHeight(fm)+insets.top;
		int y_min = Math.max(3, vert.getVertUpperBound(fm))+insets.top;
		int rowhi = (y_xas-y_min)/rows+1;
		vert.autoStep(fm, y_min+rowhi/2, y_xas-y_min-rowhi);
		int v_wd  = vert.getVertWidth(this, fm, y_min+rowhi/2, y_xas-y_min-rowhi);
		int x_yas = Math.max(v_wd, horz.getHorzLeftBound(fm))+insets.left;
		int colwd = (x_max-x_yas)/cols+1;
		horz.autoStep(fm, x_yas+colwd/2, x_max-x_yas-colwd);
		// Draw axis
		horz.draw(this, g, x_yas+colwd/2, y_xas, x_max-x_yas-colwd, y_xas-y_min);
		vert.draw(this, g, x_yas, y_min+rowhi/2, x_max-x_yas, y_xas-y_min-rowhi);
		g.drawLine(x_yas, y_xas, x_yas+colwd, y_xas);
		g.drawLine(x_max-colwd, y_xas, x_max, y_xas);
		g.drawLine(x_yas, y_xas, x_yas, y_xas-rowhi);
		g.drawLine(x_yas, y_min, x_yas, y_min+rowhi);
		//Draw the graphs
		for (int col = 0; col < cols; col++) {
			int xp = (x_max-x_yas)*col/cols+x_yas+1;
			for (int row = 0; row < rows; row++) {
				int yp = y_xas - (y_xas-y_min)*(row+1)/rows - 1;
				// Calc color
				float above = -Float.MAX_VALUE;
				Color color = Color.black;
				for (int ch = 0; ch < channels; ch++) {
					float z = matrix[col][row][ch];
					if (z > above) {
						above = z;
						color = colors[ch];
					}
				}
				g.setColor(calcC(color, above, zmin, zmax));
				g.fillRect(xp, yp, colwd, rowhi);
			}
		}
	}

	public void xGap(int gap) {
		horz.setGap(gap);
	}

	public void yGap(int gap) {
		vert.setGap(gap);
	}

	public void xLabel(String label) {
		horz.setLabel(label);
	}

	public void yLabel(String label) {
		vert.setLabel(label);
	}

	public void color(int which, Color color) {
		colors[which] = color;
	}

	public void setMatrix(int col, int row, int chan, float value) {
		matrix[col][row][chan] = value;
	}

	public Color calcC(Color color, float val, float min, float max) {
		float fac = (float)Math.min((val-min)/(max-min)+0.25f, 1.0f);
		float red = fac*color.getRed();
		float green = fac*color.getGreen();
		float blue = fac*color.getBlue();
		return new Color((int)red, (int)green, (int)blue);
	}

}





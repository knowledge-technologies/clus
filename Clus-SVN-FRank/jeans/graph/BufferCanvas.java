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
 * Digger !
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

package jeans.graph;

import java.awt.*;

public abstract class BufferCanvas extends Canvas {
	Dimension minSize;

	public final static Font fonts[] = createFonts();

	protected Image bufImg;
	protected Graphics bufGrp;
	protected Dimension bufSiz;

	public BufferCanvas(int minx, int miny) {
		minSize = new Dimension(minx, miny);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public Dimension getMinimumSize() {
		return minSize;
	}

	public void setFont(Graphics g, int size, String mys) {
		int font_no = 4;
	        FontMetrics fm = g.getFontMetrics(fonts[font_no]);
		while (fm.stringWidth(mys) > size && font_no > 0) {
			font_no--;
   		        fm = g.getFontMetrics(fonts[font_no]);
		}
		g.setFont(fonts[font_no]);
	}

	public void reSize(Dimension d) {
	     if ((bufGrp == null) || (d.width != bufSiz.width) || (d.height != bufSiz.height)) {
			if (d.width == 0 || d.height == 0) return;
			bufSiz = d;
			bufImg = createImage(d.width,d.height);
			bufGrp = bufImg.getGraphics();
			paintIt(bufGrp, bufSiz);
	      }
	}

	public void paint(Graphics g) {
        	update(g);
	}

	public void update(Graphics g) {
		reSize(getSize());
		g.drawImage(bufImg,0,0,this);
	}

	public void redraw() {
		if (bufGrp != null)
			paintIt(bufGrp, bufSiz);
		repaint();
	}

	public Dimension getBufferSize() {
		return bufSiz;
	}

	public Graphics getBufferGraphics() {
		return bufGrp;
	}

	public abstract void paintIt(Graphics g, Dimension d);

	public static Font[] createFonts() {
		Font fonts[] = new Font[5];
		for (int ctr = 0; ctr < 5; ctr++)
			fonts[ctr] = new Font("Courrier",Font.PLAIN,8+ctr*4);
		return fonts;
	}
}






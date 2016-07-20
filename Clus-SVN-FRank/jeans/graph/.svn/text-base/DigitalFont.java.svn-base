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
 *
 *
 * <APPLET code=Digger.class width=600 height=380>
 * <PARAM name=fps value=6>
 * </APPLET>
 */

package jeans.graph;

import java.awt.*;

public class DigitalFont {

	public Color dig_on_col = Color.green;
	public Color dig_off_col = Color.black;

	Graphics bufGrp;

	private int xpts[], ypts[];
	private final static int[] segs =
		{1+2+4+8+16+32,1+2,32+1+64+8+4,32+1+64+2+4,16+64+1+2,32+16+64+2+4,
		 32+16+8+4+2+64,32+1+2,32+16+64+1+2+8+4,16+32+1+64+2+4};

	public DigitalFont() {
		xpts = new int[6];
		ypts = new int[6];
	}

	public void setGraphics(Graphics graph) {
		bufGrp = graph;
	}

	public void drawNumber(int x, int y, int m_wd, int m_hi, int num, int dignum) {
		int d = Math.min(m_hi/6,m_wd/dignum/5);
		int w = (m_wd - (dignum-1)*d*2/3 - dignum*2*d) / dignum;
		int h = (m_hi - 3*d)/2;
		int t_wd = (w + 2*d) * dignum + (dignum-1)*3*d/2;
		int xp = x+t_wd+(m_wd-t_wd)/2;
		int yp = y;
		int betw = num;
		for (int dig = 0; dig < dignum; dig++) {
			int digit = betw % 10;
			xp -= w+2*d;
			drawDigit(xp,yp,w,h,d,digit);
			xp -= 2*d/3;
			betw /= 10;
		}
	}

	public void drawSeparator(int x, int y, int h, int d, char sepchar) {
	        bufGrp.setColor(dig_on_col);
		if (sepchar == ':') {
			bufGrp.fillRect(x,y+h/5,d,d);
			bufGrp.fillRect(x,y+4*h/5-d,d,d);
		}
	}

	public void drawDigit(int x, int y, int w, int h, int d, int n) {
		int seg = segs[n];
		//Above
		if ((seg & 32) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+2*d/3;   ypts[0] = y+d/2;
			xpts[1] = x+d;       ypts[1] = y+d/6;
			xpts[2] = x+d+w;     ypts[2] = y+d/6;
			xpts[3] = x+d+w+d/3; ypts[3] = y+d/2;
			xpts[4] = x+d+w;     ypts[4] = y+5*d/6;
			xpts[5] = x+d;       ypts[5] = y+5*d/6;
			bufGrp.fillPolygon(xpts,ypts,6);
		//Middle
		if ((seg & 64) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+2*d/3;   ypts[0] = y+d/2+d+h;
			xpts[1] = x+d;       ypts[1] = y+d/6+d+h;
			xpts[2] = x+d+w;     ypts[2] = y+d/6+d+h;
			xpts[3] = x+d+w+d/3; ypts[3] = y+d/2+d+h;
			xpts[4] = x+d+w;     ypts[4] = y+5*d/6+d+h;
			xpts[5] = x+d;       ypts[5] = y+5*d/6+d+h;
			bufGrp.fillPolygon(xpts,ypts,6);
		//Bottom
		if ((seg & 4) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+2*d/3;   ypts[0] = y+d/2+2*(d+h);
			xpts[1] = x+d;       ypts[1] = y+d/6+2*(d+h);
			xpts[2] = x+d+w;     ypts[2] = y+d/6+2*(d+h);
			xpts[3] = x+d+w+d/3; ypts[3] = y+d/2+2*(d+h);
			xpts[4] = x+d+w;     ypts[4] = y+5*d/6+2*(d+h);
			xpts[5] = x+d;       ypts[5] = y+5*d/6+2*(d+h);
			bufGrp.fillPolygon(xpts,ypts,6);
		//Left top
		if ((seg & 16) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+d/2;    ypts[0] = y+2*d/3;
			xpts[1] = x+5*d/6;  ypts[1] = y+d;
			xpts[2] = x+5*d/6;  ypts[2] = y+d+h;
			xpts[3] = x+d/2;    ypts[3] = y+d+h+d/3;
			xpts[4] = x+d/6;    ypts[4] = y+d+h;
			xpts[5] = x+d/6;    ypts[5] = y+d;
			bufGrp.fillPolygon(xpts,ypts,6);
		//Right top
		if ((seg & 1) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+d/2+d+w;    ypts[0] = y+2*d/3;
			xpts[1] = x+5*d/6+d+w;  ypts[1] = y+d;
			xpts[2] = x+5*d/6+d+w;  ypts[2] = y+d+h;
			xpts[3] = x+d/2+d+w;    ypts[3] = y+d+h+d/3;
			xpts[4] = x+d/6+d+w;    ypts[4] = y+d+h;
			xpts[5] = x+d/6+d+w;    ypts[5] = y+d;
			bufGrp.fillPolygon(xpts,ypts,6);
		//Left bottom
		if ((seg & 8) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+d/2;    ypts[0] = y+2*d/3+d+h;
			xpts[1] = x+5*d/6;  ypts[1] = y+d+d+h;
			xpts[2] = x+5*d/6;  ypts[2] = y+d+h+d+h;
			xpts[3] = x+d/2;    ypts[3] = y+d+h+d/3+d+h;
			xpts[4] = x+d/6;    ypts[4] = y+d+h+d+h;
			xpts[5] = x+d/6;    ypts[5] = y+d+d+h;
			bufGrp.fillPolygon(xpts,ypts,6);
		//Right bottom
		if ((seg & 2) != 0) bufGrp.setColor(dig_on_col);
		else bufGrp.setColor(dig_off_col);
			xpts[0] = x+d/2+d+w;    ypts[0] = y+2*d/3+d+h;
			xpts[1] = x+5*d/6+d+w;  ypts[1] = y+d+d+h;
			xpts[2] = x+5*d/6+d+w;  ypts[2] = y+d+h+d+h;
			xpts[3] = x+d/2+d+w;    ypts[3] = y+d+h+d/3+d+h;
			xpts[4] = x+d/6+d+w;    ypts[4] = y+d+h+d+h;
			xpts[5] = x+d/6+d+w;    ypts[5] = y+d+d+h;
			bufGrp.fillPolygon(xpts,ypts,6);
	}

}




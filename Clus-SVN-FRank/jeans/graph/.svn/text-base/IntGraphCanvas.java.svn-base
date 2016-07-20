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

public class IntGraphCanvas extends BufferCanvas {

	public final static long serialVersionUID = 1;

	protected String m_labelx, m_labely;
	protected int[] m_data, m_type;
	protected IntGraphType[] m_datatypes = new IntGraphType[1];

	public IntGraphCanvas(int wd, int hi) {
		super(wd, hi);
	}

	public void setNbData(int size) {
		if (m_data == null || m_data.length != size) {
			m_data = new int[size];
			m_type = new int[size];
		}
	}

	public void setDataItem(int idx, int value, int type) {
		m_data[idx] = value;
		m_type[idx] = type;
	}

	public void addDataType(int type, String name, Color color) {
		if (m_datatypes.length <= type) {
			IntGraphType[] newtypes = new IntGraphType[type+1];
			System.arraycopy(m_datatypes,0,newtypes,0,m_datatypes.length);
			m_datatypes = newtypes;
		}
		System.out.println("Adding data type: "+type+" "+name+" "+color);
		m_datatypes[type] = new IntGraphType(name, color);
	}

	public void setLabelX(String label) {
		m_labelx = label;
	}

	public void setLabelY(String label) {
		m_labely = label;
	}

	public void paintIt(Graphics g, Dimension d) {
		g.setColor(getBackground());
		g.fillRect(0,0,d.width,d.height);
		FontMetrics fm = g.getFontMetrics();
		int legwd = 0;
		if (m_datatypes.length > 1) {
			for (int i = 0; i < m_datatypes.length; i++) {
				IntGraphType t = m_datatypes[i];
				legwd = Math.max(legwd, fm.stringWidth(t.m_name));
			}
			legwd += 10 + 5 + 5 + 5;
		}
		int labelxhi = 0;
		if (m_labelx != null) labelxhi = fm.getHeight()+5;
		int labelywd = 0;
		if (m_labely != null) labelywd = fm.stringWidth(m_labely) + 5;
		int leftwd = d.width - legwd - labelywd;
		int nbdata = m_data.length;
		int ygraph = d.height - labelxhi - 3;
		int wd_i = Math.min(leftwd / nbdata, 30);
		int updelta = Math.max(5,wd_i/2);
		int maxvalue = 0;
		int scale = 100;
		if (nbdata > 0) {
			for (int i = 0; i < m_data.length; i++) {
				int value = m_data[i];
				maxvalue = Math.max(maxvalue, value);
			}
			scale = (ygraph-updelta)*100/maxvalue;
		}
		int bases = (int)Math.floor(Math.log(maxvalue)/Math.log(10)-1);
		int factor = (int)Math.round(Math.pow(10,bases));
		int step = factor;
		while (maxvalue / step > 5)
			step *= 5;
		int crfac = step;
		int scalewd = 0;
		while (crfac <= maxvalue) {
			scalewd = Math.max(scalewd, fm.stringWidth(String.valueOf(crfac)));
			crfac += step;
		}
		scalewd += 3 + 3;
		int xgstart = 5 + labelywd + scalewd;
		int xp = xgstart + 5;
		leftwd -= scalewd;
		wd_i = Math.min(leftwd / nbdata, 30);
		if (nbdata > 0) {
			for (int i = 0; i < m_data.length; i++) {
				int value = m_data[i]*scale/100;
				int type = m_type[i];
				if (type < m_datatypes.length) g.setColor(m_datatypes[type].m_color);
				ImageUtil.drawCube(g,xp,ygraph-value,wd_i*4/6,value,wd_i*3/6,(float)0.5);
				xp += wd_i;
			}
		}
		g.setColor(Color.black);
		g.drawLine(xgstart,ygraph+1,xgstart,updelta);
		g.drawRect(xgstart,ygraph+1,leftwd,2);
		crfac = step;
		while (crfac <= maxvalue) {
			int pos = ygraph-crfac*scale/100;
			g.drawLine(xgstart-2, pos, xgstart, pos);
			String strg = String.valueOf(crfac);
			g.drawString(strg,xgstart-6-fm.stringWidth(strg), pos+fm.getMaxAscent()/2);
			crfac += step;
		}
		if (m_labelx != null) {
			int lyw = fm.stringWidth(m_labelx);
			int xly = xgstart + leftwd/2 - lyw/2;
			int yly = d.height - 5;
			g.drawString(m_labelx,xly,yly);
		}
		if (m_labely != null) {
			g.drawString(m_labely,5,5+5+fm.getMaxAscent()/2);
		}
		int xleg = d.width - legwd + 5;
		int yleg = 5;
		int ylegdelta = Math.max(fm.getHeight(), 10) + 2;
		if (m_datatypes.length > 1) {
			for (int i = 0; i < m_datatypes.length; i++) {
				IntGraphType t = m_datatypes[i];
				g.setColor(t.m_color);
				g.fillRect(xleg,yleg,10,10);
				g.setColor(Color.black);
				g.drawRect(xleg,yleg,10,10);
				g.drawString(t.m_name,xleg+10+5,yleg+5+fm.getMaxAscent()/2);
				yleg += ylegdelta;
			}
		}
	}

}

class IntGraphType {

	public Color m_color;
	public String m_name;

	public IntGraphType(String name, Color color) {
		m_name = name;
		m_color = color;
	}

}

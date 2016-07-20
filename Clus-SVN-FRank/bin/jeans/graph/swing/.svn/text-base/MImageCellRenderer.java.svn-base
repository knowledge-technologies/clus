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

package jeans.graph.swing;

import javax.swing.*;
import java.awt.*;

public class MImageCellRenderer extends Component implements ListCellRenderer {

	public final static long serialVersionUID = 1;

	public final static int BORDER = 1;

	protected Image m_hIcon;
	protected String m_sName;
	protected boolean m_bSelected, m_bFocus;
	protected Dimension m_hDimension;
	protected Component m_hParent;
	protected int m_iWidth, m_iHeight, m_iIconWd, m_iIconHi;

	public MImageCellRenderer(Image image, Component parent) {
		m_hIcon = image;
		m_hParent = parent;
	}

	public Dimension getPreferredSize() {
		m_hDimension = new Dimension(m_iWidth+2*BORDER,m_iHeight+2*BORDER);
		return m_hDimension;
	}

    	public Dimension getMinimumSize() {
		m_hDimension = new Dimension(m_iWidth+2*BORDER,m_iHeight+2*BORDER);
		return m_hDimension;
	}

    	public void paint(Graphics g) {
		Dimension size = getSize();
		if (m_bSelected) {
			g.setColor(SystemColor.textHighlight);
			g.fillRect(0, 0, size.width-2, size.height-1);
			g.setColor(SystemColor.textHighlightText);
		} else {
			g.setColor(SystemColor.textText);
		}
		FontMetrics fm = g.getFontMetrics();
		g.drawImage(m_hIcon,BORDER,BORDER,this);
		g.drawString(m_sName, 5*BORDER + m_iIconWd, size.height/2+fm.getAscent()-fm.getHeight()/2);
		if (m_bFocus) {
			g.setColor(Color.black);
			g.drawRect(0, 0, size.width-2, size.height-1);
		}
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		m_sName = value.toString();
		m_bSelected = isSelected;
		m_bFocus = cellHasFocus;
		m_iIconWd = m_hIcon.getWidth(this);
		m_iIconHi = m_hIcon.getHeight(this);
		FontMetrics fm = m_hParent.getGraphics().getFontMetrics();
		m_iWidth = m_iIconWd + 4*BORDER + fm.stringWidth(m_sName);
		m_iHeight = Math.max(m_iIconHi, fm.getHeight());
		m_hDimension = new Dimension(m_iWidth+2*BORDER,m_iHeight+2*BORDER);
		return this;
	}

 }

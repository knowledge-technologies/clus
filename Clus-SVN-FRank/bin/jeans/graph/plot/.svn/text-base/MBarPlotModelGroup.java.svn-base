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

package jeans.graph.plot;

import java.util.*;

public class MBarPlotModelGroup {

	protected Vector m_hModels = new Vector();
	protected String m_hTitle;

	public MBarPlotModelGroup(String title) {
		m_hTitle = title;
	}

	public int getNbModels() {
		return m_hModels.size();
	}

	public String getTitle() {
		return m_hTitle;
	}

	public void addModel(MBarPlotModel model) {
		m_hModels.addElement(model);
	}

	public MBarPlotModel getModel(int idx) {
		return (MBarPlotModel)m_hModels.elementAt(idx);
	}
}

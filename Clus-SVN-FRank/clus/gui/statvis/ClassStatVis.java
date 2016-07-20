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

package clus.gui.statvis;

import jeans.graph.swing.drawable.*;
import jeans.graph.plot.*;

import java.awt.*;

import clus.statistic.*;

public class ClassStatVis implements ClusStatVisualizer, MDistrInfo {

	public final static Color[] m_Colors = {Color.red, Color.yellow, Color.cyan, Color.blue, Color.green, Color.white, Color.black};

	ClassificationStat m_Stat;

	public ClassStatVis() {
	}

	public ClassStatVis(ClassificationStat stat) {
		m_Stat = stat;
	}

	public Drawable createInstance(ClusStatistic stat) {
		ClassStatVis sv = new ClassStatVis((ClassificationStat)stat);
		return new DrawableDistrGraph(0, 0, sv, (float)stat.m_SumWeight);
	}

	public int getNbBins() {
		return m_Stat.getNbClasses(0);
	}

	public float getBinCount(int idx) {
		return (float)m_Stat.getCount(0, idx);
	}

	public Color getBinColor(int idx) {
		return m_Colors[idx % m_Colors.length];
	}

	public static Color getBinColorStatic(int idx) {
		return m_Colors[idx % m_Colors.length];
	}
}

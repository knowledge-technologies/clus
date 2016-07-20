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

package clus.ext.ilevelc;

import java.util.*;

import jeans.util.*;
import clus.data.rows.*;

public class DerivedConstraintsComputer {

	public ArrayList m_Points;
	public ArrayList m_Constraints;

	public DerivedConstraintsComputer(ArrayList points, ArrayList constr) {
		m_Points = points;
		m_Constraints = constr;
	}

	public void compute() {
		indexPoints();
		DisjointSetForest dsf = createDSFWithMustLinks();
		ArrayList[] comps = assignPointsToComponents(dsf);
		HashSet set = createCannotLinkSet(dsf);
		m_Constraints.clear();
		addTransitiveClosureOfMustLinks(comps);
		addCannotLinkConstraints(set, comps);
	}

	public DisjointSetForest createDSFWithMustLinks() {
		DisjointSetForest dsf = new DisjointSetForest(m_Points.size());
		dsf.makeSets(m_Points.size());
		for (int i = 0; i < m_Constraints.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
			int type = ic.getType();
			if (type == ILevelConstraint.ILevelCMustLink) {
				int t1 = ic.getT1().getIndex();
				int t2 = ic.getT2().getIndex();
				dsf.union(t1, t2);
			}
		}
		return dsf;
	}

	public ArrayList[] assignPointsToComponents(DisjointSetForest dsf) {
		int nbComps = dsf.numberComponents();
		ArrayList[] comps = new ArrayList[nbComps];
		for (int i = 0; i < nbComps; i++) {
			comps[i] = new ArrayList();
		}
		for (int i = 0; i < m_Points.size(); i++) {
			comps[dsf.getComponent(i)].add(m_Points.get(i));
		}
		return comps;
	}

	public HashSet createCannotLinkSet(DisjointSetForest dsf) {
		HashSet set = new HashSet();
		for (int i = 0; i < m_Constraints.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
			int type = ic.getType();
			if (type == ILevelConstraint.ILevelCCannotLink) {
				int c1 = dsf.getComponent(ic.getT1().getIndex());
				int c2 = dsf.getComponent(ic.getT2().getIndex());
				set.add(makeEdge(c1, c2));
			}
		}
		return set;
	}

	public void addTransitiveClosureOfMustLinks(ArrayList[] comps) {
		for (int i = 0; i < comps.length; i++) {
			ArrayList compcomps = comps[i];
			for (int j = 0; j < compcomps.size(); j++) {
				DataTuple tj = (DataTuple)compcomps.get(j);
				for (int k = j+1; k < compcomps.size(); k++) {
					DataTuple tk = (DataTuple)compcomps.get(k);
					m_Constraints.add(new ILevelConstraint(tj, tk, ILevelConstraint.ILevelCMustLink));
				}
			}
		}
	}

	public void addCannotLinkConstraints(HashSet set, ArrayList[] comps) {
		Iterator edges = set.iterator();
		while (edges.hasNext()) {
			int[] edge = (int[])edges.next();
			ArrayList comp1 = comps[edge[0]];
			ArrayList comp2 = comps[edge[1]];
			for (int j = 0; j < comp1.size(); j++) {
				DataTuple tj = (DataTuple)comp1.get(j);
				for (int k = 0; k < comp2.size(); k++) {
					DataTuple tk = (DataTuple)comp2.get(k);
					m_Constraints.add(new ILevelConstraint(tj, tk, ILevelConstraint.ILevelCCannotLink));
				}
			}
		}
	}

	public int[] makeEdge(int i1, int i2) {
		int[] edge = new int[2];
		edge[0] = Math.min(i1, i2);
		edge[1] = Math.max(i1, i2);
		return edge;
	}

	public void indexPoints() {
		for (int i = 0; i < m_Points.size(); i++) {
			DataTuple tuple = (DataTuple)m_Points.get(i);
			tuple.setIndex(i);
		}
	}
}

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

package clus.model.pmml.tildepmml;

import java.util.*;
import java.io.*;

import jeans.util.*;

public class CompoundPredicate {

	protected ArrayList m_ItemSets = new ArrayList();

	public void addItemset(Itemset set) {
		m_ItemSets.add(set);
	}

	public void print(PrintWriter out, int tabs) {
		StringUtils.printTabs(out, tabs);
		out.println("<CompoundPredicate booleanOperator=\"and\">");

		for (int i = 0; i < m_ItemSets.size(); i++) {
			Itemset set = (Itemset)m_ItemSets.get(i);

			StringUtils.printTabs(out, tabs);
			out.println("<ItemsetRef itemsetRef=\""+set.getId()+"\" />");
		}

		StringUtils.printTabs(out, tabs);
		out.println("</CompoundPredicate>");
	}
}

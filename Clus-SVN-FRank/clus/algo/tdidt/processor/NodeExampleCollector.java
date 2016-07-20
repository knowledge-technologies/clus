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

package clus.algo.tdidt.processor;

import jeans.util.*;
import jeans.tree.*;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.type.*;
import clus.main.Settings;
import clus.model.ClusModel;

import java.io.*;

public class NodeExampleCollector extends BasicExampleCollector {

	protected String m_FName;
	protected MyArray m_Attrs;
	protected boolean m_Missing;
	protected Settings m_Sett;

	public NodeExampleCollector(String fname, boolean missing, Settings sett) {
		m_FName = fname;
		m_Missing = missing;
		m_Sett = sett;
	}

	public void initialize(ClusModel model, ClusSchema schema) {
		m_Attrs = new MyArray();
		int nb = schema.getNbAttributes();
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_KEY) m_Attrs.addElement(at);
		}
		if (m_Attrs.size() == 0) {
			for (int i = 0; i < nb; i++) {
				ClusAttrType at = schema.getAttrType(i);
				if (at.getStatus() == ClusAttrType.STATUS_TARGET) m_Attrs.addElement(at);
			}
		}
		super.initialize(model, schema);
	}

	public void terminate(ClusModel model) throws IOException {
		ClusNode root = (ClusNode)model;
		writeFile(root);
		root.clearVisitors();
	}

	public final void writeFile(ClusNode root) throws IOException {
		PrintWriter wrt = m_Sett.getFileAbsoluteWriter(m_FName);
		LeafTreeIterator iter = new LeafTreeIterator(root);
		while (iter.hasMoreNodes()) {
			ClusNode node = (ClusNode)iter.getNextNode();
			MyArray visitor = (MyArray)node.getVisitor();
			wrt.print("leaf("+node.getID()+",[");
			for (int i = 0; i < visitor.size(); i++) {
				DataTuple tuple = (DataTuple)visitor.elementAt(i);
				if (i > 0) wrt.print(",");
				if (m_Missing) wrt.print("("+tuple.getWeight()+",");
                                int attrsize = m_Attrs.size();
				if (attrsize > 1) wrt.print("[");
				for (int j = 0; j < m_Attrs.size(); j++) {
				    //This is just temporarily done to avoid to long outputs
				    try{
				    	StringAttrType at = (StringAttrType)m_Attrs.elementAt(j);
					wrt.print(at.getString(tuple));
				    }
				    catch(ClassCastException cce) {
					//Temporarily, as mentioned above
				    }
				}
				if (m_Attrs.size() > 1) wrt.print("]");
				if (m_Missing) wrt.print(")");
			}
			wrt.println("]).");
		}
		wrt.close();
	}
}


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
 * Created on Jun 8, 2005
 */
package clus.model.modelio;

import java.io.*;
import java.util.*;

import jeans.io.*;

import clus.model.*;

public class ClusModelCollectionIO implements Serializable {

	public final static long serialVersionUID = 1L;

	protected ArrayList m_ModelInfos = new ArrayList();

	public int getNbModels() {
		return m_ModelInfos.size();
	}

	public void addModel(ClusModelInfo model) {
		m_ModelInfos.add(model);
	}

	public void insertModel(int idx, ClusModelInfo model) {
		m_ModelInfos.add(null);
		for (int i = m_ModelInfos.size()-1; i >= idx+1; i--) {
			m_ModelInfos.set(i, m_ModelInfos.get(i-1));
		}
		m_ModelInfos.set(idx, model);
	}

	public ClusModelInfo getModelInfo(int index) {
		return (ClusModelInfo)m_ModelInfos.get(index);
	}

	public ClusModel getModel(int index) {
		ClusModelInfo info = (ClusModelInfo)m_ModelInfos.get(index);
		return info.getModel();
	}

	public ClusModel getModel(String name) {
		for (int i = 0; i < getNbModels(); i++) {
			ClusModelInfo info = (ClusModelInfo)m_ModelInfos.get(i);
			if (info.getName().equals(name)) return info.getModel();
		}
		return null;
	}

	public void printModelNames() {
		if (getNbModels() == 0) {
			System.out.println("Collection does not contain any models");
		} else {
			for (int i = 0; i < getNbModels(); i++) {
				ClusModelInfo info = (ClusModelInfo)m_ModelInfos.get(i);
				System.out.println("Model: "+info.getName());
			}
		}
	}

	public void save(String filename) throws IOException {
		ObjectSaveStream strm = new ObjectSaveStream(new FileOutputStream(filename));
		strm.writeObject(this);
		strm.close();
	}

	public static ClusModelCollectionIO load(String filename) throws IOException, ClassNotFoundException {
		ObjectLoadStream strm = new ObjectLoadStream(new FileInputStream(filename));
		ClusModelCollectionIO result = (ClusModelCollectionIO)strm.readObject();
		strm.close();
		return result;
	}
}

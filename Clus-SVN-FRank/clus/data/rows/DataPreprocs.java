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

package clus.data.rows;

import clus.util.ClusException;
import jeans.util.*;

public class DataPreprocs {

	protected MyArray m_Preprocs = new MyArray();

	public void addPreproc(TuplePreproc pp) {
		m_Preprocs.addElement(pp);
	}

	public int getNbPasses() {
		int passes = 0;
		int nb = m_Preprocs.size();
		for (int i = 0; i < nb; i++) {
			TuplePreproc pp = (TuplePreproc)m_Preprocs.elementAt(i);
			passes = Math.max(passes, pp.getNbPasses());
		}
		return passes;
	}

	public void preproc(int pass, DataTuple tuple) throws ClusException {
		int nb = m_Preprocs.size();
		for (int i = 0; i < nb; i++) {
			TuplePreproc pp = (TuplePreproc)m_Preprocs.elementAt(i);
			if (pass < pp.getNbPasses()) pp.preproc(pass, tuple);
		}
	}

	public void preprocSingle(DataTuple tuple) throws ClusException {
		int nb = m_Preprocs.size();
		for (int i = 0; i < nb; i++) {
			TuplePreproc pp = (TuplePreproc)m_Preprocs.elementAt(i);
			pp.preprocSingle(tuple);
		}
	}

	public void done(int pass) throws ClusException {
		int nb = m_Preprocs.size();
		for (int i = 0; i < nb; i++) {
			TuplePreproc pp = (TuplePreproc)m_Preprocs.elementAt(i);
			if (pass < pp.getNbPasses()) pp.done(pass);
		}
	}
}

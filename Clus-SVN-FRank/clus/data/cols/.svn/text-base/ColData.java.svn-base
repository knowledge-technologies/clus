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

package clus.data.cols;

import java.util.*;

import clus.io.*;
import clus.main.*;
import clus.algo.tdidt.ClusNode;
import clus.data.ClusData;
import clus.data.type.*;
import clus.util.*;
import clus.data.rows.*;
import clus.error.*;
import clus.selection.*;
import clus.statistic.*;
import clus.data.cols.attribute.*;
import clus.data.io.ClusView;

public class ColData extends ClusData {

	protected ColTarget m_Target;
	protected int m_NbAttrs;
	protected Vector m_Attr = new Vector();

	public ClusData select(ClusSelection sel) {
		ColData res = new ColData();
		int nbsel = sel.getNbSelected();
		for (int i = 0; i < m_NbAttrs; i++) {
			ClusAttribute attr = getAttribute(i);
			res.addAttribute(attr.select(sel, nbsel));
		}
		res.setTarget(m_Target.select(sel, nbsel));
		res.setNbRows(nbsel);
		setNbRows(m_NbRows - nbsel);
		return res;
	}

	public ClusData cloneData() {
		return null;
	}

	public void insert(ClusData data, ClusSelection sel) {
		ColData other = (ColData)data;
		int nb_new = m_NbRows + sel.getNbSelected();
		for (int i = 0; i < m_NbAttrs; i++) {
			ClusAttribute attr = other.getAttribute(i);
			getAttribute(i).insert(attr, sel, nb_new);
		}
		m_Target.insert(other.getColTarget(), sel, nb_new);
		setNbRows(nb_new);
	}

	public ClusRun partition(ClusSummary summary) {
		return new ClusRun(this, summary);
	}

	public void unpartition(ClusRun cr) {
/*		ClusData tset = cr.getTestSet();
		if (tset != null) {
			ClusSelection sel = cr.getTestSelection();
			insert(tset, sel);
		}*/
	}

	public int getNbAttributes() {
		return m_NbAttrs;
	}

	public void setTarget(ColTarget target) {
		m_Target = target;
	}

	public ColTarget getColTarget() {
		return m_Target;
	}

	public ClusAttribute getAttribute(int idx) {
		return (ClusAttribute)m_Attr.elementAt(idx);
	}

	public void addAttribute(ClusAttribute attr) {
		m_Attr.addElement(attr);
		m_NbAttrs++;
	}

	public void resetSplitAttrs() {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttribute attr = (ClusAttribute)m_Attr.elementAt(j);
			attr.setSplit(false);
		}
	}

	public void resize(int nbrows) {
		m_NbRows = nbrows;
		m_Target.resize(nbrows);
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttribute attr = (ClusAttribute)m_Attr.elementAt(j);
			attr.resize(nbrows);
		}
	}

	public void prepareAttributes() {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttribute attr = (ClusAttribute)m_Attr.elementAt(j);
			attr.prepare();
		}
	}

	public void unPrepareAttributes() {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttribute attr = (ClusAttribute)m_Attr.elementAt(j);
			attr.unprepare();
		}
	}

	public ClusView createNormalView(ClusSchema schema) throws ClusException {
		int my_idx = 0;
		ClusView view = new ClusView();
		int nb = schema.getNbAttributes();
		for (int j = 0; j < nb; j++) {
			ClusAttrType at = schema.getAttrType(j);
			switch (at.getStatus()) {
				case ClusAttrType.STATUS_DISABLED:
					view.addAttribute(new DummySerializable());
					break;
				case ClusAttrType.STATUS_TARGET:
					view.addAttribute(at.createTargetAttr(m_Target));
					break;
				default:
					view.addAttribute(getAttribute(my_idx++));
			}
		}
		return view;
	}

	public void attach(ClusNode node) {
/*		ClusAttrType tpe = node.getBestTest().getType();
		node.m_SplitAttr = getAttribute(tpe.getSpecialIndex());*/
	}

	public void calcError(ClusNode node, ClusErrorList par) {
/*		node.attachData(this);
		for (int i = 0; i < m_NbRows; i++) {
			ClusNode pred = node.predict(i);
			ClusStatistic stat = pred.getTotalStat();
			par.addExample(this, i, stat);
		}
		node.detachData();	*/
	}

	public void calcTotalStat(ClusStatistic stat) {
		m_Target.calcTotalStat(stat);
	}

	public double[] getNumeric(int idx) {
		return m_Target.m_Numeric[idx];
	}

	public int[] getNominal(int idx) {
		return null; // m_Target.m_Nominal[idx];
	}

	public void preprocess(int pass, DataPreprocs pps) {
	}
}

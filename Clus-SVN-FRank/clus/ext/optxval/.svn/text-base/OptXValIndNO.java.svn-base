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

package clus.ext.optxval;

import clus.tools.debug.Debug;

import jeans.util.list.*;
import jeans.resource.*;

import java.io.*;

import clus.main.*;
import clus.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.data.type.*;
import clus.model.test.*;

public class OptXValIndNO extends OptXValInduce {

	public OptXValIndNO(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
	}

	public final int mkNewGroups(OptXValGroup mgrp, MyListIter ngrps) {
		int nb_groups = 0;
		int nb = mgrp.getNbFolds();
		for (int i = 0; i < nb; i++) {
			ClusNode fnode = mgrp.getNode(i);
			if (fnode != null) {
				NodeTest mtest = fnode.m_Test;
				// Count number of matching
				int gsize = 0;
				for (int j = i+1; j < nb; j++) {
					ClusNode onode = mgrp.getNode(j);
					if (onode != null && mtest.equals(onode.m_Test)) gsize++;
				}
				// Create group
				int gidx = 1;
				OptXValGroup ngrp = new OptXValGroup(mgrp.getData(), gsize+1);
				ngrp.setTest(mtest);
				ngrp.setFold(0, mgrp.getFold(i));
				if (gsize > 0) {
					for (int j = i+1; j < nb; j++) {
						ClusNode onode = mgrp.getNode(j);
						if (onode != null && mtest.equals(onode.m_Test)) {
							ngrp.setFold(gidx++, mgrp.getFold(j));
							mgrp.cleanNode(j);
						}
					}
				}
				// Show best test
				if (Settings.VERBOSE > 0) ngrp.println();
				ngrps.insertBefore(ngrp);
				nb_groups++;
			}
		}
		return nb_groups;
	}

	public final void xvalInduce(OptXValNode node, OptXValGroup mgrp) {
	    long t0;
if (Debug.debug == 1) {
		t0 = ResourceInfo.getCPUTime();
}

if (Debug.debug == 1) {
		ClusStat.updateMaxMemory();
}

		node.init(mgrp.getFolds());
		mgrp.stopCrit(node);
		if (mgrp.cleanFolds()) return;
		// Optimize for one fold
		if (mgrp.getNbFolds() == 1) {
		        int fold = mgrp.getFold();
			ClusNode onode = new ClusNode();
			onode.m_ClusteringStat = mgrp.getTotStat(fold);
			node.setNode(fold, onode);
			m_DFirst.induce(onode, mgrp.getData().getFoldData(fold));
			return;
		}
		// Init test selectors
		initTestSelectors(mgrp);
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

		findBestTest(mgrp);
if (Debug.debug == 1) {
		ClusStat.deltaTest();
}

		mgrp.preprocNodes(node, this);
		// Make new groups
		MyListIter ngrps = new MyListIter();
		int nb_groups = mkNewGroups(mgrp, ngrps);
if (Debug.debug == 1) {
		ClusStat.deltaSplit();
}

if (Debug.debug == 1) {
		node.m_Time = ResourceInfo.getCPUTime() - t0;
}

		// Recursive calls
		if (nb_groups > 0) {
			int idx = 0;
			node.setNbChildren(nb_groups);
			OptXValGroup grp = (OptXValGroup)ngrps.getFirst();
			while (grp != null) {
				NodeTest test = grp.getTest();
				OptXValSplit split = new OptXValSplit();
				int arity = split.init(grp.getFolds(), test);
				node.setChild(split, idx++);
				RowData gdata = grp.getData();
				long t01;
if (Debug.debug == 1) {
				t01 = ResourceInfo.getCPUTime();
}

				for (int i = 0; i < arity; i++) {
					OptXValNode child = new OptXValNode();
					split.setChild(child, i);
					OptXValGroup cgrp = grp.cloneGroup();
					cgrp.setData(gdata.apply(test, i));
					cgrp.create(m_StatManager, m_NbFolds);
					cgrp.calcTotalStats();
if (Debug.debug == 1) {
					node.m_Time += ResourceInfo.getCPUTime() - t01;
}

if (Debug.debug == 1) {
					ClusStat.deltaSplit();
}

					xvalInduce(child, cgrp);
if (Debug.debug == 1) {
					t01 = ResourceInfo.getCPUTime();
}

				}
				grp = (OptXValGroup)ngrps.getNext();
			}
		}
	}

	public OptXValNode xvalInduce(OptXValGroup mgrp) {
		OptXValNode root = new OptXValNode();
		xvalInduce(root, mgrp);
		return root;
	}
}

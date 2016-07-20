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

package clus.ext.ootind;

import clus.tools.debug.Debug;

import clus.ext.optxval.*;

import java.io.*;
import java.util.*;

import jeans.util.*;
import jeans.util.cmdline.*;
import jeans.resource.*;

import clus.*;
import clus.main.*;
import clus.util.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.algo.*;
import clus.algo.tdidt.ClusNode;
import clus.selection.*;
import clus.error.multiscore.*;

public class OOTInd {

	protected Clus m_Clus;

	public OOTInd(Clus clus) {
		m_Clus = clus;
	}

	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
		schema.addIndices(ClusSchema.ROWS);
		int nb_num = schema.getNbNumericDescriptiveAttributes();
		if (Settings.XVAL_OVERLAP && nb_num > 0) return new OOTIndOV(schema, sett);
		else return new OOTIndNO(schema, sett);
	}

	public final void addFoldNrs(RowData set, XValMainSelection sel) {
		int nb = set.getNbRows();
		int nbf = sel.getNbFolds()+1;
		for (int i = 0; i < nb; i++) {
			int fold = sel.getFold(i)+1;
			DataTuple tuple = set.getTuple(i);
			tuple.m_Folds = new int[nbf];
			tuple.m_Folds[0] = 1;
			for (int j = 1; j < nbf; j++)
				if (j != fold) tuple.m_Folds[j] = 1;
		}
	}

	public final void addSetNrs(RowData set, MyArray sels, int nbsets) {
		int nb = set.getNbRows();
		for (int i = 0; i < nb; i++) {
			DataTuple tuple = set.getTuple(i);
			tuple.m_Folds = new int[nbsets];
			for (int j = 0; j < nbsets; j++) {
				BaggingSelection sel = (BaggingSelection)sels.elementAt(j);
				tuple.m_Folds[j] = sel.getCount(i);
			}
		}
	}

	public final void baggingRun(String appname, Date date) throws IOException, ClusException {
		Settings sett = m_Clus.getSettings();
		ClusSchema schema = m_Clus.getSchema();
		RowData set = m_Clus.getRowDataClone();
		// Create baggign selections
		int nbrows = set.getNbRows();
		int nbsets = sett.getBaggingSets();
		MyArray sels = new MyArray();
		for (int i = 0; i < nbsets; i++) {
			sels.addElement(new BaggingSelection(nbrows, m_Clus.getSettings().getEnsembleBagSize()));
		}
		// Add set numbers to our datset
		addSetNrs(set, sels, nbsets);
		// Initialize induce
		OOTInduce induce = (OOTInduce)m_Clus.getInduce();
		induce.initialize(nbsets);
		induce.SHOULD_OPTIMIZE = true;
		// Doinduce
		long time;
if (Debug.debug == 1) {
		time = ResourceInfo.getCPUTime();
}

		OptXValNode root = null;
		int nbr = 0;
		while (true) {
			root = induce.ootInduce(set);
			nbr++;
if (Debug.debug == 1) {
			if ((ResourceInfo.getCPUTime() - time) > 5000.0) break;
}

		}
		ClusSummary summary = m_Clus.getSummary();
if (Debug.debug == 1) {
if (Debug.debug == 1) {
		summary.setInductionTime((long)ClusStat.addToTotal(ResourceInfo.getCPUTime() - time, nbr));
}

}

if (Debug.debug == 1) {
		ClusStat.addTimes(nbr);
}

		// Output xval trees
		MultiScore score = m_Clus.getMultiScore();
		ClusOutput output = new ClusOutput(appname+".bag", schema, sett);
		output.writeHeader();
		if (Settings.SHOW_XVAL_FOREST) OptXVal.showForest(output.getWriter(), root);
		for (int i = 0; i < nbsets; i++) {
			BaggingSelection msel = (BaggingSelection)sels.elementAt(i);
			ClusRun cr = m_Clus.partitionData(msel, i+1);
			ClusNode tree = root.getTree(i);
			tree.postProc(score);
//			m_Clus.storeAndPruneModel(cr, tree);
//			m_Clus.calcError(cr, summary);
			if (sett.isOutputFoldModels()) output.writeOutput(cr, false);
		}
		output.writeSummary(summary);
		output.close();
	}

	public final void xvalRun(String appname, Date date) throws IOException, ClusException {
		Settings sett = m_Clus.getSettings();
		ClusSchema schema = m_Clus.getSchema();
		RowData set = m_Clus.getRowDataClone();
		XValMainSelection sel = schema.getXValSelection(set);
		addFoldNrs(set, sel);
		OOTInduce induce = (OOTInduce)m_Clus.getInduce();
		induce.initialize(sel.getNbFolds()+1);
		long time;
if (Debug.debug == 1) {
		time = ResourceInfo.getCPUTime();
}

		OptXValNode root = null;
		int nbr = 0;
		while (true) {
			root = induce.ootInduce(set);
			nbr++;
if (Debug.debug == 1) {
			if ((ResourceInfo.getCPUTime() - time) > 5000.0) break;
}

		}
		ClusSummary summary = m_Clus.getSummary();
if (Debug.debug == 1) {
if (Debug.debug == 1) {
		summary.setInductionTime((long)ClusStat.addToTotal(ResourceInfo.getCPUTime() - time, nbr));
}

}

if (Debug.debug == 1) {
		ClusStat.addTimes(nbr);
}

		// Output whole tree
		MultiScore score = m_Clus.getMultiScore();
		ClusOutput output = new ClusOutput(appname+".out", schema, sett);
		output.writeHeader();
		ClusNode tree = root.getTree(0);
		ClusRun cr = m_Clus.partitionData();
		tree.postProc(score);
//		m_Clus.storeAndPruneModel(cr, tree);
//		m_Clus.calcError(cr, null);
		output.writeOutput(cr, true);
		output.close();
		// Output xval trees
		output = new ClusOutput(appname+".xval", schema, sett);
		output.writeHeader();
		if (Settings.SHOW_XVAL_FOREST) OptXVal.showForest(output.getWriter(), root);
		for (int i = 0; i < sel.getNbFolds(); i++) {
			XValSelection msel = new XValSelection(sel, i);
			cr = m_Clus.partitionData(msel, i+1);
			tree = root.getTree(i+1);
			tree.postProc(score);
//			m_Clus.storeAndPruneModel(cr, tree);
//			m_Clus.calcError(cr, summary);
			if (sett.isOutputFoldModels()) output.writeOutput(cr, false);
		}
		output.writeSummary(summary);
		output.close();
	}
}

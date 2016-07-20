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

package addon.hmc.HMCConvertDAGData;

import java.util.ArrayList;
import java.util.Arrays;

import jeans.util.*;
import clus.*;
import clus.main.*;
import clus.statistic.ClusStatistic;
import clus.data.io.*;
import clus.data.rows.*;
import clus.ext.hierarchical.ClassHierarchy;
import clus.ext.hierarchical.ClassesAttrType;
import clus.ext.hierarchical.ClassesTuple;
import clus.ext.hierarchical.WHTDStatistic;

public class HMCConvertDAGData {

	public final static boolean CREATE_TRAIN_TUNE_TEST_SPLIT = true;

	public void convert(String input, String output, double minfreq) throws Exception {
		Clus clus = new Clus();
		String appname = FileUtil.getName(input)+".s";
		clus.initializeAddOn(appname);
		ClusStatManager mgr = clus.getStatManager();
		Settings sett = clus.getSettings();
		RowData data = (RowData)clus.getData();
		if (CREATE_TRAIN_TUNE_TEST_SPLIT) {
			ClusRun run = clus.partitionData();
			ClusStatistic[] stats = new ClusStatistic[1];
			stats[0] = mgr.createClusteringStat();
			data.calcTotalStats(stats);
			if (!sett.isNullTestFile()) {
				System.out.println("Loading: " + sett.getTestFile());
				if (minfreq != 0.0) {
					RowData test  = (RowData)run.getTestSet();
					test.calcTotalStats(stats);
				} else {
					clus.updateStatistic(sett.getTestFile(), stats);
				}
			}
			if (!sett.isNullPruneFile()) {
				System.out.println("Loading: " + sett.getPruneFile());
				if (minfreq != 0.0) {
					RowData tune  = (RowData)run.getPruneSet();
					tune.calcTotalStats(stats);
				} else {
					clus.updateStatistic(sett.getPruneFile(), stats);
				}
			}
			ClusStatistic.calcMeans(stats);
			WHTDStatistic stat = (WHTDStatistic)stats[0];
			stat.showRootInfo();
			ClassHierarchy hier = mgr.getHier();
			boolean[] removed = hier.removeInfrequentClasses(stat, minfreq);
			if (minfreq != 0.0) {
				ClassesAttrType type = hier.getType();
				removeLabelsFromData((RowData)run.getTrainingSet(), type, removed);
				if (!sett.isNullTestFile()) removeLabelsFromData((RowData)run.getTestSet(), type, removed);
				if (!sett.isNullPruneFile()) removeLabelsFromData((RowData)run.getPruneSet(), type, removed);
			}
			hier.initialize();
			if (minfreq != 0.0) {
				addIntermediateLabels((RowData)run.getTrainingSet(), hier);
				if (!sett.isNullTestFile()) addIntermediateLabels((RowData)run.getTestSet(), hier);
				if (!sett.isNullPruneFile()) addIntermediateLabels((RowData)run.getPruneSet(), hier);
			}
			hier.showSummary();
			RowData train = (RowData)run.getTrainingSet();
			ARFFFile.writeArff(output+".train.arff", train);
			if (!sett.isNullTestFile()) {
				RowData test  = (RowData)run.getTestSet();
				ARFFFile.writeArff(output+".test.arff", test);
			}
			if (!sett.isNullPruneFile()) {
				RowData tune  = (RowData)run.getPruneSet();
				ARFFFile.writeArff(output+".valid.arff", tune);
			}
		} else {
			ARFFFile.writeArff(output, data);
		}
	}

	public void removeLabelsFromData(RowData data, ClassesAttrType type, boolean[] removed) {
		for (int i = 0; i < data.getNbRows(); i++) {
			ClassesTuple tuple = (ClassesTuple)data.getTuple(i).getObjVal(type.getArrayIndex());
			tuple.removeLabels(removed);
		}
	}

	public void addIntermediateLabels(RowData data, ClassHierarchy hier) {
		ClassesAttrType type = hier.getType();
		ArrayList scratch = new ArrayList();
		boolean[] alllabels = new boolean[hier.getTotal()];
		ArrayList leftdata = new ArrayList();
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			scratch.clear();
			Arrays.fill(alllabels, false);
			ClassesTuple ct = (ClassesTuple)tuple.getObjVal(type.getArrayIndex());
			ct.addIntermediateElems(hier, alllabels, scratch);
			if (ct.getNbClasses() > 0) {
				leftdata.add(tuple);
			}
		}
		data.setFromList(leftdata);
	}

	public static void main(String[] args) {
		if (args.length != 2 && args.length != 4) {
			System.out.println("Usage: HMCConvertDAGData [-minfreq f] input.arff output.arff");
			System.exit(0);
		}
		double minfreq = 0;
		String input = args[0];
		String output = args[1];
		if (args[0].equals("-minfreq")) {
			minfreq = Double.parseDouble(args[1]) / 100.0;
			input = args[2];
			output = args[3];
		}
		HMCConvertDAGData cnv = new HMCConvertDAGData();
		try {
			cnv.convert(input, output, minfreq);
		} catch (Exception e) {
			System.err.println("Error: "+e);
			e.printStackTrace();
		}
	}
}


package addon.hmc.HMCConvertToSC;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import jeans.util.*;

import clus.*;
import clus.main.*;
import clus.util.ClusException;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.hierarchical.*;

public class HMCConvertToSC {

	public void convert(String input, String output, boolean binary, boolean split) throws Exception {
		Clus clus = new Clus();
		String appname = FileUtil.getName(input)+".s";
		clus.initializeAddOn(appname);
		ClusStatManager mgr = clus.getStatManager();
		Settings sett = clus.getSettings();
		ClassHierarchy hier = mgr.getHier();
		int sidx = hier.getType().getArrayIndex();
		String[] classterms = new String[hier.getTotal()];
		for (int i=0; i < hier.getTotal(); i++) {
			ClassTerm term = hier.getTermAt(i);
			classterms[i] = term.toStringHuman(hier);
		}
		boolean[][] classes;
		if (split) {
			ClusRun run = clus.partitionData();
			RowData train = (RowData)run.getTrainingSet();
			classes = new boolean[train.getNbRows()][hier.getTotal()];
			for (int i = 0; i < train.getNbRows(); i++) {
	   			DataTuple tuple = train.getTuple(i);
	   			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
	   			Arrays.fill(classes[i], false);
	   			tp.fillBoolArrayNodeAndAncestors(classes[i]);
	   		}
			writeArffToSC(output+".train.arff", train, classterms, classes, binary);
			if (!sett.isNullTestFile()) {
				RowData test  = (RowData)run.getTestSet();
				classes = new boolean[test.getNbRows()][hier.getTotal()];
				for (int i = 0; i < test.getNbRows(); i++) {
		   			DataTuple tuple = test.getTuple(i);
		   			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
		   			Arrays.fill(classes[i], false);
		   			tp.fillBoolArrayNodeAndAncestors(classes[i]);
		   		}
				writeArffToSC(output+".test.arff", test, classterms, classes, binary);
			}
			if (!sett.isNullPruneFile()) {
				RowData tune  = (RowData)run.getPruneSet();
				classes = new boolean[tune.getNbRows()][hier.getTotal()];
				for (int i = 0; i < tune.getNbRows(); i++) {
		   			DataTuple tuple = tune.getTuple(i);
		   			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
		   			Arrays.fill(classes[i], false);
		   			tp.fillBoolArrayNodeAndAncestors(classes[i]);
		   		}
				writeArffToSC(output+".valid.arff", tune, classterms, classes, binary);
			}
		} else {
			RowData data = (RowData)clus.getData();
			classes = new boolean[data.getNbRows()][hier.getTotal()];
			for (int i = 0; i < data.getNbRows(); i++) {
	   			DataTuple tuple = data.getTuple(i);
	   			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
	   			Arrays.fill(classes[i], false);
	   			tp.fillBoolArrayNodeAndAncestors(classes[i]);
	   		}
			writeArffToSC(output+".arff", data, classterms, classes, binary);
		}
	}

	public static void writeArffHeaderToSC(PrintWriter wrt, ClusSchema schema, String[] classterms, boolean binary) throws IOException, ClusException {
		wrt.println("@RELATION "+schema.getRelationName());
		wrt.println();
		for (int i = 0; i < schema.getNbAttributes(); i++) {
			ClusAttrType type = schema.getAttrType(i);
			if (!type.isDisabled() && !type.getName().equals("class")) {
					wrt.print("@ATTRIBUTE ");
					wrt.print(StringUtils.printStr(type.getName(), 65));
					if (type.isKey()) {
						wrt.print("key");
					} else {
						type.writeARFFType(wrt);
					}
					wrt.println();
			}
		}
		for (int i = 0; i < classterms.length; i++) {
			if (!classterms[i].equals("root"))	{
				wrt.print("@ATTRIBUTE ");
				wrt.print(classterms[i]);
				if (binary) {
					// don't change this type
					wrt.print("     numeric");
				} else {
					wrt.print("     hierarchical     p,n");
				}
				wrt.println();
			}
		}
		wrt.println();
	}

	public static void writeArffToSC(String fname, RowData data, String[] classterms, boolean[][] classes, boolean binary) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		writeArffHeaderToSC(wrt, schema, classterms, binary);
		wrt.println("@DATA");
		for (int j = 0; j < data.getNbRows(); j++) {
			DataTuple tuple = data.getTuple(j);
			int aidx = 0;
			for (int i = 0; i < schema.getNbAttributes(); i++) {
				ClusAttrType type = schema.getAttrType(i);
				if (!type.isDisabled() && !type.getName().equals("class")) {
					if (aidx != 0) wrt.print(",");
					wrt.print(type.getString(tuple));
					aidx++;
				}
			}
			for (int i = 0; i < classterms.length; i++) {
				if (!classterms[i].equals("root"))	{
					if (binary) {
						if (classes[j][i]) wrt.print(",1");
						else wrt.print(",0");
					} else {
						if (classes[j][i]) wrt.print(",p");
						else wrt.print(",n");
					}
				}
			}
			wrt.println();
		}
		wrt.close();
	}

	public static void main(String[] args) {
		int mainargs = 0;
		boolean binary = false;
		boolean split = false;
		boolean match = true;
		while (match && mainargs < args.length) {
			match = false;
			if (args[mainargs].equals("-binary")) {
				// generate data in 1/0 encoding instead of p/n encoding
				// IMPORTANT: the attribute type is also "numeric" and not "hierarchical"
				binary = true;
				match = true;
			}
			if (args[mainargs].equals("-split")) {
				split = true;
				match = true;
			}
			if (match) mainargs++;
		}
		if (args.length-mainargs != 2) {
			System.out.println("Usage: HMCConvertToSC input output");
			System.exit(0);
		}
		String input = args[mainargs];
		String output = args[mainargs+1];
		HMCConvertToSC cnv = new HMCConvertToSC();
		try {
			cnv.convert(input, output, binary, split);
		} catch (Exception e) {
			System.err.println("Error: "+e);
			e.printStackTrace();
		}
	}
}

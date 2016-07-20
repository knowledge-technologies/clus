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

package clus.data.io;

import jeans.util.*;
import java.io.*;
import java.util.*;

import clus.main.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.util.*;

// FIXME - use plugin system
import clus.ext.hierarchical.*;

/// The ARFF files include the data and description of variable types.
public class ARFFFile {

	protected final static String TAG_ERROR = " tag not found in ARFF file, found instead: '";
	protected final static String[] TAG_NAME = {"@RELATION", "@ATTRIBUTE", "@DATA"};

	protected ClusReader m_Reader;
	protected int m_DataLine = -1;

	public ARFFFile(ClusReader reader) {
		m_Reader = reader;
	}

	public ClusSchema read(Settings sett) throws IOException, ClusException {
		int expected = 0;
		ClusSchema schema = new ClusSchema(m_Reader.getName());
		schema.setSettings(sett);
		MStreamTokenizer tokens = m_Reader.getTokens();
		String token = tokens.getToken().toUpperCase();
		HashMap attrMap = new HashMap();
		while (expected < 3) {
			if (token == null) {
				throw new IOException("End of ARFF file before "+TAG_NAME[expected]+" tag");
			}
			if (token.equals(TAG_NAME[0])) {
				schema.setRelationName(tokens.readTillEol().trim());
				// System.out.println("Relation name: "+schema.getRelationName());
				expected = 1;
			} else if (token.equals(TAG_NAME[1])) {
				if (expected == 0) throw new IOException(TAG_NAME[expected]+TAG_ERROR+token+"'");
				String aname = tokens.getDelimToken('\"','\"');
				String atype = tokens.readTillEol();
				int idx = atype.indexOf('%');
				if (idx != -1) atype = atype.substring(0,idx-1);
				atype = atype.trim();
				addAttribute(schema, aname, atype, attrMap);
				expected = 2;
			} else if (token.equals(TAG_NAME[2])) {
				if (expected != 2) throw new IOException(TAG_NAME[expected]+TAG_ERROR+token+"'");
				m_DataLine = tokens.getLine();
				expected = 3;
			} else {
				throw new IOException(TAG_NAME[expected]+TAG_ERROR+token+"'");
			}
			if (expected < 3) token = tokens.getToken().toUpperCase();
		}
		// System.out.println("Number of attributes: "+schema.getNbTargetAttributes());
		return schema;
	}

	public void skipTillData() throws IOException {
		boolean error = false;
		MStreamTokenizer tokens = m_Reader.getTokens();
		String token = tokens.getToken().toUpperCase();
		while (token != null) {
			if (m_DataLine != -1 && tokens.getLine() > m_DataLine) {
				error = true;
				break;
			}
			if (token.equals(TAG_NAME[2])) {
				break;
			}
			token = tokens.getToken().toUpperCase();
		}
		if (token == null || error) {
			throw new IOException("Unexpected ARFF reader error looking for @data tag");
		}
	}

	protected void addAttribute(ClusSchema schema, String aname, String atype, HashMap attrMap) throws IOException, ClusException {
		Settings sett = schema.getSettings();
		String uptype = atype.toUpperCase();
		while (attrMap.containsKey(aname)) {
			int[] cnt = (int[])attrMap.get(aname);
			int idx = ++cnt[0];
			aname = aname + "-" + idx;
		}
		attrMap.put(aname, new int [1]);
		if (uptype.equals("NUMERIC") || uptype.equals("REAL") || uptype.equals("INTEGER")) {
			schema.addAttrType(new NumericAttrType(aname));
		} else if (uptype.equals("CLASSES")) {
			ClassesAttrType type = new ClassesAttrType(aname);
			schema.addAttrType(type);
			type.initSettings(schema.getSettings());
		} else if (uptype.startsWith("HIERARCHICAL")) {
			if(schema.getSettings().getHierSingleLabel()){
				ClassesAttrTypeSingleLabel type = new ClassesAttrTypeSingleLabel(aname, atype);
				schema.addAttrType(type);
				type.initSettings(schema.getSettings());
			}else{
				ClassesAttrType type = new ClassesAttrType(aname, atype);
				schema.addAttrType(type);
				type.initSettings(schema.getSettings());
			}
			
		} else if (uptype.equals("STRING")) {
			schema.addAttrType(new StringAttrType(aname));
		} else if (uptype.equals("KEY")) {
			StringAttrType key = new StringAttrType(aname);
			schema.addAttrType(key);
			key.setStatus(ClusAttrType.STATUS_KEY);
		} else if (uptype.equals("TIMESERIES")) {
			TimeSeriesAttrType tsat = new TimeSeriesAttrType(aname);
			schema.addAttrType(tsat);
		} else {
			if (uptype.equals("BINARY")) atype = "{1,0}";
			int tlen = atype.length();
			if (tlen > 2 && atype.charAt(0) == '{' && atype.charAt(tlen-1) == '}') {
				if (sett.getReduceMemoryNominalAttrs() == true) schema.addAttrType(new BitwiseNominalAttrType(aname, atype));
				else schema.addAttrType(new NominalAttrType(aname, atype));
			} else {
				throw new IOException("Attribute '"+aname+"' has unknown type '"+atype+"'");
			}
		}
	}

	public static void writeArffHeader(PrintWriter wrt, ClusSchema schema) throws IOException, ClusException {
		wrt.println("@RELATION '"+StringUtils.removeSingleQuote(schema.getRelationName())+"'");
		wrt.println();
		for (int i = 0; i < schema.getNbAttributes(); i++) {
			ClusAttrType type = schema.getAttrType(i);
			if (!type.isDisabled()) {
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
		if(!schema.getSettings().shouldWritePredictionsFromEnsemble()) wrt.println();
	}

	public static RowData readArff(String fname) throws IOException, ClusException {
		ClusReader reader = new ClusReader(fname, null);
		ARFFFile arff = new ARFFFile(reader);
		ClusSchema schema = arff.read(null);
		schema.initialize();
		ClusView view = schema.createNormalView();
		RowData data = view.readData(reader, schema);
		reader.close();
		return data;
	}

	public static void writeArff(String fname, RowData data) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		writeArffHeader(wrt, schema);
		wrt.println("@DATA");
		for (int j = 0; j < data.getNbRows(); j++) {
			DataTuple tuple = data.getTuple(j);
			tuple.writeTuple(wrt);
		}
		wrt.close();
	}

	// Exports data to CN2 format. Can be deleted ...
	public static void writeCN2Data(String fname, RowData data) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		wrt.println("**EXAMPLE FILE**\n");
		for (int j = 0; j < data.getNbRows(); j++) {
			DataTuple tuple = data.getTuple(j);
			int aidx = 0;
			for (int i = 0; i < schema.getNbAttributes(); i++) {
				ClusAttrType type = schema.getAttrType(i);
				if (!type.isDisabled()) {
					if (aidx != 0) wrt.print("\t");
					if (type instanceof NominalAttrType) {
            String label = type.getString(tuple);
            label = label.replace("^2","two");
            label = label.replace("<","le");
            label = label.replace(">","gt");
            label = label.replace("-","_");
            label = label.replace("&","");
            if (!label.equals("?")) {
            	wrt.print("_"+label);
            } else {
            	wrt.print(label);
            }
					} else {
						wrt.print(type.getString(tuple));
					}
					aidx++;
				}
			}
			wrt.println(";");
		}
		wrt.close();
	}

	// Exports data to FRS format. Can be deleted ...
	public static void writeFRSData(String fname, RowData data, boolean train) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		// Learning/testing examples
		for (int j = 0; j < data.getNbRows(); j++) {
			DataTuple tuple = data.getTuple(j);
			int aidx = 0;
			if (train) {
				wrt.print("lrn(lr(");
			} else {
			  wrt.print("tst(lr(");
			}
			for (int i = (schema.getNbAttributes()-1); i >= 0 ; i--) {
				ClusAttrType type = schema.getAttrType(i);
				if (!type.isDisabled()) {
					if (aidx != 0) wrt.print(",");
					if (type instanceof NominalAttrType) {
            String label = type.getString(tuple);
            label = label.replace("^2","two");
            label = label.replace("<","le");
            label = label.replace(">","gt");
            label = label.replace("-","");
            label = label.replace("_","");
            label = label.replace("&","");
            label = label.replace(".","");
           	wrt.print("a"+label);
					} else {
						wrt.print(type.getString(tuple));
					}
					aidx++;
				}
			}
			wrt.println(")).");
		}
		wrt.close();
	}

	// Exports data to FRS format. Can be deleted ...
	public static void writeFRSHead(String fname, RowData data, boolean train) throws IOException, ClusException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		// Head
		wrt.println(":-dynamictype/2,type/3,variable/2,target/1,lrn/1,tst/1.\n");
		// Types
		wrt.println("type(real,continuous,0.1).");
		for (int i = (schema.getNbAttributes()-1); i >= 0 ; i--) {
			ClusAttrType type = schema.getAttrType(i);
			if ((!type.isDisabled()) && (type instanceof NominalAttrType)) {
				String[] labels = ((NominalAttrType)type).getValues();
				wrt.print("type(type_");
				wrt.print(type.getName());
				wrt.print(",discrete,[");
				for (int j = 0; j < labels.length; j++) {
					String label = new String(labels[j]);
          label = label.replace("^2","two");
          label = label.replace("<","le");
          label = label.replace(">","gt");
          label = label.replace("-","");
          label = label.replace("_","");
          label = label.replace("&","");
          label = label.replace(".","");
         	wrt.print("a"+label);
					if (j < (labels.length-1)) {
						wrt.print(",");
					}
				}
				wrt.print("]).\n");
			}
		}
		wrt.println("");
		// Variables
		for (int i = (schema.getNbAttributes()-1); i >= 0 ; i--) {
			ClusAttrType type = schema.getAttrType(i);
			if (!type.isDisabled()) {
				if (type instanceof NominalAttrType) {
					wrt.print("variable('A");
					wrt.print(type.getName());
					wrt.print("',type_");
					wrt.print(type.getName());
					wrt.print(").\n");
				} else {
					wrt.print("variable('A");
					wrt.print(type.getName());
					wrt.print("',real).\n");
				}
			}
		}
		// Target
		wrt.print("\ntarget(lr(");
		for (int i = (schema.getNbAttributes()-1); i >= 0 ; i--) {
			ClusAttrType type = schema.getAttrType(i);
			if (!type.isDisabled()) {
				wrt.print("'A");
				wrt.print(type.getName());
				wrt.print("'");
			}
  		if (i > 0) {
  			wrt.print(",");
  		}
		}
		wrt.print(")).\n\n");
		wrt.close();
	}

	// Exports data to R format. Can be deleted ...
	public static void writeRData(String fname, RowData data) throws IOException, ClusException {
		double NUMBER_INF = 9E36;
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		ClusSchema schema = data.getSchema();
		int nbAttr = schema.getNbAttributes();
//		// First print the header - names for attributes
//		for (int iColumn = 0; iColumn < nbAttr; iColumn++)
//		{
//			wrt.print(schema.getAttrType(iColumn).getName() + "\t");
//		}
//		wrt.print("\n"); // line feed
		// Learning/testing examples
		for (int jRow = 0; jRow < data.getNbRows(); jRow++) {
			DataTuple tuple = data.getTuple(jRow);
			for (int iAttr = 0; iAttr < nbAttr ; iAttr++) {
				ClusAttrType attrType = schema.getAttrType(iAttr);

				if (attrType instanceof NumericAttrType) {
					if (attrType.isMissing(tuple)){
						if (!Double.isNaN(attrType.getNumeric(tuple)) && !Double.isInfinite(attrType.getNumeric(tuple))) {// Value not given
							System.err.println("ERROR, isMissing works wrong");
							System.exit(0);
						}
						wrt.print(NUMBER_INF);
					} else // ok number
						wrt.print(attrType.getNumeric(tuple));

				} else { // Assuming nominaltype
					if (attrType.isMissing(tuple)) {
						wrt.print(NUMBER_INF);
					} else {
						wrt.print(attrType.getNominal(tuple));
					}
				}
				wrt.print("\t");
			}
			wrt.print("\n"); // line feed
		}
		wrt.close();
	}

	public static void writeRDataNominalLabels(String fname, RowData data) throws IOException, ClusException {

		ClusSchema schema = data.getSchema();
		int nbAttr = schema.getNbAttributes();

		// We first check if there are any nominal attrs. If not,
		// we do not create the file. Otherwise problems with R
		ArrayList<Integer> nominalAttrs = new ArrayList<Integer>();

		// Print the indexes of nominal attributes
		for (int iColumn = 0; iColumn < nbAttr; iColumn++)
		{
			ClusAttrType attrType = schema.getAttrType(iColumn);
			if (attrType instanceof NominalAttrType) {
				nominalAttrs.add(iColumn+1);
			}
		}

		if (nominalAttrs.size() > 0) {
			// Do not create file if not nominalattrs
			PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
			for (int iColumn = 0; iColumn < nominalAttrs.size(); iColumn++){
				wrt.print(nominalAttrs.get(iColumn) + "\t");
			}
			wrt.print("\n");
			wrt.close();
		}
	}


}

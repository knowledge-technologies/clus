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

package clus.ext.ilevelc;

import java.io.*;
import java.util.*;

import jeans.util.*;
import clus.data.io.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.util.ClusException;

public class MPCKMeansWrapper {

	protected ClusStatManager m_Manager;

	public MPCKMeansWrapper(ClusStatManager statManager) {
		m_Manager = statManager;
	}

	public ClusStatManager getStatManager() {
		return m_Manager;
	}

	public static void writeStream(InputStream in) throws IOException {
		int ch = -1;
		StringBuffer sb = new StringBuffer();
		while ((ch = in.read()) != -1) {
			sb.append((char) ch);
		}
		System.out.println(sb.toString());
	}

	public double computeRandIndex(RowData data, int[] assign, String tpe) {
		int a = 0;
		int b = 0;
		int nbex = data.getNbRows();
		ClusSchema schema = data.getSchema();
		NominalAttrType classtype = (NominalAttrType)schema.getAttrType(schema.getNbAttributes()-1);
		for (int i = 0; i < nbex; i++) {
			DataTuple ti = data.getTuple(i);
			int cia = classtype.getNominal(ti);
			int cib = assign[ti.getIndex()];
			for (int j = i+1; j < nbex; j++) {
				DataTuple tj = data.getTuple(j);
				int cja = classtype.getNominal(tj);
				int cjb = assign[tj.getIndex()];
				if (cia == cja && cib == cjb) a++;
				if (cia != cja && cib != cjb) b++;
			}
		}
		double rand = 1.0 * (a+b) / (nbex*(nbex-1)/2);
		System.out.println(tpe+"Rand = "+rand+" (nbex = "+nbex+")");
		return rand;
	}

	public ClusModel induce(RowData data, RowData test, ArrayList constraints, int cls) throws IOException, ClusException {
		String main = getStatManager().getSettings().getAppName();
		String datf = main+"-temp-MPCKMeans.arff";
		String cons = main+"-temp-MPCKMeans.cons";
		String outf = main+"-temp-MPCKMeans.assign";
		System.out.println("Calling MPCKMeans: "+main);
		// Make sure files don't exist
		FileUtil.delete(datf);
		FileUtil.delete(cons);
		FileUtil.delete(outf);
		// Write input files
		ARFFFile.writeArff(datf, data);
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cons)));
		for (int i = 0; i < constraints.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)constraints.get(i);
			int type = ic.getType();
			int t1 = ic.getT1().getIndex();
			int t2 = ic.getT2().getIndex();
			if (t1 >= t2) {
				int temp = t1;
				t1 = t2;
				t2 = temp;
			}
			int mtype = (type == ILevelConstraint.ILevelCMustLink) ? 1 : -1;
			if (t1 != t2) {
				wrt.println(t1+"\t"+t2+"\t"+mtype);
			}
		}
		wrt.close();
		String script = System.getenv("MPCKMEANS_SCRIPT");
		System.out.println("Running script: "+script);
		if (script == null) return new SimpleClusterModel(null, getStatManager());
		try {
			String line = "";
			int[] assign = new int[data.getNbRows()];
			Arrays.fill(assign, -1);
			String cmdline = "-D "+datf+" -C "+cons+" -O "+outf;
			Process proc = Runtime.getRuntime().exec(script+" "+cmdline);
			proc.waitFor();
			writeStream(proc.getInputStream());
			writeStream(proc.getErrorStream());
			LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(outf)));
			while ((line = rdr.readLine()) != null) {
				line = line.trim();
				if (!line.equals("")) {
					String[] arr = line.split("\t");
					if (arr.length != 2) {
						throw new ClusException("MPCKMeans error in output");
					}
					int idx = Integer.parseInt(arr[0]);
					int cl = Integer.parseInt(arr[1]);
					assign[idx] = cl;
				}
			}
			rdr.close();
			System.out.println("--------the file"+cons+"is not deleted !!!");
			// Make sure files don't exist
			//FileUtil.delete(datf);
			//FileUtil.delete(cons);
			//FileUtil.delete(outf);
			computeRandIndex(data, assign, "All data: ");
			if (test != null) computeRandIndex(test, assign, "Test data: ");
			return new SimpleClusterModel(assign, getStatManager());
		} catch (InterruptedException e) {
		}
		return new SimpleClusterModel(null, getStatManager());
	}


}

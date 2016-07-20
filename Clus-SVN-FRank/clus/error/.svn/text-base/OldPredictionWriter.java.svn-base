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

package clus.error;

import jeans.util.*;

import java.io.*;
import java.util.*;

import clus.data.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.*;

public class OldPredictionWriter {

	protected PrintWriter m_Writer;
	protected ClusSchema m_Schema;
	protected MyArray m_Attrs;
	protected boolean m_Enabled, m_MainEnabled;

	static OldPredictionWriter instance;

	public static void makeInstance(String fname, ClusSchema schema) {
		try {
			OldPredictionWriter inst = getInstance();
			inst.init(fname, schema);
		} catch (IOException e) {
			System.out.println(">>> IO ERROR <<<<<");
		}
	}

	public static OldPredictionWriter getInstance() {
		if (instance == null) instance = new OldPredictionWriter();
		return instance;
	}

	public void setEnabled(boolean ena) {
		m_Enabled = ena;
	}

	public void setMainEnabled(boolean ena) {
		m_MainEnabled = ena;
	}

	public void init(String fname, ClusSchema schema) throws FileNotFoundException {
		m_Schema = schema;
		m_Writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		m_Attrs = new MyArray();
		int nb = schema.getNbAttributes();
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_KEY) m_Attrs.addElement(at);
		}
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_TARGET) m_Attrs.addElement(at);
		}
	}

	public void write(String strg) {
		m_Writer.println(strg);
	}

	public void addExample(ClusData data, int idx, ClusStatistic pred) {
	}

	public int writeAttributes(DataTuple tuple) {
		int nb = m_Attrs.size();
		for (int i = 0; i < nb; i++) {
			if (i != 0) m_Writer.print(",");
			ClusAttrType at = (ClusAttrType)m_Attrs.elementAt(i);
			m_Writer.print(at.getString(tuple));
		}
		return nb;
	}

	public void addExample(DataTuple tuple, String pred) {
		if (m_Enabled && m_MainEnabled) {
			int nb = writeAttributes(tuple);
			if (nb > 0) m_Writer.print(",");
			m_Writer.println(pred);
		}
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		if (m_Enabled && m_MainEnabled) {
			writeAttributes(tuple);
			StringTokenizer tokens = new StringTokenizer(pred.getString(), "[],");
			while (tokens.hasMoreTokens()) {
				m_Writer.print(",");
				m_Writer.print(tokens.nextToken());
			}
			m_Writer.println();
		}
	}

	public void close() {
		m_Writer.close();
	}
}

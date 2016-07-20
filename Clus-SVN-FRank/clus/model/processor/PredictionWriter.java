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

package clus.model.processor;

import jeans.util.*;

import java.io.*;
import java.util.*;

import clus.main.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.util.*;
import clus.data.io.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.*;

public class PredictionWriter extends ClusModelProcessor {

	protected String m_Fname;
	protected PrintWriter m_Writer;
	protected MyArray m_Attrs;
	protected boolean m_Global;
	protected Settings m_Sett;
	protected StringBuffer m_ModelParts;
	protected ClusSchema m_OutSchema;
	protected ClusStatistic m_Target;
	protected boolean m_Initialized;
	protected String m_ToPrint;
	protected ArrayList m_ModelNames = new ArrayList();
	protected HashSet m_ModelNamesMap = new HashSet();

	public PredictionWriter(String fname, Settings sett, ClusStatistic target) {
		m_Fname = fname;
		m_Sett = sett;
		m_Target = target;
		m_ModelParts = new StringBuffer();
	}

	public boolean shouldProcessModel(ClusModelInfo info) {
		return info.shouldWritePredictions() && !info.getName().equals("Default");
	}

	public void addModelInfo(ClusModelInfo info) {
		if (!m_ModelNamesMap.contains(info.getName())) {
			m_ModelNamesMap.add(info.getName());
			m_ModelNames.add(info.getName());
		}
	}

	public void addTargetAttributesForEachModel() {
		for (int i = 0; i < m_ModelNames.size(); i++) {
			String mn = (String)m_ModelNames.get(i);
			m_Target.addPredictWriterSchema(mn, m_OutSchema);
			m_OutSchema.addAttrType(new StringAttrType(mn+"-models"));
		}
	}

	public void println(String line) {
		if (m_Initialized) m_Writer.println(line);
		else m_ToPrint = line;
	}

	public void initializeAll(ClusSchema schema) throws IOException, ClusException {
		if (m_Initialized) return;
		if (!m_Global) doInitialize(schema);
		addTargetAttributesForEachModel();
		System.out.println("PredictionWriter is writing the ARFF header");
		ARFFFile.writeArffHeader(m_Writer, m_OutSchema);
		m_Writer.println("@DATA");
		if (m_ToPrint != null) {
			m_Writer.println(m_ToPrint);
			m_ToPrint = null;
		}
		m_Initialized = true;
	}

	public void terminateAll() throws IOException {
		if (!m_Global) close();
	}

	public void globalInitialize(ClusSchema schema) throws IOException, ClusException {
		m_Global = true;
		doInitialize(schema);
	}

	public PrintWriter getWrt() {
		return m_Writer;
	}

	public void close() throws IOException {
		m_Writer.close();
	}

	public boolean needsModelUpdate() {
		return true;
	}

	public void modelUpdate(DataTuple tuple, ClusModel model) throws IOException {
		if (m_ModelParts.length() != 0) m_ModelParts.append("+");
		m_ModelParts.append(String.valueOf(model.getID()));
	}

	public void exampleUpdate(DataTuple tuple) {
		for (int j = 0; j < m_Attrs.size(); j++) {
			if (j != 0) m_Writer.print(",");
			ClusAttrType at = (ClusAttrType)m_Attrs.elementAt(j);
			m_Writer.print(at.getPredictionWriterString(tuple));
		}
	}

	public void exampleDone() {
		m_Writer.println();
		m_ModelParts.setLength(0);
	}

	public void exampleUpdate(DataTuple tuple, ClusStatistic distr) {
		m_Writer.print(",");
		if (distr == null) {
			m_Writer.print("???");
		} else {
			// TODO: do this as a function of predictTuple
			m_Writer.print(distr.getPredictWriterString(tuple));
		}
		m_Writer.print(",\""+m_ModelParts+"\"");
		m_ModelParts.setLength(0);
	}

	private void doInitialize(ClusSchema schema) throws IOException, ClusException {
		m_Attrs = new MyArray();
		int nb = schema.getNbAttributes();
		m_OutSchema = new ClusSchema(StringUtils.removeSingleQuote(schema.getRelationName())+"-predictions");
		m_OutSchema.setSettings(schema.getSettings());
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_KEY) {
				m_Attrs.addElement(at);
				m_OutSchema.addAttrType(at.cloneType());
			}
		}
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_TARGET) {
				m_Attrs.addElement(at);
				at.updatePredictWriterSchema(m_OutSchema);
			}
		}
		m_Writer = m_Sett.getFileAbsoluteWriter(m_Fname);
	}
}

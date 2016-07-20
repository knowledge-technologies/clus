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

import clus.main.*;
import clus.model.ClusModel;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.*;
import clus.util.*;

import jeans.util.*;

import java.io.*;

public class ModelProcessorCollection extends MyArray {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final void addModelProcessor(ClusModelProcessor proc) {
		addElement(proc);
	}

	public final boolean addCheckModelProcessor(ClusModelProcessor proc) {
		// only add model processor if not yet in list
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc2 = (ClusModelProcessor)elementAt(j);
			if (proc == proc2) return false;
		}
		addElement(proc);
		return true;
	}

	public final void initialize(ClusModel model, ClusSchema schema) throws IOException, ClusException {
		if (model != null) {
			for (int i = 0; i < size(); i++) {
				ClusModelProcessor proc = (ClusModelProcessor)elementAt(i);
				proc.initialize(model, schema);
			}
		}
	}

	public final void initializeAll(ClusSchema schema) throws IOException, ClusException {
		for (int i = 0; i < size(); i++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(i);
			proc.initializeAll(schema);
		}
	}

	public final void terminate(ClusModel model) throws IOException {
		if (model != null) {
			for (int i = 0; i < size(); i++) {
				ClusModelProcessor proc = (ClusModelProcessor)elementAt(i);
				proc.terminate(model);
			}
		}
	}

	public final void terminateAll() throws IOException {
		for (int i = 0; i < size(); i++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(i);
			proc.terminateAll();
		}
	}

	public final void modelDone() throws IOException {
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(j);
			proc.modelDone();
		}
	}

	public final void exampleUpdate(DataTuple tuple) throws IOException {
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(j);
			proc.exampleUpdate(tuple);
		}
	}

	public final void exampleDone() throws IOException {
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(j);
			proc.exampleDone();
		}
	}

	public final void exampleUpdate(DataTuple tuple, ClusStatistic distr) throws IOException {
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(j);
			proc.exampleUpdate(tuple, distr);
		}
	}

	public final boolean needsModelUpdate() throws IOException {
		for (int j = 0; j < size(); j++) {
			ClusModelProcessor proc = (ClusModelProcessor)elementAt(j);
			if (proc.needsModelUpdate()) return true;
		}
		return false;
	}

	public final ClusModelProcessor getModelProcessor(int i) {
		return (ClusModelProcessor)elementAt(i);
	}
}

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

import clus.data.rows.*;
import clus.data.type.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.statistic.*;
import clus.util.*;

import java.io.*;

public abstract class ClusModelProcessor {

	public boolean shouldProcessModel(ClusModelInfo info) {
		return true;
	}

	public void addModelInfo(ClusModelInfo info) {
	}

	public void initialize(ClusModel model, ClusSchema schema) throws IOException, ClusException {
	}

	public void initializeAll(ClusSchema schema) throws IOException, ClusException {
	}

	public void terminate(ClusModel model) throws IOException {
	}

	public void terminateAll() throws IOException {
	}

	public void exampleUpdate(DataTuple tuple) throws IOException {
	}

	public void exampleDone() throws IOException {
	}

	public void exampleUpdate(DataTuple tuple, ClusStatistic distr) throws IOException {
	}

	public void modelUpdate(DataTuple tuple, ClusModel model) throws IOException {
	}

	public void modelDone() throws IOException {
	}

	public boolean needsModelUpdate() {
		return false;
	}

	public boolean needsInternalNodes() {
		return false;
	}
}

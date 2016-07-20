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

package clus.model;

import jeans.util.*;

import clus.main.ClusRun;
import clus.statistic.*;
import clus.data.rows.*;
import clus.util.*;

import java.io.*;
import java.util.*;

public interface ClusModel {

	// MODEL TYPES, used at least for ClusModelInfoList functions
	/** The type of model returned. Default = the model that always predicts the mean of data set. */
	public static int DEFAULT = 0;
	/** The type of model returned. Original = the real model but without pruning.
	 * Often stored for comparison*/
	public static int ORIGINAL = 1;
	/** The type of model returned. PRUNED = the real model after pruning. */
	public static int PRUNED = 2;
	public static int RULES = 2;

	// SOMETHING ELSE
	public static int PRUNE_INVALID = 0;

    public static int TRAIN = 0;
    public static int TEST = 1;

	public ClusStatistic predictWeighted(DataTuple tuple);

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException;

	public int getModelSize();

	public String getModelInfo();

	public void printModel(PrintWriter wrt);

	public void printModel(PrintWriter wrt, StatisticPrintInfo info);

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples);

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive);

	public void printModelToPythonScript(PrintWriter wrt);

	public void attachModel(HashMap table) throws ClusException;

	public void retrieveStatistics(ArrayList list);

	public ClusModel prune(int prunetype);

	public int getID();
}

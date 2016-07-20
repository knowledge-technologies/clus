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

package clus.algo;

import java.io.IOException;

import jeans.resource.ResourceInfo;
import jeans.util.cmdline.*;
import clus.*;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.*;
import clus.data.type.*;

/**
 * For each type of algorithm there should be a ClusClassifier object.
 *
 */
public abstract class ClusInductionAlgorithmType {

	public final static int REGULAR_TREE = 0;

	// For each type of algorithm there should be a ClusClassifier object

	protected Clus m_Clus;

	public ClusInductionAlgorithmType(Clus clus) {
		m_Clus = clus;
	}

	public Clus getClus() {
		return m_Clus;
	}

	public ClusInductionAlgorithm getInduce() {
		return getClus().getInduce();
	}

	public ClusStatManager getStatManager() {
		return getInduce().getStatManager();
	}

	public Settings getSettings() {
		return getClus().getSettings();
	}

	public abstract ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException;

	public void printInfo() {
		System.out.println("Classifier: "+getClass().getName());
	}

	public abstract void pruneAll(ClusRun cr) throws ClusException, IOException;

	public abstract ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException;

	public void postProcess(ClusRun cr) throws ClusException, IOException {
		// should be implemented by subclass
	}

	/**
	 * Calls the induce function for each of the learning algorithms of this TYPE.
	 * Also collects the information about computational cost of training.
	 * @param cr
	 * @throws ClusException
	 * @throws IOException
	 */
	public void induceAll(ClusRun cr) throws ClusException, IOException {
		long start_time = ResourceInfo.getTime();
		getInduce().induceAll(cr); // Train the algorithms of this type.
		long done_time = ResourceInfo.getTime();
		cr.setInductionTime(done_time-start_time);
		pruneAll(cr);
		cr.setPruneTime(ResourceInfo.getTime()-done_time);
		postProcess(cr);
		if (Settings.VERBOSE > 0) {
			  String cpu = ResourceInfo.isLibLoaded() ? " (CPU)" : "";
			  System.out.println("Induction Time: "+(double)cr.getInductionTime()/1000+" sec"+cpu);
			  System.out.println("Pruning Time: "+(double)cr.getPruneTime()/1000+" sec"+cpu);
		}
	}

	public ClusModel induceSingle(ClusRun cr) throws ClusException, IOException {
		ClusModel unpruned = induceSingleUnpruned(cr);
		return pruneSingle(unpruned, cr);
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		return getInduce().induceSingleUnpruned(cr);
	}

	public void saveInformation(String fname) {
	}
}

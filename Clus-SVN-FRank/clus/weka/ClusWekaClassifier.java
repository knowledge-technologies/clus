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

/*
 * Created on May 3, 2005
 */
package clus.weka;

import java.io.IOException;

import jeans.util.cmdline.*;

import clus.*;
import clus.algo.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.util.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.statistic.*;

import weka.classifiers.*;
import weka.core.*;

public class ClusWekaClassifier extends ClusInductionAlgorithmType {

	protected String m_Options;
	protected Classifier m_Classifier;
	protected ClusToWekaData m_Data;
	protected ClusStatManager m_Manager;

	public ClusWekaClassifier(Clus clus, String opts) throws ClusException {
		super(clus);
		m_Options = opts;
		String[] split = opts.split("\\s+");
		String[] options = new String[split.length-1];
		System.arraycopy(split, 1, options, 0, options.length);
		try {
			System.out.println("Loading classifier: "+split[0]);
			m_Classifier = Classifier.forName(split[0], options);
		} catch (Exception e) {
			throw new ClusException("Weka Error: "+e.getClass().getName()+": "+e.getMessage());
		}
	}

	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
		return new ClusWekaInduce(schema, sett);
	}

	public void printInfo() {
		System.out.println("Weka Classifier: "+m_Options);
	}

	public void initializeInduce(ClusInductionAlgorithm induce, CMDLineArgs cargs) {
		m_Data = new ClusToWekaData(induce.getSchema());
		m_Manager = induce.getStatManager();
	}

	public ClusStatistic createStatistic() {
		return m_Manager.createClusteringStat();
	}

	public Instances getDummyData() {
		return m_Data.getDummyData();
	}

	public ClusModel induceSingle(ClusRun cr) throws ClusException {
		ClusWekaModel result = new ClusWekaModel();
		RowData data = (RowData)cr.getTrainingSet();
		try {
			Classifier copy = Classifier.makeCopy(m_Classifier);
			copy.buildClassifier(m_Data.convertData(data));
			result.setClassifier(copy);
			result.setParent(this);
			return result;
		} catch (Exception e) {
			throw new ClusException("Weka Error: "+e.getClass().getName()+": "+e.getMessage());
		}
	}

	public void pruneAll(ClusRun cr) throws ClusException, IOException {
	}

	public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
		return model;
	}

	public class ClusWekaInduce extends ClusInductionAlgorithm {

		public ClusWekaInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
			super(schema, sett);
		}

		public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
			return induceSingle(cr);
		}
	}
}

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

import java.io.*;
import java.util.*;

import jeans.io.*;
import jeans.util.*;
import clus.data.rows.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.statistic.*;

import weka.classifiers.*;
import weka.core.*;

public class ClusWekaModel implements ClusModel {

	Classifier m_Classifier;
	ClusWekaClassifier m_Parent;

	public ClusStatistic predictWeighted(DataTuple tuple) {
		Instance weka_tuple = m_Parent.m_Data.convertInstance(tuple);
	    Instance classMissing = (Instance)weka_tuple.copy();
	    classMissing.setDataset(m_Parent.getDummyData());
	    classMissing.setClassMissing();
	    try {
	    	double[] dist = m_Classifier.distributionForInstance(classMissing);
	    	ClassificationStat stat = (ClassificationStat)m_Parent.createStatistic();
	    	stat.initSingleTargetFrom(dist);
	    	stat.calcMean();
	    	return stat;
	    } catch (Exception e) {
	    	System.out.println("Weka Error: "+e.getClass().getName()+": "+e.getMessage());
	    }
		return null;
	}

	public void setParent(ClusWekaClassifier parent) {
		m_Parent = parent;
	}

	public void setClassifier(Classifier classifier) {
		m_Classifier = classifier;
	}

	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
	}

	public int getModelSize() {
		return 0;
	}

	public String getModelInfo() {
		return "Weka Model";
	}

	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
	}

	public void printModel(PrintWriter wrt) {
	}

	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
	}

	public void printModelToPythonScript(PrintWriter wrt) {
	}

	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean ex) {
	}
	public void saveModel(ObjectSaveStream strm) throws IOException {
	}

	public void attachModel(HashMap table) {
		System.err.println(getClass().getName()+"attachModel() not implemented");
	}

	public int getID() {
  		return 0;
  	}

  	public ClusModel prune(int prunetype) {
  		return this;
	}

    public void retrieveStatistics(ArrayList list) {
    }
}

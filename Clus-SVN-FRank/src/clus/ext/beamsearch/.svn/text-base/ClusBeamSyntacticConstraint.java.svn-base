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

package clus.ext.beamsearch;

import java.io.IOException;
import java.util.ArrayList;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.modelio.ClusTreeReader;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusBeamSyntacticConstraint {

	ClusNode m_Constraint;
	ArrayList<ClusStatistic> m_ConstraintPredictions;

	public ClusBeamSyntacticConstraint(ClusRun run) throws ClusException, IOException{
			initializeConstraint(run);
			ClusStatManager mgr = run.getStatManager();
			(mgr.getSchema()).attachModel((ClusModel)m_Constraint);
			createConstrStat(m_Constraint, mgr,(RowData)run.getTrainingSet());
			setConstraintPredictions(getPredictions(run));
//			m_Constraint.printTree();
	}


	public void initializeConstraint(ClusRun run) throws IOException{
		ClusStatManager csm = run.getStatManager();
		ClusTreeReader rdr = new ClusTreeReader();
		String bconstrFile = csm.getSettings().getBeamConstraintFile();
		m_Constraint = rdr.loadTree(bconstrFile, csm.getSchema());
		m_Constraint.setClusteringStat(csm.createClusteringStat());
		m_Constraint.setTargetStat(csm.createTargetStat());
	}

	public void createConstrStat(ClusNode node, ClusStatManager mgr, RowData data){
		if (node.getTest() == null) node.makeLeaf();
			else{
				for (int j = 0; j < node.getNbChildren(); j++){
					ClusNode child = (ClusNode)node.getChild(j);
					RowData subset = data.applyWeighted(node.getTest(), j);
					child.initClusteringStat(mgr, subset);
					child.initTargetStat(mgr, subset);
					child.getTargetStat().calcMean();
					createConstrStat(child, mgr, subset);
				}
			}
	}

	/**Dragi
	 * @param run
	 * @return predictions
	 */
	public ArrayList<ClusStatistic> getPredictions(ClusRun run){
		DataTuple tuple;
		RowData train = (RowData)run.getTrainingSet();
		ArrayList<ClusStatistic> predictions = new ArrayList<ClusStatistic>();
			for (int i = 0; i < (train.getNbRows()); i++){
				tuple = train.getTuple(i);
				predictions.add(m_Constraint.predictWeighted(tuple));		
			}
		return predictions;
	}

	public ArrayList<ClusStatistic> getConstraintPredictions(){
		return m_ConstraintPredictions;
	}

	public void setConstraintPredictions(ArrayList<ClusStatistic> predictions){
		m_ConstraintPredictions = predictions;
	}

}

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
 * Created on Apr 5, 2005
 */
package clus.ext.beamsearch;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.main.*;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;

import java.io.Serializable;
import java.util.ArrayList;

public class ClusBeamModel implements Comparable, Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected transient int m_HashCode = -1;
	protected int m_ParentIndex;
	protected boolean m_Refined, m_Finished;
	protected double m_Value;
	protected ClusModel m_Root;
	protected Object m_Refinement;

	protected double m_DistanceToBeam; //stores the Similarity to beam
	protected ArrayList<ClusStatistic> m_Predictions; //stores the predictions for each target attribute for each row

	public ClusBeamModel() {
	}

	public ClusBeamModel(double value, ClusModel root) {
		m_Value = value;
		m_Root = root;
	}

	public void setValue(double value) {
		m_Value = value;
	}

	public double getValue() {
		return m_Value;
	}

	public ClusModel getModel() {
		return m_Root;
	}

	public void setModel(ClusNode root) {
		m_Root = root;
	}

	public Object getRefinement() {
		return m_Refinement;
	}

	public void setRefinement(Object refinement) {
		m_Refinement = refinement;
	}

	public String toString() {
		return "" + m_Value;
	}

	public final boolean isRefined() {
		return m_Refined;
	}

	public final void setRefined(boolean ref) {
		m_Refined = ref;
	}

	public final boolean isFinished() {
		return m_Finished;
	}

	public final void setFinished(boolean finish) {
		m_Finished = finish;
	}

	public void setParentModelIndex(int parent) {
		m_ParentIndex = parent;
	}

	public int getParentModelIndex() {
		return m_ParentIndex;
	}

	public int compareTo(Object e2) {
		ClusBeamModel m2 = (ClusBeamModel)e2;
		if (m2.m_Value != m_Value) {
			return m2.m_Value < m_Value ? -1 : 1;
		} else {
			return 0;
		}
	}

	public int hashCode() {
		if (m_HashCode == -1) {
			m_HashCode = m_Root.hashCode();
			if (m_HashCode == -1) m_HashCode = 0;
		}
		return m_HashCode;
	}

	public boolean equals(Object other) {
		ClusBeamModel o = (ClusBeamModel)other;
		if (hashCode() != o.hashCode()) {
			return false;
		} else {
			return m_Root.equals(o.m_Root);
		}
	}

	public ClusBeamModel cloneNoModel() {
		ClusBeamModel res = new ClusBeamModel();
		res.m_ParentIndex = m_ParentIndex;
		return res;
	}

	public ClusBeamModel cloneModel() {
		ClusBeamModel res = new ClusBeamModel();
		res.m_ParentIndex = m_ParentIndex;
		res.m_Root = m_Root;
		return res;
	}

	public void setDistanceToBeam(double distance){
		m_DistanceToBeam = distance;
	}

	public double getDistanceToBeam(){
		return m_DistanceToBeam;
	}

	public void setModelPredictions(ArrayList<ClusStatistic> predictions){
		m_Predictions = predictions;
	}

	public ArrayList<ClusStatistic> getModelPredictions(){
		return m_Predictions;
	}
	
	public ClusStatistic getPredictionForTuple(DataTuple tuple){
		return m_Predictions.get(tuple.getIndex());
	}
}

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


import java.util.ArrayList;

//import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.*;
//import clus.data.type.ClusAttrType;
//import clus.data.type.NumericAttrType;
import clus.main.*;
import clus.model.ClusModel;
import clus.statistic.*;


public class ClusBeamModelDistance{

	RowData m_Data;
	int m_NbRows; //number of rows of the dataset
	boolean isBeamUpdated = false;

	public ClusBeamModelDistance(ClusRun run, ClusBeam beam){
		m_Data = (RowData)run.getTrainingSet();
		if (m_Data == null){
			System.err.println(getClass().getName()+": ClusBeamTreeDistance(): Error while reading the train data");
			System.exit(1);
		}
		m_NbRows = m_Data.getNbRows();
		fillBeamWithPredictions(beam);
	}

	public void fillBeamWithPredictions(ClusBeam beam){
		ArrayList arr = beam.toArray();
		ClusBeamModel model;
		for (int k = 0; k < arr.size(); k++){
			model = (ClusBeamModel)arr.get(k);
			model.setModelPredictions(getPredictions(model.getModel()));
		}
	}

	public ArrayList<ClusStatistic> getPredictions(ClusModel model){
		ArrayList<ClusStatistic> predictions = new ArrayList<ClusStatistic>();
		for (int i = 0; i < m_NbRows; i++){
			DataTuple tuple = m_Data.getTuple(i);
			ClusStatistic stat = model.predictWeighted(tuple);
			predictions.add(tuple.getIndex(), stat);
//			predictions.add(stat);
		}
		return predictions;
	}
	
	public static ArrayList<ClusStatistic> getPredictionsDataSet(ClusModel model, RowData train, boolean isNum){
		ArrayList<ClusStatistic> predictions = new ArrayList<ClusStatistic>();
		for (int i = 0; i < train.getNbRows(); i++){
			DataTuple tuple = train.getTuple(i);
			ClusStatistic stat = model.predictWeighted(tuple);
			predictions.add(stat);
		}
		return predictions;
	}

	public static double getDistance(ArrayList<ClusStatistic> a, ArrayList<ClusStatistic> b){
		double result = 0.0;
		for (int i = 0; i < a.size(); i++){
			ClusStatistic first = a.get(i);
			ClusStatistic second = b.get(i);
			result += first.getSquaredDistance(second);
		}
		return result/a.size();
	}
	
	public void calculatePredictionDistances(ClusBeam beam,ClusBeamModel candidate){
		ArrayList arr = beam.toArray();
		ClusBeamModel beamModel1, beamModel2;
		ArrayList<ClusStatistic> predModel1, predModel2, predCandidate;
		predCandidate = candidate.getModelPredictions();
		double dist; // the average distance of each model of the beam to the other beam members + the candidate model
		double candidateDist = 0.0; //the average distance of the candidate model to all beam members
		for (int i = 0; i < arr.size(); i++){
			beamModel1 = (ClusBeamModel)arr.get(i);
			predModel1 = beamModel1.getModelPredictions();
			dist = 0.0;
			for (int j = 0; j < arr.size(); j++){
				beamModel2 = (ClusBeamModel) arr.get(j);
				predModel2 = beamModel2.getModelPredictions();
				dist += getDistance(predModel1, predModel2);
			}
			double cdist = getDistance(predModel1, predCandidate); 
			dist += cdist;
			candidateDist += cdist;
			
//			dist = 1-(dist / beam.getCrWidth());
			beamModel1.setDistanceToBeam(dist);
		}
//		candidateDist = 1 - (candidateDist / beam.getCrWidth());
//		similarity = 1 - average(distance)
		candidate.setDistanceToBeam(candidateDist);
	}

	public void calculatePredictionDistancesOpt(ClusBeam beam,ClusBeamModel candidate){
		ArrayList arr = beam.toArray();
		int size = arr.size();
		ClusBeamModel beamModel1, beamModel2;
		ArrayList<ClusStatistic> predModel1, predModel2, predCandidate;
		predCandidate = candidate.getModelPredictions();
		double candidateDist = 0.0; //the average distance of the candidate model to all beam members
		double[] tempDist = new double[size];
		double temp = 0.0;
		for (int i = 0; i < (size-1); i++){
			beamModel1 = (ClusBeamModel)arr.get(i);
			predModel1 = beamModel1.getModelPredictions();
			for (int j = i+1; j < size; j++){
				beamModel2 = (ClusBeamModel) arr.get(j);
				predModel2 = beamModel2.getModelPredictions();
				temp = getDistance(predModel1, predModel2);
				tempDist[i] += temp;
				tempDist[j] += temp;
			}
			temp = getDistance(predModel1, predCandidate);
			tempDist [i] += temp;
			candidateDist += temp;
			beamModel1.setDistanceToBeam(tempDist[i]);
		}
		temp = getDistance(((ClusBeamModel)arr.get(size-1)).getModelPredictions(), predCandidate);
		tempDist [size-1] += temp;
		candidateDist += temp;
		((ClusBeamModel)arr.get(size-1)).setDistanceToBeam(tempDist[size-1]);
		candidate.setDistanceToBeam(candidateDist);
	}

	public void addDistToCandOpt(ClusBeam beam, ClusBeamModel candidate){
		ArrayList arr = beam.toArray();
		int size = arr.size();
		ClusBeamModel model;
		ArrayList<ClusStatistic> candidatepredictions = candidate.getModelPredictions();
		double dist = 0.0;
		double candidatedist = 0.0;
		double distance;
		for (int i = 0; i < size; i++){
			model = (ClusBeamModel)arr.get(i);
			dist = getDistance(model.getModelPredictions(), candidatepredictions);
			candidatedist += dist;
			distance = model.getDistanceToBeam();
			distance += dist;
			model.setDistanceToBeam(distance);
		}
		candidate.setDistanceToBeam(candidatedist);

	}

	public void deductFromBeamOpt(ClusBeam beam, ClusBeamModel candidate, int position){
		ArrayList arr = beam.toArray();
		int size = arr.size();
		ArrayList<ClusStatistic> candidatepredictions = candidate.getModelPredictions();
		ClusBeamModel model;
		double dist = 0.0;
		double distance;
		if (position == size){
			//the candidate does not enter the beam
			for (int i = 0; i < size; i++){
				model = (ClusBeamModel)arr.get(i);
				dist = getDistance(model.getModelPredictions(), candidatepredictions);
				distance = model.getDistanceToBeam();
				distance -= dist;
				model.setDistanceToBeam(distance);
			}
		}else{
			ClusBeamModel exitmodel = (ClusBeamModel)arr.get(position);
			ArrayList<ClusStatistic> exitpredictions = exitmodel.getModelPredictions();
			for (int j = 0; j < size; j++){
				if (j != position){
					model = (ClusBeamModel)arr.get(j);
					dist = getDistance(model.getModelPredictions(), exitpredictions);
					distance = model.getDistanceToBeam();
					distance -= dist;
					model.setDistanceToBeam(distance);
				}
			}
			dist = getDistance(candidatepredictions, exitpredictions);
			distance = candidate.getDistanceToBeam();
			distance -= dist;
			candidate.setDistanceToBeam(distance);
		}
	}


	public boolean getIsBeamUpdated(){
		return isBeamUpdated;
	}

	public void setIsBeamUpdated(boolean update){
		isBeamUpdated = update;
	}

	/**Dragi
	 * Calculates BeamSimilarity for a given Data Set
	 *
	 * @param beam
	 * @param data
	 * @param isNum
	 * @return
	 */
	public static double calcBeamSimilarity(ArrayList beam, RowData data, boolean isNum) {
		ArrayList<ArrayList<ClusStatistic>> predictions = new ArrayList<ArrayList<ClusStatistic>>();
		double result = 0.0;
		double dist;
		for (int i = 0; i < beam.size(); i++){
			try{
				ClusBeamModel model = (ClusBeamModel)beam.get(i);
				predictions.add(getPredictionsDataSet(model.getModel(), data, isNum));
			}catch(ClassCastException e){
				ClusModel model = (ClusModel)beam.get(i);
				predictions.add(getPredictionsDataSet(model, data, isNum));
			}
		}
		for (int m = 0; m < predictions.size(); m++){
			dist = 0.0;
			for (int n = 0; n < predictions.size(); n++){
				dist += getDistance((ArrayList<ClusStatistic>)predictions.get(m), (ArrayList<ClusStatistic>)predictions.get(n));
			}
			dist = 1 - (dist / beam.size());
//			System.out.println("Model "+m+": "+(-((ClusBeamModel)beam.get(m)).getValue())+"\t"+dist);
			result += dist;
		}
		return result / beam.size();
	}

	/**Dragi
	 * Calculates the distance between a given model and the syntactic constraint
	 * @param model
	 * @param constraint
	 * @return Similarity between the candidate and the constraint
	 */
	public double getDistToConstraint(ClusBeamModel model, ClusBeamSyntacticConstraint constraint){
		return (1 - getDistance(model.getModelPredictions(), constraint.getConstraintPredictions()));
	}
	
}

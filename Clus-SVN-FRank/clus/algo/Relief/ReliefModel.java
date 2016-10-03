package clus.algo.Relief;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.main.ClusRun;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;
import clus.statistic.StatisticPrintInfo;
import clus.util.ClusException;
import jeans.util.MyArray;

public class ReliefModel implements ClusModel{
	private int m_NbNeighbours;
	private int m_NbIterations;
	private RowData m_Data;
	
	private double[] m_Weights;
	
	public ReliefModel(int neighbours, int iterations, RowData data) throws ClusException{
		this.m_Data = data;
		if(neighbours == -1 || neighbours >= data.getNbRows()){ 
			this.m_NbNeighbours = data.getNbRows() - 1;
			System.out.println("Setting the number of neighbours to number of samples - 1.");
		} else if(neighbours > 0){
			this.m_NbNeighbours = neighbours;
		} else{
			throw new ClusException(String.format("The number neighbours should be 0 < neighbours < number of examples (= %s) or -1.\n"
					+ "But: neighbours = %d", data.getNbRows(), neighbours));
		}
		if(iterations == -1 || iterations > data.getNbRows()){
			this.m_NbIterations = data.getNbRows();
			System.out.println("Setting the number of iterations to number of samples.");
		} else if(iterations > 0){
			this.m_NbIterations = iterations;
		} else{
			throw new ClusException(String.format("The number iterations should be 0 < iterations <= number of examples (= %s) or -1.\n"
					+ "But: iterations = %d", data.getNbRows(), iterations));
		}		
	}

	@Override
	public ClusStatistic predictWeighted(DataTuple tuple) {
		throw new RuntimeException("Relief is not a predictive model."); 
	}

	@Override
	public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getModelSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getModelInfo() {
		return String.format("Relief with the weights computed in %d iterations with %d neighbours.", m_NbIterations, m_NbNeighbours);
	}

	@Override
	public void printModel(PrintWriter wrt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printModelToPythonScript(PrintWriter wrt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attachModel(HashMap table) throws ClusException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retrieveStatistics(ArrayList list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClusModel prune(int prunetype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void computeWeights(){
		int descriptive = m_Data.m_Schema.getNbDescriptiveAttributes();
		m_Weights = new double[descriptive];
		for(int i = 0; i < descriptive; i++){
			m_Weights[i] = i;
		}
		
	}
	
	public RowData getData(){
		return m_Data;
	}
	
	public int getNbNeighbours(){
		return m_NbNeighbours;
	}
	
	public int getNbIterations(){
		return m_NbIterations;
	}
}

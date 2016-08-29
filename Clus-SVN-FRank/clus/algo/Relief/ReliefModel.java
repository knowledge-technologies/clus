package clus.algo.Relief;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

	@Override
	public ClusStatistic predictWeighted(DataTuple tuple) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

}

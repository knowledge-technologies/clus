
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonObject;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.section.SettingsRelief;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public class ReliefModel implements ClusModel {

    private int[] m_NbNeighbours;
    private int[] m_NbIterations;
    private boolean m_WeightNeighbours;
    private double m_Sigma;
    private RowData m_Data;

    private double[] m_Weights;


    public ReliefModel(int[] neighbours, int[] iterations, boolean weightNeighbours, double sigma, RowData data) throws ClusException {
        this.m_Data = data;
        this.m_WeightNeighbours = weightNeighbours;
        this.m_Sigma = sigma;
        this.m_NbNeighbours = getNeighboursOrIterations(neighbours, data.getNbRows(), true);
        this.m_NbIterations = getNeighboursOrIterations(iterations, data.getNbRows(), false);
    }


    private static int[] getNeighboursOrIterations(int[] candidateValues,  int nbExamples, boolean isNeighbours){
    	HashSet<Integer> ok = new HashSet<Integer>();
    	int lowerBound = isNeighbours ? 0 : -1;
    	int upperBound = nbExamples;
    	upperBound -= isNeighbours ? 1 : 0;
    	String parameter = isNeighbours ? "neighbours <" : "iterations <=";
    	int defaultValue = isNeighbours ? SettingsRelief.RELIEF_NEIGHBOUR_DEFAULT : nbExamples;
    	defaultValue = Math.min(defaultValue, upperBound);
    	boolean allowMinusOne = !isNeighbours;
        for(int i = 0; i < candidateValues.length; i++){
	        if (lowerBound < candidateValues[i] && candidateValues[i] <= upperBound) {
	        	ok.add(candidateValues[i]);
	        }
	        else if (allowMinusOne && candidateValues[i] == -1){
	        	ok.add(nbExamples);
	        }
	        else {
	        	System.err.println(String.format("Oops. Condition is broken: %d < %d = %s number of examples (= %d).", lowerBound, candidateValues[i], parameter, upperBound));
	            System.err.println(String.format("Changed the value to the min(default value, upperBound): %d.", defaultValue));
	            ok.add(defaultValue);
	        }
       }
       int whereTo = 0;
       int[] okValues = new int[ok.size()];
       for(int value : ok){
    	   okValues[whereTo++] = value;
       }
       Arrays.sort(okValues);
       return okValues;
    	
    }
    
    @Override
    public ClusStatistic predictWeighted(DataTuple tuple) {
        throw new RuntimeException("Relief is not a predictive model.");
    }


    @Override
    public void applyModelProcessors(DataTuple tuple, MyArray mproc) throws IOException {
        

    }


    @Override
    public int getModelSize() {
        
        return 0;
    }


    @Override
    public String getModelInfo() {
    	String first = "Relief feature ranking method with the weights computed in all combinations of";
    	String second = String.format("numbers of neighbours: %s", Arrays.toString(m_NbNeighbours)).replace(",", ";");
    	String third = String.format("numbers of iterations: %s", Arrays.toString(m_NbIterations)).replace(",", ";");
        return String.join(" ", new String[]{first, second, third});
    }


    @Override
    public void printModel(PrintWriter wrt) {
        

    }


    @Override
    public void printModel(PrintWriter wrt, StatisticPrintInfo info) {
        

    }


    @Override
    public void printModelAndExamples(PrintWriter wrt, StatisticPrintInfo info, RowData examples) {
        

    }


    @Override
    public void printModelToQuery(PrintWriter wrt, ClusRun cr, int starttree, int startitem, boolean exhaustive) {
        

    }


    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices) {}
    
    @Override
    public void printModelToPythonScript(PrintWriter wrt, HashMap<String, Integer> indices, String modelIdentifier) {
    	printModelToPythonScript(wrt, indices);
    }

    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info) {
        return null;
    }

    @Override
    public JsonObject getModelJSON(StatisticPrintInfo info, RowData examples) {
        return null;
    }

    @Override
    public JsonObject getModelJSON() {
        return null;
    }


    @Override
    public void attachModel(HashMap table) throws ClusException {
        

    }


    @Override
    public void retrieveStatistics(ArrayList list) {
        

    }


    @Override
    public ClusModel prune(int prunetype) {
        
        return null;
    }


    @Override
    public int getID() {
        
        return 0;
    }


    public void computeWeights() {
        int descriptive = m_Data.m_Schema.getNbDescriptiveAttributes();
        m_Weights = new double[descriptive];
        for (int i = 0; i < descriptive; i++) {
            m_Weights[i] = i;
        }

    }


    public RowData getData() {
        return m_Data;
    }


    public int[] getNbNeighbours() {
        return m_NbNeighbours;
    }


    public int[] getNbIterations() {
        return m_NbIterations;
    }


    public boolean getWeightNeighbours() {
        return m_WeightNeighbours;
    }


    public double getSigma() {
        return m_Sigma;
    }
}

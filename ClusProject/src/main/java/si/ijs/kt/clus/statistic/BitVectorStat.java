/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.statistic;

import java.util.ArrayList;

import si.ijs.kt.clus.data.cols.ColTarget;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.ext.ensemble.ros.ClusEnsembleROSInfo;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.list.BitList;


public class BitVectorStat extends ClusStatistic {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected BitList m_Bits = new BitList();
    protected boolean m_Modified = true;
    private BitVectorStat m_ParentStat;


    public BitVectorStat(Settings sett) {
        super(sett);
        // TODO Auto-generated constructor stub
    }

    
    @Override
    public ClusStatistic cloneStat() {
        BitVectorStat stat = new BitVectorStat(this.m_Settings);
        stat.cloneFrom(this);
        return stat;
    }


    public void cloneFrom(BitVectorStat other) {
    }


    @Override
    public void setSDataSize(int nbex) {
        m_Bits.resize(nbex);
        m_Modified = true;
    }


    @Override
    public void update(ColTarget target, int idx) {
        System.err.println("BitVectorStat: this version of update not implemented");
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        m_SumWeight += tuple.getWeight();
        m_Bits.setBit(idx);
        m_Modified = true;
    }


    @Override
    public void calcMean() throws ClusException {
    }


    @Override
    public String getArrayOfStatistic() {
        return "[" + String.valueOf(m_SumWeight) + "]";
    }


    @Override
    public String getString(StatisticPrintInfo info) {
        return String.valueOf(m_SumWeight);
    }


    @Override
    public void reset() {
        m_SumWeight = 0.0;
        m_Bits.reset();
        m_Modified = true;
    }


    @Override
    public void copy(ClusStatistic other) {
        BitVectorStat or = (BitVectorStat) other;
        m_SumWeight = or.m_SumWeight;
        m_Bits.copy(or.m_Bits);
        m_Modified = or.m_Modified;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        System.err.println("BitVectorStat: addPrediction not implemented");
    }


    @Override
    public void add(ClusStatistic other) {
        BitVectorStat or = (BitVectorStat) other;
        m_SumWeight += or.m_SumWeight;
        m_Bits.add(or.m_Bits);
        m_Modified = true;
    }


    @Override
    public void addScaled(double scale, ClusStatistic other) {
        System.err.println("BitVectorStat: addScaled not implemented");
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        BitVectorStat or = (BitVectorStat) other;
        m_SumWeight -= or.m_SumWeight;
        m_Bits.subtractFromThis(or.m_Bits);
        m_Modified = true;
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        BitVectorStat or = (BitVectorStat) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_Bits.subtractFromOther(or.m_Bits);
        m_Modified = true;
    }


    public int getNbTuples() {
        return m_Bits.getNbOnes();
    }


    @Override
    public double[] getNumericPred() {
        System.err.println("BitVectorStat: getNumericPred not implemented");
        return null;
    }


    @Override
    public int[] getNominalPred() {
        System.err.println("BitVectorStat: getNominalPred not implemented");
        return null;
    }


    @Override
    public String getPredictedClassName(int idx) {
        return "";
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes) {
        System.err.println(getClass().getName() + "BitVectorStat: vote not implemented");
    }


    @Override
    public void vote(ArrayList<ClusStatistic> votes, ClusEnsembleROSInfo targetSubspaceInfo) {
        System.err.println(getClass().getName() + "BitVectorStat: vote not implemented");
    }


	@Override
	public int getNbStatisticComponents() {
		throw new RuntimeException("BitVectorStat: getNbStatisticComponents() not implemented.");
	}
	
	@Override
	public void setParentStat(ClusStatistic parent) {
		// throw new UnsupportedOperationException("Not supported yet.");
		m_ParentStat = (BitVectorStat) parent;
	}

	@Override
	public ClusStatistic getParentStat() {
		// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		return m_ParentStat;
	}
	
	@Override
	public double getTargetSumWeights() { // TODO: missing values
		return getTotalWeight();
	}
}

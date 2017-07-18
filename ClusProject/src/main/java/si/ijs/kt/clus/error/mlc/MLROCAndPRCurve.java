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

package si.ijs.kt.clus.error.mlc;

import java.io.PrintWriter;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.BinaryPredictionList;
import si.ijs.kt.clus.error.ROCAndPRCurve;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNominalError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;


/**
 * @author matejp
 * 
 *         MLROCAndPRCurve is used in multi-label classification scenario, and it is an analogue of
 *         clus.error.ROCAndPRCurve.
 */
public abstract class MLROCAndPRCurve extends ClusNominalError { // does not implement ComponentError - would this make sense?

    protected static final int averageAUROC = 0;
    protected static final int averageAUPRC = 1;
    protected static final int weightedAUPRC = 2;
    protected static final int pooledAUPRC = 3;

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_AreaROC, m_AreaPR;
    protected double[] m_Thresholds;

    protected transient boolean m_ExtendPR;
    protected transient BinaryPredictionList m_Values;
    // protected transient double[] m_PrecisionAtRecall;

    protected BinaryPredictionList[] m_ClassWisePredictions;
    protected ROCAndPRCurve[] m_ROCAndPRCurves;

    protected double m_AverageAUROC = -1.0, m_AverageAUPRC = -1.0, m_WAvgAUPRC = -1.0, m_PooledAUPRC = -1.0;


    public MLROCAndPRCurve(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);

        m_ClassWisePredictions = new BinaryPredictionList[m_Dim];
        m_ROCAndPRCurves = new ROCAndPRCurve[m_Dim];
        for (int i = 0; i < m_Dim; i++) {
            BinaryPredictionList predlist = new BinaryPredictionList();
            m_ClassWisePredictions[i] = predlist;
            m_ROCAndPRCurves[i] = new ROCAndPRCurve(predlist);
        }
    }
    
    public BinaryPredictionList[] getClassWisePredictions(){
    	return m_ClassWisePredictions;
    }

    public void add(ClusError other) {
        MLROCAndPRCurve castedOther = (MLROCAndPRCurve) other;
        BinaryPredictionList[] otherCWP = castedOther.getClassWisePredictions();
        for(int i = 0; i < m_Dim; i++){
        	m_ClassWisePredictions[i].add(otherCWP[i]);
        }
    }

    public boolean shouldBeLow() {
        return false;
    }


    public void reset() {
        m_AreaROC = -1.0;
        m_AreaPR = -1.0;

        m_Values.clear();

        m_ClassWisePredictions = new BinaryPredictionList[m_Dim];
        m_ROCAndPRCurves = new ROCAndPRCurve[m_Dim];
        for (int i = 0; i < m_Dim; i++) {
            BinaryPredictionList predlist = new BinaryPredictionList();
            m_ClassWisePredictions[i] = predlist;
            m_ROCAndPRCurves[i] = new ROCAndPRCurve(predlist);
        }

        m_AverageAUROC = -1.0;
        m_AverageAUPRC = -1.0;
        m_WAvgAUPRC = -1.0;
        m_PooledAUPRC = -1.0;

    }

    // NE RABIM?
    // public void add(ClusError other) {
    // MLROCAndPRCurve mlCurves = (MLROCAndPRCurve)other;
    // m_TP += mlCurves.m_TP;
    // m_TN += mlCurves.m_TN;
    //
    // BinaryPredictionList[] olist = ((MLROCAndPRCurve)other).m_ClassWisePredictions;
    // for (int i = 0; i < m_Dim; i++) {
    // m_ClassWisePredictions[i].add(olist[i]);
    // }
    // // TO DO: Preveri na konc,a je to to
    // }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


    // ZAKOMENTIRAL - je smiselno?
//    public double getMLROCAndPRCurve(int i) {
//        return getModelErrorComponent(i);
//    }

    public double getModelError() {
        throw new RuntimeException("This must be implemented by a subclas.");
    }


    public double getModelError(int typeOfCurve) {
        computeAll();
        switch (typeOfCurve) {
            case averageAUROC:
                return m_AverageAUROC;
            case averageAUPRC:
                return m_AverageAUPRC;
            case weightedAUPRC:
                return m_WAvgAUPRC;
            case pooledAUPRC:
                return m_PooledAUPRC;
        }
        throw new RuntimeException("Unknown type of curve: typeOfCurve" + typeOfCurve);
    }


    public void computeAll() {
        BinaryPredictionList pooled = new BinaryPredictionList();
        ROCAndPRCurve pooledCurve = new ROCAndPRCurve(pooled);

        for (int i = 0; i < m_Dim; i++) {
            // if(isEvalClass(i)){
            m_ClassWisePredictions[i].sort();
            m_ROCAndPRCurves[i].computeCurves();
            // outputPRCurve(i, m_ROCAndPRCurves[i]);
            // outputROCCurve(i, m_ROCAndPRCurves[i]);
            m_ROCAndPRCurves[i].clear(); // - ZAKAJ CLEAR?
            pooled.add(m_ClassWisePredictions[i]);
            m_ClassWisePredictions[i].clearData(); // - ZAKAJ CLEAR?
            // }
        }
        pooled.sort();
        pooledCurve.computeCurves();
        // outputPRCurve(-1, pooledCurve);
        // outputROCCurve(-1, pooledCurve);
        pooledCurve.clear();
        // Compute averages
        int cnt = 0;
        double sumAUROC = 0.0;
        double sumAUPRC = 0.0;
        double sumAUPRCw = 0.0;
        double sumFrequency = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            // In compatibility mode, averages never include classes with zero frequency in test set
            // if (isEvalClass(i)) {
            double freq = m_ClassWisePredictions[i].getFrequency();
            sumAUROC += m_ROCAndPRCurves[i].getAreaROC();
            sumAUPRC += m_ROCAndPRCurves[i].getAreaPR();
            sumAUPRCw += freq * m_ROCAndPRCurves[i].getAreaPR();
            sumFrequency += freq;
            cnt++;
            // }
        }
        m_AverageAUROC = sumAUROC / cnt;
        m_AverageAUPRC = sumAUPRC / cnt;
        m_WAvgAUPRC = sumAUPRCw / sumFrequency;
        m_PooledAUPRC = pooledCurve.getAreaPR();
        // Compute average precisions at recall values
        // if (m_RecallValues != null) {
        // int nbRecalls = m_RecallValues.length;
        // m_AvgPrecisionAtRecall = new double[nbRecalls];
        // for (int j = 0; j < nbRecalls; j++) {
        // int nbClass = 0;
        // for (int i = 0; i < m_Dim; i++) {
        // if (isEvalClass(i)) {
        // double prec = m_ROCAndPRCurves[i].getPrecisionAtRecall(j);
        // m_AvgPrecisionAtRecall[j] += prec;
        // nbClass++;
        // }
        // }
        // m_AvgPrecisionAtRecall[j] /= nbClass;
        // }
        // }
    }


    public abstract void showModelError(PrintWriter out, int detail);
    // NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
    // computeAll();
    // out.println();
    // out.println(" Average AUROC: " + m_AverageAUROC);
    // out.println(" Average AUPRC: " + m_AverageAUPRC);
    // out.println(" Average AUPRC (weighted): " + m_WAvgAUPRC);
    // out.println(" Pooled AUPRC: " + m_PooledAUPRC);
    // if (m_RecallValues != null) {
    // int nbRecalls = m_RecallValues.length;
    // for (int i = 0; i < nbRecalls; i++) {
    // int rec = (int)Math.floor(100.0*m_RecallValues[i]+0.5);
    // out.println(" P"+rec+"R: "+(100.0*m_AvgPrecisionAtRecall[i]));
    // }
    // }


    // if (m_PRCurves != null) {
    // m_PRCurves.close();
    // m_PRCurves = null;
    // }
    // if (m_ROCCurves != null) {
    // m_ROCCurves.close();
    // m_ROCCurves = null;
    // }
    // }

    public String getName() {
        return "MLROCAndPRCurve";
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        double[][] probabilities = ((ClassificationStat) pred).getProbabilityPrediction(); // probabilities[i][0] =
                                                                                           // P(label_i is relevant for
                                                                                           // the example)
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                boolean groundTruth = attr.getNominal(tuple) == 0; // label relevant for tuple IFF
                                                                   // attr.getNominal(tuple) == 0
                m_ClassWisePredictions[i].addExample(groundTruth, probabilities[i][0]);
            }
        }
    }


    public void addExample(DataTuple tuple, DataTuple pred) {
        throw new RuntimeException("Not implemented!");
    }


    // NEDOTAKNJENO
    public void addInvalid(DataTuple tuple) {
    }


    public abstract ClusError getErrorClone(ClusErrorList par);

}

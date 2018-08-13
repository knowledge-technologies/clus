package si.ijs.kt.clus.statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.DoubleStream;

import si.ijs.kt.clus.algo.kNN.KnnModel;
import si.ijs.kt.clus.algo.kNN.methods.SearchAlgorithm;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;


public class KnnMlcStat extends ClassificationStat {
	/** An array whose i-th element is an array [P(i-th label), P(not i-th label)]. The probabilities are estimated from the training set.  */
	private double[][] m_PriorLabelProbabilities;
	/** The i-th element (hash map) of this list contains the following conditional probabilities for the i-th label.
	 * The corresponding hash-map's keys {@code k} are all considered numbers of neighbours. The corresponding value is an array {@code p},
	 * where <br>{@code p[b][n] = P(i-th label is relevant for n out of k neighbours | i-th label = b)},<br> where {@code b} is either 0 (relevant) or 1 (irrelevant).
	 * The probabilities are estimated from the training set. */
    private ArrayList<HashMap<Integer, double[][]>> m_NeighbourhoodProbabilities;
    private boolean m_IsInitialized = false;

	public KnnMlcStat(Settings sett, NominalAttrType[] nomAtts) {
		super(sett, nomAtts);
		for (int i = 0; i < m_NbTarget; i++) {
			if (m_ClassCounts[i].length != 2) {
				throw new RuntimeException("This is not MLC!");
			}
		}
	}

	public KnnMlcStat(Settings sett, NominalAttrType[] nomAtts, INIFileNominalOrDoubleOrVector multiLabelThreshold) {
		super(sett, nomAtts, multiLabelThreshold);
		throw new RuntimeException("Thresholds do not have any influence here.");
	}
	
    
    @Override
    public ClusStatistic cloneStat() {
    	KnnMlcStat res = new KnnMlcStat(m_Settings, m_Attrs);
        res.m_Training = m_Training;
        res.m_ParentStat = m_ParentStat;
        if (m_Thresholds != null) {
            res.m_Thresholds = Arrays.copyOf(m_Thresholds, m_Thresholds.length);
        }
        res.m_PriorLabelProbabilities = m_PriorLabelProbabilities;
        res.m_NeighbourhoodProbabilities = m_NeighbourhoodProbabilities;
        res.m_IsInitialized = m_IsInitialized; 
        return res;
    }
    
    /**
     * Initializes MLC counts, i.e., updates the fields m_PriorLabelProbabilities and m_NeighbourhoodProbabilities.
     * @param ks
     * @param trainSet
     * @param model
     * @throws ClusException
     */
	public void tryInitializeMLC(int[] ks, RowData trainSet, KnnModel model, double smoother) throws ClusException {
		if (m_IsInitialized) {
			throw new RuntimeException("This method was called more than once.");
		}
		m_IsInitialized = true;
		int nbExamples = trainSet.getNbRows();
		int maxK = model.getMaxK();
		SearchAlgorithm search = model.getSearch();
		ClusAttrType[] labels = trainSet.getSchema().getAllAttrUse(AttributeUseType.Target);
		int nbLabels = labels.length;
		m_PriorLabelProbabilities = new double[nbLabels][2];
		m_NeighbourhoodProbabilities = new ArrayList<>();
		for(int label = 0; label < nbLabels; label++) {
			HashMap<Integer, double[][]> labelMap = new HashMap<>();
			for (int k : ks) {
				labelMap.put(k, new double[2][maxK + 1]);
			}
			m_NeighbourhoodProbabilities.add(labelMap);
		}
		// counting
		for (DataTuple dt : trainSet.getData()) {
			LinkedList<DataTuple> nearest = search.returnNNs(dt, maxK);
			for (int label = 0; label < nbLabels; label++) {
				NominalAttrType attr = (NominalAttrType) labels[label];
				int dtAttrValue = attr.getNominal(dt);
				m_PriorLabelProbabilities[label][dtAttrValue]++;	
				int labelCount = 0;
				int nbNeighs = 0;
				int kIndex = 0;
				int neighAttrValue;
				for (DataTuple n : nearest) {
					nbNeighs++;
					neighAttrValue = attr.getNominal(n);
					if (neighAttrValue == 0) {   // <--> label relevant 
						labelCount++;
					}
					if (ks[kIndex] == nbNeighs) {
						m_NeighbourhoodProbabilities.get(label).get(nbNeighs)[dtAttrValue][labelCount]++;
						kIndex++;
					}
				}
				if (kIndex != ks.length) {
					throw new RuntimeException("Not all neighbourhood sizes were analyzed.");
				}
			}
		}
		// normalization
		for (int label = 0; label < nbLabels; label++) {
			HashMap<Integer, double[][]> labelMap = m_NeighbourhoodProbabilities.get(label);
			for (double[][] counts : labelMap.values()) {
				for(int isRelevant = 0; isRelevant < counts.length; isRelevant++) {
					double countSum = DoubleStream.of(counts[isRelevant]).sum();
					for (int labelCount = 0; labelCount < counts[isRelevant].length; labelCount++)
						counts[isRelevant][labelCount] = makeSmoother(counts[isRelevant][labelCount], countSum, counts[isRelevant].length, smoother);
					}
			}
			for(int isRelevant = 0; isRelevant < m_PriorLabelProbabilities[label].length; isRelevant++) {
				m_PriorLabelProbabilities[label][isRelevant] = makeSmoother(m_PriorLabelProbabilities[label][isRelevant], nbExamples, m_PriorLabelProbabilities[label].length, smoother);
			}
		}
	}
	
	
    @Override
    public void calcMean() {
        m_MajorityClasses = new int[m_NbTarget];
        for (int i = 0; i < m_NbTarget; i++) {
            m_MajorityClasses[i] = getMajorityClass(i);
        }
    }
    
    /**
     * Computes the class value with the highest probability, for each label.
     */
    @Override
    public int getMajorityClass(int attr) {
        int majClass = -1;
        double pMax = Double.NEGATIVE_INFINITY;
        int nbRelevantNeighbourhood = (int) Math.round(m_ClassCounts[attr][0]);
        for (int i = 0; i < 2; i++) {
        	double p = getProbability(attr, i, nbRelevantNeighbourhood);
            if (p > pMax) {
                majClass = i;
                pMax = p;
            }
        }
        return majClass;
    }

    private double getProbability(int labelIndex, int labelValue, int classCount) {
    	return m_PriorLabelProbabilities[labelIndex][labelValue] * m_NeighbourhoodProbabilities.get(labelIndex).get(m_NbExamples)[labelValue][classCount];
    }
    
    private static double makeSmoother(double origNumerator, double origDenominator, double classes, double smoother) {
    	return (origNumerator + smoother) / (classes * smoother + origDenominator);
    }
}

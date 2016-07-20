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

package clus.main;

import jeans.io.ini.INIFileNominalOrDoubleOrVector;

import clus.util.*;
import clus.data.ClusData;
import clus.data.attweights.*;
import clus.data.type.*;
import clus.data.rows.*;
import clus.error.*;
import clus.error.multiscore.*;
import clus.heuristic.*;
import clus.statistic.*;
import clus.model.ClusModel;
import clus.pruning.*;

import clus.ext.hierarchical.*;
import clus.ext.semisupervised.ModifiedGainHeuristic;
import clus.ext.semisupervised.SemiSupMinLabeledWeightStopCrit;
import clus.ext.sspd.*;
import clus.ext.timeseries.*;
import clus.ext.beamsearch.*;
import clus.ext.ilevelc.*;

import clus.algo.rules.*;

import java.io.*;
import java.util.*;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.DistributionFactory;

/** Statistics manager
 * Includes information about target attributes and weights etc.
 * Also if the task is regression or classification.
 */

public class ClusStatManager implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int MODE_NONE = -1;

	public final static int MODE_CLASSIFY = 0;

	public final static int MODE_REGRESSION = 1;

	public final static int MODE_HIERARCHICAL = 2;

	public final static int MODE_SSPD = 3;

	public final static int MODE_CLASSIFY_AND_REGRESSION = 4;

	public final static int MODE_TIME_SERIES = 5;

	public final static int MODE_ILEVELC = 6;

	public final static int MODE_PHYLO = 7;
	
	public final static int MODE_BEAM_SEARCH = 8;

	protected static int m_Mode = MODE_NONE;

	protected transient ClusHeuristic m_Heuristic;

	protected ClusSchema m_Schema;

	protected boolean m_BeamSearch;

	protected boolean m_RuleInduceOnly;

	protected Settings m_Settings;

	protected ClusStatistic[] m_TrainSetStatAttrUse;

	protected ClusStatistic[] m_StatisticAttrUse;

	/** Variance used for normalization of attributes during error computation etc. */
	protected ClusAttributeWeights m_NormalizationWeights;

	protected ClusAttributeWeights m_ClusteringWeights;

	protected ClusNormalizedAttributeWeights m_DispersionWeights;

	protected ClassHierarchy m_Hier;

	protected SSPDMatrix m_SSPDMtrx;

	protected double[] m_ChiSquareInvProb;

	public ClusStatManager(ClusSchema schema, Settings sett) throws ClusException, IOException {
		this(schema, sett, true);
	}

	public ClusStatManager(ClusSchema schema, Settings sett, boolean docheck) throws ClusException, IOException {
		m_Schema = schema;
		m_Settings = sett;
		if (docheck) {
			check();
			initStructure();
		}
	}

	public Settings getSettings() {
		return m_Settings;
	}

	public int getCompatibility() {
		return getSettings().getCompatibility();
	}

	public final ClusSchema getSchema() {
		return m_Schema;
	}

	public static final int getMode() {
		return m_Mode;
	}

	public boolean isClassificationOrRegression() {
		return m_Mode == MODE_CLASSIFY || m_Mode == MODE_REGRESSION || m_Mode == MODE_CLASSIFY_AND_REGRESSION;
	}

	public final ClassHierarchy getHier() {
		// System.out.println("ClusStatManager.getHier/0 called");
		return m_Hier;
	}

	public void initStatisticAndStatManager() throws ClusException, IOException {
		initWeights();
		initStatistic();
		initHierarchySettings();
	}

	public ClusAttributeWeights getClusteringWeights() {
		return m_ClusteringWeights;
	}

	public ClusNormalizedAttributeWeights getDispersionWeights() {
		return m_DispersionWeights;
	}

	public ClusAttributeWeights getNormalizationWeights() {
		return m_NormalizationWeights;
	}

	public static boolean hasBitEqualToOne(boolean[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i])
				return true;
		}
		return false;
	}

	public void initWeights(ClusNormalizedAttributeWeights result, NumericAttrType[] num, NominalAttrType[] nom, INIFileNominalOrDoubleOrVector winfo) throws ClusException {
		result.setAllWeights(0.0);
		int nbattr = result.getNbAttributes();
		if (winfo.hasArrayIndexNames()) {
			// Weights given for target, non-target, numeric and nominal
			double target_weight = winfo.getDouble(Settings.TARGET_WEIGHT);
			double non_target_weight = winfo.getDouble(Settings.NON_TARGET_WEIGHT);
			double num_weight = winfo.getDouble(Settings.NUMERIC_WEIGHT);
			double nom_weight = winfo.getDouble(Settings.NOMINAL_WEIGHT);
			if (getSettings().getVerbose() >= 2) {
				System.out.println("  Target weight     = " + target_weight);
				System.out.println("  Non target weight = " + non_target_weight);
				System.out.println("  Numeric weight    = " + num_weight);
				System.out.println("  Nominal weight    = " + nom_weight);
			}
			for (int i = 0; i < num.length; i++) {
				NumericAttrType cr_num = num[i];
				double tw = cr_num.getStatus() == ClusAttrType.STATUS_TARGET ? target_weight : non_target_weight;
				result.setWeight(cr_num, num_weight * tw);
			}
			for (int i = 0; i < nom.length; i++) {
				NominalAttrType cr_nom = nom[i];
				double tw = cr_nom.getStatus() == ClusAttrType.STATUS_TARGET ? target_weight : non_target_weight;
				result.setWeight(cr_nom, nom_weight * tw);
			}
		} else if (winfo.isVector()) {
			// Explicit vector of weights given
			if (nbattr != winfo.getVectorLength()) {
				throw new ClusException("Number of attributes is " + nbattr
						+ " but weight vector has only "
						+ winfo.getVectorLength() + " components");
			}
			for (int i = 0; i < nbattr; i++) {
				result.setWeight(i, winfo.getDouble(i));
			}
		} else {
			// One single constant weight given
			result.setAllWeights(winfo.getDouble());
		}
		// Normalize the weights for classification/regression rules only
		if (isRuleInduceOnly() && isClassificationOrRegression()) {
			double sum = 0;
			for (int i = 0; i < num.length; i++) {
				NumericAttrType cr_num = num[i];
				sum += result.getWeight(cr_num);
			}
			for (int i = 0; i < nom.length; i++) {
				NominalAttrType cr_nom = nom[i];
				sum += result.getWeight(cr_nom);
			}
			if (sum <= 0) {
				throw new ClusException("initWeights(): Sum of clustering/dispersion weights must be > 0!");
			}
			for (int i = 0; i < num.length; i++) {
				NumericAttrType cr_num = num[i];
				result.setWeight(cr_num, result.getWeight(cr_num) / sum);
			}
			for (int i = 0; i < nom.length; i++) {
				NominalAttrType cr_nom = nom[i];
				result.setWeight(cr_nom, result.getWeight(cr_nom) / sum);
			}
		}
	}

	public void initDispersionWeights() throws ClusException {
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
		initWeights(m_DispersionWeights, num, nom, getSettings().getDispersionWeights());
		if (getSettings().getVerbose() >= 1 && (isRuleInduceOnly() || isTreeToRuleInduce())
				&& getSettings().computeDispersion()) {
			System.out.println("Dispersion:   "	+ m_DispersionWeights.getName(m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_ALL)));
		}
	}

	public void initClusteringWeights() throws ClusException {
		if (getMode() == MODE_HIERARCHICAL) {
			int nb_attrs = m_Schema.getNbAttributes();
			m_ClusteringWeights = new ClusAttributeWeights(nb_attrs	+ m_Hier.getTotal());
			double[] weights = m_Hier.getWeights();
			NumericAttrType[] dummy = m_Hier.getDummyAttrs();
			for (int i = 0; i < weights.length; i++) {
				m_ClusteringWeights.setWeight(dummy[i], weights[i]);
			}
			return;
		}
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		initWeights((ClusNormalizedAttributeWeights) m_ClusteringWeights, num, nom, getSettings().getClusteringWeights());
		if (getSettings().getVerbose() >= 1) {
			System.out.println("Clustering: " + m_ClusteringWeights.getName(m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING)));
		}
	}

	/** Initializes normalization weights to m_NormalizationWeights variable */
	public void initNormalizationWeights(ClusStatistic stat, ClusData data)	throws ClusException {
		int nbattr = m_Schema.getNbAttributes();
		m_NormalizationWeights.setAllWeights(1.0);
		boolean[] shouldNormalize = new boolean[nbattr];
		INIFileNominalOrDoubleOrVector winfo = getSettings().getNormalizationWeights();
		if (winfo.isVector()) {
			if (nbattr != winfo.getVectorLength()) {
				throw new ClusException("Number of attributes is " + nbattr
						+ " but weight vector has only "
						+ winfo.getVectorLength() + " components");
			}
			for (int i = 0; i < nbattr; i++) {
				if (winfo.isNominal(i))	shouldNormalize[i] = true;
				else m_NormalizationWeights.setWeight(i, winfo.getDouble(i));
			}
		} else {
			if (winfo.isNominal() && winfo.getNominal() == Settings.NORMALIZATION_DEFAULT) {
				Arrays.fill(shouldNormalize, true);
			} else {
				m_NormalizationWeights.setAllWeights(winfo.getDouble());
			}
		}
		if (hasBitEqualToOne(shouldNormalize)) {
			data.calcTotalStat(stat);
			CombStat cmb = (CombStat) stat;
			data.calcTotalStat(stat);
			RegressionStat rstat = cmb.getRegressionStat();
			rstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
			// Normalization is currently required for trees but not for rules
			if (!isRuleInduceOnly()) {
				ClassificationStat cstat = cmb.getClassificationStat();
				cstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
			}
			if (m_Mode == MODE_TIME_SERIES) {
				TimeSeriesStat tstat = (TimeSeriesStat)createStatistic(ClusAttrType.ATTR_USE_TARGET);
				((RowData)data).calcTotalStatBitVector(tstat);
				tstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
			}
		}
	}

	public void initWeights() {
		int nbattr = m_Schema.getNbAttributes();
		m_NormalizationWeights = new ClusAttributeWeights(nbattr);
		m_NormalizationWeights.setAllWeights(1.0);
		m_ClusteringWeights = new ClusNormalizedAttributeWeights(m_NormalizationWeights);
		m_DispersionWeights = new ClusNormalizedAttributeWeights(m_NormalizationWeights);
	}

	public void check() throws ClusException {
		int nb_types = 0;
		int nb_nom = m_Schema.getNbNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		int nb_num = m_Schema.getNbNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		System.out.println("check " + nb_nom + " " + nb_num);
		if (nb_nom > 0 && nb_num > 0) {
			m_Mode = MODE_CLASSIFY_AND_REGRESSION;
			nb_types++;
		} else if (nb_nom > 0) {
			m_Mode = MODE_CLASSIFY;
			nb_types++;
		} else if (nb_num > 0) {
			m_Mode = MODE_REGRESSION;
			nb_types++;
		}
		if (m_Schema.hasAttributeType(ClusAttrType.ATTR_USE_TARGET, ClassesAttrType.THIS_TYPE)) {
			m_Mode = MODE_HIERARCHICAL;
			getSettings().setSectionHierarchicalEnabled(true);
			nb_types++;
		}
		int nb_int = 0;
		if (nb_int > 0 || m_Settings.checkHeuristic("SSPD")) {
			m_Mode = MODE_SSPD;
			nb_types++;
		}
		if (m_Settings.checkHeuristic("GeneticDistance")) {
			m_Mode = MODE_PHYLO;
		}
		if (m_Settings.isSectionTimeSeriesEnabled()) {
			m_Mode = MODE_TIME_SERIES;
			nb_types++;
		}
		if (m_Settings.isSectionILevelCEnabled()) {
			m_Mode = MODE_ILEVELC;
		}
		if (m_Settings.isBeamSearchMode() && (m_Settings.getBeamSimilarity() != 0.0)){
			m_Mode = MODE_BEAM_SEARCH;
		}
		
		if (nb_types == 0) {
			System.err.println("No target value defined");
		}
		if (nb_types > 1) {
			throw new ClusException("Incompatible combination of clustering attribute types");
		}
	}

	public void initStructure() throws IOException {
		switch (m_Mode) {
		case MODE_HIERARCHICAL:
			createHierarchy();
			break;
		case MODE_SSPD:
			m_SSPDMtrx = SSPDMatrix.read(getSettings().getFileAbsolute(getSettings().getAppName() + ".dist"), getSettings());
			break;
		}
	}

	public ClusStatistic createSuitableStat(NumericAttrType[] num, NominalAttrType[] nom) {
		if (num.length == 0) {
			if (m_Mode == MODE_PHYLO) {
				//switch (Settings.m_PhylogenyProtoComlexity.getValue()) {
				//case Settings.PHYLOGENY_PROTOTYPE_COMPLEXITY_PAIRWISE:
					return new GeneticDistanceStat(nom);
				//case Settings.PHYLOGENY_PROTOTYPE_COMPLEXITY_PROTO:
					//return new ClassificationStat(nom);
				//}
			}
			return new ClassificationStat(nom);
		} else if (nom.length == 0) {
			return new RegressionStat(num);
		} else {
			return new CombStat(this, num, nom);
		}
	}

	public boolean heuristicNeedsCombStat() {
		if (isRuleInduceOnly()) {
			//if (m_Mode == MODE_HIERARCHICAL) {
			//	return false;
			//}
			return (getSettings().getHeuristic() == Settings.HEURISTIC_DEFAULT
					|| getSettings().getHeuristic() == Settings.HEURISTIC_DISPERSION_ADT
					|| getSettings().getHeuristic() == Settings.HEURISTIC_DISPERSION_MLT
					|| getSettings().getHeuristic() == Settings.HEURISTIC_R_DISPERSION_ADT
					|| getSettings().getHeuristic() == Settings.HEURISTIC_R_DISPERSION_MLT);
		} else {
			return false;
		}
	}

	public void initStatistic() throws ClusException {
		m_StatisticAttrUse = new ClusStatistic[ClusAttrType.NB_ATTR_USE];
		// Statistic over all attributes
		NumericAttrType[] num1 = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL);
		NominalAttrType[] nom1 = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
		m_StatisticAttrUse[ClusAttrType.ATTR_USE_ALL] = new CombStat(this, num1, nom1);
		// Statistic over all target attributes
		NumericAttrType[] num2 = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom2 = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		m_StatisticAttrUse[ClusAttrType.ATTR_USE_TARGET] = createSuitableStat(num2, nom2);
		// Statistic over clustering attributes
		NumericAttrType[] num3 = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		NominalAttrType[] nom3 = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		if (num3.length != 0 || nom3.length != 0) {
			if (heuristicNeedsCombStat()) {
				m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] = new CombStat(this, num3, nom3);
			} else {
				m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] = createSuitableStat(num3, nom3);
			}
		}
		switch (m_Mode) {
		case MODE_HIERARCHICAL:
			if (getSettings().getHierDistance() == Settings.HIERDIST_WEIGHTED_EUCLIDEAN) {
				if(getSettings().getHierSingleLabel()){
					setClusteringStatistic(new HierSingleLabelStat(m_Hier, getCompatibility()));
					setTargetStatistic(new HierSingleLabelStat(m_Hier, getCompatibility()));
				}else{
					setClusteringStatistic(new WHTDStatistic(m_Hier, getCompatibility()));
					setTargetStatistic(new WHTDStatistic(m_Hier, getCompatibility()));
				}
			} else {
				ClusDistance dist = null;
				if (getSettings().getHierDistance() == Settings.HIERDIST_JACCARD) {
					dist = new HierJaccardDistance(m_Hier.getType());
				}
				setClusteringStatistic(new HierSumPairwiseDistancesStat(m_Hier, dist, getCompatibility()));
				setTargetStatistic(new HierSumPairwiseDistancesStat(m_Hier, dist, getCompatibility()));
			}
			break;
		case MODE_SSPD:
			ClusAttrType[] target = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
			m_SSPDMtrx.setTarget(target);
			setClusteringStatistic(new SumPairwiseDistancesStat(m_SSPDMtrx, 3));
			setTargetStatistic(new SumPairwiseDistancesStat(m_SSPDMtrx, 3));
			break;
		case MODE_TIME_SERIES:
			ClusAttrType[] targets = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
			TimeSeriesAttrType type = (TimeSeriesAttrType)targets[0];
			int efficiency = getSettings().m_TimeSeriesHeuristicSampling.getValue();
			switch (getSettings().getTimeSeriesDistance()) {
			case Settings.TIME_SERIES_DISTANCE_MEASURE_DTW:
				TimeSeriesDist dist = new DTWTimeSeriesDist(type);
				setClusteringStatistic(new TimeSeriesStat(type, dist, efficiency));
				setTargetStatistic(new TimeSeriesStat(type, dist, efficiency));
				break;
			case Settings.TIME_SERIES_DISTANCE_MEASURE_QDM:
				if (type.isEqualLength()) {
					TimeSeriesDist qdm = new QDMTimeSeriesDist(type);
					setClusteringStatistic(new TimeSeriesStat(type, qdm, efficiency));
					setTargetStatistic(new TimeSeriesStat(type, qdm, efficiency));
				} else {
					throw new ClusException("QDM Distance is not implemented for time series with different length");
				}
				break;
			case Settings.TIME_SERIES_DISTANCE_MEASURE_TSC:
				TimeSeriesDist tsc = new TSCTimeSeriesDist(type);
				setClusteringStatistic(new TimeSeriesStat(type, tsc, efficiency));
				setTargetStatistic(new TimeSeriesStat(type, tsc, efficiency));
				break;
			}
			break;
		case MODE_ILEVELC:
			setTargetStatistic(new ILevelCStatistic(num2));
			setClusteringStatistic(new ILevelCStatistic(num3));
			break;
		case MODE_BEAM_SEARCH:
			if (num3.length != 0 && num2.length != 0){
				setTargetStatistic(new ClusBeamSimRegrStat(num2,null));
				setClusteringStatistic(new ClusBeamSimRegrStat(num3,null));
			}
			break;
		}
	}

	public ClusHeuristic createHeuristic(int type) {
		switch (type) {
		case Settings.HEURISTIC_GAIN:
			return new GainHeuristic(false);
		default:
			return null;
		}
	}

	public void initRuleHeuristic() throws ClusException {
		if (m_Mode == MODE_CLASSIFY) {
			switch (getSettings().getHeuristic()) {
			case Settings.HEURISTIC_DEFAULT:
				m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this,getClusteringWeights());
				getSettings().setHeuristic(Settings.HEURISTIC_R_DISPERSION_MLT);
				break;
			case Settings.HEURISTIC_REDUCED_ERROR:
				m_Heuristic = new ClusRuleHeuristicError(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_MESTIMATE:
				m_Heuristic = new ClusRuleHeuristicMEstimate(getSettings().getMEstimate());
				break;
			case Settings.HEURISTIC_DISPERSION_ADT:
				m_Heuristic = new ClusRuleHeuristicDispersionAdt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_DISPERSION_MLT:
				m_Heuristic = new ClusRuleHeuristicDispersionMlt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_R_DISPERSION_ADT:
				m_Heuristic = new ClusRuleHeuristicRDispersionAdt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_R_DISPERSION_MLT:
				m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this,getClusteringWeights());
				break;
			default:
				throw new ClusException("Unsupported heuristic for single target classification rules!");
			}
		} else if (m_Mode == MODE_REGRESSION || m_Mode == MODE_CLASSIFY_AND_REGRESSION)  {
			switch (getSettings().getHeuristic()) {
			case Settings.HEURISTIC_DEFAULT:
				m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_REDUCED_ERROR:
				m_Heuristic = new ClusRuleHeuristicError(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_DISPERSION_ADT:
				m_Heuristic = new ClusRuleHeuristicDispersionAdt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_DISPERSION_MLT:
				m_Heuristic = new ClusRuleHeuristicDispersionMlt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_R_DISPERSION_ADT:
				m_Heuristic = new ClusRuleHeuristicRDispersionAdt(this,getClusteringWeights());
				break;
			case Settings.HEURISTIC_R_DISPERSION_MLT:
				m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this,getClusteringWeights());
				break;
			default:
				throw new ClusException("Unsupported heuristic for multiple target or regression rules!");
			}
		} else if (m_Mode == MODE_HIERARCHICAL) {
			m_Heuristic = new ClusRuleHeuristicHierarchical(this, getClusteringWeights());
			return;
			//getSettings().setHeuristic(Settings.HEURISTIC_SS_REDUCTION);

			/* String name = "Weighted Hierarchical Tree Distance";
			m_Heuristic = new ClusRuleHeuristicSSD(name, createClusteringStat(),getClusteringWeights());
			getSettings().setHeuristic(Settings.HEURISTIC_SS_REDUCTION);
			return; */
		} else if (m_Mode == MODE_TIME_SERIES) {
			String name = "Time Series Intra-Cluster Variation Heuristic for Rules";
			m_Heuristic = new ClusRuleHeuristicSSD(this, name, createClusteringStat(),getClusteringWeights());
			getSettings().setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
			return;
		} else if (m_Mode == MODE_ILEVELC) {
			String name = "Intra-Cluster Variation Heuristic for Rules";
			m_Heuristic = new ClusRuleHeuristicSSD(this, name, createClusteringStat(),getClusteringWeights());
		} else {
			throw new ClusException("Unsupported mode for rules!");
		}
	}

	public void initBeamSearchHeuristic() throws ClusException {
		if (getSettings().getHeuristic() == Settings.HEURISTIC_REDUCED_ERROR) {
			m_Heuristic = new ClusBeamHeuristicError(createClusteringStat());
		} else if (getSettings().getHeuristic() == Settings.HEURISTIC_MESTIMATE) {
			m_Heuristic = new ClusBeamHeuristicMEstimate(createClusteringStat(), getSettings().getMEstimate());
		} else if (getSettings().getHeuristic() == Settings.HEURISTIC_MORISHITA) {
			m_Heuristic = new ClusBeamHeuristicMorishita(createClusteringStat());
		} else {
			m_Heuristic = new ClusBeamHeuristicSS(createClusteringStat(), getClusteringWeights());
		}
	}

	public void initHeuristic() throws ClusException {
		// All rule learning heuristics should go here, except for rules from trees
		if (isRuleInduceOnly() && !isTreeToRuleInduce()) {
			initRuleHeuristic();
			return;
		}
		if (isBeamSearch()) {
			initBeamSearchHeuristic();
			return;
		}
		if (m_Mode == MODE_HIERARCHICAL) {
			if (getSettings().getCompatibility() <= Settings.COMPATIBILITY_MLJ08) {
				m_Heuristic = new VarianceReductionHeuristicCompatibility(createClusteringStat(), getClusteringWeights());
			} else {
				m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), null);
			}
			getSettings().setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
			return;
		}
		if (m_Mode == MODE_SSPD) {
			ClusStatistic clusstat = createClusteringStat();
			m_Heuristic = new VarianceReductionHeuristic(clusstat.getDistanceName(), clusstat, getClusteringWeights());
			getSettings().setHeuristic(Settings.HEURISTIC_SSPD);
			return;
		}
		if (m_Mode == MODE_TIME_SERIES) {
			ClusStatistic clusstat = createClusteringStat();
			m_Heuristic = new VarianceReductionHeuristic(clusstat.getDistanceName(), clusstat, getClusteringWeights());
			getSettings().setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
			return;
		}
		/* Set heuristic for trees */
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		if (getSettings().getHeuristic() == Settings.HEURISTIC_SS_REDUCTION_MISSING) {
			m_Heuristic = new VarianceReductionHeuristicInclMissingValues(getClusteringWeights(), m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), createClusteringStat());
			return;
		}
		if (num.length > 0 && nom.length > 0) {
			if (getSettings().getHeuristic() != Settings.HEURISTIC_DEFAULT && getSettings().getHeuristic() != Settings.HEURISTIC_VARIANCE_REDUCTION) {
				throw new ClusException("Only SS-Reduction heuristic can be used for combined classification/regression trees!");
			}
			m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING));
			getSettings().setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
		} else if (num.length > 0) {
			if (getSettings().getHeuristic() != Settings.HEURISTIC_DEFAULT && getSettings().getHeuristic() != Settings.HEURISTIC_VARIANCE_REDUCTION) {
				throw new ClusException("Only SS-Reduction heuristic can be used for regression trees!");
			}
			m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), m_Schema .getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING));
			getSettings().setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
		} else if (nom.length > 0) {
			if (getSettings().getHeuristic() == Settings.HEURISTIC_SEMI_SUPERVISED) {
				m_Heuristic = new ModifiedGainHeuristic(createClusteringStat());
			} else if (getSettings().getHeuristic() == Settings.HEURISTIC_REDUCED_ERROR) {
				m_Heuristic = new ReducedErrorHeuristic(createClusteringStat());
			} else if (getSettings().getHeuristic() == Settings.HEURISTIC_GENETIC_DISTANCE) {
				m_Heuristic = new GeneticDistanceHeuristicMatrix();
			} else if (getSettings().getHeuristic() == Settings.HEURISTIC_VARIANCE_REDUCTION) {
				m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING));
			} else if (getSettings().getHeuristic() == Settings.HEURISTIC_GAIN_RATIO) {
				m_Heuristic = new GainHeuristic(true);
			} else {
				if ((getSettings().getHeuristic() != Settings.HEURISTIC_DEFAULT &&
				    getSettings().getHeuristic() != Settings.HEURISTIC_GAIN) &&
				    getSettings().getHeuristic() != Settings.HEURISTIC_GENETIC_DISTANCE) {
						throw new ClusException("Given heuristic not supported for classification trees!");
				}
				m_Heuristic = new GainHeuristic(false);
				getSettings().setHeuristic(Settings.HEURISTIC_GAIN);
			}
		}
		else {
		}
	}

	public void initStopCriterion() {
		ClusStopCriterion stop = null;
		int minEx = getSettings().getMinimalNbExamples();
		double knownWeight = getSettings().getMinimalKnownWeight();			
		if (minEx > 0) {
			stop = new ClusStopCriterionMinNbExamples(minEx);
		} else if (knownWeight > 0) {
			stop = new SemiSupMinLabeledWeightStopCrit(knownWeight);
		} else {
			double minW = getSettings().getMinimalWeight();
			stop = new ClusStopCriterionMinWeight(minW);
			
		}
		m_Heuristic.setStopCriterion(stop);
	}

	/**
	 * Initializes a table with Chi Squared inverse probabilities used in
	 * significance testing of rules.
	 *
	 * @throws MathException
	 *
	 */
	public void initSignifcanceTestingTable() {
		int max_nom_val = 0;
		int num_nom_atts = m_Schema.getNbNominalAttrUse(ClusAttrType.ATTR_USE_ALL);
		for (int i = 0; i < num_nom_atts; i++) {
			if (m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL)[i].m_NbValues > max_nom_val) {
				max_nom_val = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_ALL)[i].m_NbValues;
			}
		}
		if (max_nom_val == 0) { // If no nominal attributes in data set
			max_nom_val = 1;
		}
		double[] table = new double[max_nom_val];
		table[0] = 1.0 - getSettings().getRuleSignificanceLevel();
		// Not really used except below
		for (int i = 1; i < table.length; i++) {
			DistributionFactory distributionFactory = DistributionFactory.newInstance();
			ChiSquaredDistribution chiSquaredDistribution = distributionFactory.createChiSquareDistribution(i);
			try {
				table[i] = chiSquaredDistribution.inverseCumulativeProbability(table[0]);
			} catch (MathException e) {
				e.printStackTrace();
			}
		}
		m_ChiSquareInvProb = table;
	}

	public ClusErrorList createErrorMeasure(MultiScore score) {
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		TimeSeriesAttrType[] ts = m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (nom.length != 0) {
			parent.addError(new ContingencyTable(parent, nom));
			parent.addError(new MSNominalError(parent, nom,	m_NormalizationWeights));
		}
		if (num.length != 0) {
			parent.addError(new AbsoluteError(parent, num));
			parent.addError(new MSError(parent, num));
			parent.addError(new RMSError(parent, num));
			if (getSettings().hasNonTrivialWeights()) {
				parent.addError(new RMSError(parent, num, m_NormalizationWeights));
			}
			parent.addError(new PearsonCorrelation(parent, num));
		}
		if (ts.length != 0) {
			ClusStatistic stat = createTargetStat();
			parent.addError(new AvgDistancesError(parent, stat.getDistance()));
		}
		switch (m_Mode) {
		case MODE_HIERARCHICAL:
			INIFileNominalOrDoubleOrVector class_thr = getSettings().getClassificationThresholds();
			if (class_thr.hasVector()) {
				parent.addError(new HierClassWiseAccuracy(parent, m_Hier));
			}
			double[] recalls = getSettings().getRecallValues().getDoubleVector();
			boolean wrCurves = getSettings().isWriteCurves();
			if(getSettings().isCalError()){
					parent.addError(new HierErrorMeasures(parent, m_Hier, recalls, getSettings().getCompatibility(), -1, wrCurves));
			}
			break;
		case MODE_ILEVELC:
			NominalAttrType cls = (NominalAttrType) getSchema().getLastNonDisabledType();
			parent.addError(new ILevelCRandIndex(parent, cls));
			break;
		}
		return parent;
	}

	public ClusErrorList createEvalError() {
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		TimeSeriesAttrType[] ts = m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (nom.length != 0) {
			parent.addError(new Accuracy(parent, nom));
		}
		if (num.length != 0) {
			parent.addError(new RMSError(parent, num));
		}
		if (ts.length != 0) {
			ClusStatistic stat = createTargetStat();
			parent.addError(new AvgDistancesError(parent, stat.getDistance()));
		}
		return parent;
	}

	public ClusErrorList createDefaultError() {
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (nom.length != 0) {
			parent.addError(new MisclassificationError(parent, nom));
		}
		if (num.length != 0) {
			parent.addError(new RMSError(parent, num));
		}
		switch (m_Mode) {
		case MODE_HIERARCHICAL:
			parent.addError(new HierClassWiseAccuracy(parent, m_Hier));
			break;
		}
		return parent;
	}

	// additive and weighted targets
	public ClusErrorList createAdditiveError() {
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (nom.length != 0) {
			parent.addError(new MisclassificationError(parent, nom));
		}
		if (num.length != 0) {
			parent.addError(new MSError(parent, num, getClusteringWeights()));
		}
		switch (m_Mode) {
		case MODE_HIERARCHICAL:
			parent.addError(new HierClassWiseAccuracy(parent, m_Hier));
			break;
		case MODE_TIME_SERIES:
			ClusStatistic stat = createTargetStat();
			parent.addError(new AvgDistancesError(parent, stat.getDistance()));
			break;
		}
		parent.setWeights(getClusteringWeights());
		return parent;
	}

	public ClusErrorList createExtraError(int train_err) {
		ClusErrorList parent = new ClusErrorList();
		if (m_Mode == MODE_TIME_SERIES) {
			ClusStatistic stat = createTargetStat();
			parent.addError(new ICVPairwiseDistancesError(parent, stat.getDistance()));
			parent.addError(new TimeSeriesSignificantChangeTesterXVAL(parent, (TimeSeriesStat)stat));
		}
		return parent;
	}

	public PruneTree getTreePrunerNoVSB() throws ClusException {
		Settings sett = getSettings();
		if (isBeamSearch() && sett.isBeamPostPrune()) {
			sett.setPruningMethod(Settings.PRUNING_METHOD_GAROFALAKIS);
			return new SizeConstraintPruning(sett.getBeamTreeMaxSize(), getClusteringWeights());
		}
		int err_nb = sett.getMaxErrorConstraintNumber();
		int size_nb = sett.getSizeConstraintPruningNumber();
		if (size_nb > 0 || err_nb > 0) {
			int[] sizes = sett.getSizeConstraintPruningVector();
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_CART_MAXSIZE) {
				return new CartPruning(sizes, getClusteringWeights());
			} else {
				sett.setPruningMethod(Settings.PRUNING_METHOD_GAROFALAKIS);
				SizeConstraintPruning sc_prune = new SizeConstraintPruning(sizes, getClusteringWeights());
				if (err_nb > 0) {
					double[] max_err = sett.getMaxErrorConstraintVector();
					sc_prune.setMaxError(max_err);
					sc_prune.setErrorMeasure(createDefaultError());
				}
				if (m_Mode == MODE_TIME_SERIES) {
					sc_prune.setAdditiveError(createAdditiveError());
				}
				return sc_prune;
			}
		}
		INIFileNominalOrDoubleOrVector class_thr = sett.getClassificationThresholds();
		if (class_thr.hasVector()) {
			return new HierClassTresholdPruner(class_thr.getDoubleVector());
		}
		if (m_Mode == MODE_REGRESSION) {
			double mult = sett.getM5PruningMult();
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_M5_MULTI) {
				return new M5PrunerMulti(getClusteringWeights(), mult);
			}
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_DEFAULT || sett.getPruningMethod() == Settings.PRUNING_METHOD_M5) {
				sett.setPruningMethod(Settings.PRUNING_METHOD_M5);
				return new M5Pruner(getClusteringWeights(), mult);
			}
		} else if (m_Mode == MODE_CLASSIFY) {
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_DEFAULT || sett.getPruningMethod() == Settings.PRUNING_METHOD_C45) {
				sett.setPruningMethod(Settings.PRUNING_METHOD_C45);
				return new C45Pruner();
			}
		} else if (m_Mode == MODE_HIERARCHICAL) {
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_M5) {
				double mult = sett.getM5PruningMult();
				return new M5Pruner(m_NormalizationWeights, mult);
			}
		} else if (m_Mode == MODE_PHYLO) {
			if (sett.getPruningMethod() == Settings.PRUNING_METHOD_ENCODING_COST) {
				return new EncodingCostPruning();
			}
		}
		sett.setPruningMethod(Settings.PRUNING_METHOD_NONE);
		return new PruneTree();
	}

	public PruneTree getTreePruner(ClusData pruneset) throws ClusException {
		Settings sett = getSettings();
		int pm = sett.getPruningMethod();
		if (pm == Settings.PRUNING_METHOD_NONE) {
			// Don't prune if pruning method is set to none, even if validation
			// set is given
			return new PruneTree();
		}
		if (m_Mode == MODE_HIERARCHICAL && pruneset != null) {
			PruneTree pruner = getTreePrunerNoVSB();
			boolean bonf = sett.isUseBonferroni();
			HierRemoveInsigClasses hierpruner = new HierRemoveInsigClasses(pruneset, pruner, bonf, m_Hier);
			hierpruner.setSignificance(sett.getHierPruneInSig());
			hierpruner.setNoRootPreds(sett.isHierNoRootPreds());
			sett.setPruningMethod(Settings.PRUNING_METHOD_DEFAULT);
			return hierpruner;
		}
		if (pruneset != null) {
			if (pm == Settings.PRUNING_METHOD_GAROFALAKIS_VSB || pm == Settings.PRUNING_METHOD_CART_VSB) {
				SequencePruningVSB pruner = new SequencePruningVSB(
						(RowData) pruneset, getClusteringWeights());
				if (pm == Settings.PRUNING_METHOD_GAROFALAKIS_VSB) {
					int maxsize = sett.getMaxSize();
					pruner.setSequencePruner(new SizeConstraintPruning(maxsize,	getClusteringWeights()));
				} else {
					pruner.setSequencePruner(new CartPruning(getClusteringWeights(), sett.isMSENominal()));
				}
				pruner.setOutputFile(sett.getFileAbsolute("prune.dat"));
				pruner.set1SERule(sett.get1SERule());
				pruner.setHasMissing(m_Schema.hasMissing());
				return pruner;
			} else if (pm == Settings.PRUNING_METHOD_REDERR_VSB || pm == Settings.PRUNING_METHOD_DEFAULT) {
				ClusErrorList parent = createEvalError();
				sett.setPruningMethod(Settings.PRUNING_METHOD_REDERR_VSB);
				return new BottomUpPruningVSB(parent, (RowData) pruneset);
			} else {
				return getTreePrunerNoVSB();
			}
		} else {
			return getTreePrunerNoVSB();
		}
	}

	public void setTargetStatistic(ClusStatistic stat) {
		// System.out.println("Setting target statistic: " + stat.getClass().getName());
		m_StatisticAttrUse[ClusAttrType.ATTR_USE_TARGET] = stat;
	}

	public void setClusteringStatistic(ClusStatistic stat) {
		// System.out.println("Setting clustering statistic: " + stat.getClass().getName());
		m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] = stat;
	}

	public boolean hasClusteringStat() {
		return m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] != null;
	}

	public ClusStatistic createClusteringStat() {
		return m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING].cloneStat();
	}

	public ClusStatistic createTargetStat() {
		return m_StatisticAttrUse[ClusAttrType.ATTR_USE_TARGET].cloneStat();
	}

	/**
	 * @param attType
	 *            attribute use type (eg., ClusAttrType.ATTR_USE_TARGET)
	 * @return the statistic
	 */
	public ClusStatistic createStatistic(int attType) {
		return m_StatisticAttrUse[attType].cloneStat();
	}

	/**
	 *
	 * @param attType attribute use type (eg., ClusAttrType.ATTR_USE_TARGET)
	 * @return The statistic
	 */
	public ClusStatistic getStatistic(int attType) {
		return m_StatisticAttrUse[attType];
	}

	public ClusStatistic getTrainSetStat() {
		return getTrainSetStat(ClusAttrType.ATTR_USE_ALL);
	}

	public ClusStatistic getTrainSetStat(int attType) {
		return m_TrainSetStatAttrUse[attType];
	}

	public void computeTrainSetStat(RowData trainset, int attType) {
		m_TrainSetStatAttrUse[attType] = createStatistic(attType);
		trainset.calcTotalStatBitVector(m_TrainSetStatAttrUse[attType]);
		m_TrainSetStatAttrUse[attType].calcMean();
	}

	public void computeTrainSetStat(RowData trainset) {
		m_TrainSetStatAttrUse = new ClusStatistic[ClusAttrType.NB_ATTR_USE];
		if (getMode() != MODE_HIERARCHICAL) computeTrainSetStat(trainset, ClusAttrType.ATTR_USE_ALL);
		computeTrainSetStat(trainset, ClusAttrType.ATTR_USE_CLUSTERING);
		computeTrainSetStat(trainset, ClusAttrType.ATTR_USE_TARGET);
	}

	public ClusHeuristic getHeuristic() {
		return m_Heuristic;
	}

	public String getHeuristicName() {
		return m_Heuristic.getName();
	}

	public void getPreprocs(DataPreprocs pps) {
	}

	public boolean needsHierarchyProcessors() {
		if (m_Mode == MODE_SSPD)
			return false;
		else
			return true;
	}

	public void setRuleInduceOnly(boolean rule) {
		m_RuleInduceOnly = rule;
	}

	public boolean isRuleInduceOnly() {
		return m_RuleInduceOnly;
	}

	/** Often Tree to Rule is a exception for isRuleInduceOnly */
	public boolean isTreeToRuleInduce() {
		return getSettings().getCoveringMethod() == Settings.COVERING_METHOD_RULES_FROM_TREE;
	}

	public void setBeamSearch(boolean beam) {
		m_BeamSearch = beam;
	}

	public boolean isBeamSearch() {
		return m_BeamSearch;
	}

	/**
	 * @return Returns the ChiSquare inverse probability for specified
	 *         significance level and degrees of freedom.
	 */
	public double getChiSquareInvProb(int df) {
		return m_ChiSquareInvProb[df];
	}

	public void updateStatistics(ClusModel model) throws ClusException {
		if (m_Hier != null) {
			ArrayList stats = new ArrayList();
			model.retrieveStatistics(stats);
			for (int i = 0; i < stats.size(); i++) {
				WHTDStatistic stat = (WHTDStatistic) stats.get(i);
				stat.setHier(m_Hier);
			}
		}
	}

	private void createHierarchy() {
		int idx = 0;
		for (int i = 0; i < m_Schema.getNbAttributes(); i++) {
			ClusAttrType type = m_Schema.getAttrType(i);
			if (!type.isDisabled() && type instanceof ClassesAttrType) {
				ClassesAttrType cltype = (ClassesAttrType) type;
				System.out.println("Classes type: " + type.getName());
				m_Hier = cltype.getHier();
				idx++;
			}
		}
	}

	public void initHierarchySettings() throws ClusException, IOException {
		if (m_Hier != null) {
			if (getSettings().hasHierEvalClasses()) {
				ClassesTuple tuple = ClassesTuple.readFromFile(getSettings()
						.getHierEvalClasses(), m_Hier);
				m_Hier.setEvalClasses(tuple);
			}
		}
	}

	/**
	 * Initializes/checks/overrides some inter-dependent settings for rule
	 * induction.
	 *
	 * @throws ClusException
	 *
	 */
	public void initRuleSettings() throws ClusException {
		Settings sett = getSettings();
		int covering = sett.getCoveringMethod();
		int prediction = sett.getRulePredictionMethod();
		// General
		if (((sett.getHeuristic() != Settings.HEURISTIC_DISPERSION_ADT)
				|| (sett.getHeuristic() != Settings.HEURISTIC_DISPERSION_MLT)
				|| (sett.getHeuristic() != Settings.HEURISTIC_R_DISPERSION_ADT) || (sett.getHeuristic() != Settings.HEURISTIC_R_DISPERSION_MLT))
				&& sett.isHeurRuleDist()) {
			sett.setHeurRuleDistPar(0.0);
		}
		if (sett.isRuleSignificanceTesting()) {
			Settings.IS_RULE_SIG_TESTING = true;
			// Is this faster than calling isRuleSignificanceTesting()
			// from Dispersion heuristic each time?
		}
		// Random rules
		if (sett.isRandomRules()) {
			sett.setCoveringMethod(Settings.COVERING_METHOD_STANDARD);
			// sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_DECISION_LIST);
			sett.setCoveringWeight(0);
			// Ordered rules
		} else if (covering == Settings.COVERING_METHOD_STANDARD) {
			// sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_DECISION_LIST);
			sett.setCoveringWeight(0);
			// Unordered rules - Heuristic covering
		} else if (covering == Settings.COVERING_METHOD_HEURISTIC_ONLY) {
			if ((prediction == Settings.RULE_PREDICTION_METHOD_DECISION_LIST)
					|| (prediction == Settings.RULE_PREDICTION_METHOD_UNION)) {
				sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED);
			}
			sett.setCoveringWeight(0.0);
			if (getSettings().getHeurRuleDistPar() < 0) {
				throw new ClusException("Clus heuristic covering: HeurRuleDistPar must be >= 0!");
			}
			if ((sett.getHeuristic() != Settings.HEURISTIC_DISPERSION_ADT)
					|| (sett.getHeuristic() != Settings.HEURISTIC_DISPERSION_MLT)
					|| (sett.getHeuristic() != Settings.HEURISTIC_R_DISPERSION_ADT)
					|| (sett.getHeuristic() != Settings.HEURISTIC_R_DISPERSION_MLT)) {
				throw new ClusException("Clus heuristic covering: Only dispersion-based heuristics supported!");
			}
			// Unordered rules - Weighted coverings
		} else if ((covering == Settings.COVERING_METHOD_WEIGHTED_ADDITIVE)
				|| (covering == Settings.COVERING_METHOD_WEIGHTED_MULTIPLICATIVE)
				|| (covering == Settings.COVERING_METHOD_WEIGHTED_ERROR)
				|| (covering == Settings.COVERING_METHOD_BEAM_RULE_DEF_SET)
				|| (covering == Settings.COVERING_METHOD_RANDOM_RULE_SET)) {
			if ((prediction == Settings.RULE_PREDICTION_METHOD_DECISION_LIST)
					|| (prediction == Settings.RULE_PREDICTION_METHOD_UNION)) {
				sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED);
			}
			if (sett.getCoveringWeight() < 0) {
				throw new ClusException("Clus weighted covering: Covering weight must be >= 0!");
			}
			// Rule induction from bootstrap sampled data, optimized ...
		} else if (covering == Settings.COVERING_METHOD_STANDARD_BOOTSTRAP) {
			sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_OPTIMIZED);
			// Multi-label classification
		} else if (covering == Settings.COVERING_METHOD_UNION) {
			sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_UNION);
		} else if (covering == Settings.COVERING_METHOD_RULES_FROM_TREE) {
			sett.setHeuristic(Settings.HEURISTIC_VARIANCE_REDUCTION);
			sett.setRuleAddingMethod(Settings.RULE_ADDING_METHOD_ALWAYS);		
		}
	}
}

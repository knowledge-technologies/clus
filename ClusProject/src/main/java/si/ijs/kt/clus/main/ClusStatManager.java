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

package si.ijs.kt.clus.main;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicDispersionAdt;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicDispersionMlt;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicError;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicMEstimate;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicRDispersionAdt;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicRDispersionMlt;
import si.ijs.kt.clus.algo.rules.ClusRuleHeuristicSSD;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.attweights.ClusNormalizedAttributeWeights;
import si.ijs.kt.clus.data.rows.DataPreprocs;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.data.type.complex.TupleAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.data.type.primitive.TimeSeriesAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.sets.EuclideanDistance;
import si.ijs.kt.clus.distance.complex.sets.GSMDistance;
import si.ijs.kt.clus.distance.complex.sets.HammingLossDistance;
import si.ijs.kt.clus.distance.complex.sets.JaccardDistance;
import si.ijs.kt.clus.distance.primitive.nominal.NominalDistance;
import si.ijs.kt.clus.distance.primitive.timeseries.DTWTimeSeriesDist;
import si.ijs.kt.clus.distance.primitive.timeseries.QDMTimeSeriesDist;
import si.ijs.kt.clus.distance.primitive.timeseries.TSCTimeSeriesDist;
import si.ijs.kt.clus.distance.primitive.timeseries.TimeSeriesDist;
import si.ijs.kt.clus.error.AbsoluteError;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.AvgDistancesError;
import si.ijs.kt.clus.error.ContingencyTable;
import si.ijs.kt.clus.error.ICVPairwiseDistancesError;
import si.ijs.kt.clus.error.MSError;
import si.ijs.kt.clus.error.MSNominalError;
import si.ijs.kt.clus.error.MisclassificationError;
import si.ijs.kt.clus.error.PearsonCorrelation;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.RRMSError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.multiscore.MultiScore;
import si.ijs.kt.clus.error.mlc.AveragePrecision;
import si.ijs.kt.clus.error.mlc.Coverage;
import si.ijs.kt.clus.error.mlc.HammingLoss;
import si.ijs.kt.clus.error.mlc.MLAccuracy;
import si.ijs.kt.clus.error.mlc.MLFOneMeasure;
import si.ijs.kt.clus.error.mlc.MLPrecision;
import si.ijs.kt.clus.error.mlc.MLRecall;
import si.ijs.kt.clus.error.mlc.MLaverageAUPRC;
import si.ijs.kt.clus.error.mlc.MLaverageAUROC;
import si.ijs.kt.clus.error.mlc.MLpooledAUPRC;
import si.ijs.kt.clus.error.mlc.MLweightedAUPRC;
import si.ijs.kt.clus.error.mlc.MacroFOne;
import si.ijs.kt.clus.error.mlc.MacroPrecision;
import si.ijs.kt.clus.error.mlc.MacroRecall;
import si.ijs.kt.clus.error.mlc.MicroFOne;
import si.ijs.kt.clus.error.mlc.MicroPrecision;
import si.ijs.kt.clus.error.mlc.MicroRecall;
import si.ijs.kt.clus.error.mlc.OneError;
import si.ijs.kt.clus.error.mlc.RankingLoss;
import si.ijs.kt.clus.error.mlc.SubsetAccuracy;
import si.ijs.kt.clus.error.mlcForHmlc.MlcMeasuresForHmlc;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamHeuristicError;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamHeuristicMEstimate;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamHeuristicMorishita;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamHeuristicSS;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamSimRegrStat;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassesAttrType;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.ext.hierarchical.ClusRuleHeuristicHierarchical;
import si.ijs.kt.clus.ext.hierarchical.HierClassTresholdPruner;
import si.ijs.kt.clus.ext.hierarchical.HierClassWiseAccuracy;
import si.ijs.kt.clus.ext.hierarchical.HierErrorMeasures;
import si.ijs.kt.clus.ext.hierarchical.HierJaccardDistance;
import si.ijs.kt.clus.ext.hierarchical.HierRemoveInsigClasses;
import si.ijs.kt.clus.ext.hierarchical.HierSingleLabelStat;
import si.ijs.kt.clus.ext.hierarchical.HierSumPairwiseDistancesStat;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import si.ijs.kt.clus.ext.ilevelc.ILevelCRandIndex;
import si.ijs.kt.clus.ext.ilevelc.ILevelCStatistic;
import si.ijs.kt.clus.ext.semisupervised.Helper;
import si.ijs.kt.clus.ext.semisupervised.ModifiedGainHeuristic;
import si.ijs.kt.clus.ext.semisupervised.SemiSupMinLabeledWeightStopCrit;
import si.ijs.kt.clus.ext.sspd.SSPDMatrix;
import si.ijs.kt.clus.ext.structuredTypes.SetStatistic;
import si.ijs.kt.clus.ext.structuredTypes.TupleStatistic;
import si.ijs.kt.clus.ext.timeseries.TimeSeriesSignificantChangeTesterXVAL;
import si.ijs.kt.clus.ext.timeseries.TimeSeriesStat;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.heuristic.ClusStopCriterion;
import si.ijs.kt.clus.heuristic.ClusStopCriterionMinNbExamples;
import si.ijs.kt.clus.heuristic.ClusStopCriterionMinWeight;
import si.ijs.kt.clus.heuristic.GISHeuristic;
import si.ijs.kt.clus.heuristic.GainHeuristic;
import si.ijs.kt.clus.heuristic.GeneticDistanceHeuristicMatrix;
import si.ijs.kt.clus.heuristic.ReducedErrorHeuristic;
import si.ijs.kt.clus.heuristic.VarianceReductionHeuristic;
import si.ijs.kt.clus.heuristic.VarianceReductionHeuristicCompatibility;
import si.ijs.kt.clus.heuristic.VarianceReductionHeuristicEfficient;
import si.ijs.kt.clus.heuristic.VarianceReductionHeuristicInclMissingValues;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsAttribute;
import si.ijs.kt.clus.main.settings.SettingsBeamSearch;
import si.ijs.kt.clus.main.settings.SettingsConstraints;
import si.ijs.kt.clus.main.settings.SettingsGeneral;
import si.ijs.kt.clus.main.settings.SettingsHMLC;
import si.ijs.kt.clus.main.settings.SettingsHMTR;
import si.ijs.kt.clus.main.settings.SettingsRules;
import si.ijs.kt.clus.main.settings.SettingsTimeSeries;
import si.ijs.kt.clus.main.settings.SettingsTree;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.pruning.BottomUpPruningVSB;
import si.ijs.kt.clus.pruning.C45Pruner;
import si.ijs.kt.clus.pruning.CartPruning;
import si.ijs.kt.clus.pruning.EncodingCostPruning;
import si.ijs.kt.clus.pruning.M5Pruner;
import si.ijs.kt.clus.pruning.M5PrunerMulti;
import si.ijs.kt.clus.pruning.PruneTree;
import si.ijs.kt.clus.pruning.SequencePruningVSB;
import si.ijs.kt.clus.pruning.SizeConstraintPruning;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.CombStat;
import si.ijs.kt.clus.statistic.CombStatClassRegHier;
import si.ijs.kt.clus.statistic.GeneticDistanceStat;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.statistic.SumPairwiseDistancesStat;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;


/**
 * Statistics manager
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

    public final static int MODE_HIERARCHICAL_MTR = 9;

    public final static int MODE_STRUCTURED = 10;

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

    protected ClusHMTRHierarchy m_HMTRHier;

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
        return getSettings().getGeneral().getCompatibility();
    }


    public final ClusSchema getSchema() {
        return m_Schema;
    }


    public final int getMode() {
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
            double target_weight = winfo.getDouble(SettingsAttribute.TARGET_WEIGHT);
            double non_target_weight = winfo.getDouble(SettingsAttribute.NON_TARGET_WEIGHT);
            double num_weight = winfo.getDouble(SettingsAttribute.NUMERIC_WEIGHT);
            double nom_weight = winfo.getDouble(SettingsAttribute.NOMINAL_WEIGHT);
            if (getSettings().getGeneral().getVerbose() >= 2) {
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
        }
        else if (winfo.isVector()) {
            // Explicit vector of weights given
            if (getSettings().getHMLC().isHierAndClassAndReg()) {
                int l = winfo.getVectorLength();
                if (l + m_Hier.getTotal() != nbattr) { throw new ClusException("Number of attributes is " + (nbattr - m_Hier.getTotal())
                        + " but weight vector has only "
                        + l + " components"); }
                for (int i = 0; i < l - 1; i++) { //the last attribute is dummy, i.e., it represents the hierarchy, so we skip it 
                    result.setWeight(i, winfo.getDouble(i));
                }
                for (int i = winfo.getVectorLength(); i < nbattr; i++) {
                    result.setWeight(i, winfo.getDouble(l - 1));
                }

            }
            else {
                if (nbattr != winfo.getVectorLength()) { throw new ClusException("Number of attributes is " + nbattr + " but weight vector has only " + winfo.getVectorLength() + " components"); }
                for (int i = 0; i < nbattr; i++) {
                    result.setWeight(i, winfo.getDouble(i));
                }
            }
        }
        else {
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
            if (sum <= 0) { throw new ClusException("initWeights(): Sum of clustering/dispersion weights must be > 0!"); }
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
        initWeights(m_DispersionWeights, num, nom, getSettings().getRules().getDispersionWeights());
        if (getSettings().getGeneral().getVerbose() >= 1 && (isRuleInduceOnly() || isTreeToRuleInduce()) && getSettings().getRules().computeDispersion()) {
            System.out.println("Dispersion:   " + m_DispersionWeights.getName(m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_ALL)));
        }
    }


    public void initClusteringWeights() throws ClusException {
        if (getMode() == MODE_HIERARCHICAL) {
            int nb_attrs = m_Schema.getNbAttributes();

            double[] weights = m_Hier.getWeights();
            NumericAttrType[] dummy = m_Hier.getDummyAttrs();

            double[] temp = null;
            if (getSettings().getHMLC().isHierAndClassAndReg()) {
                temp = ((ClusNormalizedAttributeWeights) m_ClusteringWeights).getNormalizationWeights();
                double sum = Helper.sum(weights);
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = weights[i] / sum; //normalization with sum of all weights, as Alja� suggested
                }
            }

            m_ClusteringWeights = new ClusAttributeWeights(nb_attrs + m_Hier.getTotal());

            for (int i = 0; i < weights.length; i++) {
                m_ClusteringWeights.setWeight(dummy[i], weights[i]);
            }
            if (getSettings().getHMLC().isHierAndClassAndReg()) {
                for (int i = 0; i < temp.length - 1; i++) {
                    m_ClusteringWeights.setWeight(i, temp[i]);
                }

                m_ClusteringWeights = new ClusNormalizedAttributeWeights(m_ClusteringWeights);
            }
            else {
                return;
            }
        }
        NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
        NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);

        initWeights((ClusNormalizedAttributeWeights) m_ClusteringWeights, num, nom, getSettings().getAttribute().getClusteringWeights());
        if (getSettings().getGeneral().getVerbose() > 1) {

            /*
             * if(getSettings().isSSLWeights()) { this shuold be automatically enabled
             * ClusAttrType[] clustering = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
             * double weight;
             * for (int i = 0; i < clustering.length; i++) {
             * ClusAttrType cr = clustering[i];
             * weight = ((ClusNormalizedAttributeWeights) m_ClusteringWeights).getComposeWeight(cr);
             * double nbAtts = m_Schema.getNbDescriptiveAttributes() + m_Schema.getNbTargetAttributes();
             * double sslweight = cr.getStatus() == ClusAttrType.STATUS_TARGET ? weight * ( nbAtts /
             * m_Schema.getNbTargetAttributes()) : weight * (nbAtts / m_Schema.getNbDescriptiveAttributes());
             * m_ClusteringWeights.setWeight(cr, sslweight);
             * }
             * }
             */
            System.out.println("Clustering: " + m_ClusteringWeights.getName(m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING)));
        }
    }


    /** Initializes normalization weights to m_NormalizationWeights variable */
    public void initNormalizationWeights(ClusStatistic stat, ClusData data) throws ClusException {
        int nbattr = m_Schema.getNbAttributes();
        m_NormalizationWeights.setAllWeights(1.0);
        boolean[] shouldNormalize = new boolean[nbattr];
        INIFileNominalOrDoubleOrVector winfo = getSettings().getAttribute().getNormalizationWeights();
        if (winfo.isVector()) {
            if (nbattr != winfo.getVectorLength()) { throw new ClusException("Number of attributes is " + nbattr + " but weight vector has only " + winfo.getVectorLength() + " components"); }
            for (int i = 0; i < nbattr; i++) {
                if (winfo.isNominal(i))
                    shouldNormalize[i] = true;
                else
                    m_NormalizationWeights.setWeight(i, winfo.getDouble(i));
            }
        }
        else {
            if (winfo.isNominal() && winfo.getNominal() == SettingsAttribute.NORMALIZATION_DEFAULT) {
                Arrays.fill(shouldNormalize, true);
            }
            else {
                m_NormalizationWeights.setAllWeights(winfo.getDouble());
            }
        }
        if (hasBitEqualToOne(shouldNormalize)) {
            //            //daniela
            //            if (m_Mode == MODE_HIERARCHICAL) {
            //                WHTDStatistic tstat = (WHTDStatistic)createStatistic(ClusAttrType.ATTR_USE_TARGET);
            //                tstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
            //            }
            //            //end daniela
            data.calcTotalStat(stat);
            CombStat cmb = (CombStat) stat;
            // data.calcTotalStat(stat); // why is this here? this duplicates weights etc for no apparent reason
            RegressionStat rstat = cmb.getRegressionStat();
            rstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
            // Normalization is currently required for trees but not for rules
            if (!isRuleInduceOnly()) {
                ClassificationStat cstat = cmb.getClassificationStat();
                cstat.initNormalizationWeights(m_NormalizationWeights, shouldNormalize);
            }
            if (m_Mode == MODE_TIME_SERIES) {
                TimeSeriesStat tstat = (TimeSeriesStat) createStatistic(ClusAttrType.ATTR_USE_TARGET);
                ((RowData) data).calcTotalStatBitVector(tstat);
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
        System.out.println("Clustering attributes check ==> #nominal: " + nb_nom + " #numeric: " + nb_num);

        if (m_Schema.hasAttributeType(ClusAttrType.ATTR_USE_TARGET, ClassesAttrType.THIS_TYPE)) {
            m_Mode = MODE_HIERARCHICAL;
            getSettings().getHMLC().setSectionHierarchicalEnabled(true);
            nb_types++;
            if (nb_nom > 0 || nb_num > 0) {
                getSettings().getHMLC().setIsHierAndClassAndReg(true); //FIXME: maybe new MODE should be introduced here?
            }
        }
        else {
            if (nb_nom > 0 && nb_num > 0) {
                m_Mode = MODE_CLASSIFY_AND_REGRESSION;
                nb_types++;
            }
            else if (nb_nom > 0) {
                m_Mode = MODE_CLASSIFY;
                nb_types++;
            }
            else if (nb_num > 0) {
                m_Mode = MODE_REGRESSION;
                nb_types++;
            }
        }

        NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
        NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
        TimeSeriesAttrType[] ts = m_Schema.getTimeSeriesAttrUse(ClusAttrType.ATTR_USE_TARGET);
        boolean is_multilabel = num.length == 0 && ts.length == 0 && nom.length > 1;
        if (is_multilabel) {
            String[] twoLabels = new String[] { "1", "0" }; // Clus saves the values of @attribute atrName {1,0}/{0,1} to {"1", "0"}.
            for (int attr = 0; attr < nom.length; attr++) {
                if (!Arrays.equals(nom[attr].m_Values, twoLabels)) {
                    is_multilabel = false;
                    break;
                }
            }
        }
        getSettings().getMLC().setSectionMultiLabelEnabled(is_multilabel);

        int nb_int = 0;
        if (nb_int > 0 || getSettings().getTree().checkHeuristic("SSPD")) {
            m_Mode = MODE_SSPD;
            nb_types++;
        }
        if (getSettings().getTree().checkHeuristic("GeneticDistance")) {
            m_Mode = MODE_PHYLO;
        }
        if (getSettings().getTimeSeries().isSectionTimeSeriesEnabled()) {
            m_Mode = MODE_TIME_SERIES;
            nb_types++;
        }

        if (getSettings().getTree().checkHeuristic("StructuredData")) {
            m_Mode = MODE_STRUCTURED;
            nb_types++;
        }

        if (getSettings().getILevelC().isSectionILevelCEnabled()) {
            m_Mode = MODE_ILEVELC;
        }

        if (getSettings().getBeamSearch().isBeamSearchMode() && (getSettings().getBeamSearch().getBeamSimilarity() != 0.0)) {
            m_Mode = MODE_BEAM_SEARCH;
        }

        if (getSettings().getHMTR().isSectionHMTREnabled()) {
            m_Mode = MODE_HIERARCHICAL_MTR;
        }

        if (nb_types == 0) {
            System.err.println("No target value defined");
        }
        if (nb_types > 1) {
            if (!getSettings().getRelief().isRelief()) { throw new ClusException("Incompatible combination of clustering attribute types"); }

        }
    }


    public void initStructure() throws IOException {
        switch (m_Mode) {
            case MODE_HIERARCHICAL:
                createHierarchy();
                break;
            case MODE_SSPD:
                m_SSPDMtrx = SSPDMatrix.read(getSettings().getGeneric().getFileAbsolute(getSettings().getGeneric().getAppName() + ".dist"), getSettings());
                break;
        }
    }


    public ClusStatistic createSuitableStat(NumericAttrType[] num, NominalAttrType[] nom) {
        if (num.length == 0) {
            if (m_Mode == MODE_PHYLO) {
                // switch (Settings.m_PhylogenyProtoComlexity.getValue()) {
                // case Settings.PHYLOGENY_PROTOTYPE_COMPLEXITY_PAIRWISE:
                return new GeneticDistanceStat(getSettings(), nom);
                // case Settings.PHYLOGENY_PROTOTYPE_COMPLEXITY_PROTO:
                // return new ClassificationStat(nom);
                // }
            }
            if (getSettings().getMLC().getSectionMultiLabel().isEnabled()) {
                return new ClassificationStat(getSettings(), nom, getSettings().getMLC().getMultiLabelThreshold());
            }
            else {
                return new ClassificationStat(getSettings(), nom);
            }
        }
        else if (nom.length == 0) {
            return new RegressionStat(getSettings(), num);
        }
        else {
            return new CombStat(this, num, nom);
        }
    }


    public boolean heuristicNeedsCombStat() {
        if (isRuleInduceOnly()) {
            // if (m_Mode == MODE_HIERARCHICAL) {
            // return false;
            // }
            return (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_DEFAULT
                    || getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_DISPERSION_ADT
                    || getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_DISPERSION_MLT
                    || getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_R_DISPERSION_ADT
                    || getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_R_DISPERSION_MLT);
        }
        else {
            return false;
        }
    }


    public synchronized void initStatistic() throws ClusException {
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
            }
            else {
                m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] = createSuitableStat(num3, nom3);
            }
        }
        switch (m_Mode) {
            case MODE_HIERARCHICAL:

                WHTDStatistic clustering;
                if (getSettings().getHMLC().getHierDistance() == SettingsHMLC.HIERDIST_NO_DIST) { // poolAUPRC induction
                    //setClusteringStatistic(new WHTDStatistic(getSettings(), m_Hier, getCompatibility(), getSettings().getHMLC().getHierDistance()));
                    clustering = new WHTDStatistic(getSettings(), m_Hier, getCompatibility(), getSettings().getHMLC().getHierDistance());
                    setTargetStatistic(new WHTDStatistic(getSettings(), m_Hier, getCompatibility(), getSettings().getHMLC().getHierDistance()));
                }
                else {
                    if (getSettings().getHMLC().getHierDistance() == SettingsHMLC.HIERDIST_WEIGHTED_EUCLIDEAN) {
                        if (getSettings().getHMLC().getHierSingleLabel()) {
                            //setClusteringStatistic(new HierSingleLabelStat(getSettings(), m_Hier, getCompatibility()));
                            clustering = new HierSingleLabelStat(getSettings(), m_Hier, getCompatibility());
                            setTargetStatistic(new HierSingleLabelStat(getSettings(), m_Hier, getCompatibility()));
                        }
                        else {
                            //setClusteringStatistic(new WHTDStatistic(getSettings(), m_Hier, getCompatibility()));
                            clustering = new WHTDStatistic(getSettings(), m_Hier, getCompatibility());
                            setTargetStatistic(new WHTDStatistic(getSettings(), m_Hier, getCompatibility()));
                        }
                    }
                    else {
                        ClusDistance dist = null;
                        if (getSettings().getHMLC().getHierDistance() == SettingsHMLC.HIERDIST_JACCARD) {
                            dist = new HierJaccardDistance(m_Hier.getType());
                        }

                        //setClusteringStatistic(new HierSumPairwiseDistancesStat(getSettings(), m_Hier, dist, getCompatibility()));
                        clustering = new HierSumPairwiseDistancesStat(getSettings(), m_Hier, dist, getCompatibility());
                        setTargetStatistic(new HierSumPairwiseDistancesStat(getSettings(), m_Hier, dist, getCompatibility()));
                    }
                }
                if (getSettings().getHMLC().isHierAndClassAndReg()) {
                    setClusteringStatistic(new CombStatClassRegHier(this, num3, nom3, clustering));
                    m_StatisticAttrUse[ClusAttrType.ATTR_USE_ALL] = new CombStatClassRegHier(this, num3, nom3, (WHTDStatistic) clustering.cloneStat());
                }
                else {
                    setClusteringStatistic(clustering);
                }
                break;
            case MODE_HIERARCHICAL_MTR:
                if (getSettings().getHMTR().getHMTRDistance().getValue() == SettingsHMTR.HMTR_HIERDIST_WEIGHTED_EUCLIDEAN) {
                    if (getSettings().getGeneral().getVerbose() > 0)
                        System.out.println("HMTR - Euclidean distance");
                }
                else if (getSettings().getHMTR().getHMTRDistance().getValue() == SettingsHMTR.HMTR_HIERDIST_JACCARD) {
                    if (getSettings().getGeneral().getVerbose() > 0)
                        System.out.println("HMTR - Jaccard distance");
                }
                m_HMTRHier = m_Schema.getHMTRHierarchy();
                setTargetStatistic(new RegressionStat(getSettings(), num2, m_HMTRHier));
                setClusteringStatistic(new RegressionStat(getSettings(), num3, m_HMTRHier));

                break;
            case MODE_SSPD:
                ClusAttrType[] target = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
                m_SSPDMtrx.setTarget(target);
                setClusteringStatistic(new SumPairwiseDistancesStat(getSettings(), m_SSPDMtrx, 3));
                setTargetStatistic(new SumPairwiseDistancesStat(getSettings(), m_SSPDMtrx, 3));
                break;
            case MODE_TIME_SERIES:
                ClusAttrType[] targets = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
                TimeSeriesAttrType type = (TimeSeriesAttrType) targets[0];
                int efficiency = getSettings().getTimeSeries().getTimeSeriesHeuristicSampling();
                switch (getSettings().getTimeSeries().getTimeSeriesDistance()) {
                    case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_DTW:
                        TimeSeriesDist dist = new DTWTimeSeriesDist(type);
                        setClusteringStatistic(new TimeSeriesStat(getSettings(), type, dist, efficiency));
                        setTargetStatistic(new TimeSeriesStat(getSettings(), type, dist, efficiency));
                        break;
                    case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_QDM:
                        if (type.isEqualLength()) {
                            TimeSeriesDist qdm = new QDMTimeSeriesDist(type);
                            setClusteringStatistic(new TimeSeriesStat(getSettings(), type, qdm, efficiency));
                            setTargetStatistic(new TimeSeriesStat(getSettings(), type, qdm, efficiency));
                        }
                        else {
                            throw new ClusException("QDM Distance is not implemented for time series with different length");
                        }
                        break;
                    case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_TSC:
                        TimeSeriesDist tsc = new TSCTimeSeriesDist(type);
                        setClusteringStatistic(new TimeSeriesStat(getSettings(), type, tsc, efficiency));
                        setTargetStatistic(new TimeSeriesStat(getSettings(), type, tsc, efficiency));
                        break;
                }
                break;
            case MODE_STRUCTURED:
                efficiency = getSettings().getTree().getHeuristicComplexity();
                ClusAttrType[] structured_target = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
                try {
                    SetAttrType myType = (SetAttrType) structured_target[0];

                    ClusDistance d = getInnerDistanceForType(myType.getTypeDefinition());
                    ClusDistance setDistance = null;
                    switch (m_Settings.getTree().getSetDistance()) {
                        case SettingsTree.SETDISTANCES_GSM:
                            setDistance = new GSMDistance(myType, d, 1);
                            break;
                        case SettingsTree.SETDISTANCES_EUCLIDEAN:
                            setDistance = new EuclideanDistance(myType, d);
                            break;

                        case SettingsTree.SETDISTANCES_HAMMING:
                            setDistance = new HammingLossDistance(myType, d);
                            break;
                        case SettingsTree.SETDISTANCES_JACCARD:
                            setDistance = new JaccardDistance(myType, d);
                            break;
                    }
                    setClusteringStatistic(new SetStatistic(getSettings(), myType, setDistance, efficiency));
                    setTargetStatistic(new SetStatistic(getSettings(), myType, setDistance, efficiency));
                }
                catch (Exception e) {
                    try {

                        TupleAttrType myType = (TupleAttrType) structured_target[0];
                        ClusDistance tupleDistance = ClusAttrType.createDistance(myType, m_Settings);
                        setClusteringStatistic(new TupleStatistic(getSettings(), myType, tupleDistance, efficiency));
                        setTargetStatistic(new TupleStatistic(getSettings(), myType, tupleDistance, efficiency));
                    }
                    catch (Exception e1) {
                        try {
                            TimeSeriesAttrType myType = (TimeSeriesAttrType) structured_target[0];
                            ClusDistance tupleDistance = ClusAttrType.createDistance(myType, m_Settings);
                            setClusteringStatistic(new TimeSeriesStat(getSettings(), myType, tupleDistance, efficiency));
                            setTargetStatistic(new TimeSeriesStat(getSettings(), myType, tupleDistance, efficiency));

                        }
                        catch (Exception e2) {

                            e2.printStackTrace();
                            throw new ClusException(e2.getMessage());
                        }
                    }
                }
                //TimeSeriesAttrType type = (TimeSeriesAttrType)structured_tagget[0];
                //int efficiency = getSettings().m_TimeSeriesHeuristicSampling.getValue();          
                break;
            case MODE_ILEVELC:
                setTargetStatistic(new ILevelCStatistic(getSettings(), num2));
                setClusteringStatistic(new ILevelCStatistic(getSettings(), num3));
                break;
            case MODE_BEAM_SEARCH:
                if (num3.length != 0 && num2.length != 0) {
                    setTargetStatistic(new ClusBeamSimRegrStat(getSettings(), num2, null));
                    setClusteringStatistic(new ClusBeamSimRegrStat(getSettings(), num3, null));
                }
                break;
        }
    }


    /*
     * FIXME
     * this is pretty unfinished and buggy method - do not use it
     * TODO: yes, your fan base will probably decrease because of this one. (=this should be reviewed; martinb)
     */
    private ClusDistance getInnerDistanceForType(String myType) {

        if (myType.startsWith("SET{")) {
            String def = myType;
            //  String innerType = "";
            int open = 0;
            int start = 0, end = 0;
            def = def.replace("SET{", "");
            def = def.substring(0, def.length() - 1);
            ClusDistance cd = null;
            if (def.equalsIgnoreCase("TIMESERIES")) {
                switch (m_Settings.getTimeSeries().getTimeSeriesDistance()) {
                    case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_DTW:
                        cd = new DTWTimeSeriesDist(null);
                        break;
                    case SettingsTimeSeries.TIME_SERIES_DISTANCE_MEASURE_QDM:
                        cd = new QDMTimeSeriesDist(null);
                        break;
                }
                return cd;
            }

            for (int i = 0; i < def.length(); i++) {
                if (def.charAt(i) == '[') {
                    start = i + 1;
                    open++;
                }
                else if (def.charAt(i) == ']') {
                    end = i;
                    open--;
                }
                //FIXME!
                if (open > 1 && !myType.contains("NOMINAL")) {
                    //complex type 
                    //TODO Implement
                    try {
                        throw new Exception("not implemented");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            // innerType = def.substring(start, end);
            if (myType.contains("NOMINAL")) { return new NominalDistance(); }
        }
        // TODO Auto-generated method stub
        return null;
    }


    public ClusHeuristic createHeuristic(int type) {
        switch (type) {
            case SettingsTree.HEURISTIC_GAIN:
                return new GainHeuristic(false, getClusteringWeights(), getSettings()); // matejp: daniela without the second argument
            default:
                return null;
        }
    }


    public void initRuleHeuristic() throws ClusException {
        if (m_Mode == MODE_CLASSIFY) {
            switch (getSettings().getTree().getHeuristic()) {
                case SettingsTree.HEURISTIC_DEFAULT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this, getClusteringWeights(), getSettings());
                    getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_R_DISPERSION_MLT);
                    break;
                case SettingsTree.HEURISTIC_REDUCED_ERROR:
                    m_Heuristic = new ClusRuleHeuristicError(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_MESTIMATE:
                    m_Heuristic = new ClusRuleHeuristicMEstimate(getSettings().getNominal().getMEstimate(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_DISPERSION_ADT:
                    m_Heuristic = new ClusRuleHeuristicDispersionAdt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_DISPERSION_MLT:
                    m_Heuristic = new ClusRuleHeuristicDispersionMlt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_R_DISPERSION_ADT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionAdt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_R_DISPERSION_MLT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this, getClusteringWeights(), getSettings());
                    break;
                default:
                    throw new ClusException("Unsupported heuristic for single target classification rules!");
            }
        }
        else if (m_Mode == MODE_REGRESSION || m_Mode == MODE_CLASSIFY_AND_REGRESSION) {
            switch (getSettings().getTree().getHeuristic()) {
                case SettingsTree.HEURISTIC_DEFAULT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_REDUCED_ERROR:
                    m_Heuristic = new ClusRuleHeuristicError(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_DISPERSION_ADT:
                    m_Heuristic = new ClusRuleHeuristicDispersionAdt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_DISPERSION_MLT:
                    m_Heuristic = new ClusRuleHeuristicDispersionMlt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_R_DISPERSION_ADT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionAdt(this, getClusteringWeights(), getSettings());
                    break;
                case SettingsTree.HEURISTIC_R_DISPERSION_MLT:
                    m_Heuristic = new ClusRuleHeuristicRDispersionMlt(this, getClusteringWeights(), getSettings());
                    break;
                default:
                    throw new ClusException("Unsupported heuristic for multiple target or regression rules!");
            }
        }
        else if (m_Mode == MODE_HIERARCHICAL) {
            m_Heuristic = new ClusRuleHeuristicHierarchical(this, getClusteringWeights(), getSettings());
            return;
            // getSettings().setHeuristic(Settings.HEURISTIC_SS_REDUCTION);

            /*
             * String name = "Weighted Hierarchical Tree Distance";
             * m_Heuristic = new ClusRuleHeuristicSSD(name, createClusteringStat(),getClusteringWeights());
             * getSettings().setHeuristic(Settings.HEURISTIC_SS_REDUCTION);
             * return;
             */
        }
        else if (m_Mode == MODE_TIME_SERIES) {
            String name = "Time Series Intra-Cluster Variation Heuristic for Rules";
            m_Heuristic = new ClusRuleHeuristicSSD(this, name, createClusteringStat(), getClusteringWeights(), getSettings());
            getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            return;
        }
        else if (m_Mode == MODE_ILEVELC) {
            String name = "Intra-Cluster Variation Heuristic for Rules";
            m_Heuristic = new ClusRuleHeuristicSSD(this, name, createClusteringStat(), getClusteringWeights(), getSettings());
        }
        else {
            throw new ClusException("Unsupported mode for rules!");
        }
    }


    public void initBeamSearchHeuristic() throws ClusException {
        if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_REDUCED_ERROR) {
            m_Heuristic = new ClusBeamHeuristicError(createClusteringStat(), getSettings());
        }
        else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_MESTIMATE) {
            m_Heuristic = new ClusBeamHeuristicMEstimate(createClusteringStat(), getSettings().getNominal().getMEstimate(), getSettings());
        }
        else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_MORISHITA) {
            m_Heuristic = new ClusBeamHeuristicMorishita(createClusteringStat(), getSettings());
        }
        else {
            m_Heuristic = new ClusBeamHeuristicSS(createClusteringStat(), getClusteringWeights(), getSettings());
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
            // daniela
            if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_VARIANCE_REDUCTION_GIS) {
                m_Heuristic = new GISHeuristic(getClusteringWeights(), m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), getSettings());
            } // daniela end
            else {
                if (getSettings().getGeneral().getCompatibility() <= SettingsGeneral.COMPATIBILITY_MLJ08) {
                    m_Heuristic = new VarianceReductionHeuristicCompatibility(createClusteringStat(), getClusteringWeights(), getSettings());
                }
                else {
                    m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), null, getSettings());
                }
                getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            }

            return;
        }
        if (m_Mode == MODE_SSPD) {
            ClusStatistic clusstat = createClusteringStat();
            m_Heuristic = new VarianceReductionHeuristic(clusstat.getDistanceName(), clusstat, getClusteringWeights(), getSettings());
            getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_SSPD);
            return;
        }
        if (m_Mode == MODE_TIME_SERIES) {
            ClusStatistic clusstat = createClusteringStat();
            m_Heuristic = new VarianceReductionHeuristic(clusstat.getDistanceName(), clusstat, getClusteringWeights(), getSettings());
            getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            return;
        }
        if (m_Mode == MODE_STRUCTURED) {
            ClusStatistic clusstat = createClusteringStat();
            m_Heuristic = new VarianceReductionHeuristic(clusstat.getDistanceName(), clusstat, getClusteringWeights(), getSettings());
            getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            return;
        }
        /* Set heuristic for trees */
        NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
        NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
        if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_SS_REDUCTION_MISSING) {
            m_Heuristic = new VarianceReductionHeuristicInclMissingValues(getClusteringWeights(), m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), createClusteringStat(), getSettings());
            return;
        }
        if (num.length > 0 && nom.length > 0) {
            if (getSettings().getTree().getHeuristic() != SettingsTree.HEURISTIC_DEFAULT && getSettings().getTree().getHeuristic() != SettingsTree.HEURISTIC_VARIANCE_REDUCTION) { throw new ClusException("Only SS-Reduction heuristic can be used for combined classification/regression trees!"); }
            m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), getSettings());
            getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
        }
        else if (num.length > 0) {
            ArrayList<Integer> allowedHeuristics = new ArrayList<Integer>();
            allowedHeuristics.add(SettingsTree.HEURISTIC_DEFAULT);
            allowedHeuristics.add(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            allowedHeuristics.add(SettingsTree.HEURISTIC_VARIANCE_REDUCTION_GIS); // daniela
            if (!allowedHeuristics.contains(getSettings().getTree().getHeuristic())) {
                throw new ClusException("Only SS-Reduction heuristic and GISHeuristic can be used for regression trees!"); // daniela partially
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_VARIANCE_REDUCTION_GIS) {
                m_Heuristic = new GISHeuristic(getClusteringWeights(), m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), getSettings()); // daniela

            }
            else {
                m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), getSettings());
                getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            }
        }
        else if (nom.length > 0) {
            if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_SEMI_SUPERVISED) {
                m_Heuristic = new ModifiedGainHeuristic(createClusteringStat(), getSettings());
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_REDUCED_ERROR) {
                m_Heuristic = new ReducedErrorHeuristic(createClusteringStat(), getSettings());
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_GENETIC_DISTANCE) {
                m_Heuristic = new GeneticDistanceHeuristicMatrix(getSettings());
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_VARIANCE_REDUCTION) {
                m_Heuristic = new VarianceReductionHeuristicEfficient(getClusteringWeights(), nom, getSettings());
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_VARIANCE_REDUCTION_GIS) {
                m_Heuristic = new GISHeuristic(getClusteringWeights(), m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING), getSettings());
            }
            else if (getSettings().getTree().getHeuristic() == SettingsTree.HEURISTIC_GAIN_RATIO) {
                m_Heuristic = new GainHeuristic(true, getClusteringWeights(), getSettings());
            }
            else {
                ArrayList<Integer> allowedHeuristics = new ArrayList<Integer>();
                allowedHeuristics.add(SettingsTree.HEURISTIC_DEFAULT);
                allowedHeuristics.add(SettingsTree.HEURISTIC_GAIN);
                allowedHeuristics.add(SettingsTree.HEURISTIC_GENETIC_DISTANCE);
                allowedHeuristics.add(SettingsTree.HEURISTIC_VARIANCE_REDUCTION_GIS); // daniela
                if (!allowedHeuristics.contains(getSettings().getTree().getHeuristic())) { throw new ClusException("Given heuristic not supported for classification trees!"); }
                m_Heuristic = new GainHeuristic(false, getClusteringWeights(), getSettings());
                getSettings().getTree().setHeuristic(SettingsTree.HEURISTIC_GAIN);
            }
        }
        else {}
    }


    public void initStopCriterion() {
        ClusStopCriterion stop = null;
        int minEx = getSettings().getModel().getMinimalNbExamples();
        double knownWeight = getSettings().getModel().getMinimalKnownWeight();
        if (minEx > 0) {
            stop = new ClusStopCriterionMinNbExamples(minEx);
        }
        else if (knownWeight > 0) {
            stop = new SemiSupMinLabeledWeightStopCrit(knownWeight);
        }
        else {
            double minW = getSettings().getModel().getMinimalWeight();
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
        table[0] = 1.0 - getSettings().getRules().getRuleSignificanceLevel();
        // Not really used except below
        for (int i = 1; i < table.length; i++) {
            //DistributionFactory distributionFactory = DistributionFactory.newInstance();
            //ChiSquaredDistribution chiSquaredDistribution = distributionFactory.createChiSquareDistribution(i);
            ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(i);
            
//            try {
                table[i] = chiSquaredDistribution.inverseCumulativeProbability(table[0]);
//            }
//            catch (MathException e) {
//                e.printStackTrace();
//            }
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
            parent.addError(new MSNominalError(parent, nom, m_NormalizationWeights));
        }
        if (getSettings().getMLC().getSectionMultiLabel().isEnabled()) {
            parent.addError(new HammingLoss(parent, nom));
            parent.addError(new RankingLoss(parent, nom));
            parent.addError(new OneError(parent, nom));
            parent.addError(new Coverage(parent, nom));
            parent.addError(new AveragePrecision(parent, nom));
            parent.addError(new MLAccuracy(parent, nom));
            parent.addError(new MLPrecision(parent, nom));
            parent.addError(new MLRecall(parent, nom));
            parent.addError(new MLFOneMeasure(parent, nom));
            parent.addError(new SubsetAccuracy(parent, nom));
            parent.addError(new MacroPrecision(parent, nom));
            parent.addError(new MacroRecall(parent, nom));
            parent.addError(new MacroFOne(parent, nom));
            parent.addError(new MicroPrecision(parent, nom));
            parent.addError(new MicroRecall(parent, nom));
            parent.addError(new MicroFOne(parent, nom));
            parent.addError(new MLaverageAUROC(parent, nom));
            parent.addError(new MLaverageAUPRC(parent, nom));
            parent.addError(new MLweightedAUPRC(parent, nom));
            parent.addError(new MLpooledAUPRC(parent, nom));
        }

        if (num.length != 0) {
            parent.addError(new AbsoluteError(parent, num));
            parent.addError(new MSError(parent, num));
            parent.addError(new RMSError(parent, num));
            if (getSettings().getAttribute().hasNonTrivialWeights()) {
                parent.addError(new RMSError(parent, num, m_NormalizationWeights));
            }
            parent.addError(new RRMSError(parent, num));
            parent.addError(new PearsonCorrelation(parent, num));
        }
        if (ts.length != 0) {
            ClusStatistic stat = createTargetStat();
            parent.addError(new AvgDistancesError(parent, stat.getDistance()));
        }
        switch (m_Mode) {
            case MODE_HIERARCHICAL:
                INIFileNominalOrDoubleOrVector class_thr = getSettings().getHMLC().getClassificationThresholds();
                if (class_thr.hasVector()) {
                    parent.addError(new HierClassWiseAccuracy(parent, m_Hier));
                }
                double[] recalls = getSettings().getHMLC().getRecallValues().getDoubleVector();
                boolean wrCurves = getSettings().getOutput().isWriteCurves();
                if (getSettings().getHMLC().isCalError()) {
                    parent.addError(new HierErrorMeasures(parent, m_Hier, recalls, getSettings().getGeneral().getCompatibility(), -1, wrCurves, getSettings().getOutput().isGzipOutput()));
                    parent.addError(new MlcMeasuresForHmlc(parent, m_Hier));
                }
                break;
            case MODE_ILEVELC:
                NominalAttrType cls = (NominalAttrType) getSchema().getLastNonDisabledType();
                parent.addError(new ILevelCRandIndex(parent, cls));
                break;

            case MODE_STRUCTURED:
                ClusAttrType[] target = m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
                //@FIXME - hardcoded - how do you know it is a set ... it should be general 
                try {
                    SetAttrType set = (SetAttrType) target[0];
                    SetAttrType[] sets = { set };
                    //@FIXME - this hardcoded nominal distance should be extracted from set (setattrtype)
                    //parent.addError(new HammingLossError(parent, sets, new NominalDistance(), set.getNumberOfPossibleValues()));
                    parent.addError(new si.ijs.kt.clus.error.sets.Accuracy(parent, sets, new NominalDistance()));
                    parent.addError(new si.ijs.kt.clus.error.sets.Precision(parent, sets, new NominalDistance()));
                    parent.addError(new si.ijs.kt.clus.error.sets.Recall(parent, sets, new NominalDistance()));
                    parent.addError(new si.ijs.kt.clus.error.sets.F1Score(parent, sets, new NominalDistance()));
                    parent.addError(new si.ijs.kt.clus.error.sets.SubsetAccuracy(parent, sets, new NominalDistance()));
                }
                catch (Exception e) {
                    //TupleAttrType tuple = (TupleAttrType)target[0];
                    //@FIXME
                    //TODO IMPLEMENT THIS!!!
                    /*
                     * parent.addError(new clus.error.sets.Accuracy(parent, sets, new NominalDistance()));
                     * parent.addError(new Precision(parent, sets, new NominalDistance()));
                     * parent.addError(new Recall(parent, sets, new NominalDistance()));
                     * parent.addError(new F1Score(parent, sets, new NominalDistance()));
                     * parent.addError(new SubsetAccuracy(parent, sets, new NominalDistance()));
                     */
                }
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
            parent.addError(new TimeSeriesSignificantChangeTesterXVAL(parent, (TimeSeriesStat) stat));
        }
        return parent;
    }


    public PruneTree getTreePrunerNoVSB() throws ClusException {
        SettingsTree sett = getSettings().getTree();
        SettingsBeamSearch setb = getSettings().getBeamSearch();
        SettingsConstraints setc = getSettings().getConstraints();

        if (isBeamSearch() && setb.isBeamPostPrune()) {
            sett.setPruningMethod(SettingsTree.PRUNING_METHOD_GAROFALAKIS);
            return new SizeConstraintPruning(setb.getBeamTreeMaxSize(), getClusteringWeights());
        }
        int err_nb = setc.getMaxErrorConstraintNumber();
        int size_nb = setc.getSizeConstraintPruningNumber();
        if (size_nb > 0 || err_nb > 0) {
            int[] sizes = setc.getSizeConstraintPruningVector();
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_CART_MAXSIZE) {
                return new CartPruning(sizes, getClusteringWeights());
            }
            else {
                sett.setPruningMethod(SettingsTree.PRUNING_METHOD_GAROFALAKIS);
                SizeConstraintPruning sc_prune = new SizeConstraintPruning(sizes, getClusteringWeights());
                if (err_nb > 0) {
                    double[] max_err = setc.getMaxErrorConstraintVector();
                    sc_prune.setMaxError(max_err);
                    sc_prune.setErrorMeasure(createDefaultError());
                }
                if (m_Mode == MODE_TIME_SERIES) {
                    sc_prune.setAdditiveError(createAdditiveError());
                }
                return sc_prune;
            }
        }
        INIFileNominalOrDoubleOrVector class_thr = getSettings().getHMLC().getClassificationThresholds();
        if (class_thr.hasVector()) { return new HierClassTresholdPruner(class_thr.getDoubleVector()); }

        if (m_Mode == MODE_REGRESSION) {
            double mult = sett.getM5PruningMult();
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_M5_MULTI) { return new M5PrunerMulti(getClusteringWeights(), mult); }
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_DEFAULT || sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_M5) {
                sett.setPruningMethod(SettingsTree.PRUNING_METHOD_M5);
                return new M5Pruner(getClusteringWeights(), mult);
            }
        }
        else if (m_Mode == MODE_CLASSIFY) {
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_DEFAULT || sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_C45) {
                sett.setPruningMethod(SettingsTree.PRUNING_METHOD_C45);
                return new C45Pruner();
            }
            /**
             * M5 pruning can also be used in classification, since it is
             * defined by using variance (i.e.,getSVarS) and in terms of
             * classification this is gini
             */
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_M5) { return new M5Pruner(getClusteringWeights(), sett.getM5PruningMult()); }
        }
        else if (m_Mode == MODE_HIERARCHICAL) {
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_M5) {
                double mult = sett.getM5PruningMult();
                return new M5Pruner(m_NormalizationWeights, mult);
            }
        }
        else if (m_Mode == MODE_PHYLO) {
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_ENCODING_COST) { return new EncodingCostPruning(); }
        }
        else if (m_Mode == MODE_CLASSIFY_AND_REGRESSION) {
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_M5) { return new M5Pruner(getClusteringWeights(), sett.getM5PruningMult()); }
            if (sett.getPruningMethod() == SettingsTree.PRUNING_METHOD_C45) { return new C45Pruner(); }
        }
        sett.setPruningMethod(SettingsTree.PRUNING_METHOD_NONE);
        return new PruneTree();
    }


    public PruneTree getTreePruner(ClusData pruneset) throws ClusException {
        Settings sett = getSettings();

        int pm = sett.getTree().getPruningMethod();
        if (pm == SettingsTree.PRUNING_METHOD_NONE) {
            // Don't prune if pruning method is set to none, even if validation
            // set is given
            return new PruneTree();
        }
        if (m_Mode == MODE_HIERARCHICAL && pruneset != null) {
            SettingsHMLC seth = getSettings().getHMLC();

            PruneTree pruner = getTreePrunerNoVSB();
            boolean bonf = seth.isUseBonferroni();
            HierRemoveInsigClasses hierpruner = new HierRemoveInsigClasses(pruneset, pruner, bonf, m_Hier);
            hierpruner.setSignificance(seth.getHierPruneInSig());
            hierpruner.setNoRootPreds(seth.isHierNoRootPreds());
            sett.getTree().setPruningMethod(SettingsTree.PRUNING_METHOD_DEFAULT);
            return hierpruner;
        }
        if (pruneset != null) {
            if (pm == SettingsTree.PRUNING_METHOD_GAROFALAKIS_VSB || pm == SettingsTree.PRUNING_METHOD_CART_VSB) {
                SequencePruningVSB pruner = new SequencePruningVSB((RowData) pruneset, getClusteringWeights());
                if (pm == SettingsTree.PRUNING_METHOD_GAROFALAKIS_VSB) {
                    int maxsize = sett.getConstraints().getMaxSize();
                    pruner.setSequencePruner(new SizeConstraintPruning(maxsize, getClusteringWeights()));
                }
                else {
                    pruner.setSequencePruner(new CartPruning(getClusteringWeights(), sett.getTree().isMSENominal()));
                }
                pruner.setOutputFile(sett.getGeneric().getFileAbsolute("prune.dat"));
                pruner.set1SERule(sett.getTree().get1SERule());
                pruner.setHasMissing(m_Schema.hasMissing());
                return pruner;
            }
            else if (pm == SettingsTree.PRUNING_METHOD_REDERR_VSB || pm == SettingsTree.PRUNING_METHOD_DEFAULT) {
                ClusErrorList parent = createEvalError();
                sett.getTree().setPruningMethod(SettingsTree.PRUNING_METHOD_REDERR_VSB);
                return new BottomUpPruningVSB(parent, (RowData) pruneset);
            }
            else {
                return getTreePrunerNoVSB();
            }
        }
        else {
            return getTreePrunerNoVSB();
        }
    }


    public synchronized void setTargetStatistic(ClusStatistic stat) {
        // System.out.println("Setting target statistic: " + stat.getClass().getName());
        m_StatisticAttrUse[ClusAttrType.ATTR_USE_TARGET] = stat;
    }


    public synchronized void setClusteringStatistic(ClusStatistic stat) {
        // System.out.println("Setting clustering statistic: " + stat.getClass().getName());
        m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] = stat;
    }


    public synchronized boolean hasClusteringStat() {
        return m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING] != null;
    }


    public synchronized ClusStatistic createClusteringStat() {
        return m_StatisticAttrUse[ClusAttrType.ATTR_USE_CLUSTERING].cloneStat();
    }


    public synchronized ClusStatistic createTargetStat() {
        return m_StatisticAttrUse[ClusAttrType.ATTR_USE_TARGET].cloneStat();
    }


    /**
     * @param attType
     *        attribute use type (eg., ClusAttrType.ATTR_USE_TARGET)
     * @return the statistic
     */
    public synchronized ClusStatistic createStatistic(int attType) {
        return m_StatisticAttrUse[attType].cloneStat();
    }


    /**
     *
     * @param attType
     *        attribute use type (eg., ClusAttrType.ATTR_USE_TARGET)
     * @return The statistic
     */
    public synchronized ClusStatistic getStatistic(int attType) {
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
        if (getMode() != MODE_HIERARCHICAL)
            computeTrainSetStat(trainset, ClusAttrType.ATTR_USE_ALL);
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
        return getSettings().getRules().getCoveringMethod() == SettingsRules.COVERING_METHOD_RULES_FROM_TREE;
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
            ArrayList<WHTDStatistic> stats = new ArrayList<WHTDStatistic>();
            model.retrieveStatistics(stats);
            for (int i = 0; i < stats.size(); i++) {
                WHTDStatistic stat = stats.get(i);
                stat.setHier(m_Hier);
            }
        }
    }


    private void createHierarchy() {
        // int idx = 0;
        for (int i = 0; i < m_Schema.getNbAttributes(); i++) {
            ClusAttrType type = m_Schema.getAttrType(i);
            if (!type.isDisabled() && type instanceof ClassesAttrType) {
                ClassesAttrType cltype = (ClassesAttrType) type;
                System.out.println("Classes type: " + type.getName());
                m_Hier = cltype.getHier();
                // idx++;
            }
        }
    }


    public void initHierarchySettings() throws ClusException, IOException {
        if (m_Hier != null) {
            if (getSettings().getHMLC().hasHierEvalClasses()) {
                ClassesTuple tuple = ClassesTuple.readFromFile(getSettings().getHMLC().getHierEvalClasses(), m_Hier);
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
        SettingsRules setr = getSettings().getRules();
        SettingsTree sett = getSettings().getTree();

        int covering = setr.getCoveringMethod();
        int prediction = setr.getRulePredictionMethod();
        // General
        if (((sett.getHeuristic() != SettingsTree.HEURISTIC_DISPERSION_ADT)
                || (sett.getHeuristic() != SettingsTree.HEURISTIC_DISPERSION_MLT)
                || (sett.getHeuristic() != SettingsTree.HEURISTIC_R_DISPERSION_ADT)
                || (sett.getHeuristic() != SettingsTree.HEURISTIC_R_DISPERSION_MLT)) && setr.isHeurRuleDist()) {
            setr.setHeurRuleDistPar(0.0);
        }
        if (setr.isRuleSignificanceTesting()) {
            setr.setIS_RULE_SIG_TESTING(true);
            // Is this faster than calling isRuleSignificanceTesting()
            // from Dispersion heuristic each time?
        }
        // Random rules
        if (setr.isRandomRules()) {
            setr.setCoveringMethod(SettingsRules.COVERING_METHOD_STANDARD);
            // sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_DECISION_LIST);
            setr.setCoveringWeight(0);
            // Ordered rules
        }
        else if (covering == SettingsRules.COVERING_METHOD_STANDARD) {
            // sett.setRulePredictionMethod(Settings.RULE_PREDICTION_METHOD_DECISION_LIST);
            setr.setCoveringWeight(0);
            // Unordered rules - Heuristic covering
        }
        else if (covering == SettingsRules.COVERING_METHOD_HEURISTIC_ONLY) {
            if ((prediction == SettingsRules.RULE_PREDICTION_METHOD_DECISION_LIST) || (prediction == SettingsRules.RULE_PREDICTION_METHOD_UNION)) {
                setr.setRulePredictionMethod(SettingsRules.RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED);
            }
            setr.setCoveringWeight(0.0);
            if (setr.getHeurRuleDistPar() < 0) { throw new ClusException("Clus heuristic covering: HeurRuleDistPar must be >= 0!"); }
            if ((sett.getHeuristic() != SettingsTree.HEURISTIC_DISPERSION_ADT)
                    || (sett.getHeuristic() != SettingsTree.HEURISTIC_DISPERSION_MLT)
                    || (sett.getHeuristic() != SettingsTree.HEURISTIC_R_DISPERSION_ADT)
                    || (sett.getHeuristic() != SettingsTree.HEURISTIC_R_DISPERSION_MLT)) { throw new ClusException("Clus heuristic covering: Only dispersion-based heuristics supported!"); }
            // Unordered rules - Weighted coverings
        }
        else if ((covering == SettingsRules.COVERING_METHOD_WEIGHTED_ADDITIVE)
                || (covering == SettingsRules.COVERING_METHOD_WEIGHTED_MULTIPLICATIVE)
                || (covering == SettingsRules.COVERING_METHOD_WEIGHTED_ERROR)
                || (covering == SettingsRules.COVERING_METHOD_BEAM_RULE_DEF_SET)
                || (covering == SettingsRules.COVERING_METHOD_RANDOM_RULE_SET)) {
            if ((prediction == SettingsRules.RULE_PREDICTION_METHOD_DECISION_LIST)
                    || (prediction == SettingsRules.RULE_PREDICTION_METHOD_UNION)) {
                setr.setRulePredictionMethod(SettingsRules.RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED);
            }
            if (setr.getCoveringWeight() < 0) { throw new ClusException("Clus weighted covering: Covering weight must be >= 0!"); }
            // Rule induction from bootstrap sampled data, optimized ...
        }
        else if (covering == SettingsRules.COVERING_METHOD_STANDARD_BOOTSTRAP) {
            setr.setRulePredictionMethod(SettingsRules.RULE_PREDICTION_METHOD_OPTIMIZED);
            // Multi-label classification
        }
        else if (covering == SettingsRules.COVERING_METHOD_UNION) {
            setr.setRulePredictionMethod(SettingsRules.RULE_PREDICTION_METHOD_UNION);
        }
        else if (covering == SettingsRules.COVERING_METHOD_RULES_FROM_TREE) {
            sett.setHeuristic(SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
            setr.setRuleAddingMethod(SettingsRules.RULE_ADDING_METHOD_ALWAYS);
        }
    }
}
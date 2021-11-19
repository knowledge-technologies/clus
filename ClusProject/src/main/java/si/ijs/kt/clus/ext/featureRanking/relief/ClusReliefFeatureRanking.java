
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.hierarchies.ClassesAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.data.type.primitive.StringAttrType;
import si.ijs.kt.clus.data.type.primitive.TimeSeriesAttrType;
import si.ijs.kt.clus.distance.primitive.relief.HierarchicalMultiLabelDistance;
import si.ijs.kt.clus.distance.primitive.relief.Levenshtein;
import si.ijs.kt.clus.distance.primitive.relief.MultiLabelDistance;
import si.ijs.kt.clus.distance.primitive.timeseries.DTWTimeSeriesDist;
import si.ijs.kt.clus.distance.primitive.timeseries.QDMTimeSeriesDist;
import si.ijs.kt.clus.distance.primitive.timeseries.TSCTimeSeriesDist;
import si.ijs.kt.clus.ext.ensemble.ClusReadWriteLock;
import si.ijs.kt.clus.ext.featureRanking.ClusFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.FindNeighboursCallable;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.SaveLoadNeighbours;
import si.ijs.kt.clus.ext.featureRanking.relief.statistics.Classic;
import si.ijs.kt.clus.ext.featureRanking.relief.statistics.DistanceSimplified;
import si.ijs.kt.clus.ext.featureRanking.relief.statistics.Statistics;
import si.ijs.kt.clus.ext.featureRanking.relief.statistics.Steepness;
import si.ijs.kt.clus.ext.featureRanking.relief.statistics.Variance;
import si.ijs.kt.clus.ext.imputation.MissingTargetImputation;
import si.ijs.kt.clus.ext.timeseries.TimeSeries;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsRelief;
import si.ijs.kt.clus.main.settings.section.SettingsRelief.MissingTargetHandling;
import si.ijs.kt.clus.main.settings.section.SettingsRelief.MultilabelDistance;
import si.ijs.kt.clus.main.settings.section.SettingsRelief.ReliefStatisticsType;
import si.ijs.kt.clus.main.settings.section.SettingsTimeSeries.TimeSeriesDistanceMeasure;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.math.MathUtil;
import si.ijs.kt.clus.util.tuple.Triple;

/**
 * 
 * @author matejp
 *
 */
public class ClusReliefFeatureRanking extends ClusFeatureRanking {

	public static final int DESCRIPTIVE_SPACE = 0;
	public static final int TARGET_SPACE = 1;
	public static final int[] SPACE_TYPES = new int[] { DESCRIPTIVE_SPACE, TARGET_SPACE };

	/**
	 * The dataset used.
	 */
	private RowData m_Data;

	/** Numbers of neighbours in the importance calculation */
	private int[] m_NbNeighbours;

	/** Maximal element of m_NbNeighbours */
	private int m_MaxNbNeighbours;

	/** Numbers of iterations in the importance calculation */
	private int[] m_NbIterations;

	/** Maximal element of m_NbIterations */
	private int m_MaxNbIterations;

	/** The order of instances whose neighbours we compute etc. */
	private int[] m_Order;

	/** Tells, whether the contributions of the neighbours are weighted */
	private boolean m_WeightNeighbours;

	/**
	 * {@code >= 0}, see {@link m_NeighbourWeights}, {@code m_Sigma == 0 <=> {@link
	 * #m_WeightNeighbours} == false}
	 */
	private double m_Sigma;

	/**
	 * Weights for the contributions of the nearest neighbours. Value on the i-th
	 * place is {@code exp((- m_Sigma * i)^2)}.
	 */
	private double[] m_NeighbourWeights;

	/** {array of descriptive attributes, array of target attributes} */
	private ClusAttrType[][] m_DescriptiveTargetAttr = new ClusAttrType[2][];

	/**
	 * An analogue of {@link m_DescriptiveTargetAttr}: indices of the attributes
	 * which are used in distance computations: if data tuples are sparse, the two
	 * elements of this list may be shorted than the elements of
	 * {@link m_DescriptiveTargetAttr}
	 */
	private int[][] m_DistanceDescriptiveTargetAttr = new int[2][];

	/** number of descriptive attributes */
	private int m_NbDescriptiveAttrs;

	/** number of target attributes */
	private int m_NbTargetAttrs;

	/**
	 * 1 if we compute the ranking for the 'overall' target, and 1 +
	 * {@link #m_NbTargetAttrs} otherwise
	 */
	private int m_NbGeneralisedTargetAttrs;

	/** {numericAttributeName: minimalValueOfTheAttribute, ...} */
	private HashMap<String, Double> m_numMins;

	/** {numericAttributeName: maximalValueOfTheAttribute, ...} */
	private HashMap<String, Double> m_numMaxs;

	/** number of examples in the data */
	private int m_NbExamples;

	/** Are the data tuples sparse or dense */
	private boolean m_IsSparse;

	/** distance in the case of missing values */
	public static double BOTH_MISSING_DIST = 1.0;

	/**
	 * Random generator for sampling of the next instance. It is used iff not
	 * {@link #m_isDeterministic}
	 */
	private Random m_rnd;

	/**
	 * m_IsStandardClassification[index]: are we in the standard classification
	 * ({@code true}) or general (regression) case ({@code false}), for the target
	 * with the index of {@code index - 1} if {@code index > 0}, and overall
	 * rankings otherwise.
	 */
	private boolean[] m_IsStandardClassification;

	/**
	 * m_NbTargetValues[index]: if {link #m_IsStandardClassification}[index]: the
	 * number of classes, for the target with index {@code index}, otherwise: 1.
	 */
	private int[] m_NbTargetValues;

	/**
	 * m_TargetProbabilities[index]: relative frequencies of the target values, used
	 * in standard classification, for the target with the index of
	 * {@code index - 1} if {@code index > 0}, and overall target otherwise.<br>
	 * If not {@link #m_IsStandardClassification}[index], then
	 * m_TargetProbabilities[index] = null.
	 */
	private double[][] m_TargetProbabilities;

	/** tells, whether to perform per-target rankings also */
	private boolean m_PerformPerTargetRanking;

	private Statistics mStats;

	/** Hierarchical multi-label distance handler */
	private HierarchicalMultiLabelDistance m_HierarMLCDist = new HierarchicalMultiLabelDistance();

	/** Tells whether the task is multi-label classification. */
	private boolean m_IsMLC;
	/** Multi-label classification distance handler */
	private MultiLabelDistance m_MLCDist;
	/** Multi-label classification distance type */
	private MultilabelDistance m_MLCDistanceType;

	/** Time series distance type */
	private TimeSeriesDistanceMeasure m_TimeSeriesDistance;

	/** Counter for approximate proportion of the finished iterations. */
	private int m_Percents = 0;

	/**
	 * {@code m_NearestNeighbours[target index][tuple index]}: the nearest
	 * neighbours for a given tuple as returned by
	 * {@link #findNearestNeighbours(int, RowData, int, boolean)}.
	 * <p>
	 * If {@code target index = -1}, these are the neighbours used in the
	 * computation of the overall ranking(s). Otherwise, these are the neighbours
	 * used in the computations of the ranking(s) for target with the index
	 * {@code target index}.
	 */
	private HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> m_NearestNeighbours;

	private int m_NbThreads;

	private boolean m_ShouldLoadNeighbours;
	private boolean m_ShouldSaveNeighbours;
	/**
	 * The name of the files that are used when the nearest neighbours should be
	 * loaded from the files.
	 */
	private String[] m_LoadNearestNeigbhoursFiles;
	/**
	 * The name of the file that is used when the nearest neighbours should be saved
	 * to the file.
	 */
	private String m_SaveNearestNeigbhoursFile;

	/** How to handle missing target values */
	private MissingTargetHandling m_MissingTargetHandling;
	/** Interval for SSL amounts of supervision */
	private double[] m_SSLW;
	/** The amount of supervision for every chosen example */
	private double[] m_SSLWs;
	private static double m_BASIC_SSLw = 1.0;

	/** Clus run that is running this instance */
	private ClusRun m_Run;

	/** Counts the progress in nearest neighbour computation. */
	private int m_NeighCounter;
	private int m_NeighCounterBound;
	private int m_NeighPercents;
	private ClusReadWriteLock m_CounterLock = new ClusReadWriteLock();

	/**
	 * Constructor for the {@code ClusReliefFeatureRanking}, with the standard
	 * parameters of (R)Relief(F).
	 * 
	 * @param neighbours
	 *            The number of neighbours that is used in the feature importance
	 *            calculation. Constraints:
	 *            <p>
	 *            {@code 0 < neighbours <= number of instances in the dataset}
	 * @param iterations
	 *            The number of iterations in the feature importance calculation.
	 *            Constraints:
	 *            <p>
	 *            {@code 0 < iterations <= number of instances in the dataset}
	 * @param weightNeighbours
	 *            If {@code weightNeighbours}, then the contribution of the
	 *            {@code i}-th nearest neighbour in the feature importance
	 *            calculation are weighted with factor
	 *            {@code exp((- sigma * i) ** 2)}.
	 * @param sigma
	 *            The rate of quadratic exponential decay. Note that Weka's sigma is
	 *            the inverse of our {@code sigma}.
	 */
	public ClusReliefFeatureRanking(RowData data, int[] neighbours, int[] iterations, boolean weightNeighbours,
	        double sigma, int seed, Settings sett, MissingTargetHandling missingTargetHandling, double[] sslWinterval,
	        ClusRun cr) {
		super(sett);
		m_Data = data;
		m_NbNeighbours = neighbours;
		m_MaxNbNeighbours = m_NbNeighbours[m_NbNeighbours.length - 1];
		m_NbIterations = iterations;
		m_MaxNbIterations = m_NbIterations[m_NbIterations.length - 1];
		m_WeightNeighbours = weightNeighbours;
		m_Sigma = m_WeightNeighbours ? sigma : 0.0;
		m_NeighbourWeights = new double[m_MaxNbNeighbours];
		m_rnd = new Random(seed);
		m_MissingTargetHandling = missingTargetHandling;
		m_SSLW = sslWinterval;
		m_Run = cr;
		initialize();
	}

	/**
	 * Initialises some fields etc.
	 * 
	 * @param data
	 */
	private void initialize() {
		// note: keep the order of the inicialization fixed, sometimes it is important
		printMessage("Preprocessing steps ...", 1, getSettings().getGeneral().getVerbose());
		m_NbExamples = m_Data.getNbRows();
		setReliefDescription(m_NbNeighbours, m_NbIterations);

		initializeNeigbhourLoadSave();
		initializeNeighbourWeights();
		initializeInstanceOrder();
		initializeTimeSeriesDistance();
		initializeDescriptiveTargetAttributes();
		initializeNumericRanges();	
		// semi-supervised: this can modify the data, so keep it as high as possible
		prepareForSemiSupervised();
		
		
		initializeRankings();
		m_IsMLC = getSettings().getMLC().getSectionMultiLabel().isEnabled();
		initializeTargetProbabilities();

		// depends on m_TargetProbabilities ...
		if (m_IsMLC) {
			m_MLCDistanceType = getSettings().getRelief().getMultilabelDistance();
			double[] labelProbabilities = new double[m_NbTargetAttrs];
			String labelPresent = "1";
			for (int i = 0; i < m_NbTargetAttrs; i++) {
				NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][i];
				labelProbabilities[i] = m_TargetProbabilities[i + 1][attr.getValueIndex(labelPresent)];
			}
			m_MLCDist = new MultiLabelDistance(m_MLCDistanceType, m_DescriptiveTargetAttr[TARGET_SPACE],
			        labelProbabilities);
		}

		// check for hierarchical attributes
		for (int space : SPACE_TYPES) {
			for (int attrInd = 0; attrInd < m_DescriptiveTargetAttr[space].length; attrInd++) {
				if (m_DescriptiveTargetAttr[space][attrInd].isClasses()) {
					ClassesAttrType attr = (ClassesAttrType) m_DescriptiveTargetAttr[space][attrInd];
					m_HierarMLCDist.processAttribute(attr, m_Data);
				}
			}
		}

		// attribute relevance estimation: current statistics
		ReliefStatisticsType statType = getSettings().getRelief().getReliefStatisticsType();
		if (Arrays.stream(m_SSLWs).min().getAsDouble() < m_BASIC_SSLw && !statType.equals(ReliefStatisticsType.DistanceClassic)) {
			throw new RuntimeException("Using unsupervised data is supported only in DistanceClassic statisic type, but yours is " + statType);
		}
		switch (statType) {
		case DistanceClassic:
			mStats = new Classic(this, m_NbGeneralisedTargetAttrs, m_NbNeighbours.length, m_NbDescriptiveAttrs);
			break;
		case DistanceSimplified:
			mStats = new DistanceSimplified(this, m_NbGeneralisedTargetAttrs, m_NbNeighbours.length,
			        m_NbDescriptiveAttrs);
			break;
		case Steepness:
			mStats = new Steepness(this, m_NbGeneralisedTargetAttrs, m_NbNeighbours.length, m_NbDescriptiveAttrs,
			        m_MaxNbNeighbours, false, true); // TODO un-hardcode false
			break;
		case Variance:
			mStats = new Variance(this, m_NbGeneralisedTargetAttrs, m_NbNeighbours.length, m_NbDescriptiveAttrs);
			break;
		default:
			throw new RuntimeException("Wrong value for statistics type: " + statType);
		}
		// nearest neighbours
		m_NearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		// number of threads
		m_NbThreads = m_Data.getSchema().getSettings().getEnsemble().getNumberOfThreads();		
	}

	private void initializeNeigbhourLoadSave() {
		m_ShouldLoadNeighbours = getSettings().getKNN().shouldLoadNeighbours();
		m_LoadNearestNeigbhoursFiles = m_ShouldLoadNeighbours ? getSettings().getKNN().getLoadNeighboursFiles()
		        : new String[] {};

		m_ShouldSaveNeighbours = getSettings().getKNN().shouldSaveNeighbours();
		m_SaveNearestNeigbhoursFile = m_ShouldSaveNeighbours ? getSettings().getKNN().getSaveNeighboursFile() : "";
	}

	private void initializeNeighbourWeights() {
		if (m_WeightNeighbours) {
			for (int neigh = 0; neigh < m_MaxNbNeighbours; neigh++) {
				m_NeighbourWeights[neigh] = Math.exp(-(m_Sigma * neigh) * (m_Sigma * neigh));
			}
		} else {
			Arrays.fill(m_NeighbourWeights, 1.0);
		}
	}

	private void initializeInstanceOrder() {
		m_Order = getSettings().getRelief().getChosenIntances();
		if (Arrays.equals(m_Order, SettingsRelief.DUMMY_INSTANCES)) {
			m_Order = randomPermutation(m_NbExamples);
		}
		if (m_Order.length < m_MaxNbIterations) {
			System.err.println(
			        String.format("Warning! Maximal number of iterations = %d > %d = number of chosen instances.",
			                m_MaxNbIterations, m_Order.length));
			System.err.println("Setting the maximal number of iterations to the number of chosen instances.");
			m_MaxNbIterations = m_Order.length;
		}
	}

	private void initializeTimeSeriesDistance() {
		m_TimeSeriesDistance = m_Data.m_Schema.getSettings().getTimeSeries().getTimeSeriesDistance();
	}

	private void initializeDescriptiveTargetAttributes() {
		// Initialise descriptive and target attributes if necessary
		AttributeUseType attrType;
		for (int space : SPACE_TYPES) {
			attrType = space == DESCRIPTIVE_SPACE ? AttributeUseType.Descriptive : AttributeUseType.Target;
			if (m_DescriptiveTargetAttr[space] == null)
				m_DescriptiveTargetAttr[space] = m_Data.m_Schema.getAllAttrUse(attrType);
		}
		m_NbDescriptiveAttrs = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE].length;
		m_NbTargetAttrs = m_DescriptiveTargetAttr[TARGET_SPACE].length;
		m_IsSparse = m_Data.isSparse();
		// efficient distance computation for sparse tuples
		int nbNonNumericDescriptive = m_NbDescriptiveAttrs; // if not sparse, the variables names are misleading:
		int nbNonNumericTarget = m_NbTargetAttrs; // these are simply the numbers of desc./targ. attributes ...

		if (m_IsSparse) {
			nbNonNumericDescriptive -= m_Data.getSchema().getNbNumericAttrUse(AttributeUseType.Descriptive);
			nbNonNumericTarget -= m_Data.getSchema().getNbNumericAttrUse(AttributeUseType.Target);
		}
		m_DistanceDescriptiveTargetAttr[DESCRIPTIVE_SPACE] = new int[nbNonNumericDescriptive];
		m_DistanceDescriptiveTargetAttr[TARGET_SPACE] = new int[nbNonNumericTarget];
		for (int space : SPACE_TYPES) {
			int place = 0;
			for (int i = 0; i < m_DescriptiveTargetAttr[space].length; i++) {
				if (!m_IsSparse || !m_DescriptiveTargetAttr[space][i].isNumeric()) {
					m_DistanceDescriptiveTargetAttr[space][place] = i;
					place++;
				}
			}
			if (place != m_DistanceDescriptiveTargetAttr[space].length) {
				throw new RuntimeException("Something fishy with the number of nonumeric attributes.");
			}
		}
	}

	private void initializeRankings() {
		// number of rankings
		m_PerformPerTargetRanking = m_Data.m_Schema.getSettings().getEnsemble().shouldPerformRankingPerTarget();
		if (m_PerformPerTargetRanking && m_NbTargetAttrs == 1) {
			System.err.println("Situation:");
			System.err.println("  - Per-target rankings were desired.");
			System.err.println("  - The number of targets is 1, hence per-target ranking == overall ranking.");
			System.err.println("Consequence:");
			System.err.println("  - Only overall feature ranking(s) will be computed.");
			m_PerformPerTargetRanking = false;
		}
		m_NbGeneralisedTargetAttrs = 1 + (m_PerformPerTargetRanking ? m_NbTargetAttrs : 0);
		setNbFeatureRankings();
		// types of rankings
		m_IsStandardClassification = computeStandardClassification();
		
		m_NbTargetValues = nbTargetValues();
	}
	
	private void initializeTargetProbabilities() {
		int upperBound = m_IsMLC ? 1 + m_NbTargetAttrs : m_NbGeneralisedTargetAttrs;
		m_TargetProbabilities = new double[upperBound][];
		for (int targetIndex = -1; targetIndex < m_TargetProbabilities.length - 1; targetIndex++) {
			if ((m_IsMLC && targetIndex >= 0) || m_IsStandardClassification[targetIndex + 1]) {
				m_TargetProbabilities[targetIndex + 1] = nominalClassCounts(targetIndex);
			}
		}
	}
	
	private void initializeNumericRanges() {
		// compute min and max of numeric attributes
		m_numMins = new HashMap<String, Double>();
		m_numMaxs = new HashMap<String, Double>();
		double value;
		String attrName;
		for (int space : SPACE_TYPES) {
			AttributeUseType attrType = space == DESCRIPTIVE_SPACE ? AttributeUseType.Descriptive : AttributeUseType.Target;
			for (NumericAttrType numAttr : m_Data.m_Schema.getNumericAttrUse(attrType)) {
				attrName = numAttr.getName();
				m_numMins.put(attrName, Double.POSITIVE_INFINITY);
				m_numMaxs.put(attrName, Double.NEGATIVE_INFINITY);
				for (int example = 0; example < m_NbExamples; example++) {
					value = numAttr.getNumeric(m_Data.getTuple(example));
					if (value < m_numMins.get(attrName)) { // equivalent to ... && value != Double.POSITIVE_INFINITY
						m_numMins.put(attrName, value);
					}
					if (value > m_numMaxs.get(attrName) && value != Double.POSITIVE_INFINITY) {
						m_numMaxs.put(attrName, value);
					}
				}
			}
		}
	}
	

	private void prepareForSemiSupervised() {
		m_SSLWs = new double[m_NbExamples];
		Arrays.fill(m_SSLWs, m_BASIC_SSLw);  // neutral value for multiplication
		HashMap<Integer, ArrayList<Integer>> missingTargetValues = m_Data.getMissingTargets();

		if (missingTargetValues.size() > 0) {
			switch (m_MissingTargetHandling) {
			case Impute:
				MissingTargetImputation.impute(m_Run, missingTargetValues);
				break;
			case UseDescriptive:
				computeAmountsOfSupervision();
				break;
			default:
				ignoreMissing();
				break;
			}
		}
	}
	
	private static void ignoreMissing() {
		ClusLogger.info("Examples with missing values will be mostly ignored.");
	}
	
	private void computeAmountsOfSupervision() {
		// try load from file
		// TODO
		double[] distancesToNotUnlabeled = new double[m_NbExamples];
		// compute the nearest neighbours
		for (int i = 0; i < m_NbExamples; i++) {
			try {
				distancesToNotUnlabeled[i] = findDistanceToFirstNotUnlabeledNeighbour(i);
			} catch (ClusException e) {
				e.printStackTrace();
			}
		}
		double dMin = Arrays.stream(distancesToNotUnlabeled).min().getAsDouble();
		double dMax = Arrays.stream(distancesToNotUnlabeled).max().getAsDouble();
		double wMin = m_SSLW[0];
		double wMax = m_SSLW[1];
		// take care of some degenerate cases
		if (dMax == 0.0) {
			ClusLogger.info("The data seems to be supervised or all nearest neighbours are 0 away.");
		}
		else if ((dMax - dMin) / dMax < ClusUtil.MICRO) {
			Arrays.fill(m_SSLWs, (wMin + wMax) / 2.0); // something in between if everything is the same
		}
		else {
			// linear interpolation
			for (int i = 0; i < m_SSLWs.length; i++) {
				if (m_Data.getTuple(i).isUnlabeled()) {
					m_SSLWs[i] = computeAmountOfSupervision(distancesToNotUnlabeled[i], dMin, dMax, wMin, wMax);
				}
			}
		}
	}
	
	private static double computeAmountOfSupervision(double d, double minD, double maxD, double minW, double maxW) {
		double k = (maxW - minW) / (minD - maxD);
		return k * (d - minD) + maxW;
	}

	/**
	 * Computes and sets the number of feature rankings to be performed.
	 */
	private void setNbFeatureRankings() {
		ArrayList<String> rankings = new ArrayList<String>();
		ArrayList<String> prefixes = new ArrayList<String>();
		prefixes.add("overall");
		if (m_PerformPerTargetRanking) {
			for (int targetInd = 0; targetInd < m_NbTargetAttrs; targetInd++) {
				prefixes.add(m_DescriptiveTargetAttr[TARGET_SPACE][targetInd].getName());
			}
		}
		boolean useIndices = m_Run.getStatManager().getSettings().getRelief().shouldUseIndicesInNames();
		for (String prefix : prefixes) {
			for (int iterInd = 0; iterInd < m_NbIterations.length; iterInd++) {
				for (int neighInd = 0; neighInd < m_NbNeighbours.length; neighInd++) {
				    int iter = useIndices ? iterInd : m_NbIterations[iterInd];
				    int neig = useIndices ? neighInd : m_NbNeighbours[neighInd]; 
					rankings.add(String.format("%sIter%dNeigh%d", prefix, iter, neig));
					
				}
			}
		}
		setReliefFimpHeader(rankings);
		setNbFeatureRankings(prefixes.size() * m_NbNeighbours.length * m_NbIterations.length);
	}

	/**
	 * Returns the index of the (per-target) ranking, computed with specified number
	 * of iterations, neighbours and target index. Greater or equal to zero.
	 * 
	 * @param iterationsIndex
	 *            The index of the number of iterations in {@link #m_NbIterations}
	 * @param neighboursIndex
	 *            The index of the number of neighbours in {@link #m_NbNeighnours}
	 * @param targetIndex
	 *            If non-negative, then this is the index of the target and we are
	 *            looking for a index of a per-target ranking. If -1, then we are
	 *            looking for the index of the overall ranking.
	 * 
	 */
	private int rankingIndex(int iterationsIndex, int neighboursIndex, int targetIndex) {
		int perTargetShift = (targetIndex + 1) * m_NbIterations.length * m_NbNeighbours.length;
		return perTargetShift + iterationsIndex * m_NbNeighbours.length + neighboursIndex;
	}

	/**
	 * Calculates the feature importances for a given dataset.
	 * 
	 * @param data
	 *            The dataset, whose features are importances calculated for.
	 * @throws ClusException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void computeReliefImportance(RowData data)
	        throws ClusException, InterruptedException, ExecutionException, IOException {

		DataTuple tuple;

		int nbTargets = m_PerformPerTargetRanking ? 1 + m_NbTargetAttrs : 1;
		double[] successfulIterations = new double[nbTargets];

		// int[] theOrder = randomPermutation(m_NbExamples);
		computeNearestNeighbours(m_Order);

		int insufficientNbNeighbours = 0;
		int numIterInd = 0;
		boolean[] shouldUpdate = new boolean[nbTargets]; // [overall] or [overall, target1, target2, ...]

		printMessage("Calculating importances ...", 1, getSettings().getGeneral().getVerbose());
		for (int iteration = 0; iteration < m_MaxNbIterations; iteration++) {
			printProgress(iteration);
			// CHOOSE TUPLE AND GET NEAREST NEIGHBOURS
			int tupleInd = m_Order[iteration];
			tuple = data.getTuple(tupleInd);
			NearestNeighbour[][] nearestNeighbours;
			// UPDATE OVERALL (AND PER-TARGET) RANKING STATISTICS
			for (int targetIndex = -1; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++) {
				if (shouldUseTuple(targetIndex, tuple)) {
					successfulIterations[targetIndex + 1]++;
					if (m_IsStandardClassification[targetIndex + 1] && targetIndex >= 0) {
						nearestNeighbours = m_NearestNeighbours.get(targetIndex).get(tupleInd);
					} else {
						nearestNeighbours = m_NearestNeighbours.get(-1).get(tupleInd);
					}
					insufficientNbNeighbours += updateDistanceStatistics(data, tuple, tupleInd, nearestNeighbours, targetIndex);
					shouldUpdate[targetIndex + 1] = true;
				}
			}
			// IMPORTANCE UPDATE
			if (iteration + 1 == m_NbIterations[numIterInd]) {
				updateImportances(data, numIterInd, successfulIterations, shouldUpdate);
				numIterInd++;
				shouldUpdate = new boolean[nbTargets];
			}
		}

		if (insufficientNbNeighbours > 0) {
			int ver = getSettings().getGeneral().getVerbose();
			String message1 = "Maximal number of neighbours: " + m_MaxNbNeighbours;
			String message2 = "Number of cases when we could not find that many neighbours: "
			        + insufficientNbNeighbours;
			printMessage(message1 + "\n" + message2, 1, ver);
		}

		// NORMALISATION IF NECESSARY
		ReliefStatisticsType s = getSettings().getRelief().getReliefStatisticsType();
		if (!s.equals(ReliefStatisticsType.DistanceClassic)) {
			normalizeImportances();
		}

	}

	private void normalizeImportances() throws InterruptedException { // TODO
		printMessage("Normalising importances ... (x --> -1 / x)", 1, getSettings().getGeneral().getVerbose());
		// double[][] importanceSpans = new double[getNbFeatureRankings()][2];
		// for (int r = 0; r < getNbFeatureRankings(); r++) {
		// for (int a = 0; a < m_NbDescriptiveAttrs; a++) {
		// importanceSpans[r][0] = Double.POSITIVE_INFINITY;
		// importanceSpans[r][1] = Double.NEGATIVE_INFINITY;
		// }
		// }
		// for (int a = 0; a < m_NbDescriptiveAttrs; a++) {
		// ClusAttrType attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][a];
		// double[] info = getAttributeInfo(attr.getName());
		// for (int r = 0; r < getNbFeatureRankings(); r++) {
		// double v = info[2 + r];
		// importanceSpans[r][0] = Math.min(importanceSpans[r][0], v);
		// importanceSpans[r][1] = Math.max(importanceSpans[r][1], v);
		// }
		// }
		for (int a = 0; a < m_NbDescriptiveAttrs; a++) {
			ClusAttrType attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][a];
			double[] info = getAttributeInfo(attr.getName());
			for (int r = 0; r < getNbFeatureRankings(); r++) {
				// double minImpo = importanceSpans[r][0];
				// double maxImpo = importanceSpans[r][1];
				// if (Math.abs(maxImpo - minImpo) > ClusUtil.NANO) {
				// info[2 + r] = (info[2 + r] - minImpo); // / (maxImpo - minImpo);
				// }
				info[2 + r] = -1.0 / info[2 + r];
			}
			putAttributeInfo(attr.getName(), info);
		}
	}

	/**
	 * Updates the importances of the attributes. It is called when the current
	 * number of iterations is the element of {@link #m_NbIterations}.
	 * 
	 * @param data
	 *            The dataset under consideration.
	 * @param numIterInd
	 *            {@link #m_NbIterations}[numIterInd] equals the current number of
	 *            iterations
	 * @param successfulItearions
	 *            {@code successfulItearions}[target + 1] equals the number of
	 *            successful overall (if target = -1) or per-target (for the target
	 *            with index target) iterations.
	 *            <p>
	 *            Due to the missing values, some of these may not equal
	 *            {@link #m_NbIterations}[numIterInd].
	 * @param shouldUpdate
	 *            Tells, whether the given overall/per-target ranking statistics
	 *            should be updated. Has the same structure as
	 *            {@code successfulItearions}.
	 * @throws InterruptedException
	 */
	private void updateImportances(RowData data, int numIterInd, double successfulItearions[], boolean[] shouldUpdate)
	        throws InterruptedException {
		for (int attrInd = 0; attrInd < m_NbDescriptiveAttrs; attrInd++) {
			ClusAttrType attr = m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][attrInd];
			double[] info = getAttributeInfo(attr.getName());
			for (int targetIndex = -1; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++) {
				if (shouldUpdate[targetIndex + 1]) {
					boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
					for (int nbNeighInd = 0; nbNeighInd < m_NbNeighbours.length; nbNeighInd++) {
						int rankingInd = rankingIndex(numIterInd, nbNeighInd, targetIndex);
						info[2 + rankingInd] += mStats.computeImportances(targetIndex, nbNeighInd, attrInd,
						        isStdClassification, successfulItearions);
					}
				}
			}
			putAttributeInfo(attr.getName(), info);
		}
	}

	/**
	 * In each iteration, this method updates distance statistics after the
	 * neighbours of a chosen tuple are found.
	 * 
	 * @param data
	 * @param tuple
	 * @param nearestNeighbours
	 * @param targetIndex
	 * 
	 * @throws ClusException
	 */
	private int updateDistanceStatistics(RowData data, DataTuple tuple, int tupleIndex, NearestNeighbour[][] nearestNeighbours,
	        int targetIndex) throws ClusException {
		int tempInsufficientNbNeighbours = 0;
		int nbTargetValues = m_NbTargetValues[targetIndex + 1];
		for (int targetValue = 0; targetValue < nbTargetValues; targetValue++) {
			// The sums sum_neigh w_neigh * d, where w is non-normalised weight and d is
			// d_class * d_attr or d_class
			// etc.
			mStats.resetTempFields();
			double sumNeighbourWeights = 0.0;

			boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
			int trueIndex = getTrueTargetIndex(targetIndex);
			int numNeighInd = 0;
			int nbNeighbours = Math.min(m_MaxNbNeighbours, nearestNeighbours[targetValue].length);
			for (int neighbour = 0; neighbour < nbNeighbours; neighbour++) {
				if (nearestNeighbours[targetValue].length < m_MaxNbNeighbours) {
					tempInsufficientNbNeighbours++;
				}
				sumNeighbourWeights += m_NeighbourWeights[neighbour];
				double neighWeightNonnormalized = m_NeighbourWeights[neighbour];				
				NearestNeighbour neigh = nearestNeighbours[targetValue][neighbour];
				// take care of semi-supervised: 
				// neighWeightNonnormalized will actually be the weight of the pair (tuple, neigh).
				neighWeightNonnormalized *= m_SSLWs[tupleIndex] * m_SSLWs[neigh.getIndexInDataset()];
				mStats.updateTempStatistics(targetIndex, isStdClassification, tuple, data, neigh,
				        neighWeightNonnormalized, trueIndex, targetValue);

				if (neighbour + 1 == m_NbNeighbours[numNeighInd]) {
					mStats.updateStatistics(targetIndex, numNeighInd, sumNeighbourWeights);
					numNeighInd++;
				}
			}
		}
		return tempInsufficientNbNeighbours;
	}

	private boolean[] computeStandardClassification() {
		boolean[] isPerTarget = new boolean[m_NbGeneralisedTargetAttrs];

		ReliefStatisticsType s = getSettings().getRelief().getReliefStatisticsType();
		if (!s.equals(ReliefStatisticsType.DistanceClassic)) {
			return isPerTarget;
		}
		else if (m_MissingTargetHandling.equals(MissingTargetHandling.UseDescriptive)) {
			ClusLogger.info("Using Descriptive distances make us use regressive definition of the Relief.");
			return isPerTarget;
		}
		isPerTarget[0] = m_NbTargetAttrs == 1 && m_DescriptiveTargetAttr[TARGET_SPACE][0] instanceof NominalAttrType; // overall
		                                                                                                              // ranking
		for (int targetIndex = 1; targetIndex < m_NbGeneralisedTargetAttrs; targetIndex++) {
			isPerTarget[targetIndex] = m_DescriptiveTargetAttr[TARGET_SPACE][targetIndex
			        - 1] instanceof NominalAttrType;
		}
		return isPerTarget;
	}

	private int[] nbTargetValues() {
		int[] nbValues = new int[m_IsStandardClassification.length];
		for (int targetIndex = -1; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++) {
			int trueIndex = getTrueTargetIndex(targetIndex);
			nbValues[targetIndex + 1] = m_IsStandardClassification[targetIndex + 1]
			        ? ((NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex]).getNbValues()
			        : 1;
		}
		return nbValues;
	}

	/**
	 * Computes normalised counts of each class value, for a given target.
	 * 
	 * @param nominalTargetIndex
	 *            if -1, this is the overall ranking, hence STC is the task at hand.
	 *            Otherwise, we are computing statistics that are used in per-target
	 *            ranking.
	 * @return A list with number of examples with a particular value of the target.
	 *         In the last place, we have the number of examples whose value of the
	 *         target is missing. The counts for non-missing values are normalised
	 *         by the number of examples with non-missing value for the target.
	 */
	private double[] nominalClassCounts(int nominalTargetIndex) {
		int nbValues = m_IsMLC ? 2 : m_NbTargetValues[nominalTargetIndex + 1];
		double[] targetProbabilities = new double[nbValues + 1]; // one additional place for missing values
		int trueIndex = getTrueTargetIndex(nominalTargetIndex);
		NominalAttrType attr = (NominalAttrType) m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex];
		for (int example = 0; example < m_NbExamples; example++) {
			targetProbabilities[attr.getNominal(m_Data.getTuple(example))] += 1.0;
		}
		if (m_NbExamples > MathUtil.C1E_9 + targetProbabilities[nbValues]) { // otherwise: targetProbabilities = {0, 0,
		                                                                     // ... , 0, m_NbExamples}
			// Normalise probabilities: examples with unknown targets are effectively
			// ignored
			// The formula for standard classification class weighting still holds, i.e.,
			// sum over other classes of
			// the terms P(other class) / (1 - P(class)), equals 1
			for (int value = 0; value < nbValues; value++) {
				targetProbabilities[value] /= m_NbExamples - targetProbabilities[nbValues];
			}
		}
		return targetProbabilities;
	}

	@Deprecated
	/**
	 * Computes the nearest neighbours of example with index {@code tupleInd} in the
	 * dataset.
	 * 
	 * @param tupleInd
	 *            Row index of the example in the dataset {@code data}, whose
	 *            nearest neighbours are computed.
	 * @return An array of {@code m_NbTargetValues} arrays of
	 *         {@link NearestNeighbour}s. Each of the arrays belongs to one target
	 *         value and is sorted decreasingly with respect to the
	 *         distance(neighbour, considered tuple).
	 * @throws ClusException
	 * @throws InterruptedException
	 */
	public NearestNeighbour[][] findNearestNeighbours(int tupleInd, int targetIndex)
	        throws ClusException, InterruptedException {
		DataTuple tuple = m_Data.getTuple(tupleInd);
		boolean isStdClassification = m_IsStandardClassification[targetIndex + 1];
		int nbTargetValues = m_NbTargetValues[targetIndex + 1];
		int trueIndex = getTrueTargetIndex(targetIndex); // -1 ---> 0 in the case of STC

		int[][] neighbours = new int[nbTargetValues][m_MaxNbNeighbours]; // current candidates
		double[] distances = new double[m_NbExamples]; // distances[i] = distance(tuple, data.getTuple(i))
		int[] whereToPlaceNeigh = new int[nbTargetValues];
		int targetValue;

		for (int i = 0; i < m_NbExamples; i++) {
			distances[i] = computeDistance(tuple, m_Data.getTuple(i), DESCRIPTIVE_SPACE);
		}
		boolean sortingNeeded;
		boolean isSorted[] = new boolean[nbTargetValues]; // isSorted[target value]: tells whether the neighbours for
		                                                  // target value are sorted
		for (int i = 0; i < m_NbExamples; i++) {
			sortingNeeded = false;
			if (i != tupleInd) {
				targetValue = isStdClassification
				        ? m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex].getNominal(m_Data.getTuple(i))
				        : 0;
				if (targetValue < nbTargetValues) { // non-missing
					if (whereToPlaceNeigh[targetValue] < m_MaxNbNeighbours) {
						neighbours[targetValue][whereToPlaceNeigh[targetValue]] = i;
						whereToPlaceNeigh[targetValue]++;
						if (whereToPlaceNeigh[targetValue] == m_MaxNbNeighbours) {
							// the list of neighbours has just become full ---> sort it: O(NbNeighbours^2)
							// ...
							bruteForceSort(neighbours[targetValue], m_MaxNbNeighbours, distances);
							isSorted[targetValue] = true;
						}
					} else {
						sortingNeeded = true;
					}
				} else {
					// nothing to do here
				}
				if (sortingNeeded) {
					if (distances[i] >= distances[neighbours[targetValue][0]]) {
						continue;
					}
					int j; // here the branch prediction should kick-in
					for (j = 1; j < m_MaxNbNeighbours && distances[i] < distances[neighbours[targetValue][j]]; j++) {
						neighbours[targetValue][j - 1] = neighbours[targetValue][j];
					}
					neighbours[targetValue][j - 1] = i;
					isSorted[targetValue] = true;
				}
			}
		}
		NearestNeighbour[][] nearestNeighbours = new NearestNeighbour[nbTargetValues][];
		for (int value = 0; value < nbTargetValues; value++) {
			nearestNeighbours[value] = new NearestNeighbour[whereToPlaceNeigh[value]];
			if (!isSorted[value]) {
				bruteForceSort(neighbours[value], whereToPlaceNeigh[value], distances);
			}

			for (int i = 0; i < whereToPlaceNeigh[value]; i++) {
				int datasetIndex = neighbours[value][i];
				double descriptiveSpaceDist = distances[neighbours[value][i]];
				nearestNeighbours[value][whereToPlaceNeigh[value] - i - 1] = new NearestNeighbour(datasetIndex,
				        descriptiveSpaceDist);
			}
		}
		int currentCount = updateNeighCount();
		printProgressParallel(currentCount, m_NeighCounterBound);
		return nearestNeighbours;
	}

	public HashMap<Integer, NearestNeighbour[][]> findNearestNeighbours(int tupleInd,
	        ArrayList<Integer> necessaryTargetIndices) throws ClusException, InterruptedException {
		int currentCount;
		DataTuple tuple = m_Data.getTuple(tupleInd);
		double[] distances = new double[m_NbExamples]; // distances[i] = distance(tuple, data.getTuple(i))
		for (int i = 0; i < m_NbExamples; i++) {
			distances[i] = computeDistance(tuple, m_Data.getTuple(i), DESCRIPTIVE_SPACE);
		}
		// helping thingies
		HashMap<Integer, int[][]> neighbourss = new HashMap<>(); // current candidates
		int[][] neighbours;
		HashMap<Integer, int[]> whereToPlaceNeighs = new HashMap<>();
		int[] whereToPlaceNeigh;
		int nbTargetValues;
		int trueIndex;
		HashMap<Integer, boolean[]> isSorteds = new HashMap<>(); // isSorteds[targetIndex][targetValue]: tells whether
		                                                         // the neighbours
		                                                         // for the targetValue-th value of the targetIndex-th
		                                                         // target are sorted
		boolean[] isSorted;
		int targetValue;
		boolean sortingNeeded;
		boolean isStdClassification;
		// initialize structures
		for (int targetIndex : necessaryTargetIndices) {
			nbTargetValues = m_NbTargetValues[targetIndex + 1];
			neighbourss.put(targetIndex, new int[nbTargetValues][m_MaxNbNeighbours]);
			whereToPlaceNeighs.put(targetIndex, new int[nbTargetValues]);
			isSorteds.put(targetIndex, new boolean[nbTargetValues]);
		}

		for (int targetIndex : necessaryTargetIndices) {
			isStdClassification = m_IsStandardClassification[targetIndex + 1];
			nbTargetValues = m_NbTargetValues[targetIndex + 1];
			trueIndex = getTrueTargetIndex(targetIndex); // -1 ---> 0 in the case of STC
			neighbours = neighbourss.get(targetIndex); // new int[nbTargetValues][m_MaxNbNeighbours];
			whereToPlaceNeigh = whereToPlaceNeighs.get(targetIndex); // new int[nbTargetValues];
			isSorted = isSorteds.get(targetIndex); // new boolean[nbTargetValues];
			for (int i = 0; i < m_NbExamples; i++) {
				sortingNeeded = false;
				if (i != tupleInd) {
					targetValue = isStdClassification
					        ? m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex].getNominal(m_Data.getTuple(i))
					        : 0;
					if (targetValue < nbTargetValues) { // non-missing in classification or regression or semi-supervised
						if (whereToPlaceNeigh[targetValue] < m_MaxNbNeighbours) {
							neighbours[targetValue][whereToPlaceNeigh[targetValue]] = i;
							whereToPlaceNeigh[targetValue]++;
							if (whereToPlaceNeigh[targetValue] == m_MaxNbNeighbours) {
								bruteForceSort(neighbours[targetValue], m_MaxNbNeighbours, distances);
								isSorted[targetValue] = true;
							}
						} else {
							sortingNeeded = true;
						}
					} else {
						// nothing to do here
					}
					if (sortingNeeded) {
						if (distances[i] >= distances[neighbours[targetValue][0]]) {
							continue;
						}
						int j; // here the branch prediction should kick-in
						for (j = 1; j < m_MaxNbNeighbours
						        && distances[i] < distances[neighbours[targetValue][j]]; j++) {
							neighbours[targetValue][j - 1] = neighbours[targetValue][j];
						}
						neighbours[targetValue][j - 1] = i;
						isSorted[targetValue] = true;
					}
				}
			}
			currentCount = updateNeighCount();
			printProgressParallel(currentCount, m_NeighCounterBound);
		}
		// create NearestNeighbour objects
		HashMap<Integer, NearestNeighbour[][]> nearestNeighbourss = new HashMap<>(); // = new
		                                                                             // NearestNeighbour[nbTargetValues][];
		NearestNeighbour[][] nearestNeighbours;
		for (int targetIndex : necessaryTargetIndices) {
			nbTargetValues = m_NbTargetValues[targetIndex + 1];
			nearestNeighbours = new NearestNeighbour[nbTargetValues][];
			nearestNeighbourss.put(targetIndex, nearestNeighbours);
			whereToPlaceNeigh = whereToPlaceNeighs.get(targetIndex);
			isSorted = isSorteds.get(targetIndex);
			neighbours = neighbourss.get(targetIndex);
			for (int value = 0; value < nbTargetValues; value++) {
				nearestNeighbours[value] = new NearestNeighbour[whereToPlaceNeigh[value]];
				if (!isSorted[value]) {
					bruteForceSort(neighbours[value], whereToPlaceNeigh[value], distances);
				}

				for (int i = 0; i < whereToPlaceNeigh[value]; i++) {
					int datasetIndex = neighbours[value][i];
					double descriptiveSpaceDist = distances[neighbours[value][i]];
					nearestNeighbours[value][whereToPlaceNeigh[value] - i - 1] = new NearestNeighbour(datasetIndex,
					        descriptiveSpaceDist);
				}
			}
		}
		return nearestNeighbourss;
	}

	public double findDistanceToFirstNotUnlabeledNeighbour(int tupleInd) throws ClusException {
		DataTuple tuple = m_Data.getTuple(tupleInd);
		if (!tuple.isUnlabeled()) {
			return 0.0;
		}
		double minDistance = Double.POSITIVE_INFINITY;
		double distance;
		for (int i = 0; i < m_NbExamples; i++) {
			if (m_Data.getTuple(i).isUnlabeled()) { // i == tupleInd can be dropped due to iff above
				continue;
			}
			distance = computeDistance(tuple, m_Data.getTuple(i), DESCRIPTIVE_SPACE);
			if (distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}

	private static void bruteForceSort(int[] neighs, int upperBound, double[] distances) {
		for (int ind1 = 0; ind1 < upperBound; ind1++) {
			for (int ind2 = ind1 + 1; ind2 < upperBound; ind2++) {
				if (distances[neighs[ind1]] < distances[neighs[ind2]]) {
					int temp = neighs[ind1];
					neighs[ind1] = neighs[ind2];
					neighs[ind2] = temp;
				}
			}
		}
	}

	@Deprecated
	@SuppressWarnings("unused")
	private double computeDistanceOld(DataTuple t1, DataTuple t2, int space) throws ClusException {
		double dist = 0.0;
		if (m_IsMLC && space == TARGET_SPACE) {
			return m_MLCDist.calculateDist(t1, t2);
		} else {
			int dimensions = space == DESCRIPTIVE_SPACE ? m_NbDescriptiveAttrs : m_NbTargetAttrs;
			ClusAttrType attr;
			for (int attrInd = 0; attrInd < dimensions; attrInd++) {
				attr = m_DescriptiveTargetAttr[space][attrInd];
				dist += computeDistance1D(t1, t2, attr);
			}
			return dist / dimensions;
		}

	}

	/**
	 * Distance between tuples in the subspace {@code space}.
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param space
	 *            0 or 1; if 0, subspace is descriptive space and target space
	 *            otherwise.
	 * @return Distance between {@code t1} and {@code t2} in the given subspace.
	 * @throws ClusException
	 */
	public double computeDistance(DataTuple t1, DataTuple t2, int space) throws ClusException {
		double dist = 0.0;
		if (m_IsMLC && space == TARGET_SPACE) {
			return m_MLCDist.calculateDist(t1, t2);
		} else {
			int dimensionsFull = space == DESCRIPTIVE_SPACE ? m_NbDescriptiveAttrs : m_NbTargetAttrs;
			// necessary types: all (dense) or non-numeric (sparse)
			int dimensions = m_DistanceDescriptiveTargetAttr[space].length;
			ClusAttrType attr;
			for (int attrInd = 0; attrInd < dimensions; attrInd++) {
				attr = m_DescriptiveTargetAttr[space][m_DistanceDescriptiveTargetAttr[space][attrInd]];
				dist += computeDistance1D(t1, t2, attr);
			}
			// if sparse, non-zero numeric attributes must be added
			if (m_IsSparse) {
				Set<Integer> inds1 = ((SparseDataTuple) t1).getAttributeIndicesSet();
				Set<Integer> inds2 = ((SparseDataTuple) t2).getAttributeIndicesSet();
				HashSet<Integer> inds = new HashSet<>(inds1);
				inds.addAll(inds2);
				ClusSchema s = t1.getSchema();
				for (int ind : inds) {
					attr = s.getAttrType(ind);
					dist += computeDistance1D(t1, t2, attr); // computeNumeric1D may be not general enough in the
					                                         // future:)
				}
			}
			return dist / dimensionsFull;
		}
	}

	/**
	 * Calculates the distance between to tuples in a given component {@code attr}.
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param attr
	 *            The attribute/dimension in which the distance between {@code t1}
	 *            and {@code t2} is computed.
	 * @return distance({@code attr.value(t1), attr.value(t2)})
	 * @throws ClusException
	 */
	public double computeDistance1D(DataTuple t1, DataTuple t2, ClusAttrType attr) throws ClusException {
		if (attr.isNominal()) {
			return computeNominalDist1D(t1, t2, (NominalAttrType) attr);
		} else if (attr.isNumeric()) {
			double normFactor = m_numMaxs.get(attr.getName()) - m_numMins.get(attr.getName());
			if (normFactor == 0.0) { // if and only if the attribute has only one value ... Distance will be zero and
			                         // does not depend on normFactor
				normFactor = 1.0;
			}
			return computeNumericDist1D(t1, t2, (NumericAttrType) attr, normFactor);
		} else if (attr.isClasses()) {
			return computeHierarchicalDist1D(t1, t2, (ClassesAttrType) attr);
		} else if (attr.isTimeSeries()) {
			return computeTimeSeriesDist1D(t1, t2, (TimeSeriesAttrType) attr);
		} else if (attr.isString()) {
			return computeStringDist1D(t1, t2, (StringAttrType) attr);
		} else {
			throw new ClusException(
			        "Unknown attribute type for attribute " + attr.getName() + ": " + attr.getClass().toString());
		}

	}

	/**
	 * Calculates distance between the nominal values of the component {@code attr}.
	 * In the case of missing values, we follow Weka's solution and not the paper
	 * Theoretical and Empirical Analysis of ReliefF and RReliefF, by Robnik Sikonja
	 * and Kononenko (time complexity ...).
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param attr
	 *            The nominal attribute/dimension in which the distance between
	 *            {@code t1} and {@code t2} is computed.
	 * @return distance({@code attr.value(t1), attr.value(t2)})
	 */
	public double computeNominalDist1D(DataTuple t1, DataTuple t2, NominalAttrType attr) {
		int v1 = attr.getNominal(t1);
		int v2 = attr.getNominal(t2);
		if (v1 >= attr.getNbValues() || v2 >= attr.getNbValues()) { // at least one missing
			return 1.0 - 1.0 / attr.getNbValues();
		} else {
			return v1 == v2 ? 0.0 : 1.0;
		}
	}

	/**
	 * Calculates distance between the numeric values of the component {@code attr}.
	 * In the case of missing values, we follow Weka's solution and not the paper
	 * Theoretical and Empirical Analysis of ReliefF and RReliefF, by Robnik Sikonja
	 * and Kononenko (time complexity ...).
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param attr
	 *            The numeric attribute/dimension in which the distance between
	 *            {@code t1} and {@code t2} is computed.
	 * @param normalizationFactor
	 *            Typically,
	 *            <p>
	 *            {@code normalizationFactor = 1 / (m_numMaxs[attr.name()] - m_numMins[attr.name()])}.
	 * @return If {@code v1} and {@code v2} are the numeric values of the attribute
	 *         {@code attr} for the instances {@code t1} and {@code t2}, the value
	 *         <p>
	 *         {@code |v1 - v2| / normalizationFactor}
	 *         <p>
	 *         is returned.
	 */
	public double computeNumericDist1D(DataTuple t1, DataTuple t2, NumericAttrType attr, double normalizationFactor) {
		double v1 = attr.getNumeric(t1);
		double v2 = attr.getNumeric(t2);
		double t;
		if (isMissing(v1)) { // t1.hasNumMissing(attr.getArrayIndex())
			if (isMissing(v2)) { // t2.hasNumMissing(attr.getArrayIndex())
				t = BOTH_MISSING_DIST;
			} else {
				t = (v2 - m_numMins.get(attr.getName())) / normalizationFactor;
				t = Math.max(t, 1.0 - t);
			}
		} else {
			if (isMissing(v2)) {
				t = (v1 - m_numMins.get(attr.getName())) / normalizationFactor;
				t = Math.max(t, 1.0 - t);
			} else {
				t = Math.abs(v1 - v2) / normalizationFactor;
			}
		}
		return t;
	}

	/**
	 * Calculates the distance between hierarchical sets of labels of data tuples t1
	 * and t2.
	 * 
	 * @param t1
	 * @param t2
	 * @param attr
	 * 
	 */
	public double computeHierarchicalDist1D(DataTuple t1, DataTuple t2, ClassesAttrType attr) {
		return m_HierarMLCDist.calculateDist(t1, t2, attr);
	}

	/**
	 * Computes distance between the time series values of the component
	 * {@code attr}.
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param attr
	 *            The time series attribute/dimension in which the distance between
	 *            {@code t1} and {@code t2} is computed.
	 * @return distance({@code attr.value(t1), attr.value(t2)})
	 * @throws ClusException
	 */
	public double computeTimeSeriesDist1D(DataTuple t1, DataTuple t2, TimeSeriesAttrType attr) throws ClusException {
		TimeSeries ts1 = attr.getTimeSeries(t1);
		TimeSeries ts2 = attr.getTimeSeries(t2);

		switch (m_TimeSeriesDistance) {
		case DTW:
			return new DTWTimeSeriesDist(attr).calcDistance(t1, t2);
		case QDM:
			if (ts1.length() == ts2.length()) {
				return new QDMTimeSeriesDist(attr).calcDistance(t1, t2);
			} else {
				throw new ClusException("QDM Distance is not implemented for time series with different length");
			}
		case TSC:
			return new TSCTimeSeriesDist(attr).calcDistance(t1, t2);
		default:
			throw new ClusException("ClusReliefFeatureRanking.m_TimeSeriesDistance was not set to any known value.");
		}

	}

	/**
	 * Computes Levenshtein's distance between the string values of the component
	 * {@code attr}.
	 * 
	 * @param t1
	 *            The first tuple
	 * @param t2
	 *            The second tuple
	 * @param attr
	 *            The string attribute/dimension in which the distance between
	 *            {@code t1} and {@code t2} is computed.
	 * @return Levenshtein distance between {@code attr.value(t1)} and
	 *         {@code attr.value(t2)}.
	 */
	public double computeStringDist1D(DataTuple t1, DataTuple t2, StringAttrType attr) {
		return new Levenshtein(t1, t2, attr).getDist();
	}

	/**
	 * Computes a random permutation with Fisher-Yates algorithm.
	 * 
	 * @param examples
	 *            The number of examples that we will place in a random order. *
	 * @return A permutation given as a list whose i-th element is the index of the
	 *         example that is processed in i-th iteration.
	 */
	private int[] randomPermutation(int examples) {
		int[] permuted = new int[examples];
		// fill
		for (int i = 0; i < permuted.length; i++) {
			permuted[i] = i;
		}
		// shuffle
		for (int i = permuted.length - 1; i > 0; i--) {
			int ind = m_rnd.nextInt(i + 1);
			int temp = permuted[ind];
			permuted[ind] = permuted[i];
			permuted[i] = temp;
		}
		return permuted;
	}

	public void setReliefFimpHeader(ArrayList<String> names) {
		setFimpHeader(fimpTableHeader(names));
	}

	public void setReliefDescription(int[] neighbours, int[] iterations) {
		String first = "Ranking method: Relief with all combinations of";
		String second = String.format("numbers of neighbours: %s", Arrays.toString(neighbours));
		String third = String.format("numbers of iterations: %s", Arrays.toString(iterations));
		setRankingDescription(String.join("\n", new String[] { first, second, third }));
	}

	private void printProgress(int iteration) {
		double proportion = 100 * (double) (iteration + 1) / (m_MaxNbIterations);
		int verbosity = getSettings().getGeneral().getVerbose();
		if (verbosity > 0) {
			while (m_Percents < proportion && m_Percents < 100) {
				System.out.print(".");
				m_Percents++;
				if (m_Percents / 10 * 10 == m_Percents) {
					ClusLogger.info(String.format(" %3d percents", m_Percents));
				}
			}
		}
	}

	private synchronized void printProgressParallel(int counterVal, int counterBound) throws InterruptedException {
		double proportion = 100 * (double) (counterVal + 1) / (counterBound);
		int verbosity = getSettings().getGeneral().getVerbose();
		if (verbosity > 0) {
			m_CounterLock.writingLock();
			while (m_NeighPercents < proportion && m_NeighPercents < 100) {
				System.out.print(".");
				m_NeighPercents++;
				if (m_NeighPercents / 10 * 10 == m_NeighPercents) {
					ClusLogger.info(String.format(" %3d percents", m_NeighPercents));
				}
			}
			m_CounterLock.writingUnlock();
		}
	}

	private synchronized int updateNeighCount() throws InterruptedException {
		m_CounterLock.writingLock();
		m_NeighCounter++;
		int current = m_NeighCounter;
		m_CounterLock.writingUnlock();
		return current;
	}

	/**
	 * Converts {@code -1 <= target index <} {@link #m_NbTargetAttrs} to the index
	 * that corresponds to given target in the
	 * {@link #m_DescriptiveTargetAttr}[{@link #TARGET_SPACE}]
	 * 
	 * @param targetIndex
	 * 
	 */
	private int getTrueTargetIndex(int targetIndex) {
		if (m_IsStandardClassification[0]) {
			return targetIndex + 1;
		} else {
			return targetIndex;
		}
	}

	/**
	 * Returns the indices of the target attributes that correspond to
	 * <ul>
	 * <li>the overall ranking ({@code = -1}) - always present, or</li>
	 * <li>the standard classification case ({@code >= 0}) - at least 0 such
	 * indices.</li>
	 * </ul>
	 * 
	 * 
	 */
	private ArrayList<Integer> computeTargetIndicesThatNeedNeigbhours() {
		ArrayList<Integer> answer = new ArrayList<Integer>();
		answer.add(-1); // need overall ranking in every case
		for (int targetIndex = 0; targetIndex < m_NbGeneralisedTargetAttrs - 1; targetIndex++) {
			if (m_IsStandardClassification[targetIndex + 1]) { // STC that is not overall needs update of the neighbours
				answer.add(targetIndex);
			}
		}
		return answer;
	}

	/**
	 * Computes (or loads) all nearest neighbours in advance, i.e., before the main
	 * iterative part of Relief. Updates the field {@link #m_NearestNeighbours}
	 * accordingly.
	 * 
	 * @param randomPermutation
	 *            the order in which the instances will be used in the Relief
	 *            algorithm.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws ClusException
	 */
	private void computeNearestNeighbours(int[] randomPermutation)
	        throws InterruptedException, ExecutionException, IOException, ClusException {
		if (m_ShouldLoadNeighbours) {
			// read from file
			printMessage("Loading nearest neighbours ...", 1, getSettings().getGeneral().getVerbose());
			SaveLoadNeighbours nnLoader = new SaveLoadNeighbours(m_LoadNearestNeigbhoursFiles, null);
			m_NearestNeighbours = nnLoader.loadNeighboursFromFiles();
			// recomputeNeighbourTargetDistances(); // compute target distances again
			// (necessary for MLC)
		} else {
			// compute them now
			printMessage("Calculating nearest neighbours ...", 1, getSettings().getGeneral().getVerbose());
			ArrayList<Integer> necessaryTargetIndices = computeTargetIndicesThatNeedNeigbhours();
			m_NeighCounter = 0;
			m_NeighCounterBound = necessaryTargetIndices.size() * randomPermutation.length;
			m_NeighPercents = 0;

			ExecutorService executor = Executors.newFixedThreadPool(m_NbThreads);
			ArrayList<Future<Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>>> results = new ArrayList<Future<Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>>>();
			for (Integer targetIndex : necessaryTargetIndices) {
				m_NearestNeighbours.put(targetIndex, new HashMap<Integer, NearestNeighbour[][]>());
			}
			for (int tupleIndex : randomPermutation) {
				DataTuple tuple = m_Data.getTuple(tupleIndex);
				ArrayList<Integer> appropriateTargets = new ArrayList<>();
				for (Integer targetIndex : necessaryTargetIndices) {
					if (shouldUseTuple(targetIndex, tuple)) {
						appropriateTargets.add(targetIndex);
					}
				}
				FindNeighboursCallable task = new FindNeighboursCallable(this, tupleIndex, appropriateTargets);
				Future<Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>> result = executor
				        .submit(task);
				results.add(result);
			}
			executor.shutdown();
			executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			for (Future<Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>> futureTriple : results) {
				Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>> triple = futureTriple.get();
				for (Integer targetIndex : triple.getFirst()) {
					m_NearestNeighbours.get(targetIndex).put(triple.getSecond(), triple.getThird().get(targetIndex));
				}
			}
		}

		if (m_ShouldSaveNeighbours) {
			SaveLoadNeighbours nnSaver = new SaveLoadNeighbours(null, m_SaveNearestNeigbhoursFile);
			nnSaver.saveNeighboursToFile(m_NearestNeighbours);
		}
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void recomputeNeighbourTargetDistances() throws ClusException {
		for (int targetIndex : m_NearestNeighbours.keySet()) {
			for (int tupleIndex : m_NearestNeighbours.get(targetIndex).keySet()) {
				DataTuple t1 = m_Data.getTuple(tupleIndex);
				NearestNeighbour[][] nnss = m_NearestNeighbours.get(targetIndex).get(tupleIndex);
				for (NearestNeighbour[] nns : nnss) {
					for (NearestNeighbour nn : nns) {
						DataTuple t2 = m_Data.getTuple(nn.getIndexInDataset());
						nn.setTargetDistance(computeDistance(t1, t2, TARGET_SPACE));
					}
				}
			}
		}
	}

	/**
	 * Prints the message if verbose level in the settings file is at the desired
	 * verbose level or higher.
	 * 
	 * @param message
	 * @param verboseLevel
	 */
	public static void printMessage(String message, int desiredVerboseLevel, int verboseLevel) {
		if (desiredVerboseLevel <= verboseLevel) {
			ClusLogger.info(message);
		}
	}

	/**
	 * Tells whether the given instance should be considered for the given target
	 * when computing the ranking.
	 * 
	 * @param targetIndex
	 * @param tuple
	 * 
	 */
	private boolean shouldUseTuple(int targetIndex, DataTuple tuple) {
		int trueIndex = getTrueTargetIndex(targetIndex);
		return !(m_IsStandardClassification[targetIndex + 1]
		        && m_DescriptiveTargetAttr[TARGET_SPACE][trueIndex].isMissing(tuple));
	}

	public String getMultilabelDistance() {
		return m_MLCDist.distanceName();
	}

	private static boolean isMissing(double val) {
		return val == Double.POSITIVE_INFINITY;
	}

	public ClusAttrType getDescriptiveAttribute(int attrInd) {
		return m_DescriptiveTargetAttr[DESCRIPTIVE_SPACE][attrInd];
	}

	public ClusAttrType getTargetAttribute(int attrInd) {
		return m_DescriptiveTargetAttr[TARGET_SPACE][attrInd];
	}

	public double getTargetProbability(int targetIndex, int targetValue) {
		return m_TargetProbabilities[targetIndex + 1][targetValue];
	}

	public int getMaxNbIterations() {
		return m_MaxNbIterations;
	}
}
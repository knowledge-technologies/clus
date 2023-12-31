
package si.ijs.kt.clus.ext.featureRanking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.MisclassificationError;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.error.hmlc.HierErrorMeasures;
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
import si.ijs.kt.clus.error.mlc.MicroPrecision;
import si.ijs.kt.clus.error.mlc.MicroRecall;
import si.ijs.kt.clus.error.mlc.OneError;
import si.ijs.kt.clus.error.mlc.RankingLoss;
import si.ijs.kt.clus.error.mlc.SubsetAccuracy;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.ClusReadWriteLock;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleMethod;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleRanking;
import si.ijs.kt.clus.main.settings.section.SettingsHMLC.HierarchyMeasures;
import si.ijs.kt.clus.main.settings.section.SettingsMLC.MultiLabelMeasures;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.selection.OOBSelection;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.StringUtils;
import si.ijs.kt.clus.util.tuple.Triple;

public class ClusFeatureRanking {

	/**
	 * The keys are attribute names and the values are the arrays of the following
	 * form:<br>
	 * {@code [type of the attribute, position of the attribute, relevance1, relevance2, ..., relevanceK]}<br>
	 * where {@code K >= 1} and each relevance corresponds to some ranking.
	 */
	protected final HashMap<String, double[]> m_AllAttributes;

	private enum FimpOrdering {
		AS_IN_DATASET, BY_RELEVANCE
	};

	private FimpOrdering m_Order;

	private TreeMap<Double, ArrayList<Triple<String, int[], double[]>>> m_OutputRanking;
	/**
	 * Hash map of pairs attribute name: (double) index of the attribute in the
	 * dataset, where indices are 1-based.
	 */
	private HashMap<String, Double> m_AttributeDatasetIndices;
	/**
	 * Hash map of pairs attribute name: [rank in the 1st ranking, rank in the 2nd
	 * ranking, ...], where ranks are 1-based.
	 */
	private HashMap<String, int[]> m_AttributeRanks;

	/**
	 * Description of the ranking that appears in the first lines of the .fimp file
	 */
	private String m_RankingDescription;

	/** Header for the table of relevances in the .fimp file */
	protected String m_FimpTableHeader;

	ClusReadWriteLock m_Lock;

	private int m_NbFeatureRankings;

	private Settings m_Settings;

	private boolean m_ScoresNormalized = false;

	protected boolean m_IsEnsembleRanking = false;

	/**
	 * Separator that separates different blocks of columns in fimp file. Must not
	 * be equal to "," (see parsing in the unit tests)!
	 */
	public final static String FIMP_SEPARATOR = "\t";

	public final static int FIMP_RELEVANCES_SIGNIFICANT_PLACES = 10;

	public ClusFeatureRanking(Settings sett) {
		m_AllAttributes = new HashMap<String, double[]>();
		m_Lock = new ClusReadWriteLock();
		m_AttributeDatasetIndices = new HashMap<String, Double>();
		m_OutputRanking = new TreeMap<Double, ArrayList<Triple<String, int[], double[]>>>();
		m_Settings = sett;
	}

	public final Settings getSettings() {
		return m_Settings;
	}

	public void initializeAttributes(ClusAttrType[] descriptive, int nbRankings) {
		int num = -1;
		int nom = -1;
		// ClusLogger.info("NB = "+descriptive.length);
		int[] attributeDatasetIndices = descriptive[0].getSchema().getDescriptive().toIndices();
		for (int i = 0; i < descriptive.length; i++) {
			ClusAttrType type = descriptive[i];
			// type.setDatasetIndex(m_AttributeDatasetIndices[i]);
			m_AttributeDatasetIndices.put(type.getName(), (double) attributeDatasetIndices[i]);
			if (!type.isDisabled()) {
				// double[] info = new double[3];
				double[] info = new double[2 + nbRankings];
				if (type.getAttributeType().equals(AttributeType.Nominal)) {
					nom++;
					info[0] = 0; // type
					info[1] = nom; // order in nominal attributes
				}
				if (type.getAttributeType().equals(AttributeType.Numeric)) {
					num++;
					info[0] = 1; // type
					info[1] = num; // order in numeric attributes
				}
				for (int j = 0; j < nbRankings; j++) {
					info[2 + j] = 0; // current importance
				}
				// System.out.print(type.getName()+": "+info[1]+"\t");
				m_AllAttributes.put(type.getName(), info);
			}
		}
	}

	/**
	 * Writes fimp with where the attributes appear in the order that was chosen (as
	 * in the dataset or sorted by relevance).
	 */
	public void writeRanking(String fname) throws IOException {
		File franking = new File(fname + ".fimp");
		FileWriter wrtr = new FileWriter(franking);

		wrtr.write(m_RankingDescription + "\n");
		wrtr.write(m_FimpTableHeader + "\n");
		wrtr.write(StringUtils.makeString('-', m_FimpTableHeader.length()) + "\n");

		Set<Double> orderedKeys;
		if (m_Order == FimpOrdering.AS_IN_DATASET) {
			orderedKeys = m_OutputRanking.keySet();
		} else {
			orderedKeys = m_OutputRanking.descendingKeySet();
		}
		for (Double key : orderedKeys) {
			ArrayList<Triple<String, int[], double[]>> triples = m_OutputRanking.get(key);
			for (Triple<String, int[], double[]> triple : triples) {
				wrtr.write(fimpTableRow(triple.getFirst(), triple.getSecond(), triple.getThird()) + "\n");
			}
		}
		wrtr.flush();
		wrtr.close();
		if (getSettings().getGeneral().getVerbose() >= 1) {
			ClusLogger.info(String.format("Feature importances written to: %s.fimp", fname));
		}
	}

	/**
	 * Produces a fimp table row of the form datasetIndex of the attribute,
	 * attribute name, ranks, relevances
	 */
	public String fimpTableRow(String attributeName, int[] ranks, double[] relevances) {
		int datasetIndex = (int) Math.round(m_AttributeDatasetIndices.get(attributeName).doubleValue());
		String[] output = new String[] { Integer.toString(datasetIndex), attributeName, Arrays.toString(ranks),
		        Arrays.toString(relevances) };
		return String.join(FIMP_SEPARATOR, output);
	}

	public void writeJSON(ClusRun cr) throws IOException {
		Gson jsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		JsonObject functionOutputJSON = new JsonObject();
		ClusSchema schema = cr.getStatManager().getSchema();

		// data specification
		JsonObject dataSpec = new JsonObject();
		JsonArray attributes = new JsonArray();
		JsonArray attributesTarget = new JsonArray();
		JsonArray attributesClustering = new JsonArray();
		JsonArray attributesDescriptive = new JsonArray();
		JsonObject task = new JsonObject();

		for (ClusAttrType a : schema.getAllAttrUse(AttributeUseType.All))
			attributes.add(a.getAttributeJSON());
		for (ClusAttrType a : schema.getAllAttrUse(AttributeUseType.Target))
			attributesTarget.add(new JsonPrimitive(a.getName()));
		for (ClusAttrType a : schema.getAllAttrUse(AttributeUseType.Clustering))
			attributesClustering.add(new JsonPrimitive(a.getName()));
		for (ClusAttrType a : schema.getAllAttrUse(AttributeUseType.Descriptive))
			attributesDescriptive.add(new JsonPrimitive(a.getName()));

		String taskTypeString = (schema.getAllAttrUse(AttributeUseType.Target).length > 1) ? "MT " : "ST ";
		// taskTypeString +=
		// (m_Schema.getAllAttrUse(AttributeUseType.Target)[0].getTypeName()) ? "MT" :
		// "ST";
		if (cr.getStatManager().getTargetMode() == ClusStatManager.Mode.REGRESSION) {
			taskTypeString += "Regression";
		} else if (cr.getStatManager().getTargetMode() == ClusStatManager.Mode.CLASSIFY) {
			taskTypeString += "Classification";
		}
		JsonElement taskType = new JsonPrimitive(taskTypeString);

		task.add("taskType", taskType);
		task.add("taskDescriptiveAttributes", attributesDescriptive);
		task.add("taskTargetAttributes", attributesTarget);
		task.add("taskClusteringAttributes", attributesTarget);

		String queryValue = "Unable to get query details.";
		try {
			String fname = "query.param";
			File f = new File(fname);
			if (f.exists() && f.isFile()) {
				queryValue = new String(Files.readAllBytes(f.toPath()));
			}
		} catch (Exception ex) {
		}

		dataSpec.add("attributes", attributes);
		dataSpec.add("task", task);
		dataSpec.addProperty("query", queryValue);
		functionOutputJSON.add("dataSpecification", dataSpec);

		JsonObject algorithmSpec = new JsonObject();
		JsonElement algorithmName;
		EnsembleMethod ens_method = getSettings().getEnsemble().getEnsembleMethod();
		EnsembleRanking fr_method = getSettings().getEnsemble().getRankingMethods().get(0); // FIXME never ...
		if (ens_method == EnsembleMethod.ExtraTrees) {
			algorithmName = new JsonPrimitive("ExtraTrees/GENIE3");
		} else if ((ens_method == EnsembleMethod.RForest) && (fr_method == EnsembleRanking.RForest)) {
			algorithmName = new JsonPrimitive("RandomForestRanking");
		} else if ((ens_method == EnsembleMethod.RForest) && (fr_method == EnsembleRanking.Genie3)) {
			algorithmName = new JsonPrimitive("RandomForest/GENIE3");
		} else {
			algorithmName = new JsonPrimitive("Ranking method specified incorrectly!");
		}

		int ens_size = getSettings().getEnsemble().getNbBaggingSets().getInt();
		String feat_size = getSettings().getEnsemble().getNbRandomAttrString();

		JsonElement parameters = new JsonPrimitive("Iterations: " + ens_size + "; SelectRandomSubspaces: " + feat_size);

		algorithmSpec.add("name", algorithmName);
		algorithmSpec.add("parameters", parameters);
		algorithmSpec.addProperty("version", "1.0");
		functionOutputJSON.add("algorithmSpecification", algorithmSpec);

		JsonArray rankingResults = new JsonArray();
		// TreeMap<Double, ArrayList<String>> sorted = (TreeMap<Double,
		// ArrayList<String>>) m_FeatureRanks.clone();
		// this was not used: Iterator<Double> iter = sorted.keySet().iterator();
		prepareFeatureRankingForOutput(FimpOrdering.BY_RELEVANCE);
		Set<Double> relevances = m_OutputRanking.descendingKeySet();

		// int count = 1; matejp: ranks are already computed and we should use the same
		// values as in .fimp file
		// while (!sorted.isEmpty()) {
		// double score = sorted.lastKey();
		int rankingIndex = 0; // TODO: allow for more than one ranking
		for (Double relevance : relevances) {
			ArrayList<Triple<String, int[], double[]>> nameRanksRelevancesTriples = m_OutputRanking.get(relevance); // ArrayList<String>
			                                                                                                        // attrs
			                                                                                                        // =
			                                                                                                        // sorted.get(score);
			for (int i = 0; i < nameRanksRelevancesTriples.size(); i++) {
				JsonObject elm = new JsonObject();
				elm.addProperty("attributeName", nameRanksRelevancesTriples.get(i).getFirst());
				elm.addProperty("ordering", nameRanksRelevancesTriples.get(i).getSecond()[rankingIndex]); // elm.addProperty("ordering",
				                                                                                          // count);
				elm.addProperty("importance", nameRanksRelevancesTriples.get(i).getThird()[rankingIndex]);
				// count++;
				rankingResults.add(elm);
			}
			// sorted.remove(sorted.lastKey());
		}

		functionOutputJSON.add("ranking", rankingResults);

		File jsonfile = new File(getSettings().getGeneric().getAppName() + ".json");
		FileWriter json = new FileWriter(jsonfile);
		json.write(jsonBuilder.toJson(functionOutputJSON));
		json.flush();
		json.close();
		ClusLogger.info("JSON model written to: " + getSettings().getGeneric().getAppName() + ".json");

	}

	/**
	 * Implements the Fisher-Yates algorithm for uniform shuffling.
	 * 
	 * @param type
	 *            0 nominal, 1 numeric
	 * @param position
	 *            position at which the attribute whose values are being shuffled,
	 *            is
	 */
	public RowData createRandomizedOOBdata(OOBSelection selection, RowData data, int type, int position, int seed)
	        throws ClusException {
		RowData result = data;
		Random rndm = new Random(seed);
		for (int i = 0; i < result.getNbRows() - 1; i++) {
			// int rnd = i + ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL,
			// result.getNbRows()- i);
			int rnd = i + rndm.nextInt(result.getNbRows() - i);
			DataTuple first = result.getTuple(i);
			DataTuple second = result.getTuple(rnd);
			boolean successfullySwapped = false;
			if (first instanceof SparseDataTuple) {
				if (type == 1) {
					double swap = ((SparseDataTuple) first).getDoubleValueSparse(position);
					((SparseDataTuple) first)
					        .setDoubleValueSparse(((SparseDataTuple) second).getDoubleValueSparse(position), position);
					((SparseDataTuple) second).setDoubleValueSparse(swap, position);
					successfullySwapped = true;
				} else {
					System.err.println(
					        "WARNING: type is not 1 (numeric). We will try to swap the values like in non-sparse case, but some things might go wrong, e.g.,\n"
					                + "java.lang.NullPointerException might occur.");
				}
			}
			if (!successfullySwapped) {
				if (type == 0) {// nominal
					int swap = first.getIntVal(position);
					first.setIntVal(second.getIntVal(position), position);
					second.setIntVal(swap, position);
				} else if (type == 1) {// numeric
					double swap = first.getDoubleVal(position);
					first.setDoubleVal(second.getDoubleVal(position), position);
					second.setDoubleVal(swap, position);
				} else {
					throw new ClusException("Error while making the random permutations for feature ranking!");
				}
			}
		}
		return result;
	}

	/**
	 * Finds all attributes that appear as (part of) the test in a node in the given
	 * tree. Depth first search is used to traverse the tree.
	 * 
	 * @param root
	 *            The root of the tree
	 * @return List of attributes' names
	 */
	public ArrayList<String> getInternalAttributesNames(ClusNode root) {
		ArrayList<String> attributes = new ArrayList<String>(); // list of attribute names in the tree
		ArrayList<ClusNode> stack = new ArrayList<ClusNode>(); // stack of nodes to be processed
		HashSet<String> discovered = new HashSet<String>(); // names that are currently in attributes, used for faster
		                                                    // look-up.
		if (!root.atBottomLevel()) {
			stack.add(root);
		}
		while (stack.size() > 0) {
			ClusNode top = stack.remove(stack.size() - 1);
			String name = top.getTest().getType().getName();
			if (!(discovered.contains(name) || top.atBottomLevel())) { // top not discovered yet and is internal node
				discovered.add(name);
				attributes.add(name);
			}
			for (int i = 0; i < top.getNbChildren(); i++) {
				if (!top.getChild(i).atBottomLevel()) {
					stack.add((ClusNode) top.getChild(i));
				}
			}
		}
		return attributes;
	}

	/**
	 * Calculates values of all error measures.
	 * 
	 * @return {@code [[listOfResultsForErr1, [sign1]], [listOfResultsForErr2, [sign2]], ...]},<br>
	 *         where {@code signI = errorI.shouldBeLow() ? -1.0 : 1.0}, and
	 *         {@code listOfResultsForErr} always contains the overall {@code Err}
	 *         error in the position 0, and possibly also per target calculations
	 *         for {@code error} in the positions i {@literal >} 0.
	 */
	public double[][][] calcAverageErrors(RowData data, ClusModel model, ClusStatManager mgr)
	        throws ClusException, InterruptedException {
		ClusSchema schema = data.getSchema();
		ClusErrorList error = computeErrorList(schema, mgr);
		/* attach model to given schema */
		schema.attachModel(model);
		/* iterate over tuples and compute error */
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			ClusStatistic pred = model.predictWeighted(tuple);
			error.addExample(tuple, pred);
		}
		// if (m_FimpTableHeader == null) {
		// setRForestDescription(error);
		// }
		/* return the average errors */
		double[][][] errors = new double[error.getNbErrors()][2][];
		for (int i = 0; i < errors.length; i++) {
			ClusError currentError = error.getError(i);
			int nbResultsPerError = 1;
			if (mgr.getSettings().getEnsemble().shouldPerformRankingPerTarget()
			        && (currentError instanceof ComponentError)) {
				nbResultsPerError += currentError.getDimension();
			}
			errors[i][0] = new double[nbResultsPerError];
			// compute overall error
			errors[i][0][0] = currentError.getModelError();
			// compute per target errors if necessary
			for (int dim = 1; dim < errors[i][0].length; dim++) {
				errors[i][0][dim] = currentError.getModelErrorComponent(dim - 1);
			}
			// should be low?
			errors[i][1] = new double[] { currentError.shouldBeLow() ? -1.0 : 1.0 };
		}
		return errors;
	}

	public ClusErrorList computeErrorList(ClusSchema schema, ClusStatManager mgr) {
		Settings sett = mgr.getSettings();
		ClusErrorList error = new ClusErrorList();
		NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.Target);
		NominalAttrType[] nom = schema.getNominalAttrUse(AttributeUseType.Target);
		boolean hasNumeric = num.length > 0;
		boolean hasNominal = nom.length > 0;
		// Sometimes, instead of operating with modes (which depend on clustering attributes),
		// we operate with (numbers of) targets directly)
		if (mgr.getTargetMode() == ClusStatManager.Mode.HIERARCHICAL) {
			error.addError(
			        new HierErrorMeasures(error, mgr.getHier(), sett.getHMLC().getRecallValues().getDoubleVector(),
			                HierarchyMeasures.PooledAUPRC, false, getSettings().getOutput().isGzipOutput()));
		} else if (hasNominal && hasNumeric) {
			throw new UnsupportedOperationException("Unsupported! No error measure can handle numeric and nominal targets.");
		} else if (hasNominal) {
			if (sett.getMLC().getSectionMultiLabel().isEnabled()) {
				List<MultiLabelMeasures> measures = sett.getMLC().getMultiLabelRankingMeasures();
				for (MultiLabelMeasures measure : measures) {
					switch (measure) {
					case HammingLoss:
						error.addError(new HammingLoss(error, nom));
						break;
					case MLAccuracy:
						error.addError(new MLAccuracy(error, nom));
						break;
					case MLPrecision:
						error.addError(new MLPrecision(error, nom));
						break;
					case MLRecall:
						error.addError(new MLRecall(error, nom));
						break;
					case MLFOne:
						error.addError(new MLFOneMeasure(error, nom));
						break;
					case SubsetAccuracy:
						error.addError(new SubsetAccuracy(error, nom));
						break;
					case MacroPrecision:
						error.addError(new MacroPrecision(error, nom));
						break;
					case MacroRecall:
						error.addError(new MacroRecall(error, nom));
						break;
					case MacroFOne:
						error.addError(new MacroFOne(error, nom));
						break;
					case MicroPrecision:
						error.addError(new MicroPrecision(error, nom));
						break;
					case MicroRecall:
						error.addError(new MicroRecall(error, nom));
						break;
					case MicroFOne:
						error.addError(new MisclassificationError(error, nom));
						break;
					case OneError:
						error.addError(new OneError(error, nom));
						break;
					case Coverage:
						error.addError(new Coverage(error, nom));
						break;
					case RankingLoss:
						error.addError(new RankingLoss(error, nom));
						break;
					case AveragePrecision:
						error.addError(new AveragePrecision(error, nom));
						break;
					case AverageAUROC:
						error.addError(new MLaverageAUROC(error, nom));
						break;
					case AverageAUPRC:
						error.addError(new MLaverageAUPRC(error, nom));
						break;
					case WeightedAverageAUPRC:
						error.addError(new MLweightedAUPRC(error, nom));
						break;
					case PooledAUPRC:
						error.addError(new MLpooledAUPRC(error, nom));
						break;
					default:
						break;
					}
				}
			} else {
				error.addError(new Accuracy(error, nom));
			}
		} else if (hasNumeric) {
			error.addError(new RMSError(error, num));
		} else {
			throw new UnsupportedOperationException("Unsupported!");
		}
		return error;
	}

	public double[] getAttributeInfo(String attribute) throws InterruptedException {
		m_Lock.readingLock();
		double[] info = Arrays.copyOf(m_AllAttributes.get(attribute), m_AllAttributes.get(attribute).length);
		m_Lock.readingUnlock();
		return info;
	}

	public double getAttributeRelevance(String attribute, int rankingIndex) {
		return m_AllAttributes.get(attribute)[2 + rankingIndex];
	}

	public void putAttributeInfo(String attribute, double[] info) throws InterruptedException {
		m_Lock.writingLock();
		m_AllAttributes.put(attribute, info);
		m_Lock.writingUnlock();
	}

	public void putAttributesInfos(HashMap<String, double[][]> partialFimportances) throws InterruptedException {
		for (String attribute : partialFimportances.keySet()) {
			double[] info = getAttributeInfo(attribute);
			double[][] partialInfo = partialFimportances.get(attribute);
			int ind = 0;
			for (int i = 0; i < partialInfo.length; i++) {
				for (int j = 0; j < partialInfo[i].length; j++) {
					info[ind + 2] += partialInfo[i][j];
					ind++;
				}
			}
			putAttributeInfo(attribute, info);
		}
	}

	public void setFimpHeader(String header) {
		m_FimpTableHeader = header;
	}

	public void setRankingDescription(String descr) {
		m_RankingDescription = descr;
	}

	public String fimpTableHeader(Iterable<? extends CharSequence> list) {
		ArrayList<String> rankNames = new ArrayList<>();
		for (CharSequence elt : list) {
			rankNames.add(elt + "Rank");
		}
		String[] header = new String[] { "attributeDatasetIndex", "attributeName", rankNames.toString(),
		        "[" + String.join(", ", list) + "]" };
		return String.join(FIMP_SEPARATOR, header);
	}

	public String fimpTableHeader(CharSequence... list) {
		ArrayList<String> rankNames = new ArrayList<>();
		for (CharSequence elt : list) {
			rankNames.add(elt + "Rank");
		}
		String[] header = new String[] { "attributeDatasetIndex", "attributeName", rankNames.toString(),
		        "[" + String.join(", ", list) + "]" };
		return String.join(FIMP_SEPARATOR, header);
	}

	public int getNbFeatureRankings() {
		return m_NbFeatureRankings;
	}

	public void setNbFeatureRankings(int nbRankings) {
		m_NbFeatureRankings = nbRankings;
	}

	public void createFimp(ClusRun cr) throws IOException {
		createFimp(cr, "", -1, -1);
	}

	public void createFimp(ClusRun cr, String appendixToFimpName, int expectedNumberTrees, int realNumberOfTrees)
	        throws IOException {
		if (expectedNumberTrees != realNumberOfTrees) {
			// copy the file from resources
			String fileName = "fimp_manipulation.py";
			try {
				InputStream is = si.ijs.kt.clus.Clus.class.getResourceAsStream("/" + fileName);
				String fullFileName = getSettings().getGeneric().getFileAbsolute(fileName);
				Files.copy(is, Paths.get(fullFileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				System.err.println("Error while copying " + fileName + " to the output folder.");
				ex.printStackTrace();
			}
		}

		if (cr.getStatManager().getSettings().getEnsemble().shouldSortRankingByRelevance()) {
			m_Order = FimpOrdering.BY_RELEVANCE;
		} else {
			m_Order = FimpOrdering.AS_IN_DATASET;
		}
		if (m_Order == FimpOrdering.BY_RELEVANCE && getNbFeatureRankings() > 1) {
			if (getSettings().getGeneral().getVerbose() >= 1) {
				System.err.println(
				        "More than one feature ranking will be output. The attributes will be sorted with respect to the first ranking.");
			}
			// m_Order = FimpOrdering.AS_IN_DATASET;
		}
		computeFinalScores(realNumberOfTrees);
		computeRanks();
		prepareFeatureRankingForOutput();

		String appName = cr.getStatManager().getSettings().getGeneric()
		        .getFileAbsolute(cr.getStatManager().getSettings().getGeneric().getAppName()) + appendixToFimpName;
		writeRanking(appName);

		if (cr.getStatManager().getSettings().getOutput().isOutputJSONModel()) {
			writeJSON(cr);
		}
	}

	/**
	 * Computes the final scores (and ranks) and puts the attributes into a sorting
	 * tree, according to the chosen order.
	 */
	private void prepareFeatureRankingForOutput(FimpOrdering ordering) {
		for (String attributeName : m_AllAttributes.keySet()) {
			Double key;
			double[] scores = Arrays.copyOfRange(m_AllAttributes.get(attributeName), 2, 2 + m_NbFeatureRankings);
			if (ordering == FimpOrdering.AS_IN_DATASET) {
				key = m_AttributeDatasetIndices.get(attributeName);
			} else {
				key = scores[0]; // the only relevance or the first ranking if more than one is computed
			}
			if (!m_OutputRanking.containsKey(key)) {
				m_OutputRanking.put(key, new ArrayList<Triple<String, int[], double[]>>());
			}
			m_OutputRanking.get(key).add(
			        new Triple<String, int[], double[]>(attributeName, m_AttributeRanks.get(attributeName), scores));
		}
	}

	private void prepareFeatureRankingForOutput() {
		prepareFeatureRankingForOutput(m_Order);
	}

	/**
	 * Normalizes the feature importance scores.
	 */
	public void computeFinalScores(int nb_trees) {
		for (String attributeName : m_AllAttributes.keySet()) {
			double[] possiblyNonnormalizedScores = m_AllAttributes.get(attributeName); // indeed not normalised in the
			                                                                           // case of ensemble rankings
			for (int j = 0; j < m_NbFeatureRankings; j++) {
				// normalisation
				if (!m_ScoresNormalized && m_IsEnsembleRanking) {
					possiblyNonnormalizedScores[2 + j] /= ClusEnsembleInduce.getMaxNbBags(); // TODO: matejp /nb_trees -
					                                                                         // makes more sense:)
				}
				// rounding: done implicitly only in ranks computation
				// possiblyNonnormalizedScores[2 + j] =
				// ClusUtil.roundToSignificantFigures(possiblyNonnormalizedScores[2
				// + j], FIMP_RELEVANCES_SIGNIFICANT_PLACES);
			}
		}
		m_ScoresNormalized = true;
	}

	/**
	 * Comptues ranks of the features. To avoid the floating-point arithmetics
	 * issues we use approximately equal when computing the ranks ...
	 */
	private void computeRanks() {
		m_AttributeRanks = new HashMap<String, int[]>();
		ArrayList<String> attributeNames = new ArrayList<String>();
		for (String attributeName : m_AllAttributes.keySet()) {
			m_AttributeRanks.put(attributeName, new int[m_NbFeatureRankings]);
			attributeNames.add(attributeName);
		}
		for (int rankingInd = 0; rankingInd < m_NbFeatureRankings; rankingInd++) {
			final int ind = 2 + rankingInd;
			Collections.sort(attributeNames, new Comparator<String>() {

				@Override
				public int compare(String attr1, String attr2) {
					return Double.compare(m_AllAttributes.get(attr2)[ind], m_AllAttributes.get(attr1)[ind]); // decreasing
				}
			});
			int lastRank = -200;
			double prevRelevance = Double.NaN;
			double thisRelevance;
			for (int i = 0; i < attributeNames.size(); i++) {
				String attributeName = attributeNames.get(i);
				thisRelevance = m_AllAttributes.get(attributeName)[ind];
				if (!ClusUtil.eq(thisRelevance, prevRelevance, ClusUtil.PICO)) { // (thisRelevance != prevRelevance){
					lastRank = i + 1;
				}
				m_AttributeRanks.get(attributeName)[rankingInd] = lastRank;
				prevRelevance = thisRelevance;
			}
		}
	}
}

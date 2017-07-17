
package clus.ext.featureRanking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import clus.algo.tdidt.ClusNode;
import clus.data.ClusSchema;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.rows.SparseDataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.primitive.NominalAttrType;
import clus.data.type.primitive.NumericAttrType;
import clus.error.Accuracy;
import clus.error.MisclassificationError;
import clus.error.RMSError;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.error.common.ComponentError;
import clus.error.mlc.AveragePrecision;
import clus.error.mlc.Coverage;
import clus.error.mlc.HammingLoss;
import clus.error.mlc.MLAccuracy;
import clus.error.mlc.MLFOneMeasure;
import clus.error.mlc.MLPrecision;
import clus.error.mlc.MLRecall;
import clus.error.mlc.MLaverageAUPRC;
import clus.error.mlc.MLaverageAUROC;
import clus.error.mlc.MLpooledAUPRC;
import clus.error.mlc.MLweightedAUPRC;
import clus.error.mlc.MacroFOne;
import clus.error.mlc.MacroPrecision;
import clus.error.mlc.MacroRecall;
import clus.error.mlc.MicroPrecision;
import clus.error.mlc.MicroRecall;
import clus.error.mlc.OneError;
import clus.error.mlc.RankingLoss;
import clus.error.mlc.SubsetAccuracy;
import clus.ext.ensembles.ClusEnsembleInduce;
import clus.ext.ensembles.ClusReadWriteLock;
import clus.ext.hierarchical.HierErrorMeasures;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.settings.Settings;
import clus.main.settings.SettingsEnsemble;
import clus.main.settings.SettingsHMLC;
import clus.main.settings.SettingsMLC;
import clus.model.ClusModel;
import clus.selection.OOBSelection;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;
import clus.util.jeans.util.StringUtils;


public class ClusFeatureRanking {

    /**
     * The keys are attribute names and the values are the arrays of the following form:<br>
     * {@code [type of the attribute, position of the attribute, relevance1, relevance2, ..., relevanceK]}<br>
     * where {@code K >= 1} and each relevance corresponds to some ranking.
     */
    protected HashMap<String, double[]> m_AllAttributes;
    // boolean m_FeatRank;
    protected TreeMap<Double, ArrayList<String>> m_FeatureRanks;// sorted by the rank
    HashMap<String, Double> m_FeatureRankByName; // Part of fimp's header

    /** Description of the ranking that appears in the first lines of the .fimp file */
    String m_RankingDescription;
    /** Header for the table of relevances in the .fimp file */
    String m_FimpTableHeader;

    ClusReadWriteLock m_Lock;

    int m_NbFeatureRankings;

    Settings m_Settings;


    public ClusFeatureRanking(Settings sett) {
        m_AllAttributes = new HashMap<String, double[]>();
        m_FeatureRankByName = new HashMap<String, Double>();
        m_FeatureRanks = new TreeMap<Double, ArrayList<String>>();
        m_Lock = new ClusReadWriteLock();
        m_Settings = sett;
    }


    public final Settings getSettings() {
        return m_Settings;
    }


    public void initializeAttributes(ClusAttrType[] descriptive, int nbRankings) {
        int num = -1;
        int nom = -1;
        // System.out.println("NB = "+descriptive.length);
        for (int i = 0; i < descriptive.length; i++) {
            ClusAttrType type = descriptive[i];
            if (!type.isDisabled()) {
                // double[] info = new double[3];
                double[] info = new double[2 + nbRankings];
                if (type.getTypeIndex() == 0) {
                    nom++;
                    info[0] = 0; // type
                    info[1] = nom; // order in nominal attributes
                }
                if (type.getTypeIndex() == 1) {
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


    public void sortFeatureRanks(int numberOfTrees) {
        Iterator<String> iter = m_AllAttributes.keySet().iterator();
        while (iter.hasNext()) {
            String attr = iter.next();
            double score = m_AllAttributes.get(attr)[2] / Math.max(1.0, numberOfTrees);
            // double score = ((double[])m_AllAttributes.get(attr))[2];
            ArrayList<String> attrs = new ArrayList<String>();
            if (m_FeatureRanks.containsKey(score))
                attrs = m_FeatureRanks.get(score);
            attrs.add(attr);
            m_FeatureRanks.put(score, attrs);
        }
    }


    public void convertRanksByName() {
        TreeMap<Double, ArrayList<String>> sorted = (TreeMap<Double, ArrayList<String>>) m_FeatureRanks.clone();
        while (!sorted.isEmpty()) {
            double score = sorted.lastKey();
            ArrayList<String> attrs = new ArrayList<String>();
            attrs = sorted.get(sorted.lastKey());
            for (int i = 0; i < attrs.size(); i++)
                m_FeatureRankByName.put(attrs.get(i), score);
            sorted.remove(sorted.lastKey());
        }
    }


    /**
     * Writes fimp with attributes sorted decreasingly by relevances. This method should be called only if the number of
     * feature rankings is 1.
     * 
     * @param fname
     * @param rankingMethod
     * @throws IOException
     */
    public void writeRanking(String fname, int rankingMethod) throws IOException {
        TreeMap<Double, ArrayList<String>> ranking = (TreeMap<Double, ArrayList<String>>) m_FeatureRanks.clone();

        File franking = new File(fname + ".fimp");
        FileWriter wrtr = new FileWriter(franking);

        wrtr.write(m_RankingDescription + "\n");
        wrtr.write(m_FimpTableHeader + "\n");
        wrtr.write(StringUtils.makeString('-', m_FimpTableHeader.length()) + "\n");
        while (!ranking.isEmpty()) {
            // wrtr.write(sorted.get(sorted.lastKey()) + "\t" + sorted.lastKey()+"\n");
            wrtr.write(writeRow(ranking.get(ranking.lastKey()), ranking.lastKey()));
            ranking.remove(ranking.lastKey());
        }
        wrtr.flush();
        wrtr.close();
        System.out.println(String.format("Feature importances written to: %s.fimp", fname));
    }


    public String writeRow(ArrayList<String> attributes, double value) {
        String output = "";
        for (int i = 0; i < attributes.size(); i++) {
            String attr = (String) attributes.get(i);
            attr = attr.replaceAll("\\[", "");
            attr = attr.replaceAll("\\]", "");
            output += attr + "\t[" + value + "]\n"; // added [ and ] to make fimps look the same, when #rankings == 1 or #rankings > 1.
        }
        return output;
    }


    public void writeRankingByAttributeName(String fname, ClusAttrType[] descriptive, int rankingMethod) throws IOException {
        File franking = new File(fname + ".fimp");
        FileWriter wrtr = new FileWriter(franking);

        wrtr.write(m_RankingDescription + "\n");
        wrtr.write(m_FimpTableHeader + "\n");
        wrtr.write(StringUtils.makeString('-', m_FimpTableHeader.length()) + "\n");
        int nbRankings = m_AllAttributes.get(descriptive[0].getName()).length - 2;
        for (int i = 0; i < descriptive.length; i++) {
            String attribute = descriptive[i].getName();
            double[] values = Arrays.copyOfRange(m_AllAttributes.get(attribute), 2, nbRankings + 2);
            for (int j = 0; j < values.length; j++) {
                values[j] /= Math.max(1.0, ClusEnsembleInduce.getMaxNbBags()); // Relief has 0 number of bags ...
            }
            wrtr.write(attribute + "\t" + Arrays.toString(values) + "\n");
            wrtr.flush();
        }

        /*
         * Iterator iter = m_AllAttributes.keySet().iterator();
         * while (iter.hasNext()){
         * String attr = (String)iter.next();
         * double value = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
         * wrtr.write(attr +"\t"+value+"\n");
         * wrtr.flush();
         * }
         */

        wrtr.flush();
        wrtr.close();
        System.out.println(String.format("Feature importances written to: %s.fimp", fname));
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

        for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_ALL))
            attributes.add(a.getAttributeJSON());
        for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET))
            attributesTarget.add(new JsonPrimitive(a.getName()));
        for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING))
            attributesClustering.add(new JsonPrimitive(a.getName()));
        for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))
            attributesDescriptive.add(new JsonPrimitive(a.getName()));

        String taskTypeString = (schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET).length > 1) ? "MT " : "ST ";
        // taskTypeString += (m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET)[0].getTypeName()) ? "MT" : "ST";
        if (cr.getStatManager().getMode() == ClusStatManager.MODE_REGRESSION) {
            taskTypeString += "Regression";
        }
        else if (cr.getStatManager().getMode() == ClusStatManager.MODE_CLASSIFY) {
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
        }
        catch (Exception ex) {}

        dataSpec.add("attributes", attributes);
        dataSpec.add("task", task);
        dataSpec.addProperty("query", queryValue);
        functionOutputJSON.add("dataSpecification", dataSpec);

        JsonObject algorithmSpec = new JsonObject();
        JsonElement algorithmName;
        int ens_method = getSettings().getEnsemble().getEnsembleMethod();
        int fr_method = getSettings().getEnsemble().getRankingMethod();
        if (ens_method == SettingsEnsemble.ENSEMBLE_EXTRA_TREES) {
            algorithmName = new JsonPrimitive("ExtraTrees/GENIE3");
        }
        else if ((ens_method == SettingsEnsemble.ENSEMBLE_RFOREST) && (fr_method == SettingsEnsemble.RANKING_RFOREST)) {
            algorithmName = new JsonPrimitive("RandomForestRanking");
        }
        else if ((ens_method == SettingsEnsemble.ENSEMBLE_RFOREST) && (fr_method == SettingsEnsemble.RANKING_GENIE3)) {
            algorithmName = new JsonPrimitive("RandomForest/GENIE3");
        }
        else {
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
        TreeMap<Double, ArrayList<String>> sorted = (TreeMap<Double, ArrayList<String>>) m_FeatureRanks.clone();
        // this was not used: Iterator<Double> iter = sorted.keySet().iterator();

        int count = 1;
        while (!sorted.isEmpty()) {
            double score = sorted.lastKey();
            ArrayList<String> attrs = sorted.get(score);
            for (int i = 0; i < attrs.size(); i++) {
                JsonObject elm = new JsonObject();
                elm.addProperty("attributeName", (String) attrs.get(i));
                elm.addProperty("ordering", count);
                elm.addProperty("importance", score);
                count++;
                rankingResults.add(elm);
            }
            sorted.remove(sorted.lastKey());
        }

        functionOutputJSON.add("ranking", rankingResults);

        File jsonfile = new File(getSettings().getGeneric().getAppName() + ".json");
        FileWriter json = new FileWriter(jsonfile);
        json.write(jsonBuilder.toJson(functionOutputJSON));
        json.flush();
        json.close();
        System.out.println("JSON model written to: " + getSettings().getGeneric().getAppName() + ".json");

    }


    /**
     * Implements the Fisher–Yates algorithm for uniform shuffling.
     * 
     * @param selection
     * @param data
     * @param type
     *        0 nominal, 1 numeric
     * @param position
     *        position at which the attribute whose values are being shuffled, is
     * @return
     */
    public RowData createRandomizedOOBdata(OOBSelection selection, RowData data, int type, int position, int seed) {
        RowData result = data;
        Random rndm = new Random(seed);
        for (int i = 0; i < result.getNbRows() - 1; i++) {
            // int rnd = i + ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, result.getNbRows()- i);
            int rnd = i + rndm.nextInt(result.getNbRows() - i);
            DataTuple first = result.getTuple(i);
            DataTuple second = result.getTuple(rnd);
            boolean successfullySwapped = false;
            if (first instanceof SparseDataTuple) {
                if (type == 1) {
                    double swap = ((SparseDataTuple) first).getDoubleValueSparse(position);
                    ((SparseDataTuple) first).setDoubleValueSparse(((SparseDataTuple) second).getDoubleValueSparse(position), position);
                    ((SparseDataTuple) second).setDoubleValueSparse(swap, position);
                    successfullySwapped = true;
                }
                else {
                    System.err.println("WARNING: type is not 1 (numeric). We will try to swap the values like in non-sparse case, but some things might go wrong, e.g.,\n" + "java.lang.NullPointerException might occur.");
                }

            }
            if (!successfullySwapped) {
                if (type == 0) {// nominal
                    int swap = first.getIntVal(position);
                    first.setIntVal(second.getIntVal(position), position);
                    second.setIntVal(swap, position);
                }
                else if (type == 1) {// numeric
                    double swap = first.getDoubleVal(position);
                    first.setDoubleVal(second.getDoubleVal(position), position);
                    second.setDoubleVal(swap, position);
                }
                else {
                    System.err.println("Error while making the random permutations for feature ranking!");
                    System.exit(-1);
                }
            }
        }
        return result;
    }


    /**
     * Finds all attributes that appear as (part of) the test in a node in the given tree. Depth first search is used to
     * traverse the tree.
     * 
     * @param root
     *        The root of the tree
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
     * @param data
     * @param model
     * @param cr
     * @return {@code [[listOfResultsForErr1, [sign1]], [listOfResultsForErr2, [sign2]], ...]},<br>
     *         where {@code signI = errorI.shouldBeLow() ? -1.0 : 1.0}, and
     *         {@code listOfResultsForErr} always contains the overall {@code Err} error in the position 0, and possibly
     *         also per target calculations for {@code Err}
     *         in the positions i > 0.
     * @throws ClusException
     */
    public double[][][] calcAverageErrors(RowData data, ClusModel model, ClusStatManager mgr) throws ClusException {
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
        //        if (m_FimpTableHeader == null) {
        //            setRForestDescription(error);
        //        }
        /* return the average errors */
        double[][][] errors = new double[error.getNbErrors()][2][];
        for (int i = 0; i < errors.length; i++) {
            ClusError currentError = error.getError(i);
            int nbResultsPerError = 1;
            if (mgr.getSettings().getEnsemble().shouldPerformRankingPerTarget() && (currentError instanceof ComponentError)) {
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
        NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
        NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
        if (mgr.getMode() == ClusStatManager.MODE_CLASSIFY) {
            if (sett.getMLC().getSectionMultiLabel().isEnabled()) {
                int[] measures = sett.getMLC().getMultiLabelRankingMeasures();
                for (int measure : measures) {
                    switch (measure) {
                        case SettingsMLC.MULTILABEL_MEASURES_HAMMINGLOSS:
                            error.addError(new HammingLoss(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MLACCURACY:
                            error.addError(new MLAccuracy(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MLPRECISION:
                            error.addError(new MLPrecision(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MLRECALL:
                            error.addError(new MLRecall(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MLFONE:
                            error.addError(new MLFOneMeasure(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_SUBSETACCURACY:
                            error.addError(new SubsetAccuracy(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MACROPRECISION:
                            error.addError(new MacroPrecision(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MACRORECALL:
                            error.addError(new MacroRecall(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MACROFONE:
                            error.addError(new MacroFOne(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MICROPRECISION:
                            error.addError(new MicroPrecision(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MICRORECALL:
                            error.addError(new MicroRecall(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_MICROFONE:
                            error.addError(new MisclassificationError(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_ONEERROR:
                            error.addError(new OneError(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_COVERAGE:
                            error.addError(new Coverage(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_RANKINGLOSS:
                            error.addError(new RankingLoss(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_AVERAGEPRECISION:
                            error.addError(new AveragePrecision(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_AUROC:
                            error.addError(new MLaverageAUROC(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_AUPRC:
                            error.addError(new MLaverageAUPRC(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_WEIGHTED_AUPRC:
                            error.addError(new MLweightedAUPRC(error, nom));
                            break;
                        case SettingsMLC.MULTILABEL_MEASURES_POOLED_AUPRC:
                            error.addError(new MLpooledAUPRC(error, nom));
                            break;
                    }
                }
            }
            else {
                error.addError(new Accuracy(error, nom));
            }
        }
        else if (mgr.getMode() == ClusStatManager.MODE_REGRESSION) {
            // error.addError(new MSError(error, num));
            // error.addError(new RelativeError(error, num));
            error.addError(new RMSError(error, num));
        }
        else if (mgr.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            error.addError(new HierErrorMeasures(error, mgr.getHier(), sett.getHMLC().getRecallValues().getDoubleVector(), sett.getGeneral().getCompatibility(), SettingsHMLC.HIERMEASURE_POOLED_AUPRC, false, getSettings().getOutput().isGzipOutput()));
        }
        else {
            System.err.println("Feature ranking with Random Forests is supported only for:");
            System.err.println("- multi-target classification (multi-label classification)");
            System.err.println("- multi-target regression");
            System.err.println("- hierarchical multi-label classification");
            System.exit(-1);
        }
        return error;
    }


    // returns sorted feature ranking
    public TreeMap<Double, ArrayList<String>> getFeatureRanks() {
        return m_FeatureRanks;
    }


    // returns feature ranking
    public HashMap<String, Double> getFeatureRanksByName() {
        return m_FeatureRankByName;
    }


    public double[] getAttributeInfo(String attribute) {
        m_Lock.readingLock();
        double[] info = Arrays.copyOf(m_AllAttributes.get(attribute), m_AllAttributes.get(attribute).length);
        m_Lock.readingUnlock();
        return info;
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
        return "attributeName\t[" + String.join(",", list) + "]";
    }


    public String fimpTableHeader(CharSequence... list) {
        return "attributeName\t[" + String.join(",", list) + "]";
    }


    public int getNbFeatureRankings() {
        return m_NbFeatureRankings;
    }


    public void setNbFeatureRankings(int nbRankings) {
        m_NbFeatureRankings = nbRankings;
    }


    public void createFimp(ClusRun cr, int numberOfTrees) throws IOException {
        createFimp(cr, "", numberOfTrees);
    }


    public void createFimp(ClusRun cr, String appendixToFimpName, int numberOfTrees) throws IOException {
        boolean sorted = cr.getStatManager().getSettings().getEnsemble().shouldSortRankingByRelevance();
        if (sorted && getNbFeatureRankings() > 1) {
            System.err.println("More than one feature ranking will be output. " + "The attributes will appear as in ARFF\nand will not be sorted " + "by relevance, although SortRankingByRelevance = Yes.");
            sorted = false;
        }
        if (sorted) {
            sortFeatureRanks(numberOfTrees);
        }
        convertRanksByName();
        String appName = cr.getStatManager().getSettings().getGeneric().getFileAbsolute(cr.getStatManager().getSettings().getGeneric().getAppName()) + appendixToFimpName;
        if (sorted) {
            writeRanking(appName, cr.getStatManager().getSettings().getEnsemble().getRankingMethod());
        }
        else {
            writeRankingByAttributeName(appName, cr.getStatManager().getSchema().getDescriptiveAttributes(), cr.getStatManager().getSettings().getEnsemble().getRankingMethod());
        }
        if (cr.getStatManager().getSettings().getOutput().isOutputJSONModel()) {
            writeJSON(cr);
        }
    }
}

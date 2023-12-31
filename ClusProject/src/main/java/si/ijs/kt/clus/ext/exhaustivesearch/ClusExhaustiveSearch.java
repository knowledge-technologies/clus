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

/*
 * Created on July 31, 2006
 */

package si.ijs.kt.clus.ext.exhaustivesearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.algo.split.FindBestTest;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.algo.tdidt.ConstraintDFInduce;
import si.ijs.kt.clus.algo.tdidt.processor.BasicExampleCollector;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.beamsearch.ClusBeam;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamHeuristic;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamModel;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamTreeElem;
import si.ijs.kt.clus.ext.constraint.ClusConstraintFile;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.main.settings.section.SettingsTree.Heuristic;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.io.ClusModelCollectionIO;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.io.MyFile;
import si.ijs.kt.clus.util.jeans.math.SingleStat;
import si.ijs.kt.clus.util.jeans.util.MyArray;
import si.ijs.kt.clus.util.jeans.util.StringUtils;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


public class ClusExhaustiveSearch extends ClusInductionAlgorithmType {

    public final static int HEURISTIC_ERROR = 0;
    public final static int HEURISTIC_SS = 1;

    // public final static int m_MaxSteps = 100000;
    protected BasicExampleCollector m_Coll = new BasicExampleCollector();
    protected ConstraintDFInduce m_Induce;
    protected ClusExhaustiveInduce m_BeamInduce;
    protected boolean m_BeamChanged;
    protected int m_CurrentModel;
    protected int m_MaxTreeSize;
    protected double m_MaxError;
    protected double m_TotalWeight;
    protected ArrayList m_BeamStats;
    protected ClusBeam m_Beam;
    protected boolean m_BeamPostPruning;
    protected ClusBeamHeuristic m_Heuristic;
    protected ClusHeuristic m_AttrHeuristic;
    protected boolean m_Verbose;


    public ClusExhaustiveSearch(Clus clus) throws ClusException, IOException {
        super(clus);
    }


    public void reset() {
        m_Beam = null;
        m_BeamChanged = false;
        m_CurrentModel = -1;
        m_TotalWeight = 0.0;
        m_BeamStats = new ArrayList();
    }


    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        schema.addIndices(ClusSchema.ROWS);
        m_BeamInduce = new ClusExhaustiveInduce(schema, sett, this);
        m_BeamInduce.getStatManager().setBeamSearch(true);
        return m_BeamInduce;
    }


    public void initializeHeuristic() {
        ClusStatManager smanager = m_BeamInduce.getStatManager();
        Settings sett = smanager.getSettings();
        m_MaxTreeSize = sett.getConstraints().getMaxSize();
        m_MaxError = sett.getConstraints().getMaxErrorConstraint(0);
        ClusLogger.info("ClusExhaustiveSearch : The Max Error is " + m_MaxError);
        m_BeamPostPruning = sett.getBeamSearch().isBeamPostPrune();
        m_Heuristic = (ClusBeamHeuristic) smanager.getHeuristic();
        Heuristic attr_heur = sett.getBeamSearch().getBeamAttrHeuristic();
        if (!attr_heur.equals(Heuristic.Default)) {
            m_AttrHeuristic = smanager.createHeuristic(attr_heur);
            m_Heuristic.setAttrHeuristic(m_AttrHeuristic);
        }
    }


    public final boolean isBeamPostPrune() {
        return m_BeamPostPruning;
    }


    public double computeLeafAdd(ClusNode leaf) {
        return m_Heuristic.computeLeafAdd(leaf);
    }


    public double estimateBeamMeasure(ClusNode tree) {
        return m_Heuristic.estimateBeamMeasure(tree);
    }


    public void initSelector(CurrentBestTestAndHeuristic sel) {
        if (hasAttrHeuristic()) {
            sel.setHeuristic(m_AttrHeuristic);
        }
    }


    public final boolean hasAttrHeuristic() {
        return m_AttrHeuristic != null;
    }


    public ClusBeam initializeBeamExhaustive(ClusRun run) throws Exception {
        ClusStatManager smanager = m_BeamInduce.getStatManager();
        Settings sett = smanager.getSettings();
        sett.getModel().setMinimalWeight(1); // the minimal number of covered example in a leaf is 1
        ClusBeam beam = new ClusBeam(-1, false); // infinite size and we add everything
        /* Create single leaf node */
        RowData train = (RowData) run.getTrainingSet();
        ClusStatistic stat = m_Induce.createTotalClusteringStat(train);
        stat.calcMean();
        m_Induce.initSelectorAndSplit(stat);
        initSelector(m_Induce.getBestTest());
        ClusLogger.info("Root statistic: " + stat);
        ClusNode root = null;
        /* Has syntactic constraints? */
        String constr_file = sett.getConstraints().getConstraintFile();
        if (StringUtils.unCaseCompare(constr_file, SettingsBase.NONE)) {// no constraint
            root = new ClusNode();
            root.setClusteringStat(stat);
        }
        else {
            ClusConstraintFile file = ClusConstraintFile.getInstance();
            root = file.getClone(constr_file);
            root.setClusteringStat(stat);
            m_Induce.fillInStatsAndTests(root, train);
        }

        // ClusLogger.info("the beam is initialized to :");root.printTree();
        /* Compute total weight */
        double weight = root.getClusteringStat().getTotalWeight();
        setTotalWeight(weight);
        /* Evaluate the quality estimate */
        double value = estimateBeamMeasure(root);
        /* Add tree to beam */
        beam.addModel(new ClusBeamModel(value, root));
        ClusLogger.info("the number of children from the root node is :" + root.getNbChildren());
        return beam;
    }


    /*
     * @leaf : the leaf to refine
     * @root : the entire model which encapsuled the tree (contains the value, etc...)
     * @beam : the entire beam
     * @attr : the attribute that can be used for to refine the leaf
     * Everything that concerns setting the test is in the ClusNode Class
     */

    public void refineGivenLeafExhaustive(ClusNode leaf, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) throws Exception {
        MyArray arr = (MyArray) leaf.getVisitor();
        RowData data = new RowData(arr.getObjects(), arr.size());
        if (m_Induce.initSelectorAndStopCrit(leaf, data)) {
            // stopping criterion is met (save this for further reference?)
            ClusLogger.info("stopping criterion reached in refineGivenLeafExhaustive");
            return;
        }
        if ((leaf.getClusteringStat()).getError() == 0.0) {
            // ClusLogger.info("pure leaf");
            return;
        }
        // init base value for heuristic
        CurrentBestTestAndHeuristic sel = m_Induce.getBestTest();
        FindBestTest find = m_Induce.getFindBestTest();
        double base_value = root.getValue();
        double leaf_add = m_Heuristic.computeLeafAdd(leaf);
        m_Heuristic.setTreeOffset(base_value - leaf_add);
        // find good splits
        for (int i = 0; i < attrs.length; i++) {
            // reset selector for each attribute
            sel.resetBestTest();
            double beam_min_value = Double.NEGATIVE_INFINITY;
            sel.setBestHeur(beam_min_value);
            // process attribute
            ClusAttrType at = attrs[i];
            // ClusLogger.info("Attribute: "+at.getName());
            // look for the best avlue of the attribute the attribute
            if (at instanceof NominalAttrType)
                find.findNominal((NominalAttrType) at, data, null);
            else
                find.findNumeric((NumericAttrType) at, data, null);
            // found good test for attribute (the test type has been changed in the finnominal function)?
            if (sel.hasBestTest()) {
                ClusNode ref_leaf = (ClusNode) leaf.cloneNode();
                ref_leaf.testToNode(sel);
                // output best test
                if (getSettings().getGeneral().getVerbose() > 0)
                    ClusLogger.info("Test: " + ref_leaf.getTestString() + " -> " + sel.m_BestHeur + " (" + ref_leaf.getTest().getPosFreq() + ")");
                // create child nodes
                ClusStatManager mgr = m_Induce.getStatManager();
                int arity = ref_leaf.updateArity();
                NodeTest test = ref_leaf.getTest();
                for (int j = 0; j < arity; j++) {
                    ClusNode child = new ClusNode();
                    ref_leaf.setChild(child, j);
                    RowData subset = data.applyWeighted(test, j);
                    child.initClusteringStat(mgr, subset);
                    // the following two calls could be removed, but are useful for printing the trees
                    child.initTargetStat(mgr, subset);
                    child.getTargetStat().calcMean();
                }
                // create new model
                ClusNode root_model = (ClusNode) root.getModel();
                ClusNode ref_tree = (ClusNode) root_model.cloneTree(leaf, ref_leaf);
                double new_heur = sanityCheck(sel.m_BestHeur, ref_tree);
                // the default created model is unrefined
                ClusBeamModel new_model = new ClusBeamModel(new_heur, ref_tree);
                new_model.setParentModelIndex(getCurrentModel());
                beam.addModel(new_model);
                setBeamChanged(true);
                // Uncomment the following to print each model that is added to the beam
                // ClusLogger.info("this new model, has been added to the beam : ");
                // ((ClusNode)new_model.getModel()).printTree();
            }
            // else{ClusLogger.info("no sel.hasBestTest()");}
        } // end for each attribute
    }


    /*
     * Used to go down the tree to each leaf and then refine each leafs
     */

    public void refineEachLeaf(ClusNode tree, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) throws Exception {
        int nb_c = tree.getNbChildren();
        if (nb_c == 0) {
            refineGivenLeafExhaustive(tree, root, beam, attrs);
        }
        else {
            for (int i = 0; i < nb_c; i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                // System.out.print("refining leaf : ");child.printTree();
                refineEachLeaf(child, root, beam, attrs);
            }
        }
    }


    public void refineModel(ClusBeamModel model, ClusBeam beam, ClusRun run) throws Exception {
        ClusNode tree = (ClusNode) model.getModel();
        /* Compute size */
        if (m_MaxTreeSize >= 0) { // if the size is infinite, it equals -1
            int size = tree.getNbNodes();
            if (size + 2 > m_MaxTreeSize) { return; }
        }

        // we assume that the test are binary
        if (m_MaxError > 0 && m_MaxTreeSize > 0) {// default m_MaxError =0
            int NbpossibleSplit = (m_MaxTreeSize - tree.getModelSize()) / 2;
            // ClusLogger.info("the number of possible split is still "+NbpossibleSplit);
            double[] error = getErrorPerleaf(tree);
            // ClusLogger.info("the number of leaf is "+error.length);
            double minerror = 0.0;
            if (error.length > NbpossibleSplit) {
                for (int i = 0; i < ((error.length) - NbpossibleSplit); i++) {
                    // ClusLogger.info(" leaf "+i+", error = "+error[i]);
                    minerror += error[i];
                }
                double minerrorrel = minerror / m_TotalWeight;
                // ClusLogger.info("the minimum relative error is : "+minerrorrel);
                if (minerrorrel > m_MaxError) {
                    tree.printTree();
                    ClusLogger.info("PRUNE WITH ERROR");
                    return;
                }
            }
        }

        /* Sort the data into tree */
        RowData train = (RowData) run.getTrainingSet();
        m_Coll.initialize(tree, null);
        int nb_rows = train.getNbRows();
        for (int i = 0; i < nb_rows; i++) {
            DataTuple tuple = train.getTuple(i);
            tree.applyModelProcessor(tuple, m_Coll);
        }
        /* Data is inside tree, try to refine each leaf */
        ClusAttrType[] attrs = train.getSchema().getDescriptiveAttributes();
        refineEachLeaf(tree, model, beam, attrs);
        /* Remove data from tree */
        tree.clearVisitors();
    }


    public static double[] getErrorPerleaf(ClusNode tree) {
        int nbleaf = tree.getNbLeaf();
        double[] resulterror = new double[nbleaf];
        if (tree.atBottomLevel()) {
            ClusStatistic total = tree.getClusteringStat();
            resulterror[0] = total.getError();
            // ClusLogger.info("ClusNode : error is "+resulterror[0]);
        }
        else {
            ClusNode child0 = (ClusNode) tree.getChild(0);
            resulterror = getErrorPerleaf(child0);
            for (int i = 1; i < tree.getNbChildren(); i++) {
                ClusNode childi = (ClusNode) tree.getChild(i);
                double[] errori = getErrorPerleaf(childi);
                resulterror = concatarraysorted(resulterror, errori);
            }
        }
        return resulterror;
    }


    public static double[] concatarraysorted(double[] array1, double[] array2) {
        int size_array = array1.length + array2.length;
        double[] array = new double[size_array];
        for (int i = 0; i < array1.length; i++) {
            array[i] = array1[i];
        }
        for (int i = 0; i < array2.length; i++) {
            array[i + array1.length] = array2[i];
        }
        Arrays.sort(array);
        return array;
    }


    public void refineBeamExhaustive(ClusBeam beam, ClusRun run) throws Exception {
        setBeamChanged(false);
        ArrayList models = beam.toArray();
        // ClusLogger.info("the size of the beam is :"+models.size());
        for (int i = 0; i < models.size(); i++) {
            System.out.print("Refining model: " + i);
            setCurrentModel(i);
            ClusBeamModel model = (ClusBeamModel) models.get(i);
            if (!(model.isRefined() || model.isFinished())) {
                if (m_Verbose)
                    System.out.print("[*]");
                refineModel(model, beam, run);
                model.setRefined(true);
                model.setParentModelIndex(-1);
            }
            if (m_Verbose) {
                if (model.isRefined()) {
                    ClusLogger.info("[R]");
                }
                if (model.isFinished()) {
                    ClusLogger.info("[F]");
                }
            }
        }
    }


    @Override
    public Settings getSettings() {
        return m_Clus.getSettings();
    }


    public void estimateBeamStats(ClusBeam beam) {
        SingleStat stat_heuristic = new SingleStat();
        SingleStat stat_size = new SingleStat();
        SingleStat stat_same_heur = new SingleStat();
        ArrayList lst = beam.toArray();
        HashSet tops = new HashSet();
        for (int i = 0; i < lst.size(); i++) {
            ClusBeamModel model = (ClusBeamModel) lst.get(i);
            stat_heuristic.addFloat(model.getValue());
            stat_size.addFloat(model.getModel().getModelSize());
            NodeTest top = ((ClusNode) model.getModel()).getTest();
            if (top != null) {
                if (!tops.contains(top)) {
                    tops.add(top);
                }
            }
        }
        Iterator iter = beam.getIterator();
        while (iter.hasNext()) {
            ClusBeamTreeElem elem = (ClusBeamTreeElem) iter.next();
            stat_same_heur.addFloat(elem.getCount());
        }
        ArrayList stat = new ArrayList();
        stat.add(stat_heuristic);
        stat.add(stat_same_heur);
        stat.add(stat_size);
        stat.add(new Integer(tops.size()));
        m_BeamStats.add(stat);
    }


    public String getLevelStat(int i) {
        ArrayList stat = (ArrayList) m_BeamStats.get(i);
        StringBuffer buf = new StringBuffer();
        buf.append("Level: " + i);
        for (int j = 0; j < stat.size(); j++) {
            Object elem = stat.get(j);
            buf.append(", ");
            if (elem instanceof SingleStat) {
                SingleStat st = (SingleStat) elem;
                buf.append(st.getMean() + "," + st.getRange());
            }
            else {
                buf.append(elem.toString());
            }
        }
        return buf.toString();
    }


    public void printBeamStats(int level) {
        ClusLogger.info(getLevelStat(level));
    }


    public void saveBeamStats() {
        MyFile stats = new MyFile(getSettings().getGeneric().getAppName() + ".bmstats");
        for (int i = 0; i < m_BeamStats.size(); i++) {
            stats.log(getLevelStat(i));
        }
        stats.close();
    }


    public void writeModel(ClusModelCollectionIO strm) throws IOException, ClusException {
        saveBeamStats();
        ArrayList beam = getBeam().toArray();
        for (int i = 0; i < beam.size(); i++) {
            ClusBeamModel m = (ClusBeamModel) beam.get(i);
            ClusNode node = (ClusNode) m.getModel();
            node.updateTree();
            node.clearVisitors();
        }
        int pos = 1;
        for (int i = beam.size() - 1; i >= 0; i--) {
            ClusBeamModel m = (ClusBeamModel) beam.get(i);
            ClusModelInfo info = new ClusModelInfo("B" + pos + ": " + m.getValue());
            info.setScore(m.getValue());
            info.setModel(m.getModel());
            strm.addModel(info);
            pos++;
        }
    }


    public void setVerbose(boolean verb) {
        m_Verbose = verb;
    }


    public ClusBeam exhaustiveSearch(ClusRun run) throws Exception {
        reset();
        ClusLogger.info("Starting exhaustive search");
        m_Induce = new ConstraintDFInduce(m_BeamInduce);
        ClusBeam beam = initializeBeamExhaustive(run);
        ClusBeam beamresult = new ClusBeam(-1, false);
        int cpt_tree_evaluation = 0;
        int i = 0;
        setVerbose(true);
        while (true) {
            System.out.print("Step: ");
            if (i != 0)
                System.out.print(",");
            ClusLogger.info(i);
            System.out.flush();
            refineBeamExhaustive(beam, run);
            if (isBeamChanged()) { // look for the change of the CluBeamInduce object
                estimateBeamStats(beam);
            }
            else {
                break;
            }
            i++;
        }
        ArrayList arraybeam = beam.toArray();
        for (int j = 0; j < arraybeam.size(); j++) {
            // System.out.print("copy the "+j+"th model of the beam : ");
            ClusBeamModel m = (ClusBeamModel) arraybeam.get(j);
            // m.setRefined(false); //WHY SHOULD I DO THAT ????
            ClusNode tree = (ClusNode) m.getModel();

            // ClusLogger.info("Clus Exhaus MaxError is "+m_MaxError);
            // ClusLogger.info("Clus Exhaus error of the tree is
            // "+(ClusNode.estimateErrorRecursive(tree)/m_TotalWeight));
            cpt_tree_evaluation++;
            if (m_MaxError <= 0 || ((ClusNode.estimateErrorRecursive(tree) / m_TotalWeight) <= m_MaxError)) {
                beamresult.addModel(m);
                // ClusNode node = (ClusNode)m.getModel();
                // node.printTree();
                // ClusLogger.info("tree nb "+(beamresult.toArray()).size()+" added");
            }
        }
        ClusLogger.info();
        setBeam(beamresult);
        ArrayList arraybeamresult = beamresult.toArray();
        ClusLogger.info(" the model that fulfill the constraints are" + arraybeamresult.size());
        /*
         * for (int j = 0; j < arraybeamresult.size(); j++) {
         * ClusLogger.info("model"+j+": ");
         * ClusBeamModel m = (ClusBeamModel)arraybeamresult.get(j);
         * ClusNode node = (ClusNode)m.getModel();
         * node.printTree();
         * }
         */
        ClusLogger.info("The number of tree evaluated is " + cpt_tree_evaluation);
        return beamresult;
    }


    public void setBeam(ClusBeam beam) {
        m_Beam = beam;
    }


    public ClusBeam getBeam() {
        return m_Beam;
    }


    public boolean isBeamChanged() {
        return m_BeamChanged;
    }


    public void setBeamChanged(boolean change) {
        m_BeamChanged = change;
    }


    public int getCurrentModel() {
        return m_CurrentModel;
    }


    public void setCurrentModel(int model) {
        m_CurrentModel = model;
    }


    public void setTotalWeight(double weight) {
        m_TotalWeight = weight;
    }


    public double sanityCheck(double value, ClusNode tree) throws ClusException {
        double expected = estimateBeamMeasure(tree);
        if (Math.abs(value - expected) > 1e-6) {
            ClusLogger.info("Bug in heurisitc: " + value + " <> " + expected);
            PrintWriter wrt = new PrintWriter(System.out);
            tree.printModel(wrt);
            wrt.close();
            System.out.flush();
            
            throw new ClusException("sanity check failed");
        }
        return expected;
    }


    public void tryLogBeam(MyFile log, ClusBeam beam, String txt) {
        if (log.isEnabled()) {
            log.log(txt);
            log.log("*********************************************");
            beam.print(log.getWriter(), m_Clus.getSettings().getBeamSearch().getBeamBestN());
            log.log();
        }
    }


    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {
    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        return model;
    }


    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException {
        
        
    }
}

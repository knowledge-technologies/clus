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
 * Created on Apr 5, 2005
 */

package si.ijs.kt.clus.ext.beamsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import si.ijs.kt.clus.ext.constraint.ClusConstraintFile;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.main.settings.section.SettingsBeamSearch;
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


public class ClusBeamSearch extends ClusInductionAlgorithmType {

    public final static int HEURISTIC_ERROR = 0;
    public final static int HEURISTIC_SS = 1;

    // public final static int m_MaxSteps = 100000;

    protected BasicExampleCollector m_Coll = new BasicExampleCollector();
    protected ConstraintDFInduce m_Induce;
    protected ClusBeamInduce m_BeamInduce;
    protected boolean m_BeamChanged;
    protected int m_CurrentModel;
    protected int m_MaxTreeSize;
    protected double m_TotalWeight;
    protected ArrayList m_BeamStats;
    protected ClusBeam m_Beam;
    protected boolean m_BeamPostPruning;
    protected ClusBeamHeuristic m_Heuristic;
    protected ClusHeuristic m_AttrHeuristic;
    protected boolean m_Verbose;
    protected ClusBeamModelDistance m_BeamModelDistance;
    protected ClusBeamSyntacticConstraint m_BeamSyntConstr;


    public ClusBeamSearch(Clus clus) throws ClusException, IOException {
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
        m_BeamInduce = new ClusBeamInduce(schema, sett, this);
        m_BeamInduce.getStatManager().setBeamSearch(true);
        return m_BeamInduce;
    }


    public void initializeHeuristic() {
        ClusStatManager smanager = m_BeamInduce.getStatManager();
        Settings sett = smanager.getSettings();
        m_MaxTreeSize = sett.getBeamSearch().getBeamTreeMaxSize();
        ClusLogger.info("BeamSearch : the maximal size of the trees is " + m_MaxTreeSize);
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


    public ClusBeam initializeBeam(ClusRun run) throws Exception {
        ClusStatManager smanager = m_BeamInduce.getStatManager();
        Settings sett = smanager.getSettings();
        ClusBeam beam = new ClusBeam(sett.getBeamSearch().getBeamWidth(), sett.getBeamSearch().getBeamRemoveEqualHeur());
        /* Create single leaf node */
        RowData train = (RowData) run.getTrainingSet();
        train.addIndices();// add the indices of the tuples
        ClusStatistic stat = m_Induce.createTotalClusteringStat(train);
        stat.calcMean();
        m_Induce.initSelectorAndSplit(stat);
        initSelector(m_Induce.getBestTest());
        ClusLogger.info("Root statistic: " + stat);
        /* Has syntactic constraints? */
        ClusNode root = null;
        String constr_file = sett.getConstraints().getConstraintFile();
        if (StringUtils.unCaseCompare(constr_file, SettingsBase.NONE)) {
            root = new ClusNode();
            root.setClusteringStat(stat);
        }
        else {
            ClusConstraintFile file = ClusConstraintFile.getInstance();
            root = file.getClone(constr_file);
            root.setClusteringStat(stat);
            m_Induce.fillInStatsAndTests(root, train);
        }
        /* Make sure root also has target statistics computed */
        root.initTargetStat(getStatManager(), train);
        root.getTargetStat().calcMean();
        root.getClusteringStat().setBeam(beam);// connection to the similarity measure...
        root.getTargetStat().setBeam(beam);
        /* Compute total weight */
        double weight = root.getClusteringStat().getTotalWeight();
        setTotalWeight(weight);
        /* Evaluate the quality estimate */
        double value = estimateBeamMeasure(root);
        /* Add tree to beam */
        beam.addModel(new ClusBeamModel(value, root));
        /* Initialize Tree Distance */
        m_BeamModelDistance = new ClusBeamModelDistance(run, beam);
        /* Initialize the Syntactic Distance Constraint */
        if (SettingsBeamSearch.BEAM_SYNT_DIST_CONSTR)
            m_BeamSyntConstr = new ClusBeamSyntacticConstraint(run); // this can throw IOException
        return beam;
    }


    public void refineGivenLeaf(ClusNode leaf, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) throws Exception {
        MyArray arr = (MyArray) leaf.getVisitor();
        RowData data = new RowData(arr.getObjects(), arr.size());
        if (m_Induce.initSelectorAndStopCrit(leaf, data)) {
            // stopping criterion is met (save this for further reference?)
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
            double beam_min_value = beam.getMinValue();
            sel.setBestHeur(beam_min_value);
            // process attribute
            ClusAttrType at = attrs[i];
            // ClusLogger.info("Attribute: "+at.getName());
            if (at instanceof NominalAttrType)
                find.findNominal((NominalAttrType) at, data, null);
            else
                find.findNumeric((NumericAttrType) at, data, null);
            // found good test for attribute ?
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
                // Check for sure if _strictly_ better!

                ClusBeamModel new_model = new ClusBeamModel(new_heur, ref_tree);
                new_model.setParentModelIndex(getCurrentModel());

                if ((SettingsBeamSearch.BEAM_SIMILARITY != 0) && !SettingsBeamSearch.BEAM_SYNT_DIST_CONSTR) {
                    new_model.setModelPredictions(m_BeamModelDistance.getPredictions(new_model.getModel()));
                    if (!beam.modelAlreadyIn(new_model)) {
                        // This version is linear wrt the beam-width
                        m_BeamModelDistance.addDistToCandOpt(beam, new_model);
                        if (beam.removeMinUpdatedOpt(new_model, m_BeamModelDistance) == 1)
                            setBeamChanged(true);

                        // These were used for KDID 2006 paper
                        /*
                         * m_BeamModelDistance.calculatePredictionDistances(beam, new_model);
                         * if (beam.removeMinUpdated(new_model) == 1) setBeamChanged(true);
                         */
                        /*
                         * m_BeamModelDistance.calculatePredictionDistancesOpt(beam, new_model);
                         * if (beam.removeMinUpdated(new_model) == 1) setBeamChanged(true);
                         */
                    }
                }
                else {
                    if (SettingsBeamSearch.BEAM_SYNT_DIST_CONSTR) {
                        ClusLogger.info("OLD HEUR = " + new_heur);
                        new_model.setModelPredictions(m_BeamModelDistance.getPredictions(new_model.getModel()));
                        new_heur -= SettingsBeamSearch.BEAM_SIMILARITY * m_BeamModelDistance.getDistToConstraint(new_model, m_BeamSyntConstr);
                        ClusLogger.info("UPDT HEUR = " + new_heur);
                    }
                    // Check for sure if _strictly_ better!
                    if (new_heur > beam_min_value) {

                        beam.addModel(new_model);
                        setBeamChanged(true);
                        // Uncomment the following to print each model that is added to the beam
                        // ((ClusNode)new_model.getModel()).printTree();
                    }
                }
            }
        }
    }


    public void refineEachLeaf(ClusNode tree, ClusBeamModel root, ClusBeam beam, ClusAttrType[] attrs) throws Exception {
        int nb_c = tree.getNbChildren();
        if (nb_c == 0) {
            refineGivenLeaf(tree, root, beam, attrs);
        }
        else {
            for (int i = 0; i < nb_c; i++) {
                ClusNode child = (ClusNode) tree.getChild(i);
                refineEachLeaf(child, root, beam, attrs);
            }
        }
    }


    public void refineModel(ClusBeamModel model, ClusBeam beam, ClusRun run) throws Exception {
        ClusNode tree = (ClusNode) model.getModel();
        /* Compute size */
        if (m_MaxTreeSize >= 0) {
            int size = tree.getNbNodes();
            if (size + 2 > m_MaxTreeSize) { return; }
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


    public void refineBeam(ClusBeam beam, ClusRun run) throws Exception {
        setBeamChanged(false);
        ArrayList models = beam.toArray();
        for (int i = 0; i < models.size(); i++) {
            // ClusLogger.info("Refining model: "+i);
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
                    System.out.print("[R]");
                }
                if (model.isFinished()) {
                    System.out.print("[F]");
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


    public ClusNode beamSearch(ClusRun run) throws Exception {
        reset();
        ClusLogger.info("Starting beam search");
        m_Induce = new ConstraintDFInduce(m_BeamInduce);
        ClusBeam beam = initializeBeam(run);
        // MyFile beamlog = new MyFile("beam.log", true);
        // tryLogBeam(beamlog, beam, "Initial beam:");
        int i = 0;
        while (true) {
            ClusLogger.info("Step: " + i);
            refineBeam(beam, run);
            if (isBeamChanged()) {
                // tryLogBeam(beamlog, beam, "Step:"+i);
                estimateBeamStats(beam);
            }
            else {
                break;
            }
            i++;
        }
        setBeam(beam);
        double best = beam.getBestModel().getValue();
        double worst = beam.getWorstModel().getValue();
        ClusLogger.info("Worst = " + worst + " Best = " + best);
        printBeamStats(i - 1);
        ClusNode result = (ClusNode) beam.getBestAndSmallestModel().getModel();
        // beamlog.close();
        return result;
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
        /*
         * int size = tree.getModelSize();
         * ClusLogger.info("Size = "+size);
         * tree.printTree();
         * ClusLogger.info("Evaluating measure...");
         */
        double expected = estimateBeamMeasure(tree);
        if (Math.abs(value - expected) > 1e-6) {
            ClusLogger.info("Bug in heurisitc: " + value + " <> " + expected);
            PrintWriter wrt = new PrintWriter(System.out);
            tree.printModel(wrt);
            wrt.close();
            System.out.flush();
            throw new ClusException("Bug in heuristic: " + value + " <> " + expected);
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

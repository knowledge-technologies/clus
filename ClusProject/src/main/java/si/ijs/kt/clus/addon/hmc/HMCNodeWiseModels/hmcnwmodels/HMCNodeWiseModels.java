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

package si.ijs.kt.clus.addon.hmc.HMCNodeWiseModels.hmcnwmodels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.algo.tdidt.tune.CDTTuneFTest;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.hierarchies.ClassesAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleClassifier;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassTerm;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.ext.hierarchical.ClassesValue;
import si.ijs.kt.clus.main.ClusOutput;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.io.ClusModelCollectionIO;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.array.StringTable;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgsProvider;


public class HMCNodeWiseModels implements CMDLineArgsProvider {

    private static String[] g_Options = { "forest" };
    private static int[] g_OptionArities = { 0 };

    protected Clus m_Clus;
    protected CMDLineArgs m_Cargs;
    protected StringTable m_Table = new StringTable();
    protected Hashtable m_Mappings;
    protected double[] m_FTests;


    public void run(String[] args) throws Exception {
        m_Clus = new Clus();
        Settings sett = m_Clus.getSettings();
        m_Cargs = new CMDLineArgs(this);
        m_Cargs.process(args);
        /*
         * String[] newargs = new String[args.length-1];
         * for (int i=0; i<newargs.length; i++)
         * {
         * newargs[i] = args[i];
         * }
         * readFtests(args[args.length-1]);
         * m_Cargs.process(newargs);
         */
        if (m_Cargs.allOK()) {
            (new File("hsc")).mkdir();
            (new File("hsc/out")).mkdir();
            (new File("hsc/model")).mkdir();
            sett.getGeneric().setDate(new Date());
            sett.getGeneric().setAppName(m_Cargs.getMainArg(0));
            m_Clus.initSettings(m_Cargs);
            ClusDecisionTree clss = new ClusDecisionTree(m_Clus);
            if (sett.getTree().getFTestArray().isVector()) {
                m_FTests = sett.getTree().getFTestArray().getDoubleVector();
                clss = new CDTTuneFTest(clss, sett.getTree().getFTestArray().getDoubleVector());
            }
            m_Clus.initialize(m_Cargs, clss);
            doRun();
        }
        else {
            ClusLogger.info("m_Cargs nok");
        }
    }


//    private void readFtests(String filename) {
//        ClusLogger.info("filename: " + filename);
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(filename));
//            String s;
//            s = in.readLine();
//            String[] parts;
//            m_Mappings = new Hashtable();
//            while (s != null) {
//                parts = s.split("\t");
//                m_Mappings.put(parts[0], parts[1]);
//                s = in.readLine();
//            }
//        }
//        catch (java.io.IOException e) {
//            e.printStackTrace();
//        }
//    }


    public RowData getNodeData(RowData train, int nodeid) {
        ArrayList<DataTuple> selected = new ArrayList<DataTuple>();
        for (int i = 0; i < train.getNbRows(); i++) {
            DataTuple tuple;
            if (m_Clus.getSchema().isSparse()) {
                tuple = train.getTuple(i);
            }
            else {
                tuple = train.getTuple(i);
            }
            ClassesTuple target = (ClassesTuple) tuple.getObjVal(0);
            if (nodeid == -1 || target.hasClass(nodeid)) {
                selected.add(tuple);
            }
        }
        return new RowData(selected, train.getSchema());
    }


    public RowData createChildData(RowData nodeData, ClassesAttrType ctype, int childid) throws ClusException {
        // Create hierarchy with just one class
        ClassHierarchy chier = ctype.getHier();
        ClassesValue one = new ClassesValue("1", ctype.getTable());
        chier.addClass(one);
        chier.initialize();
        one.addHierarchyIndices(chier);
        RowData childData = new RowData(ctype.getSchema(), nodeData.getNbRows());
        for (int j = 0; j < nodeData.getNbRows(); j++) {
            ClassesTuple clss = null;
            DataTuple tuple;
            if (m_Clus.getSchema().isSparse()) {
                tuple = nodeData.getTuple(j);
            }
            else {
                tuple = nodeData.getTuple(j);
            }
            ClassesTuple target = (ClassesTuple) tuple.getObjVal(0);
            if (target.hasClass(childid)) {
                clss = new ClassesTuple(1);
                clss.addItem(new ClassesValue(one.getTerm()));
            }
            else {
                clss = new ClassesTuple(0);
            }
            DataTuple new_tuple = tuple.deepCloneTuple();
            new_tuple.setSchema(ctype.getSchema());
            new_tuple.setObjectVal(clss, 0);
            childData.setTuple(new_tuple, j);
        }
        return childData;
    }


    public ClusSchema createChildSchema(ClusSchema oschema, ClassesAttrType ctype, String name) throws ClusException, IOException {
        ClusSchema cschema = new ClusSchema(name);
        for (int j = 0; j < oschema.getNbAttributes(); j++) {
            ClusAttrType atype = oschema.getAttrType(j);
            if (!(atype instanceof ClassesAttrType)) {
                ClusAttrType copy_atype = atype.cloneType();
                cschema.addAttrType(copy_atype);
            }
        }
        cschema.addAttrType(ctype);
        cschema.initializeSettings(m_Clus.getSettings());
        if (oschema.isSparse())
            cschema.setSparse();
        return cschema;
    }


    public void doOneNode(ClassTerm node, ClassHierarchy hier, RowData train, RowData valid, RowData test) throws Exception {
        // get data relevant to node
        RowData nodeData = getNodeData(train, node.getIndex());
        String nodeName = node.toPathString("=");
        // for each child, create new tree
        for (int i = 0; i < node.getNbChildren(); i++) {
            ClassTerm child = (ClassTerm) node.getChild(i);
            String childName = child.toPathString("=");
            ClassesAttrType ctype = new ClassesAttrType(nodeName + "-" + childName);
            ClusSchema cschema = createChildSchema(train.getSchema(), ctype, "REL-" + nodeName + "-" + childName);
            RowData childData = createChildData(nodeData, ctype, child.getIndex());
            ClusInductionAlgorithmType clss;
            if (m_Cargs.hasOption("forest")) {
                m_Clus.getSettings().getEnsemble().setEnsembleMode(true);
                clss = new ClusEnsembleClassifier(m_Clus);
            }
            else {
                clss = new ClusDecisionTree(m_Clus);
            }
            if (m_FTests != null) {
                clss = new CDTTuneFTest(clss, m_FTests);
            }
            m_Clus.recreateInduce(m_Cargs, clss, cschema, childData);
            String name = m_Clus.getSettings().getGeneric().getAppName() + "-" + nodeName + "-" + childName;
            ClusRun cr = new ClusRun(childData.cloneData(), m_Clus.getSummary());
            cr.copyTrainingData();
            if (valid != null) {
                RowData validNodeData = getNodeData(valid, node.getIndex());
                RowData validChildData = createChildData(validNodeData, ctype, child.getIndex());
                // TupleIterator iter = validChildData.getIterator();
                cr.setPruneSet(validChildData, null);
                // m_Clus.initializeSummary(clss);
            }
            if (test != null) {
                RowData testNodeData = getNodeData(test, node.getIndex());
                RowData testChildData = createChildData(testNodeData, ctype, child.getIndex());
                TupleIterator iter = testChildData.getIterator();
                cr.setTestSet(iter);
                // m_Clus.initializeSummary(clss);
            }
            m_Clus.initializeSummary(clss);
            /*
             * String fstr = (String) m_Mappings.get(parentChildName);
             * if (fstr==null) {
             * ClusLogger.info("geen ftest gevonden voor "+ parentChildName);
             * }
             * else {
             * ClusLogger.info("fstr: "+ fstr);
             * float ft = Float.valueOf(fstr);
             * m_Clus.getSettings().setFTest(ft);
             * }
             */
            ClusOutput output = new ClusOutput("hsc/out/" + name + ".out", cschema, m_Clus.getSettings());
            m_Clus.getStatManager().computeTrainSetStat((RowData) cr.getTrainingSet());
            m_Clus.induce(cr, clss); // Induce model
            m_Clus.calcError(cr, null); // Calc error
            output.writeHeader();
            output.writeOutput(cr, true, m_Clus.getSettings().getOutput().isOutTrainError());
            output.close();
            ClusModelCollectionIO io = new ClusModelCollectionIO();
            io.addModel(cr.addModelInfo(ClusModel.ORIGINAL));
            io.save("hsc/model/" + name + ".model");
        }
    }


    public void computeRecursive(ClassTerm node, ClassHierarchy hier, RowData train, RowData valid, RowData test, boolean[] computed) throws Exception {
        if (!computed[node.getIndex()]) {
            // remember that we did this one
            computed[node.getIndex()] = true;
            doOneNode(node, hier, train, valid, test);
            // recursively do children
            for (int i = 0; i < node.getNbChildren(); i++) {
                ClassTerm child = (ClassTerm) node.getChild(i);
                computeRecursive(child, hier, train, valid, test, computed);
            }
        }
    }


    public void computeRecursiveRoot(ClassTerm node, ClassHierarchy hier, RowData train, RowData valid, RowData test, boolean[] computed) throws Exception {
        doOneNode(node, hier, train, valid, test);
        for (int i = 0; i < node.getNbChildren(); i++) {
            ClassTerm child = (ClassTerm) node.getChild(i);
            computeRecursive(child, hier, train, valid, test, computed);
        }
    }


    public void doRun() throws Exception {
        ClusRun cr = m_Clus.partitionData();
        RowData train = (RowData) cr.getTrainingSet();
        RowData valid = (RowData) cr.getPruneSet();
        RowData test = cr.getTestSet();
        ClusStatManager mgr = m_Clus.getStatManager();
        ClassHierarchy hier = mgr.getHier();
        ClassTerm root = hier.getRoot();
        boolean[] computed = new boolean[hier.getTotal()];
        computeRecursiveRoot(root, hier, train, valid, test, computed);
    }


    @Override
    public String[] getOptionArgs() {
        return g_Options;
    }


    @Override
    public int[] getOptionArgArities() {
        return g_OptionArities;
    }


    @Override
    public int getNbMainArgs() {
        return 1;
    }


    @Override
    public void showHelp() {
    }


    public static void main(String[] args) throws IOException {
        try {
            HMCNodeWiseModels m = new HMCNodeWiseModels();
            m.run(args);
        }
        catch (IOException io) {
            ClusLogger.info("IO Error: " + io.getMessage());
            io.printStackTrace();
        }
        catch (ClusException cl) {
            ClusLogger.info("Error: " + cl.getMessage());
            cl.printStackTrace();
        }
        catch (ClassNotFoundException cn) {
            ClusLogger.info("Error: " + cn.getMessage());
        }
        catch (InterruptedException ie) {
            ClusLogger.info("Error: " + ie.getMessage());
            ie.printStackTrace();
        }
        catch (Exception e) {
            
            e.printStackTrace();
        }
        ClusLogger.info("Finished. Have a nice day, Stevanche.");
    }

}

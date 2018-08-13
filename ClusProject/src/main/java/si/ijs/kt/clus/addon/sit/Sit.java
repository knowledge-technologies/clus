
package si.ijs.kt.clus.addon.sit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import si.ijs.kt.clus.addon.sit.mtLearner.ClusLearner;
import si.ijs.kt.clus.addon.sit.mtLearner.KNNLearner;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.AllTargets;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.GeneticSearch;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.GreedySIT;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.NoStopSearch;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.OneTarget;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.SearchAlgorithm;
import si.ijs.kt.clus.addon.sit.searchAlgorithm.TC;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.io.ARFFFile;
import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.io.ClusView;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.ClusStat;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsExperimental;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.ResourceInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.IntervalCollection;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgsProvider;


public class Sit implements CMDLineArgsProvider {

    protected Settings m_Sett = new Settings();
    protected ClusSchema m_Schema;
    protected RowData m_Data;
    protected MTLearner m_Learner;
    protected SearchAlgorithm m_Search;
    protected int m_SearchSelection;


    private Sit() {
    } // make sit a singleton

    private static Sit singleton = null;


    /**
     * Returns the one and only Sit instance (singleton pattern)
     * 
     * @return Sit singleton
     */
    public static Sit getInstance() {
        if (singleton == null) {
            singleton = new Sit();
        }
        return singleton;
    }


    /**
     * Initialize:
     * -load settings
     * -create schema
     * -read in data
     * 
     * @throws IOException
     * @throws ClusException
     */
    public void initialize() throws IOException, ClusException {
        // Load settings file
        ARFFFile arff = null;
        ClusLogger.info("Loading '" + m_Sett.getGeneric().getAppName() + "'");
        ClusRandom.initialize(m_Sett);
        ClusReader reader = new ClusReader(m_Sett.getData().getDataFile(), m_Sett);
        ClusLogger.info();
        ClusLogger.info("Reading ARFF Header");
        arff = new ARFFFile(reader);
        m_Schema = arff.read(m_Sett);
        // Count rows and move to data segment
        ClusLogger.info("Reading CSV Data");
        // Updata schema based on settings
        m_Sett.updateTarget(m_Schema);
        m_Schema.initializeSettings(m_Sett);
        m_Sett.getAttribute().setTarget(m_Schema.getTarget().toString());
        m_Sett.getAttribute().setDisabled(m_Schema.getDisabled().toString());
        m_Sett.getAttribute().setClustering(m_Schema.getClustering().toString());
        m_Sett.getAttribute().setDescriptive(m_Schema.getDescriptive().toString());
        // Load data from file
        if (ResourceInfo.isLibLoaded()) {
            ClusStat.m_InitialMemory = ResourceInfo.getMemory();
        }
        ClusView view = m_Schema.createNormalView();
        m_Data = view.readData(reader, m_Schema);
        reader.close();
        // Preprocess and initialize induce
        m_Sett.update(m_Schema);
        getSettings().getExperimental();
        // Set XVal field in Settings
        SettingsExperimental.IS_XVAL = true;
        ClusLogger.info("Has missing values: " + m_Schema.hasMissing());
    }


    /**
     * Returns the current settings object
     * 
     * @return settings
     */
    public final Settings getSettings() {
        return m_Sett;
    }


    /**
     * Initialize the settings object
     * 
     * @param cargs
     *        Commandline arguments
     * @throws IOException
     */
    public final void initSettings(CMDLineArgs cargs) throws IOException {
        m_Sett.initialize(cargs, true);
    }


    /**
     * Initialize the MTLearner with the current data and settings.
     */
    private void InitLearner() {

        if (this.m_Sett.getSIT().getLearnerName().equals("KNN")) {
            ClusLogger.info("Using KNN Learner");
            this.m_Learner = new KNNLearner();
        }
        else {
            ClusLogger.info("Using Clus Learner");
            this.m_Learner = new ClusLearner();

        }
        // this.m_Learner = new AvgLearner();
        this.m_Learner.init(this.m_Data, this.m_Sett);
        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        this.m_Learner.setMainTarget(mainTarget);

    }


    /**
     * Initialize the MTLearner with partial data for XVAL.
     */
    private void InitLearner(RowData data) {
        if (this.m_Sett.getSIT().getLearnerName().equals("KNN")) {
            ClusLogger.info("Using KNN Learner");
            this.m_Learner = new KNNLearner();
        }
        else {
            ClusLogger.info("Using Clus Learner");
            this.m_Learner = new ClusLearner();

        }
        // this.m_Learner = new AvgLearner();

        this.m_Learner.init(data, this.m_Sett);
        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        this.m_Learner.setMainTarget(mainTarget);

    }


    /**
     * Initialize the SearchAlgorithm
     */
    private void InitSearchAlgorithm() {
        String search = m_Sett.getSIT().getSearchName();
        if (search.equals("OneTarget")) {
            this.m_Search = new OneTarget();
            ClusLogger.info("Search = single target");
        }
        else if (search.equals("AllTargets")) {
            this.m_Search = new AllTargets();
            ClusLogger.info("Search = full multi target");
        }
        else if (search.equals("GeneticSearch")) {
            this.m_Search = new GeneticSearch();
            ClusLogger.info("Search = Genetic search strategy");
        }
        else if (search.equals("SIT")) {
            this.m_Search = new GreedySIT();
            ClusLogger.info("Search = SIT, with stop criterion");
        }
        else if (search.equals("NoStop")) {
            this.m_Search = new NoStopSearch();
            ClusLogger.info("Search = SIT, no stop criterion");
        }
        else if (search.equals("TC")) {
            this.m_Search = new TC();
            ClusLogger.info("Search = TC");
        }
        else {
            System.err.println("Search strategy unknown!");
        }

        this.m_Search.setMTLearner(this.m_Learner);
        this.m_Search.setSettings(this.m_Sett);

    }


    /**
     * Start the search for the optimal subset using the current learner and search algorithm
     * 
     * @return Targetset The found subset
     * @throws ClusException 
     */
    public TargetSet search() throws ClusException {

        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        IntervalCollection candidates = new IntervalCollection(m_Sett.getAttribute().getTarget());
        TargetSet candidateSet = new TargetSet(m_Schema, candidates);
        return m_Search.search(mainTarget, candidateSet);

    }

    /*************************************
     * CMDLineArgsProvider implementation
     *************************************/
    public final static String[] OPTION_ARGS = { "xval" };
    public final static int[] OPTION_ARITIES = { 0 };


    @Override
    public int getNbMainArgs() {
        return 1;
    }


    @Override
    public int[] getOptionArgArities() {
        return OPTION_ARITIES;
    }


    @Override
    public String[] getOptionArgs() {
        return OPTION_ARGS;
    }


    @Override
    public void showHelp() {
    }


    public void singleRun() throws ClusException {
        ClusLogger.info("Starting single run");
        /* Init the Learner */
        InitLearner();
        /* Init the Search algorithm */
        InitSearchAlgorithm();

        ErrorOutput errOut = new ErrorOutput(this.m_Sett);
        try {
            errOut.writeHeader();
        }
        catch (IOException e) {
            
            e.printStackTrace();
        }

        TargetSet trgset = search();

        // compute the error of the final set
        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        int errorIdx = mainTarget.getArrayIndex();
        // predict a few folds
        int nbFolds = 20;
        this.m_Learner.initXVal(nbFolds);
        // learn a model for each fold
        ArrayList<RowData[]> folds = new ArrayList<RowData[]>();
        for (int f = 0; f < nbFolds; f++) {
            folds.add(m_Learner.LearnModel(trgset, f));
        }

        //double finalerror = Evaluator.getPearsonCorrelation(folds, errorIdx);
        Evaluator.getPearsonCorrelation(folds, errorIdx);

        // errOut.addFold(0,0,m_Learner.getName(),m_Search.getName(),Integer.toString(mt+1),finalerror,"["+trgset.toString()+"]");

    }


    public void XValRun() throws Exception {
        ErrorOutput errOut = new ErrorOutput(this.m_Sett);
        errOut.writeHeader();
        ClusLogger.info("Starting XVal run");

        XValRandomSelection m_XValSel = null;
        int nrFolds = 26;
        try {

            m_XValSel = new XValRandomSelection(m_Data.getNbRows(), nrFolds);
        }
        catch (ClusException e) {
            e.printStackTrace();
        }

        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        int errorIdx = mainTarget.getArrayIndex();

        for (int i = 0; i < nrFolds; i++) {
            ClusLogger.info("Outer XVAL fold " + (i + 1));
            XValSelection msel = new XValSelection(m_XValSel, i);
            RowData train = (RowData) m_Data.cloneData();
            RowData test = (RowData) train.select(msel);

            ClusLogger.info(test.getNbRows());

            /* Init the Learner */
            InitLearner(train);
            /* Init the Search algorithm */
            InitSearchAlgorithm();

            Long d = (new Date()).getTime();
            TargetSet searchResult = search();

            // find the error
            m_Learner.setTestData(test);

            RowData[] predictions = m_Learner.LearnModel(searchResult);

            /*
             * RowData t = predictions[0];
             * DataTuple tt = t.getTuple(0);
             * double dt = mainTarget.getNumeric(tt);
             * RowData p = predictions[1];
             * DataTuple tp = p.getTuple(0);
             * double dp = mainTarget.getNumeric(tp);
             */
            Long new_d = (new Date()).getTime();
            Long dif = new_d - d;

            double error = 0;
            String errorName = m_Sett.getSIT().getError();
            if (errorName.equals("MSE")) {
                error = Evaluator.getMSE(predictions, errorIdx);
            }
            else if (errorName.equals("MisclassificationError")) {
                error = Evaluator.getMisclassificationError(predictions, errorIdx);
            }
            else {

                error = Evaluator.getPearsonCorrelation(predictions, errorIdx);
            }

            // errOut.addFold(0,i,m_Learner.getName(),m_Search.getName(),Integer.toString(mt),error,"\""+searchResult.toString()+"
            // \"",dt,dp);
            errOut.addFold(0, i, m_Learner.getName(), m_Search.getName(), Integer.toString(mt + 1), error, "\"" + searchResult.toString() + " \"", dif);

        }

    }


    public void YATSXValRun() throws Exception {
        ErrorOutput errOut = new ErrorOutput(this.m_Sett);
        errOut.writeHeader();
        ClusLogger.info("Starting XVal run");

        XValRandomSelection m_XValSel = null;
        int nrFolds = 500;
        try {

            m_XValSel = new XValRandomSelection(m_Data.getNbRows(), nrFolds);
        }
        catch (ClusException e) {
            e.printStackTrace();
        }

        int mt = new Integer(m_Sett.getSIT().getMainTarget()) - 1;
        ClusAttrType mainTarget = m_Schema.getAttrType(mt);
        int errorIdx = mainTarget.getArrayIndex();

        for (int i = 0; i < nrFolds; i++) {
            ClusLogger.info("Outer XVAL fold " + (i + 1));
            XValSelection msel = new XValSelection(m_XValSel, i);
            RowData train = (RowData) m_Data.cloneData();
            RowData test = (RowData) train.select(msel);

            // ClusLogger.info(test.getNbRows());

            /* Init the Learner */
            InitLearner(train);
            /* Init the Search algorithm */
            InitSearchAlgorithm();

            Long d = (new Date()).getTime();
            TargetSet searchResult = search();

            // find the error
            m_Learner.setTestData(test);

            RowData[] predictions = m_Learner.LearnModel(searchResult);

            // add new data to training

            RowData pred = predictions[1];
            RowData xtr_train = test.deepCloneData();
            for (int t = 0; t < pred.getNbRows(); t++) {
                DataTuple tp = pred.getTuple(t);
                double dp = mainTarget.getNumeric(tp);
                DataTuple clone = xtr_train.getTuple(t);
                ((NumericAttrType) mainTarget).setNumeric(clone, dp);

            }

            RowData new_train = new RowData(train.getSchema(), train.getNbRows() + xtr_train.getNbRows());

            for (int j = 0; j < train.getNbRows(); j++) {
                new_train.setTuple(train.getTuple(j), j);
            }
            for (int j = train.getNbRows(); j < train.getNbRows() + xtr_train.getNbRows(); j++) {
                new_train.setTuple(xtr_train.getTuple(j - train.getNbRows()), j);
            }

            InitLearner(xtr_train);
            m_Learner.setTestData(test);

            // predictions = m_Learner.LearnModel(searchResult);

            Long new_d = (new Date()).getTime();
            Long dif = new_d - d;

            double error = 0;
            String errorName = m_Sett.getSIT().getError();

            if (errorName.equals("RME")) {
                error = Evaluator.getRelativeError(predictions, errorIdx);
                ClusLogger.info(error);
            }
            else if (errorName.equals("MSE")) {
                error = Evaluator.getMSE(predictions, errorIdx);
            }
            else if (errorName.equals("MisclassificationError")) {
                error = Evaluator.getMisclassificationError(predictions, errorIdx);
            }
            else {

                error = Evaluator.getPearsonCorrelation(predictions, errorIdx);
            }

            // errOut.addFold(0,i,m_Learner.getName(),m_Search.getName(),Integer.toString(mt),error,"\""+searchResult.toString()+"
            // \"",dt,dp);
            errOut.addFold(0, i, m_Learner.getName(), m_Search.getName(), Integer.toString(mt + 1), error, "\"" + searchResult.toString() + " \"", dif);

        }

    }


    /***************************************
     * MAIN
     ***************************************/

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Sit sit = Sit.getInstance();
        Settings sett = sit.getSettings();
        CMDLineArgs cargs = new CMDLineArgs(sit);
        cargs.process(args);
        if (cargs.getNbMainArgs() == 0) {
            sit.showHelp();
            ClusLogger.info();
            ClusLogger.info("Expected main argument");
            System.exit(0);
        }
        if (cargs.allOK()) {
            sett.getGeneric().setDate(new Date());
            sett.getGeneric().setAppName(cargs.getMainArg(0));
            sit.initSettings(cargs);

        }
        else {
            System.err.println("Arguments not ok?!");
        }
        sit.initialize();

        /* Search for the optimal subset */
        sit.m_SearchSelection = 1;
        sit.XValRun();

        // sit.YATSXValRun();

        ClusLogger.info("Finished");
    }
}

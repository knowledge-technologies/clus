
package si.ijs.kt.clus.addon.sit.mtLearner;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.exception.ClusException;


public abstract class MTLearnerImpl implements MTLearner {

    protected RowData m_Data;
    protected RowData m_Test = null;
    protected Settings m_Sett;
    protected XValMainSelection m_XValSel;
    protected ResultsCache m_Cache;
    protected ClusAttrType m_MainTarget;


    /**************************
     * Interface functions
     **************************/

    /**
     * @see MTLearner
     */
    @Override
    public void init(RowData data, Settings sett) {
        this.m_Data = data;
        this.m_Sett = sett;
        this.m_Cache = new ResultsCache();
    }


    /**
     * @see MTLearner
     */
    @Override
    public RowData[] LearnModel(TargetSet targets) throws Exception {
        if (m_Test == null) { throw new Exception(); }

        RowData[] result = m_Cache.getResult(targets, this.m_Test);
        if (result != null) { return result; }
        result = LearnModel(targets, this.m_Data, this.m_Test);
        m_Cache.addResult(targets, result);
        return result;
    }


    /**
     * @see MTLearner
     */
    public void setData(RowData data) {
        this.m_Data = data;
    }


    /**
     * @see MTLearner
     */
    @Override
    public void initXVal(int nrFolds) {
        try {
            ClusRandom.initialize(m_Sett);// use the same random seed!
            m_XValSel = new XValRandomSelection(m_Data.getNbRows(), nrFolds);
        }
        catch (ClusException e) {
            e.printStackTrace();
        }

    }


    /**
     * @see MTLearner
     */
    @Override
    public int initLOOXVal() {
        try {
            ClusRandom.initialize(m_Sett);// use same random seed
            m_XValSel = new XValRandomSelection(m_Data.getNbRows(), m_Data.getNbRows());
        }
        catch (ClusException e) {
            e.printStackTrace();
        }

        return m_Data.getNbRows();

    }


    /**
     * @see MTLearner
     */
    @Override
    public RowData[] LearnModel(TargetSet targets, int foldNr) {
        XValSelection msel = new XValSelection(m_XValSel, foldNr);
        RowData train = (RowData) m_Data.cloneData();
        RowData test = (RowData) train.select(msel);

        RowData[] result = m_Cache.getResult(targets, test);
        if (result != null) { return result; }
        result = LearnModel(targets, train, test);
        m_Cache.addResult(targets, result);
        return result;
    }


    /**
     * Sets the testdata to test
     */
    @Override
    public void setTestData(RowData test) {
        this.m_Test = test;
    }


    @Override
    public void setMainTarget(ClusAttrType target) {
        this.m_MainTarget = target;
    }


    protected abstract RowData[] LearnModel(TargetSet targets, RowData train, RowData test);
}

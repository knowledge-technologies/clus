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
import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.ensemble.ClusReadWriteLock;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.selection.ClusSelection;
import si.ijs.kt.clus.util.exception.ClusException;


public class ClusRun extends ClusModelInfoList {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int m_Index;
    protected boolean m_FileTestSet;
    protected ClusData m_Train, m_Prune, m_Orig;
    protected ClusSelection m_TestSel, m_PruneSel;
    protected TupleIterator m_Test;
    protected TupleIterator m_UnlabeledData;
    protected ClusSummary m_Summary;
    
    private boolean m_IsInternalXValRun = false;

    ClusReadWriteLock m_Lock = new ClusReadWriteLock();


    public ClusRun(ClusData train, ClusSummary summary) {
        m_Index = 1;
        m_Train = train;
        m_Summary = summary;
    }


    public ClusRun(ClusRun other) throws ClusException {
        m_Index = 1;
        m_Train = other.m_Train;
        m_Prune = other.m_Prune;
        m_Orig = other.m_Orig;
        m_TestSel = other.m_TestSel;
        m_PruneSel = other.m_PruneSel;
        m_Test = other.m_Test;
        m_UnlabeledData = other.m_UnlabeledData;
        m_Summary = other.m_Summary.getSummaryClone();
        setModels(other.cloneModels());
        m_IsInternalXValRun = other.m_IsInternalXValRun;
    }


    @Override
    public ClusStatManager getStatManager() {
        return m_Summary.getStatManager();
    }
    
	public void setClusteringWeights(ClusAttributeWeights weights){
	    m_Summary.m_StatMgr.m_ClusteringWeights.m_Weights = weights.m_Weights;
	}


    /***************************************************************************
     * Getting to the summary
     ***************************************************************************/

    public ClusSummary getSummary() {
        return m_Summary;
    }


    @Override
    public ClusErrorList getTrainError() {
        return m_Summary.getTrainError();
    }


    @Override
    public ClusErrorList getTestError() {
        return m_Summary.getTestError();
    }


    @Override
    public ClusErrorList getValidationError() {
        return m_Summary.getValidationError();
    }


    /***************************************************************************
     * Index of clus run
     ***************************************************************************/

    public final int getIndex() {
        return m_Index;
    }


    public final void setIndex(int idx) {
        m_Index = idx;
    }


    public final String getIndexString() {
        String ridx = String.valueOf(getIndex());
        if (getIndex() < 10)
            ridx = "0" + ridx;
        return ridx;
    }


    /***************************************************************************
     * Original set
     ***************************************************************************/

    public final ClusData getOriginalSet() {
        return m_Orig;
    }


    public final void setOrigSet(ClusData data) {
        m_Orig = data;
    }


    public final RowData getDataSet(int whichone) throws ClusException, IOException, InterruptedException {
        switch (whichone) {
            case TRAINSET:
                return (RowData) getTrainingSet();
            case TESTSET:
                return getTestSet();
            case VALIDATIONSET:
                return (RowData) getPruneSet();
        }
        return null;
    }


    /***************************************************************************
     * Training set
     * @throws InterruptedException 
     ***************************************************************************/

    public final ClusData getTrainingSet() throws InterruptedException {
        m_Lock.readingLock();
        ClusData train = m_Train;
        m_Lock.readingUnlock();
        return train;
    }


    public final void setTrainingSet(ClusData data) {
        m_Train = data;
    }


    public final TupleIterator getTrainIter() {
        return ((RowData) m_Train).getIterator();
    }


    // To keep training examples in same order :-)
    public final void copyTrainingData() {
        RowData clone = (RowData) m_Train.cloneData();
        setTrainingSet(clone);
    }


    public final ClusSelection getTestSelection() {
        return m_TestSel;
    }


    /***************************************************************************
     * Test set
     ***************************************************************************/

    public final void setTestSet(TupleIterator iter) {
        m_Test = iter;
    }


    public final TupleIterator getTestIter() {
        return m_Test;
    }


    // If the test set is specified as a separate file, this method first reads the entire
    // file into memory, while the above method provides an interator that reads tuples one by one
    public final RowData getTestSet() throws IOException, ClusException {
        if (m_Test == null)
            return null;
        RowData data = (RowData) m_Test.getData();
        if (data == null) {
            data = (RowData) m_Test.createInMemoryData();
            m_Test = data.getIterator();
        }
        return data;
    }


    /***************************************************************************
     * Pruning set
     ***************************************************************************/

    public final ClusData getPruneSet() {
        return m_Prune;
    }


    public final TupleIterator getPruneIter() {
        return ((RowData) m_Prune).getIterator();
    }


    public final void setPruneSet(ClusData data, ClusSelection sel) {
        m_Prune = data;
        m_PruneSel = sel;
    }


    public final ClusSelection getPruneSelection() {
        return m_PruneSel;
    }


    public void combineTrainAndValidSets() throws InterruptedException, ClusException {
        RowData valid = (RowData) getPruneSet();
        if (valid != null) {
            RowData train = (RowData) getTrainingSet();
            ArrayList lst = train.toArrayList();
            lst.addAll(valid.toArrayList());
            setTrainingSet(new RowData(lst, train.getSchema()));
            setPruneSet(null, null);
            changePruneError(null);
            copyTrainingData();
        }
    }

    /***************************************************************************
    * Unlabeled set
    ****************************************************************************/

    	public final void setUnlabeledSet(TupleIterator iter) {
    		m_UnlabeledData = iter;
    	}

    	public final TupleIterator getUnlabeledIter() {
    		return m_UnlabeledData;
    	}

    	// If the unlabeled set is specified as a separate file, this method first reads the entire
    	// file into memory, while the above method provides an interator that reads tuples one by one
    	public final RowData getUnlabeledSet() throws IOException, ClusException {
    		if (m_UnlabeledData == null) return null;
    		RowData data = (RowData)m_UnlabeledData.getData();
    		if (data == null) {
    			data = (RowData)m_UnlabeledData.createInMemoryData();
    			m_UnlabeledData = data.getIterator();
    		}
    		return data;
    	}	


    /***************************************************************************
     * Preparation
     * @throws ClusException 
     ***************************************************************************/

    public void changeTestError(ClusErrorList par) throws ClusException {
        m_Summary.setTestError(par);
        int nb_models = getNbModels();
        for (int i = 0; i < nb_models; i++) {
            ClusModelInfo my = getModelInfo(i);
            my.setTestError(par.getErrorClone());
        }
    }


    public void changePruneError(ClusErrorList par) throws ClusException {
        m_Summary.setValidationError(par);
        int nb_models = getNbModels();
        for (int i = 0; i < nb_models; i++) {
            ClusModelInfo my = getModelInfo(i);
            my.setValidationError(par != null ? par.getErrorClone() : null);
        }
    }


    public void deleteData() {
        m_Train = null;
        m_Prune = null;
        m_Orig = null;
        m_TestSel = null;
        m_PruneSel = null;
        m_Test = null;
    }


    public void deleteDataAndModels() {
        deleteData();
        deleteModels();
    }
    
    /***************************************************************************
     * Other
     ***************************************************************************/
    
    public boolean getIsInternalXValRun() {
    	return m_IsInternalXValRun;
    }
    
    public void setIsInternalXValRun(boolean v) {
    	m_IsInternalXValRun = v;
    }
    
}

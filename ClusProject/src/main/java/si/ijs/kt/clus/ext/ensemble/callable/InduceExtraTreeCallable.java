
package si.ijs.kt.clus.ext.ensemble.callable;

import java.util.concurrent.Callable;

import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.ensemble.container.OneBagResults;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.util.ClusRandomNonstatic;


public class InduceExtraTreeCallable implements Callable<OneBagResults> {

    private ClusEnsembleInduce m_Cei;
    private ClusRun m_Cr;
    private int m_I;
    private TupleIterator m_Train_iterator, m_Test_iterator;
    private ClusRandomNonstatic m_Rnd;
    private ClusStatManager m_Mgr;


    public InduceExtraTreeCallable(ClusEnsembleInduce cei, ClusRun cr, int i, TupleIterator train_iterator, TupleIterator test_iterator, ClusRandomNonstatic rnd, ClusStatManager mgr) {
        m_Cei = cei;
        m_Cr = cr;
        m_I = i;
        m_Train_iterator = train_iterator;
        m_Test_iterator = test_iterator;
        m_Rnd = rnd;
        m_Mgr = mgr;
    }


    @Override
    public OneBagResults call() throws Exception {
        
        return m_Cei.induceOneExtraTree(m_Cr, m_I, m_Train_iterator, m_Test_iterator, m_Rnd, m_Mgr);
    }

}

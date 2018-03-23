
package si.ijs.kt.clus.error;

import java.util.ArrayList;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.CombStat;
import si.ijs.kt.clus.util.ClusException;


public class ClusSumError extends ClusError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ArrayList<ClusError> m_Errors = new ArrayList<>();


    public ClusSumError(ClusErrorList par) {
        super(par);
    }


    @Override
    public double getModelError() {
        int dim = 0;
        double result = 0.0;
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            result += err.getModelError() * err.getDimension();
            dim += err.getDimension();
        }
        return result / dim;
    }


    @Override
    public void reset() {
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            err.reset();
        }
    }


    @Override
    public void add(ClusError other) {
        ClusSumError others = (ClusSumError) other;
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            err.add(others.getComponent(i));
        }
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) throws ClusException {
        // this can be made more general
        CombStat stat = (CombStat) pred;
        getComponent(0).addExample(tuple, stat.getRegressionStat());
        getComponent(1).addExample(tuple, stat.getClassificationStat());
    }


    @Override
    public double computeLeafError(ClusStatistic stat) {
        CombStat cstat = (CombStat) stat;
        return getComponent(0).computeLeafError(cstat.getRegressionStat()) + getComponent(1).computeLeafError(cstat.getClassificationStat());
    }


    @Override
    public void addExample(DataTuple real, DataTuple pred) throws ClusException {
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            err.addExample(real, pred);
        }
    }


    @Override
    public void addInvalid(DataTuple tuple) {
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            err.addInvalid(tuple);
        }
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) throws ClusException {
        ClusSumError result = new ClusSumError(par);
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            result.addComponent(err.getErrorClone(par));
        }
        return result;
    }


    @Override
    public String getName() {
        StringBuffer name = new StringBuffer();
        for (int i = 0; i < m_Errors.size(); i++) {
            ClusError err = m_Errors.get(i);
            if (i != 0)
                name.append(", ");
            name.append(err.getName());
        }
        return name.toString();
    }


    public void addComponent(ClusError err) {
        m_Errors.add(err);
    }


    public ClusError getComponent(int i) {
        return (ClusError) m_Errors.get(i);
    }


    @Override
    public boolean shouldBeLow() {
        return true;
    }
}

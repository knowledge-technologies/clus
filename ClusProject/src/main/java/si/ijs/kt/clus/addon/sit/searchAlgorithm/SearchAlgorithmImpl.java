
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import java.util.ArrayList;

import si.ijs.kt.clus.addon.sit.Evaluator;
import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Abstract implementation of the SearchAlgo interface.
 * Provides some basic functions needed by most implementations.
 * 
 * @author beau
 *
 */
public abstract class SearchAlgorithmImpl implements SearchAlgorithm {

    protected MTLearner learner;
    protected Settings m_Sett;


    @Override
    public void setMTLearner(MTLearner learner) {
        this.learner = learner;
    }


    @Override
    public void setSettings(Settings s) {
        this.m_Sett = s;
    }


    protected double eval(TargetSet tset, ClusAttrType mainTarget) throws ClusException {
        // create a few folds
        // int nbFolds = 23;
        int nbFolds = learner.initLOOXVal();

        // learn a model for each fold
        ArrayList<RowData[]> folds = new ArrayList<RowData[]>();
        for (int f = 0; f < nbFolds; f++) {
            folds.add(learner.LearnModel(tset, f));
        }

        String error = m_Sett.getSIT().getError();
        if (error.equals("MSE")) {
            // ClusLogger.info("using mse");
            return 1 - Evaluator.getMSE(folds, mainTarget.getArrayIndex());
        }
        if (error.equals("MisclassificationError")) { return 1 - Evaluator.getMisclassificationError(folds, mainTarget.getArrayIndex()); }

        return Evaluator.getPearsonCorrelation(folds, mainTarget.getArrayIndex());
    }

}

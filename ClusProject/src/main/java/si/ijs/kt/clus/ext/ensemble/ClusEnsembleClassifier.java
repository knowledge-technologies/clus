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

package si.ijs.kt.clus.ext.ensemble;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleMethod;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


public class ClusEnsembleClassifier extends ClusInductionAlgorithmType {

    public ClusEnsembleClassifier(Clus clus) {
        super(clus);
        
    }


    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        if (sett.getEnsemble().getEnsembleMethod() == EnsembleMethod.Boosting) {
            return new ClusBoostingInduce(schema, sett);
        }
        else {
            return new ClusEnsembleInduce(schema, sett, m_Clus);
        }
    }


    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        
        return null;
    }


    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {
    }


    @Override
    public void printInfo() {
        ClusLogger.info("Ensemble Classifier");
    }


    @Override
    public void postProcess(ClusRun cr) throws ClusException, IOException {
        
        
    }

}

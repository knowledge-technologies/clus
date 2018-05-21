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

package si.ijs.kt.clus.algo;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.util.ResourceInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;


/**
 * For each type of algorithm there should be a ClusClassifier object.
 *
 */
public abstract class ClusInductionAlgorithmType {

    public final static int REGULAR_TREE = 0;

    // For each type of algorithm there should be a ClusClassifier object

    protected Clus m_Clus;


    public ClusInductionAlgorithmType(Clus clus) {
        m_Clus = clus;
    }


    public Clus getClus() {
        return m_Clus;
    }


    public ClusInductionAlgorithm getInduce() {
        return getClus().getInduce();
    }


    public ClusStatManager getStatManager() {
        return getInduce().getStatManager();
    }


    public Settings getSettings() {
        return getClus().getSettings();
    }


    public abstract ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException;


    public void printInfo() {
        System.out.println("Classifier: " + getClass().getName());
    }


    public abstract void pruneAll(ClusRun cr) throws ClusException, IOException, InterruptedException;


    public abstract ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException, InterruptedException;

    // should be implemented by subclass
    public abstract void postProcess(ClusRun cr) throws ClusException, IOException, InterruptedException;



    /**
     * Calls the induce function for each of the learning algorithms of this TYPE.
     * Also collects the information about computational cost of training.
     * 
     * @param cr
     * @throws Exception 
     */
    public void induceAll(ClusRun cr) throws Exception {
        
        // induce default model
        ClusModelInfo def_info = cr.addModelInfo(ClusModel.DEFAULT);
        def_info.setModel(ClusDecisionTree.induceDefault(cr));

        long start_time = ResourceInfo.getTime();

        getInduce().induceAll(cr); // Train the algorithms of this type.
        long done_time = ResourceInfo.getTime();
        
        cr.setInductionTime(done_time - start_time);
       
        pruneAll(cr);
        cr.setPruneTime(ResourceInfo.getTime() - done_time);
        postProcess(cr);
        
    }


    public ClusModel induceSingle(ClusRun cr) throws Exception {
        ClusModel unpruned = induceSingleUnpruned(cr);
        return pruneSingle(unpruned, cr);
    }


    public ClusModel induceSingleUnpruned(ClusRun cr) throws Exception {
        return getInduce().induceSingleUnpruned(cr);
    }


    public void saveInformation(String fname) {
    }
}

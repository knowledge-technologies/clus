/**
 * *********************************************************************** Clus - Software for
 * Predictive Clustering * Copyright (C) 2007 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia * * This program is free software: you can
 * redistribute it and/or modify * it under the terms of the GNU General Public License as published
 * by * the Free Software Foundation, either version 3 of the License, or * (at your option) any
 * later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT
 * ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the * GNU General Public License for more details. * * You should have received a
 * copy of the GNU General Public License * along with this program. If not, see
 * <http://www.gnu.org/licenses/>. * * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.
 * * ***********************************************************************
 */
package si.ijs.kt.clus.main;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;

public abstract class ClusModelInfoList implements Serializable {

  private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
  public static final int TRAINSET = 0;
  public static final int TESTSET = 1;
  public static final int VALIDATIONSET = 2;

  protected ClusModelInfo m_AllModelsMI = new ClusModelInfo("AllModels");
  protected ArrayList<ClusModelInfo> m_Models = new ArrayList<ClusModelInfo>();
  protected long m_IndTime,
      m_PrepTime,
      m_PruneTime,
      m_PredictionTime; // for storing execution time info
  protected int
      m_PredictionTimeNbExamples; // this counts all the examples that pass over predictWeighted() for each model (needed to properly calculate average prediction time per example)
  protected long m_IndTimeSequential = 0; // to be used with ensemble parallel execution

  /**
   * ************************************************************************* Iterating over models
   * *************************************************************************
   */
  public int getNbModels() {
    return m_Models.size();
  }

  /**
   * @param i Usually ClusModel model type (Default, Original, Pruned). However, can also be
   *     something else. For example for ClusForest this is the index of decision tree.

   */
  public ClusModelInfo getModelInfo(int i) {
    if (i >= m_Models.size()) return null;
    return m_Models.get(i);
  }

  /**
   * @param i Usually ClusModel model type (Default, Original, Pruned).
   * @param j If model "i" does not exist, return model "j". Typical use:
   *     getModelInfoFallback(ClusModel.PRUNED, ClusModel.ORIGINAL);

   */
  public ClusModelInfo getModelInfoFallback(int i, int j) {
    ClusModelInfo info = getModelInfo(i);
    if (info == null) info = getModelInfo(j);
    return info;
  }

  public ClusModelInfo getAllModelsMI() {
    return m_AllModelsMI;
  }

  /**
   * @param i Usually ClusModel model type (Default, Original, Pruned). However, can also be
   *     something else. For example for ClusForest this is the index of decision tree.

   */
  public void setModelInfo(int i, ClusModelInfo info) {
    m_Models.set(i, info);
  }

  /**
   * @param i Usually ClusModel model type (Default, Original, Pruned). However, can also be
   *     something else. For example for ClusForest this is the index of decision tree.

   */
  public ClusModel getModel(int i) {
    return getModelInfo(i).getModel();
  }

  /**
   * @param i Usually ClusModel model type (Default, Original, Pruned). However, can also be
   *     something else. For example for ClusForest this is the index of decision tree.

   */
  public String getModelName(int i) {
    return getModelInfo(i).getName();
  }

  public void setModels(ArrayList<ClusModelInfo> models) {
    m_Models = models;
  }

  public void showModelInfos() {
    for (int i = 0; i < getNbModels(); i++) {
      ClusModelInfo info = getModelInfo(i);
      ClusLogger.info("Model " + i + " name: '" + info.getName() + "'");
    }
  }

  /**
   * ************************************************************************* Adding models to it
   *
   * @throws ClusException *************************************************************************
   */
  public ClusModelInfo initModelInfo(int i) throws ClusException {
    String name = "M" + (i + 1);
    if (i == ClusModel.DEFAULT) name = "Default";
    if (i == ClusModel.ORIGINAL) name = "Original";
    if (i == ClusModel.PRUNED) name = "Pruned";
    ClusModelInfo inf = new ClusModelInfo(name);
    initModelInfo(inf);
    return inf;
  }

  /**
   * Analogue of {@code intiModelInfo(int i)}. Used in kNN case
   *
   * @param i
   * @param name Name of the kNN model.

   * @throws ClusException
   */
  public ClusModelInfo initModelInfo(int i, String name) throws ClusException {
    ClusModelInfo inf = new ClusModelInfo(name);
    initModelInfo(inf);
    return inf;
  }

  public void initModelInfo(ClusModelInfo inf) throws ClusException {
    inf.setSelectedErrorsClone(getTrainError(), getTestError(), getValidationError());
    inf.setStatManager(getStatManager());
  }

  public ClusModelInfo addModelInfo(String name) throws ClusException {
    ClusModelInfo inf = new ClusModelInfo(name);
    addModelInfo(inf);
    return inf;
  }

  public void addModelInfo(ClusModelInfo inf) throws ClusException {
    initModelInfo(inf);
    m_Models.add(inf);
  }

  public ClusModelInfo addModelInfo(int i) throws ClusException {
    while (i >= m_Models.size()) m_Models.add(null);
    ClusModelInfo inf = m_Models.get(i);
    if (inf == null) {
      inf = initModelInfo(i);
      m_Models.set(i, inf);
    }
    return inf;
  }

  /**
   * Analogue of the {@code addModelInfo(int i)} which is used in kNN case.
   *
   * @param i
   * @param model_name The name of the model

   * @throws ClusException
   */
  public ClusModelInfo addModelInfo(int i, String model_name) throws ClusException {
    while (i >= m_Models.size()) m_Models.add(null);
    ClusModelInfo inf = m_Models.get(i);
    if (inf == null) {
      inf = initModelInfo(i, model_name);
      m_Models.set(i, inf);
    }
    return inf;
  }

  public abstract ClusStatManager getStatManager();

  public abstract ClusErrorList getTrainError();

  public abstract ClusErrorList getTestError();

  public abstract ClusErrorList getValidationError();

  /**
   * ************************************************************************* Functions for all
   * models
   *
   * @throws ClusException *************************************************************************
   */
  public ArrayList<ClusModelInfo> cloneModels() throws ClusException {
    int nb_models = getNbModels();
    ArrayList<ClusModelInfo> clones = new ArrayList<ClusModelInfo>();
    for (int i = 0; i < nb_models; i++) {
      ClusModelInfo my = getModelInfo(i);
      if (my != null) my = my.cloneModelInfo();
      clones.add(my);
    }
    return clones;
  }

  public void deleteModels() {
    int nb_models = getNbModels();
    for (int i = 0; i < nb_models; i++) {
      ClusModelInfo my = getModelInfo(i);
      my.deleteModel();
    }
  }

  public void checkModelInfo() throws ClusException {
    int nb_models = getNbModels();
    for (int i = 0; i < nb_models; i++) {
      ClusModelInfo my = getModelInfo(i);
      my.check();
    }
  }

  public boolean hasModel(int i) {
    ClusModelInfo my = getModelInfo(i);
    return my.getNbModels() > 0;
  }

  public void copyAllModelsMIs() {
    ClusModelInfo allmi = getAllModelsMI();
    int nb_models = getNbModels();
    for (int i = 0; i < nb_models; i++) {
      ClusModelInfo my = getModelInfo(i);
      if (my != null) allmi.copyModelProcessors(my);
    }
  }

  public void initModelProcessors(int type, ClusSchema schema) throws IOException, ClusException {
    ClusModelInfo allmi = getAllModelsMI();
    allmi.initAllModelProcessors(type, schema);
    for (int i = 0; i < getNbModels(); i++) {
      ClusModelInfo mi = getModelInfo(i);
      if (mi != null) mi.initModelProcessors(type, schema);
    }
  }

  public void initEnsemblePredictionsWriter(int type) {
    ClusModelInfo mi =
        getModelInfo(ClusModel.ORIGINAL); // for ensembles we consider only the original at thsi
    // point
    mi.initEnsemblePredictionWriter(type);
  }

  public void termModelProcessors(int type) throws IOException, ClusException {
    ClusModelInfo allmi = getAllModelsMI();
    allmi.termAllModelProcessors(type);
    for (int i = 0; i < getNbModels(); i++) {
      ClusModelInfo mi = getModelInfo(i);
      if (mi != null) mi.termModelProcessors(type);
    }
  }

  public void termEnsemblePredictionsWriter(int type) {
    ClusModelInfo mi =
        getModelInfo(ClusModel.ORIGINAL); // for ensembles we consider only the original at thsi
    // point
    mi.terminateEnsemblePredictionWriter(type);
  }

  /**
   * ************************************************************************* Induction time
   * *************************************************************************
   */
  public final void setInductionTime(long time) {
    m_IndTime = time;
  }

  public final long getInductionTime() {
    return m_IndTime;
  }

  public final void setInductionTimeSequential(long time) {
    m_IndTimeSequential = time;
  }

  public final long getInductionTimeSequential() {
    return m_IndTimeSequential;
  }

  public final void setPruneTime(long time) {
    m_PruneTime = time;
  }

  public final long getPruneTime() {
    return m_PruneTime;
  }

  public final void setPrepareTime(long time) {
    m_PrepTime = time;
  }

  public final long getPrepareTime() {
    return m_PrepTime;
  }

  public final void addToPredictionTime(long time, int nbExamples) {
    m_PredictionTime += time;
    m_PredictionTimeNbExamples += nbExamples;
  }

  public final long getPredictionTime() {
    return m_PredictionTime;
  }

  public final long getPredictionTimeNbExamples() {
    return m_PredictionTimeNbExamples;
  }

  public final long getPredictionTimeAverage() {
    if (m_PredictionTimeNbExamples == 0) return 0;
 
    return m_PredictionTime / m_PredictionTimeNbExamples;
  }
}

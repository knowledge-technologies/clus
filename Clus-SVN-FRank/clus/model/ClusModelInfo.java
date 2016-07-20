/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package clus.model;

import clus.error.*;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.processor.*;
import clus.data.type.*;
import clus.util.*;

import java.io.*;

public class ClusModelInfo implements Serializable {

	public final static long serialVersionUID = 1L;

	public final static int TRAIN_ERR = 0;
	public final static int TEST_ERR = 1;
	public final static int VALID_ERR = 2;
	public final static int XVAL_PREDS = 3;
	
	protected String m_Name;
	protected boolean m_HasName = false;
	protected boolean m_ShouldSave = true;
	protected boolean m_ShouldWritePredictions = true;
	protected boolean m_ShouldPruneInvalid;
	protected int m_ModelSize, m_NbModels;
	protected double m_Score;
	protected ClusModel m_Model;
	public ClusErrorList m_TrainErr, m_TestErr, m_ValidErr, m_ExtraErr;
	protected ClusStatManager m_Manager;
	protected transient ModelProcessorCollection m_TrainModelProc, m_TestModelProc, m_ValidModelProc;
	protected transient ClusEnsemblePredictionWriter m_TrainPreds, m_TestPreds;//, m_XVALPreds;
	
	public ClusModelInfo(String name) {
		m_Name = name;
		m_HasName = false;
	}

	public void setAllErrorsClone(ClusErrorList train, ClusErrorList test, ClusErrorList valid) {
		m_TrainErr = null; m_TestErr = null; m_ValidErr = null;
		if (train != null) m_TrainErr = train.getErrorClone();
		if (test != null) m_TestErr = test.getErrorClone();
		if (valid != null) m_ValidErr = valid.getErrorClone();
	}

	public void setSelectedErrorsClone(ClusErrorList train, ClusErrorList test, ClusErrorList valid) {
		m_TrainErr = null; m_TestErr = null; m_ValidErr = null;
		if (train != null) m_TrainErr = train.getErrorClone(getName());
		if (test != null) m_TestErr = test.getErrorClone(getName());
		if (valid != null) m_ValidErr = valid.getErrorClone(getName());
	}

	public final String getName() {
		return m_Name;
	}

	public final ClusModel getModel() {
		return m_Model;
	}

	public final double getScore() {
		return m_Score;
	}

	public ClusStatManager getStatManager() {
		return m_Manager;
	}

	public ClusSchema getSchema() {
		return m_Manager.getSchema();
	}

	public Settings getSettings() {
		return m_Manager.getSettings();
	}

	public final ClusErrorList getTrainingError() {
		return m_TrainErr;
	}

	public final ClusErrorList getTestError() {
		return m_TestErr;
	}

	public final ClusErrorList getValidationError() {
		return m_ValidErr;
	}

	public void setStatManager(ClusStatManager mgr) {
		m_Manager = mgr;
	}

	public final void setScore(double score) {
		m_Score = score;
	}

	public void check() {
		System.out.println("MI = "+m_TestErr);
		System.exit(1);
	}

	public void clearAll() {
		m_TrainModelProc = null;
		m_TestModelProc = null;
	}

	public final void addModelProcessor(int type, ClusModelProcessor proc) {
		ModelProcessorCollection coll = getAddModelProcessors(type);
		coll.addModelProcessor(proc);
	}

	public final void addEnsemblePredictionWriter(int type,ClusEnsemblePredictionWriter wrtr){
		if (type == ClusModelInfo.TEST_ERR)m_TestPreds = wrtr;
		if (type == ClusModelInfo.TRAIN_ERR)m_TrainPreds = wrtr;
	}
	
	public final void addCheckModelProcessor(int type, ClusModelProcessor proc) {
		ModelProcessorCollection coll = getAddModelProcessors(type);
		if (coll.addCheckModelProcessor(proc)) proc.addModelInfo(this);
	}

	public final ModelProcessorCollection getAddModelProcessors(int type) {
		if (type == TRAIN_ERR) {
			if (m_TrainModelProc == null) m_TrainModelProc = new ModelProcessorCollection();
			return m_TrainModelProc;
		} else if (type == TEST_ERR) {
			if (m_TestModelProc == null) m_TestModelProc = new ModelProcessorCollection();
			return m_TestModelProc;
		} else {
			if (m_ValidModelProc == null) m_ValidModelProc = new ModelProcessorCollection();
			return m_ValidModelProc;
		}
	}

	public final ModelProcessorCollection getModelProcessors(int type) {
		if (type == TRAIN_ERR) {
			return m_TrainModelProc;
		} else if (type == TEST_ERR) {
			return m_TestModelProc;
		} else {
			return m_ValidModelProc;
		}
	}

	public final ClusEnsemblePredictionWriter getEnsemblePredictionWriter(int type){
		if (type == TEST_ERR) return m_TestPreds;
		if (type == TRAIN_ERR) return m_TrainPreds;
		return null;
	}
	
	public final void initEnsemblePredictionWriter(int type){
		String fname = "";
		if (type == ClusModelInfo.TEST_ERR && m_TestPreds == null) {
			fname = getSettings().getAppName()+".ens.test.preds";
			m_TestPreds = new ClusEnsemblePredictionWriter(fname, getSchema(), getSettings());
		}
		if (type == ClusModelInfo.TRAIN_ERR && m_TrainPreds == null) {
			fname = getSettings().getAppName()+".ens.train.preds";
			m_TrainPreds = new ClusEnsemblePredictionWriter(fname, getSchema(), getSettings());
		}
	}
	
	public final void initModelProcessors(int type, ClusSchema schema) throws IOException, ClusException {
		ModelProcessorCollection coll = getModelProcessors(type);
		if (coll != null) coll.initialize(m_Model, schema);
	}

	public final void initAllModelProcessors(int type, ClusSchema schema) throws IOException, ClusException {
		ModelProcessorCollection coll = getModelProcessors(type);
		if (coll != null) coll.initializeAll(schema);
	}

	public final void termModelProcessors(int type) throws IOException {
		ModelProcessorCollection coll = getModelProcessors(type);
		if (coll != null) coll.terminate(m_Model);
	}

	public final void termAllModelProcessors(int type) throws IOException {
		ModelProcessorCollection coll = getModelProcessors(type);
		if (coll != null) coll.terminateAll();
	}
	
	public final void terminateEnsemblePredictionWriter(int type){
		if (type == TEST_ERR) m_TestPreds.closeWriter();	
		if (type == TRAIN_ERR) m_TrainPreds.closeWriter();
	}

	public final void copyModelProcessors(ClusModelInfo target) {
		copyModelProcessors(TRAIN_ERR, target);
		copyModelProcessors(TEST_ERR, target);
	}

	public final void copyModelProcessors(int type, ClusModelInfo target) {
		ModelProcessorCollection coll = getModelProcessors(type);
		if (coll == null) return;
		for (int i = 0; i < coll.size(); i++) {
			ClusModelProcessor mproc = coll.getModelProcessor(i);
			if (mproc.shouldProcessModel(target)) {
				target.addCheckModelProcessor(type, mproc);
			}
		}
	}

	public final ClusModelInfo cloneModelInfo() {
		ClusModelInfo clone = new ClusModelInfo(m_Name);
		clone.setAllErrorsClone(m_TrainErr, m_TestErr, m_ValidErr);
		clone.setShouldSave(m_ShouldSave);
		clone.setShouldWritePredictions(m_ShouldWritePredictions);
		clone.setPruneInvalid(m_ShouldPruneInvalid);
		return clone;
	}

	public final void setModel(ClusModel model) {
		m_Model = model;
	}

	public final void deleteModel() {
		m_Model = null;
	}

	public final void setTestError(ClusErrorList err) {
		m_TestErr = err;
	}

	public final void setTrainError(ClusErrorList err) {
		m_TrainErr = err;
	}

	public final void setValidationError(ClusErrorList err) {
		m_ValidErr = err;
	}

	public void setExtraError(int type, ClusErrorList parent) {
		m_ExtraErr = parent;
	}

	public boolean hasExtraError(int type) {
		return m_ExtraErr != null;
	}

	public ClusErrorList getExtraError(int type) {
		return m_ExtraErr;
	}

	public final void setName(String name) {
		m_Name = name;
		m_HasName = true;
	}

	public final boolean hasName() {
		return m_HasName;
	}

	public final void setShouldSave(boolean save) {
		m_ShouldSave = save;
	}

	public final boolean shouldSave() {
		return m_ShouldSave;
	}

	public boolean shouldWritePredictions() {
		return m_ShouldWritePredictions;
	}

	public void setShouldWritePredictions(boolean value) {
		m_ShouldWritePredictions = value;
	}

	public final void setPruneInvalid(boolean prune) {
		m_ShouldPruneInvalid = prune;
	}

	public final boolean shouldPruneInvalid() {
		return m_ShouldPruneInvalid;
	}

	public final ClusErrorList getError(int traintest) {
		if (traintest == TRAIN_ERR) return m_TrainErr;
		else if (traintest == VALID_ERR) return m_ValidErr;
		else if (traintest == TEST_ERR) return m_TestErr;
		else return null;
	}

	public final ClusErrorList getCreateTestError() {
		if (m_TestErr == null) m_TestErr = m_TrainErr.getErrorClone();
		return m_TestErr;
	}

	public final boolean hasTestError() {
		return m_TestErr != null;
	}

	public final boolean hasTrainError() {
		return m_TrainErr != null;
	}

	public final boolean hasValidError() {
		return m_ValidErr != null;
	}

	public final String getModelInfo() {
		if (m_Model == null) return "No model available";
		else return m_Model.getModelInfo();
	}

	public final int getModelSize() {
		if (m_Model == null) return m_ModelSize;
		else return m_Model.getModelSize();
	}

	public final int getNbModels() {
		if (m_Model == null) return m_NbModels;
		else return 1;
	}

	public final boolean hasModel() {
		return getNbModels() > 0;
	}

	public final void updateName(ClusModelInfo other) throws ClusException {
		if (hasName()) {
			if (other.hasName() && !getName().equals(other.getName())) {
				throw new ClusException("Combining error measures of different models: "+getName()+" <> "+other.getName());
			}
		} else {
			if (other.hasName()) setName(other.getName());
		}
	}

	public final void add(ClusModelInfo other) throws ClusException {
		updateName(other);
		m_ModelSize += other.getModelSize();
		m_NbModels += other.getNbModels();
		if (other.hasTrainError()) {
			m_TrainErr.add(other.getTrainingError());
		}
		if (other.hasValidError()) {
			m_ValidErr.add(other.getValidationError());
		}
		if (other.hasTestError()) {
			ClusErrorList mytesterr = getCreateTestError();
			mytesterr.add(other.getTestError());
		}
	}

	public String toString() {
		return "ModelInfo '"+getName()+"' Size: "+getModelSize();
	}
}

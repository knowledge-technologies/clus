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

package clus.ext.ensembles;

import java.io.IOException;

import jeans.util.cmdline.CMDLineArgs;

import clus.main.*;
import clus.model.ClusModel;
import clus.Clus;
import clus.algo.*;
import clus.data.type.*;
import clus.util.ClusException;

public class ClusEnsembleClassifier extends ClusInductionAlgorithmType {

	public ClusEnsembleClassifier(Clus clus) {
		super(clus);
		// TODO Auto-generated constructor stub
	}

	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
		if (sett.getEnsembleMethod() == Settings.ENSEMBLE_BOOSTING) {
			return new ClusBoostingInduce(schema, sett);
		} else {
			return new ClusEnsembleInduce(schema, sett, m_Clus);
		}
	}

	public ClusModel pruneSingle(ClusModel model, ClusRun cr)
			throws ClusException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void pruneAll(ClusRun cr) throws ClusException, IOException {
		// TODO Auto-generated method stub
	}

	public void printInfo() {
		System.out.println("Ensemble Classifier");
	}

}

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

/*
 * Created on May 1, 2005
 */
package clus.algo.rules;

import java.io.*;

import jeans.io.ini.INIFileNominalOrDoubleOrVector;
import jeans.util.cmdline.CMDLineArgs;
import clus.*;
import clus.data.type.*;
import clus.algo.*;
import clus.algo.tdidt.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.util.ClusException;

public class ClusRuleClassifier extends ClusInductionAlgorithmType {

	public ClusRuleClassifier(Clus clus) {
		super(clus);
	}

	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
		
		// Compute the normalization information here if needed. We can here use the whole data set
		// instead of only the training set part. This is how trees are using it also.
		// Both default rule creation and rule omitting need the information also 
		if (sett.isRulePredictionOptimized()) {
			NumericAttrType[] descrNumTypes =  schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
			NumericAttrType[] tarNumTypes = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
			RuleNormalization.initialize(Clus.calcStdDevsForTheSet(getClus().getData(), descrNumTypes),
					Clus.calcStdDevsForTheSet(getClus().getData(), tarNumTypes));	
		}
		
		ClusInductionAlgorithm induce;
		if (sett.getCoveringMethod() == Settings.COVERING_METHOD_RULES_FROM_TREE) {
			induce = (ClusInductionAlgorithm) new ClusRuleFromTreeInduce(schema, sett, getClus());
		} else {
			if(sett.isSectionILevelCEnabled()){
				induce = (ClusInductionAlgorithm) new ClusRuleConstraintInduce(schema, sett);
			} else {
				induce = (ClusInductionAlgorithm) new ClusRuleInduce(schema, sett);
			}
			induce.getStatManager().setRuleInduceOnly(true); // Tells that the rule is the way to go
		}
		induce.getStatManager().initRuleSettings();
		return induce;
	}

	public void printInfo() {
		if (!getSettings().isRandomRules()) {
			System.out.println("RuleSystem based on CN2");
			System.out.println("Heuristic: "+getStatManager().getHeuristicName());
		} else {
			System.out.println("RuleSystem generating random rules");
		}
	}

	public void pruneAll(ClusRun cr) throws ClusException, IOException {
	}

	public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
		return model;
	}

	public void postProcess(ClusRun cr) throws ClusException, IOException {
		// For RulesFromTree the default is already an ensemble.
		if (getSettings().getCoveringMethod() != Settings.COVERING_METHOD_RULES_FROM_TREE) {
			ClusModelInfo def_model = cr.addModelInfo(ClusModel.DEFAULT);
			def_model.setModel(ClusDecisionTree.induceDefault(cr));
		}

	}
}

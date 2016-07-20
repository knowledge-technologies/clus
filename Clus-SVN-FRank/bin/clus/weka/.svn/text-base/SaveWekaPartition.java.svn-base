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

package clus.weka;

import java.util.*;
import java.io.*;
import weka.core.*;
import weka.classifiers.*;

public class SaveWekaPartition extends Evaluation {

	public SaveWekaPartition(Instances data, CostMatrix costMatrix) throws Exception {
		super(data, costMatrix);
	}

	public void saveXVAL(Instances train, int numFolds, Random rnd, String fname) throws Exception {
		Instances data = new Instances(train);
		data.randomize(rnd);
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".folds")));
		for (int i = 0; i < numFolds; i++) {
			System.out.println("Fold: "+i);
			Instances test_cv = data.testCV(numFolds, i);
			for(int j = 0; j<test_cv.numInstances(); j++) {
				Instance in = test_cv.instance(j);
				// System.out.println(i + ":" + in.toString());
				if (j != 0) wrt.print(",");
				wrt.print(in.toString(0));
			}
			wrt.println();
		}
		wrt.close();
	}

	public static void savePartition(String[] options) throws Exception {
		Instances train = null, template = null;
		int seed = 1, folds = 10, classIndex = -1;
		String trainFileName, classIndexString, seedString, foldsString;
		BufferedReader trainReader = null;
		CostMatrix costMatrix = null;
		// Get basic options (options the same for all schemes)
		classIndexString = Utils.getOption('c', options);
		if (classIndexString.length() != 0) {
			classIndex = Integer.parseInt(classIndexString);
		}
		trainFileName = Utils.getOption('t', options);
		if (trainFileName.length() == 0) {
			throw new Exception("No training file and no object input file given.");
		}
		trainReader = new BufferedReader(new FileReader(trainFileName));
		template = train = new Instances(trainReader);
		if (classIndex != -1) {
			if (classIndex > train.numAttributes()) {
				throw new Exception("Index of class attribute too large.");
			}
			train.setClassIndex(classIndex - 1);
		} else {
			train.setClassIndex(train.numAttributes() - 1);
		}
		seedString = Utils.getOption('s', options);
		if (seedString.length() != 0) {
			seed = Integer.parseInt(seedString);
		}
		foldsString = Utils.getOption('x', options);
		if (foldsString.length() != 0) {
			folds = Integer.parseInt(foldsString);
		}
		costMatrix = handleCostOption(Utils.getOption('m', options), template.numClasses());
		Utils.checkForRemainingOptions(options);
		SaveWekaPartition eval = new SaveWekaPartition(new Instances(template, 0), costMatrix);
		Random rnd = new Random(seed);
 		eval.saveXVAL(train, folds, rnd, trainFileName+"."+seed);
	}

	public static void main(String[] args) {
		try {
			savePartition(args);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}

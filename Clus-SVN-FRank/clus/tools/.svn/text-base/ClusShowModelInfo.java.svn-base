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
 * Created on Jun 8, 2005
 */
package clus.tools;

import clus.model.*;
import clus.model.modelio.*;
import clus.error.*;

import java.io.*;

public class ClusShowModelInfo {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java clus.tools.ClusShowModelInfo somefile.model");
			System.exit(1);
		}
		try {
			PrintWriter output = new PrintWriter(System.out);
			ClusModelCollectionIO dot_model_file = ClusModelCollectionIO.load(args[0]);
			int nb_models = dot_model_file.getNbModels();
			output.println("This .model file contains: "+nb_models+" models.");
//			for (int i = 0; i < nb_models; i++) {
			for (int i = 0; i < 1; i++) {
				ClusModelInfo model_info = dot_model_file.getModelInfo(i);
				ClusModel model = model_info.getModel();
				output.println("Model: "+i+", Name: "+model_info.getName());
				output.println("Size: "+model.getModelSize());
				output.println();
				model.printModel(output);
				ClusErrorList train_error = model_info.getTrainingError();
				output.println();
				if (train_error != null) {
					output.println("Training Error:");
					train_error.showError(output);
				} else {
					output.println("No Training Error Available");
				}
				ClusErrorList test_error = model_info.getTestError();
				output.println();
				if (test_error != null) {
					output.println("Testing Error:");
					test_error.showError(output);
					/* One can also get an individual error, or iterate over all error measures */
					ClusError err = test_error.getErrorByName("Classification Error");
					if (err != null) {
						for (int j = 0; j < err.getDimension(); j++) {
							output.println("Target: "+j+" error: "+err.getModelErrorComponent(j));
						}
					}
				} else {
					output.println("No Testing Error Available");
				}
			}
			output.flush();
		} catch (IOException e) {
			System.err.println("IO Error: "+e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Error: "+e.getMessage());
		}
	}
}

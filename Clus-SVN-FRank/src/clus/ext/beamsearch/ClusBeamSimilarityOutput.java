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

package clus.ext.beamsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import clus.data.rows.RowData;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.util.ClusException;
import clus.util.ClusFormat;

public class ClusBeamSimilarityOutput {

	static boolean m_WriteHeader;//is header written?
	static ArrayList m_BeamSimTrain;//the train similarities
	static ArrayList m_BeamSimTest;//the test similarities

	public ClusBeamSimilarityOutput(Settings sett) throws IOException{
		if (!m_WriteHeader) {
			writeHeader(sett);
			m_WriteHeader = true;//the header is written
			m_BeamSimTest = new ArrayList();
			m_BeamSimTrain = new ArrayList();
		}
	}

	public boolean isHeaderWritten(){
		return m_WriteHeader;
	}

	public void appendToFile(ArrayList models, ClusRun run) throws IOException, ClusException{
//		String str = run.getStatManager().getSettings().getFileAbsolute(run.getStatManager().getSettings().getAppName())+".bsim";
//		File output = new File(fname);
		FileWriter wrtr = new FileWriter(new File(run.getStatManager().getSettings().getAppName() + ".bsim"), true);
		double[]sim = new double [2];
		//sim[0] - training
		//sim[1] - testing
		NumberFormat outF = ClusFormat.FOUR_AFTER_DOT;
		if ((run.getStatManager().getMode() != 1)&&(run.getStatManager().getMode() != 0)){
			System.err.println(getClass().getName()+".appendToFile(): Unhandled Type of Target Attribute");
			throw new ClusException("Unhandled Type of Target Attribute");
		}
		boolean isNum = (run.getStatManager().getMode()==1);
		sim[0] = ClusBeamModelDistance.calcBeamSimilarity(models, (RowData)run.getTrainingSet(), isNum);
		m_BeamSimTrain.add(Double.valueOf(sim[0]));
		try {
		sim[1] = ClusBeamModelDistance.calcBeamSimilarity(models, run.getTestSet(), isNum);
		m_BeamSimTest.add(Double.valueOf(sim[1]));
			if (Settings.IS_XVAL){
				wrtr.write("Fold "+run.getIndexString()+":\t"+outF.format(sim[0])+"\t\t"+outF.format(sim[1])+"\n");
				if (run.getIndex() == run.getStatManager().getSettings().getXValFolds()){
					//we reached the last fold, so we write a summary
					wrtr.write("---------------------------------------\n");
					wrtr.write("Summary:\t"+outF.format(getAverage(m_BeamSimTrain))+"\t\t"+outF.format(getAverage(m_BeamSimTest))+"\n");
				}
			}else wrtr.append("\t\t"+outF.format(sim[0])+"\t\t"+outF.format(sim[1])+"\n");
		}catch(NullPointerException e){
			if (!Settings.IS_XVAL) wrtr.append("Summary:\t"+outF.format(sim[0])+"\t\t"+"N/A"+"\n");
		}
		wrtr.flush();
	}

	public void writeHeader(Settings sett) throws IOException{
		File output = new File(sett.getAppName() + ".bsim");
		FileWriter wrtr = new FileWriter(output);
		wrtr.write("Clus Beam-Search run\n");
		wrtr.write("----------------------\n");
		wrtr.write("Date:\t"+DateFormat.getInstance().format(sett.getDate())+"\n");
		wrtr.write("File:\t"+output+"\n");
		wrtr.write("\n");
		wrtr.write("Beam Similarity Output\n");
		wrtr.write("----------------------\n");
		wrtr.write("\t\tTraining\tTesting\n");
		wrtr.write("---------------------------------------\n");
		wrtr.flush();
	}

	public static double getAverage(ArrayList arr){
		double result = 0.0;
		for (int i = 0; i < arr.size(); i++)
			result += ((Double)arr.get(i)).doubleValue();
		return result / arr.size();
	}

}

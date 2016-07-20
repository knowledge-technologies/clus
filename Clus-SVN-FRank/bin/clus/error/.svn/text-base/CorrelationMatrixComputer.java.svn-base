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
 * Created on May 10, 2005
 *
 * 27.02.2007 Added the 'nominal correlation', i.e., Cramer V coefficient
 * or mutual information for use within the -corrmatrix option only
 * (bernard.zenko@ijs.si)
 */
package clus.error;

import clus.data.rows.*;
import clus.data.type.*;
import clus.util.*;

public class CorrelationMatrixComputer {

	PearsonCorrelation[][] m_MatrixPC;
	NominalCorrelation[][] m_MatrixNC;
	boolean m_IsRegression = true;

	public void compute(RowData data) {
		if (data.getSchema().isRegression()) {
      computeNum(data);
		} else {
			m_IsRegression = false;
      computeNom(data);
		}
	}

	public void computeNum(RowData data) {
		ClusSchema schema = data.getSchema();
		NumericAttrType[] attrs = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		int nb_num = attrs.length;
		m_MatrixPC = new PearsonCorrelation[nb_num][nb_num];
		NumericAttrType[] crtype = new NumericAttrType[1];
		crtype[0] = new NumericAttrType("corr");
		ClusErrorList par = new ClusErrorList();
		for (int i = 0; i < nb_num; i++) {
			for (int j = 0; j < nb_num; j++) {
				m_MatrixPC[i][j] = new PearsonCorrelation(par, crtype);
			}
		}
		double[] a1 = new double[1];
		double[] a2 = new double[1];
		par.setNbExamples(data.getNbRows());
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			for (int j = 0; j < nb_num; j++) {
				for (int k = 0; k < nb_num; k++) {
					a1[0] = attrs[j].getNumeric(tuple);
					a2[0] = attrs[k].getNumeric(tuple);
					m_MatrixPC[j][k].addExample(a1, a2);
				}
			}
		}
	}

	public void computeNom(RowData data) {
		ClusSchema schema = data.getSchema();
		NominalAttrType[] attrs = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		int nb_nom = attrs.length;
		m_MatrixNC = new NominalCorrelation[nb_nom][nb_nom];
		// NominalAttrType[] crtype = new NominalAttrType[1];
		// crtype[0] = new NominalAttrType("corr");
		ClusErrorList par = new ClusErrorList();
		for (int i = 0; i < nb_nom; i++) {
			for (int j = 0; j < nb_nom; j++) {
				// m_MatrixCV[i][j] = new CramerV(par, crtype, i, j);
				m_MatrixNC[i][j] = new NominalCorrelation(par, attrs, i, j);
			}
		}
		int a1;
		int a2;
		par.setNbExamples(data.getNbRows());
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			for (int j = 0; j < nb_nom; j++) {
				for (int k = 0; k < nb_nom; k++) {
					a1 = attrs[j].getNominal(tuple);
					a2 = attrs[k].getNominal(tuple);
					m_MatrixNC[j][k].addExample(a1, a2);
				}
			}
		}
	}

	public void printMatrixTeX() {
		int nb_tar;
		if (m_IsRegression) {
		  nb_tar = m_MatrixPC.length;
			System.out.println("Number of numeric: "+nb_tar);
		} else {
			nb_tar = m_MatrixNC.length;
			System.out.println("Number of nominal: "+nb_tar);
		}
		System.out.println();
		System.out.print("\\begin{tabular}{");
		for (int i = 0; i < nb_tar+2; i++) {
			System.out.print("l");
		}
		System.out.println("}");
		for (int i = 0; i < nb_tar; i++) {
			System.out.print(" & "+(i+1));
		}
		System.out.println("& Avg.");
		System.out.println("\\\\");
		int nb_pairs = 0;
		double pairs_sum = 0;
		for (int i = 0; i < nb_tar; i++) {
			System.out.print(i+1);
			double avg = 0;
			double cnt = 0;
			for (int j = 0; j < nb_tar; j++) {
				double corr;
				if (m_IsRegression) {
					corr = m_MatrixPC[i][j].getCorrelation(0);
				} else {
					// Cramer V coefficient or Mutual information
					// corr = m_MatrixNC[i][j].calcCramerV();
					corr = m_MatrixNC[i][j].calcMutualInfo();
				}
				if (i != j) {
					avg += corr;
					cnt ++;
				}
				if (i > j) {
					pairs_sum += corr;
					nb_pairs ++;
				}
				System.out.print(" & "+ClusFormat.THREE_AFTER_DOT.format(corr));
			}
			System.out.print(" & "+ClusFormat.THREE_AFTER_DOT.format(avg/cnt));
			System.out.println("\\\\");
		}
		System.out.print("\\multicolumn{" + (nb_tar+2) + "}{l}{Pairwise average:");
		if (nb_pairs > 0) {
		  System.out.println(" " + ClusFormat.THREE_AFTER_DOT.format(pairs_sum/nb_pairs) + "}");
		} else {
			System.out.println(" Undefined}");
		}
		System.out.println("\\end{tabular}");
	}

}

/*
 * Created on Feb 27, 2007
 */

package clus.error;

import clus.data.type.*;
import clus.main.*;

public class NominalCorrelation extends ClusNominalError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int[][] m_ContTable;

	public NominalCorrelation(ClusErrorList par, NominalAttrType[] nom, int ind1, int ind2) {
		super(par, nom);
		int size1 = m_Attrs[ind1].getNbValues();
		int size2 = m_Attrs[ind2].getNbValues();
		m_ContTable = new int[size1][size2];
	}

	public int calcNbCorrect(int[][] table) {
		int sum = 0;
		int size = table.length;
		for (int j = 0; j < size; j++) {
			sum += table[j][j];
		}
		return sum;
	}

	public double calcXSquare() {
		int size1 = m_ContTable.length;
		int size2 = m_ContTable[0].length;
		int n = getNbExamples();
		int[] ni = new int[size1];
		int[] nj = new int[size2];
		for (int i = 0; i < size1; i++) {
			ni[i] = sumJ(i);
		}
		for (int j = 0; j < size2; j++) {
			nj[j] = sumI(j);
		}
		double xsquare = 0.0;
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				double mij = (double)ni[i]*nj[j]/n;
				double err = (double)m_ContTable[i][j] - mij;
				if (mij != 0.0)	xsquare += err*err/mij;
			}
		}
		return xsquare;
	}

	public double calcCramerV() {
		int size1 = m_ContTable.length;
		int size2 = m_ContTable[0].length;
		int n = getNbExamples();
		double div = (double)n*Math.min(size1-1, size2-1);
		return Math.sqrt(calcXSquare()/div);
	}

	public double calcMutualInfo() {
		int size1 = m_ContTable.length;
		int size2 = m_ContTable[0].length;
		int n = getNbExamples();
		int[] ni = new int[size1];
		int[] nj = new int[size2];
		for (int i = 0; i < size1; i++) {
			ni[i] = sumJ(i);
		}
		for (int j = 0; j < size2; j++) {
			nj[j] = sumI(j);
		}
		double m_info = 0.0;
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				double pij = (double)m_ContTable[i][j] / n;
				double pi = (double)ni[i] / n;
				double pj = (double)nj[j] / n;
				double div = pi * pj;
				if (div != 0.0)	m_info += pij*Math.log(pij/div)/Math.log(2);
			}
		}
		return m_info;
	}

	public int sumI(int j) {
		int sum = 0;
		int size = m_ContTable.length;
		for (int i = 0; i < size; i++)
			sum += m_ContTable[i][j];
		return sum;
	}

	public int sumJ(int i) {
		int sum = 0;
		int size = m_ContTable[0].length;
		for (int j = 0; j < size; j++)
			sum += m_ContTable[i][j];
		return sum;
	}

	public boolean hasSummary() {
		return false;
	}

	public void addExample(int ind1, int ind2) {
		m_ContTable[ind1][ind2]++;
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new NominalCorrelation(par, m_Attrs, 0, 0);
	}

	public String getName() {
		return "Cramer's V coefficient or Mutual information";
	}

}

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

package clus.error.multiscore;

import java.util.*;

import clus.main.*;
import clus.util.*;
import clus.data.type.*;

public class MultiScore {

	protected double[] m_Thresholds;
	protected int m_NbValues;

	public MultiScore(ClusSchema schema, Settings sett) throws ClusException {
		String val = "";/* = sett.getMultiScore(); */
		int len = val.length();
		int nb_wanted = 0; // schema.getNbTarNum();
		try {
			if (len > 2 && val.charAt(0) == '{' && val.charAt(len-1) == '}') {
				StringTokenizer tokens = new StringTokenizer(val.substring(1,len-1), ", ");
				m_NbValues = tokens.countTokens();
				if (m_NbValues != nb_wanted)
					throw new ClusException("Not enough ("+m_NbValues+" < "+nb_wanted+") thresholds given for multi-score");
				m_Thresholds = new double[m_NbValues];
				for (int i = 0; i < m_NbValues; i++) m_Thresholds[i] = Double.parseDouble(tokens.nextToken());
			} else {
				double thr = Double.parseDouble(val);
				m_Thresholds = new double[m_NbValues = nb_wanted];
				for (int i = 0; i < m_NbValues; i++) m_Thresholds[i] = thr;
			}
		} catch (NumberFormatException e) {
			throw new ClusException("Parse error reading multi-score values");
		}
	}

	public int getNbTarget() {
		return m_NbValues;
	}

	// Class index 0 = positive, 1 = negative (!)
	public int[] multiScore(double[] input) {
		int[] res = new int[input.length];
		for (int i = 0; i < m_NbValues; i++)
			res[i] = (input[i] > m_Thresholds[i]) ? 0 : 1;
		return res;
	}

	// Class index 0 = positive, 1 = negative (!)
	public void multiScore(double[] input, int[] res) {
		for (int i = 0; i < m_NbValues; i++)
			res[i] = (input[i] > m_Thresholds[i]) ? 0 : 1;
	}
}

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
 * Created on Jun 17, 2005
 *
 */
package clus.data.attweights;

import java.util.Arrays;

import clus.data.type.*;
import clus.main.Settings;
import clus.util.ClusFormat;

public class ClusNormalizedAttributeWeights extends ClusAttributeWeights {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double[] m_NormalizationWeights;

	public ClusNormalizedAttributeWeights(ClusAttributeWeights norm) {
		super(norm.getNbAttributes());
		m_NormalizationWeights = norm.getWeights();
	}

	public double getWeight(ClusAttrType atttype) {
		int idx = atttype.getIndex();
		return m_Weights[idx] * m_NormalizationWeights[idx];
	}

	public double getWeight(int idx) {
		return m_Weights[idx] * m_NormalizationWeights[idx];
	}

	public double getComposeWeight(ClusAttrType atttype) {
		return m_Weights[atttype.getIndex()];
	}

	public double getNormalizationWeight(ClusAttrType atttype) {
		return m_NormalizationWeights[atttype.getIndex()];
	}

	public double[] getNormalizationWeights() {
		return m_NormalizationWeights;
	}

	public void setAllNormalizationWeights(double value) {
		Arrays.fill(m_NormalizationWeights, value);
	}

	public String getName(ClusAttrType[] type) {
		if (type.length > 50) {
			return "Weights ("+type.length+")";
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append("Weights C=[");
			for (int i = 0; i < type.length; i++) {
				if (i != 0) buf.append(",");
				buf.append(ClusFormat.THREE_AFTER_DOT.format(getComposeWeight(type[i])));
			}
			buf.append("], N=[");
			for (int i = 0; i < type.length; i++) {
				if (i != 0) buf.append(",");
				buf.append(ClusFormat.THREE_AFTER_DOT.format(getNormalizationWeight(type[i])));
			}
			buf.append("]");
			return buf.toString();
		}
	}
}

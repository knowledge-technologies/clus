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

import java.util.*;
import java.io.*;

import clus.data.type.*;
import clus.main.Settings;
import clus.util.*;

public class ClusAttributeWeights implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public double[] m_Weights;

	public ClusAttributeWeights(int nbAttr) {
		m_Weights = new double[nbAttr];
	}

	public double getWeight(ClusAttrType atttype) {
		return m_Weights[atttype.getIndex()];
	}

	public double getWeight(int i) {
		return m_Weights[i];
	}

	public void setWeight(ClusAttrType atttype, double weight) {
		m_Weights[atttype.getIndex()] = weight;
	}

	public void setWeight(int attidx, double weight) {
		m_Weights[attidx] = weight;
	}

	public void setAllWeights(double value) {
		Arrays.fill(m_Weights, value);
	}

	public int getNbAttributes() {
		return m_Weights.length;
	}

	public double[] getWeights() {
		return m_Weights;
	}

	public void copyFrom(ClusAttributeWeights other) {
		System.arraycopy(other.getWeights(), 0, this.getWeights(), 0, getNbAttributes());
	}

	public String getName() {
		if (getNbAttributes() > 10) {
			return "Weights ("+getNbAttributes()+")";
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append("Weights [");
			for (int i = 0; i < getNbAttributes(); i++) {
				if (i != 0) buf.append(",");
				buf.append(ClusFormat.THREE_AFTER_DOT.format(getWeight(i)));
			}
			buf.append("]");
			return buf.toString();
		}
	}

	public String getName(ClusAttrType[] type) {
		if (type == null) {
			return getName();
		}
		if (type.length > 10) {
			return "Weights ("+type.length+")";
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append("Weights [");
			for (int i = 0; i < type.length; i++) {
				if (i != 0) buf.append(",");
				buf.append(ClusFormat.THREE_AFTER_DOT.format(getWeight(type[i])));
			}
			buf.append("]");
			return buf.toString();
		}
	}
}

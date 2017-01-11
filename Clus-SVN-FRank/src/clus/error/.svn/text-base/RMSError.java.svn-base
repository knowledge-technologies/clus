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

package clus.error;

import java.io.*;
import java.text.*;

import clus.data.attweights.*;
import clus.data.type.NumericAttrType;
import clus.main.Settings;

public class RMSError extends MSError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public RMSError(ClusErrorList par, NumericAttrType[] num) {
		super(par, num);
	}

	public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
		super(par, num, weights);
	}

	public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
		super(par, num, weights, printall);
	}

	public double getModelError() {
		return Math.sqrt(super.getModelError());
	}

	public double getModelErrorComponent(int i) {
		return Math.sqrt(super.getModelErrorComponent(i));
	}

	public void showSummaryError(PrintWriter out, boolean detail) {
		NumberFormat fr = getFormat();
		out.println(getPrefix() + "Mean over components RMSE: "+fr.format(getModelError()));
	}

	public String getName() {
		if (m_Weights == null) return "Root mean squared error (RMSE)";
		else return "Weighted root mean squared error (RMSE) ("+m_Weights.getName(m_Attrs)+")";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new RMSError(par, m_Attrs, m_Weights, m_PrintAllComps);
	}
}

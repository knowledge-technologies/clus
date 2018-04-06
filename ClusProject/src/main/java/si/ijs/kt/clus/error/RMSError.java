/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.error;

import java.io.PrintWriter;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class RMSError extends MSError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public RMSError(ClusErrorList par, NumericAttrType[] num) {
        super(par, num);
    }


    public RMSError(ClusErrorList par, NumericAttrType[] num, String info) {
        super(par, num, info);
    }


    public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
        super(par, num, weights);
    }


    public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, String info) {
        super(par, num, weights, info);
    }


    public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
        super(par, num, weights, printall, "");
    }


    public RMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall, String info) {
        super(par, num, weights, printall, info);
    }


    @Override
    public double getModelError() {
        double sum = 0.0;
        for (int i = 0; i < m_Attrs.length; i++) {
            sum += getModelErrorComponent(i);
        }
        return sum / m_Attrs.length;
    }


    @Override
    public double getModelErrorComponent(int i) {
        return Math.sqrt(super.getModelErrorComponent(i));
    }


    @Override
    public void showSummaryError(PrintWriter out, boolean detail) {
    	ClusNumberFormat fr = getFormat();
        out.println(getPrefix() + "Mean over components RMSE: " + fr.format(getModelError()));
    }


    @Override
    public String getName() {
        if (m_Weights == null)
            return "Root mean squared error (RMSE)" + getAdditionalInfoFormatted();
        else
            return "Weighted root mean squared error (RMSE) (" + m_Weights.getName(m_Attrs) + ")" + getAdditionalInfoFormatted();
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new RMSError(par, m_Attrs, m_Weights, m_PrintAllComps, getAdditionalInfo());
    }
}

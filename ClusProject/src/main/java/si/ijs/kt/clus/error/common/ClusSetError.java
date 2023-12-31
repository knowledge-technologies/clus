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

package si.ijs.kt.clus.error.common;

import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.main.settings.Settings;


public abstract class ClusSetError extends ClusError {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    protected SetAttrType[] m_Attrs;


    public ClusSetError(ClusErrorList par, SetAttrType[] set) {
        super(par, set.length);
        m_Attrs = set;
    }


    public SetAttrType getAttr(int i) {
        return m_Attrs[i];
    }


    public ClusSetError(ClusErrorList par, int nbset) {
        super(par, nbset);
    }


    public abstract void addExample(double[] real, double[] predicted);
}

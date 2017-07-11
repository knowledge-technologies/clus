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

package clus.distance;

import clus.data.rows.DataTuple;
import clus.data.type.structured.SetAttrType;
import clus.ext.structuredDataTypes.Set;
import clus.ext.structuredDataTypes.SetStatistic;
import clus.main.settings.Settings;
import clus.statistic.ClusStatistic;


public abstract class SetDistance extends ClusStructuredDistance {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected SetAttrType m_Attr;


    public SetDistance(SetAttrType attr, ClusDistance childDistance) {
        this(childDistance);
        
        m_Attr = attr;
    }


    public SetDistance(ClusDistance childDistance) {
        m_ChildDistances = new ClusDistance[1];
        m_ChildDistances[0] = childDistance;
    }


    public abstract double calcDistance(Set t1, Set t2);


    public double calcDistance(DataTuple t1, DataTuple t2) {
        Set s1 = m_Attr.getSet(t1);
        Set s2 = m_Attr.getSet(t2);
        return calcDistance(s1, s2);
    }


    public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
        Set s1 = m_Attr.getSet(t1);
        SetStatistic stat = (SetStatistic) s2;
        return calcDistance(s1, stat.getRepresentativeMedoid());
    }
}

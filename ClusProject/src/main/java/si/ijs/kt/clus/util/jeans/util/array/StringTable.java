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

package si.ijs.kt.clus.util.jeans.util.array;

import java.util.Enumeration;
import java.util.Hashtable;

import si.ijs.kt.clus.util.ClusLogger;


public class StringTable {

    protected Hashtable table = new Hashtable();


    public String get(String strg) {
        String found = (String) table.get(strg);
        if (found == null) {
            table.put(strg, strg);
            found = strg;
        }
        return found;
    }


    public void print() {
        for (Enumeration e = table.elements(); e.hasMoreElements();) {
            String str = (String) e.nextElement();
            ClusLogger.info(str);
        }
    }

}

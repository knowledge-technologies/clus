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

package clus.jeans.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Vector;


public class MStringTable {

    protected static Vector m_Vector = new Vector();


    public static String get(int idx) {
        if (idx >= m_Vector.size())
            return null;
        return (String) m_Vector.elementAt(idx);
    }


    public static void load(String fname) throws IOException {
        String line = "";
        String store = "";
        boolean start = true;
        InputStream stream = MediaInterface.getInstance().openStream(fname);
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(stream));
        while (line != null) {
            line = reader.readLine();
            if (line != null && line.length() != 0) {
                if (line.charAt(0) == '*') {
                    m_Vector.addElement(store);
                    start = true;
                    store = "";
                }
                else {
                    if (!start)
                        store += '$';
                    store += line;
                    start = false;
                }
            }
        }
    }
}

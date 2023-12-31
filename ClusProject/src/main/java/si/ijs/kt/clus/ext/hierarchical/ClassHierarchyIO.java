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

package si.ijs.kt.clus.ext.hierarchical;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import si.ijs.kt.clus.data.type.hierarchies.ClassesAttrType;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.tree.CompleteTreeIterator;
import si.ijs.kt.clus.util.jeans.util.MStreamTokenizer;
import si.ijs.kt.clus.util.jeans.util.array.StringTable;


public class ClassHierarchyIO {

    protected StringTable m_Table = new StringTable();


    public ClassHierarchy loadHierarchy(String fname) throws ClusException, IOException {
        ClassHierarchy hier = new ClassHierarchy((ClassTerm) null);
        loadHierarchy(fname, hier);
        return hier;
    }


    public ClassHierarchy loadHierarchy(String fname, ClassesAttrType type) throws ClusException, IOException {
        ClassHierarchy hier = new ClassHierarchy(type);
        loadHierarchy(fname, hier);
        return hier;
    }


    public void loadHierarchy(String fname, ClassHierarchy hier) throws ClusException, IOException {
        MStreamTokenizer tokens = new MStreamTokenizer(fname);
        String token = tokens.getToken();
        while (token != null) {
            ClassesTuple tuple = new ClassesTuple(token, m_Table);
            tuple.addToHierarchy(hier);
            token = tokens.getToken();

        }
        tokens.close();
    }


    public void saveHierarchy(String fname, ClassHierarchy hier) throws IOException {
        PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
        CompleteTreeIterator iter = hier.getNoRootIter();
        while (iter.hasMoreNodes()) {
            ClassTerm node = (ClassTerm) iter.getNextNode();
            wrt.println(node.toString());
        }
        wrt.close();
    }


    public StringTable getStringTable() {
        return m_Table;
    }

}

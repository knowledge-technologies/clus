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

package si.ijs.kt.clus.algo.tdidt.processor;

import java.io.IOException;
import java.io.PrintWriter;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.processor.ClusModelProcessor;
import si.ijs.kt.clus.util.jeans.util.MyArray;


public class NodeIDWriter extends ClusModelProcessor {

    protected boolean m_Missing;
    protected String m_Fname;
    protected PrintWriter m_Writer;
    protected ClusSchema m_Schema;
    protected MyArray m_Attrs;
    protected boolean m_First;
    protected Settings m_Sett;


    public NodeIDWriter(String fname, boolean missing, Settings sett) {
        m_Fname = fname;
        m_Missing = missing;
        m_Sett = sett;
    }


    @Override
    public void initialize(ClusModel model, ClusSchema schema) throws IOException {
        m_Attrs = new MyArray();
        int nb = schema.getNbAttributes();
        for (int i = 0; i < nb; i++) {
            ClusAttrType at = schema.getAttrType(i);
            if (at.getStatus() == ClusAttrType.STATUS_KEY)
                m_Attrs.addElement(at);
        }
        if (m_Attrs.size() == 0) {
            for (int i = 0; i < nb; i++) {
                ClusAttrType at = schema.getAttrType(i);
                if (at.getStatus() == ClusAttrType.STATUS_TARGET)
                    m_Attrs.addElement(at);
            }
        }
        m_First = true;
        m_Writer = m_Sett.getGeneric().getFileAbsoluteWriter(m_Fname);
    }


    @Override
    public void terminate(ClusModel model) throws IOException {
        m_Writer.close();
    }


    @Override
    public boolean needsModelUpdate() {
        return true;
    }


    @Override
    public void modelUpdate(DataTuple tuple, ClusModel model) {
        ClusNode node = (ClusNode) model;
        if (m_First) {
            m_Writer.print("pred(");
            for (int j = 0; j < m_Attrs.size(); j++) {
                ClusAttrType at = (ClusAttrType) m_Attrs.elementAt(j);
                m_Writer.print(at.getString(tuple));
            }
            m_First = false;
        }
        m_Writer.print(",");
        if (m_Missing) {
            m_Writer.print("(" + tuple.getWeight() + "," + node.getID() + ")");
        }
        else {
            m_Writer.print(node.getID());
        }
    }


    @Override
    public void modelDone() {
        m_Writer.println(").");
        m_First = true;
    }
}

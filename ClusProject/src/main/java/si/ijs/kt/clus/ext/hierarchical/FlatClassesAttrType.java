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

import java.io.IOException;

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.io.ClusSerializable;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public class FlatClassesAttrType extends ClassesAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClassesAttrType m_Mimic;


    public FlatClassesAttrType(String name, ClassesAttrType mimic) {
        super(name);
        m_Mimic = mimic;
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }

    public class MySerializable extends ClusSerializable {

        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            ClassesTuple other = (ClassesTuple) tuple.getObjVal(m_Mimic.getArrayIndex());
            tuple.setObjectVal(other.toFlat(m_Table), getArrayIndex());
            return true;
        }
    }
}

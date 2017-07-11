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

package clus.io;

import java.io.IOException;

import clus.data.io.ClusReader;
import clus.data.rows.DataTuple;
import clus.data.schema.ClusSchema;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;


public abstract class ClusSerializable {

    public void term(ClusSchema schema) {
    }


    public boolean read(ClusReader data, DataTuple tuple) throws IOException {
        throw new IOException("Attribute does not support tuple wise reading");
    }
    
    public boolean calculateHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy) throws IOException {
        throw new IOException("Attribute does not support tuple wise reading");
    }

    public boolean readHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {
        throw new IOException("Attribute does not support tuple wise reading");
    }
}

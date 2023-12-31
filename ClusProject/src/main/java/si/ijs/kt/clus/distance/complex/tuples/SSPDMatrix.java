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

package si.ijs.kt.clus.distance.complex.tuples;

import java.io.IOException;

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.IntegerAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.math.matrix.MSymMatrix;


public class SSPDMatrix extends ClusDistance {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected MSymMatrix m_Matrix;
    protected IntegerAttrType m_Target;


    public SSPDMatrix(int size) {
        m_Matrix = new MSymMatrix(size);
    }


    public void setTarget(ClusAttrType[] target) throws ClusException {
        if (target.length != 1) { throw new ClusException("Only one target allowed in SSPD modus"); }
        m_Target = (IntegerAttrType) target[0];
    }


    // Matrix stores squared distances [sum of (i,j)^2 and (j,i)^2]
    public static SSPDMatrix read(String filename, Settings sett) throws IOException {
        ClusReader reader = new ClusReader(filename, sett);
        int nb = 0;
        while (!reader.isEol()) {
            reader.readFloat();
            nb++;
        }
        ClusLogger.info("Loading SSPD Matrix: " + filename + " (Size: " + nb + ")");
        SSPDMatrix matrix = new SSPDMatrix(nb);
        reader.reOpen();
        for (int i = 0; i < nb; i++) {
            for (int j = 0; j < nb; j++) {
                double value = reader.readFloat();
                matrix.m_Matrix.add_sym(i, j, value * value);
            }
            if (!reader.isEol())
                throw new IOException("SSPD Matrix is not square");
        }
        reader.close();
        // matrix.print(ClusFormat.OUT_WRITER, ClusFormat.TWO_AFTER_DOT, 5);
        // ClusFormat.OUT_WRITER.flush();
        return matrix;
    }


    @Override
    public double calcDistance(DataTuple t1, DataTuple t2) {
        int idx = m_Target.getArrayIndex();
        int i1 = t1.getIntVal(idx);
        int i2 = t2.getIntVal(idx);
        return m_Matrix.get(i1, i2);
    }


    @Override
    public String getDistanceName() {
        return "SSPD Matrix";
    }

}

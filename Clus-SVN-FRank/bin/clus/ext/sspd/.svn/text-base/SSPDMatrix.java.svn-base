/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package clus.ext.sspd;

import jeans.math.matrix.*;

import clus.data.io.ClusReader;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.IntegerAttrType;

import java.io.*;

/*

vraagje van saso

- use a slightly changed version of Clus
  that would take as input vectors of PCPs + distance matrix
  over samples (rather than calculating the distance based
  on a vector of target variables):
  could we count on Jan Struyfs prodigal programming skills to make this change?

samengevat: afstanden tussen vbn worden 1x op voorhand berekend en in een
matrix gezet.  clustertrees worden gebouwd zuiver op basis van
paarsgewijze afstanden tussen vbn (er worden dus geen prototypes berekend).
Dit houdt in dat variantie vervangen wordt door bv. SSPD, Sum of Squared
Pairwise Distances (tussen 2 vbn. ipv tussen 1 vb en prototype), wat
equivalent moet zijn.

Dgl. bomen leveren in eerste instantie dan geen prototype op en kunnen dus
niet voor predictie gebruikt worden.  (soit, achteraf kan je prototypes dan
nog altijd gaan definieren maar da's een andere zaak, nu niet direct te
bekijken)

Kan je zo'n aanpassing in Clus voorzien?

Dank,
Hendrik

PS ik weet niet of ik je dit nu algezegd had, maar patent-plannen zijn
opgeborgen.  Ga maar voor snelle publicatie van hierarchical multi-class
trees!
*/

import clus.main.*;
import clus.statistic.ClusDistance;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class SSPDMatrix extends ClusDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected MSymMatrix m_Matrix;
	protected IntegerAttrType m_Target;

	public SSPDMatrix(int size) {
		m_Matrix = new MSymMatrix(size);
	}

	public void setTarget(ClusAttrType[] target) throws ClusException {
		if (target.length != 1) {
			throw new ClusException("Only one target allowed in SSPD modus");
		}
		m_Target = (IntegerAttrType)target[0];
	}

	// Matrix stores squared distances [sum of (i,j)^2 and (j,i)^2]
	public static SSPDMatrix read(String filename, Settings sett) throws IOException {
		ClusReader reader = new ClusReader(filename, sett);
		int nb = 0;
		while (!reader.isEol()) {
			reader.readFloat();
			nb++;
		}
		System.out.println("Loading SSPD Matrix: "+filename+" (Size: "+nb+")");
		SSPDMatrix matrix = new SSPDMatrix(nb);
		reader.reOpen();
		for (int i = 0; i < nb; i++) {
			for (int j = 0; j < nb; j++) {
				double value = reader.readFloat();
				matrix.m_Matrix.add_sym(i, j, value * value);
			}
			if (!reader.isEol()) throw new IOException("SSPD Matrix is not square");
		}
		reader.close();
//		matrix.print(ClusFormat.OUT_WRITER, ClusFormat.TWO_AFTER_DOT, 5);
//		ClusFormat.OUT_WRITER.flush();
		return matrix;
	}

	public double calcDistance(DataTuple t1, DataTuple t2) {
		int idx = m_Target.getArrayIndex();
		int i1 = t1.getIntVal(idx);
		int i2 = t2.getIntVal(idx);
		return m_Matrix.get(i1, i2);
	}

	public String getDistanceName() {
		return "SSPD Matrix";
	}

}

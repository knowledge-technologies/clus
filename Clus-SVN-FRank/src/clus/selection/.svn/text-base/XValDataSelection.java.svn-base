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

package clus.selection;

import java.io.*;

import clus.util.*;
import clus.data.type.*;

public class XValDataSelection extends XValMainSelection {

	protected IndexAttrType m_Attr;

	public XValDataSelection(IndexAttrType type) {
		super(type.getMaxValue(), type.getNbRows());
		m_Attr = type;
	}

	public int getFold(int row) {
		return m_Attr.getValue(row)-1;
	}

	public static XValDataSelection readFoldsFile(String fname, int nbrows) throws IOException, ClusException {
		IndexAttrType attr = new IndexAttrType("XVAL");
		attr.setNbRows(nbrows);
		for (int i = 0; i < nbrows; i++) {
			attr.setValue(i, -1);
		}
		int fold = 0;
		LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
		String line = rdr.readLine();
		while (line != null) {
			line = line.trim();
			if (!line.equals("")) {
				fold++;
				String[] tokens = line.split("[\\,\\s]+");
				for (int j = 0; j < tokens.length; j++) {
					try {
						int exid = Integer.parseInt(tokens[j]);
						if (attr.getValue(exid) != -1) {
							throw new ClusException("Example id "+exid+" occurs twice in folds file: "+fname);
						} else {
							attr.setValue(exid, fold);
						}
					} catch (NumberFormatException e) {
						throw new ClusException("Illegal number: "+tokens[j]+" in folds file: "+fname);
					}
				}
			}
			line = rdr.readLine();
		}
		for (int i = 0; i < nbrows; i++) {
			if (attr.getValue(i) == -1) {
				throw new ClusException("Folds file does not define fold for example "+(i+1)+": "+fname);
			}
		}
		return new XValDataSelection(attr);
	}
}

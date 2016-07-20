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

package clus.ext.ilevelc;

import java.io.*;
import java.util.*;

import clus.data.rows.*;
import clus.util.ClusException;

public class ILevelConstraint implements Serializable{

	public final static int ILevelCMustLink = 0;
	public final static int ILevelCCannotLink = 1;

	protected int m_Type;
	protected DataTuple m_T1, m_T2;

	public ILevelConstraint(DataTuple t1, DataTuple t2, int type) {
		m_T1 = t1; m_T2 = t2; m_Type = type;
	}

	public DataTuple getT1() {
		return m_T1;
	}

	public DataTuple getT2() {
		return m_T2;
	}

	public int getType() {
		return m_Type;
	}

	public int getOtherTupleIdx(DataTuple tuple) {
		return tuple == m_T1 ? m_T2.getIndex() : m_T1.getIndex();
	}

	public boolean isSideOne(DataTuple tuple) {
		return tuple == m_T1;
	}

	public static void loadConstraints(String fname, ArrayList constr, ArrayList points) throws IOException {
		LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
		//rdr.readLine();
		String line = rdr.readLine();
		while (line != null) {
			StringTokenizer tokens = new StringTokenizer(line, "\t");
			int t1 = Integer.parseInt(tokens.nextToken());
			int t2 = Integer.parseInt(tokens.nextToken());
			int type = Integer.parseInt(tokens.nextToken()) == 1 ? ILevelCMustLink : ILevelCCannotLink;
			constr.add(new ILevelConstraint((DataTuple)points.get(t1), (DataTuple)points.get(t2), type));
			line = rdr.readLine();
		}
		rdr.close();
	}

	public static ArrayList loadConstraints(String fname, ArrayList points) throws ClusException {
		ArrayList constr = new ArrayList();
		try {
			ILevelConstraint.loadConstraints(fname, constr, points);
			return constr;
		} catch (IOException e) {
			throw new ClusException("Error opening '"+fname+"': "+e.getMessage());
		}
	}
}

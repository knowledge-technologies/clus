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

package clus.data.cols.attribute;

import java.io.*;

import clus.data.cols.*;
import clus.data.io.*;
import clus.data.type.*;

public class NumericTarget extends NumericAttrBase {

	protected ColTarget m_Target;
	protected int m_Index;

	public NumericTarget(ColTarget target, NumericAttrType type, int index) {
		super(type);
		m_Target = target;
		m_Index = index;
	}

	public boolean read(ClusReader data, int row) throws IOException {
		if (!data.readNoSpace()) return false;
		m_Target.setNumeric(m_Index, row, data.getFloat());
		return true;
	}
}

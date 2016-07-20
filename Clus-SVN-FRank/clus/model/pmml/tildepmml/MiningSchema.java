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

package clus.model.pmml.tildepmml;

import java.util.*;
import java.io.*;

public class MiningSchema{

private int nbOfFields;
private Vector miningFields;

	public MiningSchema(Vector $miningFields) {

	nbOfFields=$miningFields.size();
	miningFields=$miningFields;

	}


	public int getNbOfFields() {

	return nbOfFields;

	}

	public void addMiningField(MiningField $miningField) {

	miningFields.add($miningField);
	nbOfFields++;

	}

	public MiningField getMiningFieldAt(int index) {

	return (MiningField)miningFields.elementAt(index);

	}

	public void print(PrintWriter outStream) {
		boolean empty1 = true;
		int counter=0;

		if (nbOfFields > 0) {
			outStream.write("<MiningSchema>\n");
			empty1 = false;
		}

		while (nbOfFields>0) {
			((MiningField)miningFields.elementAt(counter)).print(outStream);
			nbOfFields--;
			counter++;
		}
		if (empty1 == false) outStream.write("</MiningSchema>\n");
	}
}

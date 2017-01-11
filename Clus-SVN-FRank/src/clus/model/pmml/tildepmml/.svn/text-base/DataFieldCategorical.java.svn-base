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

public class DataFieldCategorical extends DataField {

private Vector values;

	public DataFieldCategorical(String $name, Vector $values) {

	name=$name;
	opType="categorical";
	values=$values;

	}

	public void addValue(String value) {

	values.add(value);

	}

	public int getNbOfValues() {

	return values.size();

	}

	public void print(PrintWriter outStream) {

	int counter;
	int pointer=0;

	outStream.write("<DataField name=\""+name+"\" optype=\"categorical\"/>\n");

	counter = values.size();

	while (counter>0) {

	outStream.write(" <Value value=\""+values.elementAt(pointer)+"\"/>\n");

	pointer++;
	counter--;
	}

	outStream.write("</DataField>");

	}


}

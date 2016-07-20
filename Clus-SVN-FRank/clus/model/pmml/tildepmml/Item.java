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

import java.io.*;

public class Item{

private int id;
private boolean fieldOrValue; //true for field, false for value
private String field;
private String value;

	public Item(int $id, boolean $fieldOrValue, String $fieldOrValueString) {

	id=$id;

	if ($fieldOrValue) { field=""; value=$fieldOrValueString; }
	else { value=$fieldOrValueString; field=""; }

	}

	public int getId() {

	return id;

	}

	public boolean isField() {

	return fieldOrValue;

	}

	public String getField() {

	return field;

	}

	public String getValue() {

	return value;

	}

	public void print(PrintWriter outStream) {

	if (fieldOrValue)
	outStream.write("<Item id=\""+id+"\" field=\""+field+"\"/>\n");
	else outStream.write("<Item id=\""+id+"\" value=\""+value+"\"/>\n");


	}
}

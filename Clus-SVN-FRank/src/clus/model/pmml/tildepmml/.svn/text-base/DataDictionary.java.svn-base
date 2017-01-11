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

public class DataDictionary{

private int nbOfFields;
private Vector dataFields;

	public DataDictionary(Vector $dataFields) {

	nbOfFields=$dataFields.size();
	dataFields=$dataFields;

	}


	public int getNbOfFields() {

	return nbOfFields;

	}

	public void addDataField(DataField $dataField) {

	dataFields.add($dataField);
	nbOfFields++;

	}

	public DataField getDataFieldAt(int index) {

	return (DataField)dataFields.elementAt(index);

	}


}

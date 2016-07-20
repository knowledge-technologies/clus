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


package clus.algo.kNN.distance.attributeWeighting;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import clus.data.type.ClusAttrType;
import clus.main.Settings;


/**
 * @author Mitja Pugelj
 */
public abstract class AttributeWeighting implements Serializable{
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public AttributeWeighting(){
	}

	/**
	 * Returns weight for given attribute. Call calcWeights() before using
	 * this method.
	 * @param attr
	 * @return
	 */
	public abstract double getWeight(ClusAttrType attr);

	public static void saveToFile(AttributeWeighting attrWe, String file){
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(attrWe);
			out.close();
			fileOut.close();
			System.out.println("Saved weighting into file "+file);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static AttributeWeighting loadFromFile(String file){
		AttributeWeighting attrWe = null;
		try {
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			attrWe = (AttributeWeighting) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Loaded weighting from file "+file);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attrWe;
	}

}

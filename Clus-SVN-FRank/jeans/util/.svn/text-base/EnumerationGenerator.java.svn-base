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

package jeans.util;

import java.util.*;

/**
 * A class that returns an Enumeration that returns only a subset of a
 * given Enumeration using a certain filter. The filter is implemented
 * through the includeElement(Object) method that should be overridden
 * by the subclass.
 */
public abstract class EnumerationGenerator implements Enumeration {

	private boolean advanced = false;
	private Object currentElement;

	/**
	 * Returns true if there are still elements to be returned by the Enumeration.
	 */
	public boolean hasMoreElements() {
		if (!advanced) {
			advanced = true;
			currentElement = generateElement();
		}
		return currentElement != null;
	}

	/**
	 * Returns the next element in the Enumeration.
	 */
	public Object nextElement() {
		advanced = false;
		return currentElement;
	}

	public abstract Object generateElement();

}

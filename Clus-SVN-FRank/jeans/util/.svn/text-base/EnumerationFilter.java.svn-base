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
public abstract class EnumerationFilter implements Enumeration {

	private Enumeration enumeration;
	private boolean hasMore = true, advanced = false;
	private Object currentElement;

	/**
	 * A constructor for creating a new EnumerationFilter based
	 * on the given Enumeration
	 */
	public EnumerationFilter(Enumeration enumeration) {
		this.enumeration = enumeration;
	}

	/**
	 * Returns true if there are still elements to be returned by the Enumeration.
	 */
	public boolean hasMoreElements() {
		if (!advanced) advanceNext();
		return hasMore;
	}

	/**
	 * Returns the next element in the Enumeration.
	 */
	public Object nextElement() {
		advanced = false;
		return currentElement;
	}

	/**
	 * A method that should return true for those eleemnts in the Enumeration
	 * passed on in the constructor that must be included in the Enumeration
	 * that this object represents.
	 */
	public abstract boolean includeElement(Object element);

	/*
	 * A private auxiliary method that looks for the next element in the original
	 * Enumeration that must be included in this Enumeration object.
	 */
	private void advanceNext() {
		advanced = true;
		while (enumeration.hasMoreElements()) {
			currentElement = enumeration.nextElement();
			if (includeElement(currentElement)) return;
		}
		hasMore = false;
	}

}

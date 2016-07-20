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

package jeans.math;

public abstract class MNumber {

	public abstract int getLevel();

	public MNumber add(MNumber other) {
		if (other.getLevel() <= getLevel()) {
			MNumber convertedOther = other.convertTo(this);
			return doAdd(convertedOther);
		} else {
			MNumber convertedThis = convertTo(other);
			return convertedThis.doAdd(other);
		}
	}

	public MNumber substract(MNumber other) {
		if (other.getLevel() <= getLevel()) {
			MNumber convertedOther = other.convertTo(this);
			return doSubstract(convertedOther);
		} else {
			MNumber convertedThis = convertTo(other);
			return convertedThis.doSubstract(other);
		}
	}

	public MNumber multiply(MNumber other) {
		if (other.getLevel() <= getLevel()) {
			MNumber convertedOther = other.convertTo(this);
			return doMultiply(convertedOther);
		} else {
			MNumber convertedThis = convertTo(other);
			return convertedThis.doMultiply(other);
		}
	}

	public MNumber divide(MNumber other) {
		if (other.getLevel() <= getLevel()) {
			MNumber convertedOther = other.convertTo(this);
			return doDivide(convertedOther);
		} else {
			MNumber convertedThis = convertTo(other);
			return convertedThis.doDivide(other);
		}
	}

	public abstract MNumber doAdd(MNumber other);

	public abstract MNumber doSubstract(MNumber other);

	public abstract MNumber doMultiply(MNumber other);

	public abstract MNumber doDivide(MNumber other);

	public abstract MNumber convertTo(MNumber other);

	public abstract double getDouble();

}

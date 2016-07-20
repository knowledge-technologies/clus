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

public class MDouble extends MNumber {

	private double value;

	public MDouble(String strg) throws NumberFormatException {
		this(Double.parseDouble(strg));
	}

	public MDouble(double value) {
		this.value = value;
	}

	public MDouble() {
		this.value = 0;
	}

	public int getLevel() {
		return 2;
	}

	public static boolean isDouble(String str) {
		if (str.length() == 0) return false;
		int ch = str.charAt(0);
		if (!(((ch >= '0') && (ch <= '9')) || ch == '.' || ch == '+' || ch == '-')) return false;
		for (int i = 1; i < str.length(); i++) {
			ch = str.charAt(i);
			if (!(((ch >= '0') && (ch <= '9')) || ch == '.' || ch == '+' || ch == '-' || ch == 'e' || ch == 'E')) return false;
		}
		return true;
	}

	public MNumber doAdd(MNumber other) {
		return new MDouble(value+((MDouble)other).value);
	}

	public MNumber doSubstract(MNumber other) {
		return new MDouble(value-((MDouble)other).value);
	}

	public MNumber doMultiply(MNumber other) {
		return new MDouble(value*((MDouble)other).value);
	}

	public MNumber doDivide(MNumber other) {
		return new MDouble(value/((MDouble)other).value);
	}

	public MNumber convertTo(MNumber other) {
		if (other instanceof MDouble) return this;
		return new MComplex(this,MLong.ZERO);
	}

	public void addDouble(double add) {
		value += add;
	}

	public double getDouble() {
		return value;
	}

	public String toString() {
		return String.valueOf(value);
	}

}

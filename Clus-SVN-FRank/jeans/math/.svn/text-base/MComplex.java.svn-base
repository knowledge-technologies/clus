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

public class MComplex extends MNumber {

	private MNumber real, imag;

	public MComplex(MNumber real, MNumber imag) {
		this.real = real;
		this.imag = imag;
	}

	public int getLevel() {
		return 3;
	}

	public MNumber doAdd(MNumber other) {
		MNumber myreal = real.add(((MComplex)other).real);
		MNumber myimag = imag.add(((MComplex)other).imag);
		return new MComplex(myreal, myimag);
	}

	public MNumber doSubstract(MNumber other) {
		MNumber myreal = real.substract(((MComplex)other).real);
		MNumber myimag = imag.substract(((MComplex)other).imag);
		return new MComplex(myreal, myimag);
	}

	public MNumber doMultiply(MNumber other) {
		MNumber oreal = ((MComplex)other).real;
		MNumber oimag = ((MComplex)other).imag;
		MNumber myreal = real.multiply(oreal).substract(imag.multiply(oimag));
		MNumber myimag = real.multiply(oimag).add(imag.multiply(oreal));
		return new MComplex(myreal, myimag);
	}

	public MNumber doDivide(MNumber other) {
		MNumber oreal = ((MComplex)other).real;
		MNumber oimag = ((MComplex)other).imag;
		MNumber myreal = real.multiply(oreal).add(imag.multiply(oimag));
		MNumber myimag = imag.multiply(oreal).substract(real.multiply(oimag));
		MNumber denom = oreal.multiply(oreal).add(oimag.multiply(oimag));
		return new MComplex(myreal.divide(denom), myimag.divide(denom));
	}

	public MNumber convertTo(MNumber other) {
		return this;
	}

	public double getDouble() {
		return 0;
	}

	public String toString() {
		return String.valueOf(real) + "+" + String.valueOf(imag) + "i";
	}

}

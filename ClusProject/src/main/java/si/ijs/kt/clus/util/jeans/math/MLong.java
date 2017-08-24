/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.util.jeans.math;

public class MLong extends MNumber {

    public final static MLong ZERO = new MLong(0);
    public final static MLong ONE = new MLong(1);

    private long value;


    public MLong(String strg) throws NumberFormatException {
        this(Long.parseLong(strg));
    }


    public MLong(long value) {
        this.value = value;
    }


    @Override
    public int getLevel() {
        return 0;
    }


    @Override
    public MNumber doAdd(MNumber other) {
        return new MLong(value + ((MLong) other).value);
    }


    @Override
    public MNumber doSubstract(MNumber other) {
        return new MLong(value - ((MLong) other).value);
    }


    @Override
    public MNumber doMultiply(MNumber other) {
        return new MLong(value * ((MLong) other).value);
    }


    @Override
    public MNumber doDivide(MNumber other) {
        return new MLong(value / ((MLong) other).value);
    }


    @Override
    public MNumber convertTo(MNumber other) {
        if (other instanceof MLong)
            return this;
        if (other instanceof MDouble)
            return new MDouble(value);
        else
            return new MComplex(this, ZERO);
    }


    @Override
    public double getDouble() {
        return value;
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }

}

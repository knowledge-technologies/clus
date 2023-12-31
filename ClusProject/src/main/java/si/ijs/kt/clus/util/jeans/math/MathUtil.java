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

import java.util.ArrayList;
import java.util.HashSet;

import si.ijs.kt.clus.util.ClusLogger;


public class MathUtil {

    public final static double C1E_6 = 1e-6;
    public final static double C1E_9 = 1e-9;
    public final static double C1E_16 = 1e-16;
    public final static double M_LN2 = Math.log(2);


    public static double interpolate(double x, ArrayList<double[]> fct) {
        int len = fct.size();
        if (len == 0) {
            return Double.NaN;
        }
        else if (len == 1) {
            // function is a constant
            double[] pt = fct.get(0);
            return pt[1];
        }
        else {
            double[] pt0 = fct.get(0);
            if (x < pt0[0]) {
                // extrapolate left
                return pt0[1];
            }
            double[] ptn = fct.get(len - 1);
            if (x > ptn[0]) {
                // extrapolate right
                return ptn[1];
            }
            // maybe equal to some given x-value?
            int count = 0;
            double sum = 0.0;
            for (int i = 0; i < len; i++) {
                double[] pt1 = fct.get(i);
                if (x == pt1[0]) {
                    sum += pt1[1];
                    count++;
                }
            }
            if (count > 0) { return sum / count; }
            // not equal to given x-value -> base case
            for (int i = 0; i < len - 1; i++) {
                double[] pt1 = fct.get(i);
                double[] pt2 = fct.get(i + 1);
                if (x >= pt1[0] && x < pt2[0]) {
                    double y1 = pt1[1];
                    double y2 = pt2[1];
                    double x1 = pt1[0];
                    double x2 = pt2[0];
                    return (x - x1) / (x2 - x1) * (y2 - y1) + y1;
                }
            }
            return Double.NaN;
        }
    }

    protected double[] blackBoxTestInterpolateX = { 2, 5, 5, 5, 9.5 };
    protected double[] blackBoxTestInterpolateY = { 3.5, 3.5, 4.5, 5.5, 1.5 };


    protected void blackBoxTestInterpolate() {
        ArrayList<double[]> fct = new ArrayList<>();
        for (int i = 0; i < blackBoxTestInterpolateX.length; i++) {
            fct.add(new double[] { blackBoxTestInterpolateX[i], blackBoxTestInterpolateY[i] });
        }
        ClusLogger.info("Interpolate x = 0   -> y = 3.5: " + MathUtil.interpolate(0, fct));
        ClusLogger.info("Interpolate x = 4   -> y = 3.5: " + MathUtil.interpolate(4, fct));
        ClusLogger.info("Interpolate x = 5   -> y = 4.5: " + MathUtil.interpolate(5, fct));
        ClusLogger.info("Interpolate x = 7   -> y = 3.8: " + MathUtil.interpolate(7, fct));
        ClusLogger.info("Interpolate x = 9.5 -> y = 1.5: " + MathUtil.interpolate(9.5, fct));
        ClusLogger.info("Interpolate x = 10  -> y = 1.5: " + MathUtil.interpolate(10, fct));
    }


    /**
     * Computes the symmetric difference between the two lists.
     * 
     * @param elements1
     * @param elements2

     */
    public static <T> HashSet<T> symmetricDifference(ArrayList<T> elements1, ArrayList<T> elements2) {
        HashSet<T> symmetricDifference = new HashSet<T>();
        // add elements1
        for (T elt : elements1) {
            symmetricDifference.add(elt);
        }
        // remove the intersection and add elements2 - elements1
        for (T elt : elements2) {
            if (symmetricDifference.contains(elt)) {
                symmetricDifference.remove(elt);
            }
            else {
                symmetricDifference.add(elt);
            }
        }
        return symmetricDifference;
    }

    public static void main(String[] arg) {
        MathUtil mu = new MathUtil();
        mu.blackBoxTestInterpolate();
    }
}

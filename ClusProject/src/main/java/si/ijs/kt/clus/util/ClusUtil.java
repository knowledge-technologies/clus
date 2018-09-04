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

package si.ijs.kt.clus.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.main.ClusStatManager;


public class ClusUtil {

    public final static double MICRO = 1e-6;
    public final static double NANO = 1e-9;
    public final static double PICO = 1e-12;
    public static final double NaN = Double.NaN;


    public static boolean grOrEq(double a, double b) {
        /* a > b - MICRO */
        return (b - a < MICRO);
    }


    public static boolean smOrEq(double a, double b) {
        return (a - b < MICRO);
    }


    public static boolean eq(double a, double b) {
        // return (a - b < MICRO) && (b - a < MICRO);
        return eq(a, b, MICRO);
    }


    public static boolean eq(double a, double b, double allowedDifference) {
        return Math.abs(a - b) < allowedDifference;
    }


    public static double roundToSignificantFigures(double num, int n) {
        if (num == 0) { return 0; }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }


    public static void main(String[] arg) {
        double d1 = -1.23456789;
        double d2 = -0.0;
        double d3 = -1234567.89;
        double d4 = -1.234E-12;
        double d5 = 2.23E-13;
        double d6 = 0.0000000012345;
        int n = 3;
        ClusLogger.info(roundToSignificantFigures(d1, n));
        ClusLogger.info(roundToSignificantFigures(d2, n));
        ClusLogger.info(roundToSignificantFigures(d3, n));
        ClusLogger.info(roundToSignificantFigures(d4, n));
        ClusLogger.info(roundToSignificantFigures(d5, n));
        ClusLogger.info(roundToSignificantFigures(d6, n));

        String[] tests = new String[] { "dsa", "sdaaa/dsa", "a/a/a/dsa", "s\\c\\dsa" };
        for (String test : tests) {
            ClusLogger.info(fileName(test));
        }
    }


    public static double roundDouble(double value, int afterDecimalPoint) {
        double mask = Math.pow(10.0, afterDecimalPoint);

        return (Math.round(value * mask)) / mask;
    }


    public static boolean isNaN(double d) {
        return Double.isNaN(d);
    }


    public static boolean isZero(double d) {
        return d == 0.0F;
    }


    public static boolean isNaNOrZero(double d) {
        return isNaN(d) || isZero(d);
    }


    public static boolean isAnyNaNOrZero(double[] numbers) {
        for (double d : numbers) {
            if (isNaNOrZero(d))
                return true;
        }
        return false;
    }


    /**
     * From "a/b/c/d" or "a\\b\\c\\d" or "d" extracts d.
     */
    public static String fileName(String pathToFile) {
        String answer = pathToFile.replace("\\", "/");
        int i = answer.lastIndexOf("/") + 1;
        return answer.substring(i);
    }


    public static HashMap<String, Integer> getDescriptiveAttributesIndices(ClusStatManager statmgr) {
        HashMap<String, Integer> indices = new HashMap<String, Integer>();
        if (statmgr.getSettings().getOutput().isOutputPythonModel()) {
            ClusAttrType[] cat = ClusSchema.vectorToAttrArray(statmgr.getSchema().collectAttributes(AttributeUseType.Descriptive, null));

            for (int ii = 0; ii < cat.length - 1; ii++) {
                indices.put(cat[ii].getName(), ii);
            }

            int ii = cat.length - 1;
            indices.put(cat[ii].getName(), ii);
        }
        return indices;
    }

    public static String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}

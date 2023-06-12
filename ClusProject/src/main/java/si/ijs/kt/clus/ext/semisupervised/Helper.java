package si.ijs.kt.clus.ext.semisupervised;

import java.util.Arrays;

import si.ijs.kt.clus.ext.semisupervised.utils.DoublesPair;
import si.ijs.kt.clus.ext.semisupervised.utils.IndiceValuePair;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.jeans.math.MathUtil;

/**
 * Some handy functions
 * @author jurical
 */
public class Helper {

    /**
     * Calculates average of an array of values
     */
    public static double average(double[] values) {
        double avg = 0;
        for (int i = 0; i < values.length; i++) {
            avg += values[i];
        }

        return avg / values.length;
    }
    
    public static double averageIgnoreZeros(double[] values) {
        double avg = 0;
        int counter = 0;
        for (int i = 0; i < values.length; i++) {
            if(values[i] < -MathUtil.C1E_9 || values[i] > MathUtil.C1E_9) {
                counter++;
                avg += values[i];
            }
        }

        return avg / counter;
    }

    /**
     * Calculates minimum of an array of values
     */
    public static double min(double[] values) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }
    
    /**
     * Calculates sum of an array of values
     */
    public static double sum(double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return sum;
    }

    /**
     * Calculates maximum of an array of values
     */
    public static double max(double[] values) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    /**
     * Return the values of an array in a comma separated string
     *
     * @param array

     */
    public static String arrayToString(double[] array) {
        String ret = "";
        for (int i = 0; i < array.length; i++) {
            if (i + 1 < array.length) {
                ret += array[i] + ",";
            } else {
                ret += array[i];
            }
        }

        return ret;
    }
    
    /**
     * Return the values of an array in a string separated <code>sep</code>
     *
     * @param array
     * @param sep separator

     */
    public static String arrayToString(double[] array, String sep) {
        String ret = "";
        for (int i = 0; i < array.length; i++) {
            if (i + 1 < array.length) {
                ret += array[i] + sep;
            } else {
                ret += array[i];
            }
        }

        return ret;
    }
    
    /**
     * Return the values of an array in a string separated <code>sep</code>
     *
     * @param array
     * @param sep separator

     */
    public static String arrayToString(String[] array, String sep) {
        String ret = "";
        for (int i = 0; i < array.length; i++) {
            if (i + 1 < array.length) {
                ret += array[i] + sep;
            } else {
                ret += array[i];
            }
        }

        return ret;
    }
    
    /**
     * Return the values of an array in a string separated <code>sep</code>
     *
     * @param array
     * @param sep separator

     */
    public static String arrayToString(IndiceValuePair[] array, String sep) {
        String ret = "";
        for (int i = 0; i < array.length; i++) {
            if (i + 1 < array.length) {
                ret += array[i].getIndice() + ":" + array[i].getValue() + sep;
            } else {
                ret += array[i].getIndice() + ":" + array[i].getValue();
            }
        }

        return ret;
    }

    /**
     * Calculates standard deviation IMPORTANT: this implementation is buggy, it
     * returns Double.NaN, when stdDev should be 0
     */
    public static double stDevOpt(double[] a) {
        double avg = 0;
        double summ = 0;
        for (int i = 0; i < a.length; i++) {
            avg += a[i];
            summ += a[i] * a[i];
        }
        avg = avg / a.length;
        double sd = (summ - a.length * avg * avg) / (a.length - 1);
        return Math.sqrt(sd);
    }

    /**
     * Return array containing just the second values of the DoublePairs array
     */
    public static double[] getArrayOfSecond(DoublesPair[] dpArrray) {
        double[] temp = new double[dpArrray.length];

        for (int i = 0; i < dpArrray.length; i++) {
            temp[i] = dpArrray[i].getSecond();
        }

        return temp;
    }

    /**
     * Prints array
     *
     * @param array
     */
    public static void printArray(DoublesPair[] array) {
        for (int i = 0; i < array.length; i++) {
            ClusLogger.info(array[i].getFirst() + "," + array[i].getSecond());
        }
    }

    public static void printArray(double[] array) {
        for (int i = 0; i < array.length; i++) {
            if (i + 1 < array.length) {
                System.out.print(array[i] + ",");
            } else {
                System.out.print(array[i]);
            }
        }
    }

    public static double getMean(double[] data) {
        double sum = 0.0;
        for (double a : data) {
            sum += a;
        }
        return sum / data.length;
    }

    public static double getVariance(double[] data) {
        double mean = getMean(data);
        double temp = 0;
        for (double a : data) {
            temp += (mean - a) * (mean - a);
        }
        return temp / (data.length - 1);
    }

    /**
     * corrected sample standard deviation
     *
     * @param data

     */
    public static double getStdDev(double[] data) {
        return Math.sqrt(getVariance(data));
    }

    public static double median(double[] data) {
        double[] b = new double[data.length];
        System.arraycopy(data, 0, b, 0, b.length);
        Arrays.sort(b);

        if (data.length % 2 == 0) {
            return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
        } else {
            return b[b.length / 2];
        }
    }

    /**
     * Returns K least elements of an array, together with indices
     *
     * @param myArray array may contain Double.NaN, such elements of arrray are
     * ignored
     * @param K
     * @return array of K least elements (index,value pairs) of an input array
     */
    public static IndiceValuePair[] leastKelements(double[] myArray, int K) {
        IndiceValuePair[] pairs = new IndiceValuePair[K];
        int added = 0;

        for (int i = 0; i < myArray.length; i++) {
            for (int j = 0; j < K; j++) {
                //for the first five entries
                if (pairs[j] == null) {
                    if (!Double.isNaN(myArray[i])) {
                        pairs[j] = new IndiceValuePair(i, myArray[i]);
                        added++;
                    }
                    break;
                } else if (pairs[j].getValue() > myArray[i]) {
                    //inserts the new pair into its correct spot
                    for (int k = K - 1; k > j; k--) {
                        pairs[k] = pairs[k - 1];
                    }
                    if (!Double.isNaN(myArray[i])) {
                        pairs[j] = new IndiceValuePair(i, myArray[i]);
                        added++;
                    }
                    break;
                }
            }
        }

        //if resulting array contains less than K elements, truncate it
        if (added < K) {
            return Arrays.copyOf(pairs, added);
        }

        return pairs;
    }

    /**
     * Returns K greatest elements of an array, together with indices
     *
     * @param myArray
     * @param K
     * @return array of K greatest elements (index,value pairs) of an input
     * array
     */
    public static IndiceValuePair[] greatestKelements(double[] myArray, int K) {
        IndiceValuePair[] pairs = new IndiceValuePair[K];
        int added = 0;

        for (int i = 0; i < myArray.length; i++) {
            for (int j = 0; j < K; j++) {
                //for the first five entries
                if (pairs[j] == null) {
                    if (!Double.isNaN(myArray[i])) {
                        pairs[j] = new IndiceValuePair(i, myArray[i]);
                        added++;
                    }
                    break;
                } else if (pairs[j].getValue() < myArray[i]) {
                    //inserts the new pair into its correct spot
                    for (int k = K - 1; k > j; k--) {
                        pairs[k] = pairs[k - 1];
                    }
                    if (!Double.isNaN(myArray[i])) {
                        pairs[j] = new IndiceValuePair(i, myArray[i]);
                        added++;
                    }
                    break;
                }
            }
        }

        //if resulting array contains less than K elements, truncate it
        if (added < K) {
            return Arrays.copyOf(pairs, added);
        }

        return pairs;
    }
}

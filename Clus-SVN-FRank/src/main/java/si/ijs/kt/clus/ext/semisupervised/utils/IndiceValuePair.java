package si.ijs.kt.clus.ext.semisupervised.utils;

/**
* Structure that contains one int and one double
* @author jurical
*/
public class IndiceValuePair implements Comparable {

       private int indice;
       private double value;

       public IndiceValuePair(int ind, double val) {
           indice = ind;
           value = val;
       }

       public int getIndice() {
           return indice;
       }

       public double getValue() {
           return value;
       }

       @Override
       public int compareTo(Object o) {

           double otherValue = ((IndiceValuePair) o).getValue();

           if (Double.isNaN(value) && Double.isNaN(otherValue)) {
               return 0;
           }
           if (Double.isNaN(value)) {
               return -1;
           }
           if (Double.isNaN(otherValue)) {
               return 1;
           }

           double difference = value - otherValue;

           if (difference == 0.0) {
               return 0;
           }
           if (difference > 0) {
               return 1;
           }

           return -1;
       }
   }

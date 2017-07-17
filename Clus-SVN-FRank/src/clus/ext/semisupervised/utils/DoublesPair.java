package clus.ext.semisupervised.utils;

/**
* Structure that contains two doubles
* @author jurical
*/
public class DoublesPair implements Comparable {

       private double first;
       private double second;

       public DoublesPair(double first, double second) {
           this.first = first;
           this.second = second;
       }

       public double getFirst() {
           return first;
       }

       public double getSecond() {
           return second;
       }

       @Override
       public int compareTo(Object o) {
           double difference = this.getFirst() - ((DoublesPair) o).getFirst();

           if (difference == 0.0) {
               return 0;
           }
           if (difference < 0) {
               return -1;
           }

           return 1;
       }
   }

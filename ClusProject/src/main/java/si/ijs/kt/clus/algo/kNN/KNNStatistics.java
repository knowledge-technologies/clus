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

package si.ijs.kt.clus.algo.kNN;

import si.ijs.kt.clus.algo.kNN.distance.valentin.NominalStatistic;
import si.ijs.kt.clus.algo.kNN.distance.valentin.NumericStatistic;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.util.ClusLogger;

/**
 * This class calculates several usefull statistics of
 * the training data used for kNN and kNNTree
 */
@Deprecated
public class KNNStatistics {

    private DataTuple[] $prototypes;

    public KNNStatistics(RowData data){
        calcMeasures(data);
        printPrototype(0,data);
    }
    /**
     * Calculates some meaningful measures of the data like mean of each attribute.
     */
    public void calcMeasures(RowData data){

        ClusSchema schema = data.getSchema();
        ClusAttrType[] attrs = schema.getDescriptiveAttributes();

        $prototypes = new DataTuple[1/*target.getNbNomValues(0)*/];
        //Initialize the prototypes.
        for (int i=0;i<$prototypes.length;i++){
            $prototypes[i] = new DataTuple(schema);
        }

        //ClusAttrType current;
        //$stats = new AttributeStatistic[attrs.length];

        for (int i = 0; i < attrs.length; i++){

            if (attrs[i].getAttributeType().equals(AttributeType.Nominal)){
                calcNominalMeasures(data,(NominalAttrType) attrs[i]);
            }
            else if (attrs[i].getAttributeType().equals(AttributeType.Numeric)){
                calcNumericMeasures(data,(NumericAttrType) attrs[i]);
            }
        }
    }
    /**
     * Calculates the meaningful measures for given nominal attribute
     * in the data : {@literal ->} find most frequent value for given attribute
     * Also calculates the prototype values of this attribute
     */
    public void calcNominalMeasures(RowData data,NominalAttrType attr){

        int[] occurences = new int[attr.getNbValues()]; //here the number of occurences for all values stored

        int aTargetValues = $prototypes.length;
        int[][] p_occs = new int[attr.getNbValues()][aTargetValues];//occurences used for prototypes

        NominalStatistic stat = new NominalStatistic();
        int nbr = data.getNbRows();

        int curVal;
        int curTargetVal;

        for (int i = 0; i <nbr;i++){
            curVal = attr.getNominal(data.getTuple(i));

            curTargetVal = data.getTuple(i).getClassification();

            //check if not a missing value
            if (curVal != attr.getNbValues()){
                occurences[curVal]++;
                p_occs[curVal][curTargetVal]++;
            }
        }

        int max = occurences[0];
        int[] maxs = new int[aTargetValues]; //maxima per prototype
        int index_max = 0;
        int[] index_maxs = new int[aTargetValues];

        for (int i=1;i<attr.getNbValues();i++){
            if (max < occurences[i]){
                max = occurences[i];
                index_max = i;
            }
            for (int j=1;j<aTargetValues;j++){
                if (maxs[j] < p_occs[i][j]){
                    maxs[j] = p_occs[i][j];
                    index_maxs[j] = i;
                }
            }
        }
        //now index_max is the most frequent value for this attribute in the given data.
        stat.setMean(index_max);
        //ClusLogger.info("Mean for attribute "+attr.getName()+":"+index_max);
        attr.setStatistic(stat);
        ClusLogger.info("Prototype values for attribute "+attr.getName()+":");

        //fill in prototype values
        int idx = attr.getArrayIndex();

        for (int i=0;i<aTargetValues;i++){
            System.out.print(index_maxs[i]+",");
            $prototypes[i].setIntVal(index_maxs[i],idx);

        }
        ClusLogger.info();

    }
    /**
     * Calculate meaningful measures like mean, variance,max and min for
     * given numerical attribute.
     * Also calculates the prototype values of this attribute
     */
    public void calcNumericMeasures(RowData data,NumericAttrType attr){
        double min,max,mean,variance;
        NumericStatistic stat = new NumericStatistic();
        int nbr = data.getNbRows();
        int actualnbr = nbr;
        double curVal;
        int curTargetVal;
        mean = 0;

        int aTargetValues = $prototypes.length;
        double[] means = new double[aTargetValues];
        double[] p_occs = new double[aTargetValues];

        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        //first mean ,min and max
        for (int i = 0; i <nbr;i++){
            curVal = attr.getNumeric(data.getTuple(i));

            curTargetVal = data.getTuple(i).getClassification();

            //check if  missing value.
            if (curVal == Double.NaN){
                actualnbr--;
                ClusLogger.info("Fuck");
            }
            else{
                if (curVal < min ) min = curVal;
                if (curVal > max ) max = curVal;

                means[curTargetVal] += curVal;
                p_occs[curTargetVal]++;

                //mean += curVal;
            }
        }

        for (int i=0;i<aTargetValues;i++){
            means[i] = means[i]/p_occs[i];
            mean += means[i];
        }
        mean = mean / aTargetValues;

        //mean = mean / actualnbr;

        //then variance
        variance = 0;
        for (int i = 0; i <nbr;i++){
            curVal = attr.getNumeric(data.getTuple(i));
            //check if  missing value.
            if (curVal != Double.NaN){
                variance += Math.pow(curVal - mean, 2);
            }
        }
        variance = variance / actualnbr;

        stat.setMean(mean);
        stat.setMax(max);
        stat.setMin(min);
        stat.setVariance(variance);
        /*ClusLogger.info("Mean for attribute "+attr.getName()+":"+mean);
        ClusLogger.info("Max for attribute "+attr.getName()+":"+max);
        ClusLogger.info("Min for attribute "+attr.getName()+":"+min);
        ClusLogger.info("Variance for attribute "+attr.getName()+":"+variance);
        */
        //save the statistic at the attributetype
        attr.setStatistic(stat);
        ClusLogger.info("Prototype values for attribute "+attr.getName()+":");

        //fill in prototype values
        int idx = attr.getArrayIndex();

        for (int i=0;i<aTargetValues;i++){
            System.out.print(means[i]+",");
            $prototypes[i].setDoubleVal(means[i],idx);
        }
        ClusLogger.info();

    }
    public DataTuple[] getPrototypes(){
        return $prototypes;
    }
    /**
     * Returns the idx'th prototype
     * (meaning: of the idx'th value of the target attribute )
     * require
     *  idx {@literal <} amount of possible target values
     */
    public DataTuple getPrototype(int idx){
        return $prototypes[idx];
    }
    /**
     * Prints the values of the idx'th prototype.
     */
    public void printPrototype(int idx,RowData data){
        DataTuple t = $prototypes[idx];
        ClusSchema schema = data.getSchema();
        ClusAttrType[] attrs = schema.getDescriptiveAttributes();

        for (int i = 0; i < attrs.length; i++){
            if (attrs[i].getAttributeType().equals(AttributeType.Nominal)){
                System.out.print(attrs[i].getNominal(t)+",");
            }
            else if (attrs[i].getAttributeType().equals(AttributeType.Numeric)){
                System.out.print(attrs[i].getNumeric(t)+",");
            }
        }
        ClusLogger.info(")");
    }
}

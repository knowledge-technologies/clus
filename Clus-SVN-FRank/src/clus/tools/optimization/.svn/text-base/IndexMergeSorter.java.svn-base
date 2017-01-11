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

package clus.tools.optimization;

/**
 * Merge sort which returns the indexes of the target array, not the target array.
 * The order is ascending.
 * Original implementation from http://www.iti.fh-flensburg.de/lang/algorithmen/sortieren/merge/mergen.htm
 * @author Timo Aho
 *
 */
public class IndexMergeSorter
{
    private static double[] m_workArray, m_tempArray;
    private static int[] m_indOfSorted, m_indTemp;

    /*
    * if useAbsoluteValues = true, use absolute values, not real values.
    */
    public static int[] sort(double[] targetArray, boolean useAbsoluteValues)
    {
    	m_workArray = new double[targetArray.length];

		if (useAbsoluteValues) {
	        for (int iEl = 0; iEl < targetArray.length; iEl++) {
	        	m_workArray[iEl]= Math.abs(targetArray[iEl]);
	        }
		} else {
			m_workArray = targetArray.clone();
		}

        int nbOfEl=m_workArray.length;
        // according to variant either/or:
        //b=new int[nbOfEl];
        m_indOfSorted = new int[nbOfEl];
        for (int i = 0; i < nbOfEl; i++)
        	m_indOfSorted[i] = i;

        m_indTemp = new int[(nbOfEl+1)/2];
        m_tempArray=new double[(nbOfEl+1)/2];
        mergesort(0, nbOfEl-1);

        return m_indOfSorted;
    }

    /** Consider only the elements that are true in subArray. However, returned indexes
     * are for the WHOLE array, not for subarray.
     * if useAbsoluteValues = true, use absolute values, not real values.
     */
    public static int[] sortSubArray(double[] targetArray, boolean[] subArray, int nbOfTrueInSubArray,
    		boolean useAbsoluteValues)
    {
        int nbOfEl=m_workArray.length;

    	m_indOfSorted = new int[nbOfTrueInSubArray];
    	m_workArray = new double[nbOfTrueInSubArray];

    	int iSubArray = 0;
        for (int iWholeArray = 0; iWholeArray < nbOfEl; iWholeArray++) {
        	if (subArray[iWholeArray]) {
        		if (useAbsoluteValues) {
        			m_workArray[iSubArray]= Math.abs(targetArray[iWholeArray]);
        		} else {
        			m_workArray[iSubArray]=targetArray[iWholeArray];
        		}

        		m_indOfSorted[iSubArray] = iWholeArray;
        		iSubArray++;
        	}
        }

        // according to variant either/or:
        //b=new int[nbOfEl];

        m_indTemp = new int[(nbOfTrueInSubArray+1)/2];
        m_tempArray=new double[(nbOfTrueInSubArray+1)/2];
        mergesort(0, nbOfTrueInSubArray-1);

        return m_indOfSorted;
    }


    private static void mergesort(int lowestElement, int highestElement)
    {
        if (lowestElement<highestElement)
        {
            int middleElement=(lowestElement+highestElement)/2;
            mergesort(lowestElement, middleElement);
            mergesort(middleElement+1, highestElement);
            merge(lowestElement, middleElement, highestElement);
        }
    }

    private static void merge(int lowestElement, int midElement, int highestElement)
    {
    	// Efficient variant

    	int i, j, k;

    	i=0; j=lowestElement;
    	// copy first half of array a to auxiliary array b
    	while (j<=midElement) {
    		m_indTemp[i] = m_indOfSorted[j];
    		m_tempArray[i++]=m_workArray[j++];
    	}

    	i=0; k=lowestElement;
    	// copy back next-greatest element at each time
    	while (k<j && j<=highestElement) {
    		if (m_tempArray[i]<=m_workArray[j]) {
    			m_indOfSorted[k] = m_indTemp[i];
    			m_workArray[k++]=m_tempArray[i++];
    		} else {
    			m_indOfSorted[k] = m_indOfSorted[j];
    			m_workArray[k++]=m_workArray[j++];
    		}
    	}

    	// copy back remaining elements of first half (if any)
    	while (k<j) {
			m_indOfSorted[k] = m_indTemp[i];
    		m_workArray[k++]=m_tempArray[i++];
    	}
    }

}    // end class MergeSorter


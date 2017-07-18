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

package si.ijs.kt.clus.selection;

import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.ClusRandomNonstatic;


public class BaggingSelection extends ClusSelection {

    protected int[] m_Counts;
    protected int m_NbSel;


    /**
     * Creates a new bagging selection of size <code>nbrows</code> from a total of <code>nbrows</code> instances.
     * This constructor does NOT take in to account the BagSize setting users can set in the settings file. If you
     * want to take this in to account, you should use
     * <code>BaggingSelection(nbrows, getSettings().getEnsembleBagSize())</code>
     * instead.
     * 
     * @param nbrows
     *        the number of instances
     * @see clus.main.Settings#getEnsembleBagSize()
     */

    public BaggingSelection(int nbrows) {
        super(nbrows);
    }
    
    /**
     * Create a new bagging selection of size <code>nbselected</code> from a total of <code>nbrows</code> instances.
     * If <code>nbselected</code> == 0, a bagging selection of size <code>nbrows</code> is created
     * 
     * @param nbrows
     *        the total number of instances
     * @param nbselected
     *        the size of the bagging selection
     * @param rnd
     *        random generator object that is used for creating the bag. May be null. In that case, ClusRandom is used
     *        instead, which might
     *        result in non-reproducibility if the number of cores is greater than one.
     */
    public BaggingSelection(int nbrows, int nbselected, ClusRandomNonstatic rnd) {
        super(nbrows);
        m_Counts = new int[nbrows];
        if (nbselected == 0)
            nbselected = nbrows;
        if (rnd == null) {
            for (int i = 0; i < nbselected; i++) {
                m_Counts[ClusRandom.nextInt(ClusRandom.RANDOM_SELECTION, nbrows)]++;
            }
        }
        else {
            for (int i = 0; i < nbselected; i++) {
                m_Counts[rnd.nextInt(ClusRandomNonstatic.RANDOM_SELECTION, nbrows)]++;
            }
        }
        for (int i = 0; i < nbrows; i++) {
            if (m_Counts[i] != 0)
                m_NbSel++;
        }
    }


    public boolean changesDistribution() {
        return true;
    }


    public double getWeight(int row) {
        return (double) m_Counts[row];
    }


    public int getNbSelected() {
        return m_NbSel;
    }


    public boolean isSelected(int row) {
        return m_Counts[row] != 0;
    }


    public final int getCount(int row) {
        return m_Counts[row];
    }
}

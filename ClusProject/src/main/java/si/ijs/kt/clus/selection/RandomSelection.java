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

import java.util.Random;


public class RandomSelection extends ClusSelection {

    protected int m_NbSelected;
    protected boolean[] m_Selection;
    int m_Seed;


    public RandomSelection(int nbrows, double sel) {
        super(nbrows);
        m_Seed = 0; // default seed is 0
        makeSelection(nbrows, (int) Math.round(sel * nbrows));
    }


    public RandomSelection(int nbrows, int nbsel) {
        super(nbrows);
        m_Seed = 0; // default seed is 0
        makeSelection(nbrows, nbsel);
    }

    public RandomSelection(int nbrows, double sel, int randomSeed) {
		super(nbrows);
		m_Seed = randomSeed;
		makeSelection(nbrows, (int)Math.round(sel*nbrows));
	}
        
        public RandomSelection(int nbrows, int nbsel, int randomSeed) {
		super(nbrows);
		m_Seed = randomSeed;
		makeSelection(nbrows, nbsel);
	}

    public void setRandomSeed(int seed){
    	m_Seed = seed;
    }
        
    @Override
    public int getNbSelected() {
        return m_NbSelected;
    }


    @Override
    public boolean isSelected(int row) {
        return m_Selection[row];
    }


    private final void makeSelection(int nbrows, int nbsel) {
        m_NbSelected = nbsel;
        m_Selection = new boolean[nbrows];
        //Random rnd = new Random(0);
        Random rnd = new Random(m_Seed);
        for (int i = 0; i < m_NbSelected; i++) {
            int j = 0;
            int p = rnd.nextInt(nbrows - i) + 1; // Select one of the remaining positions

            while (p > 0 && j < nbrows) {
                if (!m_Selection[j]) {
                    p--;
                    if (p == 0)
                        m_Selection[j] = true;
                }
                j++;
            }
        }
    }
}

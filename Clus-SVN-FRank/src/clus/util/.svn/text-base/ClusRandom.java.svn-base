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

package clus.util;

import clus.main.*;

import java.util.*;

public class ClusRandom {

	public static int m_Preset;
	public static boolean m_IsPreset;

	public final static int NB_RANDOM = 7;
	public final static int RANDOM_TEST_DIR = 0;
	public final static int RANDOM_SELECTION = 1;
	public final static int RANDOM_PARAM_TUNE = 2;
	public final static int RANDOM_CREATE_DATA = 3;
	public final static int RANDOM_ALGO_INTERNAL = 4;
	/** Used for random forest random tree depth, because RANDOM_ALGO_INTERNAL may already be used somewhere else
	 * for tree ensemble induce. @author Timo Aho*/
	public final static int RANDOM_INT_RANFOR_TREE_DEPTH = 5;
	/** Used for sampling in RowData */
	public final static int RANDOM_SAMPLE = 6;

	public static Random[] m_Random;

	public static Random getRandom(int idx) {
		return m_Random[idx];
	}

	public static double nextDouble(int which) {
		return m_Random[which].nextDouble();
	}

	public static int nextInt(int which, int max) {
		return m_Random[which].nextInt(max);
	}

	public static void initialize(Settings sett) {
		m_Random = new Random[NB_RANDOM];
		if (sett.hasRandomSeed()) {
			m_IsPreset = true;
			m_Preset = sett.getRandomSeed();
			for (int i = 0; i < NB_RANDOM; i++) {
				m_Random[i] = new Random(m_Preset);
			}
		} else {
			for (int i = 0; i < NB_RANDOM; i++) {
				m_Random[i] = new Random();
			}
		}
	}

	public static void initialize(int initial) {
		m_Random = new Random[NB_RANDOM];
		for (int i = 0; i < NB_RANDOM; i++) {
			m_Random[i] = new Random(initial);
		}
	}

	public static void reset(int rnd) {
		if (m_IsPreset) {
			m_Random[rnd] = new Random(m_Preset);
		} else {
			m_Random[rnd] = new Random(m_Preset);
		}
	}
}

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

package clus.main;

import jeans.resource.*;
import jeans.io.*;

public class ClusStat {

	public static long m_PrevTime;

	public static long m_InitialMemory;
	public static long m_LoadedMemory;
	public static long m_FinalMemory;

	public static long m_TimeTest;
	public static long m_TimeSplit;
	public static long m_TimeStat;
	public static long m_TimeSort;
	public static long m_TimeHeur;

	public static double m_TTimeTest;
	public static double m_TTimeSplit;
	public static double m_TTimeStat;
	public static double m_TTimeSort;
	public static double m_TTimeHeur;

	public static double m_TTimeTotal;

	public static void initTime() {
		m_PrevTime = ResourceInfo.getCPUTime();
	}

	public static void deltaStat() {
		long now = ResourceInfo.getCPUTime();
		m_TimeStat += now - m_PrevTime;
		m_PrevTime = now;
	}

	public static void deltaTest() {
		long now = ResourceInfo.getCPUTime();
		m_TimeTest += now - m_PrevTime;
		m_PrevTime = now;
	}

	public static void deltaSplit() {
		long now = ResourceInfo.getCPUTime();
		m_TimeSplit += now - m_PrevTime;
		m_PrevTime = now;
	}

	public static void deltaSort() {
		long now = ResourceInfo.getCPUTime();
		m_TimeSort += now - m_PrevTime;
		m_PrevTime = now;
	}

	public static void deltaHeur() {
		long now = ResourceInfo.getCPUTime();
		m_TimeHeur += now - m_PrevTime;
		m_PrevTime = now;
	}

	public static void resetTimes() {
		m_TimeTest = 0;
		m_TimeSplit = 0;
		m_TimeStat = 0;
		m_TimeSort = 0;
		m_TimeHeur = 0;
	}

	public static double addToTotal(long value, int div) {
		double mean = (double)value/div;
		m_TTimeTotal += mean;
		return mean;
	}

	public static void addTimes(int div) {
		m_TTimeTest +=  (double)m_TimeTest/div;
		m_TTimeSplit += (double)m_TimeSplit/div;
		m_TTimeStat +=  (double)m_TimeStat/div;
		m_TTimeSort +=  (double)m_TimeSort/div;
		m_TTimeHeur +=  (double)m_TimeHeur/div;
	}

	public static void updateMaxMemory() {
		m_FinalMemory = Math.max(m_FinalMemory, ResourceInfo.getMemorySize());
	}

	public static void show() {
		double sum = m_TTimeStat+m_TTimeTest+m_TTimeSplit+m_TTimeSort+m_TTimeHeur;

		System.out.println("Mem usage (KB) [initial, loaded, max]: ["+m_InitialMemory+","+m_LoadedMemory+","+m_FinalMemory+"]");
		System.out.println("Total estimate: "+m_TTimeTotal);
		System.out.println("Total induction time: "+sum);
		System.out.println("Time for stats: "+m_TTimeStat);
		System.out.println("Time for evaluating: "+m_TTimeTest);
		System.out.println("Time for splitting: "+m_TTimeSplit);
		System.out.println("Time for sorting: "+m_TTimeSort);
		System.out.println("Time for heuristics: "+m_TTimeHeur);

		MyFile file = new MyFile("stats.txt");
		file.log("Mem usage (KB) [initial, loaded, max]: ["+m_InitialMemory+","+m_LoadedMemory+","+m_FinalMemory+"]");
		file.log("Total estimate: "+m_TTimeTotal);
		file.log("Total induction time: "+sum);
		file.log("Time for stats: "+m_TTimeStat);
		file.log("Time for evaluating: "+m_TTimeTest);
		file.log("Time for splitting: "+m_TTimeSplit);
		file.log("Time for sorting: "+m_TTimeSort);
		file.log("Time for heuristics: "+m_TTimeHeur);
		file.close();
	}

}


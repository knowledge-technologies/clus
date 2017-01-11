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


package clus.algo.kNN.test;

/**
 * @author Mitja Pugelj
 */

public class StopWatch {
    private double m_Start = 0;
    private double m_Aggregate = 0;

    public void start(){
    	m_Start = System.currentTimeMillis();
    }

    public void pause(){
        if( m_Start > 0 )
        	m_Aggregate += System.currentTimeMillis()-m_Start;
        m_Start = 0;
    }

    public void reset(){
    	m_Aggregate = 0;
        m_Start = 0;
    }

    public double readValue(){
        return m_Aggregate;
    }



}

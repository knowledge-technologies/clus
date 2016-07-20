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

/*
 * Created on August 20, 2009
 */
package clus.ext.hierarchical;

import java.io.*;
import java.util.ArrayList;

import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClusStatistic;

public class HierSingleLabelStat extends WHTDStatistic {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


	public HierSingleLabelStat(ClassHierarchy hier, int comp) {
		super(hier, comp);
	}

	public HierSingleLabelStat(ClassHierarchy hier, boolean onlymean, int comp) {
		super(hier, onlymean, comp);
	}

	public ClusStatistic cloneStat() {
		return new HierSingleLabelStat(m_Hier, false, m_Compatibility);
	}

	
	
	public void addPredictWriterSchema(String prefix, ClusSchema schema) {
		float biggest = 0;
		int prediction = 0;
		int count = 0;
		
		ClassHierarchy hier = getHier();
		ArrayList leafClasses = new ArrayList();
		for (int i = 0; i < m_NbAttrs; i++) {
			ClassTerm term = hier.getTermAt(i);
			if(term.getNbChildren()==0){
				leafClasses.add(term.toStringHuman(hier));
			}	
		}
		
		 
		
		//ClassTerm term = hier.getTermAt(prediction);
		//type.setName(prefix+"-p-"+term.toStringHuman(hier));

		
		ClusAttrType type = new NominalAttrType(prefix+"-class", leafClasses);
		schema.addAttrType(type);
		
	}
	
	public String getPredictWriterString() {
		double biggest = 0;
		int prediction = -1;
		
		StringBuffer buf = new StringBuffer();
		
		ClassHierarchy hier = getHier();
		
		for (int i = 0; i < m_NbAttrs; i++) {
			ClassTerm term = hier.getTermAt(i);
			
			if(term.getNbChildren()==0){
			
				if(prediction == -1){
					prediction = i;
					biggest = m_Means[i];
				}else {
					if(m_Means[i]>biggest){
						prediction=i;
						biggest = m_Means[i]; 
					}
				}
			}
			
		}
		

		ClassTerm term = hier.getTermAt(prediction);
		return term.toStringHuman(hier);
	}
}
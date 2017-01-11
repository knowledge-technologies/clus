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
 * Created on Jul 22, 2005
 */
package clus.error;

import clus.data.type.*;
import clus.main.Settings;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;

public class MisclassificationError extends Accuracy {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public MisclassificationError(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
	}

	public boolean shouldBeLow() {
		return true;
	}

	public double getModelErrorComponent(int i) {
		return 1.0 - ((double)m_NbCorrect[i]) / getNbExamples();
	}

	public String getName() {
		return "Misclassification error";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MisclassificationError(par, m_Attrs);
	}

	public double computeLeafError(ClusStatistic stat) {
		ClassificationStat cstat = (ClassificationStat)stat;
		return cstat.getError(null) * cstat.getNbAttributes();
	}
}

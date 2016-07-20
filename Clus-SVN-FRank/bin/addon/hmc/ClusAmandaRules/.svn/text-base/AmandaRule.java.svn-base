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

package addon.hmc.ClusAmandaRules;

/*
 * Created on Jan 2, 2006
 */

import clus.algo.tdidt.*;
import clus.algo.rules.*;
import clus.data.rows.*;
import clus.main.*;
import clus.model.test.*;

public class AmandaRule extends ClusRule {

  public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

  public AmandaRule(ClusStatManager statManager) {
    super(statManager);
  }

  public boolean doTest(NodeTest test, DataTuple tuple) {
  ///Leander: gans eerste if-test geblokkeerd
	  /*if (test.isUnknown(tuple)) {
			// Amanda does someting weird with unknown values
			// (because of the perl implementation of > and <=)
			if (test instanceof InverseNumericTest) {
			  // <= succeeds for ? <= 26
			  // <= fails for ? <= -26
				if (!(0.0 <= ((NumericTest)test).getBound())) return false;
			} else if (test instanceof NumericTest) {
				// > fails for ? > 26
			  // > succeeds for ? > -26
				if (!(0.0 > ((NumericTest)test).getBound())) return false;
			} else {
				return false;
			}
		} else {*/
			if (test.predictWeighted(tuple) != ClusNode.YES) return false;
		//}
		return true;
  }

	public boolean covers(DataTuple tuple) {
		for (int i = 0; i < getModelSize(); i++) {
			NodeTest test = getTest(i);
			if (!doTest(test, tuple)) return false;
		}
		return true;
	}
}

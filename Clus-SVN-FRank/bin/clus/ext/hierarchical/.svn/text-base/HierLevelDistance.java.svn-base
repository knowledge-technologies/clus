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

package clus.ext.hierarchical;

public class HierLevelDistance implements HierBasicDistance {

	public double getVirtualRootWeight() {
		return 0.0;
	}

	// First argument = actual, second = predicted
	public double calcDistance(ClassTerm t1, ClassTerm t2) {
		int d1 = t1.getLevel();
		int d2 = t2.getLevel();
		int d_correct = d1;
		int com_d = Math.min(d1, d2);
		double distance = (double)d2-d1;
		while (d1 > com_d) {
			t1 = t1.getCTParent();
			d1--;
		}
		while (d2 > com_d) {
			t2 = t2.getCTParent();
			d2--;
		}
		while (com_d >= 0) {
			if (t1 != t2) distance = (double)com_d-d_correct-1;
			t1 = t1.getCTParent();
			t2 = t2.getCTParent();
			com_d--;
		}
		return distance;
	}
}

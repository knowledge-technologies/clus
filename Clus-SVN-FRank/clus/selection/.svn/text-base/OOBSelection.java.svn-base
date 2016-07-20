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

package clus.selection;

public class OOBSelection extends ClusSelection{

	protected int[] m_OOBCounts;
	protected int m_OOBNbSel;

	public OOBSelection(BaggingSelection bsel) {
		super(bsel.getNbRows());
		m_OOBCounts = new int[bsel.getNbRows()];
		m_OOBNbSel = 0;
		for (int i = 0; i < bsel.getNbRows(); i++)
			if (bsel.getWeight(i) == 0){//not in the bag
				m_OOBCounts[i] = 1;
				m_OOBNbSel ++;
			}
		if (m_OOBNbSel != (bsel.getNbRows()-bsel.getNbSelected())){//check
			System.err.println(this.getClass().getName()+": Error while creating the OOB");
		}
	}

	public boolean changesDistribution() {
		return true;
	}

	public double getWeight(int row) {
		return (double)m_OOBCounts[row];
	}

	public int getNbSelected() {
		return m_OOBNbSel;
	}

	public boolean isSelected(int row) {
		return m_OOBCounts[row] != 0;
	}

	public final int getCount(int row) {
		return m_OOBCounts[row];
	}

	public void addToThis(OOBSelection other){
		for (int i = 0; i < this.m_OOBCounts.length; i++){
			if ((this.getWeight(i) == 0)&&(other.getWeight(i) == 1)){
				this.m_OOBCounts[i] = 1;
				this.m_OOBNbSel++;
			}
		}
	}

	public void addToOther(OOBSelection other){
		for (int i = 0; i < this.m_OOBCounts.length; i++){
			if ((this.getWeight(i) == 1)&&(other.getWeight(i) == 0)){
				other.m_OOBCounts[i] = 1;
				other.m_OOBNbSel++;
			}
		}
	}

}

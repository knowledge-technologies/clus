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

public class Global {
	public static int itemsetcpt;
	public static int treecpt;

	public static int get_itemsetcpt() {
		return itemsetcpt;
	}

	public static void set_itemsetcpt(int i) {
		//System.out.println("setting itemsetcpt :"+i);
		itemsetcpt=i;
	}

	public static void inc_itemsetcpt() {
		int i = get_itemsetcpt();
		//System.out.println(text+" before increment :"+i);
		i++;
		set_itemsetcpt(i);
		//System.out.println(text+" after increment :"+get_itemsetcpt());
	}


	public static int get_treecpt() {
		return treecpt;
	}

	public static void set_treecpt(int i) {
		//System.out.println("setting itemsetcpt :"+i);
		treecpt=i;
	}

	public static void inc_treecpt() {
		int i = get_treecpt();
		//System.out.println(text+" before increment :"+i);
		i++;
		set_treecpt(i);
		//System.out.println(text+" after increment :"+get_itemsetcpt());
	}

    /**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
*/
}

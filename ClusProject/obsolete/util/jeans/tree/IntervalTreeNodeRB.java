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

package si.ijs.kt.clus.util.jeans.tree;

public class IntervalTreeNodeRB {

    public boolean red;
    public int key, high, maxHigh, value;
    public IntervalTreeNodeRB left, right, parent;


    public IntervalTreeNodeRB() {
    }


    public IntervalTreeNodeRB(int min, int max, int value) {
        this.key = min;
        this.high = this.maxHigh = max;
        this.value = value;
    }


    public void print(IntervalTreeNodeRB nil, IntervalTreeNodeRB root) {
        ClusLogger.info("k = " + key + " h = " + high + " maxhigh = " + maxHigh);
        System.out.print("l.key = ");
        if (left == nil)
            ClusLogger.info("NULL");
        else
            ClusLogger.info(left.key);
        System.out.print("r.key = ");
        if (right == nil)
            ClusLogger.info("NULL");
        else
            ClusLogger.info(right.key);
        System.out.print("p.key = ");
        if (parent == root)
            ClusLogger.info("NULL");
        else
            ClusLogger.info(parent.key);
    }


    public void print() {
        ClusLogger.info("[" + key + "," + high + " (" + maxHigh + ")]: " + value);
    }


    public void setMinMax(int value) {
        key = high = maxHigh = value;
    }


    public void setAllNodes(IntervalTreeNodeRB node) {
        left = right = parent = node;
    }
}

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

import java.util.Vector;


public class SimpleNode implements Node {

    protected Vector nodes = new Vector();
    protected Node parent;


    public SimpleNode() {
        this(null);
    }


    public SimpleNode(Node parent) {
        super();
        setParent(parent);
    }


    @Override
    public int getLevel() {
        return 0;
    }


    @Override
    public void addChild(Node node) {
        node.setParent(this);
        nodes.addElement(node);
    }


    @Override
    public void removeChild(Node node) {
        node.setParent(null);
        nodes.removeElement(node);
    }


    @Override
    public Node getParent() {
        return parent;
    }


    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }


    @Override
    public Node getChild(int idx) {
        return (Node) nodes.elementAt(idx);
    }


    @Override
    public int getNbChildren() {
        return nodes.size();
    }


    @Override
    public boolean atTopLevel() {
        return getParent() == null;
    }


    @Override
    public boolean atBottomLevel() {
        return getNbChildren() == 0;
    }

}

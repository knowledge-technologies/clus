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

package jeans.tree;

import java.util.*;
import jeans.util.*;

public class IntervalTreeRB {

	public IntervalTreeNodeRB root;
	public IntervalTreeNodeRB nil;
	public int min, max, maxval;
	public Executer executer;

	public IntervalTreeRB() {
		nil = new IntervalTreeNodeRB();
		nil.setMinMax(Integer.MIN_VALUE);
		nil.setAllNodes(nil);
		root = new IntervalTreeNodeRB();
		root.setMinMax(Integer.MAX_VALUE);
		root.setAllNodes(nil);
	}

/************************************************************************
 * FUNCTION: LeftRotate                                                 *
 * INPUTS:   the node to rotate on                                      *
 * OUTPUT:   None                                                       *
 * EFFECTS:  Rotates as described in _Introduction_To_Algorithms by     *
 *           Cormen, Leiserson, Rivest (Chapter 14).  Basically this    *
 *           makes the parent of x be to the left of x, x the parent of *
 *           its parent before the rotation and fixes other pointers    *
 *           accordingly. Also updates the maxHigh fields of x and y    *
 *           after rotation.                                            *
 ************************************************************************/

	public void leftRotate(IntervalTreeNodeRB x) {
		IntervalTreeNodeRB y;
		/* I originally wrote this function to use the sentinel for */
		/* nil to avoid checking for nil.  However this introduces a */
		/* very subtle bug because sometimes this function modifies */
		/* the parent pointer of nil.  This can be a problem if a */
		/* function which calls LeftRotate also uses the nil sentinel */
		/* and expects the nil sentinel's parent pointer to be unchanged */
		/* after calling this function.  For example, when DeleteFixUP */
		/* calls LeftRotate it expects the parent pointer of nil to be */
		/* unchanged. */
		y=x.right;
		x.right=y.left;
		if (y.left != nil) y.left.parent=x; /* used to use sentinel here */
		/* and do an unconditional assignment instead of testing for nil */
		y.parent=x.parent;
		/* instead of checking if x.parent is the root as in the book, we */
		/* count on the root sentinel to implicitly take care of this case */
		if (x == x.parent.left) {
			x.parent.left=y;
		} else {
			x.parent.right=y;
		}
		y.left=x;
		x.parent=y;
		x.maxHigh=Math.max(x.left.maxHigh,Math.max(x.right.maxHigh,x.high));
		y.maxHigh=Math.max(x.maxHigh,Math.max(y.right.maxHigh,y.high));
	}

/************************************************************************
 * FUNCTION: RighttRotate                                               *
 * INPUTS:   node to rotate on                                          *
 * OUTPUT:   None                                                       *
 * EFFECTS:  Rotates as described in _Introduction_To_Algorithms by     *
 *           Cormen, Leiserson, Rivest (Chapter 14).  Basically this    *
 *           makes the parent of x be to the left of x, x the parent of *
 *           its parent before the rotation and fixes other pointers    *
 *           accordingly. Also updates the maxHigh fields of x and y    *
 *           after rotation.                                            *
 ************************************************************************/

	public void rightRotate(IntervalTreeNodeRB y) {
		IntervalTreeNodeRB x;
		/* I originally wrote this function to use the sentinel for */
		/* nil to avoid checking for nil.  However this introduces a */
		/* very subtle bug because sometimes this function modifies */
		/* the parent pointer of nil.  This can be a problem if a */
		/* function which calls LeftRotate also uses the nil sentinel */
		/* and expects the nil sentinel's parent pointer to be unchanged */
		/* after calling this function.  For example, when DeleteFixUP */
		/* calls LeftRotate it expects the parent pointer of nil to be */
		/* unchanged. */
		x=y.left;
		y.left=x.right;
		if (nil != x.right)  x.right.parent=y; /*used to use sentinel here */
		/* and do an unconditional assignment instead of testing for nil */
		/* instead of checking if x.parent is the root as in the book, we */
		/* count on the root sentinel to implicitly take care of this case */
		x.parent=y.parent;
		if (y == y.parent.left) {
			y.parent.left=x;
		} else {
			y.parent.right=x;
		}
		x.right=y;
		y.parent=x;
		y.maxHigh=Math.max(y.left.maxHigh,Math.max(y.right.maxHigh,y.high));
		x.maxHigh=Math.max(x.left.maxHigh,Math.max(y.maxHigh,x.high));
	}

/****************************************************************************
 *  FUNCTION: TreeInsertHelp                                                *
 *  INPUTS:   z is the node to insert                                       *
 *  OUTPUT:   none                                                          *
 *  EFFECTS:  Inserts z into the tree as if it were a regular binary tree   *
 *            using the algorithm described in _Introduction_To_Algorithms_ *
 *            by Cormen et al.  This funciton is only intended to be called *
 *            by the InsertTree function and not by the user                *
 ****************************************************************************/

	public void treeInsertHelp(IntervalTreeNodeRB z) {
		/* This function should only be called by InsertITTree (see above) */
		IntervalTreeNodeRB x;
		IntervalTreeNodeRB y;
		z.left=z.right=nil;
		y=root;
		x=root.left;
		while( x != nil) {
			y=x;
			if ( x.key > z.key) {
				x=x.left;
			} else { /* x.key <= z.key */
				x=x.right;
			}
		}
		z.parent=y;
		if ( (y == root) ||
		     (y.key > z.key) ) {
			y.left=z;
		} else {
			y.right=z;
		}
	}

/*********************************************************************
 *  FUNCTION: FixUpMaxHigh                                           *
 *  INPUTS:   x is the node to start from                            *
 *  OUTPUT:   none                                                   *
 *  EFFECTS:  Travels up to the root fixing the maxHigh fields after *
 *            an insertion or deletion                               *
 *********************************************************************/

	public void fixUpMaxHigh(IntervalTreeNodeRB x) {
		while(x != root) {
			x.maxHigh=Math.max(x.high,Math.max(x.left.maxHigh,x.right.maxHigh));
			x=x.parent;
		}
	}

/***************************************************************************
 *  FUNCTION: InsertNode                                                   *
 *  INPUTS:   newInterval is the interval to insert                        *
 *  OUTPUT:   This function returns a pointer to the newly inserted node   *
 *            which is guarunteed to be valid until this node is deleted.  *
 *            What this means is if another data structure stores this     *
 *            pointer then the tree does not need to be searched when this *
 *            is to be deleted.                                            *
 *  EFFECTS:  Creates a node node which contains the appropriate key and   *
 *            info pointers and inserts it into the tree.                  *
 ***************************************************************************/

	public IntervalTreeNodeRB insert(int min, int max, int value) {
		IntervalTreeNodeRB y;
		IntervalTreeNodeRB x;
		IntervalTreeNodeRB newNode;
		x = new IntervalTreeNodeRB(min, max, value);
		treeInsertHelp(x);
		fixUpMaxHigh(x.parent);
		newNode = x;
		x.red=true;
		while(x.parent.red) { /* use sentinel instead of checking for root */
			if (x.parent == x.parent.parent.left) {
				y=x.parent.parent.right;
				if (y.red) {
					x.parent.red=false;
					y.red=false;
					x.parent.parent.red=true;
					x=x.parent.parent;
				} else {
					if (x == x.parent.right) {
						x=x.parent;
						leftRotate(x);
					}
					x.parent.red=false;
					x.parent.parent.red=true;
					rightRotate(x.parent.parent);
				}
			} else {
				/* case for x.parent == x.parent.parent.right */
				/* this part is just like the section above with */
				/* left and right interchanged */
				y=x.parent.parent.left;
				if (y.red) {
					x.parent.red=false;
					y.red=false;
					x.parent.parent.red=true;
					x=x.parent.parent;
				} else {
					if (x == x.parent.left) {
						x=x.parent;
						rightRotate(x);
					}
					x.parent.red=false;
					x.parent.parent.red=true;
					leftRotate(x.parent.parent);
				}
			}
		}
		root.left.red=false;
		return newNode;
	}

/********************************************************************
 * FUNCTION: GetSuccessorOf                                         *
 * INPUTS:   x is the node we want the succesor of                  *
 * OUTPUT:   This function returns the successor of x or null if no *
 *           successor exists.                                      *
 * Note:     uses the algorithm in _Introduction_To_Algorithms_     *
 ********************************************************************/

	IntervalTreeNodeRB getSuccessorOf(IntervalTreeNodeRB x) {
		IntervalTreeNodeRB y;
		if (nil != (y = x.right)) { /* assignment to y is intentional */
			while(y.left != nil) { /* returns the minium of the right subtree of x */
				y=y.left;
			}
			return y;
		} else {
			y=x.parent;
			while(x == y.right) { /* sentinel used instead of checking for nil */
				x=y;
				y=y.parent;
			}
			if (y == root) return nil;
			return y;
		}
	}

/**********************************************************************
 * FUNCTION: GetPredecessorOf                                         *
 * INPUTS:   x is the node to get predecessor of                      *
 * OUTPUT:   This function returns the predecessor of x or null if no *
 *           predecessor exists.                                      *
 * Note:     uses the algorithm in _Introduction_To_Algorithms_       *
 **********************************************************************/

	IntervalTreeNodeRB getPredecessorOf(IntervalTreeNodeRB x) {
		IntervalTreeNodeRB y;
		if (nil != (y = x.left)) { /* assignment to y is intentional */
			while(y.right != nil) { /* returns the maximum of the left subtree of x */
				y=y.right;
			}
			return y;
		} else {
			y=x.parent;
			while(x == y.left) {
				if (y == root) return nil;
				x=y;
				y=y.parent;
			}
			return y;
		}
	}

/*************************************************************************
 * FUNCTION: DeleteFixUp                                                 *
 * INPUTS:   x is the child of the spliced                               *
 *           out node in DeleteNode.                                     *
 * EFFECT:   Performs rotations and changes colors to restore red-black  *
 *           properties after a node is deleted                          *
 * The algorithm from this function is from _Introduction_To_Algorithms_ *
 *************************************************************************/

	public void deleteFixUp(IntervalTreeNodeRB x) {
		IntervalTreeNodeRB w;
		IntervalTreeNodeRB rootLeft = root.left;
		while ((!x.red) && (rootLeft != x)) {
			if (x == x.parent.left) {
				w=x.parent.right;
				if (w.red) {
					w.red=false;
					x.parent.red=true;
					leftRotate(x.parent);
					w=x.parent.right;
				}
				if ( (!w.right.red) && (!w.left.red) ) {
					w.red=true;
					x=x.parent;
				} else {
					if (!w.right.red) {
						w.left.red=false;
						w.red=true;
						rightRotate(w);
						w=x.parent.right;
					}
					w.red=x.parent.red;
					x.parent.red=false;
					w.right.red=false;
					leftRotate(x.parent);
					x=rootLeft; /* this is to exit while loop */
				}
			} else { /* the code below is has left and right switched from above */
				w=x.parent.left;
				if (w.red) {
					w.red=false;
					x.parent.red=true;
					rightRotate(x.parent);
					w=x.parent.left;
				}
				if ( (!w.right.red) && (!w.left.red) ) {
					w.red=true;
					x=x.parent;
				} else {
					if (!w.left.red) {
						w.right.red=false;
						w.red=true;
						leftRotate(w);
						w=x.parent.left;
					}
					w.red=x.parent.red;
					x.parent.red=false;
					w.left.red=false;
					rightRotate(x.parent);
					x=rootLeft; /* this is to exit while loop */
				}
			}
		}
		x.red=false;
	}

/*************************************************************************
 * FUNCTION: DeleteNode                                                  *
 * INPUTS:   tree is the tree to delete node z from                      *
 * OUTPUT:   returns the Interval stored at deleted node                 *
 * EFFECT:   Deletes z from tree and but don't call destructor           *
 *           Then calls FixUpMaxHigh to fix maxHigh fields then calls    *
 *           ITDeleteFixUp to restore red-black properties               *
 * The algorithm from this function is from _Introduction_To_Algorithms_ *
 *************************************************************************/

	public void deleteNode(IntervalTreeNodeRB z) {
		IntervalTreeNodeRB y;
		IntervalTreeNodeRB x;
		y= ((z.left == nil) || (z.right == nil)) ? z : getSuccessorOf(z);
		x= (y.left == nil) ? y.right : y.left;
		if (root == (x.parent = y.parent)) { /* assignment of y.p to x.p is intentional */
			root.left=x;
		} else {
			if (y == y.parent.left) {
				y.parent.left=x;
			} else {
				y.parent.right=x;
			}
		}
		if (y != z) { /* y should not be nil in this case */
			/* y is the node to splice out and x is its child */
			y.maxHigh = Integer.MIN_VALUE;
			y.left=z.left;
			y.right=z.right;
			y.parent=z.parent;
			z.left.parent=z.right.parent=y;
			if (z == z.parent.left) {
				z.parent.left=y;
			} else {
				z.parent.right=y;
			}
			fixUpMaxHigh(x.parent);
			if (!(y.red)) {
				y.red = z.red;
				deleteFixUp(x);
			} else {
				y.red = z.red;
			}
		} else {
			fixUpMaxHigh(x.parent);
			if (!(y.red)) deleteFixUp(x);
		}
	}


/***********************************************************************
 * FUNCTION: Overlap                                                   *
 * INPUTS:   [a1,a2] and [b1,b2] are the low and high endpoints of two *
 *           closed intervals.                                         *
 * EFFECT:  returns 1 if the intervals overlap, and 0 otherwise        *
 ***********************************************************************/

	static boolean overlap(int a1, int a2, int b1, int b2) {
		if (a1 <= b1) {
			return( (b1 <= a2) );
		} else {
			return( (a1 <= b2) );
		}
	}

/********************************************************************
 * FUNCTION: Print                                                  *
 * EFFECTS:  This function recursively prints the nodes of the tree *
 *           inorder.                                               *
 ********************************************************************/

	public void print(IntervalTreeNodeRB x) {
		if (x != nil) {
			print(x.left);
			x.print();
			print(x.right);
		}
	}

	public void print() {
		print(root.left);
	}

/****************************************************************************
 *  function from the book and modify to find all overlapping intervals     *
 *  instead of just one.  This means that any time we take the left         *
 *  branch down the tree we must also check the right branch if and only if *
 *  we find an overlapping interval in that left branch.  Note this is a    *
 *  recursive condition because if we go left at the root then go left      *
 *  again at the first left child and find an overlap in the left subtree   *
 *  of the left child of root we must recursively check the right subtree   *
 *  of the left child of root as well as the right child of root.           *
 ****************************************************************************/

	public boolean findOverlappingIntervals(IntervalTreeNodeRB x, ArrayList list) {
		if (x != nil) {
			if (overlap(min,max,x.key,x.high)) {
				list.add(x);
				if (x.left.maxHigh >= min) {
					if (findOverlappingIntervals(x.left, list)) {
						findOverlappingIntervals(x.right, list);
					}
				} else {
					findOverlappingIntervals(x.right, list);
				}
				return true;
			} else {
				if (x.left.maxHigh >= min) {
					if (findOverlappingIntervals(x.left, list)) {
						findOverlappingIntervals(x.right, list);
						return true;
					} else {
						return false;
					}
				} else {
					return findOverlappingIntervals(x.right, list);
				}
			}
		} else {
			return false;
		}
	}

	public ArrayList findOverlappingIntervals(int min, int max) {
		this.min = min;
		this.max = max;
		ArrayList list = new ArrayList();
		findOverlappingIntervals(root.left, list);
		return list;
	}

	public boolean findOverlappingIntervalsMax(IntervalTreeNodeRB x) {
		if (x != nil) {
			if (overlap(min,max,x.key,x.high)) {
				if (x.value > maxval) {
					maxval = x.value;
				}
				if (x.left.maxHigh >= min) {
					if (findOverlappingIntervalsMax(x.left)) {
						findOverlappingIntervalsMax(x.right);
					}
				} else {
					findOverlappingIntervalsMax(x.right);
				}
				return true;
			} else {
				if (x.left.maxHigh >= min) {
					if (findOverlappingIntervalsMax(x.left)) {
						findOverlappingIntervalsMax(x.right);
						return true;
					} else {
						return false;
					}
				} else {
					return findOverlappingIntervalsMax(x.right);
				}
			}
		} else {
			return false;
		}
	}

	public int findOverlappingIntervalsMax(int min, int max) {
		this.min = min;
		this.max = max;
		this.maxval = Integer.MIN_VALUE;
		findOverlappingIntervalsMax(root.left);
		return this.maxval;
	}

	public int findOverlappingIntervalsMax(int min, int max, int init) {
		this.min = min;
		this.max = max;
		this.maxval = init;
		findOverlappingIntervalsMax(root.left);
		return this.maxval;
	}

	public void findMaxRecursive(IntervalTreeNodeRB x) {
		if (x != nil) {
			if (x.value > maxval) {
				maxval = x.value;
			}
			findMaxRecursive(x.left);
			findMaxRecursive(x.right);
		}
	}

	public int findMax(int init) {
		this.maxval = init;
		findMaxRecursive(root.left);
		return this.maxval;
	}

	public void executeRecursive(IntervalTreeNodeRB x) {
		if (x != nil) {
			executer.execute(x);
			executeRecursive(x.left);
			executeRecursive(x.right);
		}
	}

	public void execute(Executer exec) {
		this.executer = exec;
		executeRecursive(root.left);
	}

	public IntervalTreeNodeRB findOverlappingInterval(int min, int max) {
		IntervalTreeNodeRB x = root.left;
		while (x != nil && overlap(min,max,x.key,x.high) == false) {
			if (x.left.maxHigh >= min) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		return x;
	}

	public void addInterval(int min, int max, int value) {
		IntervalTreeNodeRB x = findOverlappingInterval(min, max);
		while (x != nil) {
			int xmin = x.key;
			int xmax = x.high;
			int xvalue = x.value;
			deleteNode(x);
			if (xmin < min) {
				insert(xmin, min-1, xvalue);
			}
			if (xmax > max) {
				insert(max+1, xmax, xvalue);
			}
			x = findOverlappingInterval(min, max);
		}
		insert(min, max, value);
	}

	public int checkMaxHighFieldsHelper(IntervalTreeNodeRB y, int currentHigh, int match) {
		if (y != nil) {
			match = checkMaxHighFieldsHelper(y.left,currentHigh,match) != 0 ? 1 : match;
			VERIFY("y.high <= currentHigh", y.high <= currentHigh);
			if (y.high == currentHigh) match = 1;
			match = checkMaxHighFieldsHelper(y.right,currentHigh,match) != 0 ? 1 : match;
		}
		return match;
	}

	/* Make sure the maxHigh fields for everything makes sense.
	 * If something is wrong, print a warning and exit */
	public void checkMaxHighFields(IntervalTreeNodeRB x) {
		if (x != nil) {
			checkMaxHighFields(x.left);
			if(!(checkMaxHighFieldsHelper(x,x.maxHigh,0) > 0)) {
				System.out.println("Error found in CheckMaxHighFields.");
			}
			checkMaxHighFields(x.right);
		}
	}

	public void VERIFY(String descr, boolean val) {
		if (!val) {
			System.out.println("Assertion: '"+descr+"' fails.");
		}
	}

	public void checkAssumptions() {
		VERIFY("nil.key == Integer.MIN_VALUE", nil.key == Integer.MIN_VALUE);
		VERIFY("nil.high == Integer.MIN_VALUE", nil.high == Integer.MIN_VALUE);
		VERIFY("nil.maxHigh == Integer.MIN_VALUE", nil.maxHigh == Integer.MIN_VALUE);
		VERIFY("root.key == Integer.MAX_VALUE", root.key == Integer.MAX_VALUE);
		VERIFY("root.high == Integer.MAX_VALUE", root.high == Integer.MAX_VALUE);
		VERIFY("root.maxHigh == Integer.MAX_VALUE", root.maxHigh == Integer.MAX_VALUE);
		VERIFY("nil.red == 0", nil.red == false);
		VERIFY("root.red == 0", root.red == false);
		checkMaxHighFields(root.left);
	}
}

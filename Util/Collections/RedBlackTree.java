// RedBlackTree.java, created Tue Jul  24 11:46:23 2001 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Util;

import java.util.Comparator;

/** A <code>RedBlackTree</code> is a <code>BinaryTree</code> that uses
    red-black properties to maintain a balanced form.
    
    @see "CLR section 14, (page 263).  
*/
public class RedBlackTree extends BinaryTree {  
    // red black properties
    // 1. Every node is either red or black
    // 2. Every leaf (nil) is black
    // 3. If a node is red, then both its children are black
    // 4. Every simple path from a node to a descendant leaf contains
    // the same number of black nodes.

    private static final boolean DEBUG = false;
    private void checkRep() { if (DEBUG) checkTree(root()); }
    private void checkTree(Node n) {
	if (n == NIL) 
	    Util.ASSERT(color(n) == BLACK, "(2) viol.");
	if (color(n) == RED) { // (3)
	    Util.ASSERT(color(n.left()) == BLACK, "(3) left viol. on "+n);
	    Util.ASSERT(color(n.right()) == BLACK, "(3) right viol. on "+n);
	}
	if (n != NIL) { 
	    checkTree(n.left());
	    checkTree(n.right());
	}
    }

    private static final int RED = 0;
    private static final int BLACK = 1;

    /** A <code>RBNode</code> is an element of this tree.
     */
    protected class RBNode extends Node {
	private int color = BLACK;
	protected RBNode(Object key) { super(key); }
	public String toString() {
	    return super.toString()+" col:"+ ((color ==BLACK)?"b":"r");
	}
    }
    
    /** Factory method for <code>Node</code>.  Every construction of a
	<code>Node</code> takes place through this method; thus
	subclasses of <code>RedBlackTree</code> can associate new data
	with their own nodes by extending the <code>Node</code> class
	and overriding this method. */
    protected Node makeNode(Object o) { return new RBNode(o); }

    public RedBlackTree() { super(); }
    public RedBlackTree(Comparator c) { super(c); }

    // helper method to clear out casts in code.  I can't wait for GJ
    // so I'll be able to get around this in better ways.
    private int color(Node n) { 
	if (n == NIL)
	    return BLACK; // see r.b. property (2) above.
	else 
	    return ((RBNode)n).color; 
    }
    private void setColor(Node n, int c) { ((RBNode)n).color = c; }

    protected void insertNode(Node x) {
	checkRep();
	super.insertNode(x);
	setColor(x, RED);
	while(x != root() && color(x.parent()) == RED) {
	    if (x.parent() == x.parent().parent().left()) {
		Node y = x.parent().parent().right();
		if (color(y) == RED) {
		    setColor(x.parent(), BLACK);
		    setColor(y, BLACK);
		    setColor(x.parent().parent(), RED);
		    x = x.parent().parent();
		} else {
		    if (x == x.parent().right()) {
			x = x.parent();
			leftRotate(x);
		    }
		    setColor(x.parent(), BLACK);
		    setColor(x.parent().parent(), RED);
		    rightRotate(x.parent().parent());
		}
	    } else {
		Node y = x.parent().parent().left();
		if (color(y) == RED) {
		    setColor(x.parent(), BLACK);
		    setColor(y, BLACK);
		    setColor(x.parent().parent(), RED);
		    x = x.parent().parent();
		} else {
		    if (x == x.parent().left()) {
			x = x.parent();
			rightRotate(x);
		    }
		    setColor(x.parent(), BLACK);
		    setColor(x.parent().parent(), RED);
		    leftRotate(x.parent().parent());
		}
	    }
	}
	setColor(root(), BLACK);

	checkRep();
    }

    protected void swapPositions(Node a, Node b) {
	super.swapPositions(a,b);
	int c = color(a);
	setColor(a, color(b));
	setColor(b, c);
    }
    protected void deleteNode(Node z) {
	checkRep();

	Node x, y;
	if (z.left() == NIL || z.right() == NIL) 
	    y = z;
	else
	    y = successor(z);

	if (y.left() != NIL)
	    x = y.left();
	else 
	    x = y.right();
	
	setParent(x, y.parent());

	if (y.parent() == NIL)
	    setRoot(x);
	else if (y == y.parent().left())
	    setLeft(y.parent(), x);
	else
	    setRight(y.parent(), x);
	
	// System.out.println(" x:"+x+" y:"+y+" z:"+z);
	if (y != z) {
	    swapPositions(y,z);
	}
	
	// sadly, below are the only significant differences versus
	// the superclass implementation.  But I can't find a clean
	// way to communicate the values for x and y between the two
	// procedures without making the whole thing an obvious hack.
	if (color(z) == BLACK) // we swap above, so z's col instead of y's col 
	    rbDeleteFixup(x);

	// System.out.println(dump());

	checkRep();
    }
    
    /** Post delete fixup routine.  
	@see CLR, pg. 274
    */
    protected void rbDeleteFixup(Node x) {
	while(x != root() && color(x) == BLACK) {
	    if (x == x.parent().left()) {
		Node w = x.parent().right();
		Util.ASSERT(w != NIL);
		if (color(w) == RED) {
		    setColor(w, BLACK);
		    setColor(x.parent(), RED);
		    leftRotate(x.parent());
		    w = x.parent().right();
		}
		if (color(w.left()) == BLACK && color(w.right()) == BLACK) {
		    setColor(w, RED);
		    x = x.parent();
		} else {
		    if (color(w.right()) == BLACK) {
			setColor(w.left(), BLACK);
			setColor(w, RED);
			rightRotate(w);
			w = x.parent().right();
		    }
		    setColor(w, color(x.parent()));
		    setColor(x.parent(), BLACK);
		    setColor(w.right(), BLACK);
		    leftRotate(x.parent());
		    x = root();
		}
	    } else {
		Node w = x.parent().left();
		if (color(w) == RED) {
		    setColor(w, BLACK);
		    setColor(x.parent(), RED);
		    rightRotate(x.parent());
		    w = x.parent().left();
		}
		if (color(w.right()) == BLACK && color(w.left()) == BLACK) {
		    setColor(w, RED);
		    x = x.parent();
		} else {
		    if (color(w.left()) == BLACK) {
			setColor(w.right(), BLACK);
			setColor(w, RED);
			leftRotate(w);
			w = x.parent().left();
		    }
		    setColor(w, color(x.parent()));
		    setColor(x.parent(), BLACK);
		    setColor(w.left(), BLACK);
		    rightRotate(x.parent());
		    x = root();
		}
	    }
	}
	setColor(x, BLACK);
    }

    /** Pivots around the edge (x,x.right). 
	Assumes x.right != null.
    */
    protected void leftRotate(Node x) {
	Node y = x.right();
	setRight(x, y.left());
	if (y.left() != NIL) {
	    setParent(y.left(), x);
	}
	setParent(y, x.parent());
	if (x.parent() == NIL) {
	    setRoot(y);
	} else {
	    if (x == x.parent().left()) {
		setLeft(x.parent(), y);
	    } else {
		setRight(x.parent(), y);
	    }
	}
	setLeft(y, x);
	setParent(x, y);
    }

    /** Pivots around the edge (x,x.left). 
	Assumes x.left != NIL.
    */
    protected void rightRotate(Node x) {
	Node y = x.left();
	setLeft(x, y.right());
	if (y.right() != NIL) {
	    setParent(y.right(), x);
	}
	setParent(y, x.parent());
	if (x.parent() == NIL) {
	    setRoot(y);
	} else {
	    if (x == x.parent().right()) {
		setRight(x.parent(), y);
	    } else {
		setLeft(x.parent(), y);
	    }
	}
	setRight(y, x);
	setParent(x, y);
    }

    public static void main(String[] args) { (new RedBlackTree()).test(); }
}

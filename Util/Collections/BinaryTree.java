// BinaryTree.java, created Tue Jul  24 11:46:23 2001 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.Comparator;

/** A <code>BinaryTree</code> is a <code>Tree</code> where each node has
    at most two children.
    
    <p> All elements of a given <code>BinaryTree</code> <i>t</i> must
    be mutually comparable, either inherently or through an external
    <code>Comparator</code>.
    
    <p><i>Unlike</i> a <code>TreeSet</code>, duplicate elements
    <b>are</b> allowed in a <code>BinaryTree</code>.
    
    <p> FSK: We should probably have a Tree interface by
    now... Sometimes you want to expose the fact that you're working
    with a Tree, instead of abstract it into a Set or what-have-you.
    Have to think about adding that.
    
    @see "CLR section 13, (page 244).  
*/
public class BinaryTree {
    
    /** A <code>Node</code> is an element of this tree.
     */
    public class Node {
	public final Object key;
	private Node left; 
	private Node right; 
	private Node parent; // called 'p' in CLR.  NIL if this is root. 
	
	protected final Node left() { return left; }
	protected final Node right() { return right; }
	protected final Node parent(){ return parent; }

	/** Can only be called by makeNode(Object) 
	    or by Node subclass constructors.
	 */
	protected Node(Object k) { 
	    this.key = k; 
	    left = right = parent = NIL;
	}

	// FSK: consider changing to "setLeft/Right(..)" form to ease
	// addition of call-outs during tree structure modifications
	// Note that methods need not be in node class itself; can be
	// kept in BinaryTree.
	
	public String toString() {
	    return "node: "+key
		// +" lft:"+left.key
		// +" rgt:"+right.key
		// +" par:"+parent.key
		;
	}
    }
    
    /** Creates a <code>Node</code> n for this such that n.key == k.
	Factory method for <code>Node</code>.  Every construction of a
	<code>Node</code> takes place through this method; thus
	subclasses of <code>RedBlackTree</code> can associate new data
	with their own nodes by extending the <code>Node</code> class
	and overriding this method. 
	
	<p> Note that makeNode <b>must</b> deal with the case when key
	is <code>null</code>, regardless of whether the tree itself
	allows null elements, because the NIL sentinel node has
	<code>null</code> as its key.
    */
    protected Node makeNode(Object key) { return new Node(key); }

    /** Sets the left child of p. 
	Modifies: this, p, l
	Effects: p.left_post = l.  
	Returns: p.left_pre. 
    */
    protected Node setLeft(Node p, Node l) { 
	Node n = p.left; p.left = l; return n;
    }

    /** Sets the right child of p.
	Modifies: this, p, r
	Effects: p.right_post = r.
	Returns: p.right_pre.
    */
    protected Node setRight(Node p, Node r) { 
	Node n = p.right; p.right = r; return n;
    }

    /** Sets the parent of p.
	Modifies: this, c, p
	Effects: c.parent_post = p.
	Returns: c.parent_pre.
    */
    protected Node setParent(Node c, Node p) {
	Node n = c.parent; c.parent = p; return n;
    }
    
    /** Switches the positions of <code>a</code> and <code>b</code>
	within <code>this</code>.  Subclasses are expected to swap any
	data derived from the positions as well.
    */
    protected void swapPositions(Node a, Node b) { 
	// System.out.println("swapping "+a+" and "+b);
	Node tl  = a.left, tr = a.right, tp = a.parent;
	setLeft(  a, b.left);
	setRight( a, b.right);
	setParent(a, b.parent);
	setLeft(b, tl);
	setRight(b, tr);
	setParent(b, tp);

	// this method can be called with nodes that are not
	// present in the tree (even though the nodes themselves
	// remember where they were) so we only update tree
	// pointers if they do indeed point to the swapped nodes. 
	// Thus these cases are more complex than one would think. 

	if ( a.left  != NIL && a.left.parent == b)  a.left.parent = a;
	if (a.right  != NIL && a.right.parent == b) a.right.parent = a;
	if (a.parent != NIL) {
	    if (a.parent.left == b) {
		a.parent.left = a;
	    } else if (a.parent.right == b) {  
		a.parent.right = a;
	    }
	}
	
	// Analogous case as above.
	if ( b.left != NIL && b.left.parent == a) b.left.parent = b;
	if (b.right != NIL && b.right.parent == a) b.right.parent = b;
	if (b.parent != NIL) {
	    if (b.parent.left == a) {
		b.parent.left = b;
	    } else if (b.parent.right == a) {
		b.parent.right = b;
	    }
	}
	
	if (a == root) 
	    root = b;
	else if (b == root)
	    root = a;
	
	// System.out.println("swapped "+a+" and "+b);
    } 

    private Node root;
    protected Node root() { return root; }
    protected Node setRoot(Node r) { Node n = root; root = r; return n; }

    protected final Node NIL;

    protected Comparator comp; 

    private boolean lessThan(Object a, Object b) { 
	return comp.compare(a,b) < 0;
    }
    private boolean eq(Object a, Object b) {
	return comp.compare(a,b) == 0;
    }
    
    /** Creates an empty tree which accepts only <i>mutually
        comparable</i> elements. 
	@see java.lang.Comparable
    */
    public BinaryTree() { this(Default.comparator); }
    
    /** Creates an empty tree which uses <code>c</code> to determine
        element ordering. 
    */
    public BinaryTree(Comparator c) { 
	NIL = makeNode(null);
	root = NIL;
	comp = c; 
    }

    /** Constructs a node for <code>k</code> and inserts it into this.  
	Uses nodes' own methods to dispatch 
     */
    public Node add(Object k) { 
	Node n = makeNode(k); 
	insertNode(n);
	return n; 
    }
    public void remove(Object k) {
	Node n = search(root,k);
	if (n != NIL) 
	    deleteNode(n);
    }
    /** Returns true if <code>k</code> is present in <code>this</code>. */
    public boolean contains(Object k) { return search(root,k) != NIL; }
    
    /** Returns the minimum element of <code>this</code>. 
	Requires: this is non-empty.
     */
    public Object minimum() { return minimum(root).key; }

    /** Returns the maximum element of <code>this</code>. 
	Requires: this is non-empty.
     */
    public Object maximum() { return maximum(root).key; }

    /** Finds the Node n (in the subtree rooted at x) 
	such that n.key = k.
	From CLR, pg 248.
    */ 
    protected Node search(Node x, Object k) {
	while (x != NIL && !eq(k, x.key)) {
	    if (lessThan(k, x.key)) {
		x = x.left;
	    } else {
		x = x.right;
	    }
	}
	return x;
    }
    
    /** Finds the minimum Node n (in the subtree rooted at x).
	From CLR, pg 248.
    */ 
    protected Node minimum(Node x) {
	while (x.left != NIL) 
	    x = x.left;
	return x;
    }

    /** Finds the maximum Node n (in the subtree rooted at x).
	From CLR, pg 248.
    */ 
    protected Node maximum(Node x) {
	while (x.right != NIL) 
	    x = x.right;
	return x;
    }

    /** Returns the successor of x in the sorted order determined by
        an inorder tree walk.  
	From CLR, pg 249.
    */
    protected Node successor(Node x) { 
	if (x.right != NIL) 
	    return minimum(x.right); 
	Node y = x.parent; 
	while(y!=NIL && eq(x, y.right)) { 
	    x = y; 
	    y = y.parent; 
	} 
	return y; 
    } 

    /** Inserts z into some appropriate position in <code>this</code>. 
	requires: (z.left == z.right == NIL)
	modifies: this, z
	From CLR, pg 251.
    */
    protected void insertNode(Node z) {
	assert z != NIL;
	Node y = NIL;
	Node x = root;
	while (x != NIL) {
	    y = x;
	    if (lessThan(z.key, x.key)) 
		x = x.left;
	    else 
		x = x.right;
	}
	z.parent = y;

	if (y == NIL) 
	    root = z;
	else if (lessThan(z.key, y.key)) 
	    y.left = z;
	else 
	    y.right = z;
    }
    
    /** Splices <code>z</code> out from this.
	Based on CLR, pg 253.
	modifies: this, z
     */
    protected void deleteNode(Node z) {
	// System.out.println(dump());
	Node x, y;
	
	if (z.left == NIL || z.right == NIL) 
	    y = z;
	else 
	    // fun fact: (z has 2 children) ==> (y has at most 1 child)
	    y = successor(z);
	
	if (y.left != NIL)
	    x = y.left;
	else
	    x = y.right;
	
	if (x != NIL) 
	    x.parent = y.parent;

	if (y.parent == NIL) 
	    root = x;
	else if (y == y.parent.left) 
	    y.parent.left = x;
	else
	    y.parent.right = x;
	
	// deviation from CLR; Node data is immutable, so we 
	// shift the nodes themselves around instead. 
	if (y != NIL && y != z) 
	    swapPositions(y,z);
	
    }

    /** some quick display method for testing purposes. */
    private void inorderTreeWalk() { inorderTreeWalk(root); }
    private void inorderTreeWalk(Node x) {
	if (x != NIL) {
	    inorderTreeWalk(x.left);
	    System.out.print(x.key);
	    System.out.print(' ');
	    inorderTreeWalk(x.right);	    
	}
    }
    public void test() {
	String[] strings = { "foo", "bar", "wistfully_3", "willing_4", "whilst_5",  
			     "whitling_6", "away_7", "at_8", "the_9", "bar" };

	for(int i=0; i<strings.length; i++) {
	    // System.out.println(dump());
	    add( strings[i] );
	    inorderTreeWalk(); 
	    System.out.println();
	}
	
	for(int i=0; i<strings.length; i++) {
	    // System.out.println(dump());
	    remove( strings[i] );
	    inorderTreeWalk();
	    System.out.println();
	}

    }
    public static void main(String[] args) {
	BinaryTree t = new BinaryTree();
	t.test();
    }
    public String dump() { return dump(root); }
    public String dump(Node x) {
	if (x == NIL) 
	    return ".";
	else 
	    return "("+x +" "
		+ dump(x.left) +" "
		+ dump(x.right) +")"; 
    }
}

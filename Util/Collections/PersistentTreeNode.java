// PersistentTreeNode.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Comparator;
/**
 * <code>PersistentTreeNode</code>s are nodes of a persistent binary
 * search tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentTreeNode.java,v 1.2 2002-02-25 21:09:15 cananian Exp $
 */
class PersistentTreeNode extends AbstractMapEntry {
    public final Object key;
    public final Object value;
    public final PersistentTreeNode left;
    public final PersistentTreeNode right;

    PersistentTreeNode(Object key, Object value,
		       PersistentTreeNode left, PersistentTreeNode right) {
	this.key = key;  this.value = value;
	this.left= left; this.right = right;
    }
    // ACCESSOR FUNCTIONS for Map.Entry.
    public Object getKey() { return key; }
    public Object getValue() { return value; }

    /** equals() merely checks that key and value are equivalent;
     *  isSame() checks that left and right branches are equivalent, too.
     */
    public boolean isSame(PersistentTreeNode n) {
	return
	    isSame(key,  n.key)  && isSame(value, n.value) &&
	    isSame(left, n.left) && isSame(right, n.right);
    }
    private static boolean isSame(Object o1, Object o2) {
	return ((o1==null) && (o2==null)) || (o1!=null && o1.equals(o2));
    }

    // TREE UTILITY FUNCTIONS.
    /** Creates a new node iff the created node would not be identical
     *  to the given <code>n</code>. */
    private static PersistentTreeNode newNode(PersistentTreeNode n,
					      Object key, Object value,
					      PersistentTreeNode left,
					      PersistentTreeNode right) {
	if (n != null && n.key.equals(key) && n.value.equals(value) &&
	    n.left == left && n.right == right)
	    return n;
	return new PersistentTreeNode(key, value, left, right);
    }
    /** Returns the number of nodes in the tree rooted at <code>n</code>.
     * @return 0 if <code>n==null</code>, else 1+size(n.left)+size(n.right)
     */
    static int size(PersistentTreeNode n) {
	return (n==null) ? 0 : (1 + size(n.left) + size(n.right));
    }
    /** Returns the <code>PersistentTreeNode</code> matching <code>key</code>
     *  if any, else <code>null</code>. */
    static PersistentTreeNode get(PersistentTreeNode n, Comparator c,
				  Object key) {
	if (n==null) return null; /* no node with this key. */
	int r = c.compare(key, n.key);
	return
	    (r ==0) ? n :
	    (r < 0) ? get(n.left, c, key) : get(n.right, c, key);
    }
    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code>, plus a mapping from
     *  <code>key</code> to <code>value</code>. */
    static PersistentTreeNode put(PersistentTreeNode n, Comparator c,
				  Object key, Object value) {
	if (n==null) return newNode(null, key, value, null, null);
	
	int r = c.compare(key, n.key);
	if (r==0)
	    return newNode(n, key, value, n.left, n.right);
	if (r < 0)
	    return newNode(n, n.key, n.value,
			   put(n.left, c, key, value), n.right);
	if (r > 0)
	    return newNode(n, n.key, n.value,
			   n.left, put(n.right, c, key, value));
	throw new Error("Impossible!");
    }
    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code> except that it does
     *  not contain a mapping for <code>key</code>. */
    static PersistentTreeNode remove(PersistentTreeNode n, Comparator c,
				     Object key) {
	if (n==null) return null; // key not found.

	int r = c.compare(key, n.key);
	if (r==0) // remove this node.
	    return merge(n.left, n.right, true);
	if (r < 0)
	    return newNode(n, n.key, n.value,
			   remove(n.left, c, key), n.right);
	if (r > 0)
	    return newNode(n, n.key, n.value,
			   n.left, remove(n.right, c, key));
	throw new Error("Impossible!");
    }
    /** Merge two nodes into one. */
    private static PersistentTreeNode merge(PersistentTreeNode left,
					    PersistentTreeNode right,
					    boolean toggle) {
	if (left==null) return right;
	if (right==null) return left;
	if (toggle) // try not to pile everything on one side.
	    return newNode(null, right.key, right.value,
			   merge(left, right.left, !toggle), right.right);
	else
	    return newNode(null, left.key, left.value,
			   left.left, merge(left.right, right, !toggle));
    }
}

// PersistentTree.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>PersistentTree</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentTree.java,v 1.1.2.2 1999-04-03 18:02:10 cananian Exp $
 */
public class PersistentTree  {
    /*final*/ Node root;
    /*final*/ Comparator c;

    /** Creates an empty <code>PersistentTree</code>. 
     *  Key objects must implement <code>java.lang.Comparable</code>. 
     */
    public PersistentTree() {
	this.root = null; this.c = default_comparator;
    }
    /** Creates an empty <code>PersistentTree</code> whose
     *  key objects are ordered by the given <code>Comparator</code>.
     */
    public PersistentTree(Comparator c) {
	this.root = null; this.c = c;
    }
    /** Creates a <code>PersistentTree</code> from a root <code>Node</code>
     *  and a <code>Comparator</code>.*/
    private PersistentTree(Node root, Comparator c) {
	this.root = root; this.c = c;
    }

    /** Determines if this <code>PersistentTree</code> is empty. */
    public boolean isEmpty() { return (root==null); }

    /* Creates and returns a new PersistantTree containing the mapping
     * <code>key->value</code>. */
    public PersistentTree put(Object key, Object value) {
	return new PersistentTree(_put(this.root, key, value), c);
    }
    private Node _put(Node n, Object key, Object value) {
	if (n==null)
	    return new Node(key, value, null, null);

	int r = c.compare(key, n.key);
	if (r==0)
	    return new Node(key, value, n.left, n.right);
	if (r < 0)
	    return new Node(n.key, n.value,
			    _put(n.left, key, value), n.right);
	if (r > 0)
	    return new Node(n.key, n.value,
			    n.left, _put(n.right, key, value));
	throw new Error("Impossible!");
    }

    /** Gets the mapping for <code>key</code>. */
    public Object get(Object key) {
	Node np = lookup(key);
	return (np==null)?null:np.value;
    }
    /** Determines if there is a mapping for the given <code>key</code>. */
    public boolean containsKey(Object key) {
	return (lookup(key)!=null);
    }
    /** Utility function to lookup the node containing the given key. */
    private Node lookup(Object key) {
	for (Node np = this.root; np!=null; ) {
	    int r = c.compare(key, np.key);
	    if (r == 0) return np;
	    np = (r < 0) ? np.left : np.right;
	}
	return null; /* no node with this key. */
    }

    /* FIXME:
    public Map asMap() {
	return new AbstractMap() {
	    public Set entrySet() {
		return new AbstractSet() {
		    public int size() {
			int n=0;
			for (Iterator it=iterator(); it.hasNext(); it.next())
			    n++;
			return n;
		    }
		    public Iterator iterator() {
			// do iteration. 
			return null;
		    }
		};
	    }
	};
    }
    */

    /*---------------------------------------------------------------*/
    private static class Node {
	Object key, value;
	Node left, right;
	Node(Object key, Object value, Node left, Node right) {
	    this.key = key;  this.value = value;
	    this.left= left; this.right = right;
	}
	public String toString() {
	    return "<"+key+", "+value+">";
	}
	public boolean equals(Object o) {
	    if (!(o instanceof Node)) return false;
	    Node n = (Node) o;
	    return
		isSame(key,  n.key)  && isSame(value, n.value) &&
		isSame(left, n.left) && isSame(right, n.right);
	}
	private /*static*/ boolean isSame(Object o1, Object o2) {
	    return ((o1==null) && (o2==null)) || (o1!=null && o1.equals(o2));
	}
    }
    /* A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    private static final Comparator default_comparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    return ((Comparable)o1).compareTo(o2);
	}
    };
}

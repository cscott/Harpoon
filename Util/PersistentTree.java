// PersistentTree.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
/**
 * <code>PersistentTree</code> implements a persistent binary search tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentTree.java,v 1.1.2.3 1999-04-06 14:57:40 cananian Exp $
 */
public class PersistentTree  {
    /*final*/ Node root;
    /*final*/ Comparator c;

    /** Creates an empty <code>PersistentTree</code> whose
     *  key objects will all implement <code>java.lang.Comparable</code>. 
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

    /** Determines if this <code>PersistentTree</code> has any mappings. */
    public boolean isEmpty() { return (root==null); }

    /** Count the number of key->value mappings in this
     *  <code>PersistentTree</code>. */
    public int size() {
	return _size(root);
    }
    private int _size(Node n) {
	return (n==null) ? 0 : (_size(n.left) + _size(n.right));
    }

    /** Creates and returns a new <code>PersistantTree</code> identical to
     *  this one, except it contains a mapping from <code>key</code> to
     *  <code>value. */
    public PersistentTree put(Object key, Object value) {
	Node new_root = _put(this.root, key, value);
	return (this.root == new_root) ? this :
	    new PersistentTree(new_root, c);
    }
    private Node _put(Node n, Object key, Object value) {
	if (n==null)
	    return new Node(key, value, null, null);

	int r = c.compare(key, n.key);
	if (r==0)
	    return newNode(n, key, value, n.left, n.right);
	if (r < 0)
	    return newNode(n, n.key, n.value,
			   _put(n.left, key, value), n.right);
	if (r > 0)
	    return newNode(n, n.key, n.value,
			   n.left, _put(n.right, key, value));
	throw new Error("Impossible!");
    }
    /** Make a new node, but use the old node if we can. */
    private Node newNode(Node oldNode,
			 Object key, Object value, Node left, Node right) {
	Node n = new Node(key, value, left, right);
	return (oldNode.equals(n)) ? oldNode : n;
    }

    /** Gets the value which <code>key</code> maps to. */
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
    /** Make a new <code>PersistentTree</code> identical to this one,
     *  except that it does not contain a mapping for <code>key</code>. */
    public PersistentTree remove(Object key) {
	Node new_root = _remove(this.root, key);
	return (this.root == new_root) ? this :
	    new PersistentTree(new_root, c);
    }
    private Node _remove(Node n, Object key) {
	if (n==null) return null; // key not found.

	int r = c.compare(key, n.key);
	if (r==0) // remove this node.
	    return _merge(n.left, n.right, true);
	if (r < 0)
	    return newNode(n, n.key, n.value,
			   _remove(n.left, key), n.right);
	if (r > 0)
	    return newNode(n, n.key, n.value,
			   n.left, _remove(n.right, key));
	throw new Error("Impossible!");
    }
    /** Merge two nodes into one. */
    private Node _merge(Node left, Node right, boolean toggle) {
	if (left==null) return right;
	if (right==null) return left;
	if (toggle) // try not to pile everything on one side.
	    return new Node(right.key, right.value,
			    _merge(left, right.left, !toggle), right.right);
	else
	    return new Node(left.key, left.value,
			    left.left, _merge(left.right, right, !toggle));
    }
	
    /** Human-readable representation of the map. */
    public String toString() { return asMap().toString(); }

    /*---------------------------------------------------------------*/
    private static class Node {
	Object key, value;
	Node left, right;
	Node(Object key, Object value, Node left, Node right) {
	    this.key = key;  this.value = value;
	    this.left= left; this.right = right;
	}
	public boolean equals(Object o) {
	    if (!(o instanceof Node)) return false;
	    Node n = (Node) o;
	    return
		isSame(key,  n.key)  && isSame(value, n.value) &&
		isSame(left, n.left) && isSame(right, n.right);
	}
	/*static*/ boolean isSame(Object o1, Object o2) {
	    return ((o1==null) && (o2==null)) || (o1!=null && o1.equals(o2));
	}
	Map.Entry asMapEntry() {
	    return new AbstractMapEntry() {
		public Object getKey() { return key; }
		public Object getValue() { return value; }
	    };
	}
	public String toString() { return asMapEntry().toString(); }
    }
    /* A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    private static final Comparator default_comparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    return ((Comparable)o1).compareTo(o2);
	}
    };

    /*---------------------------------------------------------------*/
    /** <code>java.util.Collection</code>s view of the mapping. */
    public Map asMap() {
	return new AbstractMap() {
	    public boolean containsKey(Object key) {
		return PersistentTree.this.containsKey(key);
	    }
	    public Object get(Object key) {
		return PersistentTree.this.get(key);
	    }
	    public boolean isEmpty() {
		return PersistentTree.this.isEmpty();
	    }
	    public int size() {
		return PersistentTree.this.size();
	    }
	    public Set entrySet() {
		return new AbstractSet() {
		    public int size() {
			return PersistentTree.this.size();
		    }
		    public Iterator iterator() {
			final Stack s = new Stack();
			if (root!=null) s.push(root);

			return new Iterator() {
			    public boolean hasNext() {
				return !s.isEmpty();
			    }
			    public Object next() {
				if (s.isEmpty())
				    throw new NoSuchElementException();
				final Node n = (Node) s.pop();
				if (n.right!=null) s.push(n.right);
				if (n.left!=null)  s.push(n.left);
				return n.asMapEntry();
			    }
			    public void remove() {
				throw new UnsupportedOperationException();
			    }
			};
		    }
		};
	    }
	};
    }
}

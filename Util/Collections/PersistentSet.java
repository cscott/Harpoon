// PersistentSet.java, created Thu Apr  8 02:32:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Collections.UnmodifiableIterator;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
/**
 * <code>PersistentSet</code> implements a persistent set, based on a
 * persistent randomized treap.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentSet.java,v 1.5 2003-06-10 15:13:39 cananian Exp $
 */
public class PersistentSet<T>  {
    final Node<T> root;
    final Comparator<T> c;
    final Node.Allocator<T> allocator;

    /** Creates an empty <code>PersistentSet</code> whose member objects
     *  will all implement <code>java.lang.Comparable</code>. */
    public PersistentSet() {
	// cast below is safe iff T implements Comparable.
	this((Comparator<T>)((Comparator)Default.comparator));
    }
    /** Creates an empty <code>PersistentSet</code> whose member objects
     *  are ordered by the given <code>Comparator</code>.
     */
    public PersistentSet(Comparator<T> c) {
	this(null, c, new Node.Allocator<T>());
    }
    /** Creates a <code>PersistentSet</code> from a root <code>Node</code>
     *  and a <code>Comparator</code>.*/
    private PersistentSet(Node<T> root, Comparator<T> c, Node.Allocator<T> a) {
	this.root = root; this.c = c; this.allocator = a;
    }

    /** Determines if this <code>PersistentSet</code> has any members. */
    public boolean isEmpty() { return (root==null); }

    /** Count the number of elements in this <code>PersistentSet</code>. */
    public int size() { return (root==null)?0:root.size; }

    /** Creates and returns a new <code>PersistantSet</code> identical to
     *  this one, except it contains <code>element</code>. */
    public PersistentSet<T> add(T element) {
	Node<T> new_root =
	    PersistentTreeNode.put(this.root, this.c, element, element,
				   allocator);
	return (this.root == new_root) ? this :
	    new PersistentSet<T>(new_root, c, allocator);
    }
    /** Determines if the given element belongs to this set. */
    public boolean contains(T element) {
	return (PersistentTreeNode.get(this.root, this.c, element)!=null);
    }

    /** Make a new <code>PersistentSet</code> identical to this one,
     *  except that it does not contain <code>element</code>. */
    public PersistentSet<T> remove(T element) {
	Node<T> new_root = 
	    PersistentTreeNode.remove(this.root, this.c, element, allocator);
	return (this.root == new_root) ? this :
	    new PersistentSet<T>(new_root, c, allocator);
    }
    /** Add all the items in the given set to this set. */
    public PersistentSet<T> addAll(PersistentSet<T> set) {
	if (this.c.equals(set.c) && this.allocator==set.allocator) {
	    Node<T> new_root =
		Node.putAll(this.root, set.root, this.c, allocator);
	    return (new_root==this.root) ? this :
		(new_root==set.root) ? set :
		new PersistentSet<T>(new_root, c, allocator);
	}
	// slow way
	if (set.size() > this.size()) return set.addAll(this);
	Node<T> new_root = this.root;
	for (Iterator<Node<T>> it=Node.iterator(set.root); it.hasNext(); ) {
	    Node<T> n = it.next();
	    new_root = Node.put(new_root, this.c, n.key, n.getValue(),
				allocator);
	}
	return (this.root == new_root) ? this :
	    new PersistentSet<T>(new_root, c, allocator);
    }
   
    /** Human-readable representation of the set. */
    public String toString() { return asSet().toString(); }

    /*---------------------------------------------------------------*/
    /** <code>java.util.Collection</code>s view of the set. */
    public Set<T> asSet() {
	return new AbstractSet<T>() {
	    public boolean contains(Object o) {
		// oops, not safe if we pass a non-T o in here!
		return PersistentSet.this.contains((T)o);
	    }
	    public boolean isEmpty() {
		return PersistentSet.this.isEmpty();
	    }
	    public int size() {
		return PersistentSet.this.size();
	    }
	    public Iterator<T> iterator() {
		final Iterator<Node<T>> it = Node.iterator(root);
		return new Iterator<T>() {
		    Node<T> last=null;
		    public boolean hasNext() { return it.hasNext(); }
		    public T next() {
			last = it.next();
			return last.key;
		    }
		    public void remove() {
			if (last==null)
			    throw new IllegalStateException();
			// XXX doesn't update the Set view
			// changes behind the iterator's back don't
			// matter, because everything's persistent!
			PersistentSet.this.remove(last.getKey());
			last=null;
		    }
		};
	    }
	};
    }
    // PersistentTreeNode subclass
    private static class Node<T>
	extends PersistentTreeNode<Node<T>,T,T> {
	/** The hash code of a <code>java.util.Set</code> with the
	 *  contents of the tree rooted at this node. */
	final int setHashCode;
	/** Size of the tree rooted at this node. */
	final int size;
	Node(T key, Node<T> left, Node<T> right) {
	    super(key, left, right);
	    this.setHashCode =
		((key==null)?0:key.hashCode()) + // this entry
		((left==null)?0:left.setHashCode) + // hash of left tree
		((right==null)?0:right.setHashCode); // hash of right tree
	    this.size = 1 + // this entry
		((left==null)?0:left.size) + // size of left tree
		((right==null)?0:right.size); // size of right tree
	}
	public T getValue() { return key; }
	static class Allocator<T>
	    extends PersistentTreeNode.Allocator<Node<T>,T,T> {
	    Node<T> newNode(T key, T value, Node<T> left, Node<T> right) {
		// ignore value.
		return new Node<T>(key, left, right);
	    }
	}
    }
}

// PersistentSet.java, created Thu Apr  8 02:32:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.UnmodifiableIterator;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
/**
 * <code>PersistentSet</code> implements a persistent set, based on a
 * binary search tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentSet.java,v 1.2 2002-02-25 21:09:15 cananian Exp $
 */
public class PersistentSet  {
    final PersistentTreeNode root;
    final Comparator c;

    /** Creates an empty <code>PersistentSet</code> whose member objects
     *  will all implement <code>java.lang.Comparable</code>. */
    public PersistentSet() {
	this.root = null; this.c = Default.comparator;
    }
    /** Creates an empty <code>PersistentSet</code> whose member objects
     *  are ordered by the given <code>Comparator</code>.
     */
    public PersistentSet(Comparator c) {
	this.root = null; this.c = c;
    }
    /** Creates a <code>PersistentSet</code> from a root <code>Node</code>
     *  and a <code>Comparator</code>.*/
    private PersistentSet(PersistentTreeNode root, Comparator c) {
	this.root = root; this.c = c;
    }

    /** Determines if this <code>PersistentSet</code> has any members. */
    public boolean isEmpty() { return (root==null); }

    /** Count the number of elements in this <code>PersistentSet</code>. */
    public int size() { return PersistentTreeNode.size(root); }

    /** Creates and returns a new <code>PersistantSet</code> identical to
     *  this one, except it contains <code>element</code>. */
    public PersistentSet add(Object element) {
	PersistentTreeNode new_root =
	    PersistentTreeNode.put(this.root, this.c, element, element);
	return (this.root == new_root) ? this :
	    new PersistentSet(new_root, c);
    }
    /** Determines if the given element belongs to this set. */
    public boolean contains(Object element) {
	return (PersistentTreeNode.get(this.root, this.c, element)!=null);
    }

    /** Make a new <code>PersistentSet</code> identical to this one,
     *  except that it does not contain <code>element</code>. */
    public PersistentSet remove(Object element) {
	PersistentTreeNode new_root = 
	    PersistentTreeNode.remove(this.root, this.c, element);
	return (this.root == new_root) ? this :
	    new PersistentSet(new_root, c);
    }
    
    /** Human-readable representation of the set. */
    public String toString() { return asSet().toString(); }

    /*---------------------------------------------------------------*/
    /** <code>java.util.Collection</code>s view of the set. */
    public Set asSet() {
	return new AbstractSet() {
	    public boolean contains(Object o) {
		return PersistentSet.this.contains(o);
	    }
	    public boolean isEmpty() {
		return PersistentSet.this.isEmpty();
	    }
	    public int size() {
		return PersistentSet.this.size();
	    }
	    public Iterator iterator() {
		final Stack s = new Stack();
		if (root!=null) s.push(root);

		return new UnmodifiableIterator() {
		    public boolean hasNext() {
			return !s.isEmpty();
		    }
		    public Object next() {
			if (s.isEmpty())
			    throw new NoSuchElementException();
			final PersistentTreeNode n =
			    (PersistentTreeNode) s.pop();
			if (n.right!=null) s.push(n.right);
			if (n.left!=null)  s.push(n.left);
			return n.key; /* element */
		    }
		};
	    }
	};
    }
}

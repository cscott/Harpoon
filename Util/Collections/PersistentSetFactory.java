// PersistentSetFactory.java, created Thu May 29 17:34:07 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Default.PairList;

import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
/**
 * A <code>PersistentSetFactory</code> uses hash-consing to ensure that
 * the <code>PersistentSet</code>s created by it maximally reuse space.
 * Equality tests between <code>Set</code>s created by this factory are
 * also fast.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentSetFactory.java,v 1.2 2003-06-14 20:44:42 cananian Exp $
 */
public class PersistentSetFactory<T> extends SetFactory<T> {
    final Allocator<T> allocator = new Allocator<T>();
    final Comparator<T> comparator;

    /** Creates a <code>PersistentSetFactory</code>. */
    public PersistentSetFactory(Comparator<T> comparator) {
	this.comparator = comparator;
    }

    /** Generates a new unsynchronized mutable <code>Set</code> which
     *  is based on persistent randomized treaps.  All <code>Set</code>s
     *  created by this factory maximally reuse space, and have very
     *  fast comparison operations. */
    public <V extends T> Set<T> makeSet(Collection<V> c) {
	return new SetImpl(c);
    }
    class SetImpl extends AbstractSet<T> {
	Node<T> root = null;
	<V extends T> SetImpl(Collection<V> c) {
	    addAll(c);
	}
	SetImpl(Node<T> root) { this.root = root; }
	public boolean isEmpty() { return this.root==null; }
	public int size() { return (root==null)?0:root.size; }
	public int hashCode() { return (root==null)?0:root.setHashCode; }
	public boolean equals(Object o) {
	    // maps from the same factory can be compared very quickly
	    if (o instanceof SetImpl &&
		factory() == ((SetImpl)o).factory())
		return this.root == ((SetImpl)o).root;
	    return super.equals(o);
	}
	// for the equals() implementation.
	private PersistentSetFactory<T> factory() {
	    return PersistentSetFactory.this;
	}
	public void clear() {
	    this.root = null;
	}
	public SetImpl clone() { return new SetImpl(this.root); }
	public boolean contains(Object key) {
	    // yuck, can't enforce that comparator will be able to handle
	    // this arbitrary key
	    return (Node.get(this.root, comparator, (T)key)!=null);
	}
	public boolean remove(Object key) {
	    Node<T> old_root = this.root;
	    this.root = Node.remove(this.root, comparator, (T)key, allocator);
	    return old_root!=this.root;
	}
	public boolean add(T elem) {
	    Node<T> old_root = this.root;
	    this.root = Node.put(this.root, comparator, elem, elem, allocator);
	    return old_root!=this.root;
	}
	public <V extends T> boolean addAll(Collection<V> c) {
	    // special fast case for sets from the same factory
	    // sets from the same factory can be compared very quickly
	    if (c instanceof SetImpl &&
		factory() == ((SetImpl)c).factory()) {
		Node<T> old_root = this.root;
		this.root = Node.putAll(this.root, ((SetImpl)c).root,
					comparator, allocator);
		return old_root!=this.root;
	    } else // slow case
		return super.addAll(c);
	}
	public <V> boolean containsAll(Collection<V> c) {
	    // special fast case for sets from the same factory
	    // sets from the same factory can be compared very quickly
	    if (c instanceof SetImpl &&
		factory() == ((SetImpl)c).factory())
		return containsAll(this.root, ((SetImpl)c).root);
	    else // slow case
		return super.containsAll(c);
	}
	public Iterator<T> iterator() {
	    final Iterator<Node<T>> it = Node.iterator(root);
	    return new Iterator<T>() {
		Node<T> last = null;
		public boolean hasNext() { return it.hasNext(); }
		public T next() {
		    last = it.next();
		    return last.key;
		}
		public void remove() {
		    if (last==null)
			throw new IllegalStateException();
		    SetImpl.this.remove(last.key);
		    last=null;
		}
	    };
	}
	// fast implementation of containsAll
	/** Returns true iff the <code>container</code> tree contains all
	 *  elements in the <code>subset</code> tree. */
	boolean containsAll(Node<T> container, Node<T> subset) {
	    if (subset==null || container==subset) return true;
	    if (container==null) return false; // subset not null.
	    assert container!=null && subset!=null;
	    assert container!=subset; // needed for next test to be correct
	    if (subset.size >= container.size) return false; // pigeonhole
	    int cmp = comparator.compare(container.key, subset.key);
	    if (cmp==0)
		return containsAll(container.left, subset.left) &&
		    containsAll(container.right, subset.right);
	    PairList<Node<T>,Node<T>> pair =
		removeAndSplit(subset, container.key);
	    return containsAll(container.left, pair.left()) &&
		containsAll(container.right, pair.right());
	}
	PairList<Node<T>,Node<T>> removeAndSplit(Node<T> node, T key) {
	    int cmp = comparator.compare(node.key, key);
	    if (cmp==0)
		return new PairList<Node<T>,Node<T>>(node.left, node.right);
	    if (cmp <0) {
		PairList<Node<T>,Node<T>> pair=removeAndSplit(node.right, key);
		return new PairList<Node<T>,Node<T>>
		    (allocator.newNode(node.key, node.getValue(),
				       node.left, pair.left()),
		     pair.right());
	    } else {
		PairList<Node<T>,Node<T>> pair=removeAndSplit(node.left, key);
		return new PairList<Node<T>,Node<T>>
		    (pair.left(),
		     allocator.newNode(node.key, node.getValue(),
				       pair.right(), node.right));
	    }
	}
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
	    this.setHashCode = (key==null?0:key.hashCode()) + // this element
		((left==null)?0:left.setHashCode) + // hash of left tree
		((right==null)?0:right.setHashCode); // hash of right tree
	    this.size = 1 + // this entry
		((left==null)?0:left.size) + // size of left tree
		((right==null)?0:right.size); // size of right tree
	}
	public T getValue() { return key; }
	// override 'equals' and 'hashCode' to facilitate hash-consing.
	public boolean equals(Object o) {
	    if (!(o instanceof Node)) return false;
	    Node n = (Node) o;
	    return ((key==null)?n.key==null:key.equals(n.key)) &&
		left == n.left && right == n.right;
	}
	public int hashCode() { return setHashCode; }
    }
    /** Allocator uses a <code>WeakHashMap</code> to do hash consing. */
    static class Allocator<T>
	extends PersistentTreeNode.Allocator<Node<T>,T,T> {
	final WeakHashMap<Node<T>,WeakReference<Node<T>>> hashConsCache =
	    new WeakHashMap<Node<T>,WeakReference<Node<T>>>();
	Node<T> newNode(T key, T value,
			Node<T> left, Node<T> right) {
	    assert key==value;
	    Node<T> n = new Node<T>(key, left, right);
	    WeakReference<Node<T>> nn = hashConsCache.get(n);
	    if (nn==null)
		hashConsCache.put(n,nn=new WeakReference<Node<T>>(n));
	    return nn.get();
	}
    }
}

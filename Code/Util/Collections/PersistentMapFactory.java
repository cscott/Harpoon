// PersistentMapFactory.java, created Thu May 29 17:34:07 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
/**
 * A <code>PersistentMapFactory</code> uses hash-consing to ensure that
 * the <code>PersistentMap</code>s created by it maximally reuse space.
 * Equality tests between <code>Map</code>s created by this factory are
 * also fast.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentMapFactory.java,v 1.1 2003-06-10 15:13:39 cananian Exp $
 */
public class PersistentMapFactory<K,V> extends MapFactory<K,V> {
    final Allocator<K,V> allocator = new Allocator<K,V>();
    final Comparator<K> comparator;

    /** Creates a <code>PersistentMapFactory</code>. */
    public PersistentMapFactory(Comparator<K> comparator) {
	this.comparator = comparator;
    }

    /** Generates a new unsynchronized mutable <code>Map</code> which
     *  is based on persistent randomized treaps.  All <code>Map</code>s
     *  created by this factory maximally reuse space, and have very
     *  fast comparison operations. */
    public <K2 extends K, V2 extends V> Map<K,V> makeMap(Map<K2,V2> mm) {
	return new MapImpl(mm);
    }
    class MapImpl extends AbstractMap<K,V> {
	Node<K,V> root = null;
	<K2 extends K, V2 extends V> MapImpl(Map<K2,V2> mm) {
	    putAll(mm);
	}
	MapImpl(Node<K,V> root) { this.root = root; }
	public boolean isEmpty() { return this.root==null; }
	public int size() { return (root==null)?0:root.size; }
	public int hashCode() { return (root==null)?0:root.mapHashCode; }
	public boolean equals(Object o) {
	    // maps from the same factory can be compared very quickly
	    if (o instanceof MapImpl &&
		factory() == ((MapImpl)o).factory())
		return this.root == ((MapImpl)o).root;
	    return super.equals(o);
	}
	// for the equals() implementation.
	private PersistentMapFactory<K,V> factory() {
	    return PersistentMapFactory.this;
	}
	public void clear() {
	    this.root = null;
	}
	public MapImpl clone() { return new MapImpl(this.root); }
	public boolean containsKey(Object key) {
	    // yuck, can't enforce that comparator will be able to handle
	    // this arbitrary key
	    return (Node.get(this.root, comparator, (K)key)!=null);
	}
	public V get(Object key) {
	    // yuck, can't enforce that comparator will be able to handle
	    // this arbitrary key
	    Node<K,V> np = Node.get(this.root, comparator, (K)key);
	    return (np==null)?null:np.value;
	}
	void removeFast(Object key) {
	    // yuck, can't enforce that comparator will be able to handle
	    // this arbitrary key
	    this.root = Node.remove(this.root, comparator, (K)key, allocator);
	}
	public V remove(Object key) {
	    V oldValue = get(key);
	    removeFast(key);
	    return oldValue;
	}
	void putFast(K key, V value) {
	    this.root = Node.put(this.root, comparator, key, value, allocator);
	}
	public V put(K key, V value) {
	    V oldValue = get(key);
	    putFast(key, value);
	    return oldValue;
	}
	public <K2 extends K, V2 extends V> void putAll(Map<K2,V2> mm) {
	    // special fast case for maps from the same factory
	    // maps from the same factory can be compared very quickly
	    if (mm instanceof MapImpl &&
		factory() == ((MapImpl)mm).factory())
		this.root = Node.putAll(this.root, ((MapImpl)mm).root,
					comparator, allocator);
	    else // slow case
		super.putAll(mm);
	}
	public MapSet<K,V> entrySet() { return new EntrySet(); }
	private class EntrySet extends AbstractSet<Map.Entry<K,V>>
	    implements MapSet<K,V> {
	    public int size() { return MapImpl.this.size(); }
	    public MapImpl asMap() { return MapImpl.this; }
	    public Iterator<Map.Entry<K,V>> iterator() {
		final Iterator<Node<K,V>> it = Node.iterator(root);
		return new Iterator<Map.Entry<K,V>>() {
		    Node<K,V> last = null;
		    public boolean hasNext() { return it.hasNext(); }
		    public Map.Entry<K,V> next() {
			last = it.next();
			return new NodeWrapper(last);
		    }
		    public void remove() {
			if (last==null)
			    throw new IllegalStateException();
			MapImpl.this.removeFast(last.getKey());
			last=null;
		    }
		};
	    }
	}
	/** Wrapper for 'Node' which restores the proper 'equals' and
	 *  'hashCode' semantics for a <code>Map.Entry</code>. */
	private class NodeWrapper extends AbstractMapEntry<K,V> {
	    Node<K,V> node;
	    NodeWrapper(Node<K,V> node) { this.node = node; }
	    public K getKey() { return node.key; }
	    public V getValue() { return node.value; }
	    // whee, side-effect impervious!
	    public V setValue(V value) {
		V oldValue = node.value;
		MapImpl.this.putFast(node.key, value);
		node = Node.get(root, comparator, node.key);
		return oldValue;
	    }
	}
    }
    // PersistentTreeNode subclass
    private static class Node<K,V>
	extends PersistentTreeNode<Node<K,V>,K,V> {
	final V value;
	/** The hash code of a <code>java.util.Map</code> with the
	 *  contents of the tree rooted at this node. */
	final int mapHashCode;
	/** Size of the tree rooted at this node. */
	final int size;
	Node(K key, V value, Node<K,V> left, Node<K,V> right) {
	    super(key, left, right);
	    this.value = value;
	    this.mapHashCode = this.entryHashCode() + // this entry
		((left==null)?0:left.mapHashCode) + // hash of left tree
		((right==null)?0:right.mapHashCode); // hash of right tree
	    this.size = 1 + // this entry
		((left==null)?0:left.size) + // size of left tree
		((right==null)?0:right.size); // size of right tree
	}
	public V getValue() { return value; }
	// override 'equals' and 'hashCode' to facilitate hash-consing.
	public boolean equals(Object o) {
	    if (!(o instanceof Node)) return false;
	    Node n = (Node) o;
	    return ((key==null)?n.key==null:key.equals(n.key)) &&
		value == n.value && left == n.left && right == n.right;
	}
	public int hashCode() { return mapHashCode; }
	public int entryHashCode() { return super.hashCode(); }
    }
    /** Allocator uses a <code>WeakHashMap</code> to do hash consing. */
    static class Allocator<K,V>
	extends PersistentTreeNode.Allocator<Node<K,V>,K,V> {
	final WeakHashMap<Node<K,V>,WeakReference<Node<K,V>>> hashConsCache =
	    new WeakHashMap<Node<K,V>,WeakReference<Node<K,V>>>();
	Node<K,V> newNode(K key, V value,
			  Node<K,V> left, Node<K,V> right) {
	    Node<K,V> n = new Node<K,V>(key, value, left, right);
	    WeakReference<Node<K,V>> nn = hashConsCache.get(n);
	    if (nn==null)
		hashConsCache.put(n,nn=new WeakReference<Node<K,V>>(n));
	    return nn.get();
	}
    }
}

// LightMap.java, created Sun Mar 19 15:46:56 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import harpoon.Util.Util;

import jpaul.Graphs.BinTreeUtil;
import jpaul.Graphs.BinTreeNav;

/**
 * <code>LightMap</code> is a very lightweight implementation of
 the <code>java.util.Map</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@alum.mit.edu>
 * @version $Id: LightMap.java,v 1.1 2005-08-17 23:34:00 salcianu Exp $
 */
public class LightMap<K,V> implements Map<K,V>, Cloneable, java.io.Serializable {
    // the number of mappings in this map
    private int size = 0;
    // the root of the binary tree used to store the mapping
    private BinTreeNode<K,V> root = null;

    /** Creates a <code>LightMap</code>. */
    public LightMap() {}

    /** Creates a <code>LightMap</code> with the same mappings as the
     *  given map. */
    public LightMap(Map<? extends K, ? extends V> map) {
	this();
	putAll(map);
    }

    public final int size() { return size; }

    public final boolean isEmpty() { return size == 0; }

    public final boolean containsKey(Object key) {
	return _get(key) != null;
    }

    /** Unsupported yet. */
    public final boolean containsValue(Object value) {
	throw new UnsupportedOperationException();
    }

    public V get(Object key) {
	BinTreeNode<K,V> node = _get(key);
	if(node == null) return null;
	return node.value;
    }


    private BinTreeNode<K,V> _get(Object key) { 
	BinTreeNode<K,V> p = root;
	int key_hash_code = key.hashCode();

	while(p != null) {
	    if(key_hash_code < p.keyHashCode) {
		p = p.left;
	    }
	    else {
		if(key_hash_code > p.keyHashCode) {
		    p = p.right;
		}
		else {
		    if(p.key.equals(key)) {
			return p;
		    }
		    else {
			p = p.right;
		    }
		}
	    }
	}

	return null;
    }


    /** Associates the specified value with the specified key in this map. */
    public final V put(K key, V value) {
	BinTreeNode<K,V> prev = null;
	BinTreeNode<K,V> node = root;
	int key_hash_code = key.hashCode();

	while(node != null) {
	    prev = node;
	    if(key_hash_code < node.keyHashCode) {
		node = node.left;
	    }
	    else {
		if((key_hash_code > node.keyHashCode) || !node.key.equals(key)) {
		    node = node.right;
		}
		else {
		    cachedHashCode -= node.hashCode();
		    V temp = node.value;
		    node.value = value;
		    cachedHashCode += node.hashCode();
		    return temp;
		}
	    }
	}

	size++;
	cachedHashCode += node.hashCode(); // invalidate the cached hash code

	BinTreeNode<K,V> new_node = new BinTreeNode(key, value);
	if(prev == null) {
	    root = new_node;
	    return null;
	}

	if(key_hash_code < prev.keyHashCode) {
	    prev.left = new_node;
	}
	else {
	    prev.right = new_node;
	}

	return null;
    }


    /** Removes the mapping previously attached to <code>key</code>.
	Returns the old mapping if any, or <code>null</code> otherwise. */
    public final V remove(Object key) {
	
	if(key == null) return null;

	int key_hash_code = key.hashCode();
	BinTreeNode<K,V> prev = null;
	int son = 0;
	BinTreeNode<K,V> node = root;

	while(node != null) {
	    if(key_hash_code < node.keyHashCode) {
		prev = node;
		node = node.left;
		son  = 0;
	    }
	    else {
		if((key_hash_code > node.keyHashCode) || !node.key.equals(key)) {
		    prev = node;
		    node = node.right;
		    son  = 1;
		}
		else {
		    size--;
		    cachedHashCode -= node.hashCode();
		    return remove_node(node, prev, son);
		}
	    }
	}
	return null;
    }

    // Remove the BinTreeNode pointed to by node. prev is supposed to be the
    // parent node, pointing to node through his left (son == 0) respectively
    // right (son == 1) link. Returns the old value attached to node.
    private final V remove_node(BinTreeNode<K,V> node, BinTreeNode<K,V> prev, int son) {
	if(node.left == null)
	    return remove_semi_leaf(node, prev, son, node.right);
	if(node.right == null)
	    return remove_semi_leaf(node, prev, son, node.left);

	// The BinTreeNode to replace node in the tree. This is either the
	// next or the precedent node (in the order of the hashCode's.
	// We decide a bit randomly, to gain some balanceness.
	BinTreeNode<K,V> m = 
	    (node.keyHashCode % 2 == 0) ?
	    extract_next(node) : extract_prev(node);

	return finish_removal(node, prev, son, m);
    }


    // Remove a [semi]-leaf (a node with at least one of its sons absent.
    // In this case, we simply "bypass" the link from the predecessor (if any)
    // to the only predecessor of node that could exist.
    private final V remove_semi_leaf(BinTreeNode<K,V> node, BinTreeNode<K,V> prev,
				     int son, BinTreeNode<K,V> m) {
	if(prev == null) {
	    root = m;
	}
	else {
	    if(son == 0)
		prev.left = m;
	    else
		prev.right = m;
	}

	return node.value;
    }


    // Terminal phase of the node removal. Returns the old value attached to
    // node. node, prev and son are as for remove_node; m points to the
    // BinTreeNode that should replace node in the tree.
    private final V finish_removal(BinTreeNode<K,V> node, BinTreeNode<K,V> prev,
				   int son, BinTreeNode<K,V> m) {
	if(m != null) { // set up the links for m
	    m.left  = node.left;
	    m.right = node.right;
	}
	if(prev == null)
	    root = m;
	else {
	    if(son == 0)
		prev.left = m;
	    else
		prev.right = m;
	}

	return node.value;
    }

    // Finds the leftmost BinTreeNode from the right subtree of node;
    // removes it from that subtree and returns it.
    private final BinTreeNode<K,V> extract_next(BinTreeNode<K,V> node) {
	BinTreeNode<K,V> prev = node.right;
	BinTreeNode<K,V> curr = prev.left;

	if(curr == null) {
	    node.right = node.right.right;
	    return prev;
	}

	while(curr.left != null) {
	    prev = curr;
	    curr = curr.left;
	}

	prev.left = curr.right;
	return curr;
    }

    // Finds the rightmost BinTreeNode from the left subtree of node;
    // removes it from that subtree and returns it.
    private final BinTreeNode<K,V> extract_prev(BinTreeNode<K,V> node) {
	BinTreeNode<K,V> prev = node.left;
	BinTreeNode<K,V> curr = prev.right;

	if(curr == null) {
	    node.left = node.left.left;
	    return prev;
	}

	while(curr.right != null) {
	    prev = curr;
	    curr = curr.right;
	}

	prev.right = curr.left;
	return curr;
    }


    /** Copies all of the mappings from the specified map to this map. */
    public final void putAll(Map<? extends K,? extends V> map) {
	for(Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }


    public final void clear() {
	size = 0;
	cachedHashCode = 0;
	root = null;
    }


    /** Returns an unmodifiable collection view of the values from this map. */
    public final Collection<V> values() {
	return new AbstractCollection<V>() {
	    public Iterator<V> iterator() {
		final Iterator<Map.Entry<K,V>> ite = entryIterator();
		return new Iterator<V>() {
		    public boolean hasNext() { return ite.hasNext(); }
		    public void    remove()  { ite.remove(); }
		    public V       next()    { return ite.next().getValue(); }
		};
	    }
	    public int size() {
		return size;
	    }
	};
    }


    private Iterator<Map.Entry<K,V>> entryIterator() {
	return BinTreeUtil.<Map.Entry<K,V>,BinTreeNode<K,V>>inOrder(root, binTreeNav);
    }


    /** Returns an unmodifiable set view of the map entries. */
    public final Set<Map.Entry<K,V>> entrySet() {

	return new AbstractSet<Map.Entry<K,V>>() {
	    public Iterator<Map.Entry<K,V>> iterator() {
		return entryIterator();
	    }
	    public int size() {
		return size;
	    }
	};
    }

    /** Returns an unmodifiable set view of the keys contained in this map. */
    public final Set<K> keySet() {
	return new AbstractSet<K>() {
	    public Iterator<K> iterator() {
		final Iterator<Map.Entry<K,V>> ite = entryIterator();
		return new Iterator<K>() {
		    public boolean hasNext() { return ite.hasNext(); }
		    public void    remove()  { ite.remove(); }
		    public K       next()    { return ite.next().getKey(); }
		};
	    }
	    public int size() {
		return size;
	    }
	};
    }


    private BinTreeNode<K,V> copy_tree(BinTreeNode<K,V> node) {
	if(node == null) return null;
	BinTreeNode<K,V> newnode = new BinTreeNode<K,V>(node.key, node.value);

	newnode.left  = copy_tree(node.left);
	newnode.right = copy_tree(node.right);
	
	return newnode;
    }


    public Object clone() {
	try {
	    LightMap<K,V> newmap = (LightMap<K,V>) super.clone();
	    newmap.root = copy_tree(root);
	    return newmap;
	} catch(CloneNotSupportedException e) {
	    throw new InternalError();
	}
    }


    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof Map/*<K,V>*/))
	    return false;
	
	Map m2 = (Map) o;

	Set<Map.Entry<K,V>> set1 = this.entrySet();
	Set<Map.Entry<K,V>> set2 = m2.entrySet();

	// two maps are equal if they have the same set of entries
	return set1.equals(set2);
    }


    public int hashCode() {
	return cachedHashCode;
    }
    private int cachedHashCode = 0; // computed incrementally


    private static class BinTreeNode<K,V> implements Map.Entry<K,V>, java.io.Serializable {
	final K key;
	V value;
	final int keyHashCode;

	BinTreeNode<K,V> left  = null;
	BinTreeNode<K,V> right = null;

	BinTreeNode(final K key, final V value) {
	    this.key    = key;
	    this.value  = value;
	    keyHashCode = key.hashCode();
	}

	public String toString() {
	    return "<" + key + "," + value + ">";
	}

	// implementation of the Map.Entry methods
	public K getKey() { return key; }
	public V getValue() { return value; }
	public V setValue(V value) { 
	    V oldValue = this.value;
	    this.value = value;
	    return oldValue;
	}
	public int hashCode() {
	    return 
		((key == null)   ? 0 : keyHashCode) ^
		((value == null) ? 0 : value.hashCode());
	}
	public boolean equals(Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if(!(o instanceof Map.Entry/*<K,V>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;

	    Map.Entry<K,V> e2 = (Map.Entry<K,V>) o;
	    return 
		(key == null ?
		 e2.getKey() == null : key.equals(e2.getKey()))  &&
		(value == null ?
		 e2.getValue()==null : value.equals(e2.getValue()));
	}
    }

    private final BinTreeNav<BinTreeNode<K,V>> binTreeNav = new BinTreeNav<BinTreeNode<K,V>>() {
	public BinTreeNode<K,V> left(BinTreeNode<K,V> node) {
	    return node.left;
	}
	public BinTreeNode<K,V> right(BinTreeNode<K,V> node) {
	    return node.right;
	}
    };

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[");
	build_str(root, buffer);
	buffer.append(" ]");
	return buffer.toString();
    }

    private void build_str(final BinTreeNode node, final StringBuffer buffer) {
	if(node == null) return;
	build_str(node.left,  buffer);
	buffer.append(" <" + node.key + "," + node.value + ">");
	build_str(node.right, buffer);
    }

}

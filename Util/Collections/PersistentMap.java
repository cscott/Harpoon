// PersistentTree.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Collections.UnmodifiableIterator;

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
 * <code>PersistentMap</code> implements a persistent map, based on a
 * binary search tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentMap.java,v 1.4 2002-08-30 22:39:56 cananian Exp $
 */
public class PersistentMap<K,V>  {
    final PersistentTreeNode<K,V> root;
    final Comparator<K> c;

    /** Creates an empty <code>PersistentMap</code> whose
     *  key objects will all implement <code>java.lang.Comparable</code>. 
     */
    public PersistentMap() {
	// cast below is safe iff T implements Comparable.
	this((Comparator)Default.comparator);
    }
    /** Creates an empty <code>PersistentMap</code> whose
     *  key objects are ordered by the given <code>Comparator</code>.
     */
    public PersistentMap(Comparator<K> c) {
	this(null, c);
    }
    /** Creates a <code>PersistentMap</code> from a root <code>Node</code>
     *  and a <code>Comparator</code>.*/
    private PersistentMap(PersistentTreeNode<K,V> root, Comparator<K> c) {
	this.root = root; this.c = c;
    }

    /** Determines if this <code>PersistentMap</code> has any mappings. */
    public boolean isEmpty() { return (root==null); }

    /** Count the number of key->value mappings in this
     *  <code>PersistentMap</code>. */
    public int size() { return PersistentTreeNode.size(root); }

    /** Creates and returns a new <code>PersistantMap</code> identical to
     *  this one, except it contains a mapping from <code>key</code> to
     *  <code>value. */
    public PersistentMap<K,V> put(K key, V value) {
	PersistentTreeNode<K,V> new_root =
	    PersistentTreeNode.put(this.root, this.c, key, value);
	return (this.root == new_root) ? this :
	    new PersistentMap<K,V>(new_root, c);
    }
    /** Gets the value which <code>key</code> maps to. */
    public V get(K key) {
	PersistentTreeNode<K,V> np = 
	    PersistentTreeNode.get(this.root, this.c, key);
	return (np==null)?null:np.value;
    }
    /** Determines if there is a mapping for the given <code>key</code>. */
    public boolean containsKey(K key) {
	return (PersistentTreeNode.get(this.root, this.c, key)!=null);
    }

    /** Make a new <code>PersistentMap</code> identical to this one,
     *  except that it does not contain a mapping for <code>key</code>. */
    public PersistentMap<K,V> remove(K key) {
	PersistentTreeNode<K,V> new_root = 
	    PersistentTreeNode.remove(this.root, this.c, key);
	return (this.root == new_root) ? this :
	    new PersistentMap<K,V>(new_root, c);
    }
    
    /** Human-readable representation of the map. */
    public String toString() { return asMap().toString(); }

    /*---------------------------------------------------------------*/
    /** <code>java.util.Collection</code>s view of the mapping. */
    public Map<K,V> asMap() {
	return new AbstractMap<K,V>() {
	    public boolean containsKey(Object key) {
		// ack, not safe is key is not a K!
		return PersistentMap.this.containsKey((K)key);
	    }
	    public V get(Object key) {
		// ack, not safe is key is not a K!
		return PersistentMap.this.get((K)key);
	    }
	    public boolean isEmpty() {
		return PersistentMap.this.isEmpty();
	    }
	    public int size() {
		return PersistentMap.this.size();
	    }
	    public Set<Map.Entry<K,V>> entrySet() {
		return new AbstractSet<Map.Entry<K,V>>() {
		    public int size() {
			return PersistentMap.this.size();
		    }
		    public Iterator<Map.Entry<K,V>> iterator() {
			final Stack<PersistentTreeNode<K,V>> s =
			    new Stack<PersistentTreeNode<K,V>>();
			if (root!=null) s.push(root);

			return new UnmodifiableIterator<Map.Entry<K,V>>() {
			    public boolean hasNext() {
				return !s.isEmpty();
			    }
			    public Map.Entry<K,V> next() {
				if (s.isEmpty())
				    throw new NoSuchElementException();
				final PersistentTreeNode<K,V> n = s.pop();
				if (n.right!=null) s.push(n.right);
				if (n.left!=null)  s.push(n.left);
				return (Map.Entry<K,V>) n;
			    }
			};
		    }
		};
	    }
	};
    }
}

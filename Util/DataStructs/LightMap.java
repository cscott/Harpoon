// LightMap.java, created Sun Mar 19 15:46:56 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

import harpoon.Util.Util;

/**
 * <code>LightMap</code> is a very lightweight implementation of
 the <code>java.util.Map</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: LightMap.java,v 1.1.2.2 2000-03-25 06:51:25 salcianu Exp $
 */
public class LightMap implements Map {

    /** If this flag is on, at each call to <code>put</code>,
	we explicitly check that it is not a redefinition of the mapping
	attached to a key. When you are absolutely sure your code doesn't
	redefine any mapping, you can gain some speed by switching this
	flag off. */
    public static final boolean CAUTION = true;
 
    // the number of mappings in this map
    private int size = 0;
    // the root of the binary tree used to store the mapping
    private BinTreeNode root = null;

    /** Creates a <code>LightMap</code>. */
    public LightMap() {}

    public final int size(){
	return size;
    }

    public final boolean isEmpty(){
	return size != 0;
    }

    /** Returns <code>true</code> if this map contains a mapping
	for the specified key. */
    public final boolean containsKey(Object key){
	return get(key) != null;
    }

    /** Unsupported yet. */
    public final boolean containsValue(Object value)
	throws UnsupportedOperationException{
	throw new UnsupportedOperationException();
    }

    /** Returns the value to which this map maps the specified key. */
    public final Object get(Object key){
	BinTreeNode p = root;
	int key_hash_code = key.hashCode();

	while(p != null)
	    if(key_hash_code < p.keyHashCode)
		p = p.left;
	    else
		if(key_hash_code > p.keyHashCode)
		    p = p.right;
		else
		    if(p.key.equals(key))
			return p.value;
		    else p = p.right;

	return null;
    }

    /** Associates the specified value with the specified key in this map. */
    public final Object put(Object key, Object value){
	BinTreeNode prev = null;
	BinTreeNode p = root;
	int key_hash_code = key.hashCode();

	while(p != null){
	    // put CAUTION false for maximal performance
	    if(CAUTION)
		Util.assert(!p.key.equals(key),
			    "Attempt to redefine a mapping!");
	    prev = p;
	    if(key_hash_code < p.keyHashCode)
		p = p.left;
	    else p = p.right;
	}

	size++;

	BinTreeNode node = new BinTreeNode(key,value);
	if(prev == null){
	    root = node;
	    return null;
	}

	if(key_hash_code < prev.keyHashCode)
	    prev.left = node;
	else prev.right = node;
	return null;
    }

    /** Unsupported. */
    public final Object remove(Object k) throws UnsupportedOperationException{
	throw new UnsupportedOperationException();
    }

    /** Unsupported. */
    public final void putAll(Map map) throws UnsupportedOperationException{
	throw new UnsupportedOperationException();
    }

    /** Unsupported. */
    public final void clear() throws UnsupportedOperationException{
	throw new UnsupportedOperationException();
    }

    /** Returns a collection view of the values contained in this map. */
    public final Collection values(){
	final Set vset = new HashSet();
	get_values(root,vset);
	return vset;
    }

    // recursively explore the tree of mappings and gather all the
    // values in the set vset
    private static final void get_values(final BinTreeNode node,
					 final Set vset){
	if(node == null) return;
	vset.add(node.value);
	get_values(node.left,  vset);
	get_values(node.right, vset);
    }

    /** Returns the set of entries of this map. The result is a 
	<code>Set</code> of <code>Map.Entry</code>. */
    public final Set entrySet() throws UnsupportedOperationException{
	final Set eset = new HashSet();
	get_entries(root, eset);
	return eset;
    }

    // recursively explore the tree of mappings and gather all the
    // entries in the set eset
    private static final void get_entries(final BinTreeNode node,
					  final Set eset){
	if(node == null) return;
	eset.add(new Entry(node.key, node.value));
	get_entries(node.left,  eset);
	get_entries(node.right, eset);
    }

    private static class Entry implements Map.Entry{
	Object key;
	Object value;

	Entry(Object key, Object value){
	    this.key   = key;
	    this.value = value;
	}

	public Object getKey(){ return key; }
	public Object getValue() { return value; }

	public Object setValue(Object value)
	    throws UnsupportedOperationException{
	    throw new UnsupportedOperationException();
	}

	public boolean equals(Object o){
	    Entry e2 = (Entry) o;
	    return key.equals(e2.key) && value.equals(e2.value);
	}

	public int hashCode(){
	    return key.hashCode() + value.hashCode();
	}
    }

    /** Unsupported. */
    public final Set keySet() throws UnsupportedOperationException{
	throw new UnsupportedOperationException();
    }

    //public final boolean equals(){
    //}

    //public final int hashCode(){
    //}

    private static class BinTreeNode{
	final Object key;
	final Object value;
	final int keyHashCode;

	BinTreeNode left;
	BinTreeNode right;

	BinTreeNode(final Object key, final Object value){
	    this.key    = key;
	    this.value  = value;
	    keyHashCode = key.hashCode();
	    left  = null;
	    right = null;
	}
    }

}

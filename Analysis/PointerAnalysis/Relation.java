// Relation.java, created Tue Jan 11 14:52:48 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import java.util.Arrays;


/**
 * <code>Relation</code> implements a generalized (i.e. one to many) relation.
 * It is similar with harpoon.Util.Collections.MultiMap but it is simpler
 * and better tailored for the implementation of the Pointer Analysis
 * algorithm.
 *
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Relation.java,v 1.1.2.10 2000-02-11 06:12:07 salcianu Exp $
 */
public class Relation{
    
    /** The top-level <code>Hashtable</code>. */
    Hashtable hash;

    /** Creates an empty <code>Relation</code>. */
    public Relation() {
        hash = new Hashtable();
    }

    /** Adds the relation from <code>key</code> to <code>value</code>. 
     *  Returns <code>true</code> if the new relation is bigger. */
    public boolean add(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) hash.put(key,set = new HashSet());
	return set.add(value);
    }

    /** Adds a relation from <code>key</code> to each element of the set
     *  <code>values</code>. Returns <code>true</code> if the new relation
     *  is bigger. */
    public boolean addAll(Object key, Set values){
	if(values.isEmpty()) return false;
	HashSet set = (HashSet)hash.get(key);
	if(set==null) hash.put(key,set = new HashSet());
	return set.addAll(values);
    }

    /** Removes the relation between <code>key</code> and 
     *  <code>value</code>. */ 
    public void remove(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) return;
	set.remove(value);
	if(set.isEmpty())
	    hash.remove(key);
    }
    
    /** Removes all the relations starting from <code>key</code> i.e.
     *  all <code>key->value</code> pairs. */
    public void removeAll(Object key){
	hash.remove(key);
	//HashSet set = (HashSet)hash.get(key);
	//if(set==null) return;
	//set.clear();
    }

    /** Checks the existence of the relation <code>key</code> ->
     *  <code>value</code>. */
    public boolean contains(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) return false;
	return set.contains(value);
    }

    /** Checks the existence of the <code>key</code> key in
     * <code>this</code> relation. */
    public boolean containsKey(Object key){
	return hash.containsKey(key);
    }

    /** Returns the image of <code>key</code> through <code>this</code>
     *  relation; set version. */
    public Set getValuesSet(Object key){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) return Collections.EMPTY_SET;
	return set;
    }

    /** Returns the image of <code>key</code> through <code>this</code>
     *  relation; iterator version. */
    public Iterator getValues(Object key){
	return getValuesSet(key).iterator();
    }

    /** Returns all the keys of this relation (i.e. all the objects
     *  <code>o</code> such that there is at least one relation of
     *  the type o->o1 */ 
    public Enumeration keys(){
	return hash.keys();
    }

    /** Similar with <code>keys()</code> but returns a <code>Set</code>
     *  view of the keys instead of an <code>Enumeration</code>. */
    public Set keySet(){
	return hash.keySet();
    }

    /** Combines <code>this</code> relation with a new one.
	A <code>null</code> parameter is considered to be an empty relation. */
    public void union(Relation rel){
	if(rel==null) return;
	Enumeration enum = rel.keys();
	while(enum.hasMoreElements()){
	    Object o = enum.nextElement();
	    addAll(o,rel.getValuesSet(o));
	}
    }


    /** Checks the equality of two relations */
    public boolean equals(Object o){
	if(o==null) return false;
	Relation r2 = (Relation)o;
	Set set1 = keySet();
	Set set2 = r2.keySet();
	if(!set1.equals(set2)) return false;
	Iterator it = set1.iterator();
	while(it.hasNext()){
	    Object obj = it.next();
	    Set set_a = getValuesSet(obj);
	    Set set_b = r2.getValuesSet(obj);
	    if(!set_a.equals(set_b)) return false;
	}
	return true;
    }

    /** Private constrcutor for <code>select</code> and <code>clone</code>. */
    private Relation(Hashtable _hash){
	hash = _hash;
    }


    /** Returns the subrelation of <code>this</code> relation that contains
	only the keys that appear in <code>set</code>. */
    public Relation select(Set set){
	Relation rel2 = new Relation();
	Enumeration enum = keys();
	while(enum.hasMoreElements()){
	    Object ob = enum.nextElement();
	    rel2.addAll(ob,getValuesSet(ob));
	}
	return rel2;
    }


    /** Creates a new, independent relation (i.e. the operations on 
     *  the new relation won't affect the old one). */
    public Object clone(){
	Hashtable new_hash = new Hashtable();
	Iterator it = hash.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry) it.next();
	    new_hash.put(entry.getKey(),((HashSet)entry.getValue()).clone());
	}
	return new Relation(new_hash);
    }

    /** Pretty-print function for debug.
	<code>rel1.equals(rel2) <==> rel1.toString().equals(rel2.toString())</code> */
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("{");
	
	Set keyset = keySet();
	Object[] keys = Debug.sortedSet(hash.keySet());
	for(int i = 0 ; i < keys.length ; i++ ){
	    Object key = keys[i];
	    buffer.append("\n  ");		
	    buffer.append(key);
	    buffer.append(" -> ");
	    buffer.append(Debug.stringImg(getValuesSet(key)));
	}
	
	buffer.append("\n }\n");
	
	return buffer.toString();
    }

}























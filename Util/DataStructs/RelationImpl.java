// RelationImpl.java, created Tue Jan 11 14:52:48 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

import java.util.Hashtable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

import java.util.Arrays;

import harpoon.Util.Util;
import harpoon.Analysis.PointerAnalysis.Debug;
import harpoon.Util.PredicateWrapper;

/**
 * <code>RelationImpl</code> is a heavy-weight implementation of the
 * <code>Relation</code> interface: it is basically a Hashtable from keys
 * to HashSet's of values. It is good for very big relations but consumes
 * lots of memory.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: RelationImpl.java,v 1.1.2.3 2001-06-17 22:36:59 cananian Exp $
 */
public class RelationImpl implements Relation, java.io.Serializable {
    
    /** The top-level <code>Hashtable</code>. */
    Hashtable hash;


    /** Creates an empty <code>Relation</code>. */
    public RelationImpl() {
        hash = new Hashtable();
    }


    public Relation getEmptyRelation() {
	return new RelationImpl();
    }


    public boolean add(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) hash.put(key,set = new HashSet());
	return set.add(value);
    }


    public boolean addAll(Object key, Collection values) {
	if(values.isEmpty()) return false;
	HashSet set = (HashSet) hash.get(key);
	if(set==null)
	    hash.put(key,set = new HashSet());
	return set.addAll(values);
    }

    public void remove(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) return;
	set.remove(value);
	if(set.isEmpty())
	    hash.remove(key);
    }

    public void removeAll(Object key, Collection values){
	Iterator it_values = values.iterator();
	while(it_values.hasNext())
	    remove(key, it_values.next());
    }
    

    public void removeKey(Object key){
	hash.remove(key);
    }


    public void removeKeys(PredicateWrapper predicate){
	Set keys = hash.keySet();
	Iterator it_keys = keys.iterator();
	while(it_keys.hasNext()){
	    Object obj = it_keys.next();
	    if(predicate.check(obj))
		it_keys.remove();
	}
    }


    private boolean removeValues(Object key, PredicateWrapper predicate){
	Set values = (Set) hash.get(key);
	Iterator it_values = values.iterator();
	while(it_values.hasNext()){
	    Object value = it_values.next();
	    if(predicate.check(value))
		it_values.remove();
	}
	return values.isEmpty();
    }

    public void removeValues(PredicateWrapper predicate){
	Iterator it_keys = keys().iterator();
	while(it_keys.hasNext())
	    if(removeValues(it_keys.next(), predicate))
		it_keys.remove();
    }


    public void removeObjects(PredicateWrapper predicate){
	removeKeys(predicate);
	removeValues(predicate);
    }


    public boolean contains(Object key, Object value){
	HashSet set = (HashSet)hash.get(key);
	if(set==null) return false;
	return set.contains(value);
    }


    public boolean containsKey(Object key){
	return hash.containsKey(key);
    }


    public boolean isEmpty(){
	return hash.isEmpty();
    }


    public Set getValues(Object key){
	HashSet set = (HashSet)hash.get(key);
	if(set==null)
	    return Collections.EMPTY_SET;
	return set;
    }


    public Set keys() {
	return hash.keySet();
    }


    public Set values() {
	Set vals = new HashSet();
	for(Iterator it = keys().iterator(); it.hasNext(); )
	    vals.addAll(getValues(it.next()));
	return vals;
    }


    public void union(Relation rel){
	if(rel==null) return;
	for(Iterator it = rel.keys().iterator(); it.hasNext(); ) {
	    Object o = it.next();
	    addAll(o, rel.getValues(o));
	}
    }


    public boolean equals(Object o){
	if(o==null) return false;
	Relation r2 = (Relation)o;
	Set set1 = (Set) keys();
	Set set2 = (Set) r2.keys();
	if(!set1.equals(set2)) return false;

	for(Iterator it = set1.iterator();it.hasNext(); ) {
	    Object obj = it.next();
	    Set set_a = (Set) getValues(obj);
	    Set set_b = (Set) r2.getValues(obj);
	    if(!set_a.equals(set_b)) return false;
	}
	return true;
    }


    // Private constructor for <code>clone</code>.
    private RelationImpl(Hashtable _hash){
	hash = _hash;
    }


    public Relation select(Collection selected_keys){
	Relation rel2 = new RelationImpl();
	for(Iterator it = keys().iterator(); it.hasNext(); ) {
	    Object key = it.next();
	    if(!selected_keys.contains(key)) continue;
	    rel2.addAll(key, getValues(key));
	}
	return rel2;
    }


    public void forAllEntries(RelationEntryVisitor visitor){
	for(Iterator itk = hash.keySet().iterator(); itk.hasNext(); ) {
	    Object key = itk.next();
	    for(Iterator itv = getValues(key).iterator(); itv.hasNext(); ) {
		Object value = itv.next();
		visitor.visit(key, value);
	    }
	}
    }

    /** Creates a new, independent relation (the operations on 
	the new relation won't affect the old one). */
    public Object clone(){
	Hashtable new_hash = new Hashtable();
	Iterator it = hash.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry) it.next();
	    new_hash.put(entry.getKey(),((HashSet)entry.getValue()).clone());
	}
	return new RelationImpl(new_hash);
    }


    /** Pretty-print function for debug.
	<code>rel1.equals(rel2) <==> rel1.toString().equals(rel2.toString())</code> */
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("{");
	
	Set keyset = (Set) keys();
	Object[] keys = Debug.sortedSet(hash.keySet());
	for(int i = 0 ; i < keys.length ; i++ ){
	    Object key = keys[i];
	    buffer.append("\n  ");		
	    buffer.append(key);
	    buffer.append(" -> ");
	    buffer.append(Debug.stringImg(getValues(key)));
	}
	
	buffer.append("\n }\n");
	
	return buffer.toString();
    }

    public void revert(final Relation result) {
	forAllEntries(new RelationEntryVisitor() {
		public void visit(Object key, Object value) {
		    result.add(value, key);
		}
	    });
    }
}























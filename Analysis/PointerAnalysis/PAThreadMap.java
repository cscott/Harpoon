// PAThreadMap.java, created Sun Jan  9 15:49:32 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.lang.System;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import java.util.Arrays;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;

/**
 * <code>PAThreadMap</code> implements the parallel thread map
 * (i.e. the "tau" function from the algorithm). For each thread node
 * n, tau(n) is a conservative approximation of the number of instances
 * of nT that could run in parallel with the current thread.
 * tau(n) is a number from the set {0,1,2} where 0 stands for no instance,
 * 1 for at most one instance and 2 for possibly multiple instances.
 * 
 * <code>PAThreadMap</code> is more or less a <code>Hashtable</code> with
 * some access functions to enforce the new rules for addition and
 * substraction.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAThreadMap.java,v 1.3.2.1 2002-02-27 08:32:06 cananian Exp $
 */
public class PAThreadMap implements java.io.Serializable {

    static private final Integer ONE  = new Integer(1);
    static private final Integer TWO  = new Integer(2);

    private Hashtable hash;
    
    /** Creates a <code>PAThreadMap</code>. */
    public PAThreadMap() {
        hash = new Hashtable();
    }

    // Look at the value attached to the thread node n
    // (i.e. tau(n)
    public int getValue(PANode n){
	Integer v = (Integer)hash.get(n);

	if(v != null){
	    return ((Integer) hash.get(n)).intValue();
	}
	else return 0;
    }

    /** Increments the value attached to <code>n</code> */
    public void inc(PANode n){
	Integer v = (Integer)hash.get(n);

	if(v == null) hash.put(n,ONE);
	if(v == ONE) hash.put(n,TWO);
    }


    /** Convenient function that calls <code>inc</code> for all the
	nodes from <code>set</code> */
    public void incAll(Set nodes){
	Iterator it = nodes.iterator();
	while(it.hasNext()){
	    PANode node = (PANode) it.next();
	    inc(node);
	}
    }
    
    /** Decrements the value attached to <code>n</code> */
    public void dec(PANode n){
	Integer v = (Integer)hash.get(n);

	if(v==null){
	    System.err.println("Error in PAThreadMap: cannot do 0--\n");
	    System.exit(1);
	}

	// another alternative would be to attach to n a special object
	// ZERO. However, I decided to remove the mapping for n - in this
	// way, we don't create two values for expressing tau(n)=0 (null
	// and ZERO) and decrease the number of keys from the map.
	if(v==ONE) hash.remove(n);
	if(v==TWO) hash.put(n,TWO);
    }

    /** Checks whether the thread "nt" is started or not. */
    public boolean isStarted(PANode nt){
	return hash.containsKey(nt);
    }

    /** Add a positive <code>delta</code> to the value attached to a node. */
    public void add(PANode n, int delta){
	assert delta>0 : ("PAThreadMap.add: delta should be > 0");
	int new_tau = getValue(n) + delta;
	if( new_tau > 1 )
	    hash.put(n,TWO);
	else
	    hash.put(n,ONE);
    }


    /** Set to zero the value attached to the node <code>n</code>. */
    public void setToZero(PANode n){
	hash.remove(n);
    }

    /** Returns all the thread nodes <code>nT</code> that have assigned
     * a non-zero count (i.e. tau(nT) > 0 in the algorithm) */ 
    public Enumeration activeThreads(){
	return hash.keys();
    }

    /** Returns all the thread nodes <code>nT</code> that have assigned
     * a non-zero count (i.e. tau(nT) > 0 in the algorithm) */ 
    public Set activeThreadSet(){
	return hash.keySet();
    }


    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> thread map. */
    public void remove(Set set){
	Iterator it_nodes = set.iterator();
	while(it_nodes.hasNext()){
	    PANode node = (PANode) it_nodes.next();
	    hash.remove(node);
	}
    }


    /** <code>join</code> combines two <code>PAThreadMap</code>s in
     *  a control-flow join poin */
    public void join(PAThreadMap tau2){
	Enumeration e = tau2.activeThreads();
	while(e.hasMoreElements()){
	    PANode n = (PANode) e.nextElement();
	    int count1 = getValue(n);
	    int count2 = tau2.getValue(n);
	    if(count2 > count1)
		switch(count2){
		case 1: 
		    hash.put(n,ONE);
		    break;
		case 2:
		    hash.put(n,TWO);
		    break;
		default:
		    System.err.println("PAThreadMap.join: Strange value!");
		}
	}
    }

    /** Inserts the image of <code>tau2</code> through the <code>mu</code>
	mapping into <code>this</code> object.
	Forall <code>node</code> in <code>tau2.activeThreads</code>,
	forall <code>node2</code> in <code>mu(node)</code>,
	<code>tau(node2) += tau2(node)</code>. */
    public void insert(PAThreadMap tau2, Relation mu){
	Enumeration e = tau2.activeThreads();
	while(e.hasMoreElements()){
	    PANode node = (PANode) e.nextElement();
	    int tau_node = tau2.getValue(node);
	    Iterator it_new_node = mu.getValues(node).iterator();
	    while(it_new_node.hasNext()){
		PANode new_node = (PANode) it_new_node.next();
		add(new_node,tau_node);
	    }
	}
    }

    /* Specializes <code>this</code> <code>PAThreadMap</code> according to
       <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public PAThreadMap specialize(final Map map){
	final PAThreadMap tau2 = new PAThreadMap();

	for(Iterator it = hash.entrySet().iterator(); it.hasNext(); ){
	    Map.Entry entry = (Map.Entry) it.next();
	    PANode node2 = PANode.translate((PANode) entry.getKey(), map);

	    tau2.hash.put(node2, (Integer) entry.getValue());
	}
	
	return tau2;
    }

    /** Private constructor used by <code>clone</code> and  
     * <code>keepTheEssential</code> */
    private PAThreadMap(Hashtable _hash){
	hash = _hash;
    }
    
    /** <code>clone</code> creates a copy of <code>this</code> thread map;
     *	by doing a simple shallow copy of the <code>hash<code> field. 
     */
    public Object clone(){
	return new PAThreadMap((Hashtable)hash.clone());
    }

    /** Produces a new <code>PAThreadMap</code> containing only the thread
     * nodes that appear in <code>essential_nodes</code>. */
    public PAThreadMap keepTheEssential(Set essential_nodes){
	Hashtable _hash = new Hashtable();
	Iterator it = hash.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry)it.next();
	    PANode node = (PANode)entry.getKey();    
	    if(essential_nodes.contains(node))
		_hash.put(node,entry.getValue());
	}
	return new PAThreadMap(_hash);
    }

    /** Checks the equality of two <code>PAThreadMap</code>s */
    public boolean equals(Object o){
	if(o==null) return false;
	PAThreadMap tau2 = (PAThreadMap) o;
	Set set1 = activeThreadSet();
	Set set2 = tau2.activeThreadSet();
	// two PAThreadMap's are equal if they contain the same keys
	if(!set1.equals(set2)){
	    ///System.out.println("PAThreadMap.equals(): different keySet's");
	    ///System.out.print("Old " + tau2);
	    ///System.out.print("New " + this);
	    return false;
	}
	Iterator it = set1.iterator();
	while(it.hasNext()){
	    PANode node = (PANode) it.next();
	    // and assign the same values to them
	    if(getValue(node)!=tau2.getValue(node)){
		///System.out.println("PAThreadMap.equals(): " + 
		///		   "different values assigned to " + node +
		///		   " v1=" + tau2.getValue(node) + 
		///		   " v2=" + getValue(node));
		return false;
	    }
	}
	return true;
    }

    /** Pretty print function for debug purposes.
     <code>tau1.equals(tau2) <==> tau1.toString().equals(tau2.toString()).</code> */
    public String toString(){
	StringBuffer buffer = new StringBuffer("Parallel Thread Map:\n");

	Object[] active_threads = Debug.sortedSet(hash.keySet());
	
	for(int i = 0 ; i < active_threads.length ; i++){
	    Object thread = active_threads[i];
	    Object value  = hash.get(thread);
	    buffer.append("  " + thread + " -> " + value + "\n");
	}
	
	return buffer.toString();    
    }

}


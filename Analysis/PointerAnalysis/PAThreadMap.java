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
 * @version $Id: PAThreadMap.java,v 1.1.2.6 2000-02-09 05:23:42 salcianu Exp $
 */
public class PAThreadMap{

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

    /** <code>join</code> combines two <code>PAThreadMap</code>s in
     *  a control-flow join poin */
    public void join(PAThreadMap tau2){
	Enumeration e = tau2.activeThreads();
	while(e.hasMoreElements()){
	    PANode n = (PANode) e.nextElement();
	    int count = tau2.getValue(n);
	    if(count == 2)
		hash.put(n,TWO);
	    else
		inc(n);
	}
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
     * nodes that appear in <code>essential_nodes</code>, too */
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

    /** Pretty print function for debug purposes. */
    public String toString(){
	StringBuffer buffer = new StringBuffer("Parallel Thread Map:\n");
	Iterator it = hash.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry e = (Map.Entry)it.next();
	    buffer.append("  " + e.getKey().toString() + " -> " + 
			  e.getValue().toString() + "\n");
	}
	return buffer.toString();
    }

}

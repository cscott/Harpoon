// ActionRepository.java, created Mon Feb  7 15:03:29 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * <code>ActionRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ActionRepository.java,v 1.1.2.2 2000-02-09 05:23:42 salcianu Exp $
 */
public class ActionRepository {
    
    static PANode THIS_THREAD = new PANode(PANode.INSIDE);

    /** Creates a <code>ActionRepository</code>. */
    public ActionRepository() {
        alpha_ld   = new HashSet();
	pi_ld      = new Relation();
	alpha_sync = new Relation();
	pi_sync    = new Hashtable();
    }


    // Set<PALoad>
    HashSet  alpha_ld;
    // Relation<PANode,PALoad>
    Relation pi_ld;


    // Adds a <code>ld</code> action done by the current thread with
    // no active threads.
    // public void add_ld(PANode n1, String f, PANode n2){
    // add_ld(n1,f,n2,THIS_THREAD,null);
    // }

    // public void add_ld(PANode n1, String f, PANode n2, PANode nt){
    // add_ld(n1,f,n2,nt,null);
    // }

    public void add_ld(PANode n1, String f, PANode n2, PANode nt,
			 Set active_threads){
	PALoad load = new PALoad(n1,f,n2,nt);

	alpha_ld.add(load);

	if(active_threads == null) return;
	
	Iterator it = active_threads.iterator();
	while(it.hasNext())
	    pi_ld.add((PANode)it.next(), load);
    }

    private void add_ld(Set set_n1, String f, PANode n2, PANode nt,
			 Set active_threads){
	Iterator it_n1 = set_n1.iterator();
	while(it_n1.hasNext()){
	    PANode n1 = (PANode) it_n1.next();
	    add_ld(n1,f,n2,nt,active_threads);
	}
    }

    private void add_ld(Set set_n1, String f, PANode n2, Set set_nt,
			 Set active_threads){

	//System.out.println("set_n1 = " + set_n1);
	//System.out.println("f      = " + f);
	//System.out.println("n2     = " + n2);
	//System.out.println("set_nt = " + set_nt);
	//System.out.println("active_threads = " + active_threads);

	Iterator it_nt = set_nt.iterator();
	while(it_nt.hasNext()){
	    PANode nt = (PANode) it_nt.next();
	    add_ld(set_n1,f,n2,nt,active_threads);
	}
    }


    // Relation<PANode,PANode>;   n -> nt (nt does a sync on n)
    Relation  alpha_sync;
    // Hashtable<PANode,Relation<PANode,PANode>>
    // nt2 -> n -> nt1 (nt1 does a sync on n in // with the thread nt2)
    Hashtable pi_sync;
    
    // public void add_sync(PANode n){
    //	add_sync(n,THIS_THREAD,null);
    // }

    // public void add_sync(PANode n, PANode nt){
    // add_sync(n,nt,null);
    // }

    public void add_sync(PANode n, PANode nt, Set active_threads){
	alpha_sync.add(n,nt);
	
	if(active_threads == null) return;

	Iterator it = active_threads.iterator();
	while(it.hasNext()){
	    PANode nt2 = (PANode) it.next();
	    
	    Relation rel = (Relation) pi_sync.get(nt2);
	    if(rel == null){
		rel = new Relation();
		pi_sync.put(nt2,rel);
	    }

	    rel.add(n,nt);
	}
    }

    private void add_sync(Set set_n, Set set_nt, Set active_threads){

	//System.out.println("set_n  = " + set_n);
	//System.out.println("set_nt = " + set_nt);
	//System.out.println("active_threads = " + active_threads);

	Iterator it_n = set_n.iterator();
	while(it_n.hasNext()){
	    PANode n = (PANode) it_n.next();
	    Iterator it_nt = set_nt.iterator();
	    while(it_nt.hasNext()){
		PANode nt = (PANode) it_nt.next();
		add_sync(n,nt,active_threads);
	    }
	}
    }

    /** Checks the equality of <code>ActionRepository</code>s. */
    public boolean equals(Object o){
	ActionRepository ar2 = (ActionRepository)o;
	return
	    alpha_ld.equals(ar2.alpha_ld) &&
	    alpha_sync.equals(ar2.alpha_sync) &&
	    pi_ld.equals(ar2.pi_ld) &&
	    pi_sync.equals(ar2.pi_sync);
    }

    /** Does the union of <code>this</code> action repository with \
	another one.
	This method is called in the control-flow join points. */
    public void join(ActionRepository ar2){
	// these first three are really easy
	alpha_ld.addAll(ar2.alpha_ld);
	pi_ld.union(ar2.pi_ld);
	alpha_sync.union(ar2.alpha_sync);
	// this one is a bit more difficult; we have to do it by hand
	Iterator  it = pi_sync.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry) it.next();
	    PANode nt2 = (PANode) entry.getKey();
	    Relation rel2 = (Relation) ar2.pi_sync.get(nt2);
	    Relation rel1 = (Relation) pi_sync.get(nt2);
	    if(rel1 == null)
		pi_sync.put(nt2,(Relation) rel2.clone());
	    else
		rel1.union(rel2);
	}
    }

    /** Translate all the actions and the parallel action relation
	into <code>ar2</code> according to the node map <code>mu</code>. */
    public void translateTheActions(ActionRepository ar2, Relation mu,
				    Set active_threads){
	// Add this "common-sense" rule to the mapping.
	mu.add(THIS_THREAD,THIS_THREAD);

	// Add the "ld" actions to ar2.
	Iterator it_alpha_ld = alpha_ld.iterator();
	while(it_alpha_ld.hasNext()){
	    PALoad load = (PALoad) it_alpha_ld.next();
	    if(mu.contains(load.n2,load.n2))
		ar2.add_ld(mu.getValuesSet(load.n1), load.f, load.n2,
			   mu.getValuesSet(load.nt), active_threads);
	}


	// Add the "sync" actions to ar2.
	Enumeration enum_n = alpha_sync.keys();
	while(enum_n.hasMoreElements()){
	    PANode n = (PANode) enum_n.nextElement();

	    HashSet set_nt = new HashSet();
	    Iterator it_nt = alpha_sync.getValues(n);
	    while(it_nt.hasNext()){
		PANode nt = (PANode) it_nt.next();
		set_nt.addAll(mu.getValuesSet(nt));
	    }

	    ar2.add_sync(mu.getValuesSet(n),set_nt,active_threads);
	}

	// Map the ordering info for the "ld" actions.
	Enumeration enum_nt2 = pi_ld.keys();
	while(enum_nt2.hasMoreElements()){
	    PANode nt2  = (PANode) enum_nt2.nextElement();
	    Set set_nt2 = mu.getValuesSet(nt2);
	    Iterator it_load = pi_ld.getValues(nt2);
	    while(it_load.hasNext()){
		PALoad load = (PALoad) it_load.next();
		ar2.add_ld(mu.getValuesSet(load.n1),
			   load.f,
			   load.n2,
			   mu.getValuesSet(load.nt),
			   set_nt2);
	    }
	}

	// Map the ordering info for the "sync" actions.
	// For each <sync,n,nt1> || nt2 in this action repository,
	// add <sync,mu(n),mu(nt1)> || mu(nt2) to ar2.
	enum_nt2 = pi_sync.keys();
	while(enum_nt2.hasMoreElements()){
	    PANode nt2 = (PANode) enum_nt2.nextElement();
	    Set set_nt2 = mu.getValuesSet(nt2);
	    Relation rel = (Relation) pi_sync.get(nt2);
	    enum_n = rel.keys();
	    while(enum_n.hasMoreElements()){
		PANode n = (PANode) enum_n.nextElement();
		Set set_n = mu.getValuesSet(n);
		Iterator it_nt1 = rel.getValues(n);
		while(it_nt1.hasNext()){
		    PANode nt1 = (PANode) it_nt1.next();
		    Set set_nt1 = mu.getValuesSet(nt1);
		    ar2.add_sync(set_n,set_nt1,set_nt2);
		}
	    }
	}
    }

    /** Private constructor for <code>clone</code>. */
    private ActionRepository(HashSet alpha_ld, Relation pi_ld,
			     Relation alpha_sync, Hashtable pi_sync){
	this.alpha_ld   = alpha_ld;
	this.pi_ld      = pi_ld;
	this.alpha_sync = alpha_sync;
	this.pi_sync    = pi_sync;
    }


    /** Produce an identical copy of <code>this</code> object. 
	The new object is totally independent from the old one: you can
	add/remove actions to/from it without affecting the original. */ 
    public Object clone(){
	HashSet  new_alpha_ld = (HashSet) alpha_ld.clone();
	Relation new_pi_ld    = (Relation) pi_ld.clone();
	Relation new_alpha_sync = (Relation) alpha_sync.clone();

	Hashtable new_pi_sync   = new Hashtable();
	Iterator  it = pi_sync.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry) it.next();
	    PANode nt2 = (PANode) entry.getKey();
	    Relation rel2 = (Relation) entry.getValue();
	    new_pi_sync.put(nt2,(Relation) rel2.clone());
	}

	return new ActionRepository(new_alpha_ld, new_pi_ld,
				    new_alpha_sync, new_pi_sync);
    }


    /** Pretty-printer for debug purposes. */
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append(" Alpha:\n");

	// string representation of the "ld" actions
	Iterator it_load = alpha_ld.iterator();
	while(it_load.hasNext())
	    buffer.append(" " + (PALoad) it_load.next() + "\n");

	// string representation of the "sync" actions
	Enumeration enum_keys = alpha_sync.keys();
	while(enum_keys.hasMoreElements()){
	    PANode n = (PANode) enum_keys.nextElement();
	    Iterator it_nt = alpha_sync.getValues(n);
	    while(it_nt.hasNext()){
		PANode nt = (PANode) it_nt.next();
		buffer.append(" < sync , " + n +
			      (nt!=THIS_THREAD?(" , " + nt):"") + " >\n");
	    }
	}

	buffer.append(" Pi:\n");

	// parallel info about the "ld" actions
	enum_keys = pi_ld.keys();
	while(enum_keys.hasMoreElements()){
	    PANode nt2 = (PANode) enum_keys.nextElement();
	    it_load = pi_ld.getValues(nt2);
	    while(it_load.hasNext()){
		PALoad load = (PALoad) it_load.next();
		buffer.append(" " + load + " || " + nt2 + "\n");
	    }
	}

	// parallel info about the "sync" actions
	enum_keys = pi_sync.keys();
	while(enum_keys.hasMoreElements()){
	    PANode nt2 = (PANode) enum_keys.nextElement();
	    Relation rel = (Relation) pi_sync.get(nt2);
	    Enumeration enum_n = rel.keys();
	    while(enum_n.hasMoreElements()){
		PANode n = (PANode) enum_n.nextElement();
		Iterator it_nt1 = rel.getValues(n);
		while(it_nt1.hasNext()){
		    PANode nt1 = (PANode) it_nt1.next();
		    buffer.append(" < sync , " + n + 
				  (nt1!=THIS_THREAD?(" , " + nt1):"") +
				  " > || " + nt2);
		}
	    }
	}
	
	return buffer.toString();
    }
}



// ActionRepository.java, created Mon Feb  7 15:03:29 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import harpoon.IR.Quads.CALL;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.ClassFile.HCodeElement;
import net.cscott.jutil.FilterIterator;

import harpoon.Util.PredicateWrapper;

import harpoon.Util.Util;

/**
 * <code>ActionRepository</code> merges together the <code>alpha</code> and
 <code>pi</code> sets from the original paper of Martin Rinard & John Whaley.
 More specifically, an <code>ActionRepository</code> maintains information
 about the actions executed by the analyzed part of the program (that's the
 meaning of alpha in the original paper) and about the
 ordering relation between these actions and the threads that are launched
 by the analyzed part (the pi structure).<br>
 Currently, only two kinds of actions are supported:
 <ul>
 <li><code>ld</code> - loading of a node by reading an outside edges;
 <li><code>sync</code> - synchronization (lock acquire/release) on a node. 
 </ul>
 The implementation has been specially optimized for these two types of actions
 and for the queries that are done for synchronization removal.<br>
 Of course, there is no problem in modifying it to support new types of
 actions.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ActionRepository.java,v 1.7 2005-08-17 23:34:00 salcianu Exp $
 */
public class ActionRepository implements java.io.Serializable {
    
    /** Fake thread node that stands for the main thread of the analyzed
	scope. This is used as the author thread for the actions done
	by the main thread of the analyzed procedure/scope, not by one of
	the threads it is starting. */
    public static final PANode THIS_THREAD = 
	NodeRepository.getNewNode(PANode.INSIDE);

    /** Creates a <code>ActionRepository</code>. */
    public ActionRepository() {
        alpha_ld   = new HashSet();
	pi_ld      = new LightRelation();
	alpha_sync = new LightRelation();
	pi_sync    = new Hashtable();
    }


    // Set<PALoad>
    HashSet  alpha_ld;
    // Relation<PANode,PALoad>; nt -> loads performed in // with nt 
    Relation pi_ld;

    // Relation<PANode,PASync>;   n -> syncs performed on n
    Relation  alpha_sync;
    // Hashtable<PANode,Relation<PANode,PASync>>
    // nt2 -> n -> syncs on n in // with nt2
    Hashtable pi_sync;


    /** Displays the differences between <code>this</code> action repository
	and <code>ar2</code>. For debug purposes. */
    private void print_difference(final ActionRepository ar2) {
	System.out.println("--- LD actions:");
	for(Object loadO : alpha_ld){
	    PALoad load = (PALoad) loadO;
	    if(!ar2.alpha_ld.contains(load))
		System.out.println(load);
	}

	System.out.println("--- LD || nt pairs:");
	pi_ld.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object key, Object value){
		    PANode nt   = (PANode) key;
		    PALoad load = (PALoad) value;
		    if(!ar2.pi_ld.contains(nt, load))
			System.out.println(load + " || " + nt);
		}
	    });

	System.out.println("--- SYNC actions:");
	alpha_sync.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object key, Object value){
		    PANode n    = (PANode) key;
		    PASync sync = (PASync) value;
		    if(!ar2.alpha_sync.contains(key, value))
			System.out.println(sync);
		}
	    });

	System.out.println("--- SYNC || nt pairs:");
	for(Iterator it = pi_sync.entrySet().iterator(); it.hasNext(); ) {
	    final Map.Entry entry = (Map.Entry) it.next();
	    final PANode nt2    = (PANode) entry.getKey();
	    final Relation rel  = (Relation) entry.getValue();
	    final Relation rel2 = (Relation) ar2.pi_sync.get(nt2);
	    rel.forAllEntries(new RelationEntryVisitor() {
		    public void visit(Object key, Object value) {
			PANode n    = (PANode) key;
			PASync sync = (PASync) value;
			if(!rel2.contains(n ,sync))
			    System.out.println(sync + " || " + nt2);
		    }
		});
	}
    }

    
    /** Shows the evolution from <code>ar2</code> to <code>this</code>:
	newly added stuff and removed stuff. Debug purposes. */
    public void show_evolution(final ActionRepository ar2){
	System.out.println("====== NEWLY ADDED STUFF:");
	print_difference(ar2);
	if(ar2 != null){
	    System.out.println("===== REMOVED STUFF:");
	    ar2.print_difference(this);
	}
    }


    // A. The most primitive operations on the action repository, they are
    // designed to be used internally; the usage pattern of the user has
    // been optimized into some specialized, high-level methods.

    // A.1. Operations on alpha, the repository of actions
    
    // Adds a "ld" action.
    private final void alpha_add_ld(PALoad load){
	alpha_ld.add(load);
    }

    // Adds a "sync" action.
    private final void alpha_add_sync(PASync sync){
	alpha_sync.add(sync.n, sync);
    }

    // A.2. Operations on pi, the relation of parallelism
    //  between actions and threads

    // Adds a parallel relation between a "ld" action and a thread. 
    private final void pi_add_ld(PALoad load, PANode nt){
	pi_ld.add(nt,load);
    }

    // Adds a parallel relation between a "sync" action and a thread.
    private final void pi_add_sync(PASync sync, PANode nt2){
	Relation rel = (Relation) pi_sync.get(nt2);
	if(rel == null){
	    rel = new LightRelation();
	    pi_sync.put(nt2,rel);
	}
	rel.add(sync.n, sync);
    }

    // B. User level function: usually when a user introduces an action,
    // he also gives a set of threads that are running in parallel: in a
    // single method, we update alpha and pi.

    /** Adds a "ld" action in parallel with all the 
	<code>active_threads</code>. */
    private final void add_ld(PALoad load, Set active_threads){
	alpha_add_ld(load);

	if(active_threads == null) return;
	
	Iterator it = active_threads.iterator();
	while(it.hasNext())
	    pi_add_ld(load, (PANode)it.next());
    }

    /** Adds a <code>ld</code> action.
	The thread <code>nt</code> read the outside edge from <code>n1</code>
	to <code>n2</code> through field <code>f</code>, in parallel with
	all the threads from <code>active_threads</code>. */
    public final void add_ld(PANode n1, String f, PANode n2, PANode nt,
		       Set active_threads){
	add_ld(new PALoad(n1,f,n2,nt),active_threads);
    }

    /** Convenient function used in the intra-procedural analysis,
	in the rule for a load operation.
	It iterates over <code>set_n1</code>, repeatedly calling
	<code>add_ld(n1,f,n2,nt,active_threads)</code> for each
	<code>n1</code> in <code>set_n1</code>. */
    public final void add_ld(Set set_n1, String f, PANode n2, PANode nt,
		       Set active_threads){
	if(set_n1.isEmpty()) return;
	for (Object n1O : set_n1){
	    PANode n1 = (PANode) n1O;
	    add_ld(n1,f,n2,nt,active_threads);
	}
    }

    /** Convenient function used by the inter-procedural and
	inter-thread analysis (when a thread node can be mapped to
	a set of nodes).
	It iterates over <code>set_nt</code>, repeatedly calling
	<code>add_ld(set_n1,f,n2,nt,active_threads)</code> for each
	<code>nt</code> in <code>set_nt</code>. */
    public final void add_ld(Set set_n1, String f, PANode n2, Set set_nt,
			Set active_threads){
	if(set_nt.isEmpty()) return;
	for (Object ntO : set_nt){
	    PANode nt = (PANode) ntO;
	    add_ld(set_n1,f,n2,nt,active_threads);
	}
    }


    /** Convenient function used by the inter-procedural and
	inter-thread analysis (when a load node can be mapped to
	a set of nodes).
	It iterates over <code>set_n2</code>, repeatedly calling
	<code>add_ld(set_n1,f,n2,set_nt,active_threads)</code> for each
	<code>n2</code> in <code>set_n2</code>. */
    public final void add_ld(Set set_n1, String f, Set set_n2, Set set_nt,
			Set active_threads){
	if(set_n2.isEmpty()) return;
	for (Object n2O : set_n2){
	    PANode n2 = (PANode) n2O;
	    add_ld(set_n1,f,n2,set_nt,active_threads);
	}
    }

    public final void add_ld(Set set_n1, String f, Set set_n2, PANode nt,
			     Set active_threads){
	if(set_n2.isEmpty()) return;
	for (Object n2O : set_n2){
	    PANode n2 = (PANode) n2O;
	    add_ld(set_n1,f,n2,nt,active_threads);
	}
    }


    /** Adds a <code>sync</code> action.
	The thread <code>nt</code> synchronized on <code>n</code> in
	parallel with all the threads from <code>active_threads</code>. */
    public final void add_sync(PASync sync, Set active_threads){
	alpha_add_sync(sync);
	
	if((active_threads == null) || active_threads.isEmpty()) return;

	if(PointerAnalysis.DEBUG2){
	    System.out.print("WONDERFUL: < sync , " + sync.n + " , " + 
			     sync.nt + " > || [");
	    
	    Iterator it2 = active_threads.iterator();
	    while(it2.hasNext())
		System.out.print(it2.next() + " ");
	    
	    System.out.println("]");
	}

	for (Object nt2O : active_threads){
	    PANode nt2 = (PANode) nt2O;
	    pi_add_sync(sync,nt2);
	}
    }

    /** Convenient fiunction for recording a set of <code>sync</code>
	actions in parallel with all the threads from the set
	<code>active_threads</code>. */
    public final void add_sync(Set syncs, Set active_threads){
	for(Iterator it = syncs.iterator(); it.hasNext(); )
	    add_sync((PASync) it.next(), active_threads);
    }
    // END of the "add" functions

    /** Checks the equality of two <code>ActionRepository</code>s. */
    public final boolean equals(Object o){
	ActionRepository ar2 = (ActionRepository)o;
	if (this == ar2) return true;
	return
	    alpha_ld.equals(ar2.alpha_ld) &&
	    alpha_sync.equals(ar2.alpha_sync) &&
	    pi_ld.equals(ar2.pi_ld) &&
	    pi_sync.equals(ar2.pi_sync);
    }

    /** Adds the information about actions and action-thread ordering from
	<code>ar2</code> to <code>this</code> action repository.
	This method is called in the control-flow join points. */
    public final void join(ActionRepository ar2){
	// these first three are really easy

	assert alpha_ld != null : "alpha_ld == null";
	assert ar2 != null : "ar2 == null";

	alpha_ld.addAll(ar2.alpha_ld);
	pi_ld.union(ar2.pi_ld);
	alpha_sync.union(ar2.alpha_sync);
	// this one is a bit more difficult; we have to do it by hand
	for (Object entryO : ar2.pi_sync.entrySet()){
	    Map.Entry entry = (Map.Entry) entryO;
	    PANode    nt2 = (PANode)   entry.getKey();
	    Relation rel2 = (Relation) entry.getValue();
	    Relation rel1 = (Relation) pi_sync.get(nt2);
	    if(rel1 == null)
		pi_sync.put(nt2, (Relation) rel2.clone());
	    else
		rel1.union(rel2);
	}
    }

    /** Visits all the actions from this repository.
	It calls <code>visitor.visit_ld</code> on the <code>ld</code> actions
	and <code>visitor.visit_sync</code> on the <code>sync</code>. */
    public final void forAllActions(ActionVisitor visitor){
	// visit all the "ld" actions
	for(Iterator it_ld = alpha_ld.iterator(); it_ld.hasNext(); ){
	    visitor.visit_ld((PALoad) it_ld.next());
	}
	// visit all the "sync" actions
	for(Object nO : alpha_sync.keys()){
	    PANode n = (PANode) nO;
	    Iterator it_sync = alpha_sync.getValues(n).iterator();
	    while(it_sync.hasNext())
		visitor.visit_sync((PASync) it_sync.next());
	}
    }


    /** Visits all the "parallel action" items of information from this
	repository (i.e. all the <code> action || thread </code> elements.
	It calls <code>visitor.visit_par_ld</code> or 
	<code>visitor.visit_par_sync</code> according to the type of the
	<code>action</code>. */
    public final void forAllParActions(ParActionVisitor visitor){
	// visit all the "ld" || nt entries
	for(Object nt2O : pi_ld.keys()) {
	    PANode nt2 = (PANode) nt2O;
	    for (Object loadO : pi_ld.getValues(nt2)){
		PALoad load = (PALoad) loadO;
		visitor.visit_par_ld(load,nt2);
	    }
	}

	// visit all the "sync" || nt entries
	for(Object nt2O : pi_sync.keySet()){
	    PANode nt2 = (PANode) nt2O;
	    Relation rel = (Relation) pi_sync.get(nt2);
	    for(Object nO : rel.keys()){
		PANode n = (PANode) nO;
		Iterator it_sync = rel.getValues(n).iterator();
		while(it_sync.hasNext())
		    visitor.visit_par_sync((PASync) it_sync.next(), nt2);
	    }
	}
    }


    /** Returns an iterator over the set of loads that are performed in 
	parallel with an <code>nt</code> thread. <code>O(1)</code> time. */
    public final Iterator parallelLoads(PANode nt){
	return pi_ld.getValues(nt).iterator();
    }


    /** Removes all the information related to edges containing nodes from
	<code>nodes</code>. */
    public final void removeNodes(final Set nodes){
	// CLEAN alpha_ld
	for(Iterator it_paload = alpha_ld.iterator(); it_paload.hasNext(); ){
	    PALoad load = (PALoad) it_paload.next();
	    if(load.isBad(nodes)) it_paload.remove();
	}

	// CLEAN pi_ld
	PredicateWrapper node_predicate = new PredicateWrapper(){
		public boolean check(Object obj){
		    return nodes.contains((PANode) obj);
		}		
	    };
	PredicateWrapper load_predicate = new PredicateWrapper(){
		public boolean check(Object obj){
		    return ((PALoad) obj).isBad(nodes);
		}
	    };
	pi_ld.removeKeys(node_predicate);
	pi_ld.removeValues(load_predicate);

	// CLEAN alpha_sync
	PredicateWrapper sync_predicate = new PredicateWrapper(){
		public boolean check(Object obj){
		    PASync sync = (PASync) obj;
		    return
			nodes.contains(sync.n) ||
			nodes.contains(sync.nt);
		}
	    };
	alpha_sync.removeKeys(node_predicate);
	alpha_sync.removeValues(sync_predicate);

	// CLEAN pi_sync
	//  1. remove the entries in the map for non-remaining nodes nt2
	for(Iterator it_nodes = nodes.iterator(); it_nodes.hasNext(); )
	    pi_sync.remove((PANode) it_nodes.next());
	//  2. check the relation attached to each remaining nt2 and
	// eliminate the bad syncs
	for(Iterator it_nt2 = pi_sync.keySet().iterator(); it_nt2.hasNext();){
	    PANode nt2  = (PANode) it_nt2.next();
	    Relation rel = (Relation) pi_sync.get(nt2);
	    rel.removeKeys(node_predicate);
	    rel.removeValues(sync_predicate);
	    // if the relation is empty, delete the entry for nt2.
	    if(rel.isEmpty()) it_nt2.remove();
	}
	
    }

    /** Removes all the information related to <code>ld</code> actions
	using the edges from <code>edges</code>.
	<code>edges</code> is supposed to be a set of <code>PAEdge</code>s. */
    public final void removeEdges(final Set edges){

	// hack - we need a temporary edge just for testing the apartenence of
	// a load edge to the set edges; instead of stressing the GC with
	// repeated allocations, we allocate it once and for all
	final PAEdge edge = new PAEdge(null,"",null);

	// clean alpha_ld
	Iterator it_paload = alpha_ld.iterator();
	while(it_paload.hasNext()){
	    PALoad load = (PALoad) it_paload.next();
	    edge.n1 = load.n1;
	    edge.f  = load.f;
	    edge.n2 = load.n2;
	    if(edges.contains(edge))
		it_paload.remove();
	}

	// clean pi_ld
	pi_ld.removeValues(new PredicateWrapper(){
		public boolean check(Object obj){
		    PALoad load = (PALoad) obj;
		    edge.n1 = load.n1;
		    edge.f  = load.f;
		    edge.n2 = load.n2;
		    return edges.contains(edge);
		}
	    });
    }

    /** Checks if all the <code>sync</code> operation on <code>n</code>
	are independent (temporarily speaking) or not. 
	If it returns <code>false</code>, two threads could synchonize
	on it at the same time (i.e. they can simultaneously access it);
	in this case, the synchonizations are really necessary and should
	NOT be removed. */
    public final boolean independent(PANode n) {
	System.out.println("n   = " + n);
	// Goes through all the threads nt2 that are synchronizing on n
	Iterator it_sync = alpha_sync.getValues(n).iterator();
	while(it_sync.hasNext()) {
	    PANode nt2 = ((PASync) it_sync.next()).nt;
	    // if(nt2 == THIS_THREAD) continue;
	    // Find the set of all the threads nt1 that synchronize 
	    // on n in || with nt2.
	    // the relation nt -> Syncs done by nt on n in parallel with nt2
	    Relation rel = (Relation) pi_sync.get(nt2);
	    Set concurrent_syncs = 
		rel == null ? Collections.EMPTY_SET : rel.getValues(n);
	    // If there are such threads, we can have concurrent syncs.
	    if(!concurrent_syncs.isEmpty())
		return false;
	}
	return true;
    }

    // Returns the sync ops performed by the thread nt on the node n.
    public final Iterator syncsOn(final PANode n, final PANode nt){
	FilterIterator.Filter f =
	    new FilterIterator.Filter() {
		public boolean isElement(Object o) {
		    return ((PASync)o).nt == nt;
		}
	    };
	return new FilterIterator(alpha_sync.getValues(n).iterator(), f);
    }
    
    /** Checks whether any <code>sync</code> action is done on the node
	<code>node</code>. */
    public boolean isSyncOn(PANode node){
	return !alpha_sync.getValues(node).isEmpty();
    }

    // Private constructor for clone and keepTheEssential
    private ActionRepository(HashSet alpha_ld, Relation pi_ld,
			     Relation alpha_sync, Hashtable pi_sync){
	this.alpha_ld   = alpha_ld;
	this.pi_ld      = pi_ld;
	this.alpha_sync = alpha_sync;
	this.pi_sync    = pi_sync;
    }

    /** Produces an <code>ActionRepository</code> containing only the \
	nodes that could be reached from the outside.
	(i.e. via parameters,
	class nodes, normally or exceptionally returned nodes or the
	started thread nodes) */
    final ActionRepository keepTheEssential(final Set remaining_nodes){
	final ActionRepository ar2 = new ActionRepository();

	// commonsense rule
	remaining_nodes.add(THIS_THREAD);

	// select only the actions referring only to remaining nodes
	forAllActions(new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(load.isGood(remaining_nodes))
			ar2.alpha_add_ld(load);
		}
		public void visit_sync(PASync sync){
		    if(remaining_nodes.contains(sync.n) &&
		       remaining_nodes.contains(sync.nt))
			ar2.alpha_add_sync(sync);
		}
	    });

	// select only the parallel action info referring only to
	// remaining nodes
	forAllParActions(new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    if(load.isGood(remaining_nodes) &&
		       remaining_nodes.contains(nt2))
			ar2.pi_add_ld(load, nt2);
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    if(remaining_nodes.contains(sync.n) &&
		       remaining_nodes.contains(sync.nt) &&
		       remaining_nodes.contains(nt2))
			ar2.pi_add_sync(sync, nt2);
		}
	    });

	return ar2;
    }

    /* Specializes <code>this</code> <code>ActionRepository</code> for the
       call site <code>call</code>, according
       to <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public final ActionRepository csSpecialize(final Map map,
					       final CALL call){
	final ActionRepository ar2 = new ActionRepository();
	
	// some cache to avoid generating specializations for the same
	// sync action twice
	final Map sync2sync = new HashMap();

	forAllActions(new ActionVisitor(){
		public void visit_ld(PALoad load){
		    ar2.alpha_add_ld(load.specialize(map));
		}
		public void visit_sync(PASync sync){
		    PASync sync2 = sync.csSpecialize(map, call);
		    ar2.alpha_add_sync(sync2);
		    sync2sync.put(sync, sync2);
		}
	    });

	forAllParActions(new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    ar2.pi_add_ld(load.specialize(map),
				  PANode.translate(nt2, map));
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    ar2.pi_add_sync((PASync) sync2sync.get(sync),
				    PANode.translate(nt2, map));
		}
	    });

	return ar2;
    }

    /* Specializes <code>this</code> <code>ActionRepository</code> for the
       thread whose run method is <code>run</code>, according
       to <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public final ActionRepository tSpecialize(final Map map,
					      final MetaMethod run){
	final ActionRepository ar2 = new ActionRepository();
	
	// some cache to avoid generating specializations for the same
	// sync action twice
	final Map sync2sync = new HashMap();

	forAllActions(new ActionVisitor(){
		public void visit_ld(PALoad load){
		    ar2.alpha_add_ld(load.specialize(map));
		}
		public void visit_sync(PASync sync){
		    PASync sync2 = sync.tSpecialize(map, run);
		    ar2.alpha_add_sync(sync2);
		    sync2sync.put(sync, sync2);
		}
	    });

	forAllParActions(new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    ar2.pi_add_ld(load.specialize(map),
				  PANode.translate(nt2, map));
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    ar2.pi_add_sync((PASync) sync2sync.get(sync),
				    PANode.translate(nt2, map));
		}
	    });

	return ar2;
    }


    /** Produces a copy of <code>this</code> object. 
	The new object is totally independent from the old one: you can
	add/remove actions to/from it without affecting the original. */ 
    public final Object clone(){
	HashSet  new_alpha_ld   = (HashSet) alpha_ld.clone();
	Relation new_pi_ld      = (Relation) pi_ld.clone();
	Relation new_alpha_sync = (Relation) alpha_sync.clone();

	Hashtable new_pi_sync   = new Hashtable();
	for (Object entryO : pi_sync.entrySet()){
	    Map.Entry entry = (Map.Entry) entryO;
	    PANode   nt2  = (PANode)   entry.getKey();
	    Relation rel2 = (Relation) entry.getValue();
	    new_pi_sync.put(nt2,(Relation) rel2.clone());
	}

	return new ActionRepository(new_alpha_ld, new_pi_ld,
				    new_alpha_sync, new_pi_sync);
    }


    /** Pretty-printer for debug purposes. 
	<code>ar1.equals(ar2) <==> 
	   ar1.toString().equals(ar2.toString()).</code> */
    public final String toString(){
	StringBuffer buffer = new StringBuffer();
	final Set strings = new HashSet();

	ActionVisitor act_visitor = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    strings.add("  " + load + "\n");
		}
		public void visit_sync(PASync sync){
		    strings.add("  " + sync + "\n");
		}
	    };

	forAllActions(act_visitor);

	Object[] strs = Debug.sortedSet(strings);
	buffer.append(" Alpha:\n");
	for(int i = 0 ; i < strs.length ; i++)
	    buffer.append(strs[i]);

	strings.clear();

	ParActionVisitor par_act_visitor = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    strings.add("  " + load + " || " + nt2 + "\n");
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    strings.add("  " + sync + " || " + nt2 + "\n");
		}
	    };
	forAllParActions(par_act_visitor);

	strs = Debug.sortedSet(strings);
	buffer.append(" Pi:\n");
	for(int i = 0 ; i < strs.length ; i++)
	    buffer.append(strs[i]);

	return buffer.toString();
    }
}






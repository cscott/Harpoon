// PAEscapeFunc.java, created Sun Jan  9 20:53:09 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import harpoon.ClassFile.HMethod;

import harpoon.Util.PredicateWrapper;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.RelationEntryVisitor;


/**
 * <code>PAEscapeFunc</code> models the escape information.
 For each <code>PANode</code> <code>node</code>, it maintains all the nodes
 <code>node</code> escapes through (e.g. parameter nodes).
 Also, it records whether <code>node</code> escapes into a method hole or not.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAEscapeFunc.java,v 1.1.2.27 2001-06-17 22:30:42 cananian Exp $
 */
public class PAEscapeFunc implements java.io.Serializable {

    // rel_n attaches to each node the set of all the nodes
    // that it can escape through.
    Relation rel_n;

    ////////////////
    // set of nodes that escaped into an unanalyzed method
    // Set escaped_into_mh;
    ////////////////

    // rel_m attaches to each node the set of all the methods
    // that it can escape into.
    Relation rel_m;

    /** Creates a <code>EscapeFunc</code>. */
    public PAEscapeFunc() {
        rel_n  = new LightRelation();
	///////// escaped_into_mh = new HashSet();
	rel_m  = new LightRelation();
    }
    
    /** Records the fact that <code>node</code> can escape through
	the node <code>n_hole</code>. Returns <code>true</code>
	if new information has been gained */
    public final boolean addNodeHole(PANode node, PANode n_hole){
	return rel_n.add(node, n_hole);
    }

    /** Records the fact that <code>node</code> can escape through
	the node <code>n_holes</code>. Returns <code>true</code>
	if new information has been gained */
    public final boolean addNodeHoles(PANode node, Set n_holes){
	return rel_n.addAll(node, n_holes);
    }

    /** The dual of <code>addNodeHole</code> */
    public final void removeNodeHole(PANode node, PANode n_hole){
	rel_n.remove(node, n_hole);
    }

    /** Removes a node hole from all the nodes:
	<code>esc(n) = esc(n) - {n_hole}</code> for all <code>n</code>. */
    public void removeNodeHoleFromAll(PANode n_hole){
	// make a private copy of the set of keys so that I avoid
	//  "java.util.ConcurrentModificationException"
	Set set = new HashSet(rel_n.keys());
    	for(Iterator it = set.iterator(); it.hasNext(); )
    	    rel_n.remove((PANode) it.next(), n_hole);
    }

    /** Returns the set of all the node &quot;holes&quot; <code>node</code>
	escapes through. */
    public Set nodeHolesSet(PANode node){
	return rel_n.getValues(node);
    }

    /** Checks whether <code>node</code> escapes through a node or not. */
    public boolean hasEscapedIntoANode(PANode node){
	return !rel_n.getValues(node).isEmpty();
    }

    /** Records the fact that <code>node</code> escaped into a method hole.
	Returns <code>true</code> if this was a new information. */
    public boolean addMethodHole(PANode node, HMethod hm){
	// System.out.println("addMethodHole " + node);
	////// return escaped_into_mh.add(node);
	return rel_m.add(node, hm);
    }

    /** Records the fact that <code>node</code> escaped into a set of
	method holes.
	Returns <code>true</code> if this was a new information. */
    public boolean addMethodHoles(PANode node, Set mholes){
	boolean changed = false;	
	for(Iterator it = mholes.iterator(); it.hasNext(); ){
	    boolean changed_last = addMethodHole(node, (HMethod) it.next());
	    changed = changed || changed_last;
	}
	return changed;
    }


    /** The dual of <code>addMethodHole</code> */
    public final void removeMethodHole(PANode node, HMethod hm){
	rel_m.remove(node, hm);
    }



    /** The methods from the set <code>good_holes</code> are unharmful
	to the specific application that uses the pointer analysis. So, they
	can be erased from the set of method holes into which a specific node
	escapes. */
    public void removeMethodHoles(final Set good_holes) {
	rel_m.removeValues(new PredicateWrapper(){
		public boolean check(Object obj) {
		    return good_holes.contains(obj);
		}
	    });
    }

    /** Returns the set of methods that <code>node</code> escapes into. */
    public Set methodHolesSet(PANode node){
	return rel_m.getValues(node);
    }

    /** Returns the set of nodes which escape into a method hole. */
    public Set getEscapedIntoMH(){
	////// return escaped_into_mh;
	return rel_m.keys();
    }

    /** Checks whether <code>node</code> escapes into a method hole or not. */
    public boolean hasEscapedIntoAMethod(PANode node){
	///// return escaped_into_mh.contains(node);
	return !rel_m.getValues(node).isEmpty();
    }

    /** Checks if <code>node</code> has escaped in some hole, ie if
	<code>node</code> could be accessed by unanalyzed code. */
    public boolean hasEscaped(PANode node){
	return
	    hasEscapedIntoANode(node) || hasEscapedIntoAMethod(node);
    }


    /** Checks whether <code>node</code> escapes at most in the caller.
	ie it doesn't escape in an unanalyzed method, a thread or a static
	field. */
    public boolean escapesOnlyInCaller(PANode node) {
	if(hasEscapedIntoAMethod(node))
	    return false; // escapes into an unanalyzed method
	for(Iterator it = nodeHolesSet(node).iterator(); it.hasNext(); ) {
	    PANode hole_node = (PANode) it.next();
	    if(hole_node.type() != PANode.PARAM)
		return false; // escapes into a thread or a static
	}
	return true;
    }


    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> object. */
    public void remove(Set set){
	for(Iterator it_nodes = set.iterator(); it_nodes.hasNext(); ){
	    PANode node = (PANode) it_nodes.next();
	    rel_n.removeKey(node);
	    //////// escaped_into_mh.remove(node);
	    rel_m.removeKey(node);
	}
    }

    /** Computes the union of <code>this</code> <code>PAEscapeFunc</code>
	with <code>e2</code>. This function is called in the control flow
	<i>join</i> points. */
    public void union(PAEscapeFunc e2){
	rel_n.union(e2.rel_n);
	//////// escaped_into_mh.addAll(e2.escaped_into_mh);
	rel_m.union(e2.rel_m);
    }

    /** Inserts the image of <code>e2</code> through the <code>mu</code>
	mapping into <code>this</code> <code>PAEscapeFunc</code>. */
    public void insert(PAEscapeFunc e2, final Relation mu, final Set noholes) {
	// insert the node holes
	RelationEntryVisitor nvisitor =
	    new RelationEntryVisitor(){
		    public void visit(Object key, Object value){
			if(noholes.contains(value)) return;
			Iterator it_key_image = mu.getValues(key).iterator();
			while(it_key_image.hasNext()){
			    PANode node = (PANode) it_key_image.next();
			    PAEscapeFunc.this.addNodeHoles(
				     node, mu.getValues(value)); 
			}
		    }
		};

	e2.rel_n.forAllEntries(nvisitor);

	///// for(Iterator it = e2.escaped_into_mh.iterator(); it.hasNext(); ){
	/////    PANode node = (PANode) it.next();
	/////    escaped_into_mh.addAll(mu.getValues(node));
	///// }
	for(Iterator it = e2.getEscapedIntoMH().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Iterator it_img = mu.getValues(node).iterator();
	    while(it_img.hasNext()) {
		PANode node_img = (PANode) it_img.next();
		addMethodHoles(node_img, e2.methodHolesSet(node));
	    }
	}
	/////
    }

    /* Specializes <code>this</code> according to <code>map</code>. */
    public PAEscapeFunc specialize(Map map){
	PAEscapeFunc e2 = new PAEscapeFunc();

	for(Iterator itn = rel_n.keys().iterator(); itn.hasNext(); ){
	    PANode node  = (PANode) itn.next();
	    PANode node2 = PANode.translate(node, map);
	    Iterator itnh = rel_n.getValues(node).iterator();
	    while(itnh.hasNext())
		e2.addNodeHole(node2,
			       PANode.translate((PANode)itnh.next(),map));
	}
	
	///// for(Iterator it = escaped_into_mh.iterator(); it.hasNext(); ){
	/////    PANode node = (PANode) it.next();
	/////    e2.escaped_into_mh.add(PANode.translate(node, map));
	///// }
	for(Iterator it = getEscapedIntoMH().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    PANode node2 = PANode.translate(node, map);
	    e2.addMethodHoles(node2, methodHolesSet(node));
	}	
	/////

	return e2;
    }

    /** Checks the equality of two <code>PAEscapeFunc</code> objects. */
    public boolean equals(Object obj){
	if(obj == null) return false;
	PAEscapeFunc e2 = (PAEscapeFunc) obj;

	///// return 
	/////    rel_n.equals(e2.rel_n) &&
	/////    escaped_into_mh.equals(e2.escaped_into_mh);
	return
	    rel_n.equals(e2.rel_n) && rel_m.equals(e2.rel_m);
    }

    /** Returns the set of escaped nodes. */
    public Set escapedNodes(){
	HashSet set = new HashSet(rel_n.keys());
	////// set.addAll(escaped_into_mh);
	set.addAll(rel_m.keys());
	//////
	return set;
    }

    /** Private constructor used only by <code>select</code> and
	<code>clone</code> */
    ///// private PAEscapeFunc(Relation rel_n, Set escaped_into_mh){
    /////	this.rel_n           = rel_n;
    /////	this.escaped_into_mh = escaped_into_mh;
    ///// }
    private PAEscapeFunc(Relation rel_n, Relation rel_m){
	this.rel_n = rel_n;
	this.rel_m = rel_m;
    }

    /** Returns a <code>PAEscapeFunc</code> containing escape information
	only about the nodes from the set <code>remaining_nodes</code>. */
    public PAEscapeFunc select(Set remaining_nodes){
	Relation _rel_n = rel_n.select(remaining_nodes);

	///// Set set = new HashSet();
	///// for(Iterator it = escaped_into_mh.iterator(); it.hasNext(); ){
	/////    PANode node = (PANode) it.next();
	/////    if(remaining_nodes.contains(node))
	/////	set.add(node);
	///// }
	Relation _rel_m = rel_m.select(remaining_nodes);
	
	//// return new PAEscapeFunc(_rel_n, set);
	return new PAEscapeFunc(_rel_n, _rel_m);
    }


    /** <code>clone</clone> does a deep copy of <code>this</code> object. */
    public Object clone(){
	/////	return
	/////    new PAEscapeFunc((Relation)(rel_n.clone()),
	/////		     (Set) ((HashSet) escaped_into_mh).clone());
	return
	    new PAEscapeFunc((Relation) (rel_n.clone()),
			     (Relation) (rel_m.clone()));
    }

    /** Pretty-print debug function.
	Two equal <code>PAEscapeFunc</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	StringBuffer buffer = new StringBuffer(" Escape function:\n");

        Set set = new HashSet(rel_n.keys());
	////// set.addAll(escaped_into_mh);
	set.addAll(rel_m.keys());

	Object[] nodes = Debug.sortedSet(set);
	for(int i = 0; i < nodes.length ; i++){
	    PANode n = (PANode) nodes[i];
	    buffer.append("  " + n + ":");
	    
	    Object[] nholes = Debug.sortedSet(nodeHolesSet(n));
	    for(int j = 0 ; j < nholes.length ; j++){
		buffer.append(" ");
		buffer.append((PANode)nholes[j]);
	    }

	    Object[] mholes = Debug.sortedSet(methodHolesSet(n));
	    for(int j = 0 ; j < mholes.length ; j++){
		buffer.append(" ");
		buffer.append((HMethod)mholes[j]);
	    }

	    ///// if(escaped_into_mh.contains(n))
	    /////	buffer.append(" M");
	    buffer.append("\n");
	}
	
	return buffer.toString();
    }

}




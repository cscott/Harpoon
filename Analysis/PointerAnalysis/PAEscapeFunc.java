// PAEscapeFunc.java, created Sun Jan  9 20:53:09 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;


/**
 * <code>PAEscapeFunc</code> models the escape information.
 For each <code>PANode</code> <code>node</code>, it maintains all the nodes
 <code>node</code> escapes through (e.g. parameter nodes).
 Also, it records whether <code>node</code> escapes into a method hole or not.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEscapeFunc.java,v 1.1.2.16 2000-04-02 19:48:00 salcianu Exp $
 */
public class PAEscapeFunc {

    // rel_n attaches to each node the set of all the nodes
    // that it can escape through.
    Relation rel_n;

    // set of nodes that escaped into an unanalyzed method
    Set escaped_into_mh;

    /** Creates a <code>EscapeFunc</code>. */
    public PAEscapeFunc() {
        rel_n           = new Relation();
	escaped_into_mh = new HashSet();
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
	Set set = new HashSet(rel_n.keySet());
    	for(Iterator it = set.iterator(); it.hasNext(); )
    	    rel_n.remove((PANode) it.next(), n_hole);
    }

    /** Records the fact that <code>node</code> escaped into a method hole.
	Returns <code>true</code> if this was a new information. */
    public boolean addMethodHole(PANode node){
	// System.out.println("addMethodHole " + node);
	// Hack: we suppose no native method can modify an object
	if(true) return false;
	return escaped_into_mh.add(node);
    }

    /** Returns the set of nodes which escape into a method hole. */
    public Set getEscapedIntoMH(){
	return escaped_into_mh;
    }

    /** Checks if <code>node</code> has escaped in some hole, ie if
	<code>node</code> could be accessed by unanalyzed code. */
    public boolean hasEscaped(PANode node){
	return
	    !rel_n.getValuesSet(node).isEmpty() || 
	    escaped_into_mh.contains(node);
    }

    /** Returns the set of all the node &quot;holes&quot; <code>node</code>
	escapes through. */
    public Set nodeHolesSet(PANode node){
	return rel_n.getValuesSet(node);
    }

    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> object. */
    public void remove(Set set){
	for(Iterator it_nodes = set.iterator(); it_nodes.hasNext(); ){
	    PANode node = (PANode) it_nodes.next();
	    rel_n.removeAll(node);
	    escaped_into_mh.remove(node);
	}
    }

    /** Computes the union of <code>this</code> <code>PAEscapeFunc</code>
	with <code>e2</code>. This function is called in the control flow
	<i>join</i> points. */
    public void union(PAEscapeFunc e2){
	rel_n.union(e2.rel_n);
	escaped_into_mh.addAll(e2.escaped_into_mh);
    }

    /** Inserts the image of <code>e2</code> through the <code>mu</code>
	mapping into <code>this</code> <code>PAEscapeFunc</code>. */ 
    public void insert(PAEscapeFunc e2, final Relation mu, final Set noholes){
	// insert the node holes
	RelationEntryVisitor nvisitor =
	    new RelationEntryVisitor(){
		    public void visit(Object key, Object value){
			if(noholes.contains(value)) return;
			Iterator it_key_image = mu.getValues(key);
			while(it_key_image.hasNext()){
			    PANode node = (PANode) it_key_image.next();
			    PAEscapeFunc.this.addNodeHoles(
				     node,mu.getValuesSet(value)); 
			}
		    }
		};

	e2.rel_n.forAllEntries(nvisitor);

	for(Iterator it = e2.escaped_into_mh.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    escaped_into_mh.addAll(mu.getValuesSet(node));
	}
    }

    /* Specializes <code>this</code> according to <code>map</code>. */
    public PAEscapeFunc specialize(Map map){
	PAEscapeFunc e2 = new PAEscapeFunc();

	for(Iterator itn = rel_n.keySet().iterator(); itn.hasNext(); ){
	    PANode node  = (PANode) itn.next();
	    PANode node2 = PANode.translate(node, map);
	    for(Iterator itnh = rel_n.getValues(node); itnh.hasNext(); )
		e2.addNodeHole(node2,
			       PANode.translate((PANode)itnh.next(),map));
	}
	
	for(Iterator it = escaped_into_mh.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    e2.escaped_into_mh.add(PANode.translate(node, map));
	}

	return e2;
    }

    /** Checks the equality of two <code>PAEscapeFunc</code> objects. */
    public boolean equals(Object obj){
	if(obj == null) return false;
	PAEscapeFunc e2 = (PAEscapeFunc) obj;

	return 
	    rel_n.equals(e2.rel_n) &&
	    escaped_into_mh.equals(e2.escaped_into_mh);
    }

    /** Returns the set of escaped nodes. */
    public Set escapedNodes(){
	HashSet set = new HashSet(rel_n.keySet());
	set.addAll(escaped_into_mh);
	return set;
    }

    /** Private constructor used only by <code>select</code> and
	<code>clone</code> */
    private PAEscapeFunc(Relation rel_n, Set escaped_into_mh){
	this.rel_n           = rel_n;
	this.escaped_into_mh = escaped_into_mh;
    }

    /** Returns a <code>PAEscapeFunc</code> containing escape information
	only about the nodes from the set <code>remaining_nodes</code>. */
    public PAEscapeFunc select(Set remaining_nodes){
	Relation _rel_n = rel_n.select(remaining_nodes);

	Set set = new HashSet();
	for(Iterator it = escaped_into_mh.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    if(remaining_nodes.contains(node))
		set.add(node);
	}
	
	return new PAEscapeFunc(_rel_n, set);
    }


    /** <code>clone</clone> does a deep copy of <code>this</code> object. */
    public Object clone(){
	return
	    new PAEscapeFunc((Relation)(rel_n.clone()),
			     (Set) ((HashSet) escaped_into_mh).clone());
    }

    /** Pretty-print debug function.
	Two equal <code>PAEscapeFunc</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	StringBuffer buffer = new StringBuffer(" Escape function:\n");

        Set set = new HashSet(rel_n.keySet());
	set.addAll(escaped_into_mh);

	Object[] nodes = Debug.sortedSet(set);
	for(int i = 0; i < nodes.length ; i++){
	    PANode n = (PANode) nodes[i];
	    buffer.append("  " + n + ":");
	    
	    Object[] nholes = Debug.sortedSet(nodeHolesSet(n));
	    for(int j = 0 ; j < nholes.length ; j++){
		buffer.append(" ");
		buffer.append((PANode)nholes[j]);
	    }

	    if(escaped_into_mh.contains(n))
		buffer.append(" M");

	    buffer.append("\n");
	}
	
	return buffer.toString();
    }

}




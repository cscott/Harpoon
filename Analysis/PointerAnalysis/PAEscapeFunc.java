// PAEscapeFunc.java, created Sun Jan  9 20:53:09 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;

import harpoon.ClassFile.HCodeElement;

/**
 * <code>PAEscapeFunc</code> models the escape information.
 For each <code>PANode</code> <code>n</code>, it maintains all the nodes
 and unanalyzed call sites <code>n</code> escapes through.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEscapeFunc.java,v 1.1.2.10 2000-02-21 04:47:59 salcianu Exp $
 */
public class PAEscapeFunc {

    // rel_n attaches to each node the set of all the nodes
    // that it can escape through.
    Relation rel_n;

    // rel_m attaches to each node the set of all the method
    // invocation sites that it can escape through.
    Relation rel_m;

    /** Creates a <code>EscapeFunc</code>. */
    public PAEscapeFunc() {
        rel_n = new Relation();
        rel_m = new Relation();
    }
    
    /** Records the fact that <code>n</code> can escape through
	the node <code>n_hole</code>. Returns <code>true</code>
	if new information has been gained */
    public final boolean addNodeHole(PANode n, PANode n_hole){
	return rel_n.add(n,n_hole);
    }

    /** Records the fact that <code>n</code> can escape through
	the node <code>n_holes</code>. Returns <code>true</code>
	if new information has been gained */
    public final boolean addNodeHoles(PANode n, Set n_holes){
	return rel_n.addAll(n,n_holes);
    }

    /** The dual of <code>addNodeHole</code> */
    public final void removeNodeHole(PANode n, PANode n_hole){
	rel_n.remove(n,n_hole);
    }

    /** Records the fact that <code>n</code> can escape through the
	method invocation site <code>m_hole</code>. Returns <code>true</code>
	if new information has been gained */
    public final boolean addMethodHole(PANode n, HCodeElement m_hole){
	return rel_m.add(n,m_hole);
    }

    /** Records the fact that <code>n</code> can escape through the
     *  method invocation sites <code>m_holes</code>. Returns <code>true</code>
     *  if new information has been gained */
    public final boolean addMethodHoles(PANode n, Set m_holes){
	return rel_m.addAll(n,m_holes);
    }

    /** The dual of <code>addMethodHole</code> */
    public void removeMethodHole(PANode n, HCodeElement m_hole){
	rel_m.remove(n,m_hole);
    }

    /** Remove a node hole from all the nodes:\
	<code>esc(n) = esc(n) - {n_hole}</code> for all n. */
    public void removeNodeHoleFromAll(PANode n_hole){
	Enumeration it = rel_n.keys();
	while(it.hasMoreElements()){
	    PANode n = (PANode) it.nextElement();
	    rel_n.remove(n,n_hole);
	}
    }

    // Check if n has escaped in some "hole" - node accessed
    // by unalyzed code or unanalyzed method invocation site
    public boolean hasEscaped(PANode n){
	Set set_n = rel_n.getValuesSet(n);
	Set set_m = rel_m.getValuesSet(n);

	return !(
		 ((set_n == null) || (set_n.isEmpty())) &&
		 ((set_m == null) || (set_m.isEmpty()))
		);
    }

    /** Returns the set of all the node "holes" <code>n</code>
	escapes through. */
    public Set nodeHolesSet(PANode n){
	return rel_n.getValuesSet(n);
    }

    /** Returns an iterator over the set of the node "holes"
	<code>n</code> escapes through. */
    public Iterator nodeHoles(PANode n){
	return rel_n.getValues(n);
    }

    /** Returns the set of all the method "holes" <code>n</code>
	escapes through. */
    public Set methodHolesSet(PANode n){
	return rel_m.getValuesSet(n);
    }

    /** Returns an iterator over the set of the unanalyzed method
	invocation sites (the method "holes") <code>n</code>
	escapes through. */
    public Iterator methodHoles(PANode n){
	return rel_m.getValues(n);
    }


    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> object. */
    public void remove(Set set){
	Iterator it_nodes = set.iterator();
	while(it_nodes.hasNext()){
	    PANode node = (PANode) it_nodes.next();
	    rel_n.removeAll(node);
	    rel_m.removeAll(node);
	}
    }


    /** Computes the union of <code>this</code> <code>PAEscapeFunc</code>
	with <code>e2</code>. This function is called in the control flow
	<i>join</i> points. */
    public void union(PAEscapeFunc e2){
	rel_n.union(e2.rel_n);
	rel_m.union(e2.rel_m);
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

	// insert the method holes
	RelationEntryVisitor mvisitor = 
	    new RelationEntryVisitor(){
		    public void visit(Object key, Object value){
			Iterator it_key_image = mu.getValues(key);
			while(it_key_image.hasNext()){
			    PANode node = (PANode) it_key_image.next();
			    PAEscapeFunc.this.addMethodHole(
				        node,(HCodeElement)value);
			}			
		    }
		};

	e2.rel_m.forAllEntries(mvisitor);

    }

    /** Checks the equality of two <code>PAEscapeFunc</code> */
    public boolean equals(Object o){
	if(o==null) return false;
	PAEscapeFunc e2 = (PAEscapeFunc)o;
	return rel_n.equals(e2.rel_n) && rel_m.equals(e2.rel_m);
    }

    public Set escapedNodes(){
	HashSet set = new HashSet();
	set.addAll(rel_n.keySet());
	set.addAll(rel_m.keySet());
	return set;
    }

    /** Private constructor used only by <code>select</code> and
	<code>clone</code> */
    private PAEscapeFunc(Relation _rel_n,Relation _rel_m){
	rel_n = _rel_n;
	rel_m = _rel_m;
    }

    /** Returns a <code>PAEscapeFunc</code> containing escape information
	only about the nodes from the set <code>remaining_nodes</code>. */
    public PAEscapeFunc select(Set remaining_nodes){
	Relation _rel_n = rel_n.select(remaining_nodes);
	Relation _rel_m = rel_m.select(remaining_nodes);
	return new PAEscapeFunc(_rel_n, _rel_m);
    }


    /** <code>clone</clone> does a deep copy of <code>this</code> object. */
    public Object clone(){
	return
	    new PAEscapeFunc((Relation)(rel_n.clone()),
			     (Relation)(rel_m.clone()));
    }

    /** Pretty-print debug function.
	Two equal <code>PAEscapeFunc</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	StringBuffer buffer = new StringBuffer(" Escape function:\n");

        HashSet set = new HashSet(rel_n.keySet());
        set.addAll(rel_m.keySet());

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
		HCodeElement hce = (HCodeElement) mholes[j];
		buffer.append("M" + hce.getSourceFile() + ":" +
			      hce.getLineNumber());
	    }
	    buffer.append("\n");
	}
	
	return buffer.toString();
    }

}




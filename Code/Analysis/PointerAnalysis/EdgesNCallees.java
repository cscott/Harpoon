// EdgesNCallees.java, created Mon Dec 11 17:58:49 2000 by vivien
// Copyright (C) 
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;


/**
 * <code>EdgesNCallees</code> models a precedence relation between
 * (inside or outside) edges and call sites skipped by an on demand
 * analysis. This precedence relation may be strict or not. 
 */
public class EdgesNCallees implements java.io.Serializable {
    public static final boolean DEBUG     = false;

    // Decide whether we do precise or conservative joins...
    private static final boolean VERY_PRECISE = false;

    // The data structure itself.
    public Map edges;
    
    // Flag which specifies if the precedence relation is an ``always
    // precedes'' relation (strict = true), or a ``may be precedes''
    // relation.
    private  boolean strict = true;
    

    /** Return the type of the precedence relation. true means "always
     * precedes".
     */
    public boolean strict()
    {
	return strict;
    }


    /** Creates a <code>EdgesNCallees</code> coding a strict precedence
     * relation.*/
    public EdgesNCallees(){
	edges = new LightMap();
	strict = true;
    }

	
    /** Creates a <code>EdgesNCallees</code> coding a strict precedence
     * relation iff the argument is true.*/
    public EdgesNCallees(boolean strictness){
	edges = new LightMap();
	strict = strictness;
    }
	

    /** Add the <code>callees</code> to the precedence relation of the
     * edge from a node in <code>heads</code> to a node in
     * <code>tails</code> via the field <code>f</code>, according to
     * the precedence policy (strict or not). */
    public void add(Set heads, String f, Set tails, Set callees)
    {
	if (callees==null) return;

	for(Iterator h_it=heads.iterator(); h_it.hasNext(); )
	    add((PANode)h_it.next(), f, tails, callees);
    }
   

    /** Add the <code>callees</code> to the precedence relation of the
     * edge from a node in <code>heads</code> to <code>tail</code> via
     * the field <code>f</code>, according to the precedence policy
     * (strict or not). */
    public void add(Set heads, String f, PANode tail, Set callees)
    {
	if (callees==null) return;

	for(Iterator h_it=heads.iterator(); h_it.hasNext(); )
	    add((PANode)h_it.next(), f, tail, callees);
    }

   
    /** Add the <code>callees</code> to the precedence relation of the
     * edge from <code>head</code> to <code>tail</code> via the field
     * <code>f</code>, according to the precedence policy (strict or
     * not). */
    public void add(PANode head, String f, PANode tail, Set callees)
    {
	if (callees==null) return;

 	Map from_head = (Map) edges.get(head);

	// To avoid the empty set problem
	HashSet holes = new HashSet(callees);
	holes.add(ODPointerAnalysis.BottomHole);
	//tbu the bottomhole should be an element of MethodHole class

 	if (from_head==null){
	    Relation tails = (Relation) new LightRelation();
	    tails.addAll(tail,holes);
	    from_head = new LightMap();
	    from_head.put(f,tails);
	    edges.put(head,from_head);
	}
	else{
	    Relation tails = (Relation) from_head.get(f);
	    if (tails==null){
		tails = new LightRelation();
		tails.addAll(tail,holes);
		from_head.put(f,tails);
	    }
	    else{
		if (strict){
		    Set sites = tails.getValues(tail);
		    if (sites==null){
			tails.addAll(tail,holes);
		    }
		    else{
			HashSet for_trash = new HashSet();
			for(Iterator it=sites.iterator();it.hasNext();){
			    Object hole =  it.next();
			    if (!holes.contains(hole)) for_trash.add(hole);
			}
			tails.removeAll(tail,for_trash);
		    }
		}
		else{
		    tails.addAll(tail,holes);
		}
	    }
	}
 	if (DEBUG){
 	    System.out.println("New relation " + toString());
	}
    }


    /** Add the <code>callees</code> to the precedence relation of all
     * the edges from <code>head</code> to a node in
     * <code>tails</code> via the field <code>f</code>, according to
     * the precedence policy (strict or not). */
    public void add(PANode head, String f, Set tails, Set callees)
    {
	if (callees==null) return;

 	if (DEBUG)
	    {
		System.out.println("EdgesNCallees.add: " + 
				   head +" "+ f +" "+ tails 
				   + " (" + callees + " ) to \n" 
				   + toString());
	    }

 	Map from_head = (Map) edges.get(head);

	// To avoid the empty set problem
	HashSet holes = new HashSet(callees);
	holes.add(ODPointerAnalysis.BottomHole);
	//tbu the bottomhole should be an element of MethodHole class

 	if (from_head==null){
	    from_head = new LightMap();
	    Relation tail_rel = (Relation) new LightRelation();
	    for(Iterator t_it=tails.iterator(); t_it.hasNext(); ){
		PANode tail = (PANode) t_it.next();
		tail_rel.addAll(tail,holes);
	    }
	    from_head.put(f,tail_rel);
	    edges.put(head,from_head);
	}
	else{
	    Relation tail_rel = (Relation) from_head.get(f);
	    if (tail_rel==null){
		tail_rel = new LightRelation();
		for(Iterator t_it=tails.iterator(); t_it.hasNext(); ){
		    PANode tail = (PANode) t_it.next();
		    tail_rel.addAll(tail,holes);
		}
		from_head.put(f,tail_rel);
	    }
	    else{
		if (strict){
		    for(Iterator t_it=tails.iterator(); t_it.hasNext(); ){
			PANode tail = (PANode) t_it.next();
			Set sites = tail_rel.getValues(tail);
			if (sites==null){
			    tail_rel.addAll(tail,holes);
			}
			else{
			    HashSet for_trash = new HashSet();
			    for(Iterator it=sites.iterator();it.hasNext();){
				Object hole =  it.next();
				if (!holes.contains(hole)) for_trash.add(hole);
			    }
			    tail_rel.removeAll(tail,for_trash);
			}
		    }
		}
		else{
		    for(Iterator t_it=tails.iterator(); t_it.hasNext(); )
			tail_rel.addAll(t_it.next(),holes);
		}
	    }
	}
	if (DEBUG){
	    System.out.println("New relation " + toString());
	}
    }


    /** Set the <code>callees</code> to the precedence relation of the
     * edge from <code>head</code> to <code>tail</code> via the field
     * <code>f</code>.
     */
    public void set(PANode head, String f, PANode tail, Set callees)
    {
	if (callees==null) return;

 	Map from_head = (Map) edges.get(head);

	// To avoid the empty set problem
	HashSet holes = new HashSet(callees);
	holes.add(ODPointerAnalysis.BottomHole);
	//tbu the bottomhole should be an element of MethodHole class

 	if (from_head==null){
	    Relation tails = (Relation) new LightRelation();
	    tails.addAll(tail,holes);
	    from_head = new LightMap();
	    from_head.put(f,tails);
	    edges.put(head,from_head);
	}
	else{
	    Relation tails = (Relation) from_head.get(f);
	    if (tails==null){
		tails = new LightRelation();
		tails.addAll(tail,holes);
		from_head.put(f,tails);
	    }
	    else{
		tails.removeKey(tail);
		tails.addAll(tail, holes);
	    }
	}
    }


    /** Return the set of callees in relation with the edges from
     * <code>head</code> to <code>tail</code> via the field
     * <code>f</code>.*/
    public Set callees(PANode head, String f, PANode tail)
    {
 	Map from_head = (Map) edges.get(head);
	
 	if (from_head==null){
	    System.err.println("Error in EdgesNCallees.callees (1): "
			       + "Map should not be null");
	    System.out.println("Error in EdgesNCallees.callees (1): "
			       + "Map should not be null");
	    System.out.println("Problem for: " + head + " --" + f + "-> " + tail);
	    System.out.println("in " + this);

	    if(DEBUG){
		System.out.println(head + " - " + f + " -> " + tail);
		System.out.println(" " + head + "(" + head.details() + ")");
		if (head.isCSSpec()){
		    System.out.println("This is a CS specialization");
		}
		else{
		    System.out.println("This is **NOT** a CS specialization");
		}
		System.out.println("  " + head.getCSParent());
		System.out.println("  " + head.getTSParent());
		System.out.println(toString());
	    }
	    return null; 
	}
	else{
	    Relation tails = (Relation) from_head.get(f);
	    if (tails==null){
		System.err.println("Error in EdgesNCallees.callees (2): "
				   + "Relation should not be null");
		System.out.println("Error in EdgesNCallees.callees (2): "
				   + "Relation should not be null");
		if(DEBUG){
		    System.out.println(head + " - " + f + " -> " + tail);
		    System.out.println(" " + head + "(" + head.details() 
				       + ")\n " + toString());
		}
		return new HashSet();
	    }
	    else
		return new HashSet(tails.getValues(tail));
	}
    }

    /** Same as callees but whithout the error messages (this method
     * is not used in the same context).
     */
    private Set calleessilent(PANode head, String f, PANode tail)
    {
 	Map from_head = (Map) edges.get(head);
	
 	if (from_head==null){
	    return null;
	}
	else{
	    Relation tails = (Relation) from_head.get(f);
	    if (tails==null)
		return null;
	    else
		return new HashSet(tails.getValues(tail));
	}
    }


    /** Removes the MethodHole <code>hole</code> from all the sets of
     * callees that may contain it.
     */
    public void remove(MethodHole hole)
    {
	if (edges==null) return;

	for(Iterator it_n1=edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) edges.get(n1);
	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    PANode n2 = (PANode) it_n2.next();
		    n1_f.remove(n2,hole);
		}
	    }
	}
    }


    /** Conservative implementation */
    public void join(Set firstHoles, EdgesNCallees second, Set secondHoles)
    {
	if (second==null) return;
	
	assert strict==second.strict() : "Attempt to join to EdgesNCallees "
		    + "of different types"; 
	
	for(Iterator it_n1=second.edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) second.edges.get(n1);

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		
		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    PANode n2 = (PANode) it_n2.next();
		    if (strict){
			HashSet callees_1 = (HashSet) callees(n1, f, n2);
			if((callees_1==null)||(callees_1.isEmpty())){
			    // simple addition as the edge does not
			    // exist in the current object.
			    add(n1, f, n2, n1_f.getValues(n2));
			}
			else{
			    HashSet callees_2 = (HashSet) n1_f.getValues(n2);

			    // Intersection
			    HashSet intersect = (HashSet) callees_1.clone();
			    intersect.retainAll(callees_2);

			    // The holes in the current object but
			    // unknown in "second" should be kept.
			    HashSet firstMinusSecond = (HashSet) callees_1.clone();
			    firstMinusSecond.removeAll(callees_2);

			    // Conversely
			    callees_2.removeAll(callees_1);

			    // What should be kept, other than the
			    // intersection
			    firstMinusSecond.addAll(callees_2);

			    // The final set
			    firstMinusSecond.addAll(intersect);
			    
			    set(n1, f, n2, firstMinusSecond);
			}
		    }
		    else
			add(n1, f, n2, n1_f.getValues(n2));
		}
	    }
	}
    }

    /** Conservative implementation */
    public void joinbis(EdgesNCallees second)
    {
	if (second==null) return;
	
	assert strict==second.strict() : "Attempt to join to EdgesNCallees "
		    + "of different types"; 
	
	for(Iterator it_n1=second.edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) second.edges.get(n1);

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		
		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    PANode n2 = (PANode) it_n2.next();
		    if (strict){
			// A real implementation should be more precise than that !
			add(n1, f, n2, n1_f.getValues(n2));
		    }
		    else
			add(n1, f, n2, n1_f.getValues(n2));
		}
	    }
	}
    }


    /** Conservative implementation */
    public void join(EdgesNCallees second, Map holeConversion, 
		     Set firstholes, Set secondholes,
		     Set control)
    {
	if (second==null) return;
	
	assert strict==second.strict() : "Attempt to join to EdgesNCallees "
		    + "of different types"; 
	
	for(Iterator it_n1=second.edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) second.edges.get(n1);

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		
		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    PANode n2 = (PANode) it_n2.next();
		    if (strict){
			if (VERY_PRECISE){
			    HashSet callees_1 = (HashSet) calleessilent(n1, f, n2);
			    if((callees_1==null)||(callees_1.isEmpty())){
				// simple addition as the edge does not
				// exist in the current object.
				set(n1, f, n2, project(n1_f.getValues(n2), holeConversion));
			    }
			    else{
				HashSet callees_2 = 
				    (HashSet) project(n1_f.getValues(n2), holeConversion);
				
				// Intersection
				HashSet intersect = (HashSet) callees_1.clone();
				intersect.retainAll(callees_2);
				
				if ((intersect.size()!=callees_1.size())||
				    (intersect.size()!=callees_2.size())){
				    // The holes in the current object but
				    // unknown in "second" should be kept.
				    callees_1.removeAll(secondholes);
				    
				    // Conversely
				    callees_2.removeAll(firstholes);
				    
				    // What should be kept, other than the
				    // intersection
				    intersect.addAll(callees_2);
				    
				    // The final set
				    intersect.addAll(callees_1);
				}

			    set(n1, f, n2, intersect);
			    }
			}
			else{
			    add(n1, f, n2, 
				project(n1_f.getValues(n2), holeConversion));
			}
		    }
		    else
			add(n1, f, n2, project(n1_f.getValues(n2), holeConversion));
		}
	    }
	}
    }

    /** Simply clone an <code>EdgesNCallees</code> object.
     */
    public Object clone()
    {
	assert edges!=null : ("Problem in EdgesNCallees.clone: null map");

	EdgesNCallees new_enc = new EdgesNCallees(strict);

	for(Iterator it_n1=edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) edges.get(n1);
	    Map new_n1_map = new LightMap();

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		Relation new_n1_f = new LightRelation();

		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    Object obj = it_n2.next();
		    PANode n2 = (PANode) obj;
		    new_n1_f.addAll(n2, n1_f.getValues(n2));
		}
		new_n1_map.put(f,new_n1_f);
	    }
	    new_enc.edges.put(n1,new_n1_map);
	}
	return new_enc;
    }


    /** clone the <code>EdgesNCallees</code> while transposing the
     * callees using the <code>map</code>.
     */
    public EdgesNCallees clone(Map hole_conversion)
    {
	assert edges!=null : ("Problem in EdgesNCallees.clone: null map");

	EdgesNCallees new_enc = new EdgesNCallees(strict);

	for(Iterator it_n1=edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    Map n1_map = (Map) edges.get(n1);
	    Map new_n1_map = new LightMap();

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		Relation new_n1_f = new LightRelation();

		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    Object obj = it_n2.next();
		    PANode n2 = (PANode) obj;
		    for(Iterator it_mh=n1_f.getValues(n2).iterator(); it_mh.hasNext(); ){
			MethodHole mh = (MethodHole) it_mh.next();
			if (mh==ODPointerAnalysis.BottomHole)
			    new_n1_f.add(n2,ODPointerAnalysis.BottomHole);
			else {
			    if (hole_conversion.get(mh)==null){
				System.err.println("EdgesNCallees.clone: " +
						   "no conversion for " + mh);
				System.out.println("EdgesNCallees.clone: " + 
						   "no conversion for " + mh
						   + " in " + hole_conversion);
			    }
			    else
				new_n1_f.add(n2,hole_conversion.get(mh));
			}
		    }
		}
		new_n1_map.put(f,new_n1_f);
	    }
	    new_enc.edges.put(n1,new_n1_map);
	}
	return new_enc;
    }



    /** clone the <code>EdgesNCallees</code> while transposing the
     * callees using the first <code>map</code>, and the nodes using
     * the second one.
     */

    public EdgesNCallees clone(Map hole_conversion, Map node_conversion)
    {
	assert edges!=null : ("Problem in EdgesNCallees.clone: null map");

	EdgesNCallees new_enc = new EdgesNCallees(strict);

	for(Iterator it_n1=edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
	    PANode new_n1  = (PANode) node_conversion.get(n1);
	    if (new_n1==null){
		System.err.println("Error in EdgesNCallees.clone with node_conversion");
		System.out.println("Error in EdgesNCallees.clone with node_conversion");
		System.out.println("Node with no translation: " + n1);
		System.out.println("Map: " + node_conversion);
	    }
	    Map n1_map = (Map) edges.get(n1);
	    Map new_n1_map = new LightMap();

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		Relation new_n1_f = new LightRelation();

		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    PANode n2 = (PANode) it_n2.next();
		    PANode new_n2 = (PANode) node_conversion.get(n2);
		    if (new_n2==null){
			System.err.println("Error in EdgesNCallees.clone " + 
					   "with node_conversion");
			System.out.println("Error in EdgesNCallees.clone " + 
					   "with node_conversion");
			System.out.println("Null node " + n2);
			System.out.println("Map: " + node_conversion);
		    }

		    for(Iterator it_mh=n1_f.getValues(n2).iterator(); it_mh.hasNext(); ){
			MethodHole mh = (MethodHole) it_mh.next();
			if (mh==ODPointerAnalysis.BottomHole)
			    new_n1_f.add(new_n2,ODPointerAnalysis.BottomHole);
			else {
			    if (hole_conversion.get(mh)==null){
				System.err.println("EdgesNCallees.clone: " +
						   "no conversion for " + mh
						   + " in " + hole_conversion);
				System.out.println("EdgesNCallees.clone: " +
						   "no conversion for " + mh
						   + " in " + hole_conversion);
			    }
			    else
				new_n1_f.add(new_n2,hole_conversion.get(mh));
			}
		    }
		}
		new_n1_map.put(f,new_n1_f);
	    }
	    new_enc.edges.put(new_n1,new_n1_map);
	}
	return new_enc;
    }



    /** Pretty-print debug function.
     */
    public String toString()
    {
	return toString("");

    }

    /** Pretty-print debug function.
     */
    public String toString(String s)
    {
	StringBuffer buffer = new StringBuffer(" EdgesNCallees: " + s);

	if (edges==null) {
	    buffer.append("\n  --  empty --\n");
	    return buffer.toString();
	}

	for(Iterator it_n1=edges.keySet().iterator();it_n1.hasNext(); ){
	    PANode n1  = (PANode) it_n1.next();
 	    buffer.append("\n" + n1 + "\t-> ");
	    Map n1_map = (Map) edges.get(n1);
	    boolean firstfield = true;
	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		if (firstfield){
		    buffer.append(f + "\t-> ");
		    firstfield=false;
		}
		else
		    buffer.append("\n\t    " + f + "\t -> ");
		boolean firstnode = true;
		for(Iterator it_n2=n1_f.keys().iterator(); it_n2.hasNext(); ){
		    Object obj = it_n2.next();
		    PANode n2 = (PANode) obj;
		    if(firstnode){
			buffer.append(n2 + "\t-> " + 
				      n1_f.getValues(n2));
			firstnode = false;
		    }
		    else
			buffer.append("\n\t    \t    " + n2 + "\t-> " +
				      n1_f.getValues(n2));
		}
	    }
	}
	return buffer.toString();
    }

    public Set project(Set holes, Map projection)
    {
	HashSet newHoles = new HashSet();

	for(Iterator it=holes.iterator(); it.hasNext(); )
	    newHoles.add(projection.get(it.next()));

	return newHoles;
    }
    

}

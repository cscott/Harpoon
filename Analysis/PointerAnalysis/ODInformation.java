// ODInformation.java, created Mon Dec 11 17:58:49 2000 by vivien
// Copyright (C) 2001 Frederic VIVIEN <vivien@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedList;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;


/**
 * <code>ODInformation</code> contains the pieces of information
 * relative to an on-demand analysis.
 *
 * @author  Frederic VIVIEN <vivien@lcs.mit.edu>
 * @version $Id: ODInformation.java,v 1.1.2.2 2001-06-17 22:30:41 cananian Exp $
 */

public class ODInformation {
    public static final boolean DEBUG = false;
    private static int odinfonum = 0;
    
    public boolean precise  = true;

    // The three relationships between edges and skipped call sites
    public EdgesNCallees inAlways    = null;
    public EdgesNCallees outAlways   = null;
    public EdgesNCallees outMaybe    = null;
    public  Set      skippedCS        = null;
    private Relation skippedCSHistory = null;
    private Relation locks            = null;
    private Set      lock_set         = null;
    private int id = -1;

    
    /** Creates an <code>ODInformation</code> object. This information
     * is precise or conservative depending of the value of the
     * corresponding static field in ODPointerAnalysis.
     */
    public ODInformation(){
	skippedCS = new HashSet();

	precise = ODPointerAnalysis.ODA_precise;

	if(precise){
	    inAlways  = new EdgesNCallees(true);
	    outAlways = new EdgesNCallees(true);
	    outMaybe  = new EdgesNCallees(false);
	    skippedCSHistory = new LightRelation();
	    locks            = new LightRelation();
	}
	else{
	    lock_set = new HashSet();
	}
	id = ODInformation.odinfonum++;
    }

   /** Private constructor for <code>clone</code>. */
    private ODInformation(boolean precision, Set cs, 
			  EdgesNCallees ia, EdgesNCallees oa, EdgesNCallees om,
			  Relation cs_hist, Relation org_locks, Set org_lock_set)
    {
	precise = precision;
	skippedCS = cs;

	inAlways  = ia;
	outAlways = oa;
	outMaybe  = om;
	skippedCSHistory = cs_hist;
	locks     = org_locks;
	lock_set  = org_lock_set;
	id = ODInformation.odinfonum++;
    }


    /** Creates an <code>ODInformation</code> object, whose precision
     * is set by its boolean argument.
     */
    public ODInformation(boolean precision){
	skippedCS = new HashSet();

	precise = precision;

	if(precise){
	    inAlways  = new EdgesNCallees(true);
	    outAlways = new EdgesNCallees(true);
	    outMaybe  = new EdgesNCallees(false);
	    skippedCSHistory = new LightRelation();
	    locks            = new LightRelation();
	    lock_set         = null;
	}
	else{
	    locks    = null;
	    lock_set = new HashSet();
	}
	id = ODInformation.odinfonum++;
    }



    /** Joins two <code>ODInformation</code> objects.
     */
    public void join(ODInformation odi2)
    {
	Util.assert(precise==odi2.precise,
		    "We can only join EdgesNCallees of same precision");

	if(precise)
	    join_precise(odi2);
	else
	    join_conservative(odi2);
   }

    private void join_precise(ODInformation odi2)
    {
	Map hole_conversion  = new LightMap();
	hole_conversion.put(ODPointerAnalysis.BottomHole,ODPointerAnalysis.BottomHole);
	Set skippedCS_org = new HashSet(skippedCS);
	Set projected_skippedCS2 = new HashSet();

	for(Iterator h2_it=odi2.skippedCS.iterator(); h2_it.hasNext(); ){
	    MethodHole h2 = (MethodHole) h2_it.next();
	    // If this MethodHole is also a MethodHole of the first
	    // ODInformation, we keep it like that.
	    if(skippedCS.contains(h2)){
		hole_conversion.put(h2,h2);
		projected_skippedCS2.add(h2);
		continue;
	    }
	    // If this MethodHole is a MethodHole of the first
	    // ODInformation, under another copy, we tranpose it to
	    // that copy.
	    MethodHole h2_bis = h2.IsInAs(skippedCS);
	    if (h2_bis!=null){
		hole_conversion.put(h2, h2_bis);
		projected_skippedCS2.add(h2_bis);
	    }
	    else {
		// This hole is unknown so far, we keep it.
		hole_conversion.put(h2, h2);
		skippedCS.add(h2);
		projected_skippedCS2.add(h2);
	    }
	    // Note: this scheme suppose no redundance in each of the
	    // two set of skipped call sites, but possible redundance
	    // between them.
	}
	
	inAlways.join (odi2.inAlways,  hole_conversion, 
		       skippedCS_org, projected_skippedCS2,
		       skippedCS);
	outAlways.join(odi2.outAlways, hole_conversion, 
		       skippedCS_org, projected_skippedCS2,
		       skippedCS);
	outMaybe.join (odi2.outMaybe,  hole_conversion, 
		       skippedCS_org, projected_skippedCS2,
		       skippedCS);

	joinHistories(odi2, hole_conversion);
	joinLocks(odi2, hole_conversion);
    }


    private void join_conservative(ODInformation odi2)
    {
	for(Iterator h2_it=odi2.skippedCS.iterator(); h2_it.hasNext(); ){
	    MethodHole h2 = (MethodHole) h2_it.next();
	    // If this MethodHole is also a MethodHole of the first
	    // ODInformation, nothing to do.
	    if(skippedCS.contains(h2)){
		continue;
	    }
	    // If this MethodHole is a MethodHole of the first
	    // ODInformation, under another copy, nothing to do.
	    MethodHole h2_bis = h2.IsInAs(skippedCS);
	    if (h2_bis!=null){
		continue;
	    }
	    // This hole is unknown so far, we keep it.
	    skippedCS.add(h2);
	    // Note: this scheme suppose no redundance in each of the
	    // two set of skipped call sites, but possible redundance
	    // between them.
	}

	lock_set.addAll(odi2.lock_set);
    }


    private void joinHistories(ODInformation odi, Map conversion)
    {
	LightRelation new_hist = new LightRelation();
	Relation org_hist = odi.skippedCSHistory;
	
	for(Iterator first_it=org_hist.keys().iterator(); first_it.hasNext(); ){
	    MethodHole org_first = (MethodHole) first_it.next();
	    MethodHole new_first = (MethodHole) conversion.get(org_first);
	    if (new_first==null){
		System.err.println("joinHistories: No conversion for " 
				   + org_first);
		System.out.println("joinHistories: No conversion for " 
				   + org_first + "\n in "
				   + conversion +
				   "\n for " + org_hist);
		System.exit(1);
	    }
	    else{
		for(Iterator second_it=org_hist.getValues(org_first).iterator();
		    second_it.hasNext(); )
		    {
			MethodHole org_second = (MethodHole) second_it.next();
			MethodHole new_second = (MethodHole) conversion.get(org_second);
			if (new_second==null){
			    System.err.println("joinHistories (2): No conversion for " 
					       + org_second);
			    System.out.println("joinHistories (2): No conversion for " 
					       + org_second + "\n in "
					       + conversion + 
					       "\n for " + org_hist);
			    System.exit(1);
			}
			else{
			    new_hist.add(new_first,new_second);
			}
		    }
	    }
	}
	odi.skippedCSHistory = new_hist;
    }

    private void joinLocks(ODInformation odi, Map conversion)
    {
	LightRelation new_locks = new LightRelation();
	Relation org_locks = odi.locks; 
	
	for(Iterator sync_it=org_locks.keys().iterator(); sync_it.hasNext(); ){
	    PASync sync = (PASync) sync_it.next();
	    for(Iterator mh_it=org_locks.getValues(sync).iterator();
		mh_it.hasNext(); )
		{
		    MethodHole org_mh = (MethodHole) mh_it.next();
		    if (org_mh==null) {
			System.err.println("Null MethodHole in joinLocks...");
			System.out.println("Null MethodHole in joinLocks...");
			continue;
		    }
		    MethodHole new_mh = (MethodHole) conversion.get(org_mh);
		    if (new_mh==null){
			System.err.println("joinLocks: No conversion for " 
					   + org_mh);
			System.out.println("joinLocks: No conversion for " 
					   + org_mh + " in "
					   + conversion);
		    }
		    else{
			new_locks.add(sync,new_mh);
		    }
		}
	}
	odi.locks = new_locks;
    }


    public Object clone()
    {
	Set cs = new HashSet(skippedCS);

	if(this.precise){
	    EdgesNCallees ia = (EdgesNCallees) inAlways. clone();
	    EdgesNCallees oa = (EdgesNCallees) outAlways.clone();
	    EdgesNCallees om = (EdgesNCallees) outMaybe. clone();
	    
	    Relation cs_hist   = (Relation) skippedCSHistory.clone();
	    Relation the_locks = (Relation) locks.clone();
	    
	    return new ODInformation(this.precise, cs,
				     ia, oa, om,
				     cs_hist, the_locks, null);
	}
	else{
	    return new ODInformation(this.precise, cs,
				     null, null, null,
				     null, null, (Set) ((HashSet) lock_set).clone());
	}
    }
    


    public Object clonebis()
    {
	LightMap hole_convert = new LightMap();
	Set cs = MethodHole.DuplicateSet(this.skippedCS, hole_convert);

	if(this.precise){
	    EdgesNCallees ia = inAlways. clone(hole_convert);
	    EdgesNCallees oa = outAlways.clone(hole_convert);
	    EdgesNCallees om = outMaybe. clone(hole_convert);
	    
	    Relation cs_hist = MethodHole.DuplicateHistory(this.skippedCSHistory,
							   hole_convert);
	
	    Relation the_locks = MethodHole.DuplicateLocks(this.locks, hole_convert);
	    
	    return new ODInformation(this.precise, cs,
				     ia, oa, om,
				     cs_hist, the_locks, null);
	}
	else{
	    return new ODInformation(this.precise, cs,
				     null, null, null,
				     null, null, (Set) ((HashSet) lock_set).clone());
	}
    }
    

    public ODInformation specialize(Map nodeConversion)
    {
	LightMap hole_convert = new LightMap();
	Set cs = MethodHole.DuplicateSet(this.skippedCS, hole_convert, nodeConversion);

	if (this.precise){
	    EdgesNCallees ia = inAlways. clone(hole_convert, nodeConversion);
	    EdgesNCallees oa = outAlways.clone(hole_convert, nodeConversion);
	    EdgesNCallees om = outMaybe. clone(hole_convert, nodeConversion);

	    Relation cs_hist = MethodHole.DuplicateHistory(this.skippedCSHistory,
							   hole_convert);
	
	    Relation the_locks = MethodHole.DuplicateLocks(this.locks, hole_convert);

	    return new ODInformation(this.precise, cs,
				     ia, oa, om,
				     cs_hist, the_locks, null);
	}
	else{
	    return new ODInformation(this.precise, cs,
				     null, null, null,
				     null, null, (Set) ((HashSet) lock_set).clone());
	}
    }

    /** Pretty-print debug function.
     */
    public String toString()
    {
	StringBuffer buffer = new StringBuffer();
	
    	if (!DEBUG)
    	    return "";

	if (this==null) {
	    return "";
	}
	else{
	    buffer.append("Id = " + id);
  	    buffer.append("\n Skipped call sites \n" + skippedCS);

// 	    buffer.append("\n Inside  edges   always  after callees " +  inAlways);
// 	    buffer.append("\n Outside edges   always  after callees " + outAlways);
// 	    buffer.append("\n Outside edges sometines after callees " + outMaybe);

// 	    buffer.append("\n History of skipped call sites\n" + skippedCSHistory);
//  	    buffer.append("\n Synchronizations and skipped call sites\n" + locks + lock_set);

	    return buffer.toString();
	}
	
    }


    public void remove(MethodHole mh)
    {
	skippedCS.remove(mh);

	if (precise){
	    inAlways. remove(mh);
	    outAlways.remove(mh);
	    outMaybe. remove(mh);

	    // Removing mh from the skippedCSHistory field
	    skippedCSHistory.removeKey(mh);
	    for(Iterator it=skippedCSHistory.keys().iterator(); it.hasNext(); ){
		skippedCSHistory.remove(it.next(), mh);
	    }

	    // Removing mh from the locks field...
	    for(Iterator it=locks.keys().iterator(); it.hasNext(); ){
		PASync syncro = (PASync) it.next();
		locks.remove(syncro, mh);
	    }
	}
    }

    public Set predecessors(MethodHole mh)
    {
	if (!precise)
	    return null;
	else
	    return skippedCSHistory.getValues(mh);
    }


    public void addOutsideEdges(Set nodes1, String f, PANode n2)
    {
	if (!precise) return;

	outAlways.add(nodes1, f, n2, skippedCS);
	outMaybe. add(nodes1, f, n2, skippedCS);
    }

    public void addOutsideEdges(PANode n1, String f, PANode n2)
    {
	if (!precise) return;

	outAlways.add(n1, f, n2, skippedCS);
	outMaybe. add(n1, f, n2, skippedCS);
    }



    public void addOutsideEdges(PANode n1, String f, PANode n2,
				ODInformation reference,
				Set new_n1)
    {
	if (!reference.precise) return;

	Set always_holes = reference.outAlways.callees(n1, f, n2);
	outAlways.add(new_n1, f, n2, always_holes);

	Set maybe_holes  = reference.outMaybe.callees(n1, f, n2);
	outMaybe.add(new_n1, f, n2, maybe_holes);
    }

    public void addOutsideEdges(PANode n1, String f, PANode n2,
				ODInformation reference,
				Set new_n1, Set previous_holes)
    {
	if (!reference.precise) return;

	Set always_holes = reference.outAlways.callees(n1, f, n2);
	always_holes.addAll(previous_holes);
	outAlways.add(new_n1, f, n2, always_holes);

	Set maybe_holes  = reference.outMaybe.callees(n1, f, n2);
	maybe_holes.addAll(previous_holes);
	outMaybe.add(new_n1, f, n2, maybe_holes);
    }


    public void addInsideEdges(PANode n1, String f, PANode n2)
    {
	if (!precise) return;
	
	inAlways.add(n1, f, n2, skippedCS);
    }


    public void addInsideEdges(Set nodes1, String f, Set nodes2)
    {
	if (!precise) return;
	
	for(Iterator it1=nodes1.iterator(); it1.hasNext(); ){
	    PANode n1 = (PANode) it1.next();
	    for(Iterator it2=nodes2.iterator(); it2.hasNext(); ){
		PANode n2 = (PANode) it2.next();
		inAlways.add(n1, f, n2, skippedCS);
	    }
	}
    }



    public void addInsideEdges(PANode n1, String f, PANode n2,
			       ODInformation reference,
			       Set new_n1, Set new_n2)
    {
	if (!precise) return;

	Set always_holes = reference.inAlways.callees(n1, f, n2);
	inAlways.add(new_n1, f, new_n2, always_holes);
    }

    public void addInsideEdges(PANode n1, String f, PANode n2,
			       ODInformation reference,
			       Set new_n1, Set new_n2,
			       Set previous_holes)
    {
	if (!precise) return;

	Set always_holes = reference.inAlways.callees(n1, f, n2);
	always_holes.addAll(previous_holes);
	inAlways.add(new_n1, f, new_n2, always_holes);
    }


    public void addLock(PASync sync)
    {
	if (precise) 
	    locks.addAll(sync, skippedCS);
	else
	    lock_set.add(sync);
    }


    public void addHole(MethodHole hole)
    {
	if (precise){
	    skippedCSHistory.addAll(hole, skippedCS);
	    skippedCS.add(hole);
	}
	else
	    if(hole.IsInAs(skippedCS)==null)
		skippedCS.add(hole);
    }



    public Set update(ODInformation new_odi,
		      ODParIntGraph pig_caller, 
		      ODParIntGraph pig_callee, 
		      Relation first_mapping,
		      Relation second_mapping_extended,
		      MethodHole hole,
		      ODInformation first_part,
		      ODInformation second_part,
		      Set toberemoved)
    {
// 	ODInformation new_odi = new ODInformation(precise);


	// ************************
	// Updating method holes
	// ************************

	// Initialization needed for the conversion of the method holes
	MethodHole.reset(pig_caller.odi.skippedCS,pig_callee.odi.skippedCS);
	    
	// Transposing the callee method_holes set
	MethodHole [] mh_converter = 
	    new MethodHole[skippedCS.size()+pig_callee.odi.skippedCS.size()+1];
	int mapsize = 0;
	HashSet mh_from_callee_new = new HashSet();

	Set mh_from_callee_org = pig_callee.odi.skippedCS;
	    
	mapsize = project_method_holes(first_mapping,
				       mh_from_callee_org,
				       mh_converter,
				       mapsize,
				       hole,
				       mh_from_callee_new);



	if(hole!=null){
	    // Transposing the caller method_holes set
	    skippedCS.remove(hole);
	    
	    mapsize = project_method_holes(second_mapping_extended,
					   skippedCS,
					   mh_converter,
					   mapsize,
					   new_odi);
	}

	// Computation of the new method_holes set
	new_odi.skippedCS.addAll(mh_from_callee_new);


	if(!precise) {
	    return lock_set;
	}


	// Update history of skipped call sites
	update_skippedCSHistory(skippedCSHistory,
				mh_converter,
				mapsize,
				hole,
				mh_from_callee_new,
				new_odi,
				pig_callee.odi.skippedCSHistory);



	
	// ************************
	// Updating in_edge_always
	// ************************

// 	System.out.println("Original inAlways " + inAlways);
// 	System.out.println("Original first_part inAlways " + first_part.inAlways);
// 	System.out.println("Original second_part inAlways " + second_part.inAlways);
// 	System.out.println("to be removed " + toberemoved);
	

	// Update the caller field (if necessary).
	if (hole!=null){
	    update(new_odi.inAlways, inAlways,
		   mh_converter, mapsize,
		   mh_from_callee_new,
		   hole,
		   toberemoved);
	}
	else
	    new_odi.inAlways = (EdgesNCallees) inAlways.clone();

	// Update and include the edges created by the
	// ``classical'' part of the map-up.
	update(new_odi.inAlways, first_part.inAlways,
	       mh_converter, mapsize,
	       mh_from_callee_new,
	       hole,
	       toberemoved);
	
	// Update and include the edges created by the second part
	// of the map-up.
	if (hole!=null){
	    update_and_add(new_odi.inAlways,
			   second_part.inAlways,
			   mh_converter,
			   hole,
			   toberemoved);
	}


	
	// ************************
	// Updating out_edge_always
	// ************************
	// Update the caller field (if necessary).

	if (hole!=null){
	    update(new_odi.outAlways, outAlways,
		   mh_converter, mapsize,
		   mh_from_callee_new,
		   hole,
		   toberemoved);
	}
	else
	    new_odi.outAlways = (EdgesNCallees) outAlways.clone();
		
	// Update and include the edges created by the
	// ``classical'' part of the map-up.
	update(new_odi.outAlways,  first_part.outAlways,
	       mh_converter, mapsize,
	       mh_from_callee_new,
	       hole,
	       toberemoved);

	// Update and include the edges created by the second part
	// of the map-up.
	if (hole!=null){
	    update_and_add(new_odi.outAlways,
			   second_part.outAlways,
			   mh_converter,
			   hole,
			   toberemoved);
	}



	// ************************
	// Updating out_edge_maybe
	// ************************
	// Update the caller field (if necessary).
	if (hole!=null){
	    update(new_odi.outMaybe,   outMaybe,
		   mh_converter, mapsize,
		   mh_from_callee_new,
		   hole,
		   toberemoved);
	}
	else
	    new_odi.outMaybe = (EdgesNCallees) outMaybe.clone();
		
	// Update and include the edges created by the ``classical''
	// part of the map-up.
	update(new_odi.outMaybe,   first_part.outMaybe,
	       mh_converter, mapsize,
	       mh_from_callee_new,
	       hole,
	       toberemoved);

	// Update and include the edges created by the second part
	// of the map-up.
	if (hole!=null){
	    update_and_add(new_odi.outMaybe,
			   second_part.outMaybe,
			   mh_converter,
			   hole,
			   toberemoved);
	}


	// ************************
	// Updating locks
	// ************************
	   
	Set par_locks = null;
	par_locks = update_locks(this,
				 new_odi,
				 pig_callee.odi,
				 first_mapping,
				 second_mapping_extended,
				 mh_converter,
				 hole,
				 mh_from_callee_new);

	return par_locks;
    }

    private int project_method_holes(Relation mu, Set holes,
				     MethodHole [] map,
				     int mapsize,
				     ODInformation new_odi)
    {
	MethodHole new_mh;

	for(Iterator it=holes.iterator(); it.hasNext(); ){
	    MethodHole mh = (MethodHole) it.next();
	    int mh_id = mh.IsInAs(map, mapsize);
	    if (mh_id<0){
		new_mh=projection(mu,mh);
		new_odi.skippedCS.add(new_mh);
		map[mapsize] = new_mh;
		mh.setId(mapsize);
		mapsize++;
	    }
	    else{
		mh.setId(mh_id);
	    }
	}
	return mapsize;
    } 

    
    private int project_method_holes(Relation mu, 
				     Set holes,
				     MethodHole [] map,
				     int mapsize,
				     MethodHole hole,
				     Set transposed_holes)
    {
	MethodHole new_mh = null;
	int depth = 0;
	if (hole!=null)
	    depth = hole.depth();
	
	int bottomid = ODPointerAnalysis.BottomHole.IsInAs(map, mapsize);
	if (bottomid<0){
	    map[mapsize] = ODPointerAnalysis.BottomHole;
	    ODPointerAnalysis.BottomHole.setId(mapsize);
	    mapsize++;
	}

	for(Iterator it=holes.iterator(); it.hasNext(); ){
	    MethodHole mh1 = (MethodHole) it.next();
	    int id = mh1.IsInAs(map, mapsize);
	    if (id<0){
		new_mh=projection(mu,mh1,depth,hole.callsitehistory());
		map[mapsize] = new_mh;
		mh1.setId(mapsize);
		mapsize++;
		transposed_holes.add(new_mh);
	    }
	    else {
		mh1.setId(id);
	    }
	}
	return mapsize;
    } 


    private static MethodHole projection(Relation mu, MethodHole org)
    {
	return projection(mu,org,0,null);
    }

    private static MethodHole projection(Relation mu, MethodHole org,
					 int depth, LinkedList history)
    {
	Set [] org_params = org.ungroupedparameters();
	Set [] new_params = new Set[org_params.length];
	for(int i = 0; i< org_params.length; i++){
	    new_params[i] = new HashSet();
	    for(Iterator j=org_params[i].iterator(); j.hasNext(); ){
		PANode n = (PANode) j.next();
		Set nodes = mu.getValues(n);
		new_params[i].addAll(nodes);
	    }
	}
	
	MethodHole projected = new MethodHole(org, new_params,
					      depth,
					      history);
	return projected;
    }


    public void update_skippedCSHistory(Relation caller_hist,
					MethodHole[] map,
					int mapsize,
					MethodHole hole,
					Set hole_corresp,
					ODInformation new_odi,
					Relation callee_hist)
    {
	if(hole==null)
	    new_odi.skippedCSHistory = (Relation) caller_hist.clone();
	else{
	    for(Iterator it=caller_hist.keys().iterator(); it.hasNext(); ){
		MethodHole mh = (MethodHole) it.next();

		if (mh!=hole) {
		    HashSet holes = new HashSet();
		    for(Iterator h_it=caller_hist.getValues(mh).iterator(); 
			h_it.hasNext(); ){
			MethodHole mh_2 = (MethodHole) h_it.next();

			if (mh_2!=hole) {
			    holes.add(map[mh_2.Id()]);
			}
			else{
			    holes.addAll(hole_corresp);
			}
		    }
		    
		    new_odi.skippedCSHistory.addAll(map[mh.Id()],holes);
		}
		else {
		    HashSet holes = new HashSet();
		    for(Iterator h_it=caller_hist.getValues(mh).iterator();h_it.hasNext(); ){
			MethodHole mh_2 = (MethodHole) h_it.next();
			
			if (mh_2!=hole) 
			    holes.add(map[mh_2.Id()]);
			else{
			    System.err.println("Error in some hole history ");
			    System.err.println("a hole precedes itself!");
			    System.out.println("Error in some hole history ");
			    System.out.println("a hole precedes itself!");
			    System.out.println("hole: " + hole);
			    System.out.println("history: " + caller_hist);
			}
		    }
		    for(Iterator hole_it=hole_corresp.iterator(); hole_it.hasNext(); )
			new_odi.skippedCSHistory.addAll(hole_it.next(),
							holes);
		}
	    }
	}
	
	for(Iterator it=callee_hist.keys().iterator(); it.hasNext(); ){
	    MethodHole mh = (MethodHole) it.next();
	    HashSet holes = new HashSet();

	    for(Iterator h_it=callee_hist.getValues(mh).iterator(); 
		h_it.hasNext(); ){
		MethodHole mh_2 = (MethodHole) h_it.next();
		holes.add(map[mh_2.Id()]);
	    }
	    new_odi.skippedCSHistory.addAll(map[mh.Id()], holes);
	}
    }


    public void update_skippedCSHistorybis(Relation caller_hist,
					MethodHole[] map,
					int mapsize,
					MethodHole hole,
					Set hole_corresp,
					ODInformation new_odi,
					Relation callee_hist)
    {
	int id1 = -1, id2 = -1;
	MethodHole new_mh = null, new_mh_2 = null;
	
	if(hole==null)
	    new_odi.skippedCSHistory = (Relation) caller_hist.clone();
	else{
	    for(Iterator it=caller_hist.keys().iterator(); it.hasNext(); ){
		MethodHole mh = (MethodHole) it.next();

		if (mh!=hole) {
		    id1 = mh.Id(); 
		    if (id1<0){
			System.err.println("Error in update (1), no available " 
					   + "conversion for method hole");
			System.out.println("Error in update (1), no available " 
					   + "conversion for method hole");
			System.out.println(mh);
			System.out.println(" in " + map);
		    }
		    new_mh = map[id1];
		    for(Iterator h_it=caller_hist.getValues(mh).iterator(); 
			h_it.hasNext(); ){
			MethodHole mh_2 = (MethodHole) h_it.next();

			if (mh_2!=hole) {
			    id2 = mh_2.Id();
			    if (id2<0){
				System.err.println("Error in update (2), no available " 
						   + "conversion for method hole");
				System.out.println("Error in update (2), no available " 
						   + "conversion for method hole");
				System.out.println(mh_2 + " in " + map);
			    }
			    
			    new_odi.skippedCSHistory.add(new_mh, 
							 map[id2]);
			}
			else{
			    new_odi.skippedCSHistory.addAll(new_mh,hole_corresp);
			}
		    }
		}
		else {
		    for(Iterator h_it=caller_hist.getValues(mh).iterator();h_it.hasNext(); ){
			MethodHole mh_2 = (MethodHole) h_it.next();
			
			if (mh_2!=hole) {
			    id2 = mh_2.Id();
			    if (id2<0){
				System.err.println("Error in update (3), no available " 
						   + "conversion for method hole");
				System.out.println("Error in update (3), no available " 
						   + "conversion for method hole");
				System.out.println(mh_2 + " in " + map);
			    }
			    new_mh_2 = map[mh_2.Id()];
			    for(Iterator hole_it=hole_corresp.iterator(); 
				hole_it.hasNext(); )
				new_odi.skippedCSHistory.add(hole_it.next(),
							     new_mh_2);
			}
			else{
			    System.err.println("Error in some hole history ");
			    System.err.println("a hole precedes itself!");
			    System.out.println("Error in some hole history ");
			    System.out.println("a hole precedes itself!");
			    System.out.println("hole: " + hole);
			    System.out.println("history: " + caller_hist);
			}
		    }
		}
	    }
	}
	
	for(Iterator it=callee_hist.keys().iterator(); it.hasNext(); ){
	    MethodHole mh = (MethodHole) it.next();
	    id1 = mh.Id(); 
	    if (id1<0){
		System.err.println("Error in update (4), no available " 
				   + "conversion for method hole");
		System.out.println("Error in update (4), no available " 
				   + "conversion for method hole");
		System.out.println(mh + "\n in " + callee_hist);
	    }
	    new_mh = map[mh.Id()];

	    for(Iterator h_it=callee_hist.getValues(mh).iterator(); 
		h_it.hasNext(); ){
		MethodHole mh_2 = (MethodHole) h_it.next();
		id2 = mh_2.Id(); 
		if (id2<0){
		    System.err.println("Error in update (5), no available " 
				       + "conversion for method hole");
		    System.out.println("Error in update (5), no available " 
				       + "conversion for method hole");
		    System.out.println(mh_2 + "\n in " + callee_hist);
		}
		new_odi.skippedCSHistory.add(new_mh, map[id2]);
	    }
	}
    }


    public void update(EdgesNCallees edges_new,
		       EdgesNCallees edges_org,
		       MethodHole [] map,
		       int mapsize,
		       Set mh_from_callee_new,
		       MethodHole m_hole,
		       Set toberemoved)
    {
	for(Iterator in_n=edges_org.edges.keySet().iterator(); in_n.hasNext(); ){
	    PANode n1 = (PANode) in_n.next();
	    if (toberemoved.contains(n1)) {System.out.println("Useful toberemoved"); continue;}
	    Map n1_map = (Map) edges_org.edges.get(n1);

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next();
		Relation n1_f = (Relation) n1_map.get(f);

		for(Iterator out_n=n1_f.keys().iterator(); out_n.hasNext(); ){
		    PANode n2 = (PANode) out_n.next();
		    if (toberemoved.contains(n2)) {System.out.println("Useful toberemoved"); continue;}
		    Set holes = n1_f.getValues(n2);
		    HashSet newholes = new HashSet();

		    for(Iterator it_mh=holes.iterator(); it_mh.hasNext(); ){
			MethodHole tmp_mh = (MethodHole) it_mh.next();
			if (tmp_mh==m_hole){
			    newholes.addAll(mh_from_callee_new);
			}
			else 
			    newholes.add(map[tmp_mh.Id()]);
		    }
		    edges_new.add(n1, f, n2, newholes);
		}
	    }
	}
    }

    public void updatebis(EdgesNCallees edges_new,
		       EdgesNCallees edges_org,
		       MethodHole [] map,
		       int mapsize,
		       Set mh_from_callee_new,
		       MethodHole m_hole,
		       Set toberemoved)
    {
	for(Iterator in_n=edges_org.edges.keySet().iterator(); in_n.hasNext(); ){
	    PANode n1 = (PANode) in_n.next();
	    if (toberemoved.contains(n1)) {System.out.println("Useful toberemoved"); continue;}
	    Map n1_map = (Map) edges_org.edges.get(n1);

	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next();
		Relation n1_f = (Relation) n1_map.get(f);

		for(Iterator out_n=n1_f.keys().iterator(); out_n.hasNext(); ){
		    PANode n2 = (PANode) out_n.next();
		    if (toberemoved.contains(n2)) {System.out.println("Useful toberemoved"); continue;}
		    Set holes = n1_f.getValues(n2);
		    HashSet newholes = new HashSet();

		    for(Iterator it_mh=holes.iterator(); it_mh.hasNext(); ){
			MethodHole tmp_mh = (MethodHole) it_mh.next();
			if (tmp_mh==m_hole){
			    newholes.addAll(mh_from_callee_new);
			}
			else {
			    if (tmp_mh.Id()>=0){
				newholes.add(map[tmp_mh.Id()]);
			    }
			    else {
				System.err.println("Problem: no translation available "
						   + " in either caller's and callee's "
						   + "map! (1)" + tmp_mh);
				System.out.println("Problem: no translation available "
						   + " in either caller's and callee's "
						   + "map! (1)\n" + tmp_mh + "\n in "
						   + map);
			    }
			}
		    }
		    edges_new.add(n1, f, n2, newholes);
		}
	    }
	}
    }

    public static void update_and_add(EdgesNCallees edges,
				      EdgesNCallees addenda,
				      MethodHole [] map,
				      MethodHole hole,
				      Set toberemoved)
    {
	for(Iterator in_n=addenda.edges.keySet().iterator(); in_n.hasNext(); ){
	    PANode n1 = (PANode) in_n.next();
	    if (toberemoved.contains(n1)) continue;
	    Map n1_map = (Map) addenda.edges.get(n1);
	    
	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		for(Iterator out_n=n1_f.keys().iterator(); out_n.hasNext(); ){
		    PANode n2 = (PANode) out_n.next();
		    if (toberemoved.contains(n2)) continue;
		    Set holes = n1_f.getValues(n2);
		    // We are not converting the hole we are removing...
		    holes.remove(hole);
		    HashSet new_holes = new HashSet();
		    
		    for(Iterator it_mh=holes.iterator(); it_mh.hasNext(); ){
			MethodHole org_mh = (MethodHole)it_mh.next();
			new_holes.add(map[org_mh.Id()]);
		    }
		    edges.add(n1, f, n2, new_holes);
		}
	    }
	}
    }


    public static void update_and_addbis(EdgesNCallees edges,
				      EdgesNCallees addenda,
				      MethodHole [] map,
				      MethodHole hole,
				      Set toberemoved)
    {
	for(Iterator in_n=addenda.edges.keySet().iterator(); in_n.hasNext(); ){
	    PANode n1 = (PANode) in_n.next();
	    if (toberemoved.contains(n1)) continue;
	    Map n1_map = (Map) addenda.edges.get(n1);
	    
	    for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		String f = (String) it_f.next(); 
		Relation n1_f = (Relation) n1_map.get(f);
		for(Iterator out_n=n1_f.keys().iterator(); out_n.hasNext(); ){
		    PANode n2 = (PANode) out_n.next();
		    if (toberemoved.contains(n2)) continue;
		    Set holes = n1_f.getValues(n2);
		    // We are not converting the hole we are removing...
		    holes.remove(hole);
		    HashSet new_holes = new HashSet();

		    for(Iterator it_mh=holes.iterator(); it_mh.hasNext(); ){
			MethodHole org_mh = (MethodHole)it_mh.next();
			if (org_mh.Id()<0){
			    System.err.println("BUG: no conversion available");
			    System.out.println("BUG: no conversion available");
			    System.out.println("hole : " + org_mh);
			    System.out.println("mapping : " + map);
			}
			else
			    new_holes.add(map[org_mh.Id()]);
		    }
		    edges.add(n1, f, n2, new_holes);
		}
	    }
	}
    }



    public static Set update_locks(ODInformation odi_org,
				   ODInformation odi_new,
				   ODInformation odi_callee,
				   Relation  first_mapping,
				   Relation second_mapping,
				   MethodHole [] mh_converter,
				   MethodHole hole,
				   Set hole_substitute)
    {
	HashSet par_sync = new HashSet();
// 	System.out.println("\n\nHole " + hole);
// 	System.out.println("\n\nHole substitute");
// 	for(Iterator sub_it=hole_substitute.iterator(); sub_it.hasNext(); )
// 	    System.out.println(sub_it.next());

	

	if(hole==null){
	    odi_new.locks = (Relation) odi_org.locks.clone();
	    System.err.println("???????");
	    System.out.println("???????");
	}
	else{
// 	    System.out.println("\n\nLock holes\n\n");
// 	    for(Iterator it=odi_org.locks.keys().iterator(); it.hasNext(); ){
// 		PASync syncro = (PASync) it.next();
// 		Set par_holes = odi_org.locks.getValues(syncro);
// 		for(Iterator h_it=par_holes.iterator(); h_it.hasNext(); ){
// 		    MethodHole mh = (MethodHole) h_it.next();
// 		    System.out.println(mh);
// 		}
// 	}

	    for(Iterator it=odi_org.locks.keys().iterator(); it.hasNext(); ){
		PASync syncro = (PASync) it.next();
		Set mapped_syncro = syncro.project(second_mapping);
		boolean parallel  = false;
		
		Set par_holes = odi_org.locks.getValues(syncro);
		HashSet new_par_holes = new HashSet();
		for(Iterator h_it=par_holes.iterator(); h_it.hasNext(); ){
		    MethodHole mh = (MethodHole) h_it.next();
		    if (mh==hole){
			new_par_holes.addAll(hole_substitute);
			parallel = true;
		    }
		    else{
			if (mh!=null){
			    if(mh.Id()>=0)
				new_par_holes.add(mh_converter[mh.Id()]);
			    else
				System.out.println("Problems with methods holes...");
			}
			else
			    System.out.println("Problems with methods holes...");
		    }
		}
		
		if (parallel)
		    par_sync.addAll(mapped_syncro);
		
		for(Iterator sync_it=mapped_syncro.iterator(); sync_it.hasNext(); )
		    odi_new.locks.addAll(sync_it.next(),new_par_holes);
	    }
	}
	
// 	System.out.println("\n\nLock holes (2)\n\n");
// 	for(Iterator it=odi_new.locks.keys().iterator(); it.hasNext(); ){
// 	    PASync syncro = (PASync) it.next();
// 	    Set par_holes = odi_new.locks.getValues(syncro);
// 	    for(Iterator h_it=par_holes.iterator(); h_it.hasNext(); ){
// 		MethodHole mh = (MethodHole) h_it.next();
// 		System.out.println(mh);
// 	    }
// 	}


	
	Set holes_b4_callee = odi_org.predecessors(hole);
    HashSet new_holes_b4_callee = new HashSet();
	for(Iterator it=holes_b4_callee.iterator(); it.hasNext(); ){
	    MethodHole temp_mh = (MethodHole) it.next();
	    if(temp_mh.Id()<0){
		System.err.println("No conversion available in update_locks");
		System.out.println("No conversion available in update_locks");
	    }
	    new_holes_b4_callee.add(mh_converter[temp_mh.Id()]);
	}
	// Update and add the callee locks
	for(Iterator it=odi_callee.locks.keys().iterator(); it.hasNext(); ){
	    PASync syncro = (PASync) it.next();
	    Set mapped_syncro = syncro.project(first_mapping);
	    
	    Set par_holes = odi_callee.locks.getValues(syncro);
	    HashSet new_par_holes = new HashSet();
	    new_par_holes.addAll(new_holes_b4_callee);
	    for(Iterator h_it=par_holes.iterator(); h_it.hasNext(); ){
		MethodHole temp_mh = (MethodHole) h_it.next();
		if(temp_mh.Id()<0){
		    System.err.println("No conversion available in update_locks (2)");
		    System.out.println("No conversion available in update_locks (2)");
		}
		new_par_holes.add(mh_converter[temp_mh.Id()]);
	    }
	    for(Iterator sync_it=mapped_syncro.iterator(); sync_it.hasNext(); )
		odi_new.locks.addAll(sync_it.next(), new_par_holes);
	}


// 	System.out.println("\n\nLock holes (3)\n\n");
// 	for(Iterator it=odi_new.locks.keys().iterator(); it.hasNext(); ){
// 	    PASync syncro = (PASync) it.next();
// 	    Set par_holes = odi_new.locks.getValues(syncro);
// 	    for(Iterator h_it=par_holes.iterator(); h_it.hasNext(); ){
// 		MethodHole mh = (MethodHole) h_it.next();
// 		System.out.println(mh);
// 	    }
// 	}


	return par_sync;
    }
    


}

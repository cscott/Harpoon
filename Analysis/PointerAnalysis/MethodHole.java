// MethodHole.java, created Fri Jul 28 by vivien
// Copyright (C) 2000 Frederic VIVIEN <vivien@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details. 
package harpoon.Analysis.PointerAnalysis;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;
import java.util.List;
import java.util.LinkedList;

import harpoon.Util.Util;

import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.MetaMethods.MetaMethod;

/**
 * <code>MetHolSet</code> contains the information corresponding to
 * method holes in a Parallel Interaction Graph.
 * 
 * @author  Frederic VIVIEN <vivien@lcs.mit.edu>
 * @version $Id: MethodHole.java,v 1.4 2002-04-10 03:00:42 cananian Exp $ */
public class MethodHole implements java.io.Serializable {
    private static boolean DEBUG  = false;
    private static boolean DEBUG2  = false;

    /** The call site of the unanalyzed method. */
    private CALL call_site;

    /** History of call sites between the original skipped call site,
     * and the current method. */
    private LinkedList sites_history;

    /** The array of the meta methods that can be called at call site
        <code>call_site</code>. */
    private MetaMethod[] mms;

    /** Set of all the parameter nodes for the unanalyzed method. */
    private Set __arguments;
    private Set __parameters;
    private Set [] __ungroupedparameters;
    private PANode retNode;
    private PANode excNode;

    // The rank of this method hole in the method holes generated for
    // the same call site (in case of loop or recursion).
    private int number;

    // The depth at which this method hole was found
    private int deepness;

    // Variable which enables to track down a MethodHole during the
    // MethodHoles conversioon pahse of mapups...
    private int mapup;
    private int id;
    private static int mapUpId = -1;
    private int numero;
    private static int nmh = 0;


    /** Return a set containing all the possible parameters.
     */
    public Set parameters() {
	if (__parameters==null){
	    __parameters = new HashSet();
	    for(int i=0; i<__ungroupedparameters.length; i++)
		__parameters.addAll(__ungroupedparameters[i]);
	}
	return __parameters;
    }
 
    /** Return a set containing all the possible parameters, and the
     * formal return and exception nodes.
     */
    public Set arguments() {
	if(__arguments==null){
	    __arguments = new HashSet(parameters());
	    if(ret()!=null)
		__arguments.add(ret());
	    if(exc()!=null)
		__arguments.add(exc());
	}
	return __arguments;
    }
 

    /** Return an array of set of possible actual parameters, each set
     * corresponding to a formal parameter.
     */
    public Set [] ungroupedparameters() {
	return __ungroupedparameters;
    }
 


    /** Creates a <code>MethodHole</code>. 
     */
    public MethodHole(CALL q, Set params, MetaMethod[] maybecalled,
		      Set [] ungroupedparams, PANode ret, PANode exc,
		      int rk, int dpth) {
	call_site = q;
	mms = maybecalled;
	__parameters = params;
	__arguments = null;
	__ungroupedparameters = ungroupedparams;
	retNode  = ret;
	excNode  = exc;
	number   = rk;
	deepness = dpth;
	if(ODPointerAnalysis.ODA_precise){
	    sites_history = new LinkedList();
	    sites_history.add(new Integer(number));
	}
	else
	    sites_history = new LinkedList();
	mapup = -1;
	id    = -1;
	numero = MethodHole.nmh++;
    }



    /** Creates a <code>MethodHole</code> which is a copy of the one
     * given as argument, except for the parameters.
     */
    public MethodHole(MethodHole ref, Set [] ungroupedparams){
	call_site = ref.callsite();
	mms = ref.callees();
	__ungroupedparameters = ungroupedparams;
	__parameters = null;
	__arguments  = null;
	retNode  = ref.ret();
	excNode  = ref.exc();
	number   = ref.rank();
	deepness = ref.depth();
	sites_history = new LinkedList(ref.callsitehistory());
	mapup = -1;
	id    = -1;
	numero = MethodHole.nmh++;
    }


    /** Creates a <code>MethodHole</code> which is a copy of the one
     * given as argument, except that the parameters are mapped using
     * the second argument.
     */
    public MethodHole(MethodHole ref, Map nodemap){
	call_site = ref.callsite();
	mms = ref.callees();
	retNode  = ref.ret();
	excNode  = ref.exc();
	number   = ref.rank();
	deepness = ref.depth();
	sites_history = new LinkedList(ref.callsitehistory());

	__ungroupedparameters = new Set [ref.ungroupedparameters().length];
	__parameters = null;
	__arguments  = null;

	for(int i=0; i<ref.ungroupedparameters().length; i++){
	    __ungroupedparameters[i] = new HashSet();
	    Set params_i = ref.ungroupedparameters()[i];
	    if ((params_i!=null)&&(!(params_i.isEmpty())))
		for(Iterator j=params_i.iterator(); j.hasNext(); ){
		    PANode n = (PANode) j.next();
		    PANode new_n = (PANode) nodemap.get(n);
		    if (new_n==null) {
			System.err.println("Node with null translation: " + n);
			System.out.println("Node with null translation: " + n);
			System.out.println("in method hole " + ref);
			System.exit(1);
		    }
		    __ungroupedparameters[i].add(new_n);
		}
	}
	mapup = -1;
	id    = -1;
	numero = MethodHole.nmh++;
    }


    /** Creates a <code>MethodHole</code> which is a copy of the one
     * given as argument, except for the parameters. 
     */
    public MethodHole(MethodHole ref, Set [] ungroupedparams, int incr, LinkedList ranks){
	call_site = ref.callsite();
	mms = ref.callees();
	__ungroupedparameters = ungroupedparams;
	__parameters = null;
	__arguments  = null;
	retNode  = ref.ret();
	excNode  = ref.exc();
	number   = ref.rank();
	deepness = ref.depth()+incr;
	sites_history = new LinkedList(ref.callsitehistory());
	if (ranks!=null)
	    sites_history.addAll(ranks);
	if ((ranks==null)&&(incr!=0)) {
	    System.err.println("Error in MethodHole creation...");
	    System.out.println("Error in MethodHole creation...");
	    System.out.println("incr = " + incr +",  CALL= " + ranks);
	    System.exit(1);
	}
	mapup = -1;
	id    = -1;
	numero = MethodHole.nmh++;
    }


    /** Return the <code>call site</code>. 
     */
    public CALL callsite(){
	return call_site;
    }

    /** Return the <code>history of call sites</code>. 
     */
    public LinkedList callsitehistory(){
	return sites_history;
    }

    /** Return the <code>rank</code>. 
     */
    public int rank(){
	return number;
    }

    /** Return the <code>depth</code>. 
     */
    public int depth(){
	return deepness;
    }

    /** Return the array of callees <code>MetaMethod</code>s. 
     */
    public MetaMethod[]  callees(){
	return mms;
    }

    /** Return the <code>HMethod</code> corresponding to the call site. 
     */
    public HMethod method(){
	return call_site.method();
    }


    /** Return the result node. 
     */
    public PANode ret(){
	return retNode;
    }


    /** Return the exception node. 
     */
    public PANode exc(){
	return excNode;
    }


    /** Check whether <code>node</code> is a parameter node for the
        unanalyzed method. 
    */
    public boolean contains(PANode node) {
	for(int i=0; i<__ungroupedparameters.length; i++)
	    if(__ungroupedparameters[i].contains(node))  return true;
	return false;
    }


    /** Check whether at least one of the <code>nodes</code> is a
        parameter node for the unanalyzed method. 
    */
    public boolean containsnode(Set nodes) {
	for(Iterator it=nodes.iterator(); it.hasNext(); )
	    if (contains((PANode)it.next()))
		return true;
	return false;
    }


    /** Check whether <code>hm</code> is the method called at the
        unanalyzed call site. 
    */
    public boolean contains(HMethod hm) {
	return hm.equals(call_site.method());
    }


    /** Set the value of the field id. 
     */
    public void setId(int IntID){
	id = IntID;
    };


    /** Read the value of the field id. 
     */
    public int Id() {
	assert mapup==mapUpId : "Bad initialization value (not " +  
		    mapUpId+ ") for MethodHole " + this;
	return id;
    };


    /** Reset the global mapup flag, and the id and mapup fields of
     * the <code>MethodHoles</code> given in argument.
     */
    public static void reset(Set holes1, Set holes2)
    {
	mapUpId++;
	for(Iterator it=holes1.iterator(); it.hasNext(); ){
	    MethodHole mh = (MethodHole) it.next();
	    mh.id = -1;
	    mh.mapup = mapUpId;
	}
	for(Iterator it=holes2.iterator(); it.hasNext(); ){
	    MethodHole mh = (MethodHole) it.next();
	    mh.id    = -1;
	    mh.mapup = mapUpId;
	}

	ODPointerAnalysis.BottomHole.id    = -1;
	ODPointerAnalysis.BottomHole.mapup =  mapUpId;
    }


    /** Pretty-print function for debug purposes. 
     */
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append("MethodHole {");
	buffer.append(" " + numero + " ");

	if (this==ODPointerAnalysis.BottomHole){
	    buffer.append(" ***bottom*** }");
	    return buffer.toString();
	}

	buffer.append("Par [");
	for(int i=0; i< __ungroupedparameters.length; i++){
	    for(Iterator parit = (__ungroupedparameters[i]).iterator(); 
		parit.hasNext(); ){
		PANode par = (PANode) parit.next();
		buffer.append(" " + par);		
	    }
	    if (i< __ungroupedparameters.length-1) buffer.append(";");
	}
	buffer.append("]; ");
	buffer.append("Ret ["+retNode+"]; ");
	buffer.append("Exc ["+excNode+"]; ");
	buffer.append("Rank ["+rank()+"]; ");
	buffer.append("Depth ["+depth()+"]; ");
	buffer.append(sites_history);

	for(int i=0; i<mms.length; i++)
	    buffer.append(" " + mms[i]);

 	if(DEBUG2)
	    {
		buffer.append(" Id " + id + " ");
		buffer.append("MapUp " + mapup + " ");
	    }
	buffer.append("}");
	

	return buffer.toString();
    }


    /** Clone a <code>MethodHole</code>.
     */
    public Object clone(){
 	return new MethodHole(this, ungroupedparameters());
    }


    /** Dummy function which creates a set of <code>MethodHole</code>
     * which is a copy of the old one, while building an identity
     * mapping other these <code>MethodHole</code>s.
     */
    public static Set DuplicateSet(Set org_holes, Map conversion)
    {
	HashSet new_holes = new HashSet();

	for(Iterator mh_it=org_holes.iterator(); mh_it.hasNext(); ){
	    MethodHole org_mh = (MethodHole) mh_it.next();
	    if (org_mh!=null){
		conversion.put(org_mh, org_mh);
		new_holes.add(org_mh);
	    }
	    else{
		System.err.println("Hole is null!!!");
		System.out.println("Hole is null!!!");
		System.out.println(org_holes);
		System.exit(1);
	    }
	}

	return new_holes;
    }

    /** Create a new set of <code>MethodHole</code> from an old one
     * and a <code>PANode</code> conversion table. This method outputs
     * the new set and produces a conversion map from the old
     * <code>MethodHole</code>s to the new ones.
     */
    public static Set DuplicateSet(Set org_holes, Map conversion, Map node_conversion)
    {
	HashSet new_holes = new HashSet();

	for(Iterator mh_it=org_holes.iterator(); mh_it.hasNext(); ){
	    MethodHole org_mh = (MethodHole) mh_it.next();
	    if (org_mh!=null){
		MethodHole new_mh = null;
		new_mh = new MethodHole (org_mh, node_conversion);
		conversion.put(org_mh, new_mh);
		new_holes.add(new_mh);
	    }
	    else{
		System.err.println("Hole is null!!!");
		System.out.println("Hole is null!!!");
		System.out.println(org_holes);
		System.exit(1);
	    }
	}
	return new_holes;
    }


    /** Create a new <code>MethodHole</code> history relation from an
     * old one and a <code>MethodHole</code> conversion map.
     */
    public static Relation DuplicateHistory(Relation org_hist, Map conversion)
    {
	LightRelation new_hist = new LightRelation();
	
	for(Iterator first_it=org_hist.keys().iterator(); first_it.hasNext(); ){
	    MethodHole org_first = (MethodHole) first_it.next();
	    MethodHole new_first = (MethodHole) conversion.get(org_first);
	    if (new_first==null){
		System.err.println("DuplicateHistory: No conversion for " 
				   + org_first + "\n in "
				   + conversion +
				   "\n for " + org_hist);
		System.out.println("DuplicateHistory: No conversion for " 
				   + org_first + "\n in "
				   + conversion +
				   "\n for " + org_hist);
		System.exit(1);
	    }

	    Set holes = new HashSet();
	    
	    for(Iterator second_it=org_hist.getValues(org_first).iterator(); second_it.hasNext(); ){

		MethodHole org_second = (MethodHole) second_it.next();
		MethodHole new_second = (MethodHole) conversion.get(org_second);
		if (new_second==null){
		    System.err.println("DuplicateHistory (2): No conversion for " 
				       + org_second + "\n in "
				       + conversion + 
				       "\n for " + org_hist);
		    System.out.println("DuplicateHistory (2): No conversion for " 
				       + org_second + "\n in "
				       + conversion + 
				       "\n for " + org_hist);
		    System.exit(1);
		}
		holes.add(new_second);
	    }
	    new_hist.addAll(new_first,holes);
	}
	return new_hist;
    }


    /** Create a new lock relation from an old one and a
     * <code>MethodHole</code> conversion map.
     */
    public static Relation DuplicateLocks(Relation org_locks, Map conversion)
    {
	LightRelation new_locks = new LightRelation();
	
	for(Iterator sync_it=org_locks.keys().iterator(); sync_it.hasNext(); ){
	    PASync sync = (PASync) sync_it.next();
	    Set new_holes = new HashSet();
	    for(Iterator mh_it=org_locks.getValues(sync).iterator(); mh_it.hasNext(); )
		{
		    MethodHole org_mh = (MethodHole) mh_it.next();
		    if (org_mh==null) {
			System.err.println("Null MethodHole in DuplicateLocks...");
			System.out.println("Null MethodHole in DuplicateLocks...");
			continue;
		    }
		    MethodHole new_mh = (MethodHole) conversion.get(org_mh);
		    if (new_mh==null){
			System.err.println("DuplicateLocks: No conversion for " 
					   + org_mh + " in "
					   + conversion);
			System.out.println("DuplicateLocks: No conversion for " 
					   + org_mh + " in "
					   + conversion);
		    }
		    else{
			new_holes.add(new_mh);
		    }
		}
	    new_locks.addAll(sync,new_holes);
	}
	return new_locks;
    }

    /** Check whether the set of <code>MethodHole</code> contains a
     * hole with the same content than the calling hole and, if this
     * is the case, return such a copy.
     */
    public MethodHole IsInAs(Set holeset)
    {
	for(Iterator mh_it=holeset.iterator(); mh_it.hasNext(); ){
	    MethodHole candidate = (MethodHole) mh_it.next();
	    if (strongEquals(candidate)) return candidate;
	}
	return null;
    }

    /** Check whether the array of <code>MethodHole</code> contains a
     * hole with the same content than the calling hole. In this case,
     * the method returns the index of such a copy.
     */
    public int IsInAs(MethodHole[] holeArray, int size)
    {
	for(int i=0; i<size; i++)
	    if (strongEquals(holeArray[i])) return i;
	return -1;
    }


    /** Check the equality of the content of two MethodHole, field by
     * field. 
     */
    public boolean strongEquals(MethodHole mh)
    {
	if (rank() !=mh.rank()){
	    return false;
	}
	if (depth() !=mh.depth()){
	    return false;
	}
	if (callsite()!=mh.callsite()){
	    return false;
	}
	if (!(callsitehistory().equals(mh.callsitehistory()))){
	    return false;
	}

	if (callees()!=mh.callees()){
	    return false;
	}
	if (ret() !=mh.ret()){
	    return false;
	}
	if (exc() !=mh.exc()){
	    return false;
	}
	if (ungroupedparameters().length !=mh.ungroupedparameters().length){
	    return false;
	}
	Set [] params_this = ungroupedparameters();
	Set [] params_that = mh.ungroupedparameters();
	for(int i=0; i<ungroupedparameters().length; i++){
	    if (!(params_this[i].containsAll(params_that[i]))){
		return false;
	    }
	    if (!(params_that[i].containsAll(params_this[i]))){
		 return false;
	    }
	}
	
	return true;
    }
}

// AppelRegAlloc.java, created Mon Feb  5 13:44:00 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Instr.AppelRegAllocClasses.Web;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrGroup;
import harpoon.Backend.Generic.Code;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.Temp.Temp;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.Maps.Derivation;

import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.DataFlow.SpaceHeavyLiveTemps;
import harpoon.Analysis.DataFlow.Solver;

import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.Loops.LoopFinder;

import harpoon.Util.Util;
import harpoon.Util.Default;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.FilterIterator;
import harpoon.Util.ReverseIterator;
import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.Factories;

import java.util.Map;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>AppelRegAlloc</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: AppelRegAlloc.java,v 1.1.2.12 2001-07-07 19:12:20 pnkfelix Exp $
 */
public class AppelRegAlloc extends /*RegAlloc*/AppelRegAllocClasses {
    // FSK: super class really SHOULD be RegAlloc, but am doing this
    // for now to inherit fields from ARAClasses (refactoring)
    
    public static final boolean PRINT_DEPTH_TO_SPILL_INFO = true;
    public static final boolean PRINT_HEURISTIC_INFO = false;
    public static final boolean PRINT_CLEANING_INFO = false;


    private static final int NUM_CLEANINGS_TO_TRY = 2;
    private boolean try_to_clean = true; 
    // activates an extension to cleaning that doesn't spill defs that
    // are dead at the BasicBlock's end.
    private static final boolean CLEAN_BB_LOCAL_DEFS = false; // trackdown _213_javac

    // activates a prepass removing "mov t0, t0" InstrMOVEs.
    private static final boolean TRIVIAL_MOVE_COALESCE = true;
    static RegAlloc.Factory FACTORY = new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new AppelRegAlloc(c);
	    }
	};


    // FSK todo: shouldn't use instanceof here, because a prepass
    // could have inserted spill code ahead of time and we want to
    // preserve those spills.  Instead, maintain a set of inserted
    // instructions locally.

    /** Removes all spill code, resets statistical info, and turns off cleaning. 
     */
    private void stopTryingToClean() {
	// Once rewrite crosses threshold, need to remove old spill
	// code so that the worst nodes will be respilled with
	// non-cleaned code.  This is a bit of a hack to get around
	// problems with keeping information alive across the
	// recreation of the interference graph with new nodes and
	// such.
	for(Iterator instrs=code.getElementsI(); instrs.hasNext();){
	    Instr i = (Instr) instrs.next();
	    if (i instanceof RestoreProxy ||
		i instanceof SpillProxy) {
		i.remove();
	    }
	}
	dontSpillTheseDefs.clear();
	depthToNumSpills = new int[SPILL_STAT_DEPTH];
	try_to_clean = false;
    }

    int K; // size of register set
    
    ReachingDefs rdefs;
    LiveTemps liveTemps;

    CFGrapher grapher;
    UseDefer  usedefer;

    static final int SPILL_STAT_DEPTH = 20;
    int[] depthToNumSpills;
    static int[] globalDepthToNumSpills = new int[SPILL_STAT_DEPTH];
    private String spillStats(int[] stats){
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<stats.length; i++) {
	    if (stats[i] != 0)
		sb.append
		    (",\tdepth:"+ i +
		     " => spills:"+ stats[i] );
	}
	return sb.toString();
    }
    

    public AppelRegAlloc(Code code) { 
	super(code); 
	K = rfi().getGeneralRegistersC().size();
	grapher= code.getInstrFactory().getGrapherFor ( InstrGroup.AGGREGATE );
	usedefer=code.getInstrFactory().getUseDeferFor( InstrGroup.AGGREGATE );
	// System.out.println("done constructing AppelRegAlloc");
	depthToNumSpills = new int[SPILL_STAT_DEPTH];
    }

    boolean intersects(Collection a, Collection b) {
	HashSet ns = new HashSet(a);
	ns.retainAll(b);
	return ! ns.isEmpty();
    }

    void buildTempToWebs() { 
	// translated from code in GraphColoringRegAlloc
	tempToWebs = new GenericMultiMap(Factories.arrayListFactory);

	//for(Iterator instrs=code.getElementsI(); instrs.hasNext();){
	for(Iterator instrs=reachableInstrs(); instrs.hasNext();){
	    Instr inst = (Instr) instrs.next();
	    for(Iterator uses = useCT(inst).iterator(); uses.hasNext();){
		Temp t = (Temp) uses.next();
		if (isRegister(t)) continue;
		Set defC = rdefs.reachingDefs(inst, t);
		Set useC = Collections.singleton(inst);
		addWeb( t, defC, useC );
	    }

	    // in practice, the remainder of the web building
	    // should work w/o the following loop.  But if there
	    // is a def w/o a corresponding use, the system breaks. 

	    for(Iterator defs=defCT(inst).iterator();defs.hasNext();){
		Temp t = (Temp) defs.next();
		
		if( isRegister(t) ) continue;
		
		if( haveWebFor(t, inst) ) {
		    Web w = webFor(t, inst);
		    w.defs.add(inst);
		} else {
		    Set defC = Collections.singleton(inst);
		    Set useC = Collections.EMPTY_SET;
		    addWeb( t, defC, useC );
		}
	    }
	}
	
	boolean changed;
	do {
	    // combine du-chains for the same symbol and that have a 
	    // def in common to make webs
	    changed = false;

	    // look into using a faster structure than a hashset here? 
	    HashSet tmp1 = new HashSet( tempToWebs.values() );
	    
	    while( ! tmp1.isEmpty() ){
		Web web1 = (Web) tmp1.iterator().next();
		tmp1.remove( web1 );

		// put webs to be removed post-iteration here
		HashSet removeFromTmp1 = new HashSet();
		
		for(Iterator t2s=tmp1.iterator(); t2s.hasNext(); ){
		    Web web2 = (Web) t2s.next();
		    
		    if( web1.temp.equals( web2.temp )){
			boolean combineWebs;
			combineWebs = intersects( web1.defs, web2.defs );

			if( ! combineWebs ){
			    // FSK IMPORTANT: current temp->reg assignment
			    // design breaks if an instr needs two
			    // different regs for the same temp in the
			    // uses and defines.  Take this clause out
			    // after that is fixed.
			    
			    combineWebs = ( intersects( web1.defs, web2.uses ) ||
					    intersects( web2.defs, web1.uses ) );
			}
			
			if( combineWebs ){
			    web1.defs.addAll( web2.defs );
			    web1.uses.addAll( web2.uses );
			    tempToWebs.remove( web2.temp, web2 );
			    removeFromTmp1.add( web2 );
			    changed = true;
			}
		    }
		}
		
		tmp1.removeAll( removeFromTmp1 );

	    }
	} while( changed );
    }

    void buildWebToNodes() { 
	webToNodes.clear();
	for(Iterator regs=rfi().getAllRegistersC().iterator();regs.hasNext();){
	    Temp reg = (Temp) regs.next();
	    makePrecolored(reg);
	}
	for(Iterator temps=tempToWebs.keySet().iterator(); temps.hasNext();){
	    Temp temp = (Temp) temps.next();
	    if( ! isRegister(temp) ) {
		makeInitial( temp );
	    }
	}
    }
    
    // wrapper for dealing with union'ing a large bitset with a small
    // appendage set.
    class ExtremityCollection extends AbstractCollection {
	ArrayList appendage;
	Collection body;
	// shares state with big, but not with small.  I bet remove()
	// is pretty sketch...
	ExtremityCollection(Collection big, Collection small) {
	    appendage = new ArrayList(small.size());
	    for(Iterator iter=small.iterator(); iter.hasNext();){
		Object s = iter.next();
		if (!big.contains(s)) {
		    appendage.add(s);
		}
	    }
	    body = big;
	}
	public Iterator iterator() { 
	    return new CombineIterator(body.iterator(), appendage.iterator());
	}
	public int size() { return body.size() + appendage.size(); }
    }
    
    Collection liveAt(Instr i) {
	// live-at(i) should be live-in(i) U defs(i), according to scott
	Collection liveTempC = liveTemps.getLiveBefore(i);
	return new ExtremityCollection( liveTempC, i.defC() );
    }

    void buildNodeToLiveAt() {
	nodeToLiveAt.clear();
	for(Iterator instrs = instrs(); instrs.hasNext(); ){
	    Instr i = (Instr) instrs.next();
	    Collection liveAt = liveAt(i);
	    for(Iterator temps = liveAt.iterator(); temps.hasNext();) {
		Temp t = (Temp) temps.next();
		Collection rdefC = rdefs.reachingDefs(i, t);
		Collection webC = tempToWebs.getValues( t );
		for(Iterator webs=webC.iterator(); webs.hasNext(); ){
		    Web w = (Web) webs.next();
		    boolean intersect = false;
		    for(Iterator rti = rdefC.iterator(); rti.hasNext(); ){
			Instr def = (Instr) rti.next();
			if(w.defs.contains( def )){
			    intersect = true;
			    break;
			}
		    }
		    if ( intersect ) {
			// w has a def that reaches i, thus w is live-at i
			Collection nodeC = (Collection) webToNodes.get(w);
			for(Iterator nodes=nodeC.iterator();nodes.hasNext();){
			    Node n = (Node) nodes.next();
			    nodeToLiveAt.add( n, i );
			}
		    }
		}
	    }
	}
    }

    public Derivation getDerivation() { return null; }

    // Invariants that hold post Build, see Appel pg 254
    public void checkInv() {
	if( ! CHECK_INV ) {
	    return; 
	}
	checkMoveSets();
	checkDisjointInv();
	checkDegreeInv();
	checkSimplifyWorklistInv();
	checkFreezeWorklistInv();
	checkSpillWorklistInv();

	for(NodeIter ni=precolored.iter(); ni.hasNext();){
	    Util.assert(ni.next().isPrecolored());
	}
    }

    public void checkDisjointInv() {
	HashSet pset = new HashSet();
	HashSet iset = new HashSet();
	HashSet simplset = new HashSet();
	HashSet fset = new HashSet();
	HashSet spillset = new HashSet();
	HashSet spilledset = new HashSet();
	HashSet coalset = new HashSet();
	HashSet coloredset = new HashSet();
	HashSet selectset = new HashSet();

	HashSet[] sets = new HashSet[] { 
	    pset, iset, simplset, fset, spillset, 
	    spilledset, coalset, coloredset, selectset
	};

	NodeIter ni;
	for(ni=precolored.iter(); ni.hasNext();) pset.add(ni.next());
	for(ni=initial.iter(); ni.hasNext();)    iset.add(ni.next());
	for(ni=simplify_worklist.iter();ni.hasNext();) simplset.add(ni.next());
	for(ni=freeze_worklist.iter();ni.hasNext();) fset.add(ni.next());
	for(ni=spill_worklist.iter();ni.hasNext();)  spillset.add(ni.next());
	for(ni=spilled_nodes.iter();ni.hasNext();) spilledset.add(ni.next());
	for(ni=coalesced_nodes.iter();ni.hasNext();) coalset.add(ni.next());
	for(ni=colored_nodes.iter();ni.hasNext();) coloredset.add(ni.next());
	for(ni=select_stack.iter(); ni.hasNext();) selectset.add(ni.next());
	
	for(int i=0; i<sets.length; i++) {
	    HashSet s = sets[i];
	    for(int j=i+1; j<sets.length; j++) {
		Util.assert( ! intersects( sets[j], s ));
	    }
	}
    }

    public void checkDegreeInv() {
	// u isIn (simplifyWorklist \/ freezeWorklist \/ spillWorklist ) ==>
	//   degree(u) = | adjList(u) /\ ( precolored \/ simplifyWorklist \/ 
	//                                 freezeWorklist \/ spillWorklist ) |
	// ( tranlates to )
	// foreach u in (simplify U freeze U spill )
	//    degree(u) = | {n | n isIn adjList(u) && 
	//                       n isIn precolored \/ simplify \/ 
	//                              freeze \/ spill } |

	HashSet outerSeen = new HashSet();

	NodeIter ni=combine(new NodeIter[]{simplify_worklist.iter(),
					   freeze_worklist.iter(),
					   spill_worklist.iter()});
	while( ni.hasNext() ) {
	    Node u = ni.next();
	    
	    if (CHECK_INV)
	    Util.assert( ! outerSeen.contains(u), " already saw " + u);

	    outerSeen.add(u);

	    int deg = u.degree;
	    for(NodeIter adj=adjacent(u); adj.hasNext();) {
		Node a = adj.next();

		if( a.isPrecolored() || 
		    a.isSimplifyWorkL() || 
		    a.isFreezeWorkL() ||
		    a.isSpillWorkL() ) {
		    deg--;
		}
	    }
	    Util.assert(deg == 0, "degree inv. violated");
	}
    }
    public void checkSimplifyWorklistInv() {
	// u isIn simplifyWorklist ==>
	//   degree(u) < K && 
	//   moveList[u] /\ ( activeMoves \/ worklistMoves ) = {} 
	
	for( NodeIter ni=simplify_worklist.iter(); ni.hasNext(); ) {
	    Node u = ni.next();
	    Util.assert(u.degree < K, 
			"simplify worklist inv. violated "+
			"( should be u.degree < K )" );
	    for( Iterator mi = u.moves.iterator(); mi.hasNext(); ) {
		Move m = (Move) mi.next();
		Util.assert( !m.isActive(), 
			     "simplify worklist inv. violated"+
			     "( should be !m.isActive() )");
		Util.assert( !m.isWorklist(), 
			     "simplify worklist inv. violated"+
			     "( should be !m.isWorklist() )");
	    }
	}
    }

    public void checkFreezeWorklistInv() {
	// u isIn freezeWorklist ==>
	//   degree(u) < K && 
	//   moveList[u] /\ ( activeMoves \/ worklistMoves ) != {}
	
	for( NodeIter ni=freeze_worklist.iter(); ni.hasNext(); ) {
	    Node u = ni.next();
	    Util.assert(u.degree < K, "freeze worklist inv. violated" );
	    
	    for( Iterator mi = u.moves.iterator(); mi.hasNext(); ) {
		Move m = (Move) mi.next();

		if( m.isActive() || m.isWorklist() ) 
		    return;
	    }

	    Util.assert( false, "freeze worklist inv. violated, "+
			 " node:"+u+
			 " moves:"+u.moves);
	}
    }

    public void checkSpillWorklistInv() {
	// u isIn spillWorklist ==> degree(u) >= K
	for( NodeIter ni=spill_worklist.iter(); ni.hasNext(); ) {
	    Node u = ni.next();
	    Util.assert( u . degree >= K , "spill worklist inv. violated");
	}
    }
    
    public void appelLoopBody( SpillHeuristic sh ) throws CouldntFindSpillExn {
	
	// System.out.println("making worklist");
	makeWorklist();
	// System.out.println("done making worklist");
	
	checkInv();
	do {
	    if( ! simplify_worklist.isEmpty()) { 
		simplify(); checkInv();
	    } else if( ! worklist_moves.isEmpty()) {
		coalesce(); checkInv();
	    } else if( ! freeze_worklist.isEmpty()) {
		freeze(); checkInv();
	    } else if( ! spill_worklist.isEmpty()) {
		selectSpill( sh ); 
		
		// FSK: (3/26/01) invariant DOESN'T HOLD here (this
		// FSK: agrees with assertion commented out from 
		// FSK: CSAHack.RegAlloc.Color
		// checkInv(); 
	    }
	    
	} while ( ! simplify_worklist.isEmpty() ||
		  ! worklist_moves.isEmpty() ||
		  ! freeze_worklist.isEmpty() ||
		  ! spill_worklist.isEmpty() );
	
    }
    
    private void trivialMoveCoalesce() {
	// FSK: TODO: insert code here to do a prepass eliminating
	// moves of the form "mov t0, t0;" because they are truly
	// useless (unless we add support for assigning different regs
	// to the same temp based on its status in its instr as a use
	// or a def)
	if (!TRIVIAL_MOVE_COALESCE) 
	    return;
	
	int numRemoved = 0;
	for(Iterator instrs=code.getElementsI(); instrs.hasNext(); ){
	    Instr i = (Instr) instrs.next();
	    if (i.isMove()) {
		Util.assert( i.defC().size() == 1);
		Util.assert( i.useC().size() == 1); 
		Temp dst = (Temp) i.defC().iterator().next(); 
		Temp src = (Temp) i.useC().iterator().next(); 
		if (dst == src) {
		    i.remove();
		    numRemoved++;
		}
	    }
	}
	
	if (numRemoved > 0) {
	    bbFact = computeBasicBlocks();
	}
    }
    
    void time(String note) { //System.out.println("TIME "+note+" "+new java.util.Date()); 
    }

    public void generateRegAssignment() { 
	time("A");
	trivialMoveCoalesce();

	while (true) {
	    // System.out.println("post spill coloring");
	    time("B");	
	    initializeSets();     	    time("C");	
	    preAnalysis();             	    time("D");	
	    buildTempToWebs();         	    time("E");	
	    buildWebToNodes();         	    time("F");	
	    
	    // Appel's real code begins here
	    buildInterferenceGraph();     	    time("G");	
	    
	    checkInv();     	    time("H");	
	    checkpointState();     	    time("I");	
	    final boolean USE_CHAITIN_ALONE = false;
	    final boolean INCREMENTAL_OUTPUT = false;
	    try {
		SpillHeuristic h_min = null;
		SpillHeuristic[] h = spillHeuristics();     	    time("J");	
		if ( ! USE_CHAITIN_ALONE ) {
		    for(int i = 0; i < h.length; i++) {
			// FSK: look into breaking out of this loop if
			// we color without any spilling at all.
			appelLoopBody( h[i] );     	    time("K");	
			
			checkMoveSets();     	    time("L");	
			
			assignColors();     	    time("M");	
			
			checkInv();     	    time("N");	
			
			if (h[i].maxExpSpills == 0)
			    Util.assert( spilled_nodes.isEmpty() );
			
			if ( spilled_nodes.isEmpty() ) {
			    h_min = h[i];
			    
			    // FSK: break here later for better speed
			    // (no spills ==> don't need reiteration of heuristics) 
			    // ((may want disable later if i gather
			    //   data measuring benefit of alt spill
			    //   heuristics))
			    break;
			} else {
			    h[i].reallySpill(spilled_nodes.iter());
			}
			
			resetState();     	    time("O");	
			
			if (INCREMENTAL_OUTPUT && PRINT_HEURISTIC_INFO ) 
			    System.out.print
				    ("\nAPPLY SPILL HEURISTIC "+i+" \t=> "+h[i]);
		    }
		    
		    
		    h_min = h[0];
		    int minIndex = -1; // negative represents "any will do"
		    
		    
		    for(int i=1; i < h.length; i++) {
			// if (h[i].accumExpCost < h_min.accumExpCost) {
			if (h[i].actualCost < h_min.actualCost) {
			    h_min = h[i];
			    minIndex = i;
			} else if (minIndex == -1 && 
				   h_min.actualCost < h[i].actualCost) {
			    // need this special case so that our data
			    // properly states when using h[0] *is*
			    // significant.
			    minIndex = 0;
			}
		    }
		    if (minIndex == -1 && (h_min.actualCost < 0.001)) {
			minIndex = -2; // -2 means "any will do && no spilling"
		    } 

		    time("P");	
		    
		    if( PRINT_HEURISTIC_INFO 
			
			// (leave -2 results out of output when not incremental)
			&& (INCREMENTAL_OUTPUT || minIndex != -2) 
			
			) {

			if(!INCREMENTAL_OUTPUT)
			    for(int i=0;i<h.length;i++)
				System.out.print
				    ("\nAPPLY SPILL HEURISTIC "+i+" \t=> "+h[i]);
			
			
			System.out.println
			    ("\nCHOOSING SPILL HEURISTIC "+minIndex+" \t=> "+h_min);
		    }
		    h_min.reset();
		    appelLoopBody( h_min );		    time("Q");	
		    
		} else {
		    appelLoopBody( h[2] );
		}
				    time("R");	
		assignColors(); 		    time("S");	
		// h_min.reallySpill(spilled_nodes.iter());
		// System.out.println("\nFINAL SPILL HEURISTIC   \t=> "+h_min);
		
		checkInv();		    time("T");	
		
		if( ! spilled_nodes.isEmpty()) {
		    System.out.print(" R"+rewriteCalledNumTimes+", S!"+spilled_nodes.asSet().size());
		    rewriteProgram();  checkInv();
		    continue;
		} else {
		    break;
		}
	    } catch (CouldntFindSpillExn e) {
		if (PRINT_CLEANING_INFO)
		    System.out.println
			("COULDN'T FIND A SPILL!  TURNING OFF CLEANING!");
		stopTryingToClean();
		bbFact = computeBasicBlocks();
	    }
	}
	
	// introduce Flex-style assignments here

	// set up color (int) -> register mapping
	Temp[] regs = new Temp[ precolored.size ];
	for(NodeIter ri=precolored.iter(); ri.hasNext();) {
	    Node r = ri.next();
	    regs[r.color] = r.web.temp;
	}
	

	// look into simplifying this interface extremely (and
	// allowing for outside cloning of the code?) by just passing
	// a (Temp,Instr)->List<Temp> mapping into 'code'

	for(Iterator instrs=code.getElementsI(); instrs.hasNext();){
	    Instr inst = (Instr) instrs.next();


	    // FSK: we're looking at the real deal here, not the
	    // abstract InstrGroupings
	    Iterator refs = new CombineIterator
		// ( useCT(inst).iterator(), defCT(inst).iterator() );
		( inst.useC().iterator(), inst.defC().iterator() );


	    while( refs.hasNext() ){
		Temp t = (Temp) refs.next();
		if (isRegister(t)) continue;
		if (null == bbFact.getBlock
		    (inst.getEntry( InstrGroup.AGGREGATE ))){
		    System.err.println
			("WARNING: code believed to be unreachable emitted");
		    code.assignRegister
			(inst,t,(List)rfi().getRegAssignments(t).iterator().next());
		} else {
		    Web w = webFor(t, inst);
		    List nodes = nodesFor( w );
		    List regList = toRegList( nodes, regs );
		    Util.assert( ! regList.isEmpty() );
		    code.assignRegister( inst, t, regList );
		}
	    }
	}

	
	// debugging output
	if (false) {
	    liveTemps.dumpElems(); System.out.println();
	    bbFact.dumpCFG();      System.out.println();
	    code.printPreallocatedCode();
	}

	fixupSpillCode();

	// copy spill stats
	String local_stats = spillStats( depthToNumSpills );
	if (local_stats.length() > 0) {
	    for(int i=0; i < depthToNumSpills.length; i++) {
		globalDepthToNumSpills[i] += depthToNumSpills[i];
	    }
	    if (PRINT_DEPTH_TO_SPILL_INFO) {
		String global_stats = spillStats(globalDepthToNumSpills);
		System.out.println();
		System.out.print("globally "+global_stats);
		System.out.println();
	    }
	}
    }

    List toRegList(List nodes, Temp[] colorToReg) {
	Temp[] regs = new Temp[nodes.size()];
	for(int i=0; i<regs.length; i++) {
	    Node n = (Node)nodes.get(i);
	    if (! (0 <= n.color && n.color < colorToReg.length) ) {
		code.printPreallocatedCode();
		printAllColors();
		System.out.println();
		System.out.println("node:"+n+" history:"+n.nodeSet_history);
		Util.assert(false,
			    "node:"+n+" should have valid color, not:"+n.color);
	    }
	    regs[i] = colorToReg[ n.color ];
	}
	return Arrays.asList( regs );
    }
    private void printAllColors() {
	for(Iterator wIter = webToNodes.keySet().iterator(); wIter.hasNext(); ){
	    Web w = (Web) wIter.next();
	    String accum = 
		"Temp:"+w.temp+
		" defs:"+instrIDs(w.defs)+
		" uses:"+instrIDs(w.uses)+
		" colors:[";
	    for(Iterator nI=((List)webToNodes.get(w)).iterator();nI.hasNext();) { 
		Node node = (Node) nI.next();
		accum += node.color;
		if (nI.hasNext()) {
		    accum += ", ";
		}
	    }
	    accum += "]";
	    System.out.println(accum);
	}
    }
    private Collection instrIDs(final Collection instrs) {
	return new AbstractCollection() { 
		public int size() { return instrs.size(); }
		public Iterator iterator() 
		{ return new FilterIterator
		    (instrs.iterator(), new FilterIterator.Filter() 
			{ public Object map(Object o)
			    { return new Integer(((Instr)o).getID()); }}); }};
    }
    
    private void preAnalysis() { 
	rdefs = new ReachingDefsAltImpl(code,grapher,usedefer);

	// TODO: update to use grapher/usedefer 
	// (requires revision of LiveTemps code)
	// liveTemps = new SpaceHeavyLiveTemps(bbFact, rfi().liveOnExit(), usedefer, grapher);
	liveTemps = new LiveTemps(bbFact, rfi().liveOnExit(), usedefer);
	liveTemps.solve();

	if (CHECK_INV) 
	    Check.liveSetsAreConsistent
		( code, bbFact, grapher, usedefer, liveTemps, rfi().liveOnExit() );
	if (CHECK_INV) // TODO need to fix that r0 def at outset thing
	    Check.allLiveVarsHaveDefs
		( code, bbFact, grapher, usedefer, rdefs, liveTemps );

	if (false) System.out.println("RegAlloc Analysis for "+
				      code.getMethod().getName()+
				      " completed successfully ");

	
	resetNestedLoopDepth();


    } 

    int depth(Instr i){ return((Integer)nestedLoopDepth().get(i)).intValue(); }
    int width(Instr i){ return liveAt(i).size(); }


    /** Instr -> Integer, val is loop nesting depth for the key (0 for
	non-looped code).  Constructed on demand; access through method
	call only. */
    Map nestedLoopDepth() {
	if (__nestedLoopDepth == null)
	    __nestedLoopDepth = buildNestedLoopDepth();
	return __nestedLoopDepth;
    }
    /** Marks nestedLoopDepth() for reconstruction. */
    void resetNestedLoopDepth() { __nestedLoopDepth = null; }

    private Map __nestedLoopDepth;
    private Map buildNestedLoopDepth() { 
	// builds the nestedLoopDepth map, doing a breadth-first
	// traversal of the loop tree.
	HashMap depthMap = new HashMap();
	LoopFinder root = new LoopFinder(code);
	
	// List<Loops> where index of vector is the looping depth
	// (thus the 0th elem is the root itself)
	ArrayList level = new ArrayList();
	level.add(root);
	int depth = 0; // tracks the current level in the tree
	while( ! level.isEmpty()) {
	    Iterator levelIter = level.iterator();
	    level = new ArrayList();
	    Integer currDepth = new Integer( depth );
	    while( levelIter.hasNext() ){
		Loops curr = (Loops) levelIter.next();
		level.addAll( curr.nestedLoops() );
		
		Iterator instrs=curr.loopExcElements().iterator(); 
		while( instrs.hasNext() ){
		    Instr i = (Instr) instrs.next();
		    Util.assert( ! depthMap.keySet().contains( i ));
		    depthMap.put( i , currDepth );
		}
	    }
	    depth++;
	}

	// consistency check: make sure that every instr is mapped to
	// some loop depth.
	if (CHECK_INV) 
	    for(Iterator iter = code.getElementsI(); iter.hasNext(); ) {
		Instr i = (Instr) iter.next();
		Util.assert( bbFact.getBlock(i) == null ||
			     depthMap.keySet().contains(i), 
			     "reachable instrs should have loop depth"); 
	    }

	return depthMap;
    }
    
    public void buildInterferenceGraph() {  
	// forall b : blocks in program
	//    let live = liveOut(b)
	//    forall I : instructions(b) in reverse order
	//       if isMoveInstruction(I) then
	//          live <- live \ use(I)
	//          forall n : def(I) \/ use(I)
	//             moveList[n] <- moveList[n] \/ I
	//          worklistMoves <- worklistMoves \/ { I }
	//       live <- live \/ def(I)
	//       forall d : def(I)
	//          forall l : live
	//             AddEdge(l, d)
	//       live <- use(I) \/ ( live \ def(I) )

	for(Iterator bbs=bbFact.blocksIterator(); bbs.hasNext();) {
	    BasicBlock bb = (BasicBlock) bbs.next();
	    Set/*Node*/ live = liveOut(bb);
	    
	    for(Instr i = lastStm(bb); !i.equals( firstStm(bb) ); i = pred(i)){
		if( i.isMove() ){
		    live.removeAll( useCN( i ) );
		    for( NodeIter ni = usedef(i); ni.hasNext(); ){
			Node n = ni.next();
			n.moves.add( moveFor(i) );
		    }
		    worklist_moves.add( moveFor(i) );
		}
		
		live.addAll( defCN(i) );
		for( NodeIter di = def(i); di.hasNext(); ){
		    Node d = di.next();
		    for( Iterator li=live.iterator(); li.hasNext(); ){
			Node n = (Node) li.next();
			addEdge( n, d );
		    }
		}
		live.removeAll( defCN(i) );
		live.   addAll( useCN(i) );
	    }
	    
	}

    }

    // RETURN a Set of Node or a NodeSet ???
    private Set liveOut(BasicBlock b) {
	Instr last = lastStm( b );
	Set s = liveTemps.getLiveAfter( last );

	// TODO: check that 'last' is the right thing to pass in here... 
	return tempCtoNodes( s, last );
    }
    private Instr lastStm(BasicBlock b) {
	List stms = b.statements();
	return (Instr) stms.get( stms.size() - 1 );
    }
    private Instr firstStm(BasicBlock b) { 
	return (Instr) b.statements().get(0); 
    }
    private Instr pred(Instr i) {
	Instr i_r = (Instr) grapher.predElemC(i).iterator().next();
	Util.assert(i != i_r);
	return i_r;
    }

    private Collection/*Node*/ useCN(Instr i) { 
	Collection nodeC = tempCtoNodes( useCT( i ), i );
	return nodeC;
    }
    
    private Collection/*Node*/ defCN(Instr i) { 
	Collection nodeC = tempCtoNodes( defCT( i ), i);
	return nodeC;
    }

    private Set/*Node*/ tempCtoNodes( Collection temps, Instr i ) {
	HashSet set = new HashSet();
	for(Iterator ts=temps.iterator(); ts.hasNext(); ) {
	    Temp t = (Temp) ts.next();
	    Web web = webFor( t, i );
	    set.addAll( nodesFor( web ));
	}
	return set; 
    }


    private NodeIter usedef(Instr i) { 
	HashSet s = new HashSet();
	s.addAll( useCN( i ));
	s.addAll( defCN( i ));
	return nodesIter( s.iterator() );
    }

    private NodeIter def(Instr i) { 
	return nodesIter( defCN( i ).iterator() );
    }
    private Collection useCT( Instr i ) { return usedefer.useC( i ); }
    private Collection defCT( Instr i ) { return usedefer.defC( i ); }

    private NodeIter nodesIter(final Iterator i) {
	return new NodeIter() {
		public boolean hasNext() { return i.hasNext(); }
		public Node next() { return (Node) i.next(); }
	    };
    }

    private Iterator instrs() {
	final Iterator bbs = bbFact.blocksIterator();
	return new UnmodifiableIterator() {
		Iterator currStms;
		public boolean hasNext() {
		    return bbs.hasNext() || currStms.hasNext();
		}
		public Object next() {
		    if (currStms != null && currStms.hasNext()) {
			return currStms.next();
		    } else if (bbs.hasNext()) {
			currStms = ((BasicBlock)bbs.next()).statements().iterator();
			return currStms.next();
		    } else {
			throw new java.util.NoSuchElementException();
		    }
		}
	    };
    }

    private void addEdge( Node u, Node v ) {
	// FSK: (6/27/01) adding because having these edges is dumb.  
	// Shouldn't break anything.  And yet...
	if (u.isPrecolored() && v.isPrecolored()) {
	    return;
	}

	if( ! adjSet.contains(u,v) && ! u.equals(v) ){
	    adjSet.add(u,v);
	    adjSet.add(v,u);
	    
	    if( ! u.isPrecolored() ){
		u.neighbors.add(v);
		u.degree++;
	    }
	    if( ! v.isPrecolored() ){
		v.neighbors.add(u);
		v.degree++;
	    }
	}
    }


    /** Moves Nodes from initial to appropriate set in 
	{ spill, freeze, simplify }_worklist.  
	MODIFIES: initial, { spill, freeze, simplify }_worklist
    */
    public void makeWorklist() { 
	while( ! initial.isEmpty()) {
	    Node n = initial.pop();
	    if( n.degree >= K) {
		spill_worklist.add(n);
	    } else if( moveRelated(n)) {
		freeze_worklist.add(n);
	    } else {
		simplify_worklist.add(n);
	    }
	}

	// at this point, all Nodes should be in appropriate starting
	// sets.  Do a consistency check...
	if (CHECK_INV) 
        for( Iterator moves = worklist_moves.iter(); moves.hasNext(); ){
	    Move m = (Move) moves.next();
	    Util.assert( m.isWorklist(), "m should be in worklist_moves" );
	    for(Iterator dI=m.dsts().iterator(); dI.hasNext(); ) {
		Node n = (Node) dI.next();
		if (CHECK_INV)
		Util.assert( moveRelated( n ), 
			     "m.dst should be moveRelated, not "+n.s_rep.name );
	    }
	    for(Iterator uI=m.srcs().iterator(); uI.hasNext(); ) {
		Node n = (Node) uI.next();
		if (CHECK_INV)
		Util.assert( moveRelated( n ), 
			     "m.src should be moveRelated, not "+n.s_rep.name);
	    }
	}
    }
    Iterator nodeMoves( Node u ){
	return activeOrWorklistMoves( u );
    }
    Iterator activeOrWorklistMoves( Node u ) {
	return new FilterIterator
	    (u.moves.iterator(),
	     new FilterIterator.Filter() {
		     public boolean isElement(Object o) {
			 Move m = (Move) o;
			 return (m.isActive() || m.isWorklist());
		     }
		 }
	     );
    }
    public boolean moveRelated( Node n ) {
	Iterator nms = nodeMoves( n );
	return nms.hasNext();
    }
    public void simplify() { 
	Node n = simplify_worklist.pop();
	select_stack.add(n);

	for( NodeIter adj=adjacent(n); adj.hasNext(); ){
	    Node m = adj.next();
	    
	    if (m.isPrecolored()) {
		// FSK: added 3/26/01 based on CSA's code in
		// FSK: CSAHack.RegAlloc.Color (though algorithm given
		// FSK: in Appel does not have this check!)
		continue;
	    }

	    decrementDegree(m);
	}
    }

    /** modifies: m, active_moves, worklist_moves, spill_worklist, 
	          freeze_worklist, simplify_worklist
        effects: m in Precolored ==> no changes
	         else decrements degree of m, moving it (and associated moves)  
                 into the appropriate lists if it has crossed the threshold 
		 from K to K-1.
    */
    private void decrementDegree( Node m ) {
	if (m.isPrecolored())
	    return;

	int d = m.degree;
	m.degree--;
	if( d == K 
	    
// FSK: this clause isn't in the book, but it *is* in 
// FSK: CSAHack.RegAlloc.Color.  Appel assumes that m is 
// FSK: in spillWorklist, but its possible for a node to 
// FSK: be in Freeze (and Simplify?), have its degree 
// FSK: incremented to K in Combine(u,v), and then this 
// FSK: method is called.  So we explicitly check if m is 
// FSK: in spillWorklist first.
// TODO: verify that this is the right behavior (intuitively, it should be...)
	    && m.isSpillWorkL() 
	    

	    ) {
	    
	    enableMoves(m);
	    enableMoves( adjacent(m) );
	    
	    spill_worklist.remove(m);
	    if( moveRelated( m )) {
		freeze_worklist.add( m );
	    } else {
		simplify_worklist.add( m );
	    }
	}
    }

    private void enableMoves( NodeIter nodes ) {
	while( nodes.hasNext() )
	    enableMoves( nodes.next() );
    }
    private void enableMoves( Node node) {
	for( Iterator moves=node.moves.iterator(); moves.hasNext(); ) {
	    Move m = (Move) moves.next();
	    if( m.isActive() ) {
		active_moves.remove(m);
		worklist_moves.add(m);
	    }
	}
    }

    public void coalesce() { 
	Move m = worklist_moves.pop();

	Iterator xs = m.dsts().iterator();
	Iterator ys = m.srcs().iterator();
	while(xs.hasNext()) {
	    Node x = (Node) xs.next();
	    Node y = (Node) ys.next();
	    coalesce(m, x, y);
	}
    }
    // helper function, adapted from Appel's original Coalesce code
    // (am just applying it to all nodes on each side of the move) 

    public void coalesce(Move m, Node x, Node y) {
	// System.out.println("called coalesce("+m+","+x+","+y+")");

	x = getAlias(x);
	y = getAlias(y);
	
	Node u, v;
	if( y.isPrecolored() ){
	    u = y; v = x;
	} else {
	    u = x; v = y;
	}
	
	// FSK: see coalesce() above [ pop() implicitly removes m ]
	// worklist_moves.remove(m); 

	// TODO: NEED TO THINK ABOUT WHICH SET THE MOVE WILL GO INTO!!! 
	// ( possibly adding new cases here?!!? )

	if (u.equals(v)) {

	    coalesced_moves.add(m);
	    addWorkList(u);
	
	} else if (v.isPrecolored() || adjSet.contains(u,v) ) {
	    // slightly obfuscated way of asking "do u and v conflict?"

	    constrained_moves.add(m);
	    addWorkList(u);
	    addWorkList(v);

	} else if((   u.isPrecolored() && 
		      forall_t_in_adj_of_v__OK_t_u(v, u))
		  ||
		  ( ! u.isPrecolored() && 
		    conservative(adjacent(u), adjacent(v))) ) {
	    // System.out.println( "ugly case, u:"+u+" v:"+v );

	    coalesced_moves.add(m);

	    combine(u, v);

	    addWorkList(u);

	} else {
	    // System.out.println( "'else' case "+m );

	    active_moves.add(m);
	}
    }
    private boolean forall_t_in_adj_of_v__OK_t_u(Node v, Node u) {
	for( NodeIter ni=adjacent(v); ni.hasNext(); ){
	    Node t = ni.next();
	    if( ! OK(t, u) ) {
		return false;
	    }
	}
	return true;
    }

    private void addWorkList(Node u) {
	if( ! u.isPrecolored() && 
	    ! moveRelated( u ) &&
	    u.degree < K ) {

	    freeze_worklist.remove( u );

	    simplify_worklist.add( u );
	}
    }

    
    private boolean OK(Node t, Node r) { 
	return t.degree < K 
	    || t.isPrecolored() 
	    || adjSet.contains(t, r);
    }

    // returns conservative( ni1 \/ ni2 )
    // conservative coalescing heuristic due to Preston Briggs
    private boolean conservative(NodeIter ni1, NodeIter ni2) {
	NodeIter union; 
	// [ FIXED, but am seeing signs that "conservation" is being broken.
	//   could just be inherent heuristical effects though ]
	// FSK combine(...) is not a strict union.
	// union = combine( new NodeIter[]{ ni1, ni2 } );
	union = union(ni1, ni2);
	
	return conservative( union );
    }
    private NodeIter union(NodeIter n1, NodeIter n2) {
	HashSet s = new HashSet();
	while(n1.hasNext()){ s.add(n1.next()); }
	while(n2.hasNext()){ s.add(n2.next()); }
	return nodesIter( s.iterator() );
    }

    private boolean conservative(NodeIter nodes) { 
	int k = 0;
	while( nodes.hasNext() ){
	    Node n = nodes.next();
	    if (n.degree >= K)
		k++;
	}
	return k < K;
    }

    private Node getAlias(Node n) { // FSK: umm... bound on this runtime? 
	while( n.isCoalesced() ) {
	    n = n.alias;
	}
	return n;
    }
    
    /**
       modifies; freeze_worklist, spill_worklist, 
     */
    private void combine(Node u, Node v) {
	if (v.isFreezeWorkL()) {
	    freeze_worklist.remove(v);
	} else {
	    spill_worklist.remove(v);
	}
	coalesced_nodes.add(v);
	v.alias = u;


	enableMoves(v); // does this belong here or after the succeeding line? 
	u.moves.append(v.moves);
	
	for( NodeIter adj=adjacent(v); adj.hasNext(); ) {
	    Node t = adj.next();
	    addEdge(t,u);
	    
	    if (false) { // tracks a break in Appel's defined invariant
		if (t.degree == K && !t.isSpillWorkL())
		    System.out.println(t+" degree inc'd to K, but not in spills");
	    }

	    decrementDegree(t);
	}
	
	if( u.degree >= K && u.isFreezeWorkL() ) {
	    freeze_worklist.remove(u);
	    spill_worklist.add(u);
	}
    }
    
    public void freeze() { 
	Node u = freeze_worklist.pop();
	// System.out.println("FREEZING :"+u);

	simplify_worklist.add(u);
	checkMoveSets();
	freezeMoves(u);
	checkMoveSets();
    }


    private void freezeMoves(Node u) {
	for( Iterator moves= nodeMoves(u); moves.hasNext(); ) {
	    Move m = (Move) moves.next();
	    Node x = m.dst, y = m.src;

	    Node v; 
	    if( getAlias(y).equals(getAlias(u)) ) {
		v = getAlias(x);
	    } else {
		v = getAlias(y);
	    }

	    checkMoveSets();
	    active_moves.remove(m);
	    frozen_moves.add(m);
	    checkMoveSets();

	    if ( ( ! nodeMoves(v).hasNext()) && v.degree < K ) {
		freeze_worklist.remove(v);
		simplify_worklist.add(v);
	    }
	}
    }

    // See "Spill code minimization techniques for optimizing
    // compilers", Bernstein et. al
    private abstract class SpillHeuristic {
	public String toString() { 
	    return "SpillHeuristic<"
		+"accumExpCost:"+accumExpCost 
		+" maxExpSpills:"+maxExpSpills
		+" actualCost:"+actualCost
		+" actualSpills:"+actualSpills
		+">";
	}

	double accumExpCost = 0.0;
	int maxExpSpills = 0;

	double actualCost = 0.0;
	int actualSpills = 0;

	HashMap instrToAreaCache = new HashMap();
	HashMap nodeToAreaCache = new HashMap();    

	private void reset() { 
	    accumExpCost = 0.0; 
	    maxExpSpills = 0;
	    actualCost = 0.0; 
	    actualSpills = 0; 
	    instrToAreaCache.clear();
	    nodeToAreaCache.clear();
	}
	void expectSpill( Node m ) { 
	    // IMPORTANT: don't confuse "accumExpCost" here (which is called
	    // "h_i" in the paper) with "cost" in the paper (which is
	    // called chaitinCost here)
	    accumExpCost += chaitinCost( m ); 
	    maxExpSpills++;
	}

	
	/** called when spill code is added for n . */
	void reallySpill( NodeIter ni ){
	    while(ni.hasNext()) 
		reallySpill(ni.next());
	}
	void reallySpill( Node n ){
	    actualCost += chaitinCost(n);
	    actualSpills++;
	}

	abstract double cost( Node m );
	
	double chaitinCost( Node m ) {
	    double sum = 0.0;
	    for(Iterator ds = m.web.defs.iterator(); ds.hasNext(); ){
		Instr i = (Instr) ds.next();
		sum += Math.pow( 10.0, depth(i));
	    }
	    for(Iterator us = m.web.uses.iterator(); us.hasNext(); ){
		Instr i = (Instr) us.next();
		sum += Math.pow( 10.0, depth(i));
	    }
	    return sum;
	}
	double area( Node m ) {
	    if (nodeToLiveAt == null) {
		nodeToLiveAt = new GenericMultiMap();
		buildNodeToLiveAt();
	    }

	    if (nodeToAreaCache.containsKey(m)) {
		return ((Double)nodeToAreaCache.get(m)).doubleValue();
	    } else {
		double sum = 0.0;
		Collection instrC = nodeToLiveAt.getValues( m );
		for(Iterator instrs = instrC.iterator(); instrs.hasNext(); ){
		    Instr i = (Instr) instrs.next();
		    
		    if (instrToAreaCache.containsKey(i)) {
			sum += ((Double)instrToAreaCache.get(i)).doubleValue();
		    } else {
			double val = (Math.pow(5.0, depth(i)) * width(i));
			sum += val;
			instrToAreaCache.put(i, new Double(val) );
		    }
		}
		nodeToAreaCache.put(m, new Double(sum));
		return sum;
	    }
	}

    }


    private SpillHeuristic[] spillHeuristics() {
	SpillHeuristic[] hs = new SpillHeuristic[] { 

	    // ** SCOTT'S SPILL HEURISTIC
	    new SpillHeuristic() { double cost(Node m) {
		return (1000*(m.web.defs.size()+m.web.uses.size() ) ) / m.degree;  }}
	    ,

	    // ** CHAITIN'S SPILL HEURISTIC **
	    new SpillHeuristic() { double cost( Node m ) {  
		return chaitinCost(m) / m.degree; }}

	    // FSK: new experiments show that the reason results were
	    // worse is that the expected-costs predicted by the
	    // alternate heuristics have much greater error (when
	    // compared to the actual cost after optimistically
	    // coloring) than the original Chaitin heuristic's error.
	    // I am leaving this in for now, but it may be worthwhile
	    // to just use the standard Chaitin heurstic in general.
	    , 
	    new SpillHeuristic() { double cost( Node m ) {  
		return chaitinCost(m) / (m.degree * m.degree); }} 
	    , 
	    new SpillHeuristic() { double cost( Node m ) { 
		return chaitinCost(m) / ( area(m) * m.degree ); }} 
	    , 
	    new SpillHeuristic() { double cost( Node m ) { 
		return chaitinCost(m) / ( area(m) * m.degree * m.degree ); }}, 
	    
	};
	
	return hs;
    }
    static class CouldntFindSpillExn extends Exception {}
    private void selectSpill(SpillHeuristic sh) throws CouldntFindSpillExn {
	// Note: avoid choosing nodes that are tiny live ranges 
	//       from fetching spilled registers
	Node minNode = null; 
	double minCost = Double.MAX_VALUE;
	
    nextNode: 
	for(NodeIter nI = spill_worklist.iter(); nI.hasNext(); ) {
	    Node n = nI.next();
	    
	    Util.assert( ! isRegister(n.web.temp) );
	   
	    if (!n.web.isSpillable())
		continue nextNode;

	    double cost = sh.cost( n );
	    if (cost > minCost) 
		continue;
	    else {
		minNode = n;
		minCost = cost;
	    }
	}
	if (minNode == null) throw new CouldntFindSpillExn();

	// System.out.println("spilling node with cost : "+minCost);

	sh.expectSpill( minNode );
	spill_worklist.remove( minNode );
	simplify_worklist.add( minNode );
	freezeMoves( minNode );
    }

    public void assignColors() { 
	while( !select_stack.isEmpty() ){
	    Node n = select_stack.pop();
	    ColorSet okColors = new ColorSet(K);
	    for( NodeIter adj=n.neighbors.iter(); adj.hasNext(); ) {
		Node w = adj.next(); w = getAlias(w);
		if( w.isColored() || 
		    w.isPrecolored() ){
		    okColors.remove( w.color );
		}
	    }
	    // TODO: Add code here to handle assigning a color to more
	    // than one node at once, ala Brigg's multigraphs... 
	    if( okColors.isEmpty() ){
		spilled_nodes.add(n);
	    } else {
		colored_nodes.add(n);
		n.color = okColors.available();
	    }
	}

	for( NodeIter ni=coalesced_nodes.iter(); ni.hasNext(); ){
	    Node n = ni.next();
	    n.color = getAlias(n).color;
	}
    }

    int rewriteCalledNumTimes = 0;
    public void rewriteProgram() {
	rewriteCalledNumTimes++;

	if (try_to_clean && 
	    rewriteCalledNumTimes == NUM_CLEANINGS_TO_TRY) {
	    stopTryingToClean();
	    initializeSets();
	    bbFact = computeBasicBlocks();
	    return;
	} 

	// Allocate memory locations for each v : spilledNodes
	// Create a new temporary v_i for each definition and each use
	// In the program (instructions), insert a store after each
	//    definition of a v_i, a fetch before each use of a v_i. 
	// Put all the v_i into a set newTemps
	// spilledNodes <- {}
	// initial <- coloredNodes \/ coalescedNodes \/ newTemps
	// coloredNodes <- {}
	// coalescedNodes <- {}
	
	// simplifies to 
	// foreach w : spilledNodes.web
	//    foreach d : w.defs
	//       insert SpillProxy
	//    foreach u : w.uses
	//       insert RestoreProxy
	// (possibly excepting instrs that both define and use w.temp) 
	// initializeSets
	HashSet seenWebs = new HashSet();
	for(NodeIter ni=spilled_nodes.iter(); ni.hasNext(); ){
	    Node n = ni.next(); 
	    // System.out.println("spilling node with cost : "+costOf(n));
	    Web w = n.web;
	    if( seenWebs.add(w) ){
		Collection   spillCode = addDefs(w);
		Collection restoreCode = addUses(w);
	    }
	}

	if (PRINT_DEPTH_TO_SPILL_INFO) {
	    System.out.println();
	    System.out.print("locally "+spillStats(depthToNumSpills));
	    System.out.println();
	}

	initializeSets();
	bbFact = computeBasicBlocks();
    }

    private Collection addDefs(Web w) {
	HashSet groupDefs = new HashSet(w.defs.size());
	int cleanedNum = 0;
	for(Iterator ds=w.defs.iterator(); ds.hasNext();) {
	    Instr d = (Instr) ds.next();
	    Instr exit = d.getExit(InstrGroup.NO_SPILL);
	    
	    BasicBlock bb = bbFact.getBlock(exit);
	    Util.assert(bbFact.getBlock(exit) != null, 
			"no BB found for exit");

	    if (CLEAN_BB_LOCAL_DEFS && try_to_clean) {
		Set liveOut = liveTemps.getLiveAfter(bb.getLast());
		if ( ! liveOut.contains( w.temp )) {
		    System.out.println
			("refusing to spill bb-local! "+w.temp);
		    dontSpillTheseDefs.add(d);
		    cleanedNum++;
		    continue;
		}
	    }
	    groupDefs.add(exit);
	}

	if ( try_to_clean ){
	    // clean out redundant targets
	    LinearSet blocks = new LinearSet();
	    for(Iterator ds = groupDefs.iterator(); ds.hasNext(); ){
		Instr instr = (Instr)ds.next(); 
		BasicBlock bb = bbFact.getBlock( instr ); 
		blocks.add( bb );
	    }
	    for(Iterator bs = blocks.iterator(); bs.hasNext(); ){
		BasicBlock bb = (BasicBlock) bs.next();
		boolean seenOne = false;
		Iterator stms =bb.statements().iterator();
		stms = new ReverseIterator( stms );
		while( stms.hasNext() ){
		    Instr i = (Instr) stms.next();
		    if (groupDefs.contains(i)){ 
			if (!seenOne) {
			    seenOne = true; 
			    continue;
			} else {
			    groupDefs.remove(i);
			    cleanedNum++;
			}
		    }
		}
	    }
	    if (PRINT_CLEANING_INFO && cleanedNum != 0)
		System.out.println("Def cleaning removed "+cleanedNum);
	}

	return addDefs( w, groupDefs );
    }
    private Collection addDefs( Web w, Collection groupDefs) {
	ArrayList spills = new ArrayList();
	for(Iterator ds=groupDefs.iterator(); ds.hasNext();) {
	    Instr d = (Instr) ds.next();
	    Instr n = d.getNext();
	    Util.assert( d.canFallThrough &&
			 d.getTargets().isEmpty() );

	    depthToNumSpills[((Integer)nestedLoopDepth().get(d)).intValue()]++;

	    SpillProxy sp = new SpillProxy(d, w.temp);
	    spills.add(sp);
	    sp.layout(d, d.getNext());
	}
	return spills;
    }
    private Collection addUses(Web w) {
	HashSet groupUses = new HashSet(w.uses.size());
	for(Iterator us=w.uses.iterator(); us.hasNext();) {
	    Instr u = (Instr) us.next();		    
	    Instr instrToAdd = u.getEntry(InstrGroup.NO_SPILL);
	    Util.assert( ! instrToAdd.isDummy() );
	    groupUses.add( instrToAdd );
	}

	if ( try_to_clean ){ 
	    // clean out redundant targets
	    int cleanedNum = 0;
	    LinearSet blocks = new LinearSet();
	    for(Iterator ds = groupUses.iterator(); ds.hasNext(); ){
		blocks.add( bbFact.getBlock( (Instr)ds.next() ));
	    }
	    for(Iterator bs = blocks.iterator(); bs.hasNext(); ){
		BasicBlock bb = (BasicBlock) bs.next();
		boolean seenOne = false;
		Iterator stms =bb.statements().iterator();

		while( stms.hasNext() ){
		    Instr i = (Instr) stms.next();
		    if (groupUses.contains(i)){ 
			if (!seenOne) {
			    seenOne = true; continue;
			} else {
			    groupUses.remove(i);
			    cleanedNum++;
			}
		    }
		    if ( !seenOne && w.defs.contains(i) ){
			if ( PRINT_CLEANING_INFO )
			    System.out.print("\nFSK saw a def before a use: "+i);
			seenOne = true;
		    }
		}
	    }
	    if (PRINT_CLEANING_INFO && cleanedNum != 0) 
		System.out.println("\nUse cleaning removed "+cleanedNum);
	}

	return addUses( w, groupUses );
    } 
    private Collection addUses( Web w, Collection groupUses ){
	ArrayList spills = new ArrayList();
	for(Iterator us=groupUses.iterator(); us.hasNext();) {
	    Instr u = (Instr) us.next();
	    Instr p = u.getPrev();
	    Util.assert( p.canFallThrough &&
			 p.getTargets().isEmpty() &&
			 u.predC().size() == 1 );

	    depthToNumSpills[((Integer)nestedLoopDepth().get(u)).intValue()]++;

	    RestoreProxy rp = new RestoreProxy(u, w.temp);
	    spills.add(rp);
	    rp.layout(p, u);
	}
	return spills;
    }

    int precolor;
    public void makePrecolored(Temp reg) { 
	Web w = addWeb(reg, Collections.EMPTY_SET, Collections.EMPTY_SET);
	Node n = new Node(precolored, w); 
	webToNodes.put(w, Arrays.asList( new Node[]{ n } ));
	n.color = precolor;

	// FSK added 5/10/01 to handle >= K comparisons
	n.degree = Integer.MAX_VALUE;

	precolor++;
    }

    public void makeInitial(Temp t)    { 
	Set assns = rfi().getRegAssignments(t);
	List assn = (List) assns.iterator().next();

	for(Iterator webs=tempToWebs.getValues(t).iterator(); webs.hasNext();){
	    Web web = (Web) webs.next();
	    Node[] nodes = new Node[ assn.size() ];
	    for(int i=0; i < nodes.length; i++) {
		Node n = new Node(initial, web); 
		nodes[i] = n;
	    }
	    webToNodes.put( web, Arrays.asList( nodes ));
	}
    }
    
    // resets the state in preparation for a coloring attempt
    private void initializeSets() {
	precolor = 0;
	nextId = 0;
	instrToMove = new HashMap();
	initializeNodeSets();
	initializeMoveSets();
	adjSet = new NodePairSet();

	// FSK: Lazily building this mapping...
	nodeToLiveAt = null;
    }

    

    private boolean haveWebFor(Temp t, Instr i) {
	// TODO: implement method or eliminate calls to it
	return false;
    }
    private Web webFor(Temp t, Instr i) {
	if ( isRegister(t) ) { return (Web) tempToWebs.get(t); }

	// TODO: Find reasoning justifying this line! (webFor works on
	// concrete view of code?  Need to revise specs of many many
	// methods...)
	i = i.getEntry( InstrGroup.AGGREGATE );

	if ( !defCT(i).contains(t) && !useCT(i).contains(t) ){
	    // slight hack; look up a def associated with 't' for 'i'
	    // on demand.  Saves us trouble of storing total live
	    // range inside of web.  (New thought; can actually get
	    // away with only storing uses in Webs if necessary)
	    
	    Set defs = rdefs.reachingDefs(i,t);
	    
	    if (defs.isEmpty()) {
		code.printPreallocatedCode();
		Util.assert(false, "should exist defs of "+t+" that reach "+i);
	    }

	    i = (Instr) defs.iterator().next();
	}

	// should never be asserted now...
	Util.assert(defCT(i).contains(t) || useCT(i).contains(t),
		    "Method not guaranteed behave properly "+
		    "on Instrs that don't refer to 't'");

	Collection webs = tempToWebs.getValues(t);
	for(Iterator wi=webs.iterator(); wi.hasNext(); ) {
	    Web w = (Web) wi.next();
	    if (w.defs.contains(i) ||
		w.uses.contains(i) ) {
		return w;
	    } 
	}
	Util.assert(false, "Couldn't find the web for "+t+" , "+i);
	return null;
    }
    private Web addWeb( Temp t, Set defs, Set uses ) {
	Web web = new Web( t, defs, uses );
	tempToWebs.add( t, web );
	return web;
    }
    
    private List/*Node*/ nodesFor( Web web ) {
	if (CHECK_INV)
	Util.assert( webToNodes.containsKey( web ),
		     "! should have nodes for "+web);
	return (List) webToNodes.get( web );
    }
    
    // Temp t -> Webs for t (rather than going through a Temp renaming
    // process on the whole method)
    GenericMultiMap tempToWebs;

    // Web w -> Listof Node
    HashMap webToNodes = new HashMap();

    // Node n -> Set of Instr i, s.t. n is alive at i
    GenericMultiMap nodeToLiveAt = new GenericMultiMap();
    
    // Instr i -> Move m such m.instr = i
    HashMap instrToMove;
    

    /** REQUIRES: i.isMove() 
	returns a Move corresponding to Instr i. 
    */ 
    Move moveFor(Instr i) { 
	Util.assert( i.isMove() ); 
 
	if( instrToMove.containsKey(i) ) { 
	    return (Move) instrToMove.get(i); 
	} else { 
	    // TODO: Fix to assign collections, not first elems! 
	    Util.assert( defCN(i).size() == 1);
	    Util.assert( useCN(i).size() == 1); 
	    Node dst = (Node) defCN(i).iterator().next(); 
	    Node src = (Node) useCN(i).iterator().next(); 
	    Move m = new Move(i, dst, src); 
	    instrToMove.put(i, m);
	    return m;
	}
    }


}

// AppelRegAlloc.java, created Mon Feb  5 13:44:00 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Instr.AppelRegAllocClasses.Web;
import harpoon.Analysis.Instr.SpillHeuristics.SpillHeuristic;

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
 * @version $Id: AppelRegAlloc.java,v 1.3 2002-02-26 22:40:21 cananian Exp $
 */
public abstract class AppelRegAlloc extends AppelRegAllocClasses {
    public static final boolean PRINT_DEPTH_TO_SPILL_INFO = true;
    public static final boolean PRINT_HEURISTIC_INFO = true;
    public static final boolean PRINT_CLEANING_INFO = true;


    private static final int NUM_CLEANINGS_TO_TRY = 2;
    private boolean try_to_clean = false; // FSK: turning off cleaning during FORCE_FELIX Experimentation
    // activates an extension to cleaning that doesn't spill defs that
    // are dead at the BasicBlock's end.
    private static final boolean CLEAN_BB_LOCAL_DEFS = false; // trackdown _213_javac

    // activates a prepass removing "mov t0, t0" InstrMOVEs.
    private static final boolean TRIVIAL_MOVE_COALESCE = true;
    static RegAlloc.Factory FACTORY = new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new AppelRegAllocStd(c);
	    }
	};

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
	    if (insertedSpillCode.contains(i)) {
		i.remove();
	    }
	}
	insertedSpillCode.clear();
	dontSpillTheseDefs.clear();
	depthToNumSpills = new int[SPILL_STAT_DEPTH];
	try_to_clean = false;
    }
    HashSet insertedSpillCode = new HashSet();

    int K; // size of register set
    
    ReachingDefs rdefs;
    LiveTemps liveTemps;

    CFGrapher grapher;
    UseDefer  usedefer;

    SpillHeuristics sh = new SpillHeuristics(this);

    // Temp t -> Webs for t (rather than going through a Temp renaming
    // process on the whole method)
    GenericMultiMap tempToWebs;

    
    static final int SPILL_STAT_DEPTH = 20;
    int[] depthToNumSpills;
    static int[] globalDepthToNumSpills = new int[SPILL_STAT_DEPTH];
    protected String spillStats(int[] stats){
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<stats.length; i++) {
	    if (stats[i] != 0)
		sb.append
		    (",\tdepth:"+ i +
		     " => spills:"+ stats[i] );
	}
	return sb.toString();
    }
    

    protected AppelRegAlloc(Code code) { 
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
checkPrecolored();
	for(Iterator regs=rfi().getAllRegistersC().iterator();regs.hasNext();){
	    Temp reg = (Temp) regs.next();
	    makePrecolored(reg);
	}
checkPrecolored();
	for(Iterator temps=tempToWebs.keySet().iterator(); temps.hasNext();){
	    Temp temp = (Temp) temps.next();
	    if( ! isRegister(temp) ) {
		makeInitial( temp );
	    } else {
    Util.ASSERT( nodesFor( (Web) tempToWebs.get( temp ) ).size() == 1);
    Util.ASSERT( ((Node)nodesFor
		  ( (Web) tempToWebs.get( temp ) ).get(0)).isPrecolored() );
	    }
checkPrecolored();
	}
checkPrecolored();
    }
    
    


    public Derivation getDerivation() { return null; }

    // Invariants that hold post Build, see Appel pg 254
    public void checkInv() {
	if( ! CHECK_INV ) return; 

	checkPrecolored();
	checkMoveSets();
	checkDisjointInv();
	checkDegreeInv();
	checkSimplifyWorklistInv();
	checkFreezeWorklistInv();
	checkSpillWorklistInv();

    }

    public void checkPrecolored() {
	if (!CHECK_INV) return;

	for(NodeIter ni=precolored.iter(); ni.hasNext();){
	    Util.ASSERT(ni.next().isPrecolored());
	}
	for(Iterator nodes=allNodes(); nodes.hasNext(); ){
	    Node n = (Node) nodes.next();
	    if (n.web == null) continue;
	    Util.ASSERT(n.isPrecolored() || !rfi().isRegister( n.web.temp ) , n );
	    Util.ASSERT(!n.isPrecolored() || rfi().isRegister( n.web.temp ) , n );
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
		Util.ASSERT( ! intersects( sets[j], s ));
	    }
	}
    }
    
    /** Checks degree invariant documented in Tiger book; 
	abstract because a variant of the algorithm breaks
	it on purpose.
	The invariant, as given in the Tiger book, is:
	<pre>
	 u isIn (simplifyWorklist \/ freezeWorklist \/ spillWorklist ) ==>
	   degree(u) = | adjList(u) /\ ( precolored \/ simplifyWorklist \/ 
	                                 freezeWorklist \/ spillWorklist ) |
        </pre>
    */
    protected abstract void checkDegreeInv();

    public void checkSimplifyWorklistInv() {
	// u isIn simplifyWorklist ==>
	//   degree(u) < K && 
	//   moveList[u] /\ ( activeMoves \/ worklistMoves ) = {} 
	
	for( NodeIter ni=simplify_worklist.iter(); ni.hasNext(); ) {
	    Node u = ni.next();
	    Util.ASSERT(u.degree < K, 
			"simplify worklist inv. violated "+
			"( should be u.degree < K )" );
	    for( Iterator mi = u.moves.iterator(); mi.hasNext(); ) {
		Move m = (Move) mi.next();
		Util.ASSERT( !m.isActive(), 
			     "simplify worklist inv. violated"+
			     "( should be !m.isActive() )");
		Util.ASSERT( !m.isWorklist(), 
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
	    Util.ASSERT(u.degree < K, "freeze worklist inv. violated" );
	    
	    for( Iterator mi = u.moves.iterator(); mi.hasNext(); ) {
		Move m = (Move) mi.next();

		if( m.isActive() || m.isWorklist() ) 
		    return;
	    }

	    Util.ASSERT( false, "freeze worklist inv. violated, "+
			 " node:"+u+
			 " moves:"+u.moves);
	}
    }

    public void checkSpillWorklistInv() {
	// u isIn spillWorklist ==> degree(u) >= K
	for( NodeIter ni=spill_worklist.iter(); ni.hasNext(); ) {
	    Node u = ni.next();
	    Util.ASSERT( u . degree >= K , "spill worklist inv. violated");
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
		Util.ASSERT( i.defC().size() == 1);
		Util.ASSERT( i.useC().size() == 1); 
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
    
    /** Finds the minimum cost heuristic in <code>h</code>.
	<BR> <B>requires:</B> all h' in h have been run.
	@return the h' in h with minimum actualCost.
    */
    protected SpillHeuristic minimum(SpillHeuristic[] h) {
	SpillHeuristic h_min = h[0];
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
	
	if( PRINT_HEURISTIC_INFO 
	    // (leave -2 results out of output when not incremental)
	    && (minIndex != -2) ) {
	    
	    for(int i=0;i<h.length;i++)
		System.out.print("\nAPPLY SPILL HEURISTIC "+i+" \t=> "+h[i]);
	    System.out.println
		("\nCHOOSING SPILL HEURISTIC "+minIndex+" \t=> "+h_min);
	}
	return h_min;
    }

    public void generateRegAssignment() { 
	trivialMoveCoalesce();

	while (true) {
	    // System.out.println("post spill coloring");
	    initializeSets();
	    preAnalysis();
	    buildTempToWebs();
	    buildWebToNodes();
	
// Tracking down MIPS failure
for(Iterator regs=rfi().getAllRegistersC().iterator();regs.hasNext();){
    Temp reg = (Temp) regs.next();
    Util.ASSERT( nodesFor( (Web) tempToWebs.get( reg ) ).size() == 1);
    Util.ASSERT( ((Node)nodesFor
		  ( (Web) tempToWebs.get( reg ) ).get(0)).isPrecolored() );
}
checkPrecolored();
	    
	    adjSet = makeNodePairSet();
	    
	    // Appel's real code begins here
	    buildInterferenceGraph();
	    
	    checkInv();
	    try {
		SpillHeuristic h_min = null;
		SpillHeuristic[] h = sh.spillHeuristics();
		if ( h.length == 1 ) {
		    appelLoopBody( h[0] );
		} else {
		    checkpointState();
		    for(int i = 0; i < h.length; i++) {
			// FSK: look into breaking out of this loop if
			// we color without any spilling at all.
			appelLoopBody( h[i] );
			
			checkMoveSets();
			
			assignColors();
			
			checkInv();
			
			if (h[i].maxExpSpills == 0)
			    Util.ASSERT( spilled_nodes.isEmpty() );
			
			if ( spilled_nodes.isEmpty() ) {
			    h_min = h[i];
			    
			    // FSK: break here for better speed
			    // (no spills ==> don't need reiteration of heuristics) 
			    // ((may want disable later if i gather
			    //   data measuring benefit of alt spill
			    //   heuristics))
			    break;
			} else {
			    h[i].reallySpill(spilled_nodes.iter());
			}
			
			resetState();
		    }
		    
		    if (h_min == null)
			h_min = minimum(h);
		    
		    h_min.reset();
		    appelLoopBody( h_min );
		    
		}

		assignColors();
		// h_min.reallySpill(spilled_nodes.iter());
		// System.out.println("\nFINAL SPILL HEURISTIC   \t=> "+h_min);
		
		checkInv();
		
		if( ! spilled_nodes.isEmpty()) {
		    System.out.print(" R"+rewriteCalledNumTimes+", S!"+spilled_nodes.asSet().size());
		    rewriteProgram();  checkInv();
		    continue;
		} else {
		    break;
		}
	    } catch (CouldntFindSpillExn e) {
		if (try_to_clean) {
		    if (PRINT_CLEANING_INFO)
			System.out.println
			    ("COULDN'T FIND A SPILL!  TURNING OFF CLEANING!");
		    stopTryingToClean();
		} else {
		    String s = "[ ";
		    for(NodeIter ni = spill_worklist.iter(); ni.hasNext();){
			s += ni.next().web+ (ni.hasNext() ? ", " : " ]");
		    }
		    Util.ASSERT(false, s);
		}
		bbFact = computeBasicBlocks();
	    }
	}
	
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

	// introduce Flex-style assignments here
	performFinalAssignment(colorToReg());

    }

    protected Temp[] colorToReg() {
	// set up color (int) -> register mapping
	Temp[] regs = new Temp[ precolored.size ];
	for(NodeIter ri=precolored.iter(); ri.hasNext();) {
	    Node r = ri.next();
	    // this is legal; node in precolored has exactly one color.
	    regs[r.color[0]] = r.web.temp; 
	}
	return regs;
    }

    // FSK: placeholder for future method that should allow me to 
    // push more code from subclasses up into here.
    protected List/*Reg*/ webToRegAssignment( Web w ){
	return null;
    }
    
    /** Gives each Instr in the code a register assignment. 
	This is the last step in generateRegAssignment; it 
	is abstract because different variants have different
	node to color mappings and so they extract the 
	assignments in different ways.
	@param regs (regs[i] == n.web.temp) ==> (n in precolored && n.color = i).
    */
    protected abstract void performFinalAssignment(Temp[] regs);

    
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
	if (false && CHECK_INV) // TODO need to fix that r0 def at outset thing
	    Check.allLiveVarsHaveDefs
		( code, bbFact, grapher, usedefer, rdefs, liveTemps );

	if (false) System.out.println("RegAlloc Analysis for "+
				      code.getMethod().getName()+
				      " completed successfully ");

	
    } 




    /** Builds the interference graph for the code.
	Abstract because Moves need to be treated specially 
	by this routine, and variants of the algorithm 
	have different ways of dealing with Moves.
	The algorithm as given in the Tiger book is: 
	<pre>
	forall b : blocks in program
	    let live = liveOut(b)
	    forall I : instructions(b) in reverse order
	       if isMoveInstruction(I) then
	          live <- live \ use(I)
	          forall n : def(I) \/ use(I)
	             moveList[n] <- moveList[n] \/ I
	          worklistMoves <- worklistMoves \/ { I }
	       live <- live \/ def(I)
	       forall d : def(I)
	          forall l : live
	             AddEdge(l, d)
	       live <- use(I) \/ ( live \ def(I) )
        </pre>
     */
    protected abstract void buildInterferenceGraph();

    protected Instr lastStm(BasicBlock b) {
	List stms = b.statements();
	return (Instr) stms.get( stms.size() - 1 );
    }
    protected Instr firstStm(BasicBlock b) { 
	return (Instr) b.statements().get(0); 
    }
    protected Instr pred(Instr i) {
	Instr i_r = (Instr) grapher.predElemC(i).iterator().next();
	Util.ASSERT(i != i_r);
	return i_r;
    }

    protected Collection useCT( Instr i ) { return usedefer.useC( i ); }
    protected Collection defCT( Instr i ) { return usedefer.defC( i ); }

    protected NodeIter nodesIter(final Iterator i) {
	return new NodeIter() {
		public boolean hasNext() { return i.hasNext(); }
		public Node next() { return (Node) i.next(); }
	    };
    }

    Iterator instrs() {
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
    
    /** Introduces an edge between u and v in both the
	interference graph and in the neighbor lists for 
	u and v. 
	Abstract because variants of the algorithm have 
	different ways of handling the incrementing of 
	the degrees and of mutating the neighbor lists. 
    */
    protected abstract void addEdge( Node u, Node v );

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
	    Util.ASSERT( m.isWorklist(), "m should be in worklist_moves" );
	    for(Iterator dI=m.dsts().iterator(); dI.hasNext(); ) {
		Node n = (Node) dI.next();
		if (CHECK_INV)
		Util.ASSERT( moveRelated( n ), 
			     "m.dst should be moveRelated, not "+n.s_rep.name );
	    }
	    for(Iterator uI=m.srcs().iterator(); uI.hasNext(); ) {
		Node n = (Node) uI.next();
		if (CHECK_INV)
		Util.ASSERT( moveRelated( n ), 
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

	    decrementDegree(m,n);
	}
    }

    /** Updates m to account for n being removed from the interference 
	graph.
	Abstract because variants of the algorithm have different 
	ways to represent the interferences between the nodes.
	modifies: m, active_moves, worklist_moves, spill_worklist, 
	          freeze_worklist, simplify_worklist
        effects: m in Precolored ==> no changes
	         else decrements degree of m, moving it (and associated moves)  
                 into the appropriate lists if it has crossed the threshold 
		 from K to K-1.
    */
    protected abstract void decrementDegree( Node m, Node n );

    protected void enableMoves( NodeIter nodes ) {
	while( nodes.hasNext() )
	    enableMoves( nodes.next() );
    }
    protected void enableMoves( Node node) {
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
	    // seems silly until you notice the getAlias(..)'s above

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
		  ( ! u.isPrecolored() && conservative(u, v)) ) {
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
    /** conservative coalescing heuristic due to Preston Briggs.
	Abstracting out because in the prescence of multi-slotted 
	nodes, this changes. 
     */
    protected abstract boolean conservative(Node u, Node v);


    protected Node getAlias(Node n) { // FSK: umm... bound on this runtime? 
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

	enableMoves(v); 
	u.moves.append(v.moves);
	
	for( NodeIter adj=adjacent(v); adj.hasNext(); ) {
	    Node t = adj.next();
	    addEdge(t,u);
	    decrementDegree(t,v);
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

    private boolean spillable( Web w ) {
	for(Iterator di = w.defs.iterator(); di.hasNext(); ) {
	    Instr d = (Instr) di.next();
	    if (d instanceof RestoreProxy ||
		dontSpillTheseDefs.contains(d)) {
		return false;
	    }
	}
	for(Iterator ui = w.uses.iterator(); ui.hasNext(); ){
	    if (ui.next() instanceof SpillProxy) {
		return false;
	    }
	}
	return true;
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
	    
	    Util.ASSERT( ! isRegister(n.web.temp) );
	   
	    // if (!n.web.isSpillable()) 
	    if ( ! spillable(n.web) ) 
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

    /** Gives the nodes a legal color assignment.
	Abstract because different variants have different 
	ways of mapping nodes to colors.
     */
    protected abstract void assignColors(); 

    int rewriteCalledNumTimes = 0;
    public void rewriteProgram() {
	rewriteCalledNumTimes++;

	if (try_to_clean && 
	    rewriteCalledNumTimes == NUM_CLEANINGS_TO_TRY) {
	    stopTryingToClean();
	    // initializeSets();
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
		insertedSpillCode.addAll(spillCode);
		insertedSpillCode.addAll(restoreCode);
	    }
	}

	if (PRINT_DEPTH_TO_SPILL_INFO) {
	    System.out.println();
	    System.out.print("locally "+spillStats(depthToNumSpills));
	    System.out.println();
	}

	// initializeSets();
	bbFact = computeBasicBlocks();
    }

    private Collection addDefs(Web w) {
	HashSet groupDefs = new HashSet(w.defs.size());
	int cleanedNum = 0;
	for(Iterator ds=w.defs.iterator(); ds.hasNext();) {
	    Instr d = (Instr) ds.next();
	    Instr exit = d.getExit(InstrGroup.NO_SPILL);
	    
	    BasicBlock bb = bbFact.getBlock(exit);
	    Util.ASSERT(bbFact.getBlock(exit) != null, 
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
	    Util.ASSERT( d.canFallThrough &&
			 d.getTargets().isEmpty() );

	    depthToNumSpills[sh.depth(d)]++;

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
	    Util.ASSERT( ! instrToAdd.isDummy() );
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
	    Util.ASSERT( p.canFallThrough &&
			 p.getTargets().isEmpty() &&
			 u.predC().size() == 1 );

	    depthToNumSpills[sh.depth(u)]++;

	    RestoreProxy rp = new RestoreProxy(u, w.temp);
	    spills.add(rp);
	    rp.layout(p, u);
	}
	return spills;
    }

    int precolor;
    protected Node makePrecolored(Temp reg) { 
	Util.ASSERT( rfi().isRegister(reg) );
	Web w = new Web(reg, Collections.EMPTY_SET, Collections.EMPTY_SET);
	tempToWebs.put(reg,w);
	Node n = new Node(precolored, w); 
	n.color[0] = precolor;
	n.degree = Integer.MAX_VALUE;
	precolor++;
	return n;
    }

    /** Creates node(s) for <code>t</code> in the interference graph.
	Abstract because variants of the algorithm map <code>Temps</code>
	to nodes differently.
    */
    protected abstract void makeInitial(Temp t);    

    // resets the state in preparation for a coloring attempt
    protected void initializeSets() {
	precolor = 0;
	initializeNodeSets();
	initializeMoveSets();
	sh.reset();
    }

    protected NodeIter def(Instr i) { 
	return nodesIter( defCN( i ).iterator() );
    }
    protected NodeIter usedef(Instr i) { 
	HashSet s = new HashSet();
	s.addAll( useCN( i ));
	s.addAll( defCN( i ));
	return nodesIter( s.iterator() );
    }

    protected Collection/*Node*/ useCN(Instr i) { 
	Collection nodeC = tempCtoNodes( useCT( i ), i );
	return nodeC;
    }
    
    protected Collection/*Node*/ defCN(Instr i) { 
	Collection nodeC = tempCtoNodes( defCT( i ), i);
	return nodeC;
    }

    protected Set/*Node*/ tempCtoNodes( Collection temps, Instr i ) {
	HashSet set = new HashSet();
	for(Iterator ts=temps.iterator(); ts.hasNext(); ) {
	    Temp t = (Temp) ts.next();
	    Web web = webFor( t, i );
	    set.addAll( nodesFor( web ));
	}
	return set; 
    }
    // RETURN a Set of Node or a NodeSet ???
    protected Set liveOut(BasicBlock b) {
	Instr last = lastStm( b );
	Set s = liveTemps.getLiveAfter( last );

	// TODO: check that 'last' is the right thing to pass in here... 
	return tempCtoNodes( s, last );
    }


    /** Returns a List of Node mapped to by <code>w</code>. 
	Abstract because variants of the algorithm map Webs to Nodes 
	in different ways.
     */
    protected abstract List nodesFor( Web w ); 


    private boolean haveWebFor(Temp t, Instr i) {
	// TODO: implement method or eliminate calls to it
	return false;
    }
    protected Web webFor(Temp t, Instr i) {
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
		Util.ASSERT(false, "should exist defs of "+t+" that reach "+i);
	    }

	    i = (Instr) defs.iterator().next();
	}

	// should never be asserted now...
	Util.ASSERT(defCT(i).contains(t) || useCT(i).contains(t),
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
	Util.ASSERT(false, "Couldn't find the web for "+t+" , "+i);
	return null;
    }
    private Web addWeb( Temp t, Set defs, Set uses ) {
	Web web = new Web( t, defs, uses );
	tempToWebs.add( t, web );
	return web;
    }
    
}

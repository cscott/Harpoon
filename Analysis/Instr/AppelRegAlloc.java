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
 * @version $Id: AppelRegAlloc.java,v 1.1.2.4 2001-06-17 22:29:48 cananian Exp $
 */
public class AppelRegAlloc extends /*RegAlloc*/AppelRegAllocClasses {
    // FSK: super class really SHOULD be RegAlloc, but am doing this
    // for now to inherit fields from ARAClasses (refactoring)

    public static final boolean CHECK_INV = false;


    static RegAlloc.Factory FACTORY = new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new AppelRegAlloc(c);
	    }
	};

    // Set of <Node, Node> pairs
    NodePairSet adjSet;
    
    int K; // size of register set
    
    ReachingDefs rdefs;
    LiveTemps liveTemps;

    CFGrapher grapher;
    UseDefer  usedefer;

    public AppelRegAlloc(Code code) { 
	super(code); 
	K = rfi().getGeneralRegistersC().size();
	grapher= code.getInstrFactory().getGrapherFor ( InstrGroup.AGGREGATE );
	usedefer=code.getInstrFactory().getUseDeferFor( InstrGroup.AGGREGATE );
	// System.out.println("done constructing AppelRegAlloc");
    }

    void buildTempToWebs() { 
	// translated from code in GraphColoringRegAlloc
	tempToWebs = new GenericMultiMap(Factories.arrayListFactory);

	for(Iterator instrs=code.getElementsI(); instrs.hasNext();){
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
			Set ns = new HashSet( web1.defs );
			ns.retainAll( web2.defs );
			combineWebs = ! ns.isEmpty();

			if( ! combineWebs ){
			    // IMPORTANT: current temp->reg assignment
			    // design breaks if an instr needs two
			    // different regs for the same temp in the
			    // uses and defines.  Take this clause out
			    // after that is fixed.
			    HashSet s1 = new HashSet( web1.defs );
			    s1.retainAll( web2.uses );
			    
			    HashSet s2 = new HashSet( web2.defs );
			    s2.retainAll( web1.uses );
			    combineWebs = ( ! s1.isEmpty() || 
					    ! s2.isEmpty() );
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
		// System.out.print("("+i+","+j+") ");
		HashSet t = new HashSet(sets[j]);
		t.retainAll(s);
		Util.assert(t.isEmpty());
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
    
    public void appelLoopBody( SpillHeuristic sh ) {
	
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
    
    public void generateRegAssignment() { 
	while (true) {
	    // System.out.println("post spill coloring");
	    initializeSets();     
	    preAnalysis();        
	    buildTempToWebs();    
	    buildWebToNodes();    
	    
	    // FSK: Lazily building...
	    // System.out.print("N");
	    // buildNodeToLiveAt();  
	    nodeToLiveAt = null;

	    // Appel's real code begins here
	    buildInterferenceGraph();
	    
	    checkInv();
	    // saveNodeSets();
	    // resetMoveSets();

	    SpillHeuristic[] h = spillHeuristics();
	    if (false) { // if (h.length > 1) {
		for(int i = 0; i < h.length; i++) {
		    appelLoopBody( h[i] );
		    checkMoveSets();
		    restoreNodeSets();
		    resetMoveSets();
		}
		SpillHeuristic h_min = h[0];
		int minIndex = 0;
		
		for(int i=1; i < h.length; i++) {
		    if (h[i].accumCost < h_min.accumCost) {
			h_min = h[i];
			minIndex = i;
		    }
		}
		appelLoopBody( h_min );
	    } else {
		appelLoopBody( h[0] );
	    }
	    assignColors(); checkInv();

	    if( ! spilled_nodes.isEmpty()) {
		System.out.print(" S!"+spilled_nodes.asSet().size());
		rewriteProgram();  checkInv();
		continue;
	    } else {
		break;
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
		Web w = webFor(t, inst);
		List nodes = nodesFor( w );
		code.assignRegister( inst, t, toRegList( nodes, regs ));
	    }
	}

	
	// debugging output
	if (false) {
	    liveTemps.dumpElems(); System.out.println();
	    bbFact.dumpCFG();      System.out.println();
	    code.printPreallocatedCode();
	}

	fixupSpillCode();
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

	
	buildNestedLoopDepth();


    } 

    // requires: nestedLoopDepth has been properly initalized
    int depth(Instr i){ return((Integer)nestedLoopDepth.get(i)).intValue(); }
    int width(Instr i){ return liveAt(i).size(); }

    // Instr -> Integer, val is loop nesting depth for the key (0 for
    // non-looped code)
    Map nestedLoopDepth; 
    void buildNestedLoopDepth() { 
	// builds the nestedLoopDepth map, doing a breadth-first
	// traversal of the loop tree.
	nestedLoopDepth = new HashMap();
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
		    Util.assert( ! nestedLoopDepth.keySet().contains( i ));
		    nestedLoopDepth.put( i , currDepth );
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
			     nestedLoopDepth.keySet().contains(i), 
			     "reachable instrs should have loop depth"); 
	    }
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
		// 5/12/01: temporarily disabling move coalescing (debug segfault)
		if( false && i.isMove() ){
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
			addEdge( (Node) li.next(), d );
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
        effects: decrements degree of m, moving it (and associated moves)  
                 into the appropriate lists if it has crossed the threshold 
		 from K to K-1.
    */
    private void decrementDegree( Node m ) {
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
	
	if (CHECK_INV) 
        Util.assert( u.isFreezeWorkL() || 
		     u.isSpillWorkL()  ||
		     u.isPrecolored()  ,
		     "u must be in Freeze or Spill or Precolored"+
		     " not "+u.s_rep.name);

	if (CHECK_INV) 
	Util.assert( v.isFreezeWorkL() || 
		     v.isSpillWorkL()  || 
		     v.isPrecolored()  ,
		     "v must be in Freeze or Spill or Precolored"+
		     " not "+v.s_rep.name);

	// FSK: see coalesce() above [ pop() implicitly removes m ]
	// worklist_moves.remove(m); 

	// TODO: NEED TO THINK ABOUT WHICH SET THE MOVE WILL GO INTO!!! 
	// ( possibly adding new cases here?!!? )

	if (u.equals(v)) {
	    // System.out.println( u+" .equals "+v );

	    coalesced_moves.add(m);
	    addWorkList(u);

	} else if (v.isPrecolored() && adjSet.contains(u,v) ) {
	    // System.out.println( v+" .isPrecolored "+"and adjSet.contains ("+u+","+v+")" );

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

	    if (CHECK_INV) 
	    Util.assert( v.isFreezeWorkL() || v.isSpillWorkL(),
			 "v must be in Freeze or in Spill, "+
			 "not "+v.s_rep.name );

	    if (CHECK_INV) 
	    Util.assert( u.isFreezeWorkL() || u.isSpillWorkL() || u.isPrecolored(),
			 "u must be in Freeze, Spill, or Precolored, "+
			 "not "+u.s_rep.name );

	    combine(u, v);

	    // FSK: this assertion should hold, but soon I'm just
	    // going to hack around the problem in addWorkList so that
	    // I can move on to other things
	    if (CHECK_INV) 
	    Util.assert( u.isFreezeWorkL() || u.isSpillWorkL() || u.isPrecolored(),
			 "u must be in Freeze, Spill, or Precolored, "+
			 "not "+u.s_rep.name+
			 " temp:"+u.web.temp);
	    
	    
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

	    // FSK: invariant "u isIn freeze_worklist" here is being
	    // violated.  Track down later.
	    freeze_worklist.remove( u );
	    // u.s_rep.remove( u );

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
	// FSK TODO: this is not a strict union.  Find out if it
	// double-counting is an issue.
	return conservative( combine( new NodeIter[]{ ni1, ni2 } ));
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
	// System.out.println("called combine("+u+","+v+")");
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
	    if (CHECK_INV) 
	    Util.assert(m.isActive(), 
			"move:"+m+" should be in active if we're doing "+
			"freezeMoves( "+u+" ),"+
			" v:"+v+
			" getAlias(x):"+getAlias(x)+
			" getAlias(y):"+getAlias(y)
			);
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
	    return "SpillHeuristic<cost:"+accumCost
		+" spills:"+numSpills
		+">";
	}
	double accumCost = 0.0;
	int numSpills = 0;
	void spill( Node m ) { 
	    // IMPORTANT: don't confuse "cost" here (which is called
	    // "h_i" in the paper) with "cost" in the paper (which is
	    // called chaitinCost here)
	    accumCost += chaitinCost( m ); 
	    numSpills++;
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

	HashMap instrToAreaCache = new HashMap();
	HashMap nodeToAreaCache = new HashMap();    
    }


    private SpillHeuristic[] spillHeuristics() {
	return new SpillHeuristic[] { 
	    new SpillHeuristic() { double cost( Node m ) {  
		return chaitinCost(m) / m.degree; }}

	    // TODO: the reason that the spills were worse is that the
	    // data-structures were not being reset "enough", making
	    // the interference graphs more dense, and thus leading to
	    // excess spilling.  FIX FIX FIX!!!

	    /* // FSK: initial experiments indicate that combo is slow
	       // and produces WORSE actual spills than the above alone.  C:( 

	    ,
	    new SpillHeuristic() { double cost( Node m ) { 
		return chaitinCost(m) / (m.degree * m.degree); }}
	    ,
	    new SpillHeuristic() { double cost( Node m ) {
		return chaitinCost(m) / ( area(m) * m.degree ); }}
	    ,
	    new SpillHeuristic() { double cost( Node m ) {
		return chaitinCost(m) / ( area(m) * m.degree * m.degree ); }},
	    */
	};
    }

    private void selectSpill(SpillHeuristic sh) {
	// Note: avoid choosing nodes that are tiny live ranges 
	//       from fetching spilled registers
	Node minNode = null; 
	double minCost = Double.MAX_VALUE;
	
    nextNode: 
	for(NodeIter nI = spill_worklist.iter(); nI.hasNext(); ) {
	    Node n = nI.next();
	    
	    Util.assert( ! isRegister(n.web.temp) );
	    
	    // these loops skip over prev. spilled nodes... 
	    for(Iterator di = n.web.defs.iterator(); di.hasNext(); )
		if (di.next() instanceof RestoreProxy) {
		    continue nextNode;
		}
	    for(Iterator ui = n.web.uses.iterator(); ui.hasNext(); )
		if (ui.next() instanceof SpillProxy) {
		    continue nextNode;
		}

	    double cost = sh.cost( n );
	    if (cost > minCost) 
		continue;
	    else {
		minNode = n;
		minCost = cost;
	    }
	}
	Util.assert( minNode != null , "couldn't find non-spilled node!" );

	// System.out.println("spilling node with cost : "+minCost);

	sh.spill( minNode );
	spill_worklist.remove( minNode );
	simplify_worklist.add( minNode );
	freezeMoves( minNode );
    }

    public void assignColors() { 
	while( !select_stack.isEmpty() ){
	    Node n = select_stack.pop();
	    ColorSet okColors = new ColorSet(K);
	    for( NodeIter adj=adjacent(n); adj.hasNext(); ) {
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

	System.out.println();
	for(int i=0; i<depthToNumSpills.length; i++) {
	    if (depthToNumSpills[i] != 0)
		System.out.println
		    (" depth:"+ i +
		     " spill instrs:"+ depthToNumSpills[i] );
	}
	
	initializeSets();

	bbFact = computeBasicBlocks();
    }
    static int[] depthToNumSpills = new int[20];
    private Collection addDefs(Web w) {
	HashSet groupDefs = new HashSet(w.defs.size());
	for(Iterator ds=w.defs.iterator(); ds.hasNext();) {
	    Instr d = (Instr) ds.next();
	    groupDefs.add(d.getExit(InstrGroup.NO_SPILL));
	}

	// FSK: cleaning made things worse?

	// NOTE (5/15/01) Cleaning is non-trivial.  Notably, you need
	// to ensure that if you remove a spill-store, that you don't
	// insert a spill-load while the spill-memory-location is
	// undefined.  
	// I think the way to do it is to change the insert-restore
	// predicate to not only track if a restore would have already
	// been there, but also if a *DEF* would have already been
	// there.
	// For now, am turning off Def-cleaning but leaving on
	// Use-cleaning as originally implemented, which can be shown
	// to be safe.  Then I'll incrementally add the changes (first
	// fixing use-cleaning, then turning def-cleaning back on)
	
	if ( false && rewriteCalledNumTimes < 2 ){
	    // clean out redundant targets
	    int cleanedNum = 0;
	    LinearSet blocks = new LinearSet();
	    for(Iterator ds = groupDefs.iterator(); ds.hasNext(); ){
		blocks.add( bbFact.getBlock( (Instr)ds.next() ));
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
			    seenOne = true; continue;
			} else {
			    groupDefs.remove(i);
			    cleanedNum++;
			}
		    }
		}
	    }
	    if (cleanedNum != 0)
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

	    depthToNumSpills[((Integer)nestedLoopDepth.get(d)).intValue()]++;

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

	// FSK: cleaning made things worse?
	if ( rewriteCalledNumTimes < 2 ){
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
		}
	    }
	    if (cleanedNum != 0) 
		System.out.println("Use cleaning removed "+cleanedNum);
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

	    depthToNumSpills[((Integer)nestedLoopDepth.get(u)).intValue()]++;

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
    

    /** REQUIRES: i.isMove()
	returns a Move corresponding to Instr i.
    */
    Move moveFor(Instr i) {
	Util.assert( i.isMove() );

	if( instrToMove.containsKey(i) ){
	    return (Move) instrToMove.get(i);
	} else {
	    Move m = new Move(i);
	    instrToMove.put(i, m);
	    return m;
	}
    }

    final class Move {
	Collection dsts() { return Collections.singleton(dst); }
	Collection srcs() { return Collections.singleton(src); }
	Node dst, src;
	Move s_prev, s_next;
	MoveSet s_rep; // Set Representative
	Instr instr = null; // null for dummy header Moves

	// dummy ctor
	Move() { s_prev = s_next = this; }

	Move(Instr i) {
	    this();
	    instr = i;

	    // TODO: Fix to assign collections, not first elems!
	    Util.assert( defCN(i).size() == 1);
	    Util.assert( useCN(i).size() == 1);
	    this.dst = (Node) defCN(i).iterator().next();
	    this.src = (Node) useCN(i).iterator().next();
	}
	public boolean isCoalesced()   { return s_rep == coalesced_moves; }
	public boolean isConstrained() { return s_rep == constrained_moves; }
	public boolean isFrozen()      { return s_rep == frozen_moves; }
	public boolean isWorklist()    { return s_rep == worklist_moves; }
	public boolean isActive()      { return s_rep == active_moves; }

	public String toString() {
	    // remove package info cruft from identity-based String
	    String s = super.toString();
	    int i = s.indexOf("Move");
	    return 
		s.substring(i)+
		" set:"+s_rep+
		" instr:"+instr;
	}
    }

    HashMap instrToMove;
    
    MoveSet coalesced_moves;
    MoveSet constrained_moves;
    MoveSet frozen_moves;
    MoveSet worklist_moves;
    MoveSet active_moves;
    void checkMoveSets() {
	if( ! CHECK_INV ) 
	    return;

	MoveSet[] sets = new MoveSet[]{
	    coalesced_moves,constrained_moves,
	    frozen_moves,worklist_moves,
	    active_moves
	};
	for(int i=0; i<sets.length; i++) 
	    sets[i].checkRep();
    }

    // these sets collectively partition the space of Moves
    void initializeMoveSets() {
	coalesced_moves   = new MoveSet("coalesced_moves");
	constrained_moves = new MoveSet("constrained_moves");
	frozen_moves      = new MoveSet("frozen_moves");
	worklist_moves    = new MoveSet("worklist_moves");
	active_moves      = new MoveSet("active_moves");
    }

    void resetMoveSets() {
	// all sets except worklist_moves
	MoveSet[] sets = new MoveSet[]{ 	
	    coalesced_moves,constrained_moves,
	    frozen_moves,active_moves
	};
	for(int i=0; i<sets.length; i++) 
	    while( ! sets[i].isEmpty() ) {
		sets[i].checkRep();
		worklist_moves.add( sets[i].pop() );
	    }
    }

    final class MoveSet { 
	private int size;
	private Move head; // dummy element
	final String name;
	public String toString() {
	    return "MoveSet:"+name;
	}
	MoveSet(String name) { 
	    this.name = name;
	    head = new Move(); 
	    add_no_check_rep(head); 
	    size = 0;
	    checkRep();
	}
	
	void checkRep() {
	    if( ! CHECK_INV )
		return;

	    Move curr = head;
	    int checkSize = -1;
	    do {
		checkSize++;
		Util.assert( curr.s_next.s_prev == curr );
		Util.assert( curr.s_next.s_prev == curr );
		Util.assert( curr.s_rep == this );
		curr = curr.s_next;
	    } while(curr != head);
	    if (CHECK_INV)
	    Util.assert( size == checkSize, 
			 "size should be "+checkSize+" not "+size );
	}
	Iterator iter() {
	    return new harpoon.Util.UnmodifiableIterator() {
		    Move curr = head;
		    public boolean hasNext() { return curr.s_next != head; }
		    public Object next() {
			Move n = curr.s_next;
			curr = curr.s_next;
			return n;
		    }
		};
	}
	boolean isEmpty() { return size == 0; }
	Move pop() { 
	    Util.assert(!isEmpty(), "should not be empty");
	    Move n = head.s_next; 
	    if (CHECK_INV)
	    Util.assert(n!=head, "should not return head, "+
			"size:"+size+" set:"+asSet()); 
	    remove(n); 
	    return n; 
	}
	
	void remove(Move n) {
	    checkRep();
	    if (CHECK_INV)
	    Util.assert(n.s_rep == this, 
			"called "+this+".remove(..) "+
			"on move:"+n+" in "+n.s_rep );
	    Util.assert(size != 0);
	    n.s_prev.s_next = n.s_next;
	    n.s_next.s_prev = n.s_prev;
	    n.s_rep = null; n.s_prev = null; n.s_next = null;
	    size--;
	    checkRep();
	}
	void add(Move n) {
	    checkRep();
	    add_no_check_rep(n);
	    checkRep();
	}
	private void add_no_check_rep(Move n){
	    Move prev = head.s_prev;
	    prev.s_next = n;
	    n.s_prev = prev; n.s_next = head;
	    n.s_rep = this;
	    head.s_prev = n;
	    size++;
	}
	Set asSet() {
	    HashSet rtn = new HashSet();
	    for(Iterator iter=iter(); iter.hasNext();) 
		rtn.add(iter.next());
	    return rtn;
	}
    }
    static final class MoveList {
	final static class Cons { Move elem; Cons next; }
	Cons first;
	int size = 0;
	boolean isEmpty() {
	    return size == 0;
	}
	void add(Move m) {
	    Cons c = new Cons();
	    c.elem = m;
	    if (first == null) {
		first = c;
	    } else {
		c.next = first;
		first = c;
	    }
	    size++;
	}
	/** INVALIDATES 'l' */
	void append(MoveList l) {
	    Cons last = first;
	    while(last.next != null) 
		last = last.next;

	    last.next = l.first;
	    size += l.size;

	    l.first = null;
	    l.size = -1;
	}
	Iterator iterator() {
	    return new UnmodifiableIterator() {
		    Cons c = first;
		    public boolean hasNext() { return c != null; }
		    public Object next() { Move m=c.elem; c=c.next; return m;}
		};
	}
	public String toString() {
	    java.util.ArrayList l = new java.util.ArrayList();
	    for(Iterator m=iterator(); m.hasNext(); ){
		l.add(m.next());
	    }
	    return l.toString();
	}
    }

}

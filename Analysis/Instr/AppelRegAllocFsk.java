// AppelRegAllocStd.java, created  Sat Jul  7 16:36:00 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Code;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrGroup;

import harpoon.Analysis.BasicBlock;

import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.CombineIterator;
import harpoon.Util.Collections.ListFactory;

import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

public class AppelRegAllocFsk extends AppelRegAlloc {
    static RegAlloc.Factory FACTORY = new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new AppelRegAllocFsk(c);
	    }
	};

    // Instr i -> Move m s.t. m.instr = i
    private HashMap instrToMove = new HashMap();
    // Web w -> Node n s.t. n.web = w
    private HashMap webToNode = new HashMap();


    public AppelRegAllocFsk(Code code) { 
	super(code); 
    }

    protected void initializeSets() {
	super.initializeSets();
	webToNode.clear();
	instrToMove.clear();
    }

    protected Node makePrecolored(Temp t) {
	Util.ASSERT( rfi().isRegister(t) );
	Node n = super.makePrecolored(t);
	Util.ASSERT( n.isPrecolored() );
	webToNode.put(n.web, n );
	return n;
    }
    private int[] makeInitColors(int slots) {
	int[] a = new int[slots];
	for(int i=0; i<slots; i++) a[i] = -1;
	return a;
    }
    protected void makeInitial(Temp t)    { 
	Util.ASSERT( ! rfi().isRegister(t) );

	int slots = rfi().occupancy( t );
	for(Iterator webs=tempToWebs.getValues(t).iterator(); webs.hasNext();){
	    Web web = (Web) webs.next();
	    Node node = new Node(initial, web);
	    node.color = makeInitColors( slots );
	    node.degree = slots - 1; // don't start w/ degree of zero necessarily
	    checkNBRs(node);
	    webToNode.put( web, node );
	}
    }

    protected void checkDegreeInv() { return; }

    protected void buildInterferenceGraph() {
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

	// introduce interferences due to RegFileInfo.illegal(Temp)
	// (this code is unnecessarily ugly though)
	for(Iterator nodes=allNodes(); nodes.hasNext(); ){
	    Node n = (Node) nodes.next();
	    if (n.web != null) {
		Collection badRegsC = rfi().illegal(n.web.temp);
		Iterator badRegs = badRegsC.iterator();
		while(badRegs.hasNext()) {
		    Temp badReg = (Temp) badRegs.next();
		    Node regNode = (Node) webToNode.get(tempToWebs.get(badReg));
		    addEdge( n, regNode );
		}
	    }
	}
    }
    
    protected void addEdge( Node u, Node v ) {
	// FSK: (6/27/01) adding because having these edges is dumb.  
	// Shouldn't break anything.  And yet...
	if (u.isPrecolored() && v.isPrecolored()) {
	    return;
	}

	if( ! adjSet.contains(u,v) && ! u.equals(v) ){
	    adjSet.add(u,v);
	    adjSet.add(v,u);
	    
	    if( ! u.isPrecolored() ){
		Util.ASSERT(!rfi().isRegister(u.web.temp), u.web.temp);
		u.neighbors.add(v);
		u.degree += rfi().pressure( u.web.temp, v.web.temp );
		checkNBRs(u);
	    }
	    if( ! v.isPrecolored() ){
		Util.ASSERT(!rfi().isRegister(v.web.temp), v.web.temp);
		v.neighbors.add(u);
		v.degree += rfi().pressure( v.web.temp, u.web.temp );
		checkNBRs(v);
	    }
	}
    }
    void checkNBRs( Node n ) {
	// this wont hold once we support 0-pressure situations
	Util.ASSERT(n.degree >= visible(n.neighbors), n); 
    }
    int visible( NodeList nl ) {
	int c = 0;
	for(NodeIter ni=nl.iter(); ni.hasNext();){
	    Node n = ni.next();
	    if (! (n.isSpilled() || 
		   n.isCoalesced() ||
		   n.isSelectStack() ) ){
		c++;
	    }
	} 
	return c;
    }
    
    protected void decrementDegree( Node m, Node n ) {
	if (m.isPrecolored())
	    return;

	int d = m.degree;
	m.degree -= rfi().pressure( m.web.temp, n.web.temp );
	Util.ASSERT(m.degree >= 0);
	if( m.degree < K
	    
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
    
    protected boolean conservative(Node u, Node v) {
	return conservative(u.color.length - 1, adjacent(u), adjacent(v));
    }
    // returns conservative( ni1 \/ ni2 )
    // conservative coalescing heuristic due to Preston Briggs
    private boolean conservative(int extra, NodeIter ni1, NodeIter ni2) {
	NodeIter union; 
	// [ FIXED, but am seeing signs that "conservation" is being broken.
	//   could just be inherent heuristical effects though ]
	// FSK combine(...) is not a strict union.
	// union = combine( new NodeIter[]{ ni1, ni2 } );
	union = union(ni1, ni2);
	
	return conservative( extra, union );
    }
    private NodeIter union(NodeIter n1, NodeIter n2) {
	HashSet s = new HashSet();
	while(n1.hasNext()){ s.add(n1.next()); }
	while(n2.hasNext()){ s.add(n2.next()); }
	return nodesIter( s.iterator() );
    }

    private boolean conservative(int start, NodeIter nodes) { 
	int k = start;
	while( nodes.hasNext() ){
	    Node n = nodes.next();
	    if (n.degree >= K)
		k += rfi().occupancy(n.web.temp);
	}
	return k < K;
    }

    private int colorFor( Temp reg ){
	Node r = (Node) webToNode.get(tempToWebs.get(reg));
	return r.color[0];
    }
    protected void assignColors() { 
	Temp[] regs = colorToReg();
	HashSet regset = new HashSet(Arrays.asList( regs ));
	HashSet occupied = new HashSet();
	while( !select_stack.isEmpty() ){
	    Node n = select_stack.pop();
	    
	    occupied.clear();

	    for( NodeIter adj=n.neighbors.iter(); adj.hasNext(); ) {
		Node w = adj.next(); w = getAlias(w);
		if( w.isColored() || 
		    w.isPrecolored() ){
		    occupied.addAll( toRegList( w, regs ));
		}
	    }
	    
	    List regL = rfi().assignment( n.web.temp, occupied );
	    if (regL == null) {
		spilled_nodes.add(n);
	    } else {
		colored_nodes.add(n);
		for(int i=0; i<n.color.length; i++) {
		    Temp reg = (Temp) regL.get(i);
		    Util.ASSERT( ! rfi().illegal(n.web.temp).contains(reg) );
		    n.color[i] = colorFor( reg );
		}
	    }
	}

	for( NodeIter ni=coalesced_nodes.iter(); ni.hasNext(); ){
	    Node n = ni.next();
	    n.color = (int[]) getAlias(n).color.clone();
	}
    }

    protected void performFinalAssignment(Temp[] regs) {
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
		    Node n = (Node) webToNode.get(w);
		    List regList = toRegList( n, regs );
		    Util.ASSERT( ! regList.isEmpty() );
		    code.assignRegister( inst, t, regList );
		}
	    }
	}

	fixupSpillCode();

    }
    private List/*Reg*/ toRegList( Node n, Temp[] colorToReg ){
	Temp[] regs = new Temp[ n.color.length ];
	for(int i=0; i<regs.length; i++) {
	    regs[i] = colorToReg[ n.color[i] ];
	}
	return Arrays.asList( regs );
    }

    /** REQUIRES: i.isMove() 
	returns a Move corresponding to Instr i. 
    */ 
    private Move moveFor(Instr i) { 
	Util.ASSERT( i.isMove() ); 
 
	if( instrToMove.containsKey(i) ) { 
	    return (Move) instrToMove.get(i); 
	} else { 
	    // TODO: Fix to assign collections, not first elems! 
	    Util.ASSERT( defCN(i).size() == 1);
	    Util.ASSERT( useCN(i).size() == 1); 
	    Node dst = (Node) defCN(i).iterator().next(); 
	    Node src = (Node) useCN(i).iterator().next(); 
	    Move m = new Move(i, dst, src); 
	    instrToMove.put(i, m);
	    return m;
	}
    }

    protected List nodesFor( Web w ){
	return ListFactory.singleton( webToNode.get( w ));
    }
}

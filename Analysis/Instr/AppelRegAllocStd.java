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
import harpoon.Util.FilterIterator;
import harpoon.Util.CombineIterator;

import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

public class AppelRegAllocStd extends AppelRegAlloc {
    // Instr i -> Move m such m.instr = i
    private HashMap instrToMove = new HashMap();

    // Web w -> Listof Node
    private HashMap webToNodes = new HashMap();

    public AppelRegAllocStd(Code code) { 
	super(code); 
    }

    protected void initializeSets() {
	super.initializeSets();
	webToNodes.clear();
	instrToMove.clear();
    }

    protected List/*Node*/ nodesFor( Web web ) {
	if (CHECK_INV)
	Util.ASSERT( webToNodes.containsKey( web ),
		     "! should have nodes for "+web);
	return (List) webToNodes.get( web );
    }
    

    protected Node makePrecolored(Temp t) {
	Node n = super.makePrecolored(t);
	webToNodes.put(n.web, Arrays.asList( new Node[]{ n } ));
	return n;
    }

    protected void makeInitial(Temp t)    { 
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

    protected void checkDegreeInv() {
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
	    Util.ASSERT( ! outerSeen.contains(u), " already saw " + u);

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
	    Util.ASSERT(deg == 0, "degree inv. violated");
	}
    }

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
		u.neighbors.add(v);
		u.degree++;
	    }
	    if( ! v.isPrecolored() ){
		v.neighbors.add(u);
		v.degree++;
	    }
	}
    }

    protected void decrementDegree( Node m, Node n ) {
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
    
    protected boolean conservative(Node u, Node v) {
	return conservative(adjacent(u), adjacent(v));
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


    protected void assignColors() { 
	while( !select_stack.isEmpty() ){
	    Node n = select_stack.pop();
	    ColorSet okColors = new ColorSet(K);
	    for( NodeIter adj=n.neighbors.iter(); adj.hasNext(); ) {
		Node w = adj.next(); w = getAlias(w);
		if( w.isColored() || 
		    w.isPrecolored() ){
		    okColors.remove( w.color[0] );
		}
	    }
	    // TODO: Add code here to handle assigning a color to more
	    // than one node at once, ala Brigg's multigraphs... 
	    if( okColors.isEmpty() ){
		spilled_nodes.add(n);
	    } else {
		colored_nodes.add(n);
		n.color[0] = okColors.available();
	    }
	}

	for( NodeIter ni=coalesced_nodes.iter(); ni.hasNext(); ){
	    Node n = ni.next();
	    n.color[0] = getAlias(n).color[0];
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
		    List nodes = nodesFor( w );
		    List regList = toRegList( nodes, regs );
		    Util.ASSERT( ! regList.isEmpty() );
		    code.assignRegister( inst, t, regList );
		}
	    }
	}

	fixupSpillCode();

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

    private List toRegList(List nodes, Temp[] colorToReg) {
	Temp[] regs = new Temp[nodes.size()];
	for(int i=0; i<regs.length; i++) {
	    Node n = (Node)nodes.get(i);
	    if (! (0 <= n.color[0] && n.color[0] < colorToReg.length) ) {
		code.printPreallocatedCode();
		// printAllColors();
		System.out.println();
		System.out.println("node:"+n+" history:"+n.nodeSet_history);
		Util.ASSERT(false,
			    "node:"+n+" should have valid color, not:"+n.color[0]);
	    }
	    regs[i] = colorToReg[ n.color[0] ];
	}
	return Arrays.asList( regs );
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
		for(int i=0; i<node.color.length; i++) {
		    accum += node.color[i] + ":";
		}
		if (nI.hasNext()) {
		    accum += ", ";
		}
	    }
	    accum += "]";
	    System.out.println(accum);
	}
    }

    static class ColorSet {
	boolean[] colors;
	int size;
	public ColorSet(int numColors) {
	    size = numColors;
	    colors = new boolean[numColors];
	    for(int i=0; i < colors.length; i++) {
		colors[i] = true;
	    }
	}
	/** requires: ! this.isEmpty() */
	public int available() {
	    for(int i=0; i<colors.length; i++) {
		if (colors[i]) 
		    return i;
	    }
	    throw new RuntimeException();
	}
	public void remove(int color) {
	    if (color < colors.length) {
		if ( colors[ color ] )
		    size--;
		colors[ color ] = false;
	    } else {
		// removing an unused register-only color; NOP
	    }
	}
	public boolean isEmpty() { return size == 0; }
    }

}

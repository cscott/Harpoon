// GraphColoringRegAlloc.java, created Mon Jul 17 16:39:13 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Maps.BackendDerivation;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.DataFlow.SpaceHeavyLiveTemps;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.GraphColoring.AbstractGraph;
import harpoon.Analysis.GraphColoring.ColorableGraph;
import harpoon.Analysis.GraphColoring.Color;
import harpoon.Analysis.GraphColoring.GraphColorer;
import harpoon.Analysis.GraphColoring.OptimisticGraphColorer;
import harpoon.Analysis.GraphColoring.SimpleGraphColorer;
import harpoon.Analysis.GraphColoring.UnableToColorGraph;
import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.CombineIterator;
import harpoon.Util.FilterIterator;
import harpoon.Util.Default;
import harpoon.Util.ArraySet;
import harpoon.Util.Collections.ListFactory;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.InvertibleMap;
import harpoon.Util.Collections.GenericInvertibleMap;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.List;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Date;

/**
 * <code>GraphColoringRegAlloc</code> uses graph coloring heuristics
 * to find a register assignment for a Code.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GraphColoringRegAlloc.java,v 1.3.2.1 2002-02-27 08:31:21 cananian Exp $
 */
public class GraphColoringRegAlloc extends RegAlloc {

    // Information output control flags
    private static final boolean TIME = false;
    private static final boolean RESULTS = false;
    private static final boolean SPILL_INFO = false;
    private static final boolean STATS = false;
    private static final boolean COALESCE_STATS = false;
    private static final boolean SCARY_OUTPUT = false;
    private static final boolean UNIFY_INFO = false;

    // Code output control flags
    private static final boolean DEF_COALESCE_MOVES = true;
    private static final boolean COALESCE_MACH_REGS = false;

    private boolean COALESCE_MOVES = DEF_COALESCE_MOVES;

    static RegAlloc.Factory BRAINDEAD_FACTORY = 
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		GraphColorer gc;
		
		// gc = new SimpleGraphColorer();

		// TODO: Implement a Node Selector that will not
		// select nodes that have already been spilled in the
		// code, and pass it in here...
		NodeSelector ns = new NodeSelector();
		gc = new OptimisticGraphColorer(ns);
		ns.gcra = new GraphColoringRegAlloc(c, gc, true);
		ns.gcra.COALESCE_MOVES = false;
		return ns.gcra;
	    }
	    };

    static RegAlloc.Factory AGGRESSIVE_FACTORY = 
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		GraphColorer gc;

		// gc = new SimpleGraphColorer();

		// TODO: Implement a Node Selector that will not
		// select nodes that have already been spilled in the
		// code, and pass it in here...
		NodeSelector ns = new NodeSelector();
		gc = new OptimisticGraphColorer(ns);
		ns.gcra = new GraphColoringRegAlloc(c, gc, true);
		
		return ns.gcra;
	    }
	};

    public static RegAlloc.Factory FACTORY =
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		GraphColorer gc;

		// gc = new SimpleGraphColorer();

		// TODO: Implement a Node Selector that will not
		// select nodes that have already been spilled in the
		// code, and pass it in here...
		NodeSelector ns = new NodeSelector();
		gc = new OptimisticGraphColorer(ns);
		ns.gcra = new GraphColoringRegAlloc(c, gc);
		
		return ns.gcra;
	    }
	};

    static class NodeSelector extends OptimisticGraphColorer.SimpleSelector {
	GraphColoringRegAlloc gcra;
	public boolean allowedToRemove(Object n, ColorableGraph g) {
	    return gcra.isAvailableToSpill( n );
	}
	public Object chooseNodeForHiding(ColorableGraph g) {
	    HashSet l = new HashSet();
	    Object n = super.chooseNodeForHiding(g);
	    while (!gcra.isAvailableToSpill(n)) {
		// delay hiding spilled nodes as long as possible
		// System.out.println("DELAY "+n);
		g.hide(n);
		l.add(n);
		Object _n = super.chooseNodeForHiding(g);
		if (_n == null) 
		    break;
		else 
		    n = _n;
	    }
	    Object _n; if (!l.isEmpty()) do {
		_n = g.replace();
	    } while(l.contains(_n));
	    //System.out.println("HIDE:"+n);
	    return n;
	}
	
	public Object chooseNodeForRemoval(ColorableGraph g) {
	    Object spillChoice = null;
	    Set nset = g.nodeSet();
	    int maxDegree = -1;
	    for(Iterator ns=nset.iterator(); ns.hasNext(); ) {
		Object n = ns.next();
		if (g.getColor(n) == null &&
		    g.getDegree(n) > maxDegree) {
		    if (gcra.isAvailableToSpill(n)) { // <= THIS IS KEY
			spillChoice = n;
			maxDegree = g.getDegree(n);
		    } 
		}
	    }
	    if(spillChoice == null) {
		// backup strategy : spill COLORED nodes
		for(Iterator ns=nset.iterator(); ns.hasNext(); ) {
		    Object n = ns.next();
		    if (g.getDegree(n) > maxDegree) {
			if (gcra.isAvailableToSpill(n)) { // <= THIS IS KEY
			    spillChoice = n;
			    maxDegree = g.getDegree(n);
			} else {
			    if (SCARY_OUTPUT) System.out.println
				("SKIP (2) "+n+" (already spilled)");
			}
		    }
		}
	    }
	    if(spillChoice == null) {
		// backup strategy : spill HIDDEN node
		LinkedList rehide = new LinkedList();
		for(Object n=g.replace(); n!=null; n=g.replace()) {
		    rehide.addLast(n);
		    if (g.getDegree(n) > maxDegree) {
			if (gcra.isAvailableToSpill(n)) {
			    spillChoice = n;
			    maxDegree = g.getDegree(n);
			} else {
			    if (SCARY_OUTPUT) System.out.println
				("SKIP (3) "+n+" (already spilled)");
			}
		    }
		}
		while(!rehide.isEmpty()) {
		    g.hide(rehide.removeLast());
		}
	    }
	    if(spillChoice == null) {
		// wtf?  Are ALL the nodes spilled?
		for(Iterator ns=g.nodeSet().iterator();ns.hasNext();){
		    Object n = ns.next();
		    assert !gcra.isAvailableToSpill(n);
		}
		LinkedList rehide = new LinkedList();
		for(Object n=g.replace(); n!=null; n=g.replace()) {
		    rehide.addLast(n);
		    assert !gcra.isAvailableToSpill(n);
		}
		while(!rehide.isEmpty()) {
		    g.hide(rehide.removeLast());
		}
	    }
	    //System.out.println("SPILL:"+spillChoice);
	    return spillChoice;
	}
    }

    private static final int INITIAL_DISPLACEMENT = 0;

    double defWt, useWt, copyWt;
    int baseReg;
    int disp = INITIAL_DISPLACEMENT, argReg;
    List stack; // List<Integer>
    Map realReg; // Integer -> Integer

    final RegFileInfo rfi;
    ReachingDefs rdefs;
    LiveTemps liveTemps;

    // TYPES IN COMMENTS
    // Reg    : All r in Temp such that isRegister(r)
    // VReg   : Temp - Reg
    // AReg   : All r such that Exists some t in Temp such that
    //                rfi.getRegAssignments(t) contains some
    //                List<Reg>, l, such that r is in l. 
    // Assign : List<AReg>
    // SysReg : Reg - AReg

    MultiMap regToDefs; // Reg -> Instr
    MultiMap regToUses; // Reg -> Instr

    // used to associate a VReg with the available register
    // assignments for its components. 
    // VReg maps to an array of Maps, where the array-indices
    // correspond to component indices in the assignment.
    Map implicitAssigns; // VReg -> (AReg -> Assign)[]


    InvertibleMap webPrecolor; // WebRecord -> AReg
    Map regToColor;  // AReg -> RegColor
    Map regToWeb; // AReg -> RegWebRecord

    Map ixtToWebPreCombine; // Instr x VReg -> WebRecord    
    Map ixtToWeb; // Instr x VReg -> WebRecord
    List webRecords; // List<WebRecord>

    List tempWebRecords; // List<TempWebRecord>
    List regWebRecords; // List<RegWebRecord>
    
    GraphColorer colorer;

    private boolean aggressivelyCoalesce;

    private WebRecord getWR(Instr i, Temp t) {
	if (isRegister(t)) {
	    for(Iterator ri=regWebRecords.iterator();ri.hasNext();){ 
		WebRecord r = (WebRecord)ri.next();
		if (r.temp().equals(t)) 
		    return r;
	    }
	    return null;
	} else {
	    return (WebRecord) ixtToWeb.get(Default.pair(i,t)); 
	}
    }

    /** Creates a <code>GraphColoringRegAlloc</code>, assigning `gc'
	as its graph coloring strategy and using a overly conservative
	move coalescing strategy. 
    */
    public GraphColoringRegAlloc(Code code, GraphColorer gc) {
        this(code, gc, false);
    }

    /** Creates a <code>GraphColoringRegAlloc</code>, assigning `gc'
	as its graph coloring strategy.  If
	<code>aggressiveCoalesce</code> is true, will choose to
	coalesce moves in the face of increased memory traffic. 
    */
    public GraphColoringRegAlloc(Code code, GraphColorer gc, 
				 boolean aggressiveCoalesce) { 
        super(code);
	rfi = frame.getRegFileInfo();
        colorer = gc;
	aggressivelyCoalesce = aggressiveCoalesce;
    }

    private LinkedList replOrigPairs = new LinkedList(); 
    protected void replace(Instr orig, Instr repl) {
	super.replace(orig, repl);
	replOrigPairs.addFirst(Default.pair(repl, orig));
    }
    private void undoCoalescing() {
	if (SCARY_OUTPUT) System.out.print(" UNDO");
	for(Iterator prs=replOrigPairs.iterator();prs.hasNext();){
	    List pr = (List) prs.next();
	    Instr repl = (Instr) pr.get(0);
	    Instr orig = (Instr) pr.get(1);
	    // System.out.println("UNDO: replacing "+repl+" w/ "+orig);
	    // System.out.println("==:"+(repl==orig)+" equls:"+repl.equals(orig));
	    Instr.replace(repl, orig);
	}
	replOrigPairs.clear();
	willRemoveLater.clear();
	webPrecolor.clear();
	// remap = EqTempSets.make(this, false);
	remap = new EqWebRecords();
    }

    protected Derivation getDerivation() {
	final Derivation oldD = code.getDerivation();
	return new BackendDerivation() {
	    private HCodeElement orig(HCodeElement h){
		return getBack((Instr)h);
	    }
	    private Temp orig(HCodeElement h, Temp t) {
		assert false;
		Temp t2 = null;
		Instr inst = (Instr) orig(h);
		Iterator rs = new
		    CombineIterator(inst.defC().iterator(),
				    inst.useC().iterator());
		while(rs.hasNext()) {
		    t2 = (Temp) rs.next();
		    // if (remap.tempMap(t2).equals(t)) 
			break;
		}
		assert t2 != null;
		return t2;
	    }
	    public HClass typeMap(HCodeElement hce, Temp t) {
		HCodeElement hce2 = orig(hce); 
		Temp t2 = orig(hce, t);
		return oldD.typeMap(hce2, t2);
	    }
	    public Derivation.DList derivation(HCodeElement hce, Temp t) {
		HCodeElement hce2 = orig(hce); 
		Temp t2 = orig(hce, t);
		try {
		    return Derivation.DList.rename
			(oldD.derivation(hce2, t2), null); // remap);
		} catch (TypeNotKnownException e) {
		    System.out.println("called derivation("+hce+","+t+")");
		    System.out.println("die on derivation("+hce2+","+t2+")");
		    throw e;
		}
	    }
	    public BackendDerivation.Register
		calleeSaveRegister(HCodeElement hce, Temp t) { 
		hce = orig(hce);
		return ((BackendDerivation)oldD).calleeSaveRegister(hce, t);
	    }
	};
    }

    /** Returns a new Collection cr, where
	for all e element of edges 
	   there exists a unique c element of cr such that 
	   c = [nodeToNum(e.get(0)), nodeToNum(e.get(1))]
    */
    private static Collection readableEdges(final Collection edges, 
					    final Map nodeToNum) {
	return new AbstractCollection() {
	    public int size() { return edges.size(); }
	    public Iterator iterator() { 
		final int sz = size();
		return new FilterIterator 
		    (edges.iterator(),
		     new FilterIterator.Filter() {
			public Object map(Object o) {
			    List l=(List)o;
			    return Default.pair(nodeToNum.get(l.get(0)),
						nodeToNum.get(l.get(1)));
			}});
	    }
	};
    }
    private static Collection readableNodes(final Collection nodes,
					    final Map nodeToNum) {
	return new AbstractCollection() {
	    public int size() { return nodes.size(); }
	    public Iterator iterator() {
		return new FilterIterator
		    (nodes.iterator(),
		     new FilterIterator.Filter() {
			public Object map(Object o) {
			    return nodeToNum.get(o);
			}
		    });
	    }
	};
    }


    protected void generateRegAssignment() {
	boolean success, coalesced;
	AdjMtx adjMtx = null;
	
	boolean doCoalescing = COALESCE_MOVES; // HACK
	
	if (TIME) System.out.println();


	do {
	    do {
		buildRegAssigns();
		if (TIME) System.out.println("ReachingDefs \t\t"+new Date());
		rdefs = new ReachingDefsAltImpl(code);
		if (TIME) System.out.println("LiveTemps \t\t"+new Date());
		liveTemps = SpaceHeavyLiveTemps.make(code, rfi.liveOnExit());
		ixtToWeb = new HashMap();
		ixtToWebPreCombine = new HashMap();
		webPrecolor = new GenericInvertibleMap();

		if (TIME) System.out.println("Making Webs \t\t"+new Date());

		makeWebs(rdefs); 

		// ASSERTION CHECKING LOOPS
		for(int k=0; k<webRecords.size(); k++) {
		    assert ((WebRecord)webRecords.get(k)).sreg() == k;
		}
		for(Iterator is=code.getElementsI(); is.hasNext();){
		    Instr i = (Instr) is.next();
		    
		    for(Iterator ts=i.defC().iterator(); ts.hasNext();) {
			Temp t = (Temp) ts.next();
			if (! isRegister(t) ) {
			    List ixt = Default.pair(i,t);
			    WebRecord web = (WebRecord)ixtToWeb.get(ixt);
			    if (web == null) {
				assert ixtToWebPreCombine.get(ixt)==null : "There was a web for "+ixt+
					    " pre-combination! "+
					    ixtToWebPreCombine.get(ixt);
				assert false : ("no web for i:"+i+", t:"+t);
			    }
			}
		    }
		    for(Iterator ts=i.useC().iterator(); ts.hasNext();) {
			Temp t = (Temp) ts.next();
			if (! isRegister(t) ) {
			    WebRecord web = (WebRecord) 
				ixtToWeb.get(Default.pair(i,t));
			    assert web != null : ("no web for i:"+i+", t:"+t);
			}
		    }
		} 
		// END ASSERTION CHECKING LOOPS
		
		// System.out.println("webs: "+webRecords);

		if(TIME)System.out.println("Building Matrix \t"+new Date());

		
		adjMtx = buildAdjMatrix();
		
		if(TIME)printConflictTime();

		if(TIME)System.out.println("Adjacency Matrix Built \t"+new Date());
		// System.out.println(adjMtx);
		if (doCoalescing) {
		    if(TIME)System.out.println("Coalescing Registers \t"+new Date());
		    Set coal = coalesceRegs(adjMtx);
		    coalesced = !coal.isEmpty();
		} else {
		    coalesced = false;
		}
		if(TIME)printConflictTime();
	    } while (coalesced);

	    if(TIME)System.out.println("Building Lists \t\t"+new Date());

	    WebRecord[] adjLsts = buildAdjLists(adjMtx); 

	    adjMtx = null;

	    // STAT GATHERING LOOPS
	    Iterator wri;
	    StatGather regStat = new StatGather();
	    for(wri=regWebRecords.iterator(); wri.hasNext();) {
		RegWebRecord rwr = (RegWebRecord) wri.next();
		int deg = rwr.adjnds.size();
		regStat.add(deg);
	    }
	    StatGather tmpStat = new StatGather();
	    for(wri=tempWebRecords.iterator(); wri.hasNext();) {
		TempWebRecord twr = (TempWebRecord) wri.next();
		int deg = twr.adjnds.size();
		tmpStat.add(deg);
	    }
	    if(STATS)System.out.println("\nRegDeg"+regStat + " TmpDeg"+tmpStat );
	    // END STAT GATHERING LOOPS

	    // System.out.println(Arrays.asList(adjLsts));

	    computeSpillCosts();

	    if(TIME)System.out.println("Building Graph \t\t"+new Date());

	    final Graph graph = buildGraph(adjLsts);

	    Map nodeToNum = printGraph(graph, RESULTS);
	    
	    if(STATS)System.out.println(" |g.V|:"+graph.nodeSet().size() + 
		  " |g.E|:"+graph.edges().size());

	    try {
		List colors = new ArrayList(regToColor.values());
		// System.out.println("colors:"+colors);
		colorer.color(graph, colors);
		
		for(int j=0; j<adjLsts.length; j++) {
		    WebRecord wr = adjLsts[j];

		    if (isRegister(wr.temp())) continue;

		    Iterator wrs;
		    for(wrs=wr.adjnds.iterator(); wrs.hasNext();){
			WebRecord nb = (WebRecord) wrs.next();
			HashSet nbl = new HashSet(graph.regs(nb));

			assert !nbl.isEmpty() : "no regs for "+nb;
			assert !graph.regs(wr).isEmpty() : "no regs for "+wr;

			nbl.retainAll(graph.regs(wr));


			assert nbl.isEmpty() : "conflict detected: "+
				    wr+"("+graph.regs(wr)+
				    ",precol:"+webPrecolor.containsKey(wr)+
				    ")"+
				    
				    " and "+
				    nb+"("+graph.regs(nb)+
				    ",precol:"+webPrecolor.containsKey(wr)+
				    ")"+
				    "";
			
			assert rfi.getRegAssignments(wr.temp()).
				    contains(graph.regs(wr)) : rfi.getRegAssignments(wr.temp())+
				    " does not contain "+graph.regs(wr);
							  
		    }
		}

		MultiMap c2n = new GenericMultiMap();
		for(Iterator nds=graph.nodeSet().iterator();nds.hasNext();){
		    Object nd = nds.next();
		    c2n.add(graph.getColor(nd), nd);
		}
		
		if (RESULTS) 
		for(Iterator cs=c2n.keySet().iterator();cs.hasNext();){ 
		    Object col=cs.next();
		    System.out.println(col + " nodes: "+
			    readableNodes(c2n.getValues(col),nodeToNum));
		}
		
		modifyCode(graph);

		// OptimisticGraphColorer.MONITOR = false;
		success = true;
	    } catch (UnableToColorGraph u) {
		// OptimisticGraphColorer.MONITOR = true;
		success = false;
		if (!aggressivelyCoalesce && doCoalescing) {
		    doCoalescing = false;
		    undoCoalescing();
		} else {
		    // doCoalescing = true;
		    genSpillCode(u, graph);
		}
	    }
	} while (!success);

	fixupSpillCode();
	for(Iterator is=willRemoveLater.iterator(); is.hasNext();){
	    Instr i = (Instr) is.next();
	    Temp sym;
	    if (isRegister(i.def()[0])) {
		sym = i.use()[0];
	    } else { 
		sym = i.def()[0];
	    }
	    InstrMOVEproxy proxy = new InstrMOVEproxy(i);	    
	    replace(i, proxy);
	    code.assignRegister(proxy, sym, code.getRegisters(i, sym));
	}
    }

    // sets regToColor, regToDefs, regToUses, implicitAssigns
    private void buildRegAssigns() {
	HashSet assigns; // Set<Assign>
	HashMap asnSetToImplc; // Set<Assign> -> Reg -> Assign
	
	// local vars
	assigns = new HashSet();
	asnSetToImplc = new HashMap();

	// global vars
	implicitAssigns = new HashMap();
	regToColor = new HashMap();
	regToDefs = new GenericMultiMap();
	regToUses = new GenericMultiMap();
	
	for(Iterator instrs=code.getElementsI(); instrs.hasNext();){
	    Instr inst = (Instr) instrs.next();
	    Iterator tmps = new CombineIterator(inst.defC().iterator(),
						inst.useC().iterator());
	    while(tmps.hasNext()) {
		Temp t = (Temp) tmps.next();
		if (rfi.isRegister(t)) {
		    if (inst.defC().contains(t)) regToDefs.add(t, inst);
		    if (inst.useC().contains(t)) regToUses.add(t, inst);
		    
		    // do not make a color for `t' here; otherwise
		    // system registers (PC, SP, etc) get colors 
		    continue;
		} 
		
		Set suggRegs = rfi.getRegAssignments(t);
		assigns.addAll(suggRegs);
		
		// incorporate into regToColor
		for(Iterator s=suggRegs.iterator(); s.hasNext();){
		    List rL = (List) s.next();
		    for(Iterator rs=rL.iterator();rs.hasNext();){
			Temp reg = (Temp) rs.next();
			regToColor(reg); 
		    }
		}
		
		if (!implicitAssigns.keySet().contains(t)) {
		    if (!asnSetToImplc.keySet().contains(suggRegs)) {
			// build (Reg -> Assign)[] for suggRegs
			Map[] i2r2a = null;
			for(Iterator s=suggRegs.iterator();s.hasNext();){
			    List asn = (List) s.next();
			    if (i2r2a == null) {
				i2r2a = new Map[asn.size()]; 
				for(int j=0; j<i2r2a.length; j++) {
				    i2r2a[j] = new HashMap(10);
				}
			    }
			    for(int i=0; i<asn.size(); i++) {
				Temp reg = (Temp) asn.get(i);
				i2r2a[i].put(reg, asn);
			    }
			}
			asnSetToImplc.put(suggRegs, i2r2a);
		    }

		    // add to implicitAssigns
		    Object i2r2a = asnSetToImplc.get(suggRegs);
		    implicitAssigns.put(t, i2r2a);
		}
	    }
	}
    }
    
    private RegColor regToColor(Temp reg) {
	RegColor c = (RegColor) regToColor.get(reg);
	if (c == null) {
	    c = new RegColor(reg);
	    regToColor.put(reg, c);
	}
	return c;
    }

    class RegColor extends Color {
	final Temp reg;
	RegColor(Temp r) {
	    this.reg = r;
	}
	public String toString() { 
	    return "c:"+reg;
	}
	public boolean equals(Object o) {
	    return ((RegColor)o).reg.equals(this.reg);
	}
    }
    
    /**
       nwebs is set after this method returns.
       regWebRecords, tempWebRecords, and webRecords are set
       after this method returns.
     */
    private void makeWebs(ReachingDefs rdefs) {
	Set webSet = new HashSet(), tmp1; // Set<TempWebRecord>
	TempWebRecord web1, web2;
	List sd; // [Temp, Def]
	int i, oldnwebs;
	
	for(Iterator instrs = code.getElementsI();instrs.hasNext();){ 
	    Instr inst = (Instr) instrs.next();
	    for(Iterator uses = inst.useC().iterator(); uses.hasNext();){ 
		Temp t = (Temp) uses.next();

		if (isRegister(t)) continue;

		TempWebRecord web = 
		    new TempWebRecord
		    (t, new LinearSet(rdefs.reachingDefs(inst,t)),
		     new LinearSet(Collections.singleton(inst)));
		webSet.add(web);

		List ixt = Default.pair(inst, t);
		if (!ixtToWebPreCombine.keySet().contains(ixt)) {
		    ixtToWebPreCombine.put(ixt, web);
		}
	    }

	    // in practice, the remainder of the web building should
	    // work w/o the following loop.  but if there is a def w/o
	    // a corresponding use, the system breaks.

	    for(Iterator defs=inst.defC().iterator();defs.hasNext();){
		Temp t = (Temp) defs.next();
		
		if (isRegister(t)) continue;

		// FSK: shouldn't this be kept in the else-block
		// alone?  Leaving alone for now... (XP style)
		TempWebRecord web =
		    new TempWebRecord
		    (t, new LinearSet(Collections.singleton(inst)),
		     new LinearSet(Collections.EMPTY_SET));
		webSet.add(web);
		
		List ixt = Default.pair(inst, t);
		if (ixtToWebPreCombine.keySet().contains(ixt)) {
		    TempWebRecord wr = (TempWebRecord) 
			ixtToWebPreCombine.get(ixt);
		    wr.defs.add(inst);
		} else {

		    ixtToWebPreCombine.put(ixt, web);
		}
	    }
	}

	// System.out.println("pre-duchain-combination");
	// System.out.println("webSet: "+webSet);

	boolean changed; 
	do {
	    // combine du-chains for the same symbol and that have a
	    // def in common to make webs  
	    changed = false;
	    tmp1 = new HashSet(webSet);

	    while(!tmp1.isEmpty()) {
		web1 = (TempWebRecord) tmp1.iterator().next();
		tmp1.remove(web1);

		// put webs to be removed post-iteration here
		HashSet removeFromTmp1 = new HashSet(); 

		for(Iterator t2s=tmp1.iterator(); t2s.hasNext(); ){
		    web2 = (TempWebRecord) t2s.next();
		    
		    if (web1.sym.equals(web2.sym)) {
			boolean combineWebs;
			Set ns = new HashSet(web1.defs);
			ns.retainAll(web2.defs);
			combineWebs = !ns.isEmpty();
			
			if (!combineWebs) {
			    // IMPORTANT: current temp->reg assignment
			    // design breaks if an instr needs two
			    // different regs for the same temp in the
			    // uses and defines.  Take this clause out
			    // after that is fixed. 
			    Set s1 = new HashSet(web1.defs);
			    s1.retainAll(web2.uses);
			    
			    Set s2 = new HashSet(web2.defs);
			    s2.retainAll(web1.uses);
			    combineWebs = (!s1.isEmpty() || !s2.isEmpty());
			}
			
			if (false) System.out.println
				       (combineWebs?
					"combining "+web1+" & "+web2:
					"NOT combining "+web1+" & "+web2);
			
			if (combineWebs) {
			    web1.defs.addAll(web2.defs);
			    web1.uses.addAll(web2.uses);
			    webSet.remove(web2);
			    removeFromTmp1.add(web2);
			    changed = true;
			}
		    }
		}
		
		tmp1.removeAll(removeFromTmp1);

	    }
	} while ( changed );
	
	// System.out.println("post-duchain-combination");
	// System.out.println("webSet: "+webSet);
	
	
	regWebRecords = new ArrayList(regToColor.keySet().size());
	
	regToWeb = new HashMap();
	Iterator rs = regToColor.keySet().iterator();
	for(i=0; rs.hasNext(); i++) {
	    Temp reg = (Temp) rs.next();
	    WebRecord w = new RegWebRecord(reg);
	    w.sreg(i);
	    regWebRecords.add(w);
	    regToWeb.put(reg, w);
	}
	tempWebRecords = new ArrayList(webSet.size());
	for(Iterator webs = webSet.iterator(); webs.hasNext(); i++) {
	    WebRecord w = (TempWebRecord) webs.next();
	    w.sreg(i);
	    tempWebRecords.add(w);
	}
	webRecords = ListFactory.concatenate
	    (Default.pair(regWebRecords, tempWebRecords));

	for(Iterator webs = tempWebRecords.iterator(); webs.hasNext(); ){
	    WebRecord wr = (WebRecord) webs.next();
	    Temp t = wr.temp();

	    Iterator is = new
		CombineIterator(wr.defs().iterator(),
				wr.uses().iterator()); 
	    while( is.hasNext() ) {
		Instr inst = (Instr) is.next();
		List ixt = Default.pair(inst,t);
		WebRecord prior = (WebRecord) ixtToWeb.get(ixt);
		assert prior == null || prior == wr;
		ixtToWeb.put(ixt, wr);
	    }
	}

	// ASSERTION EQUALITY CHECK (with feedback info)
	if(!ixtToWeb.keySet().equals(ixtToWebPreCombine.keySet())) {
	    HashSet postMinusPre = new HashSet(ixtToWeb.keySet());
	    HashSet preMinusPost = new HashSet(ixtToWebPreCombine.keySet());
	    postMinusPre.removeAll(ixtToWebPreCombine.keySet());
	    preMinusPost.removeAll(ixtToWeb.keySet());
	    
	    System.out.println("PRE - POST: " + preMinusPost);
	    System.out.println();
	    System.out.println("POST - PRE: " + postMinusPre);
	    System.out.println();
	    assert false;
	}
    }
    
    private AdjMtx buildAdjMatrix() { 
	HashSet visited = new HashSet();

	AdjMtx adjMtx = new AdjMtx(webRecords);
	int i, j;
	int sz = webRecords.size();

	for(i=0; i<sz; i++) {
	    WebRecord wr1 = (WebRecord) webRecords.get(i);
	    visited.add(wr1);
	    
	    for(j=i+1; j<sz; j++) {
		WebRecord wr2 = (WebRecord) webRecords.get(j);
		
		adjMtx.set(wr1.sreg(),wr2.sreg(),wr1.conflictsWith(wr2));
	    }
	}

	return adjMtx;
    }

    Set willRemoveLater = new HashSet(); // Set<Instr>
    // EqTempSets remap = EqTempSets.make(this, false);
    EqWebRecords remap = new EqWebRecords();

    /** returns the set of removed Instrs. */
    private Set coalesceRegs(AdjMtx adjMtx) { 
	// RESET DATA STRUCTURES
	HashSet willRemoveNow = new HashSet(); // Set<Instr>
	willRemoveLater.clear();
	webPrecolor.clear();
	// remap = EqTempSets.make(this, false);
	remap = new EqWebRecords();

	// ANALYZE
	for(Iterator is=code.getElementsI(); is.hasNext();) {
	    Instr i = (Instr) is.next();

	    // System.out.println("Instr: "+i);

	    if (i instanceof harpoon.IR.Assem.InstrMOVE) {
		Temp use = i.use()[0];
		Temp def = i.def()[0];
		WebRecord wUse = getWR(i, use);
		WebRecord wDef = getWR(i, def);

		// this assertion seems like it should hold, but
 		// doesn't.  Need to review conflictsWith code and
 		// change to update adjMtx accordingly
 		if (false) 
 		    assert wUse.conflictsWith(wDef) ==
 				 adjMtx.get(wUse.sreg(), wDef.sreg()) : " conflictsWith:"+wUse.conflictsWith(wDef)+
 				 " adjMtx.get:"+adjMtx.get(wUse.sreg(), 
 							   wDef.sreg());
		
		// if (adjMtx.get(wUse.sreg(), wDef.sreg())) {
		// if (wUse.conflictsWith(wDef)) {
		
		long start_time = System.currentTimeMillis();

		boolean remapConflicting = remap.conflicting(wDef, wUse);

		if (false)System.out.println("remapConflict compute time:"+
				   (System.currentTimeMillis() - start_time));

		if (remapConflicting) {

		} else {
		    if (def.equals(use)) {
			// (nothing special to update)
			willRemoveNow.add(i);
		    } else if (isRegister(def)) {
			if (!COALESCE_MACH_REGS) continue; // FSK investigated scalability problems

			//if (webPrecolor.containsKey(wUse)) continue;
			if (remap.anyConflicting
			    (wUse,webPrecolor.invert().getValues(def))){
			    continue;
			}
			webPrecolor.put(wUse, def);
			willRemoveLater.add(i);
		    } else if (isRegister(use)) {
			if (!COALESCE_MACH_REGS) continue; // FSK investigated scalability problems

			// if (webPrecolor.containsKey(wUse)) continue;
			if (remap.anyConflicting
			    (wDef,webPrecolor.invert().getValues(use))){
			    continue;
			}
			webPrecolor.put(wDef, use);
			willRemoveLater.add(i);
		    } else {
			assert !wDef.equals(wUse);
			remap.union(wDef, wUse);
			willRemoveNow.add(i);
		    }
		}
	    }
	}

	// TRANSFORM
	if (!willRemoveNow.isEmpty()) {
	    for(Iterator is=code.getElementsI(); is.hasNext();) {
		Instr i= (Instr) is.next();
		if (willRemoveNow.contains(i)) {
		    replace(i, new InstrMOVEproxy(remap.rename(i)));
		} else {
		    Iterator itr = new
			CombineIterator(i.useC().iterator(),
					i.defC().iterator());
		    while(itr.hasNext()) {
			Temp t = (Temp) itr.next();
			
			//if (!remap.tempMap(t).equals(t)) {
			if (remap.containsTemp(t)) {
			    Instr repl = remap.rename(i);
			    replace(i, repl);
			    if (willRemoveLater.contains(i)) {
				willRemoveLater.remove(i);
				willRemoveLater.add(repl);
			    }
			    break;
			}
		    }
		}
	    }
	    if (COALESCE_STATS)
		System.out.print("R:"+ willRemoveNow.size()   );
	} else if (!willRemoveLater.isEmpty()) {
	    if (COALESCE_STATS)
		System.out.print("R:"+ willRemoveLater.size() );
	}
	return willRemoveNow;
    }
    
    private WebRecord[] buildAdjLists(AdjMtx adjMtx) { 
	int i, j;
	final int nwebs = webRecords.size();
	final WebRecord[] adjLsts = new WebRecord[nwebs];
	for(i=0; i<regWebRecords.size(); i++) {
	    adjLsts[i] = (WebRecord) regWebRecords.get(i);
	    adjLsts[i].spcost =  Double.POSITIVE_INFINITY;
	}
	int offset = regWebRecords.size();
	for(i=0; i<tempWebRecords.size(); i++) {
	    adjLsts[offset+i]= (WebRecord) tempWebRecords.get(i);
	}
	for(i=1; i < nwebs; i++) {
	    for(j=0; j < i; j++) {
		WebRecord wr1, wr2;
		wr1 = adjLsts[i];
		wr2 = adjLsts[j];
		
 		//FSK: using this test breaks (i suspect due to
 		//FSK: modifying instr-sequence concurrently with
 		//FSK: analysis) 
		// if (wr1.conflictsWith(wr2)) {		
		if (adjMtx.get(wr1.sreg(),wr2.sreg())) {

		    wr1.adjnds.add(wr2);
		    wr2.adjnds.add(wr1);
		    wr1.nints++;
		    wr2.nints++;
		}
	    }
	}
	return adjLsts;
    }

    private void computeSpillCosts() { 
	
    }

    private Graph buildGraph(WebRecord[] adjLsts) {
	Graph g = new Graph();
	for(int i=0; i<adjLsts.length; i++) {
	    g.add(adjLsts[i]);
	}
	return g;
    }

    private Set defRegSet( Instr i ){
	LinearSet regs = new LinearSet();
	for(Iterator dts=i.defC().iterator(); dts.hasNext();){
	    Temp d = (Temp) dts.next();
	    if (code.registerAssigned(i,d))
		regs.addAll( code.getRegisters(i,d) );
	}
	return regs;
    }

    private void modifyCode(Graph g) { 
	for(Iterator wrs = tempWebRecords.iterator(); wrs.hasNext();){
	    TempWebRecord wr = (TempWebRecord) wrs.next();
	    Iterator instrs;
	    for(instrs = wr.defs.iterator(); instrs.hasNext();) {
		Instr i = (Instr) instrs.next();

		// assert def-sets are disjoint for a given instruction
		Set regs = defRegSet(i);
		regs.retainAll( g.regs(wr) );
		assert regs.isEmpty() : "def-sets should be disjoint!";

		code.assignRegister(i, wr.sym, g.regs(wr));
	    }
	    for(instrs = wr.uses.iterator(); instrs.hasNext();) {
		Instr i = (Instr) instrs.next();
		code.assignRegister(i, wr.sym, g.regs(wr));
	    }
	}
    } 


    HashSet spilled = new HashSet();
    
    private boolean isPrecolored(Object n) { 
	Graph.Node node = (Graph.Node) n;
	if (node.wr instanceof RegWebRecord) 
	    return true;
	if (webPrecolor.containsKey(node.wr)) 
	    return true;
	return false;
    }

    private boolean isAvailableToSpill(Object n) {
	Graph.Node node = (Graph.Node) n;
	if (isRegister(node.wr.temp())) 
	    return false;
	for(Iterator sps=spilled.iterator(); sps.hasNext();){ 
	    WebRecord spl = (WebRecord) sps.next();
	    if (spl.temp().equals(node.wr.temp()) &&
		spl.overlaps(node.wr)) 
		return false;
	}
	return true;
    }
	

    private void genSpillCode(UnableToColorGraph u, Graph g) { 
	Collection remove = u.getRemovalSuggestions();
	HashSet spillThese = new HashSet(remove.size());

	for(Iterator ri=remove.iterator(); ri.hasNext(); ) {
	    Graph.Node node = (Graph.Node) ri.next();

	    if (!isAvailableToSpill(node)) {
		if (SCARY_OUTPUT) System.out.println("SKIP ONE (1)");
	    }
	    
	    spillThese.add(node);
	}
	if (SCARY_OUTPUT) System.out.println("HIT1:"+spillThese.size());
	
	if (spillThese.isEmpty()) {
	    System.out.println(" remove:" +remove+ " contains nothing new");
	    Collection removeLrg = u.getRemovalSuggestionsBackup();
	    removeLrg = new HashSet(removeLrg);
	    removeLrg.removeAll(remove);
	nextNode:
	    for(Iterator ri=removeLrg.iterator(); ri.hasNext(); ) {
		Graph.Node node = (Graph.Node) ri.next();

		if (!isAvailableToSpill(node)) {
		    if (SCARY_OUTPUT) System.out.println("SKIP ONE (2)");
		    continue nextNode;
		}

		spillThese.add(node);
	    }

	    if (SCARY_OUTPUT) System.out.println("HIT2:"+spillThese.size());

	    if (spillThese.isEmpty())
		if (SCARY_OUTPUT) System.out.println
		    (" remove:" +removeLrg+ " contains nothing new");
	}


	if (spillThese.isEmpty()) {
	    // choose a node independently of the suggested ones, since
	    // those are all already spilled...
	    HashSet nset = new HashSet(g.nodeSet());
	    nset.removeAll(spilled);
	    int md = -1; Object mn=null;
	    for(Iterator ns=nset.iterator(); ns.hasNext(); ) {
		Graph.Node n = (Graph.Node) ns.next();
		if (g.getDegree(n) > md &&
		    !isRegister(n.wr.temp()) &&
		    isAvailableToSpill(n)) {
		    mn = n;
		    md = g.getDegree(n);
		}
	    }
	    if (mn != null)
		spillThese.add(mn);
	}
	
	assert !spillThese.isEmpty();
	assert !spillThese.contains(null);

	for(Iterator ss=spillThese.iterator(); ss.hasNext(); ) {
	    Graph.Node node = (Graph.Node) ss.next();
	    spilled.add(node.wr);
	    
	    TempWebRecord wr = (TempWebRecord) node.wr;
	    assert !isRegister(wr.temp());
	    
	    if (SPILL_INFO)
		System.out.print("\nSpilling "+wr);
	    
	    Temp t = wr.temp();
	    for(Iterator ds=wr.defs.iterator(); ds.hasNext(); ) {
		Instr i = (Instr) ds.next();

		if (i.isDummy()) 
		    continue;

		Instr n = i.getNext();

		// FSK: checking if <n> is a dummy and
		// shifting the spill down if so...
		while( n.isDummy()) {
		    i = n;
		    n = i.getNext();
		}
		
		assert i.canFallThrough &&
			    i.getTargets().isEmpty() : "can't insert spill at <"+i+" , "+n+">";
		SpillProxy sp = new SpillProxy(i, t);
		sp.layout(i, n);
	    }
	    for(Iterator us=wr.uses.iterator(); us.hasNext(); ) {
		Instr i = (Instr) us.next();

		if (i.isDummy()) 
		    continue;

		Instr p = i.getPrev();
		assert p.canFallThrough &&
			    p.getTargets().isEmpty() &&
			    i.predC().size() == 1 : "can't insert restore at<"+p+" , "+i+">";
		RestoreProxy rp = new RestoreProxy(i, t);
		rp.layout(p, i);
	    }
	}

	if (SCARY_OUTPUT) System.out.print("*** SPILLED ("+spilled.size()+")"+
					   (true?"":(": " + spilled)));
    }
    
    /** returns nodeToNum. */
    public static Map printGraph(ColorableGraph graph, boolean PRINT) {
	final Map nodeToNum = new HashMap(); 
	if(PRINT)System.out.println("nodes of graph");
	int i=0;
	for(Iterator nodes=graph.nodeSet().iterator();nodes.hasNext();){
	    i++;
	    Object n=nodes.next();
	    nodeToNum.put(n, new Integer(i));
	    if(PRINT)System.out.println(i+"\t"+n);
	}
	if (PRINT){
	    String s = ""+graph.edges();
	    Collection readEdges = readableEdges(graph.edges(),nodeToNum);
	    System.out.println("edges of graph "+readEdges);
	}
	return nodeToNum;
    }

    /** Graph is a graph view of the adjacency lists in this. 
	There is a many-to-one mapping from Nodes to WebRecords. 
	Note that when a Node is hidden or colored, its association
	with the WebRecord it maps to may cause other nodes to be
	hidden or colored (nb: need to think this through more
	carefully; algorithms depend on a particular semantics for
	replace()'s behavior...)
    */
    class Graph extends AbstractGraph implements ColorableGraph {
	private LinkedList nodes;  // LinkedList<Node>
	private LinkedList hidden; // List<WebRecord>
	private MultiMap wr2node;  // WebRecord -> Node

	/** Node is a record class representing the 
	    Node -> WebRecord mapping.  
	    It also holds the color for the node. 
	*/
	class Node {
	    final WebRecord wr;
	    final int index; 
	    RegColor color;
	    
	    LinearSet  adjR;
	    LinkedList adjT;

	    Node(WebRecord w, int i) { 
		wr = w; 
		index = i; 
		adjT = new LinkedList();
		adjR = new LinearSet();

		nodes.add(this);
		wr2node.add(w, this);
		
		if (w instanceof TempWebRecord) {
		    Map[] i2r2a = (Map[]) implicitAssigns.get(w.temp());
		    Set conflictRegs = new LinearSet(regToWeb.keySet());
		    Set possibleRegs = i2r2a[index].keySet();
		    conflictRegs.removeAll(possibleRegs);
		    for(Iterator cnf=conflictRegs.iterator();cnf.hasNext();){
			adjR.add(wr2node.get(regToWeb.get(cnf.next())));
		    }
		}
		for(Iterator wrs=wr.adjnds.iterator(); wrs.hasNext();){
		    WebRecord _wr = (WebRecord) wrs.next();
		    if (!wr2node.containsKey(_wr)) 
			continue;

		    Iterator nds = wr2node.getValues(_wr).iterator();
		    while(nds.hasNext()) {
			Node _n = (Node) nds.next();
			if (_n.wr instanceof TempWebRecord) {
			    adjT.add(_n);
			} else if (_n.wr instanceof RegWebRecord) {
			    adjR.add(_n);
			} else {
			    assert false;
			}

			if (w instanceof RegWebRecord) {
			    _n.adjR.add(this);
			} else {
			    _n.adjT.add(this);
			}
		    }
		}
		
	    }
	    public String toString() {
		return "n:<"+wr+","+index+","+color+">";
	    }
	}
	
	/** Helper function for resolving the WebRecord -> Node
	    relation.
	*/
	private Collection nodes(Object wr) {
	    return wr2node.getValues(wr); 
	}
	
	Graph() {
	    this.nodes = new LinkedList();
	    hidden = new LinkedList();
	    wr2node = new GenericMultiMap();
	}

	/** Adds nodes and edges representing `wr' and its conflicts
	    to this graph. 
	*/
	void add(WebRecord wr) {
	    if (isRegister(wr.temp())) {
		Node n = new Node(wr, 0);
		n.color = regToColor(wr.temp());
	    } else if (webPrecolor.keySet().contains(wr)) {
		// FSK: wrong for Precolored Doubles (fix later)
		Node n = new Node(wr, 0);
		n.color = regToColor((Temp)webPrecolor.get(wr));
	    } else {
		Map[] i2r2a = (Map[]) implicitAssigns.get(wr.temp());
		Map r2a = i2r2a[0];
		assert r2a != null : "no implicit assigns for "+wr.temp();
		List rl = (List) r2a.values().iterator().next();
		for(int j=0; j<rl.size(); j++) {
		    Node n = new Node(wr, j);
		}
	    }
	}

	/** Returns the Assignment that has been given to `wr'. 
	    requires: this has been colored.
	*/
	List regs(WebRecord wr) {
	    if (wr instanceof RegWebRecord) {
		return Collections.nCopies(1, wr.temp());
	    }

	    Collection c = wr2node.getValues(wr);
	    List a = new ArrayList(c);
	    for(Iterator nodes=c.iterator(); nodes.hasNext(); ) {
		Node n = (Node) nodes.next();
		a.set(n.index, n.color.reg);
	    }
	    return a;
	}

	public Set nodeSet() { 
	    return new AbstractSet() {
		public int size() { return nodes.size(); }
		public Iterator iterator() {
		    return nodes.iterator();
		}
	    };
	}

	public Collection neighborsOf(final Object n) { 
	    if (true) {
		// System.out.println("neighborsOf("+n+") : "+((Node)n).adjs);
		return new AbstractCollection() {
		    Node nd = (Node) n;
		    public int size() { 
			return nd.adjT.size() + nd.adjR.size();
		    }
		    public Iterator iterator() {
			return new CombineIterator
			    (nd.adjT.iterator(), nd.adjR.iterator());
		    }
		};

	    }

	    
	    if (!(n instanceof Node))
		throw new IllegalArgumentException();
	    final Node node = (Node) n;
	    return new AbstractCollection() {
		private Iterator wrs() { 
		    Iterator prependIter;
		    if (!(node.wr instanceof RegWebRecord)) {
			Iterator regWebIter = regToWeb.values().iterator();
			final Map[] i2r2a =
			    (Map[])implicitAssigns.get(node.wr.temp());
			final Map r2a = i2r2a[node.index];
			    
			FilterIterator.Filter RWFltr = 
			    new FilterIterator.Filter() {
				public boolean isElement(Object o) {
				    RegWebRecord rwr = (RegWebRecord) o;
				    return 
				    !r2a.containsKey(rwr.temp()) && 
				    !node.wr.adjnds.contains(rwr);
				}
			    };
			prependIter = 
			    new FilterIterator(regWebIter,RWFltr);
		    } else {
			prependIter = Default.nullIterator;
		    }
		    return new CombineIterator
			(prependIter, node.wr.adjnds.iterator());
		}
		public int size() {
		    int s=0;
		    for(Iterator wrs = wrs(); wrs.hasNext(); ) {
			Object wr = wrs.next();
			s += nodes(wr).size(); 
		    }
		    return s; 
		}

		FilterIterator.Filter TO_NODE_ITER = 
		    new FilterIterator.Filter() {
			public Object map(Object o) { 
			    return nodes(o).iterator();
			}
		    };
		public Iterator iterator() {
		    Iterator iters = 
			new FilterIterator(wrs(), TO_NODE_ITER);
		    return new CombineIterator(iters) {
			public boolean hasNext() {
			    return super.hasNext();
			}
			public Object next() {
			    return super.next();
			}
		    };
		}
	    };
	}

	public void hide(Object o) { 
	    if (! (o instanceof Node)) 
		throw new IllegalArgumentException(o+" not in node-set");
	    Node n = (Node) o;
	    
	    doHide(n.wr);
	}
	
	private void doHide(WebRecord wr) {
	    if (hidden.contains(wr)) {
		return;
	    }
	    nodes.removeAll( nodes(wr) );
	    Iterator nbors;
	    for(nbors=wr.adjnds.iterator(); nbors.hasNext();){ 
		WebRecord nbor = (WebRecord) nbors.next();
		nbor.adjnds.remove(wr);
	    }
	    hidden.addLast(wr);

	    for(Iterator wrns=nodes(wr).iterator(); wrns.hasNext();) {
		Node n = (Node) wrns.next();
		for(Iterator nds=n.adjT.iterator(); nds.hasNext();) {
		    Node _n = (Node) nds.next();
		    assert _n.wr instanceof TempWebRecord : _n.wr;
		    _n.adjT.remove(n);
		}
	    }
	}

	public Object replace() { 
	    WebRecord wr;
	    try {
		wr = (WebRecord) hidden.removeLast();
	    } catch (java.util.NoSuchElementException e) {
		return null;
	    }
	    nodes.addAll( nodes(wr) );
	    for(Iterator nbors=wr.adjnds.iterator();nbors.hasNext();){ 
		WebRecord nbor = (WebRecord) nbors.next();
		if (!nbor.adjnds.contains(wr)) {
		    nbor.adjnds.add(wr);
		}
	    }
	    
	    for(Iterator wrns=nodes(wr).iterator();wrns.hasNext();){
		Node n = (Node) wrns.next();
		for(Iterator nds=n.adjT.iterator(); nds.hasNext();) {
		    Node _n = (Node) nds.next();
		    assert _n.wr instanceof TempWebRecord;
		    _n.adjT.add(n);
		}
	    }

	    for(Iterator ns = nodes(wr).iterator(); ns.hasNext(); ){ 
		Node n = (Node) ns.next();
		if (n.index == 0) return n;
	    }
	    
	    throw new RuntimeException();
	}
	public void replaceAll() {
	    while(!hidden.isEmpty()) {
		replace();
	    }
	}
	public Color getColor(Object n) { 
	    Node nd;
	    try { nd = (Node) n; return nd.color;
	    } catch (ClassCastException e) {
		throw new IllegalArgumentException();
	    }
	}

	public void resetColors() { 
	    Iterator ns;
	    for(ns = nodes.iterator(); ns.hasNext();) {
		((Node) ns.next()).color = null;
	    }
	    for(Iterator wrs = hidden.iterator(); wrs.hasNext();) {
		for(ns=nodes((WebRecord)wrs.next()).iterator();ns.hasNext();){
		    ((Node) ns.next()).color = null;
		}
	    }
	}

	public void unsetColor(Object o) {
	    try {
		Node nd = (Node) o;
		for(Iterator ns=nodes(nd.wr).iterator(); ns.hasNext();){
		    Node n = (Node) ns.next();
		    n.color = null;
		}
	    } catch (ClassCastException e) {
		throw new IllegalArgumentException();
	    }
	}

	public void setColor(Object o, Color col) 
	    throws ColorableGraph.IllegalColor { 
	    assert col != null;
	    try {
		Node node = (Node) o;
		assert node.index == 0 : "setColor on bad node "+node;
		RegColor rc = (RegColor) col;
		Collection nds = nodes(node.wr);
		Map[] i2r2a = (Map[]) implicitAssigns.get(node.wr.temp());
		Map r2a = i2r2a[node.index];
		if (!r2a.keySet().contains(rc.reg)) 
		    throw new ColorableGraph.IllegalColor(o, col);
		List assign = (List) r2a.get(rc.reg);

		// verify all color choices
		for(Iterator ns=nds.iterator(); ns.hasNext(); ) {
		    Node n = (Node) ns.next();
		    Temp t = (Temp) assign.get(n.index);
		    rc = regToColor(t);
		    Iterator nbs;
		    for(nbs=neighborsOf(n).iterator();nbs.hasNext();){
			Node nb = (Node) nbs.next();
			if (nb.color != null && 
			    nb.color.equals(rc)) {
			    throw new ColorableGraph.IllegalColor(n,rc);
			}
		    }
		    if (false) System.out.println
				   (n+" passed verify for "+rc+
				    ", neighbors:"+neighborsOf(n));
		}

		// got here, colors passed verification
		for(Iterator ns=nds.iterator(); ns.hasNext();){
		    Node n = (Node) ns.next();
		    Temp t = (Temp) assign.get(n.index);
		    rc = regToColor(t);
		    n.color = rc;
		    if (false) System.out.println("set color of "+n);
		}
	    } catch (ClassCastException e) {
		throw new IllegalArgumentException();
	    }
	}
    }

    class StatGather {
	int sum=0; int sumSq=0; int cnt=0;
	void add(int elem) { cnt++; sum+=elem; sumSq+=elem*elem; } 
	int size() { return cnt; }
	int mean() { return sum / cnt; }
	int variance() { int m=mean(); return sumSq/cnt - m*m; } 
	public String toString() { 
	    return "Stat<size:"+size()+" mean:"+mean()+" var:"+variance()+">"; 
	}
    }

    static void printConflictTime() { 
	System.out.println("conflict time -"+
			   " tnr: "+rntwrNumChecks+
			   " (num:"+rnrwrNumChecks+
			   " ave:"+(rntwrNumChecks/rnrwrNumChecks)+")"+
			   " twr: "+twrConflictTime+
			   " (num:"+twrNumConflicts+
			    " ave:"+(twrConflictTime/twrNumConflicts)+")"+
			   " rwr: "+rwrConflictTime+
			   " (num:"+rwrNumConflicts+
			    " ave:"+(rwrConflictTime/rwrNumConflicts)+")");
	rwrConflictTime = twrConflictTime = 1;
	rwrNumConflicts = twrNumConflicts = 1; 
	rnrwrNumChecks = rntwrNumChecks = 1; 
    }
    static long rwrConflictTime = 0; static int rwrNumConflicts = 0;
    static long twrConflictTime = 0; static int twrNumConflicts = 0;
    static int rntwrNumChecks = 1, rnrwrNumChecks = 1;
    abstract class WebRecord {
	int nints, disp;
	double spcost;
	List adjnds; // List<WebRecord>

	private int sreg; 
	private boolean sregYet = false;

	WebRecord() {
	    nints = 0;
	    disp = Integer.MIN_VALUE;
	    spcost = 0.0;
	    adjnds = new LinkedList();
	}
	
	int sreg() { assert sregYet; return sreg; }
	void sreg(int val) {
	    assert !sregYet;
	    sreg = val;
	    sregYet = true;
	}
	
	/** Returns true if this shares a use/def with wr. */
	boolean overlaps(WebRecord wr) {
	    HashSet refs = 
		new HashSet( defs().size() + uses().size() );
	    refs.addAll(defs());
	    refs.addAll(uses());
	    HashSet rfs2 = new HashSet(refs);
	    if (refs.retainAll(wr.defs()) ||
		rfs2.retainAll(wr.uses())) {
		if (SCARY_OUTPUT) System.out.println(this + " overlaps " + wr);
		return true;
	    }
	    if (SCARY_OUTPUT) System.out.println(this + " does NOT overlap " + wr);
	    return false;
	}

	// ( interference based on Muchnick, page 494.)
	boolean conflictsWith(WebRecord wr) {
	    long immed_conflict_time, reg_conflict_time=0, start_time;

	    start_time = System.currentTimeMillis();	    
	    boolean r =
		this.conflictsWith1D(wr) ||
		wr.conflictsWith1D(this);
	    immed_conflict_time = System.currentTimeMillis() - start_time;
	    if (!r &&
		webPrecolor.containsKey(this)) {
		WebRecord rwr = (WebRecord) 
		    regToWeb.get(webPrecolor.get(this));

		start_time = System.currentTimeMillis();
		r = rwr.conflictsWith(wr);
		reg_conflict_time = System.currentTimeMillis() - start_time;
	    }
	    if (false && reg_conflict_time > 10)
		System.out.println("conflict compute time, imm: "+
				   immed_conflict_time+" reg: "+
			       reg_conflict_time);
	    return r;
	}

	// one directional conflict check (helper function) 
	// if this is live at a def in wr, returns true.
	boolean conflictsWith1D(WebRecord wr) {
	    long start_time = System.currentTimeMillis();
	    boolean r = false;
	    for(Iterator ins=this.defs().iterator();ins.hasNext();){
		Instr d = (Instr) ins.next();
		
		// "a,b := f()" ==> a and b interfere, even though
		// there may not be any defs of either that "reach"
		// this statement (a statement need not reach itself)
		if (wr.defs().contains( d ))
		    return true;

		Set l= liveTemps.getLiveAfter(d);
		if(l.contains(wr.temp())) {
		    if (wr instanceof RegWebRecord) {
			r = true;
			break;
		    }
		    
		    HashSet wDefs = new HashSet
			(rdefs.reachingDefs(d, wr.temp()));
		    wDefs.retainAll(wr.defs());
		    if (!wDefs.isEmpty()) {
			r = true;
			break;
		    }
		}
	    
	    }
	    twrConflictTime += System.currentTimeMillis() - start_time;
	    twrNumConflicts++;
	    return r;
	}

	// returns the set of instrs that this web holds definitions
	// for.  These instrs are used to detect conflicts between
	// webs. 
	abstract Set defs();

	// returns the set of instrs that this web holds uses
	// for.  These instrs are used to detect conflicts between
	// webs. 
	abstract Set uses();

	// returns the Temp that this WebRecord represents.
	abstract Temp temp();

	public String toString() { return "w:"+temp(); }
    }

    class RegWebRecord extends WebRecord {
	final Temp reg;
	RegWebRecord(Temp reg) {
	    super();
	    this.reg = reg;
	}
	public int hashCode() { return reg.hashCode(); }
	public boolean equals(Object o) { 
	    if (o == this) 
		return true;
	    if (o != null && o instanceof RegWebRecord) {
		return reg.equals(((RegWebRecord)o).reg);
	    } else {
		return false;
	    }
	}
	public Set defs() { return (Set) regToDefs.getValues(reg); }
	public Set uses() { return (Set) regToUses.getValues(reg); }
	public Temp temp() { return reg; }

	boolean conflictsWith1D(WebRecord wr) {
	    long start_time = System.currentTimeMillis();
	    boolean r;
	    if (wr instanceof RegWebRecord) {
		r = true;
	    } else {
		r = super.conflictsWith1D(wr);
		if (!r &&
		    webPrecolor.invert().containsKey(reg)) {
		    Collection preWebsForReg = webPrecolor.invert().getValues(reg);
		    int nms=0;
		    Iterator wbs = preWebsForReg.iterator();
		    while(wbs.hasNext()) { nms++;
			WebRecord _wr = (WebRecord) wbs.next();
			if (_wr.conflictsWith1D(wr) ||
			    wr.conflictsWith1D(_wr)) {
			    r = true;
			    break;
			}
		    }
		    rntwrNumChecks+=nms; rnrwrNumChecks++;
		}
	    }
	    rwrConflictTime += System.currentTimeMillis() - start_time;
	    rwrNumConflicts++;
	    return r;
	}
    }

    // converts a Set<Instr>:s1 into a Set<Integer>:s2 where the
    // elements in s2 correspond to the IDs of the instructions in s1
    private static Integer i2int(Instr i) { return new Integer(i.getID()); }
    private static Set readable(final Set instrs) {
	final FilterIterator.Filter fltr = new FilterIterator.Filter() {
	    public Object map(Object o) { return i2int((Instr)o); }
	};
	return new AbstractSet() {
	    public int size() { return instrs.size(); }
	    public Iterator iterator() {
		return new FilterIterator(instrs.iterator(), fltr);
	    }
	};
    }

    class TempWebRecord extends WebRecord {
	Temp sym;
	Set defs, uses; // Set<Instr>
	boolean spill;
	int disp;

	public int hashCode() { return sym.hashCode(); }
	public boolean equals(Object o) {
	    if (o == this) 
		return true;
	    if (o != null && o instanceof TempWebRecord) {
		TempWebRecord t = (TempWebRecord) o;
		return sym.equals(t.sym) &&
		    defs.equals(t.defs) &&
		    uses.equals(t.uses);
	    } else {
		return false;
	    }
	}
	TempWebRecord(Temp symbol, Set defSet, Set useSet) {
	    super();
	    sym = symbol; defs = defSet; uses = useSet;
	    spill = false; disp = -1;
	    assert !isRegister(sym);
	}
	
	public Temp temp() { return sym; }
	public Set defs() { return Collections.unmodifiableSet(defs);}
	public Set uses() { return Collections.unmodifiableSet(uses);}

	public String toString() {
	    List a = (List) rfi.getRegAssignments(sym).iterator().next();
	    if (true) 
		return "<web sym:"+sym+
		    ", defs:"+readable(defs)+
		    ", uses:"+readable(uses)+
		    ((a.size()==1)?", single-word":", multi-word")+
		    " >";
	    else 
		return "w:"+sym+" degree:"+adjnds.size();
	}
    }

    class AdjMtx {
	// a Lower Triangular Matrix backed by a HashSet of IntPairs
	HashSet pairSet;
	AdjMtx(List symReg) {
	    pairSet = new HashSet(symReg.size());
	}
	public boolean get(int x, int y) {
	    return pairSet.contains(new IntPair(x, y));
	}
	public void set(int x, int y, boolean b) {
	    IntPair p = new IntPair(x, y);
	    if (b) {
		pairSet.add(p);
	    } else {
		pairSet.remove(p);
	    }
	}
	class IntPair { 
	    final int m,n;
	    IntPair(int x, int y) { 
		if (x > y) {
		    m=x; n=y;
		} else {
		    m=y; n=x;
		}
	    }
	    public int hashCode() { return ((m<<16)|(m>>16)) ^ n; }
	    public boolean equals(Object o) { 
		IntPair p = (IntPair) o;
		return m == p.m && n == p.n;
	    }
	}
	
    }
    
    class AdjMtxOld {
	// a Lower Triangular Matrix backed by a BitString.  Note that
	// for Lower Triangular Matrix, order of coordinate args is
	// insignificant (from p.o.v. of user). 

	final harpoon.Util.BitString bits;
	final int side;
	AdjMtxOld(List symReg) {
	    side = symReg.size();
	    bits = new harpoon.Util.BitString(side * side / 2);
	}
	boolean get(int x, int y) {
	    assert x != y;
	    assert x < side; 
	    assert y < side;
	    return bits.get(convert(x,y));
	}
	void set(int x, int y, boolean b) {
	    assert x != y;
	    assert x < side; 
	    assert y < side;
	    if (b) bits.set(convert(x,y)); 
	    else   bits.clear(convert(x,y));
	    assert get(x,y) == b;
	}
	private int convert(int x, int y) {
	    if (x > y) return offset(x) + y;
	    else       return offset(y) + x;
	}
	private int offset(int x) { return (x * (x - 1)) / 2; }
    }

    // only defined for unions of WebRecords
    class EqWebRecords extends harpoon.Util.Collections.DisjointSet {
	private HashSet temps = new HashSet();
	MultiMap wrToEqwrs = new GenericMultiMap();

	public boolean containsTemp(Temp t) {
	    return temps.contains(t);
	}
	public Instr rename(final Instr i) {
	    TempMap tmap = new TempMap() { 
		public Temp tempMap(Temp tmp) {
		    WebRecord wr = getWR(i,tmp);
		    if (wr == null) return tmp;
		    wr = (WebRecord) find(wr);
		    return wr.temp();
		}
	    };
	    return i.rename(tmap);
	}
	public void union(Object a, Object b) {
	    if ( super.find(a).equals(super.find(b))) 
		return;

	    WebRecord wrA = (WebRecord) a;
	    WebRecord wrB = (WebRecord) b;
	    if (UNIFY_INFO) 
		System.out.println("unioning0 "
		 +" ("+System.identityHashCode(super.find(wrA))+")"
		 +" ("+System.identityHashCode(super.find(wrB))+")"
				   );
	    if (UNIFY_INFO)
		System.out.println(" unioning1  " + asTemps(unified(wrA)) 
				   + " and " + asTemps(unified(wrB))
				   );

	    Collection as = unified(wrA);
	    Collection bs = unified(wrB);
	    
	    temps.add( wrA.temp() );
	    temps.add( wrB.temp() );
	    super.union(super.find(wrA), super.find(wrB));

	    // clear old mappings
	    wrToEqwrs.remove(wrA);
	    wrToEqwrs.remove(wrB);

	    if (UNIFY_INFO)
		System.out.println(" unioning2 " + asTemps(unified(wrA)) 
				   + " and " + asTemps(unified(wrB)));

	    wrToEqwrs.addAll(super.find(wrA), as);
	    wrToEqwrs.addAll(super.find(wrB), bs);

	    if (UNIFY_INFO)
		System.out.println(" unioning3 " + asTemps(unified(wrA)) 
				   + " and " + asTemps(unified(wrB)));
	}
	
	boolean anyConflicting(WebRecord wr1, Collection wrs) {
	    for(Iterator wrI=wrs.iterator(); wrI.hasNext();) {
		WebRecord wr2 = (WebRecord) wrI.next();
		if (real_conflicting(wr1, wr2)) 
		    return true;
	    }
	    return false;
	}

	boolean conflicting(WebRecord wr1, WebRecord wr2) {
	    boolean rtn = real_conflicting(wr1, wr2);
	    return rtn;
	}
	
	private Collection unified(WebRecord wr) {
	    Collection wrs = new ArrayList(wrToEqwrs.getValues(super.find(wr)));
	    wrs.add(wr);
	    return wrs;
	}

	private Collection asTemps(final Collection wrs) {
	    return new java.util.AbstractCollection() {
		    public int size() { return wrs.size(); }
		    public Iterator iterator() { 
			return new FilterIterator
			    (wrs.iterator(), 
			     new FilterIterator.Filter() {
				     public Object map(Object o) {
					 return ((WebRecord)o).temp();
				     }
				 });
		    }
		};
	}
	private boolean real_conflicting(WebRecord wr1, WebRecord wr2) {
	    Collection wrs1 = unified(wr1);
	    Collection wrs2 = unified(wr2);
	    for(Iterator i1=wrs1.iterator(); i1.hasNext();) {
		WebRecord wrA = (WebRecord) i1.next();
		for(Iterator i2=wrs2.iterator(); i2.hasNext();) {
		    WebRecord wrB = (WebRecord) i2.next();
		    if (wrA.conflictsWith(wrB))
			return true;
		}
	    }
	    return false;
	}
    }

}

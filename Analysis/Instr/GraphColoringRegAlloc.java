// GraphColoringRegAlloc.java, created Mon Jul 17 16:39:13 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.DataFlow.CachingLiveTemps;
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
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
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

/**
 * <code>GraphColoringRegAlloc</code> uses graph coloring heuristics
 * to find a register assignment for a Code.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColoringRegAlloc.java,v 1.1.2.13 2000-08-02 01:15:22 pnkfelix Exp $
 */
public class GraphColoringRegAlloc extends RegAlloc {
    
    private static final boolean TIME = false;
    private static final boolean RESULTS = false;
    
    public static RegAlloc.Factory FACTORY =
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		GraphColorer gc;

		// gc = new SimpleGraphColorer();
		gc = new OptimisticGraphColorer();

		return new GraphColoringRegAlloc(c, gc);
	    }
	};

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
    
    Map implicitAssigns; // VReg -> AReg -> Assign
    Map regToColor;  // AReg -> RegColor

    Map ixtToWebPreCombine = new HashMap(); // Instr x VReg -> WebRecord    
    Map ixtToWeb = new HashMap(); // Instr x VReg -> WebRecord
    List webRecords; // List<WebRecord>

    List tempWebRecords; // List<TempWebRecord>
    List regWebRecords; // List<RegWebRecord>
    
    // NOT BUILT YET
    // Maps Temp:t -> Set of Reg whose live regions interfere with
    //                t's live region
    MultiMap preassignMap;

    GraphColorer colorer;

    /** Creates a <code>GraphColoringRegAlloc</code>, assigning `gc'
	as its graph coloring strategy. 
    */
    public GraphColoringRegAlloc(Code code, GraphColorer gc) {
        super(code);
	rfi = frame.getRegFileInfo();
	buildRegAssigns();
	rdefs = new ReachingDefsAltImpl(code);
	liveTemps = LiveTemps.make(code, rfi.liveOnExit());
	// preassignMap = buildPreassignMap(code, rfi.liveOnExit());
        colorer = gc;
    }

    protected Derivation getDerivation() {
	return null;
    }

    protected MultiMap buildPreassignMap(Code code, Set liveOnExit) {
	return null;
    }

    /** Returns a new Collection cr, where
	for all e element of edges 
	   there exists a unique c element of cr such that 
	   c = [nodeToNum(e.get(0)), nodeToNum(e.get(1))]
    */
    private Collection readableEdges(final Collection edges, 
				     final Map nodeToNum) {
	return new AbstractCollection() {
	    public int size() { return edges.size(); }
	    public Iterator iterator() { 
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
    private Collection readableNodes(final Collection nodes,
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
	AdjMtx adjMtx;

	if (TIME) System.out.println();
	do {
	    do {
		if (TIME) System.out.println("Making Webs");

		makeWebs(rdefs); 
		
		for(Iterator is=code.getElementsI(); is.hasNext();){
		    Instr i = (Instr) is.next();
		    for(Iterator ts=i.defC().iterator(); ts.hasNext();) {
			Temp t = (Temp) ts.next();
			if (! isRegister(t) ) {
			    List ixt = Default.pair(i,t);
			    WebRecord web = (WebRecord)ixtToWeb.get(ixt);
			    if (web == null) {
				Util.assert(ixtToWebPreCombine.get(ixt)==null,
					    "There was a web for "+ixt+
					    " pre-combination! "+
					    ixtToWebPreCombine.get(ixt));
				Util.assert(false,
					    "no web for i:"+i+", t:"+t);
			    }
			}
		    }
		    for(Iterator ts=i.useC().iterator(); ts.hasNext();) {
			Temp t = (Temp) ts.next();
			if (! isRegister(t) ) {
			    WebRecord web = (WebRecord) 
				ixtToWeb.get(Default.pair(i,t));
			    Util.assert(web != null, 
					"no web for i:"+i+", t:"+t);
			}
		    }
		}
		// System.out.println("webs: "+webRecords);

		if (TIME) System.out.println("Building Matrix");

		adjMtx = buildAdjMatrix();

		if (TIME) System.out.println
		    ( ((CachingLiveTemps)liveTemps).cachePerformance());
		
		// System.out.println("Adjacency Matrix");
		// System.out.println(adjMtx);
		coalesced = coalesceRegs(adjMtx);
	    } while (coalesced);

	    if (TIME) System.out.println("Building Lists");

	    WebRecord[] adjLsts = buildAdjLists(adjMtx); 
	    adjMtx = null;

	    // System.out.println(Arrays.asList(adjLsts));

	    computeSpillCosts();

	    if (TIME) System.out.println("Building Graph");

	    final ColorableGraph graph = buildGraph(adjLsts);
	    
	    final Map nodeToNum = new HashMap(); 
	    if (RESULTS) System.out.println("nodes of graph");
	    int i=0;
	    for(Iterator nodes=graph.nodeSet().iterator();nodes.hasNext();){
		i++;
		Object n=nodes.next();
		nodeToNum.put(n, new Integer(i));
		if (RESULTS) System.out.println(i+"\t"+n);
	    }
	    Collection readEdges = readableEdges(graph.edges(), nodeToNum);
	    if (RESULTS) System.out.println("edges of graph "+readEdges);

	    try {
		List colors = new ArrayList(regToColor.values());
		// System.out.println("colors:"+colors);
		colorer.color(graph, colors);
		
		MultiMap c2n = new GenericMultiMap();
		for(Iterator nds=graph.nodeSet().iterator();nds.hasNext();){
		    Object nd = nds.next();
		    c2n.add(graph.getColor(nd), nd);
		}
		for(Iterator cs=c2n.keySet().iterator();cs.hasNext();){
		    Object col=cs.next();
		    if (RESULTS) 
			System.out.println(col + " nodes: "+
					   readableNodes(c2n.getValues(col),
							 nodeToNum));
		}

		modifyCode();
		
		success = true;
	    } catch (UnableToColorGraph u) {
		success = false;

		System.out.println("Unable to color graph");
		System.out.println();
		System.out.println("Suggested for spilling "+
				   u.getRemovalSuggestions());
		System.out.println();
		System.out.println("nodes of graph "+
				   readableNodes(graph.nodeSet(),nodeToNum));
		System.out.println("edges of graph "+
				   readableEdges(graph.edges(),nodeToNum));
		System.exit(-1);

		genSpillCode();

	    }
	} while (!success);
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
	    Instr i = (Instr) instrs.next();
	    Iterator tmps = new CombineIterator(i.defC().iterator(),
						i.useC().iterator());
	    while(tmps.hasNext()) {
		Temp t = (Temp) tmps.next();
		if (rfi.isRegister(t)) {
		    if (i.defC().contains(t)) regToDefs.add(t, i);
		    if (i.useC().contains(t)) regToUses.add(t, i);
		    
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
			// build Reg -> Assign for suggRegs
			Map r2a = new HashMap(suggRegs.size());
			for(Iterator s=suggRegs.iterator();s.hasNext();){
			    List asn = (List) s.next();
			    Temp reg = (Temp) asn.iterator().next();
			    if (!r2a.containsKey(reg)) {
				r2a.put(reg, asn);
			    }
			}
			asnSetToImplc.put(suggRegs, r2a);
		    }

		    // add to implicitAssigns
		    Object r2a = asnSetToImplc.get(suggRegs);
		    implicitAssigns.put(t, r2a);
		}
	    }
	}
    }
    
    private Color regToColor(Temp reg) {
	Color c = (Color) regToColor.get(reg);
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
    }
    
    /**
       nwebs is set after this method returns.
       assignWebRecords, tempWebRecords, and webRecords are set
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

		// non-standard iteration because of this:

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
			    // uses and defines.  Take these out after that
			    // is fixed. 
			    Set s1 = new HashSet(web1.defs);
			    s1.retainAll(web2.uses);
			    
			    Set s2 = new HashSet(web2.defs);
			    s2.retainAll(web1.uses);
			    combineWebs = (!s1.isEmpty() || !s2.isEmpty());
			}
			
			
			if (combineWebs) {
			    web1.defs.addAll(web2.defs);
			    web1.uses.addAll(web2.uses);
			    webSet.remove(web2);
			    changed = true;
			}
			
		    }
		}
	    }
	} while ( changed );
	
	// System.out.println("post-duchain-combination");
	// System.out.println("webSet: "+webSet);
	
	
	regWebRecords = new ArrayList(regToColor.keySet().size());
	
	Iterator rs = regToColor.keySet().iterator();
	for(i=0; rs.hasNext(); i++) {
	    Temp reg = (Temp) rs.next();
	    WebRecord w = new RegWebRecord(reg);
	    w.sreg(i);
	    regWebRecords.add(w);
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
		Util.assert(prior == null || prior == wr);
		ixtToWeb.put(ixt, wr);
	    }
	}
	if(!ixtToWeb.keySet().equals(ixtToWebPreCombine.keySet())) {
	    HashSet postMinusPre = new HashSet(ixtToWeb.keySet());
	    HashSet preMinusPost = new HashSet(ixtToWebPreCombine.keySet());
	    postMinusPre.removeAll(ixtToWebPreCombine.keySet());
	    preMinusPost.removeAll(ixtToWeb.keySet());
	    
	    System.out.println("PRE - POST: " + preMinusPost);
	    System.out.println();
	    System.out.println("POST - PRE: " + postMinusPre);
	    System.out.println();
	    Util.assert(false);
	}
    }

    private AdjMtx buildAdjMatrix() { 
	AdjMtx adjMtx = new AdjMtx(webRecords);
	int i, j;
	
	Iterator assgn1 = webRecords.iterator();
	for(i=1; i<webRecords.size(); i++) {
	    WebRecord wr1 = (WebRecord) webRecords.get(i);
	    for(j=0; j<i; j++) {
		WebRecord wr2 = (WebRecord) webRecords.get(j);
		adjMtx.set(wr1.sreg(),wr2.sreg(), wr1.conflictsWith(wr2));
	    }
	}
	return adjMtx;
    }
    
    // This '.left' stuff is bullshit... just a complicated way of
    // indicating the definition type and doing the necessary
    // replacement... temp remapping should look cleaner...
    private boolean coalesceRegs(AdjMtx adjMtx) { 
	return false;
	/*
	int i, j, k, l, p, q;
	Instr inst, pqinst;
	for(i=1; i<=nblocks; i++) {
	    for(j=1; j<=ninsts[i]; j++) {
		inst = LBlock[i][j];
		if (inst.kind = regval) {
		    k = Reg_to_Int(inst.left);
		    l = Reg_to_Int(inst.opd.val);
		    if (! adjMtx.get(k,l) ||
			nonStore(LBlock,k,l,i,j)) {
			for(p=1; p<nblocks; p++) {
			    for(q=1; q<ninsts[p]; q++) {
				pqinst = LBlock[p][q];
				if (LIR_Has_Left(pqinst) &&
				    pqinst.left == inst.opt.val) {
				    pqinst.left = inst.left;
				}
			    }
			}
		    }
		    // remove the copy instruction 
		    inst.remove();
		    ((WebRecord)symReg.get(k)).defs
			.addAll(((WebRecord)symReg.get(l)).defs);
		    ((WebRecord)symReg.get(k)).uses
			.addAll(((WebRecord)symReg.get(l)).uses);
		    symReg.set(1, symReg.get(nwebs));
		    for(p=1; p<=nwebs; p++) {
			if (adjMtx.get(p,l)) {
			    adjMtx.set(p,l,true);
			}
			adjMtx.set(p,l, adjMtx.get(nwebs,p));
		    }
		    nwebs--;
		}
	    }
	}
	*/
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
		if (adjMtx.get(i,j)) {
		    adjLsts[i].adjnds.add(adjLsts[j]);
		    adjLsts[j].adjnds.add(adjLsts[i]);
		    adjLsts[i].nints++;
		    adjLsts[j].nints++;
		}
	    }
	}
	return adjLsts;
    }

    private void computeSpillCosts() { 
	
    }

    private Graph buildGraph(WebRecord[] adjLsts) {
	return new Graph(adjLsts);
    }

    private void modifyCode() { 
	HashMap colorToAssign; // RegColor -> AReg
	colorToAssign = new HashMap();
	for(Iterator ars = regWebRecords.iterator(); ars.hasNext();){
	    RegWebRecord wr = (RegWebRecord) ars.next();
	    colorToAssign.put(wr.regColor(), wr.reg);
	}
	for(Iterator wrs = tempWebRecords.iterator(); wrs.hasNext();){
	    TempWebRecord wr = (TempWebRecord) wrs.next();
	    Iterator instrs;
	    for(instrs = wr.defs.iterator(); instrs.hasNext();) {
		Instr i = (Instr) instrs.next();
		Temp reg = (Temp) colorToAssign.get(wr.regColor());

		// FSK: BROKEN!  Need to change this to map multiple
		// nodes (and associated colors) to single Temp and
		// associated reg assignment
		List regs = Arrays.asList(new Temp[]{ reg }); 

		code.assignRegister(i, wr.sym, regs);
	    }
	    for(instrs = wr.uses.iterator(); instrs.hasNext();) {
		Instr i = (Instr) instrs.next();
		List regs =  (List) colorToAssign.get(wr.regColor());
		Util.assert(regs != null);
		code.assignRegister(i, wr.sym, regs);
	    }
	}
    } 

    private void genSpillCode() { 

    }
    
    /** Graph is a graph view of the adjacency lists in this. 
	There is a many-to-one mapping from Nodes to WebRecords. 
    */
    class Graph extends AbstractGraph implements ColorableGraph {
	LinkedList adjLsts;
	LinkedList hidden;
	Graph(WebRecord[] adjLsts) {
	    this.adjLsts = new LinkedList(Arrays.asList(adjLsts));
	    hidden = new LinkedList();
	}
	public Set nodeSet() { 
	    return new AbstractSet() {
		public int size() { return adjLsts.size(); }
		public Iterator iterator() {
		    return adjLsts.iterator();
		}
	    };
	}
	public Collection neighborsOf(Object n) { 
	    if (!(n instanceof WebRecord))
		throw new IllegalArgumentException();
	    WebRecord lr = (WebRecord) n;
	    return lr.adjnds;
	}
	public void resetGraph() { replaceAll(); resetColors(); }
	public void hide(Object n) { 
	    if (adjLsts.remove(n)) { // check if in nodeSet
		WebRecord lr = (WebRecord) n;
		Iterator nbors;
		for(nbors=lr.adjnds.iterator(); nbors.hasNext();){ 
		    WebRecord nbor = (WebRecord) nbors.next();
		    boolean changed = nbor.adjnds.remove(lr);
		    Util.assert(changed);
		}
		hidden.addLast(lr);
	    } else {
		throw new IllegalArgumentException();
	    }
	}
	public Object replace() { 
	    WebRecord lr;
	    try {
		lr = (WebRecord) hidden.removeLast();
		adjLsts.add(lr);
		for(Iterator nbors=lr.adjnds.iterator();nbors.hasNext();){ 
		    WebRecord nbor = (WebRecord) nbors.next();
		    nbor.adjnds.add(lr);
		}
	    } catch (java.util.NoSuchElementException e) {
		lr = null;
	    }
	    return lr;
	}
	public void replaceAll() {
	    while(!hidden.isEmpty()) {
		replace();
	    }
	}
	public Color getColor(Object n) { 
	    if (adjLsts.contains(n)) {
		WebRecord lr = (WebRecord) n;
		return lr.regColor();
	    } else {
		throw new IllegalArgumentException();
	    }
	}

	public void resetColors() { 
	    Iterator ns;
	    for(ns = adjLsts.iterator(); ns.hasNext();) {
		((WebRecord) ns.next()).regColor(null);
	    }
	    for(ns = hidden.iterator(); ns.hasNext();) {
		((WebRecord) ns.next()).regColor(null);
	    }
	}

	public void setColor(Object n, Color c) { 
	    try {
		((WebRecord)n).regColor((RegColor) c);
	    } catch (ClassCastException e) {
		throw new IllegalArgumentException();
	    }
	}
    }

    abstract class WebRecord {
	int nints, disp;
	double spcost;
	List adjnds; // List<WebRecord>

	private int sreg; 
	private boolean sregYet = false;

	private RegColor regColor;
	private boolean regColorYet = false;
	final boolean colorOnce;
      
	WebRecord(boolean colorOnce) {
	    nints = 0;
	    regColor = null;
	    disp = Integer.MIN_VALUE;
	    spcost = 0.0;
	    adjnds = new LinkedList();
	    this.colorOnce = colorOnce;
	}
	
	int sreg() { Util.assert(sregYet); return sreg; }
	void sreg(int val) {
	    Util.assert(!sregYet);
	    sreg = val;
	    sregYet = true;
	}

	RegColor regColor()  {
	    return regColor;
	}

	/** Sets color of this.
	    requires: (this can only be colored once and it has
	               already been colored) ==> rc == null
	    modifies: this
	    effects: if this can only be colored once, has already
	       been colored then no modification to this
	       else if rc == null then deletes color for this
	       else sets color for this to rc.
	*/
	void regColor(RegColor rc)  { 
	    if (colorOnce && regColorYet) {
		// attempts to erase color should do nothing 
		Util.assert(rc == null);
	    } else {
		regColor = rc;
		regColorYet = true;
	    }
	}
	
	
	// ( interference based on Muchnick, page 494.)
	boolean conflictsWith(WebRecord wr) {
	    return this.conflictsWith1D(wr) ||
		wr.conflictsWith1D(this);
	}

	// one directional conflict check (helper function) 
	// if this is live at a def in wr, returns true.
	boolean conflictsWith1D(WebRecord wr) {
	    for(Iterator ins=this.defs().iterator();ins.hasNext();){
		Instr d = (Instr) ins.next();
		Set l= liveTemps.getLiveAfter(d);
		if (l.contains(wr.temp())) 
		    return true;
	    }
	    return false;
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
	    super(true);
	    this.reg = reg;
	    regColor( (RegColor) regToColor.get(reg) );
	}
	public Set defs() { return (Set) regToDefs.getValues(reg); }
	public Set uses() { return (Set) regToUses.getValues(reg); }
	public Temp temp() { return reg; }

	boolean conflictsWith(WebRecord wr) {
	    if (wr instanceof RegWebRecord) {
		return true;
	    } else {
		return super.conflictsWith(wr);
	    }
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
	
	TempWebRecord(Temp symbol, Set defSet, Set useSet) {
	    super(false);
	    sym = symbol; defs = defSet; uses = useSet;
	    spill = false; disp = -1;
	}
	
	public Temp temp() { return sym; }
	public Set defs() { return Collections.unmodifiableSet(defs);}
	public Set uses() { return Collections.unmodifiableSet(uses);}

	public String toString() {
	    List a = (List) rfi.getRegAssignments(sym).iterator().next();
	    if (true) 
		return "< sym:"+sym+
		    ", defs:"+readable(defs)+
		    ", uses:"+readable(uses)+
		    ((a.size()==1)?", single-word":", multi-word")+
		    " >";
	    else 
		return "w:"+sym;
	}
    }

    class AdjMtx {
	// a Lower Triangular Matrix backed by a BitString.  Note that
	// for Lower Triangular Matrix, order of coordinate args is
	// insignificant (from p.o.v. of user). 

	final harpoon.Util.BitString bits;
	final int side;
	AdjMtx(List symReg) {
	    side = symReg.size();
	    bits = new harpoon.Util.BitString(side * side / 2);
	}
	boolean get(int x, int y) {
	    Util.assert(x != y);
	    return bits.get(convert(x,y));
	}
	void set(int x, int y, boolean b) {
	    Util.assert(x != y);
	    if (b) bits.set(convert(x,y)); 
	    else   bits.clear(convert(x,y));
	}
	private int convert(int x, int y) {
	    if (x < y) return offset(x) + y;
	    else       return offset(y) + x;
	}
	private int offset(int x) { return (x * (x + 1)) / 2; }
    }

}

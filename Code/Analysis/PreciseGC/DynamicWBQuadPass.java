// DynamicWBQuadPass.java, created Wed May 29 12:05:56 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.AllocationInformationMap.AllocationPropertiesImpl;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>DynamicWBQuadPass</code> inserts dynamic write barriers, where 
 * possible and identifies <code>SET</code>s and <code>ASET</code>s for which
 * static write barriers are unnecessary.
 *
 * Operates on <code>QuadSSA</code> and <code>QuadSSI</code>.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: DynamicWBQuadPass.java,v 1.1 2002-06-25 18:16:22 kkz Exp $
 */
public class DynamicWBQuadPass 
    extends harpoon.Analysis.Transformation.MethodMutator 
    implements WriteBarrierInserter.WriteBarrierAnalysis {

    final static boolean DEBUG1 = false;
    final static boolean DEBUG2 = false;
    final static boolean DEBUG3 = false;
    final static boolean DEBUG4 = false;

    private final HMethod clearBitHM;
    private final Map ignoreMap = new HashMap(); /* maps Codes to Sets */ 

    /** Creates a <code>DynamicWBQuadPass</code>. */
    public DynamicWBQuadPass(HCodeFactory parent, Linker linker) {
        super(parent);
	clearBitHM = linker.forName
	    ("harpoon.Runtime.PreciseGC.WriteBarrier").getMethod
	    ("clearBit", new HClass[] {linker.forName("java.lang.Object")});
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	//DEBUG4 = hc.getMethod().getName().equals("rehash");
	/*
	if (hc.getMethod().getName().equals("<init>") &&
	    hc.getMethod().getDeclaringClass().getName().equals("Branch")) {
	    hc.print(new java.io.PrintWriter(System.out), null);
	    System.exit(0);
	    } */
	AllocationInformationMap aim = 
	    (AllocationInformationMap) hc.getAllocationInformation();
	// code may not have any associated allocation information
	if (aim == null) {
	    aim = new AllocationInformationMap();
	    hc.setAllocationInformation(aim);
	}
	// first, collect allocations and Temps
	InitVisitor iv = new InitVisitor();
	for(Iterator it = hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    q.accept(iv);
	}
	FOOTER footer = ((HEADER) hc.getRootElement()).footer();
	// handle object allocations that are not arrays
	for (Iterator it = iv.NEWs.iterator(); it.hasNext(); ) {
	    NEW onew = (NEW) it.next();
	    if (DEBUG2) System.out.println("\nSET @ \t" + onew);
	    associateAllocationProperties(onew, aim, true);
	    ObjectAnalysisVisitor oav = 
		new ObjectAnalysisVisitor(onew, iv.allTemps, hc);
	    // insert clear after CALL to constructor
	    for (Iterator it2 = oav.CALLs.iterator(); it2.hasNext(); ) {
		CALL call = (CALL) it2.next();
		if (DEBUG2) System.out.println("\tCLEAR @ \t" + call);
		Temp dst = call.params(0);
		footer = insertClearAfter(call, dst, footer);
	    }
	}
	// set of Quads for this method for which write barriers
	// are not required
	Set ignore = new HashSet();
	// handle Object array allocations
	for (Iterator it = iv.ANEWs.iterator(); it.hasNext(); ) {
	    ANEW anew = (ANEW) it.next();
	    ArrayAnalysisVisitor aav = 
		new ArrayAnalysisVisitor(anew, iv.allTemps, hc);
	    if (!aav.ARRAYINITs.isEmpty() || !aav.ASETs.isEmpty()) {
		if (DEBUG3) {
		    System.out.println("ANEW: "+anew);
		    System.out.println("ARRAYINITs: ");
		    for(Iterator it2 = aav.ARRAYINITs.iterator(); 
			it2.hasNext(); )
			System.out.println("\t" + it2.next());
		    System.out.println("\nASETs: ");
		    for(Iterator it2 = aav.ASETs.iterator(); 
			it2.hasNext(); )
			System.out.println("\t" + it2.next());
		    System.out.println("\nneedClear: ");
		    for(Iterator it2 = aav.needClear.keySet().iterator(); 
			it2.hasNext(); ) {
			Edge key = (Edge) it2.next();
			System.out.println("\t" + key.from() + " -> " + 
					   key.to() + " (" +
					   aav.needClear.get(key) + ")");
		    }
		    System.out.println("\n");
		}
		for (Iterator it2 = aav.needClear.keySet().iterator();
		     it2.hasNext(); ) {
		    Edge key = (Edge) it2.next();
		    Temp dst = (Temp) aav.needClear.get(key);
		    footer = insertClear((Quad) key.to(), key, dst, footer);
		}
		//hc.print(new java.io.PrintWriter(System.out), null);
		associateAllocationProperties(anew, aim, true);
	    } else {
		associateAllocationProperties(anew, aim, false);
	    }
	    // compile ASETs that do not need write barriers
	    ignore.addAll(aav.ASETs);
	}
	// handle primitive array allocations
	for (Iterator it = iv.primitiveANEWs.iterator(); it.hasNext(); ) {
	    associateAllocationProperties((ANEW) it.next(), aim, false);
	}
	// handle constructors--any assignments to receiver object
	// should optimistically have write barriers removed
	if (hc.getMethod() instanceof HConstructor) {
	    ConstructorVisitor cv = new ConstructorVisitor(iv.allTemps, hc);
	    ignore.addAll(cv.SETs);
	}
	// add results to map
	ignoreMap.put(hc.getMethod(), ignore);
	return hc;
    }

    /* returns an unmodifiable <code>Set</code> of the <code>Quad</code>s
     * for which write barriers have been optimistically removed.
     */
    public Set getIgnoreSet(Code hc) {
	return Collections.unmodifiableSet((Set)ignoreMap.get(hc.getMethod()));
    }

    // modify the given AllocationInformationMap so that the 
    // AllocationProperties associated with Quad q has its
    // setDynamicWBFlag property set accordingly
    private void associateAllocationProperties(Quad q, 
					       AllocationInformationMap aim, 
					       boolean setDynamicWBFlag) {
	AllocationProperties ap = aim.query(q);
	if (ap == null)
	    ap = DefaultAllocationInformation.SINGLETON.query(q);
	aim.associate(q, new AllocationPropertiesImpl
		      (ap.hasInteriorPointers(),
		       ap.canBeStackAllocated(),
		       ap.canBeThreadAllocated(),
		       ap.makeHeap(),
		       ap.noSync(),
		       ap.allocationHeap(),
		       ap.actualClass(),
		       setDynamicWBFlag));
    }

    // insert a placeholder CALL after the given Quad to 
    // mark where we should clear the dynamic write 
    // barrier bit for the object to which t refers.
    // requires: that t refer to the object on the
    //           outgoing edge of q unless q is a SIGMA,
    //           in which case t needs to refer to the
    //           object on the incoming edge of the
    //           SIGMA, since the object reference may be 
    //           different for each outgoing edge of the 
    //           SIGMA. remapping is done here.
    private FOOTER insertClearAfter(Quad q, Temp t, FOOTER footer) {
	// special handling for SIGMAs
	if (q instanceof SIGMA) {
	    SIGMA sigma = (SIGMA) q;
	    for(int i = 0; i < sigma.nextLength(); i++) {
		Temp renamed = null;
		for(int j = 0; j < sigma.numSigmas(); j++) {
		    if (sigma.src(j).equals(t)) {
			renamed = sigma.dst(j, i);
			break;
		    }
		}
		if (renamed == null)
		    footer = insertClear(sigma, sigma.nextEdge(i), t, footer);
		else
		    footer = insertClear
			(sigma, sigma.nextEdge(i), renamed, footer);
	    }
	} else {
	    for(int i = 0; i < q.nextLength(); i++)
		footer = insertClear(q, q.nextEdge(i), t, footer);
	}
	return footer;
    }

    // helper function to insert placeholder CALL on given Edge
    private FOOTER insertClear(Quad q, Edge e, Temp t, FOOTER f) {
	QuadFactory qf = q.getFactory();
	Temp clearexT = new Temp(qf.tempFactory(), "clearex");
	Quad q1 = new CALL(qf, q, clearBitHM, 
			   new Temp[] { t }, null, clearexT,
			   false, false, new Temp[0]);
	Quad q2 = new THROW(qf, q, clearexT);
	Quad.addEdge((Quad) e.from(), e.which_succ(), q1, 0);
	Quad.addEdge(q1, 0, (Quad) e.to(), e.which_pred());
	Quad.addEdge(q1, 1, q2, 0);
	return f.attach(q2, 0);
    }

    private static class InitVisitor extends
	harpoon.IR.Quads.QuadVisitor {
	
	final Set allTemps = new HashSet();
	final Set ANEWs = new HashSet();
	final Set primitiveANEWs = new HashSet();
	final Set NEWs = new HashSet();

	public void visit(ANEW q) {
	    if (!q.hclass().getComponentType().isPrimitive())
		ANEWs.add(q);
	    else
		primitiveANEWs.add(q);
	    visit((Quad)q);
	}
	
	public void visit(NEW q) {
	    assert !q.hclass().isPrimitive();
	    NEWs.add(q);
	    visit((Quad)q);
	}

	public void visit(Quad q) {
	    allTemps.addAll(q.useC());
	    allTemps.addAll(q.defC());
	}
    }

    private static class ConstructorVisitor extends AliasAnalysisVisitor {
	final Set SETs = new HashSet();

	ConstructorVisitor(Set allTemps, Code hc) {
	    super(allTemps);
	    HEADER header = (HEADER) hc.getRootElement();
	    METHOD method = (METHOD) header.next(1);
	    // initialize dataflow fact for METHOD
	    Set aliases = new HashSet();
	    aliases.add(method.params(0));
	    // perform analysis
	    analyze(method, aliases);
	    // find SETs of fields of the receiver object
	    for(Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof SET) {
		    SET set = (SET) q;
		    Set aliases2 = get(q.prevEdge(0));
		    if (aliases2.contains(set.objectref())) {
			SETs.add(set);
		    }
		}
	    }
	}
    }

    private static class ObjectAnalysisVisitor extends AliasAnalysisVisitor {
	private final NEW alloc;
	final Set CALLs = new HashSet();

	ObjectAnalysisVisitor(NEW alloc, Set allTemps, Code hc) {
	    super(allTemps);
	    this.alloc = alloc;
	    // initialize dataflow fact for NEW
	    Set aliases = new HashSet();
	    aliases.add(alloc.dst());
	    // perform analysis
	    analyze(alloc, aliases);
	    // find CALLs to constructor for this object allocation site
	    for(Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof CALL) {
		    CALL call = (CALL) q;
		    HMethod hm = (HMethod) call.method();
		    if (hm instanceof HConstructor &&
			hm.getDeclaringClass().compareTo(alloc.hclass()) == 0) {
			Set aliases2 = get(q.prevEdge(0));
			if (aliases2.contains(call.params(0))) {
			    CALLs.add(call);
			}
		    }
		}
	    }
	}
	    
	public void visit(NEW q) {
	    if (q == alloc) {
		Set aliases = new HashSet(get(q.prevEdge(0)));
		aliases.add(q.dst());
		raiseValue(q.nextEdge(0), aliases);
	    } else {
		visit((Quad)q);
	    }
	}
    }
    
    private static class ArrayAnalysisVisitor extends AliasAnalysisVisitor {
	private final ANEW alloc;
	final Set ARRAYINITs = new HashSet();
	final Set ASETs = new HashSet();
	final Map needClear = new HashMap();

	ArrayAnalysisVisitor(ANEW alloc, Set allTemps, Code hc) {
	    super(allTemps);
	    this.alloc = alloc;
	    // initialize dataflow fact for ANEW
	    Set aliases = new HashSet();
	    aliases.add(alloc.dst());
	    // begin analysis
	    analyze(alloc, aliases);
	    // find array assignments for this array 
	    // allocation site using depth-first search
	    markQuads(alloc, null, get(alloc.prevEdge(0)), new HashSet());
	}
	
	// depth-first search algorithm for marking Quads
	// that are SETs, ASETs, or require a clear
	// requires: that lastQ be the most recent ASET or
	//           ARRAYINIT at which lastAlias referred
	//           to an alias of the object allocated at
	//           the given allocation site
	private void markQuads(Quad q, Edge prevE, Set aliases, Set visited) {
	    if (DEBUG4) System.out.println(q);
	    // already working on this Quad
	    if (visited.contains(q)) return;
	    if (!(q instanceof PHI)) visited.add(q);
	    // find assignments for this allocation site
	    if (q instanceof ASET) {
		Temp objectref = ((ASET) q).objectref();
		if (aliases.contains(objectref))
		    ASETs.add(q);
	    } else if (q instanceof ARRAYINIT) {
		Temp objectref = ((ARRAYINIT) q).objectref();
		if (aliases.contains(objectref))
		    ARRAYINITs.add(q);
	    }
	    // find Quads where a clear is needed
	    if (q instanceof RETURN || q instanceof THROW) {
		needClear.put(prevE, aliases.iterator().next());
	    } else {
		for (int i = 0; i < q.nextLength(); i++) {
		    Edge nextE = q.nextEdge(i);
		    Set next = get(nextE);
		    if (next.isEmpty()) {
			needClear.put(prevE, aliases.iterator().next());
		    } else {
			markQuads(q.next(i), nextE, next, visited);
		    }
		}
	    }
	}

	public void visit(ANEW q) {
	    if (q == alloc) {
		Set aliases = new HashSet(get(q.prevEdge(0)));
		aliases.add(q.dst());
		raiseValue(q.nextEdge(0), aliases);
	    } else {
		visit((Quad)q);
	    }
	}
    }

    private static class AliasAnalysisVisitor extends
	harpoon.IR.Quads.QuadVisitor {

	protected final Set allTemps;
	protected final Worklist toDo = new WorkSet();

	// map of CFGEdges to aliases (Sets of Temps)
	protected final Map EdgeToTemps = new HashMap();
	
	AliasAnalysisVisitor(Set allTemps) {
	    this.allTemps = allTemps;
	}

	// requires: that start has outgoing arity of 1
	protected void analyze(Quad start, Set aliases) {
	    assert start.nextLength() == 1;
	    // initialize dataflow facts
	    EdgeToTemps.put(start.nextEdge(0), aliases);
	    // initialize to-do list
	    toDo.push(start.next(0));
	    // go, gadget, go!
	    while(!toDo.isEmpty()) {
		Quad q = (Quad) toDo.pull();
		if (DEBUG1) System.out.println(q);
		q.accept(this);
	    }
	}

	public void visit(CALL q) {
	    assert q.prevLength() == 1;
	    Set aliases = get(q.prevEdge(0));
	    // handle normal edge
	    Temp retval = q.retval();
	    if (retval != null)
		aliases.remove(retval);
	    handleSIGMAEdge(q, new HashSet(aliases), 0);
	    // handle exception edge
	    Temp retex = q.retex();
	    if (retex != null) {
		aliases.remove(retex);
		// if retex == null, then the CALL has only
		// one outgoing edge (only happens in quad-
		// with-try), so only handle exception edge 
		// if retex != null
		handleSIGMAEdge(q, new HashSet(aliases), 1);
	    }
	}

	public void visit(FOOTER q) { /* do nothing */ }

	public void visit(MOVE q) {
	    assert q.prevLength() == 1 && q.nextLength() == 1;
	    Set aliases = new HashSet(get(q.prevEdge(0)));
	    if (aliases.contains(q.src()))
		aliases.add(q.dst());
	    else
		aliases.remove(q.dst());
	    raiseValue(q.nextEdge(0), aliases);
	}

	public void visit (PHI q) {
	    // start with edge 0
	    Set aliases = get(q.prevEdge(0));
	    Set renamed = new HashSet(aliases);
	    for (int j = 0; j < q.numPhis(); j++) {
		// rename aliases as needed
		Temp src = q.src(j, 0);
		// perform check on original set
		// in case an alias has multiple
		// renames
		if (aliases.contains(src)) {
		    renamed.remove(src);
		    renamed.add(q.dst(j));
		}
	    }
	    // handle rest of edges (starting w/ edge 1)
	    for (int i = 1; i < q.arity(); i++) {
		aliases = get(q.prevEdge(i));
		Set renamed2 = new HashSet(aliases);
		for(int j = 0; j < q.numPhis(); j++) {
		    // rename aliases as needed
		    Temp src = q.src(j, i);
		    // perform check on original set
		    // in case an alias has multiple
		    // renames
		    if (aliases.contains(src)) {
			renamed2.remove(src);
			renamed2.add(q.dst(j));
		    }
		}
		// since this is a must analysis, 
		// we use set intersection: keep
		// only if present in both sets
		renamed.retainAll(renamed2);
	    }
	    raiseValue(q.nextEdge(0), renamed);
	}

	public void visit(Quad q) {
	    assert q.prevLength() == 1 && q.nextLength() == 1;
	    Set aliases = new HashSet(get(q.prevEdge(0)));
	    // remove redefined aliases, if any
	    aliases.removeAll(q.defC());
	    raiseValue(q.nextEdge(0), aliases);
	}

	public void visit(SIGMA q) {
	    assert q.prevLength() == 1;
	    Set aliases = get(q.prevEdge(0));
	    // iterate over successor edges
	    for(int i = 0; i < q.nextLength(); i++)
		handleSIGMAEdge(q, new HashSet(aliases), i);
	}

	// retreive dataflow fact for CFGEdge e
	protected Set get(CFGEdge e) {
	    Set V = (Set) EdgeToTemps.get(e);
	    if (V == null) {
		V = new HashSet(allTemps);
		EdgeToTemps.put(e, V);
	    }
	    if (DEBUG1) {
		System.out.print("\tget: ");
		if (V.size() == allTemps.size()) {
		    System.out.println("*");
		} else {
		    for(Iterator it = V.iterator(); it.hasNext(); ) {
			System.out.print(it.next());
			if (it.hasNext())
			    System.out.print(", ");
		    }
		    System.out.println("");
		}
	    }
	    return V;
	}

	// update dataflow fact for CFGEdge e
	protected void raiseValue(CFGEdge e, Set raised) {
	    if (DEBUG1) {
		System.out.print("\traiseValue: ");
		for(Iterator it = raised.iterator(); it.hasNext(); ) {
		    System.out.print(it.next());
		    if (it.hasNext())
			System.out.print(", ");
		}
		System.out.println("");
	    }
	    // add successor to to-do list, if necessary
	    if (!raised.equals((Set) EdgeToTemps.get(e))) {
		EdgeToTemps.put(e, raised);
		toDo.push(e.to());
	    }
	}

	// handleSIGMAEdge handles the index'th outgoing edge of the
	// SIGMA q, given the set of aliases on the incoming edge
	// requires: aliases be a Set of Temps
	// modifies: aliases
	protected void handleSIGMAEdge(SIGMA q, Set aliases, int index) {
	    Set insert = new HashSet();
	    Set remove = new HashSet();
	    // iterate over sigma functions
	    for(int j = 0; j < q.numSigmas(); j++) {
		Temp src = q.src(j);
		if (aliases.contains(src)) {
		    insert.add(q.dst(j, index));
		    remove.add(src);
		}
	    }
	    aliases.removeAll(remove);
	    aliases.addAll(insert);
	    raiseValue(q.nextEdge(index), aliases);
	}
    }
}

// DynamicWBInserter.java, created Tue Jul 16 19:13:41 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.AllocationInformationMap.AllocationPropertiesImpl;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>DynamicWBInserter</code> inserts instructions where needed to
 * set and clear the per-object bit for dynamic write barriers.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: DynamicWBInserter.java,v 1.2 2004-02-08 03:20:07 cananian Exp $ */
public class DynamicWBInserter extends 
    harpoon.Analysis.Transformation.MethodMutator {
    
    final static boolean DEBUG1 = false;
    final static boolean DEBUG2 = false;

    private final HMethod clearBitHM;
    private final Map ignoreMap = new HashMap(); /* maps Codes to Sets */ 
    private final DynamicWBAnalysis dwba;

    /** Creates a <code>DynamicWBInserter</code>. */
    public DynamicWBInserter(HCodeFactory parent, Linker linker,
			     ClassHierarchy ch, DynamicWBAnalysis dwba) {
        super(parent);
	clearBitHM = linker.forName
	    ("harpoon.Runtime.PreciseGC.WriteBarrier").getMethod
	    ("clearBit", new HClass[] {linker.forName("java.lang.Object")});
	this.dwba = dwba;
	// force passes to run; or else the dwba information will be wrong
	for(Iterator it=ch.callableMethods().iterator(); it.hasNext(); )
	    parent.convert((HMethod)it.next());
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	Map aem = input.ancestorElementMap();
	// fetch allocation information
	AllocationInformationMap aim = 
	    (AllocationInformationMap) hc.getAllocationInformation();
	// code may not have any associated allocation information
	if (aim == null) {
	    aim = new AllocationInformationMap();
	    hc.setAllocationInformation(aim);
	}
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	FOOTER footer = ((HEADER) hc.getRootElement()).footer();
	for (int i=0; i<allquads.length; i++) {
	    Quad q = allquads[i];
	    int kind = q.kind();
	    if (kind == QuadKind.NEW || kind == QuadKind.ANEW) {
		Quad ancestor = (Quad) aem.get(q);
		assert ancestor != null : "cannot find ancestor for "+q;
		if (dwba.areWBsRemoved(ancestor)) {
		    if (DEBUG1) System.out.println(q);
		    // insert set instructions
		    associateAllocationProperties(q, aim, true);
		    // insert clear instructions
		    BitClearAnalysis bca = new BitClearAnalysis(hc, q, true);
		    Map call2exception = new HashMap();
		    Map edge2temp = bca.needClear;
		    for(Object edgeO : edge2temp.keySet()) {
			Edge edge = (Edge) edgeO;
			Temp dst = (Temp) edge2temp.get(edge);
			//footer=insertClear((Quad)edge.to(), edge, dst, footer);
			insertClear((Quad)edge.to(), edge, dst, call2exception);
		    }
		    footer = insertTHROW(footer, call2exception);
		} else {
		    associateAllocationProperties(q, aim, false);
		}
	    }
	}
	return hc;
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

    // helper function to insert placeholder CALL on given Edge
    private FOOTER insertClear(Quad q, Edge e, Temp t, FOOTER f) {
	if (DEBUG1) System.out.println("\tclear before "+q);
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

    // helper function to insert placeholder CALL on given Edge
    private void insertClear(Quad q, Edge e, Temp t, Map call2exception) {
	if (DEBUG1) System.out.println("\tclear before "+q);
	QuadFactory qf = q.getFactory();
	Temp clearexT = new Temp(qf.tempFactory(), "clearex");
	Quad q1 = new CALL(qf, q, clearBitHM, 
			   new Temp[] { t }, null, clearexT,
			   false, false, new Temp[0]);
	Quad.addEdge((Quad) e.from(), e.which_succ(), q1, 0);
	Quad.addEdge(q1, 0, (Quad) e.to(), e.which_pred());
	call2exception.put(q1, clearexT);
    }

    // helper function to insert combined THROW for all CALLs
    private FOOTER insertTHROW(FOOTER footer, Map call2exception) {
	Iterator it=call2exception.keySet().iterator();
	if (it.hasNext()) {
	    // at least one CALL inserted
	    CALL call = (CALL) it.next();
	    QuadFactory qf = call.getFactory();
	    if (it.hasNext()) {
		// more than one CALL inserted, need PHI
		Temp clearexT = new Temp(qf.tempFactory(), "clearex");
		PHI phi = new PHI(qf, call, new Temp[] 
				  { clearexT }, new Temp[][] 
				  {new Temp[] {(Temp) call2exception.get(call)}},
				  1);
		Quad.addEdge(call, 1, phi, 0);
		// process rest of CALLs
		for(int i = 1; it.hasNext(); i++) {
		    call = (CALL) it.next();
		    phi = phi.grow(new Temp[] 
				   { (Temp) call2exception.get(call) }, i);
		    Quad.addEdge(call, 1, phi, i);
		}
		// THROW comes after PHI
		THROW thr = new THROW(qf, call, clearexT);
		Quad.addEdge(phi, 0, thr, 0);
		return footer.attach(thr, 0);
	    } else {
		// THROW comes after CALL to write barrier
		THROW thr = new THROW(qf, call, (Temp) call2exception.get(call));
		Quad.addEdge(call, 1, thr, 0);
		return footer.attach(thr, 0);
	    }
	}
	return footer;
    }

    private static class BitClearAnalysis extends PointsToQuadVisitor {

	private final boolean mobject;
	private final Quad alloc;
	final Map needClear = new HashMap();

	BitClearAnalysis(Code code, Quad alloc, boolean mobject) {
	    super(code);
	    this.alloc = alloc; // target allocation
	    this.mobject = mobject; // ignore once definitely not mobject?
	    // run alias analysis
	    analyze(Collections.EMPTY_SET);
	    // locate edges on which clear instructions need to be added
	    markQuads(alloc, null, get(alloc.prevEdge(0)), new HashSet());
	}

	// depth-first search algorithm for marking edges that require
	// a clear.
	// requires: q is the current <code>Quad</code>.
	//           prevE is the most recently traversed 
	//             <code>Edge</code>.
	//           aliases contains the <code>Temp</code>s that must
	//             point to the allocated object on the incoming
	//             <code>Edge</code> of q.
	//           visited contains <code>Quad</code>s that do not 
	//             need to be examined further.
	private void markQuads(Quad q, Edge prevE, Set aliases, Set visited) {
	    if (DEBUG2) System.out.println(q);
	    // already working on this Quad
	    if (visited.contains(q)) return;
	    int kind = q.kind();
	    if (kind != QuadKind.PHI) visited.add(q);
	    // find Quads where a clear is needed
	    if (kind == QuadKind.RETURN || 
		(kind == QuadKind.THROW &&
		 !((THROW)q).throwable().name().startsWith("wbex"))) {
		needClear.put(prevE, aliases.iterator().next());
	    } else {
		for (int i = 0; i < q.nextLength(); i++) {
		    Edge nextE = q.nextEdge(i);
		    Set next = get(nextE);
		    if (next.isEmpty()) {
			if (kind != QuadKind.PHI || 
			    (((PHI)q).numPhis() == 1 &&
			     !((PHI)q).dst(0).name().startsWith("wbex")))
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
	    } else if (mobject) {
		raiseValue(q.nextEdge(0), Collections.EMPTY_SET);
	    } else {
		visit((Quad)q);
	    }
	}

	public void visit(NEW q) {
	    if (q == alloc) {
		Set aliases = new HashSet(get(q.prevEdge(0)));
		aliases.add(q.dst());
		raiseValue(q.nextEdge(0), aliases);
	    } else if (mobject) {
		raiseValue(q.nextEdge(0), Collections.EMPTY_SET);
	    } else {
		visit((Quad)q);
	    }
	}
    }

    /** A <code>DynamicWBAnalysis</code> identifies <code>NEW</code>
     *  and <code>ANEW</code> <code>Quad</code>s for which
     *  corresponding write barriers are removed. */
    public interface DynamicWBAnalysis {

	/** Returns true if this <code>Quad</code> is a
	 *  <code>NEW</code> or <code>ANEW</code> for which write *
	 *  barriers have been removed. */
	public boolean areWBsRemoved(Quad q);

    }
}

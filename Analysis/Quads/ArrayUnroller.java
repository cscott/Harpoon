// ArrayUnroller.java, created Thu Jun 14 22:36:15 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Analysis.Loops.Loops;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
/**
 * <code>ArrayUnroller</code> unrolls loops containing arrays so that
 * <code>CacheEquivalence</code> can make larger equivalence sets.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayUnroller.java,v 1.5 2002-08-30 22:38:26 cananian Exp $
 */
public final class ArrayUnroller
    extends harpoon.Analysis.Transformation.MethodMutator {
    private static final int CACHE_LINE_SIZE = 32; /* bytes */
    
    /** Creates a <code>ArrayUnroller</code>. */
    public ArrayUnroller(HCodeFactory parent) {
	// we take in NoSSx, and output NoSSx.
	super(harpoon.IR.Quads.QuadNoSSA.codeFactory(parent));
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode ahc = input.ancestorHCode();
	HCode hc = input.hcode();
	// find loops;
        // only interested in leaf loops (with no nested loops)
	for (Iterator it=loopIterator(new LoopFinder(ahc)); it.hasNext(); ) {
	    Loops loop = (Loops) it.next();
	    if (loop.parentLoop()==null) continue; // skip top-level pseudoloop
	    if (loop.nestedLoops().size()>0) continue; // skip
	    // this is a leaf loop.  look for array refs.
	    boolean seen=false; int width=0;
	    for (Iterator it2=loop.loopIncElements().iterator();
		 it2.hasNext(); ) {
		Quad q = (Quad) it2.next();
		if (q instanceof AGET) {
		    seen=true; width=updateWidth(width,((AGET)q).type());
		} else if (q instanceof ASET) {
		    seen=true; width=updateWidth(width,((ASET)q).type());
		}
	    }
	    if (!seen) continue; // no array references in this loop.
	    unrollOne(input, loop, CACHE_LINE_SIZE/width);
	}
	harpoon.Analysis.Quads.Unreachable.prune(hc);
	return hc;
    }
    /** find the minimum array component width */
    private static int updateWidth(int width, HClass type) {
	int nwidth=0;
	if (type==HClass.Boolean || type==HClass.Byte) nwidth=1;
	else if (type==HClass.Char || type==HClass.Short) nwidth=2;
	else if (type==HClass.Int || type==HClass.Float) nwidth=4;
	else if (type==HClass.Long || type==HClass.Double) nwidth=8;
	else if (!type.isPrimitive()) nwidth=4;
	else assert false;
	return (width==0) ? nwidth : Math.min(width, nwidth);
    }
    /** iterate over loop and all children of loop recursively. */
    static Iterator loopIterator(Loops root) {
	final Stack s = new Stack(); s.push(root);
	return new UnmodifiableIterator() {
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Object next() {
		if (s.empty()) throw new NoSuchElementException();
		Loops loop = (Loops) s.pop();
		// push children on stack before returning.
		s.addAll(loop.nestedLoops());
		return loop;
	    }
	};
    }
    // the real mccoy:
    private void unrollOne(HCodeAndMaps input, Loops loop, int ntimes) {
	// step 1: copy the nodes to make a loop L' with header h'
	//         and back edges si'->h'
	Map copies[] = new Map[ntimes];
	copies[0] = input.elementMap();
	for (int i=1; i<ntimes; i++)
	    copies[i] = copy(input, loop);
	// step 2: change all the back edges in L from si->h to si->h'
	// step 3: change all the back edges in L' from si'->h' to si'->h
	for (int i=0; i<ntimes; i++) {
	    for (Iterator it=loop.loopBackEdges().iterator(); it.hasNext(); ) {
		Edge e = (Edge) it.next();
		Quad.addEdge((Quad)copies[i].get(e.from()),
			     e.which_succ(),
			     (Quad)copies[(i+1)%ntimes].get(e.to()),
			     e.which_pred());
	    }
	}
	// make PHIs for the exit edges.
	for (Iterator it=loop.loopExitEdges().iterator(); it.hasNext(); ) {
	    Edge e = (Edge) it.next();
	    QuadFactory qf = ((Quad)copies[0].get(e.from())).getFactory();
	    PHI phi = new PHI(qf, e.from(), new Temp[0], ntimes);
	    Quad.addEdge(phi, 0, (Quad) copies[0].get(e.to()), e.which_pred());
	    for (int i=0; i<ntimes; i++)
		Quad.addEdge((Quad)copies[i].get(e.from()), e.which_succ(),
			     phi, i);
	}
	// make stub PHIs for unused entrance edges
	for (Iterator it=loop.loopEntranceEdges().iterator(); it.hasNext(); ) {
	    Edge e = (Edge) it.next();
	    QuadFactory qf = ((Quad)copies[0].get(e.to())).getFactory();
	    for (int i=1; i<ntimes; i++) {
		PHI phi = new PHI(qf, e.to(), new Temp[0], 0);
		Quad.addEdge(phi, 0,
			     (Quad)copies[i].get(e.to()),
			     e.which_pred());
	    }
	}
	// we may have unconnected entrances if more than one header.
	assert loop.loopEntrances().size()==1;
	// done.
    }
    Map copy(HCodeAndMaps input, Loops l) {
	Map m = new HashMap();
	// clone all elements.
	for (Iterator it=l.loopIncElements().iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    Quad qq = (Quad) input.elementMap().get(q);
	    Quad nq = (Quad) qq.clone();
	    m.put(q, nq); // ancestor quad to newly cloned quad.
	}
	// clone all interior edges.
	for (Iterator it=l.loopIncElements().iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    for (int i=0; i<q.nextLength(); i++) {
		Edge e = q.nextEdge(i);
		assert m.containsKey(e.from());
		if (m.containsKey(e.to()))
		    Quad.addEdge((Quad)m.get(e.from()), e.which_succ(),
				 (Quad)m.get(e.to()), e.which_pred());
	    }
	}
	return m;
    }
}

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
import net.cscott.jutil.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
/**
 * <code>ArrayUnroller</code> unrolls loops containing arrays so that
 * <code>CacheEquivalence</code> can make larger equivalence sets.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayUnroller.java,v 1.8 2004-02-08 03:20:09 cananian Exp $
 */
public final class ArrayUnroller
    extends harpoon.Analysis.Transformation.MethodMutator<Quad> {
    private static final int CACHE_LINE_SIZE = 32; /* bytes */
    
    /** Creates a <code>ArrayUnroller</code>. */
    public ArrayUnroller(HCodeFactory parent) {
	// we take in NoSSx, and output NoSSx.
	super(harpoon.IR.Quads.QuadNoSSA.codeFactory(parent));
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> ahc = input.ancestorHCode();
	HCode<Quad> hc = input.hcode();
	// find loops;
        // only interested in leaf loops (with no nested loops)
	for (Iterator<Loops> it=loopIterator(new LoopFinder(ahc));
	     it.hasNext(); ) {
	    Loops loop = it.next();
	    if (loop.parentLoop()==null) continue; // skip top-level pseudoloop
	    if (loop.nestedLoops().size()>0) continue; // skip
	    // this is a leaf loop.  look for array refs.
	    boolean seen=false; int width=0;
	    for (Object qO : loop.loopIncElements()) {
		Quad q = (Quad) qO;
		if (q instanceof AGET) {
		    seen=true; width=updateWidth(width,((AGET)q).type());
		} else if (q instanceof ASET) {
		    seen=true; width=updateWidth(width,((ASET)q).type());
		}
	    }
	    if (!seen) continue; // no array references in this loop.
	    unrollOne(input, loop, CACHE_LINE_SIZE/width);
	}
	harpoon.Analysis.Quads.Unreachable.prune((harpoon.IR.Quads.Code)hc);
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
    static Iterator<Loops> loopIterator(Loops root) {
	final Stack<Loops> s = new Stack<Loops>(); s.push(root);
	return new UnmodifiableIterator<Loops>() {
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Loops next() {
		if (s.empty()) throw new NoSuchElementException();
		Loops loop = s.pop();
		// push children on stack before returning.
		s.addAll(loop.nestedLoops());
		return loop;
	    }
	};
    }
    // the real mccoy:
    private void unrollOne(HCodeAndMaps<Quad> input, Loops loop, int ntimes) {
	// step 1: copy the nodes to make a loop L' with header h'
	//         and back edges si'->h'
	List<Map<Quad,Quad>> copies = new ArrayList<Map<Quad,Quad>>(ntimes);
	copies.add(input.elementMap());
	for (int i=1; i<ntimes; i++)
	    copies.add(copy(input, loop));
	// step 2: change all the back edges in L from si->h to si->h'
	// step 3: change all the back edges in L' from si'->h' to si'->h
	for (int i=0; i<ntimes; i++) {
	    for (Object eO : loop.loopBackEdges()) {
		Edge e = (Edge) eO;
		Quad.addEdge(copies.get(i).get(e.from()),
			     e.which_succ(),
			     copies.get((i+1)%ntimes).get(e.to()),
			     e.which_pred());
	    }
	}
	// make PHIs for the exit edges.
	for (Object eO : loop.loopExitEdges()) {
	    Edge e = (Edge) eO;
	    QuadFactory qf = copies.get(0).get(e.from()).getFactory();
	    PHI phi = new PHI(qf, e.from(), new Temp[0], ntimes);
	    Quad.addEdge(phi, 0, copies.get(0).get(e.to()), e.which_pred());
	    for (int i=0; i<ntimes; i++)
		Quad.addEdge(copies.get(i).get(e.from()), e.which_succ(),
			     phi, i);
	}
	// make stub PHIs for unused entrance edges
	for (Object eO : loop.loopEntranceEdges()) {
	    Edge e = (Edge) eO;
	    QuadFactory qf = copies.get(0).get(e.to()).getFactory();
	    for (int i=1; i<ntimes; i++) {
		PHI phi = new PHI(qf, e.to(), new Temp[0], 0);
		Quad.addEdge(phi, 0,
			     copies.get(i).get(e.to()),
			     e.which_pred());
	    }
	}
	// we may have unconnected entrances if more than one header.
	assert loop.loopEntrances().size()==1;
	// done.
    }
    Map<Quad,Quad> copy(HCodeAndMaps<Quad> input, Loops l) {
	Map<Quad,Quad> m = new HashMap<Quad,Quad>();
	// clone all elements.
	for (Object qO : l.loopIncElements()) {
	    Quad q = (Quad) qO;
	    Quad qq = input.elementMap().get(q);
	    Quad nq = qq.clone();
	    m.put(q, nq); // ancestor quad to newly cloned quad.
	}
	// clone all interior edges.
	for (Object qO : l.loopIncElements()) {
	    Quad q = (Quad) qO;
	    for (int i=0; i<q.nextLength(); i++) {
		Edge e = q.nextEdge(i);
		assert m.containsKey(e.from());
		if (m.containsKey(e.to()))
		    Quad.addEdge(m.get(e.from()), e.which_succ(),
				 m.get(e.to()), e.which_pred());
	    }
	}
	return m;
    }
}

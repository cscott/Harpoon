// OneLevelInliner.java, created Tue Jul 26 10:16:03 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads.DeepInliner;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.MOVE;

import harpoon.IR.Quads.QuadVisitor;

import harpoon.Analysis.Quads.Unreachable;

import harpoon.Temp.Temp;

import harpoon.Util.Util;

/**
   <code>OneLevelInliner</code> contains the code that inlines a single
   <code>CALL</code> instruction.  This class is used by
   <code>DeepInliner</code>, but can also be used separately, for
   inlining call paths of length 1.

   <p>To perform inlining, use the static method
   <code>OneLevelInliner.inline</code>.

 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: OneLevelInliner.java,v 1.1 2005-08-09 22:40:35 salcianu Exp $ */
public class OneLevelInliner {

    private OneLevelInliner(CALL cs, HMethod callee, CachingCodeFactory ccf) {
	this.lineInfo = cs;

	Code callerCode = cs.getFactory().getParent();

	// 1. Clone the code of the callee.
	HCode  origHCode  = ccf.convert(callee);
	HEADER origHeader   = (HEADER) origHCode.getRootElement();
	HEADER clonedHeader = (HEADER) Quad.clone(cs.getFactory(), origHeader);
	computeOrig2Cloned(origHeader, clonedHeader);

	// 2. Replace the CALL instruction with code for the parameter
	// passing; the exit of this code sequence goes into the
	// cloned calle code from 1.
	addEntrySequence(cs, (METHOD) (clonedHeader.next(1)));
	
	// 3. Modify the RETURNs and THROWs from the cloned copy to
	// "flow" to the program point right after the CALL.
	modifyReturnAndThrow(cs, clonedHeader);

	// 4. Do some post-inlining cleanup
	// Note: in the past, Flex would crash without this step (unclear why).
	Unreachable.prune(callerCode);
    }

    private final Map<Quad,Quad> origQuad2clonedQuad = new HashMap<Quad,Quad>();

    private void computeOrig2Cloned(Quad origQuad, Quad clonedQuad) {
	if(origQuad2clonedQuad.containsKey(origQuad)) return;

	origQuad2clonedQuad.put(origQuad, clonedQuad);

	Quad[] origNext = origQuad.next();
	Quad[] clonedNext = clonedQuad.next();

	assert origNext.length == clonedNext.length : " Possible error in Quad.clone()";

	for(int i = 0; i < origNext.length; i++)
	    computeOrig2Cloned(origNext[i], clonedNext[i]);
    }


    private final HCodeElement lineInfo;


    private void addEntrySequence(CALL cs, METHOD qm) {
	// The new instructions we create will keep the line info of the inlined CALL
	HCodeElement lineInfo = cs;

	Quad nop = new NOP(cs.getFactory(), lineInfo);

	movePredEdges(cs, nop);

	assert cs.paramsLength() == qm.paramsLength() : 
	    " different nb. of parameters between CALL and METHOD";
	
	Quad previous = nop;
	for(int i = 0; i < cs.paramsLength(); i++) {
	    Temp formal = qm.params(i);
	    Temp actual = cs.params(i);
	    // emulate the Java parameter passing semantics
	    MOVE move = new MOVE(cs.getFactory(), lineInfo, formal, actual);
	    Quad.addEdge(previous, 0, move, 0);
	    previous = move;
	}

	// the edge pointing to the first instruction of the method body
	Edge edge = qm.nextEdge(0);
	Quad.addEdge(previous, 0, edge.to(), edge.which_pred());
    }


    // For any predecessor pred of oldq, replace the arc pred->oldq
    // with old->newq. 
    private void movePredEdges(Quad oldq, Quad newq) {
	Edge[] edges = oldq.prevEdge();
	for(int i = 0; i < edges.length; i++) {
	    Edge edge = edges[i];
	    Quad.addEdge(edge.from(), edge.which_succ(),
			 newq, edge.which_pred());
	}
    }

    private void modifyReturnAndThrow(final CALL cs, final HEADER header) {
	final Set<Quad> retSet   = new HashSet<Quad>();
	final Set<Quad> throwSet = new HashSet<Quad>();

	QuadVisitor qVis = new QuadVisitor() {
	    public void visit(Quad q) {}

	    public void visit(RETURN q) {
		Quad replacement = (cs.retval() != null) ?
		    ((Quad) new MOVE
			(cs.getFactory(), lineInfo, cs.retval(), q.retval())) :
		    ((Quad) new NOP(cs.getFactory(), lineInfo));

		// make the predecessors of q point to replace
		movePredEdges(q, replacement);
		retSet.add(replacement);
	    }

	    public void visit(THROW q) {
		Quad replacement = (cs.retex() != null) ?
		    ((Quad) new MOVE
		     (cs.getFactory(), lineInfo, cs.retex(), q.throwable())) :
		    ((Quad) new NOP(cs.getFactory(), lineInfo));
		
		// make the predecessors of q point to replace
		movePredEdges(q, replacement);
		throwSet.add(replacement);
	    }
	};

	// process all quads from the cloned code of the callee
	// (these are the values of hcam.elementMap()
	for(Quad newQuad : origQuad2clonedQuad.values()) {
	    newQuad.accept(qVis);
	}

	// Replacements of the callee RETURNs flow into normal (#0)
	// predecessor of the inlined CALL.
	PHI retPhi = bringTogether(cs, retSet);
	Quad.addEdge(retPhi, 0, cs.next(0), cs.nextEdge(0).which_pred());

	// Replacements of the callee THROWs flow into exceptional (#1)
	// predecessor of the inlined CALL.
	PHI throwPhi = bringTogether(cs, throwSet);
	Quad.addEdge(throwPhi, 0, cs.next(1), cs.nextEdge(1).which_pred());
    }

    private PHI bringTogether(CALL cs, Set<Quad> quads) {
	PHI phi = new PHI(cs.getFactory(), lineInfo, new Temp[0], quads.size());
	int edge = 0;
	for(Quad quad : quads) {
	    Quad.addEdge(quad, 0, phi, edge++);
	}
	return phi;
    }


    /** 
	@param cs     The call instruction to be inlined.

	@param callee The callee whose body should replace
	<code>cs</code>.  In case of a virtual call with a single
	possible callee, this callee is not obvious from the structure
	of the call (it may require a full-program call graph).
	Therefore, we require that the callee is passed explicitly.

	@param ccf The caching code factory that generates the code
	for <code>callee</code> and for the caller of
	<code>callee</code> (the method that <code>cs</code> belongs
	to).  We perform the inlining by mutating the cached code for
	the caller.  Must produce <code>QuadNoSSA</code> or
	<code>QuadRSSx</code> IR (otehr IRs are too complex to
	generate code for).

	@return A map that assigns to each quad from the code of
	<code>callee</code> (as provided by <code>ccf</code>), the
	corresponding quad from the copy of <code>callee</code>'s body
	that replaces <code>cs</code>.  */
    public static Map<Quad,Quad> inline(CALL cs, HMethod callee, CachingCodeFactory ccf) {
	assert 
	    ccf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename) ||
	    ccf.getCodeName().equals(harpoon.IR.Quads.QuadRSSx.codename) :
	    "ccf must be QuadNoSSA or QuadRSSx";

	OneLevelInliner oli = new OneLevelInliner(cs, callee, ccf);
	return oli.origQuad2clonedQuad;
    }

}
